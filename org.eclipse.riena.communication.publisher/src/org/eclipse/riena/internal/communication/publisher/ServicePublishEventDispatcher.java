/*******************************************************************************
 * Copyright (c) 2007, 2008 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.internal.communication.publisher;

import static org.eclipse.riena.communication.core.publisher.RSDPublisherProperties.PROP_IS_REMOTE;
import static org.eclipse.riena.communication.core.publisher.RSDPublisherProperties.PROP_REMOTE_PATH;
import static org.eclipse.riena.communication.core.publisher.RSDPublisherProperties.PROP_REMOTE_PROTOCOL;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.riena.communication.core.RemoteServiceDescription;
import org.eclipse.riena.communication.core.publisher.IServicePublishEventDispatcher;
import org.eclipse.riena.communication.core.publisher.IServicePublisher;
import org.eclipse.riena.communication.core.publisher.RSDPublisherProperties;
import org.eclipse.riena.communication.core.util.CommunicationUtil;

import org.eclipse.equinox.log.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * 
 * @author Alexander Ziegler
 * @author Christian Campo
 * 
 * @deprecated
 */
public class ServicePublishEventDispatcher implements IServicePublishEventDispatcher {

	public static final String FILTER_REMOTE = "(&(" + PROP_IS_REMOTE + "=true)(" + PROP_REMOTE_PROTOCOL + "=*)" + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private Map<String, RemoteServiceDescription> rsDescs;
	private Map<String, IServicePublisher> servicePublishers;

	private BundleContext context;

	private final static Logger LOGGER = Activator.getDefault()
			.getLogger(ServicePublishEventDispatcher.class.getName());

	public ServicePublishEventDispatcher(BundleContext context) {
		super();
		this.context = context;
		rsDescs = new HashMap<String, RemoteServiceDescription>();
		servicePublishers = new HashMap<String, IServicePublisher>(3);
	}

	public void start() {
		update();
	}

	protected synchronized void update() {
		ServiceReference[] serviceReferences = null;
		try {
			serviceReferences = context.getAllServiceReferences(null, FILTER_REMOTE);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		if (serviceReferences != null) {

			for (ServiceReference serviceReference : serviceReferences) {
				if (!isPublished(serviceReference)) {
					publish(serviceReference);
				}
			}
		}
	}

	private boolean isPublished(ServiceReference serviceRef) {
		String remoteType = CommunicationUtil.accessProperty(serviceRef
				.getProperty(RSDPublisherProperties.PROP_REMOTE_PROTOCOL), null);
		String path = CommunicationUtil.accessProperty(serviceRef.getProperty(PROP_REMOTE_PATH), null);
		RemoteServiceDescription rsDesc = rsDescs.get(remoteType + "::" + path); //$NON-NLS-1$
		if (rsDesc == null) {
			return false;
		}
		return rsDesc.getState() == RemoteServiceDescription.State.REGISTERED;
	}

	public void bind(IServicePublisher publisher) {
		servicePublishers.put(publisher.getProtocol(), publisher);
		LOGGER.log(LogService.LOG_DEBUG, "servicePublish=" + publisher.getProtocol() //$NON-NLS-1$
				+ " REGISTER...publishing all services that were waiting for him"); //$NON-NLS-1$
		// check for services which are missing a publisher

		update();
	}

	public void unbind(IServicePublisher publisher) {
		String protocol = publisher.getProtocol();
		LOGGER.log(LogService.LOG_DEBUG, "servicePublish=" + publisher.getProtocol() //$NON-NLS-1$
				+ " UNREGISTER...unpublishing all its services"); //$NON-NLS-1$
		// unregister all web services for this type

		for (RemoteServiceDescription rsDesc : rsDescs.values()) {
			if (protocol.equals(rsDesc.getProtocol())) {
				unpublish(rsDesc);
			}
		}
		servicePublishers.remove(protocol);
	}

	protected void publish(ServiceReference serviceRef) {
		if (servicePublishers.size() == 0) {
			return;
		}
		synchronized (rsDescs) {
			try {
				Object service = context.getService(serviceRef);
				ServiceHooksProxy handler = new ServiceHooksProxy(service);
				// create remote service description
				String[] interfaces = (String[]) serviceRef.getProperty(Constants.OBJECTCLASS);
				assert interfaces.length == 1 : "OSGi service registrations only with one interface supported"; //$NON-NLS-1$
				String interfaceName = interfaces[0];
				Class interfaceClazz = serviceRef.getBundle().loadClass(interfaceName);
				service = Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class[] { interfaceClazz },
						handler);
				RemoteServiceDescription rsDesc = new RemoteServiceDescription(serviceRef, service, interfaceClazz);
				handler.setRemoteServiceDescription(rsDesc);
				RemoteServiceDescription rsDescFound = rsDescs.get(rsDesc.getProtocol() + "::" + rsDesc.getPath()); //$NON-NLS-1$
				if (rsDescFound != null && rsDescFound.getState() == RemoteServiceDescription.State.REGISTERED) {
					LOGGER.log(LogService.LOG_WARNING, "A service endpoint with path=[" + rsDesc.getPath() //$NON-NLS-1$
							+ "] and remoteType=[" + rsDesc.getProtocol() + "] already published... ignored"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}

				if (rsDescFound == null) {
					if (rsDesc.getPath() == null) {
						LOGGER.log(LogService.LOG_WARNING, "no path for service: " + service.toString() //$NON-NLS-1$
								+ " Service not published remote"); //$NON-NLS-1$
						return;
					}
					if (!servicePublishers.containsKey(rsDesc.getProtocol())) {
						LOGGER.log(LogService.LOG_DEBUG, "no publisher found for protocol " + rsDesc.getProtocol()); //$NON-NLS-1$
						return;
					}
					rsDescs.put(rsDesc.getProtocol() + "::" + rsDesc.getPath(), rsDesc); //$NON-NLS-1$
					LOGGER.log(LogService.LOG_DEBUG, "service endpoints count: " + rsDescs.size()); //$NON-NLS-1$

				} else if (rsDescFound.getState() == RemoteServiceDescription.State.UNREGISTERED) {
					rsDesc = rsDescFound;
				}

				publish(rsDesc, handler);
			} catch (ClassNotFoundException e) {
				LOGGER.log(LogService.LOG_DEBUG,
						"Could not load class for remote service interface for service reference " + serviceRef, e); //$NON-NLS-1$
			}
		}

	}

	private void publish(RemoteServiceDescription rsDesc, ServiceHooksProxy handler) {
		IServicePublisher publisher = servicePublishers.get(rsDesc.getProtocol());
		if (publisher == null) {
			return;
		}
		String url = publisher.publishService(rsDesc);
		// set full URL under which the service is available
		rsDesc.setURL(url);
		handler.setMessageContextAccessor(publisher.getMessageContextAccessor());

		rsDesc.setState(RemoteServiceDescription.State.REGISTERED);

	}

	// @remote
	public RemoteServiceDescription[] getAllServices() {
		RemoteServiceDescription[] result = new RemoteServiceDescription[rsDescs.size()];
		synchronized (rsDescs) {
			rsDescs.values().toArray(result);
		}
		return result;
	}

	protected void unpublish(ServiceReference serviceRef) {
		synchronized (rsDescs) {
			try {

				String path = CommunicationUtil.accessProperty(serviceRef.getProperty(PROP_REMOTE_PATH), null);
				String remoteType = CommunicationUtil
						.accessProperty(serviceRef.getProperty(PROP_REMOTE_PROTOCOL), null);
				RemoteServiceDescription toRemove = null;
				for (RemoteServiceDescription rsDesc : rsDescs.values()) {
					if (rsDesc.getPath().equals(path) && rsDesc.getProtocol().equals(remoteType)) {
						toRemove = rsDesc;
						break;
					}
				}
				if (toRemove != null) {
					rsDescs.remove(remoteType + "::" + path); //$NON-NLS-1$
					if (servicePublishers.containsKey(toRemove.getProtocol())) {
						unpublish(toRemove);
					}
					// TODO notify all discovery services about this
				}
			} finally {
				context.ungetService(serviceRef);
			}
			LOGGER.log(LogService.LOG_DEBUG, "service endpoints count: " + rsDescs.size()); //$NON-NLS-1$
		}
	}

	private void unpublish(RemoteServiceDescription rsDesc) {
		IServicePublisher publisher = servicePublishers.get(rsDesc.getProtocol());
		if (publisher == null) {
			return;
		}

		publisher.unpublishService(rsDesc);

		rsDesc.setState(RemoteServiceDescription.State.UNREGISTERED);
	}

	public void stop() {
		synchronized (rsDescs) {
			for (RemoteServiceDescription rsDesc : rsDescs.values()) {
				if (servicePublishers.containsKey(rsDesc.getProtocol())) {
					unpublish(rsDesc);
				}
				rsDesc.dispose();
			}
			rsDescs.clear();
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2007, 2014 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.internal.demo.client;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.Plugin;

import org.eclipse.riena.communication.core.factory.Register;
import org.eclipse.riena.demo.common.ICustomerService;
import org.eclipse.riena.demo.common.IEmailService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.riena.demo.client.customer"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Register.remoteProxy(ICustomerService.class).usingUrl("http://${riena.host}/hessian/CustomerServiceWS") //$NON-NLS-1$
				.withProtocol("hessian").andStart(context); //$NON-NLS-1$

		Register.remoteProxy(IEmailService.class).usingUrl("http://${riena.host}/hessian/EmailServiceWS") //$NON-NLS-1$
				.withProtocol("hessian").andStart(context); //$NON-NLS-1$

		//		Inject.service(ICustomerService.class).into(new RemoteClientTest()).andStart(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}

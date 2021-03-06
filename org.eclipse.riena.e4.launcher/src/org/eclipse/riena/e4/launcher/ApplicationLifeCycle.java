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
package org.eclipse.riena.e4.launcher;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import org.eclipse.riena.core.wire.Wire;
import org.eclipse.riena.e4.launcher.exception.E4UncaughtExceptionHandler;
import org.eclipse.riena.e4.launcher.listener.RienaNavigationObserver;
import org.eclipse.riena.e4.launcher.part.RienaPartHelper;
import org.eclipse.riena.e4.launcher.security.LoginHelper;
import org.eclipse.riena.navigation.IApplicationNode;
import org.eclipse.riena.navigation.ui.application.IApplicationModelCreator;
import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
import org.eclipse.riena.navigation.ui.swt.application.LoginNonActivityTimer.ILoginExecutor;
import org.eclipse.riena.navigation.ui.swt.application.SwtApplication;
import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;

/**
 * ApplicationLifeCycle building the bridge between Eclipse 4 and Riena.
 */
@SuppressWarnings("restriction")
public class ApplicationLifeCycle {
	private static final String MODEL_CREATORS_EXT_POINT_SUFFIX = ".applicationModelCreators"; //$NON-NLS-1$

	@Inject
	private IExtensionRegistry extensionRegistry;

	@Inject
	private Logger logger;

	@Inject
	private IEclipseContext eclipseContext;

	@PostContextCreate
	public void initRienaApplicationNode() {
		final E4UncaughtExceptionHandler exceptioHandler = new E4UncaughtExceptionHandler().install();
		Wire.instance(exceptioHandler).andStart(Activator.getDefault().getBundleContext());
		eclipseContext.set(StatusReporter.class, exceptioHandler);

		// if there is some login configured, ask for login and remember the extension in the app context
		// later we will need this extension for the (in)activity login timer
		eclipseContext.set(ILoginExecutor.class, ContextInjectionFactory.make(LoginHelper.class, eclipseContext).checkLogin());

		IApplicationModelCreator applicationModelCreator = getApplicationModelCreatorFromExtension();
		if (applicationModelCreator == null) {
			applicationModelCreator = new SwtApplication();
		}
		final IApplicationNode applicationNode = createModel(applicationModelCreator);

		eclipseContext.set(IApplicationNode.class, applicationNode);
		applicationModelCreator.initApplicationNode(applicationNode);

		// installDefaultBinding()
		// preShutdown()

		observeRienaNavigation(applicationNode);
		new InjectSwtViewBindingDelegate().bind(new ApplicationController(applicationNode));
	}

	/**
	 * Creates the Riena application model represented by an instance of {@link IApplicationNode}
	 */
	private IApplicationNode createModel(final IApplicationModelCreator creator) {
		Wire.instance(creator).andStart(Activator.getDefault().getBundleContext());
		// call configuration hook
		creator.configure();
		return creator.createModel();
	}

	private IApplicationModelCreator getApplicationModelCreatorFromExtension() {
		final String pluginId = org.eclipse.riena.internal.navigation.ui.Activator.PLUGIN_ID;
		for (final IConfigurationElement configElement : extensionRegistry.getConfigurationElementsFor(pluginId + MODEL_CREATORS_EXT_POINT_SUFFIX)) {
			try {
				return (IApplicationModelCreator) configElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (final CoreException coreException) {
				logger.error(coreException);
			}
		}
		return null;
	}

	private void observeRienaNavigation(final IApplicationNode applicationNode) {
		eclipseContext.set(RienaPartHelper.class, ContextInjectionFactory.make(RienaPartHelper.class, eclipseContext));
		ContextInjectionFactory.make(RienaNavigationObserver.class, eclipseContext).install(applicationNode);
	}

}

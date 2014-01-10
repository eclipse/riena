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
package org.eclipse.riena.security.services.itest.authentication;

import java.net.URL;

import javax.security.auth.login.LoginException;

import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;

import org.eclipse.riena.communication.core.IRemoteServiceRegistration;
import org.eclipse.riena.communication.core.factory.Register;
import org.eclipse.riena.core.test.RienaTestCase;
import org.eclipse.riena.core.test.collect.IntegrationTestCase;
import org.eclipse.riena.internal.tests.Activator;
import org.eclipse.riena.security.authentication.callbackhandler.TestLocalCallbackHandler;
import org.eclipse.riena.security.common.authentication.IAuthenticationService;

/**
 * TODO JavaDoc
 */
@IntegrationTestCase
public class AuthenticationLoginModuleITest extends RienaTestCase {

	private IRemoteServiceRegistration authenticationService;

	private static final String JAAS_CONFIG_FILE = "config/sample_jaas.config"; //$NON-NLS-1$

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		startBundles("org\\.eclipse\\.riena.communication.core", null);
		startBundles("org\\.eclipse\\.riena.communication.factory.hessian", null);
		startBundles("org\\.eclipse\\.riena.communication.registry", null);
		stopBundles("org\\.eclipse\\.riena.example.client", null);
		authenticationService = Register.remoteProxy(IAuthenticationService.class)
				.usingUrl("http://localhost:8080/hessian/AuthenticationService").withProtocol("hessian")
				.andStart(Activator.getDefault().getContext());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		authenticationService.unregister();
	}

	public void testRemoteLogin() throws LoginException {
		// set the userid,password that the authenticating callback handler will set (as user input)
		TestLocalCallbackHandler.setSuppliedCredentials("testuser", "testpass");

		final URL configUrl = Activator.getDefault().getContext().getBundle().getEntry(JAAS_CONFIG_FILE);
		final ILoginContext secureContext = LoginContextFactory.createContext("RemoteTest", configUrl);

		secureContext.login();

		assertNotNull(secureContext.getSubject());
		assertNotNull(secureContext.getSubject().getPrincipals());
		assertTrue(secureContext.getSubject().getPrincipals().size() > 0);
		System.out.println("subject:" + secureContext.getSubject());
		System.out.println("login in sucessful");

		secureContext.logout();
		System.out.println("logoff sucessful");
	}
}

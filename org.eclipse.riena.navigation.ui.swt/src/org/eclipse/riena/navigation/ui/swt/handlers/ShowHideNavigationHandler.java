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
package org.eclipse.riena.navigation.ui.swt.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.riena.navigation.ui.swt.IApplicationUtility;
import org.eclipse.riena.navigation.ui.swt.facades.NavigationFacade;

/**
 * Show or hide the Navigation if the navigation fast view feature is enabled.
 * 
 * @since 4.0
 */
public class ShowHideNavigationHandler extends AbstractHandler {

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IApplicationUtility applicationUtility = NavigationFacade.getDefault().getApplicationUtility();
		if (applicationUtility.isNavigationFastViewEnabled()) {
			final boolean visible = applicationUtility.isNavigationVisible();
			applicationUtility.setNavigationVisible(!visible);
		}
		return null;
	}
}

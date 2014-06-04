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
package org.eclipse.riena.demo.client.rcp.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.riena.internal.ui.swt.facades.WorkbenchFacade;
import org.eclipse.riena.ui.swt.RienaMessageDialog;

/**
 * 
 */
public class DummyHandler extends AbstractHandler {

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		RienaMessageDialog.openInformation(WorkbenchFacade.getInstance().getActiveShell(), getTitle(), getMessage());

		return null;

	}

	protected String getTitle() {
		return "Dummy"; //$NON-NLS-1$
	}

	protected String getMessage() {
		return "Not implemented!"; //$NON-NLS-1$
	}

}

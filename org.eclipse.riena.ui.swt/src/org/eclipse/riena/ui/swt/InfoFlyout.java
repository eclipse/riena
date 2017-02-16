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
package org.eclipse.riena.ui.swt;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.ui.core.resource.IconSize;
import org.eclipse.riena.ui.swt.utils.IPropertyNameProvider;
import org.eclipse.riena.ui.swt.utils.UIControlsFactory;

/**
 * This widget implements a message that pops-up on top of the SubModuleView. No user interaction is possible and it closes after a few seconds.
 * <p>
 * It is possible to set a message and an icon.
 * 
 * @since 2.0
 */
public abstract class InfoFlyout implements IPropertyNameProvider {

	private String bindingId;
	/**
	 * @since 5.0
	 */
	protected int positionCorrectionY;

	/**
	 * See {@link UIControlsFactory#createInfoFlyout(Composite)}.
	 * 
	 * @since 3.0
	 */
	protected InfoFlyout() {
		// instantiate via UIControlsFactory
	}

	public abstract void openFlyout();

	public abstract void setMessage(final String message);

	public abstract void setIcon(final String icon);

	/**
	 * @since 6.2
	 */
	public abstract void setIcon(final String icon, IconSize iconSize);

	public final void setPropertyName(final String bindingId) {
		this.bindingId = bindingId;
	}

	public final String getPropertyName() {
		return bindingId;
	}

	/**
	 * @param positionCorrectionY
	 *            the positionCorrectionY to set
	 * @since 5.0
	 */
	public void setPositionCorrectionY(final int positionCorrectionY) {
		this.positionCorrectionY = positionCorrectionY;
	}

	/**
	 * Lets the InfoFlyout wait until the last one is closed.
	 */
	public abstract void waitForClosing();

}
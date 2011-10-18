/*******************************************************************************
 * Copyright (c) 2007, 2011 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.ui.ridgets.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.riena.ui.core.marker.HiddenMarker;
import org.eclipse.riena.ui.swt.utils.SwtUtilities;

/**
 * Ridget for an SWT control.
 */
public abstract class AbstractSWTRidget extends AbstractSWTWidgetRidget {

	/**
	 * The key of the SWT data property that identifies the (top) composite of a
	 * sub-module view.
	 */
	private static final String IS_SUB_MODULE_VIEW_COMPOSITE = "isSubModuleViewComposite"; //$NON-NLS-1$
	private final FocusManager focusManager;
	private boolean focusable;

	/**
	 * @since 3.0
	 */
	public FocusManager getFocusManager() {
		return focusManager;
	}

	public AbstractSWTRidget() {
		focusManager = new FocusManager(this);
		focusable = true;
	}

	@Override
	public Control getUIControl() {
		return (Control) super.getUIControl();
	}

	@Override
	public final void requestFocus() {
		if (isFocusable()) {
			if (getUIControl() != null) {
				final Control control = getUIControl();
				// if setFocus() fails because e.g. the parent is disabled
				// the state has to be saved, so it can be restored in submoduleController.unblockView()
				setRetryRequestFocus(!control.setFocus());
			}
		}
	}

	@Override
	public boolean hasFocus() {
		final Control control = getUIControl();
		if (!SwtUtilities.isDisposed(control)) {
			return control.isFocusControl();
		}
		return false;
	}

	@Override
	public final boolean isFocusable() {
		return focusable;
	}

	@Override
	public final void setFocusable(final boolean focusable) {
		if (this.focusable != focusable) {
			this.focusable = focusable;
		}
	}

	public boolean isVisible() {
		// check for "hidden.marker". This marker overrules any other visibility rule
		if (!getMarkersOfType(HiddenMarker.class).isEmpty()) {
			return false;
		}

		if (!SwtUtilities.isDisposed(getUIControl())) {
			// the swt control is bound
			if (isChildOfSubModuleView(getUIControl())) {
				return isControlVisible(getUIControl());
			} else {
				return getUIControl().isVisible();
			}
		}
		// control is not bound
		return savedVisibleState;
	}

	/**
	 * Returns whether the given control is the (top) composite of a sub-module
	 * view.
	 * 
	 * @param uiControl
	 *            UI control
	 * @return {@code true} if control is composite of a sub-module; otherwise
	 *         {@code false}
	 */
	private boolean isSubModuleViewComposite(final Control uiControl) {

		if (!(uiControl instanceof Composite)) {
			return false;
		}

		if (uiControl.getData(IS_SUB_MODULE_VIEW_COMPOSITE) instanceof Boolean) {
			if (((Boolean) uiControl.getData(IS_SUB_MODULE_VIEW_COMPOSITE))) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Returns whether the given control is a child of the (top) composite of a
	 * sub-module view.
	 * 
	 * @param uiControl
	 *            UI control
	 * @return {@code true} if control is child of a sub-module; otherwise
	 *         {@code false}
	 */
	private boolean isChildOfSubModuleView(final Control uiControl) {

		if (uiControl.getVisible()) {
			final Composite parent = uiControl.getParent();
			if (parent == null) {
				return false;
			}
			if (isSubModuleViewComposite(parent)) {
				return true;
			}
			return isChildOfSubModuleView(parent);
		} else {
			return false;
		}

	}

	/**
	 * Returns whether the given control is visible or invisible.
	 * <p>
	 * Similar to the SWT method isVisible of the class {@link Control} this
	 * method also checks if the parent composite are also visible. But this
	 * checks end at the top composite of a sub-module view.
	 * 
	 * @param uiControl
	 *            UI control
	 * @return {@code true} if control is visible; otherwise {@code false}
	 */
	private boolean isControlVisible(final Control uiControl) {

		if (uiControl.getVisible()) {
			final Composite parent = uiControl.getParent();
			if (parent == null) {
				return true;
			}
			if (isSubModuleViewComposite(parent)) {
				return true;
			}
			return isControlVisible(parent);
		} else {
			return false;
		}

	}

	@Override
	protected void unbindUIControl() {
		// save the state
		savedVisibleState = isVisible();
	}

	// helping methods
	// ////////////////

	/**
	 * Adds listeners to the <tt>uiControl</tt> after it was bound to the
	 * ridget.
	 */
	@Override
	protected final void installListeners() {
		super.installListeners();
		final Control control = getUIControl();
		if (control != null) {
			focusManager.addListeners(control);
		}
	}

	/**
	 * Removes listeners from the <tt>uiControl</tt> when it is about to be
	 * unbound from the ridget.
	 */
	@Override
	protected final void uninstallListeners() {
		final Control control = getUIControl();
		if (control != null) {
			focusManager.removeListeners(control);
		}
		super.uninstallListeners();
	}

	@Override
	protected final void updateEnabled() {
		if (getUIControl() != null) {
			getUIControl().setEnabled(isEnabled());
		}
	}

	@Override
	protected final void updateToolTip() {
		if (getUIControl() != null) {
			getUIControl().setToolTipText(getToolTipText());
		}
	}

}

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
package org.eclipse.riena.ui.ridgets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.log.LogService;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.equinox.log.Logger;

import org.eclipse.riena.core.Log4r;
import org.eclipse.riena.core.RienaStatus;
import org.eclipse.riena.internal.ui.ridgets.Activator;

/**
 * Default implementation of the {@link IComplexRidget} interface.
 * <p>
 * Implementors may extend this class instead from starting from scratch.
 */
public abstract class AbstractCompositeRidget extends AbstractRidget implements IComplexRidget {

	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), AbstractCompositeRidget.class);

	/**
	 * @see ComplexRidgetResolver
	 * @see SimpleRidgetResolver
	 */
	private IRidgetResolver ridgetResolver = new ComplexRidgetResolver();

	private final PropertyChangeListener propertyChangeListener;
	private final Map<String, IRidget> ridgets;

	private Object uiControl;
	private boolean markedHidden;
	private boolean enabled;
	private boolean configured = false;

	private String toolTip = null;

	private final RidgetToStatuslineSubscriber ridgetToStatuslineSubscriber = new RidgetToStatuslineSubscriber();

	/**
	 * Constructor
	 */
	public AbstractCompositeRidget() {
		super();
		propertyChangeListener = new PropertyChangeHandler();
		ridgets = new HashMap<String, IRidget>();
		enabled = true;
	}

	public void addRidget(final String id, final IRidget ridget) {
		final IRidget oldRidget = getRidgetResolver().addRidget(id, ridget, this, ridgets);
		if (null == oldRidget) {
			ridget.addPropertyChangeListener(propertyChangeListener);
		} else {
			ridgetToStatuslineSubscriber.removeRidget(oldRidget);
		}
		ridgetToStatuslineSubscriber.addRidget(ridget);
	}

	/**
	 * @since 5.0
	 */
	public boolean removeRidget(final String id) {
		ridgetToStatuslineSubscriber.removeRidget(getRidget(id));
		return getRidgetResolver().removeRidget(id, ridgets) != null;
	}

	/**
	 * @since 5.0
	 */
	public void setStatuslineToShowMarkerMessages(final IStatuslineRidget statuslineToShowMarkerMessages) {
		ridgetToStatuslineSubscriber.setStatuslineToShowMarkerMessages(statuslineToShowMarkerMessages, getRidgets());
	}

	/**
	 * @since 4.0
	 */
	public String idOfRidget(final IRidget ridget) {
		for (final String ridgetId : ridgets.keySet()) {
			if (ridgets.get(ridgetId) == ridget) {
				return ridgetId;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation is empty. Implementors may extend to configure (sub)-ridgets injected into this {@link IComplexRidget}.
	 */
	public void configureRidgets() {
		// implementors may extend
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Always returns null. Implementors should override.
	 */
	public String getID() {
		return null;
	}

	public <R extends IRidget> R getRidget(final String id) {
		return (R) getRidgetResolver().getRidget(id, ridgets);
	}

	/**
	 * @since 2.0
	 */
	public <R extends IRidget> R getRidget(final Class<R> ridgetClazz, final String id) {
		R ridget = getRidget(id);

		if (ridget != null) {
			if (!ridgetClazz.isInstance(ridget)) {
				String logMessage = "getRidget(Class, String): Actual ridget (id: "; //$NON-NLS-1$
				logMessage += id;
				logMessage += ")"; //$NON-NLS-1$
				logMessage += " in this composite (id: "; //$NON-NLS-1$
				logMessage += getID();
				logMessage += ")"; //$NON-NLS-1$
				logMessage += " is not of desired type: ridget is "; //$NON-NLS-1$
				logMessage += ridget.getClass().getName();
				logMessage += ", desired is "; //$NON-NLS-1$
				logMessage += ridgetClazz.getName();
				LOGGER.log(LogService.LOG_ERROR, logMessage);
			}
			return ridget;
		}
		if (allowRidgetCreation()) {
			try {
				if (ridgetClazz.isInterface() || Modifier.isAbstract(ridgetClazz.getModifiers())) {
					final Class<R> mappedRidgetClazz = (Class<R>) ClassRidgetMapper.getInstance().getRidgetClass(ridgetClazz);
					if (mappedRidgetClazz != null) {
						ridget = mappedRidgetClazz.newInstance();
					}
					Assert.isNotNull(ridget,
							"Could not find a corresponding implementation for " + ridgetClazz.getName() + " in " + ClassRidgetMapper.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					ridget = ridgetClazz.newInstance();
				}
			} catch (final InstantiationException e) {
				throw new RuntimeException(e);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			addRidget(id, ridget);
		}

		return ridget;
	}

	/**
	 * @since 3.0
	 */
	protected boolean allowRidgetCreation() {
		return RienaStatus.isTest() || !SubModuleUtils.isPrepareView();
	}

	public Collection<? extends IRidget> getRidgets() {
		return new ArrayList<IRidget>(ridgets.values());
	}

	public String getToolTipText() {
		return toolTip;
	}

	public Object getUIControl() {
		return uiControl;
	}

	public boolean hasFocus() {
		final Collection<? extends IRidget> myRidgets = getRidgets();
		for (final IRidget ridget : myRidgets) {
			if (ridget.hasFocus()) {
				return true;
			}
		}
		return false;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isFocusable() {
		return null != getFirstFocusableRidget();
	}

	public boolean isVisible() {
		// check for "hidden.marker". This marker overrules any other visibility rule
		if (markedHidden) {
			return false;
		}

		if (getUIControl() != null) {
			// the swt control is bound
			return isUIControlVisible();
		}
		// control is not bound
		return savedVisibleState;
	}

	public void requestFocus() {
		final IRidget firstFocusableRidget = getFirstFocusableRidget();
		if (null != firstFocusableRidget) {
			firstFocusableRidget.requestFocus();
		}
	}

	private IRidget getFirstFocusableRidget() {
		for (final IRidget ridget : getRidgets()) {
			if (ridget.isFocusable() && ridget.isEnabled()) {
				if (ridget instanceof IMarkableRidget && ((IMarkableRidget) ridget).isOutputOnly()) {
					continue;
				}

				if (ridget instanceof ILabelRidget) {
					continue;
				}

				if (ridget instanceof IStatusMeterRidget) {
					continue;
				}
				return ridget;
			}
		}
		return null;
	}

	public void setEnabled(final boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			updateEnabled();
			if (enabled) {
				updateMarkersOfChildRidgets();
			}
		}
	}

	private void updateMarkersOfChildRidgets() {
		for (final IRidget ridget : getRidgets()) {
			if (ridget instanceof IMarkableRidget) {
				((IMarkableRidget) ridget).updateMarkers();
			}
		}
	}

	public void setFocusable(final boolean focusable) {
		for (final IRidget ridget : getRidgets()) {
			ridget.setFocusable(focusable);
		}
	}

	public void setToolTipText(final String toolTipText) {
		final String oldValue = toolTip;
		toolTip = toolTipText;
		updateToolTipText();
		firePropertyChange(IRidget.PROPERTY_TOOLTIP, oldValue, toolTip);
	}

	public void setUIControl(final Object uiControl) {
		checkUIControl(uiControl);
		unbindUIControl();
		// save state
		this.savedVisibleState = getUIControl() != null ? isUIControlVisible() : savedVisibleState;
		this.uiControl = uiControl;
		updateVisible();
		updateEnabled();
		updateToolTipText();
		bindUIControl();
	}

	public void setVisible(final boolean visible) {
		if (this.markedHidden == visible) {
			this.markedHidden = !visible;
			updateVisible();
		}
	}

	/**
	 * Bind the current <tt>uiControl</tt> to the ridget.
	 * <p>
	 * Implementors must call {@link #getUIControl()} to obtain the current control. If the control is non-null they must do whatever necessary to bind it to
	 * the ridget.
	 * 
	 * @since 1.2
	 */
	protected void bindUIControl() {
		// implementors should overwrite
	}

	/**
	 * Performs checks on the control about to be bound by this ridget.
	 * <p>
	 * Implementors must make sure the given <tt>uiControl</tt> has the expected type.
	 * 
	 * @param uiControl
	 *            a {@link Widget} instance or null
	 * @throws BindingException
	 *             if the <tt>uiControl</tt> fails the check
	 * @since 1.2
	 */
	protected void checkUIControl(final Object uiControl) {
		// implementors should overwrite
	}

	@Override
	public void updateFromModel() {
		super.updateFromModel();
		//delegate to inner ridgets
		for (final IRidget ridget : ridgets.values()) {
			ridget.updateFromModel();
		}
	}

	/**
	 * Returns true if the ridget is marked as hidden (visible = false). Note: this reflects the ridget state, not the control state. The ridget may not have
	 * control yet.
	 * 
	 * @since 2.0
	 */
	protected final boolean isMarkedHidden() {
		return markedHidden;
	}

	/**
	 * Returns true if the control of this ridget is visible, false otherwise. This default implementation always returns true and should be overridden by
	 * subclasses.
	 * <p>
	 * Note: this will only be called when the UI control is know to be non-null.
	 */
	protected boolean isUIControlVisible() {
		return true;
	}

	/**
	 * Remove all ridgets contained in this instance.
	 * 
	 * @since 1.2
	 */
	protected final void removeRidgets() {
		for (final IRidget r : getRidgets()) {
			ridgetToStatuslineSubscriber.removeRidget(r);
		}
		ridgets.clear();
	}

	/**
	 * Unbind the current <tt>uiControl</tt> from the ridget.
	 * <p>
	 * Implementors ensure they dispose the control-to-ridget binding and dispose any data structures that are not necessary in an unbound state.
	 * 
	 * @since 1.2
	 */
	protected void unbindUIControl() {
		// implementors should overwrite
	}

	/**
	 * Updates the enabled state of the complex UI control (and of the UI controls it contains). This default implementation does nothing and should be
	 * overridden by subclasses.
	 */
	protected void updateEnabled() {
		// empty default implementation
	}

	/**
	 * Does nothing by default.
	 * <p>
	 * Subclasses should override to update the tooltip(s) of their controls in an appropriate way.
	 */
	protected void updateToolTipText() {
		// does nothing
	}

	/**
	 * Updates the visibility of the complex UI control (and of the UI controls it contains). This default implementation does nothing and should be overridden
	 * by subclasses.
	 */
	protected void updateVisible() {
		// empty default implementation
	}

	// helping classes 
	//////////////////

	/**
	 * Forwards a property change event fired by a (sub) ridget contained in this composite ridget, to the listeners of the composite ridget.
	 */
	private class PropertyChangeHandler implements PropertyChangeListener {
		public void propertyChange(final PropertyChangeEvent evt) {
			propertyChangeSupport.firePropertyChange(evt);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @since 4.0
	 */
	public void setConfigured(final boolean configured) {
		this.configured = configured;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @since 4.0
	 * 
	 */
	public boolean isConfigured() {
		return configured;
	}

	/**
	 * @return the ridgetResolver
	 * @since 5.0
	 */
	public IRidgetResolver getRidgetResolver() {
		return ridgetResolver;
	}

	/**
	 * @param ridgetResolver
	 *            the ridgetResolver to set
	 * @since 5.0
	 */
	public void setRidgetResolver(final IRidgetResolver ridgetResolver) {
		this.ridgetResolver = ridgetResolver;
	}

}

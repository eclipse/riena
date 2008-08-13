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
package org.eclipse.riena.internal.ui.ridgets.swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.Assert;
import org.eclipse.riena.core.marker.IMarker;
import org.eclipse.riena.ui.core.marker.MandatoryMarker;
import org.eclipse.riena.ui.ridgets.IMultipleChoiceRidget;
import org.eclipse.riena.ui.ridgets.util.beans.ListBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Ridget for a {@link ChoiceComposite} widget with multiple selection.
 */
public class MultipleChoiceRidget extends AbstractMarkableRidget implements IMultipleChoiceRidget {

	private static final IChangeListener DUMMY_LISTENER = new IChangeListener() {
		public void handleChange(ChangeEvent event) {
			// workaround for proper updateModelToTarget() behavior with
			// POLICY_ON_REQUEST
		}
	};

	private final WritableList optionsObservable;
	private final WritableList selectionObservable;

	private DataBindingContext dbc;
	private Binding optionsBinding;
	private Binding selectionBinding;
	private String[] optionLabels;

	public MultipleChoiceRidget() {
		optionsObservable = new WritableList();
		selectionObservable = new WritableList();
		selectionObservable.addChangeListener(new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				updateMarkers();
			}
		});
	}

	@Override
	protected void bindUIControl() {
		if (optionsBinding != null) {
			createChildren(getUIControl());
		}
	}

	@Override
	protected void checkUIControl(Object uiControl) {
		AbstractSWTRidget.assertType(uiControl, ChoiceComposite.class);
		if (uiControl != null) {
			ChoiceComposite composite = (ChoiceComposite) uiControl;
			Assert.isTrue(composite.isMultipleSelection(), "expected multiple selection ChoiceComposite"); //$NON-NLS-1$
		}
	}

	@Override
	protected void unbindUIControl() {
		disposeChildren(getUIControl());
	}

	// public methods
	// ///////////////

	@Override
	public ChoiceComposite getUIControl() {
		return (ChoiceComposite) super.getUIControl();
	}

	public void bindToModel(IObservableList optionValues, IObservableList selectionValues) {
		Assert.isNotNull(optionValues, "optionValues"); //$NON-NLS-1$
		Assert.isNotNull(selectionValues, "selectionValues"); //$NON-NLS-1$
		bindToModel(optionValues, null, selectionValues);
	}

	public void bindToModel(Object listBean, String listPropertyName, Object selectionBean, String selectionPropertyName) {
		Assert.isNotNull(listBean, "listBean"); //$NON-NLS-1$
		Assert.isNotNull(listPropertyName, "listPropertyName"); //$NON-NLS-1$
		Assert.isNotNull(selectionBean, "selectionBean"); //$NON-NLS-1$
		Assert.isNotNull(selectionPropertyName, "selectionPropertyName"); //$NON-NLS-1$
		IObservableList optionValues = BeansObservables.observeList(Realm.getDefault(), listBean, listPropertyName);
		IObservableList selectionValues = BeansObservables.observeList(Realm.getDefault(), selectionBean,
				selectionPropertyName);
		bindToModel(optionValues, null, selectionValues);
	}

	public void bindToModel(List<? extends Object> optionValues, List<String> optionLabels, Object selectionBean,
			String selectionPropertyName) {
		Assert.isNotNull(optionValues, "optionValues"); //$NON-NLS-1$
		Assert.isNotNull(selectionBean, "selectionBean"); //$NON-NLS-1$
		Assert.isNotNull(selectionPropertyName, "selectionPropertyName"); //$NON-NLS-1$
		IObservableList optionList = BeansObservables.observeList(Realm.getDefault(), new ListBean(optionValues),
				ListBean.PROPERTY_VALUES);
		// IObservableList optionList =
		// BeansObservables.observeList(Realm.getDefault(), new
		// ListWrapper(optionValues),
		// "values");
		IObservableList selectionList = BeansObservables.observeList(Realm.getDefault(), selectionBean,
				selectionPropertyName);
		bindToModel(optionList, optionLabels, selectionList);
	}

	@Override
	public void updateFromModel() {
		assertIsBoundToModel();
		super.updateFromModel();
		optionsBinding.updateModelToTarget();
		selectionBinding.updateModelToTarget();
		ChoiceComposite control = getUIControl();
		disposeChildren(control);
		createChildren(control);
	}

	public IObservableList getObservableSelectionList() {
		return selectionObservable;
	}

	public List getSelection() {
		return new ArrayList(selectionObservable);
	}

	public void setSelection(List selection) {
		assertIsBoundToModel();
		List oldSelection = new ArrayList(selectionObservable);
		List newSelection = selection == null ? Collections.EMPTY_LIST : selection;
		for (Object candidate : newSelection) {
			if (!optionsObservable.contains(candidate)) {
				throw new BindingException("candidate not in option list: " + candidate); //$NON-NLS-1$
			}
		}
		selectionObservable.clear();
		selectionObservable.addAll(newSelection);
		updateChildren(getUIControl());
		firePropertyChange(PROPERTY_SELECTION, oldSelection, newSelection);
	}

	public IObservableList getObservableList() {
		return optionsObservable;
	}

	@Override
	public boolean isDisableMandatoryMarker() {
		return selectionObservable != null && selectionObservable.size() > 0;
	}

	// helping methods
	// ////////////////

	private void assertIsBoundToModel() {
		if (optionsBinding == null || selectionBinding == null) {
			throw new BindingException("ridget not bound to model"); //$NON-NLS-1$
		}
	}

	private void bindToModel(IObservableList optionValues, List<String> optionLabels, IObservableList selectionValues) {
		if (optionLabels != null) {
			String msg = "Mismatch between number of optionValues and optionLabels"; //$NON-NLS-1$
			Assert.isLegal(optionValues.size() == optionLabels.size(), msg);
		}

		unbindUIControl();

		// clear observables as they may be bound to another model
		// must dispose old binding first to avoid updating old model
		if (dbc != null) {
			dbc.dispose();
		}
		optionsObservable.clear();
		selectionObservable.clear();

		// set up new binding
		DataBindingContext dbc = new DataBindingContext();
		optionsBinding = dbc.bindList(optionsObservable, optionValues, new UpdateListStrategy(
				UpdateListStrategy.POLICY_UPDATE), new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST));
		optionsBinding.getModel().addChangeListener(DUMMY_LISTENER);
		selectionBinding = dbc.bindList(selectionObservable, selectionValues, new UpdateListStrategy(
				UpdateListStrategy.POLICY_UPDATE), new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST));
		selectionBinding.getModel().addChangeListener(DUMMY_LISTENER);
		if (optionLabels != null) {
			this.optionLabels = optionLabels.toArray(new String[optionLabels.size()]);
		} else {
			this.optionLabels = null;
		}

		bindUIControl();
	}

	private void createChildren(Composite control) {
		if (control != null && !control.isDisposed()) {
			Object[] values = optionsObservable.toArray();
			for (int i = 0; i < values.length; i++) {
				Object value = values[i];
				String caption = optionLabels != null ? optionLabels[i] : String.valueOf(value);

				Button button = new Button(control, SWT.CHECK);
				button.setText(caption);
				button.setForeground(control.getForeground());
				button.setBackground(control.getBackground());
				button.setData(value);
				if (selectionObservable.contains(value)) {
					button.setSelection(true);
				}
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button button = (Button) e.widget;
						Object data = button.getData();
						if (button.getSelection()) {
							if (!selectionObservable.contains(data)) {
								selectionObservable.add(data);
							}
						} else {
							selectionObservable.remove(data);
						}
					}
				});
			}
			control.layout(true);
		}
	}

	private void disposeChildren(Composite control) {
		if (control != null && !control.isDisposed()) {
			for (Control child : control.getChildren()) {
				child.dispose();
			}
		}
	}

	private void updateChildren(Composite control) {
		if (control != null && !control.isDisposed()) {
			for (Control child : control.getChildren()) {
				Button button = (Button) child;
				boolean isSelected = selectionObservable.contains(button.getData());
				button.setSelection(isSelected);
			}
		}
	}

	private void updateMarkers() {
		boolean isMandatoryDisabled = isDisableMandatoryMarker();
		for (IMarker marker : getMarkersOfType(MandatoryMarker.class)) {
			MandatoryMarker mMarker = (MandatoryMarker) marker;
			mMarker.setDisabled(isMandatoryDisabled);
		}
	}

}

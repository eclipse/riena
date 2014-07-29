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
package org.eclipse.riena.example.client.controllers;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;

import org.eclipse.riena.beans.common.IntegerBean;
import org.eclipse.riena.beans.common.TypedBean;
import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
import org.eclipse.riena.ui.core.marker.ValidationTime;
import org.eclipse.riena.ui.ridgets.IActionListener;
import org.eclipse.riena.ui.ridgets.IDecimalTextRidget;
import org.eclipse.riena.ui.ridgets.INumericTextRidget;
import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.ITextRidget;
import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
import org.eclipse.riena.ui.ridgets.validation.MaxNumberLength;
import org.eclipse.riena.ui.ridgets.validation.MinLength;
import org.eclipse.riena.ui.ridgets.validation.ValidRange;

/**
 * Controller for the {@link INumericTextRidget} and {@link IDecimalTextRidget} example.
 */
public class TextNumericSubModuleController extends SubModuleController {

	private final static String[] IDS = { "StringNum", "Integer", "Long", "BigInteger", "StringDec", "Double", "Float", "BigDecimal", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"Range", "MaxEight", "MinThree" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Binds and updates the ridgets.
	 */
	@Override
	public void configureRidgets() {
		final DataBindingContext dbc = new DataBindingContext();
		for (final String id : IDS) {
			bind(dbc, id);
		}

		bindToModel("StringNum", new TypedBean<String>("1234")); //$NON-NLS-1$ //$NON-NLS-2$
		final INumericTextRidget inInteger = getRidget(INumericTextRidget.class, "inInteger"); //$NON-NLS-1$
		inInteger.setMaxLength(5);
		bindToModel("Integer", new TypedBean<Integer>(Integer.valueOf(-1234)), true); //$NON-NLS-1$

		bindToModel("Long", new TypedBean<Long>(Long.valueOf(1234))); //$NON-NLS-1$
		bindToModel("BigInteger", new TypedBean<BigInteger>(BigInteger.valueOf(12345789))); //$NON-NLS-1$

		final IDecimalTextRidget inStringDec = getRidget(IDecimalTextRidget.class, "inStringDec"); //$NON-NLS-1$
		inStringDec.setPrecision(4);
		bindToModel("StringDec", new TypedBean<String>("12345678.1234")); //$NON-NLS-1$ //$NON-NLS-2$

		bindToModel("Double", new TypedBean<Double>(Double.valueOf(-1234.00)), true); //$NON-NLS-1$
		bindToModel("Float", new TypedBean<Float>(Float.valueOf("1234"))); //$NON-NLS-1$//$NON-NLS-2$

		final IDecimalTextRidget inBigDecimal = getRidget(IDecimalTextRidget.class, "inBigDecimal"); //$NON-NLS-1$
		inBigDecimal.setMaxLength(30);
		inBigDecimal.setPrecision(10);
		bindToModel("BigDecimal", new TypedBean<BigDecimal>(BigDecimal.valueOf(12345789.1234))); //$NON-NLS-1$

		final INumericTextRidget txtRange = getRidget(INumericTextRidget.class, "inRange"); //$NON-NLS-1$
		txtRange.addValidationRule(new ValidRange(Integer.valueOf(100), Integer.valueOf(1000)), ValidationTime.ON_UPDATE_TO_MODEL);
		txtRange.bindToModel(new IntegerBean(1), "value"); //$NON-NLS-1$
		txtRange.updateFromModel();

		final INumericTextRidget txtMaxEight = getRidget(INumericTextRidget.class, "inMaxEight"); //$NON-NLS-1$
		txtMaxEight.addValidationRule(new MaxNumberLength(8), ValidationTime.ON_UI_CONTROL_EDIT);
		txtMaxEight.bindToModel(new IntegerBean(123456), "value"); //$NON-NLS-1$
		txtMaxEight.updateFromModel();

		final INumericTextRidget txtMinThree = getRidget(INumericTextRidget.class, "inMinThree"); //$NON-NLS-1$
		txtMinThree.setGrouping(false);
		txtMinThree.addValidationRule(new MinLength(4), ValidationTime.ON_UI_CONTROL_EDIT);
		txtMinThree.bindToModel(new IntegerBean(1), "value"); //$NON-NLS-1$
		txtMinThree.updateFromModel();

		final IToggleButtonRidget groupingBtn = getRidget(IToggleButtonRidget.class, "groupingBtn");
		groupingBtn.setSelected(txtMinThree.isGrouping());
		groupingBtn.addListener(new IActionListener() {

			public void callback() {
				updateGrouping(groupingBtn.isSelected());
			}

		});

	}

	private void updateGrouping(final boolean selected) {

		for (final String id : IDS) {
			final INumericTextRidget inputRidget = getRidget(INumericTextRidget.class, "in" + id); //$NON-NLS-1$
			inputRidget.setGrouping(selected);
		}

	}

	// helping methods
	//////////////////

	private void bind(final DataBindingContext dbc, final String id) {
		IRidget inputRidget;
		if (id.equals("StringDec") || id.equals("Double") || id.equals("Float") || id.equals("BigDecimal")) {
			inputRidget = getRidget(IDecimalTextRidget.class, "in" + id); //$NON-NLS-1$
		} else {
			inputRidget = getRidget(INumericTextRidget.class, "in" + id); //$NON-NLS-1$
		}
		final ITextRidget outputRidget = getRidget(ITextRidget.class, "out" + id); //$NON-NLS-1$
		outputRidget.setOutputOnly(true);
		dbc.bindValue(BeansObservables.observeValue(inputRidget, ITextRidget.PROPERTY_TEXT),
				BeansObservables.observeValue(outputRidget, ITextRidget.PROPERTY_TEXT), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER));
	}

	private void bindToModel(final String id, final TypedBean<?> value, final boolean signed) {
		final INumericTextRidget inputRidget = getRidget(INumericTextRidget.class, "in" + id); //$NON-NLS-1$
		inputRidget.setSigned(signed);
		inputRidget.bindToModel(value, TypedBean.PROP_VALUE);
		inputRidget.updateFromModel();
		inputRidget.setGrouping(false);
	}

	private void bindToModel(final String id, final TypedBean<?> value) {
		bindToModel(id, value, false);
	}
}

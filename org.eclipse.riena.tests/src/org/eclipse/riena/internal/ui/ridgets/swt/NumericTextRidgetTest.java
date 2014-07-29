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
package org.eclipse.riena.internal.ui.ridgets.swt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import org.eclipse.riena.beans.common.BooleanBean;
import org.eclipse.riena.beans.common.DoubleBean;
import org.eclipse.riena.beans.common.IntegerBean;
import org.eclipse.riena.beans.common.StringBean;
import org.eclipse.riena.core.marker.IMarker;
import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
import org.eclipse.riena.internal.ui.swt.utils.TestUtils;
import org.eclipse.riena.ui.core.marker.ErrorMarker;
import org.eclipse.riena.ui.core.marker.IMessageMarker;
import org.eclipse.riena.ui.core.marker.NegativeMarker;
import org.eclipse.riena.ui.core.marker.ValidationTime;
import org.eclipse.riena.ui.ridgets.INumericTextRidget;
import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.ITextRidget;
import org.eclipse.riena.ui.ridgets.marker.ValidationMessageMarker;
import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
import org.eclipse.riena.ui.ridgets.validation.MaxLength;
import org.eclipse.riena.ui.ridgets.validation.MaxNumberLength;
import org.eclipse.riena.ui.ridgets.validation.MinLength;
import org.eclipse.riena.ui.ridgets.validation.ValidationFailure;
import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
import org.eclipse.riena.ui.swt.utils.UIControlsFactory;

/**
 * Tests for the class {@link NumericTextRidget}.
 */
public class NumericTextRidgetTest extends TextRidgetTest {

	private static final Integer INTEGER_ONE = Integer.valueOf(471);
	private static final Integer INTEGER_TWO = Integer.valueOf(23);

	@Override
	protected IRidget createRidget() {
		return new NumericTextRidget();
	}

	@Override
	protected INumericTextRidget getRidget() {
		return (INumericTextRidget) super.getRidget();
	}

	@Override
	protected Control createWidget(final Composite parent) {
		final Control result = new Text(getShell(), SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		result.setData(UIControlsFactory.KEY_TYPE, UIControlsFactory.TYPE_NUMERIC);
		result.setLayoutData(new RowData(100, SWT.DEFAULT));
		return result;
	}

	// test methods
	///////////////

	public void testCreatePatternEmpty() throws Exception {
		final String input = ""; //$NON-NLS-1$

		// trying all configuration possibilities
		final INumericTextRidget r = getRidget();
		String pattern;

		r.setMaxLength(INumericTextRidget.MAX_LENGTH_UNBOUNDED);
		r.setSigned(true);
		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertTrue(input.matches(pattern));

		r.setSigned(false);
		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertTrue(input.matches(pattern));

		r.setMaxLength(3);
		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertTrue(input.matches(pattern));

		r.setSigned(true);
		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertTrue(input.matches(pattern));
	}

	public void testCreatePattern() throws Exception {
		final String input = "-123"; //$NON-NLS-1$

		// trying all configuration possibilities
		final INumericTextRidget r = getRidget();
		String pattern;

		r.setMaxLength(INumericTextRidget.MAX_LENGTH_UNBOUNDED);
		r.setSigned(true);
		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertTrue(input.matches(pattern));

		r.setSigned(false);
		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertFalse(input.matches(pattern));

		r.setMaxLength(3);
		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertFalse(input.matches(pattern));

		r.setSigned(true);
		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertTrue(input.matches(pattern));
	}

	public void testCreatePatternMaxLengthSigned() throws Exception {
		final INumericTextRidget r = getRidget();
		r.setMaxLength(3);
		r.setSigned(true);
		String pattern;
		String input;

		input = "-123"; //$NON-NLS-1$

		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertTrue(input.matches(pattern));

		input = "1234"; //$NON-NLS-1$

		pattern = ReflectionUtils.invokeHidden(r, "createPattern", input); //$NON-NLS-1$
		assertFalse(input.matches(pattern));
	}

	@Override
	public void testRidgetMapping() {
		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
		assertSame(NumericTextRidget.class, mapper.getRidgetClass(getWidget()));
	}

	@Override
	public void testCreate() throws Exception {
		assertFalse(getRidget().isDirectWriting());
		assertEquals("0", getRidget().getText()); //$NON-NLS-1$
	}

	@Override
	public void testSetText() throws Exception {
		final INumericTextRidget ridget = getRidget();
		ridget.setGrouping(true);

		ridget.setText(""); //$NON-NLS-1$

		assertEquals("", ridget.getText()); //$NON-NLS-1$

		ridget.setText("-1234"); //$NON-NLS-1$

		assertEquals(localize("-1.234"), ridget.getText()); //$NON-NLS-1$

		ridget.setText("1234"); //$NON-NLS-1$

		assertEquals(localize("1.234"), ridget.getText()); //$NON-NLS-1$

		ridget.setText(localize("98.765")); //$NON-NLS-1$

		assertEquals(localize("98.765"), ridget.getText()); //$NON-NLS-1$

		try {
			ridget.setText(localize("98.765,12")); //$NON-NLS-1$
			fail();
		} catch (final NumberFormatException nfe) {
			ok();
		}

		try {
			ridget.setText("abcd"); //$NON-NLS-1$
			fail();
		} catch (final NumberFormatException nfe) {
			ok();
		}

		try {
			ridget.setText("a,bcd"); //$NON-NLS-1$
			fail();
		} catch (final NumberFormatException nfe) {
			ok();
		}
	}

	public void testValueChanged() {
		final String oldValue = "1.234,5600"; //$NON-NLS-1$
		final String newValue = "1.234,56"; //$NON-NLS-1$
		assertFalse(callExternalValueChanged(oldValue, newValue));
		assertTrue(callExternalValueChanged(oldValue, newValue + "01")); //$NON-NLS-1$
		assertFalse(callExternalValueChanged(oldValue, "0" + newValue)); //$NON-NLS-1$
		assertTrue(callExternalValueChanged(",", "0")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(callExternalValueChanged(",", "0,00000")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	private boolean callExternalValueChanged(final Object... values) {
		return ReflectionUtils.invokeHidden(getRidget(), "isExternalValueChange", values); //$NON-NLS-1$
	}

	public void testSetTextNoGroup() throws Exception {
		final INumericTextRidget ridget = getRidget();
		ridget.setGrouping(false);

		ridget.setText(""); //$NON-NLS-1$

		assertEquals("", ridget.getText()); //$NON-NLS-1$

		ridget.setText("-1234"); //$NON-NLS-1$

		assertEquals("-1234", ridget.getText()); //$NON-NLS-1$

		ridget.setText("1234"); //$NON-NLS-1$

		assertEquals("1234", ridget.getText()); //$NON-NLS-1$

		ridget.setText(localize("98.765")); //$NON-NLS-1$

		assertEquals("98765", ridget.getText()); //$NON-NLS-1$

		try {
			ridget.setText(localize("98.765,12")); //$NON-NLS-1$
			fail();
		} catch (final NumberFormatException nfe) {
			ok();
		}

		try {
			ridget.setText("abcd"); //$NON-NLS-1$
			fail();
		} catch (final NumberFormatException nfe) {
			ok();
		}
	}

	/**
	 * Test that setText(null) clears the number (equiv. to setText("0")).
	 */
	@Override
	public void testSetTextNull() {
		final ITextRidget ridget = getRidget();

		ridget.setText("42"); //$NON-NLS-1$

		assertEquals("42", ridget.getText()); //$NON-NLS-1$

		ridget.setText(null);

		assertEquals("", ridget.getText()); //$NON-NLS-1$
	}

	@Override
	public void testGetText() throws Exception {
		final ITextRidget ridget = getRidget();

		assertEquals("0", ridget.getText()); //$NON-NLS-1$
	}

	@Override
	public void testBindToModelPropertyName() {
		final ITextRidget ridget = getRidget();
		final IntegerBean model = new IntegerBean(1337);
		ridget.bindToModel(model, IntegerBean.PROP_VALUE);

		assertEquals("0", ridget.getText()); //$NON-NLS-1$

		ridget.updateFromModel();

		assertEquals(localize("1.337"), ridget.getText()); //$NON-NLS-1$
	}

	@Override
	public void testUpdateFromModel() {
		final ITextRidget ridget = getRidget();
		final IntegerBean model = new IntegerBean(1337);
		ridget.bindToModel(model, IntegerBean.PROP_VALUE);

		model.setValue(-7);
		ridget.updateFromModel();

		assertEquals(localize("-7"), ridget.getText()); //$NON-NLS-1$
	}

	@Override
	public void testBindToModelIObservableValue() throws Exception {
		final ITextRidget ridget = getRidget();

		final IntegerBean model = new IntegerBean(4711);
		final IObservableValue modelOV = BeansObservables.observeValue(model, IntegerBean.PROP_VALUE);
		ridget.bindToModel(modelOV);

		assertEquals("0", ridget.getText()); //$NON-NLS-1$

		ridget.updateFromModel();

		assertEquals(localize("4.711"), ridget.getText()); //$NON-NLS-1$
	}

	@Override
	public void testFocusGainedDoesSelectOnSingleText() {
		final Text control = getWidget();

		assertEquals("0", control.getSelectionText()); //$NON-NLS-1$
		control.setSelection(0, 0);

		final Event e = new Event();
		e.type = SWT.FocusIn;
		e.widget = control;
		e.widget.notifyListeners(e.type, e);

		assertEquals(0, control.getStyle() & SWT.MULTI);
		assertEquals("0", control.getSelectionText()); //$NON-NLS-1$
	}

	@Override
	public void testFocusGainedDoesNotSelectOnMultiLineText() {
		// override test in superclass; multiline is not supported
		assertTrue(true);
	}

	public void testCheckWidget() {
		final ITextRidget ridget = getRidget();
		final Text control = new Text(getShell(), SWT.MULTI);

		try {
			ridget.setUIControl(control);
			fail();
		} catch (final RuntimeException exc) {
			ok();
		}

		try {
			ridget.setUIControl(new Button(getShell(), SWT.PUSH));
			fail();
		} catch (final RuntimeException exc) {
			ok();
		}
	}

	public void testSetSignedTrue() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();
		final IntegerBean model = new IntegerBean(1337);
		ridget.bindToModel(model, IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertTrue(ridget.isSigned());

		expectNoPropertyChangeEvent();
		ridget.setSigned(true);

		verifyPropertyChangeEvents();
		assertTrue(ridget.isSigned());

		final int caretPos = control.getText().length() - 1;
		focusIn(control);
		control.setSelection(caretPos, caretPos);

		assertEquals(localize("1.337"), control.getText()); //$NON-NLS-1$
		assertEquals(caretPos, control.getCaretPosition());

		UITestHelper.sendString(control.getDisplay(), "-"); //$NON-NLS-1$

		assertEquals(localize("-1.337"), control.getText()); //$NON-NLS-1$
		assertEquals(caretPos + 1, control.getCaretPosition());

		control.setSelection(1, 1);
		UITestHelper.sendString(control.getDisplay(), "\b"); //$NON-NLS-1$

		assertEquals(localize("1.337"), control.getText()); //$NON-NLS-1$
		assertEquals(0, control.getCaretPosition());
	}

	public void testSetSignedFalse() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();
		final IntegerBean model = new IntegerBean(1337);
		ridget.bindToModel(model, IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertTrue(ridget.isSigned());

		expectPropertyChangeEvent(INumericTextRidget.PROPERTY_SIGNED, Boolean.TRUE, Boolean.FALSE);
		ridget.setSigned(false);

		verifyPropertyChangeEvents();
		assertFalse(ridget.isSigned());

		final int caretPos = control.getText().length() - 1;
		focusIn(control);
		control.setSelection(caretPos, caretPos);

		assertEquals(localize("1.337"), control.getText()); //$NON-NLS-1$
		assertEquals(caretPos, control.getCaretPosition());

		UITestHelper.sendString(control.getDisplay(), "-"); //$NON-NLS-1$

		assertEquals(localize("1.337"), control.getText()); //$NON-NLS-1$
		assertEquals(caretPos, control.getCaretPosition());
	}

	/**
	 * As per bug #275134.
	 */
	public void testSetSignedThrowsException() {
		final INumericTextRidget ridget = getRidget();
		ridget.setText("1234"); //$NON-NLS-1$

		ridget.setSigned(false);

		try {
			ridget.setText("-4711"); //$NON-NLS-1$
			fail();
		} catch (final RuntimeException exc) {
			ok("expected"); //$NON-NLS-1$
		}
		assertEquals(localize("1.234"), ridget.getText()); //$NON-NLS-1$
	}

	public void testSetGrouping() {

		final INumericTextRidget ridget = getRidget();
		final IntegerBean model = new IntegerBean(1337);
		ridget.bindToModel(model, IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		final BooleanBean changed = new BooleanBean(false);
		model.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				changed.setValue(true);
			}
		});

		assertTrue(ridget.isGrouping());
		assertEquals(localize("1.337"), ridget.getText()); //$NON-NLS-1$

		ridget.setGrouping(false);

		assertFalse(ridget.isGrouping());
		String uiText = ((Text) ridget.getUIControl()).getText();
		assertEquals("1337", uiText); //$NON-NLS-1$
		assertEquals(localize("1.337"), ridget.getText()); //$NON-NLS-1$
		assertFalse(changed.isValue());

		ridget.setGrouping(true);

		assertTrue(ridget.isGrouping());
		uiText = ((Text) ridget.getUIControl()).getText();
		assertEquals(localize("1.337"), uiText); //$NON-NLS-1$
		assertEquals(localize("1.337"), ridget.getText()); //$NON-NLS-1$
		assertFalse(changed.isValue());

	}

	public void testUpdateFromControlUserInput() throws Exception {
		final Text control = getWidget();
		final ITextRidget ridget = getRidget();
		final Display display = control.getDisplay();
		final IntegerBean bean = new IntegerBean();
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);

		assertFalse(ridget.isDirectWriting());

		UITestHelper.sendString(display, "47"); //$NON-NLS-1$

		assertEquals("47", control.getText()); //$NON-NLS-1$
		assertEquals("0", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(0), bean.getValue());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "0", "47"), new PropertyChangeEvent(ridget, "text", "0", "47")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		UITestHelper.sendString(display, "\r"); //$NON-NLS-1$
		UITestHelper.readAndDispatch(control);

		verifyPropertyChangeEvents();
		assertEquals("47", control.getText()); //$NON-NLS-1$
		assertEquals("47", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(47), bean.getValue());

		expectNoPropertyChangeEvent();

		UITestHelper.sendString(display, "1"); //$NON-NLS-1$

		verifyPropertyChangeEvents();
		assertEquals("471", control.getText()); //$NON-NLS-1$
		assertEquals("47", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(47), bean.getValue());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "47", "471"), new PropertyChangeEvent(ridget, "text", "47", "471")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		UITestHelper.sendString(display, "\t"); //$NON-NLS-1$

		verifyPropertyChangeEvents();
		assertEquals("471", control.getText()); //$NON-NLS-1$
		assertEquals("471", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(471), bean.getValue());
	}

	public void testInputNegativeNumber() throws Exception {
		final IntegerBean bean = new IntegerBean();
		getRidget().setDirectWriting(true);
		getRidget().bindToModel(bean, IntegerBean.PROP_VALUE);
		assertEquals(Integer.valueOf(0), bean.getValue());

		UITestHelper.sendString(getWidget().getDisplay(), minus);
		assertEquals(minus, getWidget().getText());
		assertEquals(Integer.valueOf(0), bean.getValue());

		UITestHelper.sendString(getWidget().getDisplay(), "5"); //$NON-NLS-1$
		assertEquals(minus + "5", getWidget().getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(-5), bean.getValue());
	}

	public void testInputNegativeNumberInEmptyTextField() throws Exception {
		final IntegerBean bean = new IntegerBean();

		getRidget().setDirectWriting(true);
		getRidget().bindToModel(bean, IntegerBean.PROP_VALUE);
		assertEquals(Integer.valueOf(0), bean.getValue());

		UITestHelper.sendKeyAction(getWidget().getDisplay(), UITestHelper.KC_DEL);
		final Integer expectedAfterMinus = bean.getValue();

		UITestHelper.sendString(getWidget().getDisplay(), minus);
		assertEquals(minus, getWidget().getText());
		assertEquals(expectedAfterMinus, bean.getValue());

		UITestHelper.sendString(getWidget().getDisplay(), "5"); //$NON-NLS-1$
		assertEquals(minus + "5", getWidget().getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(-5), bean.getValue());
	}

	public void testUpdateFromControlUserInputDirectWriting() {
		final Text control = getWidget();
		final INumericTextRidget ridget = getRidget();

		//		ridget.addPropertyChangeListener(ITextRidget.PROPERTY_TEXT, new PropertyChangeListener() {
		//			public void propertyChange(PropertyChangeEvent evt) {
		//				System.out.println(String.format("%s %s %s", evt.getPropertyName(), evt.getOldValue(), evt
		//						.getNewValue()));
		//			}
		//		});

		final IntegerBean bean = new IntegerBean();
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
		ridget.setDirectWriting(true);

		final Display display = control.getDisplay();
		UITestHelper.sendString(display, "4"); //$NON-NLS-1$

		assertEquals("4", control.getText()); //$NON-NLS-1$
		assertEquals("4", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(4), bean.getValue());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "4", "47"), new PropertyChangeEvent(ridget, "text", "4", "47")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		UITestHelper.sendString(display, "7"); //$NON-NLS-1$

		verifyPropertyChangeEvents();
		assertEquals("47", control.getText()); //$NON-NLS-1$
		assertEquals("47", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(47), bean.getValue());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "47", "471"), new PropertyChangeEvent(ridget, "text", "47", "471")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		UITestHelper.sendString(display, "1"); //$NON-NLS-1$

		verifyPropertyChangeEvents();
		assertEquals("471", control.getText()); //$NON-NLS-1$
		assertEquals("471", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(471), bean.getValue());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "471", localize("4.711")), new PropertyChangeEvent(ridget, "text", "471", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				localize("4.711"))); //$NON-NLS-1$

		UITestHelper.sendString(display, "1"); //$NON-NLS-1$

		verifyPropertyChangeEvents();
		assertEquals(localize("4.711"), control.getText()); //$NON-NLS-1$
		assertEquals(localize("4.711"), ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(4711), bean.getValue());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", localize("4.711"), "471"), new PropertyChangeEvent(ridget, "text", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				localize("4.711"), "471")); //$NON-NLS-1$ //$NON-NLS-2$

		UITestHelper.sendKeyAction(display, SWT.ARROW_LEFT);
		UITestHelper.sendString(display, "\b"); //$NON-NLS-1$

		verifyPropertyChangeEvents();
		assertEquals("471", control.getText()); //$NON-NLS-1$
		assertEquals("471", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf("471"), bean.getValue()); //$NON-NLS-1$

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "471", "47"), new PropertyChangeEvent(ridget, "text", "471", "47")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		UITestHelper.sendString(display, String.valueOf(SWT.DEL));

		verifyPropertyChangeEvents();
		assertEquals("47", control.getText()); //$NON-NLS-1$
		assertEquals("47", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(47), bean.getValue());

		expectNoPropertyChangeEvent();

		bean.setValue(Integer.valueOf(4711));

		verifyPropertyChangeEvents();
		assertEquals("47", control.getText()); //$NON-NLS-1$
		assertEquals("47", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(4711), bean.getValue());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "47", "4"), new PropertyChangeEvent(ridget, "text", "47", "4")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		UITestHelper.sendString(display, "\b"); //$NON-NLS-1$

		verifyPropertyChangeEvents();
		assertEquals("4", control.getText()); //$NON-NLS-1$
		assertEquals("4", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf("4"), bean.getValue()); //$NON-NLS-1$
	}

	public void testUpdateFromRidgetOnRebind() throws Exception {
		final Text control = getWidget();
		final ITextRidget ridget = getRidget();

		final IntegerBean bean = new IntegerBean();
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
		bean.setValue(INTEGER_ONE);
		ridget.updateFromModel();

		assertEquals(INTEGER_ONE.toString(), control.getText());
		assertEquals(INTEGER_ONE.toString(), ridget.getText());
		assertEquals(INTEGER_ONE, bean.getValue());

		// unbind, e.g. when the view is used by another controller
		ridget.setUIControl(null);

		control.selectAll();
		UITestHelper.sendString(control.getDisplay(), "99"); //$NON-NLS-1$

		assertEquals("99", control.getText()); //$NON-NLS-1$
		assertEquals(INTEGER_ONE.toString(), ridget.getText());
		assertEquals(INTEGER_ONE, bean.getValue());

		// rebind
		ridget.setUIControl(control);

		assertEquals(INTEGER_ONE.toString(), control.getText());
		assertEquals(INTEGER_ONE.toString(), ridget.getText());
		assertEquals(INTEGER_ONE, bean.getValue());

		// unbind again
		ridget.setUIControl(null);

		bean.setValue(INTEGER_TWO);
		ridget.updateFromModel();

		assertEquals(INTEGER_ONE.toString(), control.getText());
		assertEquals(INTEGER_TWO.toString(), ridget.getText());
		assertEquals(INTEGER_TWO, bean.getValue());

		// rebind
		ridget.setUIControl(control);

		assertEquals(INTEGER_TWO.toString(), control.getText());
		assertEquals(INTEGER_TWO.toString(), ridget.getText());
		assertEquals(INTEGER_TWO, bean.getValue());
	}

	public void testValidationOnUpdateToModel() throws Exception {
		final Text control = getWidget();
		final ITextRidget ridget = getRidget();

		final IntegerBean bean = new IntegerBean();
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);

		ridget.addValidationRule(new MinLength(3), ValidationTime.ON_UPDATE_TO_MODEL);

		bean.setValue(INTEGER_ONE);
		ridget.updateFromModel();

		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
		assertEquals(INTEGER_ONE.toString(), ridget.getText());

		control.selectAll();
		UITestHelper.sendString(control.getDisplay(), "99\t"); //$NON-NLS-1$

		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
		assertEquals("99", ridget.getText()); //$NON-NLS-1$

		focusIn(control);
		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
		UITestHelper.sendString(control.getDisplay(), "9"); //$NON-NLS-1$

		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());

		UITestHelper.sendString(control.getDisplay(), "\r"); //$NON-NLS-1$

		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
		assertEquals("999", ridget.getText()); //$NON-NLS-1$
	}

	public void testCharactersAreBlockedInControl() throws Exception {
		final Text control = getWidget();
		final ITextRidget ridget = getRidget();

		final IntegerBean bean = new IntegerBean();
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
		ridget.setDirectWriting(true);

		UITestHelper.sendString(control.getDisplay(), "12"); //$NON-NLS-1$

		assertEquals("12", control.getText()); //$NON-NLS-1$
		assertEquals("12", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(12), bean.getValue());

		UITestHelper.sendString(control.getDisplay(), "foo"); //$NON-NLS-1$

		assertEquals("12", control.getText()); //$NON-NLS-1$
		assertEquals("12", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(12), bean.getValue());
	}

	public void testValidationOnUpdateFromModelWithOnEditRule() {
		final ITextRidget ridget = getRidget();
		final IntegerBean bean = new IntegerBean();
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);

		assertFalse(ridget.isErrorMarked());

		ridget.addValidationRule(new MaxLength(5), ValidationTime.ON_UI_CONTROL_EDIT);
		bean.setValue(Integer.valueOf(123456));
		ridget.updateFromModel();

		assertTrue(ridget.isErrorMarked());
		assertEquals(localize("123.456"), ridget.getText()); //$NON-NLS-1$
		assertEquals(localize("123.456"), getWidget().getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(123456), bean.getValue());

		bean.setValue(Integer.valueOf(1234));
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());
		assertEquals(localize("1.234"), ridget.getText()); //$NON-NLS-1$
		assertEquals(localize("1.234"), getWidget().getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(1234), bean.getValue());
	}

	public void testValidationOnUpdateFromModelWithOnUpdateRule() {
		final ITextRidget ridget = getRidget();
		final IntegerBean bean = new IntegerBean(Integer.valueOf(123456));
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());

		ridget.addValidationRule(new MinLength(5), ValidationTime.ON_UPDATE_TO_MODEL);
		bean.setValue(Integer.valueOf(123));
		ridget.updateFromModel();

		assertTrue(ridget.isErrorMarked());
		assertEquals("123", ridget.getText()); //$NON-NLS-1$
		assertEquals("123", getWidget().getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(123), bean.getValue());

		bean.setValue(Integer.valueOf(1234));
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());
		assertEquals(localize("1.234"), ridget.getText()); //$NON-NLS-1$
		assertEquals(localize("1.234"), getWidget().getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(1234), bean.getValue());
	}

	public void testUpdateFromRidgetWithValidationOnEditRule() {
		final Text control = getWidget();
		final ITextRidget ridget = getRidget();

		final IntegerBean bean = new IntegerBean(Integer.valueOf(1234));
		ridget.addValidationRule(new MinLength(5), ValidationTime.ON_UI_CONTROL_EDIT);
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);

		assertFalse(ridget.isErrorMarked());
		assertFalse(ridget.isDirectWriting());

		UITestHelper.sendString(control.getDisplay(), "98765\t"); //$NON-NLS-1$

		assertFalse(ridget.isErrorMarked());
		assertEquals(localize("98.765"), ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(98765), bean.getValue());

		focusIn(control);
		control.selectAll();
		// \t triggers update
		UITestHelper.sendString(control.getDisplay(), "12\t"); //$NON-NLS-1$

		assertTrue(ridget.isErrorMarked());
		// MinLength is non-blocking, so we expected '12' in ridget
		assertEquals("12", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(98765), bean.getValue());

		focusIn(control);
		control.selectAll();
		UITestHelper.sendString(control.getDisplay(), "43210\t"); //$NON-NLS-1$

		assertFalse(ridget.isErrorMarked());
		assertEquals(localize("43.210"), ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(43210), bean.getValue());
	}

	public void testUpdateFromRidgetWithValidationOnUpdateRule() {
		final Text control = getWidget();
		final ITextRidget ridget = getRidget();

		final IntegerBean bean = new IntegerBean();
		ridget.addValidationRule(new EndsWithFive(), ValidationTime.ON_UPDATE_TO_MODEL);
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);

		assertTrue(ridget.isErrorMarked());
		assertFalse(ridget.isDirectWriting());

		UITestHelper.sendString(control.getDisplay(), "98765\t"); //$NON-NLS-1$

		assertFalse(ridget.isErrorMarked());
		assertEquals(localize("98.765"), ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(98765), bean.getValue());

		focusIn(control);
		control.selectAll();
		// \t triggers update
		UITestHelper.sendString(control.getDisplay(), "98\t"); //$NON-NLS-1$

		assertTrue(ridget.isErrorMarked());
		assertEquals("98", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(98765), bean.getValue());

		focusIn(control);
		control.setSelection(2, 2);
		UITestHelper.sendString(control.getDisplay(), "555\t"); //$NON-NLS-1$

		assertFalse(ridget.isErrorMarked());
		assertEquals(localize("98.555"), ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(98555), bean.getValue());
	}

	public void testValidationMessageWithOnEditRule() throws Exception {
		final Text control = getWidget();
		final ITextRidget ridget = getRidget();

		ridget.addValidationRule(new EvenNumberOfCharacters(), ValidationTime.ON_UI_CONTROL_EDIT);
		ridget.setDirectWriting(true);

		ridget.addValidationMessage("ValidationErrorMessage"); //$NON-NLS-1$

		assertEquals(0, ridget.getMarkers().size());

		UITestHelper.sendString(control.getDisplay(), "1"); //$NON-NLS-1$

		assertEquals(2, ridget.getMarkers().size());
		final Iterator<? extends IMarker> iterator = ridget.getMarkers().iterator();
		while (iterator.hasNext()) {
			final IMarker next = iterator.next();
			assertTrue(next instanceof IMessageMarker);
			final IMessageMarker marker = (IMessageMarker) next;
			assertTrue(marker.getMessage().equals("ValidationErrorMessage") || marker.getMessage().equals("Odd number of characters.")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		UITestHelper.sendString(control.getDisplay(), "2"); //$NON-NLS-1$

		assertEquals(0, ridget.getMarkers().size());
	}

	public void testValidationMessageWithOnUpdateRule() throws Exception {
		final Text control = getWidget();
		final ITextRidget ridget = getRidget();

		ridget.bindToModel(new IntegerBean(12345), IntegerBean.PROP_VALUE);
		ridget.addValidationRule(new EvenNumberOfCharacters(), ValidationTime.ON_UPDATE_TO_MODEL);
		ridget.setDirectWriting(true);

		ridget.addValidationMessage("ValidationErrorMessage"); //$NON-NLS-1$

		assertEquals(0, ridget.getMarkers().size());

		// \r triggers update
		UITestHelper.sendString(control.getDisplay(), "1\r"); //$NON-NLS-1$

		assertEquals(2, ridget.getMarkers().size());
		TestUtils.assertMessage(ridget, ValidationMessageMarker.class, "ValidationErrorMessage"); //$NON-NLS-1$

		// \r triggers update
		UITestHelper.sendString(control.getDisplay(), "2\r"); //$NON-NLS-1$

		assertEquals(0, ridget.getMarkers().size());
	}

	public void testRevalidateOnEditRule() {
		final ITextRidget ridget = getRidget();

		ridget.bindToModel(new IntegerBean(123), IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());

		final IValidator rule = new EvenNumberOfCharacters();
		ridget.addValidationRule(rule, ValidationTime.ON_UI_CONTROL_EDIT);

		assertFalse(ridget.isErrorMarked());

		final boolean isOk1 = ridget.revalidate();

		assertFalse(isOk1);
		assertTrue(ridget.isErrorMarked());

		ridget.removeValidationRule(rule);

		assertFalse(ridget.isErrorMarked()); // since 1.2: remove updates immediately

		final boolean isOk2 = ridget.revalidate();

		assertTrue(isOk2);
		assertFalse(ridget.isErrorMarked());
	}

	public void testRevalidateOnUpdateRule() {
		final ITextRidget ridget = getRidget();

		ridget.bindToModel(new IntegerBean(123), IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());

		final IValidator rule = new EvenNumberOfCharacters();
		ridget.addValidationRule(rule, ValidationTime.ON_UPDATE_TO_MODEL);

		assertFalse(ridget.isErrorMarked());

		final boolean isOk1 = ridget.revalidate();

		assertFalse(isOk1);
		assertTrue(ridget.isErrorMarked());

		ridget.removeValidationRule(rule);

		assertFalse(ridget.isErrorMarked()); // since 1.2: remove updates immediately

		final boolean isOk2 = ridget.revalidate();

		assertTrue(isOk2);
		assertFalse(ridget.isErrorMarked());
	}

	public void testRevalidateDoesUpdate() {
		final ITextRidget ridget = getRidget();
		final Text control = getWidget();
		final EvenNumberOfCharacters evenChars = new EvenNumberOfCharacters();
		ridget.addValidationRule(evenChars, ValidationTime.ON_UI_CONTROL_EDIT);

		final IntegerBean bean = new IntegerBean(12);
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());

		focusIn(control);
		control.selectAll();
		UITestHelper.sendString(control.getDisplay(), "345\t"); //$NON-NLS-1$
		assertEquals("345", control.getText()); //$NON-NLS-1$
		// non-blocking rule, expect 'abc'
		assertEquals("345", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(12), bean.getValue());

		assertTrue(ridget.isErrorMarked());

		ridget.removeValidationRule(evenChars);
		ridget.revalidate();

		assertFalse(ridget.isErrorMarked());
		assertEquals("345", control.getText()); //$NON-NLS-1$
		assertEquals("345", ridget.getText()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(345), bean.getValue());
	}

	public void testReValidationOnUpdateFromModel() {
		final ITextRidget ridget = getRidget();

		final IntegerBean bean = new IntegerBean(12);
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());
		assertEquals("12", ridget.getText()); //$NON-NLS-1$

		final IValidator rule = new EvenNumberOfCharacters();
		ridget.addValidationRule(rule, ValidationTime.ON_UI_CONTROL_EDIT);
		bean.setValue(Integer.valueOf(321));
		ridget.updateFromModel();

		assertTrue(ridget.isErrorMarked());
		assertEquals(Integer.valueOf(321), bean.getValue());
		assertEquals("321", ridget.getText()); //$NON-NLS-1$

		ridget.removeValidationRule(rule);
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());
		assertEquals(Integer.valueOf(321), bean.getValue());
		assertEquals("321", ridget.getText()); //$NON-NLS-1$
	}

	public void testControlNotEditableWithOutputMarker() {
		final ITextRidget ridget = getRidget();
		final Text control = getWidget();

		assertTrue(control.getEditable());

		ridget.setOutputOnly(true);

		assertFalse(control.getEditable());

		ridget.setOutputOnly(false);

		assertTrue(control.getEditable());
	}

	public void testOutputMultipleSelectionCannotBeChangedFromUI() {
		final ITextRidget ridget = getRidget();
		final Text control = getWidget();

		assertEquals("0", control.getText()); //$NON-NLS-1$
		assertEquals("0", ridget.getText()); //$NON-NLS-1$

		ridget.setOutputOnly(true);
		control.selectAll();
		focusIn(control);
		UITestHelper.sendString(control.getDisplay(), "123\t"); //$NON-NLS-1$

		assertEquals("0", control.getText()); //$NON-NLS-1$
		assertEquals("0", ridget.getText()); //$NON-NLS-1$

		ridget.setOutputOnly(false);
		control.selectAll();
		focusIn(control);
		UITestHelper.sendString(control.getDisplay(), "123\t"); //$NON-NLS-1$

		assertEquals("123", control.getText()); //$NON-NLS-1$
		assertEquals("123", ridget.getText()); //$NON-NLS-1$
	}

	public void testDisabledHasNoTextFromModel() {
		if (!MarkerSupport.isHideDisabledRidgetContent()) {
			System.out.println("Skipping TextRidgetTest2.testDisabledHasNoTextFromModel()"); //$NON-NLS-1$
			return;
		}

		final ITextRidget ridget = getRidget();
		final Text control = getWidget();
		final IntegerBean bean = new IntegerBean(INTEGER_TWO);
		ridget.bindToModel(bean, IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertTrue(ridget.isEnabled());
		assertEquals(INTEGER_TWO.toString(), control.getText());
		assertEquals(INTEGER_TWO.toString(), ridget.getText());
		assertEquals(INTEGER_TWO, bean.getValue());

		ridget.setEnabled(false);

		assertEquals("", control.getText()); //$NON-NLS-1$
		assertEquals(INTEGER_TWO.toString(), ridget.getText());
		assertEquals(INTEGER_TWO, bean.getValue());

		bean.setValue(INTEGER_ONE);
		ridget.updateFromModel();

		assertEquals("", control.getText()); //$NON-NLS-1$
		assertEquals(INTEGER_ONE.toString(), ridget.getText());
		assertEquals(INTEGER_ONE, bean.getValue());

		ridget.setEnabled(true);

		assertEquals(INTEGER_ONE.toString(), control.getText());
		assertEquals(INTEGER_ONE.toString(), ridget.getText());
		assertEquals(INTEGER_ONE, bean.getValue());
	}

	public void testMaxLengthWithRule() throws Exception {
		final ITextRidget ridget = getRidget();
		final Text control = getWidget();

		ridget.addValidationRule(new MaxNumberLength(5), ValidationTime.ON_UI_CONTROL_EDIT);

		focusIn(control);
		UITestHelper.sendString(control.getDisplay(), "1234"); //$NON-NLS-1$

		assertEquals(localize("1.234"), control.getText()); //$NON-NLS-1$

		focusOut(control);

		assertEquals(localize("1.234"), ridget.getText()); //$NON-NLS-1$

		focusIn(control);
		control.setSelection(control.getText().length()); // move cursor to end
		UITestHelper.sendString(control.getDisplay(), "5"); //$NON-NLS-1$

		assertEquals(localize("12.345"), control.getText()); //$NON-NLS-1$

		focusOut(control);

		assertEquals(localize("12.345"), control.getText()); //$NON-NLS-1$

		focusIn(control);
		control.setSelection(control.getText().length()); // move cursor to end
		UITestHelper.sendString(control.getDisplay(), "6"); //$NON-NLS-1$

		assertEquals(localize("12.345"), control.getText()); //$NON-NLS-1$

		focusOut(control);

		assertEquals(localize("12.345"), control.getText()); //$NON-NLS-1$
	}

	public void testSetMarkNegative() {
		final INumericTextRidget ridget = getRidget();

		assertTrue(ridget.isMarkNegative());

		ridget.setMarkNegative(false);

		assertFalse(ridget.isMarkNegative());

		ridget.setMarkNegative(true);

		assertTrue(ridget.isMarkNegative());
	}

	public void testNegativeMarkerFromSetText() {
		final INumericTextRidget ridget = getRidget();
		ridget.setMarkNegative(true);

		ridget.setText("100"); //$NON-NLS-1$

		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());

		ridget.setText("-100"); //$NON-NLS-1$

		assertEquals(1, ridget.getMarkersOfType(NegativeMarker.class).size());

		ridget.setText("0"); //$NON-NLS-1$

		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());

		ridget.setText("-0"); //$NON-NLS-1$

		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());

		ridget.setText("-1"); //$NON-NLS-1$
		ridget.setMarkNegative(false);

		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());

		ridget.setMarkNegative(true);

		assertEquals(1, ridget.getMarkersOfType(NegativeMarker.class).size());
	}

	public void testNegativeMarkerFromControl() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();
		final Display display = control.getDisplay();
		ridget.setMarkNegative(true);

		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());

		// direct writing is false, so we update the model pressing '\r' 

		control.setFocus();
		UITestHelper.sendString(display, "123-\r"); //$NON-NLS-1$

		assertEquals(1, ridget.getMarkersOfType(NegativeMarker.class).size());

		control.setSelection(0, 0);
		UITestHelper.sendKeyAction(display, UITestHelper.KC_DEL);
		UITestHelper.sendString(display, "\r"); //$NON-NLS-1$

		assertEquals(0, ridget.getMarkersOfType(NegativeMarker.class).size());

		UITestHelper.sendString(display, "-\r"); //$NON-NLS-1$

		assertEquals(1, ridget.getMarkersOfType(NegativeMarker.class).size());
	}

	public void testRemoveLeadingCruft() {
		assertEquals("-", NumericTextRidget.removeLeadingCruft("-")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("-", NumericTextRidget.removeLeadingCruft("-0")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("0", NumericTextRidget.removeLeadingCruft("0")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("-1", NumericTextRidget.removeLeadingCruft("-01")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("-10", NumericTextRidget.removeLeadingCruft("-010")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("-101", NumericTextRidget.removeLeadingCruft("-0101")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("-23", NumericTextRidget.removeLeadingCruft("-0023")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("1", NumericTextRidget.removeLeadingCruft("01")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("10", NumericTextRidget.removeLeadingCruft("010")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("101", NumericTextRidget.removeLeadingCruft("0101")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("23", NumericTextRidget.removeLeadingCruft("0023")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testDelete() {
		final INumericTextRidget ridget = getRidget();
		ridget.setGrouping(true);
		ridget.setSigned(true);

		assertText("1^.234", UITestHelper.KC_DEL, "1^34"); //$NON-NLS-1$ //$NON-NLS-2$
		assertText("^1.234", UITestHelper.KC_DEL, "^234"); //$NON-NLS-1$ //$NON-NLS-2$
		assertText("12^.345", UITestHelper.KC_DEL, "1.2^45"); //$NON-NLS-1$ //$NON-NLS-2$
		assertText("1.234^.567", UITestHelper.KC_DEL, "123.4^67"); //$NON-NLS-1$ //$NON-NLS-2$
		assertText("1.234.5^67", UITestHelper.KC_DEL, "123.45^7"); //$NON-NLS-1$ //$NON-NLS-2$

		assertText("-1^.234", UITestHelper.KC_DEL, "-1^34"); //$NON-NLS-1$ //$NON-NLS-2$
		assertText("-^1.234", UITestHelper.KC_DEL, "-^234"); //$NON-NLS-1$ //$NON-NLS-2$
		assertText("-1.234.5^67", UITestHelper.KC_DEL, "-123.45^7"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testBackspace() {
		final INumericTextRidget ridget = getRidget();
		ridget.setGrouping(true);
		ridget.setSigned(true);

		assertText("123.^456", "\b", "12^.456"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertText("1.^456", "\b", "^456"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertText("1.234.^567", "\b", "123^.567"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertText("1.23^4", "\b", "12^4"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertText("1.234.56^7", "\b", "123.45^7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		assertText("-1.23^4", "\b", "-12^4"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertText("-1^.234", "\b", "-^234"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertText("-1.234.56^7", "\b", "-123.45^7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testMandatoryMarker() {
		final INumericTextRidget ridget = getRidget();
		ridget.setMandatory(true);

		ridget.setText("123"); //$NON-NLS-1$

		TestUtils.assertMandatoryMarker(ridget, 1, true);

		ridget.setText(null);

		TestUtils.assertMandatoryMarker(ridget, 1, false);

		ridget.setMandatory(false);

		TestUtils.assertMandatoryMarker(ridget, 0, false);
	}

	public void testGetSetMaxLength() {
		final INumericTextRidget ridget = getRidget();

		try {
			ridget.setMaxLength(0);
			fail();
		} catch (final RuntimeException rex) {
			ok();
		}

		final Integer oldValue = Integer.valueOf(ridget.getMaxLength());
		expectPropertyChangeEvent(INumericTextRidget.PROPERTY_MAXLENGTH, oldValue, Integer.valueOf(5));
		ridget.setMaxLength(5);

		verifyPropertyChangeEvents();
		assertEquals(5, ridget.getMaxLength());

		expectNoPropertyChangeEvent();
		ridget.setMaxLength(5);

		verifyPropertyChangeEvents();
	}

	public void testResetMaxLengthToUnlimited() {
		final INumericTextRidget ridget = getRidget();

		Integer oldValue = Integer.valueOf(ridget.getMaxLength());
		expectPropertyChangeEvent(INumericTextRidget.PROPERTY_MAXLENGTH, oldValue, Integer.valueOf(5));
		ridget.setMaxLength(5);
		verifyPropertyChangeEvents();
		assertEquals(5, ridget.getMaxLength());

		oldValue = Integer.valueOf(ridget.getMaxLength());
		expectPropertyChangeEvent(INumericTextRidget.PROPERTY_MAXLENGTH, oldValue, Integer.valueOf(-1));

		ridget.setMaxLength(-1);
		assertEquals(-1, ridget.getMaxLength());

		verifyPropertyChangeEvents();
	}

	public void testMaxLength() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();
		ridget.setMaxLength(6);
		final StringBean bean = new StringBean();
		ridget.bindToModel(bean, StringBean.PROP_VALUE);

		control.setFocus();
		UITestHelper.sendString(control.getDisplay(), localize("12345678-\r")); //$NON-NLS-1$

		assertEquals(localize("-123.456"), control.getText()); //$NON-NLS-1$
		assertEquals(localize("-123.456"), ridget.getText()); //$NON-NLS-1$
		assertEquals(localize("-123.456"), bean.getValue()); //$NON-NLS-1$

		boolean exception = false;
		try {
			ridget.setMaxLength(-2);
		} catch (final IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
		exception = false;
		try {
			ridget.setMaxLength(INumericTextRidget.MAX_LENGTH_UNBOUNDED);
		} catch (final IllegalArgumentException e) {
			exception = true;
		}
		assertFalse(exception);
	}

	public void testExceedMaxLengthWithSetText() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();

		ridget.setMaxLength(3);

		assertEquals(3, ridget.getMaxLength());

		ridget.setText(localize("-123")); //$NON-NLS-1$

		try {
			ridget.setText(localize("1234")); //$NON-NLS-1$
			fail();
		} catch (final RuntimeException rex) {
			// expected
			assertEquals(localize("-123"), ridget.getText()); //$NON-NLS-1$
			assertEquals(localize("-123"), control.getText()); //$NON-NLS-1$
		}
	}

	public void testExceedMaxLengthWithUpdate() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();

		ridget.setMaxLength(3);

		assertEquals(3, ridget.getMaxLength());

		final DoubleBean value = new DoubleBean(123.00d);
		ridget.bindToModel(value, DoubleBean.PROP_VALUE);
		ridget.updateFromModel();

		try {
			value.setValue(1234.12d);
			ridget.updateFromModel();
			fail();
		} catch (final RuntimeException rex) {
			// expected
			assertEquals(localize("123"), ridget.getText()); //$NON-NLS-1$
			assertEquals(localize("123"), control.getText()); //$NON-NLS-1$
		}

		value.setValue(-321d);
		ridget.updateFromModel();
		try {
			value.setValue(1234d);
			ridget.updateFromModel();
			fail();
		} catch (final RuntimeException rex) {
			// expected
			assertEquals(localize("-321"), ridget.getText()); //$NON-NLS-1$
			assertEquals(localize("-321"), control.getText()); //$NON-NLS-1$
		}
	}

	/**
	 * As per Bug 317917
	 */
	public void testSetConvertEmptyToZero() {
		final INumericTextRidget ridget = getRidget();

		assertFalse(ridget.isConvertEmptyToZero());

		ridget.setConvertEmptyToZero(true);

		assertTrue(ridget.isConvertEmptyToZero());

		ridget.setConvertEmptyToZero(false);

		assertFalse(ridget.isConvertEmptyToZero());
	}

	/**
	 * As per Bug 317917
	 */
	public void testSetConvertEmptyToZeroWithSetText() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();

		ridget.setText(null);

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("", control.getText()); //$NON-NLS-1$

		ridget.setText(""); //$NON-NLS-1$

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("", control.getText()); //$NON-NLS-1$

		ridget.setConvertEmptyToZero(true); // value = "" => control = "0,00"

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$

		ridget.setText(null);

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$

		ridget.setText(""); //$NON-NLS-1$

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$

		ridget.setText("0"); //$NON-NLS-1$

		assertEquals("0", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$
	}

	/**
	 * As per Bug 317917
	 */
	public void testSetConvertEmptyDoubleToZeroWithUpdate() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();

		ridget.setConvertEmptyToZero(true);
		final IntegerBean integerBean = new IntegerBean(null);
		ridget.bindToModel(integerBean, IntegerBean.PROP_VALUE);
		ridget.updateFromModel();

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$

		integerBean.setValue(Integer.valueOf(3141));
		ridget.updateFromModel();

		assertEquals(localize("3.141"), ridget.getText()); //$NON-NLS-1$
		assertEquals(localize("3.141"), control.getText()); //$NON-NLS-1$

		integerBean.setValue(Integer.valueOf(0));
		ridget.updateFromModel();

		assertEquals("0", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$
	}

	/**
	 * As per Bug 317917
	 */
	public void testSetConvertEmptyStringToZeroWithUpdate() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();

		ridget.setConvertEmptyToZero(true);
		final StringBean stringBean = new StringBean(null);
		ridget.bindToModel(stringBean, StringBean.PROP_VALUE);
		ridget.updateFromModel();

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$

		stringBean.setValue(localize("3141")); //$NON-NLS-1$
		ridget.updateFromModel();

		assertEquals(localize("3.141"), ridget.getText()); //$NON-NLS-1$
		assertEquals(localize("3.141"), control.getText()); //$NON-NLS-1$

		stringBean.setValue(""); //$NON-NLS-1$
		ridget.updateFromModel();

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$

		stringBean.setValue("0"); //$NON-NLS-1$
		ridget.updateFromModel();

		assertEquals("0", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$
	}

	/**
	 * As per Bug 317917
	 */
	public void testSetConvertEmptyToZeroWithMandatoryMarker() {
		final INumericTextRidget ridget = getRidget();
		ridget.setMandatory(true);
		ridget.setText(""); //$NON-NLS-1$

		TestUtils.assertMandatoryMarker(ridget, 1, false);

		ridget.setConvertEmptyToZero(true);
		ridget.setText(null);

		TestUtils.assertMandatoryMarker(ridget, 1, false);

		ridget.setText("1"); //$NON-NLS-1$

		TestUtils.assertMandatoryMarker(ridget, 1, true);
	}

	/**
	 * As per Bug 317917
	 */
	public void testSetConvertEmptyToZeroDoesNotInterfereWithHiddenValue() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();
		ridget.setText("1"); //$NON-NLS-1$

		assertEquals("1", control.getText()); //$NON-NLS-1$

		ridget.setEnabled(false);

		assertEquals("", control.getText()); //$NON-NLS-1$

		ridget.setConvertEmptyToZero(true);

		assertEquals("", control.getText()); //$NON-NLS-1$

		ridget.setText(null);

		assertEquals("", control.getText()); //$NON-NLS-1$

		ridget.setEnabled(true);

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$
	}

	/**
	 * As per Bug 317917
	 */
	public void testSetConvertEmptyToZeroWhenLosingFocus() {
		final INumericTextRidget ridget = getRidget();
		final Text control = getWidget();
		ridget.setText(localize("3.141")); //$NON-NLS-1$
		ridget.setConvertEmptyToZero(true);

		assertEquals(localize("3.141"), ridget.getText()); //$NON-NLS-1$
		assertEquals(localize("3.141"), control.getText()); //$NON-NLS-1$

		control.selectAll();
		control.setFocus();
		UITestHelper.sendString(control.getDisplay(), "\b\t"); //$NON-NLS-1$

		assertEquals("0", ridget.getText()); //$NON-NLS-1$
		assertEquals("0", control.getText()); //$NON-NLS-1$

		ridget.setConvertEmptyToZero(false);

		control.selectAll();
		control.setFocus();
		UITestHelper.sendString(control.getDisplay(), "\b\t"); //$NON-NLS-1$

		assertEquals("", ridget.getText()); //$NON-NLS-1$
		assertEquals("", control.getText()); //$NON-NLS-1$
	}

	// helping methods
	//////////////////

	private void assertText(final String before, final String keySeq, final String after) {
		TestUtils.assertText(getWidget(), localize(before), keySeq, localize(after));
	}

	private void assertText(final String before, final int keyCode, final String after) {
		TestUtils.assertText(getWidget(), localize(before), keyCode, localize(after));
	}

	private void focusIn(final Text control) {
		control.setFocus();
		assertTrue(control.isFocusControl());
	}

	private void focusOut(final Text control) {
		// clear focus
		UITestHelper.sendString(control.getDisplay(), "\t"); //$NON-NLS-1$
		assertFalse(control.isFocusControl());
	}

	private String localize(final String number) {
		return TestUtils.getLocalizedNumber(number);
	}

	// helping classes
	//////////////////

	private static final class EvenNumberOfCharacters implements IValidator {

		public IStatus validate(final Object value) {
			if (value == null) {
				return ValidationRuleStatus.ok();
			}
			if (value instanceof String) {
				final String string = (String) value;
				if (string.length() % 2 == 0) {
					return ValidationRuleStatus.ok();
				}
				return ValidationRuleStatus.error(false, "Odd number of characters."); //$NON-NLS-1$
			}
			throw new ValidationFailure(getClass().getName() + " can only validate objects of type " + String.class.getName()); //$NON-NLS-1$
		}

	}

	private static final class EndsWithFive implements IValidator {

		public IStatus validate(final Object value) {
			boolean isOk = false;
			String s = null;
			if (value instanceof Number) {
				s = ((Number) value).toString();
			} else if (value instanceof String) {
				s = (String) value;
			}
			if (s != null) {
				final char lastChar = s.charAt(s.length() - 1);
				isOk = lastChar == '5';
			}
			return isOk ? Status.OK_STATUS : Status.CANCEL_STATUS;
		}
	}
}

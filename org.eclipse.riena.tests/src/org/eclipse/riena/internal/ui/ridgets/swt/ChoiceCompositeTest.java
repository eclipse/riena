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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.riena.core.test.RienaTestCase;
import org.eclipse.riena.core.test.collect.UITestCase;
import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.ui.swt.ChoiceComposite;
import org.eclipse.riena.ui.swt.utils.SwtUtilities;

/**
 * Tests for the class {@link ChoiceComposite}.
 */
@UITestCase
public class ChoiceCompositeTest extends RienaTestCase {

	private Shell shell;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final Display display = Display.getDefault();

		shell = new Shell(display);
		shell.setLayout(new FillLayout());

		shell.setSize(130, 100);
		shell.setLocation(0, 0);
		shell.open();
		ReflectionUtils.setHidden(SwtUtilities.class, "cacheDpiFactors", new float[] { 0.0f, 0.0f }); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		shell = null;
		ReflectionUtils.setHidden(SwtUtilities.class, "cacheDpiFactors", new float[] { 0.0f, 0.0f }); //$NON-NLS-1$
		super.tearDown();
	}

	// testing methods
	// ////////////////

	public void testCreateChildWithTextWrapMulti() {
		final ChoiceComposite cc = new ChoiceComposite(shell, SWT.NONE, true);
		cc.setWrapOptionsText(true);

		// this must be a check box
		assertFalse((cc.createChild("label").getStyle() & SWT.CHECK) == 0); //$NON-NLS-1$
		assertTrue((cc.createChild("label").getStyle() & SWT.RADIO) == 0); //$NON-NLS-1$
	}

	public void testCreateChildWithTextWrapSingle() {
		final ChoiceComposite cc = new ChoiceComposite(shell, SWT.NONE, false);
		cc.setWrapOptionsText(true);

		// this must be a radio button
		assertTrue((cc.createChild("label").getStyle() & SWT.CHECK) == 0); //$NON-NLS-1$
		assertFalse((cc.createChild("label").getStyle() & SWT.RADIO) == 0); //$NON-NLS-1$
	}

	public void testCenterButtonsInHorizontalLayout() throws Exception {
		final ChoiceComposite cc = new ChoiceComposite(shell, SWT.NONE, false);
		cc.setOrientation(SWT.HORIZONTAL);
		cc.setWrapOptionsText(true);
		final Button b1 = cc.createChild("multi\nline"); //$NON-NLS-1$
		final Button b2 = cc.createChild("single-line"); //$NON-NLS-1$

		cc.layout(true, true);

		// the multi line button should be positioned at the top,
		// while the single line button will be centered (begins not at the top)
		assertTrue(b1.getBounds().y < b2.getBounds().y);
	}

	/**
	 * Tests the workaround for Bug 400248
	 */
	public void testMultiLineRadioButtons() {
		final ChoiceComposite cc = new ChoiceComposite(shell, SWT.NONE, false);
		cc.setOrientation(SWT.HORIZONTAL);
		cc.setWrapOptionsText(true);
		final Button b1 = cc.createChild("multi\nline"); //$NON-NLS-1$
		final Button b2 = cc.createChild("single-line"); //$NON-NLS-1$

		cc.layout(true, true);

		assertTrue(b1.getBounds().height > b2.getBounds().height);
	}

	public void testConstructor() {
		try {
			new ChoiceComposite(null, SWT.NONE, false);
			fail();
		} catch (final RuntimeException rex) {
			ok();
		}

		final ChoiceComposite control1 = new ChoiceComposite(shell, SWT.NONE, false);
		assertFalse(control1.isMultipleSelection());

		final ChoiceComposite control2 = new ChoiceComposite(shell, SWT.NONE, true);
		assertTrue(control2.isMultipleSelection());
	}

	public void testSetOrientation() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);

		assertEquals(SWT.VERTICAL, control.getOrientation());

		control.setOrientation(SWT.HORIZONTAL);

		assertEquals(SWT.HORIZONTAL, control.getOrientation());
		assertTrue(control.getContentComposite().getLayout() instanceof RowLayout);
		assertEquals(SWT.HORIZONTAL, ((RowLayout) control.getContentComposite().getLayout()).type);

		control.setOrientation(SWT.VERTICAL);

		assertEquals(SWT.VERTICAL, control.getOrientation());
		assertTrue(control.getContentComposite().getLayout() instanceof FillLayout);
		assertEquals(SWT.VERTICAL, ((FillLayout) control.getContentComposite().getLayout()).type);

		try {
			control.setOrientation(SWT.NONE);
			fail();
		} catch (final RuntimeException rex) {
			// expected and unchanged
			assertEquals(SWT.VERTICAL, control.getOrientation());
		}
	}

	public void testSetForeground() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		final Button child1 = new Button(control, SWT.RADIO);

		final Color colorGreen = control.getDisplay().getSystemColor(SWT.COLOR_GREEN);

		assertTrue(!colorGreen.equals(control.getForeground()));

		control.setForeground(colorGreen);

		assertEquals(colorGreen, control.getForeground());
		assertEquals(colorGreen, child1.getForeground());
	}

	public void testSetBackground() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		final Button child1 = new Button(control, SWT.RADIO);

		final Color colorRed = control.getDisplay().getSystemColor(SWT.COLOR_RED);

		assertTrue(!colorRed.equals(control.getBackground()));

		control.setBackground(colorRed);

		assertEquals(colorRed, control.getBackground());
		assertEquals(colorRed, child1.getBackground());
	}

	public void testSetEnabled() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		//		final Button child1 = new Button(control, SWT.RADIO);
		final Button child1 = control.createChild("one"); //$NON-NLS-1$

		assertTrue(control.getEnabled());
		assertTrue(child1.getEnabled());

		control.setEnabled(false);

		assertFalse(control.getEnabled());
		assertFalse(child1.getEnabled());

		control.setEnabled(true);

		assertTrue(control.getEnabled());
		assertTrue(child1.getEnabled());
	}

	/**
	 * As per Bug 321927
	 */
	public void testSetEditable() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, true);
		final Button child1 = control.createChild("one"); //$NON-NLS-1$
		child1.setSelection(true);
		final Button child2 = control.createChild("two"); //$NON-NLS-1$
		child2.setSelection(true);
		final Button child3 = control.createChild("three"); //$NON-NLS-1$
		child3.setSelection(false);

		assertTrue(control.isEnabled());

		assertTrue(control.getEditable());
		assertTrue(child1.isEnabled());
		assertTrue(child2.isEnabled());
		assertTrue(child3.isEnabled());

		control.setEditable(false);

		assertFalse(control.getEditable());
		assertTrue(child1.isEnabled()); // selected
		assertTrue(child2.isEnabled()); // selected
		assertFalse(child3.isEnabled());

		control.setEditable(true);

		assertTrue(control.getEditable());
		assertTrue(child1.isEnabled());
		assertTrue(child2.isEnabled());
		assertTrue(child3.isEnabled());
	}

	/**
	 * As per Bug 321927
	 */
	public void testSetEditableFalseBlocksChangesFromUI() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, true);
		final Button child1 = control.createChild("child1"); //$NON-NLS-1$

		child1.setSelection(true);
		control.setEditable(false);

		assertTrue(child1.getSelection());
		assertFalse(control.getEditable());

		child1.setSelection(false);
		final Event event = new Event();
		event.type = SWT.Selection;
		event.widget = child1;
		event.display = child1.getDisplay();
		child1.notifyListeners(event.type, event);

		// editable = false -> selection reverted
		assertTrue(child1.getSelection());
	}

	/**
	 * As per Bug 321927
	 */
	public void testToggleEditableWhenDisabled() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, true);
		final Button child1 = control.createChild("one"); //$NON-NLS-1$
		child1.setSelection(true);
		final Button child2 = control.createChild("two"); //$NON-NLS-1$
		child2.setSelection(true);
		final Button child3 = control.createChild("three"); //$NON-NLS-1$
		child3.setSelection(false);

		control.setEditable(false);
		control.setEnabled(false);

		assertFalse(child1.isEnabled());
		assertFalse(child2.isEnabled());
		assertFalse(child3.isEnabled());

		control.setEnabled(true);

		assertTrue(child1.isEnabled()); // selected
		assertTrue(child2.isEnabled()); // selected
		assertFalse(child3.isEnabled());

		control.setEditable(true);

		assertTrue(child1.isEnabled());
		assertTrue(child2.isEnabled());
		assertTrue(child3.isEnabled());
	}

	/**
	 * As per Bug 317568.
	 */
	public void testSetMarginsVerticalOrientation() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		assertEquals(SWT.VERTICAL, control.getOrientation());

		assertEquals(0, control.getMargins().x);
		assertEquals(0, control.getMargins().y);
		assertEquals(0, ((FillLayout) control.getContentComposite().getLayout()).marginHeight);
		assertEquals(0, ((FillLayout) control.getContentComposite().getLayout()).marginWidth);

		control.setMargins(10, 20);

		assertEquals(10, control.getMargins().x);
		assertEquals(20, control.getMargins().y);
		assertEquals(10, ((FillLayout) control.getContentComposite().getLayout()).marginHeight);
		assertEquals(20, ((FillLayout) control.getContentComposite().getLayout()).marginWidth);
	}

	/**
	 * As per Bug 317568.
	 */
	public void testSetMarginsHorizontalOrientation() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		control.setOrientation(SWT.HORIZONTAL);
		assertEquals(SWT.HORIZONTAL, control.getOrientation());

		assertEquals(0, control.getMargins().x);
		assertEquals(0, control.getMargins().y);
		assertEquals(0, ((RowLayout) control.getContentComposite().getLayout()).marginHeight);
		assertEquals(0, ((RowLayout) control.getContentComposite().getLayout()).marginWidth);

		control.setMargins(10, 20);

		assertEquals(10, control.getMargins().x);
		assertEquals(20, control.getMargins().y);
		assertEquals(10, ((RowLayout) control.getContentComposite().getLayout()).marginHeight);
		assertEquals(20, ((RowLayout) control.getContentComposite().getLayout()).marginWidth);
	}

	/**
	 * As per Bug 317568.
	 */
	public void testSetMarginsNegativeValue() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);

		try {
			control.setMargins(-5, 10);
			fail();
		} catch (final RuntimeException rex) {
			ok();
		}

		try {
			control.setMargins(10, -5);
			fail();
		} catch (final RuntimeException rex) {
			ok();
		}
	}

	/**
	 * As per Bug 317568.
	 */
	public void testSetSpacingVerticalOrientation() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		assertEquals(SWT.VERTICAL, control.getOrientation());

		assertEquals(3, control.getSpacing().x);
		assertEquals(0, control.getSpacing().y);
		assertEquals(0, ((FillLayout) control.getContentComposite().getLayout()).spacing);

		control.setSpacing(0, 10);

		assertEquals(0, control.getSpacing().x);
		assertEquals(10, control.getSpacing().y);
		assertEquals(10, ((FillLayout) control.getContentComposite().getLayout()).spacing);
	}

	/**
	 * As per Bug 317568.
	 */
	public void testSetSpacingHorizontalOrientation() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		control.setOrientation(SWT.HORIZONTAL);
		assertEquals(SWT.HORIZONTAL, control.getOrientation());

		assertEquals(3, control.getSpacing().x);
		assertEquals(0, control.getSpacing().y);
		assertEquals(3, ((RowLayout) control.getContentComposite().getLayout()).spacing);

		control.setSpacing(10, 0);

		assertEquals(10, control.getSpacing().x);
		assertEquals(0, control.getSpacing().y);
		assertEquals(10, ((RowLayout) control.getContentComposite().getLayout()).spacing);
	}

	/**
	 * As per Bug 317568.
	 */
	public void testSetSpacingNegativeValue() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);

		try {
			control.setSpacing(-5, 10);
			fail();
		} catch (final RuntimeException rex) {
			ok();
		}

		try {
			control.setSpacing(10, -5);
			fail();
		} catch (final RuntimeException rex) {
			ok();
		}
	}

	/**
	 * As per Bug 323449
	 */
	public void testDisabledWidgetHasGrayBackground() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		final Button child1 = control.createChild("child1"); //$NON-NLS-1$

		assertTrue(control.isEnabled());

		final Color defaultBg = control.getBackground();
		final Color defaultChildBg = child1.getBackground();
		final Color disabledBg = control.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

		control.setEnabled(false);

		assertEquals(disabledBg, control.getBackground());
		assertEquals(disabledBg, child1.getBackground());

		control.setEnabled(true);

		assertEquals(defaultBg, control.getBackground());
		assertEquals(defaultChildBg, child1.getBackground());
	}

	/**
	 * As per Bug 323449
	 */
	public void testSetBackgroundColorWhileDisabled() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		final Button child1 = control.createChild("child1"); //$NON-NLS-1$
		final Display display = control.getDisplay();
		final Color disabledBg = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		final Color red = display.getSystemColor(SWT.COLOR_RED);

		control.setEnabled(false);
		control.setBackground(red);

		assertEquals(disabledBg, control.getBackground());
		assertEquals(disabledBg, child1.getBackground());

		control.setEnabled(true);

		assertEquals(red, control.getBackground());
		assertEquals(red, child1.getBackground());
	}

	/**
	 * As per Bug ruv301
	 * 
	 * control should have the same background color as its parent, if it is disabled.
	 */
	public void testSetBackgroundColorWhileDisabledRespectingParentsBackground() {
		final ChoiceComposite control = new ChoiceComposite(shell, SWT.NONE, false);
		final Button child1 = control.createChild("child1"); //$NON-NLS-1$
		final Display display = control.getDisplay();
		final Color red = display.getSystemColor(SWT.COLOR_RED);

		shell.setBackground(red);
		control.setEnabled(false);

		assertEquals(red, control.getBackground());
		assertEquals(red, child1.getBackground());
		assertEquals(red, shell.getBackground());

		control.setEnabled(true);

		assertEquals(red, control.getBackground());
		assertEquals(red, child1.getBackground());
		assertEquals(red, shell.getBackground());

		control.setEnabled(false);

		assertEquals(red, control.getBackground());
		assertEquals(red, child1.getBackground());
		assertEquals(red, shell.getBackground());
	}

	/**
	 * Tests the <i>private</i> method {@code applyLayout()}.
	 * 
	 * @throws Exception
	 *             handled by JUnit
	 */
	public void testApplyLayout() throws Exception {

		ReflectionUtils.setHidden(SwtUtilities.class, "cacheDpiFactors", new float[] { 2.0f, 3.0f }); //$NON-NLS-1$

		final ChoiceComposite cc = new ChoiceComposite(shell, SWT.NONE, true);
		cc.setOrientation(SWT.HORIZONTAL);
		cc.setMargins(11, 12);
		cc.setSpacing(14, 15);

		ReflectionUtils.invokeHidden(cc, "applyLayout"); //$NON-NLS-1$
		assertEquals(RowLayout.class, cc.getContentComposite().getLayout().getClass());
		final RowLayout rowLayout = (RowLayout) cc.getContentComposite().getLayout();
		assertEquals(24, rowLayout.marginWidth);
		assertEquals(33, rowLayout.marginHeight);
		assertEquals(28, rowLayout.spacing);

		cc.setOrientation(SWT.VERTICAL);
		ReflectionUtils.invokeHidden(cc, "applyLayout"); //$NON-NLS-1$
		assertEquals(FillLayout.class, cc.getContentComposite().getLayout().getClass());
		final FillLayout fillLayout = (FillLayout) cc.getContentComposite().getLayout();
		assertEquals(24, fillLayout.marginWidth);
		assertEquals(33, fillLayout.marginHeight);
		assertEquals(45, fillLayout.spacing);

	}

}
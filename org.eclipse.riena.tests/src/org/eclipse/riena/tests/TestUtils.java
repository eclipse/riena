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
package org.eclipse.riena.tests;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import junit.framework.Assert;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Utility class for tests.
 */
public final class TestUtils {

	private TestUtils() {
		// utility class
	}

	/**
	 * TODO [ev] docs
	 */
	public static void assertText(Text control, String before, String keySeq, String after) {
		forceText(control, before);

		checkText(control, before);
		checkSelection(control, before);
		checkCaret(control, before);

		control.setFocus();
		UITestHelper.sendString(control.getDisplay(), keySeq);

		checkText(control, after);
		checkCaret(control, after);
	}

	/**
	 * TODO [ev] docs
	 */
	public static void assertText(Text control, String before, int keyCode, String after) {
		forceText(control, before);

		checkText(control, before);
		checkSelection(control, before);
		checkCaret(control, before);

		control.setFocus();
		UITestHelper.sendKeyAction(control.getDisplay(), keyCode);

		checkText(control, after);
		checkCaret(control, after);
	}

	public static boolean isArabLocaleAvailable() {
		Locale arabLocale = new Locale("ar", "AE");
		for (Locale availableLocale : Locale.getAvailableLocales()) {
			if (availableLocale.equals(arabLocale)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return a localized version of a String representation of a number. The
	 * returned string will use the grouping separator and decimal separator for
	 * the current locale.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>DE - "1.234,56" -&gt; "1.234,56"</li>
	 * <li>US - "1.234,56" -&gt; "1,234.56"</li>
	 * </ul>
	 * 
	 * @param number
	 *            a String representation of a number, where '.' is used as the
	 *            grouping separator and ',' is used as the decimal separator.
	 * @return a localized String representation of a number
	 */
	public static String getLocalizedNumber(String number) {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		char decimalSep = dfs.getDecimalSeparator();
		char groupingSep = dfs.getGroupingSeparator();
		String result = number.replace('.', '_').replace(',', decimalSep).replace('_', groupingSep);
		return result;
	}

	// helping methods
	//////////////////

	private static void checkText(Text control, String input) {
		String expected = removePositionMarkers(input);
		Assert.assertEquals(expected, control.getText());
	}

	private static void checkSelection(Text control, String input) {
		int start = input.indexOf('^');
		int end = input.lastIndexOf('^');
		String expected = "";
		if (start < end) {
			expected = input.substring(start + 1, end);
		}
		// System.out.println("exp sel: " + expected);
		Assert.assertEquals(expected, control.getSelectionText());
	}

	private static void checkCaret(Text control, String input) {
		int start = input.indexOf('^');
		if (start != -1) {
			int end = input.lastIndexOf('^');
			int expected = start < end ? end - 1 : end;
			// System.out.println("exp car: " + expected);
			Assert.assertEquals(expected, control.getCaretPosition());
		}
	}

	private static String removePositionMarkers(String input) {
		StringBuilder result = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);
			if (ch != '^') {
				result.append(ch);
			}
		}
		return result.toString();
	}

	private static void forceText(Text control, String text) {
		int start = text.indexOf('^');
		int end = text.lastIndexOf('^');

		Listener[] listeners = control.getListeners(SWT.Verify);
		for (Listener listener : listeners) {
			control.removeListener(SWT.Verify, listener);
		}
		control.setText(removePositionMarkers(text));
		for (Listener listener : listeners) {
			control.addListener(SWT.Verify, listener);
		}
		control.setFocus();
		if (start == end) {
			control.setSelection(start, start);
		} else {
			control.setSelection(start, end - 1);
		}
	}

}

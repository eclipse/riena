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
package org.eclipse.riena.navigation.model;

import junit.framework.TestCase;

/**
 * Tests of the class {@link ModuleNode}.
 */
public class ModuleNodeTest extends TestCase {

	/**
	 * Tests the method {@code calcDepth()}
	 */
	public void testCalcDepth() {

		ModuleNode m = new ModuleNode();
		assertEquals(0, m.calcDepth());

		SubModuleNode sm = new SubModuleNode();
		m.addChild(sm);
		assertEquals(0, m.calcDepth());

		SubModuleNode sm2 = new SubModuleNode();
		m.addChild(sm2);
		assertEquals(2, m.calcDepth());

		SubModuleNode sm21 = new SubModuleNode();
		sm21.setNavigationProcessor(new NavigationProcessor());
		sm2.addChild(sm21);
		assertEquals(2, m.calcDepth());
		sm2.setExpanded(true);
		assertEquals(3, m.calcDepth());

		sm21.setEnabled(false);
		assertEquals(3, m.calcDepth());
		sm21.setVisible(false);
		assertEquals(2, m.calcDepth());

	}

}

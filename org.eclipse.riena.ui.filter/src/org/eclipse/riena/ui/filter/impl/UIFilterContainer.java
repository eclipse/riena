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
package org.eclipse.riena.ui.filter.impl;

import org.eclipse.riena.ui.filter.IUIFilter;
import org.eclipse.riena.ui.filter.IUIFilterContainer;

/**
 *
 */
public class UIFilterContainer implements IUIFilterContainer {

	String nodeId;
	IUIFilter filter;

	public UIFilterContainer(IUIFilter filter, String nodeId) {
		this.nodeId = nodeId;
		this.filter = filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.ui.filter.IUIFilterContainer#getFilter()
	 */
	public IUIFilter getFilter() {
		return filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.riena.ui.filter.IUIFilterContainer#getFilterTargetNodeId()
	 */
	public String getFilterTargetNodeId() {
		return nodeId;
	}

}

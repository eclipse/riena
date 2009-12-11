/*******************************************************************************
 * Copyright (c) 2007, 2009 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.sample.app.client.mail;

import org.eclipse.riena.navigation.IModuleGroupNode;
import org.eclipse.riena.navigation.IModuleNode;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.model.ModuleNode;
import org.eclipse.riena.navigation.model.SubModuleNode;
import org.eclipse.riena.ui.workarea.WorkareaManager;

/**
 * Factory to help create {@link IModuleNode}s and {@link ISubModuleNode}s.
 */
public final class NodeFactory {

	private NodeFactory() {
		// prevent instantiation
	}

	public static IModuleNode createModule(String caption, IModuleGroupNode parent) {
		IModuleNode result = new ModuleNode(caption);
		parent.addChild(result);
		return result;
	}

	public static ISubModuleNode createSubMobule(String caption, IModuleNode parent, String viewId) {
		ISubModuleNode result = new SubModuleNode(caption);
		// path found via org.eclipse.riena.ui.swt.imagePaths in plugin.xml
		result.setIcon("generic_element.gif"); //$NON-NLS-1$
		parent.addChild(result);
		WorkareaManager.getInstance().registerDefinition(result, viewId);
		return result;
	}
}

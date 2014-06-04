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
package org.eclipse.riena.e4.launcher.rendering;

import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributedPartRenderer;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.e4.launcher.part.PartWrapper;
import org.eclipse.riena.e4.launcher.part.RienaPartHelper;
import org.eclipse.riena.e4.launcher.part.ViewInstanceProvider;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
import org.eclipse.riena.navigation.model.SubModuleNode;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;

/**
 * Riena specific {@link ContributedPartRenderer} honoring shared views. Shared views work by definition of a 1-n relationship between a {@link SubModuleView}
 * and {@link SubModuleNode}s. Every {@link SubModuleNode} is associated to one {@link PartImpl}. Multiple {@link PartImpl}s share one {@link SubModuleView}.
 */
@SuppressWarnings("restriction")
public final class RienaPartRenderer extends ContributedPartRenderer {

	@Override
	public Object createWidget(final MUIElement element, final Object parent) {

		final SwtViewId swtViewId = RienaPartHelper.extractRienaCompoundId(element);

		// is node part?
		if (isSubModuleNodePart(swtViewId)) {
			Composite parentComposite = null;
			final String typeId = swtViewId.getId();
			final String secondayId = swtViewId.getSecondary();
			final ISubModuleNode node = SwtViewProvider.getInstance().getNavigationNode(typeId, secondayId, ISubModuleNode.class);

			// if the node belongs to a shared view try to lookup the view
			if (RienaPartHelper.isSharedView(node)) {
				parentComposite = ViewInstanceProvider.getInstance().getParentComposite(swtViewId);
				if (null != parentComposite) {
					// shared view found
					if (null == element.getWidget()) {
						// register the widget at the current element. This happens only one time for each part.
						element.setWidget(parentComposite);
						ViewInstanceProvider.getInstance().increaseViewCounter(swtViewId);
					}
				}
			}

			// nothing found in cache
			if (null == parentComposite) {
				// create new view
				parentComposite = (Composite) super.createWidget(element, parent);
				// initialize and build view
				initializeView(swtViewId, parentComposite, node);
			} else {
				// Cached view found! update node in view
				updateViewNode(swtViewId, node);
			}
			// observe nodes related to shared views
			if (RienaPartHelper.isSharedView(node)) {
				node.addListener(new SharedViewNodeBinder(swtViewId));
			}
			return parentComposite;
		}

		// not related to a SubModuleNode. Nothing special here. Just create composite
		return super.createWidget(element, parent);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#forceFocus(org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public void forceFocus(final MUIElement element) {
		if (element instanceof MContribution) {
			final Object object = ((MContribution) element).getObject();
			if (object instanceof PartWrapper) {
				((PartWrapper) object).getView().setFocus();
				return;
			}
		}

		super.forceFocus(element);
	}

	private boolean isSubModuleNodePart(final SwtViewId rienaCompoundId) {
		return rienaCompoundId.getSecondary() != null;
	}

	/**
	 * Updates the {@link ISubModuleNode} in the {@link SubModuleView}
	 */
	private void updateViewNode(final SwtViewId swtViewId, final ISubModuleNode node) {
		final SubModuleView viewInstance = ViewInstanceProvider.getInstance().getView(swtViewId);
		viewInstance.setNavigationNode(node);
	}

	/**
	 * Initializes and builds the related {@link SubModuleView}. This involves calling {@link SubModuleView#createPartControl(Composite)}
	 * 
	 */
	private void initializeView(final SwtViewId swtViewId, final Composite parentComposite, final ISubModuleNode node) {
		final ViewInstanceProvider viewInstanceProvider = ViewInstanceProvider.getInstance();
		final SubModuleView viewInstance = viewInstanceProvider.getView(swtViewId);
		viewInstance.setE4Runtime(true);
		viewInstanceProvider.registerParentComposite(swtViewId, parentComposite);
		updateViewNode(swtViewId, node);
		ReflectionUtils.invokeHidden(viewInstance, "setShellProvider", RienaPartHelper.toShellProvider(parentComposite.getShell())); //$NON-NLS-1$
		viewInstance.createPartControl(parentComposite);
	}

	/**
	 * After activation of a {@link SubModuleNode} this listener binds the corresponding shared view to the node
	 */
	private final class SharedViewNodeBinder extends SubModuleNodeListener {

		private final SwtViewId swtViewId;

		private SharedViewNodeBinder(final SwtViewId swtViewId) {
			this.swtViewId = swtViewId;
		}

		@Override
		public void activated(final ISubModuleNode source) {
			final SubModuleView viewInstance = ViewInstanceProvider.getInstance().getView(swtViewId);
			viewInstance.setNavigationNode(source);
			viewInstance.bind(source);
		}
	}

}
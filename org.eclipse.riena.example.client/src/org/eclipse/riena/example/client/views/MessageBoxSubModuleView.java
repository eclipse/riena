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
package org.eclipse.riena.example.client.views;

import org.eclipse.riena.example.client.controllers.MessageBoxSubModuleController;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;
import org.eclipse.riena.ui.swt.MessageBox;
import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;
import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MessageBoxSubModuleView extends SubModuleView<MessageBoxSubModuleController> {

	public static final String ID = MessageBoxSubModuleView.class.getName();

	private static final GridData GD21LEFT = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
	private static final GridData GD230 = new GridData();

	static {
		GD230.widthHint = 230;
	}

	@Override
	protected void basicCreatePartControl(Composite parent) {

		parent.setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.SUB_MODULE_BACKGROUND));
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 10;
		parent.setLayout(layout);

		Label explanation = UIControlsFactory.createLabel(parent, "Message displayed in a popup dialog"); //$NON-NLS-1$
		explanation.setFont(new Font(explanation.getDisplay(), "Arial", 8, SWT.BOLD)); //$NON-NLS-1$
		explanation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

		UIControlsFactory.createLabel(parent, "Title"); //$NON-NLS-1$
		Text messageTitle = UIControlsFactory.createText(parent);
		messageTitle.setLayoutData(GD230);
		addUIControl(messageTitle, "messageTitle"); //$NON-NLS-1$

		UIControlsFactory.createLabel(parent, "Text"); //$NON-NLS-1$
		Text messageText = UIControlsFactory.createText(parent);
		messageText.setLayoutData(GD230);
		addUIControl(messageText, "messageText"); //$NON-NLS-1$

		UIControlsFactory.createLabel(parent, "Type"); //$NON-NLS-1$
		Combo messageType = UIControlsFactory.createCombo(parent);
		messageType.setLayoutData(GD230);
		addUIControl(messageType, "messageType"); //$NON-NLS-1$

		UIControlsFactory.createLabel(parent, "Options"); //$NON-NLS-1$
		Combo messageOptions = UIControlsFactory.createCombo(parent);
		messageOptions.setLayoutData(GD230);
		addUIControl(messageOptions, "messageOptions"); //$NON-NLS-1$

		Button showMessage = UIControlsFactory.createButton(parent);
		showMessage.setLayoutData(GD21LEFT);
		addUIControl(showMessage, "showMessage"); //$NON-NLS-1$

		UIControlsFactory.createLabel(parent, "Selected Option"); //$NON-NLS-1$
		Text selectedOption = UIControlsFactory.createText(parent);
		selectedOption.setLayoutData(GD230);
		addUIControl(selectedOption, "selectedOption"); //$NON-NLS-1$

		MessageBox messageBox = new MessageBox(parent);
		messageBox.setPropertyName("messageBox"); //$NON-NLS-1$
		addUIControl(messageBox, "messageBox"); //$NON-NLS-1$
	}
}

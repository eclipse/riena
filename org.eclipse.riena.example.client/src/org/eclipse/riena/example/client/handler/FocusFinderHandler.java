package org.eclipse.riena.example.client.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class FocusFinderHandler extends AbstractHandler {
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Control focusControl = Display.getCurrent().getFocusControl();
		if (focusControl == null) {
			System.err.println("no control has the focus?!?");
		} else {
			printFocus(focusControl, "Focus ");
		}
		return null;
	}

	private void printFocus(final Control control, final String indent) {
		System.err.print(indent);
		System.err.println("at " + control + " bindingID=" + control.getData("binding_property"));
		if (control.getParent() != null) {
			printFocus(control.getParent(), "      ");
		}
	}
}

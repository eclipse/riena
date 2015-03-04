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
package org.eclipse.riena.internal.ui.ridgets.annotation.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.eclipse.riena.core.annotationprocessor.AnnotatedOverriddenMethodsGuard;
import org.eclipse.riena.ui.ridgets.IActionListener;
import org.eclipse.riena.ui.ridgets.IClickableRidget;
import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.IRidgetContainer;
import org.eclipse.riena.ui.ridgets.annotation.OnDoubleClick;
import org.eclipse.riena.ui.ridgets.annotation.handler.AbstractRidgetContainerAnnotationHandler;

/**
 * Annotation handler for {@code @OnDoubleClick}
 * 
 * @since 3.0
 */
public class DoubleClickAnnotationHandler extends AbstractRidgetContainerAnnotationHandler {

	@Override
	public void handleAnnotation(final Annotation annotation, final IRidgetContainer ridgetContainer,
			final Object target, final Method targetMethod, final AnnotatedOverriddenMethodsGuard guard) {

		if (annotation instanceof OnDoubleClick) {
			final IRidget ridget = getRidget(annotation, targetMethod, ridgetContainer,
					((OnDoubleClick) annotation).ridgetId());
			if (ridget instanceof IClickableRidget) {
				((IClickableRidget) ridget).addDoubleClickListener(createListener(IActionListener.class, "callback", //$NON-NLS-1$
						target, targetMethod));
			} else {
				errorUnsupportedRidgetType(annotation, ridget);
			}
		}
	}
}

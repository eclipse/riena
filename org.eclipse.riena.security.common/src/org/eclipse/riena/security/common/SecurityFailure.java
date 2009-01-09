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
package org.eclipse.riena.security.common;

import org.eclipse.riena.core.exception.Failure;

/**
 * The mother of all security related failures
 * 
 */
public class SecurityFailure extends Failure {

	private static final long serialVersionUID = -4828670387343650064L;

	/**
	 * Creates a new instance of <code>SecurityFailure</code>
	 * 
	 * @param msg
	 *            message text or message code
	 * @param args
	 *            message parameters
	 * @param cause
	 *            exception which has caused this Failure
	 */
	public SecurityFailure(String msg, Object[] args, Throwable cause) {
		super(msg, args, cause);
	}

	/**
	 * Creates a new instance of <code>SecurityFailure</code>
	 * 
	 * @param msg
	 *            message text or message code
	 */
	public SecurityFailure(String msg) {
		super(msg);
	}

	/**
	 * Creates a new instance of <code>SecurityFailure</code>
	 * 
	 * @param msg
	 *            message text or message code
	 * @param cause
	 *            exception which has caused this Failure
	 */
	public SecurityFailure(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Creates a new instance of <code>SecurityFailure</code>
	 * 
	 * @param msg
	 *            message text or message code
	 * @param arg1
	 *            message parameter 1
	 * @param cause
	 *            exception which has caused this Failure
	 */
	public SecurityFailure(String msg, Object arg1, Throwable cause) {
		super(msg, arg1, cause);
	}

	/**
	 * Creates a new instance of <code>SecurityFailure</code>
	 * 
	 * @param msg
	 *            message text or message code
	 * @param arg1
	 *            message parameter 1
	 * @param arg2
	 *            message parameter 2
	 * @param cause
	 *            exception which has caused this Failure
	 */
	public SecurityFailure(String msg, Object arg1, Object arg2, Throwable cause) {
		super(msg, arg1, arg2, cause);
	}

}

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
package org.eclipse.riena.monitor.client;

import org.eclipse.equinox.log.ExtendedLogEntry;

/**
 * Instances of this interface can decide whether a {@code logEntry} should be collected or not.
 * 
 * @since 3.0
 */
public interface ILogServiceCollectorFilter {

	/**
	 * Shall this entry be collected?
	 * 
	 * @param entry
	 *            the (extended) log entry
	 * @return on {@code true} do collect; otherwise do not.
	 */
	boolean isCollectible(ExtendedLogEntry entry);

}

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
package org.eclipse.riena.demo.server;

import java.util.List;

import org.eclipse.riena.demo.common.Customer;
import org.eclipse.riena.demo.common.ICustomerService;

/**
 *
 */
public class CustomerService implements ICustomerService {

	private ICustomerRepository repository;

	/**
	 * 
	 * @param repository
	 */
	public CustomerService() {
		System.out.println("customer service started");
	}

	public void bind(ICustomerRepository repository) {
		System.out.println("customer service:repository bound");
		this.repository = repository;
	}

	public void unbind(ICustomerRepository repository) {
		System.out.println("customer service:repository unbound");
		this.repository = null;
	}
	
	public ICustomerRepository getRepository() {
		return repository;
	}

	/**
	 * 
	 * 
	 */
	public List<Customer> search(String lastName) {
		// check parameter
		
		// perform search
		return repository.search(lastName);
	}

	public boolean store(Customer customer) {
		// checkCustomer object
		try {
			repository.store(customer);
			return true;
		} catch (RuntimeException r) {
			return false;
		}
	}
}
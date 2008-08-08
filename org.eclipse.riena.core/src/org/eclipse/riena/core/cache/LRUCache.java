/*******************************************************************************
 * Copyright (c) 2007 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.core.cache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.equinox.log.Logger;
import org.eclipse.riena.core.cache.internal.ICacheEntry;
import org.eclipse.riena.core.cache.internal.SimpleCacheEntry;
import org.eclipse.riena.internal.core.Activator;
import org.osgi.service.log.LogService;

/**
 * LRU=(Last Recently Used) Implementation for IGenericObjectCache (alternative
 * to GenericObjectCache)
 * 
 * @author Christian Campo
 */
public class LRUCache implements IGenericObjectCache {

	private final static Logger LOGGER = Activator.getDefault().getLogger(GenericObjectCache.class.getName());
	private LinkedHashMap lruMap = null;
	private long timeout;
	/** minimum count of entries to keep * */
	private int minimumSize;
	private int statHit;
	private int statNotFound;
	private int statTimeout;
	private static int statDisplayCount;
	private String name = "LRUCache : "; //$NON-NLS-1$

	/**
	 * 
	 */
	public LRUCache() {
		super();
		LOGGER.log(LogService.LOG_INFO, "creating new LRUCache instance"); //$NON-NLS-1$
	}

	public void setHashMap(HashMap<Object, Object> map) {
		if (!(map instanceof LRUHashMap)) {
			throw new RuntimeException("global Hashmap must be a LRUHashMap"); //$NON-NLS-1$
		}
		lruMap = (LRUHashMap) map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.core.cache.IGenericObjectCache#isGlobalCache()
	 */
	public boolean isGlobalCache() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.core.cache.IGenericObjectCache#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name + " : "; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.riena.core.cache.IGenericObjectCache#setTimeout(int)
	 */
	public void setTimeout(int milliseconds) {
		LOGGER.log(LogService.LOG_INFO, "setTimeout = " + milliseconds); //$NON-NLS-1$
		timeout = milliseconds;
	}

	public int getTimeout() {
		return (int) timeout;
	}

	public synchronized Object get(Object key) {
		LOGGER.log(LogService.LOG_DEBUG, "get = " + key); //$NON-NLS-1$
		ICacheEntry entry = (SimpleCacheEntry) lruMap.get(key);
		/** do we find the entry * */
		if (entry == null) {
			statNotFound++;
			printStat();
			return null;
		}
		long timePassed = System.currentTimeMillis() - entry.getTimestamp();
		/** is the entry expired * */
		if (timePassed < timeout) {
			/** does the soft reference still point anywhere * */
			statHit++;
			printStat();
			return entry.getValue();
		} else {
			remove(key);
			statTimeout++;
			printStat();
			return null;
		}
	}

	public synchronized Object get(Object key, Class callingClass) {
		return get(key);
	}

	private void printStat() {
		statDisplayCount++;
		if (statDisplayCount > 100) {
			LOGGER.log(LogService.LOG_INFO, name + "Hit / NotFound / Timeout " + statHit + " / " + statNotFound + " / " + statTimeout); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			statDisplayCount = 0;
		}
	}

	public String getStatistic() {
		return name + "Hit / NotFound / Miss / Timeout " + statHit + " / " + statNotFound + " / " + statTimeout; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see org.eclipse.riena.core.cache.IGenericObjectCache#put(Object,
	 *      java.lang.Object)
	 */
	public synchronized void put(Object key, Object value) {
		LOGGER.log(LogService.LOG_DEBUG, "put = " + key + ", " + value); //$NON-NLS-1$ //$NON-NLS-2$
		lruMap.put(key, new SimpleCacheEntry(value, key));
	}

	/**
	 * @see org.eclipse.riena.core.cache.IGenericObjectCache#clear()
	 */
	public synchronized void clear() {
		LOGGER.log(LogService.LOG_DEBUG, "clear"); //$NON-NLS-1$
		lruMap.clear();
	}

	/**
	 * @see org.eclipse.riena.core.cache.IGenericObjectCache#remove(Object)
	 */
	public synchronized void remove(Object key) {
		LOGGER.log(LogService.LOG_DEBUG, "remove = " + key); //$NON-NLS-1$
		lruMap.remove(key);
	}

	/**
	 * @see org.eclipse.riena.core.cache.IGenericObjectCache#size()
	 */
	public synchronized int size() {
		LOGGER.log(LogService.LOG_DEBUG, "size <= " + lruMap.size()); //$NON-NLS-1$
		return lruMap.size();
	}

	public int getSize() {
		return size();
	}

	/**
	 * @see org.eclipse.riena.core.cache.IGenericObjectCache#setMinimumSize(int)
	 */
	public void setMinimumSize(int minSize) {
		LOGGER.log(LogService.LOG_INFO, "setMinSize = " + minSize); //$NON-NLS-1$
		minimumSize = minSize;
		lruMap = new LRUHashMap(minSize);
	}

	public int getMinimumSize() {
		return minimumSize;
	}

	/**
	 * HashMap that implements a Last Recently Used schema on a LinkedHashMap
	 */
	public static class LRUHashMap extends LinkedHashMap {
		private int minSize;

		/**
		 * @param minSize
		 */
		public LRUHashMap(int minSize) {
			super(minSize, .75F, true);
			this.minSize = minSize;
		}

		/**
		 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
		 */
		public boolean removeEldestEntry(Map.Entry eldest) {
			return size() > minSize;
		}
	}

}
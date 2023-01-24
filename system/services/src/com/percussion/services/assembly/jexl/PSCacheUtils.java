/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import org.apache.log4j.Logger;

import java.io.NotSerializableException;
import java.io.Serializable;

public class PSCacheUtils extends PSJexlUtilBase {
	
	private static Logger logger = Logger.getLogger(PSCacheUtils.class.getName());
	private static final String VELOCITY_PREV_CACHE = "sys_VelocityPreviewCache"; 
	private static final String VELOCITY_PUB_CACHE = "sys_VelocityPublishCache";
	private IPSCacheAccess cache = null;
	
	/**
	 * Method to get the cache object from provided cache name. 
	 * @param key
	 * @param cacheName
	 * @return
	 */
	@IPSJexlMethod(description = "get the value for a key from cache", params =
		   {@IPSJexlParam(name = "key", description = "the key to get the value cached"),
			@IPSJexlParam(name = "cacheName", description = "the name of the cache where to get the value from")})
	public Object get(String key, String cacheName) {
		
		//return PSCacheAccessUtils.getObject(key);
		return getCache().get(key, cacheName);
	}
	
	/**
	 * Method to put a cache object in the provided cache name.
	 * @param key
	 * @param value
	 * @param cacheName
	 */
	@IPSJexlMethod(description = "put a value for a key in the cache", params =
		   {@IPSJexlParam(name = "key", description = "the key to put the value in the cache"),
			@IPSJexlParam(name = "value", description = "the value for the key to be put in the cache"),
			@IPSJexlParam(name = "cacheName", description = "the name of the cache where to save the value to")})
	public void put(String key, Object value, String cacheName) {
		
		if(value instanceof Serializable)
			getCache().save(key, (Serializable)value, cacheName);
		else {
			logger.error("The object to be stored in the cache must be serializable.", new NotSerializableException());
		}
	}
	
	/**
	 * Method to flush all objects from the provided cache name.
	 * @param cacheName
	 */
	@IPSJexlMethod(description = "get the value for a key from cache", params = 
		{@IPSJexlParam(name = "cacheName", description = "the name of the cache from where all the cache objects need to be cleared")})
	public void flush(String cacheName) {
		getCache().clear(cacheName);
	}
	
	/**
	 * Method to flush cache for a region. This will clear the cache object with the provided key from the provided cache name.
	 * @param key
	 * @param cacheName
	 */
	@IPSJexlMethod(description = "flush the value for a key from the cache", params =
		   {@IPSJexlParam(name = "key", description = "the key for which the value should be flushed"),
			@IPSJexlParam(name = "cacheName", description = "the name of the cache from where the cache object needs to be cleared")})
	public void flush(String key, String cacheName) {
		
		getCache().evict(key, cacheName);
	}
	
	/**
	 * The maximum number of seconds an element can exist in the cache regardless of use. 
	 * The element expires at this limit and will no longer be returned from the cache. 
	 * The default value is 0, which means no TTL eviction takes place (infinite lifetime).
	 * 
	 * @param key
	 * @param region
	 * @param timeToLiveSeconds
	 */
	@IPSJexlMethod(description = "set the time for expiration by setting the time to live for the cache entry", params =
		   {@IPSJexlParam(name = "key", description = "the key for which the value should be flushed"),
			@IPSJexlParam(name = "cacheName", description = "the name of the cache from where the cache object needs to be cleared"),
			@IPSJexlParam(name = "timeToLiveSeconds", description = "the maximum number of seconds an element can exist in the cache regardless of use")})
	public void setTimeToLive(String key, String cacheName, int timeToLiveSeconds) {
		getCache().setTimeToLive(key, cacheName, timeToLiveSeconds);
	}
	
	/**
	 * The maximum number of seconds an element can exist in the cache without being accessed. 
	 * The element expires at this limit and will no longer be returned from the cache. 
	 * The default value is 0, which means no TTI eviction takes place (infinite lifetime).
	 * 
	 * @param key
	 * @param region
	 * @param timeToIdleSeconds
	 */
	@IPSJexlMethod(description = "set the time for expiration by setting the time to live for the cache entry", params =
		   {@IPSJexlParam(name = "key", description = "the key for which the value should be flushed"),
			@IPSJexlParam(name = "cacheName", description = "the name of the cache from where the cache object needs to be cleared"),
			@IPSJexlParam(name = "timeToIdleSeconds", description = "maximum number of seconds an element can exist in the cache without being accessed")})
	public void setTimeToIdle(String key, String cacheName, int timeToIdleSeconds) {
		getCache().setTimeToIdle(key, cacheName, timeToIdleSeconds);
	}
	
	public static String getVelocityPrevCache() {
		return VELOCITY_PREV_CACHE;
	}

	public static String getVelocityPubCache() {
		return VELOCITY_PUB_CACHE;
	}
	
	public IPSCacheAccess getCache() {
		return PSCacheAccessLocator.getCacheAccess();
	}

	public void setCache(IPSCacheAccess cache) {
		this.cache = cache;
	}
}

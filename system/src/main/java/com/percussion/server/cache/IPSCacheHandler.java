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

package com.percussion.server.cache;

import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.server.PSBaseResponse;
import com.percussion.server.PSCachedResponse;
import com.percussion.util.PSCacheException;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * Handles caching and retrieval of certain types of request responses.
 */
public interface IPSCacheHandler
{
   /**
    * Retrieves a cached response based on the request in the supplied cache
    * context.
    * 
    * @param context The current runtime context from which the appropriate keys 
    * are extracted to use when retrieving the item.  May not be 
    * <code>null</code>.
    * {@link #isRequestCacheable(PSCacheContext) isRequestCacheable} should be 
    * called before calling this method to ensure the current request is 
    * cacheable.
    * 
    * @return The reponse, or <code>null</code> if a response is not found in
    * the cache based on the keys extracted from the supplied 
    * <code>context</code>.
    * 
    * @throws IllegalArgumentException if <code>context</code> is 
    * <code>null</code> or if the current request is not cacheable. 
    * @throws PSCacheException if there are any errors.
    */
   public PSCachedResponse retrieveResponse(PSCacheContext context) 
      throws PSCacheException;
   
   
   /**
    * Caches the supplied response object.
    * 
    * @param context The runtime context from which the appropriate keys are 
    * extracted to use when storing the item.  May not be <code>null</code>.  
    * {@link #isRequestCacheable(PSCacheContext) isRequestCacheable} should be 
    * called before calling this method to ensure the current request is 
    * cacheable. 
    * {@link #isResponseCacheable(PSBaseResponse) isResponseCacheable} should be 
    * called to ensure the response used to create the cached reponse is 
    * cacheable so the caller does not unnecessarily convert the response to a
    * cached response.
    * 
    * @param response The cacheable form of the <code>PSResponse</code> object
    * that will be stored in the cache.  May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if either param is <code>null</code> or
    * if the current request is not cacheable. 
    * @throws PSCacheException if there are any errors.
    */
   public void storeResponse(PSCacheContext context, PSCachedResponse response)
      throws PSCacheException;
      
   
   /**
    * Determines if the request in the supplied cache context may be 
    * cached.  Only certain types of requests may be cached by each handler.
    * 
    * @param context The current runtime context that will be used to either 
    * store or retrieve a cached response.  May not be <code>null</code>.
    * 
    * @return <code>true</code> if the current request is cacheable, 
    * <code>false</code> if not.
    * 
    * @throws IllegalArgumentException if <code>context</code> is 
    * <code>null</code>.
    */
   public boolean isRequestCacheable(PSCacheContext context);
      

   /**
    * Determines if the supplied response may be cached.  Not all responses are
    * cacheable.
    * 
    * @param response The response that will be used to create a cached 
    * response to store.  May not be <code>null</code>.
    * 
    * @return <code>true</code> if the response is cacheable, <code>false</code> 
    * if not.
    * 
    * @throws IllegalArgumentException if <code>response</code> is 
    * <code>null</code>.
    */
   public boolean isResponseCacheable(PSBaseResponse response);
   
   /**
    * Retrieves a cached document based on the request in the supplied cache
    * context.
    * 
    * @param context The current runtime context from which the appropriate keys 
    * are extracted to use when retrieving the item.  May not be 
    * <code>null</code>.
    * {@link #isRequestCacheable(PSCacheContext) isRequestCacheable} should be 
    * called before calling this method to ensure the current request is 
    * cacheable.
    * 
    * @return The document, or <code>null</code> if a document is not found in
    * the cache based on the keys extracted from the supplied 
    * <code>context</code>.
    * 
    * @throws IllegalArgumentException if <code>context</code> is 
    * <code>null</code> or if the current request is not cacheable. 
    * @throws PSCacheException if there are any errors.
    */   
   public Document retrieveDocument(PSCacheContext context)
      throws PSCacheException;
   
   /**
    * Caches the supplied document.
    * 
    * @param context The runtime context from which the appropriate keys are 
    * extracted to use when storing the item.  May not be <code>null</code>.  
    * {@link #isRequestCacheable(PSCacheContext) isRequestCacheable} should be 
    * called before calling this method to ensure the current request is 
    * cacheable. 
    * 
    * @param response The document that will be stored in the cache.  May not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if either param is <code>null</code> or
    * if the current request is not cacheable. 
    * @throws PSCacheException if there are any errors.
    */
   public void storeDocument(PSCacheContext context, Document doc)
      throws PSCacheException;
      
   /**
    * Retrieves cached results based on the request in the supplied cache
    * context.
    * 
    * @param context The current runtime context from which the appropriate keys 
    * are extracted to use when retrieving the item.  May not be 
    * <code>null</code>.
    * {@link #isRequestCacheable(PSCacheContext) isRequestCacheable} should be 
    * called before calling this method to ensure the current request is 
    * cacheable.
    * 
    * @return The result data, or <code>null</code> if results are not found in
    * the cache based on the keys extracted from the supplied 
    * <code>context</code>.
    * 
    * @throws IllegalArgumentException if <code>context</code> is 
    * <code>null</code> or if the current request is not cacheable. 
    * @throws PSCacheException if there are any errors.
    */   
   public PSCachedResultPage retrieveMergedResults(PSCacheContext context)
      throws PSCacheException;   
   
   /**
    * Caches the supplied result data.
    * 
    * @param context The runtime context from which the appropriate keys are 
    * extracted to use when storing the item.  May not be <code>null</code>.  
    * {@link #isRequestCacheable(PSCacheContext) isRequestCacheable} should be 
    * called before calling this method to ensure the current request is 
    * cacheable. 
    * 
    * @param data The data that will be stored in the cache.  May not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if either param is <code>null</code> or
    * if the current request is not cacheable. 
    * @throws PSCacheException if there are any errors.
    */
   public void storeMergedResults(PSCacheContext context, PSCachedResultPage data)
      throws PSCacheException;
      
   /**
    * Determines if the supplied result data may be cached.  Not all results are
    * cacheable.
    * 
    * @param data The data that will be used to create a cached 
    * document or merged result to store.  May not be <code>null</code>.
    * 
    * @return <code>true</code> if the data is cacheable, <code>false</code> 
    * if not.
    * 
    * @throws IllegalArgumentException if <code>response</code> is 
    * <code>null</code>.
    */
   public boolean isResultDataCacheable(byte[] bytes);
 
   /**
    * Validates that the supplied keys represent all keys required by this
    * handler to flush cached items, as well as any values the handler may
    * require.  This is the map passed to {@link PSCacheManager#flush(Map)}.
    * See {@link #getKeyNames()} for more information.
    *
    * @param keys A map of key names and their values both as 
    * <code>String</code> objects. May not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>keys</code> is 
    * <code>null</code>.
    *
    * @throws PSSystemValidationException if <code>keys</code> does not have an entry
    * for each named key expected by the handler, or if any keys with required
    * values are <code>null</code>.
    */
   public void validateKeys(Map keys)
      throws PSSystemValidationException;
   
   /**
    * Gets the type of caching the handler will perform.  Each handler must
    * return a unique value.
    * 
    * @return The type, never <code>null</code> or empty.
    */
   public String getType();
   
   /**
    * Get the "signature" of the keys used by this handler to flush items.  This 
    * is repesented by the number of keys and their names.  An entry for each
    * key name returned must be supplied in the <code>Map</code> passed to 
    * {@link PSCacheManager#flush(Map)} for the handler to process it.  See
    * {@link #validateKeys(Map)} for more information.
    * 
    * @return An array of key names.  Never <code>null</code>, with no 
    * <code>null</code> or empty entries.
    */
   public String[] getKeyNames();  
   
}

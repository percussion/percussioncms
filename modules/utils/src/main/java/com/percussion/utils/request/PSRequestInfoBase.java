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
package com.percussion.utils.request;


import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class stores a variety of per request information. This is initialized
 * in a servlet at the start of a request, and cleared at the end of the
 * request. The per-request information is stored locally to the thread and is
 * therefore not accessible to other requests.
 * <P>
 * Information is stored under keys. The available keys are string values that
 * are defined on this class. The values are specified as part of the per-key
 * documentation. Note that not every key is necessarily available in every
 * environment this class is used in.
 * 
 * @author dougrand
 */
public class PSRequestInfoBase
{
    public static final String USER_SESSION_OBJECT_SYS_LANG = "sys_lang";
   
   /**
    * The authentication cookie for this request stored as a string value.
    */
   public static final String KEY_JSESSIONID = "JSESSIONID";

   /**
    * The name of the authenticated user from the session of the request
    * associated with the current thread, may not match the result of the
    * getRemoteUser() method on the original servlet request that initiated
    * processing for the current thread.
    */
   public static final String KEY_USER = "USER";

   /**
    * The request associated with the current thread. May not be the original request
    * that initiated request processing for the current thread.
    */
   public static final String KEY_PSREQUEST = "PSREQUEST";

   public static final String KEY_ORIG_MAP = "ORIG_MAP";
   
   /**
    * Setting this in the Request Info causes the PSUserSession lasst accessed to not
    * be touched during the lifecycle of the request.  Used when returning the sessioncheck request
    */
   public static final String KEY_NOSESSIONTOUCH = "NOSESSIONTOUCH";
   
   public static final String KEY_RX_REQUEST_CONTEXT = "RX_REQUEST_CONTEXT";
   
   
   /**
    * Holds per thread information in a {@link Map}. The map is initialized in
    * {@link #initRequestInfo(Map)}and is reset in {@link #resetRequestInfo()}
    * and should never be <code>null</code> between these two calls
    */
   private static ThreadLocal<Map<String,Object>> mapThreadLocal =
      new ThreadLocal<>();

   
   /**
    * Is the TLS inited ?
    */
   private static ThreadLocal<Boolean> isInitedThreadLocal = new ThreadLocal<>();
   static {
      isInitedThreadLocal.set(false);
   }
   /**
    * test the boolean value to see if this object is inited.
    * @return <code>true</code> if inited else <code>false</code>
    */
   public static boolean isInited()
   {
      Boolean initVal = isInitedThreadLocal.get();
      if ( initVal == null )
         return false;
      return initVal;
   }
   
   /**
    * Subject information for this request. Stored as 
    * {@link javax.security.auth.Subject}
    */
   public static final String SUBJECT = "SUBJECT";

   /**
    * Locale information 
    */
   public static final String KEY_LOCALE = "LOCALE";

   public static final String KEY_PSREQUESTCONTEXT = "REQUEST_CONTEXT";
   public static final String KEY_USER_INFO = "USER_INFO";
   public static final String KEY_USER_ACCESS_LEVEL = "USER_ACCESS_LEVEL";

   

   /**
    * Provide initial data for this thread's request. Call
    * {@link #resetRequestInfo()}at the end of the request. It is illegal to
    * call this if the request is currently initialized.
    * 
    * @param initialData initial data, may be <code>null</code>. If
    *           <code>null</code> then this method will create an empty
    *           {@link Map}to hold any further request information.
    */
   public static void initRequestInfo(Map<String,Object> initialData)
   {
      if (initialData == null)
         initialData = new HashMap<>();
      Map<String,Object> map = mapThreadLocal.get();
      if (map != null)
         throw new IllegalStateException("Must call reset first");
      mapThreadLocal.set(initialData);
      isInitedThreadLocal.set(true);
   }

   /**
    * Get the specified piece of request info.
    * 
    * @param key the key, never <code>null</code> or empty
    * @return the value, may be <code>null</code>
    */
   public static Object getRequestInfo(String key)
   {
      if (StringUtils.isEmpty(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      Map<String,Object> map = mapThreadLocal.get();
      if (map == null)
         return null;
      return map.get(key);
   }
   
  
   public static Map<String,Object> getRequestInfoMap()
   {
      return mapThreadLocal.get();
   }
   /**
    * Copies the request info. Modifications to this collection
    * will not change the original request info map.
    * @return maybe null.
    */
   public static Map<String, Object> copyRequestInfoMap() {
      Map<String,Object> map = mapThreadLocal.get();
      if (map == null)
         return null;
      return new HashMap<>(map);
   }

   /**
    * Set the specified piece of request info.
    * 
    * @param key the key, never <code>null</code> or empty
    * @param value the value, may be <code>null</code>
    */
   public static void setRequestInfo(String key, Object value)
   {
      if (StringUtils.isEmpty(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      Map<String,Object> map = mapThreadLocal.get();
      if (map == null)
         throw new IllegalStateException("Must call init first");
      if (value == null)
      {
         map.remove(key);
         if(key.equals(KEY_PSREQUEST))
            map.remove(KEY_PSREQUESTCONTEXT);
      }
      else
         map.put(key, value);
     
   }

   /**
    * Reset per thread request info to <code>null</code>. It is illegal to
    * call this method if the request is not initialized.
    */
   public static void resetRequestInfo()
   {
      Map<String,Object> map = mapThreadLocal.get();
      if (map == null)
         return; // do nothing there is nothing in the map.
      mapThreadLocal.remove();
      isInitedThreadLocal.set(false);
   }

}

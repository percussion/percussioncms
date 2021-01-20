/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
   static public final String USER_SESSION_OBJECT_SYS_LANG = "sys_lang";
   
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
    * The request associated with the current thread. Stored as
    * {@link com.percussion.server.PSRequest}.  May not be the original request
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
   private static ThreadLocal<Map<String,Object>> ms_map = 
      new ThreadLocal<Map<String,Object>>();

   
   /**
    * Is the TLS inited ?
    */
   private static ThreadLocal<Boolean> m_isInited = new ThreadLocal<Boolean>();
   static {
      m_isInited.set(false);
   }
   /**
    * test the boolean value to see if this object is inited.
    * @return <code>true</code> if inited else <code>false</code>
    */
   public static boolean isInited()
   {
      Boolean initVal = (Boolean)m_isInited.get();
      if ( initVal == null )
         return false;
      return initVal.booleanValue();
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
         initialData = new HashMap<String,Object>();
      Map<String,Object> map = ms_map.get();
      if (map != null)
         throw new IllegalStateException("Must call reset first");
      ms_map.set(initialData);
      m_isInited.set(true);
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
      Map<String,Object> map = ms_map.get();
      if (map == null)
         return null;
      return map.get(key);
   }
   
  
   public static Map<String,Object> getRequestInfoMap()
   {
      return ms_map.get();
   }
   /**
    * Copies the request info. Modifications to this collection
    * will not change the original request info map.
    * @return maybe null.
    */
   public static Map<String, Object> copyRequestInfoMap() {
      Map<String,Object> map = ms_map.get();
      if (map == null)
         return null;
      return new HashMap<String, Object>(map);
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
      Map<String,Object> map = ms_map.get();
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
      Map<String,Object> map = ms_map.get();
      if (map == null)
         return; // do nothing there is nothing in the map.
      ms_map.set(null);
      m_isInited.set(false);
   }

}

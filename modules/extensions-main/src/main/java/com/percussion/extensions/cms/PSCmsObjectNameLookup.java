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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.extensions.cms;


import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class implements the UDF processor interface so it can be used as a
 * Rhythmyx function. See {@link #processUdf(Object[], IPSRequestContext)
 * processUdf} for a description. This UDF is sepcifically designed to get name
 * of the CMS object given the Locator for the object. The locator is assumed to
 * have not more than three parts.
 * <p>
 * Note: This is very inefficient for the fact that it is a UDF which will be
 * called per roww in the result set and we make an internal request per row.
 * Ideally, we need to cache all CMS namelookups during server initialization
 * and avoid internal requests. In that this is a temporary feature.
 */
public class PSCmsObjectNameLookup extends PSSimpleJavaUdfExtension
   implements IPSUdfProcessor
{
   //Implementation of the Interface
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      //We need at least the Cms Object Name and the one locator key part
      if ( null == params || params.length < 2 ||
           null == params[0] || 0 == params[0].toString().trim().length() ||
           null == params[1] || 0 == params[1].toString().trim().length())
         return "";

      String cmsObjectType = params[0].toString().trim();
      String keyPart1="", keyPart2 = "", keyPart3 = "";

      if(params.length > 1)
         keyPart1 = params[1] != null ? params[1].toString() : "";
      if(params.length > 2)
         keyPart2 = params[2] != null ? params[2].toString() : "";
      if(params.length > 3)
         keyPart3 = params[3] != null ? params[3].toString() : "";

      Map cache = initLookupCache(request);
      String objectName = null;
      String cacheKey = getCacheKey(cmsObjectType, keyPart1, keyPart2, 
         keyPart3);

      objectName = (String)cache.get(cacheKey);
      
      if (objectName == null)
      {
         Map lookupParams = new HashMap();
         lookupParams.put("keyPart1", keyPart1);
         lookupParams.put("keyPart2", keyPart2);
         lookupParams.put("keyPart3", keyPart3);
   
         String resource = APP_CMS_LOOKUP + "/" + cmsObjectType;
         IPSInternalRequest iReq = null;
         Document resDoc = null;
         Element elem = null;
         try
         {
            iReq = request.getInternalRequest(resource, lookupParams, false);
            if(iReq != null)
            {
               iReq.makeRequest();
               resDoc = iReq.getResultDoc();
               if(resDoc != null)
               {
                  NodeList nl = resDoc.getElementsByTagName("CmsObject");
                  elem = (Element)nl.item(0);
                  if(elem != null)
                  {
                     objectName = elem.getAttribute("objectName");
                     cache.put(cacheKey, objectName);
                  }
               }
            }
         }
         catch(Exception e)
         {
            PSConsole.printMsg(this.getClass().getName(), e);
            //Return empty result
            objectName = "";
         }
         finally
         {
            if(iReq != null)
            {
               iReq.cleanUp();
               iReq = null;
            }
         }
      }
      return objectName;
   }
   
   /**
    * Initializes the cache in the current request. If this extension is invoked
    * more than once per overall request, but via more than one internal
    * requests, since the cache is initialized in a clone of the request it's
    * lifetime is then tied to each internal request and not the overall
    * request. In this case this method should be called with the top level
    * request object before the internal requests are made so that each cloned
    * request used by the internal request has the same instance of the cache
    * in it's private objects.
    * 
    * @param request The current request, may not be <code>null</code>.
    * 
    * @return The map used as the cache, never <code>null</code>.
    */
   public static Map initLookupCache(IPSRequestContext request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      Map cache = (Map)request.getPrivateObject(CACHE_KEY);
      if (cache == null)
      {
         cache = new HashMap();
         request.setPrivateObject(CACHE_KEY, cache);
      }
      
      return cache;
   }
   
   /**
    * Creates a key to use to cache the name retrieved from the lookup.
    * 
    * @param cmsObjectType The object type used for the lookup, assumed not 
    * <code>null</code> or empty.
    * @param part1 The first key used in the lookup, assumed not 
    * <code>null</code>, may be empty.
    * @param part2 The second key used in the lookup, assumed not 
    * <code>null</code>, may be empty.
    * @param part3 The third key used in the lookup, assumed not 
    * <code>null</code>, may be empty.
    * 
    * @return The key to use, never <code>null</code> or empty.
    */
   private String getCacheKey(String cmsObjectType, String part1, String part2, 
      String part3)
   {
      return cmsObjectType + "/" + part1 + "/" + part2 + "/" + part3;
   }
   
   //String constant representing the lookup Rx application
   static private final String APP_CMS_LOOKUP = "sys_psxCms";
   
   /**
    * Constant for key used to store the cache in the request.
    */
   private static final String CACHE_KEY = "sys_cmsObjectNameLookupCache";
}


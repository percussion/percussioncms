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
package com.percussion.cas;

import com.percussion.data.PSConversionException;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.uicontext.PSContextMenu;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * UDF to get the base URL string for a given site.
 * <p>
 * The site can be specified as the first parameter to the UDF which is the
 * siteid for which the base URL is being requested. If not specified, it will
 * be taken from the request context as
 * {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID sys_siteid}
 * parameter. This, if supplied must be a vlid siteid from the system.
 * <p>
 * The second parameter, which is optional can be used to specify to modify the
 * base URL if the context is for an intra-site. The context is treated as
 * intra-site if the originating siteid is not <code>null</code> or empty and
 * matches with the current siteid. The originating siteid as always read from
 * the request as the html parameter
 * {@link com.percussion.util.IPSHtmlParameters#SYS_ORIGINALSITEID}. The value
 * should be a string "yes" to modify the URL (case insensitive) and any other
 * value or no value is treated as "no". If the value is "yes", the URL string
 * will be modified to chop the protocol, host and port and return only the part
 * after those. If the final result is "/" an empty string is returned.
 * <p>
 * If the current siteid is not supplied and does not exist in the request, the
 * base URL will be returned as an empty string without any processing.
 * <p>
 * An internal request is executed to lookup the site attributes.
 */
public class PSGetSiteBaseUrl implements IPSUdfProcessor
{
   /**
    * Implementation of the method in the interface. See method description for
    * more details of the implementation.
    * 
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(
    *      java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      String siteid = request.getParameter(IPSHtmlParameters.SYS_SITEID);
      if (params.length > 0 && params[0].toString().length() > 0)
         siteid = params[0].toString();
      if (siteid == null || siteid.length() < 1)
      {
         //Site is not supplied return empty string.
         log.warn("siteid is not supplied and not available in the request context. Returning empty string");
         return "";
      }
      boolean modifyUrl = false;
      if (params.length > 1)
      {
         String modify = params[1].toString().trim();
         modifyUrl = modify.equalsIgnoreCase("yes");
      }
      try
      {
         String baseUrl = PSUtils.getbaseUrl(siteid, request);
         if (!modifyUrl) //not asked to modify the url, return as it is
            return baseUrl;

         String originalSiteid = request
               .getParameter(IPSHtmlParameters.SYS_ORIGINALSITEID, "");
         //Modify only if original site exists and is equal to current siteid.
         if ("".equals(originalSiteid) || originalSiteid.equals(siteid))
         {
            int loc = baseUrl.indexOf("//");
            if(loc != -1)
            {
               String temp = baseUrl.substring(loc+2);
               loc = temp.indexOf('/');
               if(loc != -1 && temp.length()>1)
                  
                  baseUrl = temp.substring(loc);
               else
                  baseUrl = "";
            }
         }
         return baseUrl;
      }
      catch (PSInternalRequestCallException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
         request.printTraceMessage(e.getMessage());
         throw new PSConversionException(e.getErrorCode(), e
               .getErrorArguments());
      }
      catch (PSNotFoundException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
         request.printTraceMessage(e.getMessage());
         throw new PSConversionException(e.getErrorCode(), e
               .getErrorArguments());
      }
   }



   /**
    * Implementation of the method in the interface. Does nothing for now.
    * 
    * @see com.percussion.extension.IPSExtension#init(
    *      com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
   {
   }


   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger log = LogManager.getLogger(PSGetSiteBaseUrl.class);

}

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
package com.percussion.cas;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSAssemblyLocation;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * The default location generator for content assemblers.
 */
public class PSDefaultAssemblyLocation implements IPSAssemblyLocation
{

   private static final Logger log = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);

   // See interface for details
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      m_def = def;
   }

   /**
    * This implementation takes 3 parameters, does a few sanity checks and 
    * then concatenates them together with the contentid. Provided parameters 
    * with backslashes will be transfomed to use forward slashes only. All 
    * parameters are required. The location url generated will have the 
    * following format:
    * root + path + contentid + suffix (e.g. /xRoads/feature/art123.html)
    * <p>
    * See {@link IPSAssemblyLocation#createLocation(Object[], IPSRequestContext) createLocation} 
    * for details.
    *
    * @param params [0] root the site root which makes up the start of the
    *    location url created. Forward and backward slashes are allowed, if 
    *    it does not ent with a path delimiter, one will be added.
    *    params[1] path the resource path which makes up the middle part
    *    of the created location url. Forward and backward slashes are 
    *    allowed. The path can be provided with or without path delimiter
    *    at its start and/or end.
    *    params[2] suffix the resource suffix which makes up the end part
    *    of the created location url. It can be provided with or without 
    *    suffix delimiter.
    * @param request The request context, never null.
    */
   public String createLocation(Object[] params, IPSRequestContext request)
      throws PSExtensionException
   {
      String exitName = getClass().getName();
      request.printTraceMessage("Entering " + exitName + ".createLocation");

      String location = "";
      try
      {
         // check the number of parameters provided is correct
         if (params.length < EXPECTED_NUMBER_OF_PARAMS)
         {
            Object[] args =
            { 
               "" + EXPECTED_NUMBER_OF_PARAMS,
               "" + params.length
            };
            throw new PSExtensionException(
               IPSExtensionErrors.EXT_PARAM_VALUE_MISMATCH, args);
         }

         String root = params[0].toString().replace('\\', '/');
         if (!root.endsWith(PATH_DELIMITER))
            root += PATH_DELIMITER;
         
         String path = params[1].toString().replace('\\', '/');
         if (path.startsWith(PATH_DELIMITER))
            path = path.substring(path.indexOf(PATH_DELIMITER) + PATH_DELIMITER.length());
         if (path.endsWith(PATH_DELIMITER))
            path = path.substring(0, path.lastIndexOf(PATH_DELIMITER));
         
         String suffix = params[2].toString();
         if (!suffix.startsWith(SUFFIX_DELIMITER))
            suffix = SUFFIX_DELIMITER + suffix;
         
         String contentid = PSHtmlParameters.get(
            IPSHtmlParameters.SYS_CONTENTID, request);
         
         location = root + path + contentid + suffix;
         request.printTraceMessage("Location= " + location);
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         request.printTraceMessage("Error: " + PSExceptionUtils.getMessageForLog(e));
      }
      finally
      {
         request.printTraceMessage("Leaving " + exitName + ".createLocation");
      }
      return location;
   }
   
   /**
    * This is the definition for this extension. You may want to use it for
    * validation purposes in the <code>createLocation</code> method.
    */
   protected IPSExtensionDef m_def = null;

   /**
    * The number of expected parameters.
    */
   private static final int EXPECTED_NUMBER_OF_PARAMS = 3;
   
   /**
    * Path delimiter, nerver <code>null</code>.
    */
   private static final String PATH_DELIMITER = "/";
   
   /**
    * Suffix delimiter, never <code>null</code>.
    */
   private static final String SUFFIX_DELIMITER = ".";
}

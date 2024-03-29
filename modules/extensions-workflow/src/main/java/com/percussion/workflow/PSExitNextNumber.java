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

package com.percussion.workflow;

import com.percussion.cms.IPSConstants;
import com.percussion.data.PSIdGenerator;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * This extension returns the value of a counter obtained from a database
 * stored procedure.
 */
public class PSExitNextNumber implements IPSRequestPreProcessor
{
   private static final String GLOBAL_KEY = "RXKEYGLOBAL";

   /* Set the parameter count to not initialized */
   private  int ms_correctParamCount = NOT_INITIALIZED;

   private static final Logger log = LogManager.getLogger(IPSConstants.WORKFLOW_LOG);

   /**************  IPSExtension Interface Implementation ************* */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      if (ms_correctParamCount == NOT_INITIALIZED)
      {
         ms_correctParamCount = 0;

         Iterator iter = extensionDef.getRuntimeParameterNames();
         while(iter.hasNext())
         {
            iter.next();
            ms_correctParamCount++;
         }
      }
   }

   // This is the main request processing handler (see IPSRequestPreProcessor)
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      if(null == request)
      {
         Object args[] = {
            ms_exitName,
            "The request must not be null" };
         log.error("Exit: {} Error: {}", args[0],args[1]);
         throw new PSExtensionProcessingException(
            IPSExtension.ERROR_INVALID_PARAMETER, args);
      }

      if(null == params) {
         log.debug("No parameters specified so skipping exit: {}", ms_exitName);
         return; //no parameters - exit with peace!
      }
      String lang = (String)request.getSessionPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;

      int nParamCount = params.length;
      String htmlParamName, htmlParamKeyName;

      if(ms_correctParamCount != nParamCount)
         throw new PSParameterMismatchException(lang, ms_correctParamCount,
            nParamCount);

      if(null == params[0] || 0 == params[0].toString().trim().length())
      {
         String key =
          Integer.toString(IPSExtensionErrors.HTML_PARAM_NULL1);
         String msg = PSI18nUtils.getString(key, lang);
         Object args[] = {ms_exitName,msg};
         throw new PSExtensionProcessingException(lang,
          IPSExtension.ERROR_INVALID_PARAMETER, args);
      }

      htmlParamName = params[0].toString();

      if(null == params[1] || 0 == params[1].toString().trim().length())
         htmlParamKeyName = GLOBAL_KEY;
      else
         htmlParamKeyName = params[1].toString();

      /*
       * Replace nulls, empties, and zeros in the named request parameter
       * with unique numbers.
       */
      Object[] htmlParamValues = request.getParameterList( htmlParamName );
      if (htmlParamValues == null || htmlParamValues.length == 0)
         htmlParamValues = new Object[] { null };
      for (int i = 0; i < htmlParamValues.length; i++)
      {
         Object value = htmlParamValues[i];
         if (value == null || value.toString().trim().length() == 0 ||
             value.toString().trim().equals("0"))
         {
            htmlParamValues[i] = getNextNumber(htmlParamKeyName).toString();
         }
      }
      request.setParameterList( htmlParamName, htmlParamValues );
   }

   /**
    * Get the next number for the supplied key.
    *
    * @param key the key to get the next number for, not <code>null</code>
    *    or empty.
    * @return the next number.
    * @throws PSExtensionProcessingException if anything goes wrong looking
    *    up the next number for the provided key.
    * @throws IllegalArgumentException if the supplied key is <code>null</code>
    *    or empty.
    */
   public static Integer getNextNumber(String key)
      throws PSExtensionProcessingException
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      try
      {
         //This is using hibernate
         return PSIdGenerator.getNextId(key);
      }
      catch (SQLException e)
      {

         throw new PSExtensionProcessingException(ms_exitName, e);
      }
   }

   private static final String ms_exitName = "PSExitNextNumber";
}


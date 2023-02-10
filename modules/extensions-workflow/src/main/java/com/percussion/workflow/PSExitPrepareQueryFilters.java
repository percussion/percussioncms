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

import com.percussion.extension.*;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class PSExitPrepareQueryFilters implements IPSRequestPreProcessor
{
   /*role delimiter (in role list) */
   private static final String ROLE_DELIMITER = ",";

   /* Set the parameter count to not initialized */
   static private int ms_correctParamCount = NOT_INITIALIZED;

   /**************  IPSExtension Interface Implementation ************* */

   // See Interface for description
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

   // See Interface for description
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      request.printTraceMessage("executing prepareQueryFilter... ");

      if(null == request)
      {
         Object args[] = {
            ms_exitName,
            "The request must not be null" };

         throw new PSExtensionProcessingException(
            IPSExtension.ERROR_INVALID_PARAMETER, args);
      }

      if(null == params)
         return; //no parameters - exit with peace!
      String lang = (String)request.getSessionPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;
      Map<String,Object> htmlParams = request.getParameters();
      if(null == htmlParams) //we must have htmlParms map
      {
         htmlParams = new HashMap<String,Object>();
         request.setParameters(htmlParams);
      }

      int nParamCount = params.length;
      String sUserName, sRoleNameList;

      if(ms_correctParamCount != nParamCount)
      {
         throw new PSParameterMismatchException(lang, ms_correctParamCount,
            nParamCount);
      }

      if(null == params[0] || 0 == params[0].toString().trim().length())
      {
         String key =
          Integer.toString(IPSExtensionErrors.EMPTY_USRNAME1);
         String msg = PSI18nUtils.getString(key, lang);
         Object args[] = {ms_exitName, msg};
         throw new PSExtensionProcessingException(lang,
          IPSExtension.ERROR_INVALID_PARAMETER, args);
      }

      if(null == params[1] || 0 == params[1].toString().trim().length())
      {
         String key =
          Integer.toString(IPSExtensionErrors.ROLELIST_EMPTY);
         String msg = PSI18nUtils.getString(key, lang);
         Object args[] = {ms_exitName, msg};
         throw new PSExtensionProcessingException(lang,
            IPSExtension.ERROR_INVALID_PARAMETER, args);
      }

      sUserName = params[0].toString();
      sUserName = PSWorkFlowUtils.filterUserName(sUserName);
      sRoleNameList = params[1].toString();

      String msg = null;
      for(int i=0; i<ms_correctParamCount; i++)
      {
         if(null == params[i])
            msg = "null";
         else
            msg = params[i].toString();
      }

      htmlParams.put(PSWorkFlowUtils.properties.getProperty(
         "HTML_PARAM_USER_FULLNAME"), sUserName);

      htmlParams.put(PSWorkFlowUtils.properties.getProperty(
         "HTML_PARAM_USER_ROLELIST"), makeQueryString(sRoleNameList));

      return;
   }

   private String makeQueryString(String sRoleList)
   {
      String sResult = "";
      StringTokenizer sTokenizer =
            new StringTokenizer(sRoleList, ROLE_DELIMITER);

      while(sTokenizer.hasMoreElements())
         sResult += "'" + sTokenizer.nextToken().trim() + "',";

      int nLoc = sResult.lastIndexOf(',');

      if(-1 != nLoc)
         sResult = sResult.substring(0,nLoc);

      return sResult;
   }

   private static String ms_exitName = "PSExitPrepareQueryFilters";
}

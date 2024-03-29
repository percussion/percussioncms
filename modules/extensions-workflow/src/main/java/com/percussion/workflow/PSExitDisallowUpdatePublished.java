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

import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is an extension that is part of Rhythmyx workflow engine. The
 * purpose of this extension is to restrict updating a document in workflow
 * when it is in state that is publishable.
 */
public class PSExitDisallowUpdatePublished implements IPSRequestPreProcessor
{
   private static final Logger ms_log = LogManager.getLogger(PSExitDisallowUpdatePublished.class);
   /* Set the parameter count to not initialized */
   static private int ms_correctParamCount = NOT_INITIALIZED;

   /**************  IPSExtension Interface Implementation ************* */
   public void init(IPSExtensionDef extensionDef, 
         @SuppressWarnings("unused") File file)
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

   /**
    * This overrides the method in the original interface and is called by the
    * server while processign the request.
    *
    * @param params - array of objects that are parameters to the extension
    *
    * @param request - request context (IPSRequestContext)
    *
    * @throws  PSExtensionProcessingException
    *
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      request.printTraceMessage("executing disallowUpdatePublished... ");

      if(null == request)
      {
         Object[] args = {
            ms_exitName,
            "The request must not be null" };
         throw new PSExtensionProcessingException(
            IPSExtension.ERROR_INVALID_PARAMETER,
            args);
      }

      String lang = (String)request.getSessionPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;

      if(null == params)
      {
         String key = Integer.toString(IPSExtensionErrors.EXIT_PARAM_NULL);
         String msg = PSI18nUtils.getString(key, lang);
         Object[] args = {ms_exitName, msg};

         throw new PSExtensionProcessingException(lang,
            IPSExtension.ERROR_INVALID_PARAMETER, args);
      }

      if(ms_correctParamCount != params.length)
         throw new PSParameterMismatchException(lang, ms_correctParamCount,
            params.length);

      if(null == params[0] ||
         0 == params[0].toString().trim().length())
      {
         String key = Integer.toString(IPSExtensionErrors.CONTENTID_NULL);
         String msg = PSI18nUtils.getString(key, lang);
         Object[] args = {ms_exitName,msg};
         throw new PSExtensionProcessingException(lang,
            IPSExtension.ERROR_INVALID_PARAMETER, args);
      }

      int contentid = Integer.parseInt(params[0].toString());

      PSConnectionMgr connectionMgr = null;

      try
      {
         //Get the connection
         connectionMgr = new PSConnectionMgr();
         Connection connection = connectionMgr.getConnection();
         disallowUpdatePublished(contentid, connection, lang);
      }
      catch(Exception e)
      {
         throw new PSExtensionProcessingException(
            ms_exitName,e);
      }
      finally
      {
         try
         {
            if(null != connectionMgr)
               connectionMgr.releaseConnection();
         }
         catch(SQLException sqe)
         {
            // Ignore, we are done.
         }
      }

      return;
   }

   /**
    * Check the specified item and throws {@link PSExtensionProcessingException}
    * if the item is in public state.
    *
    * @param contentid the id of the item in question.
    * @param connection the connection used to get the status of the specified
    *    item; assumed not <code>null</code>.
    * @param lang the locale that is used to fetch the error message should
    *    error occurs.
    *
    * @throws PSExtensionProcessingException if the specified item is in 
    *    publishable state.
    */
   private void disallowUpdatePublished(int contentid, Connection connection,
    String lang)
      throws SQLException, PSExtensionProcessingException
   {
      try
      {
         PSContentStatusContext csc = new PSContentStatusContext(connection,
            contentid);
         csc.close(); //release the JDBC resources

         int nWorkFlowAppID = csc.getWorkflowID();
         int nStateID = csc.getContentStateID();
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         IPSStatesContext sc = cms.loadWorkflowState(nWorkFlowAppID,
            nStateID);

         if (sc == null)
         {
            ms_log.error("Failure loading state information");
         }
         else if(true == sc.getIsValid())
         {
            String key =
            Integer.toString(IPSExtensionErrors.PUBDOC_UPDATE_ERROR);
            String msg = PSI18nUtils.getString(key, lang);
            throw new PSExtensionProcessingException(lang, ms_exitName,
               new Exception(msg));
         }
      }
      catch(PSEntryNotFoundException e)
      {
         //no entry for this content so proceed with peace!
      }

      return;
   }

   private static String ms_exitName = "PSExitDisallowUpdatePublished";
}

/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.workflow;

import com.percussion.extension.*;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSPreparedStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This extension returns the value of a counter obtained using
 * max(primarykeycolumn) + 1 with matching workflowid
 */
public class PSExitNextNumberMaxPP implements IPSRequestPreProcessor
{

   private static final Logger log = LogManager.getLogger(PSExitNextNumberMaxPP.class);

   /* Set the parameter count to not initialized */
   static private int ms_correctParamCount = NOT_INITIALIZED;

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
         throw new PSExtensionProcessingException(
            IPSExtension.ERROR_INVALID_PARAMETER,
            args);
      }

      Map<String,Object> htmlParams = request.getParameters();

      if(null == params)
         return; //no parameters - exit with peace!

      if(null == htmlParams)
      {
         htmlParams = new HashMap<>();
         request.setParameters(htmlParams);
      }

      String lang = (String)request.getSessionPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;
      int nParamCount = params.length;
      String htmlParamName, htmlParamValue, htmlParamKeyName;

      if(ms_correctParamCount != nParamCount)
      {
         throw new PSParameterMismatchException(lang, ms_correctParamCount,
            nParamCount);
      }

      try
      {
         if(null == params[0] || 0 == params[0].toString().trim().length())
         {
            throw new PSInvalidParameterTypeException(lang,
             IPSExtensionErrors.HTML_PARAM_NULL2);
         }

         String sHtmlParamName = params[0].toString();

         String sWorkflowID = "0";
         if(null != params[1] && 0 != params[1].toString().trim().length())
         {
            sWorkflowID = params[1].toString();
         }

         int nWorkflowid = 0;
         try
         {
            nWorkflowid = Integer.parseInt(sWorkflowID);
         }
         catch(Exception e){}

         String sTableName = "";
         if(null == params[2] || 0 == params[2].toString().trim().length())
         {
            throw new PSInvalidParameterTypeException(lang,
             IPSExtensionErrors.TABLE_NAME_NULL);
         }
         else
            sTableName = params[2].toString();

         if(null == params[3] || 0 == params[3].toString().trim().length())
         {
            throw new PSInvalidParameterTypeException(lang,
             IPSExtensionErrors.PRIMARY_KEY_NULL);
         }

         String sPrimaryKeyColumn = params[3].toString();

         String sPrimaryKeyValue = (String)htmlParams.get(sHtmlParamName);

         if((null == sPrimaryKeyValue)||(sPrimaryKeyValue.equalsIgnoreCase("0"))||
            (0 == sPrimaryKeyValue.length()))
         {
            htmlParams.put(sHtmlParamName, getNextNumber(sTableName,
               sPrimaryKeyColumn, nWorkflowid));
         }
      }
      catch(PSInvalidParameterTypeException te)
      {
         Object args[] = {
            ms_exitName,
            te.toString() };
         String language = te.getLanguageString();
         if (language == null)
            language = PSI18nUtils.DEFAULT_LANG;
         throw new PSExtensionProcessingException(language,
          IPSExtension.ERROR_INVALID_PARAMETER, args);
      }
   }

   /**
    * getNextNumber executes the stored procedure and returns the number.
    *
    */
   public static synchronized Integer getNextNumber(String sTable,
         String sPrimaryCol, int workflowid)
      throws PSExtensionProcessingException
   {
      PSConnectionMgr connectionMgr = null;
      Connection conn = null;
      try
      {
         connectionMgr = new PSConnectionMgr();
         conn = connectionMgr.getConnection();
      }
      catch(Exception e)
      {
         if(null != connectionMgr)
         {
            try{ connectionMgr.releaseConnection();}
               catch(Exception ee){}
         }
         throw new PSExtensionProcessingException(ms_exitName, e);
      }
      Integer iResult;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      int rowcount = 0;

      try
      {
         sTable = PSConnectionMgr.getQualifiedIdentifier(sTable);
         sPrimaryCol = sTable + "." + sPrimaryCol;
         String query = "SELECT MAX(" + sPrimaryCol + ") FROM " + sTable;

         if(!sPrimaryCol.toUpperCase().endsWith(".WORKFLOWAPPID"))
            query += " WHERE " + sTable + ".WORKFLOWAPPID = ?";

         stmt = PSPreparedStatement.getPreparedStatement(conn, query);

         if(!sPrimaryCol.toUpperCase().endsWith(".WORKFLOWAPPID"))
            stmt.setInt(1, workflowid);

         rs = stmt.executeQuery();
         if(false == rs.next())
            iResult = new Integer(1);
         else
            iResult = new Integer(rs.getInt(1)+1);
      }
      catch (SQLException e)
      {
         throw new PSExtensionProcessingException(ms_exitName,e);
      }
      finally
      {
         if(null != rs)
            try {rs.close();} catch (Throwable T) {
               log.error(T.getMessage());
               log.debug(T.getMessage(), T);
            };
         if(null != stmt)
            try {stmt.close();} catch (Throwable T) {
               log.error(T.getMessage());
               log.debug(T.getMessage());
            };

         try
         {
            if(null!=connectionMgr)
               connectionMgr.releaseConnection();
         }
         catch(SQLException sqe)
         {
         }
      }
      return iResult;
   }

   private static String ms_exitName = "PSExitNextNumberMaxPP";
}

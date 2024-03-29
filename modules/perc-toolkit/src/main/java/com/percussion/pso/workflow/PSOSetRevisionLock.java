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
/*
 * com.percussion.consulting.workflow PSOSetRevisionLock.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.services.PSDatabasePool;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;

/**
 * A simple workflow action to set the revision lock. 
 *
 * @author DavidBenua
 *
 */
public class PSOSetRevisionLock extends PSDefaultExtension
      implements
         IPSWorkflowAction
{
	
	   /* System services */ 
	   private static IPSWorkflowService work = null; 
	   private static IPSGuidManager gmgr = null;
	   private static IPSCmsContentSummaries summ = null; 
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSOSetRevisionLock.class);
   /**
    * 
    */
   
   /**
    * Initializes system services.  
    * Used to prevent references to these services during extension 
    * registration.
    */
   private static void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr();
         summ = PSCmsContentSummariesLocator.getObjectManager();
         work = PSWorkflowServiceLocator.getWorkflowService(); 
      }
   }
   
   
   public PSOSetRevisionLock()
   {
      super();
   }
   /**
    * Sets the revision lock for the current item. Once the lock has been set,
    * it cannot be reset.   
    * @see com.percussion.extension.IPSWorkflowAction#performAction(com.percussion.extension.IPSWorkFlowContext, com.percussion.server.IPSRequestContext)
    */
   public void performAction(IPSWorkFlowContext wfCtx, IPSRequestContext req)
         throws PSExtensionProcessingException
   {
       initServices();
       try
      {
        
         PSComponentSummary summary = summ.loadComponentSummary(wfCtx.getContentID());
         
         PSLocator loc = summary.getCurrentLocator();
         log.debug("Item id is " + loc.getId()); 

         if(summary.isRevisionLock())
         {
            log.debug("Item is already locked");
            return;
         }
         
         setLockSQL(wfCtx.getContentID());
         
      } catch (Exception e)
      {
         log.error("Unexpected Exception " + e.getLocalizedMessage(), e);
         throw new PSExtensionProcessingException(EXIT_NAME, e);
      }
   }
   
   private static void setLockSQL(int contentId)
   {
      Connection conn = null;
      PreparedStatement stmt = null;
      try {
         log.debug("locking revision for content id " + contentId); 
         conn = ms_pool.getConnection();
         stmt = conn.prepareStatement(SQL_UPDATE);
         stmt.setInt(1, contentId);
         int rows = stmt.executeUpdate();
         log.debug("rows affected " + rows); 
         if(rows != 1)
         {
            log.warn("Locking Revisions: unexpected row count " + rows); 
         }
      }
      catch(Exception ex)
      {
         log.error("SQL Error " + ex.getMessage(), ex);
      }
      finally
      { 
         if(stmt != null)
         {
            try
            {
               stmt.close();
            } catch (SQLException e)
            {
               log.error("error releasing statement " + e.getMessage(), e); 
            } 
         }
         if(conn != null)
         {
            try
            {
               ms_pool.releaseConnection(conn);
            } catch (SQLException e)
            {
               log.error("error releasing connection " + e.getMessage(), e); 
            } 
         }
      }
   }
   
   private static final String SQL_UPDATE = "UPDATE CONTENTSTATUS SET REVISIONLOCK = 'Y' where CONTENTID = ?";
   
   /**
    * Database pool from Percussion CMS Server
    */
   static PSDatabasePool ms_pool = PSDatabasePool.getDatabasePool();
   
   
   private static final String EXIT_NAME = "PSOSetRevisionLock"; 
}

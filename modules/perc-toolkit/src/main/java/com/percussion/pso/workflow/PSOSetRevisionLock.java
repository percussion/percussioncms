/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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

import org.apache.log4j.Logger;

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
   private static Logger log = Logger.getLogger(PSOSetRevisionLock.class);
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

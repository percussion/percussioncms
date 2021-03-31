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
 * com.percussion.pso.workflow PublishEditionService.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

/**
 * The PublishEdition service starts an edition. 
 * This class used to start the remote publisher via HTTP, but 
 * in 6.6 we use the IPSRxPublisherService. 
 * The <code>baseUrl, cmsUser, cmsPassword, listenerPort</code> and
 * <code>retryCount</code> parameters are no longer used.  The getters
 * and setters remain in place for backwards compatibility. 
 *
 * @author DavidBenua
 *
 */
public class PublishEditionService implements InitializingBean
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PublishEditionService.class);

   private String baseUrl = "http://127.0.0.1";  
   private String listenerPort = null;
   //if local is true we expect to run as the caller, otherwise need CMS user and password
   private boolean local = true; 
   private String cmsUser = null; 
   private String cmsPassword = null; 
   private int retryCount = 10;
   
      
   private IPSRxPublisherService rps = null; 
   private IPSGuidManager gmgr = null; 
   
   /*
    * Map of workflows
    *    Map of transitions
    *       Map of communities
    *          Value is edition
    */
   private Map<String,Map<String,Map<String,String>>> workflows 
      = new HashMap<String,Map<String,Map<String,String>>>();
   
   /**
    * Default constructor. 
    */
   public PublishEditionService()
   {
      
   }
   
   private void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr();
      }
      if(rps == null)
      {
         rps = PSRxPublisherServiceLocator.getRxPublisherService(); 
      }
   }
   /**
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      initServices();
   }

   @SuppressWarnings("deprecation")
   public void runQueuedEdition(QueuedEdition ed)
   {
      runEdition(ed.getEditionId());   
   }

   /**
    * Runs an edition. 
    * This launches a new job asynchronously via the RxPublisherService. 
    * @param editionId the edition id as a simple number.  
    * @since 6.6
    */
   public void runEdition(String editionId)
   {
      IPSGuid guid = gmgr.makeGuid(editionId, PSTypeEnum.EDITION); 
      rps.startPublishingJob(guid, null); 
   }
   
   @SuppressWarnings("deprecation")
   public void retryQueuedEdition(QueuedEdition ed)
   {
       log.info("retryQueuedEdition is no longer used"); 
   }
   
   public int findEdition(int workflow, int transition, int community)
   {
      String workKey = String.valueOf(workflow); 
      Map<String,Map<String,String>> workMap = workflows.get(workKey);
      if(workMap == null)
      {
         String emsg = "Workflow not in configuration file " + workflow;
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }
      String transKey = String.valueOf(transition); 
      Map<String,String> transMap = workMap.get(transKey);
      if(transMap == null)
      {
         String emsg = "Transition " +  transition + " not in configuration file for workflow " + workflow;
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);         
      }
      String commKey = String.valueOf(community);
      String edition  = transMap.get(commKey); 
      if(edition == null)
      {
         String emsg = "Community " +  community + " not in configuration file for workflow " + workflow
          + " and transition " + transition;
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }      
      return Integer.parseInt(edition);
   }
   
   /**
    * Makes a Queued Edition.  This is no longer necessary, but supported for backwards compatibility
    * @param editionId
    * @param sessionId
    * @return
    * @deprecated
    */
   @SuppressWarnings("deprecation")
   protected QueuedEdition makeQueuedEdition(String editionId, String sessionId)
   {
      
      QueuedEdition result = new QueuedEdition(baseUrl,listenerPort,editionId,this.isLocal(), retryCount); 
      
      return result; 
   }
   
   
   /**
    * Gets the baseUrl.  No longer used in 6.6. 
    * @return Returns the baseUrl.
    * @deprecated
    */
   public String getBaseUrl()
   {
      return baseUrl;
   }
   /**
    * No longer used in 6.6.
    * @param baseUrl The baseUrl to set.
    * @deprecated
    */
   public void setBaseUrl(String baseUrl)
   {
      this.baseUrl = baseUrl;
   }
   /**
    * No longer used in 6.6.
    * @return Returns the listenerPort.
    * @deprecated
    */
   public String getListenerPort()
   {
      return listenerPort;
   }
   /**
    * No longer used in 6.6.
    * @param listenerPort The listenerPort to set.
    * @deprecated
    */
   public void setListenerPort(String listenerPort)
   {
      this.listenerPort = listenerPort;
   }
   /**
    * @return Returns the workflows.
    */
   public Map<String, Map<String, Map<String, String>>> getWorkflows()
   {
      return workflows;
   }
   /**
    * @param workflows The workflows to set.
    */
   public void setWorkflows(
         Map<String, Map<String,  Map<String, String>>> workflows)
   {
      this.workflows = workflows;
   }
   /**
    * No longer used in 6.6.
    * @return Returns the cmsPassword.
    */
   public String getCmsPassword()
   {
      return cmsPassword;
   }
   /**
    * No longer used in 6.6.
    * @param cmsPassword The cmsPassword to set.
    */
   public void setCmsPassword(String cmsPassword)
   {
      this.cmsPassword = cmsPassword;
      log.debug("Setting CMS Password"); 
      this.local = false; 
   }
   /**
    * No longer used in 6.6.
    * @return Returns the cmsUser.
    */
   public String getCmsUser()
   {
      return cmsUser;
   }
   /**
    * No longer used in 6.6.
    * @param cmsUser The cmsUser to set.
    */
   public void setCmsUser(String cmsUser)
   {
      this.cmsUser = cmsUser;
      log.debug("Setting CMS User " + cmsUser); 
      this.local = false;
   }
   /**
    * No longer used in 6.6.
    * @return Returns the local.
    */
   public boolean isLocal()
   {
      return local;
   }
   /**
    * No longer used in 6.6.
    * @param local The local to set.
    */
   public void setLocal(boolean local)
   {
      this.local = local;
   }

   /**
    * @return Returns the retryCount.
    * @deprecated
    */
   public int getRetryCount()
   {
      return retryCount;
   }

   /**
    * @param retryCount The retryCount to set.
    * @deprecated
    */
   public void setRetryCount(int retryCount)
   {
      this.retryCount = retryCount;
   }

   /**
    * Sets the RxPublisherService in unit test.
    * @param rps the RxPublisherService to set. 
    */
   protected void setRps(IPSRxPublisherService rps)
   {
      this.rps = rps;
   }

   /**
    * Sets the Guid Manager in unit test.
    * @param gmgr the guid manager to set. 
    */
   protected void setGmgr(IPSGuidManager gmgr)
   {
      this.gmgr = gmgr;
   }
}

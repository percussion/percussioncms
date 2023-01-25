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
package com.percussion.workflow.actions;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Class wrapper for web service calls for content and 
 * component summaries.
 *
 * @author BillLanglais
 */
public class PSItemSummaryFinder
{
   /**
    * Logger for this class
    */
   private static final Log m_log = LogFactory.getLog(PSItemSummaryFinder.class);

   private static IPSCmsContentSummaries m_sumsvc = null;

   /**
    * Static methods only, never constructed.
    */
   private PSItemSummaryFinder()
   {
      
   }
   
   private static IPSCmsContentSummaries getServices()
   {  
      if(m_sumsvc == null)
      {
         m_sumsvc = PSCmsContentSummariesLocator.getObjectManager();
      }
      return(m_sumsvc);
   }
   public static PSLocator getCurrentOrEditLocator(IPSGuid guid) 
      throws PSException
   {
      int id = guid.getUUID();
      return getCurrentOrEditLocator(id);
   }
    
   public static PSLocator getCurrentOrEditLocator(String contentId)
      throws PSException
   {
      if(StringUtils.isBlank(contentId) || !StringUtils.isNumeric(contentId))
      {
         String emsg = "Content id must be numeric " + contentId; 
         m_log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      int id = Integer.parseInt(contentId);
      return getCurrentOrEditLocator(id);  
   }
   
   public static PSLocator getCurrentOrEditLocator(int id)
      throws PSException
   {
      PSComponentSummary cs = getSummary(id);
      PSLocator loc = cs.getCurrentLocator();
      if(cs.getEditLocator() != null && cs.getEditLocator().getRevision() > 0)
      {
         loc = cs.getEditLocator();
         m_log.debug("Using edit locator" + loc); 
      }
      return loc; 
   }
   
   public static final int CHECKOUT_NONE = 1; 
   public static final int CHECKOUT_BY_ME = 2; 
   public static final int CHECKOUT_BY_OTHER = 3; 
   
   public static int getCheckoutStatus(String contentId, String userName) 
      throws PSException
   {
      if(StringUtils.isBlank(userName))
      {
         String emsg = "User name must not be blank"; 
         m_log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }
      PSComponentSummary sum = getSummary(contentId);
      String uname = sum.getCheckoutUserName();
      if(StringUtils.isBlank(uname))
      {
         return CHECKOUT_NONE;
      }
      if(userName.equalsIgnoreCase(uname))
      {
         return CHECKOUT_BY_ME; 
      }
      return CHECKOUT_BY_OTHER;
   }
   /**
    * Gets the component summary for an item.
    * @param contentId the content id never <code>null</code> or empty
    * @return the component summary. Never <code>null</code>.
    * @throws PSException 
    */
   public static PSComponentSummary getSummary(String contentId) 
      throws PSException
   {
      if(StringUtils.isBlank(contentId) || !StringUtils.isNumeric(contentId))
      {
         String emsg = "Content id must be numeric " + contentId; 
         m_log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      int id = Integer.parseInt(contentId);
      return getSummary(id);
   }
  
   public static PSComponentSummary getSummary(IPSGuid guid) 
   throws PSException
   {
      int id = guid.getUUID();
      return getSummary(id);
   }
  
   public static PSComponentSummary getSummary(int id) 
      throws PSException
   {
      PSComponentSummary sum = getServices().loadComponentSummary(id);
      if(sum == null)
      {
         String emsg = "Content item not found " + id; 
         m_log.error(emsg); 
         throw new PSException(emsg);
      }
      return sum;
   }
   
    /**
    * Sets the summary service. 
    * Should be used only in unit tests. 
    * @param sumservice The sumsvc to set.
    */
   public static void setSumsvc(IPSCmsContentSummaries sumservice)
   {
      m_sumsvc = sumservice;
   } 
}



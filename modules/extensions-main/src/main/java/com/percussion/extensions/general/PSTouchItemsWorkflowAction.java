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
package com.percussion.extensions.general;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.touchitem.IPSTouchItemService;
import com.percussion.services.touchitem.PSTouchItemLocator;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * Touches Active Assembly parent items and Managed Navigation items (navons)
 * after workflow transition.
 * 
 * @author adamgent
 * @author yubingchen
 *
 */
public class PSTouchItemsWorkflowAction extends PSDefaultExtension implements IPSWorkflowAction
{

   private static Logger ms_logger = Logger.getLogger(PSTouchItemsWorkflowAction.class);
   
   /**
    * perform the workflow action.
    *
    * @param context the workflow context holds basic information about the
    *    the content item and its workflow state.
    * @param request the request context for the exit
    * @throws PSExtensionProcessingException if any processing errors occur
    */
   public void performAction(IPSWorkFlowContext context, 
      IPSRequestContext request) throws PSExtensionProcessingException
   {
      touchActiveAssemblyParents(context, request);
      IPSTouchItemService touchService = PSTouchItemLocator.getTouchItemService();
      PSLegacyGuid id = new PSLegacyGuid(context.getContentID(), context.getBaseRevisionNum());
      touchService.touchItems(id);
   }
   

   /**
    * Touches all "active assembly parent" items of the 
    * current item. These items are found by searching the related content table 
    * for parent items, and then searching for the parents of those items, etc.
    * The relationships are in the 'active assembly' category only.
    * <p>
    * The content items which are found are then updated so that the LastModifyDate
    * column contains the current date & time.
    *
    * @param context the workflow context holds basic information about the
    *    the content item and its workflow state.
    * @param request the request context for the exit
    * @throws PSExtensionProcessingException if any processing errors occur
    */
   private void touchActiveAssemblyParents(IPSWorkFlowContext context, 
      IPSRequestContext request) throws PSExtensionProcessingException
   {
      diagMessage(request,"Starting Workflow Action");
      Integer contentId = new Integer(context.getContentID());

      try
      {
         Collection<Integer> cids = new ArrayList<>();
         cids.add(contentId);
         IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
         pub.touchActiveAssemblyParents(cids);
      }
      catch (Exception e)
      {
         ms_logger.error("Failed to touch items on workflow action:", e);
         throw new PSExtensionProcessingException(getClass().getName(), e);
      }
   }
   
   /**
    * A private diagnostic message method, used primarily to hide the
    * details of tracing. Output can be to the trace file or console
    *
    * @param request the request context.  Must not be <code>null</code>.
    * @param msg the diagnostic message to output.
    */
   private void diagMessage(IPSRequestContext request, String msg)
   {
      
      notNull(request, "request may not be null");
      notNull(msg, "msg may not be null");
      request.printTraceMessage(msg);
      ms_logger.debug(msg);
   }
   

}

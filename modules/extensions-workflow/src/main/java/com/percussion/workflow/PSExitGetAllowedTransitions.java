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
package com.percussion.workflow;

import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemException;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Gets allowed transitions for list of content ids and assignment types based
 * on the current user's name and roles.
 */
public class PSExitGetAllowedTransitions extends PSDefaultExtension 
   implements IPSResultDocumentProcessor
{
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Determines the allowed transitions and appends them to the output doc.  
    * The root is replaced (if there) with an <code>ActionList</code> element
    * and each possible action is appended using 
    * {@link PSMenuAction#toXml(Document)}
    * 
    * @param params The expected params.  Param 0 is expected to be a list of
    * content ids as strings, and Param 1 is expected to be a list of assignment 
    * type ids as strings, may not be <code>null</code>.
    * 
    * @throws PSExtensionProcessingException if there are any errors.
    */
   @SuppressWarnings("unchecked")
   public Document processResultDocument(
      @SuppressWarnings("unused") Object[] params, IPSRequestContext request, 
      Document resultDoc) throws PSExtensionProcessingException
   {
      
      if(request == null)
      {
         throw new PSExtensionProcessingException(
            m_def.getRef().toString(),
            new IllegalArgumentException("The request must not be null"));
      }
      
      try
      {
         // get input params
         List<IPSGuid> contentids = getContentIds(request);
         List<PSAssignmentTypeEnum> assignmentTypes = 
            getAssignmentTypes(request, contentids);
         
         String userName = request.getUserName();
         String locale = request.getUserLocale();
         List<String> userRoles = request.getSubjectRoles();
         
         // get the actions
         IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
         List<PSMenuAction> actions = svc.getAllWorkflowActions(contentids, 
            assignmentTypes, userName, userRoles, locale);       
         
         // append each action to doc
         Element root = resultDoc.createElement("ActionList");
         PSXmlDocumentBuilder.replaceRoot(resultDoc, root);
         for (PSMenuAction action : actions)
         {
            root.appendChild(action.toXml(resultDoc));
         }
         
         return resultDoc;
      }
      catch (Exception e)
      {
         throw new PSExtensionProcessingException(
            m_def.getRef().toString(), e);
      }
   }

   /**
    * Get the list of content ids from the supplied request
    * 
    * @param request The current request, assumed not <code>null</code>. 
    * 
    * @return The list of ids, never <code>null</code>, may be empty.
    */
   private List<IPSGuid> getContentIds(IPSRequestContext request) 
   {
      List<IPSGuid> contentids = new ArrayList<>();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      Object[] ids = request.getParameterList(IPSHtmlParameters.SYS_CONTENTID);
      if (ids == null || ids.length == 0)
         return contentids;
      
      for (Object val : ids)
      {
         if (val != null)
            contentids.add(guidMgr.makeGuid(new PSLocator(val.toString())));
      }
      return contentids;
   }
   
   /**
    * Get the list of assignment types from the supplied parameters or calculate 
    * them using the system service if not supplied.
    *  
    * @param request The current request, assumed not <code>null</code>. 
    * @param contentIds The list of content ids for which types are to be
    * returned, assumed not <code>null</code>.
    * 
    * @return The list of types, may be <code>null</code>if the expected param 
    * is not found.
    * 
    * @throws PSSystemException If there is an error calculating the assignment
    * types.
    */
   @SuppressWarnings("unchecked")
   private List<PSAssignmentTypeEnum> getAssignmentTypes(
      IPSRequestContext request, List<IPSGuid> contentIds)
      throws PSSystemException
   {
      List<PSAssignmentTypeEnum> assignmentTypes = 
         new ArrayList<>();
      Object[] types = request.getParameterList(
         IPSHtmlParameters.SYS_ASSIGNMENTTYPEID);
      if (types == null || types.length == 0)
      {
         return calculateAssignmentTypes(request, contentIds);
      }
      else
      {
         if (types.length != contentIds.size())
            throw new IllegalArgumentException("The supplied number of " +
                    "contentids and assignment type ids must match");
      }
      
      for (Object val : types)
      {
         if (val != null)
            assignmentTypes.add(PSAssignmentTypeEnum.valueOf(
               Integer.parseInt(val.toString())));
      }
      
      return assignmentTypes;
   }

   /**
    * Calculate assignment type for the supplied content ids
    * 
    * @param request The current request context, assumed not <code>null</code>.
    * @param contentIds The ids to use, assumed not <code>null</code>.
    * 
    * @return The assignment types, never <code>null</code>.
    * 
    * @throws PSSystemException If there are any errors.
    */
   @SuppressWarnings("unchecked")
   private List<PSAssignmentTypeEnum> calculateAssignmentTypes(
      IPSRequestContext request, List<IPSGuid> contentIds)
      throws PSSystemException
   {
      IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();

      int userCommunity = -1;
      
      try
      {
         String usercomm = (String)request.getSessionPrivateObject(
            IPSHtmlParameters.SYS_COMMUNITY);
         if(usercomm != null)
            userCommunity = Integer.parseInt(usercomm);
      }
      catch(Exception e){}
      
      return sysSvc.getContentAssignmentTypes(contentIds, 
         request.getUserName(), request.getSubjectRoles(), userCommunity);
   }
}


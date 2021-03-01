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
package com.percussion.design.objectstore.legacy;

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.data.PSContentTypeWorkflow;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSSecurityUtils;
import com.percussion.utils.guid.IPSGuid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;



/**
 * Updates the content editors workflow info object. Part of Barracuda the
 * workflow associations are stored in the database but the workflow info object
 * in the editor is left as is to minimize the changes. If the associations
 * exist in database then the workflow info is updated with them. If not it is
 * modified as per the workflow info in the editor. If the workflow info in the
 * editor is null then new workflow info is created with all visible workflows
 * through communities that the content editor belongs to. If the type of the
 * workflow info is exclusionary then, the exclusionary list is removed from all
 * visible workflows through communities that the content editor belongs to and
 * the new list is set on the workflow info and the type is changed to
 * inclusionary. If the type of workflow info is inclusionary then does nothing.
 * The default workflow is reset if needed.
 * 
 * @author bjoginipally
 * 
 */
public class PSContentTypeWorkflowsUpdater implements IPSComponentUpdater
{

   /**
    * Empty constructor.
    */
   public PSContentTypeWorkflowsUpdater() {

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.design.objectstore.legacy.IPSComponentUpdater#updateComponent(org.w3c.dom.Element)
    */
   @SuppressWarnings("unchecked")
   public void updateComponent(PSComponent comp)
   {
      PSContentEditor editor = (PSContentEditor) (comp);

      // do nothing if the editor is not workflowable
      PSCmsObject cmsObject = PSServer.getCmsObjectRequired(editor
            .getObjectType());
      if (!cmsObject.isWorkflowable())
         return;
      //get the workflow rels from table
      List<IPSGuid> wfGuids = new ArrayList<>();
      try
      {
         wfGuids = getContentTypeWorkflows(editor);
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }
      if(wfGuids.isEmpty())
      {
         //If it is inclusionary we need not to do anything just return.
         PSWorkflowInfo wfInfo = editor.getWorkflowInfo();
         if (wfInfo != null && !wfInfo.isExclusionary())
            return;
         //Get the workflows from the editor.
         wfGuids = getWorkflowsFromEditor(editor);
      }

      Collections.sort(wfGuids, new Comparator<IPSGuid>()
      {
         /**
          * Sorts by UUID in ascending order.
          */
         public int compare(IPSGuid o1, IPSGuid o2)
         {
            return (new Integer(o1.getUUID())).compareTo(o2.getUUID());
         }
      });
      Set<Integer> wfInts = new HashSet<>();
      for (IPSGuid guid : wfGuids)
      {
         wfInts.add(guid.getUUID());
      }
      setWorkflowInfo(editor, wfInts);
      resetDefaultWorkflow(editor);
   }
   

   /**
    * If the workflow info is null, then creates the workflow info with the
    * supplied workflow ids and sets it on the editor. If the workflow info is
    * exclusionary then removes the exclusion list from the supplied workflow
    * list and then sets the remaining workflows.
    * 
    * @param editor The content editor assumed not <code>null</code>.
    * @param wfInts Set of workflow ids assumed not <code>null</code>.
    */
   private void setWorkflowInfo(PSContentEditor editor, Set<Integer> wfInts)
   {
      PSWorkflowInfo wfInfo = editor.getWorkflowInfo();
      if (wfInfo == null)
      {
         if (!wfInts.isEmpty())
         {
            wfInfo = new PSWorkflowInfo(PSWorkflowInfo.TYPE_INCLUSIONARY,
                  new ArrayList<Integer>(wfInts));
            editor.setWorkflowInfo(wfInfo);
         }
      }
      else
      {
         wfInfo.setType(PSWorkflowInfo.TYPE_INCLUSIONARY);
         wfInfo.setValues(new ArrayList<Integer>(wfInts));
      }
   }
   
   /**
    * Gets the workflows from the content editor.
    * @param editor The content editor assumed not <code>null</code>.
    * @return List of workflow guids, may be empty, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private List<IPSGuid> getWorkflowsFromEditor(PSContentEditor editor)
   {
      List<IPSGuid> wfGuids = new ArrayList<>();
      PSWorkflowInfo wfInfo = editor.getWorkflowInfo();
      if (wfInfo == null)
      {
         wfGuids = getVisibleCommunityWorkflows(editor);
      }
      else if(wfInfo.isExclusionary())
      {
         wfGuids = getVisibleCommunityWorkflows(editor);
         List<Integer> wfIds = wfInfo.getWorkflowIds();
         List<IPSGuid> removals = new ArrayList<>();
         for (IPSGuid guid : wfGuids)
         {
            if(wfIds.contains(new Integer(guid.getUUID())))
            {
               removals.add(guid);
            }
         }
         wfGuids.removeAll(removals);
      }
      else
      {
         List<Integer> wfIds = wfInfo.getWorkflowIds();
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         for (Integer wf : wfIds)
         {
            wfGuids.add(gmgr.makeGuid(wf.toString(),PSTypeEnum.WORKFLOW));
         }
      }
      return wfGuids;
   }

   /**
    * Gets the List of the visible workflows for the communities that are
    * associated with the supplied content type.
    * 
    * @param editor The content editor assumed not <code>null</code>.
    * @return List of workflow guids, may be empty, never <code>null</code>.
    */
   private List<IPSGuid> getVisibleCommunityWorkflows(PSContentEditor editor)
   {
      List<IPSGuid> wfGuids = new ArrayList<>();
      // Load the ACl from contenttypeid
      long cTypeId = editor.getContentType();
      IPSGuid ctypeGuid = new PSGuid(PSTypeEnum.NODEDEF, cTypeId);
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      IPSAcl ctypeAcl = aclService.loadAclForObject(ctypeGuid);
      // Get the list of communities from Security Manager
      IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      List<PSCommunity> communities = roleMgr.findCommunitiesByName(null);
      Map<String, IPSGuid> comms = new HashMap<>();
      for (PSCommunity comm : communities)
         comms.put(comm.getName(), comm.getGUID());
      // Get the visible communities from PSSecurityUtils class
      List<String> lst = new ArrayList<>(comms.keySet());
      List<String> filteredComms = PSSecurityUtils.getVisibleCommunities(
            ctypeAcl, lst);
      IPSAclService service = PSAclServiceLocator.getAclService();

      wfGuids.addAll(service.findObjectsVisibleToCommunities(
            filteredComms, PSTypeEnum.WORKFLOW));
      return wfGuids;
   }
   
   /**
    * Gets the content type workflow associations.
    * @param editor The content editor assumed not <code>null</code>.
    * @return List of workflow guids, may be empty, never <code>null</code>.
    * @throws RepositoryException
    */
   private List<IPSGuid> getContentTypeWorkflows(PSContentEditor editor)
      throws RepositoryException
   {
      List<IPSGuid> wfGuids = new ArrayList<>();
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      long cTypeId = editor.getContentType();
      IPSGuid ctypeGuid = new PSGuid(PSTypeEnum.NODEDEF, cTypeId);
      List<PSContentTypeWorkflow> ctwfs = mgr
            .findContentTypeWorkflowAssociations(ctypeGuid);
      for (PSContentTypeWorkflow ctwf : ctwfs)
      {
         wfGuids.add(ctwf.getWorkflowId());
      }
      return wfGuids;
   }
   
   /**
    * If the default workflow id is not in the workflow info, then reset it to
    * the first one. If the workflow info is null or empty does nothing.
    * 
    * @param editor The content editor assumed not <code>null</code>.
    */
   private void resetDefaultWorkflow(PSContentEditor editor)
   {
      PSWorkflowInfo wfInfo = editor.getWorkflowInfo();
      if (wfInfo == null)
         return;
      List<Integer> wfInts = wfInfo.getWorkflowIds();
      if (wfInts.isEmpty())
      {
         return;
      }
      else if (!wfInts.contains(new Integer(editor.getWorkflowId())))
      {
         int smallestWfId = wfInts.get(0);
         Logger log = LogManager.getLogger(this.getClass());
         log
               .warn("Reset the default workflow id to \""
                     + smallestWfId
                     + "\" (name="
                     + smallestWfId
                     + ") for Content Type (id="
                     + editor.getContentType()
                     + "). This is because the original default workflow (id="
                     + editor.getWorkflowId()
                     + ") is not visible by this Content Type. Loading and Saving "
                     + "the Content Type (from Workbench) can prevent this message reappearing.");
         editor.setWorkflowId(smallestWfId);
      }
   }
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.design.objectstore.legacy.IPSComponentUpdater#canUpdateComponent(java.lang.Class)
    */
   public boolean canUpdateComponent(Class type)
   {
      return type.getName().equals(PSContentEditor.class.getName());
   }

}

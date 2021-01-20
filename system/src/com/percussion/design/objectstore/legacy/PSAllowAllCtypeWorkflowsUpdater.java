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
import com.percussion.design.objectstore.IPSWorkflowInfoValueAccessor;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.IPSTypedPrincipal;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Updates the content editors workflow info object. For CM1, all workflows
 * should be automatically allowed by all content types. This updater sets an
 * {@link IPSWorkflowInfoValueAccessor} on the {@link PSWorkflowInfo} that will
 * dynamically return all current workflows as allowed.
 * 
 * @author JaySeletz
 */
public class PSAllowAllCtypeWorkflowsUpdater implements IPSComponentUpdater
{
   /**
    * Empty constructor.
    */
   public PSAllowAllCtypeWorkflowsUpdater() 
   {

   }
   
   /**
    * Sets an {@link IPSWorkflowInfoValueAccessor} on the {@link PSWorkflowInfo} of
    * the supplied {@link PSContentEditor}.
    */
   public void updateComponent(PSComponent comp)
   {
      PSContentEditor editor = (PSContentEditor) (comp);

      // do nothing if the editor is not workflowable
      PSCmsObject cmsObject = PSServer.getCmsObjectRequired(editor
            .getObjectType());
      if (!cmsObject.isWorkflowable())
         return;

      PSWorkflowInfo wfInfo = editor.getWorkflowInfo();
      if (wfInfo != null)
      {
         updateInfo(wfInfo);
         @SuppressWarnings("unchecked")
         List<Integer> allowedIds = IteratorUtils.toList(wfInfo.getValues());
         if (!allowedIds.isEmpty() && !IteratorUtils.toList(wfInfo.getValues()).contains(editor.getWorkflowId()))
         {
            editor.setWorkflowId(allowedIds.get(0));
         }
      }
   }

   /**
    * Updates the supplied {@link PSWorkflowInfo} object to allow
    * all workflows in the system.  Exposed with package default
    * access to allow for unit testing.
    * 
    * @param wfInfo The info to update, may not be <code>null</code>.
    */
   void updateInfo(PSWorkflowInfo wfInfo)
   {
      Validate.notNull(wfInfo);
      
      wfInfo.setValueAccessor(new IPSWorkflowInfoValueAccessor()
      {
         
         public boolean isExclusionary()
         {
            return false;
         }
         
         public List<Integer> getValues()
         {
            // load all workflow summaries
            List<Integer> wfIds = new ArrayList<Integer>();
            List<IPSGuid> wfGuids = new ArrayList<IPSGuid>();
            IPSWorkflowService wfSvc = PSWorkflowServiceLocator.getWorkflowService();
            List<PSObjectSummary> sums = wfSvc.findWorkflowSummariesByName(null);
            for (PSObjectSummary sum : sums)
            {
               wfIds.add(sum.getGUID().getUUID());
               wfGuids.add(sum.getGUID());
            }
            
            // ensure at least a default acl exists for each workflow
            IPSAclService aclSvc = PSAclServiceLocator.getAclService();
            List<IPSAcl> acls =  aclSvc.loadAclsForObjects(wfGuids);
            for (int i = 0; i < acls.size(); i++)
            {
               IPSAcl acl = acls.get(i);
               
               if (acl == null)
               {
                  IPSTypedPrincipal owner = new PSTypedPrincipal(
                        PSTypedPrincipal.DEFAULT_USER_ENTRY, PrincipalTypes.USER);
                  IPSGuid wfGuid = wfGuids.get(i);
                  IPSAcl newAcl = aclSvc.createAcl(wfGuid, owner);
                  try
                  {
                     aclSvc.saveAcls(Collections.singletonList(newAcl));
                  }
                  catch (PSSecurityException e)
                  {
                     Logger logger = Logger.getLogger(this.getClass());
                     logger.error("Unable to save default acl for worflow " + wfGuid.getUUID(), e);
                  }                  
               }
            }
            
            // PSContentTypeWorkflowsUpdater sorts the results, so we'll do that here too
            Collections.sort(wfIds);
            
            return wfIds;
         }
      });
   }

   /**
    * @return <code>true</code> if the supplied type is {@link PSContentEditor}. 
    */
   public boolean canUpdateComponent(Class type)
   {
      return type.getName().equals(PSContentEditor.class.getName());
   }

}

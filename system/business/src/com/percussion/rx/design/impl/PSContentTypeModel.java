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
package com.percussion.rx.design.impl;

import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.rx.design.IPSAssociationSet.AssociationType;
import com.percussion.server.PSRequest;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class PSContentTypeModel extends PSDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      //Content type did not implement read only load method and hence returns
      return loadModifiable(guid);
   }
   
   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      PSRequest origReq = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      PSItemDefinition itemDef = null;
      try
      {
         PSDesignModelUtils.setRequestToInternalUser(origReq);
         itemDef = PSContentTypeHelper.loadItemDef(guid);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         PSDesignModelUtils.resetRequestToOriginal(origReq, origUser);
      }
      return itemDef;
   }
   
   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      if (!(obj instanceof PSItemDefinition))
      {
         throw new RuntimeException("Invalid Object passed for save.");
      }
      PSRequest origReq = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      PSDesignModelUtils.setRequestToInternalUser(origReq);
      PSItemDefinition itemDef = (PSItemDefinition) obj;
      PSNodeDefinition nodeDef = PSContentTypeHelper.findNodeDef(itemDef
            .getGuid());
      List<IPSGuid> templateGuids = null;
      if(associationSets != null)
      {
         for (IPSAssociationSet aset : associationSets)
         {
            if (aset.getType() == AssociationType.CONTENTTYPE_WORKFLOW)
            {
               List wfs = aset.getAssociations();
               if (wfs != null)
               {
                  updateWorkflowInfo(itemDef, wfs);
               }
            }
            // Handle template associations
            else if (aset.getType() == AssociationType.CONTENTTYPE_TEMPLATE)
            {
               List templates = aset.getAssociations();
               if (templates != null)
               {
                  templateGuids = getTemplateGuids(templates);
               }
            }
         }
      }
      try
      {
         PSRequest appReq = (PSRequest) PSRequestInfo.getRequestInfo(
               PSRequestInfo.KEY_PSREQUEST);
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         PSApplication app = os.getApplicationObject(PSContentType.getAppName(
               nodeDef.getNewRequest()), appReq.getSecurityToken(), false);
         
         PSContentTypeHelper.saveContentType(itemDef, nodeDef.getVersion(),
               null, templateGuids, app.isEnabled());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         PSDesignModelUtils.resetRequestToOriginal(origReq, origUser);
      }
   }

   /**
    * Helper method to get the template guids for the supplied list of the
    * templates.
    * 
    * @param templates assumed not <code>null</code>.
    * @return List of template guids, never <code>null</code>, may be empty,
    * if the supplied template list is empty or the template names do not
    * correspond to any templates.
    */
   private List<IPSGuid> getTemplateGuids(List templates)
   {
      List<IPSGuid> templateGuids = new ArrayList<>();
      IPSAssemblyService asmSrvc = PSAssemblyServiceLocator
            .getAssemblyService();
      for (Object object : templates)
      {
         if (!(object instanceof String))
         {
            String msg = "Skipping the content type association with "
                  + "the template for value {0} as the type of the "
                  + "value is not String.";
            Object[] args = { object.toString() };
            ms_logger.warn(MessageFormat.format(msg, args));
            continue;
         }
         IPSAssemblyTemplate template;
         try
         {
            template = asmSrvc.findTemplateByName((String) object);
            templateGuids.add(template.getGUID());
         }
         catch (PSAssemblyException e)
         {
            String msg = "Unable to find the template for {0}, skipping "
                  + "the content type association with "
                  + " the template for {0}.";
            Object[] args = { object.toString() };
            ms_logger.warn(MessageFormat.format(msg, args));
         }
      }
      return templateGuids;
   }

   /**
    * Helper method to update the workflow info in the supplied item def with
    * the supplied list of workflows.
    * 
    * @param itemDef assumed not <code>null</code>
    * @param wfs list of workflows assumed not <code>null</code>.
    */
   private void updateWorkflowInfo(PSItemDefinition itemDef, List wfs)
   {
      List<Integer> workFlows = new ArrayList<>();
      IPSWorkflowService wfSrvc = PSWorkflowServiceLocator
            .getWorkflowService();
      for (Object object : wfs)
      {
         if (!(object instanceof String))
         {
            String msg = "Skipping the content type association with "
                  + "the workflow for value {0} as the type of the "
                  + "value is not String.";
            Object[] args = { object.toString() };
            ms_logger.warn(MessageFormat.format(msg, args));
            continue;
         }
         List<PSWorkflow> temp = wfSrvc.findWorkflowsByName((String) object);
         if (temp.isEmpty())
         {
            String msg = "Skipping the content type association with "
                  + "the workflow for name {0} as no workflow exists"
                  + " with the that name";
            Object[] args = { (String) object };
            ms_logger.warn(MessageFormat.format(msg, args));
            continue;
         }
         workFlows.add(new Integer(temp.get(0).getGUID().longValue() + ""));
      }
      if (!workFlows.isEmpty())
      {
         itemDef.getContentEditor()
               .setWorkflowInfo(
                     new PSWorkflowInfo(PSWorkflowInfo.TYPE_INCLUSIONARY,
                           workFlows));
      }
   }

   @Override
   public void save(Object obj)
   {
      save(obj, null);
   }

   @Override
   public String guidToName(IPSGuid guid)
   {
      PSNodeDefinition nodeDef = PSContentTypeHelper.findNodeDef(guid);
      return nodeDef.getInternalName();
   }

   @Override
   public IPSGuid nameToGuid(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name must not be null");
      List<IPSNodeDefinition> defs = PSContentTypeHelper
            .loadNodeDefs(name);
      IPSGuid guid = null;
      for (IPSNodeDefinition def : defs)
      {
         if (name.equalsIgnoreCase(def.getInternalName())
               || name.equalsIgnoreCase(def.getName()))
         {
            guid = def.getGUID();
         }
      }
      return guid;
   }

   @Override
   public List<IPSAssociationSet> getAssociationSets()
   {
      List<IPSAssociationSet> asets = new ArrayList<>();
      asets.add(new PSAssociationSet(AssociationType.CONTENTTYPE_WORKFLOW));
      asets.add(new PSAssociationSet(AssociationType.CONTENTTYPE_TEMPLATE));
      return asets;
   }

   @Override
   public Long getVersion(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid must not be null");
      PSNodeDefinition nodeDef = PSContentTypeHelper.findNodeDef(guid);
      if(nodeDef == null)
      {
         String msg = "Failed to get the version for guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return Long.valueOf(nodeDef.getVersion());
   }
   
   @Override 
   public Long getVersion(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be blank");
      return getVersion(nameToGuid(name));
   }
   
   @Override
   public void delete(IPSGuid guid)
   {
      String depTypes = PSDesignModelUtils.checkDependencies(guid);
      String name = guidToName(guid);
      if(depTypes != null)
      {
         String msg = "Skipped deletion of content type ({0}) as it is " +
               "currently being used by ({1})";
         Object[] args = { name, depTypes };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      try
      {
         PSDesignModelUtils.removeSlotAssocations(guid);
      }
      catch(Exception e)
      {
         String msg = "Failed to delete the supplied content type with name " +
         "({0}), due to the error removing the slot associations.";
         Object[] args = {name};
         throw new RuntimeException(MessageFormat.format(msg, args),e);
      }
      try
      {
         PSContentTypeHelper.deleteContentType(guid, null);
      }
      catch(Exception e)
      {
         String msg = "Failed to delete the supplied content type with name " +
         "({0}).";
         Object[] args = {name};
         throw new RuntimeException(MessageFormat.format(msg, args),e);
      }
   }


   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSContentTypeModel");

}

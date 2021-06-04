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

import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSWorkflowModel extends PSLimitedDesignModel
{

   private static final Logger log = LogManager.getLogger(PSWorkflowModel.class);

   @Override
   public Object load(IPSGuid guid)
   {
      return loadWorkflow(guid, true);
   }
   
   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      return loadWorkflow(guid, false);
   }
   
   /**
    * Loads the readonly or modifiable workflow from the workflow service for
    * the supplied guid based on the readonly flag.
    * 
    * @param guid Must not be <code>null</code> and must be a workflow guid.
    * @param readonly Flag to indicate whether to load a readonly or modifiable
    * workflow.
    * @return Object workflow object never <code>null</code>, throws
    * {@link RuntimeException} in case of an error.
    */
   private Object loadWorkflow(IPSGuid guid, boolean readonly)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      //We do not have the readonly version of workflow objects.
      //return same object for both.
      IPSWorkflowService service = (IPSWorkflowService) getService();
      PSWorkflow Wf = service.loadWorkflow(guid);
      if (Wf == null)
      {
         String msg = "Failed to load the workflow object for the "
               + "supplied guid ({0}).";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return Wf;
   }
   
   @Override
   public IPSGuid nameToGuid(String name)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be null");
      IPSWorkflowService service = (IPSWorkflowService) getService();
      List<PSObjectSummary> wfs = service.findWorkflowSummariesByName(name);
      if(wfs.isEmpty())
      {
         String msg = "Failed to get the guid for the given workflow name ({0})";
         Object[] args = {name};
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return wfs.get(0).getGUID();
   }
   
   @Override
   public String guidToName(IPSGuid guid)
   {
      PSWorkflow workflow = (PSWorkflow) load(guid);
      return workflow.getName();
   }
   
   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      String depTypes = PSDesignModelUtils.checkDependencies(guid);
      String name = guidToName(guid);
      if(depTypes != null)
      {
         String msg = "Skipped deletion of workflow ({0}) as it is " +
               "currently being used by ({1})";
         Object[] args = { name, depTypes };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      IPSWorkflowService service = (IPSWorkflowService) getService();
      try
      {
         service.deleteWorkflow(guid);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }
}

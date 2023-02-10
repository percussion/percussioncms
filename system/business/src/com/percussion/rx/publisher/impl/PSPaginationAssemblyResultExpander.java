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
package com.percussion.rx.publisher.impl;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSJexlHelper;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.codec.PSXmlDecoder;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import org.apache.commons.codec.EncoderException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * 
 * Expands the assembly result into multiple pages.
 * 
 * @author adamgent
 *
 */
public class PSPaginationAssemblyResultExpander extends PSAbstractAssemblyResultExpander
{

   public List<IPSAssemblyItem> expand(IPSAssemblyResult result) throws Exception
   {
      if (result.getParentPageReferenceId() != null)
      {
         ms_log.warn(
               "Found paged item that claims to be paged - skipping");
         return emptyList();
      }
      if (result.getCloneParentItem() != null)
      {
         ms_log.warn("Slot contents may not be paged - skipping");
         return emptyList();
      }
      
      PSJexlEvaluator eval = new PSJexlEvaluator(result.getBindings());
      Number count = (Number) eval.evaluate(PAGE_COUNT);
      
      // Create work items for the additional pages.
      // NOTE, the original item entry (in the pub log) will be replaced be
      // the 1st cloned item. so the # of added items are "count" -1.  
      List<IPSAssemblyItem> clonedItems = new ArrayList<>();
      IPSAssemblyResult orig = result;
      for(int i = 0; i < count.intValue(); i++)
      {
         IPSAssemblyItem clone = clone(orig);
         clone.setPage(i + 1);
         if (i == 0)
            clone.setReferenceId(orig.getReferenceId());
         setDeliveryLocation(clone);
         clonedItems.add(clone);
      }
      
      return clonedItems;
   }
   
   /**
    * Set the delivery location. This is mostly used by paginated pages.
    * @param workitem the work item, assumed not <code>null</code>.
    * @throws PSAssemblyException if assembly error occurs.
    * @throws EncoderException if encode error occurs.
    */
   private void setDeliveryLocation(IPSAssemblyItem workitem)
      throws PSAssemblyException, EncoderException
   {
      PSXmlDecoder dec = new PSXmlDecoder();
      PSLocationUtils lutils = new PSLocationUtils();
      IPSAssemblyTemplate template = getWorkitemTemplate(workitem);
      
      String oldContext = workitem.getParameterValue(
            IPSHtmlParameters.SYS_CONTEXT, null);
      
      workitem.setParameterValue(IPSHtmlParameters.SYS_CONTEXT, 
            String.valueOf(workitem.getDeliveryContext()));
      
      String location = (String) dec.encode(lutils
            .generateToPage(workitem, template, workitem.getPage()));
      workitem.setDeliveryPath(location);
      
      workitem.setParameterValue(IPSHtmlParameters.SYS_CONTEXT, 
            oldContext);
   }

   /**
    * Gets the template that is used by the specified work item.
    * 
    * @param workitem the work item, assumed not <code>null</code>.
    * 
    * @return the original template that was used by the item or the
    * "default" template of the item if the original template is not specified.
    * Never <code>null</code>.
    * 
    * @throws PSAssemblyException if assembly error occurs.
    */
   private IPSAssemblyTemplate getWorkitemTemplate(IPSAssemblyItem workitem)
      throws PSAssemblyException   
   {
      IPSAssemblyTemplate template = workitem.getTemplate();
      if (template != null)
        return template;
      
      IPSGuid templateId = workitem.getOriginalTemplateGuid();
      if (templateId != null) 
      {
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         template = asm.findTemplate(templateId);
         if (template != null)
            return template;
      }
      
      PSLocationUtils lutils = new PSLocationUtils();
      return lutils.findDefaultTemplate(workitem);
   }
   
   /**
    * Static expression for page count.
    */
   private static final IPSScript PAGE_COUNT = 
      PSJexlHelper.createStaticExpression("$sys.pagecount");
   
   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Logger ms_log = LogManager.getLogger(PSPaginationAssemblyResultExpander.class);
}

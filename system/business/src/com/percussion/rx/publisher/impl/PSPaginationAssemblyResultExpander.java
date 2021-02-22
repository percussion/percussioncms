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
package com.percussion.rx.publisher.impl;

import static java.util.Collections.emptyList;

import com.percussion.deploy.server.PSJexlHelper;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.codec.PSXmlDecoder;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   private static final Log ms_log = LogFactory
         .getLog(PSPaginationAssemblyResultExpander.class);
}

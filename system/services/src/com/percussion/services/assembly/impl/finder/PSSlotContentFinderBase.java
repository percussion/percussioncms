/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.services.assembly.impl.finder;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.jexl.PSAssemblerUtils;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.PSFilterException;
import com.percussion.util.IPSHtmlParameters;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base slot content finder provides the common functionality needed by each
 * slot content finder implementation. The general pattern (not followed for
 * managed nav) is to implement the abstract method
 * {@link #getContentItems(IPSAssemblyItem, IPSTemplateSlot, Map)}. This method
 * provides the information to a general implementation of the
 * {@link #find(IPSAssemblyItem, IPSTemplateSlot, Map)} method.
 * <p>
 * The base class <code>find</code> method filters and organizes the returned
 * slot items into a set of assembly items to be assembled.
 * 
 * @author dougrand
 * 
 */
public abstract class PSSlotContentFinderBase extends PSContentFinderBase<IPSTemplateSlot>
implements IPSSlotContentFinder
{
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSSlotContentFinder#find(com.percussion.services.assembly.IPSAssemblyItem,
    *      com.percussion.services.assembly.IPSTemplateSlot, java.util.Map)
    */
   @Override
   public List<IPSAssemblyItem> find(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors)
           throws RepositoryException, PSFilterException, PSAssemblyException, PSNotFoundException {
      Map<String, Object> params = mergeParameters(slot, selectors);
      return super.find(sourceItem, slot, params);
   }

   /**
    * Merges arguments of the slot finder with the given parameters.
    * 
    * @param slot the slot that contains the finder in question, assumed
    * not <code>null</code>.
    * @param params the parameters that are passed to the 
    * {@link #find(IPSAssemblyItem, IPSTemplateSlot, Map)}.
    * 
    * @return the merged parameters.
    */
   protected Map<String, Object> mergeParameters(IPSTemplateSlot slot,
         Map<String, Object> params)
   {
      Map<String, ? extends Object> args = slot.getFinderArguments();
      Map<String, Object> mergedParams = new HashMap<>();
      mergedParams.putAll(args);
      mergedParams.putAll(params);
      return mergedParams;
   }
   
   @Override
   protected IPSAssemblyItem createAssemblyItem(ContentItem slotitem, 
         IPSAssemblyItem sourceItem,
         String templatename, IPSAssemblyService asm,
         IPSTemplateSlot slot) throws PSAssemblyException
   {
      IPSAssemblyItem clone = super.createAssemblyItem(slotitem, sourceItem, 
            templatename, asm, slot);

      boolean isAaSlot = new PSAssemblerUtils().isAASlot(slot);

      // If it is not AA slot and sys_command=editrc
      clone.getParameters().put(IPSHtmlParameters.SYS_FORAASLOT, new String[]
      {Boolean.toString(isAaSlot)});

      return clone;
   }


}

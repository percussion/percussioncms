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
package com.percussion.services.assembly.impl.finder;

import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getValue;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSProxyNode;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

/**
 * The nav slot content finder returns a single item to use for managed
 * navigation. The returned item is a navigation node (navon or navtree) 
 * that relates to the specified item. Both items are under the same folder. 
 * The returned navigation node can be accessed from "$nav.self" binding of the 
 * returned assembly item. The navigation node implements {@link IPSProxyNode}.
 * In addition, the binding of "$nav.root" is the root of the navigation.
 * <p>
 * All navigation nodes, from the related node to the root of the navigation
 * are filtered by the item filter, which is specified in the given item.
 * <p>
 * The parameter of the navigation slot finder:
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>template</td>
 * <td>The template to use to format the returned items</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 */
public class PSNavSlotContentFinder extends PSSlotContentFinderBase
{
   /** (non-Javadoc)
    * @see com.percussion.services.assembly.IPSSlotContentFinder#find(com.percussion.services.assembly.IPSAssemblyItem, com.percussion.services.assembly.IPSTemplateSlot, java.util.Map)
    */
   @Override
   public List<IPSAssemblyItem> find(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors)
         throws RepositoryException, PSAssemblyException
   {
      Map<String, Object> params = mergeParameters(slot, selectors);
      String templateNameId = getValue(params, PARAM_TEMPLATE, null);
      
      IPSAssemblyItem item = PSNavFinderUtils.findItem(sourceItem, templateNameId);
      
      if (item == null)
         return Collections.emptyList();
      else
         return Collections.singletonList(item);
   }

   /** (non-Javadoc)
    * @see com.percussion.services.assembly.IPSSlotContentFinder#getType()
    */
   public Type getType()
   {
      return Type.COMPUTED;
   }

   /*
    * //see base class method for details
    */
   protected Set<ContentItem> getContentItems(@SuppressWarnings("unused")
   IPSAssemblyItem sourceItem, @SuppressWarnings("unused")
   IPSTemplateSlot slot, @SuppressWarnings("unused")
   Map<String, Object> params)
   {
      return null;
   }

}

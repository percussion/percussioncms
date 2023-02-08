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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSTemplateSlot;

import java.io.File;
import java.util.Map;
import java.util.Set;


/**
 * Find content related to the source item by a specified slot, where it is the
 * value of the "sys_slotid" property of the relationships.
 * The parameters of the finder are:
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>template</td>
 * <td>Optional parameter. If it is specified, the template will be set to the
 * returned items, which can be used to format the returned items.</td>
 * </tr>
 * <tr>
 * <td>order_by</td>
 * <td>Optional parameter. If it is specified, then the returned items will 
 * be re-ordered according to the specified value; otherwise the returned 
 * items are ordered by {@link PSContentFinderBase.ContentItem}.</td>
 * </tr>
 * <tr>
 * <td>max_results</td>
 * <td>Optional parameter. It is the maximum number of the returned result
 * from the find method if specified, zero or negative indicates no limit. 
 * It defaults to zero if not specified.</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 */
public class PSRelationshipSlotFinder extends PSSlotContentFinderBase
{
   /**
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   @Override
   @SuppressWarnings("unused")   
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      m_finderUtils.init();
   }

   /** (non-Javadoc)
    * @see com.percussion.services.assembly.IPSSlotContentFinder#getType()
    */
   public Type getType()
   {
      return Type.ACTIVE_ASSEMBLY;
   }

   /**
    * @see PSRelationshipFinderUtils#getContentItems(IPSAssemblyItem, long, Map)
    */   
   @Override
   protected Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> params)
   {
      return m_finderUtils.getContentItems(sourceItem, slot.getGUID().longValue(), params);
   }
   
   /**
    * The finder utility object, never <code>null</code>. It is initialized
    * by {@link #init(IPSExtensionDef, File)}, and never modified after that.
    */
   private PSRelationshipSlotFinderUtils m_finderUtils = new PSRelationshipSlotFinderUtils();
}

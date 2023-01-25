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

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.PSSiteManagerException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.Set;

import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getValue;

/**
 * The auto slot content finder allows a slot to be filled with items returned
 * by a query. Note that any projection specified in the
 * 
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>query</td>
 * <td>The JSR-170 query to be performed</td>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>Optional parameter. It is the type of the query, either "sql" or "xpath". 
 * It defaults to "sql" if not specified. Note that only "sql" support is 
 * officially supported at this time.</td>
 * </tr>
 * <tr>
 * <td>template</td>
 * <td>The template to use to format the returned items. This is a required 
 * parameter.</td>
 * </tr>
 * <tr>
 * <td>max_results</td>
 * <td>Optional parameter. It is the maximum number of the returned result
 * from the find method if specified, zero or negative indicates no limit. 
 * It defaults to zero if not specified.</td>
 * </tr>
 * <tr>
 * <td>mayHaveCrossSiteLinks</td>
 * <td>Optional parameter. If it is <code>true</code>, then the site ID will be 
 * set for the returned assembly items.</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 * 
 */
public class PSAutoSlotContentFinder extends PSSlotContentFinderBase
{

   private static Logger log = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);

   public PSAutoSlotContentFinder(){
      super();
      this.utils = (IPSAutoFinderUtils) PSBaseServiceLocator.getBean("sys_autoFinderUtils");
   }
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSSlotContentFinder#getType()
    */
   public Type getType()
   {
      return Type.AUTOSLOT;
   }
   
   @Override
   @SuppressWarnings("unused")
   protected Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors) throws PSNotFoundException, RepositoryException {
      String template = getValue(selectors, PARAM_TEMPLATE, null);
      if (StringUtils.isBlank(template))
      {
         throw new IllegalArgumentException("template is a required argument");
      }

      try {
         return utils.getContentItems(sourceItem, slot.getGUID().longValue(), selectors, null);
      } catch (PSSiteManagerException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RepositoryException(e);
      } catch (PSNotFoundException | RepositoryException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw e;
      }
   }

   /**
    * The utility object, used to fetch the content items.
    */
   private IPSAutoFinderUtils utils;
}

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

import com.percussion.cms.PSCmsException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSProxyNode;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provide utility method for navigation finder plugins.
 * 
 * @author YuBingChen
 */
public class PSNavFinderUtils
{
   /**
    * Finds the sibling navigation node (navon or navtree) for the specified
    * item. Both the sibling navigation node and the given item are under the
    * same folder. The looked up navigation node can be accessed from the
    * binding of the returned assembly item from "$nav.self". This node
    * implements {@link IPSProxyNode}. In addition, the binding of "$nav.root"
    * is the navtree, the root of the navigation. All navigation nodes are
    * filtered by the item filter specified in the given item.
    * 
    * @param sourceItem the specified item, not <code>null</code>.
    * @param templateNameId the name or ID of the template used to render the
    *            navigations, it may be blank if not defined.
    * 
    * @return the assembly item with the binding values described above. It may
    *         <code>null</code> if there is no sibling navigation node.
    * 
    * @throws RepositoryException if failed to retrieve node properties.
    * @throws PSAssemblyException if failed to clone the given item.
    */
   public static IPSAssemblyItem findItem(IPSAssemblyItem sourceItem,
         String templateNameId) 
      throws RepositoryException, PSAssemblyException
   {
      if (sourceItem == null)
      {
         throw new IllegalArgumentException("sourceItem may not be null");
      }

      Node navon = null;
      try
      {
         PSNavHelper helper = sourceItem.getNavHelper();
         navon = helper.findNavNode(sourceItem);
      }
      catch (Exception e1)
      {
         ms_log.warn("Problem finding the navon for {} with template {} - probably previewing a managed nav slot outside of a site. Caught exception: {}",
                 sourceItem.getId(), templateNameId, PSExceptionUtils.getMessageForLog(e1));
         // If we have a problem finding the node, just set it to null and
         // we'll have no navon. This will generally happen when previewing
         // outside of a site
         navon = null;
      }

      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();

      if (navon == null)
         return null;

      IPSAssemblyItem clone = null;
      try
      {
         IPSNode internalNode = (IPSNode) navon.getProperty("nav:proxiedNode")
               .getNode();
         Map<String, IPSGuid> optionalParams = new HashMap<>();
         IPSGuid templateId = StringUtils.isBlank(templateNameId) ? sourceItem
               .getTemplate().getGUID() : null;
         clone = PSContentFinderBase.getCloneAssemblyItem(sourceItem, asm,
               templateNameId, internalNode.getGuid(), templateId,
               optionalParams);
         clone.setNode(navon);
         return clone;
      }
      catch (CloneNotSupportedException e)
      {
         throw new RuntimeException(e);
      }
      catch (PSCmsException e)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.ITEM_CREATION, e);
      }
   }

   /**
    * Logger for content finder
    */
   private static final Logger ms_log = LogManager.getLogger(PSNavFinderUtils.class);

}

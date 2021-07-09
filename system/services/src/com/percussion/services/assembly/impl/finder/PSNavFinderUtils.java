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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly.impl.finder;

import com.percussion.cms.PSCmsException;
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
         ms_log.warn("Problem finding the navon - probably previewing a managed nav slot outside of a site. Caught exception: "
                     + e1);
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

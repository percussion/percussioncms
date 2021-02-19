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
package com.percussion.services.contentmgr.impl.jsrdata;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.PSContentMgrOption;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.PSCollectionRangeIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * Implementation of a node iterator for use in queries
 * 
 * @author dougrand
 */
public class PSQueryNodeIterator extends PSCollectionRangeIterator<IPSGuid>
      implements
         NodeIterator
{
   public PSQueryNodeIterator(Collection<IPSGuid> collection) {
      super(collection);
   }

   public Node nextNode()
   {
      IPSGuid nodeGuid = next();
      if (nodeGuid == null)
      {
         throw new NoSuchElementException();
      }
      IPSContentMgr cms = PSContentMgrLocator.getContentMgr();
      List<IPSGuid> ids = new ArrayList<>();
      ids.add(nodeGuid);
      PSContentMgrConfig config = new PSContentMgrConfig();
      config.addOption(PSContentMgrOption.LOAD_MINIMAL);
      try
      {
         Collection<Node> nodes = cms.findItemsByGUID(ids, config);
         return nodes.iterator().next();
      }
      catch (RepositoryException e)
      {
         throw new NoSuchElementException("Problem retrieving the next node: "
               + e.getLocalizedMessage());
      }
   }

}

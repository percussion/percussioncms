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

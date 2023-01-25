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
package com.percussion.services.contentmgr.impl.legacy;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.services.contentmgr.impl.PSContentInternalLocator;
import com.percussion.utils.beans.IPSPropertyLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Load the body object for the content repository
 * 
 * @author dougrand
 */
public class PSContentPropertyLoader implements IPSPropertyLoader, Serializable
{
   /**
    * Serializable id that will cause an error when the class changes and an 
    * old version is loaded.
    */
   private static final long serialVersionUID = 1L;

   /**
    * Logger for property loader
    */
   private static final Logger ms_log = LogManager.getLogger(PSContentPropertyLoader.class);
   
   /**
    * Contained data reference. Set on first reference or set is called.
    */
   private Object m_data = null;

   /**
    * The node with the property, never <code>null</code> after ctor.
    */
   private Node m_node;

   /**
    * Ctor
    * @param node the node, never <code>null</code>
    */
   public PSContentPropertyLoader(Node node) 
   {
      if (node == null)
      {
         throw new IllegalArgumentException("node may not be null");
      }
      m_node = node;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.utils.beans.IPSPropertyLoader#getLazy()
    */
   public Object getLazy()
   {
      if (m_data == null)
      {
         IPSContentRepository rep = PSContentInternalLocator
               .getLegacyRepository();
         try
         {
            List<Node> nodes = new ArrayList<>();
            nodes.add(m_node);
            rep.loadBodies(nodes);
         }
         catch (RepositoryException e)
         {
            ms_log.error("Error while trying to load a body field. Error: {}", PSExceptionUtils.getMessageForLog(e));
         }
      }
      return m_data;
   }
   
   /**
    * Called while the repository processes the body request
    * @param data the loaded object
    */
   public void setData(Object data)
   {
      m_data = data;
   }

}

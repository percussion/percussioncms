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
package com.percussion.services.contentmgr.impl.legacy;

import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.services.contentmgr.impl.PSContentInternalLocator;
import com.percussion.utils.beans.IPSPropertyLoader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   private static Log ms_log = LogFactory.getLog(PSContentPropertyLoader.class);
   
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
            ms_log.error("Error while trying to load a body field", e);
         }
         catch (Exception e){
            e.printStackTrace();
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

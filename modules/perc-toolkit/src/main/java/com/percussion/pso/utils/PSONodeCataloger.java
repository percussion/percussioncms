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
/*
 * com.percussion.pso.utils PSONodeCataloger.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;

/**
 * Finds Property Definitions for a given Node Definition.
 * This can be used to catalog node types (or content types). 
 * <p>
 * Note that the names of all Node Definitions and Property Definitions
 * begin with "rx:".  This is how they are returned from the server.  
 *
 * @author DavidBenua
 *
 */
public class PSONodeCataloger
{
   private static final Logger log = LogManager.getLogger(PSONodeCataloger.class);
   
   private IPSContentMgr cmgr = null; 
   
   /**
    * Sole constructor. 
    */
   public PSONodeCataloger()
   {
      
   }
   
   /**
    * Initialize service pointers. 
    */
   private void init()
   {
      if(cmgr == null)
      {
         cmgr = PSContentMgrLocator.getContentMgr();
      }
   }
   
   /**
    * Get Content Type Names
    * @return return the list of content type names defined in the system. 
    * @throws RepositoryException
    */
   public List<String> getContentTypeNames() throws RepositoryException 
   {
      init();
      log.trace("getting content type names"); 
      List<String> names = new ArrayList<String>(); 
      
      List<IPSNodeDefinition> nodeDefs = cmgr.findAllItemNodeDefinitions();
      for(IPSNodeDefinition nodeDef : nodeDefs)
      {
         names.add(nodeDef.getName());
      }
      log.debug("content type names " + names); 
      return names;
  
   }
   
   
   /**
    * Get Content Type Names with a specified field; 
    * @param fieldName the field name to search for.
    * @return the names of the content types.  Never <code>null</code> but may be 
    * <code>empty</code>
    * @throws RepositoryException if the content type is not found. 
    */
   public List<String> getContentTypeNamesWithField(String fieldName) throws RepositoryException
   {
      init();
      List<String> names = new ArrayList<String>(); 
      if(!fieldName.startsWith("rx:"))
      {
         fieldName = "rx:" + fieldName;
      }
      List<IPSNodeDefinition> nodeTypes = cmgr.findAllItemNodeDefinitions();
      for(IPSNodeDefinition nd : nodeTypes)
      {
         //log.trace("examining node type " + nd.getName());
         NodeType nt = nd.getDeclaringNodeType();
         if(nt != null)
         {
            PropertyDefinition[] props = nt.getDeclaredPropertyDefinitions(); 
            for(PropertyDefinition p : props)
            {
               //log.trace("examining field named " + p.getName());
               if(p.getName().equals(fieldName) )
               {
                  names.add(nt.getName());
                  break; 
               }
            }
         }
      }
      log.debug("content type names for field " + fieldName + " -- " + names); 
      return names;
      
   }

   /**
    * Gets the field names for a given content type.  
    * @param typeName the type name.  If the typename does not begive with "rx:", it will be added. 
    * @return the list of field names. Never <code>null</code> but may be 
    * <code>empty</code>
    * @throws NoSuchNodeTypeException
    * @throws RepositoryException
    */
   public List<String> getFieldNamesForContentType(String typeName) throws NoSuchNodeTypeException, RepositoryException
   {
      init();
      if(!typeName.startsWith("rx:"))
      {
         typeName = "rx:" + typeName ;
      }
      List<String> names = new ArrayList<String>();
      IPSNodeDefinition nodeDef = cmgr.findNodeDefinitionByName(typeName);
      NodeType nt = nodeDef.getDeclaringNodeType(); 
      PropertyDefinition[] props = nt.getDeclaredPropertyDefinitions();
      for(PropertyDefinition p : props)
      {
         names.add(p.getName());
      }
   
      log.debug("field names for content type " + typeName + " -- " + names); 
      return names; 
   }

   /**
    * Set the content manager service pointer.  Used only for unit testing. 
    * @param cmgr the cmgr to set
    */
   public void setCmgr(IPSContentMgr cmgr)
   {
      this.cmgr = cmgr;
   }
   
}

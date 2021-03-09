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
package com.percussion.design.objectstore;

import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A collection of PSRelationshipConfig objects.
 */
public class PSRelationshipConfigSet extends PSCollectionComponent
   implements IPSConfig
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSRelationshipConfigSet(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      this();

      fromXml(sourceNode, parentDoc, parentComponents);
   }
   
   /**
    * Default constructor.
    */
   public PSRelationshipConfigSet()
   {
      super(PSRelationshipConfig.class);
   }

   /**
    * Convenience constructor, calls
    * {@link #PSRelationshipConfigSet(Element, IPSDocument, ArrayList) 
    * PSRelationshipConfigSet(Element, null, null)}
    */
   public PSRelationshipConfigSet(Element sourceNode)
         throws PSUnknownNodeTypeException {
      this(sourceNode, null, null);
   }
   
   /**
    * Get the relationship configuration for the supplied name of the
    * relationship. The check is case-sensitive.
    *
    * @param name the relationship configuration name, not <code>null</code>
    *    or empty.
    * @return the relationship configuration, might be <code>null</code>
    *    if not found for name.
    * @throws IllegalArgumentException if the supplied name is <code>null</code>
    *    or empty.
    */
   public PSRelationshipConfig getConfig(String name)
   {
      if (name == null || name.trim().length() == 0)
        throw new IllegalArgumentException("name cannot be null or empty");

      Iterator configs = iterator();
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         if (config.getName().equals(name))
            return config;
      }

      return null;
   }

   /**
    * Get the relationship configuration with the supplied GUID.
    *
    * @param id the GUID of the requested relationship configuration, not 
    *    <code>null</code>.
    *    
    * @return the relationship configuration with the given GUID. It may be 
    *    <code>null</code> if not found.
    *    
    * @throws IllegalArgumentException if the supplied GUID is <code>null</code>
    */
   public PSRelationshipConfig getConfig(IPSGuid id)
   {
      if (id == null)
        throw new IllegalArgumentException("GUID (id) cannot be null or empty");

      Iterator configs = iterator();
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         if (config.getGUID().longValue() == id.longValue())
            return config;
      }

      return null;
   }

   /**
    * Get the relationship configuration for the supplied category. The check is
    * case-sensitive.
    *
    * @param category the relationship configuration category, not 
    *    <code>null</code> or empty.
    * @return the relationship configuration, might be <code>null</code>
    *    if not found for the category.
    * @throws IllegalArgumentException if the supplied category is <code>null</code>
    *    or empty.
    */
   public PSRelationshipConfig getConfigByCategory(String category)
   {
      if (category == null || category.trim().length() == 0)
        throw new IllegalArgumentException("category cannot be null or empty");

      Iterator configs = iterator();
      String value = null;
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         value = config.getCategory();
         if (value != null && value.equals(category))
            return config;
      }
      return null;
   }

   /**
    * Gets a list of configs for the specified type.
    * 
    * @param type the type of the returned configs, which must be either
    *    {@link PSRelationshipConfig#RS_TYPE_SYSTEM} or
    *    {@link PSRelationshipConfig#RS_TYPE_USER}.
    *     
    * @return the specified configs, never <code>null</code>, may be empty.
    */
   public List<PSRelationshipConfig> getConfigsByType(String type)
   {
      if ((!PSRelationshipConfig.RS_TYPE_SYSTEM.equals(type))
            && (!PSRelationshipConfig.RS_TYPE_USER.equals(type)))
         throw new IllegalArgumentException(
               "type must be either PSRelationshipConfig.RS_TYPE_SYSTEM or PSRelationshipConfig.RS_TYPE_USER.");
      
      List<PSRelationshipConfig> list = new ArrayList<>();
      Iterator configs = iterator();
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         if (config.getType().equals(type))
            list.add(config);
      }
      return list;      
   }
   
   /**
    * Get all relationships for a given relationship category. The check is
    * case-sensitive.
    *
    * @param category the relationship category, not <code>null</code> or empty.
    * @return iterator of relationship configurations for the given category or
    * category, never <code>null</code> but may be empty.
    * @throws IllegalArgumentException if the supplied category is <code>null</code>
    *    or empty.
    */
   public Iterator getConfigsByCategory(String category)
   {
      return getConfigListByCategory(category).iterator();
   }

   /**
    * @return a list of all relationship configurations.
    */
   public List<PSRelationshipConfig> getConfigList()
   {
      List<PSRelationshipConfig> list = new ArrayList<PSRelationshipConfig>();
      Iterator configs = iterator();
      while (configs.hasNext())
         list.add((PSRelationshipConfig)configs.next());
      
      return list;
   }
   
   /**
    * Just like {@link #getConfigsByCategory(String)}, except this returns
    * a list of relationship configs. 
    */
   public List<PSRelationshipConfig> getConfigListByCategory(
         String category)
   {
      if (category == null || category.trim().length() == 0)
        throw new IllegalArgumentException("category cannot be null or empty");

      List<PSRelationshipConfig> list = new ArrayList<>();
      Iterator configs = iterator();
      String value = null;
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         value = config.getCategory();
         if (value != null && value.equals(category))
            list.add(config);
      }
      return list;
   }

   
   /**
    * Get the relationship configuration for the supplied name or category. The
    * check is case-sensitive.
    *
    * @param nameOrCategory the relationship configuration name or category, not
    * <code>null</code> or empty. It assumed to be name first and if a
    * relationship is not found then it assumed to be category.
    * @return the relationship configuration, might be <code>null</code>
    *    if not found for the name or category.
    * @throws IllegalArgumentException if the supplied name or category is
    * <code>null</code> or empty.
    */
   public PSRelationshipConfig getConfigByNameOrCategory(String nameOrCategory)
   {
      if (nameOrCategory == null || nameOrCategory.trim().length() == 0)
        throw new IllegalArgumentException("nameOrCategory cannot be null or empty");
      PSRelationshipConfig config = getConfig(nameOrCategory);
      if(config == null)
         config = getConfigByCategory(nameOrCategory);
      return config;
   }

   /**
    * Creates a 'User' relationship configuration with the specified name and
    * adds that to this list. The configuration will have default system
    * properties.
    *
    * @param name the name of the config, may not be <code>null</code> or empty.
    * Must be unique among this collection of configurations. Can use {@link
    * #getConfig(String) } to check for uniqueness of the configuration name.
    * @param type the type of the rel  config, RS_TYPE_SYSTEM or RS_TYPE_USER, 
    * may not be <code>null</code> or empty.
    * 
    * @return the added relationship config, never <code>null</code>
    *
    * @throws IllegalArgumentException if name is invalid.
    */
   public PSRelationshipConfig addConfig(String name, String type)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      if (type == null
         || (!type.equals(PSRelationshipConfig.RS_TYPE_SYSTEM)
            && !type.equals(PSRelationshipConfig.RS_TYPE_USER)))
      {
         throw new IllegalArgumentException(
         "type must not be null and must be one of RS_TYPE_XXXX values.");
      }


      if(getConfig(name) != null)
      {
         throw new IllegalArgumentException("can not add config with name " +
            name + ", reason:duplicate");
      }

      PSRelationshipConfig rsConfig = new PSRelationshipConfig(name, type);
      add(rsConfig);

      return rsConfig;
   }

   /**
    * Deletes the configuration with the supplied name if the configuration is
    * an 'User' configuration.
    *
    * @param name the name of configuration, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if name is not valid or it represents a
    * system configuration.
    */
   public void deleteConfig(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      PSRelationshipConfig config =  getConfig(name);
      if(config != null)
      {
         if(config.isUser())
            remove(config);
         else
            throw new IllegalArgumentException(
               "name represents system configuration, can not delete.");
      }
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.design.objectstore.IPSComponent#fromXml(org.w3c.dom.Element, com.percussion.design.objectstore.IPSDocument, java.util.ArrayList)
    */
   @Override
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         node = tree.getNextElement(
            PSRelationshipConfig.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            // Use createMemberObject() to create the member objects because the
            // member objects may be created by a class that is derived from
            // PSRelationshipConfig
            PSRelationshipConfig config = createMemberObject((Element) tree
                  .getCurrent(), parentDoc, parentComponents);
            
            // check duplicates
            if (getConfig(config.getName()) != null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSRelationshipConfig.XML_NODE_NAME,
                  "Duplicate entry, must be unique server wide: " +
                     config.getName()
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            add(config);

            node = tree.getNextElement(
               PSRelationshipConfig.XML_NODE_NAME, nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Creates a member object from its XML representation.
    * 
    * @param sourceNode the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    it may be <code>null</code>.
    * @param parentComponents the parent objects of this object, it may be
    *    <code>null</code>.
    *    
    * @return the created object, never <code>null</code>.
    *     
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   protected PSRelationshipConfig createMemberObject(Element sourceNode,
         IPSDocument parentDoc, ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      return new PSRelationshipConfig(sourceNode, parentDoc, parentComponents);
   }
   

   /*
    *  (non-Javadoc)
    * @see com.percussion.design.objectstore.IPSComponent#toXml(org.w3c.dom.Document)
    */
   @Override
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      for (int i=0; i<size(); i++)
      {
         IPSComponent config = (IPSComponent) get(i);
         root.appendChild(config.toXml(doc));
      }

      return root;
   }

   //implements IPSConfig interface method
   public String getConfigString()
   {
      Document configDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(configDoc);
      return PSXmlDocumentBuilder.toString(config);
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXRelationshipConfigSet";
}

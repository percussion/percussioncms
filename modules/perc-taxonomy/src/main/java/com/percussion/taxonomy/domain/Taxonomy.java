/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.taxonomy.domain;

import java.util.Collection;

/**
 * Taxonomy class to represent information regarding taxonomy.
 *  
 */
public class Taxonomy
{

   private int id;

   private String name;

   private int admin_role_id;

   private String scheme;

   private Collection<Node> nodes;

   private Collection<Attribute> attributes;

   private Collection<Visibility> visibilities;

   private boolean has_related_ui;

   /**
    * Returns collection of visibilities of a Taxonomy
    * 
    * @return visibilities - collection of visibilities
    */
   public Collection<Visibility> getVisibilities()
   {
      return visibilities;
   }

   /**
    * Set collection of visibilities of a Taxonomy 
    * @param visibilities - Visibilities collection
    */
   public void setVisibilities(Collection<Visibility> visibilities)
   {
      this.visibilities = visibilities;
   }

   /**
    * Returns boolean true or false to determine whether a Taxonomy has related taxons ui   
    * @return has_related_ui - true or false to identify whether a Taxonomy has related taxons 
    */
   public boolean getHas_related_ui()
   {
      return has_related_ui;
   }

   /**
    * Set boolean true or false to determine whether a Taxonomy has related taxons ui  
    * @param hasRelatedUi -  boolean true or false to set for related taxons ui or not
    */
   public void setHas_related_ui(boolean hasRelatedUi)
   {
      has_related_ui = hasRelatedUi;
   }

   /**
    * Returns collection of Taxonomy nodes 
    * @return nodes - collection of taxonomy nodes
    */
   public Collection<Node> getNodes()
   {
      return nodes;
   }

   /**
    * Set collection of nodes
    * @param nodes - collection of taxonomy nodes
    */
   public void setNodes(Collection<Node> nodes)
   {
      this.nodes = nodes;
   }

   /**
    * Add node to a taxonomy
    * @param node - Node to add to a taxonomy
    */
   public void addNode(Node node)
   {
      node.setTaxonomy(this);
      nodes.add(node);
   }

   /**
    * Returns collection of Taxonomy attributes
    * @return attributes - collection of attributes
    */
   public Collection<Attribute> getAttributes()
   {
      return attributes;
   }

   /**
    * Set collection of taxonomy attribites
    * @param attributes - taxonomy attributes
    */
   public void setAttributes(Collection<Attribute> attributes)
   {
      this.attributes = attributes;
   }

   /**
    * Add an attribute to a Taxonomy
    * @param attribute 
    */
   public void addAttribute(Attribute attribute)
   {
      attribute.setTaxonomy(this);
      attributes.add(attribute);
   }

   /**
    * Returns unique int id of Taxonomy
    * @return id - int id value of Taxonomy
    */
   public int getId()
   {
      return id;
   }

   /**
    * Set unique int id of Taxonomy
    * @param id - taxonomy id
    */
   public void setId(int id)
   {
      this.id = id;
   }

   /**
    * Returns name of the Taxonomy
    * @return name - String taxonomy name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set taxonomy name
    * @param name
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Returns id of Admin_role 
    * @return admin_role_id - Taxonomy admin role id
    */
   public int getAdmin_role_id()
   {
      return admin_role_id;
   }

   /**
    * Set Admin role id
    * @param admin_role_id - int admin_role_id
    */
   public void setAdmin_role_id(int admin_role_id)
   {
      this.admin_role_id = admin_role_id;
   }

   /**
    * Returns Taxonomy Scheme
    * @return scheme
    */
   public String getScheme()
   {
      return scheme;
   }

   /**
    * Set Taxonomy scheme
    * @param scheme
    */
   public void setScheme(String scheme)
   {
      this.scheme = scheme;
   }

}

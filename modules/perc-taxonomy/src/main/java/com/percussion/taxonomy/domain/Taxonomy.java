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

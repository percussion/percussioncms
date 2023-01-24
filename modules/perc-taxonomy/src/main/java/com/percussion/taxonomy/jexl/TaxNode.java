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
package com.percussion.taxonomy.jexl;

import com.percussion.taxonomy.domain.Attribute_lang;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Read only Class for use in Jexl that represents a Taxonomy Taxon.
 * 
 * @author stephenbolton
 * 
 */
public class TaxNode
{
   private int id;

   private String name;

   private String path;

   private TaxNode parent;

   private boolean isLeaf;

   private HashMap<String, TaxValues> attributes;

   /**
    * Create a Taxon Node from the underlying Hibernate object
    * @param node
    */
   public TaxNode(Node node)
   {
      this.id = node.getId();
      if (node.getParent() != null)
      {
         this.parent = new TaxNode(node.getParent());
      }
      this.isLeaf = !node.getNot_leaf();
      attributes = new HashMap<String, TaxValues>();
      String firstAttName = null;
      boolean useFirstAttForName = false;
      for (Value value : node.getValues())
      {
         Collection<Attribute_lang> attLangs = value.getAttribute().getAttribute_langs();
         for (Attribute_lang lang : attLangs)
         {
            boolean isNodeName = lang.getAttribute().getIs_node_name() == 1;
            if (value.getLang().getName().equals(lang.getLanguage().getName()))
            {
               TaxValues currentValues = attributes.get(lang.getName());
               if (currentValues == null)
               {
                  currentValues = new TaxValues();
                  attributes.put(lang.getName(), currentValues);
               }
               currentValues.add(value.getName());
               if (firstAttName == null && value.getName() != null)
                  firstAttName = value.getName();
               if (isNodeName)
               {
                  if (this.name == null)
                  {
                     this.name = value.getName();
                  }
                  else
                  {
                     useFirstAttForName = true;
                  }

               }
            }
         }
      }
      if (useFirstAttForName || this.name == null)
      {
         this.name = firstAttName;
      }
      
      if (node.getParent()!=null) {
         this.parent = new TaxNode(node.getParent());
      }
     
   }

   
   
   /**  Get The node id
    * @return the id
    */
   public int getId()
   {
      return id;
   }

   /**
    * Get the Node Name
    * @return
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the heirarchy path for the Taxonomy node
    * @return The path
    */
   public String getPath()
   {
      if (path==null) {
         calculatePath();
      }
      return path;
   }

   /**
    * Get the parent Taxonomy node.
    * @return the parent TaxNode
    */
   public TaxNode getParent()
   {
      return parent;
   }

   /** 
    * A HashMap containing the attributes keyed by their name
    * @return
    */
   public HashMap<String, TaxValues> getAttributes()
   {
      return attributes;
   }

   /** 
    * Does this node have any child nodes
    * @return
    */
   public boolean isLeaf()
   {
      return isLeaf;
   }

   /**
    * Generate the Taxonomy hierarchy path for the node based upon the parent structure.
    */
   private void calculatePath()
   {
      ArrayList<String> pathElements = new ArrayList<String>();
      TaxNode testNode = this.parent;
      while (testNode!=null) {
         pathElements.add(testNode.getName());
         testNode = testNode.getParent();
      }
      Collections.reverse(pathElements);
      StringBuilder sb = new StringBuilder();
      for (String path : pathElements) {
         sb.append(path);
         sb.append("/");
      }
      sb.append(this.name);
   }
}

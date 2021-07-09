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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
      StringBuffer sb = new StringBuffer();
      for (String path : pathElements) {
         sb.append(path);
         sb.append("/");
      }
      sb.append(this.name);
   }
}

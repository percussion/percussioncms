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

package com.percussion.taxonomy.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Attribute_lang;
import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Taxonomy;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.domain.Visibility;

public class TaxonomyTestUtils
{

   public static Taxonomy createTestTaxonomy()
   {
      Taxonomy tax = new Taxonomy();
      tax.setName("Test Taxonomy");

      return tax;
   }

   public static Attribute createTestAttribute(Taxonomy taxonomy, int ordinal, String createdBy)
   {
      Attribute attr = new Attribute();
      attr.setCreated_at(new Timestamp(new Date().getTime()));
      attr.setCreated_by_id(createdBy);
      attr.setModified_at(new Timestamp(new Date().getTime()));
      attr.setModified_by_id(createdBy);
      attr.setIs_node_name(0);
      attr.setModified_at(new Timestamp(new Date().getTime()));
      attr.setTaxonomy(taxonomy);
      return attr;
   }

   public static Set<Attribute_lang> createAttributeLangSet(int ordinal, Attribute attribute, Language language)
   {
      Attribute_lang attrLang = new Attribute_lang();
      attrLang.setName("Test Attribute Name " + ordinal);
      attrLang.setAttribute(attribute);
      attrLang.setLanguage(language);

      Set<Attribute_lang> langs = new HashSet<Attribute_lang>();
      langs.add(attrLang);

      return langs;
   }

   public static Node createNode(Attribute attribute, String createdBy)
   {
      Node node = new Node();
      node.setTaxonomy(attribute.getTaxonomy());
      node.setCreated_by_id(createdBy);
      node.setModified_by_id(createdBy);
      node.setModified_at(new Timestamp(new Date().getTime()));
      node.setCreated_at(new Timestamp(new Date().getTime()));
      return node;
   }

   public static Set<Value> createValueSetForNode(Attribute attribute, Node node, String createdBy, Language language)
   {
      Value value1 = new Value();
      value1.setAttribute(attribute);
      value1.setName("value 1 for attribute " + attribute.getAttribute_langs().iterator().next().getName());
      value1.setCreated_at(new Timestamp(new Date().getTime()));
      value1.setCreated_by_id(createdBy);
      value1.setNode(node);
      value1.setLang(language);

      Value value2 = new Value();
      value2.setAttribute(attribute);
      value2.setName("value 1 for attribute " + attribute.getAttribute_langs().iterator().next().getName());
      value2.setCreated_at(new Timestamp(new Date().getTime()));
      value2.setCreated_by_id(createdBy);
      value2.setNode(node);
      value2.setLang(language);

      Set<Value> values = new HashSet<Value>();
      values.add(value1);
      values.add(value2);

      return values;
   }

   /**
    * Builds a tree of nodes for testing the taxonomy node ordering for
    * deletion. The nodes will have a fake id. The generated structure is:
    * - taxonomy<br>
    *    - N1<br>
    *       - N11<br>
    *       - N12<br>
    *          - N121<br>
    *          - N122<br>
    *    - N2<br>
    *       - N21<br>
    * 
    * @return {@link List}<{@link Node}> never <code>null</code> or empty.
    */
   public static List<Node> buildTaxonomyTreeNodeTwoRoots()
   {
      Node n1 = new Node();
      n1.setId(1);
      Node n11 = new Node();
      n11.setId(11);
      Node n12 = new Node();
      n12.setId(12);
      Node n121 = new Node();
      n121.setId(121);
      Node n122 = new Node();
      n122.setId(122);
      Node n2 = new Node();
      n2.setId(2);
      Node n21 = new Node();
      n21.setId(21);

      n11.setParent(n1);
      n12.setParent(n1);
      
      n121.setParent(n12);
      n122.setParent(n12);
      
      n21.setParent(n2);
      
      List<Node> nodes = new ArrayList<Node>();
      nodes.add(n1);
      nodes.add(n11);
      nodes.add(n12);
      nodes.add(n121);
      nodes.add(n122);
      nodes.add(n2);
      nodes.add(n21);
      
      return nodes;   
   }

   /**
    * Builds a tree of nodes for testing the taxonomy node ordering for
    * deletion. The nodes will have a fake id. The generated structure is:
    * - taxonomy<br>
    *    - N1<br>
    *       - N11<br>
    *       - N12<br>
    *          - N121<br>
    *             - N121<br>
    *                - N1211<br>
    *                   - N12111<br>
    *                   - N12112<br>
    *       - N13<br>
    *          - N131<br>
    * 
    * @return {@link List}<{@link Node}> never <code>null</code> or empty.
    */
   public static List<Node> buildTaxonomyTreeNodeOneRoot()
   {
      Node n1 = new Node();
      n1.setId(1);
      Node n11 = new Node();
      n11.setId(11);
      Node n12 = new Node();
      n12.setId(12);
      Node n13 = new Node();
      n13.setId(13);
      Node n121 = new Node();
      n121.setId(121);
      Node n131 = new Node();
      n131.setId(131);
      Node n1211 = new Node();
      n1211.setId(1211);
      Node n12111 = new Node();
      n12111.setId(12111);
      Node n12112 = new Node();
      n12112.setId(12112);

      n11.setParent(n1);
      n12.setParent(n1);
      n13.setParent(n1);
      
      n121.setParent(n12);
      n131.setParent(n13);
      
      n1211.setParent(n121);
      
      n12111.setParent(n1211);
      n12112.setParent(n1211);

      List<Node> nodes = new ArrayList<Node>();
      nodes.add(n1);
      nodes.add(n11);
      nodes.add(n12);
      nodes.add(n13);
      nodes.add(n121);
      nodes.add(n131);
      nodes.add(n1211);
      nodes.add(n12111);
      nodes.add(n12112);
      
      return nodes;   
   }

   /**
    * Builds a tree of nodes for testing the taxonomy node ordering for
    * deletion. The nodes will have a fake id. The generated structure is:
    * - taxonomy<br>
    *    - N1<br>
    *       - N11<br>
    *          - N111<br>
    *             - N111<br>
    *                - N11111<br>
    *                - N11112<br>
    *    - N2<br>
    *       - N21<br>
    *          - N121<br>
    *          - N122<br>
    *    - N3<br>
    *       - N31<br>
    *          - N311<br>
    *             - N3111<br>
    *             - N3112<br>
    *             - N3113<br>
    * 
    * @return {@link List}<{@link Node}> never <code>null</code> or empty.
    */
   public static List<Node> buildTaxonomyTreeNodeThreeRoots()
   {
      Node n1 = new Node();
      n1.setId(1);
      Node n2 = new Node();
      n2.setId(2);
      Node n3 = new Node();
      n3.setId(3);
      
      Node n11 = new Node();
      n11.setId(11);
      Node n21 = new Node();
      n21.setId(21);
      Node n31 = new Node();
      n31.setId(31);

      Node n111 = new Node();
      n111.setId(111);
      Node n211 = new Node();
      n211.setId(211);
      Node n212 = new Node();
      n212.setId(212);
      Node n311 = new Node();
      n311.setId(311);
      
      Node n1111 = new Node();
      n1111.setId(1111);
      Node n3111 = new Node();
      n3111.setId(3111);
      Node n3112 = new Node();
      n3112.setId(3112);
      Node n3113 = new Node();
      n3113.setId(3113);

      Node n11111 = new Node();
      n11111.setId(11111);
      Node n11112 = new Node();
      n11112.setId(11112);

      n11.setParent(n1);
      n21.setParent(n2);
      n31.setParent(n3);
      
      n111.setParent(n11);
      n211.setParent(n21);
      n212.setParent(n21);
      n311.setParent(n31);
      
      n1111.setParent(n111);
      n3111.setParent(n311);
      n3112.setParent(n311);
      n3113.setParent(n311);
      
      n11111.setParent(n1111);
      n11112.setParent(n1111);

      List<Node> nodes = new ArrayList<Node>();
      nodes.add(n1);
      nodes.add(n2);
      nodes.add(n3);
      nodes.add(n11);
      nodes.add(n21);
      nodes.add(n31);
      nodes.add(n111);
      nodes.add(n211);
      nodes.add(n212);
      nodes.add(n311);
      nodes.add(n1111);
      nodes.add(n3111);
      nodes.add(n3112);
      nodes.add(n3113);
      nodes.add(n11111);
      nodes.add(n11112);
      return nodes;   
   }
   
   public static Visibility getVisibilityForTaxonomy(Taxonomy taxonomy, int ordinal)
   {
      Visibility v = new Visibility();
      v.setTaxonomy(taxonomy);
      v.setCommunity_id(ordinal);
      return v;
   }
}

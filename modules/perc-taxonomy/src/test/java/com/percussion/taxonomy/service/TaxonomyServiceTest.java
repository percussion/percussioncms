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

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Attribute_lang;
import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_status;
import com.percussion.taxonomy.domain.Taxonomy;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.domain.Visibility;
import com.percussion.taxonomy.repository.AttributeServiceInf;
import com.percussion.taxonomy.repository.Attribute_langServiceInf;
import com.percussion.taxonomy.repository.LanguageServiceInf;
import com.percussion.taxonomy.repository.NodeServiceInf;
import com.percussion.taxonomy.repository.Node_statusServiceInf;
import com.percussion.taxonomy.repository.TaxonomyServiceInf;
import com.percussion.taxonomy.repository.ValueServiceInf;
import com.percussion.taxonomy.repository.VisibilityServiceInf;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.apache.cactus.ServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.percussion.taxonomy.service.TaxonomyTestUtils.buildTaxonomyTreeNodeOneRoot;
import static com.percussion.taxonomy.service.TaxonomyTestUtils.buildTaxonomyTreeNodeThreeRoots;
import static com.percussion.taxonomy.service.TaxonomyTestUtils.buildTaxonomyTreeNodeTwoRoots;
import static com.percussion.taxonomy.service.TaxonomyTestUtils.createAttributeLangSet;
import static com.percussion.taxonomy.service.TaxonomyTestUtils.createNode;
import static com.percussion.taxonomy.service.TaxonomyTestUtils.createTestAttribute;
import static com.percussion.taxonomy.service.TaxonomyTestUtils.createTestTaxonomy;
import static com.percussion.taxonomy.service.TaxonomyTestUtils.createValueSetForNode;
import static com.percussion.taxonomy.service.TaxonomyTestUtils.getVisibilityForTaxonomy;
import static java.util.Arrays.asList;
import static org.springframework.util.CollectionUtils.isEmpty;


@Category(IntegrationTest.class)
public class TaxonomyServiceTest extends ServletTestCase
{
   private AttributeServiceInf attributeService;

   private Attribute_langServiceInf attributeLangSevice;

   private NodeServiceInf nodeService;

   private TaxonomyServiceInf taxonomyService;

   private ValueServiceInf valueService;

   private Node_statusServiceInf nodeStatusService;

   private LanguageServiceInf languageService;
   
   private VisibilityServiceInf visibilityService;

   private IPSSecurityWs securitySrv = null;

   private Taxonomy taxonomy;

   private List<Taxonomy> taxonomies;

   private Attribute attribute1;

   private Attribute attribute2;

   private Attribute attribute3;

   private List<Attribute> attributes;

   private Language language;

   @Before
   public void setUp() throws Exception
   {
      initServices();

      securitySrv.login("admin1", "demo", null, null);

      taxonomies = new ArrayList<Taxonomy>();
      attributes = new ArrayList<Attribute>();

      taxonomy = createTestTaxonomy();
      taxonomyService.saveTaxonomy(taxonomy);

      saveAttributes();

      taxonomy = taxonomyService.getTaxonomy(taxonomy.getId());
      taxonomies.add(taxonomy);
   }

   private void saveAttributes()
   {
      attribute1 = createTestAttribute(taxonomy, 1, "admin1");
      attribute2 = createTestAttribute(taxonomy, 2, "admin1");
      attribute3 = createTestAttribute(taxonomy, 3, "admin1");

      attributeService.saveAttribute(attribute1);
      attributeService.saveAttribute(attribute2);
      attributeService.saveAttribute(attribute3);

      language = languageService.getLanguage(1);

      Set<Attribute_lang> attrLang1 = createAttributeLangSet(1, attribute1, language);
      Set<Attribute_lang> attrLang2 = createAttributeLangSet(2, attribute2, language);
      Set<Attribute_lang> attrLang3 = createAttributeLangSet(3, attribute3, language);
      attributeLangSevice.saveAttribute_lang(attrLang1.iterator().next());
      attributeLangSevice.saveAttribute_lang(attrLang2.iterator().next());
      attributeLangSevice.saveAttribute_lang(attrLang3.iterator().next());

      attribute1 = (Attribute) attributeService.getAttribute(attribute1.getId()).iterator().next();
      attribute2 = (Attribute) attributeService.getAttribute(attribute2.getId()).iterator().next();
      attribute3 = (Attribute) attributeService.getAttribute(attribute3.getId()).iterator().next();

      attributes.add(attribute1);
      attributes.add(attribute2);
      attributes.add(attribute3);
   }

   /**
    * Initialize services if they are <code>null</code>.
    */
   private void initServices()
   {
      if (securitySrv == null)
      {
         securitySrv = PSSecurityWsLocator.getSecurityWebservice();
      }

      if (attributeService == null)
      {
         attributeService = (AttributeServiceInf) PSBaseServiceLocator.getBean("AttributeService");
      }

      if (nodeService == null)
      {
         nodeService = (NodeServiceInf) PSBaseServiceLocator.getBean("NodeService");
      }

      if (taxonomyService == null)
      {
         taxonomyService = (TaxonomyServiceInf) PSBaseServiceLocator.getBean("TaxonomyService");
      }

      if (valueService == null)
      {
         valueService = (ValueServiceInf) PSBaseServiceLocator.getBean("ValueService");
      }

      if (nodeStatusService == null)
      {
         nodeStatusService = (Node_statusServiceInf) PSBaseServiceLocator.getBean("Node_statusService");
      }

      if (attributeLangSevice == null)
      {
         attributeLangSevice = (Attribute_langServiceInf) PSBaseServiceLocator.getBean("Attribute_langService");
      }

      if (languageService == null)
      {
         languageService = (LanguageServiceInf) PSBaseServiceLocator.getBean("LanguageService");
      }
      
      if (visibilityService == null)
      {
         visibilityService = (VisibilityServiceInf) PSBaseServiceLocator.getBean("VisibilityService");
      }
   }

   @After
   public void tearDown() throws Exception
   {
      if (taxonomies != null)
      {
         for (Taxonomy tax : taxonomies)
         {
            taxonomyService.removeTaxonomy(taxonomyService.getTaxonomy(tax.getId()));
         }
      }
   }

   @Test
   @Ignore()//TODO: Fix me
   public void testDeleteAttribute_withoutNodes()
   {
      attributeService.removeAttribute(attribute2);

      Collection<Attribute> attr1 = attributeService.getAttribute(attribute1.getId());
      Collection<Attribute> attr2 = attributeService.getAttribute(attribute2.getId());
      Collection<Attribute> attr3 = attributeService.getAttribute(attribute3.getId());

      assertTrue(attr1.iterator().next().getId() == attribute1.getId());
      assertTrue(isEmpty(attr2));
      assertTrue(attr3.iterator().next().getId() == attribute3.getId());

      attributes.remove(1);
   }

   @Test
   @Ignore()//TODO: Fix me
   public void testDeleteAttribute_withNodes()
   {
      Collection valuesBeforeTest = valueService.getAllValues();

      Node nodeAttr2 = createNode(attribute2, "admin1");
      nodeAttr2.setNode_status(nodeStatusService.getNode_status(Node_status.ACTIVE));
      nodeService.saveNode(nodeAttr2);

      Set<Value> values = createValueSetForNode(attribute2, nodeAttr2, "admin1", language);
      for (Value value : values)
      {
         valueService.saveValue(value);
      }
      nodeAttr2.setValues(values);

      attributeService.removeAttribute(attribute2);

      Collection<Attribute> attr1 = attributeService.getAttribute(attribute1.getId());
      Collection<Attribute> attr2 = attributeService.getAttribute(attribute2.getId());
      Collection<Attribute> attr3 = attributeService.getAttribute(attribute3.getId());

      assertTrue(attr1.iterator().next().getId() == attribute1.getId());
      assertTrue(isEmpty(attr2));
      assertTrue(attr3.iterator().next().getId() == attribute3.getId());

      Collection<Node> nodes = nodeService.findNodesByAttribute(attribute2);
      assertTrue(nodes.isEmpty());

      Collection valuesAfterTest = valueService.getAllValues();
      assertTrue(valuesAfterTest.size() == valuesBeforeTest.size());

      attributes.remove(1);
   }

   @Test
   @Ignore() //TODO: Fix me
   public void testGetNodesInDeletionOrder_emptyList()
   {
      List<Node> nodes = new ArrayList<Node>();
      Taxonomy fakeTaxonomy = new Taxonomy();
      fakeTaxonomy.setNodes(nodes);

      List<Node> ordered = taxonomyService.getNodesInDeletionOrder(fakeTaxonomy);
      assertNotNull(ordered);
      assertTrue(isEmpty(ordered));
   }

   @Test
   @Ignore() //TODO: Fix me
   public void testGetNodesInDeletionOrder_oneRoot()
   {
      List<Node> nodes = buildTaxonomyTreeNodeOneRoot();
      Taxonomy fakeTaxonomy = new Taxonomy();
      fakeTaxonomy.setNodes(nodes);

      List<Node> ordered = taxonomyService.getNodesInDeletionOrder(fakeTaxonomy);
      assertNotNull(ordered);
      assertTrue(ordered.size() == nodes.size());

      // first level: nodes n12111, n12112
      assertTrue(ordered.get(0).getId() == 12111 || ordered.get(1).getId() == 12111);
      assertTrue(ordered.get(0).getId() == 12112 || ordered.get(1).getId() == 12112);

      // second level: node n1211
      assertTrue(ordered.get(2).getId() == 1211);

      // third level: nodes n121, 131
      assertTrue(ordered.get(3).getId() == 121 || ordered.get(4).getId() == 121);
      assertTrue(ordered.get(3).getId() == 131 || ordered.get(4).getId() == 131);

      // fourth level: nodes n11, n12, n13
      assertTrue(ordered.get(5).getId() == 11 || ordered.get(6).getId() == 11 || ordered.get(7).getId() == 11);
      assertTrue(ordered.get(5).getId() == 12 || ordered.get(6).getId() == 12 || ordered.get(7).getId() == 12);
      assertTrue(ordered.get(5).getId() == 13 || ordered.get(6).getId() == 13 || ordered.get(7).getId() == 13);

      // fifth level: node n1
      assertTrue(ordered.get(8).getId() == 1);
   }

   @Test
   @Ignore() //TODO: Fix me
   public void testGetNodesInDeletionOrder_twoRoots()
   {
      List<Node> nodes = buildTaxonomyTreeNodeTwoRoots();
      Taxonomy fakeTaxonomy = new Taxonomy();
      fakeTaxonomy.setNodes(nodes);

      List<Node> ordered = taxonomyService.getNodesInDeletionOrder(fakeTaxonomy);
      assertNotNull(ordered);
      assertTrue(ordered.size() == nodes.size());

      // first level: nodes n121, n122
      assertTrue(ordered.get(0).getId() == 121 || ordered.get(1).getId() == 121);
      assertTrue(ordered.get(0).getId() == 122 || ordered.get(1).getId() == 122);

      // second level: nodes n21, n11, n12
      assertTrue(ordered.get(2).getId() == 21 || ordered.get(3).getId() == 21 || ordered.get(4).getId() == 21);
      assertTrue(ordered.get(2).getId() == 11 || ordered.get(3).getId() == 11 || ordered.get(4).getId() == 11);
      assertTrue(ordered.get(2).getId() == 12 || ordered.get(3).getId() == 12 || ordered.get(4).getId() == 12);

      // third level: nodes n1, n2
      assertTrue(ordered.get(5).getId() == 1 || ordered.get(6).getId() == 1);
      assertTrue(ordered.get(5).getId() == 2 || ordered.get(6).getId() == 2);
   }

   @Test
   @Ignore() //TODO: Fix me
   public void testGetNodesInDeletionOrder_threeRoots()
   {
      List<Node> nodes = buildTaxonomyTreeNodeThreeRoots();
      Taxonomy fakeTaxonomy = new Taxonomy();
      fakeTaxonomy.setNodes(nodes);

      List<Node> ordered = taxonomyService.getNodesInDeletionOrder(fakeTaxonomy);
      assertNotNull(ordered);
      assertTrue(ordered.size() == nodes.size());

      // first level: nodes n11111, n11112
      assertTrue(ordered.get(0).getId() == 11111 || ordered.get(1).getId() == 11111);
      assertTrue(ordered.get(0).getId() == 11112 || ordered.get(1).getId() == 11112);

      // second level: node n1111, n3111, n3112, n3113
      assertTrue(ordered.get(2).getId() == 1111 || ordered.get(3).getId() == 1111 || ordered.get(4).getId() == 1111
            || ordered.get(5).getId() == 1111);
      assertTrue(ordered.get(2).getId() == 3111 || ordered.get(3).getId() == 3111 || ordered.get(4).getId() == 3111
            || ordered.get(5).getId() == 3111);
      assertTrue(ordered.get(2).getId() == 3112 || ordered.get(3).getId() == 3112 || ordered.get(4).getId() == 3112
            || ordered.get(5).getId() == 3112);
      assertTrue(ordered.get(2).getId() == 3113 || ordered.get(3).getId() == 3113 || ordered.get(4).getId() == 3113
            || ordered.get(5).getId() == 3113);

      // third level: nodes n111, n211, n212, 311
      assertTrue(ordered.get(6).getId() == 111 || ordered.get(7).getId() == 111 || ordered.get(8).getId() == 111
            || ordered.get(9).getId() == 111);
      assertTrue(ordered.get(6).getId() == 211 || ordered.get(7).getId() == 211 || ordered.get(8).getId() == 211
            || ordered.get(9).getId() == 211);
      assertTrue(ordered.get(6).getId() == 212 || ordered.get(7).getId() == 212 || ordered.get(8).getId() == 212
            || ordered.get(9).getId() == 212);
      assertTrue(ordered.get(6).getId() == 311 || ordered.get(7).getId() == 311 || ordered.get(8).getId() == 311
            || ordered.get(9).getId() == 311);

      // fourth level: nodes n11, n21, n31
      assertTrue(ordered.get(10).getId() == 11 || ordered.get(11).getId() == 11 || ordered.get(12).getId() == 11);
      assertTrue(ordered.get(10).getId() == 21 || ordered.get(11).getId() == 21 || ordered.get(12).getId() == 21);
      assertTrue(ordered.get(10).getId() == 31 || ordered.get(11).getId() == 31 || ordered.get(12).getId() == 31);

      // fifth level: nodes n1, n2, n3
      assertTrue(ordered.get(13).getId() == 1 || ordered.get(14).getId() == 1 || ordered.get(15).getId() == 1);
      assertTrue(ordered.get(13).getId() == 2 || ordered.get(14).getId() == 2 || ordered.get(15).getId() == 2);
      assertTrue(ordered.get(13).getId() == 3 || ordered.get(14).getId() == 3 || ordered.get(15).getId() == 3);
   }

   @Test
   @Ignore() //TODO: Fix me
   public void testRemoveTaxonomy()
   {
      addVisibilitiesToTaxonomy();
      addValuesToTanoxomyNodes();
      
      // get the taxonomy and assert that it exists
      Taxonomy existing = taxonomyService.getTaxonomy(taxonomy.getId());
      assertNotNull(existing);
      
      taxonomyService.removeTaxonomy(existing);
      
      assertNull(taxonomyService.getTaxonomy(taxonomy.getId()));
      
      for(Node node : existing.getNodes())
      {
         // assert that there is no values for the given node
         assertTrue(isEmpty(nodeService.getValuesForNode(node.getId(), language.getId())));
         
         // assert that the nodes are no longer saved
         assertTrue(isEmpty(nodeService.getSomeNodes(asList(node.getId()))));
      }
      
      for(Attribute attribute : existing.getAttributes())
      {
         // assert that the attributes no longer exist
         assertTrue(isEmpty(attributeService.getAttribute(attribute.getId())));
      }
      
      for(Visibility visibility : existing.getVisibilities())
      {
         assertNull(visibilityService.getVisibility(visibility.getId()));
      }
      
      taxonomy = null;
      taxonomies.remove(0);
   }

   @Test
   @Ignore() //TODO: Fix me
   public void testTaxonomyExists()
   {
      boolean doesExist = taxonomyService.doesTaxonomyExists("Tags");
      assertTrue(doesExist);
      doesExist = taxonomyService.doesTaxonomyExists("Categories");
      assertTrue(doesExist);
      doesExist = taxonomyService.doesTaxonomyExists("Unknown");
      assertTrue(!doesExist);
   }

   private void addValuesToTanoxomyNodes()
   {
      Node nodeAttr2 = createNode(attribute2, "admin1");
      nodeAttr2.setNode_status(nodeStatusService.getNode_status(Node_status.ACTIVE));
      nodeService.saveNode(nodeAttr2);

      Set<Value> values = createValueSetForNode(attribute2, nodeAttr2, "admin1", language);
      for (Value value : values)
      {
         valueService.saveValue(value);
      }

      Node nodeAttr1 = createNode(attribute2, "admin1");
      nodeAttr1.setNode_status(nodeStatusService.getNode_status(Node_status.ACTIVE));
      nodeService.saveNode(nodeAttr1);

      values = createValueSetForNode(attribute1, nodeAttr1, "admin1", language);
      for (Value value : values)
      {
         valueService.saveValue(value);
      }
   }

   private void addVisibilitiesToTaxonomy()
   {
      visibilityService.saveVisibility(getVisibilityForTaxonomy(taxonomy, 1));
   }
   
}

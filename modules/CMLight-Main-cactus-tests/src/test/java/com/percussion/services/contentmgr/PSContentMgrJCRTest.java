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
package com.percussion.services.contentmgr;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * This test loads a content item and checks that various JCR methods are
 * working correctly. This does not proport to be any sort of complete compliant
 * test - the main interest is checking node and property metadata.
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSContentMgrJCRTest extends ServletTestCase
{
   /**
    * Load one or more items and perform fairly simple tests involving the
    * content and presence of metadata
    * 
    * @throws RepositoryException
    */
   public void testMetaData() throws RepositoryException
   {
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();

      NodeDefinition def = cmgr.findNodeDefinitionByName("rffGeneric");
      doNodeDefinitionTest(def, "rx:rffGeneric", 
            "rx:body", PropertyType.STRING, false, 
            "rx:displaytitle", PropertyType.STRING, false);
      IPSGuid c340 = gmgr.makeGuid(new PSLocator(340, 3));
      List<Node> nodes = cmgr.findItemsByGUID(Collections.singletonList(c340),
            new PSContentMgrConfig());
      assertNotNull(nodes);
      assertEquals(1, nodes.size());
      Node node = nodes.get(0);
      assertNotNull(node);
      doNodeDefinitionTest(node.getDefinition(), "rx:rffGeneric", 
            "rx:callout", PropertyType.STRING, false,
            "rx:sys_contentcreateddate", PropertyType.DATE, false);
      
      // Check some node properties
      Property body = node.getProperty("rx:body");
      Property callout = node.getProperty("rx:callout");
      Property createdate = node.getProperty("rx:sys_contentcreateddate");
      
      assertEquals("rx:body", body.getDefinition().getName());
      assertEquals(PropertyType.STRING, body.getDefinition().getRequiredType());
      assertFalse(body.getDefinition().isMultiple());

      assertEquals("rx:callout", callout.getDefinition().getName());
      assertEquals(PropertyType.STRING, callout.getDefinition()
            .getRequiredType());
      assertFalse(callout.getDefinition().isMultiple());

      assertEquals("rx:sys_contentcreateddate", createdate.getDefinition()
            .getName());
      assertEquals(PropertyType.DATE, createdate.getDefinition()
            .getRequiredType());
      assertFalse(createdate.getDefinition().isMultiple());
   }

   /**
    * Test a node definition
    * 
    * @param def the node definition to test, assumed never <code>null</code>
    * @param name the name of the node definition to check, assumed never
    *           <code>null</code> or empty
    * @param args a set of argument tuples. Each tuple has the name of a
    *           property, the enumerated type of the property and a boolean
    *           describing if the property is a multiproperty (<code>true</code>)
    *           or not
    */
   private void doNodeDefinitionTest(NodeDefinition def, String name,
         Object... args)
   {
      assertEquals(name, def.getName());
      assertNotNull(def.getDeclaringNodeType());
      assertNotNull(def.getDefaultPrimaryType());
      assertNotNull(def.getRequiredPrimaryTypes());
      assertEquals(1, def.getRequiredPrimaryTypes().length);
      assertFalse(def.allowsSameNameSiblings());
      assertFalse(def.isAutoCreated());
      assertFalse(def.isMandatory());
      assertFalse(def.isProtected());

      NodeType type = def.getDefaultPrimaryType();
      doNodeTypeTest(type, name);

      Map<String, PropertyDefinition> defs = new HashMap<String, PropertyDefinition>();
      for (PropertyDefinition pd : type.getPropertyDefinitions())
      {
         defs.put(pd.getName(), pd);
      }

      for (int i = 0; i < args.length; i += 3)
      {
         String pname = (String) args[i];
         int ptype = (Integer) args[i + 1];
         boolean multiple = (Boolean) args[i + 2];

         PropertyDefinition pd = defs.get(pname);
         assertNotNull(pd);
         assertEquals(type, pd.getDeclaringNodeType());
         assertEquals(ptype, pd.getRequiredType());
         assertEquals(multiple, pd.isMultiple());
         assertFalse(pd.isProtected());
      }
   }

   /**
    * Test a node type
    * 
    * @param type the type to check, assumed never <code>null</code>
    * @param name the name of the type, assumed never <code>null</code> or
    *           empty
    */
   private void doNodeTypeTest(NodeType type, String name)
   {
      assertEquals(name, type.getName());
      assertNotNull(type.getPropertyDefinitions());
      assertNotNull(type.getDeclaredPropertyDefinitions());
      assertEquals("rx:body", type.getPrimaryItemName());
   }
}

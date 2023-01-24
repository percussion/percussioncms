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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.services.contentmgr.impl.PSContentInternalLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test runtime content manager behaviors
 *
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSContentMgrTest extends ServletTestCase
{
   /**
    * Test property definitions
    * @throws Exception
    */
   public void testPropertyDefinitions() throws Exception
   {
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      
      // Test that a definition contains unmapped system fields in the
      // property definition
      List<IPSNodeDefinition> defs = cmgr
         .loadNodeDefinitions(Collections.singletonList((IPSGuid)
               new PSGuid(PSTypeEnum.NODEDEF, 311)));
      NodeDefinition def = defs.get(0); 
      NodeType type = def.getDefaultPrimaryType();
      PropertyDefinition props[] = type.getPropertyDefinitions();
      Set<String> names = new HashSet<String>();
      for(PropertyDefinition prop : props)
      {
         assertNotNull(prop.getName());
         assertNotNull(prop.getDeclaringNodeType());
         assertEquals(type, prop.getDeclaringNodeType());
         names.add(prop.getName());
      }
      IPSContentRepository rep = PSContentInternalLocator.getLegacyRepository();
      for(String name : rep.getUnmappedSystemFields())
      {
         assertTrue(names.contains(name));   
      }
      
      // Test multi property. Add a region field to Event with some choice
      // values and a checkbox group control
      /*
      defs = cmgr.loadNodeDefinitions(Collections
            .singletonList((IPSGuid) new PSGuid(PSTypeEnum.NODEDEF, 306)));
      def = defs.get(0); 
      type = def.getDefaultPrimaryType();
      props = type.getPropertyDefinitions();
      PropertyDefinition region = null;
      for(PropertyDefinition prop : props)
      {
         if (prop.getName().contains("region"))
         {
            region = prop;
            break;
         }
      }
      assertNotNull(region);
      assertTrue(region.isMultiple());
      assertEquals(PropertyType.STRING, region.getRequiredType());
      */
   }
}

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

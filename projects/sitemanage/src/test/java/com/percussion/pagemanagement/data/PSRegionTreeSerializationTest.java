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
package com.percussion.pagemanagement.data;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.percussion.share.dao.PSSerializerUtils;

public class PSRegionTreeSerializationTest
{
    @Before
    public void setup()
    {
    }

    @Test
    public void testRegionTreeSerializeation() throws Exception
    {

        PSRegionTree tree = new PSRegionTree();
        
        PSRegion region = new PSRegion();
        PSRegion child = new PSRegion();
        
        PSRegionCode code = new PSRegionCode();
        code.setTemplateCode("#blah()");
        
        child.setRegionId("child");
        
        region.getChildren().add(code);
        region.getChildren().add(child);
        PSRegionCode c2 = new PSRegionCode();
        c2.setTemplateCode("crap");
        region.getChildren().add(c2);
        
        region.setRegionId("Adam");
        PSWidgetItem wi = new PSWidgetItem();
        wi.setName("Blah");
        wi.setId("1");
        
        PSWidgetItem wi2 = new PSWidgetItem();
        
        wi2.setName("Foo");
        wi2.setDescription("Foo description.");
        wi2.setId("2");
        
        PSWidgetItem wi3 = new PSWidgetItem();
        wi3.setId("3");
        
        PSRegionWidgets wr = new PSRegionWidgets();
        wr.setRegionId("Adam");
        wr.setWidgetItems(asList(wi,wi2,wi3));
        Set<PSRegionWidgets> sets = new HashSet<PSRegionWidgets>();
        sets.add(wr);
        tree.setRegionWidgetAssociations(sets);
        tree.setRootRegion(region);
        String s = PSSerializerUtils.marshal(tree);
        assertNotNull(s);
        log.debug(s);
        
        PSRegionTree unmarshal = PSSerializerUtils.unmarshal(s, PSRegionTree.class);
        assertNotNull(unmarshal);
        assertNotNull(unmarshal.getRegionWidgetAssociations());
        assertFalse(unmarshal.getRegionWidgetAssociations().isEmpty());
    }
    

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSRegionTreeSerializationTest.class);

}

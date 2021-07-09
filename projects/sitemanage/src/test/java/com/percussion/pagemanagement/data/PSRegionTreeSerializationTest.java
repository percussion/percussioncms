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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

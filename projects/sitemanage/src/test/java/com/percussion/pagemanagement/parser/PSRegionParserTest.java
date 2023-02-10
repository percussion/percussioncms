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
package com.percussion.pagemanagement.parser;

import static com.percussion.pagemanagement.data.PSRegionTreeUtils.getChildRegions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.data.PSRegionNode;
import com.percussion.pagemanagement.data.PSRegionTreeWriter;
import com.percussion.pagemanagement.parser.IPSRegionParser.IPSRegionParserRegionFactory;
import com.percussion.share.test.PSTestUtils;

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class PSRegionParserTest
{

    PSTemplateRegionFactory factory;

    PSRegionParser<PSRegion, PSRegionCode> parser;
    
    PSRegionTreeWriter treeWriter;
    StringWriter sw;

    public static class PSTemplateRegionFactory implements IPSRegionParserRegionFactory<PSRegion, PSRegionCode>
    {

        public PSRegion createRegion(String regionId)
        {
            PSRegion r = new PSRegion();
            r.setRegionId(regionId);
            return r;
        }

        public PSRegion createRootRegion()
        {
            return new PSRegion();
        }

        public PSRegionCode createRegionCode()
        {
            return new PSRegionCode();
        }

    }

    @Before
    public void setUp()
    {
        factory = new PSTemplateRegionFactory();
        parser = new PSRegionParser<PSRegion, PSRegionCode>(factory);
        sw = new StringWriter();
        treeWriter = new PSRegionTreeWriter(sw);
    }

    @Test
    public void testGetRegionTree()
    {
        String html = getHtml("Default.html");
        PSParsedRegionTree<PSRegion,PSRegionCode> regTree = parser.parse(html);
        Map<String, PSRegion> regions = regTree.getRegions();
        assertEquals(regions.size(), 6);
        assertTrue(regionExists("1", regions.values()));
        assertTrue(regionExists("2", regions.values()));
        assertTrue(regionExists("3", regions.values()));
        for (String id : regions.keySet())
        {
            PSRegion region = regions.get(id);
            List<PSAbstractRegion> children = getChildRegions(region);
            if (id.equals("1"))
            {
                assertEquals(children.size(), 3);
                assertTrue(regionExists("1.1", children));
                assertTrue(regionExists("1.3", children));
                assertTrue(regionExists("1.4.1", children));
            }
            else if (id.equals("2"))
            {
                assertTrue(children.isEmpty());
            }
            else if (id.equals("3"))
            {
                assertTrue(children.isEmpty());
            }
        }
        
        //log.debug(regTree.getRegions());
    }
    
    @Test
    public void testHeaderFooterParse() throws Exception {
        String html = getHtml("TestHeaderFooter.html");
        PSParsedRegionTree<PSRegion,PSRegionCode> regTree = parser.parse(html);
        List<? extends PSRegionNode> children =  regTree.getRootNode().getChildren();
        PSRegionCode code = getCode(children.get(0));
        assertNotNull(code);
        assertEquals("#perc_header()", code.getTemplateCode().trim());
        PSRegionCode end = getCode(children.get(children.size() - 1));
        assertEquals("#perc_footer()", end.getTemplateCode().trim());
    }
    
    @Test
    public void testWrite() throws Exception {
        log.debug("Write tree");
        String html = getHtml("TestHeaderFooter.html");
        PSParsedRegionTree<PSRegion,PSRegionCode> regTree = parser.parse(html);
        treeWriter.write(regTree.getRootNode());
        String actual = sw.getBuffer().toString();
        assertEquals(html, actual);
    }
    
    @Test
    public void testRegion() throws Exception {
        String html = getHtml("Region.html");
        PSParsedRegionTree<PSRegion,PSRegionCode> regTree = parser.parse(html);
        String actual = getChildRegions(regTree.getRootNode()).get(0).getRegionId();
        assertEquals("container", actual);
        
        actual = getChildRegions(getChildRegions(regTree.getRootNode()).get(0)).get(0).getRegionId();
        assertEquals("header", actual);
    }
    
    private PSRegionCode getCode(PSRegionNode node) {
        return (PSRegionCode) node;
    }

    private boolean regionExists(String id, Collection<? extends PSRegionNode> nodes)
    {
        for (PSRegionNode node : nodes)
        {
            if (node instanceof PSRegion && id.equals(((PSRegion)node).getRegionId()))
            {
                return true;
            }
        }

        return false;
    }
    
    private String getHtml(String name) {
        return PSTestUtils.resourceToString(getClass(), name);
    }


    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSRegionParserTest.class);

}

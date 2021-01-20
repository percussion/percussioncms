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
package com.percussion.pagemanagement.data.regiontree;

import static com.percussion.pagemanagement.data.PSRegionTreeUtils.getEmptyWidgetRegions;
import static com.percussion.pagemanagement.data.PSRegionTreeUtils.getWidgetRegions;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.test.PSTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Santiago M. Murchio
 *
 */
public class PSRegionTreeUtilsTest
{
    
    private static final String TEMPLATE_CASE_1_PLAIN = "template_case1_plain.xml";
    private static final String TEMPLATE_CASE_2_BOX = "template_case2_box.xml";
    private static final String TEMPLATE_CASE_3_CUSTOM_BOX = "template_case2_custom_box.xml";
    private static final String TEMPLATE_EMPTY_REGION_CASE_1_BOX = "template_emptyRegion_case1_box.xml";
    private static final String TEMPLATE_EMPTY_REGION_CASE_2_CUSTOM_BOX = "template_emptyRegion_case2_custom_box.xml";
    
    private static final String REGION_CONTAINER = "container";
    private static final String REGION_HEADER = "header";
    private static final String REGION_LEFT_SIDE_BAR = "leftsidebar";
    private static final String REGION_CONTENT = "content";
    private static final String REGION_RIGHT_SIDE_BAR = "rightsidebar";
    private static final String REGION_FOOTER = "footer";
    private static final String REGION_TEMP_1 = "temp-region-1";
    private static final String REGION_TEMP_2 = "temp-region-2";
    private static final String REGION_TEMP_3 = "temp-region-3";
    private static final String REGION_TEMP_4 = "temp-region-4";
    private static final String REGION_TEMP_5 = "temp-region-5";
    private static final String REGION_TEMP_6 = "temp-region-6";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public void testGetWidgetRegions_case1plainTemplateOnlyContainer()
    {
        List<String> expected = new ArrayList<String>();
        expected.add(REGION_CONTAINER);

        testRegionSet(expected, getWidgetRegions(getTestRegionTree(TEMPLATE_CASE_1_PLAIN)));
    }

    @Test
    public void testGetWidgetRegions_case2Box()
    {
        List<String> expected = new ArrayList<String>();
        expected.add(REGION_HEADER);
        expected.add(REGION_CONTENT);
        expected.add(REGION_FOOTER);
        expected.add(REGION_LEFT_SIDE_BAR);
        expected.add(REGION_RIGHT_SIDE_BAR);

        testRegionSet(expected, getWidgetRegions(getTestRegionTree(TEMPLATE_CASE_2_BOX)));
    }

    @Test
    public void testGetWidgetRegions_case3CustomBox()
    {
        List<String> expected = new ArrayList<String>();
        expected.add(REGION_LEFT_SIDE_BAR);
        expected.add(REGION_RIGHT_SIDE_BAR);
        expected.add(REGION_TEMP_1);
        expected.add(REGION_TEMP_2);
        expected.add(REGION_TEMP_3);
        expected.add(REGION_TEMP_4);
        expected.add(REGION_TEMP_5);
        expected.add(REGION_TEMP_6);

        testRegionSet(expected, getWidgetRegions(getTestRegionTree(TEMPLATE_CASE_3_CUSTOM_BOX)));
    }

    @Test
    public void testGetEmptyRegions_case1Box() 
    {
        List<String> expected = new ArrayList<String>();
        expected.add(REGION_LEFT_SIDE_BAR);
        expected.add(REGION_RIGHT_SIDE_BAR);
        expected.add(REGION_FOOTER);
        expected.add(REGION_HEADER);

        testRegionSet(expected, getEmptyWidgetRegions(getTestRegionTree(TEMPLATE_EMPTY_REGION_CASE_1_BOX)));
    }

    @Test
    public void testGetEmptyRegions_case2CustomBox() 
    {
        List<String> expected = new ArrayList<String>();
        expected.add(REGION_LEFT_SIDE_BAR);
        expected.add(REGION_TEMP_2);
        expected.add(REGION_TEMP_4);
        expected.add(REGION_TEMP_5);

        testRegionSet(expected, getEmptyWidgetRegions(getTestRegionTree(TEMPLATE_EMPTY_REGION_CASE_2_CUSTOM_BOX)));
    }
    
    /**
     * Tests that the list of regions has all the regions ids present in the
     * expected list.
     * 
     * @param expected {@link List}<{@link String}> holding the expected region
     *            ids.
     * @param actual {@link List}<{@link PSRegion}> holding the regions whose
     *            ids we want to check.
     */
    private void testRegionSet(List<String> expected, Set<PSRegion> actual)
    {
        assertNotNull(actual);
        assertTrue(
                "The size of the actual set is wrong. [Expected = " + expected.size() + ", Actual = " + actual.size()
                        + "]", actual.size() == expected.size());
        for (PSRegion region : actual)
        {
            assertTrue("Region " + region.getRegionId() + " should be in the list of expected regions. [" + expected
                    + "]", expected.contains(region.getRegionId()));
        }
    }

    private PSRegionTree getTestRegionTree(String templateName)
    {
        String xmlContent = PSTestUtils.resourceToString(getClass(), templateName);
        return PSSerializerUtils.unmarshal(xmlContent, PSRegionTree.class);
    }
   
}

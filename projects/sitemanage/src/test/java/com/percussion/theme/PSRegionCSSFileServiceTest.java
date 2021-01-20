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

package com.percussion.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSRegionTreeTest;
import com.percussion.theme.data.PSRegionCSS;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.theme.data.PSRegionCSS.Property;
import com.percussion.theme.service.impl.PSRegionCSSFileService;
import com.percussion.theme.service.impl.PSThemeService;
import com.percussion.util.PSPurgableTempFile;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PSRegionCSSFileServiceTest
{
    private PSThemeService themeService;

    private PSRegionCSSFileService cssService;

    private List<PSPurgableTempFile> tempFiles;
    
    final static int SAMPLE_SIZE = 3;
    
    @Before
    public void setup()
    {
        themeService = new PSThemeService();
        themeService.setThemesRootDirectory("src/test/resources/themes");
        themeService.setThemesRootRelativeUrl("/Rhythmyx/web_resources/themes");
        themeService.setThemesTempRootDirectory("src/test/resources/themes.tmp");
        themeService.setThemesTempRootRelativeUrl("/Rhythmyx/sys_resources/temp/themes");

        cssService = new PSRegionCSSFileService();
        
        tempFiles = new ArrayList<PSPurgableTempFile>();
    }

    @After
    public void tearDown()
    {
        for (PSPurgableTempFile f : tempFiles)
        {
            f.delete();
        }
    }
    
    @Test
    public void testRead() throws Exception
    {
        List<PSRegionCSS> regions = readFromSampleFile();
        assertTrue(regions.size() == SAMPLE_SIZE);
        assertEquals("Region CSS file must equals sample data", regions, getSampleRegions());

        List<PSRegionCSS> writeList = writeToTempFileThenRead(regions);
        assertEquals("Region list, write then read must be equals", regions, writeList);
    }

    @Test
    public void testFind() throws Exception
    {
        PSRegionCSS regionCSS = cssService.findRegionCSS("container", "header", getSampleFilePath());
        assertNotNull("Sample file must contain \"container\" & \"header\" region CSS", regionCSS);

        regionCSS = cssService.findRegionCSS("container", "container", getSampleFilePath());
        assertNotNull("Sample file must contain \"container\" & \"container\" region CSS", regionCSS);

        regionCSS = cssService.findRegionCSS("container", "unkown", getSampleFilePath());
        assertTrue("Sample file must contain \"container\" & \"unkown\" region CSS", regionCSS == null);
    }

    @Test
    public void testSave() throws Exception
    {
        PSRegionCSS container = createTestRegionCSS("container", "container");

        PSPurgableTempFile tempFile = copySampleToTempFile();
        PSRegionCSS regionCSS = cssService.findRegionCSS(container.getOuterRegionName(), container.getRegionName(), tempFile.getAbsolutePath());
        assertNotNull("Sample file must contain \"container\" & \"container\" region CSS", regionCSS);
        
        // test save as update
        List<PSRegionCSS> regions = cssService.read(tempFile.getAbsolutePath());
        assertTrue(regions.size() == SAMPLE_SIZE);
        cssService.save(container, tempFile.getAbsolutePath());
        regions = cssService.read(tempFile.getAbsolutePath());
        assertTrue("The save is an update operation", regions.size() == SAMPLE_SIZE);
        
        // test save as add
        container.setRegionName("middle");
        regionCSS = cssService.findRegionCSS(container.getOuterRegionName(), container.getRegionName(), tempFile.getAbsolutePath());
        assertNull("Region CSS does not contain \"container\" & \"middle\" region CSS", regionCSS);
        
        cssService.save(container, tempFile.getAbsolutePath());
        regions = cssService.read(tempFile.getAbsolutePath());
        assertTrue("The save is an add operation", regions.size() == SAMPLE_SIZE + 1);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSort() throws Exception
    {
        PSPurgableTempFile tempFile = copySampleToTempFile();
        PSRegionCSS middle = createTestRegionCSS("container", "middle");
        cssService.save(middle, tempFile.getAbsolutePath());
        List<PSRegionCSS> regions = cssService.read(tempFile.getAbsolutePath());
        assertTrue("Just added a region", regions.size() == SAMPLE_SIZE + 1);
        
        List<PSRegionCSS> sortRegions = readFromSampleFile();
        sortRegions.add(middle);
        Collections.sort(sortRegions);
        
        assertEquals("The regions should be sorted", sortRegions, regions);
    }

    @Test
    public void testDelete() throws Exception
    {
        final String OUTER = "container";
        final String MIDDLE = "middle";
        PSPurgableTempFile tempFile = copySampleToTempFile();
        PSRegionCSS regionCSS = cssService.findRegionCSS(OUTER, MIDDLE, tempFile.getAbsolutePath());
        assertNull("Region CSS does not contain \"container\" & \"middle\" region CSS", regionCSS);

        // delete nothing if not exist
        List<PSRegionCSS> regions = cssService.read(tempFile.getAbsolutePath());
        assertTrue(regions.size() == SAMPLE_SIZE);
        cssService.delete(OUTER, MIDDLE, tempFile.getAbsolutePath());
        regions = cssService.read(tempFile.getAbsolutePath());
        assertTrue("There was nothting to delete", regions.size() == SAMPLE_SIZE);
        
        final String HEADER = "header";
        
        // delete an existing region
        regionCSS = cssService.findRegionCSS(OUTER, HEADER, tempFile.getAbsolutePath());
        assertNotNull("Sample file must contain \"container\" & \"header\" region CSS", regionCSS);
        
        cssService.delete(OUTER, HEADER, tempFile.getAbsolutePath());
        regionCSS = cssService.findRegionCSS(OUTER, HEADER, tempFile.getAbsolutePath());
        assertNull("Deleted \"container\" & \"header\" region CSS", regionCSS);
    }
    
    @Test
    public void testCopy() throws Exception
    {
        PSPurgableTempFile tempFile = copySampleToTempFile();
        PSPurgableTempFile tempCss = createTempCssFile();
        
        cssService.copyFile(tempFile.getAbsolutePath(), tempCss.getAbsolutePath());

        // validate the copy result        
        String srcText = getStringFileFile(tempFile.getAbsolutePath());
        String targetText = getStringFileFile(tempCss.getAbsolutePath());

        assertEquals(srcText, targetText);
    }
    
    private String getStringFileFile(String path) throws IOException
    {
        Reader reader = null; 
        try
        {
            reader = new FileReader(path);
            return IOUtils.toString(reader);
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
    }
    
    @Test
    public void testCreateEmptyRegionCSS() throws Exception
    {
        PSPurgableTempFile tempCss = createTempCssFile();
        tempCss.delete();
        
        assertFalse(tempCss.exists());

        cssService.copyFile(null, tempCss.getAbsolutePath());
        
        String content = getStringFileFile(tempCss.getAbsolutePath());
        assertTrue(content.equals(".percDummyRule{/* Dummy rule for correct HTML's LINK tag rendering during editing a template */}"));
        assertTrue(tempCss.exists());
    }
    
    private PSRegionCSS createTestRegionCSS(String outer, String region)
    {
        PSRegionCSS regionCSS = new PSRegionCSS(outer, region);
        Property p = new Property("height", "11px");
        List<Property> props = new ArrayList<Property>();
        props.add(p);
        regionCSS.setProperties(props);
        
        return regionCSS;
    }
    
    private List<PSRegionCSS> readFromSampleFile()
    {
        List<PSRegionCSS> regions = cssService.read(getSampleFilePath());
        return regions;
    }

    private String getSampleFilePath()
    {
        PSThemeSummary summary = themeService.find("test");
        assertNotNull("Region CSS file", summary.getRegionCssFilePath());
        
        return getRegionCssFile(summary);
    }

    private List<PSRegionCSS> writeToTempFileThenRead(List<PSRegionCSS> regions) throws IOException
    {
        PSPurgableTempFile tempCss = new PSPurgableTempFile("temp", "css", null);
        try
        {
            cssService.write(tempCss.getAbsolutePath(), regions);
            return cssService.read(tempCss.getAbsolutePath());
        }
        finally
        {
            tempCss.delete();
        }
    }

    private PSPurgableTempFile copySampleToTempFile() throws IOException
    {
        PSPurgableTempFile tempCss = createTempCssFile();
        List<PSRegionCSS> regions = readFromSampleFile();
        cssService.write(tempCss.getAbsolutePath(), regions);
        return tempCss;
    }

    private String getRegionCssFile(PSThemeSummary summary)
    {
        return themeService.getThemesRootDirectory() + "/" + summary.getRegionCssFilePath();
    }

    private PSPurgableTempFile createTempCssFile() throws IOException
    {
        PSPurgableTempFile tempCss = new PSPurgableTempFile("temp", "css", null);
        tempFiles.add(tempCss);
        return tempCss;
    }
    
    private PSRegionTree getRegionTree() throws Exception
    {
        PSRegionTree tree = PSRegionTreeTest.loadRegionTree();

        List<String> names = Arrays.asList(nameChildren);
        List<String> regionNames = PSRegionTreeTest.getRegionIds(tree.getDescendentRegions());
        assertEquals(names, regionNames);
        
        return tree;
    }

    private String[] nameChildren = new String[]{"container", "header", "middle", "leftsidebar", "content", "rightsidebar", "footer"};
    
    
    @Test
    public void testMergeRegionCSSFile() throws Exception 
    {
        // simply copy resource to target as the merged result
        PSRegionTree tree = getRegionTree();
        validateSourceMergeToTarget(tree, getSampleRegions_2(), getSampleRegions(), getSampleRegions_2());
        validateSourceMergeToTarget(tree, getSampleRegions_2(), getSampleRegions_3(), getSampleRegions_2());

        // empty tree, no change to the target
        tree = new PSRegionTree();
        validateSourceMergeToTarget(tree, getSampleRegions_2(), getSampleRegions_3(), getSampleRegions_3());
        
        // empty source, no change to the target
        tree = getRegionTree();
        validateSourceMergeToTarget(tree, new ArrayList<PSRegionCSS>(), getSampleRegions_3(), getSampleRegions_3());
        
        // merged result is mixing the source into target
        validateSourceMergeToTarget(tree, getSampleRegions_4(), getSampleRegions_2(), getSampleRegions_4_merge_2());
        
    }

    private void validateSourceMergeToTarget(PSRegionTree tree, List<PSRegionCSS> src, 
            List<PSRegionCSS> target, List<PSRegionCSS> finalRegions) throws Exception
    {
        PSPurgableTempFile tempCss = createTempCssFile();
        cssService.write(tempCss.getAbsolutePath(), target);
        
        PSPurgableTempFile tempCss_2 = createTempCssFile();
        cssService.write(tempCss_2.getAbsolutePath(), src);

        cssService.mergeFile(tree, tempCss_2.getAbsolutePath(), tempCss.getAbsolutePath());
        
        List<PSRegionCSS> mergedRegions = cssService.read(tempCss.getAbsolutePath());
        assertEquals("The merge should have copied source to target", mergedRegions, finalRegions);
    }
    
    private List<PSRegionCSS> getSampleRegions()
    {
        List<PSRegionCSS> regions = new ArrayList<PSRegionCSS>();

        PSRegionCSS r = new PSRegionCSS("container", "container");
        addProperty(r, "font-family", "Verdana");
        addProperty(r, "font-size", "11px");
        addProperty(r, "font-weight", "normal");
        regions.add(r);

        r = new PSRegionCSS("container", "header");
        addProperty(r, "font-family", "Times,\"Times New Roman\",Georgia,serif");
        addProperty(r, "font-size", "22px");
        regions.add(r);

        r = new PSRegionCSS("container", "left");
        addProperty(r, "height", "100px");
        regions.add(r);

        return regions;
    }

    private List<PSRegionCSS> getSampleRegions_2()
    {
        List<PSRegionCSS> regions = new ArrayList<PSRegionCSS>();

        PSRegionCSS r = new PSRegionCSS("container", "container");
        addProperty(r, "font-family", "Verdana");
        addProperty(r, "font-weight", "normal");
        regions.add(r);

        r = new PSRegionCSS("container", "header");
        addProperty(r, "font-family", "Times,\"Times New Roman\",Georgia,serif");
        regions.add(r);

        r = new PSRegionCSS("container", "left");
        addProperty(r, "height", "100px");
        regions.add(r);

        return regions;
    }

    private List<PSRegionCSS> getSampleRegions_3()
    {
        List<PSRegionCSS> regions = new ArrayList<PSRegionCSS>();

        PSRegionCSS r = new PSRegionCSS("container", "header");
        addProperty(r, "font-family", "Times,\"Times New Roman\",Georgia,serif");
        addProperty(r, "font-size", "22px");
        regions.add(r);

        r = new PSRegionCSS("container", "left");
        addProperty(r, "height", "100px");
        regions.add(r);

        return regions;
    }
    
    private List<PSRegionCSS> getSampleRegions_4()
    {
        List<PSRegionCSS> regions = new ArrayList<PSRegionCSS>();

        PSRegionCSS r = new PSRegionCSS("container", "container");
        addProperty(r, "font-weight", "normal");
        regions.add(r);

        r = new PSRegionCSS("container", "header");
        addProperty(r, "font-family", "Times,\"Times New Roman\",Georgia,serif");
        addProperty(r, "font-size", "22px");
        regions.add(r);

        r = new PSRegionCSS("container", "left");
        addProperty(r, "font-size", "22px");
        regions.add(r);

        return regions;
    }    

    /**
     * Gets the result of merging {@link #getSampleRegions_4()} into {@link #getSampleRegions_2()}.
     * @return the merged result.
     */
    private List<PSRegionCSS> getSampleRegions_4_merge_2()
    {
        List<PSRegionCSS> regions = new ArrayList<PSRegionCSS>();

        // from getSampleRegions_4()
        PSRegionCSS r = new PSRegionCSS("container", "container");
        addProperty(r, "font-weight", "normal");
        regions.add(r);

        // from getSampleRegions_4()
        r = new PSRegionCSS("container", "header");
        addProperty(r, "font-family", "Times,\"Times New Roman\",Georgia,serif");
        addProperty(r, "font-size", "22px");
        regions.add(r);

        // from getSampleRegions_2
        r = new PSRegionCSS("container", "left");
        addProperty(r, "height", "100px");
        regions.add(r);

        return regions;
    }    
    // private String[] nameChildren = new String[]{"container", "header", "middle", "leftsidebar", "content", "rightsidebar", "footer"};
    
    private void addProperty(PSRegionCSS r, String pname, String pvalue)
    {
        PSRegionCSS.Property p = new PSRegionCSS.Property(pname, pvalue);
        r.getProperties().add(p);
    }
}

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
package com.percussion.pagemanagement.assembler;

import static com.percussion.share.test.PSMatchers.validUrl;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.pagemanagement.data.PSResourceLinkAndLocation;
import com.percussion.pagemanagement.service.impl.PSLinkableAsset;
import com.percussion.share.data.IPSLinkableContentItem;
import com.percussion.share.data.IPSItemSummary.Category;
import com.percussion.sitemanage.data.PSSiteSummary;

import org.hamcrest.core.CombinableMatcher;
import org.junit.Before;
import org.junit.Test;

public class PSResourceLinkAndLocationUtilsTest
{
    
    private PSResourceInstance resource;
    private PSSiteSummary contextSite = new PSSiteSummary();
    private PSSiteSummary itemSite = new PSSiteSummary();
    private String locationFolderPath = "/space blah";
    private String ANALYTICS_ID = "analyticsId";
    
    @Before
    public void setup()
    {
        contextSite.setBaseUrl("http://Context.com/");
        itemSite.setBaseUrl("http://Item.com/MySite");
        itemSite.setId("1");
        contextSite.setId("2");
        
        resource = new PSResourceInstance();
        resource.setSite(itemSite);
        resource.setLinkContext(new PSRenderLinkContext() {

            @Override
            public Mode getMode()
            {
                return Mode.PUBLISH;
            }

            @Override
            public PSSiteSummary getSite()
            {
                return contextSite;
            }
        });
        resource.setLocationFolderPath(locationFolderPath);
        assertThat(resource.isCrossSite(), is(true));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testValidateSafePhysicalPathFailure() throws Exception
    {
        PSResourceLinkAndLocationUtils.validateAsPhysicalPath("/asdf?sadf/");
    }
    
    @Test
    public void testValidateSafePhysicalPath() throws Exception
    {
        PSResourceLinkAndLocationUtils.validateAsPhysicalPath("/asdf");
    }
    
    
    @Test
    public void testConcatPath() throws Exception
    {
        String path = "/peter paul";
        String actual = PSResourceLinkAndLocationUtils.concatPath(path, "");
        assertEquals("path",path,actual);
        
        actual = PSResourceLinkAndLocationUtils.concatPath("/peter paul/", "");
        assertEquals("path","/peter paul",actual);
        
        actual = PSResourceLinkAndLocationUtils.concatPath("/peter paul/", "/blah/", "", "");
        assertEquals("path","/peter paul/blah",actual);
        
    }
    @Test
    public void testEscapeUrlPath() throws Exception
    {
        String path = "/peter paul/mary joseph/j.html";
        String escapedPath = PSResourceLinkAndLocationUtils.escapePathForUrl(path);
        assertEquals("/peter%20paul/mary%20joseph/j.html", escapedPath);
    }
    
    @Test
    public void testEscapeBackSlashShouldNotFail() throws Exception
    {
        String path = "/stuff \\ crap";
        String escapedPath = PSResourceLinkAndLocationUtils.escapePathForUrl(path);
        assertEquals("/stuff%20%5C%20crap", escapedPath);
    }
    
    
    @Test
    public void testEscapeColonShouldNotFail() throws Exception
    {
        String path = "/stuff : crap";
        String escapedPath = PSResourceLinkAndLocationUtils.escapePathForUrl(path);
        assertEquals("/stuff%20:%20crap", escapedPath);
    }
    
    @Test
    public void testEscapeQuestionMarkShouldNotFail() throws Exception
    {
        String path = "/stuff ? crap";
        String escapedPath = PSResourceLinkAndLocationUtils.escapePathForUrl(path);
        assertEquals("/stuff%20%3F%20crap", escapedPath);
    }
    
    
    @Test
    public void testCreateDefaultLinkAndLocationForCrossSite() throws Exception
    {
        PSResourceLinkAndLocation link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(resource, "crap.txt");
        assertThat(link.getResourceLocation().getFilePath(), is("/space blah/crap.txt"));
        assertThat(link.getRenderLink().getUrl(), 
                is(CombinableMatcher.<String>both(validUrl()).and(equalTo("http://Item.com/MySite/space%20blah/crap.txt"))));
    }
    
    @Test
    public void testCreateDefaultLinkAndLocation() throws Exception
    {
     
        //Not a cross site link.
        contextSite = itemSite;
        PSResourceLinkAndLocation link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(resource, "crap.txt");
        assertThat(link.getResourceLocation().getFilePath(), is("/space blah/crap.txt"));
        assertThat(link.getRenderLink().getUrl(), is("/MySite/space%20blah/crap.txt"));
    }

    @Test
    public void testAppendAnalyticsId() throws Exception
    {
    	contextSite = itemSite;

    	//create our resource
    	PSResourceInstance newResourceInstance = resource;
    	PSAsset asset = new PSAsset();
    	Map<String, Object> fields = new HashMap<String, Object>();
    	fields.put(ANALYTICS_ID, "?my=analytics id");
    	asset.setFields(fields);
    	newResourceInstance.setItem(new PSLinkableAsset(asset, "crap.txt"));

    	PSResourceLinkAndLocation link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(newResourceInstance, "crap.txt");
    	assertThat(link.getRenderLink().getUrl(), is("/MySite/space%20blah/crap.txt?my=analytics+id"));

    	fields.put(ANALYTICS_ID, "?mynewid");
    	link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(newResourceInstance, "crap.txt");

    	assertThat(link.getRenderLink().getUrl(), is("/MySite/space%20blah/crap.txt?mynewid"));
    	
    	fields.put(ANALYTICS_ID, "?utm_source=help&utm_medium=web&utm_campaign=cm153newfeature&utm_term=clicktrack");
    	link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(newResourceInstance, "crap.txt");
    	
    	assertThat(link.getRenderLink().getUrl(), is("/MySite/space%20blah/crap.txt?utm_source=help&utm_medium=web&utm_campaign=cm153newfeature&utm_term=clicktrack"));
    	
    	// test to check if the analytics has no ? at the beginning of the string
    	fields.put(ANALYTICS_ID, "utm_source=help&utm_medium=web&utm_campaign=cm153newfeature&utm_term=clicktrack");
    	link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(newResourceInstance, "crap.txt");
    	
    	assertThat(link.getRenderLink().getUrl(), is("/MySite/space%20blah/crap.txt?utm_source=help&utm_medium=web&utm_campaign=cm153newfeature&utm_term=clicktrack"));
    }

    @Test
    public void testBadAppendAnalyticsId() throws Exception
    {
    	contextSite = itemSite;

    	//create our resource
    	PSResourceInstance newResourceInstance = resource;
    	PSAsset asset = new PSAsset();
    	Map<String, Object> fields = new HashMap<String, Object>();
    	String myanalyticsId = "?";
    	fields.put(ANALYTICS_ID, myanalyticsId);
    	asset.setFields(fields);
    	newResourceInstance.setItem(new PSLinkableAsset(asset, "crap.txt"));

    	PSResourceLinkAndLocation link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(newResourceInstance, "crap.txt");
    	assertThat(link.getRenderLink().getUrl(), is("/MySite/space%20blah/crap.txt"));

    	newResourceInstance.setItem(null);

    	link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(newResourceInstance, "crap.txt");
    	assertThat(link.getRenderLink().getUrl(), is("/MySite/space%20blah/crap.txt"));
    	
    	// check for null analytics id to ensure publishing doesn't break
    	fields.put(ANALYTICS_ID, null);
    	link = PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(newResourceInstance, "crap.txt");
    	
    	assertThat(link.getRenderLink().getUrl(), is("/MySite/space%20blah/crap.txt"));
    }
    
}


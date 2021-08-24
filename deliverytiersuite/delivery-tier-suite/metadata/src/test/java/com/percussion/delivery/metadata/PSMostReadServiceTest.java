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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.metadata;

import com.percussion.delivery.metadata.data.PSVisitQuery;
import com.percussion.delivery.metadata.rdbms.impl.PSDbBlogPostVisit;
import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataEntry;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional(isolation = Isolation.READ_UNCOMMITTED,propagation = Propagation.NESTED)
@ContextConfiguration(locations =
{"classpath:test-beans.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSMostReadServiceTest extends TestCase {

	private static final Logger log = LogManager.getLogger(PSMostReadServiceTest.class);
    
    @Autowired
    private IPSBlogPostVisitService blogPostService;
    
    @Autowired
    public IPSMetadataIndexerService indexer;
    
    /**
     * Used to set the number of items to create
     */
    private static final int ENTRY_COUNT = 5;
    
    /**
     * Used to set the max entries when testing
     * large numbers of hits
     * 1005 - entry_count(5) = 1000 posts
     */
    private static final int MAX_COUNT = 1005;
    
    /**
     * hack name for site
     */
    private static final String SITE_NAME = "www.holy-moly.com";
    
    /**
     * hack name for page
     */
    private static final String PAGE_NAME = "/page";
    
    /**
     * the site name and page name
     */
    private static final String PAGE_FULL = "/" + SITE_NAME + PAGE_NAME;
    
    @Before
    public void before() {

		try {
			super.setUp();

		indexer.deleteAllMetadataEntries();
		addEntries();
		} catch (Exception e) {
			log.error(e.getMessage());
			log.debug(e);
		}

    }
    
    /**
     * Checks Basic query with 1 hit on each page
     * @throws Exception
     */
    @Test
	@Ignore("TODO: These tests fail intermittently - need fixed")
    public void testA() throws Exception
    {
    	PSVisitQuery query = new PSVisitQuery();
    	query.setLimit("3");
    	query.setSectionPath("");
    	query.setSortOrder("desc");
    	query.setTimePeriod("WEEK");
    	List<String> topPages = blogPostService.getTopVisitedBlogPosts(query);
    	
        assertTrue("most read pages not empty", topPages.size() > 0);
		assertEquals("size is equal to 3", 3, topPages.size());
        
        // hits should be returned based on last entry date as there is no more than 1
        // entry for each page in DB at this time.
        String testName = PAGE_FULL + "4.html";
		assertEquals("first item in list was last item tracked", testName, topPages.get(0));
        
        // page 2 should be last item in list because we limited the query by 3
        testName = PAGE_FULL + "2.html";
		assertEquals("page2.html should be returned", testName, topPages.get(2));
    }
    
    /**
     * Mixes up the page order manually
     * and then ensures query works correctly
     * with different query params
     * @throws Exception
     */
    @Test
	@Ignore("TODO: Fix me.  These test cases sporadically fail.")
    public void testB() throws Exception {
    	PSVisitQuery query = new PSVisitQuery();
    	query.setLimit("5");
    	query.setSectionPath(PAGE_FULL);
    	query.setSortOrder("desc");
    	query.setTimePeriod("ALLTIME");
    	
    	List<String> topPages = blogPostService.getTopVisitedBlogPosts(query);
    	
    	mixUpBlogPostHits(topPages);
    	
    	topPages = blogPostService.getTopVisitedBlogPosts(query);

		assertEquals("list should contain 5 items", 5, topPages.size());
    	
    	String testName = PAGE_FULL + "0.html";
		assertEquals("page0.html should have the most hits", testName, topPages.get(4));
    	
    	testName = PAGE_FULL + "2.html";
		assertEquals("page2.html should have 2nd most hits", testName, topPages.get(2));
    	
    	testName = PAGE_FULL + "1.html";
		assertEquals("page1.html should have 3rd most hits", testName, topPages.get(3));
    	
    }
    
    /**
     * checks a few more params
     * @return Exception
     */
    @Test
	@Ignore("TODO: These tests fail intermittently - need fixed")
    public void testC() throws Exception {
    	PSVisitQuery query = new PSVisitQuery();
    	query.setLimit("5");
    	// should return nothing with this section path selected
    	query.setSectionPath("/test");
    	query.setSortOrder("asc");
    	query.setTimePeriod("ALLTIME");
    	
    	List<String> pagePaths = blogPostService.getTopVisitedBlogPosts(query);

		assertEquals("Page paths size should be 0", 0, pagePaths.size());
    }
    
    @Test
	@Ignore("TODO: These tests fail intermittently - need fixed")
    public void testD() throws Exception {
    	PSVisitQuery query = new PSVisitQuery();
    	query.setLimit("1");
    	query.setSectionPath("");
    	query.setSortOrder("asc");
    	query.setTimePeriod("ALLTIME");
    	
    	List<String> pagePaths = blogPostService.getTopVisitedBlogPosts(query);

		assertEquals("list size should be 1", 1, pagePaths.size());
    	
    	String testName = PAGE_FULL + "4.html";
		assertEquals("least hit page should be page4.html", testName, pagePaths.get(0));
    	
    	query.setSortOrder("desc");
    	pagePaths = blogPostService.getTopVisitedBlogPosts(query);
    	
    	testName = PAGE_FULL + "0.html";
    	assertEquals("page0.html should still have most hits", testName, pagePaths.get(0));
    }
    
    @Test
    @Ignore("Test is intermittently failing, TODO: Fix Me!")
    public void testE() throws Exception {
    	// tests many items in DB
    	addManyEntries();
    	
    	PSVisitQuery query = new PSVisitQuery();
    	// 25 is the current max allowed limit for the query in the UI
    	query.setLimit("25");
    	query.setSectionPath("");
    	query.setSortOrder("asc");
    	query.setTimePeriod("ALLTIME");
    	
    	List<String> topPosts = blogPostService.getTopVisitedBlogPosts(query);

		assertEquals("List size is 25", 25, topPosts.size());
    	
    	// ensure 155 still got set regardless of UI max limit
    	query.setLimit("155");
    	
    	topPosts = blogPostService.getTopVisitedBlogPosts(query);
		assertEquals("List size is 155", 155, topPosts.size());
    }
    
    @Test
	@Ignore("TODO: These tests fail intermittently - need fixed")
    public void testF() throws Exception {
    	// test the delete functionality for items 6 - 156
    	// IMPORTANT: delete functionality hasn't been completed yet
    	// check the delete methods to see code that is a template but not
    	// completed.  leaving as unsupportedOperationException for now
    	// as this test should work once those methods are implemented
    	PSVisitQuery query = new PSVisitQuery();
    	
    	// 25 is the current max allowed limit for the query in the UI
    	query.setLimit("25");
    	query.setSectionPath("");
    	query.setSortOrder("desc");
    	query.setTimePeriod("DAY");
    	
    	List<String> pagePaths = new ArrayList<String>();
    	for (int i = ENTRY_COUNT + 1; i < MAX_COUNT + 1; i++) {
    		pagePaths.add(PAGE_FULL + i + ".html");
    	}
    	
    	try {
    		blogPostService.delete(pagePaths);
    	}
    	catch (Exception e) {
    		assertTrue("delete method should be unsupported", e instanceof UnsupportedOperationException);
    	}

    }
    
    @Test
	@Ignore("TODO: These tests fail intermittently - need fixed")
    public void testG() {
    	// test miscellaneous code for coverage
    	PSDbBlogPostVisit bpv = null;
    	
    	try {
    		bpv = new PSDbBlogPostVisit(null, new Date(), BigInteger.ONE);
    	}
    	catch (IllegalArgumentException illegalArgumentException) {
    		assertEquals("pagepath cannot be null or empty", illegalArgumentException.getMessage());
    	}
    	
    	try {
    		bpv = new PSDbBlogPostVisit("test", null, BigInteger.ONE);
    	}
    	catch (IllegalArgumentException illegalArgumentException) {
    		assertEquals("hitDate cannot be null", illegalArgumentException.getMessage());
    	}
    	
    	try {
    		bpv = new PSDbBlogPostVisit("test", new Date(), null);
    	}
    	catch (IllegalArgumentException illegalArgumentException) {
    		assertEquals("hitCount cannot be null", illegalArgumentException.getMessage());
    	}
    	
    	bpv = new PSDbBlogPostVisit("test", new Date(), BigInteger.ONE);
    	assertEquals("should equal test", "test", bpv.getPagepath());
    	assertTrue(bpv.getHitDate().getTime() <= System.currentTimeMillis());
    	
    }
    
    /**
     * adds mock blog post visit classes to blog_post_visits
     * table as well as perc_page_metadata table
     */
    private void addEntries() throws Exception {
    	Collection<IPSMetadataEntry> ents = new ArrayList<IPSMetadataEntry>();
        PSDbMetadataEntry e = null;
    	
        String fullPath = null;
        String pageName = null;
    	
        for (int i = 0; i < ENTRY_COUNT; i++)
        {
    		pageName = PAGE_NAME + i + ".html";
    		// appending the slash as that is used for pagepath
    		fullPath = "/" + SITE_NAME + pageName;
    		blogPostService.trackBlogPost(fullPath);
            e = createMDEntry(pageName, "/", fullPath, "page", SITE_NAME);
            ents.add(e);
        }
        
        indexer.save(ents);

        // sleep for 2 seconds, 1 second longer than PSBlogPostVisitService
        // thread executor scheduler
		//TODO: Figure out why this is needed.
    	Thread.sleep(5000);
    }
    
    /**
     * Adds duplicates to inMemoryMap in blogpostvisitservice
     * to get better coverage and adds different hit rates to pages
     * @param pagePaths the list of pagePaths in DB
     */
    private void mixUpBlogPostHits(List<String> pagePaths) {
    	// page1.html tracks -- 3rd most hits
    	for (int i = 0; i < 3; i++) {
    		blogPostService.trackBlogPost(pagePaths.get(3));
    	}
    	// page2.html tracks -- 2nd most hits
    	for (int i = 0; i < 5; i++) {
    		blogPostService.trackBlogPost(pagePaths.get(2));
    	}
    	// page0.html tracks should now have the most
    	for (int i = 0; i < 10; i++) {
    		blogPostService.trackBlogPost(pagePaths.get(4));
    	}
    	
    	// track page3.html once
    	blogPostService.trackBlogPost(pagePaths.get(1));
    }
    
    /**
     * helper method to help test to set up over 
     * 50 entries in db to test session.flush and clear
     */
    private void addManyEntries() throws Exception {
    	Collection<IPSMetadataEntry> ents = new ArrayList<IPSMetadataEntry>();
        PSDbMetadataEntry e = null;
    	
        String fullPath = null;
        String pageName = null;
    	
        for (int i = ENTRY_COUNT + 1; i < MAX_COUNT + 1; i++)
        {
    		pageName = PAGE_NAME + i + ".html";
    		// appending the slash as that is used for pagepath
    		fullPath = "/" + SITE_NAME + pageName;
    		blogPostService.trackBlogPost(fullPath);
            e = createMDEntry(pageName, "/", fullPath, "page", SITE_NAME);
            ents.add(e);
        }
        
        indexer.save(ents);

        // sleep for 2 seconds, 1 second longer than PSBlogPostVisitService
        // thread executor scheduler
		//TODO: Figure out why this is needed.
    	Thread.sleep(5000);
    }
    
    
    private PSDbMetadataEntry createMDEntry(String name, String folder, String pagepath, String type, String testsite)
    {
        PSDbMetadataEntry entry = new PSDbMetadataEntry(name, folder, pagepath, type, testsite);
        return entry;
    }

}

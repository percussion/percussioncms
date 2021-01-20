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
package com.percussion.delivery.metadata;

import com.percussion.delivery.metadata.data.PSMetadataBlogYear;
import com.percussion.delivery.metadata.data.PSMetadataQuery;
import com.percussion.delivery.metadata.data.PSMetadataRestBlogList;
import com.percussion.delivery.metadata.impl.PSBlogsHelper;
import com.percussion.delivery.metadata.impl.utils.PSPair;
import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataEntry;
import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataProperty;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author leonardohildt
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{"classpath:test-beans.xml"})
public class PSBlogsHelperTest extends TestCase
{

    @Autowired
    public IPSMetadataQueryService service;

    @Autowired
    public IPSMetadataIndexerService indexer;

    private int entryIdx = 0;

    private static final int ENTRY_COUNT = 5;
    
    private static final int YEAR_COUNT = 2;

    @Before
    public void before() throws Exception
    {
        super.setUp();
        indexer.deleteAllMetadataEntries();
        addTestEntries();
    }
    
    @Test
    public void testBlogProcess() throws Exception
    {
        addTestEntries();
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='testsite'");
        query.setCriteria(criteria);
        query.setOrderBy("dcterms:created asc");
        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results =  searchResults.getFirst();

        PSBlogsHelper psBlogsHelper = new PSBlogsHelper();
        PSMetadataRestBlogList yearsList = new PSMetadataRestBlogList();
        yearsList = psBlogsHelper.getProcessedBlogs(results);
                
        assertNotNull("entries not null", results);
        assertEquals("years found", YEAR_COUNT, yearsList.getYears().size());
        
        for(PSMetadataBlogYear year : yearsList.getYears()){
            System.out.println("Proccesed year: " + year.getYear());
            System.out.println("Year count: " + year.getYearCount());
        }
    }
    
    private void addTestEntries()
    {
        Collection<IPSMetadataEntry> ents = new ArrayList<IPSMetadataEntry>();
        PSDbMetadataEntry e = null;
        
        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("/folderA/blogs/", "blogs linktext", getTime(2010, 11, 15), "blog", entryIdx++);
            ents.add(e);
        }

        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("/folderA/events/", "events linktext", getTime(2010, 12, 15, 16, 17, 18), "event",
                    entryIdx++);
            ents.add(e);
        }

        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("/folderA/foobars/", "foobars linktext", getTime(2011, 1, 15), "template2", "foobar",
                    entryIdx++);
            ents.add(e);
        }

        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("customSite", "/folderA/pages/", "pages linktext", getTime(2011, 2, 15), "other abstract",
                    "otherTemplate", "page", entryIdx++);
            ents.add(e);
        }

        indexer.save(ents);
    }

    private PSDbMetadataEntry createEntry(String folder, String linktext, Date date, String type, int idx)
    {
        return createEntry("testsite", folder, linktext, date, type, idx);
    }

    private PSDbMetadataEntry createEntry(String folder, String linktext, Date date, String template, String type, int idx)
    {
        return createEntry("testsite", folder, linktext, date, "a summary of the page", template, type, idx);
    }

    private PSDbMetadataEntry createEntry(String testsite, String folder, String linktext, Date date, String type, int idx)
    {
        return createEntry(testsite, folder, linktext, date, "a summary of the page", "templateName", type, idx);
    }

    private PSDbMetadataEntry createEntry(String testsite, String folder, String linktext, Date date, String abstr,
            String template, String type, int idx)
    {
        return createEntry(testsite, "ABC Title " + idx, folder, linktext, date, abstr, template, type, idx);
    }

    private PSDbMetadataEntry createEntry(String testsite, String title, String folder, String linktext, Date date,
            String abstr, String template, String type, int idx)
    {
        String name = "page" + idx + ".html";
        String pagepath = "/" + testsite + folder + name;
        PSDbMetadataEntry entry = new PSDbMetadataEntry(name, folder, pagepath, type, testsite);
        entry.setLinktext(linktext);
        entry.addProperty(new PSDbMetadataProperty("dcterms:title", title));
        entry.addProperty(new PSDbMetadataProperty("dcterms:description", "ABC Description " + idx));
        entry.addProperty(new PSDbMetadataProperty("dcterms:created", date));
        entry.addProperty(new PSDbMetadataProperty("dcterms:source", template));
        entry.addProperty(new PSDbMetadataProperty("dcterms:abstract", abstr));
        entry.addProperty(new PSDbMetadataProperty("dcterms:references", "bote, health"));
        entry.addProperty(new PSDbMetadataProperty("perc:testIndex", idx));
        entry.addProperty(new PSDbMetadataProperty("perc:type", type));
        entry.addProperty(new PSDbMetadataProperty("perc:reverseIndex", 10000 - idx));
        return entry;
    }

    private Date getTime(int year, int month, int day)
    {
        return getTime(year, month, day, 0, 0, 0);
    }

    private Date getTime(int year, int month, int day, int hour, int minute, int second)
    {
        Calendar cal = Calendar.getInstance();

        cal.clear();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        return cal.getTime();
    }

}

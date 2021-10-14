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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.percussion.delivery.metadata.IPSMetadataProperty.VALUETYPE;
import com.percussion.delivery.metadata.data.PSMetadataBlogResult;
import com.percussion.delivery.metadata.data.PSMetadataQuery;
import com.percussion.delivery.metadata.data.PSMetadataRestCategory;
import com.percussion.delivery.metadata.data.PSMetadataRestEntry;
import com.percussion.delivery.metadata.error.PSMalformedMetadataQueryException;
import com.percussion.delivery.metadata.impl.PSMetadataCategoriesHelper;
import com.percussion.delivery.metadata.impl.PSMetadataTagsHelper;
import com.percussion.delivery.metadata.impl.utils.PSPair;
import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataEntry;
import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataProperty;
import com.percussion.delivery.metadata.rdbms.impl.PSMetadataQueryService;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author erikserating
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
        {"classpath:test-beans.xml"})
public class PSMetadataQueryServiceTest extends TestCase
{
    private static final Logger log = LogManager.getLogger(PSMetadataQueryServiceTest.class);

    @Autowired
    public PSMetadataQueryService service;

    @Autowired
    public IPSMetadataIndexerService indexer;

    private int entryIdx = 0;

    private static final int ENTRY_COUNT = 5;

    @Before
    public void before() throws Exception
    {
        super.setUp();
        indexer.deleteAllMetadataEntries();
        addTestEntries();
    }

    @Test
    public void testTagsProcess() throws Exception
    {
        List<IPSMetadataEntry> results = initiazeTagsProcessTest();

        PSMetadataTagsHelper psMetadataTagsHelper = new PSMetadataTagsHelper();

        List<PSPair<String, Integer>> tags = psMetadataTagsHelper.processTags(results, null);

        assertNotNull("array tags not null", tags);
        assertEquals("size array tags", 6, tags.size());
        assertEquals("name first tags", "bar", tags.get(0).getFirst());

        System.out.println("testTagsProcess::");
        System.out.println();
        for (int i = 0; i < tags.size(); i++)
        {
            PSPair<String, Integer> e = tags.get(i);
            System.out.println(e.toString());
        }
        System.out.println();
    }

    @Test
    public void testTagsProcess_EmptyMetadataList() throws Exception
    {
        List<IPSMetadataEntry> results = new ArrayList<IPSMetadataEntry>();

        PSMetadataTagsHelper psMetadataTagsHelper = new PSMetadataTagsHelper();

        List<PSPair<String, Integer>> tags = psMetadataTagsHelper.processTags(results, null);

        assertNotNull("array tags not null", tags);
        assertEquals("size array tags", 0, tags.size());
    }

    @Test
    public void testTagsProcess_withOrderAlpha() throws Exception
    {
        List<IPSMetadataEntry> results = initiazeTagsProcessTest();

        PSMetadataTagsHelper psMetadataTagsHelper = new PSMetadataTagsHelper();

        List<PSPair<String, Integer>> tags = psMetadataTagsHelper.processTags(results, null);

        assertNotNull("array tags not null", tags);
        assertEquals("size array tags", 6, tags.size());
        assertEquals("name first tags", "bar", tags.get(0).getFirst());

        System.out.println("testTagsProcess_withOrderAlpha::");
        System.out.println();
        for (int i = 0; i < tags.size(); i++)
        {
            PSPair<String, Integer> e = tags.get(i);
            System.out.println(e.toString());
        }
        System.out.println();
    }

    @Test
    public void testTagsProcess_withOrderCount() throws Exception
    {
        List<IPSMetadataEntry> results = initiazeTagsProcessTest();

        PSMetadataTagsHelper psMetadataTagsHelper = new PSMetadataTagsHelper();

        List<PSPair<String, Integer>> tags = psMetadataTagsHelper.processTags(results, PSMetadataTagsHelper.COUNT_SORT);

        assertNotNull("array tags not null", tags);
        assertEquals("size array tags", 6, tags.size());
        assertEquals("name first tags", "jb", tags.get(0).getFirst());

        System.out.println("testTagsProcess_withOrderCount::");
        System.out.println();
        for (int i = 0; i < tags.size(); i++)
        {
            PSPair<String, Integer> e = tags.get(i);
            System.out.println(e.toString());
        }
        System.out.println();
    }

    @Test
    public void testTagsProcess_withOrderCountAndAlpha() throws Exception
    {
        List<IPSMetadataEntry> results = initiazeTagsProcessTest();

        PSMetadataTagsHelper psMetadataTagsHelper = new PSMetadataTagsHelper();

        List<PSPair<String, Integer>> tags = psMetadataTagsHelper.processTags(results, PSMetadataTagsHelper.COUNT_SORT);

        assertNotNull("array tags not null", tags);
        assertEquals("size array tags", 6, tags.size());
        assertEquals("name first tags", "jb", tags.get(0).getFirst());

        System.out.println("testTagsProcess_withOrderCountAndAlpha::");
        System.out.println();
        for (int i = 0; i < tags.size(); i++)
        {
            PSPair<String, Integer> e = tags.get(i);
            System.out.println(e.toString());
        }
        System.out.println();
    }

    @Test
    public void testReturnTotalEntries() throws Exception
    {
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='testsite'");
        query.setCriteria(criteria);
        query.setStartIndex(1);
        query.setReturnTotalEntries(true);

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);

        Integer results = searchResults.getSecond(); //getting totalEntries

        assertNotNull("Entries count should NOT be null", results);
        assertEquals("Wrong entries count" + results.intValue(), ENTRY_COUNT * 3, results.intValue());

        query.setReturnTotalEntries(false);

        searchResults = service.executeQuery(query);

        results = searchResults.getSecond(); //getting totalEntries

        assertNull("Entries count should be null", results);
    }

    @Test
    public void testOrderBy_Created_And_Linktext() throws Exception
    {
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='testsite'");
        query.setCriteria(criteria);

        query.setOrderBy("dcterms:created asc, linktext asc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);

        List<IPSMetadataEntry> results = searchResults.getFirst();
        Map<String, IPSMetadataProperty> props;

        assertNotNull("entries not null", results);
        assertEquals("entries found", ENTRY_COUNT * 3, results.size());

        Date previousDateValue = getTime(1900, 1, 1);
        IPSMetadataEntry entry;

        for (int i = 0; i < results.size(); i++)
        {
            entry = results.get(i);

            props = toPropsMap(entry.getProperties());

            assertTrue("Date greater than previous",
                    props.get("dcterms:created").getDatevalue().compareTo(previousDateValue) >= 0);

            previousDateValue = props.get("dcterms:created").getDatevalue();
        }
    }


    @Test
    public void testBlogResult_withoutNext() throws Exception
    {
        addTestEntries();
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='testsite'");

        query.setCriteria(criteria);
        query.setOrderBy("dcterms:created desc");
        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        // needs to be the last page in the array in order to make
        // sense of the test assertion logic assertNull for getNext()
        String currentPageId = results.get(results.size()-1).getPagepath();

        List<PSMetadataRestEntry> resultArr = new ArrayList<PSMetadataRestEntry>();
        for (IPSMetadataEntry entry : results)
        {
            resultArr.add(toRestMetadataEntry(entry));
        }

        PSMetadataBlogResult metadataBlogResults = new PSMetadataBlogResult();

        for (int i = 0; i < resultArr.size(); i++)
        {
            PSMetadataRestEntry entry = resultArr.get(i);
            if (entry.getPagepath().equalsIgnoreCase(currentPageId))
            {
                if (i > 0)
                {
                    metadataBlogResults.setPrevious(resultArr.get(i - 1));
                }
                metadataBlogResults.setCurrent(resultArr.get(i));
                if ((i + 1) < resultArr.size())
                {
                    metadataBlogResults.setNext(resultArr.get(i + 1));
                }
                break;
            }
        }

        assertNotNull("previous not null", metadataBlogResults.getPrevious());
        assertNotNull("current not null", metadataBlogResults.getCurrent());
        assertNull("next null", metadataBlogResults.getNext());
    }

    @Test
    public void testBlogResult_withoutPreviousAndNext() throws Exception
    {
        addTestEntries();
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();
        String currentPageId = "/testsite/folderA/foobars/page25.html";

        criteria.add("site='testsite'");
        query.setCriteria(criteria);
        query.setOrderBy("dcterms:created desc");
        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        List<PSMetadataRestEntry> resultArr = new ArrayList<PSMetadataRestEntry>();
        for (IPSMetadataEntry entry : results)
        {
            resultArr.add(toRestMetadataEntry(entry));
        }

        PSMetadataBlogResult metadataBlogResults = new PSMetadataBlogResult();

        for (int i = 0; i < resultArr.size(); i++)
        {
            PSMetadataRestEntry entry = resultArr.get(i);
            if (entry.getPagepath().equalsIgnoreCase(currentPageId))
            {
                if (i > 0)
                {
                    metadataBlogResults.setNext(resultArr.get(i - 1));
                }
                metadataBlogResults.setCurrent(resultArr.get(i));
                if ((i + 1) < resultArr.size())
                {
                    metadataBlogResults.setPrevious(resultArr.get(i + 1));
                }
                break;
            }
        }

        assertNull("next is null", metadataBlogResults.getNext());
        assertNull("current is null", metadataBlogResults.getCurrent());
        assertNull("previous is null", metadataBlogResults.getPrevious());
    }

    private List<IPSMetadataEntry> initiazeTagsProcessTest()
    {
        // String name, String folder, String pagepath, String type, String site
        PSDbMetadataEntry page1 = new PSDbMetadataEntry("page1", "f", "pp1", "t", "site1");
        PSDbMetadataEntry page2 = new PSDbMetadataEntry("page2", "f", "pp2", "t", "site1");
        PSDbMetadataEntry page3 = new PSDbMetadataEntry("page3", "f", "pp3", "t", "site1");

        PSDbMetadataProperty propNotFound1 = new PSDbMetadataProperty("NOT ref", VALUETYPE.STRING, "no tags");
        PSDbMetadataProperty propNotFound2 = new PSDbMetadataProperty("NOT ref", VALUETYPE.STRING, "no tags");
        PSDbMetadataProperty propFound31 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                " PEpe");
        PSDbMetadataProperty propFound32 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                "       bar");
        PSDbMetadataProperty propFound33 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                "   foo");
        PSDbMetadataProperty propFound34 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                " LEO");
        PSDbMetadataProperty propFound35 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                "david          ");
        PSDbMetadataProperty propFound36 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                "jb");

        PSDbMetadataProperty propFound4 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                "jb");
        PSDbMetadataProperty propFound51 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                "PEPE");
        PSDbMetadataProperty propFound52 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                "LEO");
        PSDbMetadataProperty propFound53 = new PSDbMetadataProperty(PSMetadataTagsHelper.REFERENCES, VALUETYPE.STRING,
                "jb");
        // page1
        page1.addProperty(propNotFound1);
        page1.addProperty(propNotFound2);
        page1.addProperty(propFound31);
        page1.addProperty(propFound32);
        page1.addProperty(propFound33);
        page1.addProperty(propFound34);
        page1.addProperty(propFound35);
        page1.addProperty(propFound36);
        // page2
        page2.addProperty(propNotFound1);
        page2.addProperty(propNotFound2);
        page2.addProperty(propFound4);
        // page2
        page3.addProperty(propNotFound1);
        page3.addProperty(propNotFound2);
        page3.addProperty(propFound51);
        page3.addProperty(propFound52);
        page3.addProperty(propFound53);

        List<IPSMetadataEntry> results = new ArrayList<IPSMetadataEntry>();
        results.add(page1);
        results.add(page2);
        results.add(page3);
        return results;
    }

    @Test
    public void testProcessCategories() throws Exception
    {
        List<IPSMetadataEntry> resultsPage1 = initializeCategoriesProcessTest();
        List<IPSMetadataEntry> resultsPage2 = initializeNullCategoriesProcessTest();

        PSMetadataCategoriesHelper psMetadataCategoriesHelper = new PSMetadataCategoriesHelper();
        List<PSMetadataRestCategory> categoriesPage1 = psMetadataCategoriesHelper.processCategories(resultsPage1);
        List<PSMetadataRestCategory> categoriesPage2 = psMetadataCategoriesHelper.processCategories(resultsPage2);

        // Categories in page 1
        System.out.println("testProcessCategories::\n");
        if (categoriesPage1 != null)
        {
            for (PSMetadataRestCategory category : categoriesPage1)
            {
                printTree(category, "");
            }
        }
        System.out.println();

        assertNotNull("categories not null", categoriesPage1);
        assertEquals("size array categories", 4, countCategoriesFromTree(categoriesPage1.get(0)));

        // Categories in page 2
        assertEquals(0, categoriesPage2.size());
    }

    private List<IPSMetadataEntry> initializeNullCategoriesProcessTest()
    {
        // String name, String folder, String pagepath, String type, String site
        PSDbMetadataEntry page2 = new PSDbMetadataEntry("page2", "f", "pp2", "t", "site1");

        PSDbMetadataProperty propNotFound1 = new PSDbMetadataProperty("NOT ref", VALUETYPE.STRING, "no categories");

        // page2
        page2.addProperty(propNotFound1);

        List<IPSMetadataEntry> results = new ArrayList<IPSMetadataEntry>();
        results.add(page2);
        return results;
    }

    private List<IPSMetadataEntry> initializeCategoriesProcessTest()
    {
        // String name, String folder, String pagepath, String type, String site
        PSDbMetadataEntry page1 = new PSDbMetadataEntry("page1", "f", "pp1", "t", "site1");

        PSDbMetadataProperty propFound1 = new PSDbMetadataProperty(PSMetadataCategoriesHelper.REFERENCES,
                VALUETYPE.STRING, "/Categories/CategoryB/CategoryB2");
        PSDbMetadataProperty propFound2 = new PSDbMetadataProperty(PSMetadataCategoriesHelper.REFERENCES,
                VALUETYPE.STRING, "/Categories/CategoryB/CategoryB1");
        PSDbMetadataProperty propFound3 = new PSDbMetadataProperty(PSMetadataCategoriesHelper.REFERENCES,
                VALUETYPE.STRING, "/Categories/CategoryA/CategoryA2");
        PSDbMetadataProperty propFound4 = new PSDbMetadataProperty(PSMetadataCategoriesHelper.REFERENCES,
                VALUETYPE.STRING, "/Categories/CategoryA/CategoryA1");

        // page1
        page1.addProperty(propFound1);
        page1.addProperty(propFound2);
        page1.addProperty(propFound3);
        page1.addProperty(propFound4);

        List<IPSMetadataEntry> results = new ArrayList<IPSMetadataEntry>();
        results.add(page1);
        return results;
    }

    private void printTree(PSMetadataRestCategory categories, String spaceIdent)
    {
        if (!categories.getCategory().isEmpty())
        {
            System.out.println(spaceIdent + "Category name: " + categories.getCategory());
            System.out.println(spaceIdent + "Category count: (" + categories.getCount().getFirst() + ", "
                    + categories.getCount().getSecond() + ")");
            for (PSMetadataRestCategory child : categories.getChildren())
            {
                printTree(child, spaceIdent + "    ");
            }
        }
    }

    private int countCategoriesFromTree(PSMetadataRestCategory categories)
    {
        int count = 0;

        if (!categories.getCategory().isEmpty())
        {
            count = categories.getCount().getFirst();
            for (PSMetadataRestCategory child : categories.getChildren())
            {
                count += countCategoriesFromTree(child);
            }
        }

        return count;
    }
    @Test
    public void testCategoryQuery(){
        PSMetadataQuery q = new PSMetadataQuery();

        q.setTotalMaxResults(10);
        try {
            List<Object[]> cats = service.executeCategoryQuery(q);


        } catch (PSMalformedMetadataQueryException e) {
            fail(e.getMessage());
        }
    }
    @Test
    public void testQuery() throws Exception
    {
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("folder='/folderA/blogs/'");
        criteria.add("dcterms:title like 'ABC Title%'");
        criteria.add("perc:type='blog'");
        query.setCriteria(criteria);
        query.setOrderBy("perc:reverseIndex asc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        assertNotNull("entries not null", results);
        assertEquals("entries found", ENTRY_COUNT, results.size());
        Map<String, IPSMetadataProperty> props;

        for (IPSMetadataEntry entry : results)
        {
            props = toPropsMap(entry.getProperties());

            // Folder
            assertEquals("entry folder", "/folderA/blogs/", entry.getFolder());

            // dcterms:title
            assertTrue("dcterms:title present", props.containsKey("dcterms:title"));
            assertEquals("dcterms:title value type", VALUETYPE.STRING, props.get("dcterms:title").getValuetype());
            assertTrue("dcterms:title value", props.get("dcterms:title").getStringvalue().startsWith("ABC Title"));

            // type
            assertEquals("entry type", "blog", entry.getType());
        }
    }

    /***
     * Validate that the query limit property is working.
     * @throws Exception
     */
    @Test
    public void testQueryLimit() throws Exception{
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();
        Integer configLimit = service.getQueryLimit();
        service.setQueryLimit(3);
        criteria.add("folder='/folderA/blogs/'");
        criteria.add("dcterms:title like 'ABC Title%'");
        criteria.add("perc:type='blog'");
        query.setCriteria(criteria);
        query.setOrderBy("perc:reverseIndex asc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        assertNotNull("entries not null", results);
        assertEquals("entries found", 3, results.size());
        service.setQueryLimit(configLimit);
    }

    @Test
    public void testQueryDuplicateEntries() throws Exception{
        addCategorieEntry();

        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("perc:category LIKE '/Categories%'");
        criteria.add("dcterms:source = 'templateName'");
        criteria.add("type = 'page'");

        query.setCriteria(criteria);
        query.setOrderBy("dcterms:created desc, linktext asc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        assertEquals("size array categories", 1, searchResults.getFirst().size());
        assertEquals("size array categories ", 1, searchResults.getSecond().intValue());
    }

    public void addCategorieEntry(){
        Collection<IPSMetadataEntry> ents = new ArrayList<IPSMetadataEntry>();
        ents.add(createCategoryEntry());
        indexer.save(ents);
    }

    private PSDbMetadataEntry createCategoryEntry()
    {
        String page = "categoryResultTest";
        String name = page + ".html";
        String folder = "/folderA/";
        String pagepath = "//testsite" + folder + page;
        PSDbMetadataEntry entry = new PSDbMetadataEntry(name, folder, pagepath, "page", "testsite");
        entry.addProperty(new PSDbMetadataProperty("dcterms:created", getTime(2012, 7, 16)));
        entry.addProperty(new PSDbMetadataProperty("dcterms:source", "templateName"));
        entry.addProperty(new PSDbMetadataProperty("perc:category", "/Categories/Jardineria"));
        entry.addProperty(new PSDbMetadataProperty("perc:category", "/Categories/Jardineria/Arboles"));
        entry.addProperty(new PSDbMetadataProperty("perc:category", "/Categories/Jardineria/Arboles/Perennes"));

        return entry;
    }

    @Test
    public void testCriteria_Single_EntryField_Folder() throws Exception
    {
        runEntryTest("folder = '/folderA/blogs/'", ENTRY_COUNT, new PropertyValueChecker<IPSMetadataEntry>()
        {
            public boolean valueIsCorrect(IPSMetadataEntry currentValue)
            {
                return "/folderA/blogs/".equals(currentValue.getFolder());
            }
        });
    }

    @Test
    public void testCriteria_Single_EntryField_Name() throws Exception
    {
        runEntryTest("name = 'page3.html'", 1, new PropertyValueChecker<IPSMetadataEntry>()
        {
            public boolean valueIsCorrect(IPSMetadataEntry currentValue)
            {
                return "page3.html".equals(currentValue.getName());
            }
        });
    }

    @Test
    public void testCriteria_Single_EntryField_Type()
    {
        try {
            runEntryTest("type = 'page'", ENTRY_COUNT*2, new PropertyValueChecker<IPSMetadataEntry>() {
                public boolean valueIsCorrect(IPSMetadataEntry currentValue) {
                    return "page".equals(currentValue.getType());
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            fail(e.getMessage());
        }
    }

    @Test
    public void testCriteria_Single_EntryField_Linktext() throws Exception
    {
        runEntryTest("linktext = 'foobars linktext'", ENTRY_COUNT, new PropertyValueChecker<IPSMetadataEntry>()
        {
            public boolean valueIsCorrect(IPSMetadataEntry currentValue)
            {
                return "foobars linktext".equals(currentValue.getLinktext());
            }
        });
    }

    @Test
    public void testCriteria_Single_EntryField_Pagepath() throws Exception
    {
        runEntryTest("pagepath = '/testsite/folderA/events/page7.html'", 1,
                new PropertyValueChecker<IPSMetadataEntry>()
                {
                    public boolean valueIsCorrect(IPSMetadataEntry currentValue)
                    {
                        return "/testsite/folderA/events/page7.html".equals(currentValue.getPagepath());
                    }
                });
    }

    @Test
    public void testCriteria_Single_EntryField_Site() throws Exception
    {
        runEntryTest("site = 'testsite'", ENTRY_COUNT * 3, new PropertyValueChecker<IPSMetadataEntry>()
        {
            public boolean valueIsCorrect(IPSMetadataEntry currentValue)
            {
                return "testsite".equals(currentValue.getSite());
            }
        });
    }

    @Test
    public void testCriteria_Single_EntryField_NotEqualsOperator() throws Exception
    {
        runEntryTest("site != 'testsite'", ENTRY_COUNT*2, new PropertyValueChecker<IPSMetadataEntry>()
        {
            public boolean valueIsCorrect(IPSMetadataEntry currentValue)
            {
                return !"testsite".equals(currentValue.getSite());
            }
        });
    }

    @Test
    public void testCriteria_Single_EntryField_LikeOperator() throws Exception
    {
        runEntryTest("site like 'test%'", ENTRY_COUNT * 3, new PropertyValueChecker<IPSMetadataEntry>()
        {
            public boolean valueIsCorrect(IPSMetadataEntry currentValue)
            {
                return "testsite".equals(currentValue.getSite());
            }
        });
    }

    @Test
    public void testCriteria_Single_EntryField_InOperator() throws Exception
    {
        runEntryTest("type IN ('page', 'blog')", ENTRY_COUNT * 3, new PropertyValueChecker<IPSMetadataEntry>()
        {
            public boolean valueIsCorrect(IPSMetadataEntry currentValue)
            {
                return "page".equals(currentValue.getType()) || "blog".equals(currentValue.getType());
            }
        });
    }

    @Test
    public void testCriteria_Single_Property_Title() throws Exception
    {
        runPropertyTest("dcterms:title = 'ABC Title 1'", 1, "dcterms:title", VALUETYPE.STRING,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        String value = (String) currentValue;
                        return value.equals("ABC Title 1");
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Created() throws Exception
    {
        // TODO This test might be not necessary (the front-end only allows
        // greater and
        // less than operators with created properties).
        runPropertyTest("dcterms:created = '2010-12-15T16:17:18'", ENTRY_COUNT, "dcterms:created", VALUETYPE.DATE,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        Date value = (Date) currentValue;
                        return value.compareTo(getTime(2010, 12, 15, 16, 17, 18)) == 0;
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Source() throws Exception
    {
        runPropertyTest("dcterms:source = 'templateName'", ENTRY_COUNT * 2, "dcterms:source", VALUETYPE.STRING,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        String value = (String) currentValue;
                        return value.equals("templateName");
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Abstract() throws Exception
    {
        runPropertyTest("dcterms:abstract = 'a summary of the page'", ENTRY_COUNT * 3, "dcterms:abstract",
                VALUETYPE.STRING, new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        String value = (String) currentValue;
                        return value.equals("a summary of the page");
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_String_NotEqualsOperator() throws Exception
    {
        runPropertyTest("dcterms:abstract != 'a summary of the page'", ENTRY_COUNT, "dcterms:abstract",
                VALUETYPE.STRING, new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        String value = (String) currentValue;
                        return !value.equals("a summary of the page");
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_String_InOperator() throws Exception
    {
        runPropertyTest("dcterms:source IN ('otherTemplate', 'template2')", ENTRY_COUNT * 2, "dcterms:source",
                VALUETYPE.STRING, new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        String value = (String) currentValue;
                        return value.equals("otherTemplate") || value.equals("template2");
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Date_NotEqualsOperator() throws Exception
    {
        // TODO This test might be not necessary (the front-end only allows
        // greater and
        // less than operators with created properties).
        runPropertyTest("dcterms:created != '2010-12-15T16:17:18'", ENTRY_COUNT * 3, "dcterms:created", VALUETYPE.DATE,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        Date date = (Date) currentValue;
                        return date.compareTo(getTime(2010, 12, 15, 16, 17, 18)) != 0;
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Date_GreaterThanOperator() throws Exception
    {
        runPropertyTest("dcterms:created > '2010-12-15T16:17:18'", ENTRY_COUNT * 2, "dcterms:created", VALUETYPE.DATE,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        Date date = (Date) currentValue;
                        return date.compareTo(getTime(2010, 12, 15, 16, 17, 18)) > 0;
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Date_GreaterOrEqualsThanOperator() throws Exception
    {
        runPropertyTest("dcterms:created >= '2010-12-15T16:17:18'", ENTRY_COUNT * 3, "dcterms:created", VALUETYPE.DATE,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        Date date = (Date) currentValue;
                        return date.compareTo(getTime(2010, 12, 15, 16, 17, 18)) >= 0;
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Date_LessThanOperator() throws Exception
    {
        runPropertyTest("dcterms:created < '2010-12-15T00:00:00'", ENTRY_COUNT, "dcterms:created", VALUETYPE.DATE,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        Date date = (Date) currentValue;
                        return date.compareTo(getTime(2010, 12, 15)) < 0;
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Date_LessOrEqualsThanOperator() throws Exception
    {
        runPropertyTest("dcterms:created <= '2010-12-15T16:17:18'", ENTRY_COUNT * 2, "dcterms:created", VALUETYPE.DATE,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        Date date = (Date) currentValue;
                        return date.compareTo(getTime(2010, 12, 15, 16, 17, 18)) <= 0;
                    }
                });
    }

    @Test
    public void testCriteria_Single_Property_Date_DifferentDateFormatSpecified() throws Exception
    {
        runPropertyTest("dcterms:created <= '2010-12-15 16:17:18'", ENTRY_COUNT * 2, "dcterms:created", VALUETYPE.DATE,
                new PropertyValueChecker<Object>()
                {
                    public boolean valueIsCorrect(Object currentValue)
                    {
                        Date date = (Date) currentValue;
                        return date.compareTo(getTime(2010, 12, 15, 16, 17, 18)) <= 0;
                    }
                });
    }

    @Test
    public void testOrderBy_Created() throws Exception
    {
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='testsite'");
        query.setCriteria(criteria);

        query.setOrderBy("dcterms:created asc");
        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);

        List<IPSMetadataEntry> results = searchResults.getFirst();
        Map<String, IPSMetadataProperty> props;

        assertNotNull("entries not null", results);
        assertEquals("entries found", ENTRY_COUNT * 3, results.size());

        Date previousDateValue = getTime(1900, 1, 1);
        PSDbMetadataEntry entry;

        for (int i = 0; i < results.size(); i++)
        {
            entry = (PSDbMetadataEntry)results.get(i);

            props = toPropsMap(entry.getProperties());

            assertTrue("Date greater than previous",
                    props.get("dcterms:created").getDatevalue().compareTo(previousDateValue) >= 0);

            previousDateValue = props.get("dcterms:created").getDatevalue();
        }
    }

    @Test
    public void testOrderBy_Title_SimpleOrdering() throws Exception
    {
        indexer.deleteAllMetadataEntries();

        // Create metadata entries
        List<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        String[] orderedTitles = new String[]
                {"a page", "b page", "b page 2", "c page", "c page 2", "d page"};

        // Add in inversed order
        for (int i=orderedTitles.length - 1; i>=0; i--)
        {
            String title = orderedTitles[i];

            entries.add(createEntry("customSite", title, "/folderA/pages/", "pages linktext", getTime(2011, 2, 15),
                    "other abstract", "otherTemplate", "page", entryIdx++));
        }

        indexer.save(entries.subList(entries.size() / 2, entries.size()));
        indexer.save(entries.subList(0, entries.size() / 2));

        // Get entries ordered by title
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='customSite'");
        query.setCriteria(criteria);

        query.setOrderBy("dcterms:title asc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        Map<String, IPSMetadataProperty> props;

        assertNotNull("entries not null", results);
        assertEquals("entries found", orderedTitles.length, results.size());

        PSDbMetadataEntry entry;

        for (int i = 0; i < results.size(); i++)
        {
            entry = (PSDbMetadataEntry)results.get(i);

            props = toPropsMap(entry.getProperties());

            assertEquals("Title greater than previous", orderedTitles[i], props.get("dcterms:title").getStringvalue());
        }

        //set the following values on the query to test pagination
        query.setMaxResults(2);
        query.setStartIndex(4);
        searchResults = service.executeQuery(query);
        results = searchResults.getFirst();
        assertEquals("results list size", 2, results.size());

        props = toPropsMap(results.get(0).getProperties());
        assertEquals("First title from the list", "c page 2", props.get("dcterms:title").getStringvalue());

        props = toPropsMap(results.get(1).getProperties());
        assertEquals("First title from the list", "d page", props.get("dcterms:title").getStringvalue());
    }

    @Test
    public void testOrderBy_Title_MixingUpperAndLowerCasedChars_Asc() throws Exception
    {
        indexer.deleteAllMetadataEntries();

        // Create metadata entries
        List<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        String[] orderedTitles = new String[]
                {"A page", "b page", "c page", "D page", "E page", "f page"};

        // Add in inversed order
        for (int i=orderedTitles.length - 1; i>=0; i--)
        {
            String title = orderedTitles[i];

            entries.add(createEntry("customSite", title, "/folderA/pages/", "pages linktext", getTime(2011, 2, 15),
                    "other abstract", "otherTemplate", "page", entryIdx++));
        }

        indexer.save(entries.subList(entries.size() / 2, entries.size()));
        indexer.save(entries.subList(0, entries.size() / 2));

        // Get entries ordered by title
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='customSite'");
        query.setCriteria(criteria);

        query.setOrderBy("dcterms:title asc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        Map<String, IPSMetadataProperty> props;

        assertNotNull("entries not null", results);
        assertEquals("entries found", orderedTitles.length, results.size());

        PSDbMetadataEntry entry;

        for (int i = 0; i < results.size(); i++)
        {
            entry = (PSDbMetadataEntry)results.get(i);

            props = toPropsMap(entry.getProperties());

            assertEquals("Title greater than previous", orderedTitles[i], props.get("dcterms:title").getStringvalue());
        }

        //set the following values on the query to test pagination
        query.setMaxResults(2);
        query.setStartIndex(4);
        searchResults = service.executeQuery(query);
        results = searchResults.getFirst();
        assertEquals("results list size", 2, results.size());

        props = toPropsMap(results.get(0).getProperties());
        assertEquals("First title from the list", "E page", props.get("dcterms:title").getStringvalue());

        props = toPropsMap(results.get(1).getProperties());
        assertEquals("First title from the list", "f page", props.get("dcterms:title").getStringvalue());
    }

    @Test
    public void testOrderBy_Title_MixingUpperAndLowerCasedChars_Desc() throws Exception
    {
        indexer.deleteAllMetadataEntries();

        // Create metadata entries
        List<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        String[] orderedTitles = new String[]
                {"f page", "E page", "D page", "c page", "b page", "A page"};

        // Add in inversed order
        for (int i=orderedTitles.length - 1; i>=0; i--)
        {
            String title = orderedTitles[i];

            entries.add(createEntry("customSite", title, "/folderA/pages/", "pages linktext", getTime(2011, 2, 15),
                    "other abstract", "otherTemplate", "page", entryIdx++));
        }

        indexer.save(entries.subList(entries.size() / 2, entries.size()));
        indexer.save(entries.subList(0, entries.size() / 2));

        // Get entries ordered by title
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='customSite'");
        query.setCriteria(criteria);

        query.setOrderBy("dcterms:title desc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        Map<String, IPSMetadataProperty> props;

        assertNotNull("entries not null", results);
        assertEquals("entries found", orderedTitles.length, results.size());

        PSDbMetadataEntry entry;

        for (int i = 0; i < results.size(); i++)
        {
            entry = (PSDbMetadataEntry)results.get(i);

            props = toPropsMap(entry.getProperties());

            assertEquals("Title greater than previous", orderedTitles[i], props.get("dcterms:title").getStringvalue());
        }

        //set the following values on the query to test pagination
        query.setMaxResults(2);
        query.setStartIndex(4);
        searchResults = service.executeQuery(query);
        results = searchResults.getFirst();
        assertEquals("results list size", 2, results.size());

        props = toPropsMap(results.get(0).getProperties());
        assertEquals("First title from the list", "b page", props.get("dcterms:title").getStringvalue());

        props = toPropsMap(results.get(1).getProperties());
        assertEquals("First title from the list", "A page", props.get("dcterms:title").getStringvalue());
    }

    @Test
    public void testOrderBy_PagePath() throws Exception
    {
        indexer.deleteAllMetadataEntries();
        // Create metadata entries
        List<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        String[] orderedFileNames = new String[]
                {"/run.html","/none.html","/hello.html","/bye.html"};

        String[] orderedPagePaths = new String[ENTRY_COUNT];

        String path = "/customSite/folder1/child1" ;

        Integer totalCount = 0;


        for (int i = 0; i < orderedFileNames.length; i++)
        {
            String fileName = orderedFileNames[i];
            PSDbMetadataEntry entry = new PSDbMetadataEntry(fileName, "folder", path + fileName , "TestType",
                    "customSite");
            orderedPagePaths[i] = entry.getPagepath();
            entries.add(entry);
        }
        indexer.save(entries);

        // Get entries ordered by pagepath
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='customSite'");
        query.setCriteria(criteria);
        query.setMaxResults(5);
        query.setStartIndex(0);
        query.setOrderBy("pagepath desc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        assertNotNull("entries not null", results);
        assertEquals("entries found", orderedFileNames.length, results.size());

        PSDbMetadataEntry entry;

        for (int i = 0; i < results.size(); i++)
        {
            entry = (PSDbMetadataEntry)results.get(i);
            System.out.println(entry.getPagepath());
            assertEquals("Pagepath greater than previous", orderedPagePaths[i], entry.getPagepath());
        }

        assertEquals("results list size", 4, results.size());

        totalCount = searchResults.getSecond();
        assertEquals("total entries",4,totalCount.intValue());
    }


    @Test
    public void testOrderBy_Folder() throws Exception
    {
        indexer.deleteAllMetadataEntries();
        List<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        String[] folderNames = new String[]
                {"/demofolder/child1","/folder1/child1/","/latestfolder/child1/","/oldfolder/child1/","/testingfolder/child1/",};
        for (int i = 0; i < ENTRY_COUNT ; i++)
        {
            String folderName = folderNames[i];
            String pagepath = "/testsite" + folderName + "foo.html" + i;
            PSDbMetadataEntry entry = new PSDbMetadataEntry("foo.html" + i, folderName, pagepath, "TestType",
                    "customSite");
            entries.add(entry);
        }
        indexer.save(entries);

        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='customSite'");
        query.setCriteria(criteria);

        query.setOrderBy("folder asc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        assertNotNull("entries not null", results);

        PSDbMetadataEntry entry;

        for (int i = 0; i < results.size(); i++)
        {
            entry = (PSDbMetadataEntry)results.get(i);
            assertEquals("Pagepath greater than previous", folderNames[i], entry.getFolder());
        }

        //set the following values on the query to test pagination
        query.setMaxResults(5);
        query.setStartIndex(1);
        searchResults = service.executeQuery(query);
        results = searchResults.getFirst();
        assertEquals("results list size", 4, results.size());
    }

    @Test
    public void testOrderBy_Linktext() throws Exception
    {
        indexer.deleteAllMetadataEntries();
        List<IPSMetadataEntry> entries = createPaginationEntries();
        String[] orderedLinkTexts = new String[]
                {"another linktext","first linktext","last linktext","more linktext","other linktext"};

        for (int i = 0; i < entries.size() ; i++)
        {
            PSDbMetadataEntry entry = (PSDbMetadataEntry)entries.get(i);
            entry.setLinktext(orderedLinkTexts[i]);
        }
        indexer.save(entries);

        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add("site='customSite'");
        query.setCriteria(criteria);

        query.setOrderBy("linktext asc");

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        assertNotNull("entries not null", results);

        PSDbMetadataEntry entry;

        for (int i = 0; i < results.size(); i++)
        {
            entry = (PSDbMetadataEntry)results.get(i);
            assertEquals("Pagepath greater than previous", orderedLinkTexts[i], entry.getLinktext());
        }

        //set the following values on the query to test pagination
        query.setMaxResults(5);
        query.setStartIndex(2);
        searchResults = service.executeQuery(query);
        results = searchResults.getFirst();
        assertEquals("results list size", 3, results.size());
    }

    @Test
    public void testOrderBy_Name() throws Exception
    {
        indexer.deleteAllMetadataEntries();
        List<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        Integer totalCount = 0;
        String[] pageNames = new String[]
                {"blog","demo","index","post","testing"};
        for (int i = 0; i < pageNames.length; i++)
        {
            String pageName = pageNames[i];
            String pagepath = "/testsite/folder1/child1/foo.html" + i;
            PSDbMetadataEntry entry = new PSDbMetadataEntry(pageName, "/folder1/child1/", pagepath, "TestType",
                    "customSite");
            entry.clearProperties();
            entry.addProperty(new PSDbMetadataProperty("dcterms:title", "title" + i + ""));
            entry.addProperty(new PSDbMetadataProperty("dcterms:created", new java.util.Date()));
            entries.add(entry);
        }
        indexer.save(entries);

        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();
        criteria.add("site='customSite'");
        criteria.add("dcterms:created > '2011-06-09T05:05:00'");
        query.setCriteria(criteria);
        query.setOrderBy("name asc");
        query.setMaxResults(5);
        query.setStartIndex(0);

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        searchResults = service.executeQuery(query);
        results = searchResults.getFirst();
        assertEquals("results list size", 5, results.size());

        totalCount = searchResults.getSecond();
        assertEquals("total entries",5,totalCount.intValue());

        PSDbMetadataEntry entry;
        for (int i = 0; i < results.size(); i++)
        {
            entry = (PSDbMetadataEntry)results.get(i);
            assertEquals("Pagepath greater than previous", pageNames[i], entry.getName());
        }
    }

    private   List<IPSMetadataEntry> createPaginationEntries()
    {
        List<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        for (int i = 0; i < ENTRY_COUNT ; i++)
        {
            String pagepath = "/testsite/folder1/child1/foo.html" + i;
            PSDbMetadataEntry entry = new PSDbMetadataEntry("foo.html" + i, "/folder1/child1/", pagepath, "TestType",
                    "customSite");
            entry.clearProperties();
            entry.addProperty(new PSDbMetadataProperty("prop1", "foo1"));
            entry.addProperty(new PSDbMetadataProperty("prop2", 4));
            entry.addProperty(new PSDbMetadataProperty("dcterms:title", "title" + i + ""));
            entries.add(entry);
        }
        return entries;

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

    private void runEntryTest(String criteriaString, int entryCountExpected,
                              PropertyValueChecker<IPSMetadataEntry> propertyValueChecker) throws Exception
    {
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add(criteriaString);

        query.setCriteria(criteria);

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();

        assertNotNull("entries not null", results);
        assertEquals("entries found", entryCountExpected, results.size());

        for (IPSMetadataEntry entry : results)
        {
            assertTrue("entry with correct value", propertyValueChecker.valueIsCorrect(entry));
        }
    }

    private void runPropertyTest(String criteriaString, int entryCountExpected, String propertyName,
                                 VALUETYPE propertyValuetypeExpected, PropertyValueChecker<Object> propertyValueChecker) throws Exception
    {
        PSMetadataQuery query = new PSMetadataQuery();
        List<String> criteria = new ArrayList<String>();

        criteria.add(criteriaString);

        query.setCriteria(criteria);

        PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
        List<IPSMetadataEntry> results = searchResults.getFirst();
        Map<String, IPSMetadataProperty> props;

        assertNotNull("entries not null", results);

        for (IPSMetadataEntry entry : results)
        {
            props = toPropsMap(entry.getProperties());

            assertTrue("entry " + propertyName + " prop present", props.containsKey(propertyName));
            assertEquals("entry " + propertyName + " prop valuetype", propertyValuetypeExpected, props
                    .get(propertyName).getValuetype());

            assertTrue("entry " + propertyName + " prop value",
                    propertyValueChecker.valueIsCorrect(props.get(propertyName).getValue()));
        }
    }

    private  int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private void addTestEntries()
    {
        Collection<IPSMetadataEntry> ents = new ArrayList<IPSMetadataEntry>();
        PSDbMetadataEntry e = null;
        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("/folderA/blogs/", "blogs linktext", getTime(getRandomNumber(2010,2021), getRandomNumber(1,12), getRandomNumber(1,28)), "blog", entryIdx++);
            ents.add(e);
        }

        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("/folderA/events/", "events linktext", getTime(getRandomNumber(2010,2021), getRandomNumber(1,12), getRandomNumber(1,28)), "event",
                    entryIdx++);
            ents.add(e);
        }

        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("/folderA/foobars/", "foobars linktext", getTime(getRandomNumber(2010,2021), getRandomNumber(1,12), getRandomNumber(1,28)), "template2", "foobar",
                    entryIdx++);
            ents.add(e);
        }

        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("customSite", "/folderA/pages/", "pages linktext", getTime(getRandomNumber(2010,2021), getRandomNumber(1,12), getRandomNumber(1,28)), "other abstract",
                    "otherTemplate", "page", entryIdx++);
            ents.add(e);
        }

        Faker faker = new Faker();

        for (int i = 0; i < ENTRY_COUNT; i++)
        {
            e = createEntry("portal", "/noticias/destacadas/noticias-destacadas-2021/",
                    faker.chuckNorris().fact(),
                    getTime(getRandomNumber(2010,2021), getRandomNumber(1,12), getRandomNumber(1,28)), faker.hitchhikersGuideToTheGalaxy().quote(),
                    "Noticias-Noticia-Single", "page", entryIdx++);
            ents.add(e);
        }

        indexer.save(ents);
    }


    private PSDbMetadataEntry createEntry(String folder, String linktext, Date date, String type, int idx)
    {
        return createEntry("testsite", folder, linktext, date, type, idx);
    }

    private PSDbMetadataEntry createEntry(String folder, String linktext, Date date, String template, String type,
                                          int idx)
    {
        return createEntry("testsite", folder, linktext, date, "a summary of the page", template, type, idx);
    }

    private PSDbMetadataEntry createEntry(String testsite, String folder, String linktext, Date date, String type,
                                          int idx)
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
        Faker faker = new Faker();

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
        String catl1 = faker.animal().name();
        String catl2 = faker.animal().name();
        entry.addProperty(new PSDbMetadataProperty("perc:category","/Categories/" + catl1));
        entry.addProperty(new PSDbMetadataProperty("perc:category","/Categories/" + catl1  +"/" + faker.animal().name()));
        entry.addProperty(new PSDbMetadataProperty("perc:category","/Categories/" + catl1 +"/" + catl2 + "/" + faker.animal().name()));
        entry.addProperty(new PSDbMetadataProperty("perc:type", type));
        entry.addProperty(new PSDbMetadataProperty("perc:reverseIndex", 10000 - idx));
        return entry;
    }

    private Map<String, IPSMetadataProperty> toPropsMap(Set<IPSMetadataProperty> props)
    {
        Map<String, IPSMetadataProperty> results = new HashMap<String, IPSMetadataProperty>();
        for (IPSMetadataProperty p : props)
        {
            results.put(p.getName(), p);
        }
        return results;
    }

    private PSMetadataRestEntry toRestMetadataEntry(IPSMetadataEntry entry)
    {
        PSMetadataRestEntry metadataEntry = new PSMetadataRestEntry();
        metadataEntry.setName(entry.getName());
        metadataEntry.setFolder(entry.getFolder());
        metadataEntry.setLinktext(entry.getLinktext());
        metadataEntry.setPagepath(entry.getPagepath());
        metadataEntry.setType(entry.getType());
        metadataEntry.setSite(entry.getSite());
        for (IPSMetadataProperty metaProperty : entry.getProperties())
        {
            metadataEntry.addMetadataProperty(metaProperty);
        }
        return metadataEntry;
    }


    /**
     * Customer query that failed to sort
     * {"criteria":["type = 'page'","dcterms:created >= '2020-06-01T00:00:00'","site = 'portal'","folder LIKE '/noticias/destacadas/noticias-destacadas-2021/%'","dcterms:source = 'Noticias-Noticia-Single'"],"maxResults":3,"totalMaxResults":500,"isEditMode":"false","orderBy":"dcterms:created desc, linktext_lower asc","returnTotalEntries":true,"startIndex":0}
     */
    @Test
    public void testSortByCreatedDateDesc() {
        PSMetadataQuery query = new PSMetadataQuery();
        ObjectMapper mapper = new ObjectMapper();
        try {
            query = mapper.readValue(
                    "{\"criteria\":[\"type = 'page'\",\"site = 'portal'\",\"folder LIKE '/noticias/destacadas/noticias-destacadas-2021/%'\",\"dcterms:source = 'Noticias-Noticia-Single'\"],\"maxResults\":5,\"totalMaxResults\":500,\"isEditMode\":\"false\",\"orderBy\":\"dcterms:created desc, linktext_lower asc\",\"returnTotalEntries\":true,\"startIndex\":0}",
                    PSMetadataQuery.class);

            PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
            List<IPSMetadataEntry> results = searchResults.getFirst();

            assertNotNull("entries not null", results);

            Date latest=null;
            //Test descending query
            for(IPSMetadataEntry e : results){
                Map<String,IPSMetadataProperty> props = toPropsMap(e.getProperties());

                Date curDate = props.get("dcterms:created").getDatevalue();

                if(latest == null) {
                    latest = curDate;
                    System.out.println("Starting date is " + latest.toString());
                }else{
                    assertTrue(latest.after(curDate));
                    System.out.println(latest.toString() + " is more recent than " + curDate.toString());
                    latest = curDate;
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            fail(e.getMessage());
        }
    }

    @Test
    public void testSortByCreatedDateAsc() {
        PSMetadataQuery query = new PSMetadataQuery();
        ObjectMapper mapper = new ObjectMapper();
        try {
            query = mapper.readValue(
                    "{\"criteria\":[\"type = 'page'\",\"site = 'portal'\",\"folder LIKE '/noticias/destacadas/noticias-destacadas-2021/%'\",\"dcterms:source = 'Noticias-Noticia-Single'\"],\"maxResults\":5,\"totalMaxResults\":500,\"isEditMode\":\"false\",\"orderBy\":\"dcterms:created asc, linktext_lower desc\",\"returnTotalEntries\":true,\"startIndex\":0}",
                    PSMetadataQuery.class);

            PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
            List<IPSMetadataEntry> results = searchResults.getFirst();

            assertNotNull("entries not null", results);

            Date latest=null;
            //Test ascending query
            for(IPSMetadataEntry e : results){
                Map<String,IPSMetadataProperty> props = toPropsMap(e.getProperties());

                Date curDate = props.get("dcterms:created").getDatevalue();

                if(latest == null) {
                    latest = curDate;
                }else{
                    assertTrue(latest.before(curDate));
                    System.out.println(latest.toString() + " is older than " + curDate.toString());
                    latest = curDate;
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            fail(e.getMessage());
        }


    }

    /**
     * Customer query that failed to sort
     * {"criteria":["type = 'page'","dcterms:created >= '2020-06-01T00:00:00'","site = 'portal'","folder LIKE '/noticias/destacadas/noticias-destacadas-2021/%'","dcterms:source = 'Noticias-Noticia-Single'"],"maxResults":3,"totalMaxResults":500,"isEditMode":"false","orderBy":"dcterms:created desc, linktext_lower asc","returnTotalEntries":true,"startIndex":0}
     */
    @Test
    public void testSortByLinkTextASC() {
        PSMetadataQuery query = new PSMetadataQuery();
        ObjectMapper mapper = new ObjectMapper();
        try {
            query = mapper.readValue(
                    "{\"criteria\":[\"type = 'page'\",\"site = 'portal'\",\"folder LIKE '/noticias/destacadas/noticias-destacadas-2021/%'\",\"dcterms:source = 'Noticias-Noticia-Single'\"],\"maxResults\":5,\"totalMaxResults\":500,\"isEditMode\":\"false\",\"orderBy\":\"linktext_lower asc\",\"returnTotalEntries\":true,\"startIndex\":0}",
                    PSMetadataQuery.class);

            PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
            List<IPSMetadataEntry> results = searchResults.getFirst();

            assertNotNull("entries not null", results);

            String latestLinkText = null;
            //Test descending query
            for(IPSMetadataEntry e : results){
                Map<String,IPSMetadataProperty> props = toPropsMap(e.getProperties());

                String linktext = e.getLinktext().toLowerCase();

                if(latestLinkText == null){
                    latestLinkText = linktext;
                    System.out.println("Starting link text is:" + latestLinkText);
                }else{
                    System.out.println(latestLinkText + " is greater than " + linktext);
                    assertTrue(latestLinkText.compareTo(linktext) <= 0);
                    latestLinkText = linktext;
                }

            }

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            fail(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            fail(e.getMessage());
        }
    }

    @Test
    public void testSortByLinkTextDesc() {
        PSMetadataQuery query = new PSMetadataQuery();
        ObjectMapper mapper = new ObjectMapper();
        try {
            query = mapper.readValue(
                    "{\"criteria\":[\"type = 'page'\",\"site = 'portal'\",\"folder LIKE '/noticias/destacadas/noticias-destacadas-2021/%'\",\"dcterms:source = 'Noticias-Noticia-Single'\"],\"maxResults\":5,\"totalMaxResults\":500,\"isEditMode\":\"false\",\"orderBy\":\"linktext_lower desc\",\"returnTotalEntries\":true,\"startIndex\":0}",
                    PSMetadataQuery.class);

            PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
            List<IPSMetadataEntry> results = searchResults.getFirst();

            assertNotNull("entries not null", results);

            String latestLinkText = null;
            //Test descending query
            for(IPSMetadataEntry e : results){
                Map<String,IPSMetadataProperty> props = toPropsMap(e.getProperties());

                String linktext = e.getLinktext().toLowerCase();

                if(latestLinkText == null){
                    latestLinkText = linktext;
                    System.out.println("Starting link text is:" + latestLinkText);
                }else{
                    System.out.println(latestLinkText + " is less than " + linktext);
                    assertTrue(latestLinkText.compareTo(linktext) >= 0);
                    latestLinkText = linktext;
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            fail(e.getMessage());
        }
    }

    /**
     * Customer query that failed to sort
     * {"criteria":["type = 'page'","dcterms:created >= '2020-06-01T00:00:00'","site = 'portal'","folder LIKE '/noticias/destacadas/noticias-destacadas-2021/%'","dcterms:source = 'Noticias-Noticia-Single'"],"maxResults":3,"totalMaxResults":500,"isEditMode":"false","orderBy":"dcterms:created desc, linktext_lower asc","returnTotalEntries":true,"startIndex":0}
     */
    @Test
    public void testBadInputs() {
        PSMetadataQuery query = new PSMetadataQuery();
        ObjectMapper mapper = new ObjectMapper();
        try {
            query = mapper.readValue(
                    "{\"criteria\":[\"type = 'page'\",\"site = 'portal'\",\"folder LIKE '/noticias/destacadas/noticias-destacadas-2021/%'\",\"dcterms:source = 'Noticias-Noticia-Single'\"],\"maxResults\":0<img src=&#x6a;&#x61;&#x76;&#x61;&#x73;&#x63;&#x72;&#x69;&#x70;&#x74;&#x3a;alert&#x28;1237&#x29;>\",\"totalMaxResults\":\"0<img src=&#x6a;&#x61;&#x76;&#x61;&#x73;&#x63;&#x72;&#x69;&#x70;&#x74;&#x3a;alert&#x28;1237&#x29;>\",\"isEditMode\":\"false\",\"orderBy\":\"dcterms:created desc, linktext_lower asc\",\"returnTotalEntries\":true,\"startIndex\":0}",
                    PSMetadataQuery.class);

            PSPair<List<IPSMetadataEntry>, Integer> searchResults = service.executeQuery(query);
            List<IPSMetadataEntry> results = searchResults.getFirst();

            assertNotNull("entries not null", results);

            Date latest=null;
            //Test descending query
            for(IPSMetadataEntry e : results){
                Map<String,IPSMetadataProperty> props = toPropsMap(e.getProperties());

                Date curDate = props.get("dcterms:created").getDatevalue();

                if(latest == null) {
                    latest = curDate;
                    System.out.println("Starting date is " + latest.toString());
                }else{
                    assertTrue(latest.after(curDate));
                    System.out.println(latest.toString() + " is more recent than " + curDate.toString());
                    latest = curDate;
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            fail(e.getMessage());
        }
    }




}






interface PropertyValueChecker<T>
{
    boolean valueIsCorrect(T currentValue);
}

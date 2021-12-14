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
package com.percussion.delivery.metadata;


import com.percussion.delivery.metadata.IPSMetadataProperty.VALUETYPE;
import com.percussion.delivery.metadata.extractor.data.PSMetadataEntry;
import com.percussion.delivery.metadata.extractor.data.PSMetadataProperty;
import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataEntry;
import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataProperty;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
public class PSMetadataIndexerServiceTest extends TestCase
{
    @Autowired
    public IPSMetadataIndexerService service;

    /**
     * Entries that will be created in performance tests.
     */
    private static final int ENTRIES_COUNT_FOR_PERFORMANCE_TESTING = 7;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        service.deleteAllMetadataEntries();
    }

    private List<IPSMetadataEntry> createEntries()
    {
        return createEntries(ENTRIES_COUNT_FOR_PERFORMANCE_TESTING);
    }
    
    private List<IPSMetadataEntry> createEntries(int count)
    {
        List<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        service.deleteAllMetadataEntries();
        for (int i = 0; i < count; i++)
        {
            String pagepath = "/testsite/folder1/child1/foo.html" + i;

            PSDbMetadataEntry entry = new PSDbMetadataEntry("foo.html" + i, "/folder1/child1/", pagepath, "TestType",
                    "testsite");
            entry.setLinktext("the linktext value");

            entry.clearProperties();
            entry.addProperty(new PSDbMetadataProperty("prop1", "foo1"));
            entry.addProperty(new PSDbMetadataProperty("prop2", 4));

            entries.add(entry);
        }

        return entries;
    }

    @Test
    public void testInsertMultiplePerformance() throws Exception
    {
        // Create entries
        List<IPSMetadataEntry> entries = createEntries();

        // Insert entries
        System.out.println("Saving...");

        Calendar before = Calendar.getInstance();
        service.save(entries);
        Calendar after = Calendar.getInstance();

        List<IPSMetadataEntry> allSavedEntries = service.getAllEntries();
        Map<String, List<IPSMetadataProperty>> props;
        assertEquals("Entries count", ENTRIES_COUNT_FOR_PERFORMANCE_TESTING, allSavedEntries.size());

        for (IPSMetadataEntry e : allSavedEntries)
        {
            assertTrue("Pagepath", e.getPagepath().startsWith("/testsite/folder1/child1/foo.html"));
            assertEquals("Linktext", "the linktext value", e.getLinktext());

            props = toPropsMap(e.getProperties());
            assertEquals("Properties count", 2, props.size());

            assertTrue("prop1 exists", props.containsKey("prop1"));
            assertEquals("prop1 value type", VALUETYPE.STRING, props.get("prop1").get(0).getValuetype());
            assertEquals("prop1 value", "foo1", props.get("prop1").get(0).getStringvalue());

            assertTrue("prop2 exists", props.containsKey("prop2"));
            assertEquals("prop2 value type", VALUETYPE.NUMBER, props.get("prop2").get(0).getValuetype());
            assertEquals("prop2 value", new Double(4), props.get("prop2").get(0).getNumbervalue());
        }

        System.out.println();
        System.out.print("Insertion took: " + ((after.getTimeInMillis() - before.getTimeInMillis()) / 1000)
                + " seconds");
        System.out.println();
    }

    @Test
    public void testUpdateMultiplePerformance() throws Exception
    {
        // Create entries
        List<IPSMetadataEntry> entries = createEntries();

        // Insert entries
        System.out.println("Saving new entries");
        service.save(entries);

        Map<String, List<IPSMetadataProperty>> props;

        // Create the same entries (with the same pagepath), modify any property
        // and save them
        entries = createEntries();

        for (IPSMetadataEntry e : entries)
        {
            e.setLinktext("New value for linktext");

            e.addProperty(new PSDbMetadataProperty("prop3", new Date(2011, 01, 28)));
        }

        System.out.println("Updating entries");

        Calendar before = Calendar.getInstance();
        service.save(entries);
        Calendar after = Calendar.getInstance();

        List<IPSMetadataEntry> allSavedEntries = service.getAllEntries();
        assertEquals("Entries count", ENTRIES_COUNT_FOR_PERFORMANCE_TESTING, allSavedEntries.size());

        for (IPSMetadataEntry e : allSavedEntries)
        {
            assertTrue("Pagepath", e.getPagepath().startsWith("/testsite/folder1/child1/foo.html"));
            assertEquals("Linktext", "New value for linktext", e.getLinktext());

            props = toPropsMap(e.getProperties());
            assertEquals("Properties count", 3, props.size());

            assertTrue("prop1 exists", props.containsKey("prop1"));
            assertEquals("prop1 value type", VALUETYPE.STRING, props.get("prop1").get(0).getValuetype());
            assertEquals("prop1 value", "foo1", props.get("prop1").get(0).getStringvalue());

            assertTrue("prop2 exists", props.containsKey("prop2"));
            assertEquals("prop2 value type", VALUETYPE.NUMBER, props.get("prop2").get(0).getValuetype());
            assertEquals("prop2 value", new Double(4), props.get("prop2").get(0).getNumbervalue());

            assertTrue("prop3 exists", props.containsKey("prop3"));
            assertEquals("prop3 value type", VALUETYPE.DATE, props.get("prop3").get(0).getValuetype());
            assertEquals("prop3 value", new Date(2011, 01, 28), props.get("prop3").get(0).getDatevalue());
        }

        System.out.println();
        System.out.print("Update took: " + ((after.getTimeInMillis() - before.getTimeInMillis()) / 1000) + " seconds");
        System.out.println();
    }

    @Test
    public void testDeleteMultiplePerformance() throws Exception
    {
        // Create entries
        List<IPSMetadataEntry> entries = createEntries();

        // Insert entries
        service.save(entries);

        List<IPSMetadataEntry> allSavedEntries = service.getAllEntries();

        Collection<String> entriesToDelete = new ArrayList<String>();
        for (IPSMetadataEntry e : allSavedEntries)
            entriesToDelete.add(e.getPagepath());

        assertEquals("Entries count", ENTRIES_COUNT_FOR_PERFORMANCE_TESTING, entriesToDelete.size());

        Calendar before = Calendar.getInstance();
        service.delete(entriesToDelete);
        Calendar after = Calendar.getInstance();

        assertEquals("Entries count after deletion", 0, service.getAllEntries().size());

        System.out.println();
        System.out
                .print("Deletion took: " + ((after.getTimeInMillis() - before.getTimeInMillis()) / 1000) + " seconds");
        System.out.println();
    }    

    @Test
    public void testUpdateMultiple_RemoveAllProperties() throws Exception
    {
        int entriesCount = 10;
        
        // Create entries
        List<IPSMetadataEntry> entries = createEntries(entriesCount);
        service.save(entries);
        entries = service.getAllEntries();
        
        Map<String, List<IPSMetadataProperty>> props;
        
        // Create the same entries (with the same pagepath), modify any property and save them
        entries = createEntries(entriesCount);
        
        for (IPSMetadataEntry e : entries)
        {
            e.setLinktext("New value for linktext");
            
            e.clearProperties();
        }
        
        service.save(entries);
        
        // Assert
        List<IPSMetadataEntry> allSavedEntries = service.getAllEntries();
        assertEquals("Entries count", entriesCount, allSavedEntries.size());
        
        for (IPSMetadataEntry e : allSavedEntries)
        {
            assertTrue("Pagepath", e.getPagepath().startsWith("/testsite/folder1/child1/foo.html"));
            assertEquals("Linktext", "New value for linktext", e.getLinktext());
            
            props = toPropsMap(e.getProperties());
            assertEquals("Properties count", 0, props.size());
        }
    }

    @Test
    public void testUpdateMultiple_ChangeExistingProperty() throws Exception
    {
        int entriesCount = 10;
        
        // Create entries
        List<IPSMetadataEntry> entries = createEntries(entriesCount);
        service.save(entries);
        
        Map<String, List<IPSMetadataProperty>> props;
        
        // Create the same entries (with the same pagepath), modify any property and save them
        entries = createEntries(entriesCount);
        
        for (IPSMetadataEntry e : entries)
        {
            e.setLinktext("New value for linktext");
            
            for (IPSMetadataProperty prop : e.getProperties())
            {
                if (prop.getName().equals("prop1"))
                    prop.setStringvalue(prop.getStringvalue() + "changed");
                else
                    prop.setNumbervalue(10.0);
            }
        }
        
        service.save(entries);
        
        // Assert
        List<IPSMetadataEntry> allSavedEntries = service.getAllEntries();
        assertEquals("Entries count", entriesCount, allSavedEntries.size());
        
        for (IPSMetadataEntry e : allSavedEntries)
        {
            assertTrue("Pagepath", e.getPagepath().startsWith("/testsite/folder1/child1/foo.html"));
            assertEquals("Linktext", "New value for linktext", e.getLinktext());
            
            props = toPropsMap(e.getProperties());
            assertEquals("Properties count", 2, props.size());
            
            assertTrue("prop1 exists", props.containsKey("prop1"));
            assertEquals("prop1 value type", VALUETYPE.STRING, props.get("prop1").get(0).getValuetype());
            assertEquals("prop1 value", "foo1changed", props.get("prop1").get(0).getStringvalue());
            
            assertTrue("prop2 exists", props.containsKey("prop2"));
            assertEquals("prop2 value type", VALUETYPE.NUMBER, props.get("prop2").get(0).getValuetype());
            assertEquals("prop2 value", new Double(10), props.get("prop2").get(0).getNumbervalue());
        }
    }

    @Test
    public void testUpdateWith_RemoveExistingProperty() throws Exception
    {
        int entriesCount = 1;

        // Create entries
        List<IPSMetadataEntry> entries = createEntries(entriesCount);
        service.save(entries);
        List<IPSMetadataEntry> allSavedEntries = service.getAllEntries();
        assertEquals("Entries count", entriesCount,allSavedEntries.size());

        Map<String, List<IPSMetadataProperty>> props;


        for (IPSMetadataEntry e : allSavedEntries)
        {
            e.setLinktext("New value for linktext");

            Set<IPSMetadataProperty> newProps = new HashSet<>();
            for (IPSMetadataProperty prop : e.getProperties())
            {
                //Don't add prop1 again, just add prop2 with changed value
                if (prop.getName().equals("prop1")) {
                } else{
                    prop.setNumbervalue(10.0);
                    newProps.add(prop);
                }
            }
            //Setting new list of properties
            e.setProperties(newProps);
        }

        service.save(entries);

        // Assert

        assertEquals("Entries count", entriesCount, allSavedEntries.size());

        for (IPSMetadataEntry e : allSavedEntries)
        {

            assertTrue("Pagepath", e.getPagepath().startsWith("/testsite/folder1/child1/foo.html"));
            assertEquals("Linktext", "New value for linktext", e.getLinktext());

            //Removed property should not be returned
            Set<IPSMetadataProperty> propsN = e.getProperties();
            assertEquals("Properties count", 1, propsN.size());
        }
    }


    @Test
    public void testSave_MultipleValueProperties() throws Exception
    {
        // Insert entry
        String pagepath = "/testsite/folder1/child1/foo.html";
        IPSMetadataEntry entry = new PSDbMetadataEntry("foo.html", "/folder1/child1", pagepath, "TestType", "testsite");
        PSDbMetadataProperty prop1 = new PSDbMetadataProperty("prop1", "foo1");
        entry.addProperty(prop1);
        PSDbMetadataProperty prop2 = new PSDbMetadataProperty("prop2", 4);
        entry.addProperty(prop2);
        // prop1 == prop3
        PSDbMetadataProperty prop3 = new PSDbMetadataProperty("prop3", "foo2");
        entry.addProperty(prop3);

        service.save(entry);

        // find entry
        entry = service.findEntry(pagepath);
        assertNotNull(entry);
        assertNotNull(entry.getProperties());
        assertEquals(3, entry.getProperties().size());
        
        Map<String, List<IPSMetadataProperty>> propsMap = toPropsMap(entry.getProperties());
        assertEquals("prop1 count", 1, propsMap.get("prop1").size());
        
        List<String> expectedProp1Values = new ArrayList<String>();
        expectedProp1Values.add("foo1");
        expectedProp1Values.add("foo2");
        
        for (IPSMetadataProperty pro : propsMap.get("prop1"))
        {
            assertTrue("expected prop1 value", expectedProp1Values.contains(pro.getStringvalue()));
            expectedProp1Values.remove(pro.getStringvalue());
        }
        
        assertEquals("prop2 expected value", new Double(4), propsMap.get("prop2").get(0).getNumbervalue());

        // Update
        entry.setName("TestEntry1_Modified");
        entry.clearProperties();
        entry.addProperty(prop1);
        entry.addProperty(new PSDbMetadataProperty("prop2", 66));
        entry.addProperty(new PSDbMetadataProperty("prop3", 77));

        service.save(entry);
        
        IPSMetadataEntry updatedEntry = service.findEntry(pagepath);
        assertNotNull(updatedEntry);
        assertNotNull(updatedEntry.getProperties());
        assertEquals(3, updatedEntry.getProperties().size());

        propsMap = toPropsMap(updatedEntry.getProperties());
        assertEquals("TestEntry1_Modified", updatedEntry.getName());
        assertEquals(new Double(66), propsMap.get("prop2").get(0).getNumbervalue());
        assertEquals("foo1", propsMap.get("prop1").get(0).getStringvalue());
        assertEquals(new Double(77), propsMap.get("prop3").get(0).getNumbervalue());

        // Delete
        service.delete(pagepath);
        assertNull(service.findEntry(pagepath));
    }

    @Test
    public void testSave_BigTextValue() throws Exception
    {
        // Insert entry
        String pagepath = "/testsite/folder1/child1/foo.html";
        IPSMetadataEntry entry = new PSDbMetadataEntry("foo.html", "/folder1/child1", pagepath, "TestType", "testsite");
        entry.setLinktext("link text 1");
        
        // Very big string
        StringBuilder sb = new StringBuilder();
        
        for (int i=0; i<10000; i++)
            sb.append("a");
        
        String stringValue = new String(sb.toString());
        
        PSDbMetadataProperty prop1 = new PSDbMetadataProperty("prop1", VALUETYPE.TEXT, sb.toString());
        entry.addProperty(prop1);

        service.save(entry);
        
        IPSMetadataEntry dbEntry = service.findEntry(pagepath);
        assertNotNull(dbEntry);
        assertEquals("dbEntry - name", "foo.html", dbEntry.getName());
        assertEquals("dbEntry - folder", "/folder1/child1", dbEntry.getFolder());
        assertEquals("dbEntry - type", "TestType", dbEntry.getType());
        assertEquals("dbEntry - type", "testsite", dbEntry.getSite());
        assertEquals("dbEntry - linktext", "link text 1", dbEntry.getLinktext());
        
        assertNotNull("dbEntry - properties", dbEntry.getProperties());
        Map<String, List<IPSMetadataProperty>> props = toPropsMap(dbEntry.getProperties());
        assertEquals("dbEntry - properties size", 1, dbEntry.getProperties().size());
        
        assertTrue("prop1 exists", props.containsKey("prop1"));
        assertEquals("prop1 value type", VALUETYPE.TEXT, props.get("prop1").get(0).getValuetype());
        assertEquals("prop1 value", sb.toString(), props.get("prop1").get(0).getStringvalue());
    }
    
    @Test
    public void testSaveDeleteSingle() throws Exception
    {
        // Insert entry
        String pagepath = "/testsite/folder1/child1/foo.html";
        IPSMetadataEntry entry = new PSDbMetadataEntry("foo.html", "/folder1/child1", pagepath, "TestType", "testsite");
        PSDbMetadataProperty prop1 = new PSDbMetadataProperty("prop1", "foo1");
        entry.addProperty(prop1);
        PSDbMetadataProperty prop2 = new PSDbMetadataProperty("prop2", 4);
        entry.addProperty(prop2);

        service.save(entry);

        // find entry
        entry = service.findEntry(pagepath);
        assertNotNull(entry);
        assertNotNull(entry.getProperties());
        assertEquals(2, entry.getProperties().size());

        // Update
        entry.setName("TestEntry1_Modified");
        entry.clearProperties();
        entry.addProperty(prop1);
        entry.addProperty(new PSDbMetadataProperty("prop2", 66));
        entry.addProperty(new PSDbMetadataProperty("prop3", 77));

        service.save(entry);

        // Delete
        IPSMetadataEntry updatedEntry = service.findEntry(pagepath);
        assertNotNull(updatedEntry);
        assertNotNull(updatedEntry.getProperties());
        assertEquals("Property count wrong",3, updatedEntry.getProperties().size());

        Map<String, List<IPSMetadataProperty>> propsMap = toPropsMap(updatedEntry.getProperties());
        assertEquals("TestEntry1_Modified", updatedEntry.getName());
        assertEquals(new Double(66), propsMap.get("prop2").get(0).getNumbervalue());
        assertEquals("foo1", propsMap.get("prop1").get(0).getStringvalue());
        assertEquals(new Double(77), propsMap.get("prop3").get(0).getNumbervalue());

        // Delete
        service.delete(pagepath);
        assertNull(service.findEntry(pagepath)); // Verify entry no longer
                                                 // exists
    }

    @Test
    public void testSaveDeleteMultiple_WithDatabaseEntities() throws Exception
    {
        Collection<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        String pagepath = "/testsite/folder1/child1/foo.html";
        PSDbMetadataEntry entry = new PSDbMetadataEntry("foo.html", "/folder1/child1", pagepath, "TestType", "testsite");
        PSDbMetadataProperty prop1 = new PSDbMetadataProperty("prop1", "foo1");
        entry.addProperty(prop1);
        PSDbMetadataProperty prop2 = new PSDbMetadataProperty("prop2", 4);
        entry.addProperty(prop2);
        entries.add(entry);

        String pagepath2 = "/testsite/folder1/child1/foo2.html";
        PSDbMetadataEntry entry2 = new PSDbMetadataEntry("foo2.html", "/folder1/child1", pagepath2, "TestType", "testsite");
        PSDbMetadataProperty prop3 = new PSDbMetadataProperty("prop3", "foo12");
        entry.addProperty(prop3);
        PSDbMetadataProperty prop4 = new PSDbMetadataProperty("prop4", 4);
        entry.addProperty(prop4);
        entries.add(entry2);

        // Insert entries
        service.save(entries);

        assertNotNull(service.findEntry(pagepath));
        assertNotNull(service.findEntry(pagepath2));

        // Delete
        Collection<String> deleteList = new ArrayList<String>();
        deleteList.add(pagepath);
        deleteList.add(pagepath2);
        service.delete(deleteList);
        assertNull(service.findEntry(pagepath));
        assertNull(service.findEntry(pagepath2));

    }
    
    @Test
    public void testSaveDeleteMultiple_PSMetadataEntries() throws Exception
    {
        Collection<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        
        String pagepath = "/testsite/folder1/child1/foo.html";
        PSMetadataEntry entry = new PSMetadataEntry("foo.html", "/folder1/child1", pagepath, "TestType", "testsite1");
        entry.setLinktext("link text 1");
        PSMetadataProperty prop1 = new PSMetadataProperty("prop1", "foo1");
        entry.getProperties().add(prop1);
        PSMetadataProperty prop2 = new PSMetadataProperty("prop2", 4);
        entry.getProperties().add(prop2);
        entries.add(entry);

        String pagepath2 = "/testsite/folder1/child1/foo2.html";
        PSMetadataEntry entry2 = new PSMetadataEntry("foo2.html", "/folder1/child1", pagepath2, "TestType", "testsite2");
        entry2.setLinktext("link text 2");
        PSMetadataProperty prop3 = new PSMetadataProperty("prop3", "foo12");
        entry2.getProperties().add(prop3);
        PSMetadataProperty prop4 = new PSMetadataProperty("prop4", 5);
        entry2.getProperties().add(prop4);
        entries.add(entry2);

        // Insert entries
        service.save(entries);

        // Check entry1
        PSDbMetadataEntry dbEntry = (PSDbMetadataEntry)service.findEntry(pagepath);
        assertNotNull(dbEntry);
        assertEquals("dbEntry - name", "foo.html", dbEntry.getName());
        assertEquals("dbEntry - folder", "/folder1/child1", dbEntry.getFolder());
        assertEquals("dbEntry - type", "TestType", dbEntry.getType());
        assertEquals("dbEntry - type", "testsite1", dbEntry.getSite());
        assertEquals("dbEntry - linktext", "link text 1", dbEntry.getLinktext());
        
        assertNotNull("dbEntry - properties", dbEntry.getProperties());
        Map<String, List<IPSMetadataProperty>> props = toPropsMap(dbEntry.getProperties());
        assertEquals("dbEntry - properties size", 2, dbEntry.getProperties().size());
        
        assertTrue("prop1 exists", props.containsKey("prop1"));
        assertEquals("prop1 value type", VALUETYPE.STRING, props.get("prop1").get(0).getValuetype());
        assertEquals("prop1 value", "foo1", props.get("prop1").get(0).getStringvalue());
        
        assertTrue("prop2 exists", props.containsKey("prop2"));
        assertEquals("prop2 value type", VALUETYPE.NUMBER, props.get("prop2").get(0).getValuetype());
        assertEquals("prop2 value", 4.0, props.get("prop2").get(0).getNumbervalue());
        
        // Check entry2
        dbEntry = (PSDbMetadataEntry)service.findEntry(pagepath2);
        assertNotNull(dbEntry);
        assertEquals("dbEntry - name", "foo2.html", dbEntry.getName());
        assertEquals("dbEntry - folder", "/folder1/child1", dbEntry.getFolder());
        assertEquals("dbEntry - type", "TestType", dbEntry.getType());
        assertEquals("dbEntry - type", "testsite2", dbEntry.getSite());
        assertEquals("dbEntry - linktext", "link text 2", dbEntry.getLinktext());
        
        assertNotNull("dbEntry - properties", dbEntry.getProperties());
        props = toPropsMap(dbEntry.getProperties());
        assertEquals("dbEntry - properties size", 2, dbEntry.getProperties().size());
        
        assertTrue("prop3 exists", props.containsKey("prop3"));
        assertEquals("prop3 value type", VALUETYPE.STRING, props.get("prop3").get(0).getValuetype());
        assertEquals("prop3 value", "foo12", props.get("prop3").get(0).getStringvalue());
        
        assertTrue("prop4 exists", props.containsKey("prop4"));
        assertEquals("prop4 value type", VALUETYPE.NUMBER, props.get("prop4").get(0).getValuetype());
        assertEquals("prop4 value", 5.0, props.get("prop4").get(0).getNumbervalue());
        

        // Delete
        Collection<String> deleteList = new ArrayList<String>();
        deleteList.add(pagepath);
        deleteList.add(pagepath2);
        service.delete(deleteList);
        assertNull(service.findEntry(pagepath));
        assertNull(service.findEntry(pagepath2));
    }    
    
    @Test
    public void testDeleteEntriesWithProperties_ShouldDeletePropertiesAsWell() throws Exception
    {
        Collection<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        
        // entry
        String pagepath = "/testsite/folder1/child1/foo.html";
        IPSMetadataEntry entry = new PSMetadataEntry();
        entry.setName("foo.html");
        entry.setFolder("/folder1/child1");
        entry.setPagepath(pagepath);
        entry.setType("TestType");
        entry.setSite("testsite1");
        entry.setLinktext("link text 1");
        
        IPSMetadataProperty prop1 = new PSMetadataProperty("prop1", VALUETYPE.STRING, "foo1");        
        entry.getProperties().add(prop1);
        
        PSMetadataProperty prop2 = new PSMetadataProperty("prop2", 4.0);
        entry.getProperties().add(prop2);
        
        entries.add(entry);

        // entry2
        String pagepath2 = "/testsite/folder1/child1/foo2.html";
        
        PSMetadataEntry entry2 = new PSMetadataEntry();
        entry2.setName("foo2.html");
        entry2.setFolder("/folder1/child1");
        entry2.setPagepath(pagepath2);
        entry2.setType("TestType");
        entry2.setSite("testsite2");
        entry2.setLinktext("link text 2");
        
        PSMetadataProperty prop3 = new PSMetadataProperty("prop3", VALUETYPE.STRING, "foo12");        
        entry2.getProperties().add(prop3);
        
        PSMetadataProperty prop4 = new PSMetadataProperty("prop4", 5.0);
        entry2.getProperties().add(prop4);
        
        entries.add(entry2);

        // Insert entries
        service.save(entries);

        // Delete entry1 and make sure that the only properties left are
        // the ones for the second entry
        
        List<IPSMetadataProperty> allProperties = getAllProperties();
        
        assertNotNull("properties not null", allProperties);
        assertEquals("count of properties before deleting entry", 4, allProperties.size());
        
        service.delete(entry.getPagepath());
        
        allProperties = getAllProperties();
        
        assertNotNull("properties not null", allProperties);
        assertEquals("count of properties before deleting entry", 2, allProperties.size());
        
        for (IPSMetadataProperty prop : allProperties)
            assertEquals("entry of property", entry2.getPagepath(), ((PSDbMetadataProperty)prop).getMetadataEntry().getPagepath());
    }

    private List<IPSMetadataProperty> getAllProperties()
    {
        List<IPSMetadataProperty> allProperties = new ArrayList<IPSMetadataProperty>();
        List<IPSMetadataEntry> allEntries = service.getAllEntries();
        
        for (IPSMetadataEntry en : allEntries)
            allProperties.addAll(en.getProperties());

        return allProperties;
    }    
    
    @Test
    public void testGetAllIndexedDirectories() throws Exception
    {
        Collection<IPSMetadataEntry> entries = new ArrayList<IPSMetadataEntry>();
        String pagepath = "/testsite/folder1/child1/foo.html";
        IPSMetadataEntry entry = new PSDbMetadataEntry("foo.html", "/folder1/child1", pagepath, "TestType", "testsite");
        PSDbMetadataProperty prop1 = new PSDbMetadataProperty("prop1", "foo1");
        PSDbMetadataProperty prop2 = new PSDbMetadataProperty("prop2", 4);
        Set<IPSMetadataProperty> props = new HashSet<IPSMetadataProperty>();
        props.add(prop1);
        props.add(prop2);
        entry.setProperties(props);
        entries.add(entry);

        String pagepath2 = "/testsite2/folder2/child2/foo2.html";
        PSDbMetadataEntry entry2 = new PSDbMetadataEntry("foo2.html", "/folder2/child2", pagepath2, "TestType", "testsite2");
        PSDbMetadataProperty prop3 = new PSDbMetadataProperty("prop3", "foo12");
        PSDbMetadataProperty prop4 = new PSDbMetadataProperty("prop4", 4);
        Set<IPSMetadataProperty> props2 = new HashSet<IPSMetadataProperty>();
        props2.add(prop3);
        props2.add(prop4);
        entry.setProperties(props2);
        entries.add(entry2);

        String pagepath3 = "/testsite2/folder2/foo2.html";
        PSDbMetadataEntry entry3 = new PSDbMetadataEntry("foo2.html", "/folder2", pagepath3, "TestType", "testsite2");
        PSDbMetadataProperty prop5 = new PSDbMetadataProperty("prop3", "foo12");
        PSDbMetadataProperty prop6 = new PSDbMetadataProperty("prop4", 4);
        Set<IPSMetadataProperty> props3 = new HashSet<IPSMetadataProperty>();
        props2.add(prop5);
        props2.add(prop6);
        entry.setProperties(props3);
        entries.add(entry3);

        String pagepath4 = "/testsite2/folder2/foo3.html";
        PSDbMetadataEntry entry4 = new PSDbMetadataEntry("foo2.html", "/folder2", pagepath4, "TestType", "testsite2");
        PSDbMetadataProperty prop7 = new PSDbMetadataProperty("prop3", "foo12");
        PSDbMetadataProperty prop8 = new PSDbMetadataProperty("prop4", 4);
        Set<IPSMetadataProperty> props4 = new HashSet<IPSMetadataProperty>();
        props2.add(prop7);
        props2.add(prop8);
        entry.setProperties(props4);
        entries.add(entry4);

        // Insert entries
        service.save(entries);
        assertNotNull(service.findEntry(pagepath));
        assertNotNull(service.findEntry(pagepath2));

        // Get all indexed directories
        Set<String> indexedDirectories = service.getAllIndexedDirectories();
        assertEquals("Directories count", 3, indexedDirectories.size());
        assertTrue("Directory name - 1", indexedDirectories.contains("/testsite/folder1/child1"));
        assertTrue("Directory name - 2", indexedDirectories.contains("/testsite2/folder2/child2"));
        assertTrue("Directory name - 3", indexedDirectories.contains("/testsite2/folder2"));
    }
    
   
    private Map<String, List<IPSMetadataProperty>> toPropsMap(Set<IPSMetadataProperty> props)
    {
        Map<String, List<IPSMetadataProperty>> results = new HashMap<String, List<IPSMetadataProperty>>();
        List<IPSMetadataProperty> list;
        
        for (IPSMetadataProperty p : props)
        {
            if (results.containsKey(p.getName()))
                list = results.get(p.getName());
            else
            {
                list = new ArrayList<IPSMetadataProperty>();
                results.put(p.getName(), list);
            }
            
            list.add(p);
        }

        return results;
    }
    
    
}

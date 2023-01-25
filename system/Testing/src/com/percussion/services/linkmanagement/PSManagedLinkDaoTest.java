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
package com.percussion.services.linkmanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.percussion.services.linkmanagement.data.PSManagedLink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSManagedLinkDaoTest
{
    private IPSManagedLinkDao m_dao;
    private Set<PSManagedLink> m_savedLinks;
   

    @Before
    public void setUp() throws Exception
    {
        m_dao = PSManagedLinkDaoLocator.getManagedLinkDao();
        m_savedLinks = new HashSet<PSManagedLink>();
    }

    @After
    public void tearDown() throws Exception
    {
        // delete all links we created
        for (PSManagedLink link : m_savedLinks)
        {
            m_dao.deleteLink(link);
        }
    }
    
    @Test
    public void testCreateLink() throws Exception
    {
        int parentId = 1;
        int parentRev = 1;
        int childId = 2;
        String anchor = "TARGET";
        
        PSManagedLink link = m_dao.createLink(parentId, parentRev, childId, anchor);
        assertNotNull(link);
        assertEquals(parentId, link.getParentId());
        assertEquals(parentRev, link.getParentRevision());
        assertEquals(childId, link.getChildId());
        assertEquals(-1L, link.getLinkId());
    }
    
    @Test
    public void testSaveLink() throws Exception
    {
        int parentId = 1;
        int parentRev = 1;
        int childId = 2;
        
        PSManagedLink link = createLink(parentId, parentRev, childId);
        
        PSManagedLink found = m_dao.findLinkByLinkId(link.getLinkId());
        assertNotNull(found);
        assertEquals(link.getLinkId(), found.getLinkId());
        assertEquals(link.getParentId(), found.getParentId());
        assertEquals(link.getParentRevision(), found.getParentRevision());
        assertEquals(link.getChildId(), found.getChildId());
    }

    private PSManagedLink createLink(int parentId, int parentRev, int childId) throws Exception
    {
        PSManagedLink link = m_dao.createLink(parentId, parentRev, childId, null);
        m_dao.saveLink(link);
        m_savedLinks.add(link);
        assertTrue(link.getLinkId() != -1);
        return link;
    }
    
    public void testFindLinksByParent() throws Exception
    {
        int parentId1 = 1;
        int parentRev1 = 1;
        int childId = 2;
        List<Long> parent1LinkIds = new ArrayList<Long>();
        
        while (childId < 10)
        {
            parent1LinkIds.add(createLink(parentId1, parentRev1, childId++).getLinkId());
        }
        
        int parentId2 = 2;
        int parentRev2 = 2;        
        childId = 5;
        List<Long> parent2LinkIds = new ArrayList<Long>();
        while (childId < 15)
        {
            parent2LinkIds.add(createLink(parentId2, parentRev2, childId++).getLinkId());
        }
        
        List<Long> foundLinkIds = new ArrayList<Long>();
        List<PSManagedLink> foundLinks = m_dao.findLinksByParentId(parentId1);
        for (PSManagedLink link : foundLinks)
        {
            assertEquals(parentId1, link.getParentId());
            foundLinkIds.add(link.getLinkId());
        }
        assertEquals(parent1LinkIds.size(), foundLinkIds.size());
        assertTrue(foundLinkIds.containsAll(parent1LinkIds));
        
        
        foundLinkIds.clear();
        foundLinks = m_dao.findLinksByParentId(parentId2);
        for (PSManagedLink link : foundLinks)
        {
            assertEquals(parentId2, link.getParentId());
            foundLinkIds.add(link.getLinkId());
        }
        assertEquals(parent2LinkIds.size(), foundLinkIds.size());
        assertTrue(foundLinkIds.containsAll(parent2LinkIds));
    }
    
    public void testDeleteLink() throws Exception
    {
        int parentId = 1;
        int parentRev = 1;
        int childId = 11;
        List<PSManagedLink> links = new ArrayList<PSManagedLink>();
        
        while (parentId < 10)
        {
            links.add(createLink(parentId++, parentRev, childId++));
        }
        
        for (PSManagedLink link : links)
        {
            assertNotNull(m_dao.findLinkByLinkId(link.getLinkId()));
            m_dao.deleteLink(link);
            assertTrue(m_dao.findLinkByLinkId(link.getLinkId()) == null);
        }
        
    }
    
    public void setLinkDao(IPSManagedLinkDao dao)
    {
        m_dao = dao;
    }
}

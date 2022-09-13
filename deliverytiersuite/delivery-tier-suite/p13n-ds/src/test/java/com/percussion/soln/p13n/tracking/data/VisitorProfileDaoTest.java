/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.soln.p13n.tracking.data;

import static integrationtest.spring.SpringSetup.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.impl.VisitorTrackingService;

public class VisitorProfileDaoTest {
    
    private static IVisitorProfileDataService dao;

    @BeforeClass
    public static void setUp() throws Exception {
        loadXmlBeanFiles("file:ds/webapp/WEB-INF/applicationContext.xml",
                "file:ds/webapp/WEB-INF/spring/ds/*.xml");
        VisitorTrackingService vt = getBean("visitorTrackingService", VisitorTrackingService.class);
        dao = vt.getVisitorProfileDataService();
    }

    @Test
    public void testFindAndSave() {
        VisitorProfile profile = new VisitorProfile();
        profile.setId(100);
        Map<String, Integer> weights = new HashMap<String, Integer>();
        weights.put("1", 10);
        weights.put("2", 20);
        weights.put("//a", 100);
        profile.setSegmentWeights(weights);
        dao.save(profile);
        VisitorProfile p = dao.find(100);
        assertNotNull(p);
        assertEquals(weights,profile.getSegmentWeights());
        dao.delete(p);
        assertNull(dao.find(100));
    }
    
    @Test
    public void testSaveNew() {
        VisitorProfile profile = new VisitorProfile();
        profile.setLockProfile(true);
        profile = dao.save(profile);
        assertTrue(profile.getId() != 0L);
        assertTrue("Profile should be locked", profile.isLockProfile());
        dao.delete(profile);
    }
    
    @Test
    public void testSaveNewWithEverythingSet() {
        VisitorProfile profile = new VisitorProfile();
        profile.setLockProfile(true);
        profile.setLastUpdated(new Date());
        profile.setLockProfile(true);
        profile.setLabel("Test label");
        profile.setUserId("test userid");
        profile = dao.save(profile);
        assertTrue(profile.getId() != 0L);
        assertTrue("Profile should be locked", profile.isLockProfile());
        dao.delete(profile);
    }
    
    
    @Test
    public void testFindByUserId() {
        VisitorProfile profile = new VisitorProfile();
        profile.setUserId("TEST_USERID");
        profile = dao.save(profile);
        assertTrue(profile.getId() != 0L);
        VisitorProfile actual = dao.findByUserId("TEST_USERID");
        assertEquals("TEST_USERID", actual.getUserId());
    }
    
    

}

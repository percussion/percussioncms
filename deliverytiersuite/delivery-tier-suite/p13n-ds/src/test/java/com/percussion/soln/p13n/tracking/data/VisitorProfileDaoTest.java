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

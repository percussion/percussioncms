package com.percussion.soln.p13n.tracking;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.percussion.soln.p13n.tracking.VisitorProfile;

public class VisitorProfileTest {

    VisitorProfile profileA;
    VisitorProfile profileB;
    VisitorProfile profileC;
    
    @Before
    public void setUp() throws Exception {
        Date date = new Date();
        profileA = new VisitorProfile();
        profileA.setId(1);
        profileA.setLabel("a");
        profileA.setLastUpdated(date);
        profileA.setLockProfile(false);
        profileA.setUserId("a");
        HashMap<String, Integer> aWeights = new HashMap<String, Integer>();
        aWeights.put("seg1", 1);
        profileA.setSegmentWeights(aWeights);
        
        profileC = new VisitorProfile();
        profileC.setId(1);
        profileC.setLabel("a");
        profileC.setLastUpdated(date);
        profileC.setLockProfile(false);
        profileC.setUserId("a");
        HashMap<String, Integer> cWeights = new HashMap<String, Integer>();
        cWeights.put("seg1", 1);
        profileC.setSegmentWeights(cWeights);
        
        profileB = new VisitorProfile();
        profileB.setId(2);
        
        
    }

    @Test
    public void testClone() throws Exception {
        VisitorProfile clonedProfile = profileA.clone();
        assertEquals("clone should work with equals", clonedProfile, profileA);
        assertEquals("Weights should be equal", 
                clonedProfile.getSegmentWeights(), profileA.getSegmentWeights());
    }

    @Test
    public void testEqualsObject() {
        assertFalse("a != b ", profileA.equals(profileB));
        assertTrue("a == c", profileA.equals(profileC));
        assertTrue("c == a", profileC.equals(profileA));
        profileC.setLabel("c");
        assertFalse("c != a", profileC.equals(profileA));
    }

}

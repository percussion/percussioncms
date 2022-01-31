package com.percussion.sitemanage.task.impl;

import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.data.PSSite;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PSSiteMapGeneratorTaskTests {

    @Test
    public void testCanonicalUrl(){
        IPSSite testSite = (IPSSite) new PSSite();
        testSite.setDefaultDocument("index.html");
        testSite.setCanonical(true);
        testSite.setCanonicalDist("sections");

        String result = PSSiteMapGeneratorTask.getCanonicalLocation(testSite,"/section1/index.html");

        assertEquals("/section1/", result);

        testSite.setCanonical(false);
        result = PSSiteMapGeneratorTask.getCanonicalLocation(testSite,"/section1/index.html");
        assertEquals("/section1/index.html", result);

        testSite.setCanonical(true);
        testSite.setCanonicalDist("pages");
        result = PSSiteMapGeneratorTask.getCanonicalLocation(testSite,"/section1/index.html");
        assertEquals("/section1/index.html", result);

        testSite.setCanonical(true);
        testSite.setCanonicalDist("sections");
        testSite.setDefaultDocument("index");
        result = PSSiteMapGeneratorTask.getCanonicalLocation(testSite,"/section1/index.html");
        assertEquals("/section1/index.html", result);

        result = PSSiteMapGeneratorTask.getCanonicalLocation(testSite,"/section1/index");
        assertEquals("/section1/", result);


    }
}

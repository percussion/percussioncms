package com.percussion.patch.test;

import com.percussion.linkmanagement.service.IPSManagedLinkService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PSSaveAssetsMainetanceProcessUT {

    @Test
    public void testTarget(){
        Document doc = Jsoup.parseBodyFragment("<p>This is <a href=\"#\" target=\"_blank\"/>");
        Elements targetAnchors = doc.select(IPSManagedLinkService.A_HREF + "a[target=\"_blank\"]"
                + ":not(a[rel=\"noopener noreferrer\"])");

        assertFalse(targetAnchors.isEmpty());

        doc = Jsoup.parseBodyFragment("<p>This is <a href=\"#\" target=\"_blank\" rel=\"noopener noreferrer\" />");
        targetAnchors = doc.select(IPSManagedLinkService.A_HREF + "a[target=\"_blank\"]"
                + ":not(a[rel=\"noopener noreferrer\"])");

        assertTrue(targetAnchors.isEmpty());

    }
}

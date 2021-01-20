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

package com.percussion.delivery;

import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.PSMetadataExtractorService;
import com.percussion.delivery.metadata.any23.PSTripleHandler;
import com.percussion.delivery.metadata.extractor.data.PSMetadataEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PSMetadataExtractorServiceTests {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testgetNamespace(){
        PSMetadataExtractorService svc = new PSMetadataExtractorService();

        PSTripleHandler handler = new PSTripleHandler();

        handler.getNamespace("");
    }

    @Test
    public void testgetPlainProperty(){
        PSMetadataExtractorService svc = new PSMetadataExtractorService();

        PSTripleHandler handler = new PSTripleHandler();

        handler.getPlainPropertyName("");
    }


    @Test
    public void testNoAbstract() throws IOException {
        InputStream is = PSMetadataExtractorServiceTests.class.getResourceAsStream(
                "/com/percussion/delivery/no-abstract.html");

        try (InputStreamReader inputStreamReader = new InputStreamReader(is)) {

            PSMetadataExtractorService svc = new PSMetadataExtractorService();
            PSMetadataEntry entry = svc.process(inputStreamReader, "text/html",
                    "/Sites/test/no-abstract.html", null);

            assertNotNull(entry);
            assertEquals("page", entry.getType());
            HashMap map = new HashMap();

            for (IPSMetadataProperty prop : entry.getProperties()) {
                map.put(prop.getName(), prop.getValue());
            }

            assertEquals("test",map.get("dcterms:source"));
        }

    }

    @Test
    public void testbwcorona() throws IOException {
        InputStream is = PSMetadataExtractorServiceTests.class.getResourceAsStream(
                "/com/percussion/delivery/bw-corona.html");

        try (InputStreamReader inputStreamReader = new InputStreamReader(is)) {

            PSMetadataExtractorService svc = new PSMetadataExtractorService();
            PSMetadataEntry entry = svc.process(inputStreamReader,"text/html",
                    "/Sites/www.bw.edu/bw-corona.html",null);

            assertNotNull(entry);
            assertEquals("page", entry.getType());
            HashMap map = new HashMap();

            for(IPSMetadataProperty prop : entry.getProperties()){
                map.put(prop.getName(),prop.getValue());
            }
            assertEquals("standard-flex",map.get("dcterms:source"));
            assertEquals("2020-04-27T09:50:00",map.get("dcterms:created"));
            assertEquals("2020-03-16T17:02:57",map.get("dcterms:modified"));
            assertEquals("Modified Spring Semester Schedule",entry.getLinktext());
            assertEquals("/Categories/Event Types/BW Seasons",map.get("perc:category"));
            assertEquals("1200",map.get("og:image:width"));
            assertEquals("630",map.get("og:image:height"));
            assertEquals("Baldwin Wallace University",map.get("og:site_name"));
            assertEquals("Coronavirus Updates | Baldwin Wallace University",map.get("og:title"));
            assertEquals("Find out the latest updates concerning the Coronavirus and Baldwin Wallace University.",map.get("og:description"));
            assertEquals("https://www.bw.edu/advisory/coronavirus/",map.get("og:url"));
            assertEquals("https://www.bw.edu/Assets/social-media/social-share-default.jpg",map.get("og:image"));
            assertEquals("en_US",map.get("og:locale"));
            assertEquals("summary_large_image",map.get("twitter:card"));
            assertEquals("Coronavirus Updates | Baldwin Wallace University",map.get("twitter:title"));
            assertEquals("Find out the latest updates concerning the Coronavirus and Baldwin Wallace University.",map.get("twitter:description"));
            assertEquals("https://www.bw.edu/Assets/social-media/social-share-default.jpg",map.get("twitter:image"));
            assertEquals("banner image for Coronavirus Update",map.get("twitter:image:alt"));
            assertEquals("<div class=\"rxbodyfield\">\n" +
                    " <p><img alt=\"banner image for Coronavirus Update\" height=\"121\" src=\"/Assets/home-page/seasons/seasons-coronavirus.jpg\" title=\"Coronavirus Update\" width=\"402\"></p>\n" +
                    " <p>BW has instituted a Modified Spring Semester Schedule in response to Novel COVID-19 (Coronavirus). <strong>There are currently no campus-associated cases of COVID-19.</strong></p>\n" +
                    "</div>",map.get("dcterms:abstract"));
            assertEquals("article",map.get("og:type"));

        }
    }
}

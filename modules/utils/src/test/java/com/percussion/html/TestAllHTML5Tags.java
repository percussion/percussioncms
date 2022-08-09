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

package com.percussion.html;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test to validate the HTML cleaner / parser against
 * a document with  all html 5 tags.
 */
public class TestAllHTML5Tags {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public Document parsedDoc;
    public String parsedHTML;

    @Before
    public void setup() throws IOException, PSHtmlParsingException {
        temporaryFolder.create();

        String sourceDoc = new Scanner(Objects.requireNonNull(TestPSHtmlCleanerProperties.class.getResourceAsStream("/com/percussion/html/alltags.html")), "UTF-8").useDelimiter("\\A").next();

        parsedDoc = PSHtmlUtils.createHTMLDocument(sourceDoc,
                StandardCharsets.UTF_8,
                true,
                null);
        parsedHTML = parsedDoc.html();
    }

    @After
    public void teardown(){
        temporaryFolder.delete();
    }

    @Test
    public void testParse(){

        System.out.println(parsedHTML);
        assertNotNull(parsedHTML);
        assertTrue(parsedHTML.length()>1);

    }

    @Test
    public void testAside(){
        Elements tags = parsedDoc.select("aside");
        assertTrue(tags.size()>0);

        //TODO: Test attributes of aside
    }



}

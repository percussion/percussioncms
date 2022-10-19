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
import org.jsoup.safety.Safelist;
import org.junit.Test;

import javax.xml.transform.TransformerException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests to validate that the html cleaner properties
 * are working properly.
 */
public class TestPSHtmlCleanerProperties {

    @Test
    public void testDefaultProps() {
        Properties props = PSHtmlUtils.getDefaultCleanerProperties();

        assertNotNull(props);

        assertFalse(props.isEmpty());

        Safelist sl = PSHtmlUtils.getSafeListFromProperties(props);

        assertNotNull(sl);

    }

    @Test
    public void testFragment1() throws PSHtmlParsingException, TransformerException {

        String text = new Scanner(Objects.requireNonNull(TestPSHtmlCleanerProperties.class.getResourceAsStream("/com/percussion/html/fragment1.html")), "UTF-8").useDelimiter("\\A").next();

        assertNotNull(text);

        Document doc = PSHtmlUtils.createHTMLDocument(text, StandardCharsets.UTF_8, false, null);
        assertNotNull(doc);
        System.out.println(doc.html());
        doc = PSHtmlUtils.createHTMLDocument(text, StandardCharsets.UTF_8, true, null);
        assertNotNull(doc);
        String out = doc.html();
        System.out.println(doc.html());
        assertTrue(out.contains("<aside>"));
        assertTrue(out.contains("</aside>"));
        assertTrue(out.contains("<footer>"));
        assertTrue(out.contains("</footer>"));
        assertTrue(out.contains("🤡 🤥"));
        assertTrue(out.contains("<div class=\"rxbodyfield\">"));
        assertTrue(out.contains("</div"));
        assertTrue(out.contains("<br />"));
        assertTrue(out.contains("<script>"));
        assertTrue(out.contains("</script>"));
    }

    @Test
    public void testFragment2() throws PSHtmlParsingException, TransformerException {

        String text = new Scanner(Objects.requireNonNull(TestPSHtmlCleanerProperties.class.getResourceAsStream("/com/percussion/html/fragment2.html")), "UTF-8").useDelimiter("\\A").next();

        Document doc = PSHtmlUtils.createHTMLDocument(text, StandardCharsets.UTF_8, false, null);
        String parsed = doc.html();
        assertTrue(parsed.contains("/p>"));
    }

    @Test
    public void testFragment3() throws PSHtmlParsingException, TransformerException {

        String text = new Scanner(Objects.requireNonNull(TestPSHtmlCleanerProperties.class.getResourceAsStream("/com/percussion/html/fragment3.html")), "UTF-8").useDelimiter("\\Z").next();
        Document doc = PSHtmlUtils.createHTMLDocument(text, StandardCharsets.UTF_8, true, null);
        String parsed = doc.body().toString();
        assertEquals(text,parsed);
    }
}
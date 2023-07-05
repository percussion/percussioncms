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

    @Test
    public void testRemoveDataPathItem() throws PSHtmlParsingException {

        String text = new Scanner(Objects.requireNonNull(TestPSHtmlCleanerProperties.class.getResourceAsStream("/com/percussion/html/datapathitem.html")), "UTF-8").useDelimiter("\\Z").next();
        Document doc = PSHtmlUtils.createHTMLDocument(text, StandardCharsets.UTF_8, true, null);
        String parsed = doc.body().toString();

        assertFalse(parsed.contains("data-pathitem"));
    }
}
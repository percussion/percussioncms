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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.sitemanage.importer.utils;

import static org.junit.Assert.*;

import com.percussion.sitemanage.importer.utils.PSLinkExtractor;

import com.percussion.utils.testing.BackloggedTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(BackloggedTest.class)
public class TestPSLinkExtractor {

    @Ignore
    @Test
    public void testGetSiteLinkText() {
        assertTrue("Failed to evaluate / properly", PSLinkExtractor
                .getLinkText("https://www.percussion.com", null).equals("Home"));
        assertTrue("Failed to evaluate / properly", PSLinkExtractor
                .getLinkText("https://www.percussion.com", null).equals("Home"));
    }

    @Ignore
    @Test
    public void testGetRoot() {
        assertTrue("Failed to evaluate http://foo.org/", PSLinkExtractor
                .getRoot("http://foo.org/").equals("http://foo.org"));
        assertTrue(
                "Failed to evaluate http://foo.org/Product properly",
                PSLinkExtractor.getRoot("http://foo.org/Product").equals(
                        "http://foo.org"));
        assertTrue(
                "Failed to evaluate http://www.foo.org/Product properly",
                PSLinkExtractor.getRoot("http://www.foo.org/Product").equals(
                        "http://www.foo.org"));
        assertTrue("Failed to evaluate http://foo.org", PSLinkExtractor
                .getRoot("http://foo.org").equals("http://foo.org"));
        assertTrue(
                "Failed to evaluate https://foo.org/Product properly",
                PSLinkExtractor.getRoot("https://foo.org/Product").equals(
                        "https://foo.org"));
        assertTrue(
                "Failed to evaluate http://foo.org/Product/test/item.asp properly",
                PSLinkExtractor.getRoot("http://foo.org/Product/test/item.asp")
                        .equals("http://foo.org"));
        assertTrue(
                "Failed to evaluate http://foo.org/Product/test/item.asp?foo=1&bar=2 properly",
                PSLinkExtractor.getRoot(
                        "http://foo.org/Product/test/item.asp?foo=1&bar=2")
                        .equals("http://foo.org"));
    }

    @Ignore
    @Test
    public void testGetRelativePath() {
        assertTrue(
                "Failed to evaluate http://foo.org properly",
                PSLinkExtractor.getRelativePath("http://foo.org", "/", null).equals(
                        "/"));
        assertTrue("Failed to evaluate http://foo.org/ properly",
                PSLinkExtractor.getRelativePath("http://foo.org/", "/", null)
                        .equals("/"));
        assertTrue("Failed to evaluate http://foo.org/Product properly",
                PSLinkExtractor.getRelativePath("http://foo.org/Product", "/Product", null)
                        .equals("/"));
        assertTrue("Failed to evaluate http://foo.org/Product/ properly",
                PSLinkExtractor
                        .getRelativePath("http://foo.org/Product/", "/Product/", null)
                        .equals("/Product/"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/Product.asp properly",
                PSLinkExtractor.getRelativePath(
                        "http://www.foo.org/Product.asp", "/Product.asp", null).equals("/"));
        assertTrue(
                "Failed to evaluate http://foo.org/Product/test/item.asp properly",
                PSLinkExtractor.getRelativePath(
                        "http://foo.org/Product/test/item.asp", "/Product/test/item.asp", null).equals(
                        "/Product/test/"));
        assertTrue(
                "Failed to evaluate smb://foo.org/Product/test/item.asp?foo=1&bar=2 properly",
                PSLinkExtractor.getRelativePath(
                        "http://foo.org/Product/test/item.asp?foo=1&bar=2", "/Product/test/item.asp?foo=1&bar=2",
                        null).equals("/Product/test/"));
        assertTrue(
                "Failed to evaluate https://www.foo.org\\Product.asp properly",
                PSLinkExtractor.getRelativePath("http://www.foo.org\\Product.asp", "\\Product.asp",
                        null).equals("/"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/Produc Space.asp properly",
                PSLinkExtractor.getRelativePath(
                        "http://www.foo.org/Product Space.asp", "Product Space.asp", null).equals(
                        "/"));
        assertTrue(
                "Failed to evaluate smb://foo.org/Product/test/item.asp?foo=1&bar=2 properly",
                PSLinkExtractor.getRelativePath(
                        "http://foo.org/Product/test it/item.asp?foo=1&bar=2", "Product/test it/item.asp?foo=1&bar=2",
                        null).equals("/Product/test-it/"));
    }

    @Ignore
    @Test
    public void testGetFileName() {
        assertEquals(
                "Failed to evaluate https://www.percussion.com properly",
                "index.html",
                PSLinkExtractor.getPageName("https://www.percussion.com", null));
        assertEquals(
                "Failed to evaluate https://www.percussion.com/ properly","index.html",
                PSLinkExtractor.getPageName("https://www.percussion.com/", null));
        assertTrue("Failed to evaluate http://foo.org/Product properly",
                PSLinkExtractor.getPageName("http://foo.org/Product", null)
                        .equals("Product"));
        assertTrue("Failed to evaluate http://foo.org/Product/ properly",
                PSLinkExtractor.getPageName("http://foo.org/Product/", null)
                        .equals("index"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/Product.asp properly",
                PSLinkExtractor.getPageName("http://www.foo.org/Product.asp",
                        null).equals("Product.asp"));
        assertTrue(
                "Failed to evaluate http://foo.org/Product/test/item.asp properly",
                PSLinkExtractor.getPageName(
                        "http://foo.org/Product/test/item.asp", null).equals(
                        "item.asp"));
        assertTrue(
                "Failed to evaluate smb://foo.org/Product/test/item.asp?foo=1&bar=2 properly",
                PSLinkExtractor.getPageName(
                        "http://foo.org/Product/test/item.asp?foo=1&bar=2",
                        null).equals("item-asp-foo-1-bar-2"));
        assertTrue(
                "Failed to evaluate https://www.foo.org\\Product.asp properly",
                PSLinkExtractor.getPageName("http://www.foo.org\\Product.asp",
                        null).equals("Product.asp"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/Produc Space.asp properly",
                PSLinkExtractor.getPageName(
                        "http://www.foo.org/Product Space.asp", null).equals(
                        "Product-Space.asp"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/?p=69 wordpress case",
                PSLinkExtractor.getPageName(
                        "https://www.foo.org/?p=69", null).equals(
                        "item-p-69"));
    }
}

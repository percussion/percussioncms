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
                PSLinkExtractor.getRelativePath("http://foo.org", "/", null, null).equals(
                        "/"));
        assertTrue("Failed to evaluate http://foo.org/ properly",
                PSLinkExtractor.getRelativePath("http://foo.org/", "/", null, null)
                        .equals("/"));
        assertTrue("Failed to evaluate http://foo.org/Product properly",
                PSLinkExtractor.getRelativePath("http://foo.org/Product", "/Product", null, null)
                        .equals("/"));
        assertTrue("Failed to evaluate http://foo.org/Product/ properly",
                PSLinkExtractor
                        .getRelativePath("http://foo.org/Product/", "/Product/", null, null)
                        .equals("/Product/"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/Product.asp properly",
                PSLinkExtractor.getRelativePath(
                        "http://www.foo.org/Product.asp", "/Product.asp", null, null).equals("/"));
        assertTrue(
                "Failed to evaluate http://foo.org/Product/test/item.asp properly",
                PSLinkExtractor.getRelativePath(
                        "http://foo.org/Product/test/item.asp", "/Product/test/item.asp", null, null).equals(
                        "/Product/test/"));
        assertTrue(
                "Failed to evaluate smb://foo.org/Product/test/item.asp?foo=1&bar=2 properly",
                PSLinkExtractor.getRelativePath(
                        "http://foo.org/Product/test/item.asp?foo=1&bar=2", "/Product/test/item.asp?foo=1&bar=2",
                        null, null).equals("/Product/test/"));
        assertTrue(
                "Failed to evaluate https://www.foo.org\\Product.asp properly",
                PSLinkExtractor.getRelativePath("http://www.foo.org\\Product.asp", "\\Product.asp",
                        null, null).equals("/"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/Produc Space.asp properly",
                PSLinkExtractor.getRelativePath(
                        "http://www.foo.org/Product Space.asp", "Product Space.asp", null, null).equals(
                        "/"));
        assertTrue(
                "Failed to evaluate smb://foo.org/Product/test/item.asp?foo=1&bar=2 properly",
                PSLinkExtractor.getRelativePath(
                        "http://foo.org/Product/test it/item.asp?foo=1&bar=2", "Product/test it/item.asp?foo=1&bar=2",
                        null, null).equals("/Product/test-it/"));
    }

    @Ignore
    @Test
    public void testGetFileName() {
        assertEquals(
                "Failed to evaluate https://www.percussion.com properly",
                "index.html",
                PSLinkExtractor.getPageName("https://www.percussion.com", null, null));
        assertEquals(
                "Failed to evaluate https://www.percussion.com/ properly","index.html",
                PSLinkExtractor.getPageName("https://www.percussion.com/", null, null));
        assertTrue("Failed to evaluate http://foo.org/Product properly",
                PSLinkExtractor.getPageName("http://foo.org/Product", null, null)
                        .equals("Product"));
        assertTrue("Failed to evaluate http://foo.org/Product/ properly",
                PSLinkExtractor.getPageName("http://foo.org/Product/", null, null)
                        .equals("index"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/Product.asp properly",
                PSLinkExtractor.getPageName("http://www.foo.org/Product.asp",
                        null, null).equals("Product.asp"));
        assertTrue(
                "Failed to evaluate http://foo.org/Product/test/item.asp properly",
                PSLinkExtractor.getPageName(
                        "http://foo.org/Product/test/item.asp", null, null).equals(
                        "item.asp"));
        assertTrue(
                "Failed to evaluate smb://foo.org/Product/test/item.asp?foo=1&bar=2 properly",
                PSLinkExtractor.getPageName(
                        "http://foo.org/Product/test/item.asp?foo=1&bar=2",
                        null, null).equals("item-asp-foo-1-bar-2"));
        assertTrue(
                "Failed to evaluate https://www.foo.org\\Product.asp properly",
                PSLinkExtractor.getPageName("http://www.foo.org\\Product.asp",
                        null, null).equals("Product.asp"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/Produc Space.asp properly",
                PSLinkExtractor.getPageName(
                        "http://www.foo.org/Product Space.asp", null, null).equals(
                        "Product-Space.asp"));
        assertTrue(
                "Failed to evaluate https://www.foo.org/?p=69 wordpress case",
                PSLinkExtractor.getPageName(
                        "https://www.foo.org/?p=69", null, null).equals(
                        "item-p-69"));
    }
}

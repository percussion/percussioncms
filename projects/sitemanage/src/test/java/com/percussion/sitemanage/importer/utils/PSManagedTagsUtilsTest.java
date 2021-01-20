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
package com.percussion.sitemanage.importer.utils;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Santiago M. Murchio
 *
 */
public class PSManagedTagsUtilsTest
{
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testIsManagedJSReference_noScriptTag()
    {
        Element title =  new Element(Tag.valueOf("title"), "http://");  
        title.html("The title of the page");
        assertFalse("The element " + title.toString() + " should not have been detected as managed.",
                PSManagedTagsUtils.isManagedJSReference(title));
        
        Attributes metadataAttributes = new Attributes();
        metadataAttributes.put("name", "keywords");
        metadataAttributes.put("content", "javascript html add");
        Element metadata =  new Element(Tag.valueOf("meta"), "http://", metadataAttributes);            
        assertFalse("The element " + metadata.toString() + " should not have been detected as managed.",
                PSManagedTagsUtils.isManagedJSReference(metadata));
        
        Attributes aAttributes = new Attributes();
        aAttributes.put("href", "http://javascript.about.com/");
        aAttributes.put("content", "javascript html add");
        Element a =  new Element(Tag.valueOf("a"), "http://", aAttributes);
        a.html("link text");
        assertFalse("The element " + a.toString() + " should not have been detected as managed.",
                PSManagedTagsUtils.isManagedJSReference(a));
    }
    
    @Test
    public void testIsManagedJSReference_externalJSReferences() 
    {
        assertManagedJSElement(EXTERNAL_REFERENCE_PREFIX_1);            
        assertManagedJSElement(EXTERNAL_REFERENCE_PREFIX_2);            
        assertManagedJSElement(EXTERNAL_REFERENCE_PREFIX_3);
        
        assertNotManagedJSElement(EXTERNAL_REFERENCE_PREFIX_1);
        assertNotManagedJSElement(EXTERNAL_REFERENCE_PREFIX_2);
        assertNotManagedJSElement(EXTERNAL_REFERENCE_PREFIX_3);
    }
    
    @Test
    public void testIsManagedJSReference_relativeToPageOrSiteReferences()
    {
        // relative to page
        assertManagedJSElement("");            

        assertNotManagedJSElement("");

        // relative to site
        assertManagedJSElement(RELATIVE_TO_SITE_PREFIX_1);            
        assertManagedJSElement(RELATIVE_TO_SITE_PREFIX_2);            
        assertManagedJSElement(RELATIVE_TO_SITE_PREFIX_3);            

        assertNotManagedJSElement(RELATIVE_TO_SITE_PREFIX_1);
        assertNotManagedJSElement(RELATIVE_TO_SITE_PREFIX_2);
        assertNotManagedJSElement(RELATIVE_TO_SITE_PREFIX_3);
    }
    
    @Test
    public void testIsManagedJSReference_CDNReferences()
    {
        assertManagedJSElement(CDN_REFERENCE_1);
        assertManagedJSElement(CDN_REFERENCE_2);

        assertNotManagedJSElement(RELATIVE_TO_SITE_PREFIX_1);
        assertNotManagedJSElement(RELATIVE_TO_SITE_PREFIX_2);
    }

    @Test
    public void testIsManagedMetadataTag_managed()
    {
        for(String managedTag : MANAGED_META_TAGS)
        {
            Element header = Jsoup.parse(managedTag).head();
            Element tag = header.children().get(0);
            
            assertTrue("The element " + tag.toString() + " should have been detected as managed, but was not.",
                    PSManagedTagsUtils.isManagedMetadataTag(tag));
        }
    }
    
    @Test
    public void testIsManagedMetadataTag_notManaged()
    {
        for(String notManagedTag : NOT_MANAGED_META_TAGS)
        {
            Element header = Jsoup.parse(notManagedTag).head();
            Element tag = header.children().get(0);
            
            assertFalse("The element " + tag.toString() + " should not have been detected as managed.",
                    PSManagedTagsUtils.isManagedMetadataTag(tag));
        }
    }
    
    /**
     * Checks that the managed element is recognized as managed.
     * 
     * @param prefix {@link String} with the prefix to use. Assumed not
     *            <code>null</code> but may be empty.
     */
    private void assertManagedJSElement(String prefix)
    {
        for(String sufix : MANAGED_JS_SUFIX)
        {
            Element managedJs = buildJSReferenceTag(prefix + sufix);
            assertTrue("The element " + managedJs.toString() + " should have been detected as managed, but was not.",
                    PSManagedTagsUtils.isManagedJSReference(managedJs));
        }
    }

    /**
     * Checks that the managed element is not recognized as managed.
     * 
     * @param prefix {@link String} with the prefix to use. Assumed not
     *            <code>null</code> but may be empty.
     */
    private void assertNotManagedJSElement(String prefix)
    {
        for(String sufix : NOT_MANAGED_JS_SUFIX)
        {
            Element managedJs = buildJSReferenceTag(prefix + sufix);
            assertFalse("The element " + managedJs.toString() + " should not have been detected as managed.",
                    PSManagedTagsUtils.isManagedJSReference(managedJs));
        }
    }

    /**
     * Builds an {@link Element} to emulate a given tag.
     * 
     * @param src {@link String} with the source attribute. Assumed not
     *            <code>null</code> nor empty.
     * @return {@link Element} never <code>null</code> nor empty.
     */
    private Element buildJSReferenceTag(String src)
    {
        Tag tag = Tag.valueOf("script");
        
        Attributes attributes = new Attributes();
        attributes.put("src", src);
        attributes.put("type", "text/javascript");
        
        return new Element(tag, "http://", attributes);
    }
    
    private final String EXTERNAL_REFERENCE_PREFIX_1 = "http://ajax.googleapis.com/ajax/libs/jquery/1.8/";
    private final String EXTERNAL_REFERENCE_PREFIX_2 = "http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/";
    private final String EXTERNAL_REFERENCE_PREFIX_3 = "https://ajax.googleapis.com/ajax/libs/jquery/1.4.4/";

    private final String RELATIVE_TO_SITE_PREFIX_1 = "/scripts/";
    private final String RELATIVE_TO_SITE_PREFIX_2 = "/scripts/js/";
    private final String RELATIVE_TO_SITE_PREFIX_3 = "/scripts/js/min/";

    private final String CDN_REFERENCE_1 = "http://ajax.aspnetcdn.com/ajax/jQuery/";
    private final String CDN_REFERENCE_2 = "http://ajax.aspnetcdn.com/ajax/jquery.ui/1.8.18/";
    
    private final String[] MANAGED_JS_SUFIX = new String[]
    {"jquery.js", "jquery.min.js", "jquery.ui.core.js", "jquery.tools.min.js", "jquery-latest.js", "jquery-ui.min.js",
            "jquery-1.3.2.js", "jquery-1.8.1.min.js", "jquery-1.5.2.min.js", "jquery-ui-1.8.12.custom.min.js",
            "jquery-ui-1.7.2.custom.min.js", "jquery-ui-1.8.17.custom.min.js", "jquery-1.4.2.js",
            "jquery-1.7.1.min.js", "jquery-1.4.2.min.js", "jquery-1.3.2.min.js", "jquery-1.7.2.min.js",
            "jquery-1.7.1.min.js?b=111", "jquery.ui.js"};
    
    private final String[] NOT_MANAGED_JS_SUFIX = new String[]
    {"jquery-dialog-1.3.2.js", "jquery.carrousel-1.4.2.min.js"};
    
    private static final String[] MANAGED_META_TAGS = new String[]
    {"<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\" />",
            "<meta name=\"generator\" content=\"Percussion\" />", "<meta name=\"robots\" content=\"noindex\" />",
            "<meta name=\"description\" content=\"The description of the page\" />",
            "<meta property=\"dcterms:author\" content=\"author of the page\" />",
            "<meta property=\"dcterms:type\" content=\"page\" />",
            "<meta property=\"dcterms:source\" content=\"perc.template.name\" />",
            "<meta property=\"dcterms:created\" datatype=\"xsd:dateTime\" content=\"2012-10-10\" />",
            "<meta property=\"dcterms:alternative\" content=\"perc.page.linkTitle\" />",
            "<meta property=\"perc:tags\" content=\"tag1.String\" />",
            "<meta property=\"perc:tags\" content=\"tag2.String\" />",
            "<meta property=\"perc:category\" content=\"category.String\" />",
            "<meta property=\"perc:calendar\" content=\"Calendar Name\" />",
            "<meta property=\"perc:start_date\" content=\"10/12/2012\" />",
            "<meta property=\"perc:end_date\" datatype=\"xsd:dateTime\" content=\"10/12/2012\" />"};

    private static final String[] NOT_MANAGED_META_TAGS = new String[]
    {"<meta http-equiv=\"refresh\" content=\"600\" />",
            "<meta http-equiv=\"default-style\" content=\"link_element\" />"};

}

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

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.swing.text.html.HTML;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static javax.swing.text.html.CSS.getAllAttributeKeys;
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

    Map<String, String> globalAttributes = new HashMap<String, String>();
    Map<String, String> eventAttribute = new HashMap<String, String>();

    @Before
    public void setup() throws IOException, PSHtmlParsingException {
        temporaryFolder.create();

        String sourceDoc = new Scanner(Objects.requireNonNull(TestPSHtmlCleanerProperties.class.getResourceAsStream("/com/percussion/html/alltags.html")), "UTF-8").useDelimiter("\\A").next();

        parsedDoc = PSHtmlUtils.createHTMLDocument(sourceDoc,
                StandardCharsets.UTF_8,
                true,
                null);
        parsedHTML = parsedDoc.html();
        eventAttribute.put("onafterprint", "console.log('onafterprint test');");
        eventAttribute.put("onbeforeprint", "console.log('onbeforeprint test');");
        eventAttribute.put("onbeforeunload", "console.log('onbeforeunload test');");
        eventAttribute.put("onerror", "console.log('onerror test');");
        eventAttribute.put("onhashchange", "console.log('onhashchange test');");
        eventAttribute.put("onload", "console.log('onload test');");
        eventAttribute.put("onmessage", "console.log('onmessage test');");
        eventAttribute.put("onoffline","console.log('onoffline test');");
        eventAttribute.put("ononline", "console.log('ononline test');");
        eventAttribute.put("onpagehide","console.log('onpagehide test');");
        eventAttribute.put("onpageshow","console.log('onpageshow test');");
        eventAttribute.put("onpopstate", "console.log('onpopstate test');");
        eventAttribute.put("onresize","console.log('onresize test');");
        eventAttribute.put("onstorage","console.log('onstorage test');");
        eventAttribute.put("onunload", "console.log('onunload test');");
        eventAttribute.put("onblur","console.log('onblur test');");
        eventAttribute.put("onchange", "console.log('onchange test');");
        eventAttribute.put("oncontextmenu","console.log('oncontextmenu test');");
        eventAttribute.put("onfocus","console.log('onfocus test');");
        eventAttribute.put("oninput", "console.log('oninput test');");
        eventAttribute.put("oninvalid", "console.log('oninvalid test');");
        eventAttribute.put("onreset", "console.log('onreset test');");
        eventAttribute.put("onsearch","console.log('onsearch test');");
        eventAttribute.put("onselect", "console.log('onselect test');");
        eventAttribute.put("onsubmit", "console.log('onsubmit test');");
        eventAttribute.put("onkeydown", "console.log('onkeydown test');");
        eventAttribute.put("onkeypress", "console.log('onkeypress test');");
        eventAttribute.put("onkeyup", "console.log('onkeyup test');");
        eventAttribute.put("onclick", "console.log('onclick test');");
        eventAttribute.put("ondblclick", "console.log('ondblclick test');");
        eventAttribute.put("onmousedown", "console.log('onmousedown test');");
        eventAttribute.put("onmousemove", "console.log('onmousemove test');");
        eventAttribute.put("onmouseout", "console.log('onmouseout test');");
        eventAttribute.put("onmouseover", "console.log('onmouseover test');");
        eventAttribute.put("onmouseup", "console.log('onmouseup test');");
        eventAttribute.put("onmousewheel", "console.log('onmousewheel test');");
        eventAttribute.put("onwheel", "console.log('onwheel test');");
        eventAttribute.put("ondrag", "console.log('ondrag test');");
        eventAttribute.put("ondragend", "console.log('ondragend test');");
        eventAttribute.put("ondragenter", "console.log('ondragenter test');");
        eventAttribute.put("ondragleave", "console.log('ondragleave test');");
        eventAttribute.put("ondragover","console.log('ondragover test');");
        eventAttribute.put("ondragstart", "console.log('ondragstart test');");
        eventAttribute.put("ondrop", "console.log('ondrop test');");
        eventAttribute.put("onscroll", "console.log('onscroll test');");
        eventAttribute.put("oncopy", "console.log('oncopy test');");
        eventAttribute.put("oncut", "console.log('oncut test');");
        eventAttribute.put("onpaste", "console.log('onpaste test');");
        eventAttribute.put("ondragend", "console.log('ondragend test');");
        eventAttribute.put("oncanplay","console.log('oncanplay test');");
        eventAttribute.put("oncanplaythrough", "console.log('oncanplaythrough test');");
        eventAttribute.put("oncuechange", "console.log('oncuechange test');");
        eventAttribute.put("ondurationchange", "console.log('ondurationchange test');");
        eventAttribute.put("onemptied", "console.log('onemptied test');");
        eventAttribute.put("onended", "console.log('onended test');");
        eventAttribute.put("onerror", "console.log('onerror test');");
        eventAttribute.put("onloadeddata", "console.log('onloadeddata test');");
        eventAttribute.put("onloadedmetadata", "console.log('onloadedmetadata test');");
        eventAttribute.put("onloadstart", "console.log('onloadstart test');");
        eventAttribute.put("onpause", "console.log('onpause test');");
        eventAttribute.put("onplay", "console.log('onplay test');");
        eventAttribute.put("onplaying", "console.log('onplaying test');");
        eventAttribute.put("onprogress", "console.log('onprogress test');");
        eventAttribute.put("onratechange", "console.log('onratechange test');");
        eventAttribute.put("onseeked", "console.log('onseeked test');");
        eventAttribute.put("onseeking", "console.log('onseeking test');");
        eventAttribute.put("onstalled", "console.log('onstalled test');");
        eventAttribute.put("onsuspend", "console.log('onsuspend test');");
        eventAttribute.put("ontimeupdate", "console.log('ontimeupdate test');");
        eventAttribute.put("onvolumechange", "console.log('onvolumechange test');");
        eventAttribute.put("onwaiting", "console.log('onwaiting test');");
        eventAttribute.put("ontoggle", "console.log('ontoggle test');");



        globalAttributes.put("accesskey", "A");
        globalAttributes.put("class", "unit-test");
        globalAttributes.put("contenteditable", "true");
        globalAttributes.put("data-*", "datatesttype");
        globalAttributes.put("dir", "auto");
        globalAttributes.put("draggable", "true");
        globalAttributes.put("hidden", "");
        globalAttributes.put("id", "testid");
        globalAttributes.put("lang", "en");
        globalAttributes.put("spellcheck", "true");
        globalAttributes.put("style", "color:red");
        globalAttributes.put("tabindex", "1");
        globalAttributes.put("title", "testtitle");
        globalAttributes.put("translate", "no");









    }

    @After
    public void teardown(){

    }

    @Test
    public void testParse(){

        System.out.println(parsedHTML);
        assertNotNull(parsedHTML);
        assertTrue(parsedHTML.length()>1);

    }

    private void verifyAttributes(Element tag,Map<String, String> attrList){

        for (Map.Entry attr:attrList.entrySet()) {
            String attrValue = tag.attr((String)attr.getKey());
            assertTrue(attrValue != null);
            System.out.println("Attribute Value: " + attrValue);
            System.out.println("Attribute Name: " + attr.getKey());
            System.out.println("Attribute Stored Value: " + attr.getValue());
            assertTrue(attrValue.equals(attr.getValue()));
        }

    }


    private void verifyAttributes(Attributes attributes,Map<String, String> attrList){

        for (Map.Entry attr:attrList.entrySet()) {
            String attrValue = attributes.get((String)attr.getKey());
            assertTrue(attrValue != null);
           System.out.println("Attribute Value: " + attrValue);
           System.out.println("Attribute Name: " + attr.getKey());
           System.out.println("Attribute Stored Value: " + attr.getValue());
            assertTrue(attrValue.equals(attr.getValue()));
        }

    }


    @Test
    public void testAsideTag(){
        Elements tags = parsedDoc.select("aside[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }



    @Test
    public void testATag(){

    Elements tags = parsedDoc.select("a[class=unit-test]");
    assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("download", "downloadTest");
        Attributes.put("href", "https://www.w3schools.com/TAGS/default.ASP");
        Attributes.put("hreflang", "en");
        Attributes.put("media", "all");
        Attributes.put("referrerpolicy", "no-referrer");
        Attributes.put("rel", "nofollow");
        Attributes.put("target", "_blank");
        Attributes.put("type", "text/html");
        Attributes.put("ping", "alternate");

        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }
    }

    @Test
    public void testAbbrTag(){

        Elements tags = parsedDoc.select("abbr[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testAcronymTag(){

        Elements tags = parsedDoc.select("a[class=unit-test]");
        assertTrue(tags.size()>0);




    }

    @Test
    public void testAddressTag(){

        Elements tags = parsedDoc.select("address[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testAppletTag(){

        Elements tags = parsedDoc.select("a[class=unit-test]");
        assertTrue(tags.size()>0);

    }

    @Test
    public void testAreaTag(){

        Elements tags = parsedDoc.select("area[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("download", "downloadTest");
        Attributes.put("href", "https://www.w3schools.com/TAGS/default.ASP");
        Attributes.put("hreflang", "en");
        Attributes.put("media", "all");
        Attributes.put("referrerpolicy", "no-referrer");
        Attributes.put("rel", "nofollow");
        Attributes.put("target", "_blank");
        Attributes.put("type", "text/html");
        Attributes.put("shape", "rect");
        Attributes.put("alt", "testalt");
        Attributes.put("coords", "0,0,0,0");

        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }

    }

    @Test
    public void testArticleTag(){

        Elements tags = parsedDoc.select("article[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testAudioTag(){



        Elements tags = parsedDoc.select("audio[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("autoplay", "true");
        Attributes.put("controls", "true");
        Attributes.put("muted", "true");
        Attributes.put("loop", "true");
        Attributes.put("preload", "none");
        Attributes.put("src", "https://open.spotify.com/track/141GIDaZBy7Bn3I2NAFR31?si=71e860708f3c4b55");


            for (Element tag:tags) {
                Attributes attrs = tag.attributes();
                verifyAttributes(tag,globalAttributes);
                verifyAttributes(attrs,eventAttribute);
                verifyAttributes(attrs,Attributes);
            }

    }


    @Test
    public void testBTag(){

        Elements tags = parsedDoc.select("b[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testBaseTag(){


        Elements tags = parsedDoc.select("base[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("href", "https://www.w3schools.com/TAGS/default.ASP");
        Attributes.put("target", "_blank");

        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }


    }

    @Test
    public void testBaseFontTag(){

        Elements tags = parsedDoc.select("basefont");
        assertTrue(tags.size()>0);
    }

    @Test
    public void testBDITag(){

        Elements tags = parsedDoc.select("bdi[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testBDOTag(){

        Elements tags = parsedDoc.select("bdo[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }


    }

    @Test
    public void testBIGTag(){

        Elements tags = parsedDoc.select("big");
        assertTrue(tags.size()>0);


    }

    @Test
    public void testBlockQuoteTag(){

        Elements tags = parsedDoc.select("blockquote[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("cite", "https://www.w3schools.com/TAGS/att_blockquote_cite.asp");


        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }

    }

    @Test
    public void testBodyTag(){

//        Elements tags = parsedDoc.select("body[class=unit-test]");
//        assertTrue(tags.size()>0);


    }

    @Test
    public void testBRTag(){

        Elements tags = parsedDoc.select("br[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }
    @Test
    public void testButtonTag(){

        Elements tags = parsedDoc.select("button[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("autofocus", "");
        Attributes.put("disabled", "");
        Attributes.put("form", "input_attr_form");
        Attributes.put("formaction", "/action_page1.php");
        Attributes.put("formenctype", "text/plain");
        Attributes.put("formmethod", "post");
        Attributes.put("formnovalidate", "");
        Attributes.put("formtarget", "_blank");
        Attributes.put("name", "test");
        Attributes.put("type", "submit");
        Attributes.put("value", "test.html");

        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }


    }
    @Test
    public void testCanvasTag(){

        Elements tags = parsedDoc.select("canvas[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("height", "100px");
        Attributes.put("width", "100px");


        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }

    }

    @Test
    public void testCaptionTag(){

        Elements tags = parsedDoc.select("caption[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testCenterTag(){

        Elements tags = parsedDoc.select("center");
        assertTrue(tags.size()>0);


    }

    @Test
    public void testCiteTag(){

        Elements tags = parsedDoc.select("cite[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testCodeTag(){

        Elements tags = parsedDoc.select("code[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testColumnTag(){

        Elements tags = parsedDoc.select("col[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("span", "1");



        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }
    }

    @Test
    public void testColGroupTag(){

        Elements tags = parsedDoc.select("col[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("span", "1");


        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }
    }

    @Test
    public void testDataTag(){

        Elements tags = parsedDoc.select("data[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("value", "00000");


        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }

    }

    @Test
    public void testDataListTag(){

            Elements tags = parsedDoc.select("datalist[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testDDTag(){

        Elements tags = parsedDoc.select("dd[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testDeleteTag(){

        Elements tags = parsedDoc.select("del[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("cite", "https://www.w3schools.com/TAGS/att_blockquote_cite.asp");
        Attributes.put("datetime", "2015-11-15T22:55:03Z");


        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }
    }

    @Test
    public void testDetailsTag(){

        Elements tags = parsedDoc.select("details[class=unit-test]");
        assertTrue(tags.size()>0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("open", "");


        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,Attributes);
        }
    }

    @Test
    public void testDefineTag(){

        Elements tags = parsedDoc.select("dfn[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testDialogTag() {

        Elements tags = parsedDoc.select("dialog[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("open", "");


        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testDirTag(){

        Elements tags = parsedDoc.select("dir");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDivTag(){

        Elements tags = parsedDoc.select("div[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testDLTag(){

        Elements tags = parsedDoc.select("dl[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testDTTag(){

        Elements tags = parsedDoc.select("dt[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testEmphasizeTag(){

        Elements tags = parsedDoc.select("em[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testEmbedTag(){

        Elements tags = parsedDoc.select("embed[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("height", "100px");
        Attributes.put("src", "");
        Attributes.put("type", "https://open.spotify.com/track/141GIDaZBy7Bn3I2NAFR31?si=71e860708f3c4b55");
        Attributes.put("width", "100px");


        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testFieldSetTag(){

            Elements tags = parsedDoc.select("fieldset[class=unit-test]");
            assertTrue(tags.size() > 0);
            Map<String, String> Attributes = new HashMap<String, String>();
            Attributes.put("disabled", "");
            Attributes.put("form", "");
            Attributes.put("name", "test");


            for (Element tag : tags) {
                Attributes attrs = tag.attributes();
                verifyAttributes(tag, globalAttributes);
                verifyAttributes(attrs, eventAttribute);
                verifyAttributes(attrs, Attributes);
            }
    }

    @Test
    public void testFigCaptionTag(){

        Elements tags = parsedDoc.select("figcaption[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testFigureTag(){

        Elements tags = parsedDoc.select("figure[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testFontTag(){

        Elements tags = parsedDoc.select("font");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testFooterTag(){

        Elements tags = parsedDoc.select("footer[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testFormTag(){

        Elements tags = parsedDoc.select("form[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("autocomplete","on");
        Attributes.put("enctype", "multipart/form-data");
        Attributes.put("accept-charset", "utf-test");
        Attributes.put("action", "/action_test.asp");
        Attributes.put("method", "post");
        Attributes.put("name", "test");
        Attributes.put("novalidate", "");
        Attributes.put("rel", "nofollow");
        Attributes.put("target", "_blank");


        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testFrameTag(){

        Elements tags = parsedDoc.select("frame");
        assertTrue(tags.size()==0);

    }

    @Test
    public void testFrameSetTag(){

        Elements tags = parsedDoc.select("frameset");
        assertTrue(tags.size() == 0);

    }

    @Test
    public void testH1Tag(){

        Elements tags = parsedDoc.select("h1[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testH2Tag(){

        Elements tags = parsedDoc.select("h2[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testH3Tag(){

        Elements tags = parsedDoc.select("h3[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testH4Tag(){

        Elements tags = parsedDoc.select("h4[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testH5Tag(){

        Elements tags = parsedDoc.select("h4[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testH6Tag(){

        Elements tags = parsedDoc.select("h4[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testHeadTag(){

        Elements tags = parsedDoc.select("h4[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);


        }
    }

    @Test
    public void testHeaderTag(){

        Elements tags = parsedDoc.select("header[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testHRTag(){

        Elements tags = parsedDoc.select("hr[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testhtmlTag(){

//        Elements tags = parsedDoc.select("html[class=unit-test]");
//        assertTrue(tags.size() > 0);
//        Map<String, String> Attributes = new HashMap<String, String>();
//        Attributes.put("xmlns","http://www.w3.org/1999/xhtml");
//
//
//        for (Element tag : tags) {
//            Attributes attrs = tag.attributes();
//            verifyAttributes(tag, globalAttributes);
//            verifyAttributes(attrs, eventAttribute);
//            verifyAttributes(attrs, Attributes);
//        }

    }

    @Test
    public void testITag(){

        Elements tags = parsedDoc.select("I[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public   void testIFrameTag(){

        Elements tags = parsedDoc.select("iframe[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("allow","");
        Attributes.put("allowfullscreen", "true");
        Attributes.put("allowpaymentrequest", "true");
        Attributes.put("height", "100px");
        Attributes.put("loading", "eager");
        Attributes.put("name", "test");
        Attributes.put("referrerpolicy", "no-referrer");
        Attributes.put("sandbox", "");
        Attributes.put("src", "https://open.spotify.com/track/141GIDaZBy7Bn3I2NAFR31?si=71e860708f3c4b55");
        Attributes.put("srcdoc", "<p>Hello world!</p>");
        Attributes.put("width", "100px");



        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testImageTag(){

        Elements tags = parsedDoc.select("img[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("alt","testalt");
        Attributes.put("crossorigin", "");
        Attributes.put("ismap", "");
        Attributes.put("height", "100px");
        Attributes.put("loading", "eager");
        Attributes.put("longdesc", "https://www.google.com/imgres?imgurl=https%3A%2F%2Fcdn.britannica.com%2F92%2F100692-050-5B69B59B%2FMallard.jpg&imgrefurl=https%3A%2F%2Fwww.britannica.com%2Fanimal%2Fduck&tbnid=cv-8vEQ0udAypM&vet=12ahUKEwigtsHGxI_6AhWImmoFHZ_-CKQQMygAegUIARDjAQ..i&docid=IpKgqf0_gTf_MM&w=1600&h=1423&q=duck&ved=2ahUKEwigtsHGxI_6AhWImmoFHZ_-CKQQMygAegUIARDjAQ");
        Attributes.put("referrerpolicy", "no-referrer");
        Attributes.put("sizes", "100px");
        Attributes.put("src", "https://open.spotify.com/track/141GIDaZBy7Bn3I2NAFR31?si=71e860708f3c4b55");
        Attributes.put("srcset", "https://www.w3schools.com/TAGS/tag_img.asp,https://www.w3schools.com/TAGS/tag_img.asp");
        Attributes.put("usemap", "#testmap");
        Attributes.put("width", "100px");



        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testInputTag(){

        Elements tags = parsedDoc.select("input");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testInsTag() {


            Elements tags = parsedDoc.select("ins[class=unit-test]");
            assertTrue(tags.size() > 0);
            Map<String, String> Attributes = new HashMap<String, String>();
            Attributes.put("cite", "https://www.w3schools.com/TAGS/att_blockquote_cite.asp");
            Attributes.put("datetime", "2015-11-15T22:55:03Z");


            for (Element tag : tags) {
                Attributes attrs = tag.attributes();
                verifyAttributes(tag, globalAttributes);
                verifyAttributes(attrs, eventAttribute);
                verifyAttributes(attrs, Attributes);
            }
        }


    @Test
    public void testKBDTag(){

        Elements tags = parsedDoc.select("kbd[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testLabelTag(){

        Elements tags = parsedDoc.select("label[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("for", "label");
        Attributes.put("form", "1");


        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testLegendTag(){

        Elements tags = parsedDoc.select("legend[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testLiTag(){

        Elements tags = parsedDoc.select("li[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("value", "1");



        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testLinkTag(){

        Elements tags = parsedDoc.select("link[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("href","https://www.w3schools.com/TAGS/default.ASP");
        Attributes.put("crossorigin", "");
        Attributes.put("hreflang", "en");
        Attributes.put("media", "all");
        Attributes.put("rel", "nofollow");
        Attributes.put("referrerpolicy", "no-referrer");
        Attributes.put("sizes", "100px");
        Attributes.put("type", "text/html");



        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testMainTag(){

        Elements tags = parsedDoc.select("main[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testMapTag(){

        Elements tags = parsedDoc.select("map[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("name","test");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testMarkTag(){

        Elements tags = parsedDoc.select("mark[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testMetaTag(){

        Elements tags = parsedDoc.select("meta[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("charset","utf-8");
        Attributes.put("content","test");
        Attributes.put("http-equiv","refresh");
        Attributes.put("name","test");



        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testMeterTag(){

        Elements tags = parsedDoc.select("meter[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("form","1");
        Attributes.put("high","0");
        Attributes.put("low","0");
        Attributes.put("max","0");
        Attributes.put("min","0");
        Attributes.put("optimum","0");
        Attributes.put("value","0");




        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testNavigationTag(){

        Elements tags = parsedDoc.select("nav[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testNoFramesTag(){

        Elements tags = parsedDoc.select("noframes");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testNoScriptTag(){

        Elements tags = parsedDoc.select("noscript[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testObjectTag(){

        Elements tags = parsedDoc.select("object");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testOrderedListTag(){

        Elements tags = parsedDoc.select("ol[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("reversed","0");
        Attributes.put("start","0");
        Attributes.put("type","1");



        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testOPTGroupTag(){

        Elements tags = parsedDoc.select("optgroup[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("disabled","");
        Attributes.put("label","test");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testOptionTag(){

        Elements tags = parsedDoc.select("option");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testOutputTag(){

        Elements tags = parsedDoc.select("output[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("for","1");
        Attributes.put("form","1");
        Attributes.put("name","test");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testParagraphTag(){

        Elements tags = parsedDoc.select("p[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testParameterTag(){

        Elements tags = parsedDoc.select("param[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("value","1");
        Attributes.put("name","test");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testPictureTag(){

        Elements tags = parsedDoc.select("picture[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testPreformattedTextTag(){

        Elements tags = parsedDoc.select("pre[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testProgressTag(){

        Elements tags = parsedDoc.select("progress[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("value","1");
        Attributes.put("max","0");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testQTag(){

        Elements tags = parsedDoc.select("q[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("cite","https://www.w3schools.com/TAGS/att_blockquote_cite.asp");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testRPTag(){

        Elements tags = parsedDoc.select("rp[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }


    }

    @Test
    public void testRTTag(){

        Elements tags = parsedDoc.select("rt[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testRubyTag(){

        Elements tags = parsedDoc.select("ruby[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }
//I AM HERE
    @Test
    public void testSTag(){

        Elements tags = parsedDoc.select("S[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testSampleTag(){

        Elements tags = parsedDoc.select("samp[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testScriptTag(){

        Elements tags = parsedDoc.select("script[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("async","");
        Attributes.put("crossorigin","");
        Attributes.put("defer","");
        Attributes.put("integrity","sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo");
        Attributes.put("nomodule","true");
        Attributes.put("referrerpolicy","no-referrer");
        Attributes.put("src","https://open.spotify.com/track/141GIDaZBy7Bn3I2NAFR31?si=71e860708f3c4b55");
        Attributes.put("type","text");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testSectionTag(){

        Elements tags = parsedDoc.select("section[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testSelectTag(){

        Elements tags = parsedDoc.select("select[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("autofocus","");
        Attributes.put("disabled","");
        Attributes.put("form","input_attr_form");
        Attributes.put("multiple","text");
        Attributes.put("name","test");
        Attributes.put("required","");
        Attributes.put("size","100");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testSmallTag(){

        Elements tags = parsedDoc.select("small[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testSourceTag(){

        Elements tags = parsedDoc.select("source[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("media","(min-width:650px)");
        Attributes.put("sizes","1");
        Attributes.put("src","img_orange_flowers.jpg");
        Attributes.put("srcset","img_pink_flowers.jpg");
        Attributes.put("type","image");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testSpanTag(){

        Elements tags = parsedDoc.select("span[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testStrikeTag(){

        Elements tags = parsedDoc.select("strike");
        assertTrue(tags.size()>0);

    }

    @Test
    public void testStrongTag(){

        Elements tags = parsedDoc.select("strong[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testStyleTag(){

        Elements tags = parsedDoc.select("style[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("media","test");
        Attributes.put("type","text");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testSubTag(){

        Elements tags = parsedDoc.select("sub[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testSummaryTag(){

        Elements tags = parsedDoc.select("summary[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testSupTag(){

        Elements tags = parsedDoc.select("sup[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testSVGTag(){

        Elements tags = parsedDoc.select("svg");
        assertTrue(tags.size()>0);


    }

    @Test
    public void testTableTag(){

        Elements tags = parsedDoc.select("table[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testTbodyTag(){

        Elements tags = parsedDoc.select("tbody[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testTDTag(){

        Elements tags = parsedDoc.select("td[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("colspan","1");
        Attributes.put("headers","test");
        Attributes.put("rowspan","1");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testTemplateTag(){

        Elements tags = parsedDoc.select("template[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);


        }

    }

    @Test
    public void testTextAreaTag(){

        Elements tags = parsedDoc.select("textarea[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("autofocus","");
        Attributes.put("cols","100");
        Attributes.put("dirname","test.dir");
        Attributes.put("disbaled","");
        Attributes.put("form","input_attr_form");
        Attributes.put("maxlength","100");
        Attributes.put("name","test");
        Attributes.put("placeholder","text");
        Attributes.put("readonly","");
        Attributes.put("required","");
        Attributes.put("rows","100");
        Attributes.put("wrap","hard");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testTfootTag(){

        Elements tags = parsedDoc.select("tfoot[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testTHTag(){

        Elements tags = parsedDoc.select("th[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("colspan","1");
        Attributes.put("headers","test");
        Attributes.put("rowspan","1");
        Attributes.put("abbr","test");
        Attributes.put("scope","col");

        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }
    }

    @Test
    public void testTHeadTag(){

        Elements tags = parsedDoc.select("thead[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testTimeTag(){

        Elements tags = parsedDoc.select("time[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("datetime","2017-02-14");


        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testTitleTag(){

       Elements tags = parsedDoc.select("title[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testTRTag(){

        Elements tags = parsedDoc.select("tr[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testTrackTag(){

        Elements tags = parsedDoc.select("track[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("default","");
        Attributes.put("kind","subtitles");
        Attributes.put("label","test");
        Attributes.put("src","https://open.spotify.com/track/141GIDaZBy7Bn3I2NAFR31?si=71e860708f3c4b55");
        Attributes.put("srclang","en");


        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testTTTag(){

        Elements tags = parsedDoc.select("tt");
        assertTrue(tags.size()>0);

    }

    @Test
    public void testUTag(){

        Elements tags = parsedDoc.select("u[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testULTag(){

        Elements tags = parsedDoc.select("ul[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testVarTag(){

        Elements tags = parsedDoc.select("var[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }

    @Test
    public void testVideoTag(){

        Elements tags = parsedDoc.select("video[class=unit-test]");
        assertTrue(tags.size() > 0);
        Map<String, String> Attributes = new HashMap<String, String>();
        Attributes.put("autoplay","true");
        Attributes.put("controls","true");
        Attributes.put("height","100px");
        Attributes.put("loop","true");
        Attributes.put("muted","true");
        Attributes.put("poster","https://www.google.com/imgres?imgurl=https%3A%2F%2Fcdn.britannica.com%2F92%2F100692-050-5B69B59B%2FMallard.jpg&imgrefurl=https%3A%2F%2Fwww.britannica.com%2Fanimal%2Fduck&tbnid=cv-8vEQ0udAypM&vet=12ahUKEwigtsHGxI_6AhWImmoFHZ_-CKQQMygAegUIARDjAQ..i&docid=IpKgqf0_gTf_MM&w=1600&h=1423&q=duck&ved=2ahUKEwigtsHGxI_6AhWImmoFHZ_-CKQQMygAegUIARDjAQ");
        Attributes.put("preload","none");
        Attributes.put("src","https://open.spotify.com/track/141GIDaZBy7Bn3I2NAFR31?si=71e860708f3c4b55");
        Attributes.put("width","100px");


        for (Element tag : tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(tag, globalAttributes);
            verifyAttributes(attrs, eventAttribute);
            verifyAttributes(attrs, Attributes);
        }

    }

    @Test
    public void testWBRTag(){

        Elements tags = parsedDoc.select("wbr[class=unit-test]");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }





}

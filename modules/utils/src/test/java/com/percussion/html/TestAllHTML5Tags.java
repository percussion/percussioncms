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
import java.util.Objects;
import java.util.Scanner;

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

    String[] globalAttributes= {"accesskey" , "class" , "contenteditable", "data-*", "dir", "draggable", "hidden", "id", "lang", "spellcheck", "style", "tabindex", "title", "translate"};
    String[] eventAttribute = {"onafterprint", "onbeforeprint", "onbeforeunload", "onerror", "onhashchange", "onload", "onmessage", "onoffline", "ononline", "onpagehide", "onpageshow", "onpopstate", "onresize", "onstorage", "onunload", "onblur", "onchange", "oncontextmenu", "onfocus", "oninput", "oninput", "oninvalid", "onreset", "onsearch", "onselect", "onsubmit", "onkeydown", "onkeypress", "onkeyup", "onclick", "ondblclick", "onmousedown", "onmousemove", "onmouseout", "onmouseover", "onmouseup", "onmousewheel", "onwheel", "ondrag", "ondragend", "ondragenter", "ondragleave", "ondragover", "ondragstart", "ondrop", "onscroll", "oncopy", "oncut", "onpaste", "onabort", "oncanplay", "oncanplaythrough", "oncuechange", "ondurationchange", "onemptied", "onended", "onerror", "onloadeddata", "onloadedmetadata", "onloadstart", "onpause", "onplay", "onplaying", "onprogress", "onratechange", "onseeked", "onseeking", "onstalled", "onsuspend", "ontimeupdate", "onvolumechange", "onwaiting", "ontoggle"};

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

    }

    @Test
    public void testParse(){

        System.out.println(parsedHTML);
        assertNotNull(parsedHTML);
        assertTrue(parsedHTML.length()>1);
        testAsideTag();


    }


    private void verifyAttributes(Attributes attributes,String[] attrList){

        for (String attrName:attrList) {
            String attrValue = attributes.get(attrName);
            assertTrue(attrValue != null);
            assertTrue(attrValue.contains(attrName));

        }

    }


    @Test
    public void testAsideTag(){
        Elements tags = parsedDoc.select("aside");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }
    }



    @Test
    public void testATag(){

    Elements tags = parsedDoc.select("a");
    assertTrue(tags.size()>0);
        String[] aAttributes = {"download", "href", "hreflang", "media", "ping", "referrerpolicy", "rel", "target", "type"};
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,aAttributes);
        }
    }

    public void testAbbrTag(){

        Elements tags = parsedDoc.select("abbr");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }

    @Test
    public void testAcronymTag(){

        Elements tags = parsedDoc.select("acronym");
        assertTrue(tags.size()>0);


    }

    @Test
    public void testAddressTag(){

        Elements tags = parsedDoc.select("address");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
        }

    }

    @Test
    public void testAppletTag(){

        Elements tags = parsedDoc.select("applet");
        assertTrue(tags.size()>0);

    }

    @Test
    public void testAreaTag(){

        Elements tags = parsedDoc.select("Area");
        assertTrue(tags.size()>0);
        String[] areaAttributes = {"download", "href", "hreflang", "media", "alt","coords", "referrerpolicy", "rel", "shape", "target", "type"};
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,areaAttributes);
        }

    }

    @Test
    public void testArticleTag(){

        Elements tags = parsedDoc.select("article");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
        }

    }

    @Test
    public void testAudioTag(){

        Elements tags = parsedDoc.select("audio");
        assertTrue(tags.size()>0);
        String[] audioAttributes = {"autoplay", "controls", "loop", "muted", "preload", "src"};
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,audioAttributes);
        }

    }

    @Test
    public void testBTag(){

        Elements tags = parsedDoc.select("b");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
        }

    }

    @Test
    public void testBaseTag(){

        Elements tags = parsedDoc.select("base");
        assertTrue(tags.size()>0);
        String[] baseAttributes = {"href", "target"};
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,baseAttributes);
        }

    }

    @Test
    public void testBaseFontTag(){

        Elements tags = parsedDoc.select("basefont");
        assertTrue(tags.size()>0);
    }

    @Test
    public void testBDITag(){

        Elements tags = parsedDoc.select("bdi");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
        }

    }

    @Test
    public void testBDOTag(){

        Elements tags = parsedDoc.select("bdo");
        assertTrue(tags.size()>0);
        String[] bdoAttributes = {"dir"};
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,bdoAttributes);
        }

    }

    @Test
    public void testBIGTag(){

        Elements tags = parsedDoc.select("big");
        assertTrue(tags.size()>0);


    }

    @Test
    public void testBlockQuoteTag(){

        Elements tags = parsedDoc.select("blockquote");
        assertTrue(tags.size()>0);
        String[] bigAttributes = {"cite"};
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,bigAttributes);
        }

    }

    @Test
    public void testBodyTag(){

        Elements tags = parsedDoc.select("Body");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
        }

    }

    @Test
    public void testBRTag(){

        Elements tags = parsedDoc.select("br");
        assertTrue(tags.size()>0);
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);

        }

    }
    @Test
    public void testButtonTag(){

        Elements tags = parsedDoc.select("button");
        assertTrue(tags.size()>0);
        String[] buttonAttributes = {"autofocus", "disabled", "form", "formaction", "formenctype", "formmethod", "formnovalidate", "formtarget", "name", "type", "value"};
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,buttonAttributes);
        }

    }
    @Test
    public void testCanvasTag(){

        Elements tags = parsedDoc.select("canvas");
        assertTrue(tags.size()>0);
        String[] canvasAttributes = {"height", "width"};
        for (Element tag:tags) {
            Attributes attrs = tag.attributes();
            assertTrue(attrs.size()==86);
            verifyAttributes(attrs,globalAttributes);
            verifyAttributes(attrs,eventAttribute);
            verifyAttributes(attrs,canvasAttributes);
        }

    }

    @Test
    public void testCaptionTag(){

        Elements tags = parsedDoc.select("caption");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testCenterTag(){

        Elements tags = parsedDoc.select("center");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testCiteTag(){

        Elements tags = parsedDoc.select("cite");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testCodeTag(){

        Elements tags = parsedDoc.select("code");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testColumnTag(){

        Elements tags = parsedDoc.select("col");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testColGroupTag(){

        Elements tags = parsedDoc.select("colgroup");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDataTag(){

        Elements tags = parsedDoc.select("data");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDataListTag(){

        Elements tags = parsedDoc.select("datalist");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDDTag(){

        Elements tags = parsedDoc.select("dd");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDeleteTag(){

        Elements tags = parsedDoc.select("del");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDetailsTag(){

        Elements tags = parsedDoc.select("details");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDefineTag(){

        Elements tags = parsedDoc.select("dfn");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDialogTag(){

        Elements tags = parsedDoc.select("dialog");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDirTag(){

        Elements tags = parsedDoc.select("dir");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDivTag(){

        Elements tags = parsedDoc.select("div");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDLTag(){

        Elements tags = parsedDoc.select("dl");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testDTTag(){

        Elements tags = parsedDoc.select("dt");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testEmphasizeTag(){

        Elements tags = parsedDoc.select("em");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testEmbedTag(){

        Elements tags = parsedDoc.select("embed");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testFieldSetTag(){

        Elements tags = parsedDoc.select("fieldset");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testFigCaptionTag(){

        Elements tags = parsedDoc.select("figcaption");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testFigureTag(){

        Elements tags = parsedDoc.select("figure");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testFontTag(){

        Elements tags = parsedDoc.select("font");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testFooterTag(){

        Elements tags = parsedDoc.select("footer");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testFormTag(){

        Elements tags = parsedDoc.select("form");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

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

        Elements tags = parsedDoc.select("h1");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testH2Tag(){

        Elements tags = parsedDoc.select("h2");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testH3Tag(){

        Elements tags = parsedDoc.select("h3");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testH4Tag(){

        Elements tags = parsedDoc.select("h4");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testH5Tag(){

        Elements tags = parsedDoc.select("h5");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testH6Tag(){

        Elements tags = parsedDoc.select("h6");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testHeadTag(){

        Elements tags = parsedDoc.select("head");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testHeaderTag(){

        Elements tags = parsedDoc.select("header");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testHRTag(){

        Elements tags = parsedDoc.select("hr");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testHtmlTag(){

        Elements tags = parsedDoc.select("html");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testITag(){

        Elements tags = parsedDoc.select("i");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testIFrameTag(){

        Elements tags = parsedDoc.select("iframe");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testImageTag(){

        Elements tags = parsedDoc.select("img");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testInputTag(){

        Elements tags = parsedDoc.select("input");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testInsTag(){

        Elements tags = parsedDoc.select("ins");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testKBDTag(){

        Elements tags = parsedDoc.select("kbd");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testLabelTag(){

        Elements tags = parsedDoc.select("label");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testLegendTag(){

        Elements tags = parsedDoc.select("legend");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testLiTag(){

        Elements tags = parsedDoc.select("li");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testLinkTag(){

        Elements tags = parsedDoc.select("Link");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testMainTag(){

        Elements tags = parsedDoc.select("main");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testMapTag(){

        Elements tags = parsedDoc.select("map");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testMarkTag(){

        Elements tags = parsedDoc.select("map");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testMetaTag(){

        Elements tags = parsedDoc.select("meta");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testMeterTag(){

        Elements tags = parsedDoc.select("meter");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testNavigationTag(){

        Elements tags = parsedDoc.select("nav");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testNoFramesTag(){

        Elements tags = parsedDoc.select("noframes");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testNoScriptTag(){

        Elements tags = parsedDoc.select("noscript");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testObjectTag(){

        Elements tags = parsedDoc.select("object");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testOrderedListTag(){

        Elements tags = parsedDoc.select("ol");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testOPTGroupTag(){

        Elements tags = parsedDoc.select("optgroup");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testOptionTag(){

        Elements tags = parsedDoc.select("option");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testOutputTag(){

        Elements tags = parsedDoc.select("output");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testParagraphTag(){

        Elements tags = parsedDoc.select("p");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testParameterTag(){

        Elements tags = parsedDoc.select("param");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testPictureTag(){

        Elements tags = parsedDoc.select("picture");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testPreformattedTextTag(){

        Elements tags = parsedDoc.select("pre");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testProgressTag(){

        Elements tags = parsedDoc.select("progress");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testQTag(){

        Elements tags = parsedDoc.select("q");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testRPTag(){

        Elements tags = parsedDoc.select("rp");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testRTTag(){

        Elements tags = parsedDoc.select("rt");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testRubyTag(){

        Elements tags = parsedDoc.select("ruby");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSTag(){

        Elements tags = parsedDoc.select("s");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSampleTag(){

        Elements tags = parsedDoc.select("samp");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testScriptTag(){

        Elements tags = parsedDoc.select("script");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSectionTag(){

        Elements tags = parsedDoc.select("section");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSelectTag(){

        Elements tags = parsedDoc.select("select");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSmallTag(){

        Elements tags = parsedDoc.select("small");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSourceTag(){

        Elements tags = parsedDoc.select("source");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSpanTag(){

        Elements tags = parsedDoc.select("span");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testStrikeTag(){

        Elements tags = parsedDoc.select("strike");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testStrongTag(){

        Elements tags = parsedDoc.select("strong");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testStyleTag(){

        Elements tags = parsedDoc.select("style");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSubTag(){

        Elements tags = parsedDoc.select("sub");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSummaryTag(){

        Elements tags = parsedDoc.select("summary");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSupTag(){

        Elements tags = parsedDoc.select("sup");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testSVGTag(){

        Elements tags = parsedDoc.select("svg");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTableTag(){

        Elements tags = parsedDoc.select("table");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTbodyTag(){

        Elements tags = parsedDoc.select("tbody");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTDTag(){

        Elements tags = parsedDoc.select("td");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTemplateTag(){

        Elements tags = parsedDoc.select("template");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTextAreaTag(){

        Elements tags = parsedDoc.select("textarea");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTfootTag(){

        Elements tags = parsedDoc.select("tfoot");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTHTag(){

        Elements tags = parsedDoc.select("th");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTHeadTag(){

        Elements tags = parsedDoc.select("thead");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTimeTag(){

        Elements tags = parsedDoc.select("time");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTRTag(){

        Elements tags = parsedDoc.select("tr");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTrackTag(){

        Elements tags = parsedDoc.select("track");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testTTTag(){

        Elements tags = parsedDoc.select("tt");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testUTag(){

        Elements tags = parsedDoc.select("u");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testULTag(){

        Elements tags = parsedDoc.select("ul");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testVarTag(){

        Elements tags = parsedDoc.select("var");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testVideoTag(){

        Elements tags = parsedDoc.select("video");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }

    @Test
    public void testWBRTag(){

        Elements tags = parsedDoc.select("wbr");
        assertTrue(tags.size()>0);
        //TODO: Test attributes of a tag

    }





}

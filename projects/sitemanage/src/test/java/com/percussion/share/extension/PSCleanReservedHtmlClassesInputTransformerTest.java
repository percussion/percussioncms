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
package com.percussion.share.extension;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author JaySeletz
 *
 */
public class PSCleanReservedHtmlClassesInputTransformerTest
{
    /**
     * Test method for {@link com.percussion.share.extension.PSCleanReservedHtmlClassesInputTransformer#processContent(java.lang.String)}.
     */
    @Test
    public void testCleanReservedHtmlClasses()
    {
        PSCleanReservedHtmlClassesInputTransformer cleaner = new PSCleanReservedHtmlClassesInputTransformer();
        
        // test one element no classes
        String html = "<div class=\"notPerc\">test</div>";
        String result = cleaner.processContent(html);
        assertEquals(html, result);
        
        // test multiple elements no classes
        html = "<div id=\"foo\" class=\"notPerc\">test</div><div>content</div>";
        result = cleaner.processContent(html);
        assertEquals(html, result);
        
        // test with one class
        html = "<div class=\"perc-region\">test</div>";
        String expected = "<div>test</div>";
        result = cleaner.processContent(html);
        assertEquals(jsoupResult(expected), result);
        
        // test multiple classes
        html = "<div class=\"perc-region perc-foo perc-fixed\">test</div><div class=\"perc-widget bar\">content</div>";
        expected = "<div class=\"perc-foo\">test</div><div class=\"bar\">content</div>";
        result = cleaner.processContent(html);
        assertEquals(jsoupResult(expected), result);
        
        // test with preserve
        html = "<div class=\"perc-region perc-foo perc-fixed\">test</div><div class=\"perc-widget bar\">content</div><PRESERVE><div class=\"perc-region\">foo</div><%@ page import=\"com.percussion.*\" %></PRESERVE>";
        expected = "<div class=\"perc-foo\">test</div><div class=\"bar\">content</div><PRESERVE><div class=\"perc-region\">foo</div><%@ page import=\"com.percussion.*\" %></PRESERVE>";
        result = cleaner.processContent(html);
        assertEquals(normalizeResult(expected), normalizeResult(result));
        
        // test with empty iframe
        html = "<iframe width=\"560\" height=\"315\" src=\"//www.youtube.com/embed/zCFDkj2JtyA\" frameborder=\"0\"></iframe><p>some other text</p>";
        expected = "<iframe width=\"560\" height=\"315\" src=\"//www.youtube.com/embed/zCFDkj2JtyA\" frameborder=\"0\">" + PSCleanReservedHtmlClassesInputTransformer.EMPTY_IFRAME_TEXT + "</iframe><p>some other text</p>";
        result = cleaner.processContent(html);
        assertEquals(normalizeResult(expected), normalizeResult(result));

        //test with iframe content
        html = "<iframe width=\"560\" height=\"315\" src=\"//www.youtube.com/embed/zCFDkj2JtyA\" frameborder=\"0\">Some text</iframe><p>some other text</p>";
        expected = "<iframe width=\"560\" height=\"315\" src=\"//www.youtube.com/embed/zCFDkj2JtyA\" frameborder=\"0\">Some text</iframe><p>some other text</p>";
        result = cleaner.processContent(html);
        assertEquals(normalizeResult(expected), normalizeResult(result));

        //test with iframe spaces
        html = "<iframe width=\"560\" height=\"315\" src=\"//www.youtube.com/embed/zCFDkj2JtyA\" frameborder=\"0\">     </iframe><p>some other text</p>";
        expected = "<iframe width=\"560\" height=\"315\" src=\"//www.youtube.com/embed/zCFDkj2JtyA\" frameborder=\"0\">" + PSCleanReservedHtmlClassesInputTransformer.EMPTY_IFRAME_TEXT + "</iframe><p>some other text</p>";
        result = cleaner.processContent(html);
        assertEquals(normalizeResult(expected), normalizeResult(result));

    }


    private Object normalizeResult(String expected)
    {
        return StringUtils.remove(StringUtils.remove(expected, "\n "), "\n");
    }


    private Object jsoupResult(String result)
    {
        return Jsoup.parseBodyFragment(result).body().html();
    }

}

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

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
package com.percussion.search.lucene.textconverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

import com.percussion.utils.tools.IPSUtilsConstants;

/**
 * Unit test for html text conversion.
 *
 */
public class PSTextConverterHtmlTest extends TestCase
{
   /**
    * Html text conversion test.
    * @throws Exception
    */
   public void testHtmlConversion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = new ByteArrayInputStream(HTML.getBytes(IPSUtilsConstants.RX_JAVA_ENC));
         String text = new PSTextConverterHtml().getConvertedText(is, "");
         assertEquals(StringUtils.trim(text), "text from html");
      }
      finally
      {
         if (is != null)
         {
            is.close();
         }
      }

   }
   
   /**
    * Bad html text conversion test.
    * @throws Exception
    */
   public void testBadHtmlConversion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = new ByteArrayInputStream(BAD_HTML.getBytes(IPSUtilsConstants.RX_JAVA_ENC));
         String text = new PSTextConverterHtml().getConvertedText(is, "");
         assertEquals(StringUtils.trim(text), "text from bad html");
      }
      finally
      {
         if (is != null)
         {
            is.close();
         }
      }

   }
   
   /**
    * Bad html text conversion test.
    * @throws Exception
    */
   public void testBugCML4823FacebookLike() throws Exception
   {
      InputStream is = null;
      try
      {
         is = new ByteArrayInputStream(HTML5.getBytes(IPSUtilsConstants.RX_JAVA_ENC));
         String text = new PSTextConverterHtml().getConvertedText(is, "");
         assertEquals(StringUtils.trim(text), "text from bad html Hello <http://blah.com>");
      }
      finally
      {
         if (is != null)
         {
            is.close();
         }
      }

   }

   private static final String HTML = "<!-- Only xhtml will work w/ our text converter -->"
      + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
      + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
      + "<html>"
      + "<head>"
      + "<title></title>"
      + "<meta name=\"generator\" content=\"editplus\"/>"
      + "<meta name=\"author\" content=\"\"/>"
      + "<meta name=\"keywords\" content=\"\"/>"
      + "<meta name=\"description\" content=\"\"/>"
      + "</head>"
      + "<body>"
      + "text from html"
      + "</body>"
      + "</html>";
   
   private static final String BAD_HTML = "<!-- Only xhtml will work w/ our text converter -->"
      + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
      + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
      + "<html>"
      + "<head>"
      + "<title></title>"
      + "<meta name=\"generator\" content=\"editplus\"/>"
      + "<meta name=\"author\" content=\"\"/>"
      + "<meta name=\"keywords\" content=\"\"/>"
      + "<meta name=\"description\" content=\"\"/>"
      + "</head>"
      + "<body>"
      + "text from bad html"
      + "</body>";
   
   private static final String HTML5 = "<!-- Only xhtml will work w/ our text converter -->"
       + "<!DOCTYPE>"
       + "<html>"
       + "<head>"
       + "<title></title>"
       + "<meta name=\"generator\" content=\"editplus\"/>"
       + "<meta name=\"author\" content=\"\">"
       + "<meta name=\"keywords\" content=\"\">"
       + "<meta name=\"description\" content=\"\"/>"
       + "</head>"
       + "<body>"
       + "text from bad html"
       + " <a href=\"http://blah.com\" fb:like=\"yessem\">Hello</a>"
       + "</body>";
}

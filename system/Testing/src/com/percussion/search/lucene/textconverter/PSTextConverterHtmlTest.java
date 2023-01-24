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

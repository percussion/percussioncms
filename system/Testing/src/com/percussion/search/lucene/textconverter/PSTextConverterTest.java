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
package com.percussion.search.lucene.textconverter;

import com.percussion.search.lucene.IPSLuceneConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;

import junit.framework.TestCase;

/**
 * Unit test for text convertion classes.
 *
 */
public class PSTextConverterTest extends TestCase
{
   /**
    * MS Word file text conversion test.
    * @throws Exception
    */
   public void testWordConvertion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("word.doc");
         String text = new PSTextConverterMsWord().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_MSWORD);
         assertEquals(StringUtils.trim(text), "text from word");
         
         String wordx = "Hello World";
         is = loadFile("word.docx");
         text = new PSTextConverterMsWord().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSWORD_DOC);
         assertEquals(StringUtils.trim(text), wordx);
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
    * MS Word file text conversion test.
    * @throws Exception
    */
   public void testTikaWordConvertion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("word.doc");
         String text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_MSWORD);
         assertEquals(StringUtils.trim(text), "text from word");
         
         String wordx = "Hello World";
         is = loadFile("word.docx");
         text = new PSTextConverterMsWord().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSWORD_DOC);
         assertEquals(StringUtils.trim(text), wordx);
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
    * MS Excel file text conversion test.
    * @throws Exception
    */
   public void testExcelConversion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("excel.xls");
         String text = new PSTextConverterMsExcel().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_EXCEL);
         assertEquals(StringUtils.trim(text), "text from xls");
         
         String excelx1 = "Hello";
         String excelx2 = "World";
         is = loadFile("excel.xlsx");
         text = new PSTextConverterMsExcel().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSEXCEL_SHEET);
         assertTrue(text.indexOf(excelx1) != -1);
         assertTrue(text.indexOf(excelx2) != -1);
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
    * MS Excel file text conversion test.
    * @throws Exception
    */
   public void testTikaExcelConversion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("excel.xls");
         String text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_EXCEL);
         assertTrue(text.indexOf("text from xls") != -1);
         
         String excelx1 = "Hello";
         String excelx2 = "World";
         is = loadFile("excel.xlsx");
         text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSEXCEL_SHEET);
         assertTrue(text.indexOf(excelx1) != -1);
         assertTrue(text.indexOf(excelx2) != -1);
      }
      finally
      {
         if (is != null)
         {
            is.close();
         }
      }
   }
   
   public void testTikaUnsupportedMimeTypeConversion() throws Exception
   {
      InputStream is = null;
      
      try
      {
         is = loadFile("jpeg.jpg");
         String text = new PSTikaTextConvertor().getConvertedText(is, "image/jpeg");
         assertTrue(StringUtils.isEmpty(text));
      }
      finally
      {
         IOUtils.closeQuietly(is);
      }
   }

   
   /**
    * Pdf file text conversion test.
    * @throws Exception
    */
   public void testPdfConvertion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("pdf.pdf");
         String text = new PSTextConverterPdf().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_PDF);
         assertEquals(StringUtils.trim(text), "text from pdf");
      }
      finally
      {
         if (is != null)
         {
            is.close();
         }
      }

   }
   
   public void testTikaPdfConvertion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("pdf.pdf");
         String text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_PDF);
         assertEquals(StringUtils.trim(text), "text from pdf");
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
    * Html file text conversion test.
    * @throws Exception
    */
   public void testHtmlConversion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("html.html");
         String text = new PSTextConverterHtml().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_TEXT_BY_HTML);
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
    * Html file text conversion test.
    * @throws Exception
    */
   public void testTikaHtmlConversion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("html.html");
         String text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_TEXT_BY_HTML);
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
    * Power Point file text conversion test.
    * @throws Exception
    */
   public void testPowerPointConvertion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("ppt.ppt");
         String text = new PSTextConverterMsPowerPoint().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_MSPOWERPOINT);
         assertTrue(text.indexOf("text from power point") != -1);
         
         String pptx = "Hello World";
         is = loadFile("ppt.pptx");
         text = new PSTextConverterMsPowerPoint().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSPOWERPOINT_PRES);
         assertTrue(text.indexOf(pptx) != -1);
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
    * Power Point file text conversion test.
    * @throws Exception
    */
   public void testTikaPowerPointConvertion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("ppt.ppt");
         String text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_MSPOWERPOINT);
         assertTrue(text.indexOf("text from power point") != -1);
         
         String pptx = "Hello World";
         is = loadFile("ppt.pptx");
         text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSPOWERPOINT_PRES);
         assertTrue(text.indexOf(pptx) != -1);
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
    * RTF file text conversion test.
    * @throws Exception
    */
   public void testRtfConversion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("rtf.rtf");
         String text = new PSTextConverterRtf().getConvertedText(is, "");
         assertEquals(StringUtils.trim(text), "text from rtf");
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
    * RTF file text conversion test.
    * @throws Exception
    */
   public void testTikaRtfConversion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("rtf.rtf");
         String text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_RTF);
         assertEquals(StringUtils.trim(text), "text from rtf");
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
    * Xml file text conversion test.
    * @throws Exception
    */
   public void testXmlConvertion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("xml.xml");
         String text = new PSTextConverterXml().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_XML);
         assertEquals(StringUtils.trim(text), "text from xml");
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
    * Xml file text conversion test.
    * @throws Exception
    */
   public void testTikaXmlConvertion() throws Exception
   {
      InputStream is = null;
      try
      {
         is = loadFile("xml.xml");
         String text = new PSTikaTextConvertor().getConvertedText(is, IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_XML);
         assertEquals(StringUtils.trim(text), "text from xml");
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
    * Convenient function to load the files with the supplied name.
    * @param filename name of the file.
    * @return InputStream
    * @throws Exception
    */
   private InputStream loadFile(String filename) throws Exception
   {
      return new FileInputStream(new File(dataDir + filename));
   }

   /**
    * Directory of the data files for conversion.
    */
   private static String dataDir = "UnitTestResources/com/percussion/"
         + "search/lucene/converters/";
}

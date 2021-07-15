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

package com.percussion.xsl.encoding;

import com.icl.saxon.charcode.PluggableCharacterSet;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSXslStyleSheetMerger;
import com.percussion.util.IOTools;
import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the PS...CharacterSet classes; each of which provides a new character
 * encoding output method for the Saxon XSLT processor.
 */
@Category(UnitTest.class)
public class PSCharacterSetTest
{

   public PSCharacterSetTest()
   {

   }

   /**
    * Performs the setup required by the tests: defining system properties for
    * each character set.
    */
   @BeforeClass
   public static void setup() throws Exception
   {
      System.setProperty( "encoding.windows-1251",
         PSCp1251CharacterSet.class.getName() );
      System.setProperty( "encoding.windows-1252",
         PSCp1252CharacterSet.class.getName() );
      System.setProperty( "encoding.Big5",
         PSBig5CharacterSet.class.getName() );
      System.setProperty( "encoding.SJIS",
         PSSJISCharacterSet.class.getName() );
      System.setProperty( "encoding.EUC-CN",
         PSEUC_CNCharacterSet.class.getName() );
      System.setProperty( "encoding.EUC-JP",
         PSEUC_JPCharacterSet.class.getName() );
      System.setProperty( "encoding.EUC-KR",
         PSEUC_KRCharacterSet.class.getName() );
      System.setProperty( "encoding.EUC-TW",
         PSEUC_TWCharacterSet.class.getName() );
      System.setProperty( "encoding.UTF-16",
         PSUTF16CharacterSet.class.getName() );
      System.setProperty( "encoding.UTF-16BE",
         PSUTF16BECharacterSet.class.getName() );
      System.setProperty( "encoding.UTF-16LE",
         PSUTF16LECharacterSet.class.getName() );
   }



   /**
    * Make sure each characterset class can be constructed without error.
    * This tests that the resource file is available and readable.
    * 
    * @throws IOException if one of the constructors reports an error.
    */
   @Test
   public void testCtors() throws IOException
   {
      PluggableCharacterSet charset;

     charset = new PSBig5CharacterSet();
     assertNotNull( charset.getEncodingName() );
     assertTrue( charset.inCharset( 0 ) );

      charset = new PSCp1251CharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSCp1252CharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSEUC_CNCharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSEUC_JPCharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSEUC_KRCharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSEUC_TWCharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSSJISCharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSUTF16CharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSUTF16BECharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );

      charset = new PSUTF16LECharacterSet();
      assertNotNull( charset.getEncodingName() );
      assertTrue( charset.inCharset( 0 ) );
   }


   /**
    * Tests that each character set class can be used in a transformation
    * without causing an error and that the output is actually in the expected
    * encoding.
    * 
    * @throws Exception if any error occurs
    */
   @Test
   public void testOutput() throws Exception
   {
      // transform a sample document in the desired encoding through an
      // identity stylesheet, and see if the output is byte-for-byte the same
      // as the input.
      runTest( "Google-windows-1251.xhtm", "windows-1251", "copy.xsl" );
      runTest( "funky-Cp1252.xhtm", "windows-1252", "copy.xsl" );
      runTest( "Google-Big5.xhtm", "Big5", "copy.xsl" );
      runTest( "Yahoo-Big5.xhtm", "Big5", "copy.xsl" );
      runTest( "Google-SJIS.xhtm", "SJIS", "copy.xsl" );
      runTest( "Google-GB2312.xhtm", "EUC-CN", "copy.xsl" );
      runTest( "IBM-GB2312.xhtm", "EUC-CN", "copy.xsl" );
      runTest( "Sample-euc_jp.xml", "EUC-JP", "copy.xsl" );
      runTest( "Yahoo-EUC-JP.xhtm", "EUC-JP", "copy.xsl" );
      runTest( "Yahoo-EUC-KR.xhtm", "EUC-KR", "copy.xsl" );
      runTest( "Yahoo-UTF16BE.xhtm", "UTF-16BE", "copy.xsl" );
      runTest( "Yahoo-UTF16LE.xhtm", "UTF-16LE", "copy.xsl" );
      
      // there is no test for EUC_TW, as I was unable to find a sample document
      // using that encoding.
   }


   /**
    * Builds an XML document from the specified file, transforms it using the
    * specified stylesheet, and asserts that the generated output is byte-for-
    * byte identical to the specified file.
    * 
    * @param testFile of the file containing the XML to be transformed and the 
    * expected bytes of the result, assumed not <code>null</code> or empty.
    * @param encoding name of the character encoding scheme of the test file,
    * assumed not <code>null</code> or empty.  Must be one of Java's
    * supported character encodings.
    * 
    * @param stylesheet name of the file containing the XSLT stylesheet that 
    * performs an identify copy.  Assumed not <code>null</code>.
    * 
    * @throws Exception if any error occurs
    */
   private static void runTest(String testFile, String encoding,
                               String stylesheet)
      throws Exception
   {
      String xslEncoding = encoding;
      //for both UTF-16BE and UTF-16LE java encoding is UTF-16 and stylesheet 
      //encoding remains unchanged
      if(encoding.toUpperCase().startsWith("UTF-16"))
         encoding = "UTF-16";

      InputStream input = null;
      try
      {
         Document sourceDoc;
         byte[] output;
         input = PSCharacterSetTest.class.getResourceAsStream(RESOURCE_PATH + testFile );
         sourceDoc = PSXmlDocumentBuilder.createXmlDocument(
            new InputStreamReader( input, encoding ), false );
         input.close();
         input = null;

         // test the straight copy encoding->encoding
         output = transform( sourceDoc, stylesheet, encoding );
         boolean outputEqualsInput = compare( testFile, output, encoding );
         if (!outputEqualsInput)
            writeOutputAsTempFile( output ); // assist debugging
         assertTrue( "result of transforming " + testFile + " with " + 
            stylesheet + " does not equal source", outputEqualsInput );

         // test the two-step copy encoding->UTF8->encoding
         byte[] intertial = transform( sourceDoc, "copy.xsl", "UTF-8");
         Document intertialDoc = PSXmlDocumentBuilder.createXmlDocument(
            new InputStreamReader( new ByteArrayInputStream( intertial ),
               "UTF8" ), false );
         output = transform( intertialDoc, stylesheet, encoding);
         outputEqualsInput = compare( testFile, output, encoding );
         if (!outputEqualsInput)
            writeOutputAsTempFile( output ); // assist debugging
         assertTrue( "result of transforming " + testFile +
            " through UTF-8 and back with " + stylesheet +
            " does not equal source", outputEqualsInput );

      } catch (Exception e)
      {
         System.err.println( "FAILED while testing " + testFile );
         throw e;
      } finally
      {
         try
         {
            if (input != null) input.close();
         } catch (IOException e)
         {
            /* ignore */
         }
      }
   }

   /**
    * Writes a byte array to a temporary file in the {@link #RESOURCE_PATH}
    * directory with a base name of <code>failed-output</code> and an extension
    * of <code>xml</code>.  Any errors that occur while writing this file are
    * ignored.
    * @param output the byte array to be dumped to a temporary file, assumed
    * not <code>null</code>.
    * 
    */
   private static void writeOutputAsTempFile(byte[] output)
   {
      OutputStream outStream = null;
      try
      {
         File f = File.createTempFile(
                 "failed-output", ".xml");
         f.deleteOnExit();

         outStream = new FileOutputStream( f  );
         outStream.write( output );
      } catch (IOException e)
      {
         // ignore it
      } finally
      {
         if (outStream != null)
            try
            {
               outStream.close();
            } catch (IOException e)
            {
               // ignore it
            }
      }
   }


   /**
    * Transforms <code>inDoc</code> by applying the supplied XSLT stylesheet.
    *
    * @param inDoc the source to be transformed, assumed not <code>null</code>.
    * @param stylesheet name of the file containing the XSLT stylesheet that 
    * will be applied.  Assumed not <code>null</code>.
    * @param encoding
    *
    * @return the output from the transformation, never <code>null</code>.
    *
    * @throws PSConversionException if an error occurs while transforming
    * @throws MalformedURLException if the stylesheet URL is invalid
    */
   private static byte[] transform(Document inDoc, String stylesheet, String encoding)
      throws PSConversionException, MalformedURLException
   {
      ByteArrayOutputStream output = null;
      try
      {
         Map params = new HashMap();
         params.put("encoding", encoding);
         output = new ByteArrayOutputStream();
         URL styleURL =  PSCharacterSetTest.class.getResource(RESOURCE_PATH + stylesheet );
         PSXslStyleSheetMerger merger = new PSXslStyleSheetMerger();
         merger.merge(null, inDoc, output, styleURL, params.entrySet()
               .iterator(), null);
         return output.toByteArray();
      } finally
      {
         try
         {
            if (output != null) output.close();
         } catch (IOException e)
         {
            /* ignore */
         }
      }
   }


   /**
    * Compares the characters of the expected file with the characters in the 
    * supplied array, converting bytes to characters using the specified 
    * encoding.  Line break characters in either source will be normalized to
    * LF ('\n') characters.
    * 
    * @param expectedFile name of the file containing the expected bytes,
    * assumed not <code>null</code>.
    * @param actualBytes the array of bytes to be tested against the expected
    * file.  If <code>null</code>, method will return <code>false</code>.
    * @param encoding name of the character encoding scheme used by both
    * <code>expectedFile</code> and <code>actualBytes</code>.  Assumed to be 
    * one of Java's supported character encodings and not <code>null</code> or
    * empty.
    * 
    * @return <code>true</code> if the file and the byte array are the same 
    * size and contain identical characters; <code>false</code> otherwise.
    * 
    * @throws IOException if file is not found, or an I/O error occurs.
    */
   private static boolean compare(String expectedFile, byte[] actualBytes,
                                  String encoding)
      throws IOException
   {
      if (actualBytes == null) return false;

      Reader expected = null;
      Reader actual = null;
      try
      {
         // we can't be sure of the line break style (CR, CR-LF, or LF) of the
         // sources, so use a LineNumberReader to normalize them 
         expected = new LineNumberReader( new InputStreamReader(
                 PSCharacterSetTest.class.getResourceAsStream( RESOURCE_PATH + expectedFile ), encoding ) );
         actual = new LineNumberReader( new InputStreamReader(
            new ByteArrayInputStream( actualBytes ), encoding ) );
         return IOTools.compareReaders( expected, actual );
      } finally
      {
         try
         {
            if (expected != null) expected.close();
         } catch (IOException e)
         {
            /* ignore */
         }
         try
         {
            if (actual != null) actual.close();
         } catch (IOException e)
         {
            /* ignore */
         }
      }

   }

   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */
   private static final String RESOURCE_PATH =
      "/com/percussion/xsl/encoding/";
}

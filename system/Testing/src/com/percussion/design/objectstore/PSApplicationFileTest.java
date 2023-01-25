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
package com.percussion.design.objectstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import com.percussion.error.PSIllegalStateException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

/**
 *   Unit tests for the PSApplicationFile class
 */
public class PSApplicationFileTest extends TestCase
{
   public void testConstructors() throws Exception
   {
      File tempFile = File.createTempFile("PSApplicationFileTest", null);
      tempFile.deleteOnExit();
      FileInputStream fIn = new FileInputStream(tempFile);

      try
      {
         // construct with a null input stream and a null file
         new PSApplicationFile(m_nullInputStream, m_nullFile);
         fail();
      }
      catch (IllegalArgumentException success) {}

      try
      {
         // construct with a null input stream and an empty file
         new PSApplicationFile(m_nullInputStream, m_emptyFile);
         fail();
      }
      catch (IllegalArgumentException success) {}

      try
      {
         // construct with a null input stream and a valid file
         new PSApplicationFile(m_nullInputStream, tempFile);
         fail();
      }
      catch (IllegalArgumentException success) {}

      try
      {
         // construct with a valid input stream and a null file
         new PSApplicationFile(fIn, m_nullFile);
         fail();
      }
      catch (IllegalArgumentException success) {}

      try
      {
         // construct with a valid input stream and an empty file
         new PSApplicationFile(fIn, m_emptyFile);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }

   public void testOtherMethods() throws Exception
   {
      File tempFile = File.createTempFile("PSApplicationFileTest", null);
      tempFile.deleteOnExit();
      FileInputStream fIn = new FileInputStream(tempFile);

      try
      {
         // construct with a valid input stream and a valid file
         PSApplicationFile f = new PSApplicationFile(fIn, tempFile);

         // we did not set the date, so it should be 0
         assertTrue(0L == f.getLastModified());

         assertEquals(tempFile, f.getFileName());

         assertEquals(fIn, f.getContent().getContent());

         // it should only let us call getContent() once
         boolean didThrow = false;
         try
         {
            f.getContent();
         }
         catch (PSIllegalStateException e)
         {
            didThrow = true;
         }

         assertTrue(didThrow);

         // test with a supplied valid time
         long t = System.currentTimeMillis();
         f = new PSApplicationFile(fIn, tempFile, t);
         assertTrue(t == f.getLastModified());
      }
      finally
      {
         fIn.close();
      }
   }

   /**
    * Tests the toXml and fromXml methods.
    * <OL>
    * <LI>Create a disk file and write some random bytes to it.
    * <LI>Create a PSApplicationFile whose input stream references the file.
    * <LI>Create a Document object <CODE>doc</CODE> by calling
    * PSApplicationFile.toXml().
    * <LI>Create a new PSApplicationFile object by calling fromXml(doc)
    * <LI>Compare the bytes from the new PSApplicationFile object with the
    * original bytes to make sure they went through the encoding process
    * OK.
    * </OL>
    *
    * @author   chad loder
    *
    * @version 1.0 1999/6/16
    *
    *
    * @throws   Exception
    *
    */
   public void testToFromXml() throws Exception
   {
      final File tempFile = File.createTempFile("PSApplicationFileTest", null);
      tempFile.deleteOnExit();

      final byte[] bytes = new byte[8192];
      SecureRandom rand = new SecureRandom();
      rand.nextBytes(bytes);
      {
         FileOutputStream fOut = new FileOutputStream(tempFile);
         fOut.write(bytes);
         fOut.flush();
         fOut.close();
      }

      FileInputStream fIn = new FileInputStream(tempFile);
      PSApplicationFile appFile = new PSApplicationFile(fIn, tempFile, 0);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(doc, "TheRoot");
      appFile.toXml(doc);
      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);

      PSApplicationFile appFileB = new PSApplicationFile();
      appFileB.fromXml(walker.getNextElement(PSApplicationFile.ms_nodeType), null, null);
      InputStream appFileIn = appFileB.getContent().getContent();
      int bytesRead = 0;
      byte[] readBytes = new byte[bytes.length];
      while (bytesRead < readBytes.length)
      {
         int read = appFileIn.read(readBytes, bytesRead, bytes.length - bytesRead);
         if (read < 0)
            break;
         bytesRead += read;
      }
      assertTrue(appFileIn.available() == 0);
      assertTrue("" + bytesRead + "=?=" + bytes.length, bytesRead == bytes.length);
      assertTrue(Arrays.equals(bytes, readBytes));
   }

   private File m_nullFile = null;
   private File m_emptyFile = new File("");

   private InputStream m_nullInputStream = null;

}

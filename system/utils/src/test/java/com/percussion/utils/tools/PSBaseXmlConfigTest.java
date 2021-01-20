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
package com.percussion.utils.tools;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Base class for test cases that read, write, and compare XML configurations.
 */
public abstract class PSBaseXmlConfigTest
{
   /**
    * Get the prefix to use when creating temp XML files.
    * 
    * @return The prefix, never <code>null</code> or empty.
    */
   protected abstract String getFilePrefix();
   
   /**
    * Get an XML temp file
    * 
    * @return The file, never <code>null</code>
    * 
    * @throws IOException if an error occurs
    */
   protected File getTempXmlFile() throws IOException
   {
      File file = File.createTempFile(getFilePrefix(), ".xml");
      file.deleteOnExit();
      m_tmpFiles.add(file);
      return file;
   }

   /**
    * Delete all temp files we've created
    */
   protected void deleteTmpFiles()
   {
      for (File file : m_tmpFiles)
      {
         if (file.exists())
            file.delete();
      }
   }

   /**
    * Convenience method, calls {@link #compareXmlDocs(File, File, boolean)
    * compareXmlDocs(srcFile, tgtFile, true)}
    */
   protected void compareXmlDocs(File srcFile, File tgtFile) throws Exception
   {
      compareXmlDocs(srcFile, tgtFile, true);
   }

   /**
    * Compares two Xml docs, attempting to account for formatting and white 
    * space.
    * 
    * @param srcFile The src xml file, not <code>null</code> and must 
    * exist.
    * @param tgtFile The tgt xml file, not <code>null</code> and must 
    * exist.
    * @param shouldMatch <code>true</code> if they should match, 
    * <code>false</code> if they should not match.
    * 
    * @throws Exception if the compare fails or there are any errors.
    */
   public static void compareXmlDocs(File srcFile, File tgtFile, boolean shouldMatch) throws Exception
   {
      Validate.notNull(srcFile);
      Validate.notNull(tgtFile);
      Document srcDoc = PSXmlDocumentBuilder.createXmlDocument(
         getCleanXmlStream(new FileInputStream(srcFile)), false);
      Document tgtDoc = PSXmlDocumentBuilder.createXmlDocument(
         getCleanXmlStream(new FileInputStream(tgtFile)), false);
      if (shouldMatch)
      {
         assertEquals(PSXmlDocumentBuilder.toString(srcDoc), 
            PSXmlDocumentBuilder.toString(tgtDoc));         
      }
      else
      {
         assertFalse(PSXmlDocumentBuilder.toString(srcDoc).equals( 
            PSXmlDocumentBuilder.toString(tgtDoc)));
      }
   
   }

   /**
    * Copies the specfiied XML file, by way of convert to a DOM and rewriting
    * to attempt to do some cleaning.
    * 
    * @param src The src xml file, assumed not <code>null</code> and to 
    * exist.
    * @param tgt The tgt xml file, assumed not <code>null</code>.
    * 
    * @throws Exception if the copy fails.
    */
   protected void copyXmlFile(File src, File tgt) throws Exception
   {
      FileInputStream in = null;
      FileOutputStream out = null;
      try
      {
         in = new FileInputStream(src);
         out = new FileOutputStream(tgt);
         Document srcDoc = PSXmlDocumentBuilder.createXmlDocument(
            in, false);
         PSXmlDocumentBuilder.write(srcDoc, out);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }
         
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }

   /**
    * Gets clean xml stream from passed in xml stream ignoring white spaces not
    * in the content.
    *
    * @param in input xml stream, assumed not <code>null</code> and closes this
    *    stream and sends the new stream with cleaned data.
    * @return cleaned xml stream, never <code>null</code>, caller is
    *    responsible for closing the stream
    * @throws IOException when error happens in reading/writing to streams.
    * @throws SAXException when error happens while building document out from
    *    input stream
    */
   private static InputStream getCleanXmlStream(InputStream in) throws IOException, SAXException
   {
      /*
       * First we must read over all white space lines because they will be
       * interpreted by DOM as Text nodes.
       */
      BufferedReader reader = new BufferedReader(
         new InputStreamReader(in, IPSUtilsConstants.RX_JAVA_ENC));
      StringBuffer buffer = new StringBuffer();
      String line = reader.readLine();
      while (line != null)
      {
         if (line.trim().length() > 0)
            buffer.append(line);
         
         line = reader.readLine();
      }
      reader.close();
   
      /*
       * Now clean out all additional white space through DOM and return the
       * new cleaned XML stream.
       */
      Document document = PSXmlDocumentBuilder.createXmlDocument(
         new StringReader(buffer.toString()), false);
      in.close();
      
      removeEmptyTextNodes(document.getDocumentElement());
   
      ByteArrayOutputStream cleanXml = new ByteArrayOutputStream();
      PSXmlDocumentBuilder.write(
         document, new OutputStreamWriter(cleanXml, 
            IPSUtilsConstants.RX_JAVA_ENC));
      byte[] cleanBytes = cleanXml.toByteArray();
      cleanXml.close();
   
      return new ByteArrayInputStream(cleanBytes);
   }

   /**
    * Traverse the DOM searching for text nodes that only contain whitespace
    * and remove them. Does not remove text nodes from a "leaf" element no matter
    * what.
    * 
    * @param el the current element, never <code>null</code>
    */
   private static void removeEmptyTextNodes(Element el)
   {
      NodeList children = el.getChildNodes();
      int count = children.getLength();
      if (count == 0) return;
      
      boolean onlytext = true;
      
      for(int i = 0; i < count; i++)
      {
         Node n = children.item(i);
         if (n.getNodeType() != Node.TEXT_NODE) 
         {
            onlytext = false;
         }
      }
      
      if (onlytext) return;
      
      // If we've gotten here then we need to look for text nodes to remove
      // and elements to recurse to
      Collection<Node> removals = new ArrayList<Node>();
      for(int i = 0; i < count; i++)
      {
         Node n = children.item(i);
         short type = n.getNodeType();
         switch(type)
         {
            case Node.TEXT_NODE:
               Text tn = (Text) n;
               if (StringUtils.isBlank(tn.getTextContent())) 
                  removals.add(n);
               break;
            case Node.ELEMENT_NODE:
               removeEmptyTextNodes((Element) n);
         }
      }
      
      for(Node n : removals)
      {
         el.removeChild(n);
      }
   }

   /**
    * List of temp files created by this test.
    */
   private List<File> m_tmpFiles = new ArrayList<File>();

}


/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.utils.xml;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.TestCase;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static com.percussion.util.PSResourceUtils.getResourcePath;

/**
 * Tests the copier by parsing an external file and comparing the original
 * document with the document created by the sax copier. The external file
 * should be maintained to include all types of elements copied.
 * 
 * Note that the comparison method doesn't allow certain elements to be tested
 * so don't add this. The current known list:
 * <ul>
 * <li>Comments
 * </ul>
 * 
 * @author dougrand
 */
public class PSSaxCopierTest extends TestCase
{
   /**
    * Test file in the unit resources tree
    */
   public final static File ms_testFile = new File(getResourcePath(PSSaxCopierTest.class,"/com/percussion/utils/xml/SaxCopierInput.xml"));

   /**
    * Do the test
    * 
    * @throws Exception
    */
   public void testCopier() throws Exception
   {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
      SAXParserFactory spf = SAXParserFactory.newInstance();
      StringWriter stringWriter = new StringWriter();

      DocumentBuilder db = dbf.newDocumentBuilder();
      SAXParser sp = spf.newSAXParser();

      Document doc = db.parse(ms_testFile);

      XMLOutputFactory ofact = XMLOutputFactory.newInstance();
      XMLStreamWriter writer = ofact.createXMLStreamWriter(stringWriter);
      PSSaxCopier copier = new PSSaxCopier(writer, new HashMap<String,String>(), true);
      sp.parse(ms_testFile, copier);
      writer.close();
      stringWriter.close();

      Document doc2 = db.parse(new ByteArrayInputStream(stringWriter.toString()
            .getBytes()));
      String first = PSXmlDocumentBuilder.toString(doc);
      String second = PSXmlDocumentBuilder.toString(doc2);
      assertEquals(first, second);
   }

   private static final String ms_tcomments = "<!-- a comment --><document>"
         + "<![CDATA[some cdata characters]]></document>";

   /**
    * Do the test
    * 
    * @throws Exception
    */
   public void testHelper() throws Exception
   {
      Map<String,String> renames = new HashMap<String,String>();
      String output = PSSaxHelper.parseWithXMLWriter(ms_tcomments,
            PSSaxCopier.class, renames, true);
      assertEquals(ms_tcomments, output);
   }
}

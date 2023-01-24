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
package com.percussion.extensions;

import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.testing.PSMockRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PSSortXmlExtensionTest extends TestCase
{

   PSSortXmlExtension e = new PSSortXmlExtension();
   
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   public void testProcessResultDocument() throws Exception
   {
      StringReader sr = new StringReader("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
            "<VariantList contentid=\"665\">\r\n" + 
            "   <Variant outputformat=\"2\" type=\"0\" variantId=\"501\">\r\n" + 
            "      <DisplayName>S - Callout</DisplayName>\r\n" + 
            "      <AssemblyUrl>http://localhost:9980/Rhythmyx/assembler/render</AssemblyUrl>\r\n" + 
            "      <StylesheetName/>\r\n" + 
            "      <AssemblyUrlPlain>../assembler/render</AssemblyUrlPlain>\r\n" + 
            "   </Variant>\r\n" + 
            "   <Variant outputformat=\"2\" type=\"0\" variantId=\"502\">\r\n" + 
            "      <DisplayName>S - Image Link</DisplayName>\r\n" + 
            "      <AssemblyUrl>http://localhost:9980/Rhythmyx/assembler/render</AssemblyUrl>\r\n" + 
            "      <StylesheetName/>\r\n" + 
            "      <AssemblyUrlPlain>../assembler/render</AssemblyUrlPlain>\r\n" + 
            "   </Variant>\r\n" + 
            "   <Variant outputformat=\"2\" type=\"0\" variantId=\"503\">\r\n" + 
            "      <DisplayName>S - Title Callout Link</DisplayName>\r\n" + 
            "      <AssemblyUrl>http://localhost:9980/Rhythmyx/assembler/render</AssemblyUrl>\r\n" + 
            "      <StylesheetName/>\r\n" + 
            "      <AssemblyUrlPlain>../assembler/render</AssemblyUrlPlain>\r\n" + 
            "   </Variant>\r\n" + 
            "   <Variant outputformat=\"2\" type=\"0\" variantId=\"504\">\r\n" + 
            "      <DisplayName>S - Title Link</DisplayName>\r\n" + 
            "      <AssemblyUrl>http://localhost:9980/Rhythmyx/assembler/render</AssemblyUrl>\r\n" + 
            "      <StylesheetName/>\r\n" + 
            "      <AssemblyUrlPlain>../assembler/render</AssemblyUrlPlain>\r\n" + 
            "   </Variant>\r\n" + 
            "   <Variant outputformat=\"2\" type=\"0\" variantId=\"537\">\r\n" + 
            "      <DisplayName>S - Title Callout and More Link</DisplayName>\r\n" + 
            "      <AssemblyUrl>http://localhost:9980/Rhythmyx/assembler/render</AssemblyUrl>\r\n" + 
            "      <StylesheetName/>\r\n" + 
            "      <AssemblyUrlPlain>../assembler/render</AssemblyUrlPlain>\r\n" + 
            "   </Variant>\r\n" + 
            "   <Variant outputformat=\"1\" type=\"0\" variantId=\"543\">\r\n" + 
            "      <DisplayName>P - CI Generic</DisplayName>\r\n" + 
            "      <AssemblyUrl>http://localhost:9980/Rhythmyx/assembler/render</AssemblyUrl>\r\n" + 
            "      <StylesheetName/>\r\n" + 
            "      <AssemblyUrlPlain>../assembler/render</AssemblyUrlPlain>\r\n" + 
            "   </Variant>\r\n" + 
            "</VariantList>");
      Document doc = PSXmlDocumentBuilder.createXmlDocument(sr, false);
      PSTextLiteral lt = new PSTextLiteral("DisplayName");
      Document r = e.processResultDocument(new Object[]{lt}, new PSMockRequestContext(), doc);
      Element element = (Element) r.getDocumentElement().getElementsByTagName("Variant").item(5);
      String actual = element.getElementsByTagName("DisplayName").item(0).getTextContent();
      assertEquals("S - Title Link", actual);
      //System.out.println(PSXmlDocumentBuilder.toString(r));
   }

}

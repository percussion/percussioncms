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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

/**
 * Test the {@link PSContentTypeConverter} class
 */
@Category(IntegrationTest.class)
public class PSContentTypeConverterTest extends PSConverterTestBase
{
   /**
    * Test the converter
    * 
    * @throws Exception if the test fails
    */
   public void testConverter() throws Exception
   {
      InputStream in = null;
      try
      {
         in = getClass().getResourceAsStream(
               "/com/percussion/design/objectstore/sys_Default.xml");
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSContentEditor ce = new PSContentEditor(doc.getDocumentElement(), 
            null, null);
         String appName = PSContentType.createAppName(ce.getName());
         String url = PSContentType.createRequestUrl(ce.getName());
         PSContentType typeDef = new PSContentType((int) ce.getContentType(), 
            ce.getName(), ce.getName(), ce.getDescription(), url, false, 1);
         PSItemDefinition srcDef = new PSItemDefinition(appName, typeDef, 
            ce);
         
         PSItemDefinition tgtDef = 
            (PSItemDefinition) roundTripConversion(PSItemDefinition.class, 
            com.percussion.webservices.content.PSContentType.class, 
            srcDef);
         
         boolean isEqual = false;
         try
         {
            assertEquals(PSXmlDocumentBuilder.toString(
               srcDef.toXml(PSXmlDocumentBuilder.createXmlDocument())), 
               PSXmlDocumentBuilder.toString(
                  tgtDef.toXml(PSXmlDocumentBuilder.createXmlDocument())));
            isEqual = true;
         }
         finally
         {
            // if not equal, print out the xml for debugging purposes
            if (!isEqual)
            {
               System.err.println("Item defs not equal");
               System.err.println("src def:");
               System.err.println(PSXmlDocumentBuilder.toString(
                  srcDef.toXml(PSXmlDocumentBuilder.createXmlDocument())));
               System.err.println("tgt def:");
               System.err.println(PSXmlDocumentBuilder.toString(
                  tgtDef.toXml(PSXmlDocumentBuilder.createXmlDocument())));
               
            }
         }
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
      }
   }
}


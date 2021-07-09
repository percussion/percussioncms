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


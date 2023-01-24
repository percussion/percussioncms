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
package com.percussion.rx.delivery.impl;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Test some specific db delivery handler pieces.
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSDatabaseDeliveryHandlerTest
{
   /**
    * Original document
    */
   private static final String ms_pubinput = "<datapublisher>\n"
         + "    <tabledefset>\n"
         + "        <tabledef create=\"y\" name=\"TEST\" allowSchemaChanges=\"n\">\n"
         + "            <rowdef>\n"
         + "                <columndef action=\"c\" name=\"ID\">\n"
         + "                    <jdbctype>INTEGER</jdbctype>\n"
         + "                    <size/>\n"
         + "                    <allowsnull>no</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"DESCRIPTION\">\n"
         + "                    <jdbctype>VARCHAR</jdbctype>\n"
         + "                    <size>1000</size>\n"
         + "                    <allowsnull>yes</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"START_DTM\">\n"
         + "                    <jdbctype>DATE</jdbctype>\n"
         + "                    <size/>\n"
         + "                    <allowsnull>yes</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"END_DTM\">\n"
         + "                    <jdbctype>DATE</jdbctype>\n"
         + "                    <size/>\n"
         + "                    <allowsnull>yes</allowsnull>\n"
         + "                </columndef>\n"
         + "            </rowdef>\n"
         + "            <primarykey action=\"c\" name=\"PK_TEST_ID\">\n"
         + "                <name>ID</name>\n"
         + "            </primarykey>\n"
         + "        </tabledef>\n"
         + "        <tabledef create=\"y\" name=\"TEST_CHILD\" allowSchemaChanges=\"n\">\n"
         + "            <rowdef>\n"
         + "                <columndef action=\"c\" name=\"CID\">\n"
         + "                    <jdbctype>INTEGER</jdbctype>\n"
         + "                    <size/>\n"
         + "                    <allowsnull>no</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"ID\">\n"
         + "                    <jdbctype>INTEGER</jdbctype>\n"
         + "                    <size/>\n"
         + "                    <allowsnull>no</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"DATA_1\">\n"
         + "                    <jdbctype>VARCHAR</jdbctype>\n"
         + "                    <size>100</size>\n"
         + "                    <allowsnull>yes</allowsnull>\n"
         + "                </columndef>\n"
         + "            </rowdef>\n"
         + "            <primarykey action=\"c\" name=\"PK_TEST_CHILD_ID\">\n"
         + "                <name>ID</name>\n"
         + "                <name>CID</name>\n"
         + "            </primarykey>\n"
         + "        </tabledef>\n"
         + "    </tabledefset>\n"
         + "    <tabledataset>\n"
         + "        <table name=\"TEST\">\n"
         + "            <row action=\"r\">\n"
         + "                <column name=\"ID\">126</column>\n"
         + "                <column name=\"DESCRIPTION\">abababababababababadfasdf</column>\n"
         + "                <column name=\"START_DTM\">10/22/1990</column>\n"
         + "                <column name=\"END_DTM\">1/1/2008</column>\n"
         + "                <childtable name=\"TEST_CHILD\">\n"
         + "                    <row action=\"r\">\n"
         + "                        <column name=\"ID\">126</column>\n"
         + "                        <column name=\"CID\">2</column>\n"
         + "                        <column name=\"DATA_1\">4444444444444444444444444444444444444444444444444444444444444444444444444444444444444</column>\n"
         + "                    </row>\n" + "                </childtable>\n"
         + "            </row>\n" + "        </table>\n"
         + "    </tabledataset>\n" + "</datapublisher>";

   /**
    * Test result data
    */
   private static final String ms_result = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>\n"
         + "<datapublisher>\n"
         + "    <tabledefset>\n"
         + "        <tabledef create=\"y\" name=\"TEST\" allowSchemaChanges=\"n\">\n"
         + "            <rowdef>\n"
         + "                <columndef action=\"c\" name=\"ID\">\n"
         + "                    <jdbctype>INTEGER</jdbctype>\n"
         + "                    <size />\n"
         + "                    <allowsnull>no</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"DESCRIPTION\">\n"
         + "                    <jdbctype>VARCHAR</jdbctype>\n"
         + "                    <size>1000</size>\n"
         + "                    <allowsnull>yes</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"START_DTM\">\n"
         + "                    <jdbctype>DATE</jdbctype>\n"
         + "                    <size />\n"
         + "                    <allowsnull>yes</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"END_DTM\">\n"
         + "                    <jdbctype>DATE</jdbctype>\n"
         + "                    <size />\n"
         + "                    <allowsnull>yes</allowsnull>\n"
         + "                </columndef>\n"
         + "            </rowdef>\n"
         + "            <primarykey action=\"c\" name=\"PK_TEST_ID\">\n"
         + "                <name>ID</name>\n"
         + "            </primarykey>\n"
         + "        </tabledef>\n"
         + "        <tabledef create=\"y\" name=\"TEST_CHILD\" allowSchemaChanges=\"n\">\n"
         + "            <rowdef>\n"
         + "                <columndef action=\"c\" name=\"CID\">\n"
         + "                    <jdbctype>INTEGER</jdbctype>\n"
         + "                    <size />\n"
         + "                    <allowsnull>no</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"ID\">\n"
         + "                    <jdbctype>INTEGER</jdbctype>\n"
         + "                    <size />\n"
         + "                    <allowsnull>no</allowsnull>\n"
         + "                </columndef>\n"
         + "                <columndef action=\"c\" name=\"DATA_1\">\n"
         + "                    <jdbctype>VARCHAR</jdbctype>\n"
         + "                    <size>100</size>\n"
         + "                    <allowsnull>yes</allowsnull>\n"
         + "                </columndef>\n"
         + "            </rowdef>\n"
         + "            <primarykey action=\"c\" name=\"PK_TEST_CHILD_ID\">\n"
         + "                <name>ID</name>\n"
         + "                <name>CID</name>\n"
         + "            </primarykey>\n"
         + "        </tabledef>\n"
         + "    </tabledefset>\n"
         + "    <tabledataset>\n"
         + "        <table name=\"TEST\">\n"
         + "            <row action=\"d\">\n"
         + "                <column name=\"ID\">126</column>\n"
         + "                \n"
         + "                \n"
         + "                \n"
         + "                <childtable name=\"TEST_CHILD\">\n"
         + "                    <row action=\"d\">\n"
         + "                        <column name=\"ID\">126</column>\n"
         + "                        <column name=\"CID\">2</column>\n"
         + "                        \n"
         + "                    </row>\n"
         + "                </childtable>\n"
         + "            </row>\n"
         + "        </table>\n" + "    </tabledataset>\n" + "</datapublisher>";

   /**
    * Test the extraction of unpublishing information - really tests the sax
    * copier.
    * 
    * @throws SAXException
    * @throws ParserConfigurationException
    * @throws XMLStreamException
    * @throws IOException
    */
   @Test
   public void testDBUnpublishInfo() throws ParserConfigurationException,
         SAXException, XMLStreamException, IOException
   {
      InputSource src = new InputSource(new StringReader(ms_pubinput));
      XMLOutputFactory ofact = XMLOutputFactory.newInstance();
      StringWriter writer = new StringWriter();
      XMLStreamWriter formatter = ofact.createXMLStreamWriter(writer);

      SAXParserFactory f = PSSecureXMLUtils.getSecuredSaxParserFactory(
              new PSXmlSecurityOptions(
                      true,
                      true,
                      true,
                      false,
                      true,
                      false
              ));

      SAXParser parser = f.newSAXParser();
      DefaultHandler dh = new PSDatabaseDeliveryHandler.UnpublishingContentHandler(
            formatter, null);
      formatter.writeStartDocument();
      formatter.writeCharacters("\n");
      parser.parse(src, dh);
      formatter.writeEndDocument();
      formatter.close();
      assertEquals(ms_result, writer.toString());
   }
   

}

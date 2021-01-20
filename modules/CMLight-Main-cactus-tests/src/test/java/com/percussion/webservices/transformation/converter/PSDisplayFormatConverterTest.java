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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.PSDFColumns;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.design.objectstore.PSRelationshipConfigTest;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for the {@link PSDisplayFormatConverter} class.
 */
@Category(IntegrationTest.class)
public class PSDisplayFormatConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object and vice versa.
    */
   public void testConversion() throws Exception
   {
      // test simple displayformat
      PSDisplayFormat source = getSimpleDisplayFormat("testDisplayFormat", 100);
      roundTripConversion(source);

      // test displayformat with properties
      source = getDisplayFormat("testDisplayFormat", 100);      
      roundTripConversion(source);
      
      // test with image render type column
      PSDFColumns columns = source.getColumnContainer();
      PSDisplayColumn column = new PSDisplayColumn(
            PSDisplayColumn.createKey("sys_thumbnail", 2L, false));
      column.setDisplayName("Thumbnail");
      column.setRenderType("image");
      columns.add(1, column);
      roundTripConversion(source);
      
      // test displayformat with communities
      source.addCommunity("1002");
      source.addCommunity("1003");
      source.addCommunity("1004");
      roundTripConversion(source);
      
      List<PSDisplayFormat> dfs = getDisplayFormats();
      for (PSDisplayFormat df : dfs)
      {
         roundTripConversion(df);
      }
   }

   /**
    * Test a list of server object convert to client array, and vice versa.
    *
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      List<PSDisplayFormat> srcList = new ArrayList<PSDisplayFormat>();
      srcList.add(getDisplayFormat("testAction", 100));
      srcList.add(getDisplayFormat("testAction_2", 200));

      List<PSDisplayFormat> srcList2 = roundTripListConversion(
         com.percussion.webservices.ui.data.PSDisplayFormat[].class, srcList);

      assertTrue(srcList.equals(srcList2));

      
      srcList = getDisplayFormats();
      srcList2 = roundTripListConversion(
         com.percussion.webservices.ui.data.PSDisplayFormat[].class, srcList);

      assertTrue(srcList.equals(srcList2));
      
   }


   @SuppressWarnings("unused") 
   private void roundTripConversion(PSDisplayFormat source)
         throws PSTransformationException
   {
      PSDisplayFormat target = (PSDisplayFormat) roundTripConversion(
            PSDisplayFormat.class,
            com.percussion.webservices.ui.data.PSDisplayFormat.class, source);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      //System.out.println(PSXmlDocumentBuilder.toString(source.toXml(doc)));
      //System.out.println(PSXmlDocumentBuilder.toString(target.toXml(doc)));

      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }

   /**
    * Creates an action with the given name.
    *
    * @param name the name of the new action, assumed not <code>null</code>.
    *
    * @return the created action, never <code>null</code>.
    */
   private PSDisplayFormat getDisplayFormat(String name, int id) throws Exception
   {
      PSDisplayFormat df = getSimpleDisplayFormat(name, id);
      
      df.setProperty(PSDisplayFormat.PROP_SORT_COLUMN, "Column 1");
      df.setProperty(PSDisplayFormat.PROP_SORT_DIRECTION,
            PSDisplayFormat.SORT_ASCENDING);
      df.addCommunity(PSDisplayFormat.PROP_COMMUNITY_ALL);

      return df;

   }

   /**
    * Creates an action with the given name.
    *
    * @param name the name of the new action, assumed not <code>null</code>.
    *
    * @return the created action, never <code>null</code>.
    */
   private PSDisplayFormat getSimpleDisplayFormat(String name, int id) 
      throws Exception
   {
      PSDisplayFormat df = new PSDisplayFormat();
      
      df.setLocator(PSDisplayFormat.createKey(new String[]{String.valueOf(id)}));
      df.setInternalName(name);
      df.setDisplayName(name + "_label");
      df.setDescription(name + "_description");

      return df;

   }

   /**
    * @return a list of display formats from test data file, never 
    *    <code>null</code> or empty.
    *
    * @throws Exception if an error occurs.
    */
   private List<PSDisplayFormat> getDisplayFormats() throws Exception
   {
      Element dfElems = PSRelationshipConfigTest.loadXmlResource(
            "../../rhythmyxdesign/PSDisplayFormats.xml", this.getClass());

      Iterator dfs = (new PSDbComponentCollection(dfElems)).iterator();
      List<PSDisplayFormat> dfList = new ArrayList<PSDisplayFormat>();
      while (dfs.hasNext())
      {
         dfList.add((PSDisplayFormat) dfs.next());
      }

      return dfList;
   }


}


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

import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.design.objectstore.PSRelationshipConfigTest;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.converter.PSSearchViewConverter;
import com.percussion.webservices.ui.data.PSSearchDef;
import com.percussion.webservices.ui.data.PSViewDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit tests for the {@link PSSearchViewConverter} class.
 */
@Category(IntegrationTest.class)
public class PSSearchViewConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object and vice versa.
    */
   public void testSearchConversion() throws Exception
   {
      // test with simple action
      PSSearch simpleSearch = getSimpleSearch("simpleSearch");
      roundTripConvertion(simpleSearch, PSSearchDef.class);

      List<PSSearch> srcList = getPSSearches(false);
      for (PSSearch s : srcList)
      {
         roundTripConvertion(s, PSSearchDef.class);
      }
   }

   public void testViewConversion() throws Exception
   {
      // test with simple action
      PSSearch simpleSearch = getSimpleView("simpleView");
      roundTripConvertion(simpleSearch, PSViewDef.class);

      List<PSSearch> srcList = getPSSearches(true);
      for (PSSearch s : srcList)
      {
         roundTripConvertion(s, PSViewDef.class);
      }
   }


   @SuppressWarnings("unused")
   private void roundTripConvertion(PSSearch source, Class clientClass)
      throws Exception
   {
      PSSearch target = (PSSearch) roundTripConversion(PSSearch.class,
            clientClass, source);

       Document doc = PSXmlDocumentBuilder.createXmlDocument();
      //System.out.println(PSXmlDocumentBuilder.toString(source.toXml(doc)));
      //System.out.println(PSXmlDocumentBuilder.toString(target.toXml(doc)));

      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }

   /**
    * Test a list of server object convert to (client) view array,
    * and vice versa.
    *
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testViewListToArray() throws Exception
   {
      List<PSSearch> srcList = new ArrayList<PSSearch>();
      srcList.add(getSimpleView("testView"));
      srcList.add(getSimpleView("testView_2"));

      // test simple search objects
      List<PSSearch> srcList2 = roundTripListConversion(
            PSViewDef[].class, srcList);

      assertTrue(srcList.equals(srcList2));

      // test complexed search objects
      srcList = getPSSearches(true);
      srcList2 = roundTripListConversion(PSViewDef[].class, srcList);
      assertTrue(srcList.equals(srcList2));
   }


   /**
    * Test a list of server object convert to (client) search array,
    * and vice versa.
    *
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testSearchListToArray() throws Exception
   {
      List<PSSearch> srcList = new ArrayList<PSSearch>();
      srcList.add(getSimpleSearch("testSearch"));
      srcList.add(getSimpleSearch("testSearch_2"));

      // test simple search objects
      List<PSSearch> srcList2 = roundTripListConversion(
            PSSearchDef[].class, srcList);

      assertTrue(srcList.equals(srcList2));

      // test complexed search objects
      srcList = getPSSearches(false);
      srcList2 = roundTripListConversion(PSSearchDef[].class, srcList);
      assertTrue(srcList.equals(srcList2));
   }
/*
   @SuppressWarnings("unchecked")
   protected List roundTripListConversion(Class type, List value)
         throws Exception
   {
      Object arrayValue = PSSearchViewConverter.convertListToArray(type,
            (List<PSSearch>) value);

      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      Converter converter = factory.getConverter(List.class);

      Object listValue = converter.convert(PSSearch.class, arrayValue);

      return (List) listValue;
   }
*/
   /**
    * @return a list of search objects, loaded from predefined test data,
    *    never <code>null</code> or empty.
    *
    * @throws Exception if an error occurs.
    */
   private List<PSSearch> getPSSearches(boolean isView) throws Exception
   {
      Element searchElem = PSRelationshipConfigTest.loadXmlResource(
            "../../rhythmyxdesign/PSSearches_Views.xml", this.getClass());

      NodeList nodes = searchElem.getElementsByTagName("PSXSearch");
      int length = nodes.getLength();
      List<PSSearch> searches = new ArrayList<PSSearch>();
      for (int i=0; i < length; i++)
      {
         PSSearch s = new PSSearch((Element)nodes.item(i));
         if (isView)
         {
            if (s.isView())
               searches.add(s);
         }
         else
         {
            if (! s.isView())
               searches.add(s);
         }
      }

      return searches;
   }

   /**
    * Creates an search with the given name.
    *
    * @param name the name of the new action, assumed not <code>null</code>.
    *
    * @return the created action, never <code>null</code>.
    */
   private PSSearch getSimpleSearch(String name) throws Exception
   {
      PSSearch target = new PSSearch(name);
      PSKey key = PSSearch.createKey(new String[]{"123"});
      key.setPersisted(false);
      target.setLocator(key);
      target.setType(PSSearch.TYPE_STANDARDSEARCH);
      return target;
   }

   private PSSearch getSimpleView(String name) throws Exception
   {
      PSSearch target = new PSSearch(name);
      PSKey key = PSSearch.createKey(new String[]{"123"});
      key.setPersisted(false);
      target.setLocator(key);
      target.setType(PSSearch.TYPE_VIEW);
      return target;
   }

}


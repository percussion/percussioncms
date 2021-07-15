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

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRelationshipConfigTest;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

/**
 * Unit tests for the {@link PSRelationshipConfigConverter} class.
 */
@Category(IntegrationTest.class)
public class PSAaRelationshipConverterTest extends PSConverterTestBase
{
   /* (non-Javadoc)
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      PSRelationshipConfigSet cset = PSRelationshipConfigTest.getConfigs();
      PSRelationshipCommandHandler.reloadConfigs(cset);
   }
   
   /**
    * Tests the conversion for AA relationship from a server to a client object 
    * and vice versa.
    */
   public void testAaRelationshipConversion() throws Exception
   {
      // test with simple relationship
      PSAaRelationship aaRel = createAaRelationship(1, 2, 3, 4, 5);
      roundTripConvertion(aaRel);

   }

   /**
    * Tests the conversion for relationship from a server to a client object 
    * and vice versa.
    */
   public void testRelationshipConversion() throws Exception
   {
      // test with simple relationship
      PSAaRelationship aaRel = createAaRelationship(1, 2, 3, 4, 5);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSRelationship rel = new PSRelationship(aaRel.toXml(doc));
      roundTripConvertion(rel);
   }

   
   /**
    * Creates an Active Assembly Relationship from the specified parameters.
    * 
    * @param rid the relationship id
    * @param ownerId the owner id
    * @param dependentId the dependent id
    * @param slotId the slot id
    * @param templateId the template id
    * 
    * @return the created AA relationship, never <code>null</code>.
    */
   private PSAaRelationship createAaRelationship(int rid, int ownerId, 
         int dependentId, int slotId, int templateId)
   {
      PSRelationshipConfig config = 
         PSRelationshipCommandHandler.getRelationshipConfig(
               PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      PSRelationship rel = new PSRelationship(rid, new PSLocator(ownerId, 1), 
            new PSLocator(dependentId, -1), config);
      rel.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slotId));
      rel.setProperty(IPSHtmlParameters.SYS_VARIANTID, String
            .valueOf(templateId));
      
      PSAaRelationship target = new PSAaRelationship(rel); 
      target.setSortRank(1);
      
      return target;
   }
   
   /**
    * Round trip conversion test for the specified {@link PSRelationship}
    * 
    * @param source the to be tested relationship object, assumed not 
    *    <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unused")
   private void roundTripConvertion(PSRelationship source)
      throws Exception
   {
      PSRelationship target;
      
      if (source instanceof PSAaRelationship)
      {
         target = (PSAaRelationship) roundTripConversion(
               PSAaRelationship.class,
               com.percussion.webservices.content.PSAaRelationship.class,
               source);
      }
      else
      {
         target = (PSRelationship) roundTripConversion(
               PSRelationship.class,
               com.percussion.webservices.system.PSRelationship.class,
               source);
      }

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      String sourceString = PSXmlDocumentBuilder.toString(source.toXml(doc));
      String targetString = PSXmlDocumentBuilder.toString(target.toXml(doc));
      
      //System.out.println(sourceString);
      //System.out.println(targetString);
      //assertTrue(sourceString.equals(targetString));

      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }

   /**
    * Test a list of server object convert to (client) search array,
    * and vice versa.
    *
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testConfigListToArray() throws Exception
   {
      //\/\/\/\/\/\/\/\/\/\/\
      // tset AA relationship
      //\/\/\/\/\/\/\/\/\/\/\
      List<PSAaRelationship> srcList = new ArrayList<PSAaRelationship>();
      srcList.add(createAaRelationship(2, 3, 4, 5, 6));
      srcList.add(createAaRelationship(20, 30, 40, 50, 60));

      List<PSAaRelationship> tgtList = roundTripListConversion(
            com.percussion.webservices.content.PSAaRelationship[].class, 
            srcList);

      @SuppressWarnings("unused")
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      //PSRelationshipConfigSet tgtSet = new PSRelationshipConfigSet();
      //for (PSRelationshipConfig c : tgtList) tgtSet.add(c);
      //String srcString = PSXmlDocumentBuilder.toString(cset.toXml(doc));
      //String tgtString = PSXmlDocumentBuilder.toString(tgtSet.toXml(doc));
      //assertTrue(srcString.equals(tgtString));

      assertTrue(srcList.equals(tgtList));

      //\/\/\/\/\/\/\/\/\/\/\/\/
      // tset other relationship
      //\/\/\/\/\/\/\/\/\/\/\/\/
      List<PSRelationship> srcList2 = new ArrayList<PSRelationship>();
      PSRelationship rel = createAaRelationship(12, 13, 14, 15, 16);
      srcList2.add(new PSRelationship(rel.toXml(doc)));
      rel = createAaRelationship(120, 130, 140, 150, 160);
      srcList2.add(new PSRelationship(rel.toXml(doc)));
      
      List<PSRelationship> tgtList2 = roundTripListConversion(
            com.percussion.webservices.system.PSRelationship[].class, 
            srcList2);
      assertTrue(srcList2.equals(tgtList2));

   }
}


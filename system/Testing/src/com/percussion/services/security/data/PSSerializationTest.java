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
package com.percussion.services.security.data;

import com.percussion.i18n.PSLocale;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import junit.framework.TestCase;

/**
 * Test xml helper serialization of selected objects
 * 
 * @author dougrand
 * 
 */
public class PSSerializationTest extends TestCase
{
   static
   {
      PSXmlSerializationHelper.addType(PSCommunity.class);
      PSXmlSerializationHelper.addType(PSLocale.class);
      PSXmlSerializationHelper.addType(PSGuid.class);
   }
   
   public void testGuidSer() throws Exception
   {
      PSGuid g = new PSGuid(PSTypeEnum.ACL, 101101);
      
      String ser = PSXmlSerializationHelper.writeToXml(g);
      
      PSGuid res = (PSGuid) PSXmlSerializationHelper.readFromXML(ser);
      
      assertEquals(g, res);
   }
   
   public void testCommunitySerialization() throws Exception
   {
      PSCommunity community = new PSCommunity();

      community.setDescription("Test community");
      community.setGUID(new PSGuid(PSTypeEnum.COMMUNITY_DEF, 100101));
      community.setName("Test_1");
      community.addRoleAssociation(new PSGuid(PSTypeEnum.ROLE, 10));
      community.addRoleAssociation(new PSGuid(PSTypeEnum.ROLE, 11));

      String ser = PSXmlSerializationHelper.writeToXml(community);

      PSCommunity restore = (PSCommunity) PSXmlSerializationHelper
            .readFromXML(ser);
      assertEquals(community,restore);
   }
   
   public void FIXME_testLocaleSerialization() throws Exception
   {
      PSLocale locale = new PSLocale();
      
      locale.setLocaleId(111);
      locale.setDescription("A locale");
      locale.setDisplayName("en_GB");
      locale.setLanguageString("en_UK_1");
      locale.setStatus(5);
      
      String ser = PSXmlSerializationHelper.writeToXml(locale);

      PSLocale restore = (PSLocale) PSXmlSerializationHelper
            .readFromXML(ser);
      
      assertEquals(locale,restore);
      
   }
}

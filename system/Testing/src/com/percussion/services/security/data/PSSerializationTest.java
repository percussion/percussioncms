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

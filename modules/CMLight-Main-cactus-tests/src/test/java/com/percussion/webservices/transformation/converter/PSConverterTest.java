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

import com.percussion.design.objectstore.PSRole;
import com.percussion.i18n.PSLocale;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.content.data.PSContentTypeSummaryChild;
import com.percussion.services.content.data.PSFieldDescription;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSLogin;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.services.system.data.PSDependency;
import com.percussion.services.system.data.PSDependent;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateBindingsBinding;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for the {@link PSConverter} class.
 */
@Category(IntegrationTest.class)
public class PSConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testPSLoginConversion() throws Exception
   {
      // create the source object
      PSLogin source = new PSLogin();
      source.setSessionId("session");
      source.setSessionTimeout(100000);
      source.setDefaultCommunity("defaultCommunity");
      source.setDefaultLocaleCode("defaultLocaleCode");
      
      PSLogin target = (PSLogin) roundTripConversion(PSLogin.class, 
         com.percussion.webservices.security.data.PSLogin.class, source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source object
      PSLogin source2 = new PSLogin();
      source2.setSessionId("session");
      source2.setSessionTimeout(100000);
      source2.setDefaultCommunity("defaultCommunity");
      source2.setDefaultLocaleCode("defaultLocaleCode");
      List<PSCommunity> communities = new ArrayList<PSCommunity>();
      communities.add(new PSCommunity("name", "description"));
      source2.setCommunities(communities);
      List<PSRole> roles = new ArrayList<PSRole>();
      roles.add(new PSRole("name"));
      source2.setRoles(roles);
      List<PSLocale> locales = new ArrayList<PSLocale>();
      locales.add(new PSLocale("de-ch", "Swiss German", 
         "German language used in Switzerland", PSLocale.STATUS_ACTIVE));
      source2.setLocales(locales);
      
      PSLogin target2 = (PSLogin) roundTripConversion(PSLogin.class, 
         com.percussion.webservices.security.data.PSLogin.class, source2);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source2.equals(target2));
   }
   
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testPSTemplateBindingConversion() throws Exception
   {
      // create the source object
      PSTemplateBinding source = new PSTemplateBinding(1, "variable", 
         "expression");
      
      PSTemplateBinding target = (PSTemplateBinding) roundTripConversion(
         PSTemplateBinding.class, 
         PSAssemblyTemplateBindingsBinding.class, source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }
   
   /**
    * Test conversion of a PSObjectSummary
    * 
    * @throws Exception if the test fails
    */
   public void testPSObjectSummaryConversion() throws Exception
   {
      // create soure object
      PSObjectSummary src = new PSObjectSummary(new PSGuid(PSTypeEnum.NODEDEF, 
         1001), "article", "Article", null);
      
      // convert
      PSObjectSummary tgt = (PSObjectSummary) roundTripConversion(
         PSObjectSummary.class, 
         com.percussion.webservices.common.PSObjectSummary.class, src);
      assertEquals(src, tgt);
      
      // add locking information
      src.setLockedInfo("session", "locker", 1000);

      // convert
      tgt = (PSObjectSummary) roundTripConversion(PSObjectSummary.class, 
         com.percussion.webservices.common.PSObjectSummary.class, src);
      assertEquals(src, tgt);
      
      // add permissions
      Set<PSPermissions> permset = new HashSet<PSPermissions>();
      PSUserAccessLevel accessLevel = new PSUserAccessLevel(permset);
      src.setPermissions(accessLevel);
      
      tgt = (PSObjectSummary) roundTripConversion(PSObjectSummary.class, 
         com.percussion.webservices.common.PSObjectSummary.class, src);
      assertEquals(src, tgt);

      for (PSPermissions permission : PSPermissions.values())
      {
         accessLevel.getPermissions().add(permission);
      }
      
      tgt = (PSObjectSummary) roundTripConversion(PSObjectSummary.class, 
         com.percussion.webservices.common.PSObjectSummary.class, src);
      assertEquals(src, tgt);
   }
   
   /**
    * Test conversion of a PSContentTypeSummary
    * 
    * @throws Exception if the test fails
    */
   @SuppressWarnings(value={"unchecked"})
   public void testPSContentTypeSummaryConversion() throws Exception
   {
      // create source
      PSContentTypeSummary src = new PSContentTypeSummary();
      src.setGuid(new PSGuid(PSTypeEnum.NODEDEF, 301));
      src.setName("Article");
      src.setDescription("a content type");
      
      src.addField(new PSFieldDescription("fld1", 
         PSFieldDescription.PSFieldTypeEnum.NUMBER.name()));
      src.addField(new PSFieldDescription("fld2", 
         PSFieldDescription.PSFieldTypeEnum.TEXT.name()));
      
      PSContentTypeSummaryChild child;
      child = new PSContentTypeSummaryChild("child1");
      child.addField(new PSFieldDescription("fld3", 
         PSFieldDescription.PSFieldTypeEnum.NUMBER.name()));
      child.addField(new PSFieldDescription("fld4", 
         PSFieldDescription.PSFieldTypeEnum.DATE.name()));
      child = new PSContentTypeSummaryChild("child2");
      child.addField(new PSFieldDescription("fld5", 
         PSFieldDescription.PSFieldTypeEnum.NUMBER.name()));
      src.addChild(child);      
      
      // convert
      PSContentTypeSummary tgt = (PSContentTypeSummary) roundTripConversion(
         PSContentTypeSummary.class, 
         com.percussion.webservices.content.PSContentTypeSummary.class, 
         src);
      
      assertEquals(src, tgt);
      
      // test list
      PSContentTypeSummary src2 = new PSContentTypeSummary();
      src2.setGuid(new PSGuid(PSTypeEnum.NODEDEF, 302));
      src2.setName("name2");
      src2.setDescription("another content type");
      
      src2.addField(new PSFieldDescription("fld21", 
         PSFieldDescription.PSFieldTypeEnum.NUMBER.name()));
      src2.addField(new PSFieldDescription("fld22", 
         PSFieldDescription.PSFieldTypeEnum.TEXT.name()));
      List<PSContentTypeSummary> sums = new ArrayList<PSContentTypeSummary>(2);
      sums.add(src);
      sums.add(src2);
      
      @SuppressWarnings("unused")
      List<PSContentTypeSummary> tgtsums = roundTripListConversion(
            com.percussion.webservices.content.PSContentTypeSummary[].class,
            sums); 
   }
   
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testPSDependencyConversion() throws Exception
   {
      PSDependent child;
      child = new PSDependent();
      child.setId(111);
      child.setType("type1");
      PSDependent convChild = (PSDependent) roundTripConversion(
         PSDependent.class, 
         com.percussion.webservices.system.PSDependent.class, child);
      assertEquals(child, convChild);
      
      PSDependency dep = new PSDependency();
      dep.setId(1234);
      dep.addDependent(child);
      
      PSDependency convDep = (PSDependency) roundTripConversion(
         PSDependency.class, 
         com.percussion.webservices.system.PSDependency.class, dep);
      assertEquals(dep, convDep);
      
      child = new PSDependent();
      child.setId(112);
      child.setType("type2");
      dep.addDependent(child);
      PSDependency convDep2 = (PSDependency) roundTripConversion(
         PSDependency.class, 
         com.percussion.webservices.system.PSDependency.class, dep);
      assertEquals(dep, convDep2);
      assertFalse(convDep.equals(convDep2));
      assertFalse(dep.equals(convDep));
      
      List<PSDependency> srcList = new ArrayList<PSDependency>(2);
      srcList.add(dep);
      srcList.add(convDep);
      srcList.add(convDep2);
      List tgtList = roundTripListConversion(
         com.percussion.webservices.system.PSDependency[].class, srcList);
      assertEquals(srcList, tgtList);
   }  
}

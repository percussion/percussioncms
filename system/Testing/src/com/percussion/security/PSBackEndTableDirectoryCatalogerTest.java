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
package com.percussion.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;

import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.services.PSBaseServiceLocator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link PSBackEndTableDirectoryCataloger}.
 */
@Category(IntegrationTest.class)
public class PSBackEndTableDirectoryCatalogerTest
{
   private static final String DEFAULT_PASSWORD = "89e495e7941cf9e40e6980d14a16bf023ccd4c91";
   private static final String DEFAULT_PARTIAL_PASSWORD = "89e495";

   /**
    * Test the cataloger
    * 
    * @throws Exception if the test fails
    */
   @SuppressWarnings("unchecked")
   @Test
   public void testCataloger() throws Exception
   {
      PSBaseServiceLocator.getBean("sys_connectionHelper");
      
      Properties props = new Properties();
      props.setProperty("tableName", "USERLOGIN");
      props.setProperty("datasourceName", "");
      props.setProperty("uidColumn", "USERID");
      props.setProperty("passwordColumn", "PASSWORD");
      
      // reuse password col as an attribute
      String testAttr = "testAttr";
      props.setProperty(testAttr, "PASSWORD");
      
      PSBackEndTableDirectoryCataloger cat = 
         new PSBackEndTableDirectoryCataloger(props);
      
      // test user w/ single attribute
      assertEquals("test user w/ single attribute", DEFAULT_PASSWORD, cat.getAttribute("admin1", testAttr));
      
      // test subject and attr list
      PSSubject sub = cat.getAttributes("admin1");
      Collection<String> attrs = new ArrayList<String>(1);
      attrs.add(testAttr);
      PSSubject sub2 = cat.getAttributes("admin1", attrs);
      assertEquals("test subject and attr list", sub, sub2);
      assertEquals("test subject and attr list", sub.getAttributes().getAttribute(
         testAttr).getValues().get(0), DEFAULT_PASSWORD);
      
      // test subject and null attr list
      sub = cat.getAttributes("admin1");
      sub2 = cat.getAttributes("admin1", null);
      assertEquals("test subject and null attr list", sub, sub2);
      assertEquals("test subject and null attr list", sub.getAttributes().getAttribute(
         testAttr).getValues().get(0), DEFAULT_PASSWORD);      
      
      // test subject with extra non-present attribute
      Collection<String> attrs2 = new ArrayList<String>(attrs);
      attrs2.add("foo");
      sub2 = cat.getAttributes("admin1", attrs2);
      assertTrue("test subject with extra non-present attribute", !sub.equals(sub2));

      PSConditional cond;
      Collection subs;
      // test find user with user name
      cond = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral("admin1"));
      subs = cat.findUsers(new PSConditional[] {cond}, attrs);
      assertTrue("should find user with user name", subs.size() > 0);
      
      // test find user with equals
      cond = new PSConditional(new PSTextLiteral(testAttr), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral(DEFAULT_PASSWORD));
      subs = cat.findUsers(new PSConditional[] {cond}, attrs);
      assertTrue("should find user with equals", subs.size() > 0);

      // test find user with like
      cond = new PSConditional(new PSTextLiteral(testAttr), 
         PSConditional.OPTYPE_LIKE, new PSTextLiteral(DEFAULT_PARTIAL_PASSWORD + "%"));
      subs = cat.findUsers(new PSConditional[] {cond}, attrs);
      assertTrue("should find user with like", subs.size() > 0);

      // test find user with equals and bad criteria
      cond = new PSConditional(new PSTextLiteral(testAttr), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral(DEFAULT_PARTIAL_PASSWORD));
      subs = cat.findUsers(new PSConditional[] {cond}, attrs);
      assertTrue("should not find user with equals and bad criteria", subs.isEmpty());

      // test find user with like and no attributes
      cond = new PSConditional(new PSTextLiteral(testAttr), 
         PSConditional.OPTYPE_LIKE, new PSTextLiteral(DEFAULT_PARTIAL_PASSWORD + "%"));
      subs = cat.findUsers(new PSConditional[] {cond}, null);
      assertTrue("should find user with like and no attributes", subs.size() > 0);
      
      // test find user with bad criteria and no attributes
      cond = new PSConditional(new PSTextLiteral(testAttr), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral(DEFAULT_PARTIAL_PASSWORD));
      subs = cat.findUsers(new PSConditional[] {cond}, null);
      assertTrue("should not find user with bad criteria and no attributes", subs.isEmpty());
      
   }
}


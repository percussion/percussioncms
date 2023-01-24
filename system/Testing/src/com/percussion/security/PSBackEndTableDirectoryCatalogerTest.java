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


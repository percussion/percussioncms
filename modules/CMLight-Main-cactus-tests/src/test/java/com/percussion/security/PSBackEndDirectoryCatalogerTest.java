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

import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test case for the {@link PSBackEndDirectoryCataloger}.
 */
@Category(IntegrationTest.class)
public class PSBackEndDirectoryCatalogerTest extends ServletTestCase
{
   /**
    * Test the cataloger
    * 
    * @throws Exception if the test fails
    */
   //Ignore for now will fix but need to debug on Python
   public void testCataloger() throws Exception
   {
      PSBackEndDirectoryCataloger cat = new PSBackEndDirectoryCataloger();

      Collection<String> attrs = new ArrayList<String>(1);
      attrs.add("sys_defaultcommunity");

      PSConditional cond;
      Collection subs;
      // test find user with user name
      cond = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral("admin1"));
      subs = cat.findUsers(new PSConditional[] {cond}, attrs);
      assertTrue(subs.size() == 1);
      
      // test find user with like
      cond = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_LIKE, new PSTextLiteral("admin%"));
      subs = cat.findUsers(new PSConditional[] {cond}, attrs);
      assertTrue(subs.size() == 2);

      // test find user with equals and bad criteria
      cond = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral("admin3"));
      subs = cat.findUsers(new PSConditional[] {cond}, attrs);
      assertTrue(subs.isEmpty());

      // test find user with like and no attributes
      cond = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_LIKE, new PSTextLiteral("admin%"));
      subs = cat.findUsers(new PSConditional[] {cond}, null);
      assertTrue(subs.size() > 1);
      
      // test find user with bad criteria and no attributes
      cond = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral("admin3"));
      subs = cat.findUsers(new PSConditional[] {cond}, null);
      assertTrue(subs.isEmpty());
    
      
      // test multiple
      PSConditional[] conds;
      conds = new PSConditional[4];
      conds[0] = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral("admin1"));
      conds[1] = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral("admin2"));
      conds[2] = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_LIKE, new PSTextLiteral("editor%"));
      
      conds[3] = new PSConditional(new PSTextLiteral(
         cat.getObjectAttributeName()), 
         PSConditional.OPTYPE_EQUALS, new PSTextLiteral("admin3"));
      
      subs = cat.findUsers(conds, attrs);
      assertTrue(subs.size() == 4);
   }
}


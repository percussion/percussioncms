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
package com.percussion.deploy.server.dependencies;

import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.server.PSDependencyManager;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test case for the {@link PSRoleDefDependencyHandler}.
 */
@Category(IntegrationTest.class)
public class PSRoleDefDependencyHandlerTest extends ServletTestCase
{
   /**
    * Test the handler
    * 
    * @throws Exception if the test fails
    */
   public void testHandler() throws Exception
   {
      IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      List<String> roles = roleMgr.getRhythmyxRoles();
      TestCase.assertTrue(roles.size() > 0);
      
      // test does dependency exist
      String role = roles.get(0);
            
      PSDependencyHandler hdlr = 
         PSDependencyManager.getInstance().getDependencyHandler(
               PSRoleDefDependencyHandler.DEPENDENCY_TYPE);
            
      PSSecurityToken tok = new PSSecurityToken("test");
      TestCase.assertTrue(hdlr.doesDependencyExist(tok, role));
      TestCase.assertFalse(hdlr.doesDependencyExist(tok,
            "This dependency does not exist"));
      
      // test get dependency, dependencies
      Set<PSDependency> roleDeps = new HashSet<PSDependency>();
      for (String r : roles)
      {
         PSDependency dep = hdlr.getDependency(tok, r);
         TestCase.assertTrue(dep != null);
         roleDeps.add(dep);
      }     
      
      Iterator depIter = hdlr.getDependencies(tok);
      int i = 0;
      while (depIter.hasNext())
      {
         TestCase.assertTrue(roleDeps.contains((PSDependency) depIter.next()));
         i++;
      }
      
      TestCase.assertTrue(roleDeps.size() == i);
   }
}


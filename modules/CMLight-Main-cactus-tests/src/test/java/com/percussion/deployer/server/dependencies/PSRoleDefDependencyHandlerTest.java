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
package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.PSDependencyManager;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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


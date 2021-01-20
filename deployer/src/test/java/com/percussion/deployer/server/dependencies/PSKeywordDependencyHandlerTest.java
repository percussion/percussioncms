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
package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.PSDependencyManager;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Test case for the {@link PSKeywordDependencyHandler}.
 */
@Category(IntegrationTest.class)
public class PSKeywordDependencyHandlerTest extends ServletTestCase
{
   /**
    * Test the handler
    * 
    * @throws Exception if the test fails
    */
   @SuppressWarnings("unchecked")
   public void testHandler() throws Exception
   {
      IPSContentService contentSvc = 
         PSContentServiceLocator.getContentService();
      List<PSKeyword> keywords = contentSvc.findKeywordsByLabel(null, null);
      assertTrue(keywords.size() > 0);
      
      // test does dependency exist
      PSKeyword keyword = keywords.get(0);
            
      PSDependencyHandler hdlr = 
         PSDependencyManager.getInstance().getDependencyHandler(
               PSKeywordDependencyHandler.DEPENDENCY_TYPE);
            
      PSSecurityToken tok = new PSSecurityToken("test");
      assertTrue(hdlr.doesDependencyExist(tok, keyword.getValue()));
      assertFalse(hdlr.doesDependencyExist(tok, "9999"));
      
      // test get dependency, dependencies
      Set<PSDependency> keywordDeps = new HashSet<PSDependency>();
      for (PSKeyword k : keywords)
      {
         PSDependency dep = hdlr.getDependency(tok, k.getValue());
         assertTrue(dep != null);
         keywordDeps.add(dep);
      }     
      
      Iterator depIter = hdlr.getDependencies(tok);
      int i = 0;
      while (depIter.hasNext())
      {
         assertTrue(keywordDeps.contains(depIter.next()));
         i++;
      }
      
      assertTrue(keywordDeps.size() == i);
   }
}


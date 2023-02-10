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
/*
 * test.percussion.pso.preview SiteFolderLocationTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.preview.SiteFolderLocation;
import com.percussion.server.PSRequestParsingException;
import com.percussion.services.sitemgr.IPSSite;

public class SiteFolderLocationTest
{
   private static final Logger log = LogManager.getLogger(SiteFolderLocation.class);
   
   SiteFolderLocation cut;
   Mockery context; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      cut = new SiteFolderLocation(); 
      cut.setFolderid(123);
      final IPSSite site = context.mock(IPSSite.class); 
      context.checking(new Expectations(){{
         allowing(site).getSiteId();
         will(returnValue(457L)); 
      }});
      cut.setSite(site); 
      //cut.setSiteid(457L); 
   }
   @Test
   public final void testGetParameterMap()
   {
      Map<String, Object> pmap = cut.getParameterMap(); 
      assertTrue(pmap.containsKey("sys_folderid"));
   }
   @Test
   public final void testFixUrl()
   {
      String url = "http://foo.percussion.local/xyz?a=b";
      
      try
      {
         String url2 = cut.fixUrl(url);
         assertNotNull(url2); 
         assertTrue(url2.contains("sys_folderid=123")); 
         assertTrue(url2.contains("sys_siteid=457")); 
      } catch (PSRequestParsingException ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception caught"); 
      } 
      
      
      
   }
}

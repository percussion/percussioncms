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
 * test.percussion.pso.preview ActiveAssemblyUrlBuilderTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.preview.ActiveAssemblyUrlBuilder;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;

public class ActiveAssemblyUrlBuilderTest
{
   private static final Logger log = LogManager.getLogger(ActiveAssemblyUrlBuilderTest.class);
   
   Mockery context; 
   ActiveAssemblyUrlBuilder cut; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      cut = new ActiveAssemblyUrlBuilder();
      
   }
   @Test
   public final void testBuildUrl()
   {
      final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class);
      final Map<String, Object> urlParams = new HashMap<String, Object>(); 
      
      urlParams.put(IPSHtmlParameters.SYS_CONTENTID, "1"); 
      urlParams.put(IPSHtmlParameters.SYS_REVISION, "1"); 
      
      try
      {
         context.checking(new Expectations(){{
            one(template).getGUID();
            will(returnValue(new PSLegacyGuid(100,1)));
            one(template).getAssemblyUrl();
            will(returnValue("../assembler/render"));
         }});
         
         cut.setDefaultLocationUrl("//default/location"); 
         cut.setMultipleLocationUrl("//multiple/location"); 
         
         String result = cut.buildUrl(template, urlParams, null, false);
         assertNotNull(result);
         log.info("result is " + result); 
         
         assertTrue(result.startsWith("//default/location")); 
         assertTrue(result.contains("sys_contentid=1"));
         assertTrue(result.contains("sys_variantid=100"));
         
         context.assertIsSatisfied(); 
         
         context.checking(new Expectations(){{
            one(template).getGUID();
            will(returnValue(new PSLegacyGuid(100,1)));
            one(template).getAssemblyUrl();
            will(returnValue("../assembler/render"));
         }});
         
         result = cut.buildUrl(template, urlParams, null, true);
         assertNotNull(result);
         log.info("result is " + result); 
         
         assertTrue(result.startsWith("//multiple/location")); 
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      } 
   }
}

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
package test.percussion.pso.preview;

import com.percussion.error.PSExceptionUtils;
import com.percussion.pso.preview.ConfigurableSiteLoaderImpl;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ConfigurableSiteLoaderImplTest {

	private static final Logger log = LogManager.getLogger(ConfigurableSiteLoaderImplTest.class); 
	   
    private ConfigurableSiteLoaderImpl cut; 
	   
	Mockery context;
	 
	IPSSiteManager siteMgr; 
	   
	@SuppressWarnings("static-access")
	@Before
	public void setUp() throws Exception {
		context = new Mockery(); 
		cut = new ConfigurableSiteLoaderImpl(); 
		siteMgr = context.mock(IPSSiteManager.class); 
		cut.setSiteMgr(siteMgr); 
	}

	@SuppressWarnings("serial")
	@Test
	public void testLoadAllSites() {
	    final IPSSite site1 = context.mock(IPSSite.class, "site1"); 
	    final IPSSite site2 = context.mock(IPSSite.class, "site2"); 
	    final List<IPSSite> sites = new ArrayList<IPSSite>(){{
	    	add(site1);
	    	add(site2);
	    }}; 
	    final List<String> allowed = new ArrayList<String>(){{add("site1");}};
	    
	    context.checking(new Expectations(){{
	    	atLeast(1).of(site1).getName(); 
	    	will(returnValue("site1"));
	    	atLeast(1).of(site2).getName(); 
	    	will(returnValue("site2")); 
	    	one(siteMgr).findAllSites();
	    	will(returnValue(sites)); 
	    }});
	    
	    
	    
	    try {
	    	cut.setAllowedSites(allowed); 
			List<IPSSite> results = cut.findAllSites();
			assertNotNull(results); 
			assertEquals(1,results.size()); 
			assertEquals("site1", results.get(0).getName()); 
			
			context.assertIsSatisfied(); 
			
		} catch (PSSiteManagerException e) {
			log.error("Exception caught {}", PSExceptionUtils.getMessageForLog(e));
			log.debug(PSExceptionUtils.getDebugMessageForLog(e));
			fail("Exception"); 
		} 
	    
	
	}

}

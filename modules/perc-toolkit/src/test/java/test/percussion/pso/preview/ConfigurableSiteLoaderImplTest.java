/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.preview.ConfigurableSiteLoaderImpl;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;

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
			log.error("Exception caught {}", e.getMessage());
			log.debug(e.getMessage(), e);
			fail("Exception"); 
		} 
	    
	
	}

}

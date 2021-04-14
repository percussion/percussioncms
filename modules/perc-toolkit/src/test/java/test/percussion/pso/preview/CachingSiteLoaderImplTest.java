/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * test.percussion.pso.preview CachingSiteLoaderImplTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.preview.CachingSiteLoaderImpl;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;

public class CachingSiteLoaderImplTest
{
   private static Log log = LogFactory.getLog(CachingSiteLoaderImplTest.class); 
   
   private CachingSiteLoaderImpl cut; 
   
   Mockery context;
   
   IPSSiteManager siteMgr; 
   
   public CachingSiteLoaderImplTest()
   {
      
   }
   @Before
   public void setUp() throws Exception
   {
      cut = new CachingSiteLoaderImpl();
      context = new Mockery();
      siteMgr = context.mock(IPSSiteManager.class);
      CachingSiteLoaderImpl.setSiteMgr(siteMgr);
      
   }
   @Test
   public final void testFindAllSites()
   {
      cut.setSiteReloadDelay(0L);
      final IPSSite site1 = context.mock(IPSSite.class);
      final List<IPSSite> sites = new ArrayList<IPSSite>();
      sites.add(site1); 
      
      try
      {
         context.checking(new Expectations(){{
            one(siteMgr).findAllSites();
            will(returnValue(sites)); 
         }});
         
         cut.afterPropertiesSet();
         
         List<IPSSite> results = cut.findAllSites(); 
         assertNotNull(results);
         assertEquals(1, results.size());
         assertEquals(site1, results.get(0)); 
         
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
          log.error("Unexpected Exception " + ex,ex);
          fail("Exception"); 
      }
   }
   
 
   
      
}

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
 * test.percussion.pso.preview SiteFolderLocationTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.preview.SiteFolderLocation;
import com.percussion.server.PSRequestParsingException;
import com.percussion.services.sitemgr.IPSSite;

public class SiteFolderLocationTest
{
   private Log log = LogFactory.getLog(SiteFolderLocation.class); 
   
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

/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.imageedit.data;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.ByteArrayDataSource;

public class ByteArrayDataSourceTest
{
   private static Log log = LogFactory.getLog(ByteArrayDataSourceTest.class);
   
   ByteArrayDataSource cut; 
   @Before
   public void setUp() throws Exception
   {
     
   }
   @Test
   public final void testByteArrayDataSourceStringStringInt()
   {
      log.debug("testing new bytearraydatasource"); 
      cut = new ByteArrayDataSource("xname", "text/plain", 42);
      assertEquals("xname", cut.getName());
      assertEquals("text/plain", cut.getContentType()); 
      assertEquals(0, cut.getBytes().length); 
   }
}

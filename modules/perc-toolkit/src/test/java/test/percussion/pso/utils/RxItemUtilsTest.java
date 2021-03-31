/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.utils;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSItemAccessor;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.pso.utils.RxItemUtils;

public class RxItemUtilsTest
{
   private static Log log = LogFactory.getLog(RxItemUtilsTest.class); 
   
   Mockery context; 
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{ setImposteriser(ClassImposteriser.INSTANCE);}};    
   }
   
   @Test
   public final void testIsBinaryFieldTrue()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class);
      final PSItemField fld = context.mock(PSItemField.class);
      final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class);
      
      context.checking(new Expectations(){{
         one(item).getFieldByName("a");
         will(returnValue(fld));
         one(fld).getItemFieldMeta();
         will(returnValue(meta));
         allowing(meta).getBackendDataType();
         will(returnValue(PSItemFieldMeta.DATATYPE_BINARY));
      }});
      boolean result = RxItemUtils.isBinaryField(item, "a");
      assertTrue(result); 
      context.assertIsSatisfied();
   }
   
   @Test
   public final void testIsBinaryFieldFalse()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class);
      final PSItemField fld = context.mock(PSItemField.class);
      final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class);
      
      context.checking(new Expectations(){{
         one(item).getFieldByName("a");
         will(returnValue(fld));
         one(fld).getItemFieldMeta();
         will(returnValue(meta));
         allowing(meta).getBackendDataType();
         will(returnValue(PSItemFieldMeta.DATATYPE_TEXT));
      }});
      boolean result = RxItemUtils.isBinaryField(item, "a");
      assertFalse(result);
      context.assertIsSatisfied();
   }
   
   
   @Test
   public final void testGetFieldBinary()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class);
      final PSItemField fld = context.mock(PSItemField.class);
      final PSBinaryValue value = context.mock(PSBinaryValue.class);
      final byte[] myArray = new byte[100]; 
      try
      {
         context.checking(new Expectations(){{
            one(item).getFieldByName("a");
            will(returnValue(fld));
            one(fld).getValue();
            will(returnValue(value));
            one(value).getValue();
            will(returnValue(myArray));
         }});
         
         Object o = RxItemUtils.getFieldBinary(item, "a"); 
         assertNotNull(o);
         context.assertIsSatisfied();
      } catch (PSCmsException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception");
      }
   }
}

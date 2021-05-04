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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.MasterImageMetaData;

public class MasterImageMetaDataTest
{
   private static Log log = LogFactory.getLog(MasterImageMetaDataTest.class);
   
   MasterImageMetaData cut; 
   
   @Before
   public void setUp() throws Exception
   {
      cut = new MasterImageMetaData(); 
   }
   
   @Test 
   public void testConstructor()
   {
      assertNotNull(cut.getSizedImages());
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testDescribe() 
   {
      try
      {
         cut.setAlt("alt string");
         cut.setSysTitle("sys title");
         cut.setDisplayTitle("display title"); 
         cut.setDescription("This is the description"); 
         Map  description = BeanUtils.describe(cut);
         assertTrue(description.size() > 0); 
         log.info("Master Image Metadata : " + description); 
         
         PropertyDescriptor[] p = PropertyUtils.getPropertyDescriptors(cut);
         assertNotNull(p); 
         for(PropertyDescriptor pd : p)
         {
            log.info("Property Descriptor " + pd.getName() + " type " 
                  + pd.getPropertyType()); 
         }
         
         BeanInfo beanInfo = Introspector.getBeanInfo(cut.getClass());
         assertNotNull(beanInfo);
         log.info("Bean Info " + beanInfo.getBeanDescriptor().getName());
         for(PropertyDescriptor pd2 : beanInfo.getPropertyDescriptors())
         {
            log.info("Property Descriptor " + pd2.getName() + " type " 
                  + pd2.getPropertyType().getName());
         }
         assertTrue("description complete", true); 
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      }
      
   }
}

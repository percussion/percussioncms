/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.imageedit.services.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.services.impl.ImageSizeDefinitionManagerImpl;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ImageSizeDefinitionManagerImplTest
{
   
   List<ImageSizeDefinition> sizes;
   
   ImageSizeDefinitionManagerImpl cut; 
   /**
    * @throws Exception
    */
   @Before
   public void setUp() throws Exception
   {
      cut = new ImageSizeDefinitionManagerImpl(); 
      sizes = new ArrayList<ImageSizeDefinition>(3);
      sizes.add(new ImageSizeDefinition(){{setCode("a");setLabel("Label A");}});
      sizes.add(new ImageSizeDefinition(){{setCode("b");setLabel("Label B");}});
      sizes.add(new ImageSizeDefinition(){{setCode("c");setLabel("Label C");}});
      cut.setSizes(sizes); 
   }
   /**
    * Test method for {@link ImageSizeDefinitionManagerImpl#getImageSize(String)}.
    */
   @Test
   public final void testGetImageSize()
   {
      ImageSizeDefinition r = cut.getImageSize("a"); 
      assertNotNull(r);
      assertEquals("Label A", r.getLabel()); 
      
      r = cut.getImageSize("q");
      assertNull(r); 
      
      
   }
}

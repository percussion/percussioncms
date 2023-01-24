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

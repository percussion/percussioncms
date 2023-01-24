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
package test.percussion.pso.imageedit.data;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.data.ImageMetaData;

public class ImageDataTest
{
   private static final Logger log = LogManager.getLogger(ImageDataTest.class);
   
   ImageData cut;
   @Before
   public void setUp() throws Exception
   {
      cut = new ImageData();
   }
   
   
   @Test
   public void testIsSerializable()
      {
         String sentence = "The quick brown fox jumped over the lazy dog"; 

         cut.setFilename("xyzzy.jpg");
         cut.setHeight(42);
         cut.setWidth(100);
         cut.setExt(".gif");
         cut.setMimeType("text/plain");
         cut.setSize(sentence.length()); 
         cut.setBinary(sentence.getBytes());
 
          try
         {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(out);
             oos.writeObject(cut);
             oos.close();
             assertTrue(out.toByteArray().length > 0);
             
             ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ; 
             ObjectInputStream ois = new ObjectInputStream(in);
             Object o = ois.readObject();
             
             ImageMetaData c2 = (ImageMetaData) o; 
             
             assertEquals(cut.getFilename(), c2.getFilename()); 
             assertEquals(cut.getHeight(), c2.getHeight());
             assertEquals(cut.getSize(), c2.getSize()); 
             assertEquals(cut.getWidth(), c2.getWidth()); 
             assertEquals(cut.getMimeType(), c2.getMimeType()); 
         } catch (Exception ex)
         {
               log.error("Unexpected Exception " + ex,ex);
               fail("exception");
         }
         
   }
}

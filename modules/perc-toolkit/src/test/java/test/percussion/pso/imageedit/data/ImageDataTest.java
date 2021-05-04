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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.data.ImageMetaData;

public class ImageDataTest
{
   private static Log log = LogFactory.getLog(ImageDataTest.class);
   
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

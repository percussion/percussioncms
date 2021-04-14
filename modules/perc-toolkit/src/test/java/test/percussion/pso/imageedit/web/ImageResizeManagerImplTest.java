/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.imageedit.web;

import static org.junit.Assert.*;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.data.ImageMetaData;
import com.percussion.pso.imageedit.web.impl.ImageResizeManagerImpl;

public class ImageResizeManagerImplTest
{
   Log log = LogFactory.getLog(ImageResizeManagerImplTest.class);
   
   TestableImageResizeManager cut;
   
   @Before
   public void setUp() throws Exception
   {
      cut = new TestableImageResizeManager();
   }
   
   @Test
   public final void testGenerateImageSimple()
   {
      try
      {
         InputStream imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg");
         
         ImageMetaData result = cut.generateImage(imgStream, null, null);
         
         assertNotNull(result);
         assertEquals(283, result.getWidth()); 
         assertEquals(212, result.getHeight());
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
   
   }
   
   @Test
   public final void testGenerateImageScaled()
   {
      try
      {
         InputStream imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
    
         Dimension imgSize = new Dimension(102,111);
         ImageMetaData result = cut.generateImage(imgStream, null, imgSize);
         
         assertNotNull(result);
         assertEquals(102, result.getWidth()); 
         assertEquals(111, result.getHeight());
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
   
   }
   
   @Test
   public final void testGenerateImageCropped()
   {
      try
      {
         InputStream imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
    
         Rectangle crop = new Rectangle(10, 20, 102, 111); 
         ImageMetaData result = cut.generateImage(imgStream, crop, null);
         
         assertNotNull(result);
         assertEquals(102, result.getWidth()); 
         assertEquals(111, result.getHeight());
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
   
   }
   
   @Test
   public final void testGenerateImageBoth()
   {
      try
      {
         InputStream imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
    
         Rectangle crop = new Rectangle(10, 20, 102, 111); 
         Dimension size = new Dimension(55, 75);
         ImageMetaData result = cut.generateImage(imgStream, crop, size);
         
         assertNotNull(result);
         assertEquals(55, result.getWidth()); 
         assertEquals(75, result.getHeight());
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
   
   }
   
   @Test
   public final void testComputeWidthFromAspect()
   {
      Rectangle box = new Rectangle(100, 200);
      Dimension size = new Dimension(300, 0); 
      
      Dimension result = cut.computeSizeFromAspectRatio(box, size); 
      assertEquals(600, result.height);
   }
   
   @Test
   public final void testComputeWidthFromAspectZero()
   {
      Rectangle box = new Rectangle(100, 0);
      Dimension size = new Dimension(300, 0); 
      
      Dimension result = cut.computeSizeFromAspectRatio(box, size); 
      assertEquals(0, result.height);
      assertEquals(300, result.width);
   }
   
   @Test
   public final void testComputeHeightFromAspect()
   {
      Rectangle box = new Rectangle(100, 200);
      Dimension size = new Dimension(0, 300); 
      
      Dimension result = cut.computeSizeFromAspectRatio(box, size); 
      assertEquals(150, result.width);
   }
   
   @Test
   public final void testComputeHeightFromAspectZero()
   {
      Rectangle box = new Rectangle(0, 200);
      Dimension size = new Dimension(0, 300); 
      
      Dimension result = cut.computeSizeFromAspectRatio(box, size); 
      assertEquals(0, result.width);
      assertEquals(300, result.height);
   }
   @Test
   public final void testImageQualityMetrics()
   {
      Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
      ImageWriter iw = iter.next(); 
      ImageWriteParam iwp = iw.getDefaultWriteParam();
      iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
       
      String[] compmodes = iwp.getCompressionTypes();
      for(String mode : compmodes)
      {
         log.info("compression mode " + mode); 
      }
      
      String[] qmodes = iwp.getCompressionQualityDescriptions(); 
      for(String qm : qmodes)
      {
         log.info("quality description " + qm);
      }
      
      float[] qvals = iwp.getCompressionQualityValues();
      for(float q : qvals)
      {
         log.info("quality values " + q); 
      }
      
      
   }
   
   @Test
   public final void testImageQualitySizes()
   {
      try
      {
         InputStream imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
         long prevSize = 0; 
         Dimension imgSize = new Dimension(100,100);
         cut.setCompression(0);
         ImageMetaData result = cut.generateImage(imgStream, null, imgSize);
         assertNotNull(result);
         assertTrue(result.getSize() > prevSize);
         prevSize = result.getSize(); 
         log.info("Compression 0, size " + result.getSize()); 
         
         imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
         cut.setCompression(0.5f); 
         result = cut.generateImage(imgStream, null, imgSize);
         assertNotNull(result);
         assertTrue(result.getSize() > prevSize);
         prevSize = result.getSize(); 
 
         log.info("Compression .5, size " + result.getSize()); 

         imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
         cut.setCompression(0.75f); 
         result = cut.generateImage(imgStream, null, imgSize);
         assertNotNull(result);
         assertTrue(result.getSize() > prevSize);
         prevSize = result.getSize(); 
 
         log.info("Compression .75, size " + result.getSize()); 
 
         imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
         cut.setCompression(0.85f); 
         result = cut.generateImage(imgStream, null, imgSize);
         assertNotNull(result);
         assertTrue(result.getSize() > prevSize);
         prevSize = result.getSize();  
         log.info("Compression .85, size " + result.getSize()); 
         
         imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
         cut.setCompression(0.95f); 
         result = cut.generateImage(imgStream, null, imgSize);
         assertNotNull(result);
         assertTrue(result.getSize() > prevSize);
         prevSize = result.getSize();  
         log.info("Compression .95, size " + result.getSize()); 
         
         imgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/Sample.jpg"); 
         cut.setCompression(1.0f); 
         result = cut.generateImage(imgStream, null, imgSize);
         assertNotNull(result);
         assertTrue(result.getSize() > prevSize);
         prevSize = result.getSize();  
         log.info("Compression 1, size " + result.getSize()); 
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
   }
   
   //@Test
   public final void testBigImageMemory()
   {
      ImageData result;
      Dimension imgSize = new Dimension(100,100);
      try
      {        
         InputStream bigImgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/DSC_1720.jpg");
         assertNotNull(bigImgStream);
         result = cut.generateImage(bigImgStream, null, imgSize);
         FileOutputStream fios = new FileOutputStream("thumb100x100.jpg"); 
         fios.write(result.getBinary()); 
         fios.flush();
         fios.close(); 
         
      } catch (Throwable ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
   }
   
   @Test
   public final void testHalfBigImage()
   {
      BufferedImage result;
      try
      {        
         InputStream bigImgStream = getClass().getClassLoader().getResourceAsStream("test/percussion/pso/imageedit/services/impl/DSC_1720.jpg");
         assertNotNull(bigImgStream);
         BufferedImage inImage = ImageIO.read(bigImgStream);
         result = cut.halfImage(inImage);
         
         FileOutputStream fios = new FileOutputStream("halfimage.jpg");
         ImageIO.write(result,"jpeg",fios); 
         //fios.write(ImageIO.w); 
         fios.flush();
         fios.close(); 
         
      } catch (Throwable ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
   }      
   
   private class TestableImageResizeManager extends ImageResizeManagerImpl
   {

      @Override
      public BufferedImage halfImage(BufferedImage inImage)
      {
         return super.halfImage(inImage);
      }

      @Override
      public BufferedImage scaleImage(BufferedImage inImage,
            Rectangle sourceBox, Dimension outSize)
      {
         return super.scaleImage(inImage, sourceBox, outSize);
      }
 
      
   }
}

/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.transform;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;


import com.percussion.pso.transform.PSOThumbnailGenerator;

public class PSOThumbnailGeneratorTest
{
   static Log log = LogFactory.getLog(PSOThumbnailGeneratorTest.class);
   
   private static final String GIF_IMAGE = "test/percussion/pso/transform/ninja_avatar.gif";
   private static final String PNG_IMAGE = "test/percussion/pso/transform/TestPNG.png";
   private static final String LEO_IMAGE = "test/percussion/pso/transform/leonardo.jpg";
   
   
   
   TestThumbnailGenerator cut;  
   
   
   @Before
   public void setUp() throws Exception
   {
      cut = new TestThumbnailGenerator(); 
   }
   
   @Test
   public final void testComputeSizeBoth()
   {
      Dimension originalSize = new Dimension(400,300); 
      Dimension result = cut.computeSize(0, 100, 200, originalSize); 
      assertNotNull(result);
      assertEquals(100, result.width);
      assertEquals(200, result.height); 
   }
   
   @Test
   public final void testComputeSizeWidth()
   {
      Dimension originalSize = new Dimension(100,200); 
      Dimension result = cut.computeSize(0, 50, 0 , originalSize); 
      assertNotNull(result);
      assertEquals(50, result.width);
      assertEquals(100, result.height); 
   }
   @Test
   public final void testComputeSizeHeight()
   {
      Dimension originalSize = new Dimension(100,200); 
      Dimension result = cut.computeSize(0, 0 , 100 , originalSize); 
      assertNotNull(result);
      assertEquals(50, result.width);
      assertEquals(100, result.height); 
   }
   @Test
   public final void testComputeSizeNone()
   {
      Dimension result;
      try
      {
         Dimension originalSize = new Dimension(100,200); 
         result = cut.computeSize(0, 0 , 0 , originalSize); 
         
      } catch (IllegalArgumentException ex)
      {
            log.info("Expected Exception caught");
            assertTrue("expected exception", true); 
      }
   }
   
   @Test
   public final void testComputeSizeMaxdimHeight()
   {
      Dimension originalSize = new Dimension(100,200); 
      Dimension result = cut.computeSize(100, 0 , 0 , originalSize); 
      assertNotNull(result);
      assertEquals(50, result.width);
      assertEquals(100, result.height); 
   }
   
   @Test
   public final void testComputeSizeMaxdimWidth()
   {
      Dimension originalSize = new Dimension(200,100); 
      Dimension result = cut.computeSize(100, 0 , 0 , originalSize); 
      assertNotNull(result);
      assertEquals(100, result.width);
      assertEquals(50, result.height); 
   }

   @Test
   public final void testHalfImage()
   {
      BufferedImage inImage = new BufferedImage(1000, 2000, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = inImage.createGraphics();
      g2d.setColor(Color.CYAN); 
      g2d.fillRect(0, 0, 1000, 2000);
      g2d.dispose(); 
      
      BufferedImage result = cut.halfImage(inImage); 
      assertNotNull(result);
      assertEquals(500, result.getWidth());
      assertEquals(1000, result.getHeight()); 
      assertEquals(Transparency.OPAQUE, result.getTransparency());
   }
   
   @Test
   public final void testReadJpeg()
   {
	   log.info("reading a jpeg"); 
	   InputStream is = getClass().getResourceAsStream("/com/percussion/pso/transform/leonardo.jpg");
	   assertNotNull(is); 
	   try {
		   ImageInputStream iis = ImageIO.createImageInputStream(is);
		   ImageReader ir = cut.findCompatibleReader(iis); 
		   assertNotNull(ir); 
		   assertEquals("JPEG", ir.getFormatName()); 
		   BufferedImage image = cut.readImage(iis, ir); 
		   assertNotNull(image); 
		   ColorModel cm = image.getColorModel();
		   log.info("Color Model is " + cm.getClass().getCanonicalName());
		   assertTrue(cm.getClass().getCanonicalName().contains("ComponentColorModel"));
		   log.info("height is " + image.getHeight() + " width is " + image.getWidth());
		   assertEquals(128, image.getHeight());
		   assertEquals(128, image.getWidth()); 
	   } catch (IOException e) {
		   log.error("Exception " + e, e); 
		   fail("Exception"); 
	   }
   }
   
   @Test
   public final void testReadGif()
   {
	   log.info("reading a GIF"); 
	   InputStream is = getClass().getResourceAsStream("/com/percussion/pso/transform/ninja_avatar.gif");
	   assertNotNull(is); 
	   try {
		   ImageInputStream iis = ImageIO.createImageInputStream(is);
		   ImageReader ir = cut.findCompatibleReader(iis); 
		   assertNotNull(ir); 
		   assertEquals("gif", ir.getFormatName()); 
		   BufferedImage image = cut.readImage(iis, ir); 
		   assertNotNull(image); 
		   ColorModel cm = image.getColorModel();
		   log.info("Color Model is " + cm.getClass().getCanonicalName());
		   assertTrue(cm.getClass().getCanonicalName().contains("IndexColorModel"));
		   assertTrue(cm.hasAlpha()); 
		   log.info("height is " + image.getHeight() + " width is " + image.getWidth());
		   assertEquals(100, image.getHeight());
		   assertEquals(100, image.getWidth());

	   } catch (IOException e) {
		   log.error("Exception " + e, e); 
		   fail("Exception"); 
	   }
   }
   
   @Test
   public final void testReadPng()
   {
	   log.info("reading a PNG"); 
	   InputStream is = getClass().getResourceAsStream("/com/percussion/pso/transform/TestPNG.png");
	   assertNotNull(is); 
	   try {
		   ImageInputStream iis = ImageIO.createImageInputStream(is);
		   ImageReader ir = cut.findCompatibleReader(iis); 
		   assertNotNull(ir); 
		   assertEquals("png", ir.getFormatName()); 
		   BufferedImage image = cut.readImage(iis, ir); 
		   assertNotNull(image); 
		   ColorModel cm = image.getColorModel();
		   log.info("Color Model is " + cm.getClass().getCanonicalName());
		   assertTrue(cm.getClass().getCanonicalName().contains("ComponentColorModel"));
		   assertTrue(cm.hasAlpha());
		   log.info("height is " + image.getHeight() + " width is " + image.getWidth());
		   assertEquals(50, image.getHeight());
		   assertEquals(250, image.getWidth());
		   
	   } catch (IOException e) {
		   log.error("Exception " + e, e); 
		   fail("Exception"); 
	   }
   }
   
   @Test
   public final void testResizeGif()
   {
	   log.info("resizing a GIF"); 
	   InputStream is = getClass().getResourceAsStream("/com/percussion/pso/transform/ninja_avatar.gif");
	   assertNotNull(is); 
	   try {
		   ImageInputStream iis = ImageIO.createImageInputStream(is);
		   ImageReader ir = cut.findCompatibleReader(iis); 
		   assertNotNull(ir); 
		   assertEquals("gif", ir.getFormatName()); 
		   BufferedImage image1 = cut.readImage(iis, ir); 
		   assertNotNull(image1);
		   BufferedImage image = cut.paintImage(image1, 50, 0, 0);
		   assertNotNull(image); 
		   ColorModel cm = image.getColorModel();
		   log.info("Color Model is " + cm.getClass().getCanonicalName());
		   assertTrue(cm.getClass().getCanonicalName().contains("IndexColorModel"));
		   assertTrue(cm.hasAlpha()); 
		   log.info("height is " + image.getHeight() + " width is " + image.getWidth());
		   assertEquals(50, image.getHeight());
		   assertEquals(50, image.getWidth());

	   } catch (IOException e) {
		   log.error("Exception " + e, e); 
		   fail("Exception"); 
	   }
   }
   
   @Test
   public final void testWriteJpeg()
   {
	   log.info("writing a jpeg"); 
	   InputStream is = getClass().getResourceAsStream("/com/percussion/pso/transform/leonardo.jpg");
	   assertNotNull(is); 
	   try {
		   ImageInputStream iis = ImageIO.createImageInputStream(is);
		   ImageReader ir = cut.findCompatibleReader(iis); 
		   assertNotNull(ir); 
		   BufferedImage image = cut.readImage(iis, ir); 
		   assertNotNull(image);
		   ByteArrayOutputStream baos = new ByteArrayOutputStream();
		   ImageWriter iw = ImageIO.getImageWriter(ir); 
		   cut.writeImage(image, iw, baos); 
//		   String sig = new String(Arrays.copyOfRange(baos.toByteArray(), 6, 10)); 
//		   assertEquals("JFIF", sig); 
		   
	   } catch (IOException e) {
		   log.error("Exception " + e, e); 
		   fail("Exception"); 
	   }
   }
   
   private class TestThumbnailGenerator extends PSOThumbnailGenerator
   {

      @Override
      public Dimension computeSize(int maxDim, int thumbWidth,
            int thumbHeight, Dimension originalSize)
      {
         return super.computeSize(maxDim, thumbWidth, thumbHeight, originalSize);
      }

      @Override
      public BufferedImage halfImage(BufferedImage inImage)
      {
         return super.halfImage(inImage);
      }

	@Override
	public BufferedImage createBufferedImage(int width, int height,
			BufferedImage baseImage) {
		return super.createBufferedImage(width, height, baseImage);
	}

	

	
	@Override
	public ImageReader findCompatibleReader(ImageInputStream iis) {
		return super.findCompatibleReader(iis);
	}

	

	@Override
	public Graphics2D getGraphics(BufferedImage image) {
		return super.getGraphics(image);
	}

	@Override
	public BufferedImage paintImage(BufferedImage inImage, int maxDim,
			int width, int height) {
		return super.paintImage(inImage, maxDim, width, height);
	}

	@Override
	public BufferedImage readImage(ImageInputStream iis, ImageReader reader)
			throws IOException {
		return super.readImage(iis, reader);
	}

	@Override
	public void writeImage(BufferedImage outImage, ImageWriter iw,
			OutputStream outstream) throws IOException {
		super.writeImage(outImage, iw, outstream);
	}

      
   }
}

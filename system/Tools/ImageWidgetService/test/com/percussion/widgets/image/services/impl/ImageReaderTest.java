/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * 
 */
package com.percussion.widgets.image.services.impl;

import com.percussion.widgets.image.data.ImageData;
import com.percussion.widgets.image.web.impl.ImageReader;
import com.percussion.widgets.image.web.impl.ImageReader.ImageReaderException;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.fail;

/**
 * @author matthew.ernewein
 * 
 */
public class ImageReaderTest
{

   @BeforeClass
   public static void runBeforeClass()
   {

       ImageIO.scanForPlugins();
       System.out.println("=============================Testing Image Reader");
   }

   @Test
   public void testPng() throws IOException
   {
      testImage("png_test.png"); 
   }
   
   @Test
   public void testAdobeIllistrator()throws IOException
   {
      testImage("Adobe_Illistrator_test.jpg");
   }
   
   @Test
   public void testAdobePhotoshop()throws IOException
   {
      testImage("Adobe_Photoshop_test.jpg"); 
   }
   
   @Test
   public void testAdobePhotoshopTwo()throws IOException
   {
      testImage("Adobe_Photoshop_test_2.jpg"); 
   }

   @Test
   public void testCmykJpeg()throws IOException
   {
      testImage("cmyk_jpg_test.jpg");
   }

   @Test
   public void testTif()throws IOException
   {
      testImage("tif_test.tif");
   }

   @Test
   public void testSVG() throws IOException{
      testImage("anenome.svg");
   }

   @Test
   public void testLineTif() throws IOException
   {
      testImage("small_tif_test.tif");
   }

   @Test
   public void testAdobeCmykEmbedded() throws IOException
   {
      testImage("embedded_jpg_test.jpg");
   }

   @Test
   public void testAdobeCmykNotEmbedded()throws IOException
   {
      testImage("not_embedded_jpg_test.jpg");
   }

   @Test
   public void testSmallGif() throws IOException
   {
      //This GIF in particular had an issue with site sucker.
      testImage("small_gif.gif");
   }

   private void testImage(String resourceFileName) throws IOException
   {
      String path = "com/percussion/widgets/image/services/impl/resources/";
      String resourcePath = path + resourceFileName;

      BufferedImage bufferedImage = ImageIO.read(getClass().getClassLoader()
              .getResourceAsStream(resourcePath));
      testResize(resourcePath);
      Assert.assertNotNull("Buffered image is null after read:" + resourcePath,
            bufferedImage);
   }

   private boolean testResize(String resourcePath)
   {
      ImageResizeManagerImpl resizeManager = new ImageResizeManagerImpl();
      boolean success = true;
      try
      {
         ImageData resizedImage = resizeManager.generateImage(getClass()
               .getClassLoader().getResourceAsStream(resourcePath));

         Assert.assertTrue(
               "Invalid ImageData for generateImage(InputStream input)",
               validateImageData(resizedImage));
      }
      catch (Exception e)
      {
         Assert.fail("Caught exception on resize");
         e.printStackTrace();
      }
      return success;
   }

   private boolean validateImageData(ImageData imageData)
   {
      boolean valid = true;
      if (imageData == null)
      {
         valid = false;
      }
      if ((imageData.getBinary() == null)
            || (imageData.getBinary().length == 0))
      {
         valid = false;
         Assert.fail("Invalid ImageData returned after resize");
      }
      return valid;
   }

   private BufferedImage readImage(byte[] imageBytes)
   {
      BufferedImage bufferedImage = null;
      try
      {
         long startTime = System.currentTimeMillis();
         bufferedImage = ImageReader.read(imageBytes);
         System.out.print("Image Read Time: "
               + (System.currentTimeMillis() - startTime) + " ms\n");
      }
      catch (ImageReaderException imageReaderException)
      {
         Assert.fail("Caught image reader exception");
         imageReaderException.printStackTrace();
      }
      return bufferedImage;
   }

   private ImageInfo getImageInfo(byte[] imageBytes)
   {
      ImageInfo imageInfo = null;
      try
      {
         imageInfo = Imaging.getImageInfo(imageBytes);

         System.out.print("=============================Testing "
               + imageInfo.getFormatName() + " | Color Type: "
               + imageInfo.getColorType() + "\n");

         System.out.print(imageInfo.toString());
      }
      catch (ImageReadException e)
      {
         Assert.fail("Caught image read exception getting image information:"
               + e.getMessage());
         e.printStackTrace();
      }
      catch (IOException e)
      {
         Assert.fail("Caught IO exception getting image information:"
               + e.getMessage());
         e.printStackTrace();
      }
      return imageInfo;
   }

   private byte[] readBytesForImageResource(String resourceLocation)
   {
      InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream(resourceLocation);
      byte[] imageBytes = null;
      try
      {
         imageBytes = IOUtils.toByteArray(inputStream);
      }
      catch (IOException e)
      {
         fail("unable to read image as test resource");
      }
      return imageBytes;
   }
}
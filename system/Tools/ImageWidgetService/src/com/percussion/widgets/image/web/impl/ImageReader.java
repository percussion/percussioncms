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

package com.percussion.widgets.image.web.impl;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.jpeg.JpegImageParser;
import org.apache.commons.imaging.formats.jpeg.segments.Segment;
import org.apache.commons.imaging.formats.jpeg.segments.UnknownSegment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author robertjohansen
 *
 */
@Deprecated
public final class ImageReader
{
   private static final Logger LOG = LogManager.getLogger(ImageReader.class);

   public static final class ImageReaderException extends Exception
   {
      /**
       * Empty Constructor
       */
      protected ImageReaderException()
      {
         super();
      }
   }
   
   /**
    * Apply private construction: This is a static utility class
    */
   private ImageReader()
   {
      
   }

   /**
    * Gets Sanselan ImageInfo for a byte array containing a Jpeg
    * 
    * @param imageByteArray A byte array containing a Jpeg
    * @return Sanselan ImageInfo for byte array
    * @throws ImageReaderException
    */
   public static ImageInfo getImageInfo(final byte[] imageByteArray)
         throws ImageReaderException
   {
      try
      {
         return Imaging.getImageInfo(imageByteArray);
      }
      catch (Exception e)
      {
         logException(e);
         throw getException();
      }
   }

   /**
    * Reads a Jpeg byte array into a buffered image
    * 
    * @param imageByteArray A byte array containing a Jpeg
    * @return A BufferedImage, parsed from the stream.
    * @throws Exception
    */
   public static BufferedImage read(final byte[] imageByteArray) throws ImageReaderException
   {
      BufferedImage image = null;
      try
      {
         image = ImageIO.read(new ByteArrayInputStream(imageByteArray));

         if (image == null)
         {
            image = Imaging.getBufferedImage(imageByteArray);
         }

      }
      catch (Exception e)
      {
         logException(e);
         throw getException();
      }

      return image;
   }
   

   /**
    * Checks a Jpeg byte array for the Adobe marker
    * 
    * @param imageBytes A byte array containing a Jpeg
    * @throws IOException
    * @throws ImageReadException
    */
   public static boolean hasAdobeMarker(byte[] imageBytes) throws IOException,
         ImageReadException
   {
      boolean hasAdobeMarker = false;
      @SuppressWarnings("rawtypes")
      List<Segment> segments;
      segments = getSegments(imageBytes);
      if (segments != null && segments.size() >= 1)
      {
         UnknownSegment app14Segment = (UnknownSegment) segments.get(0);
         byte[] data = app14Segment.getSegmentData();
         if (data.length >= 12 && data[0] == 'A' && data[1] == 'd'
               && data[2] == 'o' && data[3] == 'b' && data[4] == 'e')
         {
            hasAdobeMarker = true;
         }
      }
      return hasAdobeMarker;
   }

   /**
    * Checks an Jpeg byte array for YCCK color
    * 
    * @param imageBytes A byte array containing a Jpeg
    * @throws IOException
    * @throws ImageReadException
    */
   public static boolean isYcck(byte[] imageBytes) throws IOException,
         ImageReadException
   {
      boolean isYcck = false;
      List<Segment> segments = getSegments(imageBytes);
      if (segments != null && segments.size() >= 1)
      {
         UnknownSegment app14Segment = (UnknownSegment) segments.get(0);
         byte[] data = app14Segment.getSegmentData();
         if (data.length >= 12 && data[0] == 'A' && data[1] == 'd'
               && data[2] == 'o' && data[3] == 'b' && data[4] == 'e')
         {
            int transform = app14Segment.getSegmentData()[11] & 0xff;
            if (transform == 2)
               isYcck = true;
         }
      }
      return isYcck;
   }

   /**
    * Gets a list of segments from a Jpeg byte array
    * 
    * @param imageBytes A byte array containing a Jpeg
    * @return a list of segments
    * @throws ImageReadException
    * @throws IOException
    */
   private static List<Segment> getSegments(byte[] imageBytes)
         throws ImageReadException, IOException
   {
      JpegImageParser parser = new JpegImageParser();
      ByteSource byteSource = new ByteSourceInputStream(
            new ByteArrayInputStream(imageBytes), "");

      return parser.readSegments(byteSource, new int[]
      {0xffee}, true);
   }

   /**
    * Handles color inversion for Adobe based Jpegs
    * 
    * @param raster A writable raster
    */
   public static void convertInvertedColors(WritableRaster raster)
   {
      int height = raster.getHeight();
      int width = raster.getWidth();
      int stride = width * 4;
      int[] pixelRow = new int[stride];
      for (int h = 0; h < height; h++)
      {
         raster.getPixels(0, h, width, 1, pixelRow);
         for (int x = 0; x < stride; x++)
            pixelRow[x] = 255 - pixelRow[x];
         raster.setPixels(0, h, width, 1, pixelRow);
      }
   }

   /**
    * Handles YCCK conversion to CMYK
    * 
    * @param raster A writable raster
    */
   public static void handleYcckToCmyk(WritableRaster raster)
   {
      int height = raster.getHeight();
      int width = raster.getWidth();
      int stride = width * 4;
      int[] pixelRow = new int[stride];
      for (int h = 0; h < height; h++)
      {
         raster.getPixels(0, h, width, 1, pixelRow);

         for (int x = 0; x < stride; x += 4)
         {
            int y = pixelRow[x];
            int cb = pixelRow[x + 1];
            int cr = pixelRow[x + 2];

            int c = (int) (y + 1.402 * cr - 178.956);
            int m = (int) (y - 0.34414 * cb - 0.71414 * cr + 135.95984);
            y = (int) (y + 1.772 * cb - 226.316);

            if (c < 0)
               c = 0;
            else if (c > 255)
               c = 255;
            if (m < 0)
               m = 0;
            else if (m > 255)
               m = 255;
            if (y < 0)
               y = 0;
            else if (y > 255)
               y = 255;

            pixelRow[x] = 255 - c;
            pixelRow[x + 1] = 255 - m;
            pixelRow[x + 2] = 255 - y;
         }

         raster.setPixels(0, h, width, 1, pixelRow);
      }
   }

   /**
    * Converts a CMYK image to RGB given it's raster and ICC profile
    * 
    * @param cmykRaster The Raster on which to work
    * @param cmykProfile The CMYK profile for the image
    * @return A RGB Buffered Image
    * @throws IOException
    * @throws ImageReaderException
    */
   public static BufferedImage convertCmykToRgb(Raster cmykRaster,
         ICC_Profile cmykProfile) throws ImageReaderException
   {
      try
      {
         if (cmykProfile == null)
         {

            LOG.info("Attempting to convert a CMYK image without an embedded profile");
            ImageReader nonStaticReader = new ImageReader();
            cmykProfile = ICC_Profile
                  .getInstance(nonStaticReader
                        .getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                              "com/percussion/widgets/image/services/impl/ISOcoated_v2_300_eci.icc"));
         }
         ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
         BufferedImage rgbImage = new BufferedImage(cmykRaster.getWidth(),
               cmykRaster.getHeight(), BufferedImage.TYPE_INT_RGB);

         WritableRaster rgbRaster = rgbImage.getRaster();
         ColorSpace rgbCS = rgbImage.getColorModel().getColorSpace();
         ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
         cmykToRgb.filter(cmykRaster, rgbRaster);
         return rgbImage;
      }
      catch (Exception e)
      {
         logException(e);
         throw getException();
      }
   }

   /**
    * Generates an ImageReadException
    * 
    * @return A new ImageReaderException
    */
   private static ImageReaderException getException()
   {
      return new ImageReaderException();
   }

   /**
    * Logs exceptions for this class. Sources known exception types, always logs
    * a general Log.error(exception)
    * 
    * @param exception the exception to log
    */
   private static void logException(final Exception exception)
   {
      if (exception instanceof ImageReaderException)
      {
         LOG.error("Unable to read image format: ImageReadException");
      }
      else if (exception instanceof IOException)
      {
         LOG.error("Uable to read image source: IOException");
      }
      LOG.error(exception);
   }
}

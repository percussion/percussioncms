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
package com.percussion.extensions.general;

import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.PSPurgableTempFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * This class is a Rhythmyx pre-exit which examines the incoming HTML
 * parameters for attached files. If one of the files is an image then
 * the width and height dimensions will be extracted and will be added to
 * the request as image size attributes. Requires the SUN JAI library.
 */
public class PSImageInfoExtractor extends PSFileInfo
   implements IPSItemInputTransformer
{

   /**
    * Pre processes the request.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws
         PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException
   {
      //first do the normal file info processing
      super.preProcessRequest(params, request);

      Set paramKeys = new HashSet(request.getParameters().keySet());
      Iterator iter = paramKeys.iterator();
      while (iter.hasNext())
      {
         String paramName = (String)iter.next();
         Object obj = request.getParameterObject(paramName);
         if (obj instanceof PSPurgableTempFile)
         {
            PSPurgableTempFile temp = (PSPurgableTempFile) obj;
            String sourceName = temp.getSourceFileName();
            // first, check to see if this file is being cleared
            String clearValue = request.getParameter(paramName + "_clear");
            if (clearValue != null && clearValue.equals("yes"))
            {
               // the file is being cleared, clear the fileinfo params
               clearImageParams(request, paramName);
            }
            // only set the parameters if an uploaded file is found
            else if (sourceName != null && sourceName.trim().length() > 0)
            {
               //we should not proceed if the current file's mimetype is not
               // image..
               String mimetype =
                  request.getParameter(paramName + "_type").toString();
               if (mimetype.indexOf("image") >= 0)
               {
                  ImageSize imageSize = null;
                  if (temp != null)
                  {
                     try
                     {
                        imageSize =
                           getImageSize(
                              temp.getAbsolutePath());
                     }
                     catch (FileNotFoundException fnfe)
                     {
                        request.printTraceMessage(
                           "File Not Found Exception -- " + fnfe.toString());
                        fnfe.printStackTrace();
                     }
                     catch (Exception e)
                     {
                        request.printTraceMessage(
                           "Exception -- " + e.toString());
                     }

                     if (imageSize != null)
                     {
                        String imageHeight = imageSize.getHeight();
                        String imageWidth = imageSize.getWidth();

                        request.setParameter(
                           paramName + "_" + IMAGE_WIDTH,
                           imageWidth);
                        request.setParameter(
                           paramName + "_" + IMAGE_HEIGHT,
                           imageHeight);
                     }
                  } //end (temp != null)
               } //end if mimetype.indexOf...
               else
               {
                  request.printTraceMessage("The mime type is not image" + mimetype);
                  request.printTraceMessage(
                     "The mime type is not image" + mimetype.indexOf("image"));
               }
            } //end else if
         } //end  if(obj instanceof PSPurgableTempFile)
      } //end while(iter.hasNext())

   }


   /**
    * Clears image size attribute parameters from
    * the request.
    *
    * @param request the <code>IPSRequestContext</code> cannot
    * be <code>null</code>.
    * @param paramName the parameter name prefix. Cannot be <code>null</code>.
    */
   private void clearImageParams(IPSRequestContext request, String paramName)
   {
      if(null == request)
         throw new IllegalArgumentException("Request cannot be null.");
      if(null == paramName)
         throw new IllegalArgumentException("param name cannot be null.");
      request.printTraceMessage("clearing fileinfo params for " + paramName);
      request.setParameter(paramName + "_" + IMAGE_WIDTH, "");
      request.setParameter(paramName + "_" + IMAGE_HEIGHT, "");

   }

   /**
    * Returns image size dimensions
    *
    * @param filename path of the image file. Cannot be <code>null</code>
    * or empty.
    * @return the image width and height as a <code>ImageSize</code>
    * object.
    * @throws IOException 
    */
   private ImageSize getImageSize(String filename)
      throws IOException
   {
      if(null == filename || filename.trim().length() == 0)
         throw new IllegalArgumentException("Image filename cannot be null.");

      BufferedImage src = ImageIO.read(new File(filename));
      return new ImageSize(src.getWidth(), src.getHeight());
   }

   /**
    * Helper inner class to hold image size dimensions
    */
   private class ImageSize
   {
      private ImageSize(String width, String height)
      {
         mi_width = width;
         mi_height = height;
      }

      private ImageSize(int width, int height)
      {
         this(Integer.toString(width), Integer.toString(height));
      }
      /**
       * Returns the image width as a String
       * @return image width
       */
      private String getWidth()
      {
         return mi_width;
      }

      /**
       * Returns the image height as a String
       * @return image height
       */
      private String getHeight()
      {
         return mi_height;
      }
      private String mi_width;
      private String mi_height;

   }

   /**
    * Image Height
    */
   public static final String IMAGE_HEIGHT = "height";

   /**
    * Image Width
    */
   public static final String IMAGE_WIDTH = "width";



}

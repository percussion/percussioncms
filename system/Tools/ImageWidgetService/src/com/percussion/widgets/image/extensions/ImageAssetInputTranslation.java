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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.widgets.image.extensions;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSServer;
import com.percussion.tools.PSCopyStream;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.widgets.image.data.ImageData;
import com.percussion.widgets.image.services.ImageCacheManager;
import com.percussion.widgets.image.services.ImageCacheManagerLocator;
import com.percussion.widgets.image.services.ImageResizeManager;
import com.percussion.widgets.image.services.ImageResizeManagerLocator;
import com.percussion.widgets.image.web.impl.ImageReader;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageAssetInputTranslation extends PSDefaultExtension implements IPSItemInputTransformer
{
   private static final Logger log = LogManager.getLogger(ImageAssetInputTranslation.class);

   ImageCacheManager cacheManager = null;

   ImageResizeManager resizeManager = null;

   public void init(IPSExtensionDef def, File file) throws PSExtensionException
   {
      super.init(def, file);
      if (this.cacheManager == null)
      {
         this.cacheManager = ImageCacheManagerLocator.getImageCacheManager();
      }
      if (this.resizeManager == null)
      {
         this.resizeManager = ImageResizeManagerLocator.getImageResizeManager();
      }
   }

   /**
    * @throws PSAuthorizationException
    * @throws PSRequestValidationException
    * @throws PSParameterMismatchException
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request) throws PSAuthorizationException,
         PSRequestValidationException, PSParameterMismatchException, PSExtensionProcessingException
   {
      try
      {
         PSExtensionParams ep = new PSExtensionParams(params);
         String imageName = ep.getStringParam(0, "img", false);
         String thumbName = ep.getStringParam(1, "img2", false);

         if (StringUtils.isBlank(request.getParameter(thumbName + "_id")))
         {
            PSPurgableTempFile imageFile = (PSPurgableTempFile) request.getParameterObject(imageName);
            if (imageFile != null)
            {
               updateRequest(request, thumbName, generateThumbnail(imageFile));

               String mimeType = request.getParameter(imageName + "_type");
               updateRequest(request, imageName, generateImage(imageFile, mimeType));
            }
            else
            {
               this.log.debug("a value was not found for parameter " + imageName);
            }
         }
         else
         {
            processInputImage(request, imageName);
            processInputImage(request, thumbName);
         }
      }
      catch (Exception ex)
      {
         this.log.error("Unexpected Exception " + ex, ex);
         throw new PSExtensionProcessingException(getClass().getName(), ex);
      }
   }

   protected void processInputImage(IPSRequestContext request, String base) throws Exception
   {
      String dirty = request.getParameter(base + "_dirty");
      dirty = "true";
      if (StringUtils.isBlank(dirty))
      {
         this.log.debug("image " + base + " is not dirty");
         return;
      }
      String imageKey = request.getParameter(base + "_id");
      if (StringUtils.isBlank(imageKey))
      {
         this.log.debug("Image key is blank ");
         return;
      }
      ImageData iData = this.cacheManager.getImage(imageKey);
      if (iData == null)
      {
         this.log.info("Image not found in the cache for key " + imageKey);
         return;
      }
      updateRequest(request, base, iData);
   }

   private void updateRequest(IPSRequestContext request, String prefix, ImageData iData) throws Exception
   {
      PSPurgableTempFile temp = writeFile(iData);
      this.log.debug("updating file for " + prefix + " " + temp.getCanonicalPath());

      request.setParameter(prefix, temp);
      request.setParameter(prefix + "_ext", iData.getExt());
      request.setParameter(prefix + "_filename", iData.getFilename());
      request.setParameter(prefix + "_type", iData.getMimeType());
      request.setParameter(prefix + "_size", String.valueOf(iData.getSize()));
      request.setParameter(prefix + "_height", String.valueOf(iData.getHeight()));
      request.setParameter(prefix + "_width", String.valueOf(iData.getWidth()));
      request.setParameter(prefix + "_id", null);
   }

   private ImageData generateImage(PSPurgableTempFile imageFile, String mimeType) throws Exception
   {
      try(FileInputStream fin = new FileInputStream(imageFile)){
         ImageData iData = this.resizeManager.generateImage(fin);
         iData.setFilename(imageFile.getSourceFileName());
         iData.setMimeType(mimeType);

         ImageData localImageData1 = iData;
         return localImageData1;
      }
   }

   private ImageData generateThumbnail(PSPurgableTempFile imageFile) throws Exception
   {
      ImageData iData = null;
      int width;

      Properties serverProps = PSServer.getServerProps();
      String thumbWidthStr = serverProps.getProperty("imageThumbnailWidth", "50");
      int thumbWidth = Integer.parseInt(thumbWidthStr);
      try(      FileInputStream fin = new FileInputStream(imageFile)){
         final byte[] imageByteArray = IOUtils.toByteArray(fin);
         BufferedImage image = ImageReader.read(imageByteArray);
         if (image != null)
         {
            width = image.getWidth();
            int height = image.getHeight();
            Rectangle rec = new Rectangle(0, 0, width, height);
            Dimension dim = new Dimension(thumbWidth, Math.round(height / width * thumbWidth));
            try(FileInputStream fin2 = new FileInputStream(imageFile)) {
               iData = this.resizeManager.generateImage(fin2, rec, dim);
            }
         }

         return iData;
      }
   }

   protected PSPurgableTempFile writeFile(ImageData iData) throws Exception
   {
      try(ByteArrayInputStream bis = new ByteArrayInputStream(iData.getBinary())) {

         PSPurgableTempFile f = new PSPurgableTempFile("img", iData.getExt(), null, iData.getFilename(),
                 iData.getMimeType(), null);

         try(FileOutputStream fos = new FileOutputStream(f)) {
            PSCopyStream.copyStream(bis, fos);
            return f;
         }
      }
   }

   protected void setCacheManager(ImageCacheManager cacheManager)
   {
      this.cacheManager = cacheManager;
   }
}

/*
 * Location: C:\decompile\image-widget-1.0.jar Qualified Name:
 * com.percussion.widgets.image.extensions.ImageAssetInputTranslation JD-Core
 * Version: 0.6.0
 */

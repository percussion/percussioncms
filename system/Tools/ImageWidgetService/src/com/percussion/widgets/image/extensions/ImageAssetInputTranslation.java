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

package com.percussion.widgets.image.extensions;

import com.percussion.error.PSExceptionUtils;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static com.percussion.cms.IPSConstants.FALSE;

public class ImageAssetInputTranslation extends PSDefaultExtension implements IPSItemInputTransformer
{
   private static final Logger log = LogManager.getLogger(ImageAssetInputTranslation.class);

   ImageCacheManager cacheManager = null;
   ImageResizeManager resizeManager = null;

   @Override
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
    * @throws PSAuthorizationException Authorization error
    * @throws PSRequestValidationException If the request is invalid
    * @throws PSParameterMismatchException If the parameters are incorrect
    */
   @Override
   public void preProcessRequest(Object[] params, IPSRequestContext request) throws PSAuthorizationException,
         PSRequestValidationException, PSParameterMismatchException, PSExtensionProcessingException
   {
      try
      {
         PSExtensionParams ep = new PSExtensionParams(params);
         String imageName = ep.getStringParam(0, "img", false);
         String thumbName = ep.getStringParam(1, "img2", false);
         String imageFileName  = request.getParameter(imageName+"_filename");
         String thumbFileName = request.getParameter(thumbName+"_filename");

         if(StringUtils.isBlank(thumbFileName)){
            thumbFileName = getThumbnailFileName(imageFileName);
         }

         if (StringUtils.isBlank(request.getParameter(thumbName + "_id")))
         {
            PSPurgableTempFile imageFile = (PSPurgableTempFile) request.getParameterObject(imageName);
            if (imageFile != null)
            {
               if(StringUtils.isEmpty(imageFile.getSourceFileName())){
                  imageFile.setSourceFileName(imageFileName);
               }
               if(StringUtils.isEmpty(imageFile.getSourceContentType())){
                  imageFile.setSourceContentType(request.getParameter(imageName + "_type"));
               }

               String mimeType = request.getParameter(imageName + "_type");
               updateRequest(request, imageName, generateImage(imageFile, mimeType));
               updateRequest(request, thumbName, generateThumbnail(imageFile));
            }
            else
            {
               log.debug("a value was not found for parameter {}", imageName);
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
         log.error("Unexpected Exception: {}" , PSExceptionUtils.getMessageForLog(ex));
         log.debug(ex);
         throw new PSExtensionProcessingException(getClass().getName(), ex);
      }
   }

   protected void processInputImage(IPSRequestContext request, String base) throws Exception
   {
      String dirty = request.getParameter(base + "_dirty");


      if (StringUtils.isBlank(dirty))
      {
         log.debug("image {} is not dirty", base);
         dirty = FALSE;
      }
      String imageKey = request.getParameter(base + "_id");
      if (StringUtils.isBlank(imageKey))
      {
         log.debug("Image key is blank");
         return;
      }
      ImageData iData = this.cacheManager.getImage(imageKey);
      if (iData == null)
      {
         log.info("Image not found in the cache for key {}" , imageKey);
         return;
      }
      updateRequest(request, base, iData);
   }

   private void updateRequest(IPSRequestContext request, String prefix, ImageData iData) throws IOException {
      PSPurgableTempFile temp = writeFile(iData);
      log.debug("updating file for {} {}" ,prefix , temp.getCanonicalPath());

      request.setParameter(prefix, temp);
      request.setParameter(prefix + "_ext", iData.getExt());
      request.setParameter(prefix + "_filename", iData.getFilename());
      request.setParameter(prefix + "_type", iData.getMimeType());
      request.setParameter(prefix + "_size", String.valueOf(iData.getSize()));
      request.setParameter(prefix + "_height", String.valueOf(iData.getHeight()));
      request.setParameter(prefix + "_width", String.valueOf(iData.getWidth()));
      request.setParameter(prefix + "_id", null);
   }

   private ImageData generateImage(PSPurgableTempFile imageFile, String mimeType) throws Exception {
      try(FileInputStream fin = new FileInputStream(imageFile)){
         String fileType = FilenameUtils.getExtension(imageFile.getSourceFileName());
         this.resizeManager.setExtension(fileType);
         this.resizeManager.setContentType(imageFile.getSourceContentType());
         this.resizeManager.setImageFormat(fileType);
         ImageData iData = this.resizeManager.generateImage(fin);
         iData.setFilename(imageFile.getSourceFileName());
         iData.setMimeType(mimeType);

         return iData;
      }
   }

   private String getThumbnailFileName(String imageFileName){

      return "thumb_" + imageFileName;
   }

   private ImageData generateThumbnail(PSPurgableTempFile imageFile) throws Exception
   {
      ImageData iData = null;
      int width;

      Properties serverProps = PSServer.getServerProps();
      String thumbWidthStr = serverProps.getProperty("imageThumbnailWidth", "50");
      int thumbWidth = Integer.parseInt(thumbWidthStr);
      int thumbHeight = thumbWidth;

      try(      FileInputStream fin = new FileInputStream(imageFile)){
         final byte[] imageByteArray = IOUtils.toByteArray(fin);
         BufferedImage image = ImageReader.read(imageByteArray);
         if (image != null)
         {
            width = image.getWidth();
            int height = image.getHeight();
            Rectangle rec = new Rectangle(0, 0, width, height);
            Dimension dim = new Dimension(thumbWidth, thumbHeight);

            String thumbnailFileName = getThumbnailFileName(imageFile.getSourceFileName());

            try(FileInputStream fin2 = new FileInputStream(imageFile)) {
               resizeManager.setFileName(thumbnailFileName);
               iData = this.resizeManager.generateImage(fin2, rec, dim);
               if(!StringUtils.isEmpty(thumbnailFileName)) {
                  iData.setFilename(thumbnailFileName);
               }
            }



         }
         return iData;
      }
   }

   protected PSPurgableTempFile writeFile(ImageData iData) throws IOException {
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


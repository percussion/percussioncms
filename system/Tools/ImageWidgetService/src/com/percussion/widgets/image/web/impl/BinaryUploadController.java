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

package com.percussion.widgets.image.web.impl;

import com.percussion.server.PSServer;
import com.percussion.util.PSBaseBean;
import com.percussion.widgets.image.data.CachedImageMetaData;
import com.percussion.widgets.image.data.ImageData;
import com.percussion.widgets.image.services.ImageCacheManager;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

@RestController
@PSBaseBean("imageWidgetBinaryUpload")
public class BinaryUploadController
{
   private static final Logger log = LogManager.getLogger(BinaryUploadController.class);

   @Autowired
   private ImageCacheManager cacheManager = null;

   private String modelObjectName="results";

   private String viewName = "imageWidgetJSONView";

   private static final String MESSAGE_BAD_CONTENT_TYPE = "Invalid or unsupported image type \"{0}\"";

   private static final String MESSAGE_UNABLE_TO_COMPUTE_SIZE = "Possibly invalid image. Unable to determine image height and width.";

   @RequestMapping("/imageWidget/upload")
   @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
   public ModelAndView handle(HttpServletRequest request,
         HttpServletResponse response) throws PSBinaryUploadException
   {

      ModelAndView modelAndView = new ModelAndView(getViewName());
      modelAndView.addAllObjects(RequestContextUtils.getInputFlashMap(request));

      JSONArray results;

      try
      {
         results = buildResults(request);
      }
      catch (Exception ex)
      {

         String emsg = "Unexpected Exception " + ex.getMessage();
         log.error(ex.getMessage());
         log.debug(ex);
         JSONObject error = new JSONObject();
         error.put("error", emsg);
         modelAndView.addObject(getModelObjectName(), error);
         return modelAndView;
      }
      modelAndView.addObject(getModelObjectName(), results);
      return modelAndView;
   }

   protected JSONArray buildResults(HttpServletRequest request)
         throws PSBinaryUploadException
   {
      JSONArray results = new JSONArray();
      if ((request instanceof MultipartHttpServletRequest))
      {
         try
         {
            log.debug("found multipart form");
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> fileMap = mRequest.getFileMap();
            for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet())
            {
               MultipartFile mpFile = entry.getValue();
               log.debug("processing file {}" , mpFile.getOriginalFilename());
               String mimeType = mpFile.getContentType();
               if ((StringUtils.isNotBlank(mimeType))
                     && (mimeType.toLowerCase().startsWith("image")))

               {
                  CachedImageMetaData cachedData = storeImage(mpFile);
                  JSON json = JSONSerializer.toJSON(cachedData);
                  results.add(json);
               }
               else
               {
                  String emsg = MessageFormat.format(
                          MESSAGE_BAD_CONTENT_TYPE,
                          mimeType);
                  throw new PSBinaryUploadException(emsg);
               }
            }
         }
         catch (Exception ex)
         {
            if (StringUtils.isNotBlank(ex.getMessage()))

            {
               String emsg = ex.getMessage();
               results.add(buildError(emsg));
            }
         }
      }
      return results;
   }

   protected JSON buildError(String message)

   {
      JSONObject json = new JSONObject();
      json.element("error", message);
      return json;
   }

   protected CachedImageMetaData storeImage(MultipartFile mpFile)
         throws PSBinaryUploadException

   {
      ImageData iData = new ImageData();
      try {
         iData.setBinary(mpFile.getBytes());
      } catch (IOException e) {
         throw new PSBinaryUploadException(e);
      }
      iData.setSize(mpFile.getSize());
      String filename = mpFile.getOriginalFilename();
      if (StringUtils.isNotBlank(filename))

      {
         iData.setFilename(filename);
         String ext = StringUtils.substringAfterLast(filename, ".");
         iData.setExt(ext);
      }
      iData.setMimeType(mpFile.getContentType());

      Properties serverProps = PSServer.getServerProps();
      String thumbWidth = serverProps.getProperty("imageThumbnailWidth", "50");
      BufferedImage image = null;
      try {
         image = ImageIO.read(mpFile.getInputStream());
      } catch (IOException e) {
         throw new PSBinaryUploadException(e);
      }
      if (image != null)

      {
         iData.setWidth(image.getWidth());
         iData.setHeight(image.getHeight());
         iData.setThumbWidth(Integer.parseInt(thumbWidth));
      }
      else
      {
         throw new PSBinaryUploadException(
               MESSAGE_UNABLE_TO_COMPUTE_SIZE);
      }
      String key = this.cacheManager.addImage(iData);
      log.debug("storing image for key {}" , key);
      return new CachedImageMetaData(iData, key);

   }

   public ImageCacheManager getCacheManager()

   {
      return this.cacheManager;
   }

   public void setCacheManager(ImageCacheManager cacheManager)

   {
      this.cacheManager = cacheManager;
   }

   public String getModelObjectName()

   {
      return this.modelObjectName;
   }

   public void setModelObjectName(String modelObjectName)

   {
      this.modelObjectName = modelObjectName;
   }

   public String getViewName() { return this.viewName; }

   public void setViewName(String viewName)
   {
      this.viewName = viewName;
   }

}

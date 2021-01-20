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

import com.percussion.server.PSServer;
import com.percussion.widgets.image.data.CachedImageMetaData;
import com.percussion.widgets.image.data.ImageData;
import com.percussion.widgets.image.services.ImageCacheManager;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.support.RequestContextUtils;

@RestController
@RequestMapping("/imageWidget/upload")
public class BinaryUploadController
{
   private static Log log = LogFactory.getLog(BinaryUploadController.class);

   private ImageCacheManager cacheManager = null;

   private String modelObjectName;

   private String viewName;

   private static final String MESSAGE_BAD_CONTENT_TYPE = "Invalid or unsupported image type \"{0}\"";

   private static final String MESSAGE_UNABLE_TO_COMPUTE_SIZE = "Possibly invalid image. Unable to determine image height and width.";

   @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
   protected ModelAndView handle(HttpServletRequest request,
         HttpServletResponse response) throws Exception
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

         String emsg = "Unexpected Exception " + ex.getLocalizedMessage();
         log.error(emsg, ex);
         JSONObject error = new JSONObject();
         error.put("error", emsg);
         modelAndView.addObject(getModelObjectName(), error);
         return modelAndView;
      }
      modelAndView.addObject(getModelObjectName(), results);
      return modelAndView;
   }

   protected JSONArray buildResults(HttpServletRequest request)
         throws Exception
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
               log.debug("processing file " + mpFile.getOriginalFilename());
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
                        "Invalid or unsupported image type \"{0}\"",
                        new Object[]
                        {mimeType});
                  throw new RuntimeException(emsg);
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
         throws Exception

   {
      ImageData iData = new ImageData();
      iData.setBinary(mpFile.getBytes());
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
      BufferedImage image = ImageIO.read(mpFile.getInputStream());
      if (image != null)

      {
         iData.setWidth(image.getWidth());
         iData.setHeight(image.getHeight());
         iData.setThumbWidth(Integer.parseInt(thumbWidth));
      }
      else
      {
         throw new RuntimeException(
               "Possibly invalid image. Unable to determine image height and width.");
      }
      String key = this.cacheManager.addImage(iData);
      log.debug("storing image for key " + key);
      CachedImageMetaData cData = new CachedImageMetaData(iData, key);

      return cData;
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

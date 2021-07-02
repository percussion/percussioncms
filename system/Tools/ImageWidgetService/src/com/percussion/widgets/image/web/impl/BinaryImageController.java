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
      
      import com.percussion.widgets.image.data.ImageData;
      import com.percussion.widgets.image.services.ImageCacheManager;
      import com.percussion.widgets.image.services.ImageCacheManagerLocator;
      import javax.servlet.ServletOutputStream;
      import javax.servlet.http.HttpServletRequest;
      import javax.servlet.http.HttpServletResponse;
      import org.apache.commons.lang.StringUtils;
      import org.apache.logging.log4j.Logger;
      import org.apache.logging.log4j.LogManager;
      import org.springframework.web.servlet.ModelAndView;
      import org.springframework.web.servlet.mvc.AbstractController;
      import org.springframework.web.servlet.mvc.Controller;
      
      public class BinaryImageController extends AbstractController
        implements Controller
      {
      private static final Logger log = LogManager.getLogger(BinaryImageController.class);
      
      private ImageCacheManager cacheManager = null;
      
        private void initServices()
        {
        if (this.cacheManager == null)
          {
          this.cacheManager = ImageCacheManagerLocator.getImageCacheManager();
          }
        }
      
        protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
          throws Exception
        {
        initServices();
        String uri = request.getRequestURI();
        log.debug("uri is " + uri);
        String imageKey = extractKey(uri);
        if (StringUtils.isBlank(imageKey))
          {
          String emsg = "Image Key was null";
          log.error(emsg);
          response.sendError(400, emsg);
          return null;
          }
        log.debug("Image key is " + imageKey);
        if (!this.cacheManager.hasImage(imageKey))
          {
          String emsg = "The image was not found";
          log.info(emsg);
          response.sendError(404, emsg);
          return null;
          }
        ImageData data = this.cacheManager.getImage(imageKey);
      
        response.setContentType(data.getMimeType());
        int sz = Long.valueOf(data.getSize()).intValue();
        if (sz > 0)
          {
          log.debug("image size is " + sz);
          response.setContentLength(sz);
          response.setStatus(200);
          try(ServletOutputStream ostream = response.getOutputStream()) {
              ostream.write(data.getBinary());
              ostream.flush();
              response.flushBuffer();
              return null;
          }
          }
       String emsg = "Image is empty";
       log.info(emsg);
       response.sendError(204);
       response.flushBuffer();
       return null;
        }
      
        protected String extractKey(String url)
        {
       if (StringUtils.isBlank(url))
          {
         String emsg = "image URL must not be blank";
         log.error(emsg);
         throw new IllegalArgumentException(emsg);
          }
       String lastPart = StringUtils.substringAfterLast(url, "/");
       lastPart = StringUtils.substringBefore(lastPart, ".");
       String key = StringUtils.substringAfter(lastPart, "img");
            return key;
        }
      
        public void setCacheManager(ImageCacheManager cacheMgr)
        {
        	this.cacheManager = cacheMgr;
        }
      }

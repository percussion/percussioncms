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
              implements Controller {
          private static final Logger log = LogManager.getLogger(BinaryImageController.class);

          private ImageCacheManager cacheManager = null;

          private void initServices() {
              if (this.cacheManager == null) {
                  this.cacheManager = ImageCacheManagerLocator.getImageCacheManager();
              }
          }

          protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
                  throws Exception {
              initServices();
              String uri = request.getRequestURI();
              log.debug("uri is {}", uri);
              String imageKey = extractKey(uri);
              if (StringUtils.isBlank(imageKey)) {
                  String emsg = "Image Key was null";
                  log.error(emsg);
                  response.sendError(400, emsg);
                  return null;
              }
              log.debug("Image key is {}", imageKey);
              if (!this.cacheManager.hasImage(imageKey)) {
                  String emsg = "The image was not found";
                  log.info(emsg);
                  response.sendError(404, emsg);
                  return null;
              }
              ImageData data = this.cacheManager.getImage(imageKey);

              response.setContentType(data.getMimeType());
              int sz = Long.valueOf(data.getSize()).intValue();
              if (sz > 0) {
                  log.debug("image size is {}", sz);
                  response.setContentLength(sz);
                  response.setStatus(200);
                  try (ServletOutputStream ostream = response.getOutputStream()) {
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

          protected String extractKey(String url) {
              if (StringUtils.isBlank(url)) {
                  String emsg = "image URL must not be blank";
                  log.error(emsg);
                  throw new IllegalArgumentException(emsg);
              }
              String lastPart = StringUtils.substringAfterLast(url, "/");
              lastPart = StringUtils.substringBefore(lastPart, ".");
              String key = StringUtils.substringAfter(lastPart, "img");
              return key;
          }

          public void setCacheManager(ImageCacheManager cacheMgr) {
              this.cacheManager = cacheMgr;
          }
      }

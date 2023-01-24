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
package com.percussion.services.aaclient;

import com.percussion.services.filestorage.IPSFileMeta;
import com.percussion.services.filestorage.IPSFileStorageService;
import com.percussion.services.filestorage.PSFileStorageServiceLocator;
import org.apache.commons.io.IOUtils;
import org.apache.tika.metadata.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * Previews a Hashed Image.
 * This class is essential a servlet whos endpoint is
 * <code>/Rhythmyx/assembly/aa?widget=hi&hash=[HASH_HERE]</code>
 * <b>HASH_HERE</b> is the hash of the image from the 
 * {@link IPSFileStorageService}.
 * @see IPSFileStorageService
 * @author adamgent
 *
 */
public class PSHashedFileWidgetHandler implements IPSWidgetHandler
{

   public void handleRequest(
         HttpServletRequest request,
         HttpServletResponse response) throws Exception
   {
      IPSFileStorageService s = PSFileStorageServiceLocator.getFileStorageService();
      String hash = request.getParameter("hash");
      notEmpty(hash, "Hash");
      if (! s.fileExists(hash) ) {
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         response.flushBuffer();
         return;
      }
      IPSFileMeta m = s.getMeta(hash);
      notNull(m, "Meta not found");
      String contentType = m.get(HttpHeaders.CONTENT_TYPE);
      if (isNotBlank(contentType))
         response.setContentType(contentType);
      OutputStream os = response.getOutputStream();
      InputStream is = s.getStream(hash);
      notNull(is, "Inputstream not found for hash: " + hash);
      IOUtils.copy(is, os);
      response.flushBuffer();
   }

}

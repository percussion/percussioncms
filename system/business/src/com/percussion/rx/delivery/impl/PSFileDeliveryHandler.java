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
package com.percussion.rx.delivery.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.delivery.data.PSDeliveryResult;
import com.percussion.rx.publisher.PSPublisherUtils;
import com.percussion.server.PSServer;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

/**
 * This handler delivers content to the file system.
 * <p>
 * For un-publishing requests, the cached information is simply the location to
 * be removed, and the saved temp file is <code>null</code>, which indicates
 * a removal.
 * <p>
 * Subclasses can override the <code>doDelivery</code> and
 * <code>doRemoval</code> methods.
 * 
 * @author dougrand
 */
public class PSFileDeliveryHandler extends PSBaseDeliveryHandler
{
   //Default to 1mb
   private static final int COPY_BUFFER_SIZE = Integer.parseInt(PSServer.getProperty(IPSConstants.SERVER_PROP_FILE_COPY_BUFFER_SIZE,"10448576"));
   /**
    * Logger.
    */
   @SuppressWarnings("hiding")
    private static final Logger ms_log = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);

   /**
    * Remove the single item specified by location. This method can be
    * overridden in a subclass.
    * 
    * @param jobId
    * @param item
    * @param location the location, never <code>null</code> or empty.
    * @return the result of the removal operation
    */
   @Override
   protected IPSDeliveryResult doRemoval(Item item, long jobId, String location)
   {
      File fileToDelete = new File(location);
      if (fileToDelete.delete() || !fileToDelete.exists())
      {
         return getItemResult(Outcome.DELIVERED, item, jobId, null);
      }
      else
      {
         String message = COULD_NOT_DELETE + fileToDelete.getAbsolutePath();
         return getItemResult(Outcome.FAILED, item, jobId, message);
      }
   }

   @Override
   protected void removeEmptyDirectory(String dir)
   {
      File f = new File(dir);
      if ( !f.exists())
         return;
      
      if (f.list() != null && Objects.requireNonNull(f.list()).length > 0)
         return;
      
      try
      {
         Files.delete(f.toPath());
      }
      catch (Exception e)
      {
         ms_log.warn("Failed to delete directory: {} Error: {}" ,
                 f.getAbsolutePath(), PSExceptionUtils.getMessageForLog(e));
      }
   }
   

   /**
    * Resolves any relative paths to a valid path based on the root directory
    * of the containing web application, which is "../rxapp.ear" not 
    * "../rxapp.ear/rxapp.war".
    *   
    * @param path The path to resolve, may be <code>null</code> or empty.
    * 
    * @return The resolved path, or the supplied path if it was 
    * <code>null</code> or empty.
    */
   public static String resolveFilePath(String path)
   {
      return PSPublisherUtils.resolveFilePath(path);
   }
   
   @Override
   protected IPSDeliveryResult doDelivery(Item item, long jobId,
         String location)
         throws PSDeliveryException
   {
      if (StringUtils.isBlank(location))
      {
         throw new IllegalArgumentException(
               "location may not be null or empty");
      }

      String destPath = resolveFilePath(location); 
      File destination = new File(destPath);
      File directory = destination.getParentFile();

      PSDeliveryException de = null;
      try
      {
         directory.mkdirs();
         // Ensure the directory exists
         if (!directory.exists() || (!directory.isDirectory()))
         {
            de = new PSDeliveryException(IPSDeliveryErrors.DIR_CANT_CREATE,
                  directory.getAbsolutePath());
            return getItemResult(Outcome.FAILED, item, jobId,
                  de.getLocalizedMessage());
         }

         if (item.getFile() != null) {
            try (InputStream is = new FileInputStream(item.getFile())) {
               try(FileOutputStream os = new FileOutputStream(destination)) {
                  if (needsTextEncoding(item.getMimeType())) {
                     try (Writer w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                        try (Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                           IOUtils.copy(reader,w);
                        }
                     }
                  } else {
                     IOUtils.copy(is, os, COPY_BUFFER_SIZE);
                  }
               }
            }
         }else{
            try(InputStream is = item.getResultStream()){
               try(FileOutputStream os = new FileOutputStream(destination)) {
                  if(needsTextEncoding(item.getMimeType())){
                     try(Reader reader = new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))){
                        try(Writer writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))){
                           IOUtils.copy(reader,writer);
                        }
                     }
                  }else {
                     // Copy temp file to permanent location.
                     IOUtils.copy(is, os, COPY_BUFFER_SIZE);
                  }
               }
            }
         }
      }
      catch (SecurityException e)
      {
         de = new PSDeliveryException(
               IPSDeliveryErrors.CREATE_DIR_W_EXCEPTION, e, directory
                     .getAbsolutePath(), (StringUtils.isBlank(e
                     .getLocalizedMessage()) ? e.getClass().getName() : e
                     .getLocalizedMessage()));
      }
      catch (Exception e)
      {
         de = new PSDeliveryException(IPSDeliveryErrors.COPY_FILE_FAILED, e,
               item.getFile().getAbsolutePath(),
               destination.getAbsolutePath(), (StringUtils.isBlank(e
                     .getLocalizedMessage()) ? e.getClass().getName() : e
                     .getLocalizedMessage()));
      }
      if (de != null)
      {
         return getItemResult(Outcome.FAILED, item, jobId, de
               .getLocalizedMessage());
      }

      return new PSDeliveryResult(Outcome.DELIVERED, null, item.getId(),
               jobId, item.getReferenceId(), destPath.getBytes(StandardCharsets.UTF_8));

   }

   private boolean needsTextEncoding(String mimeType) {
      return mimeType.toLowerCase().contains("html") || mimeType.toLowerCase().contains("xml");
   }

   @Override
   public boolean checkConnection(IPSPubServer pubServer, IPSSite site)
   {
      if(pubServer!=null) {
         return super.checkConnection(pubServer, site);
      }else{
         return true;
      }
   }

   /**
    * @deprecated Use IOUtils instead
    * @param input in
    * @param output out
    * @return byte count
    * @throws IOException exception
    */
   @Deprecated()
   private static long copy(InputStream input, OutputStream output)
           throws IOException {
        byte[] buffer = new byte[COPY_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
   
}

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
package com.percussion.rx.delivery.impl;

import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.delivery.data.PSDeliveryResult;
import com.percussion.rx.publisher.PSPublisherUtils;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
   private static final int COPY_BUFFER_SIZE = 1024 * 1024; // 1Mb
   /**
    * Logger.
    */
   @SuppressWarnings("hiding")
    private static final Logger ms_log = LogManager.getLogger(PSFileDeliveryHandler.class);

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
      
      if (f.list().length > 0)
         return;
      
      try
      {
         f.delete();
      }
      catch (Exception e)
      {
         ms_log.warn("Failed to delete directory: " + f.getAbsolutePath(), e);
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
                  // Copy temp file to permanent location.
                  // a small buffer size splits the smb data sent when saving to a share.  Default for ioutils is 4096
                  // Analysis showed that windows can send a 1M smb block so we will use that size
                  copy(is, os);
               }
            }
         }else{
            try(InputStream is = item.getResultStream()){
               try(FileOutputStream os = new FileOutputStream(destination)) {
                  // Copy temp file to permanent location.
                  // a small buffer size splits the smb data sent when saving to a share.  Default for ioutils is 4096
                  // Analysis showed that windows can send a 1M smb block so we will use that size
                  copy(is, os);
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

      try
      {
         return new PSDeliveryResult(Outcome.DELIVERED, null, item.getId(),
               jobId, item.getReferenceId(), destPath.getBytes("UTF8"));
      }
      catch (UnsupportedEncodingException e)
      {
         ms_log.error("Problem delivering item", e);
         return new PSDeliveryResult(Outcome.FAILED, e.getLocalizedMessage(),
               item.getId(), jobId, item.getReferenceId(), null);
      }
   }

   @Override
   public boolean checkConnection(IPSPubServer pubServer, IPSSite site)
   {
      return super.checkConnection(pubServer, site);
   }
   
   
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

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
package com.percussion.util;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Image utility methods.
 */
public class PSImageTools
{
   /**
    * Fetches image information given a cas url.
    * 
    * @param request request context, never <code>null</code>.
    * @param casUrl url to image assembly, never <code>null</code> or
    *  <code>empty</code>.
    *  
    * @return image info object, never <code>null</code>.
    * @throws IOException on any file IO error.
    * @throws PSInternalRequestCallException on any internal request error.
    */
   public static BufferedImage getImageInformation(IPSRequestContext request, 
         String casUrl) throws IOException, PSInternalRequestCallException 
   {
      if (request==null)
         throw new IllegalArgumentException("request may not be null");
      
      if (casUrl==null || casUrl.trim().length() < 1)
         throw new IllegalArgumentException("casUrl may not be null or empty");
      
      /*
       * Make an Internal request with the urlstring.
       */
      IPSInternalRequest imageReq = request.getInternalRequest(
         casUrl, null, false);
      
      ByteArrayInputStream bis = null;
      FileOutputStream fos = null;
      byte[] content = null;
      try
      {
         content = imageReq.getContent();
      }
      catch (PSInternalRequestCallException e)
      {
         request.printTraceMessage(
            "Internal request to " + casUrl + " resulted in " +
            "PSInternalRequestCallException.\n" +
            "\nException : " + e.getMessage() +
            "\nFailed to get image height and width attriutes.");
         
         throw e;
      }
      
      bis = new ByteArrayInputStream(content);
      PSPurgableTempFile tmpFile = null;
      String filename = null;
      try
      {
         tmpFile = new PSPurgableTempFile("pstmpimage", ".jpg", null);
      }
      catch (IOException e)
      {
         request.printTraceMessage(
            "Creation of PSPurgableTempFile " + 
            " resulted in IOException exception.\n" +
            "\nException : " + e.getMessage() +
            "\nFailed to get image height and width attriutes.");
         
         throw e;
      }
      
      try
      {
         fos = new FileOutputStream(tmpFile);
      }
      catch (FileNotFoundException e)
      {
         //This should not happen as we just created the file
         request.printTraceMessage(
            "Conversion of PSPurgableTempFile to FileOutputStream" + 
            " resulted in FileNotFoundException exception.\n" +
            "\nException : " + e.getMessage() +
            "\nFailed to get image height and width attriutes.");
         
         throw e;
      }
      try
      {
         IOTools.copyStream(bis,fos);
      }
      catch (IOException e)
      {
         //This should not happen as we just created the stream.
         request.printTraceMessage(
            "Copy of  FileOutputStream to ByteArrayInputStream " +
            " resulted in IOException exception.\n" +
            "\nException : " + e.getMessage() +
            "\nFailed to get image height and width attriutes.");
         
         throw e;
      }
      
      
      return ImageIO.read(tmpFile);
   }
   

}

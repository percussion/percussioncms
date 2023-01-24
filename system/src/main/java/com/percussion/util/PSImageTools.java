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

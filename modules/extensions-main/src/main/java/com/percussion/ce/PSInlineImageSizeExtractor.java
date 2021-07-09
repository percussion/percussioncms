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
package com.percussion.ce;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSImageTools;

import java.awt.image.BufferedImage;
import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The purpose of this exit is to add image height and width as attributes
 * to the root of result document. This exit expects urlstring and inlinetype
 * attributes on the root element of the result document. 
 * If the value of inlinetype attribute is not image, no modification takes 
 * place, the result document will be returned as is.
 * If the urlstring produces a binary image then its height and width
 * will be added as attributes to the root element of the result document
 * otherwise no modification takes place.
 */
public class PSInlineImageSizeExtractor implements IPSResultDocumentProcessor
{
   /*
    * Implementation of the method required by the interface IPSExtension.
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resDoc)
         throws PSParameterMismatchException,
               PSExtensionProcessingException
   {
      
      Element root = resDoc.getDocumentElement(); 
      String urlstring = root.getAttribute("urlstring");
      String inlinetype = root.getAttribute("inlinetype");
      if(urlstring == null || urlstring.trim().length() < 1 ||
         inlinetype == null || !inlinetype.equalsIgnoreCase("rximage"))
      {
         request.printTraceMessage(
            "Insufficient data for adding the image sizes. " + 
            "\nEither urlstring is null or empty or inlinetype is null or" +
            "not equal to image.\n urlstring: " + urlstring +
            "\n inlinetype: " + inlinetype + " Skipped adding " +
            "image height and width attriutes.");
         return resDoc;
      }
          
      BufferedImage iinfo = null;
      try
      {
         iinfo = PSImageTools.getImageInformation(request, urlstring);
      }
      catch (Exception e)
      {
         request.printTraceMessage(
               "Unable to create an image from the supplied URL." +
               "\nSkipped adding image height and width attributes.");
         return resDoc;
      }
      
      if(iinfo!=null)
      {
         /*
          * We are all set to add the image height and width attributes.
          */
         resDoc.getDocumentElement().setAttribute(
            "height",iinfo.getHeight()+"");
         resDoc.getDocumentElement().setAttribute(
            "width",iinfo.getWidth()+"");
      }
      
      return resDoc;
   }
   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";

}

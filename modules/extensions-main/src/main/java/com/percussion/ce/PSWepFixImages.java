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

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extensions.general.PSFileInfo;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSImageTools;
import com.percussion.xml.PSNodePrinter;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is a Rhythmyx pre-exit which examines incoming Xml,
 * gets HTML content from 'wepbodyhtml' parameter; Parses it, then
 * looks for all the 'img' tags and for each of those first checks
 * whether height and width attributes are already set; if those are
 * not set then it grabs the actual image, calculates its dimentions
 * and sets width, height, rxwidth and rxheight on the img tag.
 */
public class PSWepFixImages extends PSFileInfo
   implements IPSRequestPreProcessor
{

   /**
    * Pre processes the request.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws
         PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException
   {
      //first do the normal file info processing
      super.preProcessRequest(params, request);

      String param = request.getParameter("wepbodyhtml");

      if (param==null || param.trim().length() < 1)
         return;

      Document doc = null;

      try
      {
         doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(
               param), false);

         NodeList nl = doc.getElementsByTagName("img");

         int len = nl.getLength();

         if (len < 1)
            return; //no images - nothing to do

         boolean isImgFixed = false;
         for (int i = 0; i < len; i++)
         {
            Node node = nl.item(i);

            if (!(node instanceof Element))
               continue;

            Element elImg = (Element)node;

            String height = elImg.getAttribute(IPSHtmlParameters.ATTR_HEIGHT);
            String width = elImg.getAttribute(IPSHtmlParameters.ATTR_WIDTH);

            if (height.trim().length() > 0 && width.trim().length() > 0)
               continue; //don't touch this one, user altered width and height

            isImgFixed = true;
            String src = elImg.getAttribute(IMAGE_SRC);
            BufferedImage iinfo = PSImageTools.getImageInformation(request, src);

            height = "" + iinfo.getHeight();
            width = "" + iinfo.getWidth();

            elImg.setAttribute(IPSHtmlParameters.ATTR_HEIGHT, height);
            elImg.setAttribute(IPSHtmlParameters.ATTR_RX_HEIGHT, height);
            elImg.setAttribute(IPSHtmlParameters.ATTR_WIDTH, width);
            elImg.setAttribute(IPSHtmlParameters.ATTR_RX_WIDTH, width);
         }

         if (!isImgFixed)
            return; //we didn't do anything - no need to serialize, just return

         StringWriter swriter = new StringWriter();
         PSNodePrinter np = new PSNodePrinter(swriter);
         np.printNode(doc);
         String result = swriter.toString();

         request.setParameter("wepbodyhtml", result);
      }
      catch (Exception e) {
         throw new PSExtensionProcessingException("PSWepFixImages", e);
      }

   }

    /**
    * Image Source.
    */
   public static final String IMAGE_SRC = "src";
}

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

package com.percussion.filetracker;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;

import java.io.File;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class PSUploadAppendFileAttrs implements IPSResultDocumentProcessor
{
   public PSUploadAppendFileAttrs()
   {
   }

   /*
   * Required by the interface IPSResultDocumentProcessor
   */
   public boolean canModifyStyleSheet()
   {
      return true;
   }

   /*
   * Required by the interface IPSResultDocumentProcessor
   */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
   }

   /*
   * Required by the interface <code>IPSResultDocumentProcessor</code>. Actual
   * processing happens in this method.
   */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if(params.length < 1)
      {
         throw new PSExtensionProcessingException(0, new String(
            "parameter list cannot be empty in PSUploadAppendFileAttrs"));
      }
      String fileSizeParam = null;
      if(null != params[0])
         fileSizeParam = params[0].toString();

      if(null == fileSizeParam || fileSizeParam.trim().length() < 1)
      {
         throw new PSExtensionProcessingException(0, new String(
            "File size param cannot be empty in PSUploadAppendFileAttrs"));
      }

      fileSizeParam = fileSizeParam.trim();

      String dateParam = null;
      if(params.length > 1 && null != params[1])
         dateParam = params[1].toString();

      HashMap paramMap = request.getParameters();
      if(null == paramMap)
         return resDoc;

      String paramValue = "";
      if(paramMap.containsKey(fileSizeParam))
      {
         paramValue = paramMap.get(fileSizeParam).toString();
         if(paramValue.length() > 0)
         {
            Element elem = resDoc.createElement(ELEM_SIZE);
            elem = (Element)resDoc.getDocumentElement().appendChild(elem);
            Text text = resDoc.createTextNode(paramValue);
            elem.appendChild(text);
         }
      }

      paramValue = "";
      if(paramMap.containsKey(dateParam))
      {
         paramValue = paramMap.get(dateParam).toString();
         if(paramValue.length() > 0)
         {
            Element elem = resDoc.createElement(ELEM_MODIFIED);
            elem = (Element)resDoc.getDocumentElement().appendChild(elem);
            Text text = resDoc.createTextNode(paramValue);
            elem.appendChild(text);
         }
      }

      return resDoc;
   }

   /**
   * XML element name holding the file size
   */
   static final String ELEM_SIZE = "size";

   /**
   * XML element name holding the modified date
   */
   static final String ELEM_MODIFIED = "modified";

}

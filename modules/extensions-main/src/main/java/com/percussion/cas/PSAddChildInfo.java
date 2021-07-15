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
package com.percussion.cas;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Queries the specified URL and appends the content of the returned doc to the
 * current doc (as child of the root element).
 * This exit can be used in displaying the contents of complex child tables in
 * an assembler without manual SQL and outer joins.
 */
public class PSAddChildInfo extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{

   /**
    * Queries the URL specified by required paramater <code>params[0]</code>
    * (the "resource" parameter for this exit in the Workbench). If the root
    * element of the returned Xml Document has child nodes, then appends these
    * child nodes as child of the root element of <code>resultDoc</code>.
    *
    * @param params param[0] is a required parameter. It must be the URL of the
    * resource to obtain the child table data doc. This URL is relative to the
    * Rhythmyx root and is of the form: <i>RhythmyxApplication/Resource</i>.
    * This parameter is specified by the "resource" parameter for this exit
    * in the Workbench.
    *
    * @param request the request context for this request, never
    * <code>null</code>
    *
    * @param resultDoc the XML document resulting from the Rhythmyx server
    * operation. The contents of the XML document returned by querying the
    * specified resource will be inserted into this document.
    *
    * @throws PSExtensionProcessingException if a handler for the specified
    * resource cannot be located, or an error occurs querying this resource
    *
    * @exception PSParameterMismatchException if <code>params</code> is
    * <code>null</code> or empty or if params[0] is <code>null</code> or empty
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      request.printTraceMessage(ms_className + "#processResultDocument()");

      if (resultDoc == null)
      {
         request.printTraceMessage(ms_className + "#resultDoc is null");
         return resultDoc;
      }

      Element resultDocRoot = resultDoc.getDocumentElement();
      if (resultDocRoot == null)
      {
         request.printTraceMessage(ms_className + "#resultDoc root is null");
         return resultDoc;
      }

      String childResource = null;
      if ((params != null) && (params.length >= 1)
         && (params[0] != null) && (params[0].toString().trim().length() > 0))
      {
         childResource = params[0].toString().trim();
      }
      else
      {
         String msg = ms_className +
            ": The exit param \"childResource\" must not be null or empty";
         request.printTraceMessage(msg);
         throw new PSParameterMismatchException(msg);
      }

      try
      {
         IPSInternalRequest iReq = request.getInternalRequest(childResource);
         if (iReq == null)
         {
            String msg = ms_className +
               ": Unable to locate handler for request: " + childResource;
            request.printTraceMessage(msg);
            throw new PSExtensionProcessingException(0, msg);
         }

         Document childDoc = null;
         childDoc = iReq.getResultDoc();

         if (childDoc != null)
         {
            PSXmlTreeWalker childWalker = new PSXmlTreeWalker(childDoc);
            Element childElement = childWalker.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
            while (childElement != null)
            {
               PSXmlDocumentBuilder.copyTree(
                  resultDoc, resultDocRoot, childElement);
               childElement = childWalker.getNextElement(
                  PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
            }
         }
      }
      catch (PSInternalRequestCallException ex)
      {
         request.printTraceMessage(
            ms_className + ": " + ex.getLocalizedMessage());
         throw new PSExtensionProcessingException(0, ex);
      }
      return resultDoc;
   }

   /**
    * This exit will never modify the stylesheet
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * The function name used for error handling
    */
   private static final String ms_className = "PSAddChildInfo";
}




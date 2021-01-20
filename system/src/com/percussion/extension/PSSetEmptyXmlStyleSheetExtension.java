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

package com.percussion.extension;

import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSApplicationHandler;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;


/**
 * The PSSetEmptyXmlStyleSheetExtension class implements extension handling
 * for the setEmptyXmlStyleSheet simple action extension. This exit associates
 * a style sheet with an empty XML document when there is no root node 
 * in the XML document. This is used primarily to return a static page
 * when no data is found for the request.
 * The following parameter is defined for this exit:
 * <table border="1">
 * <tr><th>Parameter</th><th>Description</th></tr>
 * <tr><td>StyleSheet</td>
 *     <td>(required) the URL of the style sheet</td></tr>
 * </table>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSetEmptyXmlStyleSheetExtension
   implements IPSResultDocumentProcessor
{
   /* *************  IPSExtension Interface Implementation ************* */

   /**
    * No-op
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {}


   /* *******  IPSResultDocumentProcessor Interface Implementation ******* */

   /**
    * Return true, this extension can modify the style sheet.
    */
   public boolean canModifyStyleSheet()
   {
      return true;
   }

   /**
    * If no rows were returned on a query, a result document will be created to
    * display the empty result. A supplied XML style sheet will be used as a
    * stylesheet for the result document.
    *
    * @param      params      the parameters defined by this extension; params
    * is an array of 1 Objects, a toString() is called to convert the object to a
    * String representation.
    * <UL>
    * <LI>stylesheet path: (required) The URL of the style sheet
    * </UL>
    *
    * @param      rc          the context of the request associated with this extension
    *
    * @param      resultDoc   the result XML document. May be <CODE>null</CODE>.
    *
    * @return                     If resultDoc is not <CODE>null</CODE>,
    * it will be returned. Otherwise, a new document is
    * created using the supplied stylesheet.
    *
    * @exception  PSParameterMismatchException  if the parameter number is incorrect
    * @exception  PSExtensionProcessingException      if the first parameter is <code>null</code>
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext rc, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if ((resultDoc != null) && (resultDoc.getDocumentElement() != null))
      {   // if this has a root node, there's nothing more to do
         // as it's not an empty document
         return resultDoc;
      }

      int size = (params == null) ? 0 : params.length;
      if (size != 1){ // one parameter is required
         throw new PSParameterMismatchException(size, 1);
      }

      if (params[0] == null){
         String msg = "params[0] must not be null to call processResultDocument";
         IllegalArgumentException ex = new IllegalArgumentException(msg);
         throw new PSExtensionProcessingException( getClass().getName(), ex);
      }

      if (resultDoc == null)
         resultDoc = PSXmlDocumentBuilder.createXmlDocument();

      try {
         String styleSheet = params[0].toString();
         if (styleSheet == null)
         {
            throw new PSExtensionProcessingException( getClass().getName(),
               new NullPointerException());
         }

         try {
            java.net.URL url = new java.net.URL(styleSheet);
            String urlText = null;
            String ssType;

            if (rc.getRequestPageType() == IPSRequestContext.PAGE_TYPE_HTML)
               urlText = PSApplicationHandler.getLocalizedURL(
                  rc.getCurrentApplicationName(), url).toExternalForm();
            else
               urlText = PSApplicationHandler.getExternalURLString(rc.getCurrentApplicationName(), rc.getRequestRoot(), url);

            int extPos = urlText.lastIndexOf('.');
            if (extPos == -1)
               ssType = "xsl";   // assume it's XSL by default
            else
               ssType = urlText.substring(extPos+1).toLowerCase();

            ProcessingInstruction pi = resultDoc.createProcessingInstruction(
               "XML-stylesheet",
               ("type=\"text/" + ssType + "\" href=\"" + urlText + "\""));
            Element root = (resultDoc == null) ? null : resultDoc.getDocumentElement();
            if (root != null)
               resultDoc.insertBefore(pi, root);
            else
               resultDoc.appendChild(pi);

            return resultDoc;
         } catch (java.net.MalformedURLException e) {
            throw new PSExtensionProcessingException( getClass().getName(), e);
         }
      } catch (Exception e) {
         throw new PSExtensionProcessingException( getClass().getName(), e);
      }
   }
}


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
package com.percussion.data;

import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;
import com.percussion.util.PSBaseHttpUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Properties;

import org.w3c.dom.Document;

/**
 * The PSResultSetHtmlConverter class extends the PSResultSetXmlConverter
 * class, providing conversion to an HTML page. It extends the XML converter
 * as the process of HTML conversion first converts to an XML document
 * and then runs the document through its style sheet to generate HTML.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSResultSetHtmlConverter extends PSResultSetXmlConverter {
   /**
    * Construct a ResultSet to HTML converter.
    *
    * @param      app   the application containing the data set
    *
    * @param      def   the data set definition
    *
    * @exception   PSIllegalArgumentException
    *                                                if request link generation is
    *                                                desired but the target data set
    *                                                cannot be found
    */
   public PSResultSetHtmlConverter(PSApplicationHandler app, PSDataSet def)
      throws PSNotFoundException,
         PSIllegalArgumentException,
         com.percussion.design.catalog.PSCatalogException,
         PSExtensionException
   {
      super(app, def);
   }


   /* ********** IPSResultSetConverter Interface Implementation ********** */

   /**
    * See {@link IPSResultSetConverter#convert(PSExecutionData,
    * IPSResultSetDataFilter) base class} for full details. More specifically,
    * this class performs the following steps during conversion:
    * <ol>
    * <li>verify reqUrl is supported</li>
    * <li>run the conditionals to determine which style sheet is in use.
    * If no conditions are met, use our default style sheet.</li>
    * <li>let our super-class create an XML document for us as this is
    * a required first step in building HTML.</li>
    * <li>initialize the output with the appropriate header info
    * (content type, etc.)</li>
    * <li>if the style sheet is XSL, run it through the XSL processor
    * for conversion to HTML (assuming the XSL is building HTML)</li>
    * <li>if the style sheet is of any other type (eg, CSS), set it as the
    * style sheet for the XML data and return it (we do no processing
    * on CSS or other such types)</li>
    * </ol>
    */
   public void convert(PSExecutionData data, IPSResultSetDataFilter filter)
      throws PSConversionException, PSUnsupportedConversionException
   {
      /* if there's more than one result set on the stack, we're
       * in trouble!!! we must have missed a join.
       */
      java.util.Stack stack = data.getResultSetStack();
      if (stack.size() > 1)
         throw new PSConversionException(
            IPSDataErrors.CANNOT_CONVERT_MULTIPLE_RESULT_SETS,
            new Integer(stack.size()));
      else if (stack.size() == 0)
         throw new PSConversionException(
            IPSDataErrors.NO_DATA_FOR_CONVERSION);

      PSRequest request = data.getRequest();

      boolean callSuper = false;

      String extension = request.getRequestPageExtension().toLowerCase();
      if (m_requestor.getMimeType(extension) == null)
      {
         if (extension.charAt(0) != '.')
            extension = "." + extension;

         if (extension.equals(".xml") || extension.equals(".txt"))
         {
            /* Need to also check the request pages!!! */
            PSCollection pages = m_resultPageSet.getResultPages();
            if (pages != null)
            {
               boolean found = false;
               for (int i = 0; i < pages.size(); i++)
               {
                  /* Check to see if the page explicitly uses the extension */
                  PSResultPage page = (PSResultPage) pages.get(i);
                  Collection c = page.getExtensions();
                  if ((c != null) && (!c.isEmpty()))
                  {
                     if (c.contains(extension))
                        found = true;
                  }
               }

               /* If we didn't find the extension in any of the pages
                  then we must call the super */
               if (!found)
                  callSuper = true;
            } else
            {
               callSuper = true;
            }
         }
      }

      if (callSuper)
      {
         if (super.isSupported(request.getRequestFileURL())) {
            super.convert(data, filter);   /* let the base converter do this */
            return;
         }
         else {
            /* There is nobody to handle this extension! */
            String pageExt = request.getRequestPageExtension();
            if (pageExt == null)
               pageExt = "";
            throw new PSUnsupportedConversionException(
               IPSDataErrors.HTML_CONV_EXT_NOT_SUPPORTED, pageExt);
         }
      }

      /* let our super-class create an XML document for us as this is
       * a required first step in building HTML.
       */
      Document doc = createXmlDocument(data, filter, false);

      String contentHeader = request.getContentHeaderOverride();
      String mimeType = null;
      int pageIndex = getResultPageIndex(data);

      if (contentHeader == null)
      {
         mimeType = getMimeTypeForRequestPage(pageIndex, data);
         if (mimeType == null)
         {
            mimeType = PSResultSetXmlConverter.getMimeTypeForRequestor
                  (m_requestor, extension, data);
         }

         /* verify reqUrl is supported */
         if (mimeType == null) {
            String pageExt = request.getRequestPageExtension();
            if (pageExt == null)
               pageExt = "";
            throw new PSUnsupportedConversionException(
               IPSDataErrors.HTML_CONV_EXT_NOT_SUPPORTED, pageExt);
         }
      }

      /* build the response object */
      PSResponse resp = request.getResponse();
      if (resp == null) {   /* this should never happen! */
         throw new PSConversionException(IPSDataErrors.NO_RESPONSE_OBJECT);
      }

      ByteArrayOutputStream bout = null;
      ByteArrayInputStream in = null;
      try {
         if (doc == null) {
            resp.setStatus(IPSHttpErrors.HTTP_NOT_FOUND);
         }
         else {
            bout = new ByteArrayOutputStream();
            String encoding = getEncodingForRequestPage(getResultPageIndex(data));
            if(encoding == null)
               encoding = m_requestor.getCharacterEncoding();
            Properties serverProps = PSServer.getServerProps();
            boolean bAllowsEncodingMods = 
               Boolean.valueOf(
                 serverProps.getProperty(PSServer.PROP_ALLOW_XSL_ENCODING_MODS,
                 "false")).booleanValue();
            PSStyleSheetMerger.merge(request, doc, bout, 
               bAllowsEncodingMods ? encoding : null);
            
            if (contentHeader == null)
            {
               contentHeader =
                  PSBaseHttpUtils.constructContentTypeHeader(mimeType, encoding);
            }
            
            int contentLength = 0;
            // Cleanup Namespaces if allowed. This will
            // remove any non-xhtml compliant namespace declarations
            if(isNamespaceCleanupAllowedForResultPage(pageIndex))
            {   
               String mergedResults = bout.toString(encoding);
               mergedResults = 
                  PSStylesheetCleanupUtils.namespaceCleanup(
                     mergedResults, PSStylesheetCleanupFilter.getInstance());
               byte[] mergedAsBytes = mergedResults.getBytes(encoding);
               contentLength = mergedAsBytes.length;
               in = new ByteArrayInputStream(mergedAsBytes);
            }
            else
            {
               contentLength = bout.size();
               in = new ByteArrayInputStream(bout.toByteArray());               
            }
            
            resp.setContent(
               in, contentLength, contentHeader, false );
            in = null;
         }
      }
      catch(UnsupportedEncodingException e)
      {         
         PSConsole.printMsg(this.getClass().getName(), e);
         throw new RuntimeException(e.getLocalizedMessage());
      }
      finally
      {
         if (in != null) {
            try { in.close(); }
            catch (java.io.IOException e) { /* should never happen on a byte stream */ }
         }

         if (bout != null) {
            try { bout.close(); }
            catch (java.io.IOException e) { /* should never happen on a byte stream */ }
         }
      }
   }

   /**
    * What is the default MIME type for this converter?
    *
    * @return               the default MIME type
    */
   public String getDefaultMimeType()
   {
      return null;// now it is just blah IPSMimeContentTypes.MIME_TYPE_TEXT_HTML;
   }

   /**
    * Generate the results for this request.
    *
    * @param   execData      the execution data associated with this request.
    *                                    This includes all context data, result sets, etc.
    *
    * @exception   PSConversionException
    *                                    if the conversion fails
    *
    * @exception   PSUnsupportedConversionException
    *                               if conversion to the format required by the
    *                               specified request URL is not supported
    */
   public void generateResults(PSExecutionData data)
      throws PSConversionException, PSUnsupportedConversionException
   {
      /* simply call convert */
      convert(data, null);
   }


   /* *********************   Protected Implementation ******************** */

   /**
    * Is the request URL supported by this converter? The request URL may
    * contain an extension. When it does, this is used in defining the
    * output which will be returned.
    *
    * @param   reqUrl      the URL which was specified when making this
    *                                    request
    *
    * @return               <code>true</code> if conversion is supported,
    *                             <code>false</code> otherwise
    */
   protected boolean isSupported(String reqPageURL)
   {
      /* check the URL to see if it matches the HTML conversion rules */
      if (reqPageURL == null)
         return false;

      reqPageURL = reqPageURL.toLowerCase();
      int slashIndex = reqPageURL.lastIndexOf('/');
      if (slashIndex > -1)
      {
         String resourcePortion = reqPageURL.substring(slashIndex + 1);
         int dotIndex = resourcePortion.indexOf('.');
         if (dotIndex > -1)
         {
            String extension = reqPageURL.substring(dotIndex + 1);
            return m_requestor.isExtensionSupported(extension);
         }
      }

      /* if not, check if our super-class supports it */
      return super.isSupported(reqPageURL);
   }
}


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

import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.server.PSRequest;
import com.percussion.util.PSCharSets;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;


/**
 * The PSStyleSheetMerger abstract class is used as the base for all
 * style sheet merger implementations.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSStyleSheetMerger
{
   /**
    * Create an XML + style sheet merger object.
    */
   protected PSStyleSheetMerger()
   {}

   /**
    * Get the merger object capable of merging the specified style sheet.
    *
    * @param   styleURL      the URL to check
    *
    * @return               the merger object or <code>null</code> if the
    *                                    type is not supported
    */
   public static PSStyleSheetMerger getMerger(java.net.URL styleURL)
   {
      String urlExt = styleURL.getFile().toLowerCase();
      int pos = urlExt.lastIndexOf('.');
      if (pos == -1)
         return null;

      urlExt = "text/" + urlExt.substring(pos+1);
      return (PSStyleSheetMerger)ms_StyleSheetMergers.get(urlExt);
   }
   
   /**
    * Convienience method for {@link #merge(PSRequest,Document,OutputStream, String)}
    */
   public static void merge(PSRequest req, Document doc, OutputStream out)
      throws PSConversionException, PSUnsupportedConversionException
   {
      merge(req, doc, out, (String)null);
   }   
   
   /**
    * Merge the style sheet defined in the XML document to generate
    * HTML output.
    *
    * @param   req         the request object (may be <code>null</code>)
    *
    * @param   doc         the XML document to be processed
    *
    * @param   out         the output stream to which the results will be
    *                      written
    * @param encoding character encoding to be used in XSL stylesheet output
    * element. Can be <code>null</code> or empty.
    *
    * @exception   PSConversionException
    *                                    if the conversion fails
    *
    * @exception  PSUnsupportedConversionException
    *                      if the style sheet defined in the XML document
    *                      is of an unsupported type
    */
   public static void merge(PSRequest req, Document doc, OutputStream out,
      String encoding)
      throws PSConversionException, PSUnsupportedConversionException
   {
      // get the style sheet info from the XML document
      PSStyleSheet styleSheet;
      try {
         styleSheet = new PSStyleSheet(doc);
      } catch (java.net.MalformedURLException e) {
         ProcessingInstruction pi = PSStyleSheet.getPINode(doc);
         String ssName;
         if (pi == null)
            ssName = "";
         else
         {
            ssName = pi.getData();

            int start      = ssName.indexOf('"') + 1;
            int end         = ssName.indexOf('"', start);
            start            = ssName.indexOf('"', end+1) + 1;
            end            = ssName.indexOf('"', start);

            ssName   = ssName.substring(start, end);
         }
         Object[] args = { req.getUserSessionId(), ssName, e.toString() };
         throw new PSConversionException(
            IPSDataErrors.HTML_GEN_BAD_STYLESHEET_URL, args);
      }

      java.net.URL styleURL = styleSheet.getURL();

      if (styleURL == null) {
         try {
            styleURL = new java.net.URL( DEFAULT_STYLE_SHEET );
         } catch (java.net.MalformedURLException e)
         {
            styleURL = null;
         }

         if (styleURL == null)
         {
            throw new PSConversionException(
               IPSDataErrors.HTML_GEN_NO_STYLESHEET);
         }
      }

      PSStyleSheetMerger merger =
         (PSStyleSheetMerger)ms_StyleSheetMergers.get(styleSheet.getType());
      if (merger != null)
      {
         merger.merge(req, doc, out, styleURL, encoding);

         // trace Ouput Conversion
         if (req.getLogHandler() instanceof PSDebugLogHandler)
         {
            PSDebugLogHandler dh = (PSDebugLogHandler)req.getLogHandler();
            if (dh.isTraceEnabled(PSTraceMessageFactory.OUTPUT_CONV_FLAG))
            {
               Object[] args = {styleSheet.getType(), styleURL};
               dh.printTrace(PSTraceMessageFactory.OUTPUT_CONV_FLAG, args);
            }
         }
      }
      else {
         try {
            Writer writer = new OutputStreamWriter(out, PSCharSets.rxJavaEnc());
            /* Put out Xml data directly, no style sheet merger defined */
            PSXmlDocumentBuilder.write(doc, writer,
               PSCharSets.rxStdEnc());   // use just the XML data
         } catch (java.io.IOException e) {
            throwConversionException(doc, styleURL, e.toString());
         }
      }
   }
   
   /**
    * Convienience method for {@link #merge(PSRequest,Document,OutputStream,URL,String)}
    */
   public void merge(
      PSRequest req, Document doc, OutputStream out, java.net.URL styleFile)
      throws   PSConversionException, PSUnsupportedConversionException
      {
         merge(req, doc, out, styleFile, null);
      }


   /**
    * Merge the specified style sheet with the XML document to generate
    * HTML output.
    *
    * @param   req         the request object (may be <code>null</code>)
    *
    * @param   doc         the XML document to be processed
    *
    * @param   out         the output stream to which the results will be
    *                      written
    *
    * @param   styleFile   the style sheet to use
    * 
    * @param encoding character encoding to be used in XSL stylesheet output
    * element. Can be <code>null</code> or empty.
    *
    * @throws   PSConversionException
    *                                    if the conversion fails
    *
    * @throws  PSUnsupportedConversionException
    *                      if the style sheet defined in the XML document
    *                      is of an unsupported type
    */
   public abstract void merge(
      PSRequest req, Document doc, OutputStream out, java.net.URL styleFile,
         String encoding)
      throws   PSConversionException, PSUnsupportedConversionException;


   protected static void throwConversionException(
      Document doc, java.net.URL styleURL, String text)
      throws PSConversionException
   {
      String root = null;
      if ((doc != null) && (doc.getDocumentElement() != null))
         root = doc.getDocumentElement().getTagName();
      if (root == null)
         root = "";

      String url = "";
      if (styleURL != null)
         url = styleURL.toExternalForm();

      Object[] args = { root, url, text };
      throw new PSConversionException(
         IPSDataErrors.STYLESHEET_MERGE_EXCEPTION, args);
   }


   /**
    * This is a hash of our style sheet merging engines. The key is
    * the style sheet MIME type (eg, text/css) and the value is an
    * instance of the appropriate PSStyleSheetMerger subclass.
    */
   private static HashMap ms_StyleSheetMergers = new HashMap();

   /**
    * Initializes the static merger map so that the appropriate mergers can
    * be found for each MIME type.
    */
   static
   {
      PSXslStyleSheetMerger xslMerger = new PSXslStyleSheetMerger();

      ms_StyleSheetMergers.put("text/css", new PSCssStyleSheetMerger());
      ms_StyleSheetMergers.put("text/xsl", xslMerger);

      /* Default case for when we need to use the the default style sheet */
      ms_StyleSheetMergers.put(null, xslMerger);
   }

   /** The stylesheet to use if one is not specified */
   public static final String DEFAULT_STYLE_SHEET="file:Defaults/StyleSheets/table.xsl";
}


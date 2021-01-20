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
package com.percussion.xmldom;

import com.percussion.data.PSCachedStylesheet;
import com.percussion.data.PSTransformErrorListener;
import com.percussion.data.PSUriResolver;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSServer;
import com.percussion.xml.PSStylesheetCacheManager;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * A Rhythmyx extension that applies an XSL Stylesheet to a temporary
 * XML document.
 *
 * <p>
 * This extension can be used as either a pre-exit or a post exit.  The
 * parameters are:
 * <table border="1">
 *   <tr><th>Param #</th><th>Name</th><th>Description</th><th>Required?</th>
 *   <th>default value</th></tr>
 *   <tr>
 *     <td>1</td>
 *     <td>sourceObjectName</td>
 *     <td>The name of the source temporary XML document object.  May be
 *         <code>InputDocument</code> when used as a pre-exit or 
 *         <code>ResultDocument</code> when used as a post-exit (see below
 *         for details).</td>
 *     <td>yes</td>
 *     <td>XMLDOM</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>StyleSheet</td>
 *     <td>File name of the XSL stylesheet to apply to source object.  This
 *         file must be stored in the current application's directory.</td>
 *     <td>yes</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>3</td>
 *     <td>destObjectName</td>
 *     <td>The name of the destination temporary XML document object.  May be
 *         <code>InputDocument</code> when used as a pre-exit or 
 *         <code>ResultDocument</code> when used as a post-exit (see below
 *         for details).</td>
 *     <td>yes</td>
 *     <td>XMLDOM</td>
 *   </tr>
 * </table>
 *
 * <p>
 * The XSL Stylesheet must reside in the current application directory.  The
 * easiest way to accomplish this is to attach it to a query in the current
 * application.  Through a combination of selection parameters, it is possible
 * to prevent this stylesheet from being used except for as part of this
 * exit.
 * <p>
 * When used as a pre-exit, the special XML document name
 * <code>InputDocument</code> may be used.  This name refers to the input XML
 * document.  Usually, this document is provided by the PSXmlUploader.
 * <p>
 * When used as a post-exit, the name <code>ResultDocument</code> may be used 
 * as the source and/or destination object.  This name refers to the document 
 * generated by the query or update resource to which this exit is attached; 
 * this is the document that will be returned to the requestor, usually after 
 * being transformed into HTML by an attached stylesheet.
 * <p>
 * This extension parses the output of the XSL processor and stores it as an
 * XML document.  For this reason, the output must be well-formed.  The best
 * way to insure this is to use
 * <code>&lt;xsl:output method="xml"&gt;</code>
 * @see PSXdTransformDomToText
 */
public class PSXdTransformDom extends PSDefaultExtension implements
      IPSRequestPreProcessor, IPSResultDocumentProcessor
{

   /**
    * This method handles the pre-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdTransformDom} for parameter details.
    *
    * @param request the request context for this request
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    *
    **/
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException
   {
      PSXmlDomContext cx = new PSXmlDomContext(ms_extname, request);

      // the second parameter (the stylesheet name) must not be null
      if (params.length < 2 || null == params[1] ||
            0 == params[1].toString().trim().length())
      {
         throw new PSParameterMismatchException(params.length, 3);
      }

      // fetch the exit parameters (with defaults)
      String sourceObjectName =
            PSXmlDomUtils.getParameter(params, 0, PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);
      String stylesheetName =
            PSXmlDomUtils.getParameter(params, 1, "");
      String destObjectName =
            PSXmlDomUtils.getParameter(params, 2, PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);

      try
      {
         Document sourceDoc;
         if (sourceObjectName.equals("InputDocument"))
            sourceDoc = request.getInputDocument();
         else
            sourceDoc = (Document) request.getPrivateObject(sourceObjectName);

         if (null == sourceDoc)
         {
            // there is no XML document.  This is not necessarily an error
            request.printTraceMessage("no document found");
            return;
         }
         Document outDoc = processXmlDoc(cx, sourceDoc,
               PSXmlDomUtils.getFullStyleName(stylesheetName,
                                              request.getCurrentApplicationName()));

         if (destObjectName.equals("InputDocument"))
            request.setInputDocument(outDoc);
         else
            request.setPrivateObject(destObjectName, outDoc);
      }
      catch (Exception e)
      {
         cx.handleException(e);
      }

   }

   //see IPSResultDocumentProcessor
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      PSXmlDomContext cx = new PSXmlDomContext(ms_extname, request);
      if (params.length < 2 || null == params[1] ||
            0 == params[1].toString().trim().length())
      {
         throw new PSParameterMismatchException(params.length, 3);
      }

      // fetch the exit parameters (with defaults)
      String sourceObjectName = PSXmlDomUtils.getParameter(params, 0, 
         PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);
      String stylesheetName = PSXmlDomUtils.getParameter(params, 1, "");
      String destObjectName = PSXmlDomUtils.getParameter(params, 2,
         PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);

      try
      {
         Document sourceDoc;
         if (sourceObjectName.equals("ResultDocument"))
            sourceDoc = resultDoc;
         else
            sourceDoc = (Document) request.getPrivateObject(sourceObjectName);

         if (null == sourceDoc)
         {
            // there is no XML document. This is not an error.
            // Perhaps we were told to look in the wrong location,
            // or perhaps the data was not included in this request
            request.printTraceMessage("no document found");
            return resultDoc;
         }
         Document outDoc = processXmlDoc(cx, sourceDoc,
               PSXmlDomUtils.getFullStyleName(stylesheetName,
                                              request.getCurrentApplicationName()));

         if (destObjectName.equals("ResultDocument"))
            resultDoc = outDoc;
         else
            request.setPrivateObject(destObjectName, outDoc);
      }
      catch (Exception e)
      {
         cx.handleException(e);
      }

      return resultDoc;
   }

   /**
    * This method will never modify the stylesheet.
    **/
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * This method handles the common parts of the pre-exit and the post-exit.
    * The stylesheet is applied to the source and the result is parsed back
    * into XML.
    *
    * @param cx the XMLDOM context for this request;
    *    must not be <code>null</code>
    * @param xmlDoc the source document to be transformed;
    *    must not be <code>null</code>
    * @param stylesheetName a URL to the XSLT stylesheet;
    *    must not be <code>null</code> or empty
    * @return an XML Document containing the output from the stylesheet
    *
    * @throws PSExtensionProcessingException
    * @throws TransformerException
    * @throws SAXException
    * @throws IOException
    **/
   private static Document processXmlDoc(PSXmlDomContext cx, Document xmlDoc,
                                         String stylesheetName)
         throws PSExtensionProcessingException,
         TransformerException, SAXException, IOException
   {
      // define the string that holds the errors
      StringWriter errorWriter = new StringWriter();
      PSTransformErrorListener errorListener = new PSTransformErrorListener(
            new PrintWriter(errorWriter));
      URL styleFile;
      
      if (stylesheetName.startsWith("file:") && stylesheetName.length() > 6 &&
            stylesheetName.charAt(5) != '/')
      {
         File f = new File(PSServer.getRxDir(), stylesheetName.substring(5));
         styleFile = f.toURI().toURL();
      }
      else
      {
         styleFile = new URL(stylesheetName);
      }
      
      PSCachedStylesheet styleCached =
            com.percussion.xml.PSStylesheetCacheManager.getStyleSheetFromCache(styleFile);

      try
      {
         Transformer nt = styleCached.getStylesheetTemplate().newTransformer();
         nt.setErrorListener(errorListener);
         nt.setURIResolver(new PSUriResolver());

         DOMSource src =
               new DOMSource((Node) xmlDoc);
         DOMResult res =
               new DOMResult((Node) PSXmlDocumentBuilder.createXmlDocument());

         nt.transform(src, res);

         Document outputDoc = (Document) res.getNode();

         cx.printTraceMessage("XSL processing complete");
         if (cx.isLogging() &&
               (errorListener.numErrors() + errorListener.numFatalErrors() > 0))
         {
            cx.printTraceMessage("Errors occurred during XSL processing \n"
                  + errorWriter.toString());
         }
         else
         {
            cx.printTraceMessage("XSL processor messages \n"
                  + errorWriter.toString());
         }

         if (cx.isLogging())
         {
            cx.printTraceMessage("writing trace file xmldocxformout.doc");
            FileOutputStream parsedOutput =
                  new FileOutputStream("xmldocxformout.doc");

            PSXmlTreeWalker walk = new PSXmlTreeWalker(outputDoc);
            walk.write(new BufferedWriter(new OutputStreamWriter(
                  parsedOutput, "UTF-8")), true);

            parsedOutput.close();
         }

         return outputDoc;
      }
      catch (TransformerConfigurationException e)
      {
         // there is an error with the XSLT stylesheet
         StringBuffer errorMsg = new StringBuffer(e.toString());
         errorMsg.append("\r\n");
         PSStylesheetCacheManager.
               appendErrorMessages(errorMsg, styleCached.getErrorListener());
         throw new PSExtensionProcessingException(0, errorMsg.toString());
      }
   }

   /**
    * name to be included in the debugging messages this class generates
    **/
   private static final String ms_extname = "PSXdTransformDom";
}

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
package com.percussion.extensions.general;

import com.percussion.data.PSCachedStylesheet;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSTransformErrorListener;
import com.percussion.data.PSUriResolver;
import com.percussion.error.PSException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSStylesheetCacheManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A UDF that makes an internal request to the specified resource and returns
 * the result document.
 */
public class PSMakeInternalRequest extends PSSimpleJavaUdfExtension
{
   /**
    * Makes an internal request to the specified Rhythmyx resource and returns
    * the result document.
    * 
    * @params[0] the Rhythmyx resource to which to make an interal request. 
    *    Specifies the application and page of the dataset to which the 
    *    internal request is to be made. May be as little as 
    *    <code>appName/pageName</code> or as much as 
    *    <code>http://127.0.0.1:9992/Rhythmyx/AppTest/nov.xml?
    * alpha=bravo&amp;test=5</code>,
    *    not <code>null</code> or empty.
    * @params[1] the name of the stylesheet to be applied to the request 
    *    result document, may be <code>null</code> or empty in which case this
    *    parameter is ignored. The stylesheet must be stored in a rhythmyx
    *    application. If stored in the current application, just the file
    *    name is needed (e.g. transform.xsl). For other applications use 
    *    relative path (e.g. ../sys_resources/stylesheets/transform.xsl).
    * @params[2] a flag as <code>String</code> to specify whether or not the 
    *    parameters of the supplied request will be inherited. If 'yes'
    *    (case insensitive) is supplied, the flag evaluates to 
    *    <code>true</code>, otherwise it will be <code>false</code>. This is
    *    optional and defaults to <code>true</code> if not supplied.
    * @params[3+n] the names of additional request parameters, optional. Parsing
    *    request parameters will be stopped if the first <code>null</code> or
    *    empty parameter name is found.
    * @params[3+n+1] the values of additional request parameters, optional. If
    *    <code>null</code>, an empty <code>String</code> is used.
    * @return the requested document or <code>null</code> if no request handler
    *    was found for the supplied resource.
    * @throws PSConversionException for any invalid parameter and all errors
    *    from the internal request.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;
      
      int index = 0;
      
      // get the rhythmyx resource, this is the only required parameter
      String resource = "";
      if (size > 0)
         resource = (params[index] == null) ? "" : 
            params[index].toString().trim();
      index++;
      if (resource.length() == 0)
      {
         int errorCode = 0;
         String errorMsg = "You must provide a valid Rhythmyx resource.";
            
         throw new PSConversionException(errorCode, errorMsg);
      }
      
      // get the stylesheet to be applied to the result, optional
      String stylesheet = "";
      if (size > 1)
         stylesheet = (params[index] == null) ? "" : 
            params[index].toString().trim();
      if (stylesheet.length() > 0)
      {
         String prefix = "../";
         if (stylesheet.startsWith(prefix))
            stylesheet = "file:" + stylesheet.substring(prefix.length());
         else
            stylesheet = "file:" + request.getCurrentApplicationName() + "/" + 
               stylesheet;
      }
      index++;

      // should request parameters be inherited, defaults to yes
      String inheritParams = INHERIT_PARAMS;
      if (size > 2 && params[index] != null)
         inheritParams = params[index].toString().trim();
      if (inheritParams.length() == 0)
         inheritParams = INHERIT_PARAMS;
      index++;
      boolean inherit = inheritParams.equalsIgnoreCase(INHERIT_PARAMS);

      int count = 0;
      String paramName = "";
      String paramValue = "";
      
      // get all specified request parameters
      Map requestParams = new HashMap();
      while (index < size)
      {
         if ((count % 2) == 0)
         {
            paramName = (params[index] == null) ? 
               "" : params[index].toString().trim();
            count++;
         }
         else
         {
            paramValue = (params[index] == null) ? 
               "" : params[index].toString().trim();
            count++;

            // stop parsing request parameters with the first unspecified name 
            if (paramName.length() > 0)
               requestParams.put(paramName, paramValue);
            else
               break;
         }
         
         index++;
      }

      try
      {
         // make the internal request with for the specified resource and params
         IPSInternalRequest ir = 
            request.getInternalRequest(resource, requestParams, inherit);
         if (ir != null)
         {
            Document doc = ir.getResultDoc();
            return processStylesheet(doc, stylesheet, request);
         }
            
         return null;
      }
      catch (Exception e)
      {
         String errorMsg = "An unexpected error occurred. The error was: " +
            e.getLocalizedMessage();
            
         throw new PSConversionException(0, errorMsg);
      }
   }

   /**
    * The stylesheet is applied to the source and the result is parsed back
    * into XML.
    *
    * @param doc the source document to be transformed, assumed not 
    *    <code>null</code>.
    * @param stylesheetName a url string to the stylesheet, may be 
    *    <code>null</code> or empty in which case this method does nothing.
    * @param request the request used to print the operations trace messages
    *    to, assumed not <code>null</code>.
    * @return the XML Document containing the output from the stylesheet
    * @throws PSConversionException for problems with the provided stylesheet.
    * @throws TransformerException for any transformation errors.
    * @throws SAXException for parsing errors.
    * @throws IOException fro any I/O error.
    */
   private Document processStylesheet(Document doc, String stylesheetName, 
      IPSRequestContext request) throws PSConversionException,
      TransformerException, SAXException, IOException
   {
      if (stylesheetName == null || stylesheetName.trim().length() == 0)
         return doc;
         
      request.printTraceMessage("Processing stylesheet...");
      
      StringWriter errorWriter = new StringWriter();
      PSTransformErrorListener errorListener = 
         new PSTransformErrorListener(new PrintWriter(errorWriter));

      URL stylesheetFile = new URL(stylesheetName);
      PSCachedStylesheet stylesheet = null;
      try
      {
         stylesheet = 
            PSStylesheetCacheManager.getStyleSheetFromCache(stylesheetFile);
            
         Transformer transformer = 
            stylesheet.getStylesheetTemplate().newTransformer();
         transformer.setErrorListener(errorListener);
         transformer.setURIResolver(new PSUriResolver());
   
         DOMResult res = new DOMResult();
         transformer.transform(new DOMSource(doc), res);
   
         Document resultDoc = (Document) res.getNode();
   
         request.printTraceMessage("Stylesheet processing complete");
   
         return resultDoc;
      }
      catch (PSException e)
      {
         throw new PSConversionException(e.getErrorCode(), 
            e.getErrorArguments());
      }
      catch (TransformerConfigurationException e)
      {
         // there is an error with the stylesheet
         StringBuffer errorMsg = new StringBuffer(e.getLocalizedMessage());
         errorMsg.append("\r\n");
         
         if (stylesheet != null)
            PSStylesheetCacheManager.appendErrorMessages(errorMsg, 
               stylesheet.getErrorListener());
         
         throw new PSConversionException(0, errorMsg.toString());
      }
   }
   
   /**
    * The value of the <code>inheritParams</code> parameter that evaluates to
    * <code>true</code>. The evaluation is made case insensitive. This is also
    * the default, if the parameter is not specified.
    */
   private static final String INHERIT_PARAMS = "yes";
}

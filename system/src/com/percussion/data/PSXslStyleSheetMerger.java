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

import com.percussion.security.xml.PSCatalogResolver;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.util.PSStopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXParseException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The PSXslStyleSheetMerger class implements XSL support for the
 * IPSStyleSheetMerger interface. This processor uses an XSL style sheet
 * and merges it with an XML document to generate HTML output.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSXslStyleSheetMerger extends PSStyleSheetMerger
{
   /**
    * Convenience method that calls {@link #merge(PSRequest, Document,
    * OutputStream, URL, Iterator, String) merge(
    *    req, doc, out, styleFile, null, encoding)}.
    */
   public void merge(
      PSRequest req, Document doc, OutputStream out, URL styleFile,
         String encoding)
      throws   PSConversionException
   {
      merge(req, doc, out, styleFile, null, encoding);
   }

   /**
    * Merge the XSL style sheet defined in the XML document to generate
    * HTML output. The <code>stylesheet</code> processing instruction
    * must exist in the XML document and refer to an XSL style sheet.
    *
    * @param   req the request object (may be <code>null</code>)
    * @param   doc the XML document to be processed,may not be <code>null</code>
    * @param   out the output stream to which the results will be written, may
    * not be <code>null</code>.
    * @param   styleFile the style sheet to use, may not be <code>null</code>
    * @param   params An iterator over zero or more Map.Entry objects, where for
    * each entry the the key is the param name as a String and the value is an
    * object whose <code>toString</code> method will be used as the value of the
    * param.  The params are passed to the style sheet processor, and will be
    * used to set the value of any global parameters defined as an <code>
    * xsl:param</code> element in the stylesheet with a matching name (the
    * parameter must be defined as a child of the xsl:stylesheet
    * element).  If a matching parameter declaration is not found in the
    * stylesheet, the supplied parameter is silently ignored.  May be <code>
    * null</code> if no parameters are to be supplied.
    * @param encoding the encoding that should be used in the XSL stylesheets
    * output element. Can be <code>null</code> or empty.
    *
    * @throws   PSConversionException if the conversion fails
    *
    * @throws  PSUnsupportedConversionException if the style sheet defined in
    * the XML document is of an unsupported type
    *
    * @throws IllegalArgumentException if <code>doc</code> or <code>out</code>
    * or <code>styleFile</code> is <code>null</code>
    */
   public void merge(PSRequest req, Document doc, OutputStream out,
                     URL styleFile, Iterator params, String encoding)
      throws   PSConversionException
   {
      if(doc == null)
         throw new IllegalArgumentException(
            "The Source document to merge should not be null");

      if(out == null)
         throw new IllegalArgumentException(
            "The output stream to which results" +
            " will be written should not be null");

      if(styleFile == null)
         throw new IllegalArgumentException(
            "The Style Sheet to merge should not be null");

      ConcurrentHashMap ssCache = null;

      // see if params are passed
      boolean hasParams = params != null && params.hasNext();

      // get the cache from the apphandler
      if (req != null)
      {
         PSApplicationHandler ah = req.getApplicationHandler();
         if (ah != null)
            ssCache = ah.getStylesheetCache();
      }

      PSCachedStylesheet cachedSS = null;
      Templates ssTemplate = null;
      StringBuilder errorMsg = new StringBuilder();

      try
      {

         if (ssCache != null)
         {
            cachedSS = (PSCachedStylesheet)ssCache.get(styleFile);
            if (cachedSS == null)
            {
               cachedSS = new PSCachedStylesheet(styleFile);
               ssCache.put(styleFile, cachedSS);
            }
         } else  // not really cached!
            cachedSS = new PSCachedStylesheet(styleFile);

         ssTemplate = cachedSS.getStylesheetTemplate(encoding);

         try
         {
            ErrorListener listener = cachedSS.getErrorListener();
            if (listener instanceof PSTransformErrorListener)
               errorMsg.append( getErrorListenerMessage( 
                  (PSTransformErrorListener) listener ) );
         } catch (IOException ioe)
         {
            throwConversionException( doc, styleFile,
               "Exception happened while reading error messages of style sheet"
               + " loading" + ioe.getLocalizedMessage() );
         }

         if(errorMsg.length() > 0)
         {
            String message = "Error loading style sheet ";
            message += errorMsg;
            throwConversionException(doc, styleFile, message);
         }

      }
      catch(Exception se)
      {
         errorMsg.append(se);

         try {
            //For SAXParseException, styleFile has been sent as parameter
            //because when I tried to getSystemId of SAXParseException for
            //getting stylesheet in which error occurred, it's giving always
            //null.
            if(se instanceof SAXParseException)
               errorMsg.append(getExceptionContextData(se, styleFile));
            else if(se instanceof TransformerException)
               errorMsg.append(getExceptionContextData(se));

            if(cachedSS != null)
            {
               ErrorListener listener = cachedSS.getErrorListener();
               if (listener instanceof PSTransformErrorListener)
                  errorMsg.append( getErrorListenerMessage( 
                     (PSTransformErrorListener) listener ) );
            }
         }
         catch(IOException ioe)
         {
            errorMsg.append(" ");
            errorMsg.append("Exception happened while reading error messages of"
               + " style sheet loading, " + ioe.getLocalizedMessage());
         }

         throwConversionException(doc, styleFile, errorMsg.toString());
      }
      //This error will be thrown when the class of a transformation factory
      //specified in the system properties cannot be found or instantiated.
      catch(TransformerFactoryConfigurationError error)
      {
         throwConversionException(doc, styleFile, error.getLocalizedMessage());
      }
      finally
      {
         if(cachedSS != null)
         {
            //clear errors from listener once read from it, so that next call to
            //getStylesheetTemplate() will not keep the old ones.
            ((PSTransformErrorListener)cachedSS.getErrorListener()).clear();
         }
      }

      Transformer transformer = null;
      try
      {
         transformer = ssTemplate.newTransformer();

         PSCatalogResolver cr = new PSCatalogResolver();
         cr.setInternalRequestURIResolver(new PSInternalRequestURIResolver());
         transformer.setURIResolver(cr);
         transformer.setErrorListener( new PSTransformErrorListener() );

         // add any params supplied
         if (hasParams)
         {
            while (params.hasNext())
            {
               Map.Entry param = (Map.Entry)params.next();
               transformer.setParameter(param.getKey().toString(),
                  param.getValue().toString());
            }
         }
         
         if (req != null)
            req.getRequestTimer().pause();
         Logger l = LogManager.getLogger(getClass());
         PSStopwatch watch = null;
         if (l.isDebugEnabled())
         {
            watch = new PSStopwatch();
            watch.start();
         }
         transformer.transform(new DOMSource(doc), new StreamResult(out));
         if (watch != null)
         {
            watch.stop();
            l.debug("Transforming stylesheet {} {}" , styleFile ,
                 watch);
         }
         if (req != null)
            req.getRequestTimer().cont();
            
         if (hasParams)
            transformer.clearParameters();

         try 
         {
            // we assigned the listener to the transformer, so it is safe to cast
            String errorMessage = getErrorListenerMessage( 
               (PSTransformErrorListener) transformer.getErrorListener() );
            if (errorMessage.length() > 0)
            {
               String message = "Error transforming the xml document with " +
                  "stylesheet " + errorMessage;
               throwConversionException(doc, styleFile, message);
            }
         }
         catch(IOException ioe)
         {
           throwConversionException(doc, styleFile,
               "Exception happened while reading error messages of document " +
               "transformation with style sheet, " + ioe.getLocalizedMessage());
         }
      }
      catch (TransformerException e)
      {
         if (e instanceof TransformerConfigurationException)
            errorMsg.append( "Error getting Transformer object." );
         else
         {
            errorMsg.append(
               "Error while transforming the XML file with stylesheet." );
            try
            {
               // we assigned the listener to the transformer, so it is safe to cast
               if (transformer != null)
               {
                  String errorMessage = getErrorListenerMessage(
                     (PSTransformErrorListener) transformer.getErrorListener() );
                  errorMsg.append( errorMessage );
               }
               errorMsg.append( e.getMessageAndLocation() );
               errorMsg.append( getExceptionContextData( e ) );
            } catch (IOException ioe)
            {
               errorMsg.append( " Exception happened while reading error " +
                  "messages of document transformation with style sheet, " );
               errorMsg.append( ioe.getLocalizedMessage() );
            }
         }

         throwConversionException( doc, styleFile, errorMsg.toString() );
      } catch (Exception e)
      {
         throwConversionException( doc, styleFile, e.toString() );
      }
   }

   /**
    * Gets error and fatal error messages of listener as a string.
    *
    * @param listener the error listener, not <code>null</code>.
    *
    * @return error message, never <code>null</code>, may be empty.
    *
    * @throws IOException reading source file in which error occurred.
    */
   public static String getErrorListenerMessage(PSTransformErrorListener listener)
      throws IOException
   {
      if (listener == null)
         throw new IllegalArgumentException( "ErrorListener may not be null" );

      StringBuilder errorMsg = new StringBuilder();

      if (listener.numErrors() > 0)
         errorMsg.append( getErrorMessages( listener.errors() ) );
      if (listener.numFatalErrors() > 0)
         errorMsg.append( getErrorMessages( listener.fatalErrors() ) );

      return errorMsg.toString();
   }

   /**
    * Appends list of error messages in to a single message with contextual
    * data of error.
    *
    * @param errors the iterator of error list, not <code>null</code>.
    *
    * @return error message, never <code>null</code>, may be empty.
    *
    * @throws IOException reading source file in which error occurred.
    */
   private static String getErrorMessages(Iterator errors)
      throws IOException
   {
      if (errors == null)
         throw new IllegalArgumentException("errors may not be null");

      StringBuilder errorMsg = new StringBuilder();
      while(errors.hasNext())
      {
         TransformerException e = (TransformerException)errors.next();
         errorMsg.append(e.getMessageAndLocation());
         errorMsg.append(" ");
         errorMsg.append(getExceptionContextData(e));
         errorMsg.append("\r\n");
      }
      return errorMsg.toString();
   }

   /**
    * Returns the range of data of source in which the exception occurred.
    * Returns empty string if source of the exception can not be found or the
    * error line number is less than 0.
    *
    * @param e the exception, not <code>null</code>.
    *
    * @return a string containing any contextual text which can be found,
    * never <code>null</code>, may be empty.
    *
    * @throws IOException reading source file in which exception occurred.
    */
   private static String getExceptionContextData(Exception e)
      throws IOException
   {
      if (e == null)
         throw new IllegalArgumentException( "Exception may not be null" );

      String resource = "";

      if (e instanceof TransformerException)
      {
         TransformerException te = (TransformerException) e;
         if (te.getLocator() != null)
            resource = te.getLocator().getSystemId();
      }
      else if (e instanceof SAXParseException)
      {
         SAXParseException se = (SAXParseException) e;
         resource = se.getSystemId();
      }

      if (resource == null || resource.equals( "" ))
         return "";

      URL url;
      try
      {
         if (resource.startsWith( "file:" ) || resource.startsWith( "http:" ))
            url = new URL( resource );
         else
            url = new URL( "file:" + resource );

         return getExceptionContextData( e, url );
      } catch (MalformedURLException ex)
      {
         //should not come here
      }
      //Treat as if source of the exception can not be found
      return "";

   }

   /**
    * Returns an error message containing the data in the specified range
    * for the exception.
    * Returns an empty string if the found error line number is less than 0.
    *
    * @param   e       the exception, assumed not <code>null</code>.
    * @param   url    the source stream in which error happened, assumed not
    * <code>null</code>.
    *
    * @return   a string containing any contextual text which can be found,
    * never <code>null</code>, may be empty.
    *
    * @throws IOException reading source file in which exception occurred.
    */
   private static String getExceptionContextData(Exception e, URL url)
      throws IOException
   {
      StringBuilder errorMsg = new StringBuilder();
      int errorLine = 0;

      if(e instanceof TransformerException)
      {
         TransformerException te = (TransformerException)e;
         if(te.getLocator() != null)
            errorLine = te.getLocator().getLineNumber();
      }
      else if(e instanceof SAXParseException)
         errorLine = ((SAXParseException)e).getLineNumber();

      if(errorLine <= 0)
         return "";

      errorMsg.append("  [Error data in range: ");

        BufferedReader reader = new BufferedReader(
           new InputStreamReader(url.openStream()) );

        getExceptionContextData(errorMsg, reader, errorLine);
      reader.close();

        errorMsg.append("]");
      return errorMsg.toString();
   }

   /**
    * Create an error message containing the data in the specified range
    * for the exception.
    *
    * @param   buf    a buffer to store the output into, assumed not
    * <code>null</code>.
    * @param   source a reader with the source(XSL data), assumed not
    * <code>null</code>.
    * @param   errorLine the error line number
    *
    *
    * @throws IOException reading source file in which exception occurred.
    */
   private static void getExceptionContextData(
      StringBuilder buf, BufferedReader source, int errorLine)
      throws IOException
   {
      if (errorLine > 0)
      {
         String   curLine;

         //append lines from one line before to one line after error.
         for (int i = 1; i <= errorLine+1; i++)
         {
            curLine = source.readLine();

            if (i >= (errorLine-1))
               buf.append(curLine);
         }
      }
   }

   /**
    * Use the application handler (if available) and the request page type
    * to set the style sheet node for the specified document.
    *
    * @param      request         the context for this request,
    *
    * @param      doc            the XML document to modify
    *
    * @param      styleSheetURL   the URL of the style sheet to use
    *
    * @throws     MalformedURLException   if the URL is invlaid
    */
   public static void setStyleSheet(
      PSRequest request, Document doc, URL styleSheetURL)
      throws MalformedURLException
   {
      if(request == null)
         throw new IllegalArgumentException(
            "The context for this request can not be null.");

      if(doc == null)
         throw new IllegalArgumentException(
            "The XML document to modify can not be null.");

      if (styleSheetURL == null)
         throw new IllegalArgumentException(
            "The URL of style sheet to use can not be null.");

      String urlText;
      String ssType;

      PSApplicationHandler ah = request.getApplicationHandler();

      /* make this "remote ready" by converting FILE URL to HTTP URL
        *
       * - part of fix for bug id TGIS-4BWSL9
       * we had the conditions backwards, using the local URL for
       * XML pages and the external form for HTML conversion.
       */
      if (ah != null)
      {
         if (request.getRequestPageType() == PSRequest.PAGE_TYPE_HTML)
            urlText = ah.getLocalizedURL(styleSheetURL).toExternalForm();
         else
            urlText = ah.getExternalURLString(styleSheetURL);
      }
      else
      {
         urlText = styleSheetURL.toExternalForm();
      }

      int extPos = urlText.lastIndexOf('.');
      if (extPos == -1)
         ssType = "xsl";   // assume it's XSL by default
      else
         ssType = urlText.substring(extPos+1).toLowerCase();

      ProcessingInstruction pi = doc.createProcessingInstruction(
         "xml-stylesheet",
         ("type=\"text/" + ssType + "\" href=\"" + urlText + "\""));

      Element root = doc.getDocumentElement();
      if (root != null)
         doc.insertBefore(pi, root);
      else
         doc.appendChild(pi);
   }
}


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
import com.percussion.extension.PSExtensionProcessingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXParseException;

/**
 * Caches stylesheets as PSCachedStylesheet in a Map keyed by URL
 **/
public class PSStylesheetCacheManager

{
   /**
    * This class contains only static methods, and is never constructed
    **/
   private PSStylesheetCacheManager()
   {
      // don't construct this
   }

   /**
    * the Stylesheet cache is a keyed table that contains
    * javax.xml.transform.Templates objects.  Hashtable is used
    * rather than HashMap because it is synchronized.
    **/
   static Map ms_cache = new Hashtable();

   /**
    * get a stylesheet from the cache by URL.  If the stylesheet does not
    * exist in the cache, it will be added
    * @param styleFile the URL of the stylesheet to cache.
    * @return the cached stylesheet object
    * @throws PSExtensionProcessingException if the file does not exist or
    *  cannot be processed.
    **/
   public static PSCachedStylesheet getStyleSheetFromCache(URL styleFile)
         throws PSExtensionProcessingException
   {

      // if we can, verify that the style sheet exists
      if (styleFile.getProtocol().equalsIgnoreCase("file"))
      {
         File f = new File(styleFile.getFile());
         if (!f.exists())
         {
            throw new PSExtensionProcessingException
                  (0, styleFile.toString() + ": Style sheet does not exist -- ");
         }
         else if (!f.isFile())
         {
            throw new PSExtensionProcessingException
                  (0, styleFile.toString() + ": Style sheet is not a file");
         }

         if (f.length() <= 0)
         {
            throw new PSExtensionProcessingException
                  (0, styleFile.toString() + "Style sheet is empty");
         }
      }

      PSCachedStylesheet cachedSS = null;

      try
      {
         cachedSS = (PSCachedStylesheet) ms_cache.get(styleFile);
         if (cachedSS == null)
         {
            cachedSS = new PSCachedStylesheet(styleFile);
            ms_cache.put(styleFile, cachedSS);
         }
      }
      catch (IOException e)
      {
         StringBuffer errorMsg = new StringBuffer(styleFile.toString());
         errorMsg.append("\r\n");
         errorMsg.append(e.toString());
         throw new PSExtensionProcessingException(0, errorMsg.toString());
      }
      catch (TransformerFactoryConfigurationError e)
      {
         StringBuffer errorMsg = new StringBuffer(styleFile.toString());
         errorMsg.append("\r\n");
         errorMsg.append(e.toString());
         throw new PSExtensionProcessingException(0, errorMsg.toString());
      }

      return cachedSS;

   }

   /**
    * Traverses an Iterator, printing each object to a StringBuffer.  If the
    * object is a TransformerException, print additional information.
    *
    * @param   buf    where to append the strings; modified by this method;
    *    cannot be <code>null</code>
    * @param   iter   where to find the objects; cannot be <code>null</code>
    */
   private static void appendFromIterator(StringBuffer buf,
                                          Iterator iter)
   {
      while (iter.hasNext())
      {
         Object o = iter.next();
         if (o instanceof TransformerException)
         {
            TransformerException e = (TransformerException) o;
            buf.append(e.getMessageAndLocation());
            buf.append("\r\n");
            try
            {
               buf.append(getExceptionContextData(e));
               buf.append("\r\n");
            } catch (IOException e1)
            {
               // if there is an error obtaining the contextual data
               // don't include it
            }
         }
         else
         {
            buf.append(o.toString());
            buf.append("\r\n");
         }
      }
   }

   /**
    * Extract errors from an PSTransformErrorListener and append them to a
    * StringBuffer, then clear the errors from the listener.
    *
    * @param   buf      where to append the strings; modified by this method;
    *    if null, return quietly.
    * @param   errors   where to find the errors; if null or not our
    *    implementation, return quietly.
    */
   public static void appendErrorMessages(StringBuffer buf,
                                          ErrorListener errors)
   {
      if (null == buf || null == errors) return;
      if (errors instanceof PSTransformErrorListener)
      {
         /*
         the ErrorListener interface does not define a mechanism to retrieve
         errors.  However, our implementation does.
         */
         PSTransformErrorListener el = (PSTransformErrorListener) errors;
         appendFromIterator(buf, el.fatalErrors());
         appendFromIterator(buf, el.errors());

         // clear errors from listener once read from it, so that next call
         // will not repeat the old ones
         el.clear();
      }
   }

   /**
    * Returns the range of data of source in which the exception occurred.
    * Returns empty string if source of the exception can not be found or the
    * error line number is less than 0.
    *
    * This method was copied from com.percussion.data.PSXslStyleSheetMerger
    *
    * @param   e       the exception, assumed not <code>null</code>.
    *
    * @return   a string containing any contextual text which can be found,
    * never <code>null</code>, may be empty.
    *
    * @throws IOException reading source file in which exception occurred.
    */
   private static String getExceptionContextData(Exception e)
      throws IOException
   {
      StringBuffer errorMsg = new StringBuffer();
      String resource = "";

      if(e instanceof TransformerException)
      {
         TransformerException te = (TransformerException)e;
         if(te.getLocator() != null)
            resource = te.getLocator().getSystemId();
      }
      else if(e instanceof SAXParseException)
      {
         SAXParseException se = (SAXParseException)e;
         resource = se.getSystemId();
      }

      if(resource == null || resource.equals(""))
         return "";

      URL url;
      try {
         if( resource.startsWith("file:") || resource.startsWith("http:") )
            url = new URL(resource);
         else
            url = new URL("file:" + resource);

         return getExceptionContextData(e, url);
      }
      catch(MalformedURLException ex){
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
    * This method was copied from com.percussion.data.PSXslStyleSheetMerger
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
      StringBuffer errorMsg = new StringBuffer();
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
    * This method was copied from com.percussion.data.PSXslStyleSheetMerger.
    * 
    * @param   buf    a buffer to store the output into, assumed not
    * <code>null</code>.
    * @param   source a reader with the source(XSL data), assumed not
    * <code>null</code>.
    * @param   errorLine the error line number
    */
   private static void getExceptionContextData(
         StringBuffer buf, BufferedReader source, int errorLine)
   {
      if (errorLine > 0)
      {
         String curLine;
         try
         {
            for (int i = 0; i <= errorLine;)
            {
               i++;
               curLine = source.readLine();

               if (i >= (errorLine - 1))
                  buf.append(curLine);
            }
         }
         catch (IOException ioe)
         {
            buf.append(ioe.toString());
         }
      }
   }
}

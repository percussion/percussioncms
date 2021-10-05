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

package com.percussion.server;

import com.percussion.content.PSContentFactory;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * The PSFileRequestHandler class is used to get files from the file
 * system. This is primarily used for getting style sheets, etc.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSFileRequestHandler implements IPSRequestHandler,
   IPSInternalRequestHandler
{

   /**
    * Construct a file handler.
    *
    * @param baseDirectory the directory within which all files must
    * reside; <code>null</code> for the current
    * directory
    **/
   public PSFileRequestHandler(String baseDirectory)
   {
      super();
      File root = PSServer.getRxDir();
      if (baseDirectory != null)
         m_baseDirectory = new File(root, baseDirectory);
      else
      m_baseDirectory = root;
   }

   /**
    * Can the specified request be handled by this handler?
    *
    * @param request the request to check
    * @return <code>true</code> if it can
    */
    public boolean isValidFile(PSRequest request)
    {
      /*
       * Note: we always check for the file with extension first. If not found,
       * we look for the one with extension. This behavior may produce a rare
       * and funny behavior in that user may get served a page with extension
       * when he actually asked for the one without extension. Similarly, he
       * may get a page without extension when he actually asked for that with
       * extension depending on which of these (or both of these) exist in the
       * file system.
       */
      File f = new File(m_baseDirectory, request.getRequestPage());
      if (!f.exists())
         f = new File(m_baseDirectory, request.getRequestPage(false));
      return f.exists();
    }


   /* ************ IPSRequestHandler Interface Implementation ************ */
   /**
    * Process a file request using the input context information and data.
    * The results will be written to the specified output stream.
    * <p>
    * The following steps are performed to handle the request:
    * <ol>
    * <li>read the file from disk</li>
    * <li>set the content type based upon file extension</li>
    * <li>set the file as the content on the response</li>
    * </ol>
    * If the request type indicated in the supplied <code>request</code> is
    * a HEAD request, the contents of the file are omitted.
    *
    * @param request the request object containing all context data associated
    * with the request
    *
    * @todo Currently we validate security above this level, but we may want
    * to do some kind of lower-level (file level) security in addition
    */
   public void processRequest(PSRequest request)
   {
      FileInputStream fin = null;
      try
      {
         PSResponse resp = request.getResponse();
         /*
          * Note: Look for the idiosyncratic behavior explained in the method
          * isValidFile() in this class.
          */
         File f = new File(m_baseDirectory, request.getRequestPage());
         if (!f.exists())
         {
            f = new File(m_baseDirectory, request.getRequestPage(false));
            if (!f.exists())
            {
               resp.setStatus(IPSHttpErrors.HTTP_NOT_FOUND);
               return;
            }
         }
         // TODO: currently we validate security above this level, but we may
         // want to do some kind of lower-level (file level) security in
         // addition

         String lastMod = request.getServletRequest().getHeader("If-Modified-Since");
         if ((lastMod != null) && (lastMod.length() != 0))
         {
            try
            {
               Date modDate = PSResponse.parseDateFromHeader(lastMod);

               //As lastMod date from request doesn't have milli seconds
               //ignore milliseconds of file modified time and compare
               Date fileLastModDate = new Date(f.lastModified()) ;
               Calendar calendar = Calendar.getInstance();
               calendar.setTime(fileLastModDate);
               calendar.set(Calendar.MILLISECOND, 0);
               fileLastModDate = calendar.getTime();

               if(fileLastModDate.getTime() <= modDate.getTime())
               {
                  resp.setStatus(IPSHttpErrors.HTTP_NOT_MODIFIED);
                  return;
               }
            }
            catch ( ParseException e)
            {
               /* ignore the conditional check and return the file */
            }
         }

         InputStream is;
         if (request.getServletRequest().getMethod()
               .equalsIgnoreCase("head"))
         {
            byte[] buf = new byte[0];
            is = new ByteArrayInputStream(buf);
         }
         else
         {
            is = new FileInputStream(f);
         }

         // set the last modified time header for this file
         resp.setHeader(PSResponse.EHDR_LAST_MOD,
         PSResponse.formatDateForHeader(new Date(f.lastModified())));

         // now write the file to the requestor

         /* JLS Fix for Rx-00-09-0037
            specify no charset spec since now we write it
            without adding any encoding info
          */

         resp.setContent(is, f.length(), getTypeFromExtension(f), false);
         fin = null;
      }
      catch (Exception e)
      {
         PSConsole.printMsg("Server",
            "PSFileRequest Could not process file request " +
            m_baseDirectory + File.separator + request.getRequestPage() ,
            new String[] { e.toString() } );
      }
      finally
      {
         if (fin != null)
            try { fin.close(); } catch (IOException e) { }
      }
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }

   // see IPSInternalRequestHandler interface for description
   public PSExecutionData makeInternalRequest(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      throw new UnsupportedOperationException("Not supported for file requests!");
   }

   // see IPSInternalRequestHandler interface for description
   public Document getMergedResultDocument(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      throw new UnsupportedOperationException("Not supported for file requests!");
   }

   // see IPSInternalRequestHandler interface for description
   public Document getResultDocument(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      Document resultDoc = null;
      FileInputStream is = null;
      try
      {
         File f = new File(m_baseDirectory, request.getRequestPage());
         if (!f.exists())
            throw new PSInternalRequestCallException(
               IPSServerErrors.APP_FILE_DOES_NOT_EXIST, f.toString());

         // test if this is an XML file
         is = new FileInputStream(f);
         byte[] buffer = new byte[XML_HEADER.length()];
         is.read(buffer);
         String xmlHeader = new String(buffer);
         if (!xmlHeader.equals(XML_HEADER))
            throw new PSInternalRequestCallException(
               IPSServerErrors.APP_ONLY_XML_FILES_SUPPORTED, f.getName());

         // reset the input buffer and create the document
         is.close();
         is = new FileInputStream(f);
         resultDoc = PSXmlDocumentBuilder.createXmlDocument(is, false);
      }
      catch (IOException e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
            e.getLocalizedMessage());
      }
      catch (SAXException e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
            e.getLocalizedMessage());
      }
      finally
      {
         if (is != null)
            try { is.close(); } catch (IOException e) { /* ignore */ }

         return resultDoc;
      }
   }

   /**
    * Returns <code>IPSInternalRequest.REQUEST_TYPE_FILE_SYSYSTEM</code>.
    *
    * see {@link com.percussion.data.IPSInternalRequestHandler#getRequestType()}
    * for details.
    */
   public int getRequestType()
   {
      return IPSInternalRequest.REQUEST_TYPE_FILE_SYSYSTEM;
   }

   private String getTypeFromExtension(File f)
   {
      return PSContentFactory.guessMimeType(f, "application/octet-stream");
   }
   
   public boolean isBinary(PSRequest req)
   {
      throw new UnsupportedOperationException("No implemented");
   }

   /* Base directory */
   private File m_baseDirectory;

   /**
    * The XML header found in every well-formed XML file.
    */
   private static final String XML_HEADER = "<?xml";
}


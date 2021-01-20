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

import com.percussion.tools.PSHttpRequest;
import com.percussion.tools.PrintNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class encapsulates the application's snapshot DOM document as a whole.
 * All the element and attribute names in the content list XML document are
 * defined as static final constants. This class also includes methods for
 * loading, refreshing and saving he application as a snapshot document.
 *
 */
public class PSFUDApplication
{
   /**
    * Constructor. Loads the snapshot document on construction.
    */
   public PSFUDApplication()
   {
      loadSnapshot();
   }

   /**
    * return DOM element encapsulated by this node
    *
    * @return DOM Element, never <code>null</code>
    *
    */
   public Element getElement()
   {
      return m_Element;
   }

   /**
    * Sets status code in the status child DOM element of the current DOM
    * element. If the code is not parseable as String a default status code
    * is set.
    *
    * @param code as int.
    *
    * @see IPSFUDNode for status constants
    *
    */
   private void setStatusCode(int code)
   {
      if(null == m_ElementStatus)
         m_ElementStatus = PSFUDDocMerger.createChildElement(m_Element,
                              IPSFUDNode.ELEM_STATUS);

      if(null == m_ElementStatus) //never happens
         return;

      String tmp = null;
      try
      {
         tmp = Integer.toString(IPSFUDNode.STATUS_CODE_NORMAL); //default value
         tmp = Integer.toString(code);
      }
      catch(NumberFormatException e)
      {
         if(null == tmp) //should never happen
            tmp = "";
      }
      m_ElementStatus.setAttribute(IPSFUDNode.ATTRIB_CODE, tmp);
   }

   /**
    * Returns true if server was not unreachable in the last attempt.
    *
    */
   public boolean isOffline()
   {
      return m_bOffline;
   }

   /**
    * Loads the snapshot document. Snapshot document is the local copy of the
    * content list metadata XML document obtained from the server with
    * additional XML fields to indicate current state of each element. If load
    * fails snapshot, document shall be null.
    */
   private void loadSnapshot()
   {
      m_snapshotDoc = null;
      m_Element = null;
      m_ElementStatus = null;
      m_bOffline = false;
      try
      {
         File snapshot = new File(MainFrame.getConfig().getUserPath(),
                           SNAPSHOTFILE);
         if(snapshot.exists())
         {
            DocumentBuilder db = RXFileTracker.getDocumentBuilder();
            m_snapshotDoc = db.parse(snapshot.getPath());
            m_Element = m_snapshotDoc.getDocumentElement();
         }
      }
      catch(IOException ioe)
      {
         //do nothing
      }
      catch(SAXException se)
      {
         se.printStackTrace();
      }
   }

   /**
    * loads the content list document from the remote sever using the URL in
    * the configuration file.
    *
    * @throws  PSFUDAuthenticationFailureException when HTTP authentication
               fails while loading content item list metadata document with
               current userid and password.
    *
    * @throws  PSFUDServerException when HTTP request returns any other
    *          (than authentication) status code.
    *
    */
   public Document loadRemote()
      throws
         PSFUDAuthenticationFailureException,
         PSFUDServerException
   {
      URL urlQuery = null;
        InputStream content = null;
      int nPort = MainFrame.getConfig().getPort();

      String sHost = MainFrame.getConfig().getHost();
      if(nPort > 0)
         sHost += ":" + Integer.toString(nPort);

      try
      {
         if( nPort > 0 )
            urlQuery = new URL(
                        MainFrame.getConfig().getProtocol(),
                        MainFrame.getConfig().getHost(), nPort,
                        MainFrame.getConfig().getContentListURL());
         else
            urlQuery = new URL(
                        MainFrame.getConfig().getProtocol(),
                        MainFrame.getConfig().getHost(),
                        MainFrame.getConfig().getContentListURL());

         MainFrame.setStatus(MessageFormat.format(MainFrame.getRes().getString(
            "statusFetchingContentList"), new String[]{urlQuery.toString()}));

         PSHttpRequest httpRequest = new PSHttpRequest(urlQuery);
         httpRequest.addRequestHeader( "HOST", sHost);
         httpRequest.addRequestHeader( "USER_AGENT", HTTP_USERAGENT);
         httpRequest.sendRequest();

         int nStatus = httpRequest.getResponseCode();
         if(nStatus == MainFrame.getConfig().
                           HTTP_STATUS_BASIC_AUTHENTICATION_FAILED)
         {
            //Authentication failure means Authentication required in future
            MainFrame.getConfig().setIsAuthenticationRequired(true);

            httpRequest.addRequestHeader("Authorization","Basic " +
                        MainFrame.getConfig().getEncryptedUseridPassword());
            httpRequest.sendRequest();

            nStatus = httpRequest.getResponseCode();
            if(nStatus == MainFrame.getConfig().
                              HTTP_STATUS_BASIC_AUTHENTICATION_FAILED)
            {
               String sError = MessageFormat.format(MainFrame.getRes().
                  getString("errorHTTPAuthentication"), new String[]{Integer.
                  toString(nStatus), urlQuery.toString()});

               throw new PSFUDAuthenticationFailureException(sError);
            }
         }

         // here we don't check for the status range!!!
         if(nStatus != MainFrame.getConfig().HTTP_STATUS_OK)
         {
            String sError = MessageFormat.format(MainFrame.getRes().getString(
               "errorHTTP"), new String[]{Integer.
               toString(nStatus), urlQuery.toString()});

            throw new PSFUDServerException(sError);
         }
         content = httpRequest.getResponseContent();

         DocumentBuilder db = RXFileTracker.getDocumentBuilder();
         Document doc = null;
         doc = db.parse(new InputSource(content));
         content.close();
         return doc;
      }
      catch(IOException e)
      {
         throw new PSFUDServerException(e.getMessage());
      }
      catch(SAXException e)
      {
         throw new PSFUDServerException(e.getMessage());
      }
   }

   /**
    * Called by refresh method to merge the remote document just load with the
    * snapshot douemnt. Simply delegates the process to PSFUDDocMerger object.
    *
    * @param remote document as DOM Document can be <code>null</code>, however
    *        doc meger will throw exception in that case.
    *
    * @see PSFUDDocMerger
    *
    */
   private void merge(Document doc)
      throws   PSFUDNullDocumentsException,
               PSFUDMergeDocumentsException
   {
      PSFUDDocMerger merger = new PSFUDDocMerger(m_snapshotDoc, doc);
      merger.merge(this);
   }

   /**
    * Merges the remote content item list metdata XML document with the
    * snapshot. If snapshot document is null then remote XML doc becomes
    * the snapshot;
    *
    * @param   remote content item list metadata as DOM Document, can be
    *          <code>null</code>.
    *
    */
   public void refresh(Document remoteDoc)
      throws   PSFUDNullDocumentsException,
               PSFUDMergeDocumentsException
   {
      loadSnapshot();
      m_bOffline = false;
      if(null == remoteDoc)
      {
         if(null == m_snapshotDoc)
         {
            throw new PSFUDNullDocumentsException();
         }
         else
         {
            m_bOffline = true;
            setStatusCode(IPSFUDNode.STATUS_CODE_ABSENT);
            return;
         }
      }
      setStatusCode(IPSFUDNode.STATUS_CODE_NORMAL);

      //local snapshot is not available, remote is assigned to snapshot
      if(null == m_snapshotDoc)
      {
         m_snapshotDoc = remoteDoc;
      }
      //snapshot and remote are merged
      else
      {
         merge(remoteDoc);
      }
      try
      {
         save();
      }
      catch(IOException e)
      {
         throw new PSFUDMergeDocumentsException(e.getMessage());
      }
      m_Element = m_snapshotDoc.getDocumentElement();
   }

   /**
    * Saves the snapshot XML Document locally to SNAPSHOTFILE. The path is
    * calculated as $current/serveralias/userid/SNAPSHOTFILE, where $current is
    * the current workign directory for the application.
    *
    * @throws IOException if save fails
    *
    */
   public void save()
      throws IOException
   {
      File file = new File(MainFrame.getConfig().getUserPath(), SNAPSHOTFILE);
      if(null != file.getParentFile())
         file.getParentFile().mkdirs();

      FileOutputStream ostream = new FileOutputStream(file);
      OutputStreamWriter fw = new OutputStreamWriter(ostream, "UTF8");
      PrintNode.printNode(m_snapshotDoc, " ", fw);
      fw.flush();
      fw.close();
   }

   /**
    * Snapshot DOM document. A snapshot document is an DOM document holding all
    * the information from the remote application document (which is another
    * DOM Document provided by Rx server) plus the state information for all
    * the DOM elements that are encapsulated by the IPSFUDNodes.
    *<p>
    * Shall never be null once user closes the connect dialog box.
    */
   private Document m_snapshotDoc = null;

   /**
    * Application node element encapsulated by this class.
    */
   private Element m_Element = null;

   /*
    * Status child element of the application DOM node.
    */
   private Element m_ElementStatus = null;

   /**
    * Flag to set offline status, set to <code>true</code> when remote load
    * fails.
    */
   private boolean m_bOffline = false;

   /**
    * Snapshot file name
    */
   public static final String SNAPSHOTFILE = "snapshot.xml";

   /**
    * HTTP header string for user agent. This will always be the application
    * title appended with version string.
    */
   public static final String HTTP_USERAGENT = MainFrame.APPTITLE + "; " +
                                 MainFrame.getVersionString();

   /**
    * The element and attribute names in the content list document based on the
    * DTD
    */
   public static final String ELEM_APPLICATION = "psrxfudapplication";
   public static final String ELEM_CATEGORY = "category";
   public static final String ELEM_CONTENTITEM = "contentitem";
   public static final String ELEM_FILE = "file";
   public static final String ELEM_DOWNLOADURL = "downloadurl";
   public static final String ELEM_UPLOADURL = "uploadurl";
   public static final String ELEM_TIMESTAMP = "timestamp";
   public static final String ELEM_MODIFIED = "modified";
   public static final String ELEM_SIZE = "size";

   public static final String ATTRIB_CATEGORYID = "categoryid";
   public static final String ATTRIB_CONTENTID = "contentid";
   public static final String ATTRIB_NAME = "name";
   public static final String ATTRIB_TITLE = "title";
   public static final String ATTRIB_SIZE = "size";
   public static final String ATTRIB_MODIFIED = "modified";
   public static final String ATTRIB_MIMETYPE = "mimetype";
   public static final String ATTRIB_REMOTE = "remote";
   public static final String ATTRIB_LOCAL = "local";

}




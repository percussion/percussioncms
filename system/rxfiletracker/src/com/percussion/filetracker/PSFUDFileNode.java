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

import com.percussion.tools.PSCopyStream;
import com.percussion.tools.PSHttpRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.SequenceInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class wraps the file node in the application and extends
 * the abstract class PSFUDAbstractNode. A file node can have various states,
 * e.g. normal, remote absent, local new, remote new etc.
 *
 * @see IPSFUDNode for node states.
 *
 */
public class PSFUDFileNode extends PSFUDAbstractNode
{
   /**
    * Constructor takes the parent node and current element that is
    * encapsulated by this class. Validation of these parameters is handled
    * by the base class.
    *
    * @param parent node as IPSFUDNode
    *
    * @param content item element in the XML document as DOM Element
    *
    * @throws PSFUDNullElementException that is thrown by the base class
    *
    */
   public PSFUDFileNode(IPSFUDNode parent, Element elem)
      throws PSFUDNullElementException
   {
      super(parent, elem);

      Node node = m_Element.getParentNode();
      //Evaluate local path for the file and create a File object
      if(node instanceof Element)
      {
         Element contentItem = (Element)node;
         m_LocalPath = MainFrame.getConfig().getUserPath();
         String temp = contentItem.getAttribute(
            PSFUDApplication.ATTRIB_CONTENTID);
         if(null != temp && temp.length() > 0)
            m_LocalPath += File.separator + temp;
         temp = this.toString();
         if(null != temp && temp.length() > 0)
            m_LocalPath += File.separator + temp;

         m_LocalFile = new File(m_LocalPath);
      }
      m_ElementTimestamp = PSFUDDocMerger.createChildElement(m_Element,
                           PSFUDApplication.ELEM_TIMESTAMP);
   }

   /**
    * This method is part of IPSFUDNode interface. A PSFUDFileNode is always
    * a leaf node and hence has no children.
    *
    * @return always null.
    *
    */
   public Object[] getChildren()
   {
      //File Node is always leaf node, no children
      return null;
   }

   /**
    * Returns current status code for the node.
    *
    * @return status code as int
    *
    * @see IPSFUDNode class for status values
    *
    */
   public int getStatusCode()
   {
      if(null != m_Parent && !m_Parent.isRemoteExists())
         return STATUS_CODE_ABSENT;

      int code = this.STATUS_CODE_NORMAL;
      if(null  == m_ElementStatus)
         return code;
      String tmp = m_ElementStatus.getAttribute(IPSFUDNode.ATTRIB_CODE);
      if(tmp.trim().length() > 1)
      {
         try
         {
            code = Integer.parseInt(tmp);
         }
         catch(NumberFormatException e)
         {
            return code;
         }
      }

      if(IPSFUDNode.STATUS_CODE_ABSENT == code)
         return code;

      //check if remote is modified
      String remote1 = m_ElementTimestamp.
         getAttribute(PSFUDApplication.ATTRIB_REMOTE);

      String remote2 = m_Element.getAttribute(PSFUDApplication.ATTRIB_MODIFIED);
      if(null != remote1 &&
         remote1.trim().length() > 0 &&
         !remote1.equals(remote2))
      {
         code = IPSFUDNode.STATUS_CODE_REMOTENEW;
         m_ElementStatus.setAttribute(IPSFUDNode.ATTRIB_CODE,
            Integer.toString(code));

         return code;
      }
      //check if local is modified
      if(!m_LocalFile.exists())
      {
         code = IPSFUDNode.STATUS_CODE_NORMAL;
         m_ElementStatus.setAttribute(IPSFUDNode.ATTRIB_CODE,
            Integer.toString(code));

         return code;
      }
      String local1 = m_ElementTimestamp.getAttribute(
         PSFUDApplication.ATTRIB_LOCAL);
      long date1 = 0;
      try
      {
         date1 = Long.parseLong(local1);
      }
      catch(NumberFormatException e){}
      long date2 = m_LocalFile.lastModified();
      if(Math.abs(date1 - date2) > 1000L) //more than a second
      {
         code = IPSFUDNode.STATUS_CODE_LOCALNEW;
         m_ElementStatus.setAttribute(IPSFUDNode.ATTRIB_CODE,
            Integer.toString(code));

         return code;
      }
      code = IPSFUDNode.STATUS_CODE_INSYNC;
      m_ElementStatus.setAttribute(IPSFUDNode.ATTRIB_CODE,
         Integer.toString(code));

      return  code;
   }

   /**
    * Returns the the string to be used to display this leaf in the JTree.
    *
    * @return string representation of the node for display as String
    *
    */
   public String toString()
   {
      //return just the file name
      return m_Element.getAttribute(PSFUDApplication.ATTRIB_NAME).trim();
   }

   /**
    * Returns the download URL
    *
    * @return download URL as String - never <code>null</code>, may be empty.
    *
    */
   public String getDownloadURL()
   {
      NodeList nl = m_Element.getElementsByTagName(
         PSFUDApplication.ELEM_DOWNLOADURL);
      if(null == nl || nl.getLength() < 1)
         return "";

      Element elem = (Element)nl.item(0);
      Node node = elem.getFirstChild();
      if(node instanceof Text)
         return ((Text)node).getData();
      else
         return "";
   }

   /**
    * Returns the upload URL
    *
    * @return upload URL as String - never <code>null</code>, may be empty.
    *
    */
   public String getUploadURL()
   {
      NodeList nl = m_Element.getElementsByTagName(
         PSFUDApplication.ELEM_UPLOADURL);
      if(null == nl || nl.getLength() < 1)
         return "";

      Element elem = (Element)nl.item(0);
      Node node = elem.getFirstChild();
      if(node instanceof Text)
         return ((Text)node).getData();
      else
         return "";
   }

   /**
    * Returns file size in bytes as String
    *
    * @return file size in bytes as String - never <code>null</code>,
    * default is "0".
    *
    */
   public String getSize()
   {
      if(m_LocalFile.exists())
      {
         try
         {
            return Long.toString(m_LocalFile.length());
         }
         catch(NumberFormatException e)
         {
            //conversion fails, size is 0
            e.printStackTrace();
            return "0";
         }
      }
      return m_Element.getAttribute(PSFUDApplication.ATTRIB_SIZE);
   }

   /**
    * Returns file modified date as String
    *
    * @return modified date as String - never <code>null</code>, may be empty.
    *
    */
   public String getModified()
   {
      if(m_LocalFile.exists())
      {
         return ms_DateFormat.format(new Date(m_LocalFile.lastModified()));
      }
      return m_Element.getAttribute(PSFUDApplication.ATTRIB_MODIFIED);
   }

   /**
    * Returns mime type as String
    *
    * @return mime type as String - never <code>null</code>, may be empty.
    *
    */
   public String getMimeType()
   {
      return m_Element.getAttribute(PSFUDApplication.ATTRIB_MIMETYPE);
   }

   /**
    * returns true if the copy of remote file is present locally else returns
    * false.
    *
    *  @return true if local copy present else false
    */
   public boolean isLocalCopy()
   {
      if(m_LocalFile.exists())
         return true;

      return false;
   }

   /**
    * Downloads the remote file to local machine. The path saved will be
    * $current/serveralias/userid/contentid/filename, where $current is
    * the current workign directory for the application.
    *
    * @throws  PSFUDAuthenticationFailureException when HTTP authentication
    *          failswhile loading content item list metadata document with
    *          current userid and password.
    *
    * @throws  PSFUDServerException when HTTP request returns any other
    *          (than authentication) status code.
    *
    */
   public void download()
      throws
         IOException,
         MalformedURLException,
         PSFUDServerException,
         PSFUDAuthenticationFailureException
   {
      URL urlQuery = null;
      InputStream content = null;
      int nPort = MainFrame.getConfig().getPort();
      String urlString = getDownloadURL();
      String sHost = MainFrame.getConfig().getHost();
      if(nPort > 0)
         sHost += ":" + Integer.toString(nPort);
      if(urlString.toLowerCase().startsWith("http://") ||
         urlString.toLowerCase().startsWith("https://")) //Absolute URL specified
      {
         urlQuery = new URL(urlString);
      }
      else
      {
         if( nPort > 0 )
            urlQuery = new URL(MainFrame.getConfig().getProtocol(),
                        MainFrame.getConfig().getHost(), nPort, urlString);
         else
            urlQuery = new URL(MainFrame.getConfig().getProtocol(),
                        MainFrame.getConfig().getHost(), urlString);
      }

      PSHttpRequest httpRequest = new PSHttpRequest(urlQuery);
      httpRequest.addRequestHeader( "HOST", sHost);
      httpRequest.addRequestHeader( "USER_AGENT",
         PSFUDApplication.HTTP_USERAGENT);
      if(MainFrame.getConfig().getIsAuthenticationRequired())
      {
         httpRequest.addRequestHeader("Authorization","Basic " +
                     MainFrame.getConfig().getEncryptedUseridPassword());
      }
      httpRequest.sendRequest();
      int nStatus = httpRequest.getResponseCode();
      if(nStatus == MainFrame.getConfig().
                     HTTP_STATUS_BASIC_AUTHENTICATION_FAILED)
      {
         String sError = MessageFormat.format(MainFrame.getRes().getString(
            "errorHTTPAuthentication"), new String[]{Integer.
            toString(nStatus), urlQuery.toString()});

         throw new PSFUDAuthenticationFailureException(sError);
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

      File parent = m_LocalFile.getParentFile();
      if(null != parent)
         parent.mkdirs();

      FileOutputStream fos = new FileOutputStream(m_LocalFile);

      PSCopyStream.copyStream(content, fos);

      fos.flush();
      fos.close();

      updateTimestamp(); //make sure you do this.
      getStatusCode(); //update status code
   }

   /**
    * Uploads the local file to server. HTTP Headers are set as per the
    * requirements outlined in RFC 1867 for uploading a file using HTTP POST.
    *
    * @throws IOException if downloaded file cannot be saved.
    *
    * @throws PSFUDAuthenticationFailureException when we receive HTTP
    *         authentication failure from the server.
    *
    * @throws PSFUDServerException when we receive any other HTTP error from
    *         the server
    */
   public void upload()
      throws
         IOException,
         MalformedURLException,
         PSFUDServerException,
         PSFUDAuthenticationFailureException
   {
      FileInputStream fis = new FileInputStream(m_LocalFile);
      int nPort = MainFrame.getConfig().getPort();
      URL postURL = null;
      String urlString = getUploadURL();
      String sHost = MainFrame.getConfig().getHost();
      if(nPort > 0)
         sHost += ":" + Integer.toString(nPort);
      try
      {
         if(urlString.toLowerCase().startsWith("http://") ||
            urlString.toLowerCase().startsWith("https://")) //Absolute URL specified
         {
            postURL = new URL(urlString);
         }
         else
         {
            if( nPort > 0 )
               postURL = new URL(MainFrame.getConfig().getProtocol(),
                              MainFrame.getConfig().getHost(), nPort, urlString);
            else
               postURL = new URL(MainFrame.getConfig().getProtocol(),
                              MainFrame.getConfig().getHost(), urlString);
         }

         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         PrintStream ps = new PrintStream(bos);
         ps.println(); //blank line

         ps.println(UPLOAD_BOUNDARY);
         ps.println("Content-Disposition: form-data; name=\"contentbody\"; " +
            "filename=\"" + getFileName() + "\"");
         ps.println("Content-Type: " + getMimeType());
         ps.println(); //blank line

         SequenceInputStream sis = new SequenceInputStream(
            new ByteArrayInputStream(bos.toByteArray()), fis);
         bos = new ByteArrayOutputStream();
         PSCopyStream.copyStream(sis, bos);
         ps = new PrintStream(bos);

         ps.println(); //blank line
         ps.println(UPLOAD_BOUNDARY);
         ps.println("Content-Disposition: form-data; name=\"DBActionType\"");
         ps.println(); //blank line
         ps.println("UPDATE");

         ps.println(UPLOAD_BOUNDARY + "--"); //end
         PSHttpRequest postRequest  = new PSHttpRequest(postURL.toString(),
                        "POST", new ByteArrayInputStream(bos.toByteArray()));
         postRequest.addRequestHeader( "Content-Type",
                        "multipart/form-data; boundary=" + UPLOAD_BOUNDARYX);
         postRequest.addRequestHeader( "HOST", sHost);
         postRequest.addRequestHeader( "REFERER", postURL.toString());
         if(MainFrame.getConfig().getIsAuthenticationRequired())
         {
            postRequest.addRequestHeader("Authorization","Basic " +
                        MainFrame.getConfig().getEncryptedUseridPassword());
         }
         postRequest.addRequestHeader( "USER_AGENT",
            PSFUDApplication.HTTP_USERAGENT);
         postRequest.addRequestHeader( "Content-Length",
                        Integer.toString(bos.size()));
         postRequest.sendRequest();

         int nStatus = postRequest.getResponseCode();
         if(nStatus == MainFrame.getConfig().
                        HTTP_STATUS_BASIC_AUTHENTICATION_FAILED)
         {
            String sError = MessageFormat.format(MainFrame.getRes().getString(
               "errorHTTPAuthentication"), new String[]{Integer.
               toString(nStatus), postURL.toString()});

            throw new PSFUDAuthenticationFailureException(sError);
         }

         if(MainFrame.getConfig().HTTP_STATUS_OK != nStatus)
         {
            String sError = MessageFormat.format(MainFrame.getRes().getString(
               "errorHTTP"), new String[]{Integer.
               toString(nStatus), postURL.toString()});

            throw new PSFUDServerException(sError);
         }

         InputStream content = postRequest.getResponseContent();
         if(null != content)
         {
            try
            {
               DocumentBuilder db = RXFileTracker.getDocumentBuilder();
               Document doc = null;
               Element elem = null;
               Node node = null;
               NodeList nl = null;

               doc = db.parse(new InputSource(content));
               if (doc != null)
                     nl = doc.getElementsByTagName(PSFUDApplication.ELEM_SIZE);
               if(null != nl && nl.getLength() > 0)
               {
                  elem = (Element)nl.item(0);
                  node = elem.getFirstChild();
                  if(node instanceof Text)
                     m_Element.setAttribute(PSFUDApplication.ATTRIB_SIZE,
                        ((Text)node).getData());
               }
               if (doc != null)
                  nl = doc.getElementsByTagName(PSFUDApplication.ELEM_MODIFIED);
               if(null != nl && nl.getLength() > 0)
               {
                  elem = (Element)nl.item(0);
                  node = elem.getFirstChild();
                  if(node instanceof Text)
                     m_Element.setAttribute(PSFUDApplication.ATTRIB_MODIFIED,
                        ((Text)node).getData());
               }
               this.updateTimestamp();
            }
            // status  document (as InputStream) received from the server is
            // not parseable
            catch(SAXException e)
            {
               throw new PSFUDServerException(e.getMessage());
            }
         }
      }
      finally
      {
         if(null != fis)
         {
            fis.close();
         }
      }
   }

   /**
    * Launches the local file with associated program. A windows message box
    * is displayed if launch fails or if no program is associated with this
    * type of file.
    *
    */
   public void launch()
   {
      if(m_LocalFile.exists())
         UTBrowserControl.displayURL(m_LocalFile.getAbsolutePath());
   }

   /**
    * Deletes the local file.
    *
    * @return true if successful false otherwise.
    *
    * throws SecurityException if current user has no delete access forthe file.
    *
    */
   public boolean purgeLocal() throws SecurityException
   {
      if(!m_LocalFile.exists())
         return true;

      if(m_LocalFile.delete())
      {
         m_ElementTimestamp.removeAttribute(PSFUDApplication.ATTRIB_REMOTE);
         m_ElementTimestamp.removeAttribute(PSFUDApplication.ATTRIB_LOCAL);
          return true;
      }
      return false;
   }

   /**
    * Get the local file path.
    *
    * @return full path as String,  can be empty string but never
    *         <code>null</code>.
    *
    */
   public String  getLocalPath()
   {
      if(m_LocalFile.exists())
         return m_LocalFile.getAbsolutePath();

      return "";
   }

   /**
    * Get the file name,  can be empty string but never
    *         <code>null</code>.
    *
    * @return file name as as String
    *
    */
   public String  getFileName()
   {
      if(m_LocalFile.exists())
         return m_LocalFile.getName();

      return "";
   }

   /**
    * Resets or updates the timestamps for the timestamp child element of the
    * DOM element encapsulated by this class. The modified date from the
    * element is copied to that in the time stamp element. Current modified
    * date for the local disk file is copied to time stamp element. See the
    * DTD in the spec for the element and attribute names holding the
    * timestamp elements.
    */
   public void updateTimestamp()
   {
      m_ElementTimestamp.setAttribute(PSFUDApplication.ATTRIB_REMOTE,
                  m_Element.getAttribute(PSFUDApplication.ATTRIB_MODIFIED));

      long date = m_LocalFile.lastModified();
      String sDate = "0";
      try{ sDate = Long.toString(date); }catch(Exception e){}

      m_ElementTimestamp.setAttribute(PSFUDApplication.ATTRIB_LOCAL, sDate);
   }

   /**
    * Reference to time stamp element that is child of current element.
    * Stored since this required very frequently hence avoiding walking through
    * evrytime. Shall never be null since we create one in the constructor if
    * does not exist.
    */
   private Element m_ElementTimestamp = null;

   /**
    * Local file path obtained in the constructor from the configuration
    * document.
    * Never be null since this an attribute of an element in the configuration
    * document.
    */
   private String m_LocalPath = "";

   /**
    * File object for local file path obtained in the constructor, never  null
    * (even if the file does not exist) after constructor is called.
    */
   private File   m_LocalFile = null;


   /**
    * Default date format for display.
    */
   static public SimpleDateFormat ms_DateFormat =
                  new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");


   /**
    * Boundary string used in the header for the upload post request. This is
    * like a signature we register in the header.
    */
   static public final String UPLOAD_BOUNDARYX ="rxfudboundary";

   /**
    * Boundary string used in the upload post request. This is used as
    * separator for each block of data we upload to the server. This is the
    * same as UPLOAD_BOUNDARYX prefixed with '--'. Defined for convenience.
    */
   static public final String UPLOAD_BOUNDARY ="--rxfudboundary";
}







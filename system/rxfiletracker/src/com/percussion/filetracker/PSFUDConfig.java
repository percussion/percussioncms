/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.filetracker;

import com.percussion.tools.Base64;
import com.percussion.tools.PrintNode;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class wraps the configuration document (rxftconfig.xml). Has required
 * set and get methods for parameters such as userid, password etc. No
 * parameter is stored locally and obtioned walking through the XML document on
 * demand except the password. Password is never stored part of the document.
 * Also has save() method that saves the config document, again password is
 * never saved as part of the file.
 */
public class PSFUDConfig
{
   /**
    * Default and constructor. Configuration file is loaded first. If load
    * succeeds, current element is obtained walking throw the socument. Current
    * element is created if one does not exist. Current element is the one that
    * stores the last connected server alias and userid.
    *
    * @throws PSFUDConfigFileNotFoundException if the configuartion file not
    *         found.
    *
    * @throws SAXException if the loaded XML document is not parsable.
    *
    * @throws IOException if load fails for other reason.
    *
    */
   public PSFUDConfig()
      throws   PSFUDConfigFileNotFoundException,
               IOException, SAXException
   {
      File file = new File(FUDCONFIGFILE);
      if(!file.exists())
         throw new PSFUDConfigFileNotFoundException();

      DocumentBuilder db = RXFileTracker.getDocumentBuilder();
      try(FileInputStream fi = new FileInputStream(file)) {
         m_ConfigDoc = db.parse(new InputSource(fi));
         if (m_ConfigDoc != null) {
            m_ElementCurrent = PSFUDDocMerger.createChildElement(
                    m_ConfigDoc.getDocumentElement(), ELEM_CURRENT);
         }
      }
   }


   /**
    * Saves the configuration file
    *
    * @throws IOException if save fails.
    *
    */
   public void save()
      throws IOException
   {
      FileWriter fw = new FileWriter(FUDCONFIGFILE);
      PrintNode.printNode(m_ConfigDoc, " ", fw);
      fw.flush();
      fw.close();
   }

   /**
    * Get method for server alias
    *
    * @return serveralias as Stirng, never by <code>null</code>, can be empty.
    *
    */
   public String getServerAlias()
   {
      return m_ElementCurrent.getAttribute(ATTRIB_SERVERALIAS);
   }

   /**
    * Get method for userid
    *
    * @return userid as Stirng, never by <code>null</code>, can be empty.
    *
    */
   public String getUserid()
   {
      return m_ElementCurrent.getAttribute(ATTRIB_USERID);
   }

   /**
    * Get method for user path. User path is calculated as
    * $current/serveralias/userid, where $current is the current working
    * directory. The snapshot file is always saved to user path.
    *
    * @return userpath as Stirng, never <code>null</code>
    *
    */
   public String getUserPath()
   {
      return m_ElementCurrent.getAttribute(ATTRIB_SERVERALIAS) +
               File.separator +
               m_ElementCurrent.getAttribute(ATTRIB_USERID);
   }

   /**
    * Get method for user's unencrypted password. This will never be saved part
    * of the configuration document
    *
    * @return password as Stirng, can be empty or ,code>null</code>.
    *
    */
   public String getPassword()
   {
      return m_password;
   }

   /**
    * Get method for protocol
    *
    * @return protocol as Stirng, never <code>null</code> default is
    *         DEFAULT_PROTOCOL.
    *
    * @see #DEFAULT_PROTOCOL
    *
    */
   public String getProtocol()
   {
      NodeList nl = m_ConfigDoc.getElementsByTagName(ELEM_PROTOCOL);
      if(null==nl || nl.getLength() < 1)
      {
         return DEFAULT_PROTOCOL;
      }
      Element elem = (Element)nl.item(0);
      Node node = elem.getFirstChild();
      if(Node.TEXT_NODE != node.getNodeType())
      {
         return DEFAULT_PROTOCOL;
      }

      return ((Text)node).getData();
   }

   /**
    * Get method for host name/IP Address
    *
    * @return host as Stirng, never be <code>null</code> but can be empty.
    *
    */
   public String getHost()
   {
      return m_ElementCurrentURL.getAttribute(ATTRIB_HOST);
   }

   /**
    * Get method for port number
    *
    * @return port as int, default is 80
    *
    */
   public int getPort()
   {
      String tmp = m_ElementCurrentURL.getAttribute(ATTRIB_PORT);
      int port = 80;

      try { port = Integer.parseInt(tmp);}catch(NumberFormatException e){ }

      return port;
   }

   /**
    * Get method for content list URL
    *
    * @return URL as String, never be <code>null</code> but can be empty.
    *
    */
   public String getContentListURL()
   {
      Node node = m_ElementCurrentURL.getFirstChild();
      if(null == node || Node.TEXT_NODE != node.getNodeType())
         return "";
      return ((Text)node).getData();
   }

   /**
    * Get method to get a list of all available server aliases in the
    * configuration file
    *
    * @return  server alias list as an array of strings, never be empty or
    *          <code>null</code>.
    *
    * @throws PSFUDInvalidConfigFileException if the there are no server
               aliases defined in the document.
    *
    */
   public Object[] getServerAliases()
      throws PSFUDInvalidConfigFileException
   {
      NodeList nl = m_ConfigDoc.getElementsByTagName(ELEM_URL);
      if(null == nl || nl.getLength() < 1)
      {
         throw new PSFUDInvalidConfigFileException(
            MainFrame.getRes().getString("errorServerAliasListEmpty"));
      }

      Element elem = null;
      String[] array = new String[nl.getLength()];
      for(int i=0; i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         array[i] = elem.getAttribute(ATTRIB_SERVERALIAS);
      }

      return array;
   }

   /**
    * Set method for user's unencrypted password. This will never be saved part
    * of the configuration document
    *
    * @param password as String, always unencrypted.
    *
    */
   public void setPassword(String password)
   {
      m_password = password;
   }

   /**
    * Set method for current server alias.
    *
    * @param server alias as String, can not be <code>null>/code> or empty.
    *
    * @throws PSFUDEmptyServerAliasException if specified server alias
    *         is <code>null</code> or empty.
    *
    * @throws PSFUDInvalidConfigFileException if specified server alias
    *         is not found in the list of aliases.
    *
    */
   public void setServerAlias(String serverAlias)
      throws
      PSFUDEmptyServerAliasException,
      PSFUDInvalidConfigFileException
   {
      if(null == serverAlias || serverAlias.trim().length() < 1)
         throw new PSFUDEmptyServerAliasException(
            MainFrame.getRes().getString(ERROR_SERVER_ALIAS));

      setCurrentURL(serverAlias);
      m_ElementCurrent.setAttribute(ATTRIB_SERVERALIAS, serverAlias);
   }

   /**
    * Set method that finds and stores current URL Element
    *
    * @throws PSFUDInvalidConfigFileException if specified server alias
    *         is not found in the list of aliases.
    */
   private void setCurrentURL(String serverAlias)
      throws
      PSFUDEmptyServerAliasException,
      PSFUDInvalidConfigFileException
   {
      if(null == serverAlias || serverAlias.trim().length() < 1)
         throw new PSFUDEmptyServerAliasException(
            MainFrame.getRes().getString(ERROR_SERVER_ALIAS));

      NodeList nl = m_ConfigDoc.getElementsByTagName(ELEM_URL);
      if(null == nl || nl.getLength() < 1)
      {
         throw new PSFUDInvalidConfigFileException(
            MainFrame.getRes().getString(ERROR_SERVER_ALIAS));
      }

      Element elem = null;
      String tmp = "";
      for(int i=0; i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         tmp = elem.getAttribute(ATTRIB_SERVERALIAS);
         if(tmp.equals(serverAlias))
         {
            m_ElementCurrentURL = elem;
            break;
         }
      }
   }

   /**
    * Set method for userid
    * @param userid as String, set to empty string if <code>null</code>
    * is specified.
    *
    */
   public void setUserid(String userid)
   {
      if(null == userid)
         userid = "";

      m_ElementCurrent.setAttribute(ATTRIB_USERID, userid);
   }

   /**
    * Method to get the purge local option. The default is false.
    *
    * @return true if loacl copy is to be purged after upload else false;
    *
    */
   public boolean getIsPurgeLocalCopy()
   {
      Boolean value = (Boolean)getOption(OPTION_PURGELOCAL);

      if(null == value)
         return false;

      return value.booleanValue();
   }

   /**
    * Method to set the purge local option.
    *
    * @param bPurge as boolean
    *
    */
   public void setIsPurgeLocalCopy(boolean bPurge)
   {
      Boolean value = Boolean.valueOf(bPurge);
      setOption(OPTION_PURGELOCAL, value);
   }

   /**
    * Method to get the prompt before purging option.
    *
    * @return true if prompt required beofre purging else false. Default is
    * true.
    *
    */
   public boolean getIsPromptBeforePurge()
   {
      Boolean value = (Boolean)getOption(OPTION_PROMPTPURGE);
      if(null == value)
         return true;

      return value.booleanValue();
   }

   /**
    * Method to set the prompt before purging option.
    *
    * @param bPrompt as boolean.
    *
    */
   public void setIsPromptBeforePurge(boolean bPrompt)
   {
      Boolean value = Boolean.valueOf(bPrompt);
      setOption(OPTION_PROMPTPURGE, value);
   }

   /**
    * Method to get the prompt before overwriting file option. The default is
    * true.
    *
    * @return true if prompt required beofre overwriting, else false;
    *
    */
   public boolean getIsPromptBeforeOverwrite()
   {
      Boolean value = (Boolean)getOption(OPTION_PROMPTOVERWRITE);
      if(null == value)
         return true;

      return value.booleanValue();
   }

   /**
    * Method to set the prompt before overwriting file option.
    *
    * @param bPrompt as boolean.
    *
    */
   public void setIsPromptBeforeOverwrite(boolean bPrompt)
   {
      Boolean value = Boolean.valueOf(bPrompt);
      setOption(OPTION_PROMPTOVERWRITE, value);
   }

   /**
    * General helper function to get an option value. For now it is meant for
    * only booleans, but can be generalized in future.
    *
    * @param option code as int
    *
    * @return Option value as Object
    *
    */
   private Object getOption(int option)
   {
      String tagname = "";

      switch(option)
      {
         case OPTION_PURGELOCAL:
            tagname = ELEM_PURGELOCALOPTION;
         break;
         case OPTION_PROMPTPURGE:
            tagname = ELEM_PROMPTPURGE;
         break;
         case OPTION_PROMPTOVERWRITE:
            tagname = ELEM_PROMPTOVERWRITE;
         break;
         default:
            return null;
      }
      NodeList nl = m_ConfigDoc.getElementsByTagName(tagname);
      if(null == nl || nl.getLength() < 1)
         return Boolean.FALSE;

      Element elem = (Element)nl.item(0);
      Node node = elem.getFirstChild();
      if(node instanceof Text)
      {
         String tmp = ((Text)node).getData();
         if(tmp.equalsIgnoreCase(OPTION_YES)) {
            return Boolean.TRUE;
         }
      }
      return Boolean.FALSE;
   }

   /**
    * General helper function to set an option value. For now it is meant for
    * only booleans, but can be generalized in future.
    *
    * @param option code as int, one of the option types.
    *
    * @param value as Object, can not be <code>null</code>.
    *
    */
   private void setOption(int option, Object value)
   {
      if(null == value)
         return;

      String tagname = "";
      String temp = "";
      switch(option)
      {
         case OPTION_PURGELOCAL:
            tagname = ELEM_PURGELOCALOPTION;
            temp = OPTION_NO;
         break;
         case OPTION_PROMPTPURGE:
            tagname = ELEM_PROMPTPURGE;
            temp = OPTION_YES;
         break;
         case OPTION_PROMPTOVERWRITE:
            tagname = ELEM_PROMPTOVERWRITE;
            temp = OPTION_YES;
         break;
         default: //undefine option value
            return;
      }

      if(value instanceof Boolean)
      {
         if(((Boolean)value).booleanValue())
            temp = OPTION_YES;
         else
            temp = OPTION_NO;
      }
      NodeList nl = m_ConfigDoc.getElementsByTagName(tagname);
      Element elem = null;

      if(null == nl || nl.getLength() < 1)
      {
         elem = m_ConfigDoc.createElement(tagname);
         elem = (Element)m_ConfigDoc.getDocumentElement().appendChild(elem);
      }
      else
         elem = (Element)nl.item(0);

      Node node = elem.getFirstChild();
      if( !(node instanceof Text))
      {
         node = m_ConfigDoc.createTextNode(temp);
         elem.appendChild(node);
         return;
      }
      ((Text)node).setData(temp);
   }

   /**
   * Method to get enctrypted userid-password combo required for HTTP
   * authentication.
   *
   * @return Base64 encrypted userdid-password combo as required for the header
   *         in the request for HTTP authentication.
   */
   public String getEncryptedUseridPassword()
   {
      return Base64.encode(convertStringToByteArray(getUserid()
             + ":" + getPassword()));
   }

   /**
   * Method to see if authentication is required for Rx app.
   *
   * @return <code>true</code> if authentication was required to access the
   * Rx app, else <code>false</code>.
   */
   public boolean getIsAuthenticationRequired()
   {
      return m_bAuthenticationRequired;
   }

   /**
   * Method to set if authentication is required for Rx app.
   *
   * @param <code>true</code> if authentication was required to access the
   * Rx app, else <code>false</code>.
   */
   public void setIsAuthenticationRequired(boolean bAuthenticate)
   {
      m_bAuthenticationRequired = bAuthenticate;
   }
   /**
    * Cheap method to convert a stringto byte array. Be aware that this does
    * not use any encoding. This is used in password encryption process and
    * is sufficient.
    */
   public static byte[] convertStringToByteArray(String str)
   {
      byte[] byteArray = new byte[str.length()];
      for (int i = 0; i < str.length(); i++)
      {
         byteArray[i] = (byte)(str.charAt(i));
      }
      return byteArray;
   }

   /**
    * Default protocol to talk to Rhythmyx server.
    */
   public static final String DEFAULT_PROTOCOL = "http";

   /**
    * HTTP status that is returned after sending a request to a URL
    * successfully.
    */
   public static final int HTTP_STATUS_OK = 200;

   /**
    * HTTP status that is returned after sending a request to a URL indicating
    * authentication failure.
    */
   public static final int HTTP_STATUS_BASIC_AUTHENTICATION_FAILED = 401;

   /**
    * HTTP status cutoff value. HTTP status values below 400 are actually not
    * errors but warnings. Better we check for this value rather than full
    * success value pf HTTP_STATUS_OK above.
    */
   public static final int HTTP_STATUS_OK_RANGE = 399;

   /**
    * Configuration XML DOM Document, never null after this class is
    * instantiated successfully.
    */
   private Document m_ConfigDoc = null;

   /**Current element that stores current user and server alias, never null
   * after this class is instantiated successfully.
    */
   private Element m_ElementCurrent = null;

   /**URL to get Content List for the current server alias, never null after
    * this class is instantiated successfully.
    */
   private Element m_ElementCurrentURL = null;

   /**
    * password specified by the user, can be empty but never <code>null</code>.
    */
   private String m_password = "";

   /**
    * configuration file name - predefined
    */
   public static final String FUDCONFIGFILE = "rxftconfig.xml";

   private static final String ERROR_SERVER_ALIAS = "errorServerAliasEmpty";

   /**
    * Elements and attributes in the configuration document as per the DTD.
    */
   private static final String ELEM_CURRENT = "current";
   private static final String ELEM_URL = "url";
   private static final String ELEM_PROTOCOL = "protocol";

   private static final String ELEM_PURGELOCALOPTION =
                                          "purgelocalcopyafterupload";
   private static final String ELEM_PROMPTPURGE = "promptbeforepurge";
   private static final String ELEM_PROMPTOVERWRITE = "promptbeforeoverwrite";

   private static final String ATTRIB_HOST = "host";
   private static final String ATTRIB_PORT = "port";

   private static final String ATTRIB_SERVERALIAS = "serveralias";
   private static final String ATTRIB_USERID = "userid";

   /*
    * Values for Option Types
    */
   private static final int OPTION_PURGELOCAL =       0;
   private static final int OPTION_PROMPTPURGE =       1;
   private static final int OPTION_PROMPTOVERWRITE =  2;

   /**
    * String constants for "yes" and "no"
    */
   private static final String OPTION_YES = "yes";
   private static final String OPTION_NO = "no";

   /**
    * This variable is set when application connects to the server and gets
    * content item list. This is required since upload requires (under the
    * current server bahavior) the userid and password supplied during the
    * first post request itself. We will have problems if the Rx application
    * requires authentication versus not. This does happen with a query request.
    * So when we get the content item list we request first without password.
    * If request fails with Authentication error, we supply the credentials and
    * then make a request. Essentially, this variable tells us whether
    * authentication was actually required when content item list was obtained.
    * If so, we supply the credentials for upload, otherwise not. Default
    * is <code>flase</code>.
    */
    private boolean m_bAuthenticationRequired = false;
}

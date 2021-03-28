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

package com.percussion.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import com.percussion.utils.io.PathUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import javax.xml.parsers.DocumentBuilder;

import com.percussion.install.RxAppConverter;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
*
* This is a simple utility class that converts existing Rhythmyx applications
* to point to a new backend database. It searches and replaces for driver,
* server, database and origin fields in the applications with those specified
* in the properties file. Please note that this is usefull only if all the
* resources use the same backend database source.
*
*/
public class PSAppConverter
{
   private static final Logger logger = LogManager.getLogger(PSAppConverter.class);

   /**
   * Empty constructor
   */
   public PSAppConverter()
   {
   }

   /**
   * The only static method that actually converts the application.
   *
   * @param props - JAVA Properties file that has all the new field values.
   *
   * @param root - The Rhythmyx root directory name. e.g. c:/Rhythmyx
   *
   * @param appName - The Rhythmyx application name. e.g. WFEditor in
   * (ObjectStore/WFEditor.xml).
   *
   * @param bModifyCredential <code>true</code> if credentials need to be
   *        modified false otherwise.
   *
   * @param sPort new port number for existing workflow applications, can be
   * <code>null</code>
   *
   * @throws IOException - if file is invalid or inaccessible
   *
   * @throws SAXException - if the application file is not parseable XML
   * document
   *
   */
   static public void updateRxApp(Properties props, String root, String appName,
      boolean bModifyCredential, String sPort)
      throws IOException, SAXException
   {
      updateRxApp(props, root, appName, bModifyCredential, sPort, false);
   }

   /**
   * The only static method that actually converts the application.
   *
   * @param props - JAVA Properties file that has all the new field values.
   *
   * @param root - The Rhythmyx root directory name. e.g. c:/Rhythmyx
   *
   * @param appName - The Rhythmyx application name. e.g. WFEditor in
   * (ObjectStore/WFEditor.xml).
   *
   * @param bModifyCredential <code>true</code> if credentials need to be
   *        modified false otherwise.
   *
   * @param sPort new port number for existing workflow applications, can be
   * <code>null</code>
   *
   * @param bEnable <code>true</code> if the app should be activated.
   *
   * @throws IOException - if file is invalid or inaccessible
   *
   * @throws SAXException - if the application file is not parseable XML
   * document
   *
   */
   static public void updateRxApp(Properties props, String root, String appName,
      boolean bModifyCredential, String sPort, boolean bEnable)
      throws IOException, SAXException
   {
      String fileName = root + File.separator +  "ObjectStore" +
                               File.separator  + appName + ".xml";

      System.out.println("Updating application file: " + fileName +
         " for backend database information...");

      DocumentBuilder db = Utils.getDocumentBuilder();
      Document doc = null;
      NodeList nl = null;
      Element elem = null;
      Text text = null;

      String driver = props.getProperty(DB_DRIVER_NAME, "");
      String server = props.getProperty(DB_SERVER, "");
      String schema = props.getProperty(DB_SCHEMA, "");
      String database = props.getProperty(DB_NAME, "");

      FileWriter writer = null;

      doc = db.parse(fileName);

      //enable if specified
      if(bEnable)
      {
         Element elemRoot = doc.getDocumentElement();
         if(elemRoot != null)
         {
            elemRoot.setAttribute("enabled", "yes");
         }
      }

      nl = doc.getElementsByTagName(DRIVER);
      RxAppConverter.setChildNode(doc, nl, driver);

      nl = doc.getElementsByTagName(SERVER);
      RxAppConverter.setChildNode(doc, nl, server);

      nl = doc.getElementsByTagName(ORIGIN);
      RxAppConverter.setChildNode(doc, nl, schema);

      nl = doc.getElementsByTagName(DATABASE);
      RxAppConverter.setChildNode(doc, nl, database);

      //modify credentials if asked
      if(bModifyCredential)
      {
         nl = doc.getElementsByTagName("PSXBackEndCredential");
         if(null != nl && nl.getLength() > 0)
         {
            elem = (Element)nl.item(0);
            setElementData(elem, DRIVER, driver);
            setElementData(elem, SERVER, server);

            String uid = props.getProperty(DB_UID, "");
            setElementData(elem, USERID, uid);

            String pwd = props.getProperty(DB_PWD, "");
            //assumes the attribute "encrypted" is always set to "yes"
            try {
               setElementData(elem, PASSWORD, PSEncryptor.getInstance("AES",
                       PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).encrypt(pwd));
            } catch (PSEncryptionException e) {
               logger.error("Error encrypting password: " + e.getMessage(),e);
               setElementData(elem, PASSWORD, "");
            }
         }
      }

      //modify the port numbers if required
      //This is temporary fix till MakeLink is modfied - Ram
      //This can only search an replace and cannot insert if there is port specified!!!
      RxAppConverter.modifyPort(sPort, doc);

      writer = new FileWriter(fileName);
      PrintNode.printNode(doc, " ", writer);
      writer.flush();
      writer.close();
   }

   private static boolean setElementData(Element parent, String elemName,
            String elemValue)
   {
      NodeList childs = parent.getElementsByTagName(elemName);
      if(null == childs || childs.getLength() < 1)
         return false;

      Element elem = (Element)childs.item(0);
      Node child = elem.getFirstChild();
      if(null != child && Node.TEXT_NODE == child.getNodeType())
      {
         ((Text)child).setData(elemValue);
      }
      else
      {
         Text text = parent.getOwnerDocument().createTextNode(elemValue);
         elem.appendChild(text);
      }

      return true;
   }

   /**
   * The DB_DRIVER_NAME field name in the properties file.
   * The syntax should be like - [DB_DRIVER_NAME=oracle:thin]
   */
   static public String DB_DRIVER_NAME =  "DB_DRIVER_NAME";

   /**
   * The DB_SERVER field name in the properties file.
   * The syntax should be like - [DB_SERVER=@38.222.12.13:1521:ORCL]
   */
   static public String DB_SERVER =       "DB_SERVER";

   /**
   * The DB_SCHEMA name in the properties file.
   * The syntax should be like - [DB_SCHEMA=dbo]
   */
   static public String DB_SCHEMA =       "DB_SCHEMA";

   /**
   * The DB_NAME name in the properties file.
   * The syntax should be like - [DB_NAME=RxWorkflow]
   */
   static public String DB_NAME =         "DB_NAME";

   /**
   * The UID name in the properties file.
   * The syntax should be like - [UID=username]
   */
   static public String DB_UID =         "UID";

   /**
   * The PWD name in the properties file.
   * The syntax should be like - [PWD=password]
   */
   static public String DB_PWD =         "PWD";

   /**
   * The "driver" field name in the application XML Document. This field value
   * will be replaced with DB_DRIVER_NAME field value in the properties file.
   *
   */
   static public String DRIVER =          "driver";

   /**
   * The "server" field name in the application XML Document. This field value
   * will be replaced with DB_SERVER field value in the properties file.
   *
   */
   static public String SERVER =          "server";

   /**
   * The "origin" field name in the application XML Document. This field value
   * will be replaced with DB_SCHEMA field value in the properties file.
   *
   */
   static public String ORIGIN =          "origin";

   /**
   * The "database" field name in the application XML Document. This field value
   * will be replaced with DB_NAME field value in the properties file.
   *
   */
   static public String DATABASE =        "database";

   /**
   * The "userId" field name in the application XML Document. This field value
   * will be replaced with UID field value in the properties file.
   *
   */
   static public String USERID =        "userId";

   /**
   * The "password" field name in the application XML Document. This field value
   * will be replaced with PWD field value in the properties file.
   *
   */
   static public String PASSWORD =        "password";

   /**
   * The "encrypted" attribute in the application XML Document.
   *
   */
   static public String ATTRIB_ENCRYPTED =        "encrypted";

   /**
   * The main method. This takes two command line parameters. The first one is
   * the Properties file name (absolute path). Second one is the comma separated
   * list of Rhythmyx application names (without any path specification and
   * extension) to be converted.
   *
   */
   public static void main(String[] args)
   {
      if(args.length < 3)
      {
         System.out.println("Usage: ");
         System.out.println(" PSAppConverter <Properties file name> "+
            "<Rhythmyx RootDir> <Application list (comma separated)> [port]");
         System.out.println("Press ENTER to continue...");
         try {System.in.read();}catch(Exception e){}
         System.exit(1);
      }

      Properties props = new Properties();
      RxAppConverter.loadProps(args, props);

      StringTokenizer tokenizer = new StringTokenizer(args[2], ",");
      PSAppConverter appConverter = new PSAppConverter();
      String temp = null;
      String port = null;
      if(args.length > 3)
         port = args[3];

      while(tokenizer.hasMoreElements())
      {
         temp = tokenizer.nextElement().toString();
         if(null == temp)
            continue;
         temp = temp.trim();
         try
         {
            appConverter.updateRxApp(props, args[1], temp, false, port);
         }
         catch(Exception e)
         {
            System.out.println("Failed to covert application '" + temp +
            "' Error: " + e.getMessage());
         }
      }

      System.out.println("Press ENTER to continue...");
      try {System.in.read();}catch(Exception e){}
      System.exit(1);
   }
}

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

package com.percussion.install;

import com.percussion.util.PSCharSets;
import com.percussion.utils.xml.PSEntityResolver;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;

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
public class RxAppConverter
{
   /**
   * Empty constructor
   */
   public RxAppConverter()
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
      updateRxApp(props, root, appName, bModifyCredential, sPort, false, false);
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
   * @param bUpdateNativeStatement - <code>true</code>if the native statement
   *  should be converted.
   *
   * @throws IOException - if file is invalid or inaccessible
   *
   * @throws SAXException - if the application file is not parseable XML
   * document
   *
   */
   static public void updateRxApp(Properties props, String root, String appName,
      boolean bModifyCredential, String sPort, boolean bEnable, boolean bUpdateNativeStatement)
      throws IOException, SAXException
   {
      String fileName = root + File.separator +  "ObjectStore" +
                               File.separator  + appName + ".xml";

      updateRxApp(props, root, fileName, bModifyCredential, sPort, bEnable, true,
                  null, bUpdateNativeStatement);
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
   * @param bAppConv <code>true</code> if the app should be converted
   *
   * @param strUpdateSecProv the security provider to update.
   *
   * @param bUpdateNativeStatement - <code>true</code>if the native statement
   *  should be converted.
   *
   * @throws IOException - if file is invalid or inaccessible
   *
   * @throws SAXException - if the application file is not parseable XML
   * document
   *
   */
  static public void updateRxApp(Properties props, String root, String fileName,
      boolean bModifyCredential, String sPort, boolean bEnable, boolean bAppConv,
      String strUpdateSecProv, boolean bUpdateNativeStatement)
      throws IOException, SAXException
   {
      //System.out.println("Updating application file: " + fileName +
      //   " for backend database information...");

      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
      Document doc = null;
      NodeList nl = null;
      Element elem = null;
      Text text = null;

      String driver = props.getProperty(DB_DRIVER_NAME, "");
      String server = props.getProperty(DB_SERVER, "");
      String schema = props.getProperty(DB_SCHEMA, "");
      String database = props.getProperty(DB_NAME, "");
      String uid = props.getProperty(DB_UID, "");
      String pwd = props.getProperty(DB_PWD, "");
      String backend = props.getProperty(DB_BACKEND, "");

      File f = new File(fileName);
      if (f.exists())
      {
         doc = db.parse(f);
      }
      else
      {
         return;
      }

      //enable if specified
      if(bEnable)
      {
         Element elemRoot = doc.getDocumentElement();
         if(elemRoot != null)
         {
            elemRoot.setAttribute("enabled", "yes");
         }
      }

      if(bAppConv)
      {
         nl = doc.getElementsByTagName(DRIVER);
          setChildNode(doc, nl, driver);

          nl = doc.getElementsByTagName(SERVER);
          setChildNode(doc, nl, server);

          nl = doc.getElementsByTagName(ORIGIN);
          setChildNode(doc, nl, schema);

          nl = doc.getElementsByTagName(DATABASE);
          setChildNode(doc, nl, database);
      }

      //modify credentials if asked
      if(bModifyCredential)
      {
         nl = doc.getElementsByTagName("PSXTableLocator");
         if(null != nl && nl.getLength() > 0)
         {
           for(int i = 0; i < nl.getLength(); i++)
            {
               elem = (Element)nl.item(i);
               setElementData(elem, "Database", database);
               setElementData(elem, "Origin", schema);
              }
         }

         nl = doc.getElementsByTagName("PSXBackEndCredential");
         if(null != nl && nl.getLength() > 0)
         {
           for(int i = 0; i < nl.getLength(); i++)
            {
               elem = (Element)nl.item(i);
               setElementData(elem, DRIVER, driver);
               setElementData(elem, SERVER, server);

               setElementData(elem, USERID, uid);

               //set the attribute "encrypted" to "yes"
               Element pwdElem = setElementData(elem, PASSWORD, pwd);
               pwdElem.setAttribute(ATTRIB_ENCRYPTED, "yes");
              }
         }
      }

      //update specified security provider
      if(strUpdateSecProv != null &&
        strUpdateSecProv.length() > 0)
      {
          nl = doc.getElementsByTagName("PSXSecurityProviderInstance");
          if(null != nl && nl.getLength() > 0)
          {
              for(int i = 0; i < nl.getLength(); i++)
              {
                  elem = (Element)nl.item(i);

                  //System.out.println("found security provider...");
                  //match the name
                  NodeList childs = elem.getElementsByTagName("name");
                  if(childs != null && childs.getLength() > 0)
                  {
                      //System.out.println("getting name...");
                      Element childElem = (Element)childs.item(0);
                      Node child = childElem.getFirstChild();
                      if(null != child && Node.TEXT_NODE == child.getNodeType())
                      {
                          String data = (String)((Text)child).getData();
                          //System.out.println("name is... " + data);
                          if(data != null && data.equals(strUpdateSecProv))
                          {
                              nl = elem.getElementsByTagName("Properties");
                              if(null != nl && nl.getLength() > 0)
                              {
                                  //System.out.println("found properties...");
                                  elem = (Element)nl.item(0);
                                  setElementData(elem, "driverName", driver);
                                  setElementData(elem, "loginId", uid);
                                  setElementData(elem, "databaseName", database);
                                  setElementData(elem, "serverName", server);
                                  setElementData(elem, "schemaName", schema);
                                   //assumes the attribute "encrypted" is always set to "yes"
                                  //and is already encrypted
                                   setElementData(elem, "loginPw", pwd);
                                  //System.out.println("setting password: " + pwd);
                              }

                              break;
                          }
                      }
                  }
              }
          }
      }

      //modify the port numbers if required
      //This is temporary fix till MakeLink is modfied - Ram
      //This can only search an replace and cannot insert if there is port specified!!!
       modifyPort(sPort, doc);

       if(bUpdateNativeStatement)
      {
          String nativeStatement = NATIVE_STATEMENT_MSSQL;
          if(backend.equalsIgnoreCase("ORACLE"))
              nativeStatement = NATIVE_STATEMENT_ORACLE;

          nl = doc.getElementsByTagName("nativeStatement");
          if(null!=nl)
          {
              elem = (Element)nl.item(0);
              text = (Text)elem.getFirstChild();
              if(null == text)
              {
                  text = doc.createTextNode(nativeStatement);
                  elem.appendChild(text);
              }
              else
                  text.setData(nativeStatement);
          }
      }

      Writer writer = null;

      try
      {
         PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);

         walker.setConvertXmlEntities(true);

         FileOutputStream os = new FileOutputStream(fileName);
         writer = new OutputStreamWriter(os, PSCharSets.rxJavaEnc());
         walker.write(writer);
      }
      finally
      {
         try
         {
            if (writer != null)
               writer.close();
         }
         catch (Exception e)
         {
         }
      }
  }

    public static void setChildNode(Document doc, NodeList nl, String server) {
        Element elem;
        Text text;
        if(null!=nl && null != server)
        {
          for(int j=0; j<nl.getLength(); j++)
          {
            elem = (Element)nl.item(j);
            text = (Text)elem.getFirstChild();
            if(null == text)
            {
              text = doc.createTextNode(server);
              elem.appendChild(text);
            }
            else
              text.setData(server);
          }
        }
    }

    public static void modifyPort(String sPort, Document doc) {
        NodeList nl;
        Element elem;
        if(null != sPort) //ignore if port is null
        {
           String searchPort = null;
           String replacePort = "";    //if port is 80 then empty it;
           if(!sPort.equals("80"))
              replacePort = new String(":" + sPort);

           nl = doc.getElementsByTagName("text");
           String url = null;
           String left = null;
           String right= null;
           Node node = null;
           int loc = -1;
           int locEnd = -1;
           for(int i=0; (null != nl) && i<nl.getLength(); i++)
           {
              elem = (Element)nl.item(i);
              node = elem.getFirstChild();
              if(null == node || !(node instanceof Text))
                 continue;
              url = ((Text)node).getData();
              if(!url.startsWith("http"))
                 continue;

              loc = url.indexOf(':', 7); //skip the one after http or https
              if(loc < 1)
                 continue;
              locEnd = url.indexOf('/', loc);
              if(locEnd < loc)
                 continue;
              searchPort = url.substring(loc, locEnd);

              left = url.substring(0, loc);
              right = url.substring(loc+searchPort.length());

              left = url.substring(0, loc);
              right = url.substring(loc+searchPort.length());

              ((Text)node).setData(left+replacePort+right);
           }
        }
    }

    private static Element setElementData(Element parent, String elemName,
            String elemValue)
   {
      NodeList childs = parent.getElementsByTagName(elemName);
      if(null == childs || childs.getLength() < 1)
         return null;

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

      return elem;
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
   * The BACKEND name in the properties file.
   */
   static public String DB_BACKEND =         "DB_BACKEND";

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
   * Native statement required in the application for ORACLE.
   */
   static public String NATIVE_STATEMENT_ORACLE =
      "SELECT DISTINCT * FROM WORKFLOWAPPS, STATES, TRANSITIONS WHERE "+
      "WORKFLOWAPPS.WORKFLOWAPPID=STATES.WORKFLOWAPPID AND (STATES.STATEID="+
      "TRANSITIONS.TRANSITIONFROMSTATEID(+) AND STATES.WORKFLOWAPPID="+
      "TRANSITIONS.WORKFLOWAPPID (+)) AND WORKFLOWAPPS.WORKFLOWAPPID= "+
      ":\"PSXParam/workflowid\"";

   /**
   * Native statement required in the application for MSSQL or MSACCESS.
   */
   static public String NATIVE_STATEMENT_MSSQL =
      "SELECT WORKFLOWAPPS.*, STATES.*, TRANSITIONS.* FROM WORKFLOWAPPS "+
      "INNER JOIN ( STATES LEFT OUTER JOIN TRANSITIONS ON TRANSITIONS."+
      "TRANSITIONFROMSTATEID=STATES.STATEID AND TRANSITIONS.WORKFLOWAPPID="+
      "STATES.WORKFLOWAPPID ) ON WORKFLOWAPPS.WORKFLOWAPPID="+
      "STATES.WORKFLOWAPPID WHERE WORKFLOWAPPS.WORKFLOWAPPID= "+
      ":\"PSXParam/workflowid\"";

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
         System.out.println(" RxAppConverter <Properties file name> "+
            "<Rhythmyx RootDir> <Application list (comma separated)> [port]");
         System.out.println("Press ENTER to continue...");
         try {System.in.read();}catch(Exception e){}
         System.exit(1);
      }

      Properties props = new Properties();
      loadProps(args, props);

       File rxRoot = new File(args[1]);
      if (rxRoot.isDirectory())
         PSEntityResolver.getInstance().setResolutionHome(rxRoot);

      StringTokenizer tokenizer = new StringTokenizer(args[2], ",");
      RxAppConverter appConverter = new RxAppConverter();
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
            RxAppConverter.updateRxApp(props, args[1], temp, true, port);
         }
         catch(Exception e)
         {
            System.out.println("Failed to covert application '" + temp +
            "' Error: " + e.getMessage());
         }
      }

      System.exit(1);
   }

    public static void loadProps(String[] args, Properties props) {
        try
        {
           props.load(new FileInputStream(args[0]));
        }
        catch(IOException ioe)
        {
           System.out.println("Failed to load properties file '" + args[0] +
              "' Error: " + ioe.getMessage());
           System.out.println("Press ENTER to continue...");
           try {System.in.read();}catch(Exception e){}
           System.exit(1);
        }
    }
}

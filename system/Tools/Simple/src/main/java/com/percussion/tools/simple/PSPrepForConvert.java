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

package com.percussion.tools.simple;

import com.percussion.security.PSEncryptor;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.legacy.security.deprecated.PSCryptographer;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class prepares for the running of the backend converter program
 * by creating a locator document from the specified install's server
 * properties file. The converter uses this file to set the database credentials
 * and connection info in the objectstore files
 */
public class PSPrepForConvert
{

   /**
    * Creates a new instance of PrepForConvert
    * @param serverBase the base directory path for the server to be converted
    * Never <code>null</code>.
    */
   public PSPrepForConvert(String serverBase)
   {
      if(null == serverBase)
         throw new IllegalArgumentException("Server base cannot be null.");

      m_serverBase = new File(serverBase);
      if(!m_serverBase.exists() && !m_serverBase.isDirectory())
         throw new IllegalArgumentException("Server base is does not exist.");

     try
      {
         DocumentBuilderFactory factory = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(false);
         factory.setIgnoringElementContentWhitespace(true);
         m_docBuilder =
            factory.newDocumentBuilder();
      }
      catch(ParserConfigurationException e)
      {
         throw new RuntimeException("Error creating document builder:\n" +
            e.getMessage());
      }
   }

   /**
    * Creates a new locator xml file based on the install's server properties
    */
   public void execute()
   {

      File dir = new File(m_serverBase.getPath() + INSTALL_UPDATE_DIR_PATH);
      if(!dir.exists() || !dir.isDirectory())
         dir.mkdir();
      File locatorFile =
         new File(m_serverBase.getPath() + LOCATOR_FILE_PATH);
      Properties props = getProperties(m_serverBase.getPath() + PROPERTY_FILE_PATH);
      if(null == props)
      {
         System.out.println("Error trying to read server properties file.");
         return;
      }

      // Build locator xml document from server properties
      Document doc = m_docBuilder.newDocument();
      Element root = doc.createElement(ELEM_BACKEND_CONVERTER);
      doc.appendChild(root);

      Element locator = addChildElement(root, ELEM_TABLE_LOCATOR, null);
      locator.setAttribute(ATTR_ALIAS, "RX_DEFAULT");
      Element creds = addChildElement(locator, ELEM_BACKEND_CREDENTIAL, null);
      creds.setAttribute(ATTR_ID, "0");
      addChildElement(creds, ELEM_ALIAS, "FOOBAR");
      addChildElement(creds, ELEM_COMMENT, null);
      addChildElement(creds, ELEM_DRIVER, props.getProperty(DRIVER_PROP_KEY));
      addChildElement(creds, ELEM_SERVER, props.getProperty(SERVER_PROP_KEY));
      addChildElement(creds, ELEM_USERID, props.getProperty(USERID_PROP_KEY));
      Element password =
         addChildElement(creds, ELEM_PASSWORD, props.getProperty(PASSWORD_PROP_KEY));
      password.setAttribute(ATTR_ENCRYPT, "yes");

      addChildElement(locator, ELEM_DATABASE, props.getProperty(DATABASE_PROP_KEY));
      addChildElement(locator, ELEM_ORIGIN, props.getProperty(ORIGIN_PROP_KEY));

      // Save the locator xml document
      try
      {
         saveXML(doc, locatorFile);
         modifyRepositoryProps(props);
      }
      catch(TransformerException | IOException e)
      {
         System.err.println("Error saving file:\n" + e.getMessage());
      }

   }

   /**
    * Modifies the rxrepository.properties file to reflect the db settings
    * in the passed in server properties.
    *
    * @param props the server properties, cannot be <code>null</code>.
    */
    private void modifyRepositoryProps(Properties props) throws IOException
    {
       if(null == props)
          throw new IllegalArgumentException("Properties cannot be null.");
       File file =
          new File(m_serverBase.getPath() + REPOSITORY_PROPERTY_FILE_PATH);
       System.out.println(file.getAbsolutePath());
       if(file.exists())
       {

          Map<String, String[]> dbDriverInfo = new HashMap<String, String[]>();
          dbDriverInfo.put(
             "odbc", new String[] {"MSACCESS", "sun.jdbc.odbc.JdbcOdbcDriver"});
          dbDriverInfo.put(
             "oracle:thin", new String[] {"ORACLE", "oracle.jdbc.OracleDriver"});
          dbDriverInfo.put(
             "psxml", new String[] {"PSXML", "com.percussion.data.jdbc.PSXmlDriver"});
          dbDriverInfo.put(
             "psfilesystem",
             new String[] {"PSFILESYSTEM", "com.percussion.data.jdbc.PSFileSystemDriver"});
          dbDriverInfo.put(
             "inetdae7", new String[] {"MSSQL", "com.inet.tds.TdsDriver"});
          dbDriverInfo.put(
             "jtds:sqlserver", new String[] {"MSSQL", "net.sourceforge.jtds.jdbc.Driver"});
          String[] tmp = dbDriverInfo.get(props.getProperty(DRIVER_PROP_KEY));
          String backend = tmp == null ? "" : tmp[0];
          String driverClass = tmp == null ? "" : tmp[1];


          Properties rxprops = getProperties(file.getAbsolutePath());
          rxprops.setProperty("DB_DRIVER_NAME", props.getProperty(DRIVER_PROP_KEY));
          rxprops.setProperty("DB_SCHEMA", props.getProperty(ORIGIN_PROP_KEY));
          rxprops.setProperty("DB_SERVER", props.getProperty(SERVER_PROP_KEY));
          rxprops.setProperty("DB_NAME", props.getProperty(DATABASE_PROP_KEY));
          rxprops.setProperty("DB_BACKEND", backend);
          rxprops.setProperty("DB_DRIVER_CLASS_NAME", driverClass);

          rxprops.setProperty("PWD",
             decrypt(
                props.getProperty(PASSWORD_PROP_KEY),
                props.getProperty(USERID_PROP_KEY)));
          rxprops.setProperty("UID", props.getProperty(USERID_PROP_KEY));


          try(FileOutputStream fout =new FileOutputStream(file))
          {
             rxprops.store(fout, null);
          }

       }

    }

   /***
    * @deprecated
    * @param pwd
    * @param uid
    * @return
    */
    @Deprecated
    private String decrypt(String pwd, String uid)
    {
       String ret;

       try {
          ret = PSEncryptor.getInstance("AES",
                  m_serverBase.getPath().concat(PSEncryptor.SECURE_DIR)).decrypt(pwd);
       }catch(Exception ex){
          ret = PSCryptographer.decrypt(
                  PSLegacyEncrypter.getInstance(
                          m_serverBase.getPath().concat(PSEncryptor.SECURE_DIR)
                  ).OLD_SECURITY_KEY(),
                  uid,
                  pwd);
       }
       return ret;
    }

   /**
    * Adds a child element to the node passed in using the name and
    * value specified. If no value is specified then an empty element is
    * created.
    *
    * @param parent the parent node to add this element to, cannot be
    * <code>null</code>.
    *
    * @param name the name of the element to be added, cannot be
    * <code>null</code> or empty.
    *
    * @param value the value of this element, may be <code>null</code> or
    * empty.
    *
    * @return the newly created element, never <code>null</code>.
    */

   private Element addChildElement(Node parent, String name, String value)
   {
      if(null == parent)
         throw new IllegalArgumentException("Parent node cannot be null.");
      if(null == name || name.trim().length() == 0)
         throw new IllegalArgumentException("Name cannot be null or empty.");

      Document doc = parent.getOwnerDocument();
      Element elem = doc.createElement(name);
      parent.appendChild(elem);

      if(value != null && value.trim().length() > 0)
         elem.appendChild(doc.createTextNode(value));


      return elem;

   }

   /**
    * Retrieves the server properties file as a Properties
    * object
    *
    * @param path the path to the property file, cannot be
    * <code>null</code> or empty.
    * @return Properties object that representsthe server
    * properties file. Returns <code>null</code> if the properties
    * file does not exist.
    */
   private Properties getProperties(String path)
   {
      if(null == path || path.trim().length() == 0)
         throw new IllegalArgumentException("Path cannot be null or empty.");
      Properties props = null;
      File propsFile = new File(path);
      try
      {
         if(propsFile.exists())
         {
            props = new Properties();
            props.load(new FileInputStream(propsFile));
         }
      }
      catch(FileNotFoundException fnfe)
      {
         System.err.println(fnfe.getMessage());
      }
      catch(IOException ioe)
      {
         System.err.println(ioe.getMessage());
      }
      return props;
   }


   /**
    * Saves the xml document back to a file
    * @param doc the xml document node to save. May not be <code>null</code>.
    * @param file the file to save. May not be <code>null</code>.
    */
   private void saveXML(Document doc, File file) throws TransformerException
   {

      // write the content into xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();

      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(file);

      transformer.transform(source, result);

   }



   /*
    * Main class
    */
   public static void main(String[] args)
   {
       if(args.length == 1)
       {
          PSPrepForConvert prep = new PSPrepForConvert(args[0]);
          prep.execute();
       }
       else
       {
          System.out.println("Usage:\njava PSPrepForConvert [<ServerBase path>]");
       }


   }

   /**
    * The server base directory path. Set in ctor never <code>null</code>
    * after that.
    */
   private File m_serverBase;

   /**
    * DocumentBuilder instance. Set in ctor never <code>null</code>
    * after that.
    */
   private DocumentBuilder m_docBuilder;

   /**
    * The relative server property file path
    */
   private static final String PROPERTY_FILE_PATH =
      "/rxconfig/Server/server.properties";

   /**
    * The relative rxInstallUpdate directory path
    */
   private static final String INSTALL_UPDATE_DIR_PATH =
      "/rxInstallUpdate";

   /**
    * The relative server property file path
    */
   private static final String LOCATOR_FILE_PATH =
      INSTALL_UPDATE_DIR_PATH + "/rxInstallUpdateLocator.xml";

   /**
    * The relative rxrepository property file path
    */
   private static final String REPOSITORY_PROPERTY_FILE_PATH =
      INSTALL_UPDATE_DIR_PATH + "/rxrepository.properties";

   /**
    * The property file key for the driver property
    */
   private static final String DRIVER_PROP_KEY = "driverType";

   /**
    * The property file key for the server property
    */
   private static final String SERVER_PROP_KEY = "serverName";

   /**
    * The property file key for the database property
    */
   private static final String DATABASE_PROP_KEY = "databaseName";

   /**
    * The property file key for the origin property
    */
   private static final String ORIGIN_PROP_KEY = "schemaName";

   /**
    * The property file key for the login password property
    */
   private static final String PASSWORD_PROP_KEY = "loginPw";

   /**
    * The property file key for the login user id property
    */
   private static final String USERID_PROP_KEY = "loginId";

   /**
    * The backend table xml element node
    */
   private static final String ELEM_BACKENDTABLE = "PSXBackEndTable";

   /**
    * The back end converter xml element node
    */
   private static final String ELEM_BACKEND_CONVERTER = "BackEndConverter";

   /**
    * The table locator xml element node
    */
   private static final String ELEM_TABLE_LOCATOR = "PSXTableLocator";

   /**
    * The back end credential xml element node
    */
   private static final String ELEM_BACKEND_CREDENTIAL = "PSXBackEndCredential";

   /**
    * The driver xml element node
    */
   private static final String ELEM_DRIVER = "driver";

   /**
    * The server xml element node
    */
   private static final String ELEM_SERVER = "server";

   /**
    * The database xml element node
    */
   private static final String ELEM_DATABASE = "Database";

   /**
    * The origin xml element node
    */
   private static final String ELEM_ORIGIN = "Origin";

   /**
    * The password xml element node
    */
   private static final String ELEM_PASSWORD = "password";

   /**
    * The alias xml element node
    */
   private static final String ELEM_ALIAS = "alias";

   /**
    * The userID xml element node
    */
   private static final String ELEM_USERID = "userId";

   /**
    * The comment xml element node
    */
   private static final String ELEM_COMMENT = "comment";

   /**
    * The encrypted xml attribute node
    */
   private static final String ATTR_ENCRYPT = "encrypted";

   /**
    * The alias xml attribute node
    */
   private static final String ATTR_ALIAS = "alias";

   /**
    * The id xml attribute node
    */
   private static final String ATTR_ID = "id";





}

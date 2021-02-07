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

package com.percussion.deployer.objectstore;

import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSCryptographer;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;

/**
 * Class to encapsulate information regarding a database connection including
 * server information and user credentials.
 */
public class PSDbmsInfo implements IPSDeployComponent
{
   private static final Logger logger = LogManager.getLogger(PSDbmsInfo.class);

   /**
    * Construct this class with all required parameters.
    * 
    * @param driver The driver to use, may not be <code>null</code> or empty.
    *           Supply the name of the jdbc driver sub-protocol.
    * @param server The server to use, may not be <code>null</code> or empty.
    * @param database The name of the database, may be <code>null</code> or
    *           empty.
    * @param origin The origin or schema to use, may be <code>null</code> or
    *           empty.
    * @param uid The user id to use when connecting, may be <code>null</code>
    *           or empty.
    * @param pwd The password to use when connecting, may be <code>null</code>
    *           or empty.
    * @param isPwdEncrypted <code>true</code> if supplied password is
    *           encrypted, <code>false</code> if not. If encrypted, password
    *           must have been encrypted by this class.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSDbmsInfo(String driver, String server, String database,
         String origin, String uid, String pwd, boolean isPwdEncrypted) {
      this(null, driver, server, database, origin, uid, pwd, isPwdEncrypted);
   }

   /**
    * Construct this class with all required parameters.
    * 
    * @param dataSource the datasource name may be <code>null</code> or empty.
    * @param driver The driver to use, may not be <code>null</code> or empty.
    *           Supply the name of the jdbc driver sub-protocol.
    * @param server The server to use, may not be <code>null</code> or empty.
    * @param database The name of the database, may be <code>null</code> or
    *           empty.
    * @param origin The origin or schema to use, may be <code>null</code> or
    *           empty.
    * @param uid The user id to use when connecting, may be <code>null</code>
    *           or empty.
    * @param pwd The password to use when connecting, may be <code>null</code>
    *           or empty.
    * @param isPwdEncrypted <code>true</code> if supplied password is
    *           encrypted, <code>false</code> if not. If encrypted, password
    *           must have been encrypted by this class.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */

   public PSDbmsInfo(String dataSource, String driver, String server,
         String database, String origin, String uid, String pwd,
         boolean isPwdEncrypted) {
      if (driver == null || driver.trim().length() == 0)
         throw new IllegalArgumentException("driver may not be null or empty");

      if (server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty");

      m_connInfo = new PSDbmsConnectionInfo(dataSource);

      setDatasource(dataSource);
      setDriver(driver);
      setServer(server);
      setOrigin(origin);
      setDatabase(database);
      setUserNamePwd(uid, pwd, isPwdEncrypted);
   }

   /**
    * Construct this object from its XML representation.
    * 
    * @param src The element containing this object's state. May not be
    *           <code>null</code> and must match the format defined by
    *           {@link #toXml(Document) toXml()}.
    * 
    * @throws IllegalArgumentException if <code>src</code> is
    *            <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    *            represent a type supported by the class.
    */
   public PSDbmsInfo(Element src) throws PSUnknownNodeTypeException {
      fromXml(src);
   }

   /**
    * Copy ctor
    * 
    * @param source The source from which a shallow copy is made, may not be
    *           <code>null</code>.
    */
   public PSDbmsInfo(PSDbmsInfo source) {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      copyFrom(source);
   }

   /**
    * Parameterless ctor for use by derived classes only.
    */
   protected PSDbmsInfo() {
   }

   /**
    * Get the connection info if provided at construction.
    * 
    * @return The connection info, may be <code>null</code> or empty to
    *         indicate the default datasource.
    */
   public IPSConnectionInfo getConnectionInfo()
   {
      return m_connInfo;
   }

   /**
    * Gets the name of the server.
    * 
    * @return The server, never <code>null</code> or empty.
    */
   public String getServer()
   {
      return m_server;
   }

   /**
    * Gets the name of the jdbc driver sub-protocol.
    * 
    * @return The driver name, never <code>null</code> or empty.
    */
   public String getDriver()
   {
      return m_driver;
   }

   /**
    * Gets the name of the database.
    * 
    * @return The database name, never <code>null</code>, may be empty.
    */
   public String getDatabase()
   {
      return m_database;
   }

   /**
    * Get the origin or schema name to use.
    * 
    * @return The origin, never <code>null</code>, may be empty.
    */
   public String getOrigin()
   {
      return m_origin;
   }

   /**
    * Gets the user id to use when connecting to the database specified by this
    * object.
    * 
    * @return The user id, never <code>null</code>, may be empty.
    */
   public String getUserId()
   {
      return m_uid;
   }

   /**
    * Get the password.
    * 
    * @param encrypted If <code>true</code>, the password is returned
    *           encrypted. Otherwise it is returned as clear text.
    * 
    * @return The password, possibly encrypted. Never <code>null</code>, may
    *         be empty. If encrypted, can only be decrypted by this class.
    */
   public String getPassword(boolean encrypted)
   {
      String pwd = m_pw;
      if (!encrypted && passwordEncrypted) {
         pwd = decryptPwd(m_uid, pwd);
      }else if(encrypted && !passwordEncrypted){
         try {
            pwd = PSEncryptor.getInstance().encrypt(m_pw);
         } catch (PSEncryptionException e) {
            logger.warn("Error encrypting datasource password:" + e.getMessage());
            logger.debug(e.getMessage(),e);
         }
      }

      return pwd;
   }

   /**
    * Get the datasource name
    * 
    * @return Returns the datasource may be <code>null</code>, or empty
    */
   public String getDatasource()
   {
      return m_connInfo != null ? m_connInfo.getDataSource() : null;
   }

   /**
    * Set the datasource name
    * 
    * @param datasource The datasource name, may be <code>null</code> or
    *           empty.
    */
   public void setDatasource(String datasource)
   {
      if (datasource != null)
         m_connInfo = new PSDbmsConnectionInfo(datasource);
   }

   /**
    * Sets user name and password.
    * 
    * @param usr The user id to use when connecting, may be <code>null</code>
    *           or empty.
    * @param pwd The password to use when connecting, may be <code>null</code>
    *           or empty.
    * @param isPwdEncrypted <code>true</code> if supplied password is
    *           encrypted, <code>false</code> if not. If encrypted, password
    *           must have been encrypted by this class.
    */
   public void setUserNamePwd(String usr, String pwd, boolean isPwdEncrypted)
   {
      passwordEncrypted = isPwdEncrypted;

      if(StringUtils.isNotEmpty(usr))
         m_uid=usr;
      else
         m_uid = "";

      if(StringUtils.isNotEmpty(pwd))
         m_pw=pwd;
      else
         m_pw = "";

      if(!isPwdEncrypted && !passwordEncrypted) {
         if(!passwordEncrypted || StringUtils.isEmpty(m_pw)) {
            m_pw = encryptPwd(m_uid, pwd);
            passwordEncrypted = true;
         }else{
            m_pw = pwd;
            passwordEncrypted = false;
         }
      }else {
         passwordEncrypted = isPwdEncrypted;
      }
   }

   /**
    * Sets the database name.
    * 
    * @param db database name, may be <code>null</code> or empty.
    */
   public void setDatabase(String db)
   {
      m_database = (db == null ? "" : db);
   }

   /**
    * Sets the driver name.
    * 
    * @param driver driver name, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException, if the input parameter is invalid.
    */
   public void setDriver(String driver)
   {
      if (driver == null || driver.length() == 0)
         throw new IllegalArgumentException(
               "Driver name cannot be null or empty");
      m_driver = driver;
   }

   /**
    * Sets the schema name.
    * 
    * @param origin schema name, may be <code>null</code> or empty.
    */
   public void setOrigin(String origin)
   {
      m_origin = (origin == null ? "" : origin);
   }

   /**
    * Sets the server name.
    * 
    * @param server server name, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException, if the input parameter is invalid.
    */
   public void setServer(String server)
   {
      if (server == null || server.length() == 0)
         throw new IllegalArgumentException(
               "Server name cannot be null or empty");
      m_server = server;
   }

   /**
    * Determines if the supplied object references the same database as this
    * object.
    * 
    * @param other The dbms info object to compare to. May not be
    *           <code>null</code>.
    * 
    * @return <code>true</code> if the supplied object and this one have the
    *         same driver, server, database, and origin (case-sensitive).
    * 
    * @throws IllegalArgumentException If <code>other</code> is
    *            <code>null</code>.
    */
   public boolean isSameDb(PSDbmsInfo other)
   {
      boolean isSame = true;

      if (m_connInfo != null && other.getConnectionInfo() != null)
      {
         if (!m_connInfo.getDataSource().equals(
               other.getConnectionInfo().getDataSource()))
            isSame = false;
      }
      if (!m_driver.equals(other.m_driver))
         isSame = false;
      else if (!m_server.equals(other.m_server))
         isSame = false;
      else if (!m_database.equals(other.m_database))
         isSame = false;
      else if (!m_origin.equals(other.m_origin))
         isSame = false;

      return isSame;
   }

   // Methods from implementation of interface IPSDeployComponent

   /**
    * Serializes this object's state to its XML representation. Format is:
    * 
    * <pre><code>
    *     %lt;!ELEMENT PSXDbmsInfo (datasource,driver, server, database, origin, userid,
    *        password)&gt;
    *     %lt;!ELEMENT datasource (#PCDATA)&gt;
    *     %lt;!ELEMENT driver (#PCDATA)&gt;
    *     %lt;!ELEMENT server (#PCDATA)&gt;
    *     %lt;!ELEMENT database (#PCDATA)&gt;
    *     %lt;!ELEMENT origin (#PCDATA)&gt;
    *     %lt;!ELEMENT userid (#PCDATA)&gt;
    *     %lt;!ELEMENT password (#PCDATA)&gt;
    * </code>
    * /&lt;pre&gt;
    * 
    *  See {@link IPSDeployComponent#toXml(Document)} for more info.
    * 
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      if (getConnectionInfo() != null)
         PSXmlDocumentBuilder.addElement(doc, root, DATASOURCE_XML_ELEMENT,
               getConnectionInfo().getDataSource());
      PSXmlDocumentBuilder.addElement(doc, root, DRIVER_XML_ELEMENT, m_driver);
      PSXmlDocumentBuilder.addElement(doc, root, SERVER_XML_ELEMENT, m_server);
      PSXmlDocumentBuilder.addElement(doc, root, DATABASE_XML_ELEMENT,
            m_database);
      PSXmlDocumentBuilder.addElement(doc, root, ORIGIN_XML_ELEMENT, m_origin);
      PSXmlDocumentBuilder.addElement(doc, root, UID_XML_ELEMENT, m_uid);
      PSXmlDocumentBuilder.addElement(doc, root, PASSWORD_XML_ELEMENT, m_pw);
      PSXmlDocumentBuilder.addElement(doc, root, PASSWORD_ENCRYPTED_XML_ELEMENT,Boolean.toString(passwordEncrypted));

      return root;
   }

   /**
    * Restores this object's state from its XML representation. See
    * {@link #toXml(Document)} for format of XML. See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      String datasource = tree.getElementData(DATASOURCE_XML_ELEMENT);
      if ( datasource == null )
      {
         PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
         datasource = dbmsHelper.findADataSource();
      }

      m_connInfo = new PSDbmsConnectionInfo(datasource);
      m_driver = PSDeployComponentUtils.getRequiredElement(tree, XML_NODE_NAME,
            DRIVER_XML_ELEMENT, true);
      m_server = PSDeployComponentUtils.getRequiredElement(tree, XML_NODE_NAME,
            SERVER_XML_ELEMENT, true);
      m_database = PSDeployComponentUtils.getRequiredElement(tree,
            XML_NODE_NAME, DATABASE_XML_ELEMENT, false);
      m_origin = PSDeployComponentUtils.getRequiredElement(tree, XML_NODE_NAME,
            ORIGIN_XML_ELEMENT, false);
      m_uid = PSDeployComponentUtils.getRequiredElement(tree, XML_NODE_NAME,
            UID_XML_ELEMENT, false);
      m_pw = PSDeployComponentUtils.getRequiredElement(tree, XML_NODE_NAME,
            PASSWORD_XML_ELEMENT, false);
      String temp = null;
      try {
            temp = PSDeployComponentUtils.getRequiredElement(tree, XML_NODE_NAME,
                 PASSWORD_ENCRYPTED_XML_ELEMENT, false);
      }catch(PSUnknownNodeTypeException e){
         //If this element is not found in tree, then we have to treat it as pwd is encrypted.
      }
      if(temp == null || temp.equalsIgnoreCase("null") || temp.equalsIgnoreCase("")){
         passwordEncrypted = true; //If we persisted the xml we can assume it was encrypted on generation.
      }else{
         passwordEncrypted = Boolean.parseBoolean(temp);
      }
   }

   /**
    * Creates a new instance of this object, performing a shallow copy of all
    * members.
    * 
    * @param obj The object from which to copy values.
    * 
    * @throws IllegalArgumentException if the supplied object is
    *            <code>null</code> or of the wrong type.
    */
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSDbmsInfo))
         throw new IllegalArgumentException("obj wrong type");

      PSDbmsInfo src = (PSDbmsInfo) obj;
      if (src.getConnectionInfo() != null)
         m_connInfo = new PSDbmsConnectionInfo(src.getConnectionInfo()
               .getDataSource());
      m_driver = src.m_driver;
      m_server = src.m_server;
      m_database = src.m_database;
      m_origin = src.m_origin;
      m_uid = src.m_uid;
      m_pw = src.m_pw;
      passwordEncrypted = src.passwordEncrypted;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDbmsInfo)) return false;
      PSDbmsInfo that = (PSDbmsInfo) o;

      String p1 = m_pw;
      String p2 = that.m_pw;

      if(passwordEncrypted){
            p1 = decryptPwd(m_uid,m_pw);
      }

      if(that.passwordEncrypted){
         p2 = decryptPwd(that.m_uid,that.m_pw);
      }

      return Objects.equals(m_driver, that.m_driver) &&
              Objects.equals(m_server, that.m_server) &&
              Objects.equals(m_database, that.m_database) &&
              Objects.equals(m_origin, that.m_origin) &&
              Objects.equals(m_uid, that.m_uid) &&
              Objects.equals(p1, p2) &&
              m_connInfo.getDataSource().equals(that.m_connInfo.getDataSource());

   }

   @Override
   public int hashCode() {
      return Objects.hash(m_driver, m_server, m_database, m_origin, m_uid, m_pw, m_connInfo.getDataSource());
   }

   /**
    * Gets a string representation of the database in the form
    * &lt;driver>:&lt;server>:&lt;database>:&lt;origin>.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getDbmsIdentifier()
   {
      String sep = ":";

      return m_driver + sep + m_server + sep + m_database + sep + m_origin;
   }

   /**
    * Encrypts the supplied password if it is non-&lt;code>null&lt;/code> and
    * not empty.
    * 
    * @param uid The user id, may be &lt;code>null&lt;/code> or empty.
    * @param pwd The password to encrypt, may be &lt;code>null&lt;/code> or
    *           empty.
    * 
    * @return The encrypted password, or an empty string if the supplied
    *         password is &lt;code>null&lt;/code> or empty.
    */
   private String encryptPwd(String uid, String pwd)
   {
      if (pwd == null || pwd.trim().length() == 0)
         return "";

      try {
         return PSEncryptor.getInstance().encrypt(pwd);
      } catch (PSEncryptionException e) {
         return "";
      }
   }

   /**
    * Decrypts the supplied password if it is non-&lt;code>null&lt;/code> and
    * not empty.
    * 
    * @param uid The user id, may be &lt;code>null&lt;/code> or empty.
    * @param pwd The password to decrypt, may be &lt;code>null&lt;/code> or
    *           empty.
    * 
    * @return The decrypted password, or an empty string if the supplied
    *         password is &lt;code>null&lt;/code> or empty.
    */
   private String decryptPwd(String uid, String pwd)
   {
      if (pwd == null || pwd.trim().length() == 0)
         return "";

      String key = uid == null || uid.trim().length() == 0
            ? PSLegacyEncrypter.INVALID_DRIVER()
            : uid;

      try {
         return PSEncryptor.getInstance().decrypt(pwd);
      } catch (PSEncryptionException e) {
         return PSCryptographer.decrypt(PSLegacyEncrypter.INVALID_CRED(), key, pwd);
      }

   }

   public boolean isPasswordEncrypted() {
      return passwordEncrypted;
   }

   public void setPasswordEncrypted(boolean passwordEncrypted) {
      this.passwordEncrypted = passwordEncrypted;
   }

   public class PSDbmsConnectionInfo implements IPSConnectionInfo
   {
      private String m_datasource = null;

      public PSDbmsConnectionInfo(String dataSrc) {
         m_datasource = dataSrc;
      }

      public String getDataSource()
      {
         return m_datasource;
      }
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXDbmsInfo";

   /**
    * Name of the JDBC driver to use to connect to the database, initialized
    * during construction, never &lt;code>null&lt;/code> or empty. May be
    * modified by call to <code>setDriver(String)</code>
    */
   private String m_driver;

   /**
    * Name of the server to use to connect to the database, initialized during
    * construction, never &lt;code>null&lt;/code> or empty. May be modified by
    * call to <code>setServer(String)</code>
    */
   private String m_server;

   /**
    * Name of the database, initialized during construction, never
    * &lt;code>null&lt;/code>, may be empty. May be modified by call to
    * <code>setDatabase(String)</code>
    */
   private String m_database;

   /**
    * Name of the origin or schema, initialized during construction, never
    * &lt;code>null&lt;/code>, may be empty. May be modified by call to
    * <code>setOrigin(String)</code>
    */
   private String m_origin;

   /**
    * Name of user to use when connecting, initialized during construction,
    * never &lt;code>null&lt;/code>, may be empty. May be modified by call to
    * <code>setUserNamePwd(String, String, boolean)</code>
    */
   private String m_uid;

   /**
    * Encrypted password of user to use when connecting, initialized during
    * construction, never &lt;code>null&lt;/code>, may be empty. May be modified
    * by call to <code>setUserNamePwd(String, String, boolean)</code>
    */
   private String m_pw;

   private boolean passwordEncrypted;

   /**
    * The connection info if supplied during construction, may be
    * <code>null</code> if not set or to use the default connection.
    */
   private IPSConnectionInfo m_connInfo = null;

   // private members for XML representation
   // datasource is public since it can be used during reading/writing archive
   // manifest.
   public static final String DATASOURCE_XML_ELEMENT = "datasource";

   private static final String DRIVER_XML_ELEMENT = "driver";

   private static final String SERVER_XML_ELEMENT = "server";

   private static final String DATABASE_XML_ELEMENT = "database";

   private static final String ORIGIN_XML_ELEMENT = "origin";

   private static final String UID_XML_ELEMENT = "userid";

   private static final String PASSWORD_XML_ELEMENT = "password";

   private static final String PASSWORD_ENCRYPTED_XML_ELEMENT = "encrypted";

}

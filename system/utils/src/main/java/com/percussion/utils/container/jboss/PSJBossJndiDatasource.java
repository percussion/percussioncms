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

package com.percussion.utils.container.jboss;

import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSJndiDatasourceImpl;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Object representation of a JNDI datasource configuration.  
 */
public class PSJBossJndiDatasource extends PSJndiDatasourceImpl implements Cloneable, IPSJndiDatasource
{

    /**
    * Construct a JNDI datasource, specifying each of the basic properties.
    * The validity of the values is not checked until it is first used.
    * 
    * @param name The name of the datasource, may not be <code>null</code> or
    * empty.  See {@link #setName(String)} for more info.
    * @param driverName The name of the JDBC driver, may not be
    * <code>null</code> or empty.
    * @param driverClassName The name of the JDBC driver class, may not be
    * <code>null</code> or empty.
    * @param server The name of the server to which the datasource will connect,
    * may not be <code>null</code> or empty.
    * @param userId The user id to use when connecting, may be <code>null</code>
    * or empty.
    * @param password The password to use when connecting, may be
    * <code>null</code> or empty. Ignored if <code>userId</code> is not
    * supplied.  The value must not be encrypted.
    */
   public PSJBossJndiDatasource(String name, String driverName, 
      String driverClassName, String server, 
      String userId, String password)
   {

      super(name,driverName,driverClassName,server,userId,password);
      
      // specify conn checker and exception sorter if null defaults are defined
      setDriverSpecificData();      
   }

   /**
    * Construct a JNDI datasource from its XML representation as defined by
    * the "local-tx-datasource" element in the the JBoss "jboss-ds_1_5.dtd" DTD.
    * All child elements will be held in memory and re-written by 
    * {@link #toXml(Document)} so that properties not known to this object will 
    * still be maintained intact.
    * 
    * @param source The source element, may not be <code>null</code>.
    * @throws PSInvalidXmlException If the element is not in the expected 
    * format.
    */
   public PSJBossJndiDatasource(Element source) throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      if (!source.getNodeName().equals(DATASOURCE_NODE_NAME))
         throw new IllegalArgumentException(
            "invalid source datasource element");
      
      final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      
      PSXmlTreeWalker walker = new PSXmlTreeWalker(source);

      Element element = null; 
      name = getElementValue(walker, source, JNDI_NAME, true);
      m_useJavaContext = getElementValue(walker, source, USE_JAVA_CONTEXT, 
         false);
      
      String connUrl = getElementValue(walker, source, CONN_URL, true);
      driverName = PSJdbcUtils.getDriverFromUrl(connUrl);
      server = PSJdbcUtils.getServerFromUrl(connUrl);

      driverClassName = getElementValue(walker, source, DRIVER_CLASS, true);
      m_transactionIsolation = getElementValue(walker, source, 
         TRANSACTION_ISOLATION, false);
      
      walker.setCurrent(source);
      element = walker.getNextElement(CONNECTION_PROPERTY, firstFlag);
      while (element != null)
      {
         String propName = PSXmlUtils.checkAttribute(element, 
            CONNECTION_PROPERTY_NAME_ATTR, false);
         String propVal = PSXmlUtils.getElementData(element,
            CONNECTION_PROPERTY, false);
         m_connectionPropMap.put(propName, propVal);
         element = walker.getNextElement(CONNECTION_PROPERTY, nextFlag);
      }
      

      userId = getElementValue(walker, source, USER_NAME, false);
      password = getElementValue(walker, source, PASSWORD, false);
      securityDomain = getElementValue(walker, source, SECURITY_DOMAIN,
         false);
      
      if (StringUtils.isBlank(securityDomain))
      {
         // may have one of the other security elements
         element = walker.getNextElement(APP_MANAGED_SEC, firstFlag);
         if (element != null)
            m_applicationManagedSecurity = true; 
         else
            m_securityDomainApp = getElementValue(walker, source, 
               SECURITY_DOMAIN_APP, false);
      }

      minConnections = getElementValueInt(walker, source, MIN_POOL_SIZE);
      maxConnections = getElementValueInt(walker, source, MAX_POOL_SIZE);
      m_blockingTimeoutMillis = getElementValue(walker, source, 
         BLOCKING_TIMEOUT, false);
      idleTimeout = getElementValueInt(walker, source, IDLE_TIMEOUT);
      m_noTxSeparatePools = getElementValue(walker, source, NO_TX_SEP_POOLS,
         false);
      m_newConnectionSql = getElementValue(walker, source, NEW_CONN_SQL, false);
      m_checkValidConnectionSql = getElementValue(walker, source,
         CHECK_VALID_CONN_SQL, false);

      m_connectionCheckerClass = getElementValue(walker, source,
         CONN_CHECKER_CLASS, false);
      
      m_backgroundValidation= getElementValue(walker, source,
            BACKGROUND_VALIDATION, false);
      m_validateOnMatch= getElementValue(walker, source,
            VALIDATE_ON_MATCH, false);
      m_backgroundValidationMillis= getElementValue(walker, source,
            BACKGROUND_VALIDATION_MILLIS,false);

 
      m_exceptionSorterClass = getElementValue(walker, source, EX_SORTER_CLASS,
         false);
      
      // specify conn checker and exception sorter if null defaults are defined
      setDriverSpecificData();
      
      m_trackStatements = getElementValue(walker, source, TRACK_STMTS, false);
      m_preparedStatementCacheSize = getElementValue(walker, source,
         PREP_STMT_CACHE_SIZE, false);
      
      walker.setCurrent(source);
      element = walker.getNextElement(DEPENDS, firstFlag);
      while (element != null)
      {
         m_depends.add(PSXmlUtils.getElementData(element, DEPENDS, true));
         element = walker.getNextElement(DEPENDS, nextFlag);
      }
      
      walker.setCurrent(source);
      element = walker.getNextElement(METADATA, firstFlag);
      if (element == null)
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            METADATA);
      
   }

    public PSJBossJndiDatasource(IPSJndiDatasource ds) {
        try {
            BeanUtils.copyProperties(this,ds);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw  new RuntimeException(e);
        }
    }

    /**
    * Sets up any driver specific data.  Currently this is the connection 
    * checker class and exception sorter class.
    */
   private void setDriverSpecificData()
   {
      if (m_backgroundValidation==null)
         m_backgroundValidation = "true";
      if (m_backgroundValidation.equalsIgnoreCase("true")) {
         m_validateOnMatch="true";
         if (m_backgroundValidationMillis==null)
            m_backgroundValidationMillis = "60000";
      }
      if (m_connectionCheckerClass == null)
         m_connectionCheckerClass = ms_connCheckerMap.get(driverName);
      if (m_exceptionSorterClass == null)
         m_exceptionSorterClass = ms_exceptionSorterMap.get(driverName);

      // for DB2, set the transaction isolation level to read uncommitted
      if (driverName.equalsIgnoreCase(PSJdbcUtils.DB2))
      {
         m_newConnectionSql = "SET CURRENT ISOLATION UR";
      }
      
      // Update driver class if old.
      if (driverClassName.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver"))
      {
          driverClassName="oracle.jdbc.OracleDriver";
      }
      
      // for mysql, set check-valid-connection-sql to ping database
      // mysql uses REPEATABLE_READ isolation level by default need READ_COMMITTED = 2
      if (driverName.equalsIgnoreCase(PSJdbcUtils.MYSQL))
      {
        m_transactionIsolation = "2";
        m_checkValidConnectionSql = "/* ping */ SELECT 1";
      }
      
   }

   /**
    * Serializes this component to the JBoss datasource XML format.  During
    * serialization, this method will add a connection checker class for Oracle 
    * datasources if one is not present (see the oracle-ds.xml example in 
    * <Jboss docs>\examples\jca), and will also add an exception sorter for 
    * both Oracle datasources if one is not present.  Also, if 
    * {@link #getSecurityDomain()} returns <code>null</code>, then the userId 
    * and password elements will not be written.
    * 
    * See {@link #PSJndiDatasource(Element)} for more info.
    * 
    * @param doc The document to use, may not be <code>null</code>.
    * 
    * @return The root element of the XML, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = doc.createElement(DATASOURCE_NODE_NAME);
      
      PSXmlDocumentBuilder.addElement(doc, root, JNDI_NAME, name);
      addOptionalElement(doc, root, USE_JAVA_CONTEXT, m_useJavaContext);
      PSXmlDocumentBuilder.addElement(doc, root, CONN_URL, 
         PSJdbcUtils.getJdbcUrl(driverName, server));
      PSXmlDocumentBuilder.addElement(doc, root, DRIVER_CLASS, 
         driverClassName);
      addOptionalElement(doc, root, TRANSACTION_ISOLATION, 
         m_transactionIsolation);
      if (!m_connectionPropMap.isEmpty())
      {
         for (Map.Entry<String, String> entry : m_connectionPropMap.entrySet())
         {
            Element prop = PSXmlDocumentBuilder.addElement(doc, root, 
               CONNECTION_PROPERTY, entry.getValue());
            prop.setAttribute(CONNECTION_PROPERTY_NAME_ATTR, entry.getKey());
         }
         
      }
      
      // if we have a security domain, don't serialize credentials 
      if (securityDomain == null)
      {
         addOptionalElement(doc, root, USER_NAME, userId);
         addOptionalElement(doc, root, PASSWORD, password);
      }
         
      if (m_applicationManagedSecurity)
         addOptionalElement(doc, root, APP_MANAGED_SEC, "");
      
      addOptionalElement(doc, root, SECURITY_DOMAIN, securityDomain);
      addOptionalElement(doc, root, SECURITY_DOMAIN_APP, m_securityDomainApp);
      PSXmlDocumentBuilder.addElement(doc, root, MIN_POOL_SIZE, String.valueOf(
         minConnections));
      PSXmlDocumentBuilder.addElement(doc, root, MAX_POOL_SIZE, String.valueOf(
         maxConnections));
      addOptionalElement(doc, root, BLOCKING_TIMEOUT, m_blockingTimeoutMillis);
      PSXmlDocumentBuilder.addElement(doc, root, IDLE_TIMEOUT, String.valueOf(
         idleTimeout));
      addOptionalElement(doc, root, NO_TX_SEP_POOLS, m_noTxSeparatePools);
      addOptionalElement(doc, root, NEW_CONN_SQL, m_newConnectionSql);
      addOptionalElement(doc, root, CHECK_VALID_CONN_SQL, 
         m_checkValidConnectionSql);
      addOptionalElement(doc, root, BACKGROUND_VALIDATION, 
            m_backgroundValidation);
      addOptionalElement(doc, root, BACKGROUND_VALIDATION_MILLIS, 
            String.valueOf(m_backgroundValidationMillis));
      
      addOptionalElement(doc, root, VALIDATE_ON_MATCH, 
            String.valueOf(m_validateOnMatch));
      
      addOptionalElement(doc, root, CONN_CHECKER_CLASS,
         m_connectionCheckerClass);
      addOptionalElement(doc, root, EX_SORTER_CLASS, m_exceptionSorterClass);
      addOptionalElement(doc, root, TRACK_STMTS, m_trackStatements);
      addOptionalElement(doc, root, PREP_STMT_CACHE_SIZE, 
         m_preparedStatementCacheSize);
      
      for (String depends : m_depends)
      {
         PSXmlDocumentBuilder.addElement(doc, root, DEPENDS, depends);
      }
      
      Element metaData = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         METADATA);
      return root;
   }
   

   /**
    * Adds the optional element to the supplied root if it is not 
    * <code>null</code>.
    * 
    * @param doc The doc to use, assumed not <code>null</code>.
    * @param root The root element, assumed not <code>null</code>.
    * @param name The name of the element to add, assumed not <code>null</code> 
    * or empty.
    * @param value The value of the element to add, may be <code>null</code> or 
    * empty.
    */
   private void addOptionalElement(Document doc, Element root, String name, 
      String value)
   {
      if (value != null)
         PSXmlDocumentBuilder.addElement(doc, root, name, value);
   }


   
   /**
    * The root XML element name for a datasource. 
    */
   public static final String DATASOURCE_NODE_NAME = "local-tx-datasource";
   
   /**
    * Gets the string text value of the specified element as a child of the
    * root node of the supplied walker.  Walker position upon return is 
    * set back to the root.
    * 
    * @param walker The tree walker to use, assumed not <code>null</code>.
    * @param source The root element, assumed not <code>null</code>.
    * @param name The name of the element from which to get the value, assumed 
    * not <code>null</code>. 
    * @param required <code>true</code> if a value is expected, 
    * <code>false</code> if it is optional.
    * 
    * @return The value, may be <code>null</code> if the specified element is 
    * not found, or empty if the element has no data.
    * 
    * @throws PSInvalidXmlException if no element or value is found and 
    * <code>required</code> is <code>true</code>.
    */
   private String getElementValue(PSXmlTreeWalker walker, Element source, 
      String name, boolean required) throws PSInvalidXmlException
   {
      walker.setCurrent(source);
      Element element = walker.getNextElement(name, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      walker.setCurrent(source);
      return PSXmlUtils.getElementData(element , name, required);
   }
   
   /**
    * Convenience that calls
    * {@link #getElementValue(PSXmlTreeWalker, Element, String, boolean) 
    * getElementValue(walker, source, name, true)} and then returns the value as
    * an integer.
    * 
    * @throws PSInvalidXmlException if the value cannot be parsed as an integer
    * or if the delegated call throws an exception.
    */
   private int getElementValueInt(PSXmlTreeWalker walker, Element source, 
      String name) throws PSInvalidXmlException
   {
      String val = getElementValue(walker, source, name, true);
      
      int intVal = -1;
      try
      {
         intVal = Integer.parseInt(val);
      }
      catch (NumberFormatException e)
      {
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_INVALID_VALUE, 
            new String[] {name, val});
      }
      
      return intVal;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }   
   

   @Override
   public Object clone() throws CloneNotSupportedException
   {
      PSJBossJndiDatasource clone = (PSJBossJndiDatasource) super.clone();
      
      // clone collections with mutable members
      Map<String, String> connPropMap = new LinkedHashMap<String, String>();
      for (Map.Entry<String, String> entry : m_connectionPropMap.entrySet())
      {
         connPropMap.put(entry.getKey(), entry.getValue());
      }
      clone.m_connectionPropMap = connPropMap;
      
      return clone;
   }


   /**
    * Name of the oracle connection checker class.
    */
   private static final String ORACLE_CONN_CHECKER =
      "org.jboss.resource.adapter.jdbc.vendor.OracleValidConnectionChecker";

   /**
    * Name of the oracle exception sorter class.
    */
   private static final String ORACLE_EX_SORTER =
      "org.jboss.resource.adapter.jdbc.vendor.OracleExceptionSorter";

   /**
    * Map of driver names to connection checker class names.  Initialized by a
    * static initializer, never <code>null</code> or modified after that.
    * See {@link #m_connectionCheckerClass} for more info.
    */
   private static Map<String, String> ms_connCheckerMap =
      new HashMap<String, String>();

   /**
    * Map of driver names to exception sorter class names.  Initialized by a
    * static initializer, never <code>null</code> or modified after that.  See
    * {@link #m_exceptionSorterClass} for more info.
    */
   private static Map<String, String> ms_exceptionSorterMap =
      new HashMap<String, String>();

   static 
   {
      ms_connCheckerMap.put(PSJdbcUtils.ORACLE, ORACLE_CONN_CHECKER);
      ms_connCheckerMap.put(PSJdbcUtils.ORACLE_OCI7, ORACLE_CONN_CHECKER);
      ms_connCheckerMap.put(PSJdbcUtils.ORACLE_OCI8, ORACLE_CONN_CHECKER);
      
      ms_exceptionSorterMap.put(PSJdbcUtils.ORACLE, ORACLE_EX_SORTER);
      ms_exceptionSorterMap.put(PSJdbcUtils.ORACLE_OCI7, ORACLE_EX_SORTER);
      ms_exceptionSorterMap.put(PSJdbcUtils.ORACLE_OCI8, ORACLE_EX_SORTER);
   }
   
   /* the following members are merely round tripped in the XML and not modified
    * by this implementation
    */
   private String m_useJavaContext = null;
   private String m_transactionIsolation = null;
   private Map<String, String> m_connectionPropMap = 
      new LinkedHashMap<String, String>();
   private boolean m_applicationManagedSecurity = false;
   private String m_securityDomainApp = null;
   private String m_blockingTimeoutMillis = null;
   private String m_noTxSeparatePools = null;
   private String m_newConnectionSql = null;
   private String m_checkValidConnectionSql = null;
   private String m_trackStatements = null;
   private String m_preparedStatementCacheSize = null;
   private List<String> m_depends = new ArrayList<String>();

   // reqwuired XML element names
   private static final String JNDI_NAME = "jndi-name";
   private static final String CONN_URL = "connection-url";
   private static final String DRIVER_CLASS = "driver-class";
   private static final String USER_NAME = "user-name";
   private static final String PASSWORD = "password";
   private static final String SECURITY_DOMAIN = "security-domain";
   private static final String MIN_POOL_SIZE = "min-pool-size";
   private static final String MAX_POOL_SIZE = "max-pool-size";
   private static final String IDLE_TIMEOUT = "idle-timeout-minutes";
   private static final String CONN_CHECKER_CLASS = 
      "valid-connection-checker-class-name";
   private static final String BACKGROUND_VALIDATION = 
         "background-validation";
   private static final String BACKGROUND_VALIDATION_MILLIS = 
         "background-validation-millis";
   private static final String VALIDATE_ON_MATCH = 
         "validate-on-match";
   private static final String EX_SORTER_CLASS = "exception-sorter-class-name";
   private static final String METADATA = "metadata";
   
   // optional (round-tripped) XML element names
   private static final String USE_JAVA_CONTEXT = "use-java-context";
   private static final String TRANSACTION_ISOLATION = "transaction-isolation";
   private static final String CONNECTION_PROPERTY = "connection-property";
   private static final String CONNECTION_PROPERTY_NAME_ATTR = "name";
   private static final String APP_MANAGED_SEC = "application-managed-security";
   private static final String SECURITY_DOMAIN_APP = 
      "security-domain-and-application";
   private static final String BLOCKING_TIMEOUT = "blocking-timeout-millis";
   private static final String NO_TX_SEP_POOLS = "no-tx-separate-pools";
   private static final String NEW_CONN_SQL = "new-connection-sql";
   private static final String CHECK_VALID_CONN_SQL = 
      "check-valid-connection-sql";
   private static final String TRACK_STMTS = "track-statements";
   private static final String PREP_STMT_CACHE_SIZE = 
      "prepared-statement-cache-size";
   private static final String DEPENDS = "depends";

    /**
     * Name of the class assigned to the {@link #CONN_CHECKER_CLASS} element.
     * See {@link #toXml(Document)} for more info.
     */
    private String m_connectionCheckerClass = null;

    /**
     * Name of the class assigned to the {@link #CONN_CHECKER_CLASS} element.
     * See {@link #toXml(Document)} for more info.
     */
    private String m_backgroundValidation = null;

    /**
     * Name of the class assigned to the {@link #CONN_CHECKER_CLASS} element.
     * See {@link #toXml(Document)} for more info.
     */
    private String m_backgroundValidationMillis = "60000";

    /**
     * Name of the class assigned to the {@link #CONN_CHECKER_CLASS} element.
     * See {@link #toXml(Document)} for more info.
     */
    private String m_validateOnMatch = null;

    /**
     * Name of the class assigned to the {@link #EX_SORTER_CLASS} element.  See
     * {@link #toXml(Document)} for more info.
     */
    private String m_exceptionSorterClass = null;


}

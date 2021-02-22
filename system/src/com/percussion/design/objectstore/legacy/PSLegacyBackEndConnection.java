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


package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.IPSValidationContext;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;


/**
 * The PSBackEndConnection class is used to define the attributes for
 * a back-end connection pool. To optimize access to back-end data, the
 * server provides mechanisms for sharing connections across threads. The
 * connections can also be kept open, even when idle, to avoid the heavy
 * overhead of opening a connection.
 *
 * @see         PSLegacyServerConfig
 * @see         PSLegacyServerConfig#getBackEndConnections()
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLegacyBackEndConnection extends PSComponent
{
   private static final long serialVersionUID = 1L;

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSLegacyBackEndConnection(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSLegacyBackEndConnection()
   {
      super();
   }

   /**
    * Constructs a back-end connection object for the specified
    * JDBC driver.
    *
    * @param   driverName      the JDBC driver name
    *
    * @param   className      the Java class implementing this driver
    *
    * @param   serverName      the server the connection pool is for
    */
   public PSLegacyBackEndConnection(String driverName, String className,
                               String serverName)
   {
      setJdbcDriverName(driverName);
      setJdbcClassName(className);
      setServer(serverName);
   }


   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSLegacyBackEndConnection)) return false;
      if (!super.equals(o)) return false;
      PSLegacyBackEndConnection that = (PSLegacyBackEndConnection) o;
      return m_connMin == that.m_connMin &&
              m_connMax == that.m_connMax &&
              m_idleTimeoutSeconds == that.m_idleTimeoutSeconds &&
              m_refreshPeriodSeconds == that.m_refreshPeriodSeconds &&
              Objects.equals(m_jdbcDriverName, that.m_jdbcDriverName) &&
              Objects.equals(m_jdbcClassName, that.m_jdbcClassName) &&
              Objects.equals(m_server, that.m_server);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_jdbcDriverName, m_jdbcClassName, m_server, m_connMin, m_connMax, m_idleTimeoutSeconds, m_refreshPeriodSeconds);
   }

   /**
    * Get the JDBC driver name used by this driver. This is used in the
    * connect string to access the driver.
    *
    * @return            the driver name
    */
   public String getJdbcDriverName()
   {
      return m_jdbcDriverName;
   }

   /**
    * Set the JDBC driver name used by this driver. This is used in the
    * connect string to access the driver.
    *
    * @param   name      the driver name
    */
   public void setJdbcDriverName(String name)
   {
      IllegalArgumentException ex = validateJdbcDriverName(name);
      if (ex != null)
         throw ex;

      m_jdbcDriverName = name;
   }

   private static IllegalArgumentException validateJdbcDriverName(String name)
   {
      if (null == name || name.length() == 0)
      {
         return new IllegalArgumentException("back-end driver is null");
      }

      return null;
   }

   /**
    * Get the name of the class implementing this JDBC driver.
    * This is used to load the driver, if it is not already loaded.
    *
    * @return            the class name
    */
   public String getJdbcClassName()
   {
      return m_jdbcClassName;
   }

   /**
    * Set the name of the class implementing this JDBC driver.
    * This is used to load the driver, if it is not already loaded.
    *
    * @param   name      the class name
    */
   public void setJdbcClassName(String name)
   {
      IllegalArgumentException ex = validateJdbcClassName(name);
      if (ex != null)
         throw ex;

      m_jdbcClassName = name;
   }

   private static IllegalArgumentException validateJdbcClassName(String name)
   {
      if (null == name || name.length() == 0)
      {
         return new IllegalArgumentException("jdbc driver class is null");
      }

      return null;
   }

   /**
    * Get the back-end server associated with this connection pool.
    *
    * @return       the back-end server
    */
   public java.lang.String getServer()
   {
      return m_server;
   }

   /**
    * Set the back-end server associated with this connection pool.
    *
    * @param server   the back-end server
    */
   public void setServer(java.lang.String server)
   {
      if (server == null)
         server = "";

      IllegalArgumentException ex = validateServer(server);
      if (ex != null)
         throw ex;

      m_server = server;
   }

   private static IllegalArgumentException validateServer(String server)
   {
      if (server != null && server.length() > MAX_SERVER_NAME_LEN) {
         return new IllegalArgumentException("back-end server is too big" +
            MAX_SERVER_NAME_LEN + " " + server.length());
      }

      return null;
   }

   /**
    * Get the maximum number of connections which can be established
    * to the back-end through this database pool.
    *
    * @return            the maximum number of connections; -1 if
    *                     unlimited connections are permitted
    */
   public int getConnectionMax()
   {
      return m_connMax;
   }

   /**
    * Set the maximum number of connections which can be established
    * to the back-end through this database pool.
    *
    * @param   max      the maximum number of connections; use -1 to
    *                     allow unlimited connections
    */
   public void setConnectionMax(int max)
   {
      IllegalArgumentException ex = validateConnectionMax(max);
      if (ex != null)
         throw ex;

      m_connMax = max;
   }

   public IllegalArgumentException validateConnectionMax(int max)
   {
      if (max == 0)
      {
         return new IllegalArgumentException("back-end connection maxconn invalid" +
            m_jdbcDriverName + " " + max);
      }

      return null;
   }

   /**
    * Get the minimum number of connections which should be established
    * to the back-end through this database pool. Even if a connection
    * stays idle beyond the idle time limit, it will not be closed if
    * that would cause the number of open connections to fall below this
    * limit.
    *
    * @return            the minimum number of connections
    *
    * @see               #getIdleTimeout
    */
   public int getConnectionMin()
   {
      return m_connMin;
   }

   /**
    * Set the minimum number of connections which should be established
    * to the back-end through this database pool. Even if a connection
    * stays idle beyond the idle time limit, it will not be closed if
    * that would cause the number of open connections to fall below this
    * limit.
    *
    * @param   min      the minimum number of connections
    *
    * @see               #setIdleTimeout
    */
   public void setConnectionMin(int min)
   {
      IllegalArgumentException ex = validateConnectionMin(min);
      if (ex != null)
         throw ex;
      m_connMin = min;
   }

   private IllegalArgumentException validateConnectionMin(int min)
   {
      if (min < 0)
      {
         return new IllegalArgumentException("back-end connection minconn invalid" +
            m_jdbcDriverName + " " + min);
      }

      return null;
   }

   /**
    * Get the amount of idle time, in seconds, that will cause a connection
    * to be closed. Even if a connection stays idle beyond the idle time
    * limit, it will not be closed if that would cause the number of open
    * connections to fall below the minimum number of connections required.
    *
    * @return            the amount of idle time, in seconds, which will
    *                     cause an idle connection to close
    *
    * @see               #getConnectionMin
    */
   public int getIdleTimeout()
   {
      return m_idleTimeoutSeconds;
   }

   /**
    * Set the amount of idle time, in seconds, that will cause a connection
    * to be closed. Even if a connection stays idle beyond the idle time
    * limit, it will not be closed if that would cause the number of open
    * connections to fall below the minimum number of connections required.
    *
    * @param   seconds   the amount of idle time, in seconds, which will
    *                     cause an idle connection to close
    */
   public void setIdleTimeout(int seconds)
   {
      m_idleTimeoutSeconds = seconds;
   }

   /**
    * Gets the maximum the amount of time, in seconds, that a connection should
    * go without use.  When this time has elapsed, the connection should be
    * used to ensure that it will stay alive.
    *
    * @return the maximum number of seconds that should elapse between
    * refreshes, or -1 if refreshes are disabled.
    */
   public int getRefreshPeriodSeconds()
   {
      return m_refreshPeriodSeconds;
   }

   /**
    * Sets the number of seconds that may elapse between tests of the validity
    * of a connection to this backend.  When this period has elapsed, a
    * connection should be exercised to ensure it is still valid and will
    * not be closed due to idleness.
    *
    * @param seconds the amount of time, in seconds, which should cause a
    * connection to be refreshed (supply <code>-1</code> to disable refreshing).
    */
   public void setRefreshPeriodSeconds(int seconds)
   {
      if (seconds != -1 && seconds < 1)
         throw new IllegalArgumentException(
            "Illegal value for refresh period: " + seconds );

      m_refreshPeriodSeconds = seconds;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXBackEndConnection XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *      PSBackEndConnection defines the connection configuration for a
    *      back-end data store. The connection info is used by the
    *      server's database pool for connecting to the back-end.
    *    --&gt;
    *    &lt;!ELEMENT PSXBackEndConnection   (   jdbcDriverName,
    *                                             jdbcClassName,
    *                                             serverName,
    *                                             connectionMin,
    *                                             connectionMax,
    *                                             connectionIdleTimeout)&gt;
    *
    *   &lt;!--
    *    Attributes associated with the connection:
    *
    *    id - the internal identifier for this object.
    *   --&gt;
    *   &lt;!ATTLIST
    *         id       ID         #REQUIRED
    *   &gt;
    *
    *    &lt;!--
    *      the JDBC driver for which the connection pool is being defined.
    *    --&gt;
    *    &lt;!ELEMENT jdbcDriverName         (#PCDATA)&gt;
    *
    *    &lt;!--
    *      the Java class which implements the JDBC driver. The server
    *      must be able to find this class using the CLASSPATH.
    *    --&gt;
    *    &lt;!ELEMENT jdbcClassName            (#PCDATA)&gt;
    *
    *    &lt;!--
    *      the back-end server for which the connection pool is being
    *        defiend.
    *    --&gt;
    *    &lt;!ELEMENT serverName               (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the minimum number of connections which should be established
    *         to the back-end through this database pool. Even if a connection
    *         stays idle beyond the idle time limit, it will not be closed if
    *       that would cause the number of open connections to fall below
    *       this limit.
    *    --&gt;
    *    &lt;!ELEMENT connectionMin            (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the maximum number of connections which can be established
    *       to the back-end through this database pool.
    *    --&gt;
    *    &lt;!ELEMENT connectionMax            (#PCDATA)&gt;
    *
    *    &lt;!--
    *         the amount of idle time, in seconds, that will cause a connection
    *       to be closed. Even if a connection stays idle beyond the idle
    *       time limit, it will not be closed if that would cause the number
    *       of open connections to fall below the minimum number of
    *         connections required.
    *    --&gt;
    *    &lt;!ELEMENT connectionIdleTimeout   (#PCDATA)&gt;
     *
     *    &lt;!--
    *   the period of idle time, in seconds, that will cause a connection
    *    to be refreshed.
    *    --&gt;
    *    &lt;!ELEMENT connectionRefreshPeriod   (#PCDATA)&gt;
    * </code></pre>
    *
    * @return      the newly created PSXBackEndConnection XML element node
    */
   public Element toXml(Document   doc)
   {
      Element root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      // store the driver name
      PSXmlDocumentBuilder.addElement(
         doc, root, "jdbcDriverName", m_jdbcDriverName);

      // store the class name
      PSXmlDocumentBuilder.addElement(
         doc, root, "jdbcClassName", m_jdbcClassName);

      // store the server name
      PSXmlDocumentBuilder.addElement(
         doc, root, "serverName", m_server);

      // store min connection count
      PSXmlDocumentBuilder.addElement(
         doc, root, "connectionMin", String.valueOf(m_connMin));

      // store max connection count
      PSXmlDocumentBuilder.addElement(
         doc, root, "connectionMax", String.valueOf(m_connMax));

       // store idle connection timeout (in seconds)
       PSXmlDocumentBuilder.addElement(doc, root, "connectionIdleTimeout",
          String.valueOf(m_idleTimeoutSeconds));

       PSXmlDocumentBuilder.addElement(doc, root, "connectionRefreshPeriod",
          String.valueOf(m_refreshPeriodSeconds));

      return root;
   }

   /**
    * This method is called to populate a PSBackEndConnection Java object
    * from a PSXBackEndConnection XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not of
     * type PSXBackEndConnection
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

         if (false == ms_NodeType.equals (sourceNode.getNodeName()))
         {
            Object[] args = { ms_NodeType, sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         // get the driver name
         sTemp = tree.getElementData("jdbcDriverName");
         if ((sTemp == null) || (sTemp.length() == 0)) {
            Object[] args = { ms_NodeType, "jdbcDriverName", "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_jdbcDriverName = sTemp;

         // get the class name
         sTemp = tree.getElementData("jdbcClassName");
         if ((sTemp == null) || (sTemp.length() == 0)) {
            Object[] args = { ms_NodeType, "jdbcClassName", "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_jdbcClassName = sTemp;

         //read server name
         try {
            setServer(tree.getElementData("serverName"));
         } catch (IllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "serverName",
               new PSException (e.getLocalizedMessage()));
         }

         // read min connection count
         sTemp = tree.getElementData("connectionMin");
         try {
            m_connMin = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, "connectionMin",
               ((sTemp == null) ? "" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // read max connection count
         sTemp = tree.getElementData("connectionMax");
         try {
            m_connMax = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, "connectionMax",
               ((sTemp == null) ? "" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // read idle connection timeout (in seconds)
         sTemp = tree.getElementData("connectionIdleTimeout");
         try {
            m_idleTimeoutSeconds = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, "connectionIdleTimeout",
               ((sTemp == null) ? "" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

           // read refresh period (in seconds) -- optional
           sTemp = tree.getElementData("connectionRefreshPeriod");
           if (sTemp != null)
           try
           {
              setRefreshPeriodSeconds( Integer.parseInt( sTemp ) );
           } catch (Exception e)
           {
              throw new PSUnknownNodeTypeException(
                 IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
                 new Object[] { ms_NodeType, "connectionRefreshPeriod", sTemp }
              );
           }
      } finally {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateJdbcDriverName(m_jdbcDriverName);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateJdbcClassName(m_jdbcClassName);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateServer(m_server);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateConnectionMax(m_connMax);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateConnectionMin(m_connMin);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      // validate that we can load the JDBC driver class
      try
      {
         Class.forName(m_jdbcClassName);
      }
      catch (Exception e)
      {
         Object[] args = new Object[] { m_jdbcClassName, e.toString() };
         cxt.validationError(this,
            IPSObjectStoreErrors.JDBC_DRIVER_CLASS_LOAD_ERROR, args);
      }
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSComponent.
    */
   public void copyFrom(PSComponent c)
   {
      PSLegacyBackEndConnection conn = (PSLegacyBackEndConnection)c;

      super.copyFrom(c);
      conn.m_jdbcDriverName = m_jdbcDriverName;
      conn.m_jdbcClassName = m_jdbcClassName;
      conn.m_server = m_server;
      conn.m_connMin = m_connMin;
      conn.m_connMax = m_connMax;
      conn.m_idleTimeoutSeconds = m_idleTimeoutSeconds;
       conn.m_refreshPeriodSeconds = m_refreshPeriodSeconds;
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer(100);
      buf.append("Back end connection to ");
      buf.append(m_server);
      buf.append(" over ");
      buf.append(m_jdbcDriverName);
      buf.append(" using driver class ");
      buf.append(m_jdbcClassName);
      buf.append(", min conns " + m_connMin);
      buf.append(", max conns " + m_connMax);
      buf.append(", idle timeout " + m_idleTimeoutSeconds);
      buf.append("sec");

       buf.append(", refresh period ");
       buf.append(m_refreshPeriodSeconds);
       buf.append("sec");
       buf.append(".");

      return buf.toString();
   }

   private String            m_jdbcDriverName         = null;
   private String            m_jdbcClassName         = null;
   private String            m_server                  = null;
   private int               m_connMin               = 0;
   private int               m_connMax               = -1;
   private int               m_idleTimeoutSeconds      = 300;

   /**
    * How often should connections to this backend be refreshed (maximum
    * time between queries)?  A value of <code>-1</code> indicates connections
    * never need to be refreshed.  Assigned in <code>fromXml</code> and
    * <code>setRefreshPeriodSeconds</code>.
    */
   private int m_refreshPeriodSeconds = -1;

   private static final int         MAX_SERVER_NAME_LEN   = 128;

   /* package access on this so they may reference each other in fromXml */
   static final String      ms_NodeType = "PSXBackEndConnection";
}

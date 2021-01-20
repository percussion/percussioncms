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
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.error.PSException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSBackEndTable class is used to define a back-end table. Back-end
 * tables can be used in PSBackEndDataTank and PSBackEndColumn objects to
 * define the table they are acting upon.
 *
 * @see PSBackEndDataTank
 * @see PSBackEndColumn
 */
public class PSLegacyBackEndTable extends PSComponent
{
   private static final long serialVersionUID = 1L;

   /**
    * Provide a suitable key for indexing database connections
    */
   public class PSServerKey
   {
      
      /* (non-Javadoc)
       * @see java.lang.Object#equals(java.lang.Object)
       */
      public boolean equals(Object obj)
      {
         if (! (obj instanceof PSServerKey))
         {
            return false;
         }
         
         PSServerKey key = (PSServerKey) obj;
         
         if (! eq(key.getServer(), getServer())) return false;
         if (! eq(key.getDriver(), getDriver())) return false;
         if (! eq(key.getDatabase(), getDatabase())) return false;
         
         return true;
         
      }

      /* (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */
      public int hashCode()
      {         
         int hash = 0;
         
         if (m_database != null) hash += m_database.hashCode();
         if (m_driver != null) hash += m_driver.hashCode();
         if (m_server != null) hash += m_server.hashCode();
         
         return hash; 
      }
      
      /*
       * (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         String db = m_database == null ? "unknown db" : m_database;
         String drv = m_driver == null ? "unknown driver" : m_driver;
         String srv = m_server == null ? "unknown server" : m_server; 
          
         return db + ":" + drv + ":" + srv;
      }

      /**
       * Gets the server name
       * @return the server from the backend table {@link #m_server}, never
       * <code>null</code> or empty for a validly created {@link PSLegacyBackEndTable}
       * created and populated with {@link PSLegacyBackEndTable#fromXml()}. For any
       * other case there are no guarantees.
       */
      public String getServer()
      {
         return m_server;
      }
      
      /**
       * Gets the driver name
       * @return the driver from the backend table {@link #m_driver}, never
       * <code>null</code> or empty for a validly created {@link PSLegacyBackEndTable}
       * created and populated with {@link PSLegacyBackEndTable#fromXml()}. For any
       * other case there are no guarantees.
       */
      public String getDriver()
      {
         return m_driver;
      }
      
      /**
       * Gets the database name
       * @return the database from the backend table {@link #m_database}, never
       * <code>null</code> or empty for a validly created {@link PSLegacyBackEndTable}
       * created and populated with {@link PSLegacyBackEndTable#fromXml()}. For any
       * other case there are no guarantees.
       */
      public String getDatabase()
      {
         return m_database;
      }
      
      /**
       * Compare two objects for equality
       * @param a first object, may be <code>null</code>
       * @param b second object, may be <code>null</code>
       * @return <code>true</code> if the objects are equal or both 
       * <code>null</code>
       */
      private boolean eq(Object a, Object b)
      {
         if (a == null && b == null) return true;
         if (a == null && b != null) return false;
         if (b == null && a != null) return false;
         
         return a.equals(b);
      }
   }
   
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
   public PSLegacyBackEndTable(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a back-end table object. The object can be used to
    * allow multiple joined tables or it can enforce that only a single
    * table be used. The former is commonly associated with data tanks
    * performing query operations and the latter with data tanks performing
    * insert, update and/or delete operations.
    * <p>
    * Once this object is constructed, it cannot be changed from allowing
    * single table to multiple tables.
    *
    * @param alias            the new alias of the back-end table. This
    *                         must be a unique name across all tables used
    *                         in the data tank. If it is non-unique, an
    *                         exception will be thrown when the back-end
    *                         table is associated with a data tank.
    *
    * @see         #setAlias
    */
   public PSLegacyBackEndTable(java.lang.String alias)
   {
      super();
      setAlias(alias);
   }

   /**
    * Ctor
    */
   PSLegacyBackEndTable()
   {
   }


   /**
    * Get the alias of the back-end table.
    *
    * @return     the alias of the back-end table
    */
   public java.lang.String getAlias()
   {
      return m_alias;
   }

   /**
    * Set the alias of the back-end table.
    * This is limited to 128 characters.
    *
    * @param alias   the new alias of the back-end table. This must be a
    *                unique name across all tables used in the data tank.
    *                If it is non-unique, an exception will be thrown when
    *                the back-end table is associated with a data tank.
    *
    */
   public void setAlias(java.lang.String alias)
   {
      IllegalArgumentException ex = validateAlias(alias);
      if (ex != null)
         throw ex;

      m_alias = alias;
   }

   private static IllegalArgumentException validateAlias(String alias)
   {
      if (null == alias || alias.length() == 0)
         return new IllegalArgumentException("back-end table alias is null");
      else if (alias.length() > MAX_ALIAS_NAME_LEN) {
         return new IllegalArgumentException("back-end table alias is too big");
      }

      return null;
   }

   /**
    * Get the driver used to access the back-end table.
    *
    * @return     the driver used to access the back-end table
    */
   public java.lang.String getDriver()
   {
      return m_driver;
   }

   /**
    * Set the driver used to access the back-end table.
    * This is limited to 128 characters.
    * <p>
    * The valid list of drivers can be located by using the PSCataloger
    * to perform a catalog of category "data" and type "Driver".
    *
    * @param driver   the driver used to access the back-end table.
    */
   public void setDriver(java.lang.String driver)
   {
      IllegalArgumentException ex = validateDriver(driver);
      if (ex != null)
         throw ex;

      m_driver = driver;
   }

   private static IllegalArgumentException validateDriver(String driver)
   {
      if (null == driver || driver.length() == 0)
         return new IllegalArgumentException("back-end driver is null");
      else if (driver.length() > MAX_DRIVER_NAME_LEN) {
         return new IllegalArgumentException("back-end driver is too big");
      }

      return null;
   }

   /**
    * Get the server on which the back-end table resides.
    *
    * @return     the server on which the back-end table resides
    */
   public java.lang.String getServer()
   {
      return m_server;
   }

   /**
    * Set the server on which the back-end table resides.
    * This is limited to 128 characters.
    *
    * @param server   the server on which the back-end table resides.
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
      if (server.length() > MAX_SERVER_NAME_LEN) {
         return new IllegalArgumentException("back-end server is too big");
      }

      return null;
   }

   /**
    * Get the database on which the back-end table resides.
    *
    * @return     the database on which the back-end table resides
    */
   public java.lang.String getDatabase()
   {
      return m_database;
   }

   /**
    * Set the database on which the back-end table resides.
    * This is limited to 128 characters.
    *
    * @param database  the database on which the back-end table resides.
    */
   public void setDatabase(java.lang.String database)
   {
      if (database == null)
         database = "";

      IllegalArgumentException ex = validateDatabase(database);
      if (ex != null)
         throw ex;

      m_database = database;
   }

   private static IllegalArgumentException validateDatabase(String database)
   {
      if (database.length() > MAX_DB_NAME_LEN) {
         return new IllegalArgumentException("back-end database is too big");
      }

      return null;
   }

   /**
    * Get the back-end table's origin. This is also referred to as the owner
    * or schema.
    *
    * @return     the back-end table's origin
    */
   public java.lang.String getOrigin()
   {
      return m_origin;
   }

   /**
    * Set the back-end table's origin. This is also referred to as the owner
    * or schema.
    * This is limited to 128 characters.
    *
    * @param       origin   the back-end table's origin.
    */
   public void setOrigin(java.lang.String origin)
   {
      if (origin == null)
         origin = "";

      IllegalArgumentException ex = validateOrigin(origin);
      if (ex != null)
         throw ex;

      m_origin = origin;
   }

   private static IllegalArgumentException validateOrigin(String origin)
   {
      if (origin.length() > MAX_ORIGIN_NAME_LEN) {
         return new IllegalArgumentException("back-end origin is too big");
      }

      return null;
   }

   /**
    * Get the name of the back-end table.
    *
    * @return     the name of the back-end table
    */
   public java.lang.String getTable()
   {
      return m_table;
   }

   /**
    * Set the name of the back-end table.
    * This is limited to 128 characters.
    *
    * @param table   the name of the back-end table.
    */
   public void setTable(java.lang.String table)
   {
      if (table == null)
         table = "";

      IllegalArgumentException ex = validateTable(table);
      if (ex != null)
         throw ex;

      m_table = table;
   }

   private static IllegalArgumentException validateTable(String table)
   {
      if (table.length() > MAX_TABLE_NAME_LEN) {
         return new IllegalArgumentException("back-end table is too big");
      }

      return null;
   }

   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXBackEndTable XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXBackEndTable class is used to define a back-end table.
    *       Back-end tables can be used in PSXBackEndDataTank and
    *       PSXBackEndColumn objects to define the table they are acting
    *       upon.
    *    --&gt;
    *    &lt;!ELEMENT PSXBackEndTable   (alias, driver, server,
    *                                   database, origin, table)&gt;
    *
    *    &lt;!--
    *       the alias of the back-end table. This must be a unique name
    *       across all tables used in the data tank. This is limited to
    *       50 characters.
    *    --&gt;
    *    &lt;!ELEMENT alias            (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the driver used to access the back-end table.
    *    --&gt;
    *    &lt;!ELEMENT driver            (%PSXBackEndProviderType)&gt;
    *
    *    &lt;!--
    *       the server on which the back-end table resides.
    *    --&gt;
    *    &lt;!ELEMENT server            (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the database on which the back-end table resides.
    *    --&gt;
    *    &lt;!ELEMENT database         (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the back-end table's origin. This is also referred to as the
    *       owner or schema.
    *    --&gt;
    *    &lt;!ELEMENT origin            (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the name of the back-end table.
    *    --&gt;
    *    &lt;!ELEMENT table            (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXBackEndTable XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private      String          m_alias;
      PSXmlDocumentBuilder.addElement(doc, root, "alias", m_alias);

      //private      String          m_driver;
      PSXmlDocumentBuilder.addElement(   doc, root, "driver", m_driver);

      //private      String          m_server;
      PSXmlDocumentBuilder.addElement(   doc, root, "server", m_server);

      //private      String          m_database;
      PSXmlDocumentBuilder.addElement(   doc, root, "database", m_database);

      //private      String          m_origin;
      PSXmlDocumentBuilder.addElement(   doc, root, "origin", m_origin);

      //private      String          m_table;
      PSXmlDocumentBuilder.addElement(   doc, root, "table", m_table);

      return root;
   }

   /**
    * This method is called to populate a PSBackEndTable Java object
    * from a PSXBackEndTable XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXBackEndTable
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
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

      try {      //private      String          m_alias;
         setAlias(tree.getElementData("alias"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "alias", new PSException (e.getLocalizedMessage()));
      }

      try {      //private      String          m_driver;
         setDriver(tree.getElementData("driver"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "driver", new PSException (e.getLocalizedMessage()));
      }

      try {      //private      String          m_server;
         setServer(tree.getElementData("server"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "server", new PSException (e.getLocalizedMessage()));
      }

      try {      //private      String          m_database;
         setDatabase(tree.getElementData("database"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "database", new PSException (e.getLocalizedMessage()));
      }

      try {      //private      String          m_origin;
         setOrigin(tree.getElementData("origin"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "origin", new PSException (e.getLocalizedMessage()));
      }

      try {      //private      String          m_table;
         setTable(tree.getElementData("table"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "table", new PSException (e.getLocalizedMessage()));
      }
   }


   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects.
    */
   public int hashCode()
   {
      int hash = 0;
      if ( null != m_alias )
         hash += m_alias.hashCode();
      if ( null != m_driver )
         hash += m_driver.hashCode();
      if ( null != m_server )
         hash += m_server.hashCode();
      if ( null != m_database )
         hash += m_database.hashCode();
      if ( null != m_origin )
         hash += m_origin.hashCode();
      if ( null != m_table )
         hash += m_table.hashCode();
      return hash;
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateAlias(m_alias);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateDriver(m_driver);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateServer(m_server);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateDatabase(m_database);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateOrigin(m_origin);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateTable(m_table);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());
   }

   /**
    * Performs a copy of the data in the supplied component to this component.
    * Since the state of this object consists of non-mutable objects, the copy
    * is effectively a deep copy.
    *
    * @param c a valid <code>PSBackEndTable</code>, not <code>null</code>.
    */
   public void copyFrom(PSComponent c)
   {
      if (c instanceof PSLegacyBackEndTable)
      {
         PSLegacyBackEndTable table = (PSLegacyBackEndTable) c;
         try
         {
            super.copyFrom( table );
         } catch (IllegalArgumentException e) { } // cannot happen
         m_alias = table.m_alias;
         m_driver = table.m_driver;
         m_server = table.m_server;
         m_database = table.m_database;
         m_origin = table.m_origin;
         m_table = table.m_table;
      }
      else
         throw new IllegalArgumentException( "INVALID_OBJECT_FOR_COPY" );
   }

   public boolean equals(Object o)
   {
      if (!(o instanceof PSLegacyBackEndTable))
         return false;

      PSLegacyBackEndTable other = (PSLegacyBackEndTable)o;

      if (m_alias == null || other.m_alias == null)
      {
         if (m_alias != null || other.m_alias != null)
            return false;
      }
      else if (!m_alias.equals(other.m_alias))
         return false;

      if (m_driver == null || other.m_driver == null)
      {
         if (m_driver != null || other.m_driver != null)
            return false;
      }
      else if (!m_driver.equals(other.m_driver))
         return false;

      if (m_server == null || other.m_server == null)
      {
         if (m_server != null || other.m_server != null)
            return false;
      }
      else if (!m_server.equals(other.m_server))
         return false;

      if (m_database == null || other.m_database == null)
      {
         if (m_database != null || other.m_database != null)
            return false;
      }
      else if (!m_database.equals(other.m_database))
         return false;

      if (m_origin == null || other.m_origin == null)
      {
         if (m_origin != null || other.m_origin != null)
            return false;
      }
      else if (!m_origin.equals(other.m_origin))
         return false;

      if (m_table == null || other.m_table == null)
      {
         if (m_table != null || other.m_table != null)
            return false;
      }
      else if (!m_table.equals(other.m_table))
         return false;

      return true;
   }
   
   /**
    * Get a server key for use in storing and retrieving database connections.
    * @return a new server key, never <code>null</code>
    */
   public Object getServerKey()
   {
      return new PSServerKey();
   }
      
   /**
    * Creates a deep copy of this PSBackEndTable instance
    * @return a clone of PSBackEndTable
    */
   public Object clone()
   {
      PSLegacyBackEndTable copy = (PSLegacyBackEndTable)super.clone();
      return copy;
   }

   private      String          m_alias = "";
   private      String          m_driver;
   private      String          m_server;
   private      String          m_database;
   private      String          m_origin;
   private      String          m_table;

   private static final int         MAX_ALIAS_NAME_LEN   = 128;
   private static final int         MAX_DRIVER_NAME_LEN   = 128;
   private static final int         MAX_SERVER_NAME_LEN   = 128;
   private static final int         MAX_DB_NAME_LEN      = 128;
   private static final int         MAX_ORIGIN_NAME_LEN   = 128;
   private static final int         MAX_TABLE_NAME_LEN   = 128;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXBackEndTable";
}

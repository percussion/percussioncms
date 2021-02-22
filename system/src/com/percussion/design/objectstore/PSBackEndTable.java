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

package com.percussion.design.objectstore;

import com.percussion.design.objectstore.legacy.IPSComponentConverter;
import com.percussion.error.PSException;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.apache.commons.lang.StringUtils;
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
public class PSBackEndTable extends PSComponent
{
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
         
         return key.toString().equalsIgnoreCase(toString());
      }

      /* (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */
      public int hashCode()
      {         
         return m_dataSource == null ? 0 : m_dataSource.hashCode(); 
      }
      
      /*
       * (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         return m_dataSource == null ? "" : m_dataSource;
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
   public PSBackEndTable(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      
      // check for a converter
      IPSComponentConverter converter = getComponentConverter(this.getClass());
      

      /*
       * If it's a forced conversion, then just convert the source and copy from
       * what we get back
       */
      if (converter != null && converter.isForcedConversion())
      {
         PSBackEndTable convertedTable = 
            (PSBackEndTable) converter.convertComponent(sourceNode);
         copyFrom(convertedTable);
      }
      else
      {
         // try to restore from xml as usual
         try
         {
            fromXml(sourceNode, parentDoc, parentComponents);
         }
         catch (PSUnknownNodeTypeException e)
         {
            if (converter == null)
            {
               // no converter, so it's just bad XML
               throw e;
            }
            else
            {
               /*
                * if we have a converter, then try to convert. If that succeeds,
                * then copy from the converted object. Otherwise it will throw
                * an exception
                */
               PSBackEndTable convertedTable = 
                  (PSBackEndTable) converter.convertComponent(sourceNode);
               copyFrom(convertedTable);
            }
         }        
      }
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
   public PSBackEndTable(java.lang.String alias)
   {
      super();
      setAlias(alias);
   }

   /**
    * Ctor
    */
   PSBackEndTable()
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
    * @return Returns the dataSource.
    */
   public String getDataSource()
   {
      return m_dataSource;
   }
   /**
    * @param dataSource The dataSource to set.
    */
   public void setDataSource(String dataSource)
   {
      m_dataSource = dataSource;
   }
   
   /**
    * Set the information for connecting to the database from a backend
    * credential. 
    * @param cred The credential, never <code>null</code>
    */
   public void setConnectionInfo(PSBackEndCredential cred)
   {
      if (cred == null)
      {
         throw new IllegalArgumentException("cred may not be null");
      }
      m_dataSource = cred.getDataSource();
   }

   /**
    * Set table information from the locator
    * @param tableLoc the locator, never <code>null</code>
    */
   public void setInfoFromLocator(PSTableLocator tableLoc)
   {
      if (tableLoc == null)
      {
         throw new IllegalArgumentException("tableLoc may not be null");
      }
      setConnectionInfo(tableLoc.getCredentials());
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
    * This method is called to create a PSXBackEndTable XML element node
    * containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * 
    * <pre><code>
    * 
    *     &lt;!--
    *        PSXBackEndTable class is used to define a back-end table.
    *        Back-end tables can be used in PSXBackEndDataTank and
    *        PSXBackEndColumn objects to define the table they are acting
    *        upon.
    *     --&gt;
    *     &lt;!ELEMENT PSXBackEndTable   (alias, table, datasource)&gt;
    * 
    *     &lt;!--
    *        the alias of the back-end table. This must be a unique name
    *        across all tables used in the data tank. This is limited to
    *        50 characters.
    *     --&gt;
    *     &lt;!ELEMENT alias            (#PCDATA)&gt;
    * 
    *     &lt;!--
    *        the name of the back-end table.
    *     --&gt;
    *     &lt;!ELEMENT table            (#PCDATA)&gt;
    *  
    * </code></pre>
    * 
    * @return the newly created PSXBackEndTable XML element node
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      // private String m_alias;
      PSXmlDocumentBuilder.addElement(doc, root, "alias", m_alias);

      // private String m_table;
      PSXmlDocumentBuilder.addElement(doc, root, "table", m_table);

      PSXmlDocumentBuilder.addElement(doc, root, "datasource", m_dataSource);

      return root;
   }

   /**
    * This method is called to populate a PSBackEndTable Java object from a
    * PSXBackEndTable XML element node. See the {@link #toXml(Document) toXml}
    * method for a description of the XML object.
    * 
    * @exception PSUnknownNodeTypeException if the XML element node is not of
    * type PSXBackEndTable
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      java.util.ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (false == ms_NodeType.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {ms_NodeType, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try
      {
         m_id = Integer.parseInt(sTemp);
      }
      catch (Exception e)
      {
         Object[] args =
         {ms_NodeType, ((sTemp == null) ? "null" : sTemp)};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      try
      { // private String m_alias;
         setAlias(tree.getElementData("alias"));
      }
      catch (IllegalArgumentException e)
      {
         throw new PSUnknownNodeTypeException(ms_NodeType, "alias",
            new PSException(e.getLocalizedMessage()));
      }

      try
      { // private String m_dataSource;
         Element dsEl = tree.getNextElement("datasource", 
            tree.GET_NEXT_ALLOW_CHILDREN);
         if (dsEl == null)
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, "datasource");
         setDataSource(tree.getElementData(dsEl));
      }
      catch (IllegalArgumentException e)
      {
         // Ignore
      }

      try
      { // private String m_table;
         setTable(tree.getElementData("table"));
      }
      catch (IllegalArgumentException e)
      {
         throw new PSUnknownNodeTypeException(ms_NodeType, "table",
            new PSException(e.getLocalizedMessage()));
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
      if ( null != m_dataSource )
         hash += m_dataSource.hashCode();
      if ( null != m_table )
         hash += m_table.hashCode();
      return hash;
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it should
    * register any errors with the validation context, which will decide whether
    * to throw the exception (in which case the implementation of <CODE>validate</CODE>
    * should not catch it unless it is to be rethrown).
    * 
    * @param cxt The validation context.
    * 
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateAlias(m_alias);
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
      if (c instanceof PSBackEndTable)
      {
         PSBackEndTable table = (PSBackEndTable) c;
         try
         {
            super.copyFrom( table );
         } catch (IllegalArgumentException e) { } // cannot happen
         m_alias = table.m_alias;
         m_table = table.m_table;
         m_dataSource = table.m_dataSource;
      }
      else
         throw new IllegalArgumentException( "INVALID_OBJECT_FOR_COPY" );
   }

   public boolean equals(Object o)
   {
      if (!(o instanceof PSBackEndTable))
         return false;

      PSBackEndTable other = (PSBackEndTable)o;

      if (m_alias == null || other.m_alias == null)
      {
         if (m_alias != null || other.m_alias != null)
            return false;
      }
      else if (!m_alias.equals(other.m_alias))
         return false;
      
      if (m_dataSource == null || other.m_dataSource == null)
      {
         if (m_dataSource != null || other.m_dataSource != null)
            return false;
      }
      else if (!m_dataSource.equals(other.m_dataSource))
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
    * Compare two tables to see if they both reference the same datasource.
    * 
    * @param secondTable the other table to compare to, never <code>null</code>
    * @return <code>true</code> if they both reference the same datasource.
    */
   public boolean isSameDatasource(PSBackEndTable secondTable)
   {
      if (secondTable == null)
      {
         throw new IllegalArgumentException("secondTable may not be null");
      }
      
      return StringUtils.equals(getDataSource(), 
         secondTable.getDataSource());
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
    * Set connection detail of this table's datasource.  The detail is not 
    * persisted or serialized as part of this object.  
    * See {@link #getDataSource()}.
    * 
    * @param connDetail The detail, may be <code>null</code> to clear the
    * detail.
    */
   public void setConnectionDetail(PSConnectionDetail connDetail)
   {
      m_connDetail = connDetail;
   }
   
   /**
    * Get the connection detail set on this object.  See 
    * {@link #setConnectionDetail(PSConnectionDetail)} for more info.
    * 
    * @return The connection detail, may be <code>null</code>.
    */
   public PSConnectionDetail getConnectionDetail()
   {
      return m_connDetail;
   }
      
   /**
    * Creates a deep copy of this PSBackEndTable instance
    * @return a clone of PSBackEndTable
    */
   public Object clone()
   {
      PSBackEndTable copy = (PSBackEndTable)super.clone();
      return copy;
   }

   private      String          m_alias = "";
   private      String          m_table;
   
   /**
    * The datasource that resolves to the location of the table, may be
    * <code>null</code>.
    */
   private String m_dataSource;
   
   /**
    * The connection detail for this table's datasource, <code>null</code>
    * until set by a call to {@link #setConnectionDetail(PSConnectionDetail)}.
    */
   private transient PSConnectionDetail m_connDetail = null;

   private static final int         MAX_ALIAS_NAME_LEN   = 128;
   private static final int         MAX_TABLE_NAME_LEN   = 128;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXBackEndTable";


}

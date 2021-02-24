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

import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for the PSXChoiceTableInfo DTD in BasicObjects.dtd.
 */

public class PSChoiceTableInfo extends PSComponent
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Constructs a new choice table info object
    * 
    * @param dataSource name of the data source, must not be <code>null</code>
    *           or empty.
    * @param tableName name of the table, must not be <code>null</code> or
    *           empty.
    * @param lableColumn name of the label column, must not be <code>null</code>
    *           or empty.
    * @param valueColumn name of the value column, must not be <code>null</code>
    *           or empty.
    */
   public PSChoiceTableInfo(String dataSource, String tableName,
         String lableColumn, String valueColumn) {
      super();
      setDataSource(dataSource);
      setLableColumn(lableColumn);
      setTableName(tableName);
      setValueColumn(valueColumn);
   }

   /**
    * Constructs a new choice table info object with value column name same as
    * label couln name.
    * 
    * @param dataSource name of the data source, must not be <code>null</code>
    *           or empty.
    * @param tableName name of the table, must not be <code>null</code> or
    *           empty.
    * @param labelColumn name of the label column, must not be <code>null</code>
    *           or empty.
    */
   PSChoiceTableInfo(String dataSource, String tableName, String labelColumn) {
      this(dataSource, tableName, labelColumn, labelColumn);
   }

   /**
    * Construct a Java object from its XML representation.
    * 
    * @param sourceNode the XML element node to construct this object from, not
    *           <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object, not
    *           <code>null</code>.
    * @param parentComponents the parent objects of this object, not
    *           <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *            appropriate type
    */
   public PSChoiceTableInfo(Element sourceNode, IPSDocument parentDoc,
         ArrayList parentComponents) throws PSUnknownNodeTypeException {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSChoiceTableInfo() {
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    * 
    * @param c a valid PSChoiceTableInfo, not <code>null</code>.
    */
   public void copyFrom(PSChoiceTableInfo c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setDataSource(c.getDataSource());
      setTableName(c.getTableName());
      setLableColumn(c.getLableColumn());
      setValueColumn(c.getValueColumn());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSChoiceTableInfo)) return false;
      if (!super.equals(o)) return false;
      PSChoiceTableInfo that = (PSChoiceTableInfo) o;
      return Objects.equals(m_dataSource, that.m_dataSource) &&
              Objects.equals(m_tableName, that.m_tableName) &&
              Objects.equals(m_lableColumn, that.m_lableColumn) &&
              Objects.equals(m_valueColumn, that.m_valueColumn);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_dataSource, m_tableName, m_lableColumn, m_valueColumn);
   }

   /**
    * 
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(ATTR_DATA_SOURCE, m_dataSource);
      root.setAttribute(ATTR_TABLE_NAME, m_tableName);
      root.setAttribute(ATTR_LABLE_COLUMN_NAME, m_lableColumn);
      root.setAttribute(ATTR_VALUE_COLUMN_NAME, m_valueColumn);
      return root;
   }

   /**
    * 
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, 
      @SuppressWarnings("unused") IPSDocument parentDoc,
         ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try
      {
         setDataSource(sourceNode.getAttribute(ATTR_DATA_SOURCE));
         setTableName(sourceNode.getAttribute(ATTR_TABLE_NAME));
         setLableColumn(sourceNode.getAttribute(ATTR_LABLE_COLUMN_NAME));
         setValueColumn(sourceNode.getAttribute(ATTR_VALUE_COLUMN_NAME));
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Get the choice entries corresponding to the table, label and value
    * columns. As this gets called from content editor, in case of error We
    * don't want to throw an exception here and break the content editor. Log
    * the error and just add a choice entry that shows that the table info is
    * invalid.
    * 
    * @return Iterator of PSEntry objects. May be empty but never
    *         <code>null</code>.
    */
   public Iterator<PSEntry> getChoiceEntries()
   {
      List<PSEntry> choices = new ArrayList<>();
      if(StringUtils.isBlank(m_tableName))
      {
         ms_log.error("Unable to build entries as the table name is empty");
         choices.add(createInvalidPSEntryChoice());
      }
      if(StringUtils.isBlank(m_lableColumn))
      {
         ms_log.error("Unable to build entries as the label column name is empty");
         choices.add(createInvalidPSEntryChoice());
      }
      
      IPSDatasourceManager mgr = null;
      mgr = PSDatasourceMgrLocator.getDatasourceMgr();
      
      String valueColumn = m_valueColumn;
      if(StringUtils.isBlank(valueColumn))
         valueColumn = m_lableColumn;
      Connection con = null;
      try
      {
         IPSConnectionInfo info = new PSConnectionInfo(m_dataSource);
         con = mgr.getDbConnection(info);
         PSConnectionDetail detail = mgr.getConnectionDetail(info);
         
         String tableName = PSSqlHelper.qualifyTableName(m_tableName, 
            detail.getDatabase(), detail.getOrigin(), detail.getDriver());
         
         PreparedStatement stmt = null;
         String sql = "SELECT " + m_lableColumn + "," + valueColumn
               + " FROM " + tableName;
         stmt = PSPreparedStatement.getPreparedStatement(con, sql);
         ResultSet rs = stmt.executeQuery();
         while (rs.next())
         {
            String label = rs.getString(1);
            String value = rs.getString(2);
            if (StringUtils.isBlank(value))
            {
               ms_log.error("Skipped the row as the value column value is null or empty");
               continue;
            }
            if (label == null)
               label = "";
            choices.add(new PSEntry(value, label));
         }
      }
      catch (NamingException e)
      {
         ms_log.error(e);
         choices.clear();
         choices.add(createInvalidPSEntryChoice());

      }
      catch (SQLException e)
      {
         ms_log.error(e);
         choices.clear();
         choices.add(createInvalidPSEntryChoice());
      }
      finally
      {
         if (con != null)
         {
            try
            {
               con.close();
            }
            catch (SQLException e)
            {
               ms_log.error(
                     "Unable to close the SQL connection while getting the choice entries.",
                           e);
            }
         }
      }

      return choices.iterator();
   }

   /**
    * Helper method to create an PSEntry object with invalid choice.
    * 
    * @return The entry, never <code>null</code>.
    */
   private PSEntry createInvalidPSEntryChoice()
   {
      return new PSEntry("invalid", new PSDisplayText(
            "Error! Invalid Table Info"));
   }

   /**
    * Get the data source
    * 
    * @return the data source, never <code>null</code> may be empty.
    */
   public String getDataSource()
   {
      return m_dataSource;
   }

   /**
    * Set the new data source
    * 
    * @param dataSource the new data source,  if <code>null</code> will be set to
    *           empty.
    */
   public void setDataSource(String dataSource)
   {
      if (StringUtils.isBlank(dataSource))
         m_dataSource = StringUtils.EMPTY;
      else
         m_dataSource = dataSource;
   }

   /**
    * Get the label column
    * 
    * @return the label column, never <code>null</code> or empty.
    */
   public String getLableColumn()
   {
      return m_lableColumn;
   }

   /**
    * Set the new label column
    * 
    * @param lableColumn the new label column, if <code>null</code> will be
    * set to empty.
    */
   public void setLableColumn(String lableColumn)
   {
      if (StringUtils.isBlank(lableColumn))
         m_lableColumn = StringUtils.EMPTY;
      else
         m_lableColumn = lableColumn;
   }

   /**
    * Get the table name
    * 
    * @return the table name, never <code>null</code> or empty.
    */
   public String getTableName()
   {
      return m_tableName;
   }

   /**
    * Set the new table name
    * 
    * @param tableName the new table name, if <code>null</code> will be set to
    *           empty.
    */
   public void setTableName(String tableName)
   {
      if (StringUtils.isBlank(tableName))
         m_tableName = StringUtils.EMPTY;
      else
         m_tableName = tableName;
   }

   /**
    * Get the value column
    * 
    * @return the value column, never <code>null</code> may be empty.
    */
   public String getValueColumn()
   {
      return m_valueColumn;
   }

   /**
    * Set the new value column
    * 
    * @param valueColumn the new value column,  if <code>null</code> will be set to
    *           empty.
    */
   public void setValueColumn(String valueColumn)
   {
      if (StringUtils.isBlank(valueColumn))
         m_valueColumn = StringUtils.EMPTY;
      else
         m_valueColumn = valueColumn;
   }

   /**
    * The data source, never <code>null</code> after construction may be empty.
    */
   private String m_dataSource = "";

   /**
    * The table name, never <code>null</code> after construction may be empty.
    */
   private String m_tableName;

   /**
    * The label column, never <code>null</code> after construction may be empty.
    */
   private String m_lableColumn;

   /**
    * The value column, never <code>null</code> after construction may be empty.
    */
   private String m_valueColumn;

   /**
    * The XML node name for this object
    */
   public static final String XML_NODE_NAME = "PSXChoiceTableInfo";

   /**
    * Logger for this class
    */
   private static Log ms_log = LogFactory.getLog(PSChoiceTableInfo.class);

   /*
    * XML element and attribute names to represent the data of this class
    */
   private static final String ATTR_DATA_SOURCE = "dataSource";

   private static final String ATTR_TABLE_NAME = "tableName";

   private static final String ATTR_LABLE_COLUMN_NAME = "labelColumnName";

   private static final String ATTR_VALUE_COLUMN_NAME = "valueColumnName";
}

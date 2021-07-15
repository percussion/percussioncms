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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.deploy.server;

import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSIdMapping;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Manages saving and retrieving <code>PSIdMap</code>
 * objects to and from the database repository.
 */
public class PSIdMapManager
{

   /**
    * Constructing the object and retrieving the table schema from the
    * database.
    *
    * @throws PSDeployException If fail to get the table schema, which is used
    * to save and retore all <code>PSIdMap</code> objects.
    */
   public PSIdMapManager() throws PSDeployException
   {
      m_dbmsHandle = PSDbmsHelper.getInstance();
      m_tableSchema = m_dbmsHandle.catalogTable(IDMAP_TABLE_NAME, false);
      m_tableSchema.setAllowSchemaChanges(false);
   }

   /**
    * Get the ID Map of the <code>sourceServer</code> from the database.
    *
    * @param sourceServer The string used to identify the source repository.
    * It may not be <code>null</code> or empty.
    *
    * @return The <code>PSIdMap</code> for the <code>sourceServer</code>, it will
    * never be <code>null</code>, but the <code>PSIdMap</code> may not have
    * any <code>PSIdMapping</code> objects. The object will not have any
    * <code>PSIdMapping</code> objects if it does not exist in the database.
    *
    * @throws PSDeployException if there are any errors.
    */
   public PSIdMap getIdmap(String sourceServer) throws PSDeployException
   {
      if ( sourceServer == null || sourceServer.trim().length() == 0 )
         throw new IllegalArgumentException("map may not be null or empty");

      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(IDMAP_SRC_COL,
         PSJdbcSelectFilter.EQUALS, sourceServer, Types.VARCHAR);

      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(m_tableSchema,
         null, filter);


      PSIdMap idmapResult = null;
      if (tData != null)
      {
         Iterator rows = tData.getRows();
         if ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();
            PSJdbcColumnData cdata= row.getColumn(IDMAP_XML_COL);
            if (cdata == null || cdata.getValue() == null ||
               cdata.getValue().trim().length() == 0)
            {
               Object[] args = {IDMAP_TABLE_NAME, IDMAP_XML_COL,
                  cdata.getValue() == null ? "null" : cdata.getValue()};
                  throw new PSDeployException(
                     IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
            }
            idmapResult = convertStringToIdMap(cdata.getValue());

            // We only expecting one row in the resultset sicne it should be
            // indexed/primary-key on the serverServer name, error otherwise.
            if ( rows.hasNext() )
            {
               Object[] args = {sourceServer, IDMAP_TABLE_NAME};
                  throw new PSDeployException(
                     IPSDeploymentErrors.UNEXPECTED_EXTRA_ROW, args);
            }
         }
      }

      if (idmapResult == null)
      {
         // does not exists in the database
         idmapResult = new PSIdMap(sourceServer);
      }

      return idmapResult;
   }

   /**
    * Save the a <code>PSIdMap</code> object into the database.
    *
    * @param map The <code>PSIdMap</code> object to be saved into the database.
    * It may not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>map</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   public void saveIdMap(PSIdMap map) throws PSDeployException
   {
      if ( map == null )
         throw new IllegalArgumentException("map may not be null");

      validateSavedIdMap(map);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element mapEl = map.toXml(doc);
      String sXml = PSXmlDocumentBuilder.toString(mapEl);
      PSJdbcTableData tableData = getTableDataForSave(map.getSourceServer(),
          sXml);

      m_dbmsHandle.processTable(m_tableSchema, tableData);
   }

   /**
    * Prepare a <code>PSJdbcTableData</code> for saving the given
    * parameter into the database
    *
    * @param srcServer The source server, assume it is not <code>null</code> or
    * empty
    * @param xml The XML document (in <code>String</code>), assume it is not
    * <code>null</code> or empty
    *
    * @return The constructed <code>PSJdbcTableData</code> object for saving
    * to the database, it never be <code>null</code>.
    */
   private PSJdbcTableData getTableDataForSave(String srcServer, String xml)
   {
      List cols = new ArrayList();

      PSJdbcColumnData col = new PSJdbcColumnData(IDMAP_SRC_COL, srcServer);

      cols.add(col);
      col = new PSJdbcColumnData(IDMAP_XML_COL, xml);
      cols.add(col);

      // do insert if not exist; otherwise, update
      PSJdbcRowData rData = new PSJdbcRowData(cols.iterator(),
         PSJdbcRowData.ACTION_REPLACE);

      List rDataList = new ArrayList();
      rDataList.add(rData);
      PSJdbcTableData tData = new PSJdbcTableData(IDMAP_TABLE_NAME,
         rDataList.iterator(), false);

      return tData;
   }

   /**
    * Validating the given <code>PSIdMap</code> object, which will be saved
    * to the database.
    *
    * @param map The to be validated <code>PSIdMap</code> object. Assuming
    * it is not <code>null</code>.
    *
    * @throws PSDeployException if the given <code>PSIdMap</code> object is not
    * in the saved state.
    */
   private void validateSavedIdMap(PSIdMap map) throws PSDeployException
   {
      Iterator mappingList = map.getMappings();
      while (mappingList.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping) mappingList.next();
         if ((mapping.getTargetId() == null) && (!mapping.isNewObject()))
         {
           Object[] args = {map.getSourceServer() , mapping.getSourceId(),
               mapping.getSourceName()};
            throw new PSDeployException(
               IPSDeploymentErrors.INVALID_SAVED_ID_MAP, args);
         }
      }
   }

   /**
    * Converting XML document (in <code>String</code>) to <code>PSIdMap</code>
    *
    * @param xml The XML representation of a <code>PSIdMap</code> object,
    * assume it is not <code>null</code>.
    *
    * @return The converted <code>PSIdMap</code> object. It never be
    * <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   private PSIdMap convertStringToIdMap(String xml)
      throws PSDeployException
   {
      StringReader sReader = new StringReader(xml);

      PSIdMap mapResult = null;
      try {
         Document resultDoc = PSXmlDocumentBuilder.createXmlDocument(sReader,
             false);
         Element root = resultDoc.getDocumentElement();
         mapResult = new PSIdMap(root);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      return mapResult;
   }

   /**
    * The name of the database table for storing all <code>PSIdMap</code>
    * objects.
    */
   public static final String IDMAP_TABLE_NAME = "DPL_ID_MAPPING";


   /**
    * The column name of the {@link #TABLE_NAME} table for the source-id
    */
   private static final String IDMAP_SRC_COL = "REPOSITORY_ID";

   /**
    * The column name of the {@link #TABLE_NAME} table for storing XML of the
    * <code>PSIdMap</code> object.
    */
   private static final String IDMAP_XML_COL = "ID_MAP";

   /**
    * The table schema info, used to save and restore all <code>PSIdMap</code>
    * objects. It is initialized by the constructor. It will never be
    * <code>null</code> or modified after that.
    */
   private PSJdbcTableSchema m_tableSchema;

   /**
    * The <code>PSDbmsHelper</code> object, used to communicate with
    * the database. It is initialized by constructor, then never modified
    * afterwards.
    */
   PSDbmsHelper m_dbmsHandle;

}

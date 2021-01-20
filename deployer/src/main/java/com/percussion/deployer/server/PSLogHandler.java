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

package com.percussion.deployer.server;


import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.IPSDeployComponent;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchiveManifest;
import com.percussion.deployer.objectstore.PSArchivePackage;
import com.percussion.deployer.objectstore.PSArchiveSummary;
import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSLogDetail;
import com.percussion.deployer.objectstore.PSLogSummary;
import com.percussion.deployer.objectstore.PSTransactionLogSummary;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.server.PSServer;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcFilterContainer;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Handling the processing all log table related operations.
 */
public class PSLogHandler
{
   /**
    * Constructing the object and retrieving all log table schemas from the
    * database.
    *
    * @throws PSDeployException If fail to get any of the table schemas.
    */
   public PSLogHandler() throws PSDeployException
   {
      m_dbmsHandle = PSDbmsHelper.getInstance();
      m_archiveLogSummarySchema = m_dbmsHandle.catalogTable(ALS_TABLE_NAME,
         false);
      m_archiveLogSummarySchema.setAllowSchemaChanges(false);

      m_archivePackageSchema = m_dbmsHandle.catalogTable(AP_TABLE_NAME, false);
      m_archivePackageSchema.setAllowSchemaChanges(false);
      
      // need one for deletes, so we can set an update key
      m_archivePackageDeleteSchema = new PSJdbcTableSchema(
         m_archivePackageSchema);   
      m_archivePackageDeleteSchema.setAllowSchemaChanges(false);
      m_dbmsHandle.setUpdateKeyForSchema(AP_ARCHIVE_LOG_ID,
         m_archivePackageDeleteSchema);

      m_logTxnSchema = m_dbmsHandle.catalogTable(TXN_TABLE_NAME, false);
      m_logTxnSchema.setAllowSchemaChanges(false);

      m_logSummarySchema = m_dbmsHandle.catalogTable(LS_TABLE_NAME, false);
      m_logSummarySchema.setAllowSchemaChanges(false);
   }

   /**
    * Gets all <code>PSArchiveSummary</code> objects from the log tables for
    * a given target server.
    *
    * @param tgtServer The target server, which is the combination of hostname
    * and port number, as in "hostname":"port". It may not be <code>null</code>
    * or empty.
    *
    * @return An iterator over zero or more <code>PSArchiveSummary</code>
    * objects. It will never be <code>null</code>, but may be empty.
    *
    * @throws IllegalArgumentException if <code>tgtServer</code> is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   public Iterator getArchiveSummaries(String tgtServer)
        throws PSDeployException
   {
      if (tgtServer == null || tgtServer.trim().length() == 0)
         throw new IllegalArgumentException(
            "tgtServer may not be null or empty");
      
      return getArchiveSummaries(tgtServer, null);
   }

   /**
    * Gets all <code>PSArchiveSummary</code> objects from the log tables for
    * a given target server, and optionally for a specific archive ref.
    *
    * @param tgtServer The target server, which is the combination of hostname
    * and port number, as in "hostname":"port". May be <code>null</code> to use
    * the local server name and port, never empty.
    * or empty.
    * @param archiveRef The archive ref, may be <code>null</code>, never empty.
    *
    * @return An iterator over zero or more <code>PSArchiveSummary</code>
    * objects. It will never be <code>null</code>, but may be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   public Iterator getArchiveSummaries(String tgtServer, String archiveRef)
        throws PSDeployException
   {
      if (tgtServer != null && tgtServer.trim().length() == 0)
         throw new IllegalArgumentException("tgtServer may not be empty");

      if (archiveRef != null && archiveRef.trim().length() == 0)
         throw new IllegalArgumentException("archiveRef may not be empty");
      
      List<PSArchiveSummary> asList = new ArrayList<PSArchiveSummary>();

      if (tgtServer == null)
         tgtServer = PSServer.getHostName() + ":" + PSServer.getListenerPort();
      
      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(ALS_TGT_SERVER_NAME,
         PSJdbcSelectFilter.EQUALS, tgtServer, Types.VARCHAR);

      if (archiveRef != null)
      {
         PSJdbcFilterContainer container = new PSJdbcFilterContainer();
         container.doAND(filter);
         filter = new PSJdbcSelectFilter(ALS_ARCHIVE_REF,
            PSJdbcSelectFilter.EQUALS, archiveRef, Types.VARCHAR);
         container.doAND(filter);
         filter = container;
      }
      
      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(
         m_archiveLogSummarySchema, null, filter);

      // collect the result set if any
      if (tData != null)
      {
         Iterator rows = tData.getRows();
         while ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();
            asList.add(getArchiveSummary(row));
         }
      }
      return asList.iterator();
   }

   /**
    * Get the most recent archive summary for the specified archive ref from the
    * server.
    * 
    * @param archiveRef The name used to identify the archive for which the most
    * recent log will be retrieved.  May not be <code>null</code> or empty, and
    * must refer to an existing archive file on the server.  
    * 
    * @return The retrieved archive summary object. It may be <code>null</code>
    * if cannot find a record with the given archive reference.
    * 
    * @throws IllegalArgumentException if <code>archiveRef</code> is 
    * <code>null</code> or empty.
    * @throws PSDeployException if any other errors occur.
    */
   public PSArchiveSummary getArchiveSummary(String archiveRef) 
      throws PSDeployException
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");

      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(ALS_ARCHIVE_REF,
         PSJdbcSelectFilter.EQUALS, archiveRef, Types.VARCHAR);

      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(
         m_archiveLogSummarySchema, null, filter);

      PSArchiveSummary archiveSummary = null;
      // collect the result set if any
      if (tData != null)
      {
         Date latestDate = null;
         PSJdbcRowData latestRow = null;
         Iterator rows = tData.getRows();
         while ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();
            Date date = m_dbmsHandle.getColumnDate(ALS_TABLE_NAME, 
               ALS_INSTALL_DATE, row);
            if ((latestDate == null) || (date.compareTo(latestDate) > 0))
            {
               date = latestDate;
               latestRow = row;
            }
         }
         archiveSummary = getArchiveSummary(latestRow);
      }
      return archiveSummary;
   }   

   /**
    * Gets a <code>PSArchiveSummary</code> object, which is specified by a
    * given identifier of the archive summary table.
    *
    * @param archiveId The identifier of the archive summary table.
    *
    * @return The specified <code>PSArchiveSummary</code> object. It may be
    * <code>null</code> if cannot find in the archive log summary table.
    *
    * @throws PSDeployException if any error occurs.
    */
   public PSArchiveSummary getArchiveSummary(int archiveId)
      throws PSDeployException
   {
      PSArchiveSummary arSummary = null;

      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(ALS_ARCHIVE_LOG_ID,
         PSJdbcSelectFilter.EQUALS, Integer.toString(archiveId), Types.INTEGER);

      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(
         m_archiveLogSummarySchema, null, filter);

      if (tData != null)
      {
         Iterator rows = tData.getRows();
         if ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();
            arSummary = getArchiveSummary(row);
         }
      }

      return arSummary;
   }

   /**
    * Gets a <code>PSArchiveSummary</code> object from a given row data.
    *
    * @param row The row data, retrieved record from archive log summary table,
    * assume not <code>null</code>.
    *
    * @return The retrieved archive summary object, never <code>null</code>.
    *
    * @throws PSDeployException if any error occurs
    */
   private PSArchiveSummary getArchiveSummary(PSJdbcRowData row)
      throws PSDeployException
   {
      int archiveId = m_dbmsHandle.getColumnInt(ALS_TABLE_NAME,
         ALS_ARCHIVE_LOG_ID, row);

      Date dateTime = m_dbmsHandle.getColumnDate(ALS_TABLE_NAME,
         ALS_INSTALL_DATE, row);

      PSArchiveInfo archiveInfo = (PSArchiveInfo) getColumnClob(ALS_TABLE_NAME,
         ALS_ARCHIVE_INFO, row, PSArchiveInfo.class);
      PSArchiveManifest archiveMan = (PSArchiveManifest) getColumnClob(
         ALS_TABLE_NAME, ALS_ARCHIVE_MANIFEST, row, PSArchiveManifest.class);

      Iterator pkgList = getArchivePackages(archiveId);

      PSArchiveSummary arSummary = new PSArchiveSummary(archiveInfo, dateTime,
         pkgList);
      arSummary.setArchiveManifest(archiveMan);
      arSummary.setId( m_dbmsHandle.getColumnInt(ALS_TABLE_NAME,
         ALS_ARCHIVE_LOG_ID, row) );

      return arSummary;
   }

   /**
    * Gets a list of archive packages from the archive package table for the
    * specified archive summary id.
    *
    * @param archiveId The archive summary id.
    *
    * @return An iterator over zero or more <code>PSArchivePackage</code>
    * objects, never <code>null</code>, but may be empty.
    *
    * @throws PSDeployException if any error occurs.
    */
   private Iterator getArchivePackages(int archiveId) throws PSDeployException
   {
      List<PSArchivePackage> pkgList = new ArrayList<PSArchivePackage>();

      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(AP_ARCHIVE_LOG_ID,
         PSJdbcSelectFilter.EQUALS, Integer.toString(archiveId), Types.INTEGER);

      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(
         m_archivePackageSchema, null, filter);

      if (tData != null)
      {
         Iterator rows = tData.getRows();
         while ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();
            String name = m_dbmsHandle.getColumnString(AP_TABLE_NAME,
               AP_PACKAGE_NAME, row);
            String type = m_dbmsHandle.getColumnString(AP_TABLE_NAME,
               AP_PACKAGE_TYPE, row);
            int status = m_dbmsHandle.getColumnInt(AP_TABLE_NAME,
               AP_STATUS, row);
            int logId = m_dbmsHandle.getColumnInt(AP_TABLE_NAME,
               AP_LOG_SUMMARY_ID, row);
               
            PSArchivePackage pkg = new PSArchivePackage(name, type, status, 
               logId);
            pkgList.add(pkg);
         }
      }
      return pkgList.iterator();
   }


   /**
    * Gets all log summary objects from the log tables for a given target
    * server.
    *
    * @param tgtServer The target server, which is the combination of hostname
    * and port number, as in "hostname":"port". It may not be <code>null</code>
    * or empty.
    *
    * @return An iterator over zero or more <code>PSLogSummary</code> objects.
    * It will not be <code>null</code>, but may be empty.  The summaries will 
    * not contain <code>PSLogDetail</code> objects.
    *
    * @throws IllegalArgumentException if <code>tgtServer</code> is not valid.
    * @throws PSDeployException if any error occurs.
    */
   public Iterator getLogSummaries(String tgtServer) throws PSDeployException
   {
      if (tgtServer == null || tgtServer.trim().length() == 0)
         throw new IllegalArgumentException(
            "tgtServer may not be null or empty");

      List<PSLogSummary> logSmryList = new ArrayList<PSLogSummary>();

      PSJdbcTableData tableData = getLogSummariesTableData(tgtServer);

      if (tableData != null)
      {
         Iterator rows = tableData.getRows();
         while ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();
            logSmryList.add(getLogSummary(row, false));
         }
      }
      return logSmryList.iterator();
   }

   /**
    * Gets a list of log summary records (in a table data from the log summary
    * table), where the target server column of each record equals the
    * specified <code>tgtServer</code>.
    *
    * @param tgtServer The name of the target server, assume not
    * <code>null</code> or empty.
    *
    * @return The table data object, it may be <code>null</code> if cannot find
    * any qualified record.  Will not contain the columns required to create a 
    * {@link PSLogDetail} object.
    *
    * @throws PSDeployException if an error occurs.
    */
   private PSJdbcTableData getLogSummariesTableData(String tgtServer)
      throws PSDeployException
   {
      StringBuffer sArchiveIds = new StringBuffer(0);

      // get a list of archive summary id from archive log summary table first.
      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(ALS_TGT_SERVER_NAME,
         PSJdbcSelectFilter.EQUALS, tgtServer, Types.VARCHAR);

      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(
         m_archiveLogSummarySchema, new String[] {ALS_ARCHIVE_LOG_ID}, filter);         

      // build the IN clause for later use.
      if (tData != null)
      {
         Iterator rows = tData.getRows();
         String archiveId;
         while ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();
            archiveId = m_dbmsHandle.getColumnString(ALS_TABLE_NAME,
               ALS_ARCHIVE_LOG_ID, row);
            if ( sArchiveIds.length() == 0 )
               sArchiveIds.append("(" + archiveId);
            else
               sArchiveIds.append("," + archiveId);
         }
         sArchiveIds.append(")");
      }
      if ( sArchiveIds.length() != 0 )
      {
         // now get the specified data from log summary table
         filter = new PSJdbcSelectFilter(LS_ARCHIVE_LOG_ID,
            PSJdbcSelectFilter.IN, sArchiveIds.toString(), Types.INTEGER);

         return m_dbmsHandle.catalogTableData(m_logSummarySchema, 
            LS_SUM_COLS_NO_DETAIL, filter);
      }
      else
      {
         return null; // cannot find any
      }
   }

   /**
    * Convenience method that calls 
    * {@link #getLogSummary(int, boolean) getLogSummary(logId, true)} to return
    * the full log summary including the log detail.
    */
   public PSLogSummary getLogSummary(int logId) throws PSDeployException
   {
      return getLogSummary(logId, true);
   }

   /**
    * Get a log summary object, which is specified by an id of the log summary
    * table.
    *
    * @param logId The id used to locate the log in the repository.
    * @param includeDetail <code>true</code> to include the {@link PSLogDetail}, 
    * <code>false</code> if not.  
    *
    * @return The specified <code>PSLogSummary</code> object. It may be
    * <code>null</code> if cannot find any from the log summary table.
    *
    * @throws PSDeployException if any error occurs.
    */
   public PSLogSummary getLogSummary(int logId, boolean includeDetail) 
      throws PSDeployException
   {
      PSLogSummary logSummary = null;

      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(LS_LOG_SUMMARY_ID,
         PSJdbcSelectFilter.EQUALS, Integer.toString(logId), Types.INTEGER);

      String[] cols = includeDetail ? null : LS_SUM_COLS_NO_DETAIL;
      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(
         m_logSummarySchema, cols, filter);

      if (tData != null)
      {
         Iterator rows = tData.getRows();
         if ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();
            logSummary = getLogSummary(row, includeDetail);
         }
      }
      return logSummary;
   }

   
   /**
    * Creates a log summary object from a row data, which is retrieved from the
    * log summary table.
    *
    * @param row The row data, assume not <code>null</code>.
    * @param includeDetail <code>true</code> to load the {@link PSLogDetail}, 
    * <code>false</code> if not.  If <code>true</code>, the <code>row</code>
    * is assumed to contain the required column data.
    *
    * @return A <code>PSLogSummary</code> object, never be <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSLogSummary getLogSummary(PSJdbcRowData row, boolean includeDetail)
      throws PSDeployException
   {

      PSDeployableElement pkg = (PSDeployableElement) getColumnClob(
         LS_TABLE_NAME, LS_PACKAGE_DETAIL, row, PSDeployableElement.class);
      int archiveId = m_dbmsHandle.getColumnInt(LS_TABLE_NAME,
         LS_ARCHIVE_LOG_ID, row);
      int logId = m_dbmsHandle.getColumnInt(LS_TABLE_NAME, LS_LOG_SUMMARY_ID,
         row);

      PSArchiveSummary arSummary = getArchiveSummary(archiveId);
      PSLogSummary logSummary = new PSLogSummary(pkg, arSummary);      
      logSummary.setId(logId);


      // load detail if required
      if (includeDetail)
      {
         // get the dbms map, which could be null
         PSDbmsMap dbmsMap = (PSDbmsMap) getColumnClobNullable(LS_TABLE_NAME,
            LS_DBMS_MAP, row, PSDbmsMap.class);

         // get the id map, which could be null
         PSIdMap idMap = (PSIdMap) getColumnClobNullable(LS_TABLE_NAME, 
            LS_ID_MAP, row, PSIdMap.class);;

         PSValidationResults vr = (PSValidationResults) getColumnClob(
            LS_TABLE_NAME, LS_VALIDATION_RESULTS, row, 
            PSValidationResults.class);

         PSTransactionLogSummary txns = getTxnLogSummary(logId);

         PSLogDetail logDetail = new PSLogDetail(vr, idMap, dbmsMap, txns);
         logSummary.setLogDetail(logDetail);
      }

      return logSummary;
   }

   /**
    * Gets a <code>PSTransactionLogSummary</code> object, which is specified
    * by an id of the log summary table.
    *
    * @param logId The id of the log summary table.
    *
    * @return The specified <code>PSTransactionLogSummary</code> object. It
    * will never be <code>null</code>, but may be empty if cannot find any
    * in the log transaction table.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSTransactionLogSummary getTxnLogSummary(int logId)
      throws PSDeployException
   {
      PSTransactionLogSummary txns = new PSTransactionLogSummary();

      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(TXN_LOG_SUMMARY_ID,
         PSJdbcSelectFilter.EQUALS, Integer.toString(logId), Types.INTEGER);

      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(
         m_logTxnSchema, null, filter);

      if (tData != null)
      {
         // build sorted map
         TreeMap<Integer, PSTransactionSummary> txnMap = 
                                 new TreeMap<Integer, PSTransactionSummary>();
         boolean hasSeq = true;
         int seq = 0;
         
         Iterator rows = tData.getRows();
         while ( rows.hasNext() )
         {
            PSJdbcRowData row = (PSJdbcRowData) rows.next();

            String depDesc = m_dbmsHandle.getColumnString(TXN_TABLE_NAME,
               TXN_DEPENDENCY, row);
            String name = m_dbmsHandle.getColumnString(TXN_TABLE_NAME,
               TXN_ELEMENT_NAME, row);
            String type = m_dbmsHandle.getColumnString(TXN_TABLE_NAME,
               TXN_ELEMENT_TYPE, row);
            int action = m_dbmsHandle.getColumnInt(TXN_TABLE_NAME,
               TXN_ACTION_TOOK, row);

            PSTransactionSummary txn = new PSTransactionSummary(logId, depDesc,            
               name, type, action);
               
            // we'll either have a sequence or not, to support backward compat            
            if (hasSeq)
            {
               try
               {
                  seq = m_dbmsHandle.getColumnInt(TXN_TABLE_NAME, TXN_SEQUENCE, 
                     row);
               }
               catch (PSDeployException e)
               {
                  // only try once if we don't have it
                  hasSeq = false;
               }
            }
            
            // if not using sequence, maintain order from db
            if (!hasSeq)
               seq++;
            
            txnMap.put(new Integer(seq), txn);            
         }
         
         Iterator transactions = txnMap.values().iterator();
         while (transactions.hasNext())
            txns.addTransaction((PSTransactionSummary)transactions.next());
      }
      return txns;
   }

   /**
    * Gets a <code>IPSDeployComponent</code> object from the given parameters.
    *
    * @param table The table name, assume not <code>null</code> or empty.
    * @param column The column name, assume not <code>null</code> or empty.
    * @param row The row data, which contains XML document (as clob) of the to
    * be retrieved object, assume not <code>null</code>.
    * @param compClass The <code>Class</code> of the to be retrieved object,
    * assume never <code>null</code>.
    *
    * @return The <code>IPSDeployComponent</code> object, will never be
    * <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private IPSDeployComponent getColumnClob(String table, String column,
      PSJdbcRowData row, Class compClass) throws PSDeployException
   {
      String xml = m_dbmsHandle.getColumnString(table, column, row);

      StringReader sReader = new StringReader(xml);

      IPSDeployComponent component = null;
      try
      {
         Document resultDoc = PSXmlDocumentBuilder.createXmlDocument(sReader,
             false);
         Element root = resultDoc.getDocumentElement();

         Constructor compCtor = compClass.getConstructor( new Class[]
            { Element.class });
         component = (IPSDeployComponent) compCtor.newInstance(
            new Object[] {root} );
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      return component;
   }

   /**
    * Gets a <code>IPSDeployComponent</code> object from the given parameters.
    * It does the same as the <code>getColumnClob()</code>, except this method
    * accept the value of the given column to be <code>null</code>.
    *
    * @return A <code>IPSDeployComponent</code> object, it may be
    * <code>null</code> if the specified column contains <code>null</code> value
    *
    * See {@link #getColumnClob(String, String, PSJdbcRowData, Class)} for
    * detail info.
    */
   private IPSDeployComponent getColumnClobNullable(String table, String column,
      PSJdbcRowData row, Class compClass) throws PSDeployException
   {
      IPSDeployComponent comp = null;

      PSJdbcColumnData cdata= row.getColumn(column);

      if (cdata == null) // the column not exist
      {
         Object[] args = {table, column, "null"};
         throw new PSDeployException(
             IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
      }
      if ( cdata.getValue() != null && cdata.getValue().trim().length() != 0 )
         comp = getColumnClob(table, column, row, compClass);

      return comp;
   }

   /**
    * Deletes a row in the archive log summary table, where the
    * <code>ARCHIVE_LOG_ID</code> column equals <code>archiveId</code>..
    *
    * @param archiveId The id of the archive summary.
    *
    * @throws PSDeployException if an error occurs.
    */
   private void deleteFromArchiveSummary(int archiveId) throws PSDeployException
   {
      deleteArchiveIdFromTable(archiveId, ALS_ARCHIVE_LOG_ID, ALS_TABLE_NAME,
         m_archiveLogSummarySchema);
   }

   /**
    * Deletes all rows in the archive package table, where the
    * <code>ARCHIVE_LOG_ID</code> column of each row equals
    * <code>archiveId</code>.
    *
    * @param archiveId The id of the archive summary.
    *
    * @throws PSDeployException if an error occurs.
    */
   private void deleteFromArchivePackage(int archiveId)
      throws PSDeployException
   {
      deleteArchiveIdFromTable(archiveId, AP_ARCHIVE_LOG_ID, AP_TABLE_NAME,
         m_archivePackageDeleteSchema);
   }

   /**
    * Deletes all rows (in the given <code>table</code>), where the
    * <code>column</code> of each row is <code>archiveId</code>.
    *
    * @param archiveId The id of the archive summary.
    * @param column The column name, assume not <code>null</code> or empty.
    * @param table The table name, assume not <code>null</code> or empty.
    * @param schema The schema, assume not <code>null</code>.
    *
    * @throws PSDeployException if an error occurs.
    */
   private void deleteArchiveIdFromTable(int archiveId, String column,
      String table, PSJdbcTableSchema schema) throws PSDeployException
   {
      List<PSJdbcRowData> rowList = new ArrayList<PSJdbcRowData>();

      PSJdbcRowData rowData = m_dbmsHandle.getRowDataForOneColumn(column,
         Integer.toString(archiveId), PSJdbcRowData.ACTION_DELETE);
      rowList.add(rowData);

      PSJdbcTableData tblData;
      tblData = new PSJdbcTableData(table, rowList.iterator(), false);

      m_dbmsHandle.processTable(schema, tblData);
   }

   /**
    * Deletes all archive logs and their related package logs for the specified 
    * archive.
    * 
    * @param archiveRef The name of the archive on the server, may not be
    * <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>archiveRef</code> is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public void deleteAllLogs(String archiveRef) throws PSDeployException
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");
      
      // get all summaries for this ref
      Iterator summmaries = getArchiveSummaries(null, archiveRef);
      while (summmaries.hasNext())
      {
         PSArchiveSummary sum = (PSArchiveSummary)summmaries.next();
         int archiveId = sum.getId();
         
         // walk the packages and delete their logs
         Iterator pkgs = sum.getPackageList();
         while (pkgs.hasNext())
         {
            PSArchivePackage pkg = (PSArchivePackage)pkgs.next();
            if (pkg.isInstalled())
               deleteLog(pkg.getLogId(), false);
         }
         
         // delete archive package logs and the archive summary log
         deleteFromArchivePackage(archiveId);
         deleteFromArchiveSummary(archiveId);
      }
   }
   
   /**
    * Deletes the specified records from both log summary and log transaction
    * tables for a id of the log summary. Optionally, if the deleted package log 
    * is the last one referencing its archive summary, the summary is deleted as 
    * well.
    *
    * @param logId The id of the log summary.
    * @param cascade If <code>true</code>, the delete is cascaded to the archive
    * summary and archive package entries, otherwise only the log summary and 
    * log transaction entries are deleted.
    *
    * @return <code>null</code> if the deletion only happened in log summary
    * and transaction tables; archive reference (a non-empty
    * <code>String</code>) if the deletion also affected archive summary and
    * package tables.
    *
    * @throws PSDeployException if any error occurs.
    */
   private String deleteLog(int logId, boolean cascade) throws PSDeployException
   {
      String result = null;
      
      deleteInLogTxn(logId);
      PSLogSummary logSummary = getLogSummary(logId, false);
      if (logSummary != null)
      {
         int archiveId = logSummary.getArchiveSummary().getId();
         String archiveRef =
            logSummary.getArchiveSummary().getArchiveInfo().getArchiveRef();
   
         deleteInLogSummary(logId);
         
         if (cascade && !archiveIdExistInLogSummary(archiveId))
         {
            deleteFromArchivePackage(archiveId);
            deleteFromArchiveSummary(archiveId);
   
            result = archiveRef;
         }
      }
      
      return result;
      
   }

   /**
    * Determines if a given id of archive summary exists in the
    * <code>LS_ARCHIVE_LOG_ID</code> column in the log summary table.
    *
    * @param archiveId The id of archive summary.
    *
    * @return <code>true</code> if found a row contains the searched id;
    * <code>false</code> otherwise.
    *
    * @throws PSDeployException if any error occurs.
    */
   private boolean archiveIdExistInLogSummary(int archiveId)
      throws PSDeployException
   {
      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(LS_ARCHIVE_LOG_ID,
         PSJdbcSelectFilter.EQUALS, Integer.toString(archiveId), Types.INTEGER);

      PSJdbcTableData tData = m_dbmsHandle.catalogTableData(
         m_logSummarySchema, new String[]{LS_LOG_SUMMARY_ID}, filter);

      return ((tData != null) && tData.getRows().hasNext());
   }

   /**
    * Determines if a archive reference exist in the archive summary table.
    *
    * @param archiveRef The to be searched archive reference, may not be
    * <code>null</code> or empty.
    *
    * @return <code>true</code> if the archive reference exist in the
    * archive summary table; <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>archiveRef</code> is invalid.
    * @throws PSDeployException if any error occurs.
    */
   public boolean doesArchiveRefExist(String archiveRef)
      throws PSDeployException
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");

      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(ALS_ARCHIVE_REF,
         PSJdbcSelectFilter.EQUALS, archiveRef, Types.VARCHAR);

      PSJdbcTableData tableData = m_dbmsHandle.catalogTableData(
         m_archiveLogSummarySchema, new String[]{ALS_ARCHIVE_LOG_ID}, filter);

      return ((tableData != null) && tableData.getRows().hasNext());
   }

   /**
    * Deletes the specified records from log summary table for a id of the
    * log summary.
    *
    * @param logId The id of the log summary.
    *
    * @throws PSDeployException if any error occurs.
    */
   private void deleteInLogSummary(int logId) throws PSDeployException
   {

      PSJdbcRowData rowData = m_dbmsHandle.getRowDataForOneColumn(
         LS_LOG_SUMMARY_ID, Integer.toString(logId),
         PSJdbcRowData.ACTION_DELETE);

      m_dbmsHandle.processTable(m_logSummarySchema, LS_TABLE_NAME, rowData);
   }

   /**
    * Deletes the specified records from log transaction table for a id of
    * the log summary.
    *
    * @param logId The id of the log summary.
    *
    * @throws PSDeployException if any error occurs.
    */
   private void deleteInLogTxn(int logId) throws PSDeployException
   {
      PSJdbcRowData rowData = m_dbmsHandle.getRowDataForOneColumn(
         TXN_LOG_SUMMARY_ID, Integer.toString(logId),
         PSJdbcRowData.ACTION_DELETE);

      // set the update key for deletion since there is no primary key
      m_dbmsHandle.setUpdateKeyForSchema(TXN_LOG_SUMMARY_ID, m_logTxnSchema);

      // invoke the delete process
      m_dbmsHandle.processTable(m_logTxnSchema, TXN_TABLE_NAME, rowData);
   }

   /**
    * Inserts the given parameters into the archive summary and
    * archive package tables.
    *
    * @param archiveInfo The to be inserted archive information. It may not be
    * <code>null</code>, it should also contain the archive detail.
    * @param archiveManifest The to be inserted archive manifest. It may not be
    * <code>null</code>.
    *
    * @return The archive id of new entry in the archive log summary table.
    *
    * @throws PSDeployException if any error occurs.
    */
   public int createArchiveLog(PSArchiveInfo archiveInfo,
      PSArchiveManifest archiveManifest) throws PSDeployException
   {
      if (archiveInfo == null || archiveInfo.getArchiveDetail() == null)
         throw new IllegalArgumentException(
            "archiveInfo and its getArchiveDetail() may not be null");

      if (archiveManifest == null)
         throw new IllegalArgumentException("archiveManifest may not be null");

      // make sure not to have archive detail from the archive info object.
      PSArchiveInfo savedInfo = new PSArchiveInfo(archiveInfo);
      savedInfo.setArchiveDetail(null);

      int id = m_dbmsHandle.getNextId(ALS_TABLE_NAME);

      // insert to the archive package table first
      PSJdbcTableData tableData = getTableDataForAddPackages(id,
         archiveInfo.getArchiveDetail().getPackages());
      m_dbmsHandle.processTable(m_archivePackageSchema, tableData);

      // insert to the archive sumary table afterwards
      tableData = getTableDataForSaveArchiveSummary(id, savedInfo,
         archiveManifest);
      m_dbmsHandle.processTable(m_archiveLogSummarySchema, tableData);

      return id;
   }

   /**
    * Creates <code>PSJdbcTableData</code> object for inserting to archive
    * summary table from the given parameters.
    *
    * @param id The id of the archive log summary table.
    * @param archiveInfo The archive info object, assume not <code>null</code>.
    * @param archiveManifest The archive manifest object, assume not
    * <code>null</code>.
    *
    * @return The to be inserted <code>PSJdbcTableData</code> object. It will
    * never be <code>null</code>.
    */
   private PSJdbcTableData getTableDataForSaveArchiveSummary(int id,
      PSArchiveInfo archiveInfo, PSArchiveManifest archiveManifest)
   {
      // prepare the row data
      List<PSJdbcColumnData> cols = new ArrayList<PSJdbcColumnData>();

      PSJdbcColumnData col = new PSJdbcColumnData(ALS_ARCHIVE_LOG_ID,
         Integer.toString(id));
      cols.add(col);

      Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
      col = new PSJdbcColumnData(ALS_INSTALL_DATE, timeStamp.toString());
      cols.add(col);

      col = new PSJdbcColumnData(ALS_ARCHIVE_REF, archiveInfo.getArchiveRef());
      cols.add(col);

      col = new PSJdbcColumnData(ALS_USER_ID, archiveInfo.getUserName());
      cols.add(col);

      col = new PSJdbcColumnData(ALS_SRC_SERVER_NAME,
         archiveInfo.getServerName());
      cols.add(col);

      col = new PSJdbcColumnData(ALS_SRC_SERVER_VERSION,
         archiveInfo.getServerVersion());
      cols.add(col);

      col = new PSJdbcColumnData(ALS_SRC_SERVER_BUILD_ID,
         archiveInfo.getServerBuildId());
      cols.add(col);

      col = new PSJdbcColumnData(ALS_TGT_SERVER_NAME,
         PSServer.getHostName() + ":" + PSServer.getListenerPort());
      cols.add(col);

      String sqlDatePattern = "yyyy-mm-dd hh:mm:ss"; // JDBC Date format.
      SimpleDateFormat dateFormat = new SimpleDateFormat(sqlDatePattern);
      col = new PSJdbcColumnData(ALS_SRC_SERVER_BUILD_DATE,
         dateFormat.format(archiveInfo.getServerBuildDate()));
      cols.add(col);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element infoEl = archiveInfo.toXml(doc);
      col = new PSJdbcColumnData(ALS_ARCHIVE_INFO,
          PSXmlDocumentBuilder.toString(infoEl));
      cols.add(col);

      Element element = archiveManifest.toXml(doc);
      col = new PSJdbcColumnData(ALS_ARCHIVE_MANIFEST,
          PSXmlDocumentBuilder.toString(element));
      cols.add(col);

      PSJdbcRowData rData = new PSJdbcRowData(cols.iterator(),
         PSJdbcRowData.ACTION_INSERT);

      // prepare the table data
      List<PSJdbcRowData> rDataList = new ArrayList<PSJdbcRowData>();
      rDataList.add(rData);
      PSJdbcTableData tData = new PSJdbcTableData(ALS_TABLE_NAME,
         rDataList.iterator(), false);

      return tData;
   }

   /**
    * Updates a package status for both archive package and log summary tables
    * from the given parameters.
    *
    * @param archiveId The id for archive log summary table, may not < 0.
    * @param logId The id of log summary table, may not < 0.
    * @param pkgType The package type, may not <code>null</code> or empty.
    * @param pkgId The package id, may not <code>null</code> or empty.
    * @param status The to be updated status for the package. It must be one of
    * the <code>PSArchivePackage.STATUS_XXX</code> values.
    *
    * @throws PSDeployException if an error occurs.
    */
   public void updatePackageStatus(int archiveId, int logId, String pkgType,
      String pkgId, int status) throws PSDeployException
   {
       if (archiveId < 0)
         throw new IllegalArgumentException("archiveId may not be < 0");
       if (logId < 0)
         throw new IllegalArgumentException("logId may not be < 0");
       if (pkgType == null || pkgType.trim().length() == 0)
         throw new IllegalArgumentException("pkgType may not be null or empty");
       if (pkgId == null || pkgId.trim().length() == 0)
         throw new IllegalArgumentException("pkgId may not be null or empty");
      if (! PSArchivePackage.validateStatus(status))
         throw new IllegalArgumentException("status value is not a valid");

      updatePkgStatusForLogSummary(logId, pkgType, pkgId, status);

      updatePkgStatusForArchivePackage(archiveId, pkgType, pkgId, status, 
         logId);
   }

   /**
    * Updates a package status for log summary table from the given parameters.
    *
    * @param logId The id of log summary table, assume >= 0.
    * @param pkgType The package type, assume not <code>null</code> or empty.
    * @param pkgId The package id, assume not <code>null</code> or empty.
    * @param status The to be updated status for the package. Assume is one of
    * the <code>PSArchivePackage.STATUS_XXX</code> values.
    *
    * @throws PSDeployException if an error occurs.
    */
   private void updatePkgStatusForLogSummary(int logId, String pkgType,
      String pkgId, int status) throws PSDeployException
   {
      // prepare the row data
      List<PSJdbcColumnData> cols = new ArrayList<PSJdbcColumnData>();
      PSJdbcColumnData col = new PSJdbcColumnData(LS_LOG_SUMMARY_ID,
         Integer.toString(logId));
      cols.add(col);
      col = new PSJdbcColumnData(LS_PACKAGE_TYPE, pkgType);
      cols.add(col);
      col = new PSJdbcColumnData(LS_PACKAGE_ID, pkgId);
      cols.add(col);
      col = new PSJdbcColumnData(LS_STATUS, Integer.toString(status));
      cols.add(col);

      PSJdbcRowData rowData = new PSJdbcRowData(cols.iterator(),
         PSJdbcRowData.ACTION_UPDATE);

      // invoke the update process
      m_dbmsHandle.processTable(m_logSummarySchema, LS_TABLE_NAME, rowData);
   }

   /**
    * Updates a package status for archive summary table from the given
    * parameters.
    *
    * @param archiveId The id for archive summary table, assume >= 0.
    * @param pkgType The package type, assume not <code>null</code> or empty.
    * @param pkgId The package id, assume not <code>null</code> or empty.
    * @param status The to be updated status for the package. Assume is one of
    * the <code>PSArchivePackage.STATUS_XXX</code> values.
    * @param logId The id for log summary table, assume >= 0.
    *
    * @throws PSDeployException if an error occurs.
    */
   private void updatePkgStatusForArchivePackage(int archiveId, String pkgType,
      String pkgId, int status, int logId) throws PSDeployException
   {
      // prepare the row data
      List<PSJdbcColumnData> cols = new ArrayList<PSJdbcColumnData>();
      PSJdbcColumnData col = new PSJdbcColumnData(AP_ARCHIVE_LOG_ID,
         Integer.toString(archiveId));
      cols.add(col);
      col = new PSJdbcColumnData(AP_PACKAGE_TYPE, pkgType);
      cols.add(col);
      col = new PSJdbcColumnData(AP_PACKAGE_ID, pkgId);
      cols.add(col);
      col = new PSJdbcColumnData(AP_STATUS, Integer.toString(status));
      cols.add(col);
      col = new PSJdbcColumnData(AP_LOG_SUMMARY_ID, Integer.toString(logId));
      cols.add(col);
   
      PSJdbcRowData rowData = new PSJdbcRowData(cols.iterator(),
         PSJdbcRowData.ACTION_UPDATE);

      // invoke the update process
      m_dbmsHandle.processTable(m_archivePackageSchema, AP_TABLE_NAME, rowData);
   }

   /**
    * Inserts an entry into the log summary table from the given parameters.
    *
    * @param archiveLogId The id of the archive log summary table. It may not
    * be less than 0.
    * @param pkg The package object, may not be <code>null</code>, and it
    * must contains a non-<code>null</code> valitation results object.
    * @param importCtx The import context object, may not be <code>null</code>.
    *
    * @return The id of the inserted record in the log summary table.
    *
    * @throws PSDeployException if any error occurs.
    */
   public int createPackageLog(int archiveLogId, PSImportPackage pkg,
      PSImportCtx importCtx) throws PSDeployException
   {
       if (archiveLogId < 0)
         throw new IllegalArgumentException("archiveLogId may not be < 0");
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");
      if (importCtx == null)
         throw new IllegalArgumentException("importCtx may not be null");

      if (pkg.getValidationResults() == null)
      {
         Object[] args = {pkg.getPackage().getDisplayName()};
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_VALIDATION_RESULTS, args);
      }

      PSIdMap idMap = importCtx.getCurrentIdMap();
      int logId = m_dbmsHandle.getNextId(LS_TABLE_NAME);

      createPackageLog(logId, archiveLogId, pkg.getPackage(),
         pkg.getValidationResults(), idMap, importCtx.getdbmsMap());

      return logId;
   }

   /**
    * Inserts an entry into the log summary table from the given parameters.
    * Does the same as the
    * {@link #createPackageLog(int, PSImportPackage, PSImportCtx)}, but with
    * different set of parameters and nothing to return.
    *
    * @param logId The id of the log summary table.
    * @param archiveLogId The id of the archive log summary table.
    * @param pkg The package object, assume not <code>null</code>
    * @param vr The validation results object, assume not <code>null</code>.
    * @param idMap The ID Map object, may be <code>null</code>.
    * @param dbmsmap The DBMS Map object, assume not <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private void createPackageLog(int logId, int archiveLogId, PSDependency pkg,
      PSValidationResults vr, PSIdMap idMap, PSDbmsMap dbmsmap)
      throws PSDeployException
   {
      // prepare row data
      List<PSJdbcColumnData> cols = new ArrayList<PSJdbcColumnData>();

      PSJdbcColumnData col = new PSJdbcColumnData(LS_LOG_SUMMARY_ID,
         Integer.toString(logId));
      cols.add(col);
      col = new PSJdbcColumnData(LS_PACKAGE_NAME, pkg.getDisplayName());
      cols.add(col);
      col = new PSJdbcColumnData(LS_PACKAGE_TYPE, pkg.getObjectType());
      cols.add(col);
      col = new PSJdbcColumnData(LS_PACKAGE_ID, pkg.getDependencyId());
      cols.add(col);
      col = new PSJdbcColumnData(LS_STATUS,
         Integer.toString(PSArchivePackage.STATUS_IN_PROGRESS));
      cols.add(col);
      col = new PSJdbcColumnData(LS_ARCHIVE_LOG_ID,
         Integer.toString(archiveLogId));
      cols.add(col);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element element = pkg.toXml(doc);
      col = new PSJdbcColumnData(LS_PACKAGE_DETAIL,
         PSXmlDocumentBuilder.toString(element));
      cols.add(col);

      element = vr.toXml(doc);
      col = new PSJdbcColumnData(LS_VALIDATION_RESULTS,
         PSXmlDocumentBuilder.toString(element));
      cols.add(col);

      col = new PSJdbcColumnData(LS_ID_MAP, LS_ID_MAP);
      if ( idMap == null )
         col.setValue(null);
      else
         col.setValue(PSXmlDocumentBuilder.toString(idMap.toXml(doc)));
      cols.add(col);

      col = new PSJdbcColumnData(LS_DBMS_MAP, LS_DBMS_MAP);
      if ( dbmsmap == null )
         col.setValue(null);
      else
         col.setValue(PSXmlDocumentBuilder.toString(dbmsmap.toXml(doc)));
      cols.add(col);

      PSJdbcRowData rowData = new PSJdbcRowData(cols.iterator(),
         PSJdbcRowData.ACTION_INSERT);

      // process the prepared row data
      m_dbmsHandle.processTable(m_logSummarySchema, LS_TABLE_NAME, rowData);
   }

   /**
    * Creates table data object for inserting to archive package table from
    * the given parameters.
    *
    * @param archiveLogId The id of the archive log summary table.
    * @param pkgList The list of <code>PSDependency</code> objects, assume
    * not <code>null</code> or empty.
    *
    * @return The created table data object, never <code>null</code>.
    */
   private PSJdbcTableData getTableDataForAddPackages(int archiveLogId,
      Iterator pkgList)
   {
      List<PSJdbcRowData> rowDataList = new ArrayList<PSJdbcRowData>();
      PSJdbcRowData rowData;

      while (pkgList.hasNext())
      {
         PSDependency pkg = (PSDependency) pkgList.next();
         rowData = getRowDataForAddPackage(archiveLogId, pkg);
         rowDataList.add(rowData);
      }

      PSJdbcTableData tableData = new PSJdbcTableData(AP_TABLE_NAME,
         rowDataList.iterator(), false);

      return tableData;
   }

   /**
    * Creates a row data object for inserting to archive package table from
    * the given parameters.
    *
    * @param archiveLogId The id of archive log summary table.
    * @param pkg The <code>PSDependency</code> object, assume not
    * <code>null</code>.
    *
    * @return The row data object, never <code>null</code>.
    */
   private PSJdbcRowData getRowDataForAddPackage(int archiveLogId,
      PSDependency pkg)
   {
      List<PSJdbcColumnData> cols = new ArrayList<PSJdbcColumnData>();

      PSJdbcColumnData col = new PSJdbcColumnData(AP_ARCHIVE_LOG_ID,
         Integer.toString(archiveLogId));
      cols.add(col);

      col = new PSJdbcColumnData(AP_PACKAGE_NAME, pkg.getDisplayName());
      cols.add(col);

      col = new PSJdbcColumnData(AP_PACKAGE_TYPE, pkg.getObjectType());
      cols.add(col);

      col = new PSJdbcColumnData(AP_PACKAGE_ID, pkg.getDependencyId());
      cols.add(col);

      col = new PSJdbcColumnData(AP_STATUS,
         Integer.toString(PSArchivePackage.STATUS_IN_PROGRESS));
      cols.add(col);

      col = new PSJdbcColumnData(AP_LOG_SUMMARY_ID, Integer.toString(-1));
      cols.add(col);

      return new PSJdbcRowData(cols.iterator(), PSJdbcRowData.ACTION_INSERT);
   }

   /**
    * Inserts an entry into the log transaction table from the given parameters.
    * This is a unit testable function
    *
    * @param logId  The log id, may not be less thans zero.
    * @param depString The string for <code>DEPENDENCY</code> column, it may 
    * not be <code>null</code> or empty.
    * @param elementName The element name, it may not be <code>null</code> or 
    * empty.
    * @param elementType The element type, it may not be <code>null</code> or 
    * empty.
    * @param action the action value, it must be one of the 
    * <code>PSTransactionSummary.ACTION_xxx</code> values.
    * @param sequence The sequence number of the transaction, used to retrieve
    * the transactions in a predictable order.
    *
    * @throws IllegalArgumentException if there is any invalid parameters.
    * @throws PSDeployException if any other error occurs.
    */   
   public void addTransactionLogEntry(int logId, String depString, 
      String elementName, String elementType, int action, int sequence) 
      throws PSDeployException
   {  
      if (logId < 0)
         throw new IllegalArgumentException("logId may not be < 0");
      if (depString == null || depString.trim().length() ==0)
         throw new IllegalArgumentException(
            "depString may not be null or empty");
      if (elementName == null || elementName.trim().length() ==0)
         throw new IllegalArgumentException(
            "elementName may not be null or empty");
      if (elementType == null || elementType.trim().length() ==0)
         throw new IllegalArgumentException(
            "elementType may not be null or empty");
      if (! PSTransactionSummary.isActionValid(action))
         throw new IllegalArgumentException(
            "action must be one of the PSTransactionSummary.ACTION_XXX values");
      
      List<PSJdbcColumnData> cols = new ArrayList<PSJdbcColumnData>();
      PSJdbcColumnData col;

      // prepare column data
      col = new PSJdbcColumnData(TXN_LOG_SUMMARY_ID,
         Integer.toString(logId));
      cols.add(col);

      col = new PSJdbcColumnData(TXN_DEPENDENCY, depString);
      cols.add(col);
      
      col = new PSJdbcColumnData(TXN_ELEMENT_NAME, elementName);
      cols.add(col);

      col = new PSJdbcColumnData(TXN_ELEMENT_TYPE, elementType);
      cols.add(col);

      col = new PSJdbcColumnData(TXN_ACTION_TOOK, Integer.toString(action));
      cols.add(col);

      col = new PSJdbcColumnData(TXN_SEQUENCE, Integer.toString(sequence));
      cols.add(col);

      PSJdbcRowData rowData = new PSJdbcRowData(cols.iterator(),
         PSJdbcRowData.ACTION_INSERT);

      // invoke the insertion process
      m_dbmsHandle.processTable(m_logTxnSchema, TXN_TABLE_NAME, rowData);
   }

   /**
    * Testing all features of this class.
    *
    * @throws PSDeployException if an error occurs.
    */
   /* All tests should match test filter
   public static void unitTest() throws PSDeployException
   {
      PSLogHandler lh = new PSLogHandler();

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // Testing DPL_ARCHIVE_LOG_SUMMARY and DPL_ARCHIVE_PACKAGE tables
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

      // prepare a PSArchiveInfo
      PSArchiveInfo srcInfo = PSArchiveInfoTest.getArchiveInfo(true);
      PSArchiveManifest srcManifest = new PSArchiveManifest();

      int archiveLogId = lh.createArchiveLog(srcInfo, srcManifest);
      PSArchiveSummary arSummary = lh.getArchiveSummary(archiveLogId);

      // comparing the data between original and retrieved
      Iterator srcPkgIter = srcInfo.getArchiveDetail().getPackages();
      Iterator tgtPkgIter = arSummary.getPackageList();
      List srcPkgList = PSDeployComponentUtils.cloneList(srcPkgIter);
      List tgtPkgList = PSDeployComponentUtils.cloneList(tgtPkgIter);
      // srcPkgList is a list of PSDeploymentElement objects
      // tgtPkgList is a list of PSArchivePackage objects
      // they should have the same size, but not the same list of objects.
      boolean bEqualPkgList = srcPkgList.size() == tgtPkgList.size();

      String tgtSrv = PSServer.getHostName() + ":" + PSServer.getListenerPort();
      Iterator asIter = lh.getArchiveSummaries(tgtSrv);
      List asList = PSDeployComponentUtils.cloneList(asIter);

      PSArchiveManifest tgtManifest = arSummary.getArchiveManifest();
      boolean bEqualMan = srcManifest.equals(tgtManifest);
      if ((!bEqualMan) || (!bEqualPkgList) || asList.size()<=0)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            "Error retrieved archive info or manifest");
      }

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // Testing DPL_LOG_SUMMARY table
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

      PSDeployableElement pkg = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element",
         "myTestElement", true, false, false);
      PSValidationResult vResult =
         PSValidationResultTest.getValidationResult2();
      PSValidationResults results = new PSValidationResults();
      results.addResult(vResult);

      PSIdMapping idMapping =
         new PSIdMapping("sourceId_1", "sourceName_1", "type_1");
      PSIdMap idMap = new PSIdMap("sourceServer");
      idMap.addMapping(idMapping);

      PSDbmsInfo dbms1 = new PSDbmsInfo("driver", "server", "db", "orig",
         "uid", "pwd", false);
      PSDbmsInfo dbms2 = new PSDbmsInfo("driver2", "server2", "db", "orig",
         "uid", "pwd", false);
      PSDbmsMap dbmsMap = new PSDbmsMap("sourceServer");
      PSDbmsMapping dbmsMapping1 = new PSDbmsMapping(
                           new PSDatasourceMap(dbms1.getDatasource(),
                                               dbms2.getDatasource()));
      dbmsMapping1.setTargetInfo(dbms2.getDatasource());
      dbmsMap.addMapping(dbmsMapping1);

      int logId = PSDbmsHelper.getInstance().getNextId(LS_TABLE_NAME);
      lh.createPackageLog(logId, archiveLogId, pkg, results, idMap, dbmsMap);

      int logId2 = PSDbmsHelper.getInstance().getNextId(LS_TABLE_NAME);
      lh.createPackageLog(logId2, archiveLogId, pkg, results, null, null);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/
      // Testing DPL_TXN_LOG table
      //\/\/\/\/\/\/\/\/\/\/\/\/\/

      // Testing inserting the transaction table
      lh.addTransactionLogEntry(logId, "element1", LS_TABLE_NAME,
         PSTransactionSummary.TYPE_DATA, 0, 1);
      lh.addTransactionLogEntry(logId, "element2", LS_TABLE_NAME,
         PSTransactionSummary.TYPE_SCHEMA, 1, 2);

      //\/\/\/\/\/\/\/\/\/\/\
      // Accessing all tables
      //\/\/\/\/\/\/\/\/\/\/\

      lh.updatePackageStatus(archiveLogId, logId, pkg.getObjectType(),
         pkg.getDependencyId(), PSArchivePackage.STATUS_COMPLETED);

      lh.updatePackageStatus(archiveLogId, logId, pkg.getObjectType(),
         pkg.getDependencyId(), PSArchivePackage.STATUS_ABORTED);

      lh.getLogSummary(logId);
      Iterator logIter = lh.getLogSummaries(tgtSrv);
      List logList = PSDeployComponentUtils.cloneList(logIter);
      if (logList.size()<=0)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            "Error retrieved log summary (getLogSummarys()) <= 0");
      }

      //\/\/\/\/\/\/\/\/\/\
      // Cleanup all tables
      //\/\/\/\/\/\/\/\/\/\
      String archiveRef = srcInfo.getArchiveRef();
      lh.deleteAllLogs(archiveRef);
      if ( lh.doesArchiveRefExist(archiveRef) )
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            "archiveRef should not exit in archive summary table");
   }
   */
   // String constants for archive summary (DPL_ARCHIVE_LOG_SUMMARY) table.
   // Its primary key is ALS_ARCHIVE_LOG_ID
   private static final String ALS_TABLE_NAME = "DPL_ARCHIVE_LOG_SUMMARY";
   private static final String ALS_ARCHIVE_LOG_ID = "ARCHIVE_LOG_ID";
   private static final String ALS_INSTALL_DATE = "INSTALL_DATE";
   private static final String ALS_ARCHIVE_REF = "ARCHIVE_REF";
   private static final String ALS_USER_ID = "USER_ID";
   private static final String ALS_SRC_SERVER_NAME = "SRC_SERVER_NAME";
   private static final String ALS_SRC_SERVER_VERSION = "SRC_SERVER_VERSION";
   private static final String ALS_SRC_SERVER_BUILD_ID = "SRC_SERVER_BUILD_ID";
   private static final String ALS_SRC_SERVER_BUILD_DATE =
      "SRC_SERVER_BUILD_DATE";
   private static final String ALS_TGT_SERVER_NAME = "TGT_SERVER_NAME";
   private static final String ALS_ARCHIVE_INFO = "ARCHIVE_INFO";
   private static final String ALS_ARCHIVE_MANIFEST = "ARCHIVE_MANIFEST";

   // String constants for archive package (DPL_ARCHIVE_PACKAGE) table
   // Its primary key is the combination of
   // AP_ARCHIVE_LOG_ID, AP_PACKAGE_TYPE and AP_PACKAGE_ID
   private static final String AP_TABLE_NAME = "DPL_ARCHIVE_PACKAGE";
   private static final String AP_ARCHIVE_LOG_ID = "ARCHIVE_LOG_ID";
   private static final String AP_PACKAGE_NAME = "PACKAGE_NAME";
   private static final String AP_PACKAGE_TYPE = "PACKAGE_TYPE";
   private static final String AP_PACKAGE_ID = "PACKAGE_ID";
   private static final String AP_STATUS = "STATUS";
   private static final String AP_LOG_SUMMARY_ID = "LOG_SUMMARY_ID";

   // String constants for transaction (DPL_LOG_TXN) table.
   // This table does not have a primary key.
   private static final String TXN_TABLE_NAME = "DPL_LOG_TXN";
   private static final String TXN_LOG_SUMMARY_ID = "LOG_SUMMARY_ID";
   private static final String TXN_DEPENDENCY = "DEPENDENCY";
   private static final String TXN_ELEMENT_NAME = "ELEMENT_NAME";
   private static final String TXN_ELEMENT_TYPE = "ELEMENT_TYPE";
   private static final String TXN_ACTION_TOOK = "ACTION_TOOK";
   private static final String TXN_SEQUENCE = "SEQUENCE";

   // String constants for log summary (DPL_LOG_SUMMARY) table.
   // Its primary key is LS_LOG_SUMMARY_ID
   private static final String LS_TABLE_NAME = "DPL_LOG_SUMMARY";
   private static final String LS_LOG_SUMMARY_ID = "LOG_SUMMARY_ID";
   private static final String LS_PACKAGE_NAME = "PACKAGE_NAME";
   private static final String LS_PACKAGE_TYPE = "PACKAGE_TYPE";
   private static final String LS_PACKAGE_ID = "PACKAGE_ID";
   private static final String LS_STATUS = "STATUS";
   private static final String LS_ARCHIVE_LOG_ID = "ARCHIVE_LOG_ID";
   private static final String LS_PACKAGE_DETAIL = "PACKAGE_DETAIL";
   private static final String LS_VALIDATION_RESULTS = "VALIDATION_RESULTS";
   private static final String LS_ID_MAP = "ID_MAP";
   private static final String LS_DBMS_MAP = "DBMS_MAP";
   
   /**
    * Array of column names needed to load the log summary without the log 
    * detail.  See {@link #getLogSummary(PSJdbcRowData, boolean)} for more info.
    */
   private static final String[] LS_SUM_COLS_NO_DETAIL = {LS_LOG_SUMMARY_ID, 
      LS_ARCHIVE_LOG_ID, LS_PACKAGE_DETAIL};

   /**
    * The schema for log summary table. Initialized by the constructor,
    * it will never be <code>null</code> or modified after that.
    */
   private PSJdbcTableSchema m_logSummarySchema;

   /**
    * The schema for log transaction table. Initialized by the constructor,
    * it will never be <code>null</code> or modified after that.
    */
   private PSJdbcTableSchema m_logTxnSchema;

   /**
    * The schema for archive package table. Initialized by the
    * constructor, it will never be <code>null</code> or modified after that.
    */
   private PSJdbcTableSchema m_archivePackageSchema;
   
   /**
    * The schema for archive package table, used to delete entries by archive 
    * log id. Has its update key set to the archive log id.  Initialized by the
    * constructor, it will never be <code>null</code> or modified after that.
    */
   private PSJdbcTableSchema m_archivePackageDeleteSchema;

   /**
    * The schema for archive log summary table. Initialized by the
    * constructor, it will never be <code>null</code> or modified after that.
    */
   private PSJdbcTableSchema m_archiveLogSummarySchema;

   /**
    * The <code>PSDbmsHelper</code> object, used to communicate with
    * the database. It is initialized by constructor, then never modified
    * afterwards.
    */
   private PSDbmsHelper m_dbmsHandle;

}

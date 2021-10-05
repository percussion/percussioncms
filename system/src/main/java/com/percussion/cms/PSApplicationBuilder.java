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

package com.percussion.cms;

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.data.PSMetaDataCache;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSDocumentMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndCredential;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSDataSynchronizer;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSPageDataTank;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSResultPageSet;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSSortedColumn;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSTraceInfo;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSUpdateColumn;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.design.objectstore.PSUserContext;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.util.PSUniqueObjectGenerator;
import com.percussion.xml.PSDtdBuilder;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used by server objects to build dynamic applications.
 */
public class PSApplicationBuilder
{
   /**
    * Creates an application object with the specified name.  Name is not
    * checked for uniqueness until the app is saved or run on the server.  The
    * provided app's DBActionType field name and values, and Acl are copied from 
    * the provided app.  
    * The app is initially enabled.  It is recommended that this method be used
    * when creating an app.
    *
    * @param appName The name of the application, also used as the request root.
    * @param srcApp The app to copy default values from.
    * 
    * @return The app, never <code>null</code>.
    */
   public static PSApplication createApplication(String appName,
      PSApplication srcApp)
   {
      if (appName == null || appName.length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      if (srcApp == null)
         throw new IllegalArgumentException("srcApp may not be null");

      PSApplication app = null;
      try
      {
         // create the app
         app = PSServerXmlObjectStore.getInstance().createEmptyApplication();

         // set the applications name, we also default the application root the
         // the same as the application name
         app.setName(appName);
         app.setRequestRoot(appName);

         // copy dbactiontype values
         app.setRequestTypeHtmlParamName(
            srcApp.getRequestTypeHtmlParamName());
         app.setRequestTypeValueQuery(srcApp.getRequestTypeValueQuery());
         app.setRequestTypeValueUpdate(srcApp.getRequestTypeValueUpdate());
         app.setRequestTypeValueInsert(srcApp.getRequestTypeValueInsert());
         app.setRequestTypeValueDelete(srcApp.getRequestTypeValueDelete());

         // Copy the acl from the source app, replacing it entirely
         PSAcl myAcl = srcApp.getAcl();
         app.setAcl(myAcl);

         // copy the tracing info
         PSTraceInfo traceInfo = app.getTraceInfo();
         traceInfo.copyFrom( srcApp.getTraceInfo());

         // enable the application
         app.setEnabled(true);

      }
      catch (IllegalArgumentException e)
      {
         // no sense in continuing this maddness
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      return app;

   }

   /**
    * Creates an empty application object with the specified name.  Name is not
    * checked for uniqueness until the app is saved or run on the server.  The
    * following defaults are set on the app:
    * <ol>
    * <li>The request type param name: {@link #REQUEST_TYPE_HTML_PARAMNAME}</li>
    * <li>The request db action for query: {@link #REQUEST_TYPE_VALUE_QUERY}
    * </li>
    * <li>The request db action for update: {@link #REQUEST_TYPE_VALUE_UPDATE}
    * </li>
    * <li>The request db action for insert: {@link #REQUEST_TYPE_VALUE_INSERT}
    * </li>
    * <li>The request db action for delete: {@link #REQUEST_TYPE_VALUE_DELETE}
    * </li>
    * <li>Anonymous is added to the acl with full data access</li>
    * <li>Default is added to the acl with full design and data access</li>
    * </ol>
    * The app is initially enabled.
    * @param appName The name of the application, also used as the request root.
    * @return The app, never <code>null</code>.
    */
   public static PSApplication createApplication(String appName)
   {
      PSApplication app = null;
      try
      {
         // create the app
         app = PSServerXmlObjectStore.getInstance().createEmptyApplication();

         // set the applications name, we also default the application root the
         // the same as the applictaion name
         app.setName(appName);
         app.setRequestRoot(appName);

         // Set the request default types
         app.setRequestTypeHtmlParamName(REQUEST_TYPE_HTML_PARAMNAME);
         app.setRequestTypeValueQuery(REQUEST_TYPE_VALUE_QUERY);
         app.setRequestTypeValueUpdate(REQUEST_TYPE_VALUE_UPDATE);
         app.setRequestTypeValueInsert(REQUEST_TYPE_VALUE_INSERT);
         app.setRequestTypeValueDelete(REQUEST_TYPE_VALUE_DELETE);

         // enable the application
         app.setEnabled(true);


      }
      catch (IllegalArgumentException e)
      {
         // no sense in continuing this maddness
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      return app;

   }

   /**
    * Adds the specified acl entry to the app.
    *
    * @param app The PSApplication object to add the entry to.
    * @param name The name of the user, group or role to associate with this
    * entry.
    * @param type The type of entry this represents (use the appropriate
    * PSAclEntry.ACE_TYPE_xxx flag).
    * @param accessLevel The access level for this entry (<code>OR</code>
    * together the necessary PSAclEntry.AACE_xxx_xxx flags).
    *
    * @return The AclEntry that has been created and added to the app.  The
    * entry returned may then be further modified.  It is not necessary to
    * set this entry back into the app after making changes to it.
    *
    * @see PSAclEntry
    */
   public static PSAclEntry addAclEntry(PSApplication app, String name,
      int type, int accessLevel)
   {

      if (app == null)
         throw new IllegalArgumentException("app may not be null");

      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("Name may not be null or empty");

      PSAclEntry aclEntry = null;
      try
      {
         PSAcl acl = app.getAcl();
         PSCollection entries = acl.getEntries();

         aclEntry = new PSAclEntry(name, type);
         aclEntry.setAccessLevel(accessLevel);

         entries.add(aclEntry);
         acl.setEntries(entries);
         app.setAcl(acl);
      }
      catch (IllegalArgumentException e)
      {
         //convert to be nice
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      return aclEntry;
   }

   /**
    * Creates and adds an update dataset based on the supplied content editor
    * pipe.  Performs the following setps to create the dataset:
    * <ol>
    * <li>Creates a dataset using the supplied name</li>
    * <li>Creates a PageDataTank using a DTD that is constructed using
    * the PSField.submitName of each field to be mapped written out to a temp
    * file. The DBActionType field name is set to the provided xml field.</li>
    * <li>Create a Pipe.</li>
    * <li>Creates a backend datatank, adding each table from the provided
    * PSTableSet collection that is referenced by a field in the display mapper.
    * Uses the alias in the tableset's tablelocator to
    * determine the driver and server from the backend credentials in the
    * supplied app.  This means that both server and app credentials must have
    * been added to the supplied application before making this call.
    * Adds this to the pipe.</li>
    * <li>Creates a DataMapper and adds fields from the provided PSFieldSet that
    * are referenced in the provided display mapper,
    * excluding any fields in any nested fieldsets.  The left side of the
    * mapping will be the PSField.submitName converted to a PSParam replacement
    * value, and the right side will be the PSField.locator, which is an
    * IPSReplacementValue already.</li>
    * <li>Creates a DataSynchronizer and adds the required key columns from
    * each table. For a parent and child tables, adds contentId and revision as
    * keys. For complex child tables, also adds sysId.</li>
    * <li>Creates a requestor and adds it to the dataset.</li>
    * </ol>
    *
    * @param app The PSApplication object to add the resource to.
    * @param requestName The name to use as both the resource name and the
    * reqeust page name in the resource's requestor.  May not be
    * <code>null</code>.
    * @param ce The content editor.  May not be <code>null</code>.
    * @param mapper The display mapper to use. May be <code>null</code>.
    * @param xmlActionTypeField The name of the xml field to get the db action
    * type from.  May be <code>null</code>, in which case the default setting
    * from the app is used.
    * @param systemMappings List of mappings to add.  May be <code>
    * null</code>.  All mappings should reference the contentstatus table
    * specified in the system def.
    * @param includeBinary If <code>true</code>, then any binary fields located
    * in the field set are included in the mapper.  If <code>false</code>, they
    * are not.  If no binary fields are in the fieldset, this parameter is
    * ignored.
    *
    * @throws PSSystemValidationException if any objectstore objects are found to be
    * invalid.
    */
   public static void createUpdateDataset(PSApplication app, String requestName,
      PSContentEditor ce, PSDisplayMapper mapper, String xmlActionTypeField,
      PSDataMapper systemMappings, boolean includeBinary)
         throws PSSystemValidationException
   {
      createUpdateDataset(app, requestName, ce, mapper,
         xmlActionTypeField, systemMappings, includeBinary, FLAG_ALLOW_UPDATES);
   }


   /**
    * Creates a dataset that only allows inserts.  This method's parameters are
    * the same as <code>createUpdateDataSet()</code>, with the following
    * exceptions:
    * <ol>
    * <li>Binary fields are always included</li>
    * </ol>
    *
    * @throws PSSystemValidationException if any objectstore objects are found to be
    * invalid.
    *
    * @see #createUpdateDataset(PSApplication, String, PSContentEditor,
    * PSDisplayMapper, String, PSDataMapper, boolean, int)
    * createUpdateDataset()
    */
   public static void createInsertDataset(PSApplication app, String requestName,
      PSContentEditor ce, PSDisplayMapper mapper, String xmlActionTypeField,
      PSDataMapper systemMappings)
         throws PSSystemValidationException
   {
      createUpdateDataset(app, requestName, ce, mapper,
         xmlActionTypeField, systemMappings, true, FLAG_ALLOW_INSERTS);
   }

   /**
    * Creates a dataset that only allows deletes.  This method's parameters are
    * the same as <code>createInsertDataSet()</code>.
    *
    * @throws PSSystemValidationException if any objectstore objects are found to be
    * invalid.
    *
    * @see #createInsertDataset(PSApplication, String, PSContentEditor,
    * PSDisplayMapper, String, PSDataMapper)
    * createInsertDataset()
    */
   public static void createDeleteDataset(PSApplication app, String requestName,
      PSContentEditor ce, PSDisplayMapper mapper, String xmlActionTypeField,
      PSDataMapper systemMappings)
         throws PSSystemValidationException
   {
      createUpdateDataset(app, requestName, ce, mapper,
         xmlActionTypeField, systemMappings, true, FLAG_ALLOW_DELETES);
   }


   /**
    * Same as {@createUpdateDataset(PSApplication, String, PSContentEditor,
    * PSDisplayMapper, String, PSDataMapper, boolean) createUpdateDataset()}
    * but allows you to specify what actions (insert, update, or delete) are
    * allowed.
    *
    * @param allowActions A flag indicating which actions to allow.  May be
    * any of the following Or'd together:
    * <ol>
    * <li>{@link #FLAG_ALLOW_INSERTS}</li>
    * <li>{@link #FLAG_ALLOW_UPDATES}</li>
    * <li>{@link #FLAG_ALLOW_DELETES}</li>
    * </ol>
    *
    */
   public static void createUpdateDataset(PSApplication app,
      String requestName, PSContentEditor ce, PSDisplayMapper mapper,
      String xmlActionTypeField, PSDataMapper systemMappings,
      boolean includeBinary, int allowActions)
      throws PSSystemValidationException
   {
      createUpdateDataset(app, requestName, ce, mapper,
         xmlActionTypeField, systemMappings, includeBinary, allowActions, null);
   }


   /**
    * Same as {@createUpdateDataset(PSApplication, String, PSContentEditor,
    * PSDisplayMapper, String, PSDataMapper, boolean, int)
    * createUpdateDataset()} but allows you to specify a Dtd to use.  The
    * supplied Dtd must work with the supplied mappings - this is not validated
    * by this method.
    *
    * @param dtd If supplied, will be used instead of building one dynamically.
    * May be <code>null</code>, in which case one is built dynamically.
    */
   public static void createUpdateDataset(PSApplication app,
      String requestName, PSContentEditor ce, PSDisplayMapper mapper,
      String xmlActionTypeField, PSDataMapper systemMappings,
      boolean includeBinary, int allowActions, PSDtdBuilder dtd)
      throws PSSystemValidationException
   {
      if ( null == app || null == requestName || null == ce ||
            requestName.length() == 0 )
      {
         throw new IllegalArgumentException( "One or more params was null." );
      }

      // output stream for writing the page tank dtd
      FileOutputStream dtdOut = null;
      boolean buildDtd = ((dtd == null) ? true : false);

      try
      {
         PSContentEditorPipe cePipe = (PSContentEditorPipe) ce.getPipe();
         PSContentEditorMapper ceMapper = cePipe.getMapper();
         PSFieldSet mainFieldSet = ceMapper.getFieldSet();

         PSDataSet ds = new PSDataSet(requestName);

         // create page tank
         File serverDir = PSServer.getRxDir();
         File appDir = new File(serverDir, app.getRequestRoot());
         appDir.mkdir();

         File dtdFile = null;
         String dtdRoot = null;

         if (buildDtd)
         {
            dtdFile = File.createTempFile("Rx_", ".dtd", appDir);
            dtdRoot = dtdFile.getName().substring( 0,
                  dtdFile.getName().lastIndexOf('.'));
            dtd = new PSDtdBuilder( dtdRoot );
         }
         else
         {
            dtdRoot = dtd.getRootName();
            dtdFile = new File(appDir, dtdRoot + ".dtd");
            dtdFile.deleteOnExit();
         }

         URL dtdUrl = new URL( "file:" + dtdFile.getName());
         PSPageDataTank page = new PSPageDataTank(dtdUrl);

         if (xmlActionTypeField != null)
            page.setActionTypeXmlField(xmlActionTypeField);
         ds.setPageDataTank(page);

         // create pipe
         PSUpdatePipe pipe = new PSUpdatePipe(
               PSUniqueObjectGenerator.makeUniqueName( "UpdatePipe" ));

         /* create a map of tables to fix up the backend columns when we're done
          * creating data mappers and backend tables
          */
         HashMap tableMap = new HashMap();

         /* create map for datamappers.  Will create one for each table and
          * combine them at the end so we can set group ids on mappings and
          * include mappings sorted by group id.
          */
         HashMap dataMappers = new HashMap();

         // add the system mappings if we have them
         if (systemMappings != null)
         {
            Iterator sysMappings = systemMappings.iterator();
            while (sysMappings.hasNext())
            {
               PSDataMapping sysMapping = (PSDataMapping)sysMappings.next();
               IPSBackEndMapping beMapping = sysMapping.getBackEndMapping();
               if (beMapping instanceof PSBackEndColumn)
               {
                  PSBackEndTable sysTable =
                     ((PSBackEndColumn)beMapping).getTable();
                  String tableRef = sysTable.getAlias();

                  PSDataMapper dataMapper = (PSDataMapper)dataMappers.get(
                     tableRef.toLowerCase());
                  if (dataMapper == null)
                  {
                     dataMapper = new PSDataMapper();
                     dataMappers.put(tableRef.toLowerCase(), dataMapper);
                     tableMap.put(tableRef.toLowerCase(), sysTable);
                  }
                  dataMapper.add(sysMapping);
               }
            }
         }


         // create synchronizer - add to it as we build the mapper
         PSDataSynchronizer dataSync = new PSDataSynchronizer();

         boolean allowInserts = ((allowActions & FLAG_ALLOW_INSERTS) ==
            FLAG_ALLOW_INSERTS);
         boolean allowUpdates = ((allowActions & FLAG_ALLOW_UPDATES) ==
            FLAG_ALLOW_UPDATES);
         boolean allowDeletes = ((allowActions & FLAG_ALLOW_DELETES) ==
            FLAG_ALLOW_DELETES);

         dataSync.setInsertingAllowed(allowInserts);
         dataSync.setUpdatingAllowed(allowUpdates);
         dataSync.setDeletingAllowed(allowDeletes);
         PSCollection updateColumns = dataSync.getUpdateColumns();


         PSFieldSet mapperFieldSet = null;
         if (mapper != null)
         {
            // get the field set the display mapper uses
            String mapperFieldSetRef = mapper.getFieldSetRef();
            if (mapperFieldSetRef.equals(mainFieldSet.getName()))
               mapperFieldSet = mainFieldSet;
            else
               mapperFieldSet = ceMapper.getFieldSet(mapperFieldSetRef);

            if (mapperFieldSet == null)
            {
               throw new PSSystemValidationException(
                  IPSServerErrors.CE_MISSING_FIELDSET, mapperFieldSetRef);
            }

            /* Walk the display mappings - this will drive the contents of the
             * data mapper, the synchronizer, and the backend datatank
             */
            Iterator mappings = mapper.iterator();

            while (mappings.hasNext())
            {
               PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
               String fieldRef = mapping.getFieldRef();
               Object o = mapperFieldSet.get(fieldRef);
               if (o == null)
               {
                  // see if it is from a multi-property child fieldset
                  o = mapperFieldSet.getChildField(fieldRef,
                     PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
                  if (o == null)
                  {
                     String label = "unlabeled";
                     if ( null != mapping.getUISet().getLabel())
                        label = mapping.getUISet().getLabel().getText();
                     String [] args = { fieldRef, label };
                     throw new PSSystemValidationException(
                           IPSServerErrors.CE_MISSING_FIELD, args );
                  }
               }

               if (o instanceof PSFieldSet)
                  continue;
               PSField field = (PSField)o;

               /* skip non-backend columns - it's some other replacement value
                * used for queries
                */
               IPSBackEndMapping beMapping = field.getLocator();
               if (beMapping instanceof PSBackEndColumn)
               {
                  PSBackEndColumn column = (PSBackEndColumn)beMapping;

                  /* if this is a reserved column, ignore, as all such columns
                   * are provided in the system mappings
                   */
                  if (isReservedColumn(column.getColumn()))
                     continue;

                  /* if not including binary fields, skip if binary (on update).
                   * Check if field is forceBinary here, and check the actual
                   * metadata later once we have full table info
                   */
                  if (!includeBinary && field.isForceBinary())
                        continue;

                  /* need to copy this so we have a new instance.  Otherwise
                   * each time we build a dataset using this field we will be
                   * adding the same instance of the column to the dataMapper.
                   * When we later set the table on it, we overwrite the table
                   * in all datasets containing a mapping with this backend
                   * column.
                   */
                  PSBackEndColumn newColumn = new PSBackEndColumn(
                     column.getTable(), column.getColumn());
                  newColumn.copyFrom(column);
                  column = newColumn;
                  beMapping = column;

                  String submitName = field.getSubmitName();

                  // build DTD for requestor as we go
                  if (buildDtd)
                  {
                     dtd.addElement(submitName, PSDtdBuilder.OCCURS_ONCE,
                        dtdRoot);
                  }

                  /* now add mapping - need to create separate mappers for each
                   * table and combine at the end so mappings are sorted by
                   * grouping.
                   */

                  PSDataMapping dataMapping = new PSDataMapping(
                     new PSHtmlParameter(submitName), beMapping);

                  /* create a mapper for this table if we don't have one.  This
                   * will also build a set of table names for creating backend
                   * datatank.
                   */
                  String tableRef = column.getTable().getAlias().toLowerCase();
                  tableMap.put(tableRef, column.getTable());
                  PSDataMapper dataMapper = (PSDataMapper)dataMappers.get(
                     tableRef.toLowerCase());
                  if (dataMapper == null)
                  {
                     dataMapper = new PSDataMapper();
                     dataMappers.put(tableRef.toLowerCase(), dataMapper);
                  }
                  dataMapper.add(dataMapping);
               }
            }
         }

         // create backend datatank
         PSBackEndDataTank tank = new PSBackEndDataTank();
         PSCollection tables = tank.getTables();

         // will need a map of tableSets in case we check for binary columns
         HashMap tableSetMap = new HashMap();
         Iterator tableSets = cePipe.getLocator().getTableSets();
         while (tableSets.hasNext())
         {
            // get table set
            PSTableSet tableSet = (PSTableSet)tableSets.next();

            // walk the tables we've actually used
            Iterator tableRefs = tableSet.getTableRefs();
            while (tableRefs.hasNext())
            {
               PSTableRef tableRef = (PSTableRef)tableRefs.next();
               String tableKey = tableRef.getAlias().toLowerCase();
               if (dataMappers.keySet().contains(tableKey))
               {
                  // add tableSet to map with table alias as key
                  tableSetMap.put(tableKey, tableSet);
               }
            }
         }


         // add to tank based on order of tableRefs so it will match the mapper
         Iterator tableRefs = dataMappers.keySet().iterator();
         while (tableRefs.hasNext())
         {
            String tableRef = (String)tableRefs.next();
            tables.add((PSBackEndTable)tableMap.get(tableRef));
         }

         tank.setTables(tables);

         pipe.setBackEndDataTank(tank);

         /* combine the datamappers, and set the full table on the backend
          * column and the groupids as we go
          */
         PSDataMapper masterMapper = new PSDataMapper();
         int curGroup = 0;
         tableRefs = dataMappers.keySet().iterator();
         while (tableRefs.hasNext())
         {
            String tableRef = (String)tableRefs.next();
            curGroup++;

            // get the data mapper for this tableref
            PSDataMapper dataMapper = (PSDataMapper)dataMappers.get(tableRef);
            // walk the mapper and add mappings
            Iterator dataMappings = dataMapper.iterator();
            while (dataMappings.hasNext())
            {
               PSDataMapping dataMapping = (PSDataMapping)dataMappings.next();
               PSBackEndColumn beCol =
                  (PSBackEndColumn)dataMapping.getBackEndMapping();

               // if not including binary, check metadata now
               if (!includeBinary && PSMetaDataCache.isBinaryBackendColumn(
                     (PSTableSet)tableSetMap.get(
                        beCol.getTable().getAlias().toLowerCase()), beCol))
               {
                  continue;
               }

               // set the group id
               dataMapping.setGroupId(curGroup);
               masterMapper.add(dataMapping);

               // Also add to the synchronizer
               boolean isKey = isKeyColumn(beCol.getColumn());
               PSUpdateColumn updateColumn = new PSUpdateColumn(beCol, isKey);
               updateColumn.setUpdateable(isKey ? false : true);
               updateColumns.add(updateColumn);

            }
         }

         // set the combined datamapper on the pipe
         pipe.setDataMapper(masterMapper);

         // set the update columns on the synchronizer and add it to the pipe
         dataSync.setUpdateColumns(updateColumns);
         pipe.setDataSynchronizer(dataSync);

         // set pipe on dataset
         ds.setPipe(pipe);

         // setup transaction support
         ds.setTransactionForAllRows();

         // write out DTD
         dtdOut = new FileOutputStream(dtdFile);
         dtd.write(dtdOut);

         // create requestor and add to the dataset
         PSRequestor requestor = new PSRequestor();
         requestor.setRequestPage(requestName);
         ds.setRequestor(requestor);

         // Add the dataset to the app
         PSCollection datasets = app.getDataSets();
         if (datasets == null)
            datasets = new PSCollection(PSDataSet.class);
         datasets.add(ds);
         app.setDataSets(datasets);
      }
      catch (IllegalArgumentException e)
      {
         // todo get rid of this exception
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }
      catch (MalformedURLException e)
      {
         // todo: figure out what to do
         throw new RuntimeException(e.getLocalizedMessage());
      }
      catch (IOException e)
      {
         // todo: figure out what to do
         throw new RuntimeException( e.getLocalizedMessage());
      }
      catch ( SQLException sqe )
      {
         // todo: get all linked messages
         throw new RuntimeException( sqe.getLocalizedMessage());
      }
      finally
      {
         if (dtdOut != null)
         {
            try{dtdOut.close();} catch(IOException e){}
         }
      }
   }


   /**
    * Creates and adds a query dataset based on the supplied content editor
    * pipe. The application root must be a unique name because a directory
    * matching the application request root will be created by the method if
    * it doesn't already exist. Cleanup of the approot directory is not
    * handled by this class.
    * <p>
    * Performs the following steps to create the dataset:
    * <ol>
    * <li>Creates a dataset using the supplied name</li>
    * <li>Creates a PageDataTank using a DTD that is constructed using
    * the PSField.submitName of each field to be mapped.</li>
    * <li>Create a Pipe.</li>
    * <li>Creates a backend datatank, adding each table from the provided
    * PSTableSet collection. Uses the alias in the tableset's tablelocator to
    * determine the driver and server from the backend credentials in the
    * supplied app.  This means that both server and app credentials must have
    * been added to the supplied application before making this call.
    * Adds this to the pipe.</li>
    * <li>Creates a DataMapper and adds all fields in the provided PSFieldSet.
    * The left side of the
    * mapping will be the PSField.locator and the right side will be the
    * PSField.submitName converted to a PSParam XML document element. For
    * fields whose type is binary, the field will not be added. Adds this to
    * the pipe.</li>
    * <li>Creates a Data selector and adds the required key columns from
    * each table. For a parent and child tables, adds contentId and revision as
    * keys. For complex child tables, also adds sysId.</li>
    * <li>Joins all of the tables as needed. The main table (the one containing
    *    the data represented by the display mapper) is joined to any other
    *    table referenced by fields in the display mapper. At most, 1
    *    system table and 0 or more content tables are allowed. The only child
    *    content tables supported are for SDMP type children (which are edited
    *    in their parent's row editor).</li>
    * </ol>
    *
    * @param app The PSApplication object to add the resource to.
    *
    * @param ce The editor definition that will be used to create the resource.
    *
    * @param dispMapper The mapper for which the the query will be built. The
    *    resulting resource will query all fields that have backend columns
    *    as the locator.
    *
    * @param selectionKeys A set of pairs that are used to build the WHERE
    *    clauses for this query. Each element must be a Map.Entry object.
    *    The key of each pair should be the name of
    *    a column in the table, while the value should be the name of the
    *    html parameter from which the check will be made. Must have at least
    *    one entry. The keys will also be included in the mapper so they can
    *    be used if needed (or ignored if not needed).
    *
    * @param auxMappings Use this set of entries to add additional mappings if
    *    you need something in the result set that isn't in the dispMapper
    *    (for example, a system key that is only used for selection). A set of
    *    Map.Entry objects. The key is the xml target as a String (w/o the
    *    root), the value is the name of the column, as a String. Never <code>
    *    null</code>. The column(s) will be added as members of the table
    *    associated with the item.
    *
    * @param sortColumns Every column included in this list will be sorted
    *    in ascending order, in the order they appear in the list. Never
    *    <code>null</code>. May be empty. The column(s) will be added as
    *    members of the table associated with the item. Each entry must be
    *    a String.
    *
    * @return The request name of the dataset, never empty.  If the resulting 
    *    dataset's mapper would be empty, no dataset is created and 
    *    <code>null</code> is returned. 
    *
    * @throws PSSystemValidationException If any errors in the definition are found
    *    while building the dataset.
    */
   public static String createQueryDataset(PSApplication app, PSContentEditor ce,
         PSDisplayMapper dispMapper, Iterator selectionKeys,
         Iterator auxMappings, Iterator sortColumns )
      throws PSSystemValidationException
   {
      if ( null == app || null == ce || null == dispMapper ||
            null == selectionKeys || null == auxMappings || null == sortColumns )
      {
         throw new IllegalArgumentException( "One or more params was null." );
      }

      if ( !selectionKeys.hasNext())
      {
         throw new IllegalArgumentException( "No keys supplied." );
      }

      OutputStream dtdOut = null;
      try
      {
         PSQueryPipe pipe = new PSQueryPipe(
               PSUniqueObjectGenerator.makeUniqueName( "QueryPipe" ));
         PSDataSet ds = new PSDataSet(
               PSUniqueObjectGenerator.makeUniqueName( "DataSet" ));

         // set up page tank
         File serverDir = PSServer.getRxDir();
         File appDir = new File(serverDir, app.getRequestRoot());
         appDir.mkdir();
         File dtdFile = File.createTempFile( "Rx_", ".dtd", appDir );
         URL dtdUrl = new URL( "file:" + dtdFile.getName());
         PSPageDataTank page = new PSPageDataTank(dtdUrl);
         String dtdRoot = dtdFile.getName().substring( 0,
               dtdFile.getName().lastIndexOf('.'));
         PSDtdBuilder dtd = new PSDtdBuilder( dtdRoot );
         ds.setPageDataTank( page );

         // set up mapper
         PSContentEditorPipe cePipe = (PSContentEditorPipe) ce.getPipe();
         PSContentEditorMapper ceMapper = cePipe.getMapper();
         PSFieldSet mainFieldSet = ceMapper.getFieldSet();
         PSFieldSet fieldSet;
         if ( mainFieldSet.getName().equals( dispMapper.getFieldSetRef()))
            fieldSet = mainFieldSet;
         else
            fieldSet = (PSFieldSet) mainFieldSet.get(
                  dispMapper.getFieldSetRef());

         if ( null == fieldSet )
         {
            throw new PSSystemValidationException(
                  IPSServerErrors.CE_MISSING_FIELDSET,
                  dispMapper.getFieldSetRef());
         }

         PSBackEndTable systemTable = null;
         // this is the table associated with the main mapper
         // it will be the parent of all joins
         PSBackEndTable localTable = null;
         // contains table alias (lowercased) as key, PSBackEndTable as value
         Map sdmpTables = new HashMap();

         // set up BE table
         // used later to fixup tables in mappings
         HashMap tableMap = new HashMap();
         Iterator tableSets = cePipe.getLocator().getTableSets();
         // Key is the table name, value is the table set for the table
         Map tableSetMap = new HashMap();
         while (tableSets.hasNext())
         {
            PSTableSet tableSet = (PSTableSet)tableSets.next();
            PSTableLocator tableLocator = tableSet.getTableLocation();
            Iterator tableRefs = tableSet.getTableRefs();
            while (tableRefs.hasNext())
            {
               PSTableRef tableRef = (PSTableRef)tableRefs.next();
               //todo: dbg
               PSBackEndTable table = new PSBackEndTable(
                     PSUniqueObjectGenerator.makeUniqueName( "table" ));
               table.setAlias(tableRef.getAlias());
               table.setInfoFromLocator(tableLocator);
               table.setTable(tableRef.getName());

               tableMap.put( table.getAlias().toLowerCase(), table );
               tableSetMap.put( table.getAlias(), tableSet );
            }
         }

         /* Walk thru all mappings and re-order them for optimal grouping */
         Iterator dispMappings = dispMapper.iterator();
         HashMap colMap = new HashMap();
         while ( dispMappings.hasNext())
         {
            PSDisplayMapping dispMapping =
                  (PSDisplayMapping) dispMappings.next();
            String fieldRef = dispMapping.getFieldRef();
            // field sets indicate a child which is handled seperately
            Object o = fieldSet.get(fieldRef);
            if ( o instanceof PSFieldSet )
               continue;

            boolean sdmpField = false;
            if ( null == o )
            {
               o = fieldSet.getChildField( fieldRef,
                     PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
               if ( null == o )
               {
                  String mappingLabel = "unknown";
                  PSUISet uiSet = dispMapping.getUISet();
                  if ( null != uiSet )
                  {
                     PSDisplayText label = uiSet.getLabel();
                     if ( null != label )
                        mappingLabel = label.getText();
                  }

                  String [] args =
                  {
                     fieldRef,
                     mappingLabel,
                  };
                  throw new PSSystemValidationException(
                        IPSServerErrors.CE_MISSING_FIELD, args );
               }
               sdmpField = true;
            }
            PSField field = (PSField) o;

            // sdmp fields get retrieved independently
            if ( sdmpField )
               continue;

            IPSBackEndMapping locator = field.getLocator();
            if ( locator instanceof PSBackEndColumn )
            {
               PSBackEndColumn col = (PSBackEndColumn) locator;
               String alias = col.getTable().getAlias().toLowerCase();
               
               // binary fields can only be queried with a non-text resource,
               // but we still want to get the local table in case there's only
               // a single binary field which should be okay
               if (!field.isForceBinary())
               { 
                  List cols = (List) colMap.get( alias );
                  if ( null == cols )
                  {
                     cols = new ArrayList();
                     colMap.put( alias, cols );
                  }
                  cols.add( dispMapping );
               }
               
               switch ( field.getType())
               {
                  case PSField.TYPE_SYSTEM:
                     if ( null != systemTable )
                     {
                        if ( !systemTable.getAlias().equalsIgnoreCase( alias))
                        {
                           String [] args =
                           {
                              fieldSet.getName(),
                              systemTable.getAlias(),
                              col.getTable().getAlias()
                           };
                           throw new PSSystemValidationException(
                                 IPSServerErrors.CE_MULTIPLE_TABLES_NOT_SUPPORTED,
                                 args );
                        }
                     }
                     else
                        systemTable = col.getTable();
                     break;
                  case PSField.TYPE_SHARED:
                  case PSField.TYPE_LOCAL:
                     if ( !sdmpField )
                     {
                        if ( null != localTable )
                        {
                           if ( !localTable.getAlias().equalsIgnoreCase( alias ))
                           {
                              String [] args =
                              {
                                 fieldSet.getName(),
                                 localTable.getAlias(),
                                 col.getTable().getAlias()
                              };
                              throw new PSSystemValidationException(
                                    IPSServerErrors.CE_MULTIPLE_TABLES_NOT_SUPPORTED,
                                    args );
                           }
                        }
                        else
                           localTable = col.getTable();
                     }
                     else
                     {
                        sdmpTables.put( col.getTable().getAlias().toLowerCase(),
                              col.getTable());
                     }
                     break;
               }
            }
            else
            {
               List nonCol = (List) colMap.get( "nbe" );
               if ( null == nonCol )
                  nonCol = new ArrayList();
               colMap.put( "nbe", nonCol );
               nonCol.add(dispMapping);
            }
         }

         // 1 of each table used by mappings, fully qualified
         PSCollection tables =
               new PSCollection(PSBackEndTable.class);
         // fix up our tables with complete connection info
         if ( null != systemTable )
         {
            String alias = systemTable.getAlias();
            systemTable = (PSBackEndTable) tableMap.get( alias.toLowerCase());
            if ( null == systemTable )
            {
               throw new PSSystemValidationException(
                     IPSServerErrors.CE_MISSING_TABLE, alias );
            }
            tables.add( systemTable );
         }

         if ( null != localTable )
         {
            String alias = localTable.getAlias();
            localTable = (PSBackEndTable) tableMap.get( alias.toLowerCase());
            if ( null == localTable )
            {
               throw new PSSystemValidationException(
                     IPSServerErrors.CE_MISSING_TABLE, alias );
            }
            tables.add( localTable );
         }
         else
         {
            // we have to have at least 1 mapping for main table
            throw new PSSystemValidationException(
                  IPSServerErrors.CE_MISSING_MAPPINGS,
                  dispMapper.getFieldSetRef());
         }

         if ( !sdmpTables.isEmpty())
         {
            // we have 1 or more SDMP children
            Iterator sdmpTablesIter = sdmpTables.values().iterator();
            while ( sdmpTablesIter.hasNext())
            {
               PSBackEndTable table = (PSBackEndTable) sdmpTablesIter.next();
               table = (PSBackEndTable)
                  tableMap.get( table.getAlias().toLowerCase());
               sdmpTables.put( table.getAlias().toLowerCase(), table );
               tables.add( table );
            }
         }

         // now we have all columns grouped by table name
         Iterator colSets = colMap.values().iterator();
         PSDataMapper mapper = new PSDataMapper();
         int groupId = 0;
         HashMap groupMap = new HashMap();
         while ( colSets.hasNext())
         {
            dispMappings = ((List) colSets.next()).iterator();
            PSBackEndTable table = null;
            while ( dispMappings.hasNext())
            {
               PSDisplayMapping dispMapping =
                     (PSDisplayMapping) dispMappings.next();
               String fieldRef = dispMapping.getFieldRef();
               PSField field = (PSField) fieldSet.get(fieldRef);
               if ( null == field )
               {
                  // must be an sdmp field
                  field = fieldSet.getChildField( fieldRef,
                        PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
               }

               IPSBackEndMapping beMapping = field.getLocator();
               if ( beMapping instanceof PSBackEndColumn )
               {
                  PSBackEndColumn beCol = (PSBackEndColumn) beMapping;
                  table = (PSBackEndTable) tableMap.get(
                        beCol.getTable().getAlias().toLowerCase());
                  beCol.setTable( table );
                  if ( PSMetaDataCache.isBinaryBackendColumn(
                        (PSTableSet) tableSetMap.get( beCol.getTable().getAlias()),
                        beCol ))
                  {
                     continue;
                  }
               }
               PSDataMapping mapping = new PSDataMapping(
                     dtdRoot + "/" + field.getSubmitName(), beMapping );
               mapping.setGroupId(groupId);

               mapper.add( mapping );

               dtd.addElement( field.getSubmitName(), PSDtdBuilder.OCCURS_ONCE,
                     dtdRoot );
            }
            if ( null != table )
            {
               groupMap.put( table.getAlias().toLowerCase(),
                     new Integer( groupId ));
            }
            groupId++;
         }

         //add any extra mappings
         while( auxMappings.hasNext())
         {
            Map.Entry entry = (Map.Entry) auxMappings.next();
            String colName = (String) entry.getValue();
            PSBackEndColumn col = new PSBackEndColumn( localTable, colName );
            PSDataMapping dm = new PSDataMapping(
                  dtdRoot + "/" + (String) entry.getKey(), col );
            Integer groupIdObj = (Integer) groupMap.get(
                  col.getTable().getAlias().toLowerCase());
            if ( null == groupIdObj )
            {
               // this should never happen
               throw new RuntimeException(
                     "Local table " + localTable + " not found in group map" );
            }
            dm.setGroupId( groupIdObj.intValue());
            addMapping( mapper, dm );
         }
         
         // if no mappings, return null resource name
         if (mapper.size() == 0)
            return null;

         pipe.setDataMapper( mapper );
         ds.setPipe( pipe );

         // write out the dtd we built while scanning the mapper
         dtdOut = new FileOutputStream(dtdFile);
         dtd.write(dtdOut);

         // add tables and joins to the tank
         PSBackEndDataTank tank = new PSBackEndDataTank();
         tank.setTables( tables );

         // add join if needed
         PSCollection joins = null;
         if ( null != systemTable )
         {
            // todo: dbg
            PSBackEndColumn leftCol =
                  new PSBackEndColumn( localTable, IPSConstants.ITEM_PKEY_CONTENTID );
            PSBackEndColumn rightCol =
                  new PSBackEndColumn( systemTable, IPSConstants.ITEM_PKEY_CONTENTID );
            PSBackEndJoin join = new PSBackEndJoin( leftCol, rightCol );
            joins = new PSCollection( join.getClass());
            joins.add( join );
         }
         Iterator sdmpTableIter = sdmpTables.values().iterator();
         while ( sdmpTableIter.hasNext())
         {
            PSBackEndTable table = (PSBackEndTable) sdmpTableIter.next();
            // todo: dbg
            PSBackEndColumn leftCol = new PSBackEndColumn( localTable,
                  IPSConstants.ITEM_PKEY_CONTENTID );
            PSBackEndColumn rightCol = new PSBackEndColumn( table,
                  IPSConstants.ITEM_PKEY_CONTENTID );
            PSBackEndJoin join = new PSBackEndJoin( leftCol, rightCol );
            if ( null == joins )
               joins = new PSCollection( join.getClass());
            joins.add( join );
            leftCol = new PSBackEndColumn( localTable,
                  IPSConstants.ITEM_PKEY_REVISIONID );
            rightCol = new PSBackEndColumn( table,
                  IPSConstants.ITEM_PKEY_REVISIONID );
            join = new PSBackEndJoin( leftCol, rightCol );
            joins.add( join );
         }
         if ( null != joins )
            tank.setJoins( joins );
         pipe.setBackEndDataTank( tank );

         // set up selector
         PSDataSelector selector = new PSDataSelector();
         PSCollection clauses = null;
         // used by sorted column code below
         PSBackEndColumn beColumn = null;
         while ( selectionKeys.hasNext())
         {
            Map.Entry entry = (Map.Entry) selectionKeys.next();
            beColumn = new PSBackEndColumn( localTable, (String) entry.getKey());
            IPSReplacementValue value =
                  new PSSingleHtmlParameter((String) entry.getValue());
            PSWhereClause clause = new PSWhereClause( beColumn,
                  PSWhereClause.OPTYPE_EQUALS, value, true );

            if ( null == clauses )
               clauses = new PSCollection( clause.getClass());
            clauses.add( clause );
         }
         selector.setWhereClauses( clauses );

         PSCollection sortedCols = null;
         if ( sortColumns.hasNext())
         {
            while( sortColumns.hasNext())
            {
               String colName = (String) sortColumns.next();
               PSBackEndColumn col = new PSBackEndColumn( localTable, colName );
               PSSortedColumn sortedCol = new PSSortedColumn( col, true );
               if ( null == sortedCols )
                  sortedCols = new PSCollection( sortedCol.getClass());
               sortedCols.add( sortedCol );
            }
         }

         selector.setSortedColumns( sortedCols );
         pipe.setDataSelector( selector );

         PSCollection dsSet = app.getDataSets();
         if ( null == dsSet )
         {
            dsSet = new PSCollection(PSDataSet.class);
         }
         dsSet.add( ds );

         // set up requestor
         PSRequestor req = new PSRequestor();
         req.setRequestPage(
               PSUniqueObjectGenerator.makeUniqueName( "request" ));
         HashMap mimeTypes = new HashMap();
         PSTextLiteral type = new PSTextLiteral( "text/html" );
         mimeTypes.put( "htm", type );
         mimeTypes.put( "html", type );
         req.setMimeProperties( mimeTypes );
         ds.setRequestor( req );

         /* set up result pages, there must be at least one defined, null
            means use the system default */
         PSResultPage resultPage = new PSResultPage( null );
         PSResultPageSet pageSet = new PSResultPageSet();
         PSCollection pages = new PSCollection( resultPage.getClass());
         pages.add( resultPage );
         pageSet.setResultPages( pages );
         ds.setOutputResultPages( pageSet );

         app.setDataSets(dsSet);
         return ds.getName();
      }
      catch ( IllegalArgumentException e )
      {
         // todo get rid of this exception
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }
      catch ( MalformedURLException me )
      {
         // todo: figure out what to do
         throw new RuntimeException( me.getLocalizedMessage());
      }
      catch ( IOException ioe )
      {
         // todo: figure out what to do
         throw new RuntimeException( ioe.getLocalizedMessage());
      }
      catch ( SQLException sqe )
      {
         // todo: get all linked messages
         throw new RuntimeException( sqe.getLocalizedMessage());
      }
      finally
      {
         try
         {
            if ( null != dtdOut )
               dtdOut.close();
         }
         catch ( IOException e )
         { /* ignore */ }
      }
   }


   /**
    * Convenience method that passes <code>null</code> for the <code>dtd</code>
    * parameter and <code>false</code> for isBinary.
    * @see #createQueryDataset(PSApplication, PSDataMapper, Iterator, Iterator,
    * PSDtdBuilder, boolean)
    */
   public static String createQueryDataset(PSApplication app,
      PSDataMapper dataMapper, Iterator selectionKeys, Iterator sortCols )
      throws PSSystemValidationException
   {
      return createQueryDataset(app, dataMapper, selectionKeys, sortCols, null,
         false);
   }

   /**
    * Creates a simple query dataset from the provided datamapper.  Does not
    * support multiple tables (joins) or binary fields.
    *
    * @param app The app to create the dataset in.
    * @param dataMapper Mappings of XmlFields to PSBackendColumns. Other
    * types of replacement values will be ignored.  The xmlFields should not
    * contain the root as the dtd is dynamically created and the field's root
    * in the mapping will be updated as the dataset is created.
    * @param selectionKeys A set of pairs that are used to build the WHERE
    *    clauses for this query. Each element must be a Map.Entry object.
    *    The key of each pair should be the name of
    *    a column in the table, while the value should be the name of the
    *    html parameter from which the check will be made. Must have at least
    *    one entry.
    * @param sotrCols Iterator over one or more PSSortedColumns that the
    * results should be sorted by, in the order that the sort should occur. All
    * columns must be part of the mapper.  May be <code>null</code> if no sort
    * is desired.
    * @param dtd Optional dtdbuilder that will be used.  It is the caller's
    * responsibility to ensure that the dtd will work with the supplied data
    * mappings.  May be <code>null</code>.
    * @param isBinary If <code>true</code>, the requestor will be set up to
    * get a direct data stream to support a non-text query.  If <code>false
    * </code>, this is not done.
    *
    * @return The name of the dataset, which is unique among all names from
    * this method during a session.
    *
    * @throws PSSystemValidationException for any other errors.
    */
   public static String createQueryDataset(PSApplication app,
      PSDataMapper dataMapper, Iterator selectionKeys, Iterator sortCols,
      PSDtdBuilder dtd, boolean isBinary)
      throws PSSystemValidationException
   {
      if (null == app || null == dataMapper ||
            null == selectionKeys )
      {
         throw new IllegalArgumentException( "One or more params was null." );
      }

      if (dataMapper.size() == 0)
      {
         throw new IllegalArgumentException( "No data mappings supplied." );
      }

      if (!selectionKeys.hasNext())
      {
         throw new IllegalArgumentException( "No keys supplied." );
      }

      OutputStream dtdOut = null;
      try
      {
         PSQueryPipe pipe = new PSQueryPipe(
               PSUniqueObjectGenerator.makeUniqueName( "QueryPipe" ));
         PSDataSet ds = new PSDataSet(
               PSUniqueObjectGenerator.makeUniqueName( "DataSet" ));

         // set up page tank
         File serverDir = PSServer.getRxDir();
         File appDir = new File(serverDir, app.getRequestRoot());
         appDir.mkdir();
         File dtdFile = null;
         String dtdRoot = null;
         boolean buildDtd = true;
         if (dtd == null)
         {
            dtdFile = File.createTempFile( "Rx_", ".dtd", appDir );
            dtdRoot = dtdFile.getName().substring( 0,
               dtdFile.getName().lastIndexOf('.'));
            dtd = new PSDtdBuilder( dtdRoot );
         }
         else
         {
            dtdFile = new File(appDir, dtd.getRootName() + ".dtd");
            dtdRoot = dtd.getRootName();
            buildDtd = false;
         }

         dtdFile.deleteOnExit();
         URL dtdUrl = new URL( "file:" + dtdFile.getName());
         PSPageDataTank page = new PSPageDataTank(dtdUrl);
         ds.setPageDataTank( page );

         PSCollection tables =
               new PSCollection(PSBackEndTable.class);

         // walk the mapper to build the dtd and get tables (should only be one)
         HashMap tableMap = new HashMap();
         String tableRef = null;
         Iterator mappings = dataMapper.iterator();
         while (mappings.hasNext())
         {
            PSDataMapping dataMapping = (PSDataMapping)mappings.next();
            IPSBackEndMapping beMapping = dataMapping.getBackEndMapping();
            if (beMapping instanceof PSBackEndColumn)
            {
               PSBackEndColumn beCol = (PSBackEndColumn)beMapping;
               tableRef = beCol.getTable().getAlias();
               tableMap.put(tableRef.toLowerCase(), beCol.getTable());
            }

            // add it to the dtd if building one
            if (buildDtd)
               dtd.addElement(dataMapping.getXmlField(),
                  PSDtdBuilder.OCCURS_ONCE, dtdRoot );

            // fix up the xmlField with the root
            dataMapping.setXmlField(dtdRoot + "/" + dataMapping.getXmlField());
         }

         if (tableMap.size() != 1)
         {
            throw new IllegalArgumentException(
               "dataMapper must result in exactly one table");
         }
         tables.add(tableMap.get(tableRef.toLowerCase()));

         // we don't want a fake row in our results
         dataMapper.setAllowEmptyDocReturn(true);

         pipe.setDataMapper( dataMapper );
         ds.setPipe( pipe );

         // write out the dtd we built while scanning the mapper
         dtdOut = new FileOutputStream(dtdFile);
         dtd.write(dtdOut);

         // add tables to the tank
         PSBackEndDataTank tank = new PSBackEndDataTank();
         tank.setTables( tables );
         pipe.setBackEndDataTank( tank );

         // set up selector
         PSBackEndTable localTable = (PSBackEndTable)tables.firstElement();
         PSDataSelector selector = new PSDataSelector();
         PSCollection clauses = null;
         PSBackEndColumn name = null;
         while ( selectionKeys.hasNext())
         {
            Map.Entry entry = (Map.Entry) selectionKeys.next();
            name = new PSBackEndColumn( localTable, (String) entry.getKey());
            IPSReplacementValue value =
                  new PSSingleHtmlParameter((String) entry.getValue());
            PSWhereClause clause = new PSWhereClause( name,
                  PSWhereClause.OPTYPE_EQUALS, value, true );
            if ( null == clauses )
               clauses = new PSCollection( clause.getClass());
            clauses.add( clause );
         }
         selector.setWhereClauses( clauses );

         if (sortCols != null && sortCols.hasNext())
         {
            PSCollection sortedCols = new PSCollection(sortCols);
            selector.setSortedColumns(sortedCols);
         }

         pipe.setDataSelector( selector );

         PSCollection dsSet = app.getDataSets();
         if ( null == dsSet )
         {
            dsSet = new PSCollection(PSDataSet.class);
         }
         dsSet.add( ds );

         // set up requestor
         PSRequestor req = new PSRequestor();
         req.setRequestPage(
               PSUniqueObjectGenerator.makeUniqueName( "request" ));
         HashMap mimeTypes = new HashMap();
         PSTextLiteral type = new PSTextLiteral( "text/html" );
         mimeTypes.put( "htm", type );
         mimeTypes.put( "html", type );
         req.setMimeProperties( mimeTypes );
         req.setDirectDataStream(isBinary);

         ds.setRequestor( req );

         /* set up result pages, there must be at least one defined, null
            means use the system default */
         PSResultPage resultPage = new PSResultPage( null );
         PSResultPageSet pageSet = new PSResultPageSet();
         PSCollection pages = new PSCollection( resultPage.getClass());
         pages.add( resultPage );
         pageSet.setResultPages( pages );
         ds.setOutputResultPages( pageSet );

         app.setDataSets(dsSet);
         return ds.getName();
      }
      catch ( IllegalArgumentException e )
      {
         // todo get rid of this exception
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }
      catch ( MalformedURLException me )
      {
         // todo: figure out what to do
         throw new RuntimeException( me.getLocalizedMessage());
      }
      catch ( IOException ioe )
      {
         // todo: figure out what to do
         throw new RuntimeException( ioe.getLocalizedMessage());
      }
      finally
      {
         try
         {
            if ( null != dtdOut )
               dtdOut.close();
         }
         catch ( IOException e )
         { /* ignore */ }
      }
   }


   /**
    * Adds the supplied mapping to the supplied mapper in the correct position
    * based on the group id of the mapping.
    *
    * @param mapper The container for the mapping. Assumed not <code>null
    *    </code>.
    *
    * @param mapping The mapping to add to the mapper. Assumed not <code>null
    *    </code>.
    */
   private static void addMapping( PSDataMapper mapper, PSDataMapping mapping )
   {
      int groupId = mapping.getGroupId();

      boolean mappingAdded = false;
      int size = mapper.size();
      for ( int i = 0; i < size && !mappingAdded; i++ )
      {
         PSDataMapping testMapping = (PSDataMapping) mapper.get(i);
         if ( testMapping.getGroupId() == groupId )
         {
            mapper.insertElementAt( mapping, i );
            mappingAdded = true;
         }
      }
      if ( !mappingAdded )
         mapper.add( mapping );
   }

   /**
    * Writes the application to an XML file with a name of the form
    * <p>dbg_<i>appname</i>.xml</p>
    *
    * <p>
    * The file is written to the Rx install directory.  It will be deleted
    * when the Java Virtual Machine exits.
    * </p>
    *
    * @param app The application to store. If <code>null</code>, returns
    *    immediately.
    */
   public static void write( PSApplication app )
   {
      if ( null == app )
         return;

      OutputStream out = null;
      try
      {
         Document doc = app.toXml();
         File serverDir = PSServer.getRxDir();
         File debugfile = new File(serverDir, "dbg_" + app.getName() + ".xml");
         // bug# Rx-01-08-0075: make sure these files get cleaned up
         debugfile.deleteOnExit();
         debugfile.createNewFile();
         out = new FileOutputStream( debugfile );
         PSXmlDocumentBuilder.write( doc, out );
      }
      catch ( IOException e )
      {
         PSConsole.printMsg("Cms", e);
      }
      finally
      {
         try
         {
            if (null != out) out.close();
         }
         catch ( IOException e )
         {
            PSConsole.printMsg("Cms", e);
         }
      }
   }

   /**
    * Creates list of system mappings to set on app for inserts.  This will
    * provide values for non-user settable system fields when a new item is
    * inserted.
    *
    * @param ceHandler The content editor handler.  May not be <code>null
    * </code>.
    * @param ce The content editor definiton.  May not be <code>null</code>.
    *
    * @return ArrayList of system mappings, never <code>null</code>.
    */
   public static ArrayList<PSSystemMapping> getSystemInsertMappings(PSContentEditorHandler
      ceHandler, PSContentEditor ce)
   {
      ArrayList<PSSystemMapping> mappings = new ArrayList<>();
      PSSystemMapping mapping;


      try
      {
         // first add all system update mappings
         mappings.addAll(getSystemUpdateMappings(ceHandler, ce));
         String sysTableRef =
            PSContentEditorSystemDef.CONTENT_STATUS_TABLE_ALIAS;
         PSBackEndTable sysTable = new PSBackEndTable(sysTableRef);
         PSBackEndCredential sysCred = new PSBackEndCredential("cred");
         ceHandler.getSystemDef().populateSystemTableInfo(sysTableRef,
            sysCred, sysTable);

         // add currentrevision = revisionId html param, will insert as "1"
         mapping = new PSSystemMapping(sysTable, "CURRENTREVISION",
            new PSHtmlParameter(ceHandler.getParamName(
            PSContentEditorHandler.REVISION_ID_PARAM_NAME)));
         mappings.add(mapping);

         // add contentCheckoutUsername = username
         IPSDocumentMapping value = new PSUserContext("User/Name");
         if (!ceHandler.getCmsObject().isWorkflowable())
            value = new PSTextLiteral(""); //Set empty if not workflowable 
         mapping =
            new PSSystemMapping(sysTable, "CONTENTCHECKOUTUSERNAME", value);
         mappings.add(mapping);

         // add contentCreatedBy = username
         mapping = new PSSystemMapping(sysTable, "CONTENTCREATEDBY",
            new PSUserContext("User/Name"));
         mappings.add(mapping);

         // Create UDF's to put in a literal "1" and today's date
         // TODO: at some point we don't want hard coded udfs here
         PSExtensionRef literalRef = new PSExtensionRef("Java",
            "global/percussion/generic/", "sys_Literal");

         // add contentCreatedDate = current datetime
         mapping = new PSSystemMapping(sysTable, "CONTENTCREATEDDATE",
            getTodaysDateUdf());
         mappings.add(mapping);

         // make a UDF for literal "1"
         PSExtensionParamValue literalOne = new PSExtensionParamValue(
            new PSTextLiteral("1"));
         PSExtensionParamValue[] literalOneParams = {literalOne};
         PSExtensionCall literalOneUdf = new PSExtensionCall(literalRef,
            literalOneParams);

         // add contentTypeId = type specified in content editor def
         PSExtensionParamValue literalType = new PSExtensionParamValue(
            new PSTextLiteral(String.valueOf(ce.getContentType())));
         PSExtensionParamValue[] literalTypeParams = {literalType};
         PSExtensionCall literalTypeUdf = new PSExtensionCall(literalRef,
            literalTypeParams);

         mapping = new PSSystemMapping(sysTable, "CONTENTTYPEID",
            literalTypeUdf);
         mappings.add(mapping);

         // add workflowAppId = id from the request
         if ( ceHandler.getCmsObject().isWorkflowable())
         {
            mapping = new PSSystemMapping(sysTable, "WORKFLOWAPPID",
               new PSSingleHtmlParameter(IPSHtmlParameters.SYS_WORKFLOWID));
            mappings.add(mapping);

            //Initial state is to be looked up from the workflow
            //We use the UDF that looksup object name which is a misnomer!!!
            //UDF prepare start
            PSExtensionRef wfInitStateLookupRef = new PSExtensionRef("Java",
               "global/percussion/cms/", "sys_CmsObjectNameLookup");

            PSExtensionParamValue paramLookupResource =
               new PSExtensionParamValue(
               new PSTextLiteral("WorkflowInitialState"));

            PSExtensionParamValue paramWorkflowid = new PSExtensionParamValue(
               new PSSingleHtmlParameter(IPSHtmlParameters.SYS_WORKFLOWID));

            PSExtensionParamValue[] params =
               {paramLookupResource, paramWorkflowid};
            PSExtensionCall wfInitStateLookupUdf =
               new PSExtensionCall(wfInitStateLookupRef, params);
            //UDF prepare end

            mapping = new PSSystemMapping(sysTable, "CONTENTSTATEID",
               wfInitStateLookupUdf);
            mappings.add(mapping);
         }
         else // add workflowAppId = INVALID_WORKFLOW_ID if not workflowable
         {
            PSNumericLiteral literalNumber = new PSNumericLiteral(
               new Integer(IPSConstants.INVALID_WORKFLOW_ID),
               new DecimalFormat("#"));
            mapping = new PSSystemMapping(sysTable, "WORKFLOWAPPID",
               literalNumber);
            mappings.add(mapping);
            mapping = new PSSystemMapping(sysTable, "CONTENTSTATEID",
               literalOneUdf);
            mappings.add(mapping);
         }

         // add clonedparent mapping
         mapping = new PSSystemMapping(sysTable, "CLONEDPARENT",
            new PSSingleHtmlParameter(IPSHtmlParameters.SYS_CLONEDPARENTID));
         mappings.add(mapping);

         // add EDITREVISION = 1
         mapping = new PSSystemMapping(sysTable,
            IPSConstants.ITEM_EDITREVISION, literalOneUdf);
         mappings.add(mapping);

         // add TIPREVISION = 1
         mapping = new PSSystemMapping(sysTable,
            IPSConstants.ITEM_TIPREVISION, literalOneUdf);
         mappings.add(mapping);

         // add OBJECTTYPE = objectType of the content editor
         PSNumericLiteral literalNumber = new PSNumericLiteral(
            new Integer(ce.getObjectType()), new DecimalFormat("#"));
         mapping = new PSSystemMapping(sysTable, "OBJECTTYPE", literalNumber);

         mappings.add(mapping);

      }
      catch(IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      return mappings;
   }

   /**
    * Creates list of system mappings to set on app for updates.  This will
    * provide values for non-user settable system fields when an item is
    * updated.
    *
    * @param ceHandler The content editor handler.  May not be <code>null
    * </code>.
    * @param ce The content editor definiton.  May not be <code>null</code>.
    *
    * @return ArrayList of system mappings, never <code>null</code>.
    */
   public static ArrayList<PSSystemMapping> getSystemUpdateMappings(PSContentEditorHandler
      ceHandler, PSContentEditor ce)
   {
      ArrayList<PSSystemMapping> mappings = new ArrayList<>();
      PSSystemMapping mapping;

      try
      {
         String sysTableRef =
            PSContentEditorSystemDef.CONTENT_STATUS_TABLE_ALIAS;
         PSBackEndTable sysTable = new PSBackEndTable(sysTableRef);
         PSBackEndCredential sysCred = new PSBackEndCredential("cred");
         ceHandler.getSystemDef().populateSystemTableInfo(sysTableRef,
            sysCred, sysTable);

         // add contentId = contentId html param
         mapping = new PSSystemMapping(sysTable, "CONTENTID",
            new PSHtmlParameter(ceHandler.getParamName(
            PSContentEditorHandler.CONTENT_ID_PARAM_NAME)));
         mappings.add(mapping);

         // add contentLastModified = username
         mapping = new PSSystemMapping(sysTable, "CONTENTLASTMODIFIER",
            new PSUserContext("User/Name"));
         mappings.add(mapping);

         // add contentLastModifiedDate = current datetime
         mapping = new PSSystemMapping(sysTable, "CONTENTLASTMODIFIEDDATE",
            getTodaysDateUdf());
         mappings.add(mapping);
      }
      catch(IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      return mappings;

   }

   /**
    * Returns an extension call containing a udf that will return the current
    * datetime.
    *
    * @return The extension call.  Never <code>null</code>.
    */
   private static PSExtensionCall getTodaysDateUdf()
   {
      // TODO: at some point we don't want hard coded udfs here
      PSExtensionRef dateFormatRef = new PSExtensionRef("Java",
         "global/percussion/generic/", "sys_DateFormat");

      // pass empty params to get current datetime at runtime
      PSExtensionParamValue first = new PSExtensionParamValue(
         new PSTextLiteral(""));
      PSExtensionParamValue second = new PSExtensionParamValue(
         new PSTextLiteral(""));
      PSExtensionParamValue[] todaysDateParams = {first, second};
      PSExtensionCall todaysDateUdf = new PSExtensionCall(dateFormatRef,
         todaysDateParams);

      return todaysDateUdf;
   }

   /**
    * Initializes the data mappings for inserting or updating system fields
    * in the content status table when creating or updating a content item and
    * adds them to a data mapper.
    *
    * @param mappings An iterator over PSSystemMapping objects.  The table in
    * each mapping must be a system table specified in the system def or a
    * table specified in the display mappings of the passed in content editor.
    * If not a system table, the display mapper cannot also contain an entry
    * for this column.
    *
    * @return A data mapper with the system mappings in it.
    */
   public static PSDataMapper createSystemMappings(Iterator mappings)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      // create the mappings
      PSDataMapper dataMapper = new PSDataMapper();
      try
      {
         PSDataMapping mapping;
         String colName;
         PSBackEndColumn column;
         IPSDocumentMapping docMap;

         while (mappings.hasNext())
         {
            PSSystemMapping sysMapping = (PSSystemMapping)mappings.next();
            column = new PSBackEndColumn(sysMapping.getTable(),
               sysMapping.getColumnName());
            mapping = new PSDataMapping(sysMapping.getDocMapping(), column);
            dataMapper.add(mapping);
         }

      }
      catch (IllegalArgumentException e)
      {
         // this should have already been validated
         throw new RuntimeException("Invalid system locator mapping");
      }

      return dataMapper;
   }


   /**
    * Determines if the column name used is one of the key column names.
    *
    * @param columnName The name of the column to check.  May not be <code>null
    * </code> or empty.
    *
    * @return <code>true</code> if this is a key column, <code>false</code> if
    * not.
    */
   public static boolean isKeyColumn(String columnName)
   {
      boolean isKey = false;

      if (columnName == null || columnName.trim().length() == 0)
         throw new IllegalArgumentException(
            "columnName may not be null or empty");

      if (columnName.equalsIgnoreCase(IPSConstants.CHILD_ITEM_PKEY) ||
         columnName.equalsIgnoreCase(IPSConstants.ITEM_PKEY_CONTENTID) ||
         columnName.equalsIgnoreCase(IPSConstants.ITEM_PKEY_REVISIONID))
      {
         isKey = true;
      }

      return isKey;
   }


   /**
    * Determines if the column name used is one of the reserved column
    * names.
    *
    * @param columnName The name of the column to check.  May not be <code>null
    * </code> or empty.
    *
    * @return <code>true</code> if this is a reserved column, <code>false</code>
    * if not.
    */
   public static boolean isReservedColumn(String columnName)
   {
      boolean isReserved = false;

      if (columnName == null || columnName.trim().length() == 0)
         throw new IllegalArgumentException(
            "columnName may not be null or empty");

      if (columnName.equalsIgnoreCase(IPSConstants.CHILD_SORT_KEY) ||
         isKeyColumn(columnName))
      {
         isReserved = true;
      }

      return isReserved;
   }


   /**
    * Constant for the Html parameter name used to determine what db action
    * is required.
    */
   public static final String REQUEST_TYPE_HTML_PARAMNAME = "DBActionType";

   /**
    * Constant used to specify that a query action is to be performed.
    */
   public static final String REQUEST_TYPE_VALUE_QUERY = "QUERY";

   /**
    * Constant used to specify that a update action is to be performed.
    */
   public static final String REQUEST_TYPE_VALUE_UPDATE = "UPDATE";

   /**
    * Constant used to specify that a insert action is to be performed.
    */
   public static final String REQUEST_TYPE_VALUE_INSERT = "INSERT";

   /**
    * Constant used to specify that a delete action is to be performed.
    */
   public static final String REQUEST_TYPE_VALUE_DELETE = "DELETE";

   /**
    * Constant for the name of the contentstatus table.
    */
   private static final String CONTENT_STATUS_TABLE = "CONTENTSTATUS";

   /**
    * Flag used when creating update datasets to indicate if inserts are
    * allowed.
    */
   public static final int FLAG_ALLOW_INSERTS = 0x0001;

   /**
    * Flag used when creating update datasets to indicate if update are
    * allowed.
    */
   public static final int FLAG_ALLOW_UPDATES = 0x0002;

   /**
    * Flag used when creating update datasets to indicate if deletes are
    * allowed.
    */
   public static final int FLAG_ALLOW_DELETES = 0x0004;
}




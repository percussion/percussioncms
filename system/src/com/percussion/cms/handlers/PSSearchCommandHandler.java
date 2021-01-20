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

package com.percussion.cms.handlers;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.conn.PSServerException;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.IPSResultSetDataFilter;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSFolderPermissionsDataFilter;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMimeContentResult;
import com.percussion.data.PSQueryHandler;
import com.percussion.data.PSUnsupportedConversionException;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSFunctionParamValue;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSPageDataTank;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSResultPageSet;
import com.percussion.design.objectstore.PSResultPager;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.extension.PSDatabaseFunctionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSUniqueObjectGenerator;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class handles the 'search' command. It will create a new
 * dynamic search query based on specific data. The search input
 * document is defined in the sys_SearchParameters.xsd schema file.
 * The request contains an input document that defines the response
 * elements for the specified search. The input document also contains
 * the where clause data (also defined in sys_SearchParameters.xsd) that
 * indicates to the search what conditional clauses to create.
 *
 * Because of the preceding requirements, this command handler can only be
 * accessed via an internal request, currently support for standard browser
 * requests has not bee implemented.
 */
public class PSSearchCommandHandler extends PSCommandHandler
   implements IPSInternalCommandRequestHandlerEx, IPSInternalResultHandler
{
   /**
    * The internal name of this handler. When handler names are used in
    * config files, this is the name that must be used.
    */
   public static final String COMMAND_NAME = "search";

   /**
    * Initializes a search command handler.
    *
    * @param ah The handler for the application that contains this editor
    * resource. See base class for requirements.
    * @param ceh The parent handler of this handler. Never <code>null</code>.
    * @param ce The definition of the editor. See base class for requirements.
    * @param app Any resources that are created dynamically will be added to
    * this application. Never <code>null</code>.
    * @throws PSNotFoundException If an exit cannot be found.
    * @throws PSExtensionException If any problems occur druing extension
    * initialization.
    * @throws PSServerException
    */
   public PSSearchCommandHandler(PSApplicationHandler ah,
                                 PSContentEditorHandler ceh,
                                 PSContentEditor ce,
                                 PSApplication app)
      throws PSNotFoundException,
             PSExtensionException,
             PSServerException
   {
      super(ah, ceh, ce, app);
      
      init();
   }

   // see IPSInternalRequestHandler interface for description
   public PSExecutionData makeInternalRequest(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      // store handlers so we can clean them up at the end.
      List execDataCleanupList = new ArrayList();

      PSExecutionData data = new PSExecutionData(m_appHandler, this, request);

      try
      {
         executeSearchRequest(request, execDataCleanupList);

         return data;
      }
      catch (Exception e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
            getExceptionText(e));
      }
      finally
      {
         cleanup(data, execDataCleanupList);
      }
   }

   // see IPSInternalCommandRequestHandlerEx interface for description
   public Document makeInternalRequestEx(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      return getResultDocument(request);
   }

   // See IPSInternalResultHandler interface for description
   @SuppressWarnings("unused")
   public Document getResultDoc(PSExecutionData data)
   {
      throw new UnsupportedOperationException("getResultDoc");
   }

   // See IPSInternalResultHandler interface for description
   @SuppressWarnings("unused")
   public ResultSet getResultSet(PSExecutionData data)
   {
      throw new UnsupportedOperationException("getResultSet");
   }

   // See IPSInternalResultHandler interface for description
   
   @SuppressWarnings("unused")
   public PSMimeContentResult getMimeContent(PSExecutionData data,
      boolean setResponse) throws PSInternalRequestCallException
   {
      throw new UnsupportedOperationException("getMimeContent");
   }

   /**
    * Creates a search result document using the supplied request.
    *
    * @param request the request to make, not <code>null</code>.
    * @return the result document created through the provided request,
    *    never <code>null</code>.
    * @throws PSInternalRequestCallException
    * @throws PSAuthorizationException
    * @throws PSAuthenticationFailedException
    */
   public Document getResultDocument(PSRequest request)
      throws PSInternalRequestCallException,
         PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      // store handlers so we can clean them up at the end.
      List execDataCleanupList = new ArrayList();

      try
      {
         return executeSearchRequest(request, execDataCleanupList);
      }
      catch (PSUnsupportedConversionException e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
            getExceptionText(e));
      }
      catch (PSValidationException e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
            getExceptionText(e));
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSInternalRequestCallException(
            e.getErrorCode(), e.getErrorArguments());
      }
      finally
      {
         cleanup(null, execDataCleanupList);
      }
   }
   
   /**
    * Determine if the back end is case sensitive or not.
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    * 
    * @throws IllegalStateException if at least one instance of this class has
    * not been initialized.
    */
   public static boolean isDBCaseSensitive()
   {
      if (!ms_isInited)
         throw new IllegalStateException("Cannot call isDBCaseSensitive() " +
            "before PSSearchCommandHandler is initialized");
            
      return ms_isDBCaseSensitive;
   }
   
   public boolean isBinary(PSRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      return false;
   }
   
   /**
    * Executes the provided search request to produce the execution data.
    *
    * @param req the request, assumed not <code>null</code>.
    * @param execDataCleanupList a list of execution data objects that need
    *    cleanup when done with the request.
    *    
    * @return The results of the search, never <code>null</code>.
    * 
    * @throws PSInternalRequestCallException If there is an error making an 
    * internal request
    * @throws PSAuthorizationException If the user is not authorized to perform
    * this search.
    * @throws PSAuthenticationFailedException If the user has not been 
    * authenticated.
    * @throws PSValidationException If there is an error adding the dynamic
    * search dataset.
    * @throws PSUnsupportedConversionException if an invalid page type is
    * requested based on the page extension.
    * @throws PSUnknownNodeTypeException if the input doc does not conform to 
    * the sys_SearchParameters.xsd schema.
    */
   @SuppressWarnings("unchecked")
   private Document executeSearchRequest(PSRequest req, 
      List execDataCleanupList)
         throws PSInternalRequestCallException,
            PSAuthorizationException, PSAuthenticationFailedException,
            PSValidationException, PSUnsupportedConversionException,
            PSUnknownNodeTypeException
   {
      // We only support requests for htm?, xml or txt
      if (PSRequest.PAGE_TYPE_UNKNOWN == req.getRequestPageType())
      {
         String pageExt = req.getRequestPageExtension();
         throw new PSUnsupportedConversionException(
            IPSDataErrors.HTML_CONV_EXT_NOT_SUPPORTED, pageExt);
      }

      Document doc;

      // create the dynamic dataset
      PSDataSet ds = createSearchDataSet(req);
      if (ds != null)
      {
         // get the internal application handler
         PSApplicationHandler ah = m_ceHandler.getInternalAppHandler();

         // add the new dynamic dataset
         ah.addDataHandler(ds);

         // create an internal request to the dataset
         String reqName = createRequestName(m_internalApp.getName(),
                                            ds.getName());
         IPSInternalResultHandler rh =
            (IPSInternalResultHandler)PSServer.getInternalRequestHandler(
               reqName);

         if (rh == null)
         {
            throw new RuntimeException("Unexpected: Couldn't find handler for "
               + ds.getName());
         }

         if (rh instanceof PSQueryHandler)
         {
            IPSResultSetDataFilter filter = getResulSetDataFilter(
               m_ceHandler.getCmsObject());
            
            if (filter != null)
               ((PSQueryHandler) rh).setRowFilter(filter);
         }

         // make the request to the dynamic dataset
         PSExecutionData reqData = rh.makeInternalRequest(req);
         execDataCleanupList.add(reqData);

         // get the result set
         doc = rh.getResultDoc(reqData);
         
         // remove the dynamic data handler
         ah.removeDataHandler(ds);
      }
      else
      {
         // return empty doc
         doc = PSXmlDocumentBuilder.createXmlDocument();
      }
         
      convertResultDocument(doc);

      return doc;
   }

   /**
    * Get the result set data filter based on the supplied cms object
    * 
    * @param cmsObject The cmsObject to use to define the correct filter,
    * assumed not <code>null</code>. 
    * 
    * @return The filter to use, or <code>null</code> if no filter is required.
    */
   private static IPSResultSetDataFilter getResulSetDataFilter(
      PSCmsObject cmsObject)
   {
      IPSResultSetDataFilter filter = null;
      
      if (cmsObject.getTypeId() == PSCmsObject.TYPE_FOLDER)
      {
         // add folder permissions filter
         filter = new PSFolderPermissionsDataFilter("contentid");
      }
      
      return filter;
   }

   /**
    * Creates a new dynamic dataset to be added to the current handler to
    * execute the search request.
    
    * @param request the request, assumed not <code>null</code>.
    * @return a complete dataset to be added to the command handler to build
    * the SQL for the specified search, this is then removed after the search
    * has been executed.  Will be <code>null</code> if the supplied 
    * <code>request</code> does not specify any search criteria.
    * 
    * @throws PSUnknownNodeTypeException if the input doc does not conform to 
    * the sys_SearchParameters.xsd schema.
    */
   @SuppressWarnings({"unchecked","unchecked"})
   private PSDataSet createSearchDataSet(PSRequest request) 
      throws PSUnknownNodeTypeException
   {
      Collection<String> dynMapper = new HashSet<String>();
      dynMapper.addAll(ms_staticSearchResults);
      
      boolean hasSearchRequest = false;
      PSWSSearchRequest searchRequest = 
         (PSWSSearchRequest)request.getPrivateObject(SEARCH_REQUEST_OBJECT);
      if (searchRequest == null)
      {
         // check input doc
         Document inputDoc = request.getInputDocument();
         Element root = null;
         if (inputDoc != null)
            root = inputDoc.getDocumentElement();
         
         if (root != null)
         {
            searchRequest = new PSWSSearchRequest(root);
         }
      }
      
      if (searchRequest != null)
      {
         PSWSSearchParams searchParams = searchRequest.getSearchParams();
         if (searchParams != null)
         {
            dynMapper.addAll(searchParams.getResultFields());
            hasSearchRequest = true;
         }
      }

      // if no search request, return null
      if (!hasSearchRequest)
         return null;
      
      
      try
      {
         PSQueryPipe pipe = new PSQueryPipe(
            PSUniqueObjectGenerator.makeUniqueName("QueryPipe"));
         PSDataSet ds = new PSDataSet(
            PSUniqueObjectGenerator.makeUniqueName("DataSet"));

         // set up the result pager
         // only if the maximum results values exists on the request
         try
         {
            int max = Integer.parseInt(
               request.getParameter(MAXIMUM_RESULTS));

            PSResultPager rp = new PSResultPager();
            rp.setMaxRowsPerPage(max);
            rp.setMaxPageLinks(0);

            ds.setResultPager(rp);
         }
         catch (NumberFormatException nfe)
         {
            /* ignore, do not set up a result pager */
         }

         // set up page tank
         File appDir = new File(m_internalApp.getRequestRoot());
         appDir.mkdir();
         
         String dtdRoot = EL_SEARCH_RESPONSE;
         IPSRhythmyxInfo rxInfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
         String rxRootDir = (String) rxInfo
               .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);

         File dtdFile = new File(rxRootDir + File.separator
               + "DTD/sys_SearchResponse.dtd");
         URL dtdUrl = dtdFile.toURL();
         PSPageDataTank page = new PSPageDataTank(dtdUrl);
         ds.setPageDataTank(page);

         // set up mapper
         PSContentEditorPipe cePipe = (PSContentEditorPipe)m_ce.getPipe();
         PSContentEditorMapper ceMapper = cePipe.getMapper();
         PSFieldSet ceFieldSet = ceMapper.getFieldSet();
         
         // set up the table map
         Map tableMap = cePipe.getLocator().getBackEndTables();

         // this is the table associated with this content type
         PSBackEndTable localTable = null;
         // these are the child tables for the required search
         Map childTables = new HashMap();

         // 1 of each table used by mappings, fully qualified
         PSCollection tables = new PSCollection(PSBackEndTable.class);

         /* Walk thru all mappings and re-order them for optimal grouping */
         Iterator dynIter = dynMapper.iterator();
         Map colMap = new HashMap();
         while (dynIter.hasNext())
         {
            String fieldRef = (String)dynIter.next();
            
            // field sets indicate a child which is handled seperately
            Object o = ceFieldSet.get(fieldRef);
            PSField field;
            
            boolean childField;
            if (o instanceof PSField)
            {
               field = (PSField)o;
               childField = false;
            }
            else
            {
               field = ceFieldSet.findFieldByName(fieldRef);
               childField = true;
            }

            if (field == null)
            {
               // let's check the read only fields
               field = getSystemFieldByName(fieldRef);
               childField = false;
            }

            // if the field is still not found, it must not exist in this
            // content type just ignore and continue with the rest of the fields
            if (field == null)
               continue;

            IPSBackEndMapping locator = field.getLocator();
            if (locator instanceof PSBackEndColumn)
            {
               PSBackEndColumn col = (PSBackEndColumn)locator;
               String alias = col.getTable().getAlias().toLowerCase();
               List cols = (List)colMap.get(alias);
               if (cols == null)
               {
                  cols = new ArrayList();
                  colMap.put(alias, cols);
               }
               cols.add(fieldRef);
               switch (field.getType())
               {
                  case PSField.TYPE_SHARED:
                  case PSField.TYPE_LOCAL:
                     if (childField)
                     {
                        childTables.put(alias, col.getTable());
                     }
                     else
                     {
                        if (tables.isEmpty())
                        {
                           alias = col.getTable().getAlias();
                           tables.add(tableMap.get(alias.toLowerCase()));
                        }
                     }
                     break;
               }
            }
            else if (locator instanceof PSExtensionCall)
            {
               PSExtensionCall ec = (PSExtensionCall)locator;
               String alias = ec.getName();
               List cols = (List)colMap.get(alias);
               if (cols == null)
               {
                  cols = new ArrayList();
                  colMap.put(alias, cols);
               }
               cols.add(fieldRef);
            }
            else if(locator instanceof PSTextLiteral)
            {
               PSTextLiteral tl = (PSTextLiteral)locator;
               String alias = tl.getText();
               List cols = (List)colMap.get(alias);
               if (cols == null)
               {
                  cols = new ArrayList();
                  colMap.put(alias, cols);
               }
               cols.add(fieldRef);
            }
         }

         // this is the table associated with the main mapper
         // it will be the parent of all joins
         PSBackEndTable beTable = new PSBackEndTable(
            PSContentEditorSystemDef.CONTENT_STATUS_TABLE_ALIAS);
         PSBackEndTable systemTable = (PSBackEndTable)tableMap.get(
            beTable.getAlias().toLowerCase());

         // create the "where" clauses
         List<PSBackEndTable> stateSearchTables = 
            new ArrayList<PSBackEndTable>();
         PSCollection conditionals = createConditionals(searchRequest,
                                                        ceFieldSet,
                                                        tables,
                                                        tableMap,
                                                        childTables,
                                                        stateSearchTables);

         // if there are only references to the child table and we
         // are not searching on the parent content type table, we
         // get the first local field and use that to get the local
         // table name, which is needed to do the join
         if (!(childTables.isEmpty() && stateSearchTables.isEmpty()) && 
            tables.isEmpty())
         {
            Iterator iter = ceFieldSet.getEveryField();
            while (iter.hasNext())
            {
               Object field = iter.next();
               if (field instanceof PSField)
               {
                  PSField f = (PSField) field;
                  if (f.getType() == PSField.TYPE_LOCAL)
                  {
                     PSBackEndColumn col = (PSBackEndColumn)f.getLocator();
                     String alias = col.getTable().getAlias();
                     tables.add(tableMap.get(alias.toLowerCase()));
                     break;
                  }
               }
            }
         }

         if (!tables.isEmpty())
         {
            localTable = (PSBackEndTable)tables.get(0);
         }

         // add the system table to the list
         tables.add(systemTable);

         if (!childTables.isEmpty())
         {
            // we have 1 or more child tables
            Iterator childTablesIter = childTables.values().iterator();
            while (childTablesIter.hasNext())
            {
               PSBackEndTable table = (PSBackEndTable)childTablesIter.next();
               table = (PSBackEndTable)tableMap.get(
                  table.getAlias().toLowerCase());
               childTables.put(table.getAlias().toLowerCase(), table);
               tables.add(table);
            }
         }

         for (PSBackEndTable table : stateSearchTables)
         {
            tables.add(table);
         }
         
         // create the mapper
         PSDataMapper mapper = new PSDataMapper();
         mapper.setAllowEmptyDocReturn(true);

         int groupId = 0;
         PSBackEndColumn col;
         PSDataMapping mapping = null;
         dtdRoot += "/" + EL_RESULT;

         /**
          * Note: the following fields are added this way because we dont't
          * want to add them to the system definition. If you add additional
          * fields, you should add them into the static list unless the field
          * is not specified in the system definition.
          */

         // always add the current revision to the search result
         col = new PSBackEndColumn(systemTable, 
            IPSConstants.ITEM_CURRENTREVISION);
         mapping = new PSDataMapping(dtdRoot + "/" + "sys_currentrevision", col);
         mapping.setGroupId(groupId);
         mapper.add(mapping);

         // always add the tip revision to the search result
         col = new PSBackEndColumn(systemTable, IPSConstants.ITEM_TIPREVISION);
         mapping = new PSDataMapping(dtdRoot + "/" + "sys_tiprevision", col);
         mapping.setGroupId(groupId);
         mapper.add(mapping);

         groupId++;

         // now we have all columns grouped by table name
         Iterator colSets = colMap.values().iterator();
         while (colSets.hasNext())
         {
            Iterator csIter = ((List)colSets.next()).iterator();
            
            while (csIter.hasNext())
            {
               String fieldRef = (String)csIter.next();
               // field sets indicate a child which is handled seperately
               Object o = ceFieldSet.get(fieldRef);
               PSField field;
               if (o instanceof PSField)
               {
                  field = (PSField)o;
               }
               else
               {
                  field = ceFieldSet.findFieldByName(fieldRef);
               }

               if (field == null)
               {
                  // let's check the read only fields
                  field = getSystemFieldByName(fieldRef);
               }

               // if the field is still not found, it must not exist in this
               // content type just ignore and continue with the rest of the fields
               if (field == null)
                  continue;
               
               IPSBackEndMapping backEndMapping = field.getLocator();
               
               // must clone the backend mapping to avoid alias modifications
               // of system field columns by the query optimizer later                     
               if (backEndMapping instanceof PSBackEndColumn)
               {
                  /*note: The backEndMapping.clone() fails with 
                   * NullPointerException!
                   * The reason of this bug is NOT known!
                   * It only happens when the server runs with the JIT enabled.
                   * Nothing bad happens if the server is running under 
                   * debugger!
                   */
                  PSBackEndColumn column = (PSBackEndColumn)backEndMapping;
                  backEndMapping = copyBackendColumn(column);
               }
  
               mapping = new PSDataMapping(dtdRoot + "/" +
                  field.getSubmitName(), backEndMapping);
               mapping.setGroupId(groupId);
               mapper.add(mapping);

            }
            
            groupId++;
         }
         
         pipe.setDataMapper(mapper);
         ds.setPipe(pipe);

         // add tables and joins to the tank
         PSBackEndDataTank tank = new PSBackEndDataTank();
         tank.setTables(tables);

         // add joins if needed
         PSCollection joins = new PSCollection(PSBackEndJoin.class);

         if (localTable != null)
         {
            if (systemTable != null)
            {
               PSBackEndColumn leftCol =
                  new PSBackEndColumn(localTable,
                     IPSConstants.ITEM_PKEY_CONTENTID);
               PSBackEndColumn rightCol =
                  new PSBackEndColumn(systemTable,
                     IPSConstants.ITEM_PKEY_CONTENTID);
               PSBackEndJoin join = new PSBackEndJoin(leftCol, rightCol);
               joins.add(join);
               leftCol = new PSBackEndColumn(localTable,
                  IPSConstants.ITEM_PKEY_REVISIONID);
               rightCol = new PSBackEndColumn(systemTable,
                  IPSConstants.ITEM_CURRENTREVISION);
               join = new PSBackEndJoin(leftCol, rightCol);
               joins.add(join);
            }
            Iterator childTableIter = childTables.values().iterator();
            while (childTableIter.hasNext())
            {
               PSBackEndTable table = (PSBackEndTable)childTableIter.next();
               PSBackEndColumn leftCol =
                  new PSBackEndColumn(localTable,
                     IPSConstants.ITEM_PKEY_CONTENTID);
               PSBackEndColumn rightCol =
                  new PSBackEndColumn(table,
                     IPSConstants.ITEM_PKEY_CONTENTID);
               PSBackEndJoin join = new PSBackEndJoin(leftCol, rightCol);
               joins.add(join);
               leftCol = new PSBackEndColumn(localTable,
                  IPSConstants.ITEM_PKEY_REVISIONID);
               rightCol = new PSBackEndColumn(table,
                  IPSConstants.ITEM_PKEY_REVISIONID);
               join = new PSBackEndJoin(leftCol, rightCol);
               joins.add(join);
            }
            
            if (!stateSearchTables.isEmpty())
            {
               PSBackEndColumn leftCol =
                  new PSBackEndColumn(systemTable, WORKFLOWAPPID);
               PSBackEndColumn rightCol =
                  new PSBackEndColumn(ms_workflowAppsTable, WORKFLOWAPPID);
               PSBackEndJoin join = new PSBackEndJoin(leftCol, rightCol);
               joins.add(join);
               leftCol = new PSBackEndColumn(ms_workflowAppsTable, 
                  WORKFLOWAPPID);
               rightCol = new PSBackEndColumn(ms_statesTable, WORKFLOWAPPID);
               join = new PSBackEndJoin(leftCol, rightCol);
               joins.add(join);
               leftCol = new PSBackEndColumn(systemTable, 
                  CONTENTSTATEID);
               rightCol = new PSBackEndColumn(ms_statesTable, STATEID);
               join = new PSBackEndJoin(leftCol, rightCol);
               joins.add(join);
               
            }
         }
         
         
         if (!joins.isEmpty())
         {
            tank.setJoins(joins);
         }
         pipe.setBackEndDataTank(tank);

         // set up selector (where clause)
         PSDataSelector selector = new PSDataSelector();
         PSCollection clauses = new PSCollection(PSWhereClause.class);

         // always add the contentTypeId where clause
         PSBackEndColumn leftVal =
            new PSBackEndColumn(systemTable, IPSConstants.CONTENTTYPEID_COLUMN);

         PSNumericLiteral rightVal = new PSNumericLiteral(m_ce.getContentType(), 
            new DecimalFormat("#"));

         PSWhereClause clause = new PSWhereClause(
            leftVal, PSConditional.OPTYPE_EQUALS, rightVal, true);

         clauses.add(clause);

         if (conditionals != null)
         {
            for (int sIdx = 0; sIdx < conditionals.size(); sIdx++)
            {
               PSConditional cond = (PSConditional)conditionals.elementAt(sIdx);

               clause = new PSWhereClause(cond.getVariable(),
                  cond.getOperator(), cond.getValue(), true);
               clause.setBoolean(cond.getBoolean());
               clauses.add(clause);
            }
         }
         selector.setWhereClauses(clauses);

         pipe.setDataSelector(selector);

         // set up requestor
         PSRequestor req = new PSRequestor();
         req.setRequestPage(PSUniqueObjectGenerator.makeUniqueName("request"));
         HashMap mimeTypes = new HashMap();
         PSTextLiteral type = new PSTextLiteral("text/html");
         mimeTypes.put("htm", type);
         mimeTypes.put("html", type);
         req.setMimeProperties(mimeTypes);
         ds.setRequestor(req);

         /* set up result pages, there must be at least one defined, null
         means use the system default */
         PSResultPage resultPage = new PSResultPage(null);
         PSResultPageSet pageSet = new PSResultPageSet();
         PSCollection pages = new PSCollection(resultPage.getClass());
         pages.add(resultPage);
         pageSet.setResultPages(pages);
         ds.setOutputResultPages(pageSet);

         return ds;
      }
      catch (MalformedURLException me)
      {
         throw new RuntimeException(me.getLocalizedMessage());
      }
   }

   /**
    * Produces the final search result document, this includes all the results
    * retrieved from the search. The definition of each result row was defined
    * by the dynamic mapper passed in. Each field within the dynMapper is
    * checked against the backend to get the proper locator and if it does not
    * exist to just ignore that field for this content type. Converts to
    * a document with the completed search results, defined in
    * sys_SearchParameters.xsd schema file, may be <code>null</code>
    *    
    * @param doc the document to convert, assumed not <code>null</code>.
    */
   private void convertResultDocument(Document doc)
   {
      Element root = doc.getDocumentElement();

      // if no results to convert just return
      if (root == null)
         return;

      // convert from
      // <result ...>
      //    <fieldname>data</fieldname>
      // </result>
      // to:
      // <result ...>
      //    <resultfield name="fieldname">data</resultfield>
      // </result>
      Element resultEl = PSXMLDomUtil.getFirstElementChild(root);
      while (resultEl != null && resultEl.getNodeName().equals(EL_RESULT))
      {
         Element el = PSXMLDomUtil.getFirstElementChild(resultEl);
         while (el != null)
         {
            Element newEl = doc.createElement(EL_RESULT_FIELD);
            newEl.setAttribute(ATTR_NAME, el.getNodeName());
            newEl.appendChild(
               doc.createTextNode(PSXMLDomUtil.getElementData(el)));

            resultEl.replaceChild(newEl, el);

            el = PSXMLDomUtil.getNextElementSibling(newEl);
         }
         resultEl = PSXMLDomUtil.getNextElementSibling(resultEl);
      }
   }

   /**
    * A simple helper routine to return the system field based on the
    * "display" names given in the search message.
    *
    * @param fieldName the name of the system field to retrieve, the match
    * is done case insensitive
    *
    * @return returns the field if found, otherwise <code>null</code>
    */
   private PSField getSystemFieldByName(String fieldName)
   {
      PSFieldSet systemFieldSet =
         PSServer.getContentEditorSystemDef().getFieldSet();

      return systemFieldSet.findFieldByName(fieldName, true);
   }
   
   /**
    * Creates a copy of the supplied backend column.
    * 
    * @param column The column to copy, assumed not <code>null</code>.
    * 
    * @return The copy of the column, never <code>null</code>.
    */
   private PSBackEndColumn copyBackendColumn(PSBackEndColumn column)
   {
      PSBackEndColumn colCopy = 
         new PSBackEndColumn(column.getTable(), column.getColumn());
                  
      colCopy.copyFrom(column);
                  
      return colCopy;      
   }

   /**
    * This has not been implemented for general use by the browser front end,
    * currently we expect an input XML document for the where clauses as well
    * as the definition of the output data elements. It is a non standard
    * command handler that currently supports only internal requests.
    *
    * @param req not used
    */
   public void processRequest(@SuppressWarnings("unused") PSRequest req) {}

   /**
    * Cleanup all resources created during a request. Provide <code>null</code>
    * to keep the execution data and clean it yourself.
    *
    * @param data the execution data to be cleaned, <code>null</code> if nothing
    *    to clean.
    * @param execDataCleanupList a list of execution data to be cleaned,
    *    assumed not <code>null</code>.
    */
   private void cleanup(PSExecutionData data, List execDataCleanupList)
   {
      Iterator execData = execDataCleanupList.iterator();
      while (execData.hasNext())
      {
         PSExecutionData reqData = (PSExecutionData)execData.next();
         reqData.release();
      }

      // finally, release any resources held by the execution context
      if (data != null)
         data.release();
   }

   /**
    * Performs resource cleanup when the handler is shut down. If derived
    * classes override this method, they must perform their own cleanup, then
    * call the base class.
    */
   public void shutdown()
   {
      // dynamic app shutdown by content editor handler
   }


   /**
    * Used to create the where clauses from the search fields in the supplied
    * search request.
    *
    * @param searchReq The object defining the search request, assumed not  
    * <code>null</code> and for {@link PSWSSearchRequest#getSearchParams()}
    * not to return <code>null</code>.
    *
    * @param fieldSet this content editor's field set to determine the
    * true location of the field within the backend table assumed not
    * <code>null</code>
    *
    * @param tables the <code>PSBackEndTable</code> objects that will be used to 
    * return the column names for the conditional requirements assumed not 
    * <code>null</code>.
    *
    * @param tableMap the map of existing tables to retrieve the connection
    * and other specific details of the table.  Key is the lowercased table 
    * alias as a <code>String</code>, and the value is the corresponding
    * <code>PSBackEndTable</code> object.
    *
    * @param childTables the child tables used by this search, assumed
    * not <code>null</code>, entries for child tables required are added to the 
    * map.  The key is the lowercased table alias as a <code>String</code>, and 
    * the value is the corresponding <code>PSBackEndTable</code> object.
    * @param stateSearchTables List to which tables required for statename 
    * search are added if required, assumed not <code>null</code>.
    *
    * @return returns a collection of conditionals that define the where
    * clauses for a specific search command, the collection may be empty if no
    * input document is found in the request or it doesn't contain any
    * elements that define conditions
    */
   @SuppressWarnings("unchecked")
   private PSCollection createConditionals(PSWSSearchRequest searchReq,
      PSFieldSet fieldSet, Collection tables, Map tableMap, Map childTables,
      List<PSBackEndTable> stateSearchTables)
   {
      PSCollection conditionals = new PSCollection(PSConditional.class);
      
      boolean useDatabaseCase = !searchReq.isCaseInsensitiveSearch();
      PSWSSearchParams searchParams = searchReq.getSearchParams();

      Iterator searchFields = searchParams.getSearchFields().iterator();
      while (searchFields.hasNext())
      {
         PSWSSearchField searchField = (PSWSSearchField)searchFields.next();                  
         String name = searchField.getName();
         
         Object o = fieldSet.get(name);
         PSField field;
         boolean childField;
         boolean stateNameField = false;
         if (o instanceof PSField)
         {
            field = (PSField)o;
            childField = false;
         }
         else
         {
            field = fieldSet.findFieldByName(name);
            childField = true;
         }

         if (field == null)
         {
            if (name.equals(IPSHtmlParameters.SYS_STATE_NAME))
            {
               stateNameField = true;
               field = ms_stateNameField;
            }
            else
            {
               // check the read only fields as well
               field = getSystemFieldByName(name);
            }
            childField = false;
         }

         // if we don't find the field within this fieldset,
         // just ignore the conditional for this content type
         if (field != null)
         {
            // must clone the backend mapping to avoid alias modifications
            // of system field columns by the query optimizer later
            IPSReplacementValue leftVal = field.getLocator();
            if (leftVal instanceof PSBackEndColumn)
            {
               /*note: The backEndMapping.clone() fails with 
                * NullPointerException!
                * The reason of this bug is NOT known!
                * It only happens when the server runs with the JIT enabled.
                * Nothing bad happens if the server is running under debugger!
                */
               PSBackEndColumn column = (PSBackEndColumn)leftVal;
               leftVal = copyBackendColumn(column);
            }
            
            appendConditional(
               conditionals,
               leftVal,
               field.getDataType(),
               searchField,
               useDatabaseCase);

            PSBackEndColumn col = (PSBackEndColumn)leftVal;

            if (!field.isSystemField())
            {
               if (childField)
               {
                  String alias = col.getTable().getAlias().toLowerCase();
                  childTables.put(alias, col.getTable());
               }
               else if (stateNameField)
               {
                  stateSearchTables.add(ms_workflowAppsTable);
                  stateSearchTables.add(ms_statesTable);
               }
               else
               {
                  // add to the list if not there yet
                  if (tables.isEmpty())
                  {
                     String alias = col.getTable().getAlias();
                     tables.add(tableMap.get(
                        alias.toLowerCase()));
                  }
               }
            }
         }
      }      
      
      return conditionals;
   }

   /**
    * Private helper function to append to the list of conditionals.
    *
    * @param conditionals the current collection of conditions, assumed not
    * <code>null</code>, may be an empty collection
    *
    * @param leftVal the left value of the conditional,
    * assumed not <code>null</code>
    *
    * @param dataType the data type of the field used to determine the type of
    * literal to build for the right hand side of the expression, allowed types
    * one of the <code>PSField.DT_xxx</code> values, assumed not <code>null</code>
    *
    * @param searchField the current field that is being added as a condition
    * assumed not <code>null</code>
    *
    * @param useDatabaseCase if <code>false</code> and database is
    * case-sensitive, then the database function "UPPER" is applied to the
    * left value of the conditional if it is an instance of
    * <code>com.percussion.design.objectstore.PSBackEndColumn</code> and
    * <code>data</code> is converted to uppercase.
    */
   private void appendConditional(
      PSCollection conditionals,
      IPSReplacementValue leftVal,
      String dataType,
      PSWSSearchField searchField,
      boolean useDatabaseCase)
   {
      String op = convertOperator(searchField);
      String data = searchField.getValue();
      
      // based on the field data type create the proper literal
      IPSReplacementValue rightVal = null;
      try
      {
         if (dataType.equals(PSField.DT_INTEGER) ||
            dataType.equals(PSField.DT_FLOAT))
         {
            DecimalFormat df = null;
            if (dataType.equals(PSField.DT_INTEGER))
               df = new DecimalFormat("#");
            else
               df = new DecimalFormat("#.###############");

            if (op.equals(PSConditional.OPTYPE_IN) ||
               op.equals(PSConditional.OPTYPE_NOTIN))
            {
               PSLiteralSet ls = new PSLiteralSet(PSNumericLiteral.class);
               List list = PSStringOperation.getSplittedList(data, ',');
               Iterator iter = list.iterator();
               while (iter.hasNext())
               {
                  ls.add(new PSNumericLiteral(
                     new Integer((String)iter.next()), df));
               }
               rightVal = ls;
               if ((ms_contentStatusContentIdColumn != null) &&
                  (leftVal instanceof PSBackEndColumn) &&
                  (((PSBackEndColumn)leftVal).doesMatch(
                        ms_contentStatusContentIdColumn)))
               {     
                  PSFunctionParamValue funcParam = new PSFunctionParamValue(ls);
                  PSFunctionParamValue funcParams[] = null;
                  String function = null;
                  // This clause is only executed for the oracle driver. This
                  // is controlled by the content of the
                  // ms_contentStatusContentIdColumn,
                  // which is only set for oracle.
                  // 
                  // The selection here optimizes the use of IN-NUMBER-ARRAY to
                  // only occur for cases where oracle cannot use IN-NUMBER.
                  if (list.size() >= 1000)
                  {
                     function = FUNC_NAME_IN_NUMBER_ARRAY;
                     funcParams = new PSFunctionParamValue[]{funcParam};
                  }
                  else
                  {
                     function = FUNC_NAME_IN_NUMBER;
                     PSLiteralSet emptyset = 
                        new PSLiteralSet(PSNumericLiteral.class);
                     PSFunctionParamValue empty = 
                        new PSFunctionParamValue(emptyset);
                     funcParams = new PSFunctionParamValue[]{funcParam, empty};
                  }
                  // This is CONTENTSTATUS.CONTENTID column with IN/NOTIN clause
                  // Need to convert rightVal into a function call.
                  
                  rightVal = new PSFunctionCall(function,
                     funcParams, null, null);
               }
            }
            else
            {
               rightVal = new PSNumericLiteral(
                  new Integer(data), df);
            }
         }
         else if (dataType.equals(PSField.DT_DATETIME)
            || dataType.equals(PSField.DT_DATE)
            || dataType.equals(PSField.DT_TIME))
         {
            if (op.equals(PSConditional.OPTYPE_IN) ||
               op.equals(PSConditional.OPTYPE_NOTIN))
            {
               PSLiteralSet ls = new PSLiteralSet(PSDateLiteral.class);
               List list = PSStringOperation.getSplittedList(data, ',');
               Iterator iter = list.iterator();
               while (iter.hasNext())
               {
                  StringBuffer formatBuf = new StringBuffer();
                  Date date = PSDataTypeConverter.parseStringToDate(
                     (String)iter.next(), formatBuf);
                  ls.add(new PSDateLiteral(date, new SimpleDateFormat(
                     formatBuf.toString())));
               }
               rightVal = ls;
            }
            else
            {
               StringBuffer formatBuf = new StringBuffer();
               Date date =
                  PSDataTypeConverter.parseStringToDate(data, formatBuf);

               // handle the "on" date case special, since an "=" of a
               // specific date does not yield the proper results, it does
               // not include the date specified, to work around this we
               // change the "date = 'somedate'" to the following:
               // date > 'somedate at 12:00am' and date < 'somedate at 11:59pm'
               // see bug Rx-03-11-0027 for more information
               //
               // only if we are not searching with a time do we need to 
               // do the following workaround
               if (op.equals(PSConditional.OPTYPE_EQUALS)
                  && (formatBuf.toString().toLowerCase().indexOf("h") == -1))
               {
                  // time not found, we need to add the time
                  SimpleDateFormat dateFormat =
                     new SimpleDateFormat(
                        formatBuf.append(" hh:mm:ss").toString());
                  date = dateFormat.parse(data + " 00:00:00");

                  IPSReplacementValue tmpRightVal =
                     new PSDateLiteral(
                        date,
                        new SimpleDateFormat(formatBuf.toString()));

                  // add the beginning value which is midnight on the specified day
                  conditionals.add(
                     new PSConditional(
                        leftVal,
                        PSConditional.OPTYPE_GREATERTHANOREQUALS,
                        tmpRightVal));

                  // change the operator from equals to <= to include the whole day
                  op = PSConditional.OPTYPE_LESSTHANOREQUALS;

                  // set the right value to now point to almost midnight of
                  // the specified day, this will be used below for the condition
                  // 1 second short of midnight
                  date = dateFormat.parse(data + " 23:59:59");
               }
               rightVal = new PSDateLiteral(date, new SimpleDateFormat(
                  formatBuf.toString()));
            }
         }
         else
         {
            IPSReplacementValue[] vals =
               handleDatabaseCase(leftVal, data, useDatabaseCase);
            leftVal = vals[0];
            rightVal = vals[1];
            
            if (op.equals(PSConditional.OPTYPE_IN) ||
               op.equals(PSConditional.OPTYPE_NOTIN))
            {
               PSLiteralSet ls = new PSLiteralSet(PSTextLiteral.class);
               List list = PSStringOperation.getSplittedList(
                  rightVal.getValueText(), ',');
               Iterator iter = list.iterator();
               while (iter.hasNext())
               {
                  ls.add(new PSTextLiteral((String)iter.next()));
               }
               rightVal = ls;
            }            
         }
      }
      catch (Exception e)
      {
         // for now all errors just default to text literal
         IPSReplacementValue[] vals =
            handleDatabaseCase(leftVal, data, useDatabaseCase);
         leftVal = vals[0];
         rightVal = vals[1];
      }
      String con = convertConnector(searchField);

      conditionals.add(new PSConditional(leftVal, op, rightVal, con));
   }
   
   /**
    * Creates the right val of the condition and modifies the left val based on
    * the case-sensitivity of the database.
    * <p>
    * The first element of the returned array is the left val of the
    * conditional. If the database is not case-sensitive, left val is returned
    * without any changes, otherwise it is modified to be a
    * <code>PSFunctionCall</code> object (constructed using the the
    * database function "UPPER" and the original left val specified as
    * parameter to this method).
    * <p>
    * The second element of the returned array is the right val of the
    * conditional. It is a <code>PSTextLiteral</code> object constructed using
    * the string <code>data</code>. If the database is case-sensitive,
    * <code>data</code> is converted to uppercase before creating right val.
    *
    * @param leftVal the left value of the conditional,
    * assumed not <code>null</code>
    *
    * @param data the value being conditioned against, may be <code>null</code>
    * or empty
    *
    * @param useDatabaseCase if <code>false</code> and database is
    * case-sensitive, then the database function "UPPER" is applied to the
    * left value of the conditional if it is an instance of
    * <code>com.percussion.design.objectstore.PSBackEndColumn</code> and
    * right val is created using <code>data</code> converted to uppercase.
    *
    * @return array containing two elements, modified left val is the first
    * element and right val is the second element of the array. Never
    * <code>null</code> and always contains two elements.
    *
    * @throws IllegalArgumentException if the database function manager does
    * not return a valid (non-<code>null</code>) definition of the database
    * function "UPPER" for the database driver
    */
   private IPSReplacementValue[] handleDatabaseCase(IPSReplacementValue leftVal,
      String data, boolean useDatabaseCase)
   {
      boolean convertToUppercase = (ms_isDBCaseSensitive && (!useDatabaseCase));
      if (convertToUppercase)
      {
         if (leftVal instanceof PSBackEndColumn)
         {
            PSBackEndColumn backEndMap = (PSBackEndColumn)leftVal;

            // modify left val
            PSFunctionParamValue paramVal = new PSFunctionParamValue(backEndMap);
            leftVal = new PSFunctionCall(
               PSDatabaseFunctionManager.DB_FUNCTION_UPPER,
               new PSFunctionParamValue[] {paramVal},
               null, null);

            // convert data to uppercase
            data = data.toUpperCase();
         }
      }
      IPSReplacementValue rightVal = new PSTextLiteral(data);
      return new IPSReplacementValue[]{leftVal, rightVal};
   }


   /**
    * Utility to convert from a search field operator to our internal
    * operator names. 
    *
    * @param searchField The search field whose operator is to be converted, 
    *                    assumed not <code>null</code>.
    * 
    * @return            the operator converted to an internal PSConditional
    *                    constant value, see PSConditional for values, if the
    *                    supplied operator was not found the default operator
    *                    "=" is returned
    */
   private String convertOperator(PSWSSearchField searchField)
   {
      String strOp = PSConditional.OPTYPE_EQUALS;
      
      int operator = searchField.getOperatorEnum().getOrdinal();
      switch (operator)
      {
         case PSWSSearchField.OP_ATTR_EQUAL :
            strOp = PSConditional.OPTYPE_EQUALS;
            break;

         case PSWSSearchField.OP_ATTR_NOTEQUAL :
             strOp = PSConditional.OPTYPE_NOTEQUALS;
             break;

         case PSWSSearchField.OP_ATTR_LESSTHAN :
             strOp = PSConditional.OPTYPE_LESSTHAN;
             break;

         case PSWSSearchField.OP_ATTR_LESSTHANEQUAL :
             strOp = PSConditional.OPTYPE_LESSTHANOREQUALS;
             break;

         case PSWSSearchField.OP_ATTR_GREATERTHAN :
             strOp = PSConditional.OPTYPE_GREATERTHAN;
             break;
             
         case PSWSSearchField.OP_ATTR_GREATERTHANEQUAL :
             strOp = PSConditional.OPTYPE_GREATERTHANOREQUALS;
             break;

         case PSWSSearchField.OP_ATTR_ISNULL :
             strOp = PSConditional.OPTYPE_ISNULL;
             break;

         case PSWSSearchField.OP_ATTR_ISNOTNULL :
             strOp = PSConditional.OPTYPE_ISNOTNULL;
             break;

         case PSWSSearchField.OP_ATTR_IN :
             strOp = PSConditional.OPTYPE_IN;
             break;

         case PSWSSearchField.OP_ATTR_NOTIN :
             strOp = PSConditional.OPTYPE_NOTIN;
             break;

         case PSWSSearchField.OP_ATTR_LIKE :
             strOp = PSConditional.OPTYPE_LIKE;
             break;

         case PSWSSearchField.OP_ATTR_NOTLIKE :
             strOp = PSConditional.OPTYPE_NOTLIKE;
             break;
      }

      return strOp;
   }

   /**
    * Utility to convert from a search field connector to our internal
    * connector names. 
    *
    * @param searchField The search field whose operator is to be converted, 
    *                    assumed not <code>null</code>.
    *
    * @return            the string converted to an internal PSConditional
    *                    constant value, see PSConditional for values, if
    *                    supplied connector is not found, returns the default
    *                    "AND" connector
    */
   private String convertConnector(PSWSSearchField searchField)
   {      
      int conn = searchField.getConnectorEnum().getOrdinal();
      String strConn = PSConditional.OPBOOL_AND;
      switch (conn)
      {
         case PSWSSearchField.CONN_ATTR_AND :
            strConn = PSConditional.OPBOOL_AND; 
            break;

         case PSWSSearchField.CONN_ATTR_OR :
            strConn = PSConditional.OPBOOL_OR; 
            break;
      }
      
      return strConn;
   }

   /**
    * Initialization routine. If this is the first instance of this object then
    * obtains the case sensitivity of the database, otherwise it simply returns.
    *
    * @throws PSServerException if any error occurs getting the response
    * document when making the internal request to the resources
    * <code>DBLOOKUP_UPPER_URL</code> or <code>DBLOOKUP_LOWER_URL</code>
    */
   private void init() throws PSServerException
   {
      if (!ms_isInited)
      {
         ms_isInited = true;
         if (makeDBLookupRequest(DBLOOKUP_UPPER_URL))
         {
            if (makeDBLookupRequest(DBLOOKUP_LOWER_URL))
               ms_isDBCaseSensitive = false;
         }

         // initialize ms_contentStatusContentIdColumn for "oracle:thin" driver
         String driver;
         try
         {
            driver = PSConnectionHelper.getConnectionDetail(null).getDriver();
         }
         catch (NamingException e)
         {
            throw new PSServerException(IPSServerErrors.RAW_DUMP, 
               e.getLocalizedMessage());
         }
         catch (SQLException e)
         {
            throw new PSServerException(IPSServerErrors.RAW_DUMP, 
               e.getLocalizedMessage());
         }
         if (driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY))
         {
            PSBackEndTable table =
               new PSBackEndTable(IPSConstants.CONTENT_STATUS_TABLE);
            ms_contentStatusContentIdColumn = new PSBackEndColumn(
               table, IPSConstants.ITEM_PKEY_CONTENTID);
         }
         
         ms_statesTable = new PSBackEndTable("STATES");
         ms_statesTable.setTable("STATES");
         ms_statesTable.setDataSource("");
         ms_workflowAppsTable = new PSBackEndTable("WORKFLOWAPPS");
         ms_workflowAppsTable.setTable("WORKFLOWAPPS");
         ms_workflowAppsTable.setDataSource("");
         PSBackEndColumn stCol = new PSBackEndColumn(ms_statesTable, 
            "STATENAME");
         ms_stateNameField = new PSField(IPSHtmlParameters.SYS_STATE_NAME, 
            stCol);
         ms_stateNameField.setDataType(PSField.DT_TEXT);
      }
   }
   
   
   /**
    * Makes a request to the specified URL and parses the response document.
    *
    * @param resourceName the url of the resource relative to the Rhythmyx root,
    * assumed not <code>null</code> and non-empty
    *
    * @return <code>true</code> if the response document has an integer value
    * for <code>ATTR_CASE_SENSITVE</code> attribute, ortherwise false.
    *
    * @throws PSServerException if any error occurs getting the response
    * document when making the internal request to the specified resource
    */
   private boolean makeDBLookupRequest(String resourceName)
      throws PSServerException
   {
      boolean ret = false;
      try
      {
         PSRequest req = PSRequest.getContextForRequest();
         PSInternalRequest ir =
            PSServer.getInternalRequest(resourceName, req, null, true);
         if(ir == null)
         {
            throw new RuntimeException(
               "No handler found for resource : " + resourceName);
         }
         Document respDoc = ir.getResultDoc();
         if ((respDoc != null) && (respDoc.getDocumentElement() != null))
         {
            String strCaseSensitive =
               respDoc.getDocumentElement().getAttribute(ATTR_CASE_SENSITVE);
            if ((strCaseSensitive != null) && (strCaseSensitive.trim().length() > 0))
            {
               try
               {
                  Integer.parseInt(strCaseSensitive);
                  ret = true;
               }
               catch(NumberFormatException ex)
               {
               }
            }
         }
      }
      catch (PSInternalRequestCallException ex)
      {
         throw new PSServerException(ex.getErrorCode(), ex.getErrorArguments());
      }
      return ret;
   }

   /**
    * A static collection of field references as <code>String</code> which
    * will be added to all search results. Never <code>null</code> or empty.
    * If this list is updated, also update the documentation in 
    * <code>sys_SearchParameters.xsd</code>.
    */
   private static Collection<String> ms_staticSearchResults = null;
   
   static
   {
      ms_staticSearchResults = new ArrayList<String>();
      ms_staticSearchResults.add("sys_contentid");
      ms_staticSearchResults.add("sys_contenttypeid");
      ms_staticSearchResults.add("sys_title");
      ms_staticSearchResults.add("sys_contentcheckoutusername");
      ms_staticSearchResults.add("sys_workflowid");
      ms_staticSearchResults.add("sys_contentstateid");
      ms_staticSearchResults.add("sys_publishabletype");
      ms_staticSearchResults.add("sys_assignmenttypeid");
      ms_staticSearchResults.add("sys_checkoutstatus");
   }

   /**
    * Stores the case sensitivity of the database. If <code>true</code> then
    * the database is case-sensitive, otherwise not. Initialized to
    * <code>true</code>. Set in the <code>init()</code> method when the first
    * object of this class is created. Never modified after that.
    */
   private static boolean ms_isDBCaseSensitive = true;

   /**
    * Indicates if any object of this class has been created. <code>false</code>
    * until the first object of this class is created, <code>true</code>
    * otherwise. Modified in the <code>init()</code> method when the first
    * object is created, never modified after that.
    */
   private static boolean ms_isInited = false;

   /**
    * URL for the resource which performs a query similar to:
    * SELECT NEXTNR FROM NEXTNUMBER WHERE UPPER(KEYNAME) = UPPER('PSX_PROPERTIES')
    * This URL should return single row irrespective of the case-sensitivity
    * of the database.
    */
   private static final String DBLOOKUP_UPPER_URL =
      "sys_psxCms/DBLookupUpper.xml";

   /**
    * URL for the resource which performs a query similar to:
    * SELECT NEXTNR FROM NEXTNUMBER WHERE KEYNAME = 'psx_properties'
    * This URL will return a row only if the database is case-insensitive.
    */
   private static final String DBLOOKUP_LOWER_URL =
      "sys_psxCms/DBLookupLower.xml";

   /**
    * parameter on the request to define the maximum results for the search
    */
   public static final String MAXIMUM_RESULTS = "maximumresults";
   
   /**
    * Key to use when setting or retrieving the <code>PSWSSearchRequest</code>
    * request private object.
    */
   public static final String SEARCH_REQUEST_OBJECT = "sys_wsSearchRequest";

   /**
    * replacement value for the backend column CONTENTSTATUS.CONTENTID,
    * initialized to <code>null</code>, set to non-<code>null</code> value
    * in <code>init()</code> method for "oracle:thin" driver only, never
    * modified after that
    */
   private static PSBackEndColumn ms_contentStatusContentIdColumn = null;

   
   // XML element and attribute constants from sys_SearchParameters.xsd
   
   private static final String EL_SEARCH_RESPONSE      = "SearchResponse";
   private static final String EL_RESULT               = "Result";
   private static final String EL_RESULT_FIELD         = "ResultField";
   private static final String  ATTR_NAME              = "name";


   // database case-sensitivity lookup response attributes
   private static final String  ATTR_CASE_SENSITVE = "caseSensitive";

   /**
    * Constant for the name of the database function "IN-NUMBER-ARRAY"
    */
   private static final String FUNC_NAME_IN_NUMBER_ARRAY = "IN-NUMBER-ARRAY";

   /**
    * Constant for the name of the database function "IN-NUMBER"
    */
   private static final String FUNC_NAME_IN_NUMBER = "IN-NUMBER";
   
   /**
    * States table used to perform state name search, never <code>null</code>
    * after {@link #init()}
    */
   private static PSBackEndTable ms_statesTable;
   
   /**
    * Workflow table used to perform state name search, never <code>null</code>
    * after {@link #init()}
    */
   private static PSBackEndTable ms_workflowAppsTable;
   
   /**
    * Statename field used to perform state name search, never <code>null</code>
    * after {@link #init()}
    */
   private static PSField ms_stateNameField;
   
   /**
    * Constant for the column name identifying the workflow id.
    */
   private static final String WORKFLOWAPPID = "WORKFLOWAPPID";
   
   /**
    * Constant for the column name identifying the state id in the content 
    * status table
    */
   private static final String CONTENTSTATEID = "CONTENTSTATEID";
   
   /**
    * Constant for the column name identifying the state id in the STATES table
    */
   private static final String STATEID = "STATEID";
}




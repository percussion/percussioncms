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
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMetaDataCache;
import com.percussion.data.PSMimeContentResult;
import com.percussion.data.PSRowDataBuffer;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.design.objectstore.PSXmlField;
import com.percussion.error.PSBackEndQueryProcessingError;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.util.PSCollection;
import com.percussion.util.PSUniqueObjectGenerator;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

/**
 * This class encapsulates behaviour to handle the binary commands.
 */
public class PSBinaryCommandHandler extends PSCommandHandler implements
   IPSInternalCommandRequestHandlerEx, IPSInternalResultHandler
{
   /**
    * Walks the display mapper and creates a binary resource for all binary
    * or forcedBinary fields found. Is recursing all mappers.
    *
    * @param ah The handler for the application that contains this editor
    *    resource.
    * @param ceh The parent handler of this handler.
    * @param ce The definition of the editor.
    * @param app Any resources that are created dynamically will be added to
    *    this application. Never <code>null</code>.
    * @throws PSNotFoundException If an exit cannot be found.
    * @throws PSExtensionException If an exit init failed.
    * @throws PSSystemValidationException if anything is wrong creating the binary
    *    application
    */
   public PSBinaryCommandHandler(PSApplicationHandler ah,
      PSContentEditorHandler ceh, PSContentEditor ce, PSApplication app)
         throws PSNotFoundException, PSExtensionException, PSSystemValidationException
   {
      super(ah, ceh, ce, app);
      if (app == null)
         throw new IllegalArgumentException("app cannot be null");

      m_appName = app.getName();

      // get the sysdef from ceh and override the exit defs in the base
      PSContentEditorSystemDef sysDef = ceh.getSystemDef();
      PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();

      // get dataset and cmd pre and post exits and prepare them
      PSExtensionCallSet preExits = new PSExtensionCallSet();
      preExits.addAll(new PSCollection(
            sysDef.getInputDataExits(COMMAND_NAME)));
      PSExtensionCallSet pipePreExits = pipe.getInputDataExtensions();
      if (pipePreExits != null)
         preExits.addAll(pipePreExits);
      setPreProcExits(preExits);

      PSExtensionCallSet postExits = new PSExtensionCallSet();
      postExits.addAll(new PSCollection(
            sysDef.getResultDataExits(COMMAND_NAME)));
      PSExtensionCallSet pipePostExits = pipe.getResultDataExtensions();
      if (pipePostExits != null)
         postExits.addAll(pipePostExits);
      setResultDocExits(postExits);


      PSDisplayMapper dispMapper =
            pipe.getMapper().getUIDefinition().getDisplayMapper();

      createBinaryResources(app, ceh, ce, dispMapper);
   }

   // see IPSRequestHandler interface for description
   public void processRequest(PSRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req cannot be null");

      PSExecutionData resultData = null;
      IPSInternalResultHandler rh = null;
      try
      {
         rh = getInternalResultHandler(req);
         if (rh != null)
         {
            resultData = rh.makeInternalRequest(req);

            // run post exits before data is returned to the requestor
            runPostProcessingExtensions(resultData, null);

            //Get the mime content and set on response of the request.
            PSMimeContentResult mimeContent =
               rh.getMimeContent(resultData, false);
            PSResponse resp = req.getResponse();
            if(mimeContent == null)
               resp.setStatus(IPSHttpErrors.HTTP_NO_CONTENT);
            else
            {
               resp.setContent(mimeContent.getContent(),
                  mimeContent.getContentLength(), mimeContent.getMimeType() );
            }
         }
      }
      catch (Throwable t)
      {
         // catch anything that comes our way
         String source = COMMAND_NAME;

         String sessId = "";
         PSUserSession sess = req.getUserSession();
         if (sess != null)
            sessId = sess.getId();

         int errorCode;
         Object[] errorArgs;

         PSException e = null;

         if (t instanceof PSException)
         {
            e = (PSException) t;
            errorCode = e.getErrorCode();
            errorArgs = e.getErrorArguments();
         }
         else
         {
            errorCode = IPSServerErrors.RAW_DUMP;
            errorArgs = new Object[] { getExceptionText(t) };
         }

         PSBackEndQueryProcessingError err =
            new PSBackEndQueryProcessingError(
               m_appHandler.getId(), sessId, errorCode, errorArgs, source);
         m_appHandler.reportError(req, err);
      }
      finally
      {
         if (resultData != null)
            resultData.release();
      }
   }

   // see IPSRequestHandler interface for description
   public void shutdown()
   {
   }

   // see IPSInternalRequestHandler interface for description
   public PSExecutionData makeInternalRequest(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      PSExecutionData data = new PSExecutionData(m_appHandler, this, request);
      try
      {
         IPSInternalResultHandler rh = getInternalResultHandler(request);
         if (rh != null)
         {
            data = rh.makeInternalRequest(request);

            // run post exits before data is returned to the requestor
            runPostProcessingExtensions(data, null);
         }
      }
      catch (PSException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments());
      }

      return data;
   }

   /**
    * Get the internal result handler from the provided request.
    *
    * @param request the request to get the internal result handler from,
    *    assumed not <code>null</code>.
    * @return the internal result handler if found, <code>null</code> otherwise.
    */
   private IPSInternalResultHandler getInternalResultHandler(PSRequest request)
   {
      IPSInternalResultHandler rh = null;
      String submitName = request.getParameter(
         IPSConstants.SUBMITNAME_PARAM_NAME);
      if (submitName != null && submitName.trim().length() > 0)
      {
         String resourceName = (String) m_resourceMap.get(
            submitName.toLowerCase());
         if (resourceName != null)
         {
            String reqName = createRequestName(m_appName, resourceName);
            rh = (IPSInternalResultHandler) PSServer.getInternalRequestHandler(
               reqName);
         }
      }

      return rh;
   }

   /**
    * see IPSInternalCommandRequestHandlerEx interface for description
    *
    * @return an empty document if the binary resource requested was found,
    *    <code>null</code> otherwise.
    */
   public Document makeInternalRequestEx(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      PSExecutionData data = new PSExecutionData(m_appHandler, this, request);
      try
      {
         IPSInternalResultHandler rh = getInternalResultHandler(request);
         if (rh != null)
         {
            data = rh.makeInternalRequest(request);
            ResultSet rs = data.getNextResultSet();
            if (rs != null)
            {
               PSRowDataBuffer buf = new PSRowDataBuffer(rs);
               buf.readRow();
               Object[] row = buf.getCurrentRow();
               if (row != null && row[0] != null)
                  return PSXmlDocumentBuilder.createXmlDocument();
            }
         }
      }
      catch (SQLException e)
      {
         throw new PSInternalRequestCallException(0, e.getLocalizedMessage());
      }
      catch (PSException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments());
      }
      finally
      {
         if (data != null)
            data.release();
      }

      return null;
   }

   /**
    * This method is recursive, as it scans the mapper, if it finds a
    * PSFieldSet for a complex child, this method is called to process that
    * child. Otherwise the field is checked if binary or forcedBinary and a
    * binary resource will be created if so.
    *
    * @param app The app in which the datasets will be built. Assumed not
    *    <code>null</code>.
    * @param ceh The parent handler of this handler, assumed not
    *    <code>null</code> .
    * @param ce The definition of the entire editor. It is used read only.
    *    Assumed not <code>null</code>.
    * @param dispMapper The mapper with the editor for which this method will
    *    initialize the datasets and other information, assumed not
    *    <code>null</code>.
    * @throws PSSystemValidationException If anything used by this method is missing
    *    or misconfigured.
    */
   private void createBinaryResources(PSApplication app,
      PSContentEditorHandler ceh, PSContentEditor ce,
      PSDisplayMapper dispMapper)
      throws PSSystemValidationException
   {
      try
      {
         PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
         PSFieldSet fieldSet =
               pipe.getMapper().getFieldSet(dispMapper.getFieldSetRef());

         Iterator mappings = dispMapper.iterator();
         while (mappings.hasNext())
         {
            PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
            String fieldName = mapping.getFieldRef();
            Object o = fieldSet.get(fieldName);
            if ( null == o )
               o = fieldSet.getChildField( fieldName,
                     fieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );

            if (o instanceof PSFieldSet)
            {
               PSFieldSet fs = (PSFieldSet) o;
               PSDisplayMapper childMapper = mapping.getDisplayMapper();
               createBinaryResources(app, ceh, ce, childMapper);
            }
            else
            {
               PSField field = (PSField) o;
               HashMap keys = new HashMap();
               if ((fieldSet.getType() == PSFieldSet.TYPE_PARENT))
               {
                  keys.put(IPSConstants.ITEM_PKEY_CONTENTID,
                     ceh.getParamName(ceh.CONTENT_ID_PARAM_NAME));
                  keys.put(IPSConstants.ITEM_PKEY_REVISIONID,
                     ceh.getParamName(ceh.REVISION_ID_PARAM_NAME));
               }
               else
               {
                  keys.put(IPSConstants.CHILD_ITEM_PKEY,
                     ceh.getParamName(ceh.CHILD_ROW_ID_PARAM_NAME));
               }
               if (field.getLocator() instanceof PSBackEndColumn)
               {
                  PSBackEndColumn bec = (PSBackEndColumn) field.getLocator();
                  PSBackEndTable backend = bec.getTable();
                  PSTableSet tableSet = getTableSet(
                     backend.getAlias(), pipe.getLocator());
                  if (isBinaryField(field, tableSet))
                  {
                     // fix for bug #RX-01-08-0041
                     // assume the mimetype <PSXField> is named by appending
                     // "_type" to the binary <PSXField> name
                     // (which is how the PSFileInfo exit requires it)
                     // if found, the mimetype will come from the database
                     // column mapped by that field
                     String mimeTypeFieldName = field.getSubmitName() + "_type";
                     PSField mimeField =
                        fieldSet.findFieldByName(mimeTypeFieldName);
                     addBinaryResource(app, tableSet, field, mimeField,
                                       keys.entrySet());
                  }
               }
            }
         }
      }
      catch (SQLException e)
      {
         throw new PSSystemValidationException(e.getErrorCode(),
            e.getLocalizedMessage());
      }
   }

   /**
    * Test if the provided field is of type binary or if it is forcedBinary.
    *
    * @param field the field to test, may be <code>null</code>.
    * @param tableSet the field's table set, may be <code>null</code>.
    * @return <code>true</code> if the provided field is binary or has the
    *    attribute forceBinary set, <code>false</code> otherwise or if the
    *    supplied field or tableLocator is <code>null</code>.
    * @throws SQLException if any database operation fails.
    */
   private boolean isBinaryField(PSField field, PSTableSet tableSet)
      throws SQLException
   {
      if (field != null)
      {
         if (field.isForceBinary())
            return true;

         if (tableSet != null)
         {
            PSBackEndColumn column = (PSBackEndColumn) field.getLocator();
            return PSMetaDataCache.isBinaryBackendColumn(tableSet, column);
         }
      }

      return false;
   }

   /**
    * Get the table set from the provided container for the supplied table
    * alias.
    *
    * @param tableAlias the table alias of the table set we are looking for,
    *    assumed not <code>null</code>.
    * @param container the container locator we are searching in, assumed not
    *    <code>null</code>.
    * @return the table set found or <code>null</code>.
    */
   private PSTableSet getTableSet(
      String tableAlias, PSContainerLocator container)
   {
      boolean found = false;
      PSTableSet tableSet = null;
      Iterator tableSets = container.getTableSets();
      while (tableSets.hasNext() && !found)
      {
         tableSet = (PSTableSet) tableSets.next();
         Iterator tableRefs = tableSet.getTableRefs();
         while (tableRefs.hasNext() && !found)
         {
            PSTableRef tableRef = (PSTableRef) tableRefs.next();
            if (tableAlias.equals(tableRef.getAlias()))
               found = true;
         }
      }

      if (found)
         return tableSet;

      return null;
   }

   /**
    * This method will add a new binary resource to the provided application.
    *
    * @param tableLocator the table locator to use, assumed not
    *    <code>null</code>.
    * @param app the application to add the binary resource to, assumed not
    *    <code>null</code>.
    * @param field a binary field to add the resource for, assumed not
    *    <code>null</code>.
    * @param mimeField if not <code>null</code>, the field that maps to the
    *    database column that specifies the mimetype of the binary field.
    *    This column must be in the same table as the binary field column.
    * @param keys a set of keys used for the resource selector, assumed not
    *    <code>null</code>. Each element in the set must be a Map.Entry,
    *    whose key is the backend column name and whose value is the HTML
    *    parameter name from which the key will be obtained.
    * @throws PSSystemValidationException if a duplicate submit name was found.
    */
   private void addBinaryResource(PSApplication app, PSTableSet tableSet,
      PSField field, PSField mimeField, Set keys)
      throws PSSystemValidationException
   {
      try
      {
         PSBackEndColumn bec = (PSBackEndColumn) field.getLocator();
         PSBackEndColumn mime_bec = null;
         if (null != mimeField)
            mime_bec = (PSBackEndColumn) mimeField.getLocator();

         PSDataSet dataset = createBinaryDataset(
            tableSet, bec, mime_bec, keys, field.getSubmitName()
         );
         if (dataset != null)
         {
            PSCollection datasets = app.getDataSets();
            if (datasets == null)
               datasets = new PSCollection(dataset.getClass());
            datasets.add(dataset);
            app.setDataSets(datasets);
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Creates a new binary dataset for the provided parameters.
    *
    * @param tableSet the table set to create the dataset for, assumed
    *    not <code>null</code>.
    * @param column the backend column to create the dataset for, assumed not
    *    <code>null</code>.
    * @param mimeColumn the backend column to that holds the mime type,
    *    allowed to be <code>null</code>, in which case the mime type is
    *    determined by looking up the request extension in MimeProperties hash.
    * @param keys a set of keys used for the resource selector, assumed not
    *    <code>null</code>. Each element in the set must be a Map.Entry,
    *    whose key is the backend column name and whose value is the HTML
    *    parameter name from which the key will be obtained.
    * @param submitName the fields submit name for the binary resource this
    *    will create, not <code>null</code> or empty, must be unique accross
    *    all binary resources created.
    * @return the new binary dataset or <code>null</code> if anything went
    *    wrong.
    * @throws PSSystemValidationException if a duplicate submit name was found.
    */
   private PSDataSet createBinaryDataset(PSTableSet tableSet,
                                         PSBackEndColumn column,
                                         PSBackEndColumn mimeColumn,
                                         Set keys, String submitName)
      throws PSSystemValidationException
   {
      try
      {
         // create the backend tank
         PSTableLocator tableLoc = tableSet.getTableLocation();
         PSBackEndTable table = column.getTable();
         if (tableLoc.getCredentials() == null)
            throw new IllegalArgumentException("ce missing credentials: " + tableLoc.getAlias());
         table.setInfoFromLocator(tableLoc);
         if (table.getTable() == null || table.getTable().trim().length() == 0)
            table.setTable(getTableNameForAlias(table.getAlias(), tableSet));
         PSBackEndDataTank backend = new PSBackEndDataTank();
         PSCollection tables = new PSCollection(table.getClass());
         tables.add(table);
         backend.setTables(tables);

         // create data mapper
         PSDataMapper mapper = new PSDataMapper();
         mapper.add(new PSDataMapping(new PSXmlField("IMAGE"), column));

         // create data selector
         PSDataSelector selector = new PSDataSelector();
         PSCollection clauses = null;
         // used by sorted column code below
         PSBackEndColumn name = null;
         Iterator it = keys.iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            name = new PSBackEndColumn(table, (String) entry.getKey());
            PSHtmlParameter value =
                  new PSHtmlParameter((String) entry.getValue());
            PSWhereClause clause = new PSWhereClause(name,
                  PSWhereClause.OPTYPE_EQUALS, value, false);
            if (clauses == null)
               clauses = new PSCollection(clause.getClass());
            clauses.add(clause);
         }
         selector.setWhereClauses(clauses);

         // create the query pipe
         PSQueryPipe pipe = new PSQueryPipe(
            PSUniqueObjectGenerator.makeUniqueName("QueryPipe"));
         pipe.setBackEndDataTank(backend);
         pipe.setDataMapper(mapper);
         pipe.setDataSelector(selector);

         // create requestor, use default mime types
         HashMap mimeProps = new HashMap(3);
         mimeProps.put("jpg", new PSTextLiteral("image/jpeg"));
         mimeProps.put("jpeg", new PSTextLiteral("image/jpeg"));
         mimeProps.put("gif", new PSTextLiteral("image/gif"));

         // (RX-01-08-0041): was planning to expand defaults since CE provides
         // no way to extend, but since the CE handler must respond first
         // none of these extensions would be recognized

//         mimeProps.put("tif", new PSTextLiteral("image/tiff"));
//         mimeProps.put("tiff", new PSTextLiteral("image/tiff"));
//         mimeProps.put("psd", new PSTextLiteral("image/x-photoshop"));
//         mimeProps.put("qt", new PSTextLiteral("video/quicktime"));
//         mimeProps.put("mov", new PSTextLiteral("video/quicktime"));
//         mimeProps.put("swf", new PSTextLiteral("application/x-shockwave-flash"));
//         mimeProps.put("mpt", new PSTextLiteral("application/vnd.ms-project"));
//         mimeProps.put("mpp", new PSTextLiteral("application/vnd.ms-project"));
//         mimeProps.put("ps", new PSTextLiteral("application/postscript"));
//         mimeProps.put("eps", new PSTextLiteral("application/postscript"));
//         mimeProps.put("png", new PSTextLiteral("image/png"));
//         mimeProps.put("hqx", new PSTextLiteral("application/mac-binhex40"));
//         mimeProps.put("pdf", new PSTextLiteral("application/pdf"));
//         mimeProps.put("hqx", new PSTextLiteral("application/mac-binhex40"));
//         mimeProps.put("rtf", new PSTextLiteral("application/rtf"));
//         mimeProps.put("ppt", new PSTextLiteral("application/vnd.ms-powerpoint"));
//         mimeProps.put("zip", new PSTextLiteral("application/x-zip-compressed"));
//         mimeProps.put("xls", new PSTextLiteral("application/vnd.ms-excel"));
//         mimeProps.put("bmp", new PSTextLiteral("image/x-ms-bmp"));

         PSRequestor requestor = new PSRequestor();
         requestor.setRequestPage(column.getColumn());
         requestor.setMimeProperties(mimeProps);
         requestor.setDirectDataStream(true);

         // (RX-01-08-0041): PSQueryOptimizer.createStatements will add
         // the mimeColumn to the mapper automatically
         if (mimeColumn != null)
                  requestor.setOutputMimeType(mimeColumn);

         // create dataset
         PSDataSet dataset = new PSDataSet(
            PSUniqueObjectGenerator.makeUniqueName(BINARY_QUERY));
         dataset.setPipe(pipe);
         dataset.setRequestor(requestor);

         /*
          * Check uniqueness of submit name and add the dataset name to our
          * resource map.
          */
         if (m_resourceMap.containsKey(submitName))
            throw new PSSystemValidationException(
               IPSServerErrors.CE_DUPLICATE_SUBMIT_NAME, submitName);
         else
            m_resourceMap.put(submitName.toLowerCase(), dataset.getName());

         return dataset;
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * This method tries to get the table name in the provided table set for
    * the supplied table alias.
    *
    * @param alias the table alias we need the table name for, assumed not
    *    <code>null</code>.
    * @param tableSet the table set in which to search for the table name.
    *    Assumed not <code>null</code>.
    * @return the table name or the alias if no matching table name was found.
    */
   private String getTableNameForAlias(String alias, PSTableSet tableSet)
   {
      Iterator refs = tableSet.getTableRefs();
      while (refs.hasNext())
      {
         PSTableRef ref = (PSTableRef) refs.next();
         if (ref.getAlias().equals(alias))
            return ref.getName();
      }

      return alias;
   }

   // See IPSInternalResultHandler interface for description
   public ResultSet getResultSet(PSExecutionData data)
      throws PSInternalRequestCallException
   {
      ResultSet rs = null;
      IPSInternalResultHandler rh = getInternalResultHandler(data.getRequest());
      if (rh != null)
         rs = rh.getResultSet(data);

      return rs;
   }

   // See IPSInternalResultHandler interface for description
   public Document getResultDoc(PSExecutionData data)
      throws PSInternalRequestCallException
   {
      Document doc = null;
      IPSInternalResultHandler rh = getInternalResultHandler(data.getRequest());
      if (rh != null)
         doc = rh.getResultDoc(data);

      return doc;
   }

   // See IPSInternalResultHandler interface for description
   public PSMimeContentResult getMimeContent(PSExecutionData data,
      boolean setResponse) throws PSInternalRequestCallException
   {
      PSMimeContentResult mcr = null;
      IPSInternalResultHandler rh = getInternalResultHandler(data.getRequest());
      if (rh != null)
         mcr = rh.getMimeContent(data, setResponse);

      return mcr;
   }
   
   public boolean isBinary(PSRequest req)
   {
      return true;
   }

   /**
    * The internal name of this handler. When handler names are used in
    * config files, this is the name that must be used.
    */
   public static final String COMMAND_NAME = "binary";

   /**
    * The dataset base name of all binary_query resources.
    */
   private static final String BINARY_QUERY = "binary_query";

   /**
    * The name of the dynamically created update application. Initialized
    * during construction, never <code>null</code> after that.
    */
   private String m_appName = null;

   /**
    * A map of resource (dataset) names created. The key is the field submit
    * name this resource is created for while the value is the resource name.
    */
   private HashMap m_resourceMap = new HashMap();
}

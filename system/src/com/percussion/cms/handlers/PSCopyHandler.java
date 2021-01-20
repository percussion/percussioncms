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

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.PSChoiceBuilder;
import com.percussion.cms.PSInlineLinkField;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSCloneFactory;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSContentItemStatusExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMetaDataCache;
import com.percussion.data.PSMimeContentResult;
import com.percussion.data.PSXmlFieldExtractor;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSContentItemStatus;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.util.PSUniqueObjectGenerator;
import com.percussion.xml.PSDtdBuilder;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class handles the task of creating a full copy of a particular content
 * item revision.  All item data and child data is copied and inserted as a new
 * revision of the item.  This is done by separately querying each backend
 * table's data, building a result document that is a composite of all rows
 * returned, and then submitting that result document to an update resource
 * to insert all data as a new revision or item.
 */
class PSCopyHandler implements IPSCopyHandler
{
   /**
    * Convenience ctor that calls
    * {@link #PSCopyHandler(PSContentEditorHandler, PSContentEditor,
    *    PSApplication, PSCommandHandler, boolean) this(ceh, ce, app, cmd, false)}.
    */
   public PSCopyHandler(PSContentEditorHandler ceh, PSContentEditor ce,
      PSApplication app, PSCommandHandler cmd) throws PSValidationException
   {
      this(ceh, ce, app, cmd, false);
   }

   /**
    * Initializes the handler by walking the content editor's display mappers
    * and creates datasets required to query for the existing item and insert
    * the new copy of it, adding those datasets to the content editor's
    * application.
    *
    * @param ceh The content editor handler for the ContentEditor this handler
    *    will be copying.  May not be <code>null</code>.
    * @param ce The content editor this command handler will process modify
    *    commands for.  May not be <code>null</code>.
    * @param app The application created by the PSContentEditor for each
    *    command handler to add datasets to.  The app is started and stopped by the
    * ContentEditorHandler.  May not be <code>null</code>.
    * @param cmd The command handler that is creating the copy  May not be
    *    <code>null</code>.
    * @param skipRelationships set to <code>true</code> will not, set to
    *    <code>false</code> will recreate all related content relationships
    *    from the original.
    * @throws PSValidationException for any other errors.
    */
   public PSCopyHandler(PSContentEditorHandler ceh, PSContentEditor ce,
      PSApplication app, PSCommandHandler cmd, boolean skipRelationships)
         throws PSValidationException
   {
      if (ceh == null || ce == null || app == null || cmd == null)
         throw new IllegalArgumentException("one or more params was null");

      PSContentEditorPipe pipe = (PSContentEditorPipe)ce.getPipe();
      PSDisplayMapper mapper =
         pipe.getMapper().getUIDefinition().getDisplayMapper();

      m_ceHandler = ceh;
      m_ce = ce;
      m_app = app;
      m_cmdHandler = cmd;

      /* build a list of workflows allowed for this content type (so we don't
         have to walk the iterator a million times */
      if ( m_ceHandler.getCmsObject().isWorkflowable() &&
           null != ce.getWorkflowInfo())
      {
         Iterator iter = ce.getWorkflowInfo().getValues();
         m_workflowIds = new ArrayList();
         while ( iter.hasNext())
            m_workflowIds.add(((Integer)iter.next()).toString());
      }

      // build table maps
      try
      {
         m_beTables = new HashMap();
         m_tableSets = new HashMap();

         Iterator tableSets = pipe.getLocator().getTableSets();
         while (tableSets.hasNext())
         {
            PSTableSet tableSet = (PSTableSet)tableSets.next();
            PSTableLocator tableLocator = tableSet.getTableLocation();
            Iterator tableRefs = tableSet.getTableRefs();
            while (tableRefs.hasNext())
            {
               PSTableRef tableRef = (PSTableRef)tableRefs.next();
               PSBackEndTable table = new PSBackEndTable(
                     PSUniqueObjectGenerator.makeUniqueName( "table" ));
               table.setAlias(tableRef.getAlias());
               table.setInfoFromLocator(tableLocator);
               table.setTable(tableRef.getName());

               m_beTables.put(table.getAlias().toLowerCase(), table);
               m_tableSets.put(table.getAlias().toLowerCase(), tableSet);
            }
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_contentIdParamName = m_ceHandler.getParamName(
            PSContentEditorHandler.CONTENT_ID_PARAM_NAME);

      m_revisionIdParamName =  m_ceHandler.getParamName(
            PSContentEditorHandler.REVISION_ID_PARAM_NAME);

      // create all required datasets
      createDataSets(mapper, pipe, null, null, skipRelationships);
   }

   /** see IPSCopyHandler for a description */
   public void createCopy(PSLocator source, PSLocator target,
      PSExecutionData data, boolean checkin)
      throws PSAuthorizationException, PSInternalRequestCallException,
         PSAuthenticationFailedException, PSNotFoundException, SQLException
   {
      if (source == null || target == null)
        throw new IllegalArgumentException("locators cannot be null");

      int curRev = source.getRevision();
      if(curRev == -1)
      {
         Object revision = null;
         try
         {
            revision = m_currentRevisionExtractor.extract(data);
         }
         catch (PSDataExtractionException e)
         {
            // this should never happen
            revision = "1";
         }
         curRev = Integer.parseInt(revision.toString());
      }
      createCopy(source.getId(), curRev, target.getId(),
         target.getRevision(), data);

      if (checkin && m_ceHandler.getCmsObject().isWorkflowable())
      {
         PSWorkflowCommandHandler wfh =
            (PSWorkflowCommandHandler) m_ceHandler.getCommandHandler(
               PSWorkflowCommandHandler.COMMAND_NAME);

         // clone the original request
         PSRequest req = data.getRequest();
         PSRequest wfReq = req.cloneRequest();

         // setup the parameters of th erequest clone for a checkin
         HashMap params = new HashMap();
         params.put(IPSHtmlParameters.SYS_CONTENTID,
            Integer.toString(target.getId()));
         params.put(IPSHtmlParameters.SYS_REVISION,
            Integer.toString(target.getRevision()));
         params.put("WFAction", "checkin");
         wfReq.setParameters(params);

         // perform the checkin
         PSExecutionData tempData = null;
         try
         {
            tempData = wfh.makeInternalRequest(wfReq);
         }
         finally
         {
            if (tempData != null)
            {
               tempData.release();
               tempData = null;
            }
         }
      }
   }

   /**
    * Copies all item data specified by the current content and revision ids,
    * and inserts it as a new revision using the specified new revision id. If
    * the contentId and newContentId do not match, an entirely new item is
    * created, and an entry in the CONTENTSTATUS table is made as well.
    *
    * @param contentId The content id of the item that will be copied.
    * @param revisionId The revision id specifying the particular revision of
    * that item to copy.
    * @param newContentId The content id to use when creating the new item.
    * @param newRevisionId The new revision to create.
    * @param data The execution data of the request that is causing this call
    * to be made.  May not be <code>null</code>.
    *
    * @throws PSNotFoundException if the content and revision ids do not
    * specify and existing content item revision.
    * @throws PSAuthorizationException if the user is not authorized to execute
    * the query.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSInternalRequestCallException if there is an error executing the
    * queries.
    * @throws SQLException if there is an error generating new Ids.
    */
   public void createCopy(int contentId, int revisionId,
      int newContentId, int newRevisionId, PSExecutionData data)
      throws PSAuthorizationException, PSInternalRequestCallException,
         PSAuthenticationFailedException, PSNotFoundException, SQLException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      // save request data we will modify
      PSRequest request = data.getRequest();
      HashMap originalParams = request.getParameters();
      HashMap newParams = new HashMap();
      newParams.putAll(originalParams);
      request.setParameters(newParams);
      Document originalDoc = request.getInputDocument();

      boolean isNewItem = (contentId != newContentId) ? true : false;

      // List of files returned from binary queries, held in this variable
      // so they are not G.C.'ed till the update occurs.
      List binaryTempFiles = new ArrayList();

      try
      {
         // fixup parameters and set dbactiontype
         request.setParameter(PSContentEditorHandler.CONTENT_ID_PARAM_NAME,
            String.valueOf(contentId));
         request.setParameter(PSContentEditorHandler.REVISION_ID_PARAM_NAME,
            String.valueOf(revisionId));
         request.setParameter(m_app.getRequestTypeHtmlParamName(),
            m_app.getRequestTypeValueQuery());

         // run each query, add all elements within root to the input doc
         Document inputDoc = executeQueries(data, contentId, revisionId,
            isNewItem, binaryTempFiles);

         // create new child id's for any complex children before inserting
         Map childRowMappings = fixupChildIds(inputDoc);
         request.setPrivateObject(
            PSCloneFactory.CHILD_ROW_MAPPINGS_PRIVATE_OBJECT, childRowMappings);

         /* Fix workflowId and clonedparentId parameters if we are making a new
          * item. If making a new revision, don't need to do this as we are not
          * updating the contentstatus table (and have not queried it, so the
          * method would fail anyhow.
          */
         /* The community of the new item should be the community of user who
          * is creating the new version. So update the community id of the item
          * from user session.
          */
         if (isNewItem)
         {
            //sets the cloned parent id
            request.setParameter(IPSHtmlParameters.SYS_CLONEDPARENTID,
               String.valueOf(contentId));

            //Build fieldname-value elem map for all fields from the input doc
            Map fieldElemMap = BuildFieldElementMap(inputDoc);
            
            //Workflow id is required to be the HTML parameter. Extract from 
            //the input doc and set as HTML param
            Object obj = fieldElemMap.get(IPSHtmlParameters.SYS_WORKFLOWID);
            String defWorkflowId = "";
            if(obj != null)
            {
               obj = ((Element) obj).getFirstChild();
               if (obj != null && obj instanceof Text)
                  defWorkflowId = ((Text) obj).getData();
               request.setParameter(
                  IPSHtmlParameters.SYS_WORKFLOWID,
                  defWorkflowId);
            }

            //Override the clone fields
            Map fields = (Map) request.getParameterObject(
                  PSCloneCommandHandler.SYS_CLONE_OVERRIDE_FIELDSET, null);
            if(fields != null)
            {
               Iterator iter = fields.keySet().iterator();
               String fieldName = null;
               String value = null;
               Node node = null;
               while(iter.hasNext())
               {
                  fieldName = (String)iter.next();
                  value = fields.get(fieldName).toString();

                  if(fieldElemMap.containsKey(fieldName))
                  {
                     if (value.length() < 1)
                     {
                        //Do not touch if the clone override values for 
                        //special fields are empty 
                        if (fieldName.equals(IPSHtmlParameters.SYS_WORKFLOWID)
                           || fieldName.equals(IPSHtmlParameters.SYS_LANG)
                           || fieldName.equals(
                              IPSHtmlParameters.SYS_COMMUNITYID))
                           continue;
                     }
                     //Workflowid must be present as an HTML parameter for the 
                     //update resource.
                     if (fieldName.equals(IPSHtmlParameters.SYS_WORKFLOWID))
                        request.setParameter(fieldName, value);
                  
                     // is the current Content Type visible by this community?
                     if (fieldName.equals(IPSHtmlParameters.SYS_COMMUNITYID))
                        validateCommunity(contentId, revisionId, value);
                     
                     Element el = (Element)fieldElemMap.get(fieldName);
                     if(el == null)
                        continue;
                     node = el.getFirstChild();
                     if(node == null || node.getNodeType()!=Node.TEXT_NODE)
                     {
                        Text val = inputDoc.createTextNode(fields.get(fieldName).toString());
                        el.appendChild(val);
                     }
                     else
                     {
                        ((Text)node).setData(fields.get(fieldName).toString());
                     }
                  }
               }
            }
         }

         // set input doc on the request
         request.setInputDocument(inputDoc);

         // set dbactiontype, newconteid, and newrevidparam
         request.setParameter(m_app.getRequestTypeHtmlParamName(),
            m_app.getRequestTypeValueInsert());
         request.setParameter(m_contentIdParamName,
            String.valueOf(newContentId));
         request.setParameter(m_revisionIdParamName,
            String.valueOf(newRevisionId));

         // make the correct update
         String updateResourceName = null;
         if (isNewItem)
            updateResourceName = (String)m_updateResourceNames.get(
               INSERT_ITEM_RESOURCE);
         else
            updateResourceName = (String)m_updateResourceNames.get(
               INSERT_REVISION_RESOURCE);

         executeUpdate(updateResourceName, data);
      }
      finally
      {
         // reset the request data
         request.setParameters(originalParams);
         request.setInputDocument(originalDoc);

         if (binaryTempFiles != null) // delete the temp files if there are any
         {
            PSPurgableTempFile tmpFile = null;
            for (int i=0; i < binaryTempFiles.size(); i++)
            {
               tmpFile = (PSPurgableTempFile) binaryTempFiles.get(i);
               tmpFile.release();
            }
         }

      }
   }

   /**
    * Validates the specified community, to make sure the current Content Type
    * of the processed item is visible by the specified community.
    *  
    * @param contentId the ID of the copied Content Item.
    * @param revision the revision of the copied Content Item.
    * @param communityId the id of the specified community; assumed not 
    *    <code>null</code> or empty.
    *    
    * @throws PSAuthorizationException if the Content Type is not visible in
    *    the specified community.
    */
   private void validateCommunity(int contentId, int revision,
         String communityId) throws PSAuthorizationException
   {
      // validate the content type is visible to the community
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      try
      {
         boolean isVisible = mgr.isVisibleToCommunity(m_ce.getContentType(),
               Integer.parseInt(communityId));
         if (! isVisible)
         {
            Object[] args = new Object[] {
                  String.valueOf(contentId), String.valueOf(revision), 
                  String.valueOf(m_ce.getContentType()), 
                  communityId
            };
            PSAuthorizationException e = new PSAuthorizationException(
                  IPSCmsErrors.CONTENT_TYPE_NOT_VISIBLE_BY_COMMUNITY, args);
            throw e;
         }
      }
      catch (PSInvalidContentTypeException e)
      {
         e.printStackTrace(); // this is not possible
         throw new RuntimeException(e);
      }
   }

   /**
    * Convenient method to build a map of field name and element value that 
    * has the value from the XML document supplied. This document is assumed 
    * to have a predefined DTD that is produced by 
    * {@link #executeQueries(PSExecutionData, int, int, boolean, List) executeQueries}.
    * @param inputDoc, assumed not <code>null</code>.
    * @return map of element field and value elements, never <code>null</code> 
    * may be empty.
    */
   private Map BuildFieldElementMap(Document inputDoc)
   {
      Map map = new HashMap();
      //Other fields must come first
      NodeList nl = inputDoc.getElementsByTagName("main");
      Element elem =  null;
      if(nl.getLength() > 0)
      {
         nl = ((Element)nl.item(0)).getElementsByTagName("*");
         for(int i=0; i<nl.getLength(); i++)
         {
            elem = (Element)nl.item(i);
            map.put(elem.getTagName(), elem);
         }
      }
      //System fields must come last
      nl = inputDoc.getElementsByTagName("systemFieldset");
      if(nl.getLength() > 0)
      {
         nl = ((Element)nl.item(0)).getElementsByTagName("*");
         for(int i=0; i<nl.getLength(); i++)
         {
            elem = (Element)nl.item(i);
            map.put(elem.getTagName(), elem);
         }
      }
      return map;
   }

   /**
    * Creates all required datasets.  Will recursively walk the display
    * mapper and create a query resource for each table.  Finally creates two
    * update resources, one for new items (inserts row in CONTENTSTATUS table),
    * and one for new revisions.  The update resources will handle inserting
    * the queried data into all tables in a single request.
    *
    * @param mapper The mapper to create datasets for.  May not be
    * <code>null</code>.
    * @param pipe The Content Editor's pipe. May not be <code>null
    * </code>.
    * @param updateMapper The data mapper to build for the update dataset.
    * Should be <code>null</code> for the first call to this method, and it will
    * be created and passed on each successive call as the mapper is recursed.
    * @param updateDtd The DTD builder that will ultimately be used for creating
    * the update resource.  Should be <code>null</code> for the first call to
    * this method, and it will be created and passed on each successive call
    * as the mapper is recursed.
    * @param skipRelationships set to <code>true</code> will not, set to
    *    <code>false</code> it will recreate all related content relationships
    *    from the original.
    *
    * @throws PSValidationException If anything used by this method is missing
    *    or misconfigured.
    */
   private void createDataSets(PSDisplayMapper mapper, PSContentEditorPipe pipe,
      PSDataMapper updateMapper, PSDtdBuilder updateDtd,
      boolean skipRelationships) throws PSValidationException
   {
      if (mapper == null || pipe == null)
         throw new IllegalArgumentException(
            "mapper or pipe may not be null");

      boolean firstCall = false;
      if (updateMapper == null)
      {
         updateMapper = new PSDataMapper();
         updateDtd = new PSDtdBuilder(PSUniqueObjectGenerator.makeUniqueName(
            "CopyDTD"));
         m_updateDtdRoot = updateDtd.getRootName();
         firstCall = true;
      }

      PSFieldSet fieldSet =
            pipe.getMapper().getFieldSet(mapper.getFieldSetRef());

      if (fieldSet == null)
         throw new PSValidationException(IPSServerErrors.CE_MISSING_FIELDSET,
            mapper.getFieldSetRef());

      // create list of system mappings to process later
      Map systemMappings = new HashMap();

      // create list of child mappers to process later
      List childMappers = new ArrayList();

      /* create map of sdmp table's mappings.  Key is the fieldSet, value is
       * the HashMap of mappings.
       */
      Map sdmpMap = new HashMap();

      // Create the query resource for the mapper
      Map dataMappings = new HashMap();
      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         // save child mappers, they will be processed later
         PSDisplayMapper childMapper = mapping.getDisplayMapper();
         if (childMapper != null)
         {
            if (!(skipRelationships && childMapper.getFieldSetRef().equals(
               IPSHtmlParameters.RELATED_CONTENT)))
               childMappers.add(childMapper);
            continue;
         }

         String fieldRef = mapping.getFieldRef();
         Object o = fieldSet.get(fieldRef);
         if (o == null)
         {
            // if sdmp, need to have result come back as
            // separate rows in their own element with their own keys
            PSFieldSet sdmpFieldSet = fieldSet.getChildsFieldSet(fieldRef,
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
            if (sdmpFieldSet != null)
            {
               o = sdmpFieldSet.get(fieldRef);
               if (o != null && o instanceof PSField)
               {
                  // save this for later
                  Map sdmpMappings = (Map)sdmpMap.get(sdmpFieldSet);
                  if (sdmpMappings == null)
                  {
                     sdmpMappings = new HashMap();
                     sdmpMap.put(sdmpFieldSet, sdmpMappings);
                  }
                  sdmpMappings.put(mapping, (PSField)o);
                  continue;
               }
            }

            if (o == null)
            {
               String label = "unlabeled";
               if ( null != mapping.getUISet().getLabel())
                  label = mapping.getUISet().getLabel().getText();
               String [] args = { fieldRef, label };

               throw new PSValidationException(
                  IPSServerErrors.CE_MISSING_FIELD, args );
            }
         }

         if (o instanceof PSFieldSet)
            continue;

         PSField field = (PSField)o;

         // save system fields for a separate resource
         if (field.getType() == PSField.TYPE_SYSTEM)
         {
            systemMappings.put(mapping, field);
            continue;
         }

         dataMappings.put(mapping, field);
      }

      // create the dataset
      createDataSet(dataMappings.entrySet().iterator(), fieldSet, updateMapper,
         updateDtd, false);

      // process system mappings
      if (systemMappings.size() > 0)
      {
         PSContentEditorSystemDef sysDef = m_ceHandler.getSystemDef();
         createDataSet(systemMappings.entrySet().iterator(), sysDef.getFieldSet(),
            updateMapper, updateDtd, true);
      }

      // process sdmp mappings
      Iterator sdmpMappings = sdmpMap.entrySet().iterator();
      while (sdmpMappings.hasNext())
      {
         Map.Entry entry = (Map.Entry)sdmpMappings.next();
         PSFieldSet sdmpFieldSet = (PSFieldSet)entry.getKey();
         Map sdmpDataMappings = (HashMap)entry.getValue();
         createDataSet(sdmpDataMappings.entrySet().iterator(), sdmpFieldSet,
            updateMapper, updateDtd, false);
      }

      // process child mappers
      Iterator children = childMappers.iterator();
      while (children.hasNext())
         createDataSets((PSDisplayMapper)children.next(), pipe, updateMapper,
            updateDtd, skipRelationships);

      // if first call, create update dataset
      if (firstCall)
         createUpdateDatasets(updateMapper, updateDtd);
   }

   /**
    * Creates a query dataset that will query all rows for a particular
    * content item in the table and columns specified by the supplied
    * display mappings and fieldset, simultaneously adding to the update
    * data mapper and dtd so that a full update dataset may be created later.
    * For any binary columns encountered, creates a separate query for each, the
    * results of which will be combined with the table's query results before
    * the update.
    *
    * @param mappings An iterator over PSDisplayMappings that should be used
    * to build the data mapper for this dataset.  Assumed not <code>null</code>.
    * @param fieldSet The fieldSet that the display mappings reference. Assumed
    * not <code>null</code>.
    * @param updateMapper A data mapper that will be used to create the update
    * datasets once all query datasets have been created. Assumed not
    * <code>null</code>.
    * @param updateDtd A dtd builder that will be used to create the update
    * datasets once all query datasets have been created.  Assumed not
    * <code>null</code>.
    * @param isSystemTable <code>true</code> if this will query a system table,
    * <code>false</code> if not.
    *
    * @throws PSValidationException if anything specified in the supplied
    * mappings or fieldSet is invalid.
    */
   private void createDataSet(Iterator mappings, PSFieldSet fieldSet,
      PSDataMapper updateMapper, PSDtdBuilder updateDtd, boolean isSystemTable)
      throws PSValidationException
   {
      try
      {
         PSCollection tables =
               new PSCollection(PSBackEndTable.class);
         PSDataMapper dataMapper = new PSDataMapper();

         /* need to create our own dtd, and wrap all fields as a child of a
          * parent element below the root to deal with multiple rows
          */
         PSDtdBuilder queryDtd = new PSDtdBuilder(
            PSUniqueObjectGenerator.makeUniqueName("CopyDTD"));
         String parentElement = fieldSet.getName();
         int occurence = fieldSet.getType() == PSFieldSet.TYPE_PARENT ?
            PSDtdBuilder.OCCURS_ONCE : PSDtdBuilder.OCCURS_ANY;
         queryDtd.addElement(parentElement, occurence, queryDtd.getRootName());
         // build update dtd as we go
         updateDtd.addElement(parentElement, occurence,
            updateDtd.getRootName());

         // Create context
         PSMappingContext ctx = new PSMappingContext(dataMapper, updateMapper,
            queryDtd, updateDtd, fieldSet);

         while (mappings.hasNext())
         {
            Map.Entry entry = (Map.Entry)mappings.next();
            PSField field = (PSField)entry.getValue();

            IPSBackEndMapping beMapping = field.getLocator();
            if (!(beMapping instanceof PSBackEndColumn))
               continue;
            PSBackEndColumn beCol = (PSBackEndColumn)beMapping;

            if (PSApplicationBuilder.isKeyColumn(beCol.getColumn()))
               continue;

            String tableAlias = beCol.getTable().getAlias();
            PSBackEndTable beTable = (PSBackEndTable)m_beTables.get(
               tableAlias.toLowerCase());
            if (beTable == null)
            {
               throw new PSValidationException(IPSServerErrors.CE_MISSING_TABLE,
                  tableAlias);
            }
            beCol.setTable(beTable);

            if (!tables.contains(beTable))
               tables.add(beTable);

            if (PSMetaDataCache.isBinaryBackendColumn((PSTableSet)
               m_tableSets.get(tableAlias.toLowerCase()), beCol))
            {
               // Create binary queries as we go
               m_binaryQueryHandlers.add(new PSBinaryQueryHandler(ctx,
                  fieldSet, field.getSubmitName(), beCol, isSystemTable));
               continue;
            }

            // Create column mappings and update the dtd's
            ctx.addColumnMapping(field.getSubmitName(), beCol);
         }

         // we should have exactly one table!
         if (tables.size() != 1)
         {
            Object[] args = {fieldSet.getName(), ((PSBackEndTable)tables.get(0)
               ).getAlias(), ((PSBackEndTable)tables.get(1)).getAlias()};
            throw new PSValidationException(
               IPSServerErrors.CE_MULTIPLE_TABLES_NOT_SUPPORTED, args);
         }

         // add keys to both mappers and create selkeys for query
         PSBackEndTable localTable = (PSBackEndTable)tables.get(0);
         Map selKeys = new HashMap();
         ctx.addSystemColumns(localTable, selKeys, isSystemTable);

         String resourceName = PSApplicationBuilder.createQueryDataset(m_app,
            dataMapper, selKeys.entrySet().iterator(), null, queryDtd,
            false);

         m_queryResources.add(new PSQueryResourceContext(resourceName,
            fieldSet, localTable, isSystemTable));
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
      catch (SQLException e)
      {
         throw new RuntimeException(PSDataHandler.getExceptionText(e));
      }
   }


   /**
    * Creates datasets necessary to insert the copied data.  Will create two
    * resources, one that includes contentstatus entries, and one that does not.
    *
    * @param updateMapper A data mapper that contains mappings for all columns
    * in all tables that will be updated.  The document mapping side of each
    * mapping must reference an xml element in the supplied updateDtd parameter.
    * Assumed not <code>null</code>.
    *
    * @param updateDtd A dtd builder that contains all xml fields referenced
    * in the updateMapper.  Should also contain the elements from each of the
    * query resource dtd's that will be used to query the item's data and build
    * the input document for these queries.  Assumed not <code>null</code>.
    *
    * @throws PSValidationException If anything used by this method is missing
    *    or misconfigured.
    */
   private void createUpdateDatasets(PSDataMapper updateMapper,
      PSDtdBuilder updateDtd) throws PSValidationException
   {
      m_updateResourceNames = new HashMap();

      // create revision insert dataset
      String resNameRevisionInsert = PSUniqueObjectGenerator.makeUniqueName(
         "insertRevision");

      PSApplicationBuilder.createUpdateDataset(m_app, resNameRevisionInsert,
         m_ce, null, null, updateMapper, true,
            PSApplicationBuilder.FLAG_ALLOW_INSERTS, updateDtd);

      m_updateResourceNames.put(INSERT_REVISION_RESOURCE,
         resNameRevisionInsert);

      // make the content status insert mappings
      ArrayList insertSysMappings =
         PSApplicationBuilder.getSystemInsertMappings(m_ceHandler, m_ce);

      PSDataMapper insertSystemMapper =
         PSApplicationBuilder.createSystemMappings(
            insertSysMappings.iterator());

      insertSystemMapper.addAll(updateMapper);

      // create item insert dataset
      String resNameItemInsert = PSUniqueObjectGenerator.makeUniqueName(
         "insertItem");

      PSApplicationBuilder.createUpdateDataset(m_app, resNameItemInsert, m_ce,
         null, null, insertSystemMapper, true,
            PSApplicationBuilder.FLAG_ALLOW_INSERTS, updateDtd);

      m_updateResourceNames.put(INSERT_ITEM_RESOURCE, resNameItemInsert);
   }

   /**
    * Executes all queries and returns the resulting xml document containing
    * all rows, each table's rows within an element that is named by the
    * PSFieldSet that was used to build the query.
    *
    * @param data The execution data, assumed not <code>null</code>.
    * @param contentId The contentId to use.
    * @param revisionId The revisionId to use.
    * @param isNewItem <code>true</code> if this will create a new item, and
    * thus needs to query for user settable system fields, <code>false</code> if
    * not.
    * @param binaryTmpFiles a list of <code>PSPurgableTempFile</code> objects,
    *    used to collect all temp files, assume it is not <code>null</code>.
    *    Called is responsible to delete the temp files.
    *
    * @return All results in a single Xml document, never <code>null</code>.
    *
    * @throws PSNotFoundException if the content and revision ids do not
    * specify and existing content item revision.
    * @throws PSAuthorizationException if the user is not authorized to execute
    * the query.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSInternalRequestCallException if there is an error executing the
    * queries.
    * @throws SQLException if there is an error generating new Ids.
    */
   private Document executeQueries(PSExecutionData data, int contentId,
      int revisionId, boolean isNewItem, List binaryTmpFiles)
         throws PSAuthorizationException,
         PSInternalRequestCallException,
         PSAuthenticationFailedException,
         PSNotFoundException, SQLException
   {

      // create empty result doc
      Document resultDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(resultDoc,
         m_updateDtdRoot);

      // run each query, add all elements within root to the input doc
      boolean parentRowSelected = false;
      Iterator resources = m_queryResources.iterator();
      while (resources.hasNext())
      {
         PSQueryResourceContext queryCtx =
            (PSQueryResourceContext)resources.next();

         // don't query system table if only making a new revision
         if (!isNewItem && queryCtx.isSystemTable())
            continue;

         int numRows = executeQueryResource(data, root,
            PSCommandHandler.createRequestName(m_app.getRequestRoot(),
               queryCtx.getResourceName()));
         queryCtx.setRowCount(numRows);
         if (queryCtx.getFieldSet().getType() == PSFieldSet.TYPE_PARENT &&
            numRows > 0)
         {
            parentRowSelected = true;
         }
      }

      if (!parentRowSelected)
      {
         Object[] args = {String.valueOf(contentId),
            String.valueOf(revisionId)};
         throw new PSNotFoundException(
            IPSServerErrors.CE_COPY_REVISION_NOT_FOUND, args);
      }

      // run binary queries and add file info to appropriate rows
      Iterator binHandlers = m_binaryQueryHandlers.iterator();
      while (binHandlers.hasNext())
      {
         PSBinaryQueryHandler qh = (PSBinaryQueryHandler)binHandlers.next();
         binaryTmpFiles.addAll(qh.execute(data, resultDoc));
      }

      return resultDoc;
   }

   /**
    * Executes a query and adds the results to the supplied root.
    *
    * @param data the execution data, assumed not <code>null</code>.
    * @param root the root element to which to add the query results, assumed 
    *    not <code>null</code>.
    * @param resourceName the name of the query resource to execute, assumed
    *    not <code>null</code>.
    * @return the number of rows added to the root element.
    * @throws PSNotFoundException if the content and revision ids do not
    *    specify and existing content item revision.
    * @throws PSAuthorizationException if the user is not authorized to execute
    *    the query.
    * @throws PSAuthenticationFailedException if the user cannot be
    *    authenticated.
    * @throws PSInternalRequestCallException if there is an error executing the
    *    query.
    */
   private int executeQueryResource(PSExecutionData data, Element root, 
      String resourceName) throws PSAuthorizationException,
         PSInternalRequestCallException, PSAuthenticationFailedException,
         PSNotFoundException
   {
      PSExecutionData queryData = null;
      int numRows = 0;

      try
      {
         // execute the query
         IPSInternalResultHandler rh = (IPSInternalResultHandler)
            PSServer.getInternalRequestHandler(resourceName);
         queryData = rh.makeInternalRequest(data.getRequest());

         if (queryData == null)
         {
            // bug!
            throw new RuntimeException("no query handler found for " +
               resourceName);
         }

         Document queryDoc = rh.getResultDoc(queryData);
         queryData.release();
         queryData = null;

         // add the data to the result doc
         PSXmlTreeWalker walker = new PSXmlTreeWalker(queryDoc);
         Element queryRoot = queryDoc.getDocumentElement();

         walker.setCurrent(queryRoot);

         // find the first set of columns
         Element tableRow = walker.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

         while (tableRow != null)
         {
            /* 
             * Save the element and then get the next, otherwise the call to
             * appendChild changes the parent of the walker's current element
             * and we next search in the wrong document!
             */
            Element savedRow = tableRow;

            // get next set of columns
            tableRow = walker.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);

            Node importNode = root.getOwnerDocument().importNode(
               savedRow, true);
            root.appendChild(importNode);
            numRows++;
         }

         return numRows;
      }
      finally
      {
         if (queryData != null)
         {
            queryData.release();
            queryData = null;
         }
      }
   }

   /**
    * Executes the specified update resource.
    *
    * @param resourceName The name of the resource to make a request against.
    * May not be <code>null</code> or empty.
    * @param data The execution data, may not be <code>null</code>.
    *
    * @throws PSAuthorizationException if the user is not authorized to execute
    * the update.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSInternalRequestCallException if there is an error executing the
    * update.
    * @throws RuntimeException if supplied resourceName does not return an
    * internal request handler.
    */
   private void executeUpdate(String resourceName, PSExecutionData data)
      throws PSAuthorizationException, PSInternalRequestCallException,
         PSAuthenticationFailedException, PSNotFoundException
   {
      if (resourceName == null || resourceName.trim().length() == 0)
         throw new IllegalArgumentException(
            "resourceName may not be null or empty");

      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      IPSInternalRequestHandler rh = PSServer.getInternalRequestHandler(
         PSCommandHandler.createRequestName(m_app.getRequestRoot(),
            resourceName));

      if (rh == null)
      {
         // bug!
         throw new RuntimeException("no update handler found for " +
            resourceName);
      }

      PSExecutionData resultData = null;
      try
      {
         resultData = rh.makeInternalRequest(data.getRequest());
      }
      finally
      {
         if (resultData != null)
            resultData.release();
      }

   }

   /**
    * Walks results and replaces the sysId for any row that has one.
    *
    * @param resultDoc The doc to walk and set id's on. Assumed not 
    *    <code>null</code>.
    * @return a map of replaced sysId's, the key is the original sysId as
    *    <code>String</code> and the value is the new sysId as 
    *    <code>String</code>, never <code>null</code>, may be empty.
    * @throws SQLException if there is an error generating new ids.
    * @throws RuntimeException if more rows found in query results than reported
    *    for dataset
    */
   private Map fixupChildIds(Document resultDoc) throws SQLException
   {
      Map replacedIds = new HashMap();
      
      PSXmlTreeWalker walker = new PSXmlTreeWalker(resultDoc);
      Element root = resultDoc.getDocumentElement();

      Iterator queries = m_queryResources.iterator();
      while (queries.hasNext())
      {
         PSQueryResourceContext ctx = (PSQueryResourceContext)queries.next();
         if (ctx.getFieldSet().getType() != PSFieldSet.TYPE_COMPLEX_CHILD)
            continue;

         // determine size of id block to reserve
         int numIds = ctx.getRowCount();

         // make sure we have results to fix up
         if (numIds == 0)
            continue;

         // get the id's
         int[] idBlock = m_cmdHandler.getNextIdBlock(ctx.getTable().getTable(),
            numIds);

         // make sure we search from the root
         walker.setCurrent(root);

         // locate this query's first row
         String searchEl = ctx.getFieldSet().getName();
         int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         Element row = walker.getNextElement(searchEl, firstFlag);

         int curRow = 0;
         while (row != null)
         {
            if (curRow >= numIds)
            {
               // bug! (not really possible)
               throw new RuntimeException(
                  "More rows found in query results than reported for dataset" +
                     ctx.getResourceName());
            }

            // fixup the row's sysid element
            Element sysIdEl = walker.getNextElement(
               PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME, firstFlag);
            if (sysIdEl == null)
            {
               // bug - should have been mapped
               throw new RuntimeException(
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME +
                  " element expected but not found in query results from dataset "
                  + ctx.getResourceName());
            }

            /*
             * the item's value is in one or more text nodes which are its
             * immediate children
             */
            Node text = sysIdEl.getFirstChild();
            while (text != null)
            {
               if (text instanceof Text)
               {
                  String oldSysId = text.getNodeValue();
                  String newSysId = String.valueOf(idBlock[curRow]);
                  
                  Iterator fields = ctx.getFieldSet().getAll();
                  while (fields.hasNext())
                  {
                     Object test = fields.next();
                     if (test instanceof PSField)
                     {
                        PSField field = (PSField) test;
                        if (field.mayHaveInlineLinks())
                           replacedIds.put(
                              PSInlineLinkField.makeInlineRelationshipId(
                                 field.getSubmitName(), oldSysId), 
                              PSInlineLinkField.makeInlineRelationshipId(
                                 field.getSubmitName(), newSysId));
                     }
                  }
                  
                  text.setNodeValue(newSysId);
                  break;
               }
               text = text.getNextSibling();
            }

            // get next row
            walker.setCurrent(row);
            row = walker.getNextElement(searchEl, nextFlag);
            curRow++;
         }
      }
      
      return replacedIds;
   }

   /**
    * This adds the workflowId from the system field set in the provided
    * document to the supplied request. This is necessary because the insert
    * resource uses a PSSingleHtmlParameter extractor to set the workflowId.
    * <p><strong>NOTE:</strong> This method <strong>MUST</strong> be called
    * before <code>fixCommunityId</code> or it may not work correctly. It
    * needs to get the original community id before it is changed by the
    * aforementioned method.
    *
    * @param doc the document to get the workflowId from, assumed not
    *    <code>null</code>.
    * @param request the request to add the workflowId to, assumed not
    *    <code>null</code>.
    *
    * @throws PSInternalRequestCallException If the request to get the list of
    *    workflows for a community fails while processing.
    * @throws PSAuthorizationException if the user is not authorized to execute
    *    the query.
    * @throws PSAuthenticationFailedException if the user cannot be
    *    authenticated.
    */
   private void fixWorkflowId(Document doc, PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      boolean suppliedWorkflowId = true;
      String strWorkflowId =
         request.getParameter(IPSHtmlParameters.SYS_WORKFLOWID);
      if (strWorkflowId == null || strWorkflowId.trim().length() == 0)
      {
         strWorkflowId = PSChoiceBuilder.getFirstTagText(
            doc.getDocumentElement(), IPSHtmlParameters.SYS_WORKFLOWID);
         suppliedWorkflowId = false;
      }

      String communityId = request.getParameter(
         IPSHtmlParameters.SYS_COMMUNITYID);
      if (communityId == null || communityId.trim().length() == 0)
         communityId = getRequestorsCommunity(request);

     // Are communities enabled?
      boolean communitiesEnabled = PSServer.getServerProps().getProperty(
         "communities_enabled").trim().equalsIgnoreCase("yes");

      // If community exists and communities are enabled
      // is set to yes, then get workflow intersection
      // else we just return the "standard" default workflow
      // for this content type.
      if (null != communityId && communitiesEnabled)
      {
         String resourceName = "sys_psxCataloger/communityWorkflows";
         PSInternalRequest rh =
               PSServer.getInternalRequest( resourceName, request, null, true );

         if (rh == null)
         {
            // bug!
            throw new RuntimeException("No handler found for " +
               resourceName);
         }

         Document communities = rh.getResultDoc();
         ArrayList commWorkflowIds = new ArrayList();
         if ( null != communities )
         {
            // get the possible communities for the workflow
            PSXmlTreeWalker walker = new PSXmlTreeWalker(communities);
            String nodeName = "Workflow";
            Element el = walker.getNextElement( nodeName,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN );
            while (el != null)
            {
               commWorkflowIds.add(el.getAttribute("workflowid"));
               el = walker.getNextElement( nodeName,
                     PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
            }
         }

         //Intersect list w/ this content editors list
         Collection resultWorkflows = new ArrayList();
         Iterator iter = commWorkflowIds.iterator();
         boolean isInclusionary = null != m_workflowIds
               ? m_ce.getWorkflowInfo().getType().equals(
                  PSWorkflowInfo.TYPE_INCLUSIONARY)
               : true;
         while ( iter.hasNext())
         {
            Object id = iter.next();
            if ( null == m_workflowIds )
               resultWorkflows.add( id );
            else if (("" + m_ce.getWorkflowId()).equals((String) id))
               resultWorkflows.add( id );
            else if ( isInclusionary )
            {
               if (m_workflowIds.contains( id ))
                  resultWorkflows.add( id );
            }
            else
            {
               if ( !m_workflowIds.contains( id ))
                  resultWorkflows.add( id );
            }
         }

         /**
          * If a workflow id was supplied with the request, we only need to
          * test if it is valid for the requested community.
          * If it was not supplied in the request we must make sure our lookup
          * is not ambiguous.
          */
         String errMsg = null;
         if (suppliedWorkflowId)
         {
            boolean found = false;
            Iterator testIds = resultWorkflows.iterator();
            while (!found && testIds.hasNext())
            {
               String testId = (String) testIds.next();
               if (testId != null && strWorkflowId.equals(testId))
                  found = true;
            }

            if (!found)
            {
               errMsg = "The supplied workflow id (" + strWorkflowId +
                  ") is not allowed for community id (" + communityId + ").";
            }
         }
         else
         {
            String NON_UNIQUE_MSG = "Couldn't find unique workflow for "
                  + "community id '" + communityId + "' and content type id '"
                  + m_ce.getContentType() + "'.";
            // If we have exactly 1 entry, we are golden, otherwise validating
            // the workflow id if needed.
            switch ( resultWorkflows.size())
            {
               case 0:
                  if ( commWorkflowIds.size() == 0 )
                  {
                     errMsg = "Community with id '" + communityId + "' does not "
                           + "have any workflows assigned.";
                  }
                  else
                     errMsg = NON_UNIQUE_MSG;
                  break;
               case 1:
                  strWorkflowId = resultWorkflows.iterator().next().toString();
                  break;
               default:
                  /* If the src doc and user community are the same, use the wf
                     from the src doc. */
                  PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
                  Element el = walker.getNextElement(
                     IPSHtmlParameters.SYS_COMMUNITYID);
                  String docCommunityId = "";
                  if(el != null)
                     docCommunityId = walker.getElementData(el);
                  if ( docCommunityId.equals( communityId ))
                  {
                     break;
                  }
                  // If the workflow from the src doc exists for the current
                  // user community (in "resultWorkflows"), then use it
                  else if ( resultWorkflows.contains(strWorkflowId))
                  {
                     break;
                  }
                  else
                  {
                     errMsg = NON_UNIQUE_MSG;
                  }
                  break;
            }
         }
         if ( null != errMsg )
            throw new RuntimeException( errMsg );
      }

      request.setParameter(IPSHtmlParameters.SYS_WORKFLOWID, strWorkflowId);
   }

   /**
    * Looks in the session associated with the supplied request for a private
    * session object that contains the community id.
    * <p><em>Note:</em> This is problematic because we have hardcoded in
    * knowledge that we really shouldn't know. We could be smarter here and
    * check that communities are enabled and validate that community id, but
    * that will be left for when the workflow engine is more tightly integreated
    * with the server.
    *
    * @param request The request being processed. Assumed not <code>null</code>.
    *
    * @return A string representation of the numeric community id of the user
    * that made the request, or <code>null</code> if no id was found in the
    * session (e.g. communities are disabled). No validity check is done on the
    * returned text, it is only guaranteed not to be empty if it is not <code>
    * null</code>.
    */
   private String getRequestorsCommunity( PSRequest request )
   {
      PSUserSession session = request.getUserSession();
      Object obj = session.getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
      /* since we don't have any guarantees on this variable, I'm being a little
         more carefule */
      String communityId = null;
      if(obj != null)
      {
         communityId = obj.toString();
         if ( null == communityId || communityId.trim().length() == 0 )
            communityId = null;
      }
      return communityId;
  }

   /**
    * Updates the community element in the document with the community id of
    * the user. Ignores the action if the community element is not found in the
    * request.
    *
    * @param doc the document to update, assumed not to be <code>null</code>
    * @param request the request to get the community id from, assumed not to be
    * <code>null</code>
    *
    */
   private void fixCommunityId(Document doc, PSRequest request)
   {
      String communityId = request.getParameter(
         IPSHtmlParameters.SYS_COMMUNITYID);
      if (communityId == null || communityId.trim().length() == 0)
         communityId = getRequestorsCommunity(request);
      if ( null == communityId )
         return;

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      Element el = walker.getNextElement( IPSHtmlParameters.SYS_COMMUNITYID );
      if(el != null)
      {
         Node childNode = el.getFirstChild();
         if(childNode instanceof Text)
         {
            ((Text)childNode).setData( communityId );
         }
      }
   }

   /**
    * Fixes the language id in the supplied document if the supplied request
    * specifies the <code>SYS_LANG</code> parameter with a valid value, does
    * nothing otherwise.
    *
    * @param doc the document to update, assumed not to be <code>null</code>
    * @param request the request to get the language id from, assumed not to be
    *    <code>null</code>
    */
   private void fixLangId(Document doc, PSRequest request)
   {
      String langId = request.getParameter(IPSHtmlParameters.SYS_LANG);
      if (langId == null || langId.trim().length() == 0)
         return;

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      Element elem = walker.getNextElement(IPSHtmlParameters.SYS_LANG);
      if (elem != null)
      {
         Node childNode = elem.getFirstChild();
         if(childNode instanceof Text)
            ((Text)childNode).setData(langId);
      }
   }

   /**
    * List of PSQueryResourceContext objects to process in order to build the
    * input document for the update.  Never <code>null</code>, entries are added
    * in the ctor.
    */
   private List m_queryResources = new ArrayList();

   /**
    * Map of the resources used to make the copy of all data.  Key is a constant
    * used to identify each resource required, value is the request name.
    * Initialized in during construction, never <code>null</code> or modified
    * after that.
    */
   private Map m_updateResourceNames = null;

   /**
    * The application object to which all datasets will be added.  Initialized
    * in the ctor, never <code>null</code> after that.
    */
   private PSApplication m_app = null;

   /**
    * The Content Editor definition.  Initialized in the ctor, never <code>null
    * </code> after that.
    */
   private PSContentEditor m_ce = null;

   /**
    * The Content Editor Handler for the Content Editor definition.  Initialized
    * in the ctor, never <code>null </code> after that.
    */
   private PSContentEditorHandler m_ceHandler = null;


   /**
    * The command handler that is creating the copy.  Initialized in the ctor,
    * never <code>null </code> after that.
    */
   private PSCommandHandler m_cmdHandler = null;

   /**
    * Map of PSTableSets contained in the Content Editor's pipe, with the table
    * alias lowercased as the key.  Initialized in the ctor, never <code>null
    * </code> after that.
    */
   private Map m_tableSets = null;

   /**
    * Map of PSBackEndTables constructed from the PSTableSets contained in the
    * Content Editor's pipe, with the table alias lowercased as the key.
    * Initialized in the ctor, never <code>null</code> after that.
    */
   private Map m_beTables = null;

   /**
    * List of PSBinaryQueryHandler objects used to separately query binary data
    * from each table.  Added to during construction, never modified after that.
    */
   private List m_binaryQueryHandlers = new ArrayList();

   /**
    * Constant to locate the entry for creating a new revision in the
    * {@link #m_updateResourceNames} map.
    */
   private static final String INSERT_REVISION_RESOURCE = "InsertRevision";

   /**
    * Constant to locate the enry for creating a new item in the
    * {@link #m_updateResourceNames} map.
    */
   private static final String INSERT_ITEM_RESOURCE = "InsertItem";

   /**
    * Root element name of the update dtd, preserved so that an input
    * document can be built to conform to the dtd.  Set during construction,
    * never <code>null</code> after that.
    */
   private String m_updateDtdRoot = null;

   /**
    * Parameter name to use to set and retrieve the content id.  Initialized by
    * the ctor, never <code>null</code> or modified after that.
    */
   private String m_contentIdParamName = null;

   /**
    * Parameter name to use to set and retrieve the revision id.  Initialized by
    * the ctor, never <code>null</code> or modified after that.
    */
   private String m_revisionIdParamName = null;

   /**
    * Contains the workflow ids for this content editor. Each
    * entry is a String that contains a numeric workflow identifier. It is
    * <code>null</code> if the object of the content editor is not workflowable;
    * otherwise, if there is a list of included or excluded workflows, then
    * this set will be non-<code>null</code>, else it is <code>null</code>.
    * Initialized in ctor, then never changed. If present, this list is used in
    * conjunction with the type field of the PSWorkflowInfo to determine how to
    * interpret this list.
    */
   private Collection m_workflowIds = null;

   /**
    * The data extractor used to get the current revision from the current
    * execution context, initialized here and never <code>null</code> or
    * changed after that.
    */
   protected PSContentItemStatusExtractor m_currentRevisionExtractor = 
      new PSContentItemStatusExtractor(
         new PSContentItemStatus("CONTENTSTATUS", "CURRENTREVISION"));

   /**
    * Class to encapsulate several objects required to add mappings and keys
    * to a query resource while simultaneously building an update resource,
    * and to provide functionality to help build data mappers.
    */
   private class PSMappingContext
   {
      /**
       * Constructor for this class.
       *
       * @param queryMapper mapper that query datamappings are added to.  May
       * not be <code>null</code>.
       * @param updateMapper mapper that update datamappings are added to.
       * May not be <code>null</code>.
       * @param queryDtd dtdBuilder that xmlfields used in query datamappings
       * are added to. May not be <code>null</code>.
       * @param updateDtd dtdBuilder that xmlfields used in update datamappings
       * are added to. May not be <code>null</code>.
       * @param fieldSet The fieldSet that is being mapped. May not be
       * <code>null</code>.
       */
      public PSMappingContext(PSDataMapper queryMapper,
         PSDataMapper updateMapper, PSDtdBuilder queryDtd,
         PSDtdBuilder updateDtd, PSFieldSet fieldSet)
      {
         if (queryMapper == null || updateMapper == null || queryDtd == null ||
            updateDtd == null || fieldSet == null)
         {
            throw new IllegalArgumentException("One or more params is invalid");
         }

         m_queryMapper = queryMapper;
         m_updateMapper = updateMapper;
         m_queryDtdBuilder = queryDtd;
         m_updateDtdBuilder = updateDtd;
         m_fieldSet = fieldSet;

      }

      /**
       * Returns the query data mapper.
       *
       * @return The mapper, never <code>null</code>.
       */
      public PSDataMapper getQueryMapper()
      {
         return m_queryMapper;
      }

      /**
       * Returns the update data mapper.
       *
       * @return The mapper, never <code>null</code>.
       */
      public PSDataMapper getUpdateMapper()
      {
         return m_updateMapper;
      }

      /**
       * Returns the query dtd builder.
       *
       * @return The dtd builder, never <code>null</code>.
       */
      public PSDtdBuilder getQueryDtd()
      {
         return m_queryDtdBuilder;
      }

      /**
       * Returns the update dtd builder.
       *
       * @return The dtd builder, never <code>null</code>.
       */
      public PSDtdBuilder getUpdateDtd()
      {
         return m_updateDtdBuilder;
      }

      /**
       * Returns the name of the parent element to which dtd entries will be
       * added. This is the name of the fieldset provided to the ctor.
       *
       * @return The element name, never <code>null</code>.
       */
      public String getParentElement()
      {
         return m_fieldSet.getName();
      }

      /**
       * Convenience version of {@link #addColumnMapping(String,
       * PSBackEndColumn, PSHtmlParameter)} that passes <code>null</code> for
       * the updateDocMapping parameter.
       */
      public void addColumnMapping(String xmlFieldName, PSBackEndColumn beCol)
      {
         addColumnMapping(xmlFieldName, beCol, null);
      }


      /**
       * Creates query and update datamappings for the specified backend column
       * and xml field pair.  Also adds the xml field to the dtd's in the
       * supplied mapping context.
       *
       * @param xmlFieldName The name of the xml element to map the backend
       * column to in the data mappers, and to add to the dtds. Assumed not
       * <code>null</code>.
       * @param beCol The backend column that is used in the data mappings.
       * Assumed not <code>null</code>.
       * @param updateDocMapping If supplied, will be used for the docMapping
       * side of the mapping instead of an xmlField.  May be <code>null</code>,
       * in which case the xmlFieldName is used.
       */
      public void addColumnMapping(String xmlFieldName, PSBackEndColumn beCol,
         PSHtmlParameter updateDocMapping)
      {
         try
         {
            String parentElement = getParentElement();
            // app builder will add the root name for queries
            PSDataMapping dataMapping = new PSDataMapping(parentElement + "/"
               + xmlFieldName, beCol);
            m_queryMapper.add(dataMapping);

            // add to update mapper as we go - supply root for update mappings
            if (updateDocMapping == null)
               dataMapping = new PSDataMapping(m_updateDtdBuilder.getRootName()
                  + "/" + parentElement + "/" + xmlFieldName,  beCol);
            else
               dataMapping = new PSDataMapping(updateDocMapping, beCol);

            m_updateMapper.add(dataMapping);

            // add to both dtd's
            m_queryDtdBuilder.addElement(xmlFieldName,
               PSDtdBuilder.OCCURS_ONCE, parentElement);
            m_updateDtdBuilder.addElement(xmlFieldName,
               PSDtdBuilder.OCCURS_ONCE, parentElement);
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalArgumentException(e.getLocalizedMessage());
         }
      }


      /**
       * Adds all system columns required to query and update the the specified
       * backend table.  System columns are added to the update data mapper in
       * the supplied mapping context. Those that are keys are added to the
       * supplied selectionKeys map.
       *
       * @param beTable The backend table for which keys are being created. May
       * not be <code>null</code>.
       * @param selectionKeys A map to which selection key mappings are added.
       * May not be <code>null</code>.
       * @param isSystemTable <code>true</code> if this will query a system
       * table, <code>false</code> if not.
       */
      public void addSystemColumns(PSBackEndTable beTable, Map selectionKeys,
         boolean isSystemTable)
      {
         if (beTable == null || selectionKeys == null)
            throw new IllegalArgumentException("one or more params is null");

         addKeyColumn(beTable, IPSConstants.ITEM_PKEY_CONTENTID,
            m_contentIdParamName);

         if (!isSystemTable)
         {
            addKeyColumn(beTable, IPSConstants.ITEM_PKEY_REVISIONID,
               m_revisionIdParamName);

            if (m_fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            {
               addKeyColumn(beTable, IPSConstants.CHILD_ITEM_PKEY,
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME, null);

               if (m_fieldSet.isSequencingSupported())
               {
                  addKeyColumn(beTable, IPSConstants.CHILD_SORT_KEY,
                     IPSConstants.CHILD_SORT_KEY, null);
               }
            }
         }

         addSelectionKeys(selectionKeys, isSystemTable);
      }

      /**
       * Creates update data mapping and query selection key mapping for the
       * specified column/xml field pair.
       *
       * @param beTable The backend table to use to create a PSBackEndColumn for
       * the data mapping.  Assumed not <code>null</code>.
       * @param columnName The name of the key column.  Assumed not <code>null
       * </code>.
       * @param xmlFieldName The name of the xml element to use as the html
       * parameter name in the selection keys mapping.  Assumed not <code>null
       * </code>.
       * @param updateHtmlParamName The html parameter name to use for the doc
       * mapping side of the update data mapping.  If <code>null</code>, then
       * the xmlField will be used for the update mapping as well as the query
       * data mapping.
       */
      private void addKeyColumn(PSBackEndTable beTable, String columnName,
         String xmlFieldName, String updateHtmlParamName)
      {
         try
         {
            PSBackEndColumn beCol = new PSBackEndColumn(beTable,
               columnName);
            PSHtmlParameter updateParam = null;
            if (updateHtmlParamName != null)
               updateParam = new PSHtmlParameter(updateHtmlParamName);
            addColumnMapping(xmlFieldName, beCol, updateParam);
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalArgumentException(e.getLocalizedMessage());
         }
      }

      /**
       * Convenience version of {@link #addKeyColumn(PSBackEndTable, String,
       * String, String)}, uses an PSHtmlParameter for the update mapping using
       * the xmlFieldName for updateHtmlParamName.
       */
      private void addKeyColumn(PSBackEndTable beTable, String columnName,
         String xmlFieldName)
      {
         addKeyColumn(beTable, columnName, xmlFieldName, xmlFieldName);
      }

      /**
       * Add's required selection keys to the provided map.
       *
       * @param selectionKeys Map to add keys to.  Assumed not <code>null
       * </code>.
       * @param isSystemTable <code>true</code> if this will query a system
       * table, <code>false</code> if not.
       */
      public void addSelectionKeys(Map selectionKeys, boolean isSystemTable)
      {
         addSelectionKeys(selectionKeys, isSystemTable, false);
      }

      /**
       * Add's required selection keys to the provided map.
       *
       * @param selectionKeys Map to add keys to.  Assumed not <code>null
       * </code>.
       * @param isSystemTable <code>true</code> if this will query a system
       * table, <code>false</code> if not.
       * @param includeChildKeys If <code>true</code>, keys will be added to
       * select a specific child row.
       */
      public void addSelectionKeys(Map selectionKeys, boolean isSystemTable,
         boolean includeChildKeys)
      {
         selectionKeys.put(IPSConstants.ITEM_PKEY_CONTENTID,
            m_contentIdParamName);

         if (!isSystemTable)
         {
            selectionKeys.put(IPSConstants.ITEM_PKEY_REVISIONID,
               m_revisionIdParamName);

            if (includeChildKeys)
               selectionKeys.put(IPSConstants.CHILD_ITEM_PKEY,
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);
         }
      }

      /**
       * Data mapper for building a query resource.  Initialized in the ctor,
       * may be modified after that.
       */
      private PSDataMapper m_queryMapper = null;

      /**
       * Data mapper for building an update resource.  Initialized in the ctor,
       * may be modified after that.
       */
      private PSDataMapper m_updateMapper = null;

      /**
       * Dtd Builder for building a query resource.  Initialized in the ctor,
       * may be modified after that.
       */
      private PSDtdBuilder m_queryDtdBuilder = null;

      /**
       * Dtd Builder for building an update resource.  Initialized in the ctor,
       * may be modified after that.
       */
      private PSDtdBuilder m_updateDtdBuilder = null;

      /**
       * The fieldSet to create mappings for.  Initialized in the
       * ctor, never <code>null</code> or modified after that.
       */
      private PSFieldSet m_fieldSet = null;
   }

   /**
    * Used to create a binary query for the given column, and run the query,
    * fixing up the result doc with the results.
    */
   private class PSBinaryQueryHandler
   {
      /**
       * Creates the query resource.
       *
       * @param ctx The mapping context to use when creating the query.  May
       * not be <code>null</code>.
       * @param fieldSet The the fieldSet that is being updated. May not be
       * <code>null</code> or empty.
       * @param xmlFieldName The name of the xml field to add to the result doc.
       * May not be <code>null</code> or empty.
       * @param beCol The backend column to query for the binary data.
       * May not be <code>null</code>.
       * @param isSystemTable <code>true</code> if this will query a system
       * table, <code>false</code> if not.
       *
       * @throws PSValidationException If anything used by this method is
       * missing or misconfigured.
       */
      public PSBinaryQueryHandler(PSMappingContext ctx, PSFieldSet fieldSet,
         String xmlFieldName, PSBackEndColumn beCol, boolean isSystemTable)
         throws PSValidationException
      {
         if (ctx == null || xmlFieldName == null ||
            xmlFieldName.trim().length() == 0 || fieldSet == null ||
            beCol == null)
         {
            throw new IllegalArgumentException(
               "One or more params is null or empty");
         }

         m_fieldSet = fieldSet;
         m_xmlFieldName = xmlFieldName;

         // create new query data mapper and dtd
         PSDataMapper binQueryMapper = new PSDataMapper();
         PSDtdBuilder binQueryDtd = new PSDtdBuilder(
            PSUniqueObjectGenerator.makeUniqueName("CopyDTD"));
         binQueryDtd.addElement(ctx.getParentElement(),
            PSDtdBuilder.OCCURS_ONCE, binQueryDtd.getRootName());

         // make new ctx using current update stuff
         PSMappingContext binCtx = new PSMappingContext(binQueryMapper,
            ctx.getUpdateMapper(), binQueryDtd, ctx.getUpdateDtd(),
            fieldSet);

         // add column
         binCtx.addColumnMapping(xmlFieldName, beCol);

         // create selection keys
         Map selKeys = new HashMap();
         binCtx.addSelectionKeys(selKeys, isSystemTable, (fieldSet.getType() ==
            PSFieldSet.TYPE_COMPLEX_CHILD));

         // create the dataset and store its name
         m_resourceName = PSApplicationBuilder.createQueryDataset(m_app,
            binQueryMapper, selKeys.entrySet().iterator(), null, binQueryDtd,
               true);
      }

      /**
       * Walks the resultDoc and for each element found that matches this
       * handler's fieldSetName, extracts the content and revision id's,
       * set's up the request parameters, executes the query, and adds the
       * binary field element to the result doc.
       *
       * @param data The execution data, assumed not <code>null</code>.
       * @param resultDoc The doc to add the results to. Assumed not <code>null
       * </code>.
       *
       * @return A List of Purgable Temp File objects for which a reference is
       * added to the result doc.  May be empty if the query did not return any
       * data, but never <code>null</code>.
       *
       * @throws PSAuthorizationException if the user is not authorized to
       * execute
       * the query.
       * @throws PSAuthenticationFailedException if the user cannot be
       * authenticated.
       * @throws PSInternalRequestCallException if there is an error executing
       * the query.
       * @throws RuntimeException if the resource handler for this query is not
       * returned by the server.
       */
      public List execute(PSExecutionData data, Document resultDoc)
         throws PSAuthorizationException, PSInternalRequestCallException,
            PSAuthenticationFailedException
      {
         List fileList = new ArrayList();

         // get the request handler
         String requestName = PSCommandHandler.createRequestName(
            m_app.getName(), m_resourceName);
         IPSInternalResultHandler rh = (IPSInternalResultHandler)
            PSServer.getInternalRequestHandler(requestName);

         if (rh == null)
         {
            // bug!
            throw new RuntimeException("No binary query handler found for " +
               requestName);
         }

         PSRequest request = data.getRequest();

         // walk each result set and if found, query and add binary content
         PSXmlTreeWalker walker = new PSXmlTreeWalker(resultDoc);
         Element root = resultDoc.getDocumentElement();
         walker.setCurrent(root);

         Element row = walker.getNextElement(m_fieldSet.getName(),
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

         while (row != null)
         {
            // Extract the keys and add them as params to the request
            String strKey = walker.getElementData(m_contentIdParamName,
               false);
            request.setParameter(m_contentIdParamName, strKey);

            strKey = walker.getElementData(m_revisionIdParamName,
               false);
            request.setParameter(m_revisionIdParamName, strKey);

            if (m_fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            {
               strKey = walker.getElementData(
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME, false);
               request.setParameter(
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME, strKey);
            }

            // execute the query - this will add the data to the row
            PSPurgableTempFile result = executeQuery(rh, data, resultDoc, row);
            if (result != null)
               fileList.add(result);

            // get the next row
            row = walker.getNextElement(m_fieldSet.getName(),
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         return fileList;
      }


      /**
       * Executes the query for the current data and adds the file reference to
       * the supplied element.
       *
       * @param data The execution data, assumed not <code>null</code>.
       * @param doc The doc to add the results to. Assumed not <code>null
       * </code>.
       * @param row The element to update with the resulting file info.
       *
       * @return The file object for which a reference is added to the result
       * doc.  May be <code>null</code> if the query did not return any data.
       *
       * @throws PSAuthorizationException if the user is not authorized to
       * execute the query.
       * @throws PSAuthenticationFailedException if the user cannot be
       * authenticated.
       * @throws PSInternalRequestCallException if there is an error executing
       * the query.
       */
      private PSPurgableTempFile executeQuery(IPSInternalResultHandler rh,
         PSExecutionData data, Document doc, Element row)
         throws PSAuthorizationException, PSInternalRequestCallException,
            PSAuthenticationFailedException
      {
         PSExecutionData queryData = null;
         PSPurgableTempFile queryResult = null;
         try
         {
            // execute query
            queryData = rh.makeInternalRequest(data.getRequest());
            PSMimeContentResult result  = rh.getMimeContent(queryData, false);
            if (result != null)
            {
               // grab or transfer the temp file from the "result"
               queryResult = result.getFileResource();
               URL u = null;
               try
               {
                  u = queryResult.toURL();
               }
               catch(MalformedURLException e)
               {
                  // shouldn't happen!
                  throw new RuntimeException(e.getLocalizedMessage());
               }

               Element fileEl = PSXmlDocumentBuilder.addEmptyElement(doc, row,
                  m_xmlFieldName);
               fileEl.setAttribute(
                  PSXmlFieldExtractor.XML_URL_REFERENCE_ATTRIBUTE,
                     u.toExternalForm());
            }

            return queryResult;
         }
         finally
         {
            if (queryData != null)
            {
               queryData.release();
               queryData = null;
            }
         }
      }

      /**
       * The fieldSet being updated. Initialized in the ctor, never
       * <code>null</code> after that.
       */
      private PSFieldSet m_fieldSet = null;

      /**
       * The name of the xml field element to add in the result doc. Initialized
       * in the ctor, never <code>null</code> after that.
       */
      private String m_xmlFieldName = null;

      /**
       * Name of resource to query for the binary data.  Initialized in the
       * ctor, never <code>null</code> after that.
       */
      private String m_resourceName = null;
   }

   /**
    * Encapsulates information regarding a query dataset that has been created.
    */
   private class PSQueryResourceContext
   {
      /**
       * Constructor for this class.
       *
       * @param resourceName The dataset name of the resource.  May not be
       * <code>null</code> or empty.
       * @param fieldSet The fieldSet this resource is querying data for.
       *  May not be <code>null</code> or empty.
       * @param table The table this resource is querying.  May not be <code>
       * null</code>.
       * @param isSystemTable Specify <code>true</code> if the table being
       * queried is a system table, <code>false</code> if not.
       */
      public PSQueryResourceContext(String resourceName, PSFieldSet fieldSet,
         PSBackEndTable table, boolean isSystemTable)
      {
         if (resourceName == null || resourceName.trim().length() == 0)
            throw new IllegalArgumentException(
               "resourceName may not be null or empty");

         if (fieldSet == null)
            throw new IllegalArgumentException("fieldSet may not be null");

         if (table == null)
            throw new IllegalArgumentException("table may not be null");

         m_resourceName = resourceName;
         m_fieldSet = fieldSet;
         m_table = table;
         m_isSystemTable = isSystemTable;
      }

      /**
       * Returns the name to use when getting the internal request handler for
       * this query.
       *
       * @return The name, never <code>null</code>.
       */
      public String getResourceName()
      {
         return m_resourceName;
      }

      /**
       * Returns the field set used to create this query, and whose name is used
       * as the enclosing XML elemement name for any rows returned by this
       * query.
       *
       * @return The name, never <code>null</code>.
       */
      public PSFieldSet getFieldSet()
      {
         return m_fieldSet;
      }

      /**
       * Returns the table this query is using.
       *
       * @return The table, never <code>null</code>.
       */
      public PSBackEndTable getTable()
      {
         return m_table;
      }



      /**
       * Determines if this query is for a system table.
       *
       * @return <code>true</code> if this will query a system table, false
       * if not.
       */
      public boolean isSystemTable()
      {
         return m_isSystemTable;
      }


      /**
       * Sets the number of rows this query returned when last executed.
       *
       * @param rowCount The number of rows.  Must not be less than zero.
       */
      public void setRowCount(int rowCount)
      {
         if (rowCount < 0)
            throw new IllegalArgumentException(
               "rowCount may not be less than zero");

         m_rowCount = rowCount;
      }

      /**
       * Returns the number of rows this query returned when last executed.
       *
       * @return The number of rows, or <code>zero</code> if it has never been
       * set.
       */
      public int getRowCount()
      {
         return m_rowCount;
      }

      /**
       * The dataset name of the query resource.  Initialized in the ctor, never
       * <code>null</code> after that.
       */
      private String m_resourceName = null;

      /**
       * The fieldset used to create this resource.  Initialized in the ctor,
       * never <code>null</code> after that.
       */
      private PSFieldSet m_fieldSet = null;

      /**
       * The table used to create this resource.  Initialized in the ctor,
       * never <code>null</code> after that.
       */
      private PSBackEndTable m_table = null;

      /**
       * Specifies if the table being queried is a system table or not.
       */
      private boolean m_isSystemTable;

      /**
       * The number of rows this query last returned.  Initially
       * zero, is modified by calls to {#@link setRowCount(int)
       * setRowCount()}
       */
      private int m_rowCount = 0;
   }
}

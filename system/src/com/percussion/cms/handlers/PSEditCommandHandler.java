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
import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.PSCompleteChildDocumentBuilder;
import com.percussion.cms.PSEditorDocumentBuilder;
import com.percussion.cms.PSEditorDocumentContext;
import com.percussion.cms.PSPageInfo;
import com.percussion.cms.PSRowEditorDocumentBuilder;
import com.percussion.cms.PSSummaryEditorDocumentBuilder;
import com.percussion.conn.PSServerException;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.PSExtensionException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.PSCms;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSMapPair;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates the builders used to compose new documents of a specific type.
 * These builders are then used by the base class when it is processing a
 * request. No internal requests are used by this handler.
 * <p>Each new handler is composed of 1 or more builders. There is always a
 * builder for the parent row editor. Then there will be a builder for each
 * complex child. Summary editors for complex children are always handled by
 * the edit handler, not this handler.
 */
public class PSEditCommandHandler extends PSQueryCommandHandler
{
   /**
    * The internal name of this handler. When handler names are used in
    * config files, this is the name that must be used.
    */
   public static final String COMMAND_NAME = "edit";

   /**
    * Creates a handler to process requests for modifying existing content
    * items.
    * <p>See {@link PSQueryCommandHandler#PSQueryCommandHandler(
    * PSApplicationHandler, PSContentEditorHandler, PSContentEditor,
    * PSApplication, String) base} class for a description of the params and
    * exception.
    */
   public PSEditCommandHandler( PSApplicationHandler ah,
         PSContentEditorHandler ceh, PSContentEditor ce,
         PSApplication app )
      throws PSNotFoundException, PSIllegalArgumentException,
         PSExtensionException, PSServerException, PSSystemValidationException
   {
      super( ah, ceh, ce, app, COMMAND_NAME );

      // todo: get meta data and validate

      PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
      if ( null == pipe )
      {
         String [] args =
         {
               ce.getName(),
               ah.getApplicationDefinition().getName()
         };
         throw new PSSystemValidationException(
               IPSServerErrors.APP_NO_QUERY_PIPES_IN_DATASET, args );
      }
      PSDisplayMapper dispMapper =
            pipe.getMapper().getUIDefinition().getDisplayMapper();

      StringBuffer result = new StringBuffer(100); // arbitrary size
      initializeRequestResources( app, ce, dispMapper, 0,
            PSQueryCommandHandler.ROOT_PARENT_PAGE_ID, m_pageInfo, result );

      m_appName = app.getName();

      // init builders
      PSEditorDocumentContext rowCtx = new PSEditorDocumentContext( ceh,
            ah.getApplicationDefinition(), ce );

      URL url;
      try
      {
         if ( null == ce.getRequestor())
         {
            throw new PSSystemValidationException(
                  IPSServerErrors.CE_MISSING_REQUESTOR, ce.getName());
         }
          if (PSServer.getProperty("requestBehindProxy","false").equalsIgnoreCase("true")) {
              url = new URL( PSServer.getProperty("proxyScheme","http"),
                      PSServer.getProperty("publicCmsHostname","127.0.0.1"),
                      Integer.parseInt(PSServer.getProperty("proxyPort",""+PSServer.getListenerPort()))
                      , ah.getFullRequestRoot() + "/"
                      + ce.getRequestor().getRequestPage() + ".html");
          }else {
              url = new URL("http", PSServer.getServerName().toLowerCase(),
                      PSServer.getListenerPort(), ah.getFullRequestRoot() + "/"
                      + ce.getRequestor().getRequestPage() + ".html");
          }
      }
      catch ( MalformedURLException mue )
      {
         throw new PSSystemValidationException( IPSServerErrors.RAW_DUMP,
               mue.getLocalizedMessage());
      }

      rowCtx.setPageInfoMap( m_pageInfo );
      rowCtx.setRequestUrl( url.toString());
      PSEditorDocumentContext summaryCtx = new PSEditorDocumentContext(
            ceh, ah.getApplicationDefinition(), ce );
      summaryCtx.setPageInfoMap( m_pageInfo );
      summaryCtx.setEditorType(
            PSEditorDocumentContext.EDITOR_TYPE_SUMMARY_EDITOR );
      summaryCtx.setRequestUrl( url.toString());

      initializeEditorBuilders( ce, dispMapper,
            PSQueryCommandHandler.ROOT_PARENT_PAGE_ID, rowCtx, summaryCtx );
   }


   /**
    * This method will set up the parameters for the
    * authenticateUser exit. This is called by the base classes
    * processRequest method before it does its work.
    *
    * @param request the request needed to make an internal request
    *  to get workflow communities. Assumed not <code>null</code>.
    * @throws PSInternalRequestCallException if the request to get
    *  the list of workflows for a community fails while processing.
    * @throws PSAuthorizationException if the user is not authorized to
    *  execute the query.
    * @throws PSAuthenticationFailedException if the user cannot be
    *  authenticated.
    *
    */
   public void preProcessRequest(PSRequest req)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      String contentId =
            req.getParameter( m_ceHandler.CONTENT_ID_PARAM_NAME );
      String condition;
      if ( null == contentId || contentId.trim().length() == 0 )
      {
         // new item - if the wf id isn't supplied, use the default
         int workflowId;
         if (m_ceHandler.getCmsObject().isWorkflowable()) 
            workflowId = PSCms.getDefaultWorkflowId(req, m_ce);
         else
            workflowId = IPSConstants.INVALID_WORKFLOW_ID;
         setWorkflowAppId(req, workflowId);
         condition = IPSConstants.CHECKINOUT_CONDITION_IGNORE;
      }
      else
         // editing existing item
         condition = IPSConstants.CHECKINOUT_CONDITION_CHECKOUT;
      PSCommandHandler.setCheckInOutCondition( req, condition );
   }

   /**
    * Returns the map of PSPageInfo objects keyed to the page id. This should
    * be treated as a read-only object by the caller as it is the actual map
    * used by this handler. Any changes to the object would affect the handler.
    * The value of the key of the primary page is always {@link
    * PSQueryCommandHandler#ROOT_PARENT_PAGE_ID}.
    *
    * @return A map containing PSPageInfo objects with their corresponding
    *    pageId as the key (as an Integer). Never empty.
    */
   public Map getPageMap()
   {
      return m_pageInfo;
   }


   // see base for description
   protected Iterator getAppList( int id, PSExecutionData data,
         boolean isNewDoc )
      throws PSDataExtractionException
   {
      if ( isNewDoc )
         return PSIteratorUtils.emptyIterator();

      // todo: cache these?
      PSPageInfo info = (PSPageInfo) m_pageInfo.get( new Integer( id ));
      Iterator datasetNames = info.getDatasetList();
      List handlers = new ArrayList();
      while ( datasetNames.hasNext())
      {
         String datasetName = (String) datasetNames.next();
         String reqName = createRequestName( m_appName, datasetName );
         IPSInternalResultHandler rh = (IPSInternalResultHandler)
            PSServer.getInternalRequestHandler( reqName );

         if ( null == rh )
         {
            throw new RuntimeException(
                  "Unexpected: Couldn't find handler for " + reqName );
         }
         handlers.add( rh );
      }

      return handlers.iterator();
   }


   // See base class for description.
   protected PSEditorDocumentBuilder getDocumentBuilder(
      int id,
      PSExecutionData data)
   {
      if (data == null) // this constrain is defined by base class
         throw new IllegalArgumentException("data may not be null");
         
      PSPageInfo info = (PSPageInfo) m_pageInfo.get( new Integer( id ));
      return null == info ? null : info.getBuilder();
   }


   /**
    * Creates all of the keys and their replacement values needed to query
    * the data for some child table. The root editor is associated with the
    * top level table. SDMP children have a table depth equal to the parent
    * table while all other child tables have a tableDepth 1 greater than
    * their parent.
    *
    * @param tableDepth Where in the hierarchy of tables is the table for
    *    which you want the key(s). The top table is 0. Must be greater
    *    than or equal to zero.
    *
    * @return A set where each element in the set is a Map.Entry, whose key is
    *    the backend column name and whose value is the HTML parameter name
    *    from which the key will be obtained. Never <code>null</code> or
    *    empty.
    *
    * @throws IllegalArgumentException if tableDepth is < 0.
    */
   private Set getQueryKeys( int tableDepth )
   {
      if ( tableDepth < 0 )
         throw new IllegalArgumentException( "table depth can't be negative" );

      HashMap keys = new HashMap();

      // children of children don't get these keys
      if ( tableDepth >= 0 && tableDepth <= 1 )
      {
         keys.put( IPSConstants.ITEM_PKEY_CONTENTID, m_ceHandler.
               getParamName( m_ceHandler.CONTENT_ID_PARAM_NAME ));
         keys.put( IPSConstants.ITEM_PKEY_REVISIONID, m_ceHandler.
               getParamName( m_ceHandler.REVISION_ID_PARAM_NAME ));
      }

      if ( tableDepth > 0 )
      {
         keys.put( IPSConstants.CHILD_ITEM_PKEY, m_ceHandler.
               getParamName( m_ceHandler.CHILD_ROW_ID_PARAM_NAME ));
      }

      return keys.entrySet();
   }


   /**
    * Creates a new mapper for each SDMP fieldset referenced by a mapping in
    * the supplied mapper. Each mapper contains all of the mappings for the
    * associated fieldset in the same order they appear in the supplied
    * mapper.
    *
    * @param fields A valid fieldset. Assumed not <code>null</code>.
    *
    * @param dispMapper A valid mapper. Assumed not <code>null</code>.
    *
    * @return A valid list containing 0 or more entries. Each entry is a
    *    PSDisplayMapper with 1 or more mappings.
    */
   private static List buildSdmpChildMappers( PSFieldSet fields,
         PSDisplayMapper dispMapper )
   {
      // This list contains 1 PSMapPair entry for each sdmp child
      List fieldSetMappers = new ArrayList();

      Iterator mappings = dispMapper.iterator();
      while ( mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         PSField sdmpField = fields.getChildField( mapping.getFieldRef(),
               fields.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
         if ( null != sdmpField )
         {
            PSFieldSet sdmpFieldSet = fields.getChildsFieldSet(
                  sdmpField.getSubmitName(),
                  fields.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
            PSDisplayMapper mapper = null;
            if ( !contains( fieldSetMappers, sdmpFieldSet ))
            {
               mapper = new PSDisplayMapper( sdmpFieldSet.getName());
               fieldSetMappers.add( new PSMapPair( sdmpFieldSet, mapper ));
            }
            else
            {
               mapper = (PSDisplayMapper) get( fieldSetMappers, sdmpFieldSet );
            }

            mapper.add( mapping );
         }
      }

      List childMappers = new ArrayList();
      Iterator mappers = fieldSetMappers.iterator();
      while ( mappers.hasNext())
      {
         PSMapPair entry = (PSMapPair) mappers.next();
         childMappers.add( entry.getValue());
      }
      return childMappers;
   }

   /**
    * Walks the list of objects, looking at the key of each one until
    * it finds key or there are no more entries in the list. It compares the
    * objects using the <code>equals</code> method.
    *
    * @param A valid list of 0 or more entries. Each entry is a PSMapPair.
    *    Assumed not <code>null</code>.
    *
    * @param kay A valid ref. Assumed not <code>null</code>.
    *
    * @return <code>true</code> if the list contains an entry whose key
    *    matches the supplied key.
    */
   private static boolean contains( List pairs, Object key )
   {
      boolean found = false;
      Iterator pairsIter = pairs.iterator();
      while ( pairsIter.hasNext() && !found )
      {
         PSMapPair entry = (PSMapPair) pairsIter.next();
         if ( entry.getKey().equals( key ))
            found = true;
      }
      return found;
   }

   /**
    * Walks the list of objects, looking at the key of each one until
    * it finds key or there are no more entries in the list. It compares the
    * objects using the <code>equals</code> method. The matching entry is
    * returned.
    *
    * @param A valid list of 0 or more entries. Each entry is a PSMapPair.
    *    Assumed not <code>null</code>.
    *
    * @param kay A valid ref. Assumed not <code>null</code>.
    *
    * @return The value associated with the entry whose key matches the
    *    supplied key.
    */
   private static Object get( List pairs, Object key )
   {
      Object value = null;
      Iterator pairsIter = pairs.iterator();
      while ( pairsIter.hasNext() && null == value )
      {
         PSMapPair entry = (PSMapPair) pairsIter.next();
         if ( entry.getKey().equals( key ))
            value = entry.getValue();
      }
      return value;
   }



   /**
    * Adds 2 builders for the supplied mapper, a row editor and a summary
    * editor builder and calls this method on any complex fields found in the
    * supplied mapper. The builder is added to the pageMapInfo list by looking
    * up the existing info and adding the builder to the info object. An
    * exception will be thrown if an existing page info object can't be found.
    * <p>This method uses the same algorithm as {@link
    * #initializeRequestResources(PSApplication, PSContentEditor,
    * PSDisplayMapper,int,int,HashMap,StringBuffer) initializeRequestResources}
    * for calculating page ids.
    *
    * @param ce The definition of the entire editor. It is used read only.
    *    Assumed not <code>null</code>.
    *
    * @param dispMapper A valid mapper. This method scans all the mappings
    *    and recursively calls this method for each complex child. Assumed
    *    not <code>null</code>.
    *
    * @param pageId The key that will be used when the builder is added
    *    to the pageMap. An entry with this key must already exist, the
    *    builder will be added to the object. This key must be supplied as the
    *    param to {@link #getDocumentBuilder(int) getDocumentBuilder} when
    *    requesting this builder at run time.
    *
    * @param rowCtx The doc context for the row editor. Assumed not <code>
    *    null</code>.
    *
    * @param summaryCtx The doc context for the summary editor. Assumed not
    *    <code>null</code>.
    *
    * @return An id that is 1 larger than any of the ids used while generating
    *    this builder or any of its child builders.
    *
    * @throws PSNotFoundException If an entry for a given pageId can't be
    *    found in pageMapInfo.
    */
   private int initializeEditorBuilders( PSContentEditor ce,
         PSDisplayMapper dispMapper, int pageId,
         PSEditorDocumentContext rowCtx, PSEditorDocumentContext summaryCtx )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      Map pageInfoMap = rowCtx.getPageInfoMap();
      updatePageInfo( pageInfoMap, pageId,
            new PSRowEditorDocumentBuilder( ce, rowCtx, dispMapper, pageId,
            false ));
      pageId++;

      PSFieldSet fields = ((PSContentEditorPipe) ce.getPipe()).getMapper().
            getFieldSet( dispMapper.getFieldSetRef());
      Iterator mappings = dispMapper.iterator();
      while ( mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fields.get( fieldName );
         if ( o instanceof PSFieldSet )
         {
            PSFieldSet fs = (PSFieldSet) o;
            if ( PSFieldSet.TYPE_COMPLEX_CHILD == fs.getType())
            {
               PSDisplayMapper childMapper = mapping.getDisplayMapper();
               updatePageInfo( pageInfoMap, pageId,
                     new PSSummaryEditorDocumentBuilder( ce, summaryCtx,
                     mapping, pageId, false ));

               //Hack alert: see COMPLETE_CHILD_PAGEID_OFFSET for details
               updatePageInfo( pageInfoMap, pageId+COMPLETE_CHILD_PAGEID_OFFSET,
                     new PSCompleteChildDocumentBuilder( ce, summaryCtx,
                     mapping, pageId+COMPLETE_CHILD_PAGEID_OFFSET, false ));

               pageId++;
               pageId = initializeEditorBuilders( ce, childMapper, pageId,
                     rowCtx, summaryCtx );
            }
         }
      }
      return pageId;
   }

   /**
    * Looks up a PSPageInfo object in the supplied map using the pageId as a
    * key and adds the supplied builder to it. If the object can't be found,
    * an exception is thrown.
    *
    * @param pageInfo A map containing 1 or more entries, each entry should
    *    have a key that is an Integer of the page id for the value, which is
    *    a PSPageInfo object. Assumed not <code>null</code>.
    *
    * @param pageId The key to one of the elements in the supplied map.
    *
    * @param builder The builder to add to the page info. Assumed not <code>
    *    null</code>.
    *
    * @throws PSNotFoundException If a page info can't be found using the
    *    supplied id.
    */
   private static void updatePageInfo( Map pageInfo, int pageId,
         PSEditorDocumentBuilder builder )
      throws PSNotFoundException
   {
      Integer key = new Integer(pageId);
      PSPageInfo info = (PSPageInfo) pageInfo.get(key);
      if ( null == info )
      {
         String [] args =
         {
            "page",
            key.toString()
         };
         throw new PSNotFoundException(
               IPSServerErrors.CE_MISSING_PAGEMAP_ENTRY, args );
      }
      info.setBuilder( builder );
   }


   /**
    * This method does a lot of prep work including the following:
    * <ol>
    *    <li>Creates a dataset for the supplied mapper and all summary views
    *       in the document. The names of all of the datasets needed by the
    *       row editor created by this page are stored in a PSPageInfo object
    *       in the supplied pageInfoMap, using the supplied pageId as the
    *       key.</li>
    *    <li>Creates a map that lists all of the page ids referenced by this
    *       row editor. There are 0 or more pages that could be referenced.</li>
    *    <li></li>
    * </ol>
    * This method is recursive, as it scans the mapper, if it finds a
    * PSFieldSet for a complex child, this method is called to process that
    * child.
    * <p>Page ids follow a pattern. The mapper is processed in document order,
    * as each complex child is reached, the current id is assigned to the
    * summary editor for that child, then that child is processed by calling
    * this method again, incrementing the id as necessary for the grand
    * children. If no page id is included in the query, the parent
    * id is assumed (which is the id passed to the first invocation of this
    * method).
    * <p>The list of dataset names built for this editor (which are added to
    * the page map) is in the following order:
    * <ol>
    *    <li>Resource to get result set for this editor</li>
    *    <li>1 resource for each SDMP child in this editor, in doc order
    *       according to their first appearance</li>
    *    <li>1 resource for each complex child, in doc order</li>
    * </ol>
    *
    * @param app The app in which the datasets will be built. Assumed not
    *    <code>null</code>.
    *
    * @param ce The definition of the entire editor. It is used read only.
    *    Assumed not <code>null</code>.
    *
    * @param dispMapper The mapper with the editor for which this method will
    *    initialize the datasets and other information.
    *
    * @param recursionDepth How deeply we have recursed while processing the
    *    editors. 0 should be passed in the first time this method is called.
    *    Each time this method calls itself, a value 1 greater than the value
    *    passed in will be passed to the next call. This value is used to
    *    determine what keys are needed for the query.
    *
    * @param pageId The next available id to use for identifying pages. The
    *    value passed in and all values above it should be free for use. The
    *    largest value used by this method and any recursive calls is
    *    incremented and returned. The datasetName returned in
    *    resultDatasetName has a pageId equal the the passed in id.
    *
    * @param pageInfoMap A PSPageInfo object is added to the list for the
    *    editor pages created by this method, with the key being the pageId
    *    and the value being the PSPageInfo object. Assumed not <code>null
    *    </code>.
    *
    * @param resultDatasetName The name of the dataset created by this call.
    *    The buffer is cleared before the name is set in the buffer. Assumed
    *    not <code>null</code>.
    *
    * @return A value one larger than the largest id used by all recursive
    *    calls to this method, i.e. it can be used as a new id as is.
    *
    * @throws PSSystemValidationException If anything used by this method is missing
    *    or misconfigured.
    */
   private int initializeRequestResources( PSApplication app,
         PSContentEditor ce, PSDisplayMapper dispMapper, int recursionDepth,
         int pageId, HashMap pageInfoMap, StringBuffer resultDatasetName )
      throws PSSystemValidationException
   {
      PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
      PSFieldSet fields =
            pipe.getMapper().getFieldSet(dispMapper.getFieldSetRef());

      Iterator auxMappings = PSIteratorUtils.emptyIterator();
      Iterator sortedCols = PSIteratorUtils.emptyIterator();
      if ( pageId != ROOT_PARENT_PAGE_ID )
      {
         // need to add the row id to the result set for children
         Map extraMappings = new HashMap();
         String colName = IPSConstants.CHILD_ITEM_PKEY;
         extraMappings.put( colName, colName );
         auxMappings = extraMappings.entrySet().iterator();

         // we need to sort children by their sort key
         if ( fields.isSequencingSupported()) //if specified, use sort rank
            colName = IPSConstants.CHILD_SORT_KEY;
         else //otherwise use sysid as default
            colName = IPSConstants.CHILD_ITEM_PKEY;
         List sortCols = new ArrayList();
         sortCols.add( colName );
         sortedCols = sortCols.iterator();
      }

      Set keys = getQueryKeys( recursionDepth );
      String datasetName = PSApplicationBuilder.createQueryDataset( app, ce,
            dispMapper, keys.iterator(), auxMappings, sortedCols );
      resultDatasetName.delete( 0, resultDatasetName.length());
      resultDatasetName.append( datasetName );

      List datasetNames = new ArrayList();
      datasetNames.add( datasetName );

      // add all of the SDMP child tables next
      List sdmpMappers = buildSdmpChildMappers( fields, dispMapper );
      Iterator sdmpMapperIter = sdmpMappers.iterator();
      while ( sdmpMapperIter.hasNext())
      {
         String resourceName = PSApplicationBuilder.createQueryDataset( app, ce,
         (PSDisplayMapper) sdmpMapperIter.next(), keys.iterator(),
         auxMappings, PSIteratorUtils.emptyIterator());
         
         // sdmp mapper could result in no fields mapped in dataset, in which
         // case no resource is created an null is returned.
         if (resourceName != null)
            datasetNames.add(resourceName);
      }

      List pageMap = new ArrayList();

      int nextPageId = pageId + 1;
      Iterator mappings = dispMapper.iterator();
      StringBuffer resultName = new StringBuffer(100);
      while ( mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fields.get( fieldName );
         if ( o instanceof PSFieldSet )
         {
            PSFieldSet fs = (PSFieldSet) o;
            PSDisplayMapper childMapper = mapping.getDisplayMapper();
            if ( null == childMapper )
            {
               String [] args =
               {
                  fields.getName(),
                  fs.getName()
               };
               throw new PSSystemValidationException(
                     IPSServerErrors.CE_MISSING_CHILDMAPPER, args );
            }
            if ( PSFieldSet.TYPE_COMPLEX_CHILD == fs.getType())
            {
               /* we need to add a page for the summary editor and recursively
                  process for the row editor */
               int summaryPageId = nextPageId++;

               pageMap.add( new Integer( summaryPageId ));
               pageMap.add( new Integer( nextPageId ));
               nextPageId = initializeRequestResources( app, ce, childMapper,
                     recursionDepth+1, nextPageId, pageInfoMap, resultName );
               datasetNames.add( resultName.toString());

               /* we must do the summary editor last, because it uses the
                  dataset generated by the row editor  */
               List summaryDSName = new ArrayList(1);
               summaryDSName.add( resultName.toString());
               List summaryPageMap = new ArrayList(1);
               summaryPageMap.add( new Integer( summaryPageId+1 ));
               pageInfoMap.put( new Integer( summaryPageId ),
                     new PSPageInfo( PSPageInfo.TYPE_SUMMARY_EDITOR,
                     childMapper.getId(), summaryDSName, summaryPageMap ));

               //Hack alert: see COMPLETE_CHILD_PAGEID_OFFSET for details
               pageInfoMap.put(
                     new Integer( summaryPageId+COMPLETE_CHILD_PAGEID_OFFSET ),
                     new PSPageInfo( PSPageInfo.TYPE_SUMMARY_DATA,
                     childMapper.getId(), summaryDSName, summaryPageMap ));
            }
            else
            {
               // just need to add query for the column
               keys = getQueryKeys( recursionDepth+1 );
               datasetNames.add( PSApplicationBuilder.createQueryDataset(
                     app, ce, childMapper, keys.iterator(), auxMappings,
                     sortedCols ));
            }
         }
      }
      pageInfoMap.put( new Integer( pageId ),
            new PSPageInfo( PSPageInfo.TYPE_ROW_EDITOR, dispMapper.getId(),
            datasetNames, pageMap ));

      return nextPageId;
   }



   /**
    * Contains a PSPageInfo object for every editor built by this handler.
    * The info is keyed by the pageid of the editor. Never <code>null</code>.
    * Never empty after construction.
    */
   private HashMap m_pageInfo = new HashMap();

   /**
    * The name of the application that contains all of the datasets
    * stored in the PSPageInfo values within the m_pageInfo map. Never empty
    * after construction.
    */
   private String m_appName;

   /**
    * This is a hack. For web services, we need a way to get all children 
    * regardless of showInSummary and showInPreview flags.  We do this by 
    * creating another page for every summary  editor. This pageid is the 
    * summary page id + the value of this constant (1000).
    */  
   public static final int COMPLETE_CHILD_PAGEID_OFFSET = 1000;   
}



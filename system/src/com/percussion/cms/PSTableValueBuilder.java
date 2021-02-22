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
package com.percussion.cms;

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSEditCommandHandler;
import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.extension.PSExtensionException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSMapPair;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates the DisplayField element according to the ContentEditor.dtd when
 * the data is an entire result set which contains multiple columns and
 * multiple rows. Behavior will vary depending on whether the context is
 * a row editor or summary editor and whether there is currently any data in
 * the table being displayed with this builder.
 */
public class PSTableValueBuilder extends PSDisplayFieldBuilder
{
   /**
    * Convenience method. Same as calling {@link #PSTableValueBuilder(
    * PSFieldSet,PSUISet,PSDisplayMapper,PSEditorDocumentBuilder,boolean)
    * PSTableValueBuilder(fieldSet, ui, mapper, parentBuilder, false)}.
    */
   public PSTableValueBuilder( PSFieldSet fieldSet, PSUISet ui,
         PSDisplayMapper mapper, PSEditorDocumentBuilder parentBuilder )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      this(fieldSet, ui, mapper, parentBuilder, false);
   }

   /**
    * Creates a DisplayField builder that creates a table view. All fields
    * referenced in the mapper which are not binary and which the designer
    * has not restricted from this view are allowed. The table headings are
    * labeled the same as the field in the row editor and the order from
    * left to right matches the order from top to bottom in the row editor
    * for the same fieldset.
    * <p>See {@link PSDisplayFieldBuilder#PSDisplayFieldBuilder(PSFieldSet,
    * PSUISet, PSEditorDocumentBuilder) base} class for description of fieldSet and ui and their
    * requirements.
    *
    * @param mapper A list of mappings used to determine the column headings
    *    and display order for this table view.
    *
    * @param parentBuilder This builder is always a single row in a larger
    *    document. The larger document is managed by this parentBuilder.
    *
    * @param showAllFields This flag overrides the showInSummary and
    *    showInPreview properties. If <code>true</code>, these properties are
    *    ignored, otherwise they are honored.
    */
   public PSTableValueBuilder( PSFieldSet fieldSet, PSUISet ui,
         PSDisplayMapper mapper, PSEditorDocumentBuilder parentBuilder,
         boolean showAllFields )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( fieldSet, ui, parentBuilder );
      if ( null == mapper || null == parentBuilder )
         throw new IllegalArgumentException( "Mapper is null." );

      m_supportsSequencing = fieldSet.isSequencingSupported();
      m_parentBuilder = parentBuilder;
      PSEditorDocumentContext ctx = m_parentBuilder.getDocContext();
      Iterator mappings = mapper.iterator();
      if ( !mappings.hasNext())
      {
         throw new PSSystemValidationException( IPSServerErrors.CE_MISSING_MAPPINGS,
               fieldSet.getName());
      }
      m_showAllFields = showAllFields;
      m_fieldSetName = fieldSet.getName();
      m_myId = mapper.getId();
      Iterator pages = ctx.getPageInfoList( m_myId );
      while ( pages.hasNext())
      {
         Map.Entry entry = (Map.Entry) pages.next();
         PSPageInfo info = (PSPageInfo) entry.getValue();
         if ( info.isSummaryEditor())
         {
            m_parentId =
                  ctx.getParentPageId(((Integer) entry.getKey()).intValue());
            break;
         }
      }

      PSBackEndColumn column = null;
      while ( mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldRef = mapping.getFieldRef();
         Object o = fieldSet.get( fieldRef );
         // check for sdmp child
         if ( null == o )
         {
            o = fieldSet.getChildField( fieldRef,
                  PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
         }
         String label = "unlabeled";
         if ( null != mapping.getUISet().getLabel())
            label = mapping.getUISet().getLabel().getText();

         if ( null == o )
         {
            String [] params =
            {
               fieldRef,
               label,
            };
            throw new PSSystemValidationException( IPSServerErrors.CE_MISSING_FIELD,
                   params );
         }

         if ( !( o instanceof PSField ))
            continue;

         PSField field = (PSField) o;
         if (!m_showAllFields && (( !field.isShowInPreview() && !ctx.isEditMode())
               || !field.isShowInSummary()))
         {
            continue;
         }

         IPSBackEndMapping locator = field.getLocator();
         if ( !( locator instanceof IPSReplacementValue ))
         {
            throw new PSSystemValidationException(
                  IPSServerErrors.CE_UNSUPPORTED_MAPPING_TYPE,
                  field.getSubmitName());
         }

         if ( null == column && locator instanceof PSBackEndColumn )
            column = (PSBackEndColumn) locator;

         PSDisplayFieldBuilder builder = null;

         if ( parentBuilder.isBinaryField( field ))
         {
            builder = new PSDisplayFieldBuilder( field,
                  mapping.getUISet(), parentBuilder );
         }
         else
         {
            builder = new PSSingleValueBuilder( field, mapping.getUISet(),
               locator, parentBuilder );
         }

         builder.setReadOnly( true );
         m_builders.add( builder );
         m_columnHeaders.add( label );
         m_fieldNames.add( field.getSubmitName());
      }

      // build extractors needed for button params
      String [][] paramInfo =
      {
         {
            ctx.getSystemParam( PSContentEditorHandler.CONTENT_ID_PARAM_NAME ),
            CONTENT_ID_EXTRACTOR_KEY
         },
         {
            ctx.getSystemParam( PSContentEditorHandler.REVISION_ID_PARAM_NAME),
            REVISION_ID_EXTRACTOR_KEY
         },
         {
            ctx.getSystemParam( PSContentEditorHandler.CHILD_ID_PARAM_NAME ),
            CHILD_ID_EXTRACTOR_KEY
         },
         {
            ctx.getSystemParam( PSContentEditorHandler.PAGE_ID_PARAM_NAME ),
            PAGE_ID_EXTRACTOR_KEY
         }
      };
      Iterator params = PSIteratorUtils.iterator( paramInfo );
      String controlName =
            ctx.getInitParam( IPSConstants.HIDDEN_CONTROL_PARAM_NAME );
      while ( params.hasNext())
      {
         String [] paramPair = (String []) params.next();
         PSHtmlParameter value = new PSHtmlParameter( paramPair[0] );
         Map tmp = new HashMap();
         try
         {
            tmp.put( paramPair[0], PSDataExtractorFactory
                  .createReplacementValueExtractor( value ));
         }
         catch ( IllegalArgumentException e )
         {
            throw new IllegalArgumentException( e.getLocalizedMessage());
         }
         m_paramExtractors.put( paramPair[1],
               (Map.Entry) tmp.entrySet().iterator().next());
      }

      if ( null == column )
      {
         throw new PSSystemValidationException(
               IPSServerErrors.CE_BACKEND_COL_REQUIRED );
      }

      // add an extractor for the primary key
      try
      {
         PSBackEndColumn col = new PSBackEndColumn( column.getTable(),
               IPSConstants.CHILD_ITEM_PKEY );
         Map tmp = new HashMap();
         tmp.put( ctx.getSystemParam(
               PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME ),
               PSDataExtractorFactory.createReplacementValueExtractor( col ));
         m_paramExtractors.put( ROW_ID_EXTRACTOR_KEY,
               (Map.Entry) tmp.entrySet().iterator().next());
      }
      catch ( IllegalArgumentException e )
      {
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }

      // did the user specify any buttons for the row actions?
      PSContentEditorSystemDef def = PSServer.getContentEditorSystemDef();
      Map allParams = def.getInitParams();
      List cmdParams = (List) allParams.get(
            PSEditCommandHandler.COMMAND_NAME );
      Object [][] vals =
      {
         { new Integer(MODIFY_INDEX), "com.percussion.rowModifyImage" },
         { new Integer(DELETE_INDEX), "com.percussion.rowDeleteImage" },
         { new Integer(MOVEUP_INDEX), "com.percussion.rowMoveUpImage" },
         { new Integer(MOVEDOWN_INDEX), "com.percussion.rowMoveDownImage" },
      };

      for ( int i = 0; i < vals.length; i++ )
      {
         Iterator paramIter = cmdParams.iterator();
         String location = null;
         while ( paramIter.hasNext() && null == location )
         {
            PSParam p = (PSParam) paramIter.next();
            if ( p.getName().equalsIgnoreCase((String) vals[i][1]))
               location = p.toString();
            if ( null != location && location.trim().length() == 0 )
               location = null;
         }
         m_imageLocations[((Integer) vals[i][0]).intValue()] = location;
      }
   }

   // see base for desc
   protected boolean addDataElement( Document doc, Element parent,
         PSExecutionData data, boolean isNewDoc )
      throws PSDataExtractionException
   {
      if ( null == doc || null == parent )
      {
         throw new IllegalArgumentException(
               "Document or parent element was null." );
      }

      try
      {
         // create root
         Element tableElem = doc.createElement( TABLE_NAME );

         // create header nodes
         Iterator headers = m_columnHeaders.iterator();
         if ( headers.hasNext())
         {
            Element headerElem = doc.createElement( HEADER_NAME );
            tableElem.appendChild( headerElem );

            while ( headers.hasNext())
            {
               Element colElem = doc.createElement( HEADER_COLUMN_NAME );
               colElem.appendChild(
                     doc.createTextNode( headers.next().toString()));
               headerElem.appendChild( colElem );
            }
         }

         boolean addedRow = false;

         if ( null != data && !isNewDoc )
         {
            try
            {
               data.saveResultSetContext();
               if ( null == data.getNextResultSet())
               {
                  throw new PSDataExtractionException(
                        IPSServerErrors.CE_MISSING_RESULTSET, m_fieldSetName );
               }

               Element rowSetElem = doc.createElement( ROWSET_NAME );
               tableElem.appendChild( rowSetElem );
               while ( data.readRow())
               {
                  addedRow = true;
                  Map.Entry entry = (Map.Entry)
                        m_paramExtractors.get( ROW_ID_EXTRACTOR_KEY );
                  IPSDataExtractor extractor =
                        (IPSDataExtractor) entry.getValue();
                  Object rowId = extractor.extract( data );
                  if ( null == rowId )
                  {
                     throw new PSDataExtractionException(
                           IPSServerErrors.CE_MISSING_RESULTSET,
                           m_fieldSetName );
                  }
                  Element rowElem = doc.createElement( ROW_NAME );
                  rowElem.setAttribute(PSEditorDocumentBuilder.CHILDKEY_ATTRIB,
                     rowId.toString());
                  rowSetElem.appendChild( rowElem );
                  Iterator fieldNames = m_fieldNames.iterator();
                  Iterator builders = m_builders.iterator();
                  while ( builders.hasNext())
                  {
                     PSDisplayFieldBuilder builder =
                           (PSDisplayFieldBuilder) builders.next();
                     Element colElem = doc.createElement( COLUMN_NAME );
                     rowElem.appendChild( colElem );
                     Element control = builder.createControlNode(doc, data,
                           isNewDoc );
                     colElem.appendChild( control );
                  }
                  if ( m_parentBuilder.getDocContext().isSummaryEditor())
                  {
                     Element actionsElem = doc.createElement( ACTIONSET_NAME );
                     rowElem.appendChild( actionsElem );
                     addRowEditActions( doc, actionsElem, data,
                           rowId.toString());
                  }
               }
            }
            finally
            {
               /* I don't think this is really necessary to be in a finally
                  block because processing should stop, but just in case
                  behavior changes in the future */
               data.restoreResultSetContext();
            }
         }

         // add hidden fields needed by all buttons
         Element hiddenFields = doc.createElement( HIDDENFIELDS_NAME );
         /* Currently, there are no common fields
         if ( addHiddenFields( doc, hiddenFields, data, new String[0] ))
            tableElem.appendChild( hiddenFields );
         */

         if ( m_parentBuilder.getDocContext().isRowEditor()
               && m_parentBuilder.getDocContext().isEditMode())
         {
            Element actionSetElem = doc.createElement( ACTIONSET_NAME );
            tableElem.appendChild( actionSetElem );
            addSummaryViewActions( doc, actionSetElem, data, addedRow );
         }

         parent.appendChild( tableElem );
         return addedRow;
      }
      catch ( SQLException e )
      {
         StringBuffer buf = new StringBuffer(250);    // arbitrary size
         buf.append( System.getProperty( "line.separator" ));
         buf.append( e.getLocalizedMessage());
         SQLException next = e.getNextException();
         while ( null != next )
         {
            buf.append( System.getProperty( "line.separator" ));
            buf.append( next.getLocalizedMessage());
            next = next.getNextException();
         }
         throw new PSDataExtractionException( IPSServerErrors.CE_SQL_ERRORS,
               buf.toString());
      }
   }


   /**
    * Creates the hidden elements that will become hidden form parameters.
    *
    * @param doc The document that will eventually contain the nodes. Assumed
    *   not <code>null</code>.
    *
    * @param parent The generated elements will be added to this one. Assumed
    *   not <code>null</code>.
    *
    * @param data The data used to get the values for the nodes. Assumed
    *   not <code>null</code>.
    *
    * @param keyNames Contains the keys for extractors which you want to
    *    add as hidden fields. Assumed none of the entries are empty.
    *
    * @return <code>true</code> if any node was added to parent, <code>false
    *   </code> otherwise.
    *
    * @throws PSDataExtractionException If any problems occur while getting
    *    needed values from the execution data.
    */
   private boolean addHiddenFields( Document doc, Element parent,
         PSExecutionData data, String [] keyNames )
      throws PSDataExtractionException
   {
      boolean addedField = false;

      Map params = new HashMap();
      Iterator keys = PSIteratorUtils.iterator( keyNames );
      String pageParam = null;
      while ( keys.hasNext())
      {
         String key = (String) keys.next();
         Map.Entry entry = (Map.Entry) m_paramExtractors.get( key );
         if ( null == entry )
         {
            /* if this happens it is a coding flaw, added key name w/o
               creating the extractor in the ctor */
            throw new IllegalArgumentException(
                  "extractor not found for: " + key );
         }
         IPSDataExtractor extractor = (IPSDataExtractor) entry.getValue();
         Object o = extractor.extract( data );
         if ( null != o )
         {
            String paramValue = o.toString();
            if ( null != paramValue && paramValue.trim().length() > 0 )
               params.put((String) entry.getKey(), paramValue );
         }
      }

      Iterator outputParams = params.entrySet().iterator();
      while ( outputParams.hasNext())
      {
         Map.Entry entry = (Map.Entry) outputParams.next();
         Element paramNode = doc.createElement( PARAM_NAME );
         parent.appendChild( paramNode );
         paramNode.appendChild( doc.createTextNode((String) entry.getValue()));
         paramNode.setAttribute( NAME_NAME, (String) entry.getKey());
         addedField = true;
      }
      return addedField;
    }


   /**
    * Creates the ActionLink elements for the summary editor. Each row in the
    * summary editor can have several actions, such as delete, move up, move
    * down and modify.
    *
    * @param doc The document that will eventually contain the nodes. Assumed
    *   not <code>null</code>.
    *
    * @param parent The generated elements will be added to this one. Assumed
    *   not <code>null</code>.
    *
    * @param data The data used to get the values for the nodes. Assumed
    *   not <code>null</code>.
    *
    * @param rowId The unique identifier for the table row which these
    *   actions will work upon.
    *
    * @return <code>true</code> if any node was added to parent, <code>false
    *   </code> otherwise.
    *
    * @throws PSDataExtractionException If any problems occur while getting
    *    needed values from the execution data.
    */
   private boolean addRowEditActions( Document doc, Element parent,
         PSExecutionData data, String rowId )
      throws PSDataExtractionException
   {
      try
      {
         PSEditorDocumentContext docContext = m_parentBuilder.getDocContext();
         String commandParamName = docContext.
               getSystemParam( PSContentEditorHandler.COMMAND_PARAM_NAME );
         String rowIdParamName = docContext.
               getSystemParam( PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME );
         String dbActionType = docContext.getRequestTypeHtmlParamName();
         String childIdParamName = docContext.
               getSystemParam( PSContentEditorHandler.CHILD_ID_PARAM_NAME );
         String pageIdParamName = docContext.
               getSystemParam( PSContentEditorHandler.PAGE_ID_PARAM_NAME );
         String modifyCmdName = PSModifyCommandHandler.COMMAND_NAME;

         List params = new ArrayList();
         String pageId = ""+m_parentBuilder.getPageId();
         String childId = ""+docContext.getChildId( m_parentBuilder.getPageId());

         String view = getViewEvaluator().getNextView(data,
            m_parentBuilder.getPageId());

         // Delete button
         params.add( new PSMapPair( commandParamName, modifyCmdName ));
         params.add( new PSMapPair( rowIdParamName, rowId ));
         params.add( new PSMapPair( pageIdParamName, pageId ));
         params.add( new PSMapPair( childIdParamName, childId ));
         params.add( new PSMapPair( dbActionType,
               docContext.getRequestTypeValueDelete()));
         params.add( new PSMapPair(IPSHtmlParameters.SYS_VIEW, view));
         Element elem = m_parentBuilder.createActionElement( doc, "Del",
               params.iterator(), true );
         parent.appendChild( elem );
         if ( m_imageLocations[DELETE_INDEX] != null )
         {
            elem.setAttribute( IMAGEREF_ATTRIB, m_imageLocations[DELETE_INDEX]);
         }

         // edit button
         params.clear();
         params.add( new PSMapPair( commandParamName,
               PSEditCommandHandler.COMMAND_NAME ));
         params.add( new PSMapPair( rowIdParamName, rowId ));
         PSPageInfo info = (PSPageInfo) docContext.getPageInfoMap().
               get( new Integer( m_parentBuilder.getPageId()));
         params.add( new PSMapPair( pageIdParamName,
               info.getPageIdList().next().toString()));
         params.add( new PSMapPair(IPSHtmlParameters.SYS_VIEW, view));
         elem = m_parentBuilder.createActionElement( doc, "Edit",
               params.iterator(), true );
         parent.appendChild( elem );
         if ( m_imageLocations[MODIFY_INDEX] != null )
         {
            elem.setAttribute( IMAGEREF_ATTRIB, m_imageLocations[MODIFY_INDEX]);
         }

         if ( m_supportsSequencing )
         {
            /* move up button (assuming rows are sorted in ascending order,
               this is done when the app is built) */
            //todo: figure out what is needed for these 2 buttons
            params.clear();
            params.add( new PSMapPair( commandParamName, modifyCmdName ));
            params.add( new PSMapPair( dbActionType,
                  PSContentEditorHandler.DB_ACTION_SEQUENCE_DECREMENT ));
            params.add( new PSMapPair( rowIdParamName, rowId ));
            params.add( new PSMapPair( pageIdParamName, pageId ));
            params.add( new PSMapPair( childIdParamName, childId ));
            params.add( new PSMapPair(IPSHtmlParameters.SYS_VIEW, view));
            elem = m_parentBuilder.createActionElement( doc, "Up",
                  params.iterator(), true );
            parent.appendChild( elem );
            if ( m_imageLocations[MOVEUP_INDEX] != null )
            {
               elem.setAttribute( IMAGEREF_ATTRIB,
                     m_imageLocations[MOVEUP_INDEX]);
            }

            // move down button
            params.clear();
            params.add( new PSMapPair( commandParamName, modifyCmdName ));
            params.add( new PSMapPair( dbActionType,
                  PSContentEditorHandler.DB_ACTION_SEQUENCE_INCREMENT ));
            params.add( new PSMapPair( rowIdParamName, rowId ));
            params.add( new PSMapPair( pageIdParamName, pageId ));
            params.add( new PSMapPair( childIdParamName, childId ));
            params.add( new PSMapPair(IPSHtmlParameters.SYS_VIEW, view));
            elem = m_parentBuilder.createActionElement( doc, "Dn",
                  params.iterator(), true );
            parent.appendChild( elem );
            if ( m_imageLocations[MOVEDOWN_INDEX] != null )
            {
               elem.setAttribute( IMAGEREF_ATTRIB,
                     m_imageLocations[MOVEDOWN_INDEX]);
            }
         }
         return true;
      }
      catch ( PSNotFoundException nfe )
      {
         throw new PSDataExtractionException( nfe.getErrorCode(),
               nfe.getErrorArguments());
      }
   }


   /**
    * Creates the ActionLink elements for the summary view. The summary view
    * is one of the child table views used when building a row editor. Any
    * custom actions are properly handled.
    *
    * @param doc The document that will eventually contain the nodes. Assumed
    *   not <code>null</code>.
    *
    * @param parent The generated elements will be added to this one. Assumed
    *   not <code>null</code>.
    *
    * @param data The data used to get the values for the nodes. Assumed
    *   not <code>null</code>.
    *
    * @param hasData A flag to indicate whether this table has any rows.
    *    <code>true</code> indicates 1 or more rows are present. The text of
    *    the button and the redirect differ between these 2 states.
    *
    * @return <code>true</code> if any node was added to parent, <code>false
    *   </code> otherwise.
    *
    * @throws PSDataExtractionException If any problems occur while getting
    *    needed values from the execution data.
    */
   private boolean addSummaryViewActions( Document doc, Element parent,
         PSExecutionData data, boolean hasData )
      throws PSDataExtractionException
   {
      List params = new ArrayList();

      PSEditorDocumentContext docContext = m_parentBuilder.getDocContext();

      int pageId = -99;    // id of target page
      boolean found = false;
      Iterator possibleTargets = docContext.getPageInfoList( m_myId );
      PSSummaryEditorDocumentBuilder summaryBuilder = null;
      String lang = m_parentBuilder.getUserLocaleString(data);
      int summaryChildId = -1;
      while ( possibleTargets.hasNext())
      {
         Map.Entry entry = (Map.Entry) possibleTargets.next();
         PSPageInfo info = (PSPageInfo) entry.getValue();
         if ( info.getBuilder() instanceof PSSummaryEditorDocumentBuilder )
         {
            summaryBuilder = (PSSummaryEditorDocumentBuilder) info.getBuilder();
            summaryChildId = info.getChildId();
         }
         if ( !found && (( info.isRowEditor() && !hasData )
               || ( info.isSummaryEditor() && hasData )))
         {
            found = true;
            pageId = ((Integer) entry.getKey()).intValue();
         }
      }

      boolean isNewDoc = m_parentBuilder.isNewDocument( data,
            m_parentBuilder.getDocContext().isRowEditor());

      if ( !hasData && null == summaryBuilder )
      {
         throw new RuntimeException( "Summary editor unexpectedly missing" );
      }
      else if ( !hasData )
      {
         // get our actions from the summary editor, which is the action we
         // want to duplicate if we have no data
         Iterator actionSet = summaryBuilder.getSubmitActions();
         if ( !actionSet.hasNext())
            return false;

         /* The values for some of the params coming from the summary editor
            are not what we want. We will override those values. To do that,
            build a list of the undesirable ones and discard them when
            building the param list. */
         Map unwantedParams = new HashMap();
         unwantedParams.put( docContext.getSystemParam(
            PSContentEditorHandler.COMMAND_PARAM_NAME ), null );
         unwantedParams.put( docContext.getSystemParam(
            PSEditorDocumentBuilder.FORMACTION_NAME ), null );
         // the following params will be re-added by the addLocalParams method
         unwantedParams.put( docContext.getSystemParam(
            PSContentEditorHandler.CHILD_ID_PARAM_NAME ), null );
         unwantedParams.put( docContext.getSystemParam(
            PSContentEditorHandler.PAGE_ID_PARAM_NAME ), null );
         unwantedParams.put( docContext.getSystemParam(
            IPSHtmlParameters.SYS_VIEW ), null );

         while ( actionSet.hasNext())
         {
            PSMapPair actionEntry = (PSMapPair) actionSet.next();
            String label = (String) actionEntry.getKey();
            List extractorPairs = (List) actionEntry.getValue();
            Iterator extractors = extractorPairs.iterator();
            while ( extractors.hasNext())
            {
               PSMapPair paramEntry = (PSMapPair) extractors.next();
               String name = (String) paramEntry.getKey();
               if ( !unwantedParams.containsKey( name ))
               {
                  IPSDataExtractor extractor =
                        (IPSDataExtractor) paramEntry.getValue();
                  params.add( new PSMapPair(
                        name, extractor.extract( data ).toString()));
               }
            }
            addLocalParams( isNewDoc, pageId, docContext, data, params );
            //if action label is ADDITEM_ACTION_LABEL then
            //get the localized string
            if(label.equalsIgnoreCase(summaryBuilder.ADDITEM_ACTION_LABEL))
            {
               label = PSI18nUtils.getString(PSI18nUtils.PSX_CE_ACTION +
                  PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST +
                  summaryBuilder.ADDITEM_ACTION_LABEL, lang);
            }
            parent.appendChild( PSEditorDocumentBuilder.createActionElement(
                  doc, label, params.iterator(), true ));

            params.clear();   // get ready for next iteration
         }
         return true;
      }

      // all other needed params are in the main form
      addLocalParams( isNewDoc, pageId, docContext, data, params );

      //Get localized string for form submit label Edit Table
      String actionLabel = PSI18nUtils.getString(PSI18nUtils.PSX_CE_ACTION +
         PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST +
         FORMSUBMIT_LABEL , lang);

      parent.appendChild(
            PSEditorDocumentBuilder.createActionElement( doc, actionLabel,
            params.iterator(), true ));
      return true;
   }


   /**
    * Adds all the parameters needed for the submit button on this form so
    * it submits to the modify handler first. The following params are added:
    * <ul>
    *    <li>sys_pageid - passed in value</li>
    *    <li>DBActionType - insert or update based on new doc status</li>
    *    <li>sys_childid - mapper id of the associated table</li>
    *    <li>sys_contentid - contentid of this item, only present if not
    *       empty.</li>
    *    <li>sys_revision - revision id of this item, only present if not
    *       empty.</li>
    *    <li>sys_view - the view to use for the next page displayed</li>
    * </ul>
    *
    * @param isNewDoc A flag to indicate whether this request is for a new
    *    document versus editing existing content.
    *
    * @param pageId The page id of the target editor (either a summary or row
    *    editor).
    *
    * @param docContext The document context for this object. Assumed not
    *    <code>null</code>.
    *
    * @param data The data used to get the values for the nodes. Assumed
    *   not <code>null</code>.
    *
    * @param params All created param pairs will be added to this list as
    *    PSMapPair objects, with the key as the parameter name and the value
    *    as the parameter value (could be <code>null</code>). Assumed not
    *    <code>null</code>.
    */
   private void addLocalParams( boolean isNewDoc, int pageId,
         PSEditorDocumentContext docContext, PSExecutionData data, List params )
      throws PSDataExtractionException
   {
      params.add( new PSMapPair( docContext.getSystemParam(
            PSContentEditorHandler.PAGE_ID_PARAM_NAME ), ""+pageId ));

      String actionType = isNewDoc ? docContext.getRequestTypeValueInsert() :
            docContext.getRequestTypeValueUpdate();
      params.add( new PSMapPair( docContext.getRequestTypeHtmlParamName(),
            actionType ));

      params.add( new PSMapPair( docContext.
           getSystemParam( PSContentEditorHandler.CHILD_ID_PARAM_NAME ),
            ""+m_parentId ));

      // we only want to add keys (contentid & revision) if they have values
      Object o = m_parentBuilder.getExtractor(
            m_parentBuilder.CONTENT_ID_EXTRACTOR_KEY ).extract( data );
      if ( null != o && o.toString().trim().length() > 0 )
      {
         params.add( new PSMapPair( docContext.
             getSystemParam( PSContentEditorHandler.CONTENT_ID_PARAM_NAME ),
            o.toString()));
      }

      o = m_parentBuilder.getExtractor(
            m_parentBuilder.REVISION_ID_EXTRACTOR_KEY ).extract( data );
      if ( null != o && o.toString().trim().length() > 0 )
      {
         params.add( new PSMapPair( docContext.
            getSystemParam( PSContentEditorHandler.REVISION_ID_PARAM_NAME ),
            o.toString()));
      }

      // determine the view to use for the sys_view param
      String view = getViewEvaluator().getNextView(data, pageId);

      // add the view name
      params.add( new PSMapPair(IPSHtmlParameters.SYS_VIEW, view));
   }

   // tag names for elements in the DisplayField element
   private static final String TABLE_NAME = "Table";
   private static final String HEADER_NAME = "Header";
   /**
    * The row element name specified in sys_ContentEditor.dtd. Never
    * <code>null</code> or empty.
    */
   public static final String ROW_NAME = "Row";
   public static final String ROWSET_NAME = "RowData";

   /** Name of the child elements of the {@link #HEADER_NAME} element */
   private static final String HEADER_COLUMN_NAME = "HeaderColumn";
   private static final String COLUMN_NAME = "Column";
   private static final String ACTIONSET_NAME = "ActionLinkList";
   private static final String PARAM_NAME = "Param";
   private static final String NAME_NAME = "name";
   private static final String HIDDENFIELDS_NAME = "HiddenFormParams";
   private static final String IMAGEREF_ATTRIB = "imageHref";

   /**
    * The display text for the main button that submits the form to edit the
    * table. Never <code>null</code> or empty.
    */
   private static final String FORMSUBMIT_LABEL = "Edit table";

   /**
    * An index into the <code>m_imageLocations</code> array where the delete
    * button url is stored.
    */
   private static final int DELETE_INDEX = 0;

   /**
    * An index into the <code>m_imageLocations</code> array where the modify
    * button url is stored.
    */
   private static final int MODIFY_INDEX = 1;

   /**
    * An index into the <code>m_imageLocations</code> array where the moveup
    * button url is stored.
    */
   private static final int MOVEUP_INDEX = 2;

   /**
    * An index into the <code>m_imageLocations</code> array where the movedown
    * button url is stored.
    */
   private static final int MOVEDOWN_INDEX = 3;

   /**
    * 1 greater than the largest value in the ..._INDEX constants. Used for
    * validation. If x is a constant, then x >= 0 && x < IMAGE_COUNT.
    *
    */
   private static final int IMAGE_COUNT = 4;

   /**
    * This array stores the optional images for the row editing buttons.
    * Use the ..._INDEX constants to reference the elements. If an image hasn't
    * been defined, the entry has <code>null</code>.
    */
   private String [] m_imageLocations = new String[IMAGE_COUNT];

   /**
    * The set of builders, 1 for each column, used to create the Control
    * nodes for the output document when a request is being processed.
    * The order in the list is for left to right in the table. Never
    * empty after construction. The number of entries in this list is equal
    * to the number of items in <code>m_columnHeaders</code>.
    */
   private List m_builders = new ArrayList();

   /**
    * Contains the strings that label each column after construction. Never
    * <code>null</code>. Contains the same number of items as m_builders.
    * The strings are ordered for use from left to right.
    */
   private List m_columnHeaders = new ArrayList();

   /**
    * Contains the names for all fields in a row, in left to right order as
    * Strings. Never <code>null</code>. Immutable after construction. Contains
    * the same number of entries as the <code>m_columnHeaders</code> list.
    */
   private List m_fieldNames = new ArrayList();

   /**
    * Used for error messages. Never empty or changed after construction.
    */
   private String m_fieldSetName;

   /**
    * Contains Map.Entry objects, keyed with the constants
    * ..._EXTRACTOR_KEY. Each entry has the HTML param as the key and an
    * extractor to get the output value as the entry's value. Never <code>null
    * </code>.
    */
   private Map m_paramExtractors = new HashMap();

   /**
    * Never <code>null</code> after construction. Contains the context for
    * the editor builder that owns this builder. Set during construction,
    * then immutable.
    */
   private PSEditorDocumentBuilder m_parentBuilder;

   /**
    * This is the mapper id for the parent that owns this builder. It is sent
    * to the modify handler to tell it which table to update. Set during
    * construction, then immutable.
    */
   private int m_parentId = -1;

   /**
    * This is the 'childid' for the mapper associated with this table. Set in
    * ctor, then immutable.
    */
   private int m_myId;

   /**
    * A flag to indicate whether ordering by the end user is supported. It
    * causes the reordering buttons to show or not. Set in ctor, then
    * immutable.
    */
   private boolean m_supportsSequencing;

   /**
    * A flag that tells the builder to override the showInSummary and
    * showInPreview settings of the editor def. If <code>true</code>, these
    * settings are overridden.
    * Set in ctor, never changed after that.
    */
   private boolean m_showAllFields;

   /**
    * The key to get the content id extractor. The content id is part of the
    * primary key for the content item.
    */
   private static final String CONTENT_ID_EXTRACTOR_KEY = "a";

   /**
    * The key to get the revision id extractor. The revision id is part of the
    * primary key for the content item.
    */
   private static final String REVISION_ID_EXTRACTOR_KEY = "b";

   /**
    * The key to get the child id extractor. This is the mapper id.
    */
   private static final String CHILD_ID_EXTRACTOR_KEY = "c";

   /**
    * The key to get the child row id extractor. This is the primary key for
    * the child table.
    */
   private static final String ROW_ID_EXTRACTOR_KEY = "d";

   /**
    * The key to get the page id extractor. This is the unique identifier for
    * the editor page.
    */
   private static final String PAGE_ID_EXTRACTOR_KEY = "e";
}



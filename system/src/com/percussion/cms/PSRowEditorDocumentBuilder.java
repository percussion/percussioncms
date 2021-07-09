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
import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.cms.handlers.PSQueryCommandHandler;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSMetaDataCache;
import com.percussion.data.PSResultSet;
import com.percussion.data.PSResultSetMetaData;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.extension.PSExtensionException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSMapPair;
import com.percussion.util.PSSqlHelper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Document;

/**
 * This class defines a row editor, which is one of the editor types that make
 * up the editor set in a content editor. A row editor allows the user to
 * fiddle with all the fields in a single row at the same time. He cannot
 * delete the row from this view.
 */
public class PSRowEditorDocumentBuilder extends PSModifyDocumentBuilder
{
   /**
    * Processes the supplied editor definition, creating an efficient
    * representation of the object for runtime. Creates a 'row editor'
    * builder. A row editor allows the end user to create/modify a single
    * row of content. It also displays child data in read-only tables.
    * <p>See the {@link PSModifyDocumentBuilder#PSModifyDocumentBuilder(
    * PSContentEditor, PSEditorDocumentContext, int, boolean) base} class
    * for a description of the params and exceptions.
    */
   public PSRowEditorDocumentBuilder( PSContentEditor ce,
         PSEditorDocumentContext ctx, PSDisplayMapper dispMapper, int pageId,
         boolean isError )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( ce, ctx, pageId, isError );

      Iterator mappings = dispMapper.iterator();
      PSFieldSet fields = ((PSContentEditorPipe) ce.getPipe())
            .getMapper().getFieldSet( dispMapper.getFieldSetRef());
      if ( null == fields )
         throw new PSNotFoundException( IPSServerErrors.CE_MISSING_FIELDSET,
               dispMapper.getFieldSetRef());

      // remember name for error msgs
      m_fieldSetName = fields.getName();

      m_sdmpExtractors = createSdmpExtractors( fields, dispMapper );

      while ( mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fields.get( fieldName );
         if ( null == o )
         {
            o = fields.getChildField( fieldName,
                  fields.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
         }
         if ( null == o )
         {
            String mappingLabel = "unknown";
            PSUISet uiSet = mapping.getUISet();
            if ( null != uiSet )
            {
               PSDisplayText label = uiSet.getLabel();
               if ( null != label )
                  mappingLabel = label.getText();
            }
            String [] args =
            {
               fieldName,
               mappingLabel
            };
            throw new PSSystemValidationException(
                  IPSServerErrors.CE_MISSING_FIELD, args );
         }
         if ( o instanceof PSFieldSet )
         {
            PSFieldSet fieldSet = (PSFieldSet) o;
            PSDisplayMapper childMapper = mapping.getDisplayMapper();
            if ( null == childMapper )
            {
               throw new PSSystemValidationException(
                     IPSServerErrors.CE_MISSING_MAPPINGS,
                     fieldSet.getName());
            }

            if (fieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
            {
               addBuildStep(new PSMultiValueBuilder(fieldSet,
                     mapping.getUISet(), this));
            }
            else
            {
               addBuildStep( new PSTableValueBuilder( fieldSet,
                     mapping.getUISet(), childMapper, this ));
            }
         }
         else
         {
            PSField field = (PSField) o;
            if ( showField( field ))
            {
               if ( isBinaryField( field ))
               {
                  addBuildStep(new PSDisplayFieldBuilder( field,
                        mapping.getUISet(), this ));
               }
               else
               {
                  addBuildStep(new PSSingleValueBuilder( field,
                        mapping.getUISet(), field.getLocator(), this ));
               }
            }
         }
      }

      Object [][] hiddenParamSet =
      {
         {
            ctx.getSystemParam( PSContentEditorHandler.COMMAND_PARAM_NAME ),
            new PSTextLiteral( PSModifyCommandHandler.COMMAND_NAME )
         },
      };
      String controlName =
            ctx.getInitParam( IPSConstants.HIDDEN_CONTROL_PARAM_NAME );
      if ( null == controlName || controlName.trim().length() == 0 )
      {
         String [] args =
         {
            IPSConstants.HIDDEN_CONTROL_PARAM_NAME,
            "InitParam is empty or missing from system def"
         };
         throw new PSSystemValidationException( IPSServerErrors.CE_INVALID_PARAM,
               args );
      }

      for ( int i = 0; i < hiddenParamSet.length; i++ )
      {
         addBuildStep( new PSSingleValueBuilder( controlName,
               (String) hiddenParamSet[i][0],
               (IPSReplacementValue) hiddenParamSet[i][1], this ));
      }

   }

   // see base for desc
   protected Iterator getActionLinks( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "One or more params is null." );

      try
      {
         List actions = new ArrayList();
         List params = new ArrayList();

         int pageId = getPageId();
         if ( pageId != PSQueryCommandHandler.ROOT_PARENT_PAGE_ID )
         {
            /* we want the id to be the id for the summary editor, which
               happens to be our parent as well */
            pageId = getDocContext().getParentPageId( pageId );
         }
         params.add( new PSMapPair( m_docContext.
               getSystemParam( PSContentEditorHandler.PAGE_ID_PARAM_NAME ),
               ""+pageId ));

         params.add( new PSMapPair(IPSHtmlParameters.SYS_VIEW,
            getViewEvaluator().getNextView(data, pageId)));

         boolean isNewDoc = isNewDocument( data, true );
         String actionType = isNewDoc ?
            m_docContext.getRequestTypeValueInsert() :
               m_docContext.getRequestTypeValueUpdate();
         params.add( new PSMapPair( m_docContext.getRequestTypeHtmlParamName(),
               actionType ));

         params.add( new PSMapPair( m_docContext.
               getSystemParam( PSContentEditorHandler.CHILD_ID_PARAM_NAME ),
               ""+m_docContext.getChildId( getPageId())));

         // we only want to add the keys (contentid and revision) if they have
         // values
         Object o = getExtractor( CONTENT_ID_EXTRACTOR_KEY ).extract( data );
         if ( null != o && o.toString().trim().length() > 0 )
         {
            params.add( new PSMapPair( m_docContext.
               getSystemParam( PSContentEditorHandler.CONTENT_ID_PARAM_NAME ),
               o.toString()));
         }

         o = getExtractor( ROW_ID_EXTRACTOR_KEY ).extract( data );
         if ( null != o && o.toString().trim().length() > 0 )
         {
            params.add( new PSMapPair( m_docContext.
               getSystemParam( PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME ),
               o.toString()));
         }

         o = getExtractor( super.REVISION_ID_EXTRACTOR_KEY ).extract( data );
         if ( null != o && o.toString().trim().length() > 0 )
         {
            params.add( new PSMapPair( m_docContext.
               getSystemParam( PSContentEditorHandler.REVISION_ID_PARAM_NAME ),
               o.toString()));
         }

         String lang = getUserLocaleString(data);

         String label = isNewDoc ?
            PSI18nUtils.getString(PSI18nUtils.PSX_CE_ACTION +
            PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST +  "Insert", lang)
            :
            PSI18nUtils.getString(PSI18nUtils.PSX_CE_ACTION +
            PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST +  "Update", lang);

         String accesskey = isNewDoc ? 
            PSI18nUtils.getMnemonic(
            PSI18nUtils.PSX_CE_ACTION + PSI18nUtils.LOOKUP_KEY_SEPARATOR
                  + "Insert", lang, 'I') 
            : 
            PSI18nUtils.getMnemonic(
            PSI18nUtils.PSX_CE_ACTION + PSI18nUtils.LOOKUP_KEY_SEPARATOR
                  + "Update", lang, 'U');
         
         actions.add( createActionElement( doc, label, null,
               params.iterator(), true, accesskey));
         return actions.iterator();
      }
      catch ( PSNotFoundException nfe )
      {
         throw new PSDataExtractionException( nfe.getErrorCode(),
               nfe.getErrorArguments());
      }
   }


   // see base class for description
   public void prepareExecutionData( PSExecutionData data )
      throws PSConversionException, SQLException, PSDataExtractionException
   {
      if ( isNewDocument( data, true ))
         return;

      /* If we have any SDMP children, we have to pop them all and create a
         new result set that contains them and the parent. */
      if ( null != m_sdmpExtractors )
      {
         mergeResultSets( data );
      }
      else
      {
         ResultSet parentRs = data.getNextResultSet();
         if ( null == parentRs )
         {
            throw new PSConversionException( IPSServerErrors.CE_MISSING_RESULTSET,
                  m_fieldSetName );
         }
      }

      Object[] curRow = data.getCurrentResultRowData();
      if ( !data.readRow())
      {
         throw new PSConversionException(
               IPSServerErrors.CE_NO_DATA_IN_RESULT_SET, m_fieldSetName );
      }
   }


   /**
    * Walks the supplied mapper looking for mappings that reference fields
    * belonging to sdmp children. It finds all the different fieldsets used
    * as sdmp children. If there are any, an extractor is created for every
    * parent field and every sdmp field. The returned list is either <code>
    * null</code> if there are no sdmp children, or contains an entry for the
    * parent and each sdmp child. The parent is the first entry in the list
    * followed by all sdmp children in the same order as they are specified
    * in the content editor definition.
    * Each entry in the list is a PSMapPair. Each pair has the alias of the
    * table associated with the fieldset (lowercased) (as a String) as the key
    * and a Collection as the value. Each entry in the collection is an
    * IPSDataExtractor built from a PSBackEndColumn.
    *
    * @param fields The fieldset that contains all the fields referenced by
    *    the supplied mapping. Assumed not <code>null</code>.
    *
    * @param mapper The mapper for this editor. Assumed not <code>null</code>.
    *
    * @return If no queriable sdmp fields are present, <code>null</code> is 
    *    returned.  Queriable means a field with a non-binary back-end column.
    *    Otherwise, for each sdmp field and the parent, an entry will be
    *    created. Each entry is a PSMapPair. The key of this pair is the table
    *    name, and the value is a list of IPSDataExtractors for all of the
    *    fields in the table that have been mapped. The parent is always the
    *    first entry, and the sdmp child entries appear in the order of the
    *    appearance of the first mapping of each child in the document.
    *
    * @throws PSSystemValidationException If the backend columns in the supplied
    *    fieldset are not fully specified.
    */
   private List createSdmpExtractors( PSFieldSet fs, PSDisplayMapper mapper )
      throws PSSystemValidationException
   {
      // key is sdmp fieldset, value is List of mappings for that fieldset
      Map sdmpFieldSets = new HashMap();
      // after the loop is finished, contains all parent mappings for be cols
      List parentMappings = new ArrayList();
      // an ordered list of fieldsets, the parent first and then all sdmp
      List orderedSdmpFieldSets = new ArrayList();

      Iterator mappingsIter = mapper.iterator();
      while ( mappingsIter.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappingsIter.next();
         PSField sdmpField = fs.getChildField( mapping.getFieldRef(),
               fs.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
         if ( null != sdmpField )
         {
            PSFieldSet sdmpFieldSet = fs.getChildsFieldSet(
                  sdmpField.getSubmitName(),
                  fs.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
            List mappings = (List) sdmpFieldSets.get( sdmpFieldSet );
            if ( null == mappings )
            {
               mappings = new ArrayList();
               sdmpFieldSets.put( sdmpFieldSet, mappings );
               orderedSdmpFieldSets.add(sdmpFieldSet);
            }
            mappings.add( mapping );
         }
         else
         {
            Object o = fs.get( mapping.getFieldRef());
            if ( o instanceof PSField )
            {
               PSField field = (PSField) o;
               if ( field.getLocator() instanceof PSBackEndColumn )
                  parentMappings.add( mapping );
            }
         }
      }
      if ( 0 == sdmpFieldSets.size())
         return null;

      List results = new ArrayList();
      // create the extractors for the parent
      List parentExtractors = new ArrayList();
      String tableName = createExtractors( fs, parentMappings,
            parentExtractors );
      results.add( new PSMapPair( tableName, parentExtractors ));

      // create the extractors for the sdmp children
      Iterator sdmpIter = orderedSdmpFieldSets.iterator();
      while ( sdmpIter.hasNext())
      {
         PSFieldSet sdmpFs = (PSFieldSet) sdmpIter.next();
         List mappings = (List) sdmpFieldSets.get( sdmpFs );
         List extractors = new ArrayList();
         tableName = createExtractors( sdmpFs, mappings, extractors );
         if (tableName != null)
            results.add( new PSMapPair( tableName, extractors ));
      }

      if (results.isEmpty())
         results = null;

      return results;
   }


   /**
    * Walks the supplied mappings list. For each mapping it gets the referenced
    * field from the supplied fieldset. If the field has a non-binary as
    * determined by {@link PSEditorDocumentBuilder#isBinaryField(PSField)
    * isBinaryField} PSBackEndColumn as its data source, an extractor is built
    * and added to the results list.
    *
    * @param fs A valid fieldset. Assumed not <code>null</code>.
    *
    * @param mappings A list of PSDisplayMappings that use the supplied
    *    field set. Only columns that are mapped will get extractors created.
    *    Assumed that all mappings in this list reference only PSFields and
    *    the locator for each one is a PSBackEndColumn.
    *
    * @param results The created extractors are added to this list. Each
    *    added entry is an IPSDataExtractor for a backend column.
    *
    * @return The name of the table that contains the columns in the fieldset.
    *    May be <code>null</code> if the mappings reference only binary fields
    *    or fields with locators referencing non-backend columns, never empty.
    *
    * @throws PSValidationExceptin if the name of the table associated with
    *    a backend column is <code>null</code> or empty.
    */
   private String createExtractors( PSFieldSet fs, List mappings,
         List results )
      throws PSSystemValidationException
   {
      String tableName = null;
      Iterator mappingIter = mappings.iterator();
      while ( mappingIter.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappingIter.next();
         PSField field = (PSField) fs.get( mapping.getFieldRef());
         if (isBinaryField(field))
            continue;
         IPSReplacementValue value = (IPSReplacementValue) field.getLocator();
         if (!(value instanceof PSBackEndColumn))
            continue;
         if ( null == tableName )
         {
            PSBackEndColumn col = (PSBackEndColumn) value;
            tableName = col.getTable().getTable();
            if ( null == tableName || tableName.trim().length() == 0 )
            {
               throw new PSSystemValidationException(
                     IPSServerErrors.CE_MISSING_TABLE_NAME,
                     col.getColumn());
            }
         }
         try
         {
            results.add(
               PSDataExtractorFactory.createReplacementValueExtractor( value ));
         }
         catch ( IllegalArgumentException e )
         {
            throw new IllegalArgumentException( e.getLocalizedMessage());
         }
      }
      return tableName;
   }


   /**
    * If there are any sdmp children in this editor, the parent result set
    * and the result sets for all sdmp children are popped and their data
    * is merged into a new result set, which is then pushed back onto the
    * result set stack in the execution data. Upon returning, the execution
    * data is ready for processing, whether any result sets were merged or
    * not.
    * This method count on the fact that the list of extractors
    * (m_sdmpExtractors) is in the correct order. See
    * {@link #createSdmpExtractors(PSFieldSet, PSDisplayMapper)} for details.
    * <p>Originally, we got the data from sdmp fields
    * by joining the table in the parent's request. However, if an sdmp child
    * is added after content has been added to a parent, this model doesn't
    * work. Therefore, we query each sdmp child individually, and create a
    * new result set that contains the parent and all sdmp children as if
    * they had been joined and empty rows existed where there were no rows.
    * <p><em>Note:<em> I didn't put this in the data package because it didn't
    * seem general enough.
    *
    * @param data The execution data that contains the result sets to be
    *    merged. Assumed that no result set has been popped yet. Upon return,
    *    the top result will have been popped by calling <code>
    *    getNextResultSet</code> on the data. Assumed not <code>null</code>.
    *
    * @throws PSConversionException If there are insufficient Result sets in
    *    the supplied execution data.
    *
    * @throws SQLException If any problems occur while working with any of the
    *    ResultSets. This could happen if the result sets in the execution
    *    data are not in the expected order.
    *
    * @throws PSDataExtractionException If any problems occur while trying to
    *    get the data from the result sets.
    */
   private void mergeResultSets( PSExecutionData data )
      throws PSConversionException, SQLException, PSDataExtractionException
   {
      if ( null != m_sdmpExtractors )
      {
         int mergeCount = m_sdmpExtractors.size();

         // data used to build merged result set
         List mergedData = new ArrayList();
         // used to build merged result set
         HashMap colToIndex = new HashMap();
         // used to create merged meta data
         List metaDataList = new ArrayList();
         // index of column w/in merged resultset
         int colIndex = 1;
         for ( int i = 0; i < mergeCount; i++ )
         {
            ResultSet rs = data.getNextResultSet();
            if ( null == rs )
            {
               throw new PSConversionException( IPSServerErrors.CE_MISSING_RESULTSET,
                     m_fieldSetName );
            }

            ResultSetMetaData md = rs.getMetaData();
            boolean hasData = data.readRow();

            List cols = new ArrayList();
            PSMapPair entry = (PSMapPair) m_sdmpExtractors.get(i);
            Iterator extractorIter = ((List) entry.getValue()).iterator();
            while ( extractorIter.hasNext())
            {
               Object value = null;
               IPSDataExtractor ext = (IPSDataExtractor) extractorIter.next();
               if ( hasData )
                  value = ext.extract( data );
               PSBackEndColumn col = (PSBackEndColumn) ext.getSource()[0];
               String colName = col.getColumn();
               List rowData = new ArrayList(1);
               rowData.add( value );
               mergedData.add( rowData );
               colToIndex.put( colName, new Integer( colIndex++ ));
               cols.add( col );
            }
            metaDataList.add( new PSMapPair( md, cols ));
         }

         List [] finalData =  new List[mergedData.size()];
         mergedData.toArray( finalData );
         ResultSet rs = new PSResultSet( finalData, colToIndex,
               new PSResultSetMetaData( new MergedMetaData( metaDataList )));
         Stack stack = data.getResultSetStack();
         stack.push( rs );
      }
      data.getNextResultSet();
   }


   /**
    * Creates a new meta data object that makes meta data objects from
    * multiple result sets look like a single one.
    */
   private class MergedMetaData implements ResultSetMetaData
   {
      /**
       * Builds an internal representation using the supplied data so this
       * class will act like a single meta data object even though it is
       * composed of multiple meta data objects.
       *
       * @param metaDataInfo A list of PSMapPairs. Each pair has the original
       *    meta data as the key and a List of PSBackEndColumns as the value.
       *    The merged meta data will represent the columns concatenated in
       *    the order they appear in this list and within the sub-lists.
       *
       * @throws ClassCastException If any of the entries aren't of the
       *    correct type.
       *
       * @throws SQLException If a specified column can't be found in its
       *    associated result set.
       */
      public MergedMetaData( List metaDataInfo )
         throws SQLException
      {
         if ( null == metaDataInfo || metaDataInfo.size() == 0 )
         {
            throw new IllegalArgumentException(
                  "param can't be null or empty" );
         }

         // build map of index to metadata
         Iterator iter = metaDataInfo.iterator();
         int mergedColIndex = 1;
         while ( iter.hasNext())
         {
            PSMapPair entry = (PSMapPair) iter.next();
            ResultSetMetaData md = (ResultSetMetaData) entry.getKey();
            List cols = (List) entry.getValue();
            Iterator colIter = cols.iterator();
            if ( !colIter.hasNext())
            {
               throw new IllegalArgumentException(
                     "missing columns for metadata" );
            }
            while ( colIter.hasNext())
            {
               PSBackEndColumn col = (PSBackEndColumn) colIter.next();
               String alias = col.getAlias();
               String colName;
               if ( null != alias && alias.length() > 0 )
                  colName = alias;
               else
                  colName = col.getColumn();
               PSBackEndTable table = col.getTable();
               PSMetaDataCache.loadConnectionDetail(table);
               int idx = getColumnIndex( md, col.getTable().getTable(),
                     colName, table.getConnectionDetail().getOrigin(),
                     table.getConnectionDetail().getDriver());
               m_colIndexToMetaData.put( new Integer( mergedColIndex++ ),
                     new PSMapPair( new Integer( idx ), md ));
            }
         }
      }

      // see interface for desc
      public String getCatalogName( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getCatalogName(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public String getColumnClassName( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getColumnClassName(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public int getColumnCount()
         throws SQLException
      {
         return m_colIndexToMetaData.size();
      }

      // see interface for desc
      public int getColumnDisplaySize( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getColumnDisplaySize(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public String getColumnLabel( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getColumnLabel(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public String getColumnName( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getColumnName(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public int getColumnType( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getColumnType(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public String getColumnTypeName( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getColumnTypeName(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public int getPrecision( int index )
         throws SQLException
      {
         ResultSetMetaData meta = getOriginalMetaData(index);
         int originalIndex = getOriginalIndex(index);
         int originalType = meta.getColumnType(originalIndex);

         /* LOBS were causing a number format exception in oracle, since
            they are not numbers, we don't need their precision */
         if ((originalType != Types.CLOB) && (originalType != Types.BLOB))
            return meta.getPrecision(originalIndex);
         else
            return 0;
      }

      // see interface for desc
      public int getScale( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getScale(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public String getSchemaName( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).getSchemaName(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public String getTableName( int index )
         throws SQLException
      {
         String tableName = null;
         try
         {
            tableName = getOriginalMetaData(index).getTableName(
               getOriginalIndex(index));
         }
         catch (UnsupportedOperationException e)
         {
            // ignore, sybase doesn't return null like other drivers.
         }

         return tableName;
      }

      // see interface for desc
      public boolean isAutoIncrement( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isAutoIncrement(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public boolean isCaseSensitive( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isCaseSensitive(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public boolean isCurrency( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isCurrency(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public boolean isDefinitelyWritable( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isDefinitelyWritable(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public int isNullable( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isNullable(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public boolean isReadOnly( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isReadOnly(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public boolean isSearchable( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isSearchable(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public boolean isSigned( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isSigned(
               getOriginalIndex( index ));
      }

      // see interface for desc
      public boolean isWritable( int index )
         throws SQLException
      {
         return getOriginalMetaData( index ).isWritable(
               getOriginalIndex( index ));
      }


      /**
       * Looks in the supplied metadata to find the column that matches the
       * supplied table and column name (in a case insensitive manner). The
       * index of this column is returned.
       *
       * @param md A valid meta data object.
       *
       * @param tableName A valid, non-empty table name.
       *
       * @param colName A valid, non-empty column name.
       *
       * @param origin The origin or schema of the table.  May be <code>null
       * </code> or empty.
       *
       * @param driver The JDBC subprotocol used in the connection string. For
       *    example: 'odbc', 'oracle:thin', 'inetdae7'. Case sensitive.
       *
       * @return The index of the column in the meta data whose qualified name
       *    is tableName.colName. If there is no table name available in the
       *    supplied metadata, then just the column names are compared. 1 based
       *
       * @throws SQLException If it can't be found.
       */
      private int getColumnIndex( ResultSetMetaData md, String tableName,
            String colName, String origin, String driver )
         throws SQLException
      {
         int count = md.getColumnCount();
         int colIndex = -1;

         for ( int i = 1; i <= count && colIndex < 0; i++ )
         {
            String tmpTableName = null;
            try
            {
               tmpTableName = md.getTableName(i);
            }
            catch (UnsupportedOperationException e)
            {
               // ignore, sybase doesn't return null like other drivers.
            }

            String tmpOrigin = null;
            if ( null != tmpTableName && tmpTableName.trim().length() > 0 )
            {
               StringBuffer originBuf = new StringBuffer();
               tmpTableName = PSSqlHelper.parseTableName( driver, tmpTableName,
                     originBuf, null );
               if ( originBuf.length() > 0 )
                  tmpOrigin = originBuf.toString();
               if ( null != tmpOrigin && tmpOrigin.trim().length() == 0 )
                  tmpOrigin = null;
            }
            /* At this point, tmpTableName and tmpOrigin either contain a valid,
               simple name or are null. */

            String tmpColName = md.getColumnName(i);

            if ( (null == tmpTableName || tmpTableName.length() == 0)
                  && tmpColName.equalsIgnoreCase( colName ))
            {
               // no table name available, the column names matched
               colIndex = i;
            }
            else if ( null == tmpOrigin && tmpTableName != null &&
               tmpTableName.equalsIgnoreCase( tableName )
               && tmpColName.equalsIgnoreCase( colName ))
            {
               colIndex = i;
            }
            else if ( null != tmpOrigin && null != origin
               && tmpOrigin.equalsIgnoreCase(origin)
               && tmpTableName != null
               && tmpTableName.equalsIgnoreCase( tableName )
               && tmpColName.equalsIgnoreCase( colName ))
            {
               colIndex = i;
            }
         }
         if ( colIndex < 0 )
            throw new SQLException( "Column not found in result set" );
         return colIndex;
      }


      /**
       * Returns the original meta data for the column at the specified index
       * in the merged meta data.
       *
       * @param index Position of the column in the merged meta data, 1 based.
       *
       * @return The original result set that contains the column at the
       *    specified index.
       */
      private ResultSetMetaData getOriginalMetaData( int index )
         throws SQLException
      {
         if ( index < 1 || index > getColumnCount())
            throw new SQLException( "Invalid column index" );

         PSMapPair entry = (PSMapPair)
               m_colIndexToMetaData.get( new Integer( index ));
         return (ResultSetMetaData) entry.getValue();
      }

      /**
       * Returns the index of the column that exists at the specified index
       * in the merged result set. This index can be used with the original
       * meta data to get information about the column.
       *
       * @throws SQLException if index is < 1 or greater than <code>
       *    getColumnCount()</code>.
       *
       * @see #getOriginalMetaData
       */
      private int getOriginalIndex( int index )
         throws SQLException
      {
         if ( index < 1 || index > getColumnCount())
            throw new SQLException( "Invalid column index" );

         PSMapPair entry = (PSMapPair)
               m_colIndexToMetaData.get( new Integer( index ));
         return ((Integer) entry.getKey()).intValue();

      }


      /**
       * A map whose key is the index of the column in the merged metadata
       * (as an Integer) and whose value is a PSMapPair. The key of this pair
       * is the original index into the original meta data. The original
       * index is an Integer and the value is a ResultSetMetaData object.
       */
      private Map m_colIndexToMetaData = new HashMap();


      public boolean isWrapperFor(Class<?> iface) throws SQLException
      {
         throw new UnsupportedOperationException("This method is not yet implemented");
      }

      public <T> T unwrap(Class<T> iface) throws SQLException
      {
         throw new UnsupportedOperationException("This method is not yet implemented");
      }
   }


   /**
    * This is set once during initialization and never changed after that.
    * Might be <code>null</code>. For details see the create method
    * {@link #createSdmpExtractors(PSFieldSet, PSDisplayMapper)}.
    */
   private List m_sdmpExtractors;

   /**
    * The name of the fieldset that contained the definition for this editor.
    * Used for error messages. Never empty or <code>null</code> after
    * construction.
    */
   private String  m_fieldSetName;
}

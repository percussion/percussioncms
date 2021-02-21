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
import com.percussion.data.PSMetaDataCache;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndCredential;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSCollection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base class for all modify plan builders.  Provides common functionality
 * for all modify plan builders.
 */
public abstract class PSModifyPlanBuilder
{

   /**
    * Constructor for this class.
    *
    * @param ceh The content editor handler of the dataset for which this plan
    * is being built.  May not be <code>null</code>.
    * @param ce The content editor dataset.  May not be <code>null</code>.
    * @param app The application that all dataset created are added to.  May not
    * be <code>null</code>.
    *
    * @throws IllegalArgumentExcpetion if ceh or app are <code>null</code>.
    */
   public PSModifyPlanBuilder(PSContentEditorHandler ceh, PSContentEditor ce,
      PSApplication app)
   {
      if (ceh == null || ce == null || app == null)
         throw new IllegalArgumentException("one or more params was null");

      m_ceHandler = ceh;
      m_ce = ce;
      m_internalApp = app;
   }

   /**
    * Creates a plan that will perform the required updates.
    *
    * @param mapper The parent display mapper.  May not be <code>null</code>.
    * @param fieldSet The fieldSet used by the mapper.  May not be
    * <code>null</code>.
    *
    * @return The modify plan to execute at runtime.
    *
    * @throws SQLException if there is an error determining a column's datatype.
    * @throws PSSystemValidationException if any objects used by this method are
    * invalid.
    */
   public abstract PSModifyPlan createModifyPlan(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSSystemValidationException, SQLException;


   /**
    * Returns the table that a display mapper uses.
    *
    * @param dispMapper The mapper to get the table for.  Not <code>null</code>.
    * @param fieldSet The fieldSet that the mapper uses.  Not <code>null</code>.
    *
    * @return The table, never <code>null</code>.
    *
    * @throws IllegalStateException if no tables are found.
    */
   public static PSBackEndTable getMapperTable(PSDisplayMapper dispMapper,
      PSFieldSet fieldSet)
   {
      if (dispMapper == null || fieldSet == null)
         throw new IllegalArgumentException("One or more params is null");

      PSBackEndTable table = null;
      Iterator mappings = dispMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         String fieldRef = mapping.getFieldRef();
         Object o = fieldSet.get(fieldRef);
         if (o instanceof PSFieldSet)
            continue;
         PSField field = (PSField)o;

         if (field == null)
            continue;

         IPSBackEndMapping beMapping = field.getLocator();
         if (beMapping instanceof PSBackEndColumn)
         {
            PSBackEndColumn column = (PSBackEndColumn)beMapping;
            PSBackEndTable colTable = column.getTable();

            // be sure this isn't the content status table
            if (!colTable.getAlias().equalsIgnoreCase(
               PSContentEditorSystemDef.CONTENT_STATUS_TABLE_ALIAS))
            {
               table = colTable;
               break;
            }
         }
      }

      if (table == null)
         throw new IllegalStateException(
            "Unable to determine table for DisplayMapper with id " +
               dispMapper.getId() + " from FieldSet named "
                  + fieldSet.getName());

      return table;
   }

   /**
    * Walks the mapper and builds a list of all tables that the display mappings
    * reference.  For any mappings that reference a field not found
    * in the provided fieldSet, will check any immediate child fieldsets of
    * the type {@link PSFieldSet#TYPE_MULTI_PROPERTY_SIMPLE_CHILD} for that
    * field, and if found also adds that field's table to the list that is
    * returned.
    *
    * @param dispMapper The mapper to get the table for.  Not <code>null</code>.
    * @param fieldSet The fieldSet that the mapper uses.  Not <code>null</code>.
    *
    * @return An array of PSBackEndTable objects, never <code>null</code>.  Will
    * exclude the CONTENTSTATUS table.
    */
   public static PSBackEndTable[] getMapperTables(PSDisplayMapper dispMapper,
      PSFieldSet fieldSet)
   {
      if (dispMapper == null || fieldSet == null)
         throw new IllegalArgumentException("One or more params is null");

      Map tables = new HashMap();
      Iterator mappings = dispMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         String fieldRef = mapping.getFieldRef();
         Object o = fieldSet.get(fieldRef);
         if (o instanceof PSFieldSet)
            continue;
         PSField field = (PSField)o;

         if (field == null)
         {
            // see if it is from an allowed child fieldset
            field = fieldSet.getChildField(fieldRef,
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
            if (field == null)
               continue;
         }

         IPSBackEndMapping beMapping = field.getLocator();
         if (beMapping instanceof PSBackEndColumn)
         {
            PSBackEndColumn column = (PSBackEndColumn)beMapping;
            PSBackEndTable colTable = column.getTable();
            String colTableAlias = column.getTable().getAlias().toUpperCase();

            // be sure this isn't the content status table
            if (!colTableAlias.equalsIgnoreCase(
               PSContentEditorSystemDef.CONTENT_STATUS_TABLE_ALIAS))
            {
               // don't add more than once
               if (!tables.containsKey(colTableAlias))
                  tables.put(colTableAlias, colTable);
            }
         }
      }

      PSBackEndTable[] tableArray = new PSBackEndTable[tables.size()];
      tables.values().toArray(tableArray);

      return tableArray;

   }


   /**
    * Determines if multiple rows may be inserted in a single request.  Checks
    * the fieldset to ensure that it is a complex child, and makes sure that
    * the mapper does not contain a mapping referencing a simple child fieldset.
    *
    * @param mapper The mapper to check.  Assumed not <code>null</code>.
    *
    * @param fieldSet The fieldSet the mapper references.  Assumed not <code>
    * null</code> and to be the correct fieldSet
    *
    * @return <code>true</code> if multiple inserts are allowed, <code>false
    * </code> if not.
    */
   public static boolean allowMultipleInserts(PSDisplayMapper mapper,
      PSFieldSet fieldSet)
   {
      return (fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD &&
         !containsChildType(mapper, fieldSet, PSFieldSet.TYPE_SIMPLE_CHILD));
   }

   /**
    * Creates a PSValidateModifyStep that compares the edit revision in the
    * database against the supplied revision id.
    *
    * @throws PSSystemValidationException if there is an error creating the dataset.
    */
   protected IPSModifyStep createRevisionValidationStep()
      throws PSSystemValidationException
   {
      try
      {
         // create a backend table
         PSContentEditorSystemDef sysDef = m_ceHandler.getSystemDef();
         PSBackEndTable beTable = new PSBackEndTable(
            PSContentEditorSystemDef.CONTENT_STATUS_TABLE_ALIAS);
         PSBackEndCredential cred = new PSBackEndCredential("cred");
         sysDef.populateSystemTableInfo(beTable.getAlias(), cred, beTable);

         // use that to create the columns and param mappings we need
         PSDataMapper dataMapper = new PSDataMapper();

         // Add the CONTENTID column
         String contentIdParam = m_ceHandler.getParamName(
            PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
         PSBackEndColumn contentIdCol = new PSBackEndColumn(beTable,
            IPSConstants.ITEM_PKEY_CONTENTID);

         PSDataMapping dataMapping = new PSDataMapping(contentIdParam,
            contentIdCol);
         dataMapper.add(dataMapping);

         // Add the EDITREVISION column
         String editRevParam = m_ceHandler.getParamName(
            PSContentEditorHandler.REVISION_ID_PARAM_NAME);
         PSBackEndColumn revCol = new PSBackEndColumn(beTable,
            IPSConstants.ITEM_EDITREVISION);

         dataMapping = new PSDataMapping(editRevParam, revCol);
         dataMapper.add(dataMapping);

         // set up selection key for contentid
         HashMap selectionKeys = new HashMap();
         selectionKeys.put(contentIdCol.getColumn(),
            contentIdParam);

         String resourceName = PSApplicationBuilder.createQueryDataset(
            m_internalApp, dataMapper, selectionKeys.entrySet().iterator(),
            null);

         // now create the validations
         Map validations = new HashMap();
         validations.put(IPSConstants.ITEM_EDITREVISION,
            new PSSingleHtmlParameter(editRevParam));

         // create the step
         IPSModifyStep valStep = new PSValidateModifyStep(resourceName,
            m_internalApp.getRequestTypeHtmlParamName(),
            m_internalApp.getRequestTypeValueQuery(), validations);

         return valStep;
      }
      catch (IllegalArgumentException e)
      {
         // won't happen
         throw new IllegalArgumentException(e.toString());
      }
   }


   /**
    * Creates data mapper to use when updating the CONTENTSTATUS table.
    *
    * @param mapper The display mapper to use, may not be <code>null</code>.
    * @param fieldSet The fieldSet used by the mapper, may not be
    * <code>null</code>.
    *
    * @return A data mapper with the required mappings, never <code>null</code>.
    */
   protected PSDataMapper getSystemUpdateMapper(PSDisplayMapper mapper,
      PSFieldSet fieldSet)
   {
      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("One or more params is null");

      ArrayList updateSysMappings =
         PSApplicationBuilder.getSystemUpdateMappings(m_ceHandler, m_ce);
      updateSysMappings = addTableKeys(updateSysMappings,
         mapper, fieldSet);
      PSDataMapper updateSystemMapper =
         PSApplicationBuilder.createSystemMappings(
         updateSysMappings.iterator());

      return updateSystemMapper;
   }

   /**
    * Creates data mapper to use when deleting the CONTENTSTATUS table.
    *
    * @param mapper The display mapper to use, may not be <code>null</code>.
    * @param fieldSet The fieldSet used by the mapper, may not be
    * <code>null</code>.
    *
    * @return A data mapper with the required mappings, never <code>null</code>.
    */
   protected ArrayList getSystemDeleteMappings(PSDisplayMapper mapper,
      PSFieldSet fieldSet)
   {
      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("One or more params is null");

      // add the system mappings
      ArrayList deleteMappings = new ArrayList();     
      String sysTableRef =
         PSContentEditorSystemDef.CONTENT_STATUS_TABLE_ALIAS;
      PSBackEndTable sysTable = new PSBackEndTable(sysTableRef);
      PSBackEndCredential sysCred = new PSBackEndCredential("cred");
      m_ceHandler.getSystemDef().populateSystemTableInfo(sysTableRef,
         sysCred, sysTable);

      // add contentId = contentId html param
      PSSystemMapping sysMapping = new PSSystemMapping(sysTable, "CONTENTID",
         new PSHtmlParameter(m_ceHandler.getParamName(
         PSContentEditorHandler.CONTENT_ID_PARAM_NAME)));
      deleteMappings.add(sysMapping);
      return deleteMappings;
   }

   
   /**
    * a private utility method to return a fieldset based on the mapper id 
    * @param the mapper id
    * @return the field set with the above mapper id. May  be <code>null</code>.
    */
   private PSFieldSet getMapperFieldSet(int mapperId)
   {
      PSFieldSet fieldSet = null;
      PSContentEditorPipe pipe =
         (PSContentEditorPipe)m_ce.getPipe();
      PSDisplayMapper mapper =
         pipe.getMapper().getUIDefinition().getDisplayMapper(
            mapperId);

      if (mapper != null)
         fieldSet = pipe.getMapper().getFieldSet(
            mapper.getFieldSetRef());

      return fieldSet;
   }
   
   /**
    * build complex and simple child mappings to add to delete plans
    * @param mapper The display mapper to use, may not be <code>null</code>.
    * @param fieldSet The fieldSet used by the mapper, may not be
    * <code>null</code>.
    * @return A data mapper with the required mappings, never <code>null</code>.
    */
   protected List getDeleteChildMappings(PSDisplayMapper mapper,
           PSFieldSet fieldSet)
   {
       List deleteChildMappings = new ArrayList(); 
       
       Iterator mappings = mapper.iterator();
       while (mappings.hasNext())
       {
          PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
          PSDisplayMapper childMapper = mapping.getDisplayMapper();
          if (childMapper == null)
             continue;

          // we have a child, see if it is simple or complex
          String mapperFieldSetRef = childMapper.getFieldSetRef();
          fieldSet = getMapperFieldSet(childMapper.getId());

          if ( fieldSet == null )
             continue;
          if ( fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD ||
               fieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD  )
          {   
              deleteChildMappings = 
                  addTableKeysForDelete(
                          deleteChildMappings, childMapper, fieldSet);
          }
       }
       return deleteChildMappings;
   }
   
   /**
    * Returns any field in the fieldset that is force binary or a binary type.
    * Will also check any immediate SDMP child fieldsets.
    *
    * @param fieldSet The fieldset to check. May not be <code>null</code>.
    *
    * @return A List of binary PSField objects, may be empty if the fieldset
    * does not contain any.
    *
    * @throws PSSystemValidationException if any field's table cannot be located.
    * @throws SQLException if the column type cannot be determined
    */
   protected List getBinaryFields(PSFieldSet fieldSet)
      throws SQLException, PSSystemValidationException
   {
      if (fieldSet == null)
         throw new IllegalArgumentException("fieldSet may not be null");

      return getBinaryFields(fieldSet, true);
   }

   /**
    * Same as {@link #getBinaryFields(PSFieldSet)} but will optionally check the
    * immediate SDMP child fieldset if specified.  Only the additional
    * parameter is described below.
    *
    * @param checkChildren If <code>true</code>, binary fields located in any
    * immediate SDMP fieldsets will be included in the resulting list.
    */
   private List getBinaryFields(PSFieldSet fieldSet, boolean checkChildren)
      throws SQLException, PSSystemValidationException
   {
      if (fieldSet == null)
         throw new IllegalArgumentException("fieldSet may not be null");

      // see if we've already checked this fieldSet - may be null in the map
      if (m_binaryFields.containsKey(fieldSet))
         return ((List)m_binaryFields.get(fieldSet));

      PSContentEditorPipe pipe =
         (PSContentEditorPipe)m_ce.getPipe();

      PSCollection tableSetCol = new PSCollection(
         pipe.getLocator().getTableSets());

      // walk the fields looking for binary fields
      List binFields = new ArrayList();
      Iterator fields = fieldSet.getAll();
      while (fields.hasNext())
      {
         Object o = fields.next();
         if (o instanceof PSFieldSet)
         {
            if (!checkChildren)
               continue;

            PSFieldSet childFieldSet = (PSFieldSet)o;
            if (childFieldSet.getType() ==
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD)
            {
               binFields.addAll(getBinaryFields(childFieldSet, false));
            }

            continue;
         }

         PSField field = (PSField)o;

         // if force binary, then we're done
         if (field.isForceBinary())
         {
            binFields.add(field);
         }
         else
         {
            // need to check metadata of each column in each table in each set
            IPSBackEndMapping beMapping = field.getLocator();
            if (!(beMapping instanceof PSBackEndColumn))
               continue;

            PSBackEndColumn column = (PSBackEndColumn)beMapping;
            String tableName = column.getTable().getAlias();

            // find this field's table in the table sets and check the metadata
            boolean foundIt = false;
            Iterator tableSets = tableSetCol.iterator();
            while (tableSets.hasNext())
            {
               PSTableSet tableSet = (PSTableSet)tableSets.next();
               Iterator refs = tableSet.getTableRefs();
               while (refs.hasNext())
               {
                  PSTableRef ref = (PSTableRef)refs.next();
                  if (ref.getAlias().equals(tableName))
                  {
                     foundIt = true;
                     break;
                  }
               }

               if (foundIt)
               {
                  if (PSMetaDataCache.isBinaryBackendColumn(tableSet,
                     (PSBackEndColumn)beMapping))
                  {
                     binFields.add(field);
                  }
               }
            }

            if (!foundIt)
               throw new PSSystemValidationException(IPSServerErrors.CE_MISSING_TABLE,
                  tableName);
         }
      }

      m_binaryFields.put(fieldSet, binFields);

      return binFields;
   }

   /**
    * Convenience method that does not sort results by default.
    * @see #addTableKeys(ArrayList, PSDisplayMapper, PSFieldSet, boolean)
    * addTableKeys
    */
   protected ArrayList addTableKeys(ArrayList mappings,
      PSDisplayMapper dispMapper, PSFieldSet fieldSet)
   {
      return addTableKeys(mappings, dispMapper, fieldSet, false);
   }

   /**
    * Adds required keys to the mappings array provided based on table and
    * fieldset type of the supplied displayMapper, if they are not present in
    * the displayMapper.
    *
    * @param mappings The list of PSSystemMapping objects to append to.  This is
    * done with a copy, so the mappings list passed in is not modfied.  Not
    * <code>null</code>.
    * @param dispMapper The display mapper to use to determine what to add.
    * Not <code>null</code>.
    * @param fieldSet The fieldSet the display mapper uses. Not
    * <code>null</code>.
    * @param updateSequence If <code>true</code>, and the fieldSet is a
    * complex child type that supports sequencing, then the SORTRANK column
    * is added (although it is not technically a key, it is not part of the
    * fieldSet and so is added here).  If <code>false</code> or if the fieldset
    * is not of type complexChild then nothing is added.  If the fieldset does
    * not support sequencing, then this parameter has no effect.
    *
    * @return A copy of the supplied mappings, with the new entries added.  The
    * original mappings are not modified.  Never <code>null</code>.
    */
   protected ArrayList addTableKeys(ArrayList mappings,
      PSDisplayMapper dispMapper, PSFieldSet fieldSet, boolean updateSequence)
   {
      if (mappings == null || dispMapper == null || fieldSet == null)
         throw new IllegalArgumentException("One or more params is null");

      ArrayList resultMappings = (ArrayList)mappings.clone();

      // get table used by the mapper's fieldSet to add complex child keys
      PSBackEndTable table = getMapperTable(dispMapper, fieldSet);
      PSSystemMapping sysMapping = null;

      // if complex child, add sysid and possibly sequence
      if (fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
      {
         sysMapping = new PSSystemMapping(table,
            IPSConstants.CHILD_ITEM_PKEY, new PSHtmlParameter(
            m_ceHandler.getParamName(
               PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME)));
         resultMappings.add(sysMapping);

         //Sequence updates

         if (updateSequence && fieldSet.isSequencingSupported())
         {
            sysMapping = new PSSystemMapping(table,
               IPSConstants.CHILD_SORT_KEY, new PSHtmlParameter(
               m_ceHandler.getParamName(
                  PSContentEditorHandler.SORT_RANK_PARAM_NAME)));
            resultMappings.add(sysMapping);
         }

      }

      // get all tables used by the mapper and add keys all tables must have
      PSBackEndTable[] tableList = getMapperTables(dispMapper, fieldSet);
      for (int i = 0; i < tableList.length; i++)
         addCommonTableKeys(tableList[i], resultMappings);

      return resultMappings;
   }

   
   /**
    * Adds required keys to the mappings array provided based on table and
    * fieldset type of the supplied displayMapper, if they are not present in
    * the displayMapper. Same as addTableKeys, except this is used for delete
    *
    * @param mappings The list of PSSystemMapping objects to append to.  This is
    * done with a copy, so the mappings list passed in is not modfied.  Not
    * <code>null</code>.
    * @param dispMapper The display mapper to use to determine what to add.
    * Not <code>null</code>.
    * @param fieldSet The fieldSet the display mapper uses. Not
    * <code>null</code>.
    * @return A copy of the supplied mappings, with the new entries added.  The
    * original mappings are not modified.  Never <code>null</code>.
    */
   protected List addTableKeysForDelete(List mappings,
      PSDisplayMapper dispMapper, PSFieldSet fieldSet)
   {
      if (mappings == null || dispMapper == null || fieldSet == null)
         throw new IllegalArgumentException("One or more params is null");

      List resultMappings = (List)((ArrayList)mappings).clone();


      // get all tables used by the mapper and add keys all tables must have
      PSBackEndTable[] tableList = getMapperTables(dispMapper, fieldSet);
      for (int i = 0; i < tableList.length; i++)
      {
          PSSystemMapping perTableSysMapping = new PSSystemMapping(tableList[i],
             IPSConstants.ITEM_PKEY_CONTENTID, new PSHtmlParameter(
                m_ceHandler.getParamName(
                        PSContentEditorHandler.CONTENT_ID_PARAM_NAME)));
          resultMappings.add(perTableSysMapping);

      }
      return resultMappings;
   }

   /**
    * Adds contentid and revisionid keys to the mappings using the specified
    * table.
    *
    * @param table The table we are adding keys for.  Assumed not <code>null
    * </code>.
    * @param mappings The list of mappings to append to.  Assumed not
    * <code>null</code>.
    */
   private void addCommonTableKeys(PSBackEndTable table, List mappings)
   {
      // all tables must have the following
      PSSystemMapping sysMapping = new PSSystemMapping(table,
         IPSConstants.ITEM_PKEY_CONTENTID, new PSHtmlParameter(
            m_ceHandler.getParamName(
               PSContentEditorHandler.CONTENT_ID_PARAM_NAME)));
      mappings.add(sysMapping);

      sysMapping = new PSSystemMapping(table,
         IPSConstants.ITEM_PKEY_REVISIONID, new PSHtmlParameter(
            m_ceHandler.getParamName(
               PSContentEditorHandler.REVISION_ID_PARAM_NAME)));
      mappings.add(sysMapping);
   }


   /**
    * Adds contentid key to the mappings using the specified table.
    * This is specific for delete/purge item operation
    */
   private void addCommonTableKeysForDelete(PSBackEndTable table, List mappings)
   {
      // all tables must have the following
      PSSystemMapping sysMapping = new PSSystemMapping(table,
         IPSConstants.ITEM_PKEY_CONTENTID, new PSHtmlParameter(
            m_ceHandler.getParamName(
               PSContentEditorHandler.CONTENT_ID_PARAM_NAME)));
      mappings.add(sysMapping);
   }

   
   /**
    * Determines if the mapper contains a mapping that references a fieldSet
    * of the specified type.
    *
    * @param mapper The mapper to check.  Assumed not <code>null</code>.
    *
    * @param fieldSet The fieldSet the mapper references.  Assumed not <code>
    * null</code> and to be the fieldSet referenced by the supplied mapper.
    *
    * @param childType The child fieldset type to locate.  Must be a valid
    * PSFieldSet.TYPE_XXX_CHILD type.
    *
    * @return <code>true</code> if the mapper contains a mapping which
    * references the specified fieldset type, <code>false</code> if not.
    */
   private static boolean containsChildType(PSDisplayMapper mapper,
      PSFieldSet fieldSet, int childType)
   {
      boolean result = false;

      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         Object o = fieldSet.get(mapping.getFieldRef());
         if (o == null || o instanceof PSField)
            continue;

         PSFieldSet childFieldSet = (PSFieldSet)o;
         if (childFieldSet.getType() == childType)
            result = true;

         break;
      }

      return result;
   }



   /**
    * The main content editor handler. Set in ctor, never <code>null</code> or
    * modified after that.
    */
   protected PSContentEditorHandler m_ceHandler = null;

   /**
    * The application used to internally perform queries and updates.
    * Obtained in the constructor, never <code>null</code> after that.  Query
    * and update resources are added to this application as plans are built.
    */
   protected PSApplication m_internalApp = null;

   /**
    * Map of fieldsets that have been checked for binary fields. Key is the
    * PSFieldSet object, and value is a list of PSField objects that are binary
    * fields, which may be empty if the fieldset does not contain any binary
    * fields.  Never <code>null</code>, entries are added by calls to {@link
    * #getBinaryFields(PSFieldSet, boolean) getBinaryFields()}.
    */
   private Map m_binaryFields = new HashMap();

   /**
    * The content editor dataset this handler is processing commands
    * for, saved as a PSContentEditor reference for convenience. Never
    * <code>null</code> after construction.
    */
   protected PSContentEditor m_ce = null;

}

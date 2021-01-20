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

import com.percussion.cms.IPSConstants;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.jexl.PSDeployJexlUtils;
import com.percussion.deployer.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.idtypes.PSAppCEItemIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppConditionalIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppDataMappingIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppDisplayMapperIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppEntryIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppExtensionCallIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppExtensionParamIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppIndexedItemIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppNamedItemIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppUISetIdContext;
import com.percussion.deployer.objectstore.idtypes.PSAppUrlRequestIdContext;
import com.percussion.deployer.objectstore.idtypes.PSApplicationIdContext;
import com.percussion.deployer.objectstore.idtypes.PSBindingIdContext;
import com.percussion.deployer.objectstore.idtypes.PSBindingParamIdContext;
import com.percussion.deployer.objectstore.idtypes.PSJexlBinding;
import com.percussion.deployer.objectstore.idtypes.PSJexlBindings;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSMutatableReplacementValue;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSAbstractParamValue;
import com.percussion.design.objectstore.PSApplicationFlow;
import com.percussion.design.objectstore.PSApplyWhen;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSChoiceFilter;
import com.percussion.design.objectstore.PSChoices;
import com.percussion.design.objectstore.PSCloneOverrideField;
import com.percussion.design.objectstore.PSCloneOverrideFieldList;
import com.percussion.design.objectstore.PSCommandHandlerStylesheets;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSConditionalExit;
import com.percussion.design.objectstore.PSConditionalExtension;
import com.percussion.design.objectstore.PSConditionalRequest;
import com.percussion.design.objectstore.PSConditionalStylesheet;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSCustomActionGroup;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSFieldTranslation;
import com.percussion.design.objectstore.PSFieldValidationRules;
import com.percussion.design.objectstore.PSFormAction;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSProcessCheck;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.design.objectstore.PSVisibilityRules;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.util.PSCollection;
import com.percussion.util.PSUrlUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles discovery and transformations of literal ids specified in
 * application and other objectstore elements.
 */
public class PSAppTransformer
{
   /** 
    * Checks the supplied item data for literals and adds the required mappings
    * to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param itemData The item data to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.  
    */
   public static void checkItemData(List mappings, PSItemData itemData, 
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");
      
      if (itemData == null)
         throw new IllegalArgumentException("itemData may not be null");

      // Be sure we can locate the table name in the def
      String tableAlias = itemData.getTableAlias();      
      if (tableAlias == null)
         return;
      
      // get the root fieldset
      PSFieldSet fs = itemData.getParentFieldSet();
      checkFieldSetData(mappings, fs, itemData, ctx);
   }

   /**
    * Checks the supplied fieldset data for literals and adds the required 
    * mappings to the supplied list.
    * 
    * @param mappings The list, assumed not <code>null</code>.
    * @param fieldSet The fieldset being checked, assumed not <code>null</code>.
    * @param itemData The data to retrieve field values from, assumed not 
    * <code>null</code>.
    * @param ctx The current context, may be <code>null</code>. 
    */
   private static void checkFieldSetData(List mappings, PSFieldSet fieldSet, 
      PSItemData itemData, PSApplicationIdContext ctx)
   {
      if (fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
      {
         PSAppNamedItemIdContext fieldCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_ITEM_FIELD, fieldSet.getName());
         fieldCtx.setParentCtx(ctx);
         ctx = fieldCtx;
      }            

      // walk the fields 
      Iterator fields = fieldSet.getAll(false);
      while (fields.hasNext())
      {
         Object o = fields.next();
         if (o instanceof PSField)
         {
            checkFieldData(mappings, (PSField)o, itemData, fieldSet.getType(), 
               ctx);
         }
         else
         {
            // fieldset, check it
            checkFieldSetData(mappings, (PSFieldSet)o, itemData, ctx);
         }
      }
   }
   
   /**
    * Checks the supplied field data for literals and adds the required mappings
    * to the supplied list.
    * 
    * @param mappings The list, assumed not <code>null</code>.
    * @param field The field being checked, assumed not <code>null</code>.
    * @param itemData The data to retrieve field values from, assumed not 
    * <code>null</code>.
    * @param fieldSetType One of the <code>PSFieldSet.TYPE_XXX</code> values to
    * indicate the type of field set containing this field.
    * @param ctx The current context, may be <code>null</code>. 
    */
   private static void checkFieldData(List mappings, PSField field, 
      PSItemData itemData, int fieldSetType, PSApplicationIdContext ctx)
   {
      // make sure it's our table
      IPSBackEndMapping locator = field.getLocator();
      if (!(locator instanceof PSBackEndColumn))
      {
         return;
      }   
      
      PSBackEndColumn beCol = (PSBackEndColumn)locator;
      if (!beCol.getTable().getAlias().equalsIgnoreCase(
         itemData.getTableAlias()))
      {
         return;
      }
      
      // see if need to check for ids
      if (!field.getBooleanProperty(PSField.MAY_CONTAIN_IDS_PROPERTY))
         return;

      // get the field value(s) from the data
      Set vals = new HashSet();
      Iterator rows = itemData.getSrcTableData().getRows();
      while (rows.hasNext())
      {
         PSJdbcRowData row = (PSJdbcRowData) rows.next();
         
         // find our column value
         PSJdbcColumnData col = row.getColumn(beCol.getColumn(), true);
         
         // didn't find our column, done
         if (col == null)
            break;

         // get the column value
         String val = col.getValue();
         if (val == null || val.trim().length() == 0)
            continue;
         
         // copy reference of the context passed in so we can change it
         PSApplicationIdContext curCtx = ctx;
         
         // check child row id if needed and build context
         if (fieldSetType == PSFieldSet.TYPE_COMPLEX_CHILD)
         {
            PSJdbcColumnData sysIdCol = row.getColumn(
               IPSConstants.CHILD_ITEM_PKEY, true);
            if (sysIdCol == null)
               break;
            
            PSAppNamedItemIdContext childCtx = new PSAppNamedItemIdContext(
               PSAppNamedItemIdContext.TYPE_CHILD_ITEM, sysIdCol.getValue());
            childCtx.setParentCtx(curCtx);         
            curCtx = childCtx;         
         }
         
         PSAppNamedItemIdContext fieldCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_ITEM_FIELD, field.getSubmitName());
         fieldCtx.setParentCtx(curCtx);
         curCtx = fieldCtx;
         
         // See if we possibly have multiple values
         if (fieldSetType == PSFieldSet.TYPE_SIMPLE_CHILD)
         {
            PSAppNamedItemIdContext simpleCtx = new PSAppNamedItemIdContext(
               PSAppNamedItemIdContext.TYPE_SIMPLE_CHILD_VALUE, val);
            simpleCtx.setParentCtx(curCtx);
            curCtx = simpleCtx;
         }
         
         if (isNumeric(val))
         {
            // only process once for repeated values
            if (!vals.contains(val))
            {
               // add mapping
               PSApplicationIDTypeMapping mapping = 
                  new PSApplicationIDTypeMapping(curCtx, val);
               mappings.add(mapping);
               vals.add(val);
            }
            
         }
         else
         {
            // try as url
            Map paramMap = PSDeployComponentUtils.parseParams(val, null);
            Iterator entries = paramMap.entrySet().iterator();
            while (entries.hasNext())
            {
               Map.Entry entry = (Map.Entry)entries.next();            
               
               // convert to PSParam to leverage existing transformer code
               Iterator params = PSDeployComponentUtils.convertToParams(
                  entry).iterator();
               while (params.hasNext())
               {
                  PSParam param = (PSParam)params.next();
                  checkParam(mappings, param, curCtx);
               } 
            }
         }
      }      
   }

   /**
    * Transforms the supplied item data using the supplied mapping.
    *
    * @param itemData The item data to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformItemData(PSItemData itemData, 
      PSApplicationIDTypeMapping mapping, PSIdMap idMap) 
         throws PSDeployException
   {
      if (itemData == null)
         throw new IllegalArgumentException("itemData may not be null");
      
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      // peek the root ctx to validate
      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getNextRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid item data ctx");
      PSAppNamedItemIdContext fieldCtx = (PSAppNamedItemIdContext)root;
      if (fieldCtx.getType() != PSAppNamedItemIdContext.TYPE_ITEM_FIELD)
      {
         throw new IllegalArgumentException("invalid item data ctx");
      }
      
      // Get the table name
      String tableAlias = itemData.getTableAlias();      
      if (tableAlias == null)
         return;
      
      // get the root fieldset
      PSFieldSet fs = itemData.getParentFieldSet();
      transformFieldSetData(fs, itemData, mapping, idMap);
   }
   
   /**
    * Transforms the supplied item data using the supplied mapping and fieldset.
    *
    * @param fieldSet The fieldset to check for the field referenced by the
    * supplied mapping's context, assumed not <code>null</code>.
    * @param itemData The item data to transform, assumed not <code>null</code>.
    * @param mapping The mapping to use for the transformation, assumed not 
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    * 
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   private static void transformFieldSetData(PSFieldSet fieldSet, 
      PSItemData itemData, PSApplicationIDTypeMapping mapping, 
      PSIdMap idMap) throws PSDeployException
   {
      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid fieldset data ctx");
      PSAppNamedItemIdContext fieldCtx = (PSAppNamedItemIdContext)root;

      /*
       * If we get a complex child field set, we should have a child item 
       * context with an item field context below that.  If we get any other
       * fieldset type, we should just get an item field context.
       */
      boolean isChild = fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD;
      String fieldName = null;
      if (!isChild && fieldCtx.getType() == 
         PSAppNamedItemIdContext.TYPE_ITEM_FIELD)
      {
         fieldName = fieldCtx.getName();
      }
      else if (isChild && 
         fieldCtx.getType() == PSAppNamedItemIdContext.TYPE_CHILD_ITEM)
      {
         // In this case the child item context name is the sysid, and the
         // field name in the complex child fieldset is the name of the next
         // context down.
         PSApplicationIdContext nextCtx = ctx.getNextRootCtx();
         if (nextCtx instanceof PSAppNamedItemIdContext)
         {
            PSAppNamedItemIdContext childFieldCtx = 
               (PSAppNamedItemIdContext)nextCtx;
            fieldName = childFieldCtx.getName();
         }
         else
         {
            throw new IllegalArgumentException("invalid child item ctx");
         }
      }
      else
      {
         throw new IllegalArgumentException("invalid fieldset data ctx");
      }
      
      // reset the context we popped
      ctx.resetCurrentRootCtx();
      
      // walk the fieldset looking for match on field name
      Iterator fields = fieldSet.getAll(false);
      while (fields.hasNext())
      {
         Object o = fields.next();
         if (o instanceof PSField)
         {
            PSField field = (PSField)o;
            if (field.getSubmitName().equals(fieldName))
            {
               transformFieldData(field, itemData, mapping, idMap);
            }
         }
         else
         {
            PSFieldSet fs = (PSFieldSet)o;
            
            // see if we are expecting a complex child field
            ctx.getCurrentRootCtx(); // pop the previously checked ctx
            PSAppNamedItemIdContext nextCtx = 
               (PSAppNamedItemIdContext) ctx.getNextRootCtx();
            if (nextCtx.getType() == fieldCtx.TYPE_CHILD_ITEM && 
               fs.getName().equals(fieldName))
            {
               // leave the child item ctx as next root and recurse
               transformFieldSetData(fs, itemData, mapping, idMap);
               // push the item field context back as root now that we're done
               ctx.resetCurrentRootCtx();
            }
            else if (fs.getType() != fs.TYPE_COMPLEX_CHILD && 
               nextCtx.getType() != fieldCtx.TYPE_CHILD_ITEM)
            {
               // its a simple or shared field, so push the field ctx back as
               // root as we'll need it to find the field in the child fieldset
               ctx.resetCurrentRootCtx();
               transformFieldSetData(fs, itemData, mapping, idMap);
            }
            else
            {
               // not interested in this field set, reset the ctx for next loop
               ctx.resetCurrentRootCtx();
            }
         }
      }
   }

   /**
    * Transforms the supplied item data using the supplied mapping and field.
    * 
    * @param field The field referenced by the supplied mapping's context, 
    * assumed not <code>null</code>.
    * @param itemData The item data to transform, assumed not <code>null</code>.
    * @param mapping The mapping to use for the transformation, assumed not 
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    * 
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   private static void transformFieldData(PSField field, PSItemData itemData, 
      PSApplicationIDTypeMapping mapping, PSIdMap idMap) 
         throws PSDeployException
   {
      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid field data ctx");
      PSAppNamedItemIdContext fieldCtx = (PSAppNamedItemIdContext)root;
      PSAppNamedItemIdContext childItemCtx = null; 
      PSAppNamedItemIdContext simpleCtx = null;
      
      try
      {
         boolean isChild = false;
         PSApplicationIdContext nextCtx;
         if (fieldCtx.getType() == fieldCtx.TYPE_CHILD_ITEM)
         {
            isChild = true;
            childItemCtx = fieldCtx;
            nextCtx = ctx.getNextRootCtx();
            if (!(nextCtx instanceof PSAppNamedItemIdContext))
               throw new IllegalArgumentException("invalid field data ctx");   
            fieldCtx = (PSAppNamedItemIdContext) ctx.getCurrentRootCtx();
         }
         
         if (fieldCtx.getType() != fieldCtx.TYPE_ITEM_FIELD)
            throw new IllegalArgumentException("invalid field data ctx");

         // make sure it's our table
         IPSBackEndMapping locator = field.getLocator();
         if (!(locator instanceof PSBackEndColumn))
         {
            return;
         }   
         
         PSBackEndColumn beCol = (PSBackEndColumn)locator;
         if (!beCol.getTable().getAlias().equalsIgnoreCase(
            itemData.getTableAlias()))
         {
            return;
         }
         
         // check for simple child values       
         nextCtx = ctx.getNextRootCtx();
         if (nextCtx instanceof PSAppNamedItemIdContext)
         {
            PSAppNamedItemIdContext test = (PSAppNamedItemIdContext) nextCtx;
            if (test.getType() == test.TYPE_SIMPLE_CHILD_VALUE)
            {
               simpleCtx = (PSAppNamedItemIdContext) ctx.getCurrentRootCtx();
               fieldCtx = simpleCtx;
            }
         }
         
         // see if we have a literal, or a url pattern
         boolean isUrl = false;
         nextCtx = ctx.getNextRootCtx();
         if (nextCtx instanceof PSAppNamedItemIdContext)
         {
            PSAppNamedItemIdContext paramCtx = (PSAppNamedItemIdContext)nextCtx;
            if (paramCtx.getType() == PSAppNamedItemIdContext.TYPE_PARAM)
               isUrl = true;
         }
         
         // transform the field, checking source values and updating target values
         // as some target values may have already changed, so we can't use them
         // to identify field values to transform.  Walk target in tandem.
         String mappedSrcVal = mapping.getValue();
         Iterator srcRows = itemData.getSrcTableData().getRows();
         Iterator tgtRows = itemData.getTgtTableData().getRows();
         while (srcRows.hasNext())
         {
            if (!tgtRows.hasNext())
            {
               throw new IllegalArgumentException(
                  "target data has fewer rows than source");
            }
            
            PSJdbcRowData srcRow = (PSJdbcRowData) srcRows.next();
            PSJdbcRowData tgtRow = (PSJdbcRowData) tgtRows.next();
            
            // find our column value
            PSJdbcColumnData srcCol = srcRow.getColumn(beCol.getColumn(), true);
            PSJdbcColumnData tgtCol = tgtRow.getColumn(beCol.getColumn(), true);
                     
            // didn't find our column - a bug
            if (srcCol == null || tgtCol == null)
               throw new IllegalArgumentException(
                  "column to transform not found");
            
            // if a complex child, ensure it's the right row
            if (isChild)
            {
               PSJdbcColumnData sysIdCol = tgtRow.getColumn(
                  IPSConstants.CHILD_ITEM_PKEY, true);
               if (sysIdCol == null)
                  throw new IllegalArgumentException(
                     "child row id column not found");
               if (!childItemCtx.getName().equals(sysIdCol.getValue()))
                  continue;
            }
               
            /* TODO: there is a bug here.  only one source value has a mapped
             * defined and will be transformed.  if other rows have a different
             * value they are installed on the target without being transformed
             * (invalid id values are installed to the target).
             */
            String newTgtVal = null;
            String srcVal = srcCol.getValue();
            if (!isUrl && srcVal != null && srcVal.equals(mappedSrcVal))
            {
               newTgtVal = String.valueOf(getNewId(mapping, idMap));               
            }
            else if (isUrl)
            {
               // in this case, need to work on the target value as other params
               // in it may have been modified aleady, and these can be 
               // identified by their name.
               StringBuffer base = new StringBuffer();
               Map paramMap = PSDeployComponentUtils.parseParams(
                  tgtCol.getValue(), base);
               
               Iterator entries = paramMap.entrySet().iterator();
               while (entries.hasNext())
               {
                  Map.Entry entry = (Map.Entry)entries.next();            
                  
                  // convert to PSParam to leverage existing transformer code
                  List valList = new ArrayList();
                  Iterator params = PSDeployComponentUtils.convertToParams(
                     entry).iterator();
                  while (params.hasNext())
                  {
                     PSParam param = (PSParam)params.next();
                     transformParam(param, mapping, idMap);
                     valList.add(param.getValue().getValueText());
                  } 

                  
                  Object newVal;
                  if (valList.size() > 1)
                     newVal = valList;
                  else
                     newVal = valList.get(0);
                     
                  entry.setValue(newVal);                
               }
               
               // reassemble and set back on tgt
               newTgtVal = PSUrlUtils.createUrl(base.toString(), 
                  PSDeployComponentUtils.convertToEntries(paramMap), null);
            }
            
            if (newTgtVal != null)
            {
               tgtCol.setValue(newTgtVal);
               // update simple child context value if we have one
               if (simpleCtx != null)
                  simpleCtx.updateCtxValue(newTgtVal);
            }
         }
         
      }
      finally
      {
         // push simple ctx back to root if we got one 
         if (simpleCtx != null)
            ctx.resetCurrentRootCtx();
         
         // push field ctx back to root
         ctx.resetCurrentRootCtx();
         
         // push complex child ctx back to root if we got one
         if (childItemCtx != null)
            ctx.resetCurrentRootCtx();
      }
   }

   /**
    * Checks the supplied field set for literals and adds the required mappings
    * to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param fs The field set to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkFieldSet(List mappings, PSFieldSet fs,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (fs == null)
         throw new IllegalArgumentException("fs may not be null");

      PSAppNamedItemIdContext fsCtx = new PSAppNamedItemIdContext(
         PSAppNamedItemIdContext.TYPE_FIELD_SET,
         fs.getName());
      fsCtx.setParentCtx(ctx);
      Iterator fields = fs.getEveryField();
      while (fields.hasNext())
      {
         Object o = fields.next();
         if (o instanceof PSField)
         {
            // check field data locator
            PSField field = (PSField)o;
            PSAppNamedItemIdContext fieldCtx = new PSAppNamedItemIdContext(
               PSAppNamedItemIdContext.TYPE_CE_FIELD,
               field.getSubmitName());
            fieldCtx.setParentCtx(fsCtx);
            if (field.getLocator() != null)
               checkDataLocator(mappings, field.getLocator(), fieldCtx);

            // check field default value
            IPSReplacementValue defVal = field.getDefault();
            if (defVal != null)
            {
               PSAppCEItemIdContext defCtx = new PSAppCEItemIdContext(
                  PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
               defCtx.setParentCtx(fieldCtx);
               checkReplacementValue(mappings, defVal, defCtx);
            }

            // check translations
            PSFieldTranslation fTrans = field.getInputTranslation();
            if (fTrans != null)
            {
               PSExtensionCallSet callSet = fTrans.getTranslations();
               if (!callSet.isEmpty())
               {
                  PSAppCEItemIdContext iTransCtx =
                     new PSAppCEItemIdContext(
                        PSAppCEItemIdContext.TYPE_INPUT_TRANSLATION);
                  iTransCtx.setParentCtx(fieldCtx);
                  checkExtensionCalls(mappings, callSet.iterator(), iTransCtx);
               }
            }

            fTrans = field.getOutputTranslation();
            if (fTrans != null)
            {
               PSExtensionCallSet callSet = fTrans.getTranslations();
               if (!callSet.isEmpty())
               {
                  PSAppCEItemIdContext oTransCtx =
                     new PSAppCEItemIdContext(
                        PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION);
                  oTransCtx.setParentCtx(fieldCtx);
                  checkExtensionCalls(mappings, callSet.iterator(), oTransCtx);
               }
            }

            // check validation rules
            PSFieldValidationRules valRules = field.getValidationRules();
            if (valRules != null)
            {
               PSAppCEItemIdContext valRuleCtx =
                  new PSAppCEItemIdContext(
                     PSAppCEItemIdContext.TYPE_VALIDATION_RULE);
               valRuleCtx.setParentCtx(fieldCtx);
               checkRules(mappings, valRules.getRules(), valRuleCtx);
               PSApplyWhen applyWhen = valRules.getApplyWhen();
               if (applyWhen != null)
                  checkApplyWhen(mappings, applyWhen, valRuleCtx);
            }


            // check visibility rules
            PSVisibilityRules visRules = field.getVisibilityRules();
            if (visRules != null)
            {
               PSAppCEItemIdContext visRuleCtx =
                  new PSAppCEItemIdContext(
                     PSAppCEItemIdContext.TYPE_VISIBILITY_RULE);
               visRuleCtx.setParentCtx(fieldCtx);
               checkRules(mappings, visRules.iterator(), visRuleCtx);
            }

            // check choices
            PSChoices choices = field.getChoices();
            if (choices != null)
               checkChoices(mappings, choices, fieldCtx);
         }
         else if (o instanceof PSFieldSet)
         {
            checkFieldSet(mappings, (PSFieldSet)o, fsCtx);
         }
      }
   }

   /**
    * Transforms the supplied fieldset using the supplied mapping.
    *
    * @param fieldSet The field set to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformFieldSet(PSFieldSet fieldSet,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (fieldSet == null)
         throw new IllegalArgumentException("fieldSet may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid field set ctx");
      PSAppNamedItemIdContext fsCtx = (PSAppNamedItemIdContext)root;
      if (fsCtx.getType() != PSAppNamedItemIdContext.TYPE_FIELD_SET ||
         !fsCtx.getName().equals(fieldSet.getName()))
      {
         throw new IllegalArgumentException("invalid field set ctx");
      }

      // now get context of the field or fieldset within the fieldset that we
      // need to transform
      root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid child of field set ctx");

      PSAppNamedItemIdContext childCtx = (PSAppNamedItemIdContext)root;
      Object child = fieldSet.get(childCtx.getName());
      if (childCtx.getType() == PSAppNamedItemIdContext.TYPE_FIELD_SET)
      {
         ctx.resetCurrentRootCtx(); // don't use the root here
         if (child instanceof PSFieldSet)
            transformFieldSet((PSFieldSet)child, mapping, idMap);
      }
      else if (childCtx.getType() == PSAppNamedItemIdContext.TYPE_CE_FIELD)
      {
         // get the field
         if (child instanceof PSField)
         {
            PSField field = (PSField)child;
            // xform data locator
            PSApplicationIdContext nextRoot = ctx.getNextRootCtx();
            if (!(nextRoot instanceof PSAppCEItemIdContext))
               throw new IllegalArgumentException("invalid child of field ctx");
            PSAppCEItemIdContext nextCtx = (PSAppCEItemIdContext)nextRoot;
            if (nextCtx.getType() == PSAppCEItemIdContext.TYPE_DATA_LOCATOR)
               transformDataLocator(field.getLocator(), mapping, idMap);
            else if (nextCtx.getType() == PSAppCEItemIdContext.TYPE_CHOICES &&
               field.getChoices() != null)
            {
               transformChoices(field.getChoices(), mapping, idMap);
            }
            else
            {
               nextCtx = (PSAppCEItemIdContext)ctx.getCurrentRootCtx();

               switch (nextCtx.getType())
               {
                  case PSAppCEItemIdContext.TYPE_DEFAULT_VALUE:
                     if (field.getDefault() != null)
                        transformReplacementValue(field.getDefault(), mapping,
                           idMap);
                     break;
                  case PSAppCEItemIdContext.TYPE_INPUT_TRANSLATION:
                     PSFieldTranslation iTrans = field.getInputTranslation();
                     if (iTrans != null)
                        transformExtensionCalls(
                           iTrans.getTranslations().iterator(), mapping, idMap);
                     break;
                  case PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION:
                     PSFieldTranslation oTrans = field.getOutputTranslation();
                     if (oTrans != null)
                        transformExtensionCalls(
                           oTrans.getTranslations().iterator(), mapping, idMap);
                     break;
                  case PSAppCEItemIdContext.TYPE_VALIDATION_RULE:
                     PSFieldValidationRules valRules =
                        field.getValidationRules();
                     if (valRules != null)
                     {
                        nextRoot = ctx.getNextRootCtx();
                        if (nextRoot instanceof PSAppIndexedItemIdContext)
                           transformRules(valRules.getRules(), mapping, idMap);
                        else if (nextRoot instanceof PSAppCEItemIdContext)
                           transformApplyWhen(valRules.getApplyWhen(), mapping,
                              idMap);
                        else
                           throw new IllegalArgumentException(
                              "invalid validation rule ctx");
                     }
                     break;
                  case PSAppCEItemIdContext.TYPE_VISIBILITY_RULE:
                     PSVisibilityRules visRules = field.getVisibilityRules();
                     if (visRules != null)
                     {
                        transformRules(visRules.iterator(), mapping, idMap);
                     }
                     break;
                  default:
                     throw new IllegalArgumentException(
                        "invalid child of field ctx");
               }


               ctx.resetCurrentRootCtx(); // for field child
            }
         }

         ctx.resetCurrentRootCtx(); // for field
      }
      else
         throw new IllegalArgumentException("invalid child of field set ctx");

      ctx.resetCurrentRootCtx(); // for the field set

   }

   /**
    * Checks the supplied ui def for literals and adds the required mappings
    * to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param uiDef The ui def to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkUIDef(List mappings, PSUIDefinition uiDef,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (uiDef == null)
         throw new IllegalArgumentException("uiDef may not be null");

      // check default ui
      Iterator uiSets = uiDef.getDefaultUI();
      if (uiSets != null)
      {
         PSAppCEItemIdContext uiDefCtx = new PSAppCEItemIdContext(
            PSAppCEItemIdContext.TYPE_DEFAULT_UI);
         uiDefCtx.setParentCtx(ctx);
         while (uiSets.hasNext())
         {
            checkUISet(mappings, (PSUISet)uiSets.next(), uiDefCtx);
         }
      }

      // now check mappers
      checkDisplayMapper(mappings, uiDef.getDisplayMapper(), ctx);
   }

   /**
    * Transforms the supplied ui def using the supplied mapping.
    *
    * @param uiDef The ui def to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if the mapping's context is not valid
    * for this method or any param is <code>null</code>.
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformUIDef(PSUIDefinition uiDef,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (uiDef == null)
         throw new IllegalArgumentException("uiDef may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (root instanceof PSAppCEItemIdContext)
      {
         PSAppCEItemIdContext defUICtx = (PSAppCEItemIdContext)root;
         if (defUICtx.getType() != PSAppCEItemIdContext.TYPE_DEFAULT_UI)
            throw new IllegalArgumentException("invalid default ui ctx");

         Iterator uiSets = uiDef.getDefaultUI();
         if (ctx.getNextRootCtx() instanceof PSAppUISetIdContext &&
            uiSets.hasNext())
         {
            PSAppUISetIdContext setCtx =
               (PSAppUISetIdContext)ctx.getNextRootCtx();
            String setName = setCtx.getName();
            if (setName == null)
              throw new IllegalArgumentException(
               "invalid ui set ctx in default ui ctx");

            while (uiSets.hasNext())
            {
               PSUISet uiSet = (PSUISet)uiSets.next();
               if (setName.equals(uiSet.getName()))
               {
                  transformUISet(uiSet, mapping, idMap);
                  break;
               }
            }
         }

         ctx.resetCurrentRootCtx();
      }
      else
      {
         ctx.resetCurrentRootCtx();
         transformDisplayMapper(uiDef.getDisplayMapper(), mapping, idMap);
      }
   }

   /**
    * Checks the supplied display mapper for literals and adds the required
    * mappings to the supplied list.  Recursively checks any child mappers.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param mapper The mappper to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkDisplayMapper(List mappings, PSDisplayMapper mapper,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (mapper == null)
         throw new IllegalArgumentException("mapper may not be null");

      PSAppDisplayMapperIdContext mapperCtx = new PSAppDisplayMapperIdContext(
         mapper);
      mapperCtx.setParentCtx(ctx);
      Iterator dispMappings = mapper.iterator();
      while (dispMappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)dispMappings.next();

         PSAppNamedItemIdContext mappingCtx =
            new PSAppNamedItemIdContext(
               PSAppNamedItemIdContext.TYPE_DISPLAY_MAPPING,
               mapping.getFieldRef());
         mappingCtx.setParentCtx(mapperCtx);

         checkUISet(mappings, mapping.getUISet(), mappingCtx);

         PSDisplayMapper childMapper = mapping.getDisplayMapper();
         if (childMapper != null)
            checkDisplayMapper(mappings, childMapper, mappingCtx);
      }
   }

   /**
    * Transforms the supplied display mapper using the supplied mapping.
    *
    * @param mapper The mappper to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformDisplayMapper(PSDisplayMapper mapper,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (mapper == null)
         throw new IllegalArgumentException("mapper may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppDisplayMapperIdContext))
         throw new IllegalArgumentException("invalid display mapper ctx");

      root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid display mapping ctx");
      PSAppNamedItemIdContext mappingCtx = (PSAppNamedItemIdContext)root;
      if (mappingCtx.getType() != PSAppNamedItemIdContext.TYPE_DISPLAY_MAPPING)
         throw new IllegalArgumentException("invalid display mapping ctx");

      Iterator dispMappings = mapper.iterator();
      while (dispMappings.hasNext())
      {
         PSDisplayMapping dispMapping = (PSDisplayMapping)dispMappings.next();
         if (!mappingCtx.getName().equals(dispMapping.getFieldRef()))
            continue;

         if (ctx.getNextRootCtx() instanceof PSAppUISetIdContext)
         {
            transformUISet(dispMapping.getUISet(), mapping, idMap);
         }
         else
         {
            PSDisplayMapper childMapper = dispMapping.getDisplayMapper();
            if (childMapper != null)
               transformDisplayMapper(childMapper, mapping, idMap);
         }
         break;
      }

      ctx.resetCurrentRootCtx();
      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the supplied application flow for literals and adds the required
    * mappings to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param appFlow The appFlow to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkAppFlow(List mappings, PSApplicationFlow appFlow,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (appFlow == null)
         throw new IllegalArgumentException("appFlow may not be null");

      Iterator names = appFlow.getCommandHandlerNames();
      while (names.hasNext())
      {
         String name = (String)names.next();
         PSAppNamedItemIdContext flowCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_APP_FLOW, name);
         flowCtx.setParentCtx(ctx);

         int index = 0;
         Iterator redirects = appFlow.getRedirects(name);
         while (redirects.hasNext())
         {
            PSConditionalRequest req = (PSConditionalRequest)redirects.next();
            PSAppIndexedItemIdContext reqCtx = new PSAppIndexedItemIdContext(
               PSAppIndexedItemIdContext.TYPE_CONDITIONAL_REQUEST, index++);
            reqCtx.setParentCtx(flowCtx);
            
            // if on last one, ignore conditional component - its a dummy
            if (redirects.hasNext())
               checkRules(mappings, req.getConditions(), reqCtx);
            checkUrlRequest(mappings, req, reqCtx);
         }
      }
   }

   /**
    * Transforms the supplied application flow using the supplied mapping.
    *
    * @param appFlow The appflow to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformAppFlow(PSApplicationFlow appFlow,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (appFlow == null)
         throw new IllegalArgumentException("appFloww may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid appflow ctx");
      PSAppNamedItemIdContext flowCtx = (PSAppNamedItemIdContext)root;
      if (flowCtx.getType() != PSAppNamedItemIdContext.TYPE_APP_FLOW)
         throw new IllegalArgumentException("invalid appflow ctx");
      Iterator redirects = appFlow.getRedirects(flowCtx.getName());

      root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppIndexedItemIdContext))
         throw new IllegalArgumentException("invalid conditional request ctx");
      PSAppIndexedItemIdContext reqCtx = (PSAppIndexedItemIdContext)root;
      if (reqCtx.getType() !=
         PSAppIndexedItemIdContext.TYPE_CONDITIONAL_REQUEST)
      {
         throw new IllegalArgumentException("invalid conditional request ctx");
      }

      int index = 0;
      while (redirects.hasNext())
      {
         PSConditionalRequest req = (PSConditionalRequest)redirects.next();
         if (index++ == reqCtx.getIndex())
         {
            if (ctx.getNextRootCtx() instanceof PSAppIndexedItemIdContext)
               transformRules(req.getConditions(), mapping, idMap);
            else
               transformUrlRequest(req, mapping, idMap);
            break;
         }
      }
      ctx.resetCurrentRootCtx(); // bump up to appflow ctx
      ctx.resetCurrentRootCtx(); // bump up to what was passed in
   }

   /**
    * Checks the supplied data mapper for literals and adds the required
    * mappings to the supplied list.  Recursively checks any child mappers.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param mapper The mappper to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkDataMapper(List mappings, PSDataMapper mapper,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (mapper == null)
         throw new IllegalArgumentException("mapper may not be null");

      // check mappings
      Iterator dataMappings = mapper.iterator();
      while (dataMappings.hasNext())
      {
         PSDataMapping mapping = (PSDataMapping)dataMappings.next();
         if (mapping.getBackEndMapping() instanceof IPSReplacementValue)
         {
            PSAppDataMappingIdContext mappingCtx =
               new PSAppDataMappingIdContext(mapping,
                  PSAppDataMappingIdContext.TYPE_BACK_END);
            mappingCtx.setParentCtx(ctx);
            checkReplacementValue(mappings,
               (IPSReplacementValue)mapping.getBackEndMapping(), mappingCtx);
         }

         if (mapping.getDocumentMapping() instanceof IPSReplacementValue)
         {
            PSAppDataMappingIdContext mappingCtx =
               new PSAppDataMappingIdContext(mapping,
                  PSAppDataMappingIdContext.TYPE_XML);
            mappingCtx.setParentCtx(ctx);
            checkReplacementValue(mappings,
               (IPSReplacementValue)mapping.getDocumentMapping(), mappingCtx);
         }

         PSCollection conds = mapping.getConditionals();
         if (conds != null)
         {
            PSAppDataMappingIdContext mappingCtx =
               new PSAppDataMappingIdContext(mapping,
                  PSAppDataMappingIdContext.TYPE_COND);
            mappingCtx.setParentCtx(ctx);
            checkConditionals(mappings, conds.iterator(), mappingCtx);
         }
      }
   }

   /**
    * Transforms the supplied data mapper using the supplied mapping.  Also
    * transforms the context value.
    *
    * @param mapper The mappper to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformDataMapper(PSDataMapper mapper,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (mapper == null)
         throw new IllegalArgumentException("mapper may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppDataMappingIdContext))
         throw new IllegalArgumentException("invalid data mapping ctx");
      PSAppDataMappingIdContext mappingCtx = (PSAppDataMappingIdContext)root;

      Iterator dataMappings = mapper.iterator();
      while (dataMappings.hasNext())
      {
         PSDataMapping dataMapping = (PSDataMapping)dataMappings.next();
         if (mappingCtx.isSameMapping(dataMapping, true))
         {
            if (mappingCtx.getType() == PSAppDataMappingIdContext.TYPE_BACK_END)
            {
               transformReplacementValue(
                  (IPSReplacementValue)dataMapping.getBackEndMapping(), mapping,
                     idMap);
               mappingCtx.updateCtxValue(
                  (IPSReplacementValue)dataMapping.getBackEndMapping());
            }
            else if (mappingCtx.getType() == PSAppDataMappingIdContext.TYPE_XML)
            {
               transformReplacementValue(
                  (IPSReplacementValue)dataMapping.getDocumentMapping(),
                   mapping, idMap);
               mappingCtx.updateCtxValue(
                  (IPSReplacementValue)dataMapping.getDocumentMapping());
            }
            else if (mappingCtx.getType() == PSAppDataMappingIdContext.TYPE_COND)
            {
               PSCollection conds = dataMapping.getConditionals();
               if (conds != null)
                 transformConditionals(conds.iterator(), mapping, idMap);
            }
         }
      }
      ctx.resetCurrentRootCtx();
   }
   /**
    * Checks the conditional exits for literals and adds the required
    * mappings to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param exits An iterator over zero or more
    * <code>PSConditionalExit</code> objects to check, may not be
    * <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkConditionalExits(List mappings, Iterator exits,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (exits == null)
         throw new IllegalArgumentException("exits may not be null");

      int index = 0;
      while (exits.hasNext())
      {
         PSConditionalExit exit = (PSConditionalExit)exits.next();
         PSAppIndexedItemIdContext exitCtx = new PSAppIndexedItemIdContext(
            PSAppIndexedItemIdContext.TYPE_CONDITIONAL_EXIT, index++);
         exitCtx.setParentCtx(ctx);
         PSApplyWhen applyWhen = exit.getCondition();
         if (applyWhen != null)
            checkApplyWhen(mappings, applyWhen, exitCtx);
         checkExtensionCalls(mappings, exit.getRules().iterator(), exitCtx);
      }
   }

   /**
    * Transforms the supplied conditional exits using the supplied mapping.
    *
    * @param exits An iterator over zero or more
    * <code>PSConditionalExit</code> objects to transform, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformConditionalExits(Iterator exits,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (exits == null)
         throw new IllegalArgumentException("exits may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppIndexedItemIdContext))
         throw new IllegalArgumentException("invalid conditional exit ctx");
      PSAppIndexedItemIdContext exitCtx = (PSAppIndexedItemIdContext)root;
      if (exitCtx.getType() != PSAppIndexedItemIdContext.TYPE_CONDITIONAL_EXIT)
         throw new IllegalArgumentException("invalid conditional exit ctx");

      int index = 0;
      while (exits.hasNext())
      {
         PSConditionalExit exit = (PSConditionalExit)exits.next();
         if (index++ == exitCtx.getIndex())
         {
            if (ctx.getNextRootCtx() instanceof PSAppCEItemIdContext)
            {
               PSApplyWhen applyWhen = exit.getCondition();
               if (applyWhen != null)
                  transformApplyWhen(applyWhen, mapping, idMap);
            }
            else
            {
               transformExtensionCalls(exit.getRules().iterator(), mapping,
                  idMap);
            }
         }
      }

      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the conditional effects for literals and adds the required
    * mappings to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param effects An iterator over zero or more
    * <code>PSConditionalEffect</code> objects to check, may not be
    * <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkConditionalEffects(List mappings, Iterator effects,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (effects == null)
         throw new IllegalArgumentException("effects may not be null");

      int index = 0;
      while (effects.hasNext())
      {
         PSConditionalEffect effect = (PSConditionalEffect)effects.next();
         PSAppIndexedItemIdContext effectCtx = new PSAppIndexedItemIdContext(
            PSAppIndexedItemIdContext.TYPE_CONDITIONAL_EFFECT, index++);
         effectCtx.setParentCtx(ctx);
         checkRules(mappings, effect.getConditions(), effectCtx);

         PSExtensionCall call = effect.getEffect();
         PSAppExtensionCallIdContext callCtx = new PSAppExtensionCallIdContext(
            call);
         callCtx.setParentCtx(effectCtx);
         checkCallParams(mappings, call.getParameters().iterator(), callCtx);
      }
   }

   /**
    * Transforms the supplied conditional effects using the supplied mapping.
    *
    * @param effects An iterator over zero or more
    * <code>PSConditionalEffect</code> objects to transform, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformConditionalEffects(Iterator effects,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (effects == null)
         throw new IllegalArgumentException("effects may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppIndexedItemIdContext))
         throw new IllegalArgumentException("invalid conditional exit ctx");
      PSAppIndexedItemIdContext effectCtx = (PSAppIndexedItemIdContext)root;
      if (effectCtx.getType() !=
         PSAppIndexedItemIdContext.TYPE_CONDITIONAL_EFFECT)
      {
         throw new IllegalArgumentException("invalid conditional exit ctx");
      }

      int index = 0;
      while (effects.hasNext())
      {
         PSConditionalEffect effect = (PSConditionalEffect)effects.next();
         if (index++ == effectCtx.getIndex())
         {
            if (ctx.getNextRootCtx() instanceof PSAppExtensionCallIdContext)
            {
               ctx.getCurrentRootCtx();
               transformCallParams(
                  effect.getEffect().getParameters().iterator(), mapping,
                  idMap);
               ctx.resetCurrentRootCtx();
            }
            else
            {
               transformRules(effect.getConditions(), mapping, idMap);
            }
         }
      }

      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the conditional extensions for literals and adds the required
    * mappings to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param extensions An iterator over zero or more
    * <code>PSConditionalExtension</code> objects to check, may not be
    * <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkConditionalExtensions(List mappings,
      Iterator extensions, PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (extensions == null)
         throw new IllegalArgumentException("extensions may not be null");

      int index = 0;
      while (extensions.hasNext())
      {
         PSConditionalExtension extension =
            (PSConditionalExtension)extensions.next();
         PSAppIndexedItemIdContext extesnionCtx = new PSAppIndexedItemIdContext(
            PSAppIndexedItemIdContext.TYPE_CONDITIONAL_EXTENSION, index++);
         extesnionCtx.setParentCtx(ctx);
         checkRules(mappings, extension.getConditions(), extesnionCtx);

         PSExtensionCall call = extension.getExtension();
         PSAppExtensionCallIdContext callCtx = new PSAppExtensionCallIdContext(
            call);
         callCtx.setParentCtx(extesnionCtx);
         checkCallParams(mappings, call.getParameters().iterator(), callCtx);
      }
   }

   /**
    * Transforms the supplied conditional extensions using the supplied mapping.
    *
    * @param extensions An iterator over zero or more
    * <code>PSConditionalExtension</code> objects to transform, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformConditionalExtensions(Iterator extensions,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (extensions == null)
         throw new IllegalArgumentException("effects may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppIndexedItemIdContext))
         throw new IllegalArgumentException(
            "invalid conditional extension ctx");
      PSAppIndexedItemIdContext extensionCtx = (PSAppIndexedItemIdContext)root;
      if (extensionCtx.getType() !=
         PSAppIndexedItemIdContext.TYPE_CONDITIONAL_EXTENSION)
      {
         throw new IllegalArgumentException(
            "invalid conditional extension ctx");
      }

      int index = 0;
      while (extensions.hasNext())
      {
         PSConditionalExtension extension =
            (PSConditionalExtension)extensions.next();
         if (index++ == extensionCtx.getIndex())
         {
            if (ctx.getNextRootCtx() instanceof PSAppExtensionCallIdContext)
            {
               ctx.getCurrentRootCtx();
               transformCallParams(
                  extension.getExtension().getParameters().iterator(), mapping,
                  idMap);
               ctx.resetCurrentRootCtx();
            }
            else
            {
               transformRules(extension.getConditions(), mapping, idMap);
            }
         }
      }

      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the custom action groups for literals and adds the required
    * mappings to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param groups An iterator over zero or more
    * <code>PSCustomActionGroup</code> objects to check, may not be
    * <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkCustomActionGroups(List mappings, Iterator groups,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (groups == null)
         throw new IllegalArgumentException("groups may not be null");

      int index = 0;
      while (groups.hasNext())
      {
         PSCustomActionGroup custGroup = (PSCustomActionGroup)groups.next();
         if (custGroup != null)
         {
            PSFormAction formAction = custGroup.getFormAction();
            if (formAction != null)
            {
               PSAppIndexedItemIdContext grpCtx = new PSAppIndexedItemIdContext(
                  PSAppIndexedItemIdContext.TYPE_CUSTOM_ACTION_GROUP, index++);
               grpCtx.setParentCtx(ctx);
               PSUrlRequest urlReq = formAction.getAction();
               if (urlReq != null)
               {
                  checkUrlRequest(mappings, urlReq, grpCtx);
               }
            }
         }
      }
   }

   /**
    * Transforms the supplied custom action groups using the supplied mapping.
    *
    * @param groups An iterator over zero or more
    * <code>PSCustomActionGroup</code> objects to transform, may not be
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformCustomActionGroups(Iterator groups,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (groups == null)
         throw new IllegalArgumentException("groups may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppIndexedItemIdContext))
         throw new IllegalArgumentException("invalid group ctx");
      PSAppIndexedItemIdContext groupCtx = (PSAppIndexedItemIdContext)root;
      if (groupCtx.getType() !=
         PSAppIndexedItemIdContext.TYPE_CUSTOM_ACTION_GROUP)
      {
         throw new IllegalArgumentException("invalid group ctx");
      }

      int index = 0;
      while (groups.hasNext())
      {
         PSCustomActionGroup custGroup = (PSCustomActionGroup)groups.next();
         if (index++ == groupCtx.getIndex())
         {
            if (custGroup != null)
            {
               PSFormAction formAction = custGroup.getFormAction();
               if (formAction != null)
               {
                  PSUrlRequest urlReq = formAction.getAction();
                  if (urlReq != null)
                  {
                     transformUrlRequest(urlReq, mapping, idMap);
                  }
               }
            }
            break;
         }
      }
      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the stylesheet set for literals and adds the required
    * mappings to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param sheets The set to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkStylesheetSet(List mappings,
      PSCommandHandlerStylesheets sheets, PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (sheets == null)
         throw new IllegalArgumentException("sheets may not be null");

      Iterator names = sheets.getCommandHandlerNames();
      while (names.hasNext())
      {
         String name = (String)names.next();
         PSAppNamedItemIdContext sheetsCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_STYLESHEET_SET, name);
         sheetsCtx.setParentCtx(ctx);
         int index = 0;
         Iterator styleSheets = sheets.getStylesheets(name);
         while (styleSheets.hasNext())
         {
            PSAppIndexedItemIdContext sheetCtx = new PSAppIndexedItemIdContext(
               PSAppIndexedItemIdContext.TYPE_CONDITIONAL_STYLESHEET, index++);
            PSConditionalStylesheet sheet =
               (PSConditionalStylesheet)styleSheets.next();

            // don't check conditions for last sheet, they are fake
            if (styleSheets.hasNext())
               checkRules(mappings, sheet.getConditions(), sheetCtx);
            checkUrlRequest(mappings, sheet.getRequest(), sheetCtx);
         }
      }
   }

   /**
    * Transforms the supplied stylesheet set using the supplied mapping.
    *
    * @param sheets The set to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformStylesheetSet(
      PSCommandHandlerStylesheets sheets, PSApplicationIDTypeMapping mapping,
      PSIdMap idMap) throws PSDeployException
   {
      if (sheets == null)
         throw new IllegalArgumentException("sheets may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid stylesheet set ctx");
      PSAppNamedItemIdContext sheetsCtx = (PSAppNamedItemIdContext)root;
      if (sheetsCtx.getType() != PSAppNamedItemIdContext.TYPE_STYLESHEET_SET)
         throw new IllegalArgumentException("invalid stylesheet set ctx");

      Iterator names = sheets.getCommandHandlerNames();
      while (names.hasNext())
      {
         String name = (String)names.next();
         if (!sheetsCtx.getName().equals(name))
            continue;

         root = ctx.getCurrentRootCtx();
         if (!(root instanceof PSAppIndexedItemIdContext))
            throw new IllegalArgumentException("invalid stylesheet ctx");
         PSAppIndexedItemIdContext sheetCtx = (PSAppIndexedItemIdContext)root;
         int index = 0;
         Iterator styleSheets = sheets.getStylesheets(name);
         while (styleSheets.hasNext())
         {
            PSConditionalStylesheet sheet =
               (PSConditionalStylesheet)styleSheets.next();

            if (index++ == sheetCtx.getIndex())
            {
               if (ctx.getNextRootCtx() instanceof PSAppIndexedItemIdContext)
                  transformRules(sheet.getConditions(), mapping, idMap);
               else
                  transformUrlRequest(sheet.getRequest(), mapping, idMap);
               break;
            }
         }
      }

      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the result page for literals and adds the required
    * mappings to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param page The result page object to check, may not be
    * <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkResultPage(List mappings, PSResultPage page,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (page == null)
         throw new IllegalArgumentException("page may not be null");

      // if url is null, using the default stylesheet - should only be one of
      // these, and no conditionals
      if (page.getStyleSheet() != null)
      {
         String name = page.getStyleSheet().getFile();
         PSAppNamedItemIdContext pageCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_RESULT_PAGE, name);
         pageCtx.setParentCtx(ctx);
         PSCollection condColl = page.getConditionals();
         if (condColl != null)
         {
            checkConditionals(mappings, condColl.iterator(), pageCtx);
         }
      }
   }


   /**
    * Transforms the supplied result page using the supplied mapping.
    *
    * @param page resultPage to check, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformResultPage(PSResultPage page,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (page == null)
         throw new IllegalArgumentException("page may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid result page ctx");
      PSAppNamedItemIdContext pageCtx = (PSAppNamedItemIdContext)root;
      if (pageCtx.getType() != PSAppNamedItemIdContext.TYPE_RESULT_PAGE)
         throw new IllegalArgumentException("invalid result page ctx");

      if (page.getStyleSheet() != null)
      {
         String name = page.getStyleSheet().getFile();
         if (pageCtx.getName().equals(name))
         {
            PSCollection condColl = page.getConditionals();
            if (condColl != null)
            {
               transformConditionals(condColl.iterator(), mapping, idMap);
            }
         }
      }
      ctx.resetCurrentRootCtx();
   }
   /**
    * Checks the supplied ui set for literals and adds the required mappings
    * to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param uiSet The uiset to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkUISet(List mappings, PSUISet uiSet,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (uiSet == null)
         throw new IllegalArgumentException("uiSet may not be null");

      PSAppUISetIdContext uiSetCtx = new PSAppUISetIdContext(uiSet);
      uiSetCtx.setParentCtx(ctx);

      // check uiset choices
      PSChoices choices = uiSet.getChoices();
      if (choices != null)
         checkChoices(mappings, choices, uiSetCtx);

      // uiset's control ref has psxparams, which has a data locator
      PSControlRef control = uiSet.getControl();
      if (control != null)
      {
         PSAppNamedItemIdContext ctlCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_CONTROL,
            control.getName());
         ctlCtx.setParentCtx(uiSetCtx);
         Iterator ctlParams = control.getParameters();
         while (ctlParams.hasNext())
         {
            checkParam(mappings, (PSParam)ctlParams.next(), ctlCtx);
         }
      }

      // chek uiset's readonly rules
      Iterator rules = uiSet.getReadOnlyRules();
      PSAppCEItemIdContext rulesCtx = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_READ_ONLY_RULES);
      rulesCtx.setParentCtx(uiSetCtx);
      checkRules(mappings, rules, rulesCtx);
   }

   /**
    * Transforms the supplied uiSet using the supplied mapping.
    *
    * @param uiSet The uiset to check, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformUISet(PSUISet uiSet,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (uiSet == null)
         throw new IllegalArgumentException("uiSet may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppUISetIdContext))
         throw new IllegalArgumentException("invalid uiSet ctx");

      PSApplicationIdContext next = ctx.getNextRootCtx();

      // check uiset choices
      PSChoices choices = uiSet.getChoices();
      if (next instanceof PSAppCEItemIdContext)
      {
         PSAppCEItemIdContext itemCtx = (PSAppCEItemIdContext)next;
         if (itemCtx.getType() == PSAppCEItemIdContext.TYPE_CHOICES &&
            choices != null)
         {
            transformChoices(choices, mapping, idMap);
         }
         else
         {
            PSAppCEItemIdContext rulesCtx =
               (PSAppCEItemIdContext)ctx.getCurrentRootCtx();
            if (rulesCtx.getType() != PSAppCEItemIdContext.TYPE_READ_ONLY_RULES)
               throw new IllegalArgumentException("invalid rules ctx");

            Iterator rules = uiSet.getReadOnlyRules();
            transformRules(rules, mapping, idMap);
            ctx.resetCurrentRootCtx();
         }
      }
      else if (next instanceof PSAppNamedItemIdContext)
      {
         PSAppNamedItemIdContext ctlCtx =
            (PSAppNamedItemIdContext)ctx.getCurrentRootCtx();
         if (ctlCtx.getType() != PSAppNamedItemIdContext.TYPE_CONTROL)
            throw new IllegalArgumentException("invalid control ctx");

         PSControlRef control = uiSet.getControl();
         if (control != null && ctlCtx.getName().equals(control.getName()))
         {
            Iterator ctlParams = control.getParameters();
            while (ctlParams.hasNext())
            {
               transformParam((PSParam)ctlParams.next(), mapping, idMap);
            }
         }
         ctx.resetCurrentRootCtx();
      }

      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the supplied choices and adds mappings to the supplied
    * list for each literal found.  Target of the mappings will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param choices The choices to check, may not be <code>null</code>.
    * @param ctx The current context, may not be <code>null</code>.
    */
   public static void checkChoices(List mappings, PSChoices choices,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (choices == null)
         throw new IllegalArgumentException("choices may not be null");

      // check the entry
      Iterator choiceList = choices.getLocal();
      PSAppCEItemIdContext choiceCtx = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_CHOICES);
      choiceCtx.setParentCtx(ctx);
      while (choiceList.hasNext())
      {
         PSEntry entry = (PSEntry)choiceList.next();
         checkEntry(mappings, entry, choiceCtx);
      }

      // check the url request of the choice
      PSUrlRequest urlRequest = choices.getLookup();
      if (urlRequest != null)
      {
         checkUrlRequest(mappings, urlRequest, choiceCtx);
      }

      // check filter's url request if supplied
      PSChoiceFilter filter = choices.getChoiceFilter();
      if (filter != null)
      {
         PSUrlRequest lookup = filter.getLookup();
         PSAppCEItemIdContext filterCtx = new PSAppCEItemIdContext(
            PSAppCEItemIdContext.TYPE_CHOICE_FILTER);
         filterCtx.setParentCtx(choiceCtx);
         checkUrlRequest(mappings, lookup, filterCtx);
      }
   }

   /**
    * Transforms the supplied choices using the supplied mapping.
    *
    * @param choices The choices to transform, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformChoices(PSChoices choices,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (choices == null)
         throw new IllegalArgumentException("choices may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");


      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppCEItemIdContext))
         throw new IllegalArgumentException("invalid choices ctx");
      PSAppCEItemIdContext itemCtx = (PSAppCEItemIdContext)root;
      if (itemCtx.getType() != PSAppCEItemIdContext.TYPE_CHOICES)
         throw new IllegalArgumentException("invalid choices ctx");

      if (ctx.getNextRootCtx() instanceof PSAppEntryIdContext)
      {
         Iterator choiceList = choices.getLocal();
         while (choiceList.hasNext())
         {
            PSEntry entry = (PSEntry)choiceList.next();
            transformEntry(entry, mapping, idMap);
         }
      }
      else if (ctx.getNextRootCtx() instanceof
         PSAppUrlRequestIdContext)
      {
         // check the url request of the choice
         PSUrlRequest urlRequest = choices.getLookup();
         if (urlRequest != null)
         {
            transformUrlRequest(urlRequest, mapping, idMap);
         }
      }
      else if (ctx.getNextRootCtx() instanceof PSAppCEItemIdContext)
      {
         // check filter's url request if supplied
         PSChoiceFilter filter = choices.getChoiceFilter();
         if (filter != null)
         {
            PSAppCEItemIdContext filterCtx =
               (PSAppCEItemIdContext)ctx.getCurrentRootCtx();
            if (filterCtx.getType() !=
               PSAppCEItemIdContext.TYPE_CHOICE_FILTER)
            {
               throw new IllegalArgumentException("invalid choices ctx");
            }
            transformUrlRequest(filter.getLookup(), mapping, idMap);
            ctx.resetCurrentRootCtx();
         }
      }

      ctx.resetCurrentRootCtx();
   }


   /**
    * Checks the supplied applyWhen and adds mappings to the supplied
    * list for each literal found.  Target of the mappings will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param applyWhen The applyWhen to check, may not be <code>null</code>.
    * @param ctx The current context, may not be <code>null</code>.
    */
   public static void checkApplyWhen(List mappings, PSApplyWhen applyWhen,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (applyWhen == null)
         throw new IllegalArgumentException("applyWhen may not be null");

      PSAppCEItemIdContext itemCtx = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_APPLY_WHEN);
      itemCtx.setParentCtx(ctx);
      checkRules(mappings, applyWhen.iterator(), itemCtx);
   }

   /**
    * Transforms the supplied apply when using the supplied mapping.
    *
    * @param applyWhen The applyWhen to transform, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformApplyWhen(PSApplyWhen applyWhen,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (applyWhen == null)
         throw new IllegalArgumentException("applyWhen may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppCEItemIdContext))
         throw new IllegalArgumentException("invalid applyWhen ctx");
      PSAppCEItemIdContext itemCtx = (PSAppCEItemIdContext)root;
      if (itemCtx.getType() != PSAppCEItemIdContext.TYPE_APPLY_WHEN)
         throw new IllegalArgumentException("invalid applyWhen ctx");

      transformRules(applyWhen.iterator(), mapping, idMap);
      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the supplied rules and adds mappings to the supplied
    * list for each literal found.  Target of the mappings will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param rules An iterator over zero or more <code>PSRule</code> objects to
    * check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkRules(List mappings, Iterator rules,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (rules == null)
         throw new IllegalArgumentException("rules may not be null");

      int iRule = 0;
      while (rules.hasNext())
      {
         PSRule rule = (PSRule)rules.next();
         PSAppIndexedItemIdContext ruleCtx = new PSAppIndexedItemIdContext(
            PSAppIndexedItemIdContext.TYPE_RULE, iRule++);
         ruleCtx.setParentCtx(ctx);
         Iterator condRules = rule.getConditionalRules();
         if (condRules != null)
            checkConditionals(mappings, condRules, ruleCtx);
         PSExtensionCallSet callSet = rule.getExtensionRules();
         if (callSet != null)
            checkExtensionCalls(mappings, callSet.iterator(), ruleCtx);
      }

   }

   /**
    * Checks the supplied process checks and adds mappings to the supplied
    * list for each literal found.  Target of the mappings will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param procChecks an iterator over zero or more process checks
    * (<code>PSProcessCheck</code> objects), may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static void checkProcessChecks(List mappings, Iterator procChecks,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (procChecks == null)
         throw new IllegalArgumentException("process checks may not be null");

      while (procChecks.hasNext())
      {
         PSProcessCheck check = (PSProcessCheck)procChecks.next();

         PSAppNamedItemIdContext procCheckCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_PROCESS_CHECK, check.getName());
         procCheckCtx.setParentCtx(ctx);

         Iterator rules = check.getConditions();
         if (rules != null)
            checkRules(mappings, rules, procCheckCtx);
      }
   }
   
   /**
    * Transforms the appropriate rule in the supplied list using the supplied
    * mapping.
    *
    * @param rules Iterator of <code>PSRule</code> objects, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformRules(Iterator rules,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (rules == null)
         throw new IllegalArgumentException("rules may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppIndexedItemIdContext))
         throw new IllegalArgumentException("invalid rule ctx");
      PSAppIndexedItemIdContext ruleCtx = (PSAppIndexedItemIdContext)root;
      if (ruleCtx.getType() != PSAppIndexedItemIdContext.TYPE_RULE)
         throw new IllegalArgumentException("invalid rule ctx");
      int index = 0;
      while (rules.hasNext())
      {
         PSRule rule = (PSRule)rules.next();
         if (index++ == ruleCtx.getIndex())
         {
            Iterator condRules = rule.getConditionalRules();
            if (ctx.getNextRootCtx() instanceof PSAppConditionalIdContext &&
               condRules != null)
            {
               transformConditionals(condRules, mapping, idMap);
            }

            PSExtensionCallSet callSet = rule.getExtensionRules();
            if (ctx.getNextRootCtx() instanceof PSAppExtensionCallIdContext &&
               callSet != null)
            {
               transformExtensionCalls(callSet.iterator(), mapping, idMap);
            }

            break;
         }
      }
      ctx.resetCurrentRootCtx();
   }

   /**
    * Transforms the appropriate process checks in the supplied list using the
    * supplied mapping.
    *
    * @param procChecks an iterator over zero or more process checks
    * (<code>PSProcessCheck</code> objects), may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if the mapping's context is not valid
    * for this method or any param is <code>null</code>.
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformProcessChecks(Iterator procChecks,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (procChecks == null)
         throw new IllegalArgumentException("process checks may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid process check ctx");

      PSAppNamedItemIdContext procCheckCtx = (PSAppNamedItemIdContext)root;
      if (procCheckCtx.getType() != PSAppNamedItemIdContext.TYPE_PROCESS_CHECK)
         throw new IllegalArgumentException("invalid process check ctx");

      while (procChecks.hasNext())
      {
         PSProcessCheck check = (PSProcessCheck)procChecks.next();
         if (check.getName().equals(procCheckCtx.getName()))
         {
            Iterator rules = check.getConditions();
            if ((ctx.getNextRootCtx() instanceof PSAppIndexedItemIdContext) &&
               (rules != null))
            {
               transformRules(rules, mapping, idMap);
            }
            break;
         }
      }
      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the supplied overrides and adds mappings to the supplied
    * list for each literal found.  Target of the mappings will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param overrideList List of clone field overrides to check, may not be 
    * <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkCloneFieldOverrides(List mappings, 
      PSCloneOverrideFieldList overrideList, PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");
      
      if (overrideList == null)
         throw new IllegalArgumentException("overrideList may not be null");

      int index = 0;
      Iterator overrides = overrideList.iterator();
      while (overrides.hasNext())
      {
         PSCloneOverrideField override = (PSCloneOverrideField)overrides.next();
         PSAppIndexedItemIdContext overrideCtx = new PSAppIndexedItemIdContext(
            PSAppIndexedItemIdContext.TYPE_CLONE_FIELD_OVERRIDE, index++);  
         overrideCtx.setParentCtx(ctx);
         checkReplacementValue(mappings, override.getReplacementValue(), 
            overrideCtx);
         checkRules(mappings, override.getRules().iterator(), overrideCtx);
      }      
   }
   
   /**
    * Checks the supplied overrides and adds mappings to the supplied
    * list for each literal found.  Target of the mappings will be unspecified.
    *
    * @param overrideList List of clone field overrides to transform, may not be 
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>, must contain a context appropriate for this method.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    * 
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformCloneFieldOverrides(
      PSCloneOverrideFieldList overrideList, PSApplicationIDTypeMapping mapping, 
         PSIdMap idMap) throws PSDeployException
   {
      if (overrideList == null)
         throw new IllegalArgumentException("overrideList may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppIndexedItemIdContext))
         throw new IllegalArgumentException("invalid clone field override ctx");
      PSAppIndexedItemIdContext overrideCtx = (PSAppIndexedItemIdContext)root;
      if (overrideCtx.getType() != 
         PSAppIndexedItemIdContext.TYPE_CLONE_FIELD_OVERRIDE)
      {
         throw new IllegalArgumentException("invalid clone field override ctx");
      }

      int index = 0;         
      Iterator overrides = overrideList.iterator();
      while (overrides.hasNext())
      {
         PSCloneOverrideField override = (PSCloneOverrideField)overrides.next();
         if (index++ == overrideCtx.getIndex())
         {
            PSApplicationIdContext nextCtx = ctx.getNextRootCtx();
            if (nextCtx instanceof PSAppExtensionCallIdContext)
            {
               transformReplacementValue(override.getReplacementValue(), 
                  mapping, idMap);               
            }
            else
            {
               transformRules(override.getRules().iterator(), mapping, idMap);
            }
            break;
         }
      }
      ctx.resetCurrentRootCtx();            
   }

   /**
    * Checks the supplied entry and adds mappings to the supplied
    * list for each numeric literal found.  Target of the mappings will be
    * unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param entry The entry to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkEntry(List mappings, PSEntry entry,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (entry == null)
         throw new IllegalArgumentException("entry may not be null");

      PSAppEntryIdContext entryCtx = new PSAppEntryIdContext(entry);
      entryCtx.setParentCtx(ctx);
      String value = entry.getValue();
      if (isNumeric(value))
      {
         PSApplicationIDTypeMapping mapping = new PSApplicationIDTypeMapping(
            entryCtx, value);
         mappings.add(mapping);
      }
   }

   /**
    * Transforms the supplied entry using the supplied mapping.  Also transforms
    * the mapping value and context values.
    *
    * @param entry The entry to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformEntry(PSEntry entry,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (entry == null)
         throw new IllegalArgumentException("entry may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppEntryIdContext))
         throw new IllegalArgumentException("invalid entry ctx");
      PSAppEntryIdContext entryCtx = (PSAppEntryIdContext)root;

      if (entryCtx.getOriginalEntry().equals(entry))
      {
         String newValue = String.valueOf(getNewId(mapping, idMap)); 
         entry.setValue(newValue);
         ctx.updateCtxValue(entry);
      }
      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the supplied urlRequest and adds mappings to the supplied
    * list for each literal found.  Target of the mappings will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param urlRequest The request to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkUrlRequest(List mappings, PSUrlRequest urlRequest,
      PSApplicationIdContext ctx)
   {
      if (urlRequest == null)
         throw new IllegalArgumentException("urlRequest may not be null");

      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      PSAppUrlRequestIdContext urlCtx = new PSAppUrlRequestIdContext(
         urlRequest);
      urlCtx.setParentCtx(ctx);
      Iterator params = urlRequest.getQueryParameters();
      while (params.hasNext())
      {
         checkParam(mappings, (PSParam)params.next(), urlCtx);
      }

      PSExtensionCall converter = urlRequest.getConverter();
      if (converter != null)
      {
         PSAppExtensionCallIdContext callCtx = new PSAppExtensionCallIdContext(
            converter);
         callCtx.setParentCtx(urlCtx);
         checkCallParams(mappings, converter.getParameters().iterator(),
            callCtx);
      }
   }

   /**
    * Transforms the supplied urlRequest using the supplied mapping.
    *
    * @param urlRequest The request to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformUrlRequest(PSUrlRequest urlRequest,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (urlRequest == null)
         throw new IllegalArgumentException("urlRequest may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppUrlRequestIdContext))
         throw new IllegalArgumentException("invalid urlreq ctx");
      PSAppUrlRequestIdContext reqCtx = (PSAppUrlRequestIdContext)root;

      if (reqCtx.isSameRequest(urlRequest))
      {
         // need to see which "child" context we have
         PSExtensionCall converter = urlRequest.getConverter();
         if(ctx.getNextRootCtx() instanceof PSAppExtensionCallIdContext)
         {
            if (converter != null)
            {
               ctx.getCurrentRootCtx();
               transformCallParams(
                  urlRequest.getConverter().getParameters().iterator(), mapping,
                  idMap);
               ctx.resetCurrentRootCtx();
            }
         }
         else
         {
            Iterator params = urlRequest.getQueryParameters();
            while (params.hasNext())
            {
               transformParam((PSParam)params.next(), mapping, idMap);
            }
         }
      }
      ctx.resetCurrentRootCtx();

   }

   /**
    * Checks the supplied param and adds mappings to the supplied
    * list for each literal found.  Target of the mappings will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param param The param to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkParam(List mappings, PSParam param,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (param == null)
         throw new IllegalArgumentException("param may not be null");

      PSAppNamedItemIdContext paramCtx = new PSAppNamedItemIdContext(
         PSAppNamedItemIdContext.TYPE_PARAM,
         param.getName());
      paramCtx.setParentCtx(ctx);
      checkReplacementValue(mappings, param.getValue(), paramCtx);
   }

   

   /**
    * checks the supplied bindings for any id types and adds the required
    * mappings
    * @param mappings the list, assumed not <code>null</code>.
    * @param bindings the jexl bindings from which to retrieve the values, 
    *        never <code>null</code>
    * @throws PSDeployException
    */
   public static void checkJexlBinding(
         List<PSApplicationIDTypeMapping> mappings,
         List<PSJexlBinding> bindings)
         throws PSDeployException
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");
      
      if (bindings == null)
         throw new IllegalArgumentException("bindings may not be null");
      
      Iterator<PSJexlBinding> entries = bindings.iterator();
      while (entries.hasNext())
      {
         PSJexlBinding entry = entries.next();
         String bindingName  = entry.getName();
         String bindingIx    = String.valueOf(entry.getIndex());
         

         // From the expression, get the ids using the Jexl Visitor
         List<String> ids = 
            PSDeployJexlUtils.getIdsFromBinding(entry.getExpression());
         
         // no ids in this binding? go to the next one
         if (ids.size() == 0)
            continue;
         
         List<PSParam> paramList = new ArrayList<PSParam>(ids.size());
         // the occurence map, or histogram map, this will keep track of
         // the occurences of a value within this binding
         // Map holds this information: Map<"301", 3> to say id 301 occurs 3
         // times and build up a param list
         Map<String, Integer> occurenceMap = new HashMap<String, Integer>();
         if (ids.size() > 1)
         {
            Iterator<String> it = ids.iterator();
            int paramIx = 0;
            while (it.hasNext())
            {
               String tmpName = bindingName + "[" + paramIx++ + "]";
               paramList
                     .add(new PSParam(tmpName, new PSTextLiteral(it.next())));
            }
         }
         else if (ids.size() == 1)
         {
            paramList
                  .add(new PSParam(bindingName, new PSTextLiteral(ids.get(0))));
         }

         Iterator<PSParam> params = paramList.iterator();
         PSBindingIdContext bCtx = new PSBindingIdContext(bindingIx,
               bindingName, entry.getExpression());

         int paramIx = 0;
         while (params.hasNext())
         {
            PSParam p = params.next();
            String val = p.getValue().getValueText();
            if (!occurenceMap.containsKey(val))
               occurenceMap.put(val, 0);
            else
            {
               Integer i = occurenceMap.get(val);
               i++;
               occurenceMap.put(val, i);
            }
            PSAppTransformer.checkJexlBindingParam(mappings, p, paramIx++,
                  occurenceMap.get(val), bCtx);
         } // iterate on next param within this binding

         //start fresh for next binding
         paramList.clear();
         occurenceMap.clear();
      } // iterate on next binding

   }

/**
 * Checks the current binding that has a jexl expression and adds 
 * mappings based on the supplied list of "ids"
 * @param mappings the list, may not be <code>null</code>.
 * @param param  the param to check, may not be <code>null</code>.
 * @param paramIx the parameter index in this expression
 * @param occur the occurence of this parameter such as 301's occurence in this
 *        expression $rx.db.getFoo("301", "356, "301") . . . 
 * @param bCtx the binding context never <code>null</code>
 */
   @SuppressWarnings("unchecked")
   public static void checkJexlBindingParam(
         List<PSApplicationIDTypeMapping> mappings, PSParam param, int paramIx,
         int occur, PSBindingIdContext bCtx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (param == null)
         throw new IllegalArgumentException("param may not be null");
            
      IPSReplacementValue value = param.getValue();
      if (value == null)
         throw new IllegalStateException("value may not be null");
      
      if ( !(value instanceof PSLiteral) )
         throw new IllegalStateException("value must be PSLiteral type");
      
      if ( bCtx == null )
         throw new IllegalArgumentException("binding context may not be null");

      // create a binding parameter context with index, occurence and value
      PSBindingParamIdContext paramCtx = new PSBindingParamIdContext(paramIx,
         occur, (PSTextLiteral)param.getValue());
      paramCtx.setParentCtx(bCtx);
      
      // add this param ctx only if it is numeric
      String valueText = value.getValueText();
      if ( !StringUtils.isBlank(valueText) && isNumeric(valueText) )
      {
         PSApplicationIDTypeMapping mapping = new PSApplicationIDTypeMapping(
               paramCtx, value.getValueText());
         mappings.add(mapping);
      }
   }
   

   /**
    * Transforms the supplied param using the supplied mapping.
    *
    * @param param The param to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformParam(PSParam param,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (param == null)
         throw new IllegalArgumentException("param may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid param ctx");
      PSAppNamedItemIdContext paramCtx = (PSAppNamedItemIdContext)root;
      if (paramCtx.getType() != PSAppNamedItemIdContext.TYPE_PARAM)
         throw new IllegalArgumentException("invalid param ctx");

      if (paramCtx.getName().equals(param.getName()))
         transformReplacementValue(param.getValue(), mapping, idMap);

      ctx.resetCurrentRootCtx();
   }

   

  /**
   * Problem: You can have a binding<name,expression> pair like this:
   *    SAMPLE BINDING: name="", expression=$myfuncs.foo.bar("301", "301","301")
   *    bindings from the template is a <code><b>List</b></code> of PSJexlBinding 
   *    based with the following attrs: 
   *     <LI> <code><b>index</b></code> of the binding</LI> 
   *     <LI> <code><b>name</b></code> of the binding</LI> 
   *     <LI> <code><b>expression</b></code> for the binding</LI>
   *    The problem is replacing the nth occurence of id "301". 
   *    Solution:
   *        
   * @param mapping The mapping to use for the transformation, may not be
   *                <code>null</code>, must contain a context appropriate 
   *                for this method.
 * @param idMap The idMap to use for the transform,may not be <code>null</code>
 * @param bindings the jexl bindings that are indexed, never <code>null</code>
   *        may be empty
   * @throws PSDeployException
   */ 
   public static void transformJexlBinding(PSApplicationIDTypeMapping mapping,
         PSIdMap idMap, PSJexlBindings bindings)
         throws PSDeployException
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSJexlBindings origBindings = bindings.getSrcBindings();

      PSBindingParamIdContext paramCtx = (PSBindingParamIdContext) mapping
            .getContext();
      
      if ( paramCtx == null )
         throw new IllegalStateException("mapping exists without a parameter " +
               "context");
      PSBindingIdContext bindingCtx = (PSBindingIdContext) paramCtx
            .getParentCtx();
      if ( bindingCtx == null )
         throw new IllegalStateException("parameter context exists without a" +
               "parent binding context");
      
      PSJexlExpressionHelper je = new PSJexlExpressionHelper(mapping);
      String bindKey = bindingCtx.getName();
      PSJexlBinding b = null;
      // it is always easy to locate a binding by the key
      if (StringUtils.isNotBlank(bindKey))
         b = bindings.getByName(bindKey);
      else
      {
         // based on the binding context value, get the original binding index
         // and from it get to the correct binding
         PSJexlBinding pb = origBindings.getByExpression(bindingCtx.getValue());
         if (pb != null)
            b = bindings.getByIndex(pb.getIndex());
      }
      // if the binding exists ...
      if (b != null)
         PSAppTransformer.transformJexlBindingParam(b, mapping, idMap, je);
   }
   
   
   /**
    * updates the binding for the correct parameter based on :
    *    <li> index </li> 
    *    <li> occurence</li> of the parameter in the original exp. If more than
    *    one time, then figure out the earlier occurence of this id. This id has
    *    already been replaced with a new value. Get this new value and iterate
    *    until you find the <code>occurence</code> value.
    * @param binding the jexl binding  never <code>null</code>
    * @param mapping The mapping to use for the transformation, may not be
   *                <code>null</code>, must contain a context appropriate 
   *                for this method.
    * @param idMap The idMap to use for the transform,may not be <code>null</code>
    * @param je the jexl expression helper never <code>null</code>
    * @throws PSDeployException
    */
   public static void transformJexlBindingParam(PSJexlBinding binding,
         PSApplicationIDTypeMapping mapping, PSIdMap idMap,
         PSJexlExpressionHelper je) throws PSDeployException
   {
      if (binding == null)
         throw new IllegalArgumentException("bindings may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (je == null)
         throw new IllegalArgumentException(
               "jexl expression helper may not be null");

      PSBindingParamIdContext paramCtx = (PSBindingParamIdContext) mapping
            .getContext();

      String oldVal = mapping.getValue();
      String newVal = String.valueOf(getNewId(mapping, idMap));
      String bVal = binding.getExpression();

      if (paramCtx.getOccurence() == 0)
      {
         binding.setExpression(StringUtils.replaceOnce(bVal, oldVal, newVal));
         je.updateBindingParam(oldVal, newVal, paramCtx);
      }
      else
      {
         int index = 0;
         // iterate on all the previous occurences of this old value. These
         // have been replaced with new values, so find the new value in the
         // expression, until this occurence
         for (int i = 0; i < paramCtx.getOccurence(); i++)
         {
            String nVal = je.getJexlBindingParamOccurenceValue(oldVal,
                  paramCtx, i);
            if (nVal != null)
               index = bVal.indexOf(nVal, index);
         }
         // now find this oldVal in the binding expression
         index = bVal.indexOf(oldVal, index);
         if (index > 0)
         {
            StringBuffer newBindVal = new StringBuffer(bVal.length());
            String before = bVal.substring(0, index);
            String after = bVal.substring(index + oldVal.length());
            newBindVal.append(before);
            newBindVal.append(newVal);
            newBindVal.append(after);
            binding.setExpression(newBindVal.toString());
         }
         je.updateBindingParam(oldVal, newVal, paramCtx);
         // update the context value, so future packagings on this new 
         // deployment server would know the id types and not prompt the user to
         // choose again
         paramCtx.updateCtxValue(new PSTextLiteral(newVal));
      }
      paramCtx.resetCurrentRootCtx();
   }


   /**
    * Checks the supplied properties and adds mappings to the supplied list for
    * each literal found. Target of the mappings will be unspecified.
    * 
    * @param mappings The list, may not be <code>null</code>.
    * @param props An iterator over zero or more <code>PSProperty</code>
    *           objects to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkProperties(List mappings, Iterator props,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (props == null)
         throw new IllegalArgumentException("props may not be null");

      while (props.hasNext())
      {
         PSProperty prop = (PSProperty)props.next();
         PSAppNamedItemIdContext propCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_PSPROPERTY,
            prop.getName());
         propCtx.setParentCtx(ctx);
         Object value = prop.getValue();
         if (value != null && prop.getType() == prop.TYPE_STRING &&
            isNumeric(value.toString()))
         {
            PSApplicationIDTypeMapping mapping = new PSApplicationIDTypeMapping(
               propCtx, value.toString());
            mappings.add(mapping);
         }
      }
   }

   /**
    * Transforms the supplied param using the supplied mapping.
    *
    * @param props An iterator over zero or more <code>PSProperty</code> objects
    * to transform, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformProperties(Iterator props,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (props == null)
         throw new IllegalArgumentException("props may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("invalid prop ctx");
      PSAppNamedItemIdContext propCtx = (PSAppNamedItemIdContext)root;
      if (propCtx.getType() != PSAppNamedItemIdContext.TYPE_PSPROPERTY)
         throw new IllegalArgumentException("invalid prop ctx");

      while (props.hasNext())
      {
         PSProperty prop = (PSProperty)props.next();
         if (propCtx.getName().equals(prop.getName()))
         {
            String newValue = String.valueOf(getNewId(mapping, idMap)); 
            prop.setValue(newValue);
         }
      }

      ctx.resetCurrentRootCtx();
   }
   /**
    * Checks the supplied extension calls and adds mappings to the supplied
    * list for each literal found in one of the extensions.  Target of the
    * mapping will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param calls An iterator over zero or more <code>PSExtensionCall</code>
    * objects to check, may not be <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    */
   public static void checkExtensionCalls(List mappings,
      Iterator calls, PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (calls == null)
         throw new IllegalArgumentException("calls may not be null");

      int index = 0;
      while (calls.hasNext())
      {
         PSExtensionCall call = (PSExtensionCall)calls.next();
         PSAppExtensionCallIdContext callCtx = new PSAppExtensionCallIdContext(
            call, index++);
         callCtx.setParentCtx(ctx);
         checkCallParams(mappings, call.getParameters().iterator(), callCtx);
      }
   }

   /**
    * Transforms the supplied extension calls using the supplied mapping.
    *
    * @param calls An iterator over zero or more <code>PSExtensionCall</code>
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformExtensionCalls(Iterator calls,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (calls == null)
         throw new IllegalArgumentException("calls may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppExtensionCallIdContext))
         throw new IllegalArgumentException("invalid extension calls ctx");
      PSAppExtensionCallIdContext callCtx = (PSAppExtensionCallIdContext)root;

      int index = 0;
      while (calls.hasNext())
      {
         PSExtensionCall call = (PSExtensionCall)calls.next();
         if (index++ == callCtx.getIndex() && callCtx.getExtensionRef().equals(
            call.getExtensionRef().getFQN()))
        {
            transformCallParams(call.getParameters().iterator(), mapping,
               idMap);
            break;
        }

      }

      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the suppplied data locator and adds mappings to the supplied list
    * for each literal found.  Target of the mapping will be unspecified.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param locator The data locator's replacement value, may not be
    * <code>null</code>.
    * @param ctx The current context, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static void checkDataLocator(List mappings,
      IPSReplacementValue locator, PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      PSAppCEItemIdContext locatorCtx = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DATA_LOCATOR);
      locatorCtx.setParentCtx(ctx);
      checkReplacementValue(mappings, locator, locatorCtx);
   }

   /**
    * Transforms the supplied data locator using the supplied mapping.
    *
    * @param locator The data locator's replacement value, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformDataLocator(IPSReplacementValue locator,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppCEItemIdContext))
         throw new IllegalArgumentException("invalid data locator ctx");
      PSAppCEItemIdContext condCtx = (PSAppCEItemIdContext)root;
      if (condCtx.getType() != PSAppCEItemIdContext.TYPE_DATA_LOCATOR)
         throw new IllegalArgumentException("invalid ext ata locator ctx");

      transformReplacementValue(locator, mapping, idMap);
      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the suppplied conditionals and adds a mapping to the supplied list
    * for each literal found.  Target of the mapping will be unspecified.
    * Does not add mappings when both sides of the conditional specify a literal
    * replacement value.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param conds An iterator over zero or more <code>PSConditional</code>
    * objects, may not be <code>null</code>.
    * @param ctx The current context, may not be <code>null</code>.
    */
   public static void checkConditionals(List mappings, Iterator conds,
      PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (conds == null)
         throw new IllegalArgumentException("conds may not be null");

      while (conds.hasNext())
      {
         PSConditional cond = (PSConditional)conds.next();

         // skip if value is null or both sides are literals - can't be an id.
         if (cond.getValue() == null || (cond.getValue() instanceof PSLiteral &&
            cond.getVariable() instanceof PSLiteral))
         {
            continue;
         }
         
         // skip if the operator is a unary operator
         if ( cond.isUnary())
            continue;
         
         PSAppConditionalIdContext condCtx;
         condCtx = new PSAppConditionalIdContext(cond,
            PSAppConditionalIdContext.TYPE_VALUE);
         condCtx.setParentCtx(ctx);
         checkReplacementValue(mappings, cond.getValue(), condCtx);

         condCtx = new PSAppConditionalIdContext(cond,
            PSAppConditionalIdContext.TYPE_VARIABLE);
         condCtx.setParentCtx(ctx);
         checkReplacementValue(mappings, cond.getVariable(), condCtx);
      }
   }

   /**
    * Transforms the supplied conditionals using the supplied mapping.  Also
    * transforms the context value.
    *
    * @param conds An iterator over zero or more <code>PSConditional</code>
    * objects, may not be <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformConditionals(Iterator conds,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (conds == null)
         throw new IllegalArgumentException("conds may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppConditionalIdContext))
         throw new IllegalArgumentException("invalid ext param ctx");
      PSAppConditionalIdContext condCtx = (PSAppConditionalIdContext)root;
      while (conds.hasNext())
      {
         PSConditional cond = (PSConditional)conds.next();         
         if (condCtx.isSameConditional(cond, true))
         {
            IPSReplacementValue replVal;
            if (condCtx.getType() == PSAppConditionalIdContext.TYPE_VARIABLE)
               replVal = cond.getVariable();
            else
               replVal = cond.getValue();

            transformReplacementValue(replVal, mapping, idMap);
            condCtx.updateCtxValue(replVal);
            break;
         }
      }
      ctx.resetCurrentRootCtx();
   }

   /**
    * Checks the supplied call's params to see if a literal has been
    * specified and if so creates and add required mappings to the supplied
    * list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param params An iterator over zero or more
    * <code>PSAbstractParamValue</code> objects, may not be <code>null</code>.
    * @param ctx The current context, may not be <code>null</code>.
    */
   public static void checkCallParams(List mappings,
      Iterator params, PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      int index = 0;
      while (params.hasNext())
      {
         PSAbstractParamValue val = (PSAbstractParamValue)params.next();
         PSAppExtensionParamIdContext paramCtx =
            new PSAppExtensionParamIdContext(index++, val);
         paramCtx.setParentCtx(ctx);
         checkReplacementValue(mappings, val.getValue(), paramCtx);
      }
   }


   /**
    * Transforms the supplied call params using the supplied mapping.  Also
    * transforms the context value.
    *
    * @param params An iterator over zero or more
    * <code>PSAbstractParamValue</code> objects to transform, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformCallParams(Iterator params,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      PSApplicationIdContext ctx = mapping.getContext();
      PSApplicationIdContext root = ctx.getCurrentRootCtx();
      if (!(root instanceof PSAppExtensionParamIdContext))
         throw new IllegalArgumentException("invalid call param ctx");
      PSAppExtensionParamIdContext paramCtx =
         (PSAppExtensionParamIdContext)root;
      int index = 0;
      while (params.hasNext())
      {
         PSAbstractParamValue val = (PSAbstractParamValue)params.next();
         if (index++ == paramCtx.getIndex())
         {
            transformReplacementValue(val.getValue(), mapping, idMap);
            paramCtx.updateCtxValue(val.getValue());
            break;
         }
      }
      ctx.resetCurrentRootCtx();
   }


   
   /**
    * Checks the supplied replacement value to see if a numeric literal has been
    * specified and if so creates and adds a mapping to the supplied list.
    *
    * @param mappings The list, may not be <code>null</code>.
    * @param value The value to check, may not be <code>null</code>.
    * @param ctx The current context, may not be <code>null</code>.
    */
   public static void checkReplacementValue(
         List<PSApplicationIDTypeMapping> mappings, IPSReplacementValue value,
         PSApplicationIdContext ctx)
   {
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");

      if (value == null)
         throw new IllegalArgumentException("value may not be null");

      if (value instanceof PSLiteral && !(value instanceof PSDateLiteral))
      {
         String valueText = value.getValueText();
         if (valueText != null && valueText.trim().length() > 0 && isNumeric(
            valueText))
         {
            PSApplicationIDTypeMapping mapping = new PSApplicationIDTypeMapping(
               ctx, value.getValueText());
            mappings.add(mapping);
         }
      }
      else if (value instanceof PSExtensionCall)
      {
         PSExtensionCall call = (PSExtensionCall)value;
         PSAppExtensionCallIdContext callCtx =
            new PSAppExtensionCallIdContext(call);
         callCtx.setParentCtx(ctx);
         checkCallParams(mappings, call.getParameters().iterator(), callCtx);
      }
      else if (value instanceof PSFunctionCall)
      {
         PSFunctionCall call = (PSFunctionCall)value;
         PSAppNamedItemIdContext callCtx = new PSAppNamedItemIdContext(
            PSAppNamedItemIdContext.TYPE_FUNCTION_CALL, call.getName());
         callCtx.setParentCtx(ctx);
         checkCallParams(mappings, call.getParameters().iterator(), callCtx);
      }
   }

   /**
    * Transforms the supplied replacement value using the supplied mapping.
    * Also transforms the mapping value.
    *
    * @param value The replacement value to transform, may not be
    * <code>null</code>.
    * @param mapping The mapping to use for the transformation, may not be
    * <code>null</code>.
    * @param idMap The idMap to use for the transform, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if a valid id mapping cannot be located.
    */
   public static void transformReplacementValue(IPSReplacementValue value,
      PSApplicationIDTypeMapping mapping, PSIdMap idMap)
         throws PSDeployException
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");

      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (value instanceof PSExtensionCall)
      {
         PSApplicationIdContext ctx = mapping.getContext();
         PSApplicationIdContext root = ctx.getCurrentRootCtx();
         if (!(root instanceof PSAppExtensionCallIdContext))
            throw new IllegalArgumentException("invalid extension call ctx");
         transformCallParams(
            ((PSExtensionCall)value).getParameters().iterator(), mapping,
            idMap);
         ctx.resetCurrentRootCtx();
      }
      else if (value instanceof PSFunctionCall)
      {
         PSApplicationIdContext ctx = mapping.getContext();
         PSApplicationIdContext root = ctx.getCurrentRootCtx();
         if (!(root instanceof PSAppNamedItemIdContext))
            throw new IllegalArgumentException("invalid function call ctx");
         PSAppNamedItemIdContext callCtx = (PSAppNamedItemIdContext)root;
         if (callCtx.getType() != PSAppNamedItemIdContext.TYPE_FUNCTION_CALL)
            throw new IllegalArgumentException("invalid function call ctx");
         transformCallParams(
            ((PSFunctionCall)value).getParameters().iterator(), mapping,
            idMap);
         ctx.resetCurrentRootCtx();
      }
      else
      {
         if (value instanceof IPSMutatableReplacementValue)
         {
            String newValue = String.valueOf(getNewId(mapping, idMap));
            ((IPSMutatableReplacementValue)value).setValueText(newValue);
         }
         else if (value instanceof PSNumericLiteral)
         {
            int newId = getNewId(mapping, idMap); 
            ((PSNumericLiteral)value).setNumber(new Integer(newId));
         }
      }
   }

   /**
    * Attempts to parse the supplied text value to an integer.
    *
    * @param value The value to check, assumed not <code>null</code> or empty.
    *
    * @return <code>true</code> if it can be parsed, <code>false</code>
    * otherwise.
    */
   private static boolean isNumeric(String value)
   {
      boolean isNumber = true;
      try
      {
         Integer.parseInt(value);
      }
      catch (Exception ex)
      {
         isNumber = false;
      }

      return isNumber;

   }
   
   /**
    * Gets the new id for the specified mapping, and updates the mapping with
    * the new id information. If this has already been done once before, it just
    * returns the current mapping value, which will be the new id.
    * 
    * @param mapping The mapping to use to get the new id and to update with
    * the new id infomation, assumed not <code>null</code>.
    * @param idMap The idMap to use, assumed not <code>null</code>.
    * 
    * @return The new id.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private static int getNewId(PSApplicationIDTypeMapping mapping, PSIdMap 
      idMap) throws PSDeployException
   {
      int newValue;
      if (mapping.hasNewValue())
      {
         try
         {
            newValue = Integer.parseInt(mapping.getValue());
         }
         catch (NumberFormatException e)
         {
            // should never happen, would be a bug
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());
         }         
      }
      else
      {
         newValue = idMap.getNewIdInt(mapping.getValue(),
            mapping.getType(), mapping.getParentId(),
            mapping.getParentType());      
         mapping.setValue(String.valueOf(newValue));
         if (mapping.getParentId() != null)
         {
            String newParentId = idMap.getNewId(mapping.getParentId(),
               mapping.getParentType());
            mapping.setParent(newParentId, mapping.getParentType());
         }
      }
      
      return newValue;      
   }

}

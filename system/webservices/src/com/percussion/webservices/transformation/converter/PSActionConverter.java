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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionProperty;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSChildActions;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSMenuChild;
import com.percussion.cms.objectstore.PSMenuContext;
import com.percussion.cms.objectstore.PSMenuMode;
import com.percussion.cms.objectstore.PSMenuModeContextMapping;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.webservices.ui.data.PSActionCommandParametersParameter;
import com.percussion.webservices.ui.data.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes
 * {@link com.percussion.cms.objectstore.PSAction} and
 * {@link com.percussion.webservices.ui.data.PSAction}.
 */
public class PSActionConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSActionConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(@SuppressWarnings("unused") Class type, Object value)
   {
      if (value == null)
         return null;

      if (isClientToServer(value))
      {
         com.percussion.webservices.ui.data.PSAction source =
            (com.percussion.webservices.ui.data.PSAction) value;

         return getActionFromClient(source);
      }
      else
      {
         PSAction source = (PSAction) value;

         return getActionFromServer(source);
      }
   }

   /**
    * Gets the webservice (client) Action object from the objectstore object.
    *  
    * @param source the to be converted action object, assumed not 
    *    <code>null</code>.
    *    
    * @return the converted action object, never <code>null</code>.
    */
   private com.percussion.webservices.ui.data.PSAction getActionFromServer(
         PSAction source)
   {
      Long id = new PSDesignGuid(source.getGUID()).getValue();
      
      // get target (actTgt) 
      com.percussion.webservices.ui.data.PSActionTarget actTgt = null;
      String tgtName = source.getProperties().getProperty(PSAction.PROP_TARGET);
      if (tgtName != null)
      {
         actTgt = new com.percussion.webservices.ui.data.PSActionTarget(tgtName);
         actTgt.setStyle(source.getProperties().getProperty(
               PSAction.PROP_TARGET_STYLE, ""));
      }
      
      com.percussion.webservices.ui.data.PSActionCommand command = 
         getCommand(source);
      com.percussion.webservices.ui.data.PSActionUsageUsed[] usage = 
         getUsage(source);
      com.percussion.webservices.ui.data.PSActionVisibilitiesContext[] visibilities = 
         getVisibilities(source);
      com.percussion.webservices.ui.data.PSActionChildrenChildAction[] childAction = 
         getChildActions(source);
      com.percussion.webservices.ui.data.ActionType type = getType(source);
      
      org.apache.axis.types.NonNegativeInteger sortRank = 
         new org.apache.axis.types.NonNegativeInteger(
            String.valueOf(source.getSortRank()));
      Property[] properties = getProperties(source);
      
      String tooltip = source.getProperty(PSAction.PROP_SHORT_DESC);
      String iconPath = source.getProperty(PSAction.PROP_SMALL_ICON);
      String acceleratorKey = source.getProperty(PSAction.PROP_ACCEL_KEY);
      String mnemonicKey  = source.getProperty(PSAction.PROP_MNEM_KEY);
      boolean isLaunchNewWindow = source.getPropertyBoolean(
         PSAction.PROP_LAUNCH_NEW_WND);
      boolean isSupportsMultiSelect = source.getPropertyBoolean(
         PSAction.PROP_MUTLI_SELECT);

      com.percussion.webservices.ui.data.RefreshType refreshHint = 
         getRefreshHint(source);
      
      return new com.percussion.webservices.ui.data.PSAction(
        id,
        source.getDescription(),
        actTgt,
        command,
        usage,
        visibilities,
        childAction,
        properties,
        source.getName(),
        source.getLabel(),
        tooltip,
        iconPath,
        type,
        sortRank,
        acceleratorKey,
        mnemonicKey,
        isLaunchNewWindow,
        source.isClientAction(),
        isSupportsMultiSelect,
        refreshHint);
   }

   /**
    * Creates a refresh type from {@link PSAction#PROP_REFRESH_HINT}
    * property.
    * 
    * @param source the action source, assumed not <code>null</code>.
    * 
    * @return the created refresh type, never <code>null</code>. Defaults to
    *    {@link #NONE} if the property does not exist.
    */
   private com.percussion.webservices.ui.data.RefreshType getRefreshHint(
         PSAction source)
   {
      String refHint = source.getProperty(PSAction.PROP_REFRESH_HINT);
      
      // normalize the refresh hint string
      if (PARENT.equalsIgnoreCase(refHint))
         refHint = PARENT;
      else if (ROOT.equalsIgnoreCase(refHint))
         refHint = ROOT;
      else if (SELECTED.equalsIgnoreCase(refHint))
         refHint =  SELECTED;
      else
         refHint =  NONE;
      
      return com.percussion.webservices.ui.data.RefreshType.fromString(refHint);
   }
   
   /**
    * Constants defined in 
    * {@link com.percussion.webservices.ui.data.RefreshType}
    */
   private static final String NONE = 
      com.percussion.webservices.ui.data.RefreshType._none;
   private static final String PARENT = 
      com.percussion.webservices.ui.data.RefreshType._parent;
   private static final String ROOT = 
      com.percussion.webservices.ui.data.RefreshType._root;
   private static final String SELECTED = 
      com.percussion.webservices.ui.data.RefreshType._selected;
   
   /**
    * Gets WS type from the source action.
    * 
    * @param source the source action, assumed not <code>null</code>.
    * 
    * @return the WS type, never <code>null</code>.
    */
   private com.percussion.webservices.ui.data.ActionType getType(
      PSAction source)
   {
      com.percussion.webservices.ui.data.ActionType type;
      if (source.isMenuItem())
         type = com.percussion.webservices.ui.data.ActionType.item;
      else if (source.isCascadedMenu())
         type = com.percussion.webservices.ui.data.ActionType.cascading;
      else
         type = com.percussion.webservices.ui.data.ActionType.dynamic;
      
      return type;
   }
   
   /**
    * Constansts for {@link com.percussion.webservices.ui.data.ActionType}
    */
   private static final String WS_TYPE_ITEM = 
      com.percussion.webservices.ui.data.ActionType._item;
   private static final String WS_TYPE_CASCADING =
      com.percussion.webservices.ui.data.ActionType._cascading;
   
   /**
    * Gets a list of unknown properties.
    * 
    * @param source the source action contains properties, assumes not 
    *   <code>null</code>.
    * 
    * @return the properties, it may be <code>null</code> if there is no
    *   unknown properties.
    */
   private Property[] getProperties(PSAction source)
   {
      Iterator props = source.getProperties().iterator();
      List<Property> tgts = new ArrayList<Property>();
      Property tgtProp;
      while (props.hasNext())
      {
         PSActionProperty prop = (PSActionProperty) props.next();
         if (!ms_knownProps.contains(prop.getName()))
         {
            tgtProp = new Property();
            tgtProp.setName(prop.getName());
            tgtProp.setValue(prop.getValue());
         }
      }

      if (tgts.isEmpty())
      {
         return null;
      }
      else
      {
         Property[] result = new Property[tgts.size()];
         tgts.toArray(result);
         return result;
      }
   }

   /**
    * A list of known properties
    */
   private static Set<String> ms_knownProps = new HashSet<String>();
   static 
   {
      ms_knownProps.add(PSAction.PROP_ACCEL_KEY);
      ms_knownProps.add(PSAction.PROP_LAUNCH_NEW_WND);
      ms_knownProps.add(PSAction.PROP_MNEM_KEY);
      ms_knownProps.add(PSAction.PROP_MUTLI_SELECT);
      ms_knownProps.add(PSAction.PROP_REFRESH_HINT);
      ms_knownProps.add(PSAction.PROP_SHORT_DESC);
      ms_knownProps.add(PSAction.PROP_SMALL_ICON);
      ms_knownProps.add(PSAction.PROP_TARGET);
      ms_knownProps.add(PSAction.PROP_TARGET_STYLE);
   }

   /**
    * Gets the child actions from a supplied (objectstore) action object.
    * 
    * @param source the source object, assumed not <code>null</code>.
    * 
    * @return the constructed child actions, never <code>null</code>,
    *    may be empty.
    */
   private com.percussion.webservices.ui.data.PSActionChildrenChildAction[] getChildActions(
      PSAction source)
   {
      List<com.percussion.webservices.ui.data.PSActionChildrenChildAction> tgtChildren = 
         new ArrayList<com.percussion.webservices.ui.data.PSActionChildrenChildAction>();

      com.percussion.webservices.ui.data.PSActionChildrenChildAction tgtChild;
      Iterator srcChildren = source.getChildren().iterator();
      for (int i=0; srcChildren.hasNext(); i++)
      {
         PSMenuChild srcChild = (PSMenuChild) srcChildren.next();
         if (srcChild.getState() == IPSDbComponent.DBSTATE_MARKEDFORDELETE)
            continue;
         
         tgtChild = 
            new com.percussion.webservices.ui.data.PSActionChildrenChildAction();
         int childId = Integer.parseInt(srcChild.getChildActionId());
         tgtChild.setId(new PSDesignGuid(
            PSAction.getGuidFromId(childId)).getValue());
         tgtChild.setName(srcChild.getChildActioName());
         
         tgtChildren.add(tgtChild);
      }

      // convert list to array
      com.percussion.webservices.ui.data.PSActionChildrenChildAction[] result = 
         new com.percussion.webservices.ui.data.PSActionChildrenChildAction[tgtChildren.size()];
      tgtChildren.toArray(result);

      return result;
   }

   
   /**
    * Gets the visibilities object from a supplied (objectstore) action object.
    * 
    * @param source the source object, assumed not <code>null</code>.
    * 
    * @return the constructed visibilities object, never <code>null</code>,
    *    may be empty.
    */
   private com.percussion.webservices.ui.data.PSActionVisibilitiesContext[] getVisibilities(
      PSAction source)
   {
      List<com.percussion.webservices.ui.data.PSActionVisibilitiesContext> tgtVises = 
         new ArrayList<com.percussion.webservices.ui.data.PSActionVisibilitiesContext>();
      PSActionVisibilityContext srcVis;
      com.percussion.webservices.ui.data.PSActionVisibilitiesContext tgtVis;
      Iterator srcVises = source.getVisibilityContexts().iterator();
      while (srcVises.hasNext())
      {
         srcVis = (PSActionVisibilityContext) srcVises.next();

         if (srcVis.getState() == IPSDbComponent.DBSTATE_MARKEDFORDELETE)
            continue;
         
         if (srcVis.hasValues())
         {
            Iterator values = srcVis.iterator();
            while (values.hasNext())
            {
               tgtVis = 
                  new com.percussion.webservices.ui.data.PSActionVisibilitiesContext();
               tgtVis.setName(srcVis.getName());
               tgtVis.setValue((String)values.next());
               tgtVises.add(tgtVis);
            }
         }
         else
         {
            tgtVis = 
               new com.percussion.webservices.ui.data.PSActionVisibilitiesContext();
            tgtVis.setName(srcVis.getName());
            tgtVises.add(tgtVis);
         }
      }
      
      com.percussion.webservices.ui.data.PSActionVisibilitiesContext[] result = 
         new com.percussion.webservices.ui.data.PSActionVisibilitiesContext[tgtVises.size()];
      tgtVises.toArray(result);
      
      return result;
   }
   
   /**
    * Gets the usage object from a supplied (objectstore) action object.
    * 
    * @param source the source object, assumed not <code>null</code>.
    * 
    * @return the constructed usage object, never <code>null</code>.
    */
   private com.percussion.webservices.ui.data.PSActionUsageUsed[] getUsage(
         PSAction source)
   {
      com.percussion.webservices.ui.data.PSActionUsageUsed[] usage = 
         new com.percussion.webservices.ui.data.PSActionUsageUsed[source.getModeUIContexts().size()];
      
      Iterator modeCtxs = source.getModeUIContexts().iterator();
      PSMenuModeContextMapping mapping;
      PSDesignGuid ctxGuid, modeGuid;
      for (int i=0; modeCtxs.hasNext(); i++)
      {
         mapping = (PSMenuModeContextMapping) modeCtxs.next();
         
         if (mapping.getState() == IPSDbComponent.DBSTATE_MARKEDFORDELETE)
            continue;

         usage[i] = new com.percussion.webservices.ui.data.PSActionUsageUsed();

         // convert id to GUID
         ctxGuid = PSMenuContext.getGuidFromId(Integer.parseInt(mapping
               .getContextId()));
         modeGuid = PSMenuMode.getGuidFromId(Integer.parseInt(mapping
               .getModeId()));
         
         usage[i].setContextId(ctxGuid.getValue());
         usage[i].setContextName(mapping.getContextName());
         usage[i].setUserInterfaceId(modeGuid.getValue());
         usage[i].setUserInterfaceName(mapping.getNodeName());
      }
      
      return usage;
   }
   
   /**
    * Gets the command object from a supplied (objectstore) action object.
    * 
    * @param source the source object, assumed not <code>null</code>.
    * 
    * @return the constructed command object, never <code>null</code>.
    */
   private com.percussion.webservices.ui.data.PSActionCommand getCommand(
      PSAction source)
   {
      com.percussion.webservices.ui.data.PSActionCommandParametersParameter[] tgtParams = 
         new com.percussion.webservices.ui.data.PSActionCommandParametersParameter[source.getParameters().size()];
      com.percussion.webservices.ui.data.PSActionCommandParametersParameter prm;
      Iterator srcParams = source.getParameters().iterator();
      for (int i=0; srcParams.hasNext(); i++)
      {
         PSActionParameter srcParam = (PSActionParameter) srcParams.next();
         prm = 
            new com.percussion.webservices.ui.data.PSActionCommandParametersParameter();
         prm.setName(srcParam.getName());
         prm.set_value(srcParam.getValue());
         
         tgtParams[i] = prm;
      }
      
      return new com.percussion.webservices.ui.data.PSActionCommand(tgtParams,
         source.getURL());
   }
   
   /**
    * Gets the objectstore Action object from the webservice client object.
    *  
    * @param source the to be converted action object, assumed not 
    *    <code>null</code>.
    *    
    * @return the converted action object, never <code>null</code>.
    */
   private PSAction getActionFromClient(
      com.percussion.webservices.ui.data.PSAction source)
   {
      PSAction target = new PSAction(source.getName(), source.getLabel());
      
      long actionId = (new PSDesignGuid(source.getId())).longValue();
      PSKey locator = PSAction.createKey(String.valueOf(actionId));
      locator.setPersisted(false);
      target.setLocator(locator);
      
      target.setSortRank(source.getSortRank().intValue());
      target.setDescription(source.getDescription());
      target.setURL(source.getCommand().getUrl());
      target.setClientAction(source.isClientAction());

      if (source.getType().getValue().equals(WS_TYPE_ITEM))
      {
         target.setMenuType(PSAction.TYPE_MENUITEM);
      }
      else if (source.getType().getValue().equals(WS_TYPE_CASCADING))
      {
         target.setMenuType(PSAction.TYPE_MENU);
      }
      else 
      {
         target.setMenuType(PSAction.TYPE_MENU);
         target.setMenuDynamic(true);
      }

      // set url parameters
      PSActionCommandParametersParameter[] srcParams = 
         source.getCommand().getParameters();
      for (PSActionCommandParametersParameter srcParam : srcParams)
         target.getParameters().add(
               new PSActionParameter(srcParam.getName(), srcParam
                     .get_value()));
      
      // set properties
      addProperty(target, PSAction.PROP_ACCEL_KEY, source.getAcceleratorKey());
      addProperty(target, PSAction.PROP_MNEM_KEY, source.getMnemonicKey());
      addProperty(target, PSAction.PROP_SHORT_DESC, source.getTooltip());
      addProperty(target, PSAction.PROP_SMALL_ICON, source.getIconPath());
      addProperty(target, PSAction.PROP_LAUNCH_NEW_WND, source
            .isLaunchNewWindow() ? PSAction.YES : PSAction.NO);
      addProperty(target, PSAction.PROP_MUTLI_SELECT, source
            .isSupportsMultiSelect() ? PSAction.YES : PSAction.NO);
      if (source.getRefreshHint() != null)
         addProperty(target, PSAction.PROP_REFRESH_HINT, source
               .getRefreshHint().getValue());
      
      if (source.getTarget() != null)
      {
         String targetName = source.getTarget().get_value();
         String targetStyle = source.getTarget().getStyle();

         addProperty(target, PSAction.PROP_TARGET, targetName);
         addProperty(target, PSAction.PROP_TARGET_STYLE, targetStyle);
      }
      Property[] props = source
            .getProperties();
      if (props != null)
      {
         for (Property p : props)
            addProperty(target, p.getName(), p.getValue());
      }
      
      // done with properties
      
      PSDbComponentCollection modeCtxs = getModeUIContexts(source.getUsage(), 
         actionId);
      target.setModeUIContexts(modeCtxs);
      
      PSActionVisibilityContexts visCtxs = getVisibilityContexts(source
            .getVisibilities());
      target.setVisibilityContexts(visCtxs);
      
      // get child (reference) actions
      if (source.getChildren() != null && source.getChildren().length > 0)
      {
         PSChildActions children = target.getChildren();
         for (com.percussion.webservices.ui.data.PSActionChildrenChildAction srcChild : 
            source.getChildren())
         {
            int childId = PSAction.getIdFromGuid(
               new PSDesignGuid(srcChild.getId()));
            PSMenuChild tgtChild = new PSMenuChild(childId, srcChild
                  .getName(), target.getId());
            children.add(tgtChild);
         }
      }
      
      return target;
   }

   /**
    * Converts the visibility contexts from the webservice object to 
    * objectstore object.
    * 
    * @param srcCtxs the to be converted object, it may be <code>null</code>.
    * 
    * @return the converted list, never <code>null</code>, but may be empty.
    */
   private PSActionVisibilityContexts getVisibilityContexts(
      com.percussion.webservices.ui.data.PSActionVisibilitiesContext[] srcCtxs)
   {
      if (srcCtxs == null)
         return new PSActionVisibilityContexts();
      
      // transfer "srcCtxs" into a Map
      Map<String, PSActionVisibilityContext> mapper = 
         new HashMap<String, PSActionVisibilityContext>();
      PSActionVisibilityContext tgtCtx;
      for (com.percussion.webservices.ui.data.PSActionVisibilitiesContext srcCtx
            : srcCtxs)
      {
         if (mapper.get(srcCtx.getName()) == null)
         {
            tgtCtx = new PSActionVisibilityContext(srcCtx.getName(), srcCtx
                  .getValue());
            mapper.put(srcCtx.getName(), tgtCtx);
         }
         else
         {
            tgtCtx = mapper.get(srcCtx.getName());
            tgtCtx.add(srcCtx.getValue());
         }
      }
      
      // transfer the Map into the target list
      PSActionVisibilityContexts tgtCtxs = new PSActionVisibilityContexts();
      for (PSActionVisibilityContext ctx : mapper.values())
      {
         tgtCtxs.add(ctx);
      }
      
      return tgtCtxs;
   }
   
   /**
    * Gets the mode-uicontext mapping list from a usage object.
    * 
    * @param usage the to be converted object, it may be <code>null</code>.
    * @param actionId the parent id, assumed not <code>null</code>.
    * 
    * @return the mapping list, never <code>null</code>, but may be empty.
    */
   private PSDbComponentCollection getModeUIContexts(
         com.percussion.webservices.ui.data.PSActionUsageUsed[] usage, 
         Long actionId)
   {
      PSDbComponentCollection modeCtxs = new PSDbComponentCollection(
            PSMenuModeContextMapping.class);
      PSMenuModeContextMapping mapping;
      String sModeId, sContextId;
      PSDesignGuid modeGuid, ctxGuid;
      String sActionId = String.valueOf(actionId);
      for (com.percussion.webservices.ui.data.PSActionUsageUsed used : usage)
      {
         // convert GUID to id
         modeGuid = new PSDesignGuid(used.getUserInterfaceId());  
         sModeId = String.valueOf(PSMenuMode.getIdFromGuid(modeGuid));
         ctxGuid = new PSDesignGuid(used.getContextId());
         sContextId = String.valueOf(PSMenuContext.getIdFromGuid(ctxGuid));
         
         mapping = new PSMenuModeContextMapping(sModeId, sContextId, sActionId);
         mapping.setModeName(used.getUserInterfaceName());
         mapping.setContextName(used.getContextName());
         modeCtxs.add(mapping);
      }
      return modeCtxs;
   }
   
   /**
    * Adds the supplied property (name and value) to the supplied action object.
    * 
    * @param action the action object that contains properties, assumed not
    *    <code>null</code>. 
    * @param name the property name, assumed not <code>null</code>.
    * @param value the property value, it may be <code>null</code> or empty if
    *    not to add the property.
    */
   private void addProperty(PSAction action, String name, String value)
   {
      if (value != null && value.trim().length() > 0)
      {
         PSActionProperty prop = new PSActionProperty(name, value);
         action.getProperties().add(prop);
      }
   }
}

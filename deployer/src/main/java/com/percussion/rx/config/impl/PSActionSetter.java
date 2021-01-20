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
package com.percussion.rx.config.impl;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionParameters;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PSActionSetter extends PSSimplePropertySetter
{
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (!(obj instanceof PSAction))
         throw new IllegalArgumentException("obj type must be PSAction.");

      PSAction action = (PSAction) obj;
      m_actionName = action.getName();
      if (URL_PARAMS.equals(propName))
      {
         setUrlParams(action, propValue);
      }
      else if (VISIBILITY.equals(propName))
      {
         setVisibility(action, propValue);
      }
      else
      {
         super.applyProperty(obj, state, aSets, propName, propValue);
      }
      return true;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected boolean addPropertyDefs(Object obj, String propName,
         Object pvalue, Map<String, Object> defs)
   {
      if (super.addPropertyDefs(obj, propName, pvalue, defs))
         return true;
      if (!(obj instanceof PSAction))
         throw new IllegalArgumentException("obj type must be PSAction.");
      PSAction action = (PSAction) obj;
      
      if (URL_PARAMS.equals(propName))
      {
         addPropertyDefsForMap(propName, pvalue, getUrlParams(action), defs);
      }
      else if (VISIBILITY.equals(propName))
      {
         addPropertyDefsForMap(propName, pvalue, getActionVisibility(action), defs);
      }
      return true;
   }

   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      if (!(obj instanceof PSAction))
         throw new IllegalArgumentException("obj type must be PSAction.");
      PSAction action = (PSAction) obj;
      m_actionName = action.getName();
      if(propName.equalsIgnoreCase("URL"))
      {
         return action.getURL();
      }
      else if (URL_PARAMS.equals(propName))
      {
         return getUrlParams(action);
      }
      else if (VISIBILITY.equals(propName))
      {
         return getActionVisibility(action);
      }
      return super.getPropertyValue(obj, propName);
   }
   
   /**
    * Sets the url parameters on the action. It is a full replacement. Adds all
    * input parameters and removes any parameter that does not exist in the
    * supplied list.
    * 
    * @param action The action object assumed not <code>null</code>.
    * @param propValue The url parameter values object assumed not
    * <code>null</code>, and expects to be an instance of Map.
    */
   @SuppressWarnings("unchecked")
   private void setUrlParams(PSAction action, Object propValue)
   {
      if (!(propValue instanceof Map))
      {
         String msg = "The type of propValue object is not valid for "
               + "this setter. Expected type is Map.";
         throwError(msg, null);
      }

      PSActionParameters aps = action.getParameters();
      Map<String, String> params = (Map<String, String>) propValue;
      Iterator<String> iter1 = params.keySet().iterator();
      while (iter1.hasNext())
      {
         String name = iter1.next();
         aps.setParameter(name, params.get(name));
      }
      // Prepare a delete list by getting current params
      List<PSActionParameter> deleteList = new ArrayList<PSActionParameter>();
      Iterator iter = aps.iterator();
      while (iter.hasNext())
      {
         PSActionParameter param = (PSActionParameter) iter.next();
         if (params.get(param.getName()) == null)
            deleteList.add(param);
      }
      // Delete the params in delete list if not empty
      if (!deleteList.isEmpty())
      {
         for (PSActionParameter parameter : deleteList)
         {
            aps.remove(parameter);
         }
      }
   }

   /**
    * Convenient method to get the Url params of the action as Object.
    * @param action the Action object must not be <code>null</code>.
    * @return The object corresponding to the url params. It is a map of name of
    * the parameter(String and the value of it(String).
    */
   private Map<String, Object> getUrlParams(PSAction action)
   {
      Map<String, Object> params = new HashMap<String, Object>();
      PSActionParameters aps = action.getParameters();
      Iterator iter = aps.iterator();
      while (iter.hasNext())
      {
         PSActionParameter param = (PSActionParameter) iter.next();
         params.put(param.getName(), param.getValue());
      }
      return params;
   }
   
   /**
    * Convenient method to get the visibility contexts of the action as Object.
    * 
    * @param action the Action object must not be <code>null</code>.
    * @return The object corresponding to the Action Visibility Contexts. It is
    * a map of name of the parameter(String and the value of it(String).
    */
   private Map<String, Object> getActionVisibility(PSAction action)
   {
      Map<String, Object> propValue = new HashMap<String, Object>();
      PSActionVisibilityContexts actionContexts = action
            .getVisibilityContexts();
      Iterator ctxIter = actionContexts.iterator();
      String avCxtName = "";
      Map<String, String> contexts = getResourceLookupData(VISIBILITY_CONTEXTS_LOOKUP_KEY);
      Map<String, String> revContexts = getReverseMap(contexts);
      initVisibilityContexts();
      List<String> processedContexts = new ArrayList<String>();
      while (ctxIter.hasNext())
      {
         PSActionVisibilityContext avContext = (PSActionVisibilityContext) ctxIter
               .next();
         avCxtName = avContext.getName();
         processedContexts.add(avCxtName);
         Iterator propsIter = avContext.iterator();
         List<String> values = new ArrayList<String>();
         String resource = m_vcResources.get(avCxtName);

         Map<String, String> supportedVals = new HashMap<String, String>();
         if (resource != null)
            supportedVals = getReverseMap(getResourceLookupData(resource));

         while (propsIter.hasNext())
         {
            String val = (String) propsIter.next();
            val = supportedVals.get(val) == null ? val : supportedVals
                  .get(val);
            values.add(val);
         }
         propValue.put(revContexts.get(avCxtName), values);
      }
      for (String cxt : revContexts.keySet())
      {
         if(!processedContexts.contains(cxt))
         {
            propValue.put(revContexts.get(cxt), null);
         }
      }
      return propValue;
   }
   
   /**
    * Sets the visibility contexts on the action. It is a full replacement for a
    * given context. If the supported values of context are static for example
    * the values of Assignment Types context are none, reader, assignee, admin.
    * Then the supplied values are validated against these values and throws
    * {@link PSConfigException} if not valid. If the values are dynamic, like
    * content types etc. then if the object corresponding to any value does not
    * exist in the system, then that value is ignored.
    * 
    * @param action The action object assumed not <code>null</code>.
    * @param propValue Must be a map of String or List of Strings.
    */
   @SuppressWarnings("unchecked")
   private void setVisibility(PSAction action, Object propValue)
   {
      if (!(propValue instanceof Map))
      {
         String msg = "The type of propValue object is not valid for "
               + "this setter. Expected type is Map.";
         throwError(msg, null);
      }
      Map<String, String> visContexts = getNormalizedMaps(
            getResourceLookupData(VISIBILITY_CONTEXTS_LOOKUP_KEY));
      PSActionVisibilityContexts actionContexts = action
      .getVisibilityContexts();

      Map<String, Object> params = (Map<String, Object>) propValue;
      Iterator<String> iter = params.keySet().iterator();
      initVisibilityContexts();
      while (iter.hasNext())
      {
         String context = iter.next();
         String contextVal = visContexts.get(StringUtils.trimToEmpty(
               context).toLowerCase());
         if (contextVal == null)
         {
            String msg = "Supplied visibility context ({0}) is invalid.";
            Object[] args = { context };
            throwError(msg, args);
         }
         Object values = params.get(context);
         if (!(values instanceof String || values instanceof List))
         {
            String msg = "Unsupported object supplied for values of visibility "
                  + "context ({0}).";
            Object[] args = { context };
            throwError(msg, args);
         }
         List valList = values instanceof String ? Collections
               .singletonList(((String) values)) : (List) values;
         String[] validValues = getValidValues(context, contextVal, valList);
         // Delete the ones that do not exist in the current list
         PSActionVisibilityContext cxt = actionContexts.getContext(contextVal);
         if(cxt == null)
         {
            actionContexts.addContext(contextVal, validValues);
            continue;
         }
         List<String> delList = new ArrayList<String>();
         Iterator iter1 = cxt.iterator();
         while (iter1.hasNext())
         {
            String val = (String) iter1.next();
            if (!ArrayUtils.contains(validValues, val))
            {
               delList.add(val);
            }
         }
         for (String val : delList)
         {
            cxt.remove(val);
         }
         // Add the valid context values.
         actionContexts.addContext(contextVal, validValues);
      }
   }

   /**
    * Returns the valid values from the list of passed in values, if the context
    * supports only the static values and if the supplied value is not supported
    * then throws {@link PSConfigException} otherwise ignores the value.
    * 
    * @param contextVal The visibility context value, assumed not
    * <code>null</code> and a valid context.
    * @param values List of values that needs to be validated assumed not
    * <code>null</code>.
    * @return String array of valid values, never <code>null</code>, may be
    * empty.
    */
   private String[] getValidValues(String context, String contextVal,
         List<String> values)
   {
      List<String> results = new ArrayList<String>();
      Map<String, String> supportedVals = new HashMap<String, String>();
      if (contextVal
            .equals(PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE))
      {
         IPSDesignModelFactory f = PSDesignModelFactoryLocator
               .getDesignModelFactory();
         IPSDesignModel dm = f.getDesignModel(PSTypeEnum.NODEDEF);
         for (String val : values)
         {
            try
            {
               IPSGuid cguid = dm.nameToGuid(val);
               results.add(cguid.getUUID() + "");
            }
            catch (Exception e)
            {
               String msg = "Unsupported value ({0}) is supplied for "
                     + "context ({1}). Skipping the visibility context "
                     + "setting for action ({2}).";
               Object[] args = { val, context, m_actionName };
               ms_logger.warn(MessageFormat.format(msg, args));
               continue;
            }
         }
      }
      else
      {
         supportedVals = getNormalizedMaps(getResourceLookupData(m_vcResources
               .get(contextVal)));
         for (String val : values)
         {
            String sVal = supportedVals.get(StringUtils.trimToEmpty(val)
                  .toLowerCase());
            if (sVal == null)
            {
               if (ArrayUtils.contains(m_staticValues, contextVal))
               {
                  String msg = "Unsupported value ({0}) is supplied for "
                        + "context ({1}).";
                  Object[] args = { val, context };
                  throwError(msg, args);
               }
               else
               {
                  String msg = "Unsupported value ({0}) is supplied for "
                        + "context ({1}). Skipping the visibility context "
                        + "setting for action ({2}).";
                  Object[] args = { val, context, m_actionName };
                  ms_logger.warn(MessageFormat.format(msg, args));
                  continue;
               }
            }
            results.add(sVal);
         }
      }
      return results.toArray(new String[results.size()]);
   }

   /**
    * Returns a map with trimmed and lowercased keys.
    * 
    * @param inputMap The input map assumed not <code>null</code>.
    * @return The normalized map, never <code>null</code>, may be empty.
    */
   private Map<String, String> getNormalizedMaps(Map<String, String> inputMap)
   {
      Map<String, String> normalizedMap = new HashMap<String, String>();
      Iterator<String> iter = inputMap.keySet().iterator();
      while (iter.hasNext())
      {
         String key = iter.next();
         normalizedMap.put(StringUtils.trimToEmpty(key).toLowerCase(),
               inputMap.get(key));
      }
      return normalizedMap;
   }

   /**
    * Gets the xml document corresponding to the supplied resource and then
    * creates the map of the name and value. Expects the result of the document
    * follows sys_lookup.dtd.
    * 
    * @param resource The name of the resource, if number treats it as lookup
    * id.
    * @return map, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private static Map<String, String> getResourceLookupData(String resource)
   {
      Map<String, String> data = new HashMap<String, String>();
      Map params = new HashMap();
      try
      {
         int lookupId = Integer.parseInt(resource);
         params.put(RXLOOKUP_KEY_PARAM, lookupId);
         resource = LOOKUP_RESOURCE;
      }
      catch (NumberFormatException e)
      {
         // Treat this as resource.
      }
      Document doc = PSConfigUtils.getDocument(resource, params, false);
      Element elem = doc.getDocumentElement();
      NodeList nL = elem.getElementsByTagName(PSXENTRY);
      int sz = nL.getLength();
      Element psxentry = null;
      String key = null;
      String value = null;
      for (int k = 0; k < sz; k++)
      {
         psxentry = (Element) nL.item(k);
         key = psxentry.getElementsByTagName(KEY).item(0).getFirstChild()
               .getNodeValue();
         value = psxentry.getElementsByTagName(VALUE).item(0).getFirstChild()
               .getNodeValue();
         data.put(value, key);
      }
      return data;
   }

   /**
    * Initializes the supported visibility contexts.
    */
   private void initVisibilityContexts()
   {
      m_vcResources.put(PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE,
            "121");
      m_vcResources.put(PSActionVisibilityContext.VIS_CONTEXT_OBJECT_TYPE,
            "sys_psxContentEditorCataloger/getObjectTypes");
      m_vcResources.put(PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS,
            "168");
      m_vcResources.put(
            PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE, "172");
      m_vcResources.put(PSActionVisibilityContext.VIS_CONTEXT_FOLDER_SECURITY,
            "24");
      m_vcResources.put(PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE,
            "sys_psxContentEditorCataloger/ContentTypeLookup");
      m_vcResources.put(PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE,
            "sys_psxContentEditorCataloger/RolesLookup");
      m_vcResources.put(PSActionVisibilityContext.VIS_CONTEXT_LOCALES_TYPE,
            "sys_psxContentEditorCataloger/LocaleLookup");
      m_vcResources.put(PSActionVisibilityContext.VIS_CONTEXT_WORKFLOWS_TYPE,
            "sys_psxContentEditorCataloger/WorkflowLookup");

   }

   /**
    * Utility method to reverse the key valuse of a map.
    * @param map The input map that needs to be reversed, assumed not <code>null</code>.
    * @return Reversed map, never <code>null</code>, may be empty.
    */
   private Map getReverseMap(Map<String,String> map)
   {
      Map<String,String> revMap = new HashMap<String,String>();
      Iterator i = map.entrySet().iterator();
      while(i.hasNext())
      {
         Map.Entry entry = (Map.Entry) i.next();
         revMap.put((String)entry.getValue(), (String)entry.getKey());
      }      
      return revMap;
   }
   /**
    * Convenient method to throw error for this action.
    * 
    * @param msg assumed not <code>null</code>.
    * @param args may be <code>null</code>.
    */
   private void throwError(String msg, Object[] args)
   {
      String m1 = "Failed to set the properties for action '" + m_actionName
            + "'.\n";
      String m2 = args == null ? msg : MessageFormat.format(msg, args);
      throw new PSConfigException(m1 + m2);
   }

   /**
    * Array of visibility contexts whose values are static.
    */
   private static String[] m_staticValues = {
         PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE,
         PSActionVisibilityContext.VIS_CONTEXT_OBJECT_TYPE,
         PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS,
         PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE,
         PSActionVisibilityContext.VIS_CONTEXT_FOLDER_SECURITY };

   /**
    * The map of visibility contexts and lookup key.
    */
   private Map<String, String> m_vcResources = new HashMap<String, String>();

   /**
    * The property name for the keyword choices.
    */
   public static final String URL_PARAMS = "urlParams";

   /**
    * The property name for the keyword choices.
    */
   public static final String VISIBILITY = "visibility";

   /**
    * The key for the possible visibility contexts for action menus. This is a
    * key into the RXLOOKUP table. See also {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String VISIBILITY_CONTEXTS_LOOKUP_KEY = "157";

   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSActionSetter");

   /**
    * Name of the action for logging purpose, initialized in apply property
    * method.
    */
   private String m_actionName = "";

   // Resources
   private static final String LOOKUP_RESOURCE = "sys_ceSupport/lookup";

   /**
    * The HTML parameter name used to supply the key parameter to the lookup
    * resource for global keywords from t the RXLOOKUP table. Never
    * <code>null</code>, empty or changed.
    */
   private static final String RXLOOKUP_KEY_PARAM = "key";

   // XMLNODENAMES
   private static final String PSXENTRY = "PSXEntry";

   private static final String KEY = "Value";

   private static final String VALUE = "PSXDisplayText";
}

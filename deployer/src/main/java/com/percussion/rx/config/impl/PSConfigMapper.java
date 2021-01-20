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

import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to map the values of the setter properties defined in an
 * implementer's configuration file (in spring bean format). 
 *  
 * @author bjoginipally
 * @author YuBingChen
 */
public class PSConfigMapper
{
   /**
    * Replace the property values for the setters defined in a spring bean file.
    * Assumed the bean file contains a list of {@link IPSConfigHandler} beans
    * and the property values of the setters in the handlers are in the
    * place-holder format, e.g. "${com.percussion.RSS.servername}".
    * 
    * @param configDefFile the path of the configuration definition file in
    * spring bean format. Must not be <code>null</code> or empty.
    * 
    * @param props the properties can be applied to the handlers specified in
    * the <code>configDefFile</code> file. This is a list of properties that
    * need to apply to the design object right after installed or reinstalled a
    * package; or this may be a list of properties that have been modified since
    * previously applied the properties to the design objects (which are
    * specified by the <code>configDefFile</code>).
    * 
    * @param curProps the current properties, the combined default and local
    * configure properties. Never <code>null</code>, may be empty.
    * 
    * @param preProps the previous applied properties, the combined default and
    * local configure properties that were applied in the last configuration.
    * Never <code>null</code>, may be empty. Note, this is actually the combined
    * of previous local & default with current default. It is because the 
    * previous (local & default) properties may not have all entries defined
    * in the new configure definition file (<code>configDefFile</code>).
    * 
    * @return a list of handlers defined in configuration file and they contain
    * some of the properties in the <code>changedProps</code>. Never
    * <code>null</code>, but may be empty.
    */
   public List<IPSConfigHandler> getResolvedHandlers(String configDefFile,
         Map<String, Object> props, Map<String, Object> curProps,
         Map<String, Object> preProps)
   {
      validateConfigProperties(configDefFile, props, curProps, preProps);
      
      List<IPSConfigHandler> cfgHandlers = new ArrayList<IPSConfigHandler>();
      PSImplConfigLoader loader = null;
      try
      {
         loader = new PSImplConfigLoader(configDefFile);
         for (String bean : loader.getAllBeanNames())
         {
            ms_log.debug("Replace properties for handler id = \"" + bean + "\".");
            IPSConfigHandler handler = loader.getBean(bean);
            if (replaceProperties(handler, props, curProps, preProps))
            {
               ms_log.debug("Add handler id = \"" + bean + "\".");
               cfgHandlers.add(handler);
            }
            else
            {
               ms_log.debug("Skip handler id = \"" + bean + "\".");               
            }
         }
      }
      finally
      {
         if(loader != null)
         {
            loader.close();
         }
      }
      return cfgHandlers;
   }

   /**
    * Validates the parameters for
    * {@link #getResolvedHandlers(String, Map, Map, Map)}.
    * Output debug messages for the given parameters if the debug log is on.
    * 
    * @param configDefFile the path of the configuration definition file in
    * spring bean format. Must not be <code>null</code> or empty.
    * 
    * @param props the properties can be applied to the handlers specified in
    * the <code>configDefFile</code> file. This is a list of properties that
    * need to apply to the design object right after installed or reinstalled a
    * package; or this may be a list of properties that have been modified since
    * previously applied the properties to the design objects (which are
    * specified by the <code>configDefFile</code>).
    * 
    * @param curProps the current properties, the combined default and local
    * configure properties. Never <code>null</code>, may be empty.
    * 
    * @param preProps the previous applied properties, the combined default and
    * local configure properties that were applied in the last configuration.
    * Never <code>null</code>, may be empty.
    */
   private void validateConfigProperties(String configDefFile,
         Map<String, Object> props, Map<String, Object> curProps,
         Map<String, Object> preProps)
   {
      if (StringUtils.isBlank(configDefFile))
         throw new IllegalArgumentException("configDefFile may not be null or empty.");
      if (props == null)
         throw new IllegalArgumentException("props may not be null.");
      if (curProps == null)
         throw new IllegalArgumentException("curProps may not be null.");
      if (preProps == null)
         throw new IllegalArgumentException("preProps may not be null.");
      
      if (!ms_log.isDebugEnabled())
         return;
      
      ms_log.debug("Config Def file: \"" + configDefFile + "\".");
      if (props != curProps)
         debugProperties("Delta properties", props);
      else
         ms_log.debug("Delta properties is the same as current properties.");
      debugProperties("Current (local & default)", curProps);
      debugProperties("Previous (local & default)", preProps);               
   }
   
   /**
    * Output debug message for the given properties name and its key/value pairs.
    * 
    * @param propName the name of the properties, assumed not <code>null</code>
    * or empty.
    * @param props the properties, assumed not <code>null</code>, but may be
    * empty.
    */
   private void debugProperties(String propName, Map<String, Object> props)
   {
      if (props.isEmpty())
         ms_log.debug("\"" + propName + "\" properties is empty.");
      else
         ms_log.debug("\"" + propName + "\" properties are:");
      List<String> keys = new ArrayList<String>();
      keys.addAll(props.keySet());
      Collections.sort(keys);
      for (String k : keys)
      {
         ms_log.debug("\"" + k + "\" = " + props.get(k).toString());
      }
   }
   /**
    * Replaces the values of the setter property defined in the specified
    * handler.
    * 
    * @param handler the handler, assumed not <code>null</code>.
    * @param props the source properties. It is either the delta of current
    * and previous properties, or the combined of (current) default and local
    * configure properties. Never <code>null</code>, may be empty.
    * @param curProps the current properties, the combined default and local
    * configure properties. Never <code>null</code>, may be empty.
    * @param preProps the previous applied properties, the combined default and
    * local configure properties that were applied in the last configuration.
    * Never <code>null</code>, may be empty.
    * 
    * @return <code>true</code> replaced some of the handler or setter
    * properties; otherwise return <code>false</code>.
    */
   private boolean replaceProperties(IPSConfigHandler handler,
         Map<String, Object> props, Map<String, Object> curProps,
         Map<String, Object> preProps)
   {
      // must resolve previous properties first.
      ms_log.debug("Resolve handler property values with previous properties.");
      replaceHandlerProperties(handler, preProps, false, false);
      
      // has to resolve current properties after previous properties; 
      // otherwise the handler.getExtraProperties() will be updated
      boolean isDelta = props != curProps ? true : false;
      boolean isReplaced = false;
      
      if (isDelta)
         isReplaced = replaceHandlerProperties(handler, props, true, true);
      ms_log.debug("Resolve handler property values with current properties.");
      replaceHandlerProperties(handler, curProps, true, false);
      
      for (IPSPropertySetter setter : handler.getPropertySetters())
      {
         ms_log.debug("Resolve property values for setter class: \""
               + setter.getClass().getName() + "\".");
         
         // if process a delta & replaced handler properties, 
         // then ignore delta for resolving setter properties.
         if (isDelta && isReplaced)
            props = curProps;
         
         if (replacePropertyValues(setter, props, curProps, preProps))
            isReplaced = true;
      }
      
      // always consider has replaced if not delta, hence the handler will be
      // processed; otherwise the handler will be processed if there is any
      // replaced properties
      return (!isDelta) || (isDelta && isReplaced);
   }

   /**
    * Replace properties for the given handler. Do nothing if
    * <code>allProps</code> is empty.
    * 
    * @param handler the handler that contains the properties, assumed not
    * <code>null</code>.
    * @param srcProps the source properties. It is either the delta of current
    * and previous properties, or the combined of default and local configure
    * properties. Never <code>null</code>, may be empty. Do nothing if this is
    * empty.
    * @param isCurProps <code>true</code> if <code>allProps</code> is
    * current properties; otherwise <code>allProps</code> is previously
    * applied properties.
    * @param isDelta <code>true</code> if <code>srcProps</code> is the delta
    * of current and previous properties. In this case, don't log warning if
    * failed to lookup a property; otherwise log warning.
    * 
    * @return <code>true</code> if one or more values of the handler
    * properties have been replaced.
    */
   private boolean replaceHandlerProperties(IPSConfigHandler handler,
         Map<String, Object> srcProps, boolean isCurProps, boolean isDelta)
   {
      // is there anything to be resolved with.
      if (srcProps.isEmpty())
         return false; 
      
      // skip the known constant properties, "name" & "type", 
      // only consider handler specific properties

      if (handler.getExtraProperties().isEmpty())
      {
         return false;
      }
      
      // resolve handler specific properties
      Map<String, Object> tgtProps = new HashMap<String, Object>();

      // pre-populate previous properties, needed for constant properties
      if (!isCurProps)
         tgtProps.putAll(handler.getExtraProperties());
      
      boolean isReplaced = false;
      PSPair<Object, Boolean> ns;
      for (String pname : handler.getExtraProperties().keySet())
      {
         Object value = handler.getExtraProperties().get(pname);
         if (value instanceof String)
         {
            ns = getReplacedValue((String) value, srcProps, isDelta);
            if (ns.getSecond())
            {
               tgtProps.put(pname, ns.getFirst());
               isReplaced = true;
            }
         }
      }
      if (isReplaced)
      {
         if (isCurProps)
            handler.setExtraProperties(tgtProps);
         else
            handler.setPrevExtraProperties(tgtProps);
      }
      
      return isReplaced;
   }
   
   /**
    * Replace the value of the properties in the supplied setter.
    * 
    * @param setter the setter, assumed not <code>null</code>.
    * @param props the properties that may contain only the intersection of
    * <code>curProps</code> or <code>preProps</code>, assumed not
    * <code>null</code>, but may be empty.
    * @param curProps the current properties, the combined default and local 
    * configure properties. Never <code>null</code>, may be empty.
    * @param preProps the previous applied properties, the combined default and
    * local configure properties that were applied in the last configuration.
    * Never <code>null</code>, may be empty.
    * 
    * @return <code>true</code> if replaced some of the property values 
    * defined in the setter; otherwise return <code>false</code>.
    */
   private boolean replacePropertyValues(IPSPropertySetter setter,
         Map<String, Object> props, Map<String, Object> curProps,
         Map<String, Object> preProps)
   {
      Map<String, Object> repCurProps;
      // is processing a delta 
      if (props != curProps)
      {
         // this replacement may only replace partial of the properties if any
         repCurProps = replacePropertyValues(setter.getProperties(), props,
               true);
         if (repCurProps == null)
         {
            setter.setProperties(null);
            return false;
         }
      }
      
      // now, use the "curProps" to get the (replaced) new "Properties"
      repCurProps = replacePropertyValues(setter.getProperties(), curProps,
            false);

      // now, use the "preProps" to get "PrevProperties" 
      if (!preProps.isEmpty())
      {
         Map<String, Object> repPrevProps = replacePropertyValues(setter
               .getProperties(), preProps, false);
         setter.setPrevProperties(repPrevProps);
      }
      // has to call setter.setProperties() last if needed since it is used above.
      setter.setProperties(repCurProps);

      return !(repCurProps == null || repCurProps.isEmpty());
   }

   /**
    * Replace the value of the properties in the supplied setter properties.
    * 
    * @param setterProps the properties which values contain to be replaced
    * "place-holder", assumed not <code>null</code>.
    * @param props the properties that may contain replaced value, assumed not
    * <code>null</code>, but may be empty.
    * @param isDelta <code>true</code> if <code>props</code> is the delta
    * of current and previous properties. In this case, don't log warning if
    * failed to lookup a property; otherwise log warning.
    * 
    * @return the properties with resolved values. It may be <code>null</code>
    * if there is no replacement.
    */
   private Map<String, Object> replacePropertyValues(
         Map<String, Object> setterProps, Map<String, Object> props,
         boolean isDelta)
   {
      if (props.isEmpty())
         return null;
      
      boolean replaced = false;
      Map<String, Object> replacedProps = new HashMap<String, Object>();
      for (Map.Entry<String, Object> prop : setterProps.entrySet())
      {
         PSPair<Object, Boolean> value;
         Object origValue = prop.getValue();
         if (!(origValue instanceof String))
         {
            value = replaceCollectionValues(origValue, props, isDelta);
         }
         else
         {
            value = getReplacedValue((String) origValue, props, isDelta);
         }
         
         replacedProps.put(prop.getKey(), value.getFirst());
         if (value.getSecond())
            replaced = true;
      }
      return replaced ? replacedProps : null;
   }

   /**
    * Replaces a given collection of values.
    * 
    * @param value the collection of values in question, assumed not 
    * <code>null</code>.
    * @param props the properties may contains replacement value, assumed not
    * <code>null</code>.
    * @param isDelta <code>true</code> if <code>props</code> is the delta
    * of current and previous properties.
    * 
    * @return a pair, where the 1st element is the collection that may or may
    * not contains the replaced values; the 2nd element determines if a 
    * replacement has happened, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private PSPair<Object, Boolean> replaceCollectionValues(Object value,
         Map<String, Object> props, boolean isDelta)
   {
      PSPair<Object, Boolean> r;
      boolean replaced = false;
      if (value instanceof Collection)
      {
         Collection values = (Collection) value;
         List<Object> tmpList = new ArrayList<Object>();
         tmpList.addAll(values);
         values.clear();
         
         for (Object obj : (List)tmpList)
         {
            if (obj instanceof String)
               r = getReplacedValue((String)obj, props, isDelta);
            else
               r = replaceCollectionValues(obj, props, isDelta);
            if (r.getSecond())
               replaced = true;
            values.add(r.getFirst());
         }
      }
      else if (value instanceof Map)
      {
         r = replaceMapValues((Map)value, props, isDelta);
         if (r.getSecond())
            replaced = true;
      }
      // ignore other types if there is any

      return new PSPair(value, replaced);
   }

   /**
    * Replaces the values in the given map.
    * 
    * @param map the map in question, assumed not <code>null</code>.
    * @param props the properties may contains replacement value, assumed not
    * <code>null</code>.
    * @param isDelta <code>true</code> if <code>props</code> is the delta
    * of current and previous properties.
    * 
    * @return a pair, where the 1st element may or may not contains the 
    * replaced values; the 2nd element determines if a replacement has happened,
    * never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private PSPair<Object, Boolean> replaceMapValues(Map map,
         Map<String, Object> props, boolean isDelta)
   {
      PSPair<Object, Boolean> r;
      boolean replaced = false;
      
      for (Object key : map.keySet())
      {
         Object value = map.get(key);
         if (value instanceof String)
            r = getReplacedValue((String)value, props, isDelta);
         else
            r = replaceCollectionValues(value, props, isDelta);
         if (r.getSecond())
            replaced = true;
         map.put(key, r.getFirst());
      }

      return new PSPair(map, replaced);
   }
   
   /**
    * Gets the replaced value for the specified place-holder.
    * 
    * @param origValue the value may contain place-holder in the format of
    * "${com.percussion.RSS.serverName}", where the key of the value is
    * surrounded by prefix "${" and postfix "}".
    * @param props the properties may contain the value of the place-holder,
    * assumed not <code>null</code>, but be empty.
    * @param isDelta <code>true</code> if <code>props</code> is the delta
    * of current and previous properties.
    * 
    * @return a pair, where the 1st element is the replaced value or the 
    * original value; the 2nd element determines if a replacement has happened,
    * never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private PSPair<Object, Boolean> getReplacedValue(String origValue,
         Map<String, Object> props, boolean isDelta)
   {
      if (StringUtils.isBlank(origValue))
         return new PSPair(origValue, false);
      
      origValue = origValue.trim();
      // is this a simple (one) place-holder?
      PSPair<Object, Boolean> r = resolveSimplePlaceholder(origValue, props,
            isDelta);
      if (r != null)
         return r;
      
      boolean replaced = false;
      StringBuffer buffer = new StringBuffer();
      // take the part before PREFIX
      int i = origValue.indexOf(PREFIX);
      if (i == -1)
         return new PSPair(origValue, false);
      
      buffer.append(origValue.substring(0, i));
      origValue = origValue.substring(i);
      
      // use "split" to take care multiple ${place-holder}
      String[] values = StringUtils.split(origValue, PREFIX);
      for (String v : values)
      {
         r = replaceOneValue(v, props, isDelta);
         if (r.getSecond())
         {
            replaced = true;
            buffer.append(r.getFirst().toString());
         }
         else
         {
            buffer.append(PREFIX + v);
         }
      }
      String newValue = buffer.toString();
      return new PSPair(newValue, replaced);
   }
   
   /**
    * Resolves the value of a simple place-holder from the given property value.
    * Note, the 1st object of the returned pair can be a string or a collection
    * (List or Map).
    *  
    * @param origValue the string in question, it may not <code>null</code>.
    * @param props the properties may contain replaced value, it may not
    * <code>null</code>.
    * @param isDelta <code>true</code> if <code>props</code> is the delta
    * of current and previous properties.
    * 
    * @return a pair, where the 1st element is the replaced value or the 
    * original value; the 2nd element determines if a replacement has happened.
    * It may be <code>null</code> if there is no leading {@link #PREFIX} and no 
    * ending {@link #SUFFIX}.
    */
   public static PSPair<Object, Boolean> resolveSimplePlaceholder(
         String origValue, Map<String, Object> props)
   {
      return resolveSimplePlaceholder(origValue, props, false);
   }

   /**
    * Gets one (simple) place-holder that may be wrapped in the given string.
    * 
    * @param origValue the string in question, which may be blank.
    * 
    * @return the place-holder. It may be <code>null</code> if the given string
    * does not contain one place-holder.
    */
   private static String getSimplePlaceholder(String origValue)
   {
      if (StringUtils.isBlank(origValue))
         return null;
      
      String trimed = origValue.trim();
      if ((!trimed.startsWith(PREFIX)) || (!trimed.endsWith(SUFFIX)))
         return null;

      String sub = trimed.substring(PREFIX.length());
      if (sub.indexOf(PREFIX) != -1)
         return null;
      
      int index = sub.indexOf(SUFFIX);
      if (index == -1)
         return null;
      
      trimed = sub.substring(0, index);
      return trimed.trim();
   }
   
   /**
    * Gets a list of ${place-holder} from a given string. It returns a pair of
    * values, where the 1st value is a list of ${place-holders}, 2nd value 
    * indicates if the string contains only one ${place-holder} or it contains 
    * ${place-holders} and other texts. For example, if the string contains only
    * one ${place-holder} without any other text, then the 2nd value is
    * <code>true</code>; otherwise the 2nd value is <code>false</code>.
    * 
    * @param origValue the string may contain one or more ${place-holder}. It
    * may be blank.
    * 
    * @return a pair described above. It may be <code>null</code> if there is
    * no ${place-holder} in the given string.
    */
   public static PSPair<List<String>, Boolean> getPlaceholders(String origValue)
   {
      if (StringUtils.isBlank(origValue))
         return null;
      
      // is this a simple (one) place-holder?
      String oneHolder = getSimplePlaceholder(origValue);
      if (oneHolder != null)
      {
         List<String> list = Collections.singletonList(oneHolder);
         return new PSPair<List<String>, Boolean>(list, true);
      }
            
      List<String> holders = new ArrayList<String>();
      // take the part before PREFIX
      int i = origValue.indexOf(PREFIX);
      if (i == -1)
         return null;
      
      origValue = origValue.substring(i);
      
      // use "split" to take care multiple ${place-holder}
      String[] values = StringUtils.split(origValue, PREFIX);
      for (String v : values)
      {
         String holder = getOnePlaceholder(v);
         if (holder != null)
            holders.add(holder);
      }
      if (holders.isEmpty())
      {
         return null;
      }
      else
      {
         return new PSPair<List<String>, Boolean>(holders, false);
      }
   }

   /**
    * Gets one place-holder from the given string. Assumed the string does not
    * contain {@link #PREFIX}, but it may contain {@link #SUFFIX}.
    * 
    * @param origValue the string in question, it may be blank.
    * 
    * @return the place-holder. It may be <code>null</code> if cannot find
    * a matching {@link #SUFFIX} in the given string.
    */
   private static String getOnePlaceholder(String origValue)
   {
      if (StringUtils.isBlank(origValue))
         return null;
      
      int index = origValue.indexOf(SUFFIX);
      if (index == -1)
         return null;
      
      String key = origValue.substring(0, index);
      return key.trim();
   }
   
   /**
    * Resolves the value of a simple place-holder from the given property value.
    * Note, the 1st object of the returned pair can be a string or a collection
    * (List or Map).
    *  
    * @param origValue the string in question, it may not <code>null</code>.
    * @param props the properties may contain replaced value, it may not
    * <code>null</code>.
    * @param isDelta <code>true</code> if <code>props</code> is the delta
    * of current and previous properties.
    * 
    * @return a pair, where the 1st element is the replaced value or the 
    * original value; the 2nd element determines if a replacement has happened.
    * It may be <code>null</code> if there is no leading {@link #PREFIX} and no 
    * ending {@link #SUFFIX}.
    */
   private static PSPair<Object, Boolean> resolveSimplePlaceholder(
         String origValue, Map<String, Object> props, boolean isDelta)
   {
      if (origValue == null)
         throw new IllegalArgumentException("origValue may not be null.");
      if (props == null)
         throw new IllegalArgumentException("props may not be null.");
      
      if ((!origValue.startsWith(PREFIX)) || (!origValue.endsWith(SUFFIX)))
         return null;

      String sub = origValue.substring(PREFIX.length());
      if (sub.indexOf(PREFIX) != -1)
         return null;
      
      PSPair<Object, Boolean> r = replaceOneValue(sub, props, isDelta);
      if (!r.getSecond())
         r.setFirst(origValue);
      return r;
   }
   
   /**
    * Replace a string, which may contain replaced place-holder.
    * The place-holder is surrounded by {@link #PREFIX} and {@link #SUFFIX}.
    * <p>
    * Note, the ${place-holder} will not be replaced if the place-holder does 
    * not exist in the given properties, and a warning will be logged in this 
    * case.
    * </p>
    * @param origValue the string in question, assumed not <code>null</code>.
    * @param props the properties may contain replaced value, assumed not
    * <code>null</code>.
    * @param isDelta <code>true</code> if <code>props</code> is the delta
    * of current and previous properties.
    * 
    * @return a pair, where the 1st element is the replaced value or the 
    * original value; the 2nd element determines if a replacement has happened,
    * never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private static PSPair<Object, Boolean> replaceOneValue(String origValue,
         Map<String, Object> props, boolean isDelta)
   {
      int index = origValue.indexOf(SUFFIX);
      if (index == -1)
         return new PSPair(origValue, false);
      
      String key = origValue.substring(0, index);
      Object rValue = props.get(key);
      if (rValue == null)
      {
         boolean replaced = props.keySet().contains(key);
         if (!replaced)
         {
            if (!isDelta)
            {
               ms_log.warn("Cannot find value for \"" + PREFIX + key + SUFFIX
                     + "\".");
            }
            return new PSPair<Object, Boolean>(origValue, false);
         }
         
         // the replaced value is null 
         if (index == origValue.length()-1)
            return new PSPair(null, true);
         else
            return new PSPair(origValue.substring(index + 1), true);
      }
      
      if (index == origValue.length()-1)
         return new PSPair(rValue, true);
      else
         return new PSPair(rValue.toString() + origValue.substring(index + 1),
               true);
   }
   
   /**
    * The prefix of the place holder
    */
   public static final String PREFIX = "${";
   
   /**
    * The suffix of the place holder
    */
   public static final String SUFFIX = "}";
   
   /**
    * Logger for this class.
    */
   private static Log ms_log = LogFactory.getLog("PSConfigMapper");
}

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

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.config.PSConfigValidation;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.SimpleTypeConverter;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Sets the simple property on the design object through reflection. 
 * 
 * @author bjoginipally
 * @author YuBingChen
 */
public class PSSimplePropertySetter implements IPSPropertySetter
{

   /*
    * (non-Javadoc)
    * @see com.percussion.services.config.IPSPropertySetter#setProperty(java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public boolean applyProperties(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets)
   {
      Map<String, Object> properties = getProperties();
      if (properties == null || properties.isEmpty())
         return false;
      
      try
      {
         boolean changed = false;
         for (Map.Entry<String, Object> prop : properties.entrySet())
         {
            if (applyProperty(obj, state, aSets, prop.getKey(), prop.getValue()))
               changed = true;
         }
         return changed;
      }
      catch (Exception e)
      {
         String errorMsg = "Failed to apply properties for object with type of \""
               + obj.getClass().getName() + "\"";
         ms_log.error(errorMsg, e);
         throw new PSConfigException(errorMsg, e);
      }
   }

   /*
    * //see base class method for details
    */
   public boolean deApplyProperties(Object obj, List<IPSAssociationSet> aSets)
   {
      Map<String, Object> properties = getProperties();
      if (properties == null || properties.isEmpty())
         return false;
      
      try
      {
         boolean changed = false;
         for (Map.Entry<String, Object> prop : properties.entrySet())
         {
            if (deApplyProperty(obj, aSets, prop.getKey(), prop.getValue()))
               changed = true;
         }
         return changed;
      }
      catch (Exception e)
      {
         String errorMsg = "Failed to de-apply properties for object with type of \""
               + obj.getClass().getName() + "\"";
         ms_log.error(errorMsg, e);
         throw new PSConfigException(errorMsg, e);
      }
   }

   /**
    * De-applies the given property (name and value) to the supplied object.
    * 
    * @param obj the object, not <code>null</code>. Assumed the state of the
    * object is {@link ObjectState#PREVIOUS}.
    * @param aSets the list of association sets, may be <code>null</code> if
    * there is no association to be set on the design object.
    * @param propName the property name, may not be <code>null</code> or
    * empty.
    * @param propValue the new value of the property.
    * 
    * @return <code>true</code> if the object has been changed.
    * 
    * @throws Exception if an error occurs.
    */
   protected boolean deApplyProperty(Object obj,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      return false;
   }
   
   /**
    * Applies the given property (name and value) to the supplied object.
    * <p>
    * <b>Note</b>, if the property name is "innerObject.innerObject2.stringValue",
    * then the property value will set to the most inner object, which is
    * <code>obj.getInnerObject().getInnerObject2().setStringValue(value)</code>;
    * otherwise if the property name is "stringValue", then the property value
    * is simply set on the given object, which is 
    * <code>obj.setStringValue(value)</code>.
    * 
    * @param obj the object, not <code>null</code>.
    * @param state the state of the specified design object, not
    * <code>null</code>.
    * @param aSets the list of association sets, may be <code>null</code> if
    * there is no association to be set on the design object.
    * @param propName the property name, may not be <code>null</code> or
    * empty.
    * @param propValue the new value of the property.
    * 
    * @return <code>true</code> if the object has been changed.
    * 
    * @throws Exception if an error occurs.
    */
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null.");
      if (StringUtils.isBlank(propName))
         throw new IllegalArgumentException("propName may not be null or empty.");
      
      PSPair<Object, Method> objMethod = findSetOrGetMethod(obj, propName, true);
      Method method = objMethod.getSecond();
      Object[] args = new Object[] { convertValue(method, propValue) };
      method.invoke(objMethod.getFirst(), args);
      
      return true;
   }

   /*
    * //see base interface method for details
    */
   public void addPropertyDefs(Object obj, Map<String, Object> defs)
   {
      if (defs == null)
         throw new IllegalArgumentException("defs may not be null.");
      
      Map<String, Object> props = getProperties();
      for (String p : props.keySet())
      {
         Object pvalue = props.get(p);
         addPropertyDefs(obj, p, pvalue, defs);
      }
   }

   /**
    * Creates the property definitions (as name/value pairs) for the specified
    * property of the given object. The created property definitions will be
    * added to the specified map.
    * 
    * @param obj the object in question, it may not be <code>null</code>.
    * @param propName the name of the property, it may not be blank.
    * @param pvalue the value of the property in its original format, which may
    * contain ${place-holders} or <code>null</code>.
    * @param defs the holder for created property definitions, never
    * <code>null</code>.
    * 
    * @return <code>false</code> if cannot handle the specified parameters;
    * otherwise return <code>true</code>.
    */
   protected boolean addPropertyDefs(Object obj, String propName,
         Object pvalue, Map<String, Object> defs)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null.");
      if (StringUtils.isBlank(propName))
         throw new IllegalArgumentException("propName may not be blank.");
      if (defs == null)
         throw new IllegalArgumentException("defs may not be null.");

      if (!(pvalue instanceof String))
            return false;
      
      PSPair<List<String>, Boolean> refNames;
      refNames = getReferenceNames(pvalue);
      if (refNames == null)
         return true;
      
      if (refNames.getSecond())
      {
         String refName = refNames.getFirst().get(0);
         Object v = getPropertyValue(obj, propName);
         defs.put(refName, v);
         return true;
      }
      
      for (String ref : refNames.getFirst())
      {
         defs.put(ref, FIX_ME);
      }
      return true;
   }

   /**
    * Gets the reference names from the given string.
    * 
    * @param pvalue the string in question, may be blank.
    * 
    * @return the reference names. It may be <code>null</code> if there is no 
    * reference name. The 1st value is the reference names; the 2nd value is
    * <code>true</code> if there is only one ${place-holder} in the specified 
    * string.
    */
   protected PSPair<List<String>, Boolean> getReferenceNames(Object pvalue)
   {
      if (!(pvalue instanceof String))
         return null;
      
      return PSConfigMapper.getPlaceholders((String) pvalue);
   }
   
   /**
    * Gets the value of the property for the given object.
    * <p>
    * <b>Note</b>, if the property name is "innerObject.innerObject2.stringValue",
    * then the returned property value is from the most inner object, which is
    * <code>obj.getInnerObject().getInnerObject2().getStringValue()</code>;
    * otherwise if the property name is "stringValue", then the returned 
    * property value is from the given object, which is 
    * <code>obj.getStringValue()</code>.
    * <p>
    * The derived class may override this method, for example, it needs to 
    * convert a IPSGuid to name, where the actual property is a IPSGuid, but
    * the value of the property in the configure file is a name.
    * 
    * @param obj the object that contains the specified property, it may not be 
    * <code>null</code>.
    * @param propName the property name of the given object, may not be 
    * <code>null</code> or empty.
    * 
    * @return the value of the property, it may be <code>null</code>.
    * 
    * @throws Exception if failed to get the property value.
    */
   protected Object getPropertyValue(Object obj, String propName)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null.");
      if (StringUtils.isBlank(propName))
         throw new IllegalArgumentException("propName may not be null or empty.");
      
      try
      {
         PSPair<Object, Method> objMethod = findSetOrGetMethod(obj, propName,
               false);
         Method method = objMethod.getSecond();
         
         return method.invoke(objMethod.getFirst(), new Object[] {});
      }
      catch (Exception e)
      {
         throw new PSConfigException("Failed to get \"" + propName
               + "\" property for object \"" + obj.toString() + "\"", e);
      }
   }
   
   /**
    * Creates "fix-me" property definitions for any unknown elements of the
    * specified list property. The created elements are added into the given map
    * holder. An unknown element is a string contains one or more
    * ${place-holders}.
    * <p>
    * Note, it only scans the element if the type of the element is
    * {@link String}, {@link PSPair}, {@link List} or {@link Map}.
    * 
    * @param propName the name of the property, never blank.
    * @param pvalue the list property value. It is expected to be an instance of
    * a {@link List}. It may be <code>null</code>.
    * @param defs the holder for the created property definitions, never
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   protected void addFixmePropertyDefsForList(
         String propName, Object pvalue, Map<String, Object> defs)
   {
      if (pvalue == null)
         return;
      if (StringUtils.isBlank(propName))
         throw new IllegalArgumentException("property name may not be blank.");
      if (defs == null)
         throw new IllegalArgumentException("defs may not be null.");
      
      if (!(pvalue instanceof List))
         throw new PSConfigException("The \"pvalue\" type (of property \""
               + propName + "\") must be List.");
      
      List listValues = (List) pvalue;
      
      for (Object elem : listValues)
      {
         if (elem instanceof String)
         {
            addPropertyDefs((String)elem, null, defs);
         }
         else if (elem instanceof PSPair)
         {
            PSPair pair = (PSPair) elem;
            if (pair.getFirst() instanceof String)
               addPropertyDefs((String)pair.getFirst(), null, defs);
            if (pair.getSecond() instanceof String)
               addPropertyDefs((String)pair.getSecond(), null, defs);
         }
         else if (elem instanceof List)
         {
            addFixmePropertyDefsForList("List", elem, defs);
         }
         else if (elem instanceof Map)
         {
            addPropertyDefsForMap("Map", elem, null, defs);
         }
      }
   }

   /**
    * Adds property definitions into the specified holder for the specified 
    * property value if it contains one or more ${place-holder}s. 
    * The value of the property definition is the specified reference value
    * if the given reference value is not <code>null</code> and the string in 
    * question contains only one ${place-holder}; otherwise the value of
    * the property definition is {@link #FIX_ME}. 
    * 
    * @param pvalue the string in question, it may be <code>null</code> or empty.
    * @param refValue the reference value, which may be used as the value of
    * the property definition if the string in question contains only one
    * ${place-holder}. This will not be used if it is <code>null</code>.
    * @param defs the holder for the created property definitions, assumed not
    * <code>null</code>.
    */
   protected void addPropertyDefs(String pvalue, Object refValue,
         Map<String, Object> defs)
   {
      PSPair<List<String>, Boolean> pair;
      pair = getReferenceNames(pvalue);
      if (pair != null)
      {
         if (pair.getSecond())
         {
            defs.put(pair.getFirst().get(0), refValue);
         }
         else
         {
            for (String n : pair.getFirst())
               defs.put(n, FIX_ME);
         }
      }
   }
   
   /**
    * Creates property definitions for any "unknown" entry of the specified map
    * property. The created property definitions are added into a given holder
    * An unknown element is a string contains one or more ${place-holders}. For
    * each entry of the map property, if the value contains one ${place-holder}
    * and the entry also exists in the supplied source map, then the referenced
    * value is taken from the source map; otherwise the referenced value is
    * {@link #FIX_ME}.
    * <p> 
    * Note, it only scans the entries of the map if the type of the map value
    * is {@link String}, {@link List} or {@link Map}. 
    * 
    * @param propName the name of the property, never blank.
    * @param pvalue the property in question. It is expected to be an
    * instance of {@link Map}. It may be <code>null</code>. 
    * @param srcMap the source contains contains entries which may be referenced
    * by the property value in question. It may be <code>null</code>.
    * @param defs the holder for the created property definitions, never
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   protected void addPropertyDefsForMap(String propName, Object pvalue,
         Map<String, Object> srcMap, Map<String, Object> defs)
   {
      if (pvalue == null)
         return;
      
      if (StringUtils.isBlank(propName))
         throw new IllegalArgumentException("property name may not be blank.");
      if (defs == null)
         throw new IllegalArgumentException("defs may not be null.");

      if (!(pvalue instanceof Map))
         throw new PSConfigException("The type of property \"" + propName
               + "\" must be Map.");

      Map<String, Object> mapProp = (Map<String, Object>) pvalue;

      for (String k : mapProp.keySet())
      {
         Object v = mapProp.get(k);
         if (v instanceof String && srcMap != null)
         {
            addPropertyDefs((String)v, srcMap.get(k), defs);
         }
         else if (v instanceof List)
         {
            addFixmePropertyDefsForList(k, v, defs);
         }
         else if (v instanceof Map)
         {
            addPropertyDefsForMap(k, v, null, defs);
         }
      }
   }


   /**
    * Converts the given value from string to the given type of the parameter of
    * the supplied method.
    * 
    * @param method the method that has one parameter, assumed not
    * <code>null</code>.
    * @param value the value of the parameter. It may be <code>null</code>.
    * 
    * @return the converted value with type of the parameter of the method.
    */
   @SuppressWarnings("unchecked")
   private Object convertValue(Method method, Object value)
   {
      Class paramType = method.getParameterTypes()[0];
      
      return convertValue(value, paramType);
   }

   /**
    * Converts the specified value (if needed) from string to the specified type
    * 
    * @param value the to be converted value, possibly in string format.
    * @param type the target type of the returned value, never <code>null</code>.
    * 
    * @return the converted value, may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   protected Object convertValue(Object value, Class type)
   {
      SimpleTypeConverter cvt = new SimpleTypeConverter();
      return cvt.convertIfNecessary(value, type);
   }
   
   /*
    * see base class method for details
    */
   public Map<String, Object> getProperties()
   {
      return m_curProps;
   }
   
   /*
    * see base class method for details
    */
   public void setProperties(Map<String, Object> props)
   {
      m_curProps = props;
   }

   public Map<String, Object> getPrevProperties()
   {
      return m_prevProps;
   }
   
   public void setPrevProperties(Map<String, Object> props)
   {
      m_prevProps = props;
   }

   /**
    * Finds a setter method (with one parameter) for the given property.
    * 
    * @param obj the object that may contain the setter method, assumed not
    * <code>null</code>.
    * @param propertyName the name of the property, assumed not
    * <code>null</code> or empty.
    * @param isSetter <code>true</code> if lookup for a setter method; otherwise
    * lookup for a getter method.
    * 
    * @return the object and setter method pair, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private PSPair<Object, Method> findSetOrGetMethod(Object obj,
         String propertyName, boolean isSetter)
   {
      // find the most inner object & property name if there is any
      PSPair<Object, String> objProp = findObjectPropName(obj, propertyName);
      if (objProp.getFirst() != obj)
      {
         obj = objProp.getFirst();
         propertyName = objProp.getSecond();
      }
      
      // look up the method from current class to all its super classes
      Class clz = obj.getClass();
      String prefix = isSetter ? "set" : "get";
      String methodName = prefix + StringUtils.capitalize(propertyName);
      int numParams = isSetter ? 1 : 0;
      Method m = findtMethod(clz, methodName, numParams);
      while (m == null && clz.getSuperclass() != null)
      {
         clz = clz.getSuperclass();
         m = findtMethod(clz, methodName, numParams);
      }

      if (m != null)
         return  new PSPair(obj, m);
      
      throw new PSConfigException(
            "Cannot find a setter method for property, \'" + propertyName
                  + "' on type \"" + obj.getClass().getName() + "\".");               
   }
   
   /**
    * Finds the specified method from the given class.
    * 
    * @param clz the class in question, assumed not <code>null</code>.
    * @param methodName the method name, assumed not <code>null</code> or
    * empty.
    * @param numParams the number of parameters of the specified method.
    * 
    * @return the method in the given class. It may be <code>null</code> if
    * cannot find the method.
    */
   @SuppressWarnings("unchecked")
   private Method findtMethod(Class clz, String methodName, int numParams)
   {
      for (Method m : clz.getDeclaredMethods())
      {
         if (m.getName().equals(methodName))
         {
            if (m.getParameterTypes().length == numParams)
            {
               return  m;
            }
         }
      }
      
      return null;
   }
   
   /**
    * Find the most inner object and the related property name if there is any.
    * For example, for property name "innerObject.innerObject2.stringValue",
    * the most inner object is <code>getInnerObject().getInnerObject2()</code>,
    * which is the returned object (1st object), and the "stringValue" is the 
    * returned property name (2nd object).
    * 
    * @param origObj the original object, assumed not <code>null</code>.
    * @param origName the original property name, assumed not <code>null</code>
    *    or empty. This may delimited by ".".
    * 
    * @return the inner object and the related property name.
    */
   @SuppressWarnings("unchecked")
   private PSPair<Object, String> findObjectPropName(Object origObj, 
         String origName)
   {
      if (origName.indexOf('.') == -1)
         return new PSPair(origObj, origName);
      
      Object innerObj = origObj;
      String propNames[] = origName.split("\\.");
      for (int i=0; i<propNames.length-1; i++)
      {
         innerObj = getObject(innerObj, propNames[i]);
      }
      return new PSPair(innerObj, propNames[propNames.length-1]);
   }
   
   /**
    * Gets the child object for a given parent object. The child object is
    * the specified property of the parent object.
    * 
    * @param parentObj the parent object, assumed not <code>null</code>.
    * @param propName the property name, assumed not <code>null</code> or empty.
    * 
    * @return the child object, never <code>null</code>.
    */
   private Object getObject(Object parentObj, String propName)
   {
      Method[] methods = parentObj.getClass().getDeclaredMethods();
      String methodName = "get" + StringUtils.capitalize(propName);
      Exception invokeException = null;
      Object innerObj = null;
      for (Method m : methods)
      {
         if (m.getName().equals(methodName))
         {
            if (m.getParameterTypes().length == 0)
            {
               try
               {
                  innerObj = m.invoke(parentObj, new Object[]{});
                  if (innerObj != null)
                     return innerObj;
                  
                  break;
               }
               catch (Exception e)
               {
                  invokeException = e;
                  break;
               }
            }
         }
      }
      
      // Couldn't find the (non-null) child object, throw exception with 
      // proper error message.
      String errorMsg;
      if (innerObj == null && invokeException == null)
      {
         errorMsg = "Failed to get property \"" + propName
         + "\" for object with type \"" + parentObj.getClass().getName()
         + "\", where the property is NULL.";         
      }
      else
      {
         errorMsg = "Failed to get property \"" + propName
            + "\" for object with type \"" + parentObj.getClass().getName()
            + "\".";
      }
      if (invokeException == null)
      {
         ms_log.error(errorMsg);
         throw new PSConfigException(errorMsg);
      }
      else
      {
         ms_log.error(errorMsg, invokeException);
         throw new PSConfigException(errorMsg, invokeException);
      }
   }
   
   /**
    * Sets the association with the given type on the supplied association list.
    * 
    * @param aSets the to be applied association list, never <code>null</code>
    * or empty.
    * @param type the type of the association, never <code>null</code>.
    * @param value the association value, never <code>null</code> but may be
    * empty. The expected type is {@link List}
    */
   @SuppressWarnings("unchecked")   
   protected void setListAssociation(List<IPSAssociationSet> aSets,
         IPSAssociationSet.AssociationType type, Object value)
   {
      if (!(value instanceof List))
         throw new PSConfigException("The value type of the " + type.name()
               + " association must be List.");
      
      if (aSets == null || aSets.isEmpty())
         throw new IllegalArgumentException(
               "The Template/Slot association list must not be null or empty");

      for (IPSAssociationSet aset : aSets)
      {
         if (aset.getType().equals(type))
         {
            aset.setAssociations((List)value);
            break;
         }
      }
   }
   
   @Override
   public boolean equals(Object otherObj)
   {
      if (!(otherObj instanceof PSSimplePropertySetter))
         return false;
      PSSimplePropertySetter other = (PSSimplePropertySetter) otherObj;
      
      return new EqualsBuilder().append(m_curProps, other.m_curProps).append(
            m_prevProps, other.m_prevProps).isEquals();
   }
   
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_curProps).append(m_prevProps)
            .toHashCode();
   }
   
   /*
    * //see base class method for details
    */
   public List<PSConfigValidation> validate(@SuppressWarnings("unused")
   String objName, @SuppressWarnings("unused")
   ObjectState state, @SuppressWarnings("unused")
   IPSPropertySetter setter)
   {
      return Collections.emptyList();
   }
   
   /**
    * The current properties. It may be wired by spring framework.
    */
   private Map<String, Object> m_curProps;
   
   /**
    * The previously applied properties. It may be wired by spring framework.
    */
   private Map<String, Object> m_prevProps;
   
   /**
    * Logger for this class.
    */
   private static Log ms_log = LogFactory.getLog("PSSimplePropertySetter");
   
   /**
    * This is used when creating a property definition in a default configure
    * definition, where a custom value is needed after the file is created.
    */
   public static final String FIX_ME = "FIXME";   
}

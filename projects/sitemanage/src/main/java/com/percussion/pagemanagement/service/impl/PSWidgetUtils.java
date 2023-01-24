/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.pagemanagement.service.impl;

import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetDefinition.AbstractUserPref;
import com.percussion.pagemanagement.data.PSWidgetDefinition.AbstractUserPref.EnumValue;
import com.percussion.pagemanagement.data.PSWidgetDefinition.CssPref;
import com.percussion.pagemanagement.data.PSWidgetDefinition.UserPref;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetPropertyDataType;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSCollectionUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;


/**
 * 
 * Utilities functions for convert/manipulating widget items,
 * widget properties and widget definitions.
 * 
 * @author adamgent
 *
 */
public class PSWidgetUtils
{

    private PSWidgetUtils(){
        //noop
    }

    private static UserPrefMapper userPrefMapper = new UserPrefMapper();
    
    private static class UserPrefMapper extends PSCollectionUtils.MapperValueAdapter<String, UserPref> {
        public String getKey(UserPref value)
        {
            return value.getName();
        }
    }
    
    private static CssPrefMapper cssPrefMapper = new CssPrefMapper();
    
    private static class CssPrefMapper extends PSCollectionUtils.MapperValueAdapter<String, CssPref> {
        public String getKey(CssPref value)
        {
            return value.getName();
        }
    }
    
    /**
     * 
     * @param definition never <code>null</code>.
     * @return key is a prop name, never <code>null</code>.
     */
    public static Map<String, UserPref> getUserPrefs(PSWidgetDefinition definition) {
        notNull(definition);
        List<UserPref> prefs = definition.getUserPref();
        return userPrefMapper.toMap(prefs);
    }
    
    /**
     * @param definition never <code>null</code>.
     * @return key is prop name, never <code>null</code>
     */
    public static Map<String, CssPref> getCssPrefs(PSWidgetDefinition definition) {
        List<CssPref> prefs = definition.getCssPref();
        return cssPrefMapper.toMap(prefs);
    }
    
    /**
     * Gets the enumerated values for the property definition
     * if it has any.
     * @param userPref never <code>null</code>, maybe empty if this property does not have an enum.
     * @return never <code>null</code>.
     */
    public static Set<Object> getEnums(AbstractUserPref userPref) {
        notNull(userPref, "userPref");
        List<EnumValue> ev = userPref.getEnumValue();
        Set<Object> enums = new HashSet<>();
        if (ev != null) {
            for(EnumValue e : ev) {
                Object validValue = PSSerializerUtils.getObjectFromJson(e.getValue());
                enums.add(validValue);
            }
        }
        return enums;
    }
    
    /**
     * Checks to see if the property is a enum type.
     * @param userPref never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static boolean isEnum(AbstractUserPref userPref) {
       return PSWidgetPropertyDataType.ENUM == PSWidgetPropertyDataType.fromDefinition(userPref);
    }

    /**
     * Converts raw JSON into a property object based on the property definition.
     * <p>
     * Data coercion will be done if the resolved JSON is not the same java type as the property definition.
     * 
     * @param json never <code>null</code>.
     * @param userPref never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSWidgetPropertyCoercionException see {@link #coerceProperty(String, Object, Class)}
     */
    public static Object convertRawProperty(String json, AbstractUserPref userPref) throws PSWidgetPropertyCoercionException {
        Class<?> klass = getJavaType(userPref);
        String name = userPref.getName();
        Object object = PSSerializerUtils.getObjectFromJson(json);
        return coerceProperty(name, object, klass);
    }
    
    /**
     * Coerce an object into the correct java type for the given property definition.
     * @param object never <code>null</code>.
     * @param userPref never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSWidgetPropertyCoercionException see {@link #coerceProperty(String, Object, Class)}
     */
    public static Object coerceProperty(Object object, AbstractUserPref userPref) throws PSWidgetPropertyCoercionException{
        Class<?> klass = getJavaType(userPref);
        return coerceProperty(userPref.getName(), object, klass);
    }
    
    /**
     * Gets the default value for the given property definition.
     * @param userPref never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSWidgetPropertyCoercionException see {@link #coerceProperty(String, Object, Class)}
     */
    public static Object getDefaultValue(AbstractUserPref userPref) throws PSWidgetPropertyCoercionException {
        return coerceProperty(userPref.getDefaultValue(), userPref);
    }

    /**
     * @see PSWidgetPropertyDataType#getJavaType()
     * @param userPref never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static Class<?> getJavaType(AbstractUserPref userPref)
    {
        String dt = userPref.getDatatype();
        dt = dt == null ? "string" : dt.toLowerCase();
        return PSWidgetPropertyDataType.parseType(dt).getJavaType();
    }
    
    /**
     * Coerce the given object into different object of the given type.
     * If the given type is the same as the object, the object is returned.
     * This datatype coercion somewhat mimics javascripts datatype coercion
     * where String are turned into numbers when needed and numbers into strings.
     * <p>
     * <code>true</code> and <code>false</code> strings are also handled. If there
     * datatype is {@link Boolean} then they will be converted appropriatly.
     * 
     * <em>Take not that the object parameter maybe null</em>
     * @param <T> the type of class that the object should be coerced into.
     * @param name never <code>null</code> or empty.
     * @param object maybe <code>null</code>
     * @param dataType never <code>null</code>.
     * @return only <code>null</code> if the object parameter is <code>null</code> otherwise never <code>null</code>.
     * @throws PSWidgetPropertyCoercionException coersion exception if the property can't be coerced.
     */
    @SuppressWarnings("unchecked")
    public static <T> T coerceProperty(String name, Object object, Class<T> dataType) 
        throws PSWidgetPropertyCoercionException {
        notEmpty(name, "name");
        notNull(dataType, "dataType");
        if (object == null)
            return null;
        
        boolean emptyString = false;
        
        T rvalue = null;
        try
        {
            if (dataType.isAssignableFrom(object.getClass()))
            {
                rvalue = (T) object;
            }
            else if (object instanceof String)
            {
                String s = (String) object;
                if (s.isEmpty()) {
                    emptyString = true;
                }
                else if (Number.class.isAssignableFrom(dataType))
                {
                    rvalue = (T) NumberUtils.createNumber(s);
                }
                else if (Boolean.class.isAssignableFrom(dataType))
                {
                    rvalue = (T) Boolean.valueOf(Boolean.parseBoolean(s));
                }
            }
        }
        catch (Exception e)
        {
            throw new PSWidgetPropertyCoercionException(name, object, dataType, e);
        }
        
        if (emptyString) {
            throw new PSWidgetPropertyBlankStringCoercionException(name, object, dataType);
        }
        if (rvalue == null) {
            throw new PSWidgetPropertyCoercionException(name, object, dataType);
        }
        
        return rvalue;

    }
    
    
    
    /**
     * Gets the default values from the given definition and set the default
     * values to the given widget item if the properties do not exist in in
     * the widget item.
     * 
     * @param item the widget item, assumed not <code>null</code>.
     * @param def the widget definition, assumed not <code>null</code>.
     */
    public static void setDefaultValuesFromDefinition(PSWidgetItem item, PSWidgetDefinition def)
    {
        setDefaultValuesFromDefinition(item.getProperties(), def.getUserPref());
    }
    
    /**
     * Gets the default values from the given definition and set the default
     * values to the given widget item if the properties do not exist in in
     * the widget item.
     * 
     * @param props the widget properties, assumed not <code>null</code>.
     * @param prefs the widget property definitions, assumed not <code>null</code>.
     */
    public static void setDefaultValuesFromDefinition(Map<String,Object> props, List<? extends AbstractUserPref> prefs)
    {
        Set<String> propNames = props.keySet();
        for (AbstractUserPref pref : prefs)
        {
            String defValue = pref.getDefaultValue();
            if (defValue !=  null && !propNames.contains(pref.getName()))
            {
                Object v = PSWidgetUtils.getDefaultValue(pref);
                props.put(pref.getName(), v);
            }
        }
    }
    
    /**
     * Indicates a data type coercion error for widget properties.
     * @author adamgent
     *
     */
    public static class PSWidgetPropertyCoercionException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        private final String name;
        private final transient Object value;
        private final Class<?> dataType;

        public PSWidgetPropertyCoercionException(String name, Object object, Class<?> dataType)
        {
            this(name, object, dataType, null);
        }
        

        public PSWidgetPropertyCoercionException(String name, Object object, Class<?> dataType, Throwable cause)
        {
            super(format("Failed convert property:{0}, value:{1}, datatype:{2}", name, object, dataType), cause);
            this.name = name;
            this.value = object;
            this.dataType = dataType;
        }

        public String getName()
        {
            return name;
        }
        public Object getValue()
        {
            return value;
        }
        public Class<?> getDataType()
        {
            return dataType;
        } 

    }
    
    /**
     * Indicates failed to coerce because the inputted object was a blank string (but never <code>null</code>).
     * @author adamgent
     *
     */
    public static class PSWidgetPropertyBlankStringCoercionException extends PSWidgetPropertyCoercionException
    {
        private static final long serialVersionUID = 1L;

        public PSWidgetPropertyBlankStringCoercionException(String name, Object object, Class<?> dataType)
        {
            super(name, object, dataType);
        }
        
    }
}


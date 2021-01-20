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

package com.percussion.extensions.utils;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSSlotContentFinder;

/**
 * 
 * Helps make getting Extension/Result parameters easier by doing the necessary conversion
 * and error handling.
 *  
 * @author adamgent
 * @see #getParameter(String)
 */
public class PSExtensionParamsHelper {
   
    Object[] params;
    IPSExtensionDef extensionDef;
    Map<String,String> extensionParameters;
    Map<String,? extends Object> slotArguments;
    Map<String,Object> slotSelectors;
    
    IPSRequestContext request;
    
    /**
     * The log instance to use for this class if one is not provided, never <code>null</code>.
     */
    private static final Log defaultLog = LogFactory
            .getLog(PSExtensionParamsHelper.class);
    
    /**
     * The user log instance.
     */
    private Log log;
    
    /**
     * Constructor
     * 
     * @param def The extension definition, never null.
     * @param params The parameters passed to the extension, never null.
     * @param request The request passed to the extension, maybe null 
     * if the extension does not handle requests (UDFs).
     * @param log The logger to use for reporting errors and debug messages, maybe null.
     * If null the logger for this class will be used.
     * 
     * @see #getParameter(String)
     */
    public PSExtensionParamsHelper(IPSExtensionDef def, Object [] params,
            IPSRequestContext request,
            Log log) {
        this.extensionDef = def;
        this.params = params;
        this.request = request;
        if (params == null) throw new IllegalArgumentException("params cannot be null");
        if (def == null) throw new IllegalArgumentException("Extension def cannot be null");
        doParameters();
        doLog(log);
    }
    
    /**
     * Constructor
     * 
     * @param parameters The extension parameters already converted to a Map.
     * @param request The request passed to the extension, maybe null.
     * @param log The logger to use for reporting errors and debug messages, maybe null.
     * If null the logger for this class will be used.
     * 
     * @see #getParameter(String)
     */
    public PSExtensionParamsHelper(Map<String,String> parameters, 
            IPSRequestContext request, Log log) {
        this.extensionParameters = parameters;
        this.request = request;
        doLog(log);
    }
    
    /**
     * Use this constructor to help with slot finders.
     * @see IPSSlotContentFinder#find(com.percussion.services.assembly.IPSAssemblyItem, com.percussion.services.assembly.IPSTemplateSlot, Map)
     * @param args slot finders args from slotfinder.getFinderArguments()
     * @param selectors The selectors that are passed with the find method.
     * @param log
     */
    public PSExtensionParamsHelper(Map<String,? extends Object> args, 
            Map<String,Object> selectors, Log log) {
        doLog(log);
        if (args == null)
        {
            String message = "args may not be null";
            this.log.error(message);
           throw new IllegalArgumentException(message);
        }
        if (selectors == null)
        {
            String message = "selectors may not be null";
            this.log.error(message);
           throw new IllegalArgumentException(message);
        }
        this.slotArguments = args;
        this.slotSelectors = selectors;
        
    }
    
    /**
     * Gets a parameter by first trying to get it from the request then
     * followed by the parameters passed to the extension.
     * 
     * @param paramName
     * @return The value of the parameter, or null if the parameter is not found.
     */
    public String getParameter(String paramName) {
        
        if (slotSelectors != null && slotArguments != null) {
            // This helper is for a slot finder.
            Object val = slotSelectors.get(paramName);
            if (val != null) {
                log.debug("Got the parameter name = " + paramName
                        + " value = " + val + " from the slot selectors.");
            }
            if (val == null)
            {
               val = slotArguments.get(paramName);
               if (val != null) {
                   log.debug("Got the parameter name = " + paramName
                           + " value = " + val + " from the slot arguments.");
               }
            }
            if (val instanceof String)
               return (String) val;
            else if (val instanceof String[])
            {
               String vals[] = (String[]) val;
               if (vals.length == 0) {
                   String errorMessage = "No value for " + paramName;
                   log.error(errorMessage);
                  throw new RuntimeException(errorMessage);
               }
               return vals[0];
            }
            else if (val != null)
               return val.toString();
            else {
                log.debug("Returning null for slot aruments and selectors");
               return null;
            }
        }
        
        if (request != null) {
            String value = request.getParameter(paramName);
            log.debug("Got the parameter name = " + paramName
                    + " value = " + value + " from the request.");
            if (value != null) return value;
        }
        if (extensionParameters != null) {
            String value = extensionParameters.get(paramName);
            log.debug("Got the parameter name = " + paramName
                    + " value = " + value + " from the extension parameters.");
            return value;
        }
        log.warn("Extension Parameters is null");
        return null;
        
    }
    
    public Number getRequiredParameterAsNumber(String paramName) {
        return paramToNumber(paramName, getRequiredParameter(paramName));
    }
    
    public Boolean getRequiredParameterAsBoolean(String paramName)
    {
       return paramToBoolean(paramName, getRequiredParameter(paramName));
    }
    /**
     * Gets a parameter and if its null or an empty String then an
     * IllegalArgumentException is thrown.
     * 
     * @param paramName Name of the parameter.
     * @return value of the parameter never null or empty.
     * @throws IllegalArgumentException
     */
    public String getRequiredParameter(String paramName){
        String value = getParameter(paramName);
        if (value == null || isBlank(value)) {
            String message = "Param: " + paramName + " is a required field.";
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        return value;
    }
    
    public Number getOptionalParameterAsNumber(String paramName, String defaultValue) {
        return paramToNumber(paramName, getOptionalParameter(paramName, defaultValue));
    }
    
    public Boolean getOptionalParameterAsBoolean(String paramName, Boolean defaultValue)
    {
       String b = (defaultValue.booleanValue()) ? "true" : "false" ;
       return paramToBoolean(paramName, getOptionalParameter(paramName, b));
    }
    
    /**
     * Gets a parameter and if its null or empty returns the provided default value.
     * 
     * @param paramName Name of the parameter.
     * @param defaultValue The value to return if paramName is not found. Maybe null.
     * @return The value of the parameter or the defaultValue.
     */
    public String getOptionalParameter(String paramName, String defaultValue) {
        String value = getParameter(paramName);
        if ( value == null || isBlank(value)) {
            log.debug("Parameter " + paramName + " was not set. Using default value = " + defaultValue);
            return defaultValue;
        }
        return value;        
    }
    
    public void errorOnParameter(String paramName, String reason) {
        String error = "Parameter: " + paramName + " is invalid."+ " Reason: " + reason;
        log.error(error);
        throw new IllegalArgumentException(error);
    }
    
    public Number paramToNumber(String paramName, String param) {
        try {
            return NumberUtils.createNumber(param);
        }
        catch (NumberFormatException e) {
            String message = "Parameter " + paramName + " is not a number. " +
                 "Param value=" + param;
            log.error(message,e);
            throw new IllegalArgumentException(message,e);
        }
    }
    
    public Boolean paramToBoolean(String paramName, String param)
    {
        Converter cvt = new BooleanConverter();
        try
      {
         Boolean val = (Boolean) cvt.convert(Boolean.class, param);
           return val;
      } catch (ConversionException ex)
      {
         String message = "Parameter " + paramName + " is not a boolean. " +
         "Param value=" + param;
         log.error(message,ex);
         throw new IllegalArgumentException(message,ex);
      }
    }
    /**
     * Get the parameters passed to the extension as a map.
     * <em>This does not include the parameters that are in the request.</em>
     * 
     * @return a map with the parameter name as key and the parameter value as
     *         <code>String</code> object, never <code>null</code>, may be
     *         empty. Parameter values may be <code>null</code>.
     */
    public Map<String, String> getExtensionParameters() {
        return this.extensionParameters;
    }
    
    protected void doLog(Log log) {
        this.log = log == null ? PSExtensionParamsHelper.defaultLog : log;
    }
    
    @SuppressWarnings("unchecked")
    protected void doParameters()
    {  
       extensionParameters = new HashMap<String, String>();
       
       if (params != null)
       {
          int index = 0;
          Iterator names = extensionDef.getRuntimeParameterNames();
          while (names.hasNext())
          {
             String name = (String) names.next();
          
             if (params.length > index) {
                 Object p = params[index];
                 String pString;
                 if (p instanceof IPSReplacementValue) {
                    IPSReplacementValue iP = (IPSReplacementValue) p;
                    pString = iP.getValueText();
                 }
                 else if (p instanceof String) {
                     pString = (String)p;
                 }
                 else {
                     pString = p.toString();
                 }
                extensionParameters.put(name, pString);
             }
             else
                extensionParameters.put(name, null);
             
             index++;
          }
       }
    }
}
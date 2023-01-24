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
package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSMetadataProperty.VALUETYPE;
import com.percussion.delivery.metadata.IPSMetadataQueryService;
import com.percussion.delivery.metadata.data.impl.PSCriteriaElement;
import com.percussion.delivery.metadata.utils.PSHashCalculator;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author erikserating
 *
 */
public abstract class PSMetadataQueryServiceHelper
{
    
    private PSMetadataQueryServiceHelper()
    {
        
    }
    
    /**
     * A set of property keys that are not stored as properties but are instead
     * columns in the metadata entry table.
     */
    public static final Set<String> ENTRY_PROPERTY_KEYS = new HashSet<String>();
    static
    {
        ENTRY_PROPERTY_KEYS.add("folder");
        ENTRY_PROPERTY_KEYS.add("name");
        ENTRY_PROPERTY_KEYS.add("type");
        ENTRY_PROPERTY_KEYS.add("linktext");
        ENTRY_PROPERTY_KEYS.add("linktext_lower");
        ENTRY_PROPERTY_KEYS.add("pagepath");
        ENTRY_PROPERTY_KEYS.add("site");
    }
    
    
    /**
     * 2011-01-21T09:36:05
     */
    public static FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");

    public static VALUETYPE getDatatype(String name, PSPropertyDatatypeMappings datatypeMappings)
    {
          String nameWithOutNamespace;
          
          if (name.contains(":"))
             nameWithOutNamespace = name.split(":")[1];
          else
             nameWithOutNamespace = name;
          
          return datatypeMappings.getDatatype(nameWithOutNamespace);
    }

    @SuppressWarnings("unchecked")
    public static List parseToList(String key, String val, PSPropertyDatatypeMappings datatypeMappings, PSHashCalculator hashCalc) throws ParseException
    {
       VALUETYPE type = datatypeMappings.getDatatype(key);
       List results = new ArrayList();


       if(type == VALUETYPE.NUMBER)
       {
          for(String s : val.split(","))
          {
             results.add(new Double(s));
          }
       }
       else if(type == VALUETYPE.DATE)
       {
          for(String s : val.split("'"))
          {
             if(s.trim().equals(",") || s.trim().equals(""))
                continue;
             results.add(dateFormat.parse(s));
          }
       }
       else
       { //For text / string use value hash if it is a property
           boolean calcHash = true;
           if(!key.contains("propValue") && datatypeMappings.getDatatypeMappings().getProperty(key,"").equals("")){
               calcHash = false;
           }
          for(String s : val.split("'"))
          {
             if(s.trim().equals(",") || s.trim().equals(""))
                continue;

             if(calcHash)
                results.add(hashCalc.calculateHash(s));
             else
                 results.add(s);
          }
       }
       return results;  
    
    }

    /**
     * For the provided propertyName it returns the column names that belongs to in the database, default return
     * column name is stringvalue
     * @param ce
     * @param datatypeMappings
     * @return
     */
    public static String getValueColumnName(PSCriteriaElement ce, PSPropertyDatatypeMappings datatypeMappings)
    {
        String valueColumn = "";
        VALUETYPE dt = getDatatype(ce.getName(), datatypeMappings);

        switch(dt)
        {
            case DATE:
                valueColumn = IPSMetadataQueryService.PROP_DATEVALUE_COLUMN_NAME;
                break;
            case NUMBER:
                valueColumn = IPSMetadataQueryService.PROP_NUMBERVALUE_COLUMN_NAME;
                break;
            case TEXT:
                if(ce.getOperationType() == PSCriteriaElement.OPERATION_TYPE.LIKE)
                    valueColumn = IPSMetadataQueryService.PROP_TEXTVALUE_COLUMN_NAME;
                else
                    valueColumn = IPSMetadataQueryService.PROP_VALUEHASH_COLUMN_NAME;
                break;
             default :
                 if(ce.getOperationType() == PSCriteriaElement.OPERATION_TYPE.LIKE)
                     valueColumn = IPSMetadataQueryService.PROP_STRINGVALUE_COLUMN_NAME;
                 else
                     valueColumn = IPSMetadataQueryService.PROP_VALUEHASH_COLUMN_NAME;
        }
        return valueColumn;
    }

    /**
     * For the provided propertyName it returns the column names that belongs to in the database, default return
     * column name is stringvalue
     * @param name
     * @param datatypeMappings
     * @return
     */
    public static String getValueColumnName(String name, PSPropertyDatatypeMappings datatypeMappings)
    {
        String valueColumn = "";
        VALUETYPE dt = getDatatype(name, datatypeMappings);

        switch(dt)
        {
            case DATE:
                valueColumn = IPSMetadataQueryService.PROP_DATEVALUE_COLUMN_NAME;
                break;
            case NUMBER:
                valueColumn = IPSMetadataQueryService.PROP_NUMBERVALUE_COLUMN_NAME;
                break;
            case TEXT:
                    valueColumn = IPSMetadataQueryService.PROP_TEXTVALUE_COLUMN_NAME;
                    break;
            default :
                    valueColumn = IPSMetadataQueryService.PROP_STRINGVALUE_COLUMN_NAME;
        }
        return valueColumn;
    }
    
    /**
     * Returns the sorting order based on the passed in orderby string, if nothing is there 
     * in the orderby then the default would be asc
     * @param orderBy
     * @return
     */
    public static String getSortingOrder(String orderBy)
    {
        return orderBy.toLowerCase().endsWith(IPSMetadataQueryService.SORT_ORDER_DESCEND)
           ? IPSMetadataQueryService.SORT_ORDER_DESCEND 
           : IPSMetadataQueryService.SORT_ORDER_ASCEND ;
    }
    
    /**
     * if the orderBy ends with either asc or desc then remove the suffix, trim it and return
     * the string, other wise just trim the passed in ordey by and return it.
     * Ex: orderBy = "dcterms:created asc" and the method returns dcterms:created
     * Ex: orderBy = "dcterms:created asc" and the method returns dcterms:created
     * @param orderBy cannot be <code>null</code> or empty
     * @return
     */
    public static String getSortPropertyName(String orderBy)
    {
        String sortProperty = orderBy;
        String sortPropertyLowerCase = orderBy.toLowerCase();
        if(sortPropertyLowerCase.endsWith(IPSMetadataQueryService.SORT_ORDER_ASCEND) || sortPropertyLowerCase.endsWith(IPSMetadataQueryService.SORT_ORDER_DESCEND))
        {
            sortProperty = sortProperty.substring(0, sortProperty.indexOf(' ')).trim();
        }
        else
        {
            sortProperty = sortProperty.trim();
        }
        return sortProperty;  
    }

public static String getSortingOrderForProperty(String name, String orderBy){
    	
        String sortOrder = IPSMetadataQueryService.SORT_ORDER_ASCEND;
	    
    	String props[] = orderBy.split(",");
    	  
    	for(int i =0;i<props.length;i++){
    		
	        String sortProperty = props[i].trim();
	        String[] pair = sortProperty.split(" ");
	        
            if(pair.length>0)
            	sortProperty = pair[0].trim();
           
            	
            if(sortProperty.equals(name)){
            	if(pair.length>1){
        			sortOrder = pair[1].trim();
            	}
            	
        		if(sortOrder.equals(IPSMetadataQueryService.SORT_ORDER_ASCEND) | sortOrder.equals(IPSMetadataQueryService.SORT_ORDER_DESCEND)){
        			return sortOrder;
            	}
            }
	}
    	
    	return sortOrder;
    }
}

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
package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSMetadataQueryService;
import com.percussion.delivery.metadata.IPSMetadataProperty.VALUETYPE;
import com.percussion.delivery.metadata.data.impl.PSCriteriaElement;
import com.percussion.delivery.metadata.utils.PSHashCalculator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static VALUETYPE getDatatype(String name, PSPropertyDatatypeMappings datatypeMappings)
    {
          String nameWithOutNamespace;
          
          if (name.contains(":"))
             nameWithOutNamespace = name.split(":")[1];
          else
             nameWithOutNamespace = name;
          
          return datatypeMappings.getDatatype(nameWithOutNamespace);
    }

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
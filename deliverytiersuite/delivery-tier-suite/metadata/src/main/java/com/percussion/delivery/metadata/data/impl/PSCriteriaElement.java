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
package com.percussion.delivery.metadata.data.impl;

import com.percussion.delivery.metadata.error.PSMalformedMetadataQueryException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author erikserating
 *
 */
@SuppressWarnings("unchecked")
public class PSCriteriaElement
{
   private String name;
   private String operation;
   private String value;
   
   /**
    * Ctor
    * @param rawCriteria the criteria string, cannot be <code>null</code>.
    * @throws PSMalformedMetadataQueryException if the criteria string was found to be malformed.
    */
   public PSCriteriaElement(String rawCriteria) throws PSMalformedMetadataQueryException
   {
      if(rawCriteria == null)
         throw new IllegalArgumentException("rawCriteria can not be null");
      parse(rawCriteria);
   }
   
   /**
    * Parses the raw criteria string and adds the values to this object.
    * @param rawCriteria assumed not <code>null</code>.
    * @throws PSMalformedMetadataQueryException
    */
   private void parse(String rawCriteria) throws PSMalformedMetadataQueryException
   {
      int firstApos = rawCriteria.indexOf("'");
      
      boolean isMalformed = true;
      for(String op : OPERATORS.keySet())
      {
         String temp = op;
         
         if(op.equalsIgnoreCase(OPERATION_TYPE.LIKE.toString()) ||
               op.equalsIgnoreCase(OPERATION_TYPE.IN.toString()))
         {
            temp = " " + op + " ";
         }
         
         // Get the current operation length. If the spaces were added in
         // each side, they will be catch here.
         int operationLength = temp.length();

         int opPos = rawCriteria.toUpperCase().indexOf(temp);
         
         if(opPos == -1)
         {   
            continue;
         }
         else
         {
            if(firstApos == -1  || (firstApos != -1 && opPos < firstApos))
            {
               name = StringUtils.trim(rawCriteria.substring(0, opPos));
               operation = op;
               value = StringUtils.trim(rawCriteria.substring(opPos + operationLength));
               if(operation.equalsIgnoreCase(OPERATION_TYPE.LIKE.toString()))
               {
                  if(!value.startsWith("'") || !value.endsWith("'"))
                  {
                     throw new PSMalformedMetadataQueryException(MALFORMED + rawCriteria);
                  }
               }
               else if(operation.equals(OPERATION_TYPE.IN.toString()))
               {
                  if(!value.startsWith("(") || !value.endsWith(")"))
                  {
                     throw new PSMalformedMetadataQueryException(MALFORMED + rawCriteria);
                  }
                 // String[] vals = chopFirstLast(value).split(",");
                  //below is the fix in case if tags  and categories having comma in their name then below code use to throw exception
                   String[] vals;
                   if(value.contains("'")){
                      vals=chopFirstLast(value).replaceAll("', '", "'~~, ~~'").replaceAll("','", "'~~, ~~'").split("~~, ~~");
                   }else{
                       vals = chopFirstLast(value).split(",");
                   }

                  for(String val : vals)
                  {
                     val = val.trim();
                     if((val.startsWith("'") && !val.endsWith("'")) || (!val.startsWith("'") && val.endsWith("'")))
                     {
                        throw new PSMalformedMetadataQueryException(MALFORMED + rawCriteria);
                     }
                     if(val.startsWith("'") || val.endsWith("'"))
                     {
                        if(hasUnescapedApos(chopFirstLast(val)))
                        {
                           throw new PSMalformedMetadataQueryException(MALFORMED + rawCriteria);
                        }
                     }
                     else
                     {
                        //Make sure its a number and nothing else so sql does not get spoofed
                        if(!val.matches("[-+]?[0-9]*\\.?[0-9]+"))
                        {
                           throw new PSMalformedMetadataQueryException(MALFORMED + rawCriteria);
                        }
                     }
                  }
               }
               else if(!(value.startsWith("'") && value.endsWith("'")))
               {
                //Make sure its a number and nothing else so sql does not get spoofed
                  if(!value.matches("[-+]?[0-9]*\\.?[0-9]+"))
                  {
                     throw new PSMalformedMetadataQueryException(MALFORMED + rawCriteria);
                  }
               }
               
               if(value.startsWith("'") || value.startsWith("("))
               {
                  value = chopFirstLast(value);
               }
               isMalformed = false;
               break;
            }
         }         
      }
      // Some basic validation to be sure this criteria element is not malformed
      if(StringUtils.isBlank(name) || StringUtils.isBlank(value))
      {
         isMalformed = true;
      }
      
      
      if(isMalformed)
         throw new PSMalformedMetadataQueryException(MALFORMED + rawCriteria);
   }
   
   /**
    * Helper method to determine if a sql string contains an unescaped Apostrophe.
    * @param s assumed to not be <code>null</code>.
    * @return <code>true</code> if an unescaped apos was found.
    */
   private boolean hasUnescapedApos(String s)
   {
      StringReader r = new StringReader(s);
      boolean lastCharApos = false;
      int c;
      try
      {
         
         while ((c = r.read()) != -1)
         {
           char ch = (char)c;
           if(lastCharApos)
           {
              if(ch != '\'')
              {
                 return true;
              }
              else
              {
                 lastCharApos = false;
              }
           }
           else if(ch == '\'')
           {
              lastCharApos = true;
           }
         }
         
      }
      catch (IOException ignore)
      {

      }
      finally
      {
         r.close();
      }
      return lastCharApos;
   }
   
   /**
    * Util method to chop first and last character off string
    * @param s assumed not <code>null</code>.
    * @return chopped string
    */
   private String chopFirstLast(String s)
   {
      return StringUtils.chop(s.substring(1));
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @return the operation
    */
   public String getOperation()
   {
      return operation;
   }
   
   public OPERATION_TYPE getOperationType()
   {
      return OPERATORS.get(operation);
   }

   /**
    * @return the value
    */
   public String getValue()
   {
      return value;
   }
   
   public static final String MALFORMED = "Query criteria element is malformed: ";
   
   public static enum OPERATION_TYPE
   {
      GREATER_THAN,
      GREATER_THAN_EQUAL,
      EQUAL,
      IN,
      LESS_THAN,
      LESS_THAN_EQUAL,
      LIKE,
      NOT_EQUAL      
   }
       
   private static final Map<String, OPERATION_TYPE> OPERATORS = 
      MapUtils.orderedMap(new HashMap<String, OPERATION_TYPE>());
   static
   {
      //Order matters largest length to smallest
      OPERATORS.put("LIKE", OPERATION_TYPE.LIKE);
      OPERATORS.put("IN", OPERATION_TYPE.IN);
      OPERATORS.put("<>", OPERATION_TYPE.NOT_EQUAL);
      OPERATORS.put("<=", OPERATION_TYPE.LESS_THAN_EQUAL);
      OPERATORS.put(">=", OPERATION_TYPE.GREATER_THAN_EQUAL);
      OPERATORS.put("!=", OPERATION_TYPE.NOT_EQUAL);
      OPERATORS.put("=", OPERATION_TYPE.EQUAL);     
      OPERATORS.put("<", OPERATION_TYPE.LESS_THAN);
      OPERATORS.put(">", OPERATION_TYPE.GREATER_THAN);
   }
   
   
   
}

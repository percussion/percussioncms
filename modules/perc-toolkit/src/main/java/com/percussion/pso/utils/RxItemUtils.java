/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.utils RxItemUtils.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.IPSItemAccessor;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.PSTextValue;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class RxItemUtils
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(RxItemUtils.class);
   
   
   /**
    * Static methods only
    */
   private RxItemUtils()
   {
   }
   
   /**
    * Gets the value of a field, handling null or empty fields. 
    * @param item the item 
    * @param fieldName the field name
    * @return the value of the field. Never <code>null</code>
    * may be <code>empty</code>
    * @throws PSCmsException when value cannot be converted
    */
   public static String getFieldValue(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {
         log.debug("no such field " + fieldName); 
         return EMPTY; 
      }
      IPSFieldValue value = fld.getValue(); 
      if(value == null)
      { 
         log.debug("field has no values " + fieldName); 
         return EMPTY;
      }
      return value.getValueAsString(); 
   }
   
   /**
    * Gets the raw value of a field
    * @param item the item or child to get the field from.
    * @param fieldName the field name
    * @return the value as an <code>Object</code>. Will be <code>null</code> if the field 
    * does not exist or has no values.
    * @throws PSCmsException
    */
   public static Object getFieldValueRaw(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {
         log.debug("no such field " + fieldName); 
         return null; 
      }
      IPSFieldValue value = fld.getValue(); 
      if(value == null)
      { 
         log.debug("field has no values " + fieldName); 
         return null;
      }
      return value.getValue(); 
   }
   
   /**
    * Gets a numeric field 
    * @param item the item or child to get the field from
    * @param fieldName the field name
    * @return the field value as a number. Will be <code>ZERO</code> if the field does not exist,
    * has no values, or is not a number.  
    * @throws PSCmsException when an error occurs 
    */
   public static Number getFieldNumeric(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {
         log.debug("no such field " + fieldName); 
         return ZERO; 
      }
      IPSFieldValue value = fld.getValue(); 
      if(value == null)
      { 
         log.debug("field has no values " + fieldName); 
         return ZERO;
      }
      String fval = value.getValueAsString();
      if(StringUtils.isBlank(fval))
      {
         return ZERO;
      }
      if(StringUtils.isNumeric(fval))
      {
         return new Integer(fval);
      }
      log.info("numeric field contains non numeric data " + fieldName + " - "  + fval);
      return ZERO;
   }
   
   /**
    * Determines if a given field is a binary field. 
    * @param item the item 
    * @param fieldName the field name
    * @return <code>true</code> if this is a binary field.  
    */
   public static boolean isBinaryField(IPSItemAccessor item, String fieldName)
   {
      String emsg; 
      if(StringUtils.isBlank(fieldName))
      {
         emsg = "field name must not be blank";
         log.debug(emsg);
         return false; 
      }
      PSItemField field = item.getFieldByName(fieldName);
      if(field == null)
      {
         emsg = "no such field " + fieldName; 
         log.debug(emsg); 
         return false;  
      }
      PSItemFieldMeta meta = field.getItemFieldMeta();
      if(meta.getBackendDataType() == PSItemFieldMeta.DATATYPE_BINARY)
      {
         return true;
      }
      return false; 
   }
   
   /**
    * Gets the value of a Date Field.
    * @param item the item or child to get the value from
    * @param fieldName the field name
    * @return the date value.  Will be <code>null</code> if the field does not exist, has no values or 
    * is not a date. 
    * @throws PSCmsException
    */
   public static Date getFieldDate(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {
         log.debug("no such field " + fieldName); 
         return null; 
      }
      IPSFieldValue value = fld.getValue(); 
      if(value == null)
      { 
         log.debug("field has no values " + fieldName); 
         return null;
      }
      if(value instanceof PSDateValue)
      {
         return (Date)value.getValue(); 
      }
      log.warn("Date field is not a date " + fieldName + " - " + value.getValueAsString());
      return null;
   }
   
   public static byte[] getFieldBinary(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {
         log.debug("no such field " + fieldName); 
         return null; 
      }
      IPSFieldValue value = fld.getValue(); 
      if(value == null)
      { 
         log.debug("field has no values " + fieldName); 
         return null;
      }
      if(value instanceof PSBinaryValue)
      {
         return (byte[])value.getValue(); 
      }
      log.warn("Binary field is not binary " + fieldName + " - " + value.getValueAsString());
      return null;
   }
   
   public static List<String> getFieldValues(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
       List<String> list = new ArrayList<String>(); 
       PSItemField fld = item.getFieldByName(fieldName);
       if(fld == null)
       {
          log.debug("no such field " + fieldName); 
          return list; 
       }
       Iterator<IPSFieldValue> values = fld.getAllValues();
       while(values.hasNext())
       {
          IPSFieldValue val = values.next();
          list.add(val.getValueAsString()); 
       }
       
       return list; 
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, IPSFieldValue value)
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {  
         String emsg = "no such field " + fieldName;  
         log.debug(emsg); 
         throw new IllegalArgumentException(emsg);  
      }
      fld.clearValues(); 
      fld.addValue(value);
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, String textValue)
   {
      log.debug("setting field " + fieldName + " value " + textValue); 
      IPSFieldValue val = new PSTextValue(textValue); 
      setFieldValue(item, fieldName, val); 
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, Date dateValue)
   {
      log.debug("setting field " + fieldName + " value " + dateValue); 
      IPSFieldValue val = new PSDateValue(dateValue);
      setFieldValue(item, fieldName, val);
   }  
 
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, Number numbValue)
   {
      log.debug("setting field " + fieldName + " value " + numbValue); 
      String textValue = String.valueOf(numbValue); 
      IPSFieldValue val = new PSTextValue(textValue); 
      setFieldValue(item, fieldName, val); 
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, DataSource data) 
   {
      log.debug("setting binary field " + fieldName); 
      try
      {
         setFieldValue(item, fieldName, data.getInputStream());
      }catch (IOException ex)
      {
         //should never happen
         log.error("Unexpected IO Exception " + ex.getLocalizedMessage(), ex); 
      }
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, InputStream streamValue)
   {
      IPSFieldValue val;
      try
      {
         val = new PSBinaryValue(streamValue);
         setFieldValue(item, fieldName, val);
      } catch (IOException ex)
      {
         //should never happen, this is really a byte array
         log.error("Unexpected IO Exception " + ex.getLocalizedMessage(), ex); 
      }
      
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, List<String> listValue)
   {
      log.debug("setting mult-value field " + fieldName); 
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {  
         String emsg = "no such field " + fieldName;  
         log.debug(emsg); 
         throw new IllegalArgumentException(emsg);  
      }
      fld.clearValues(); 
      for(String textValue : listValue)
      {
         log.debug("adding value " + textValue); 
         fld.addValue(new PSTextValue(textValue)); 
      }
   }
   
   private static final String EMPTY = "";
   private static final Number ZERO = new Integer(0); 
   
}

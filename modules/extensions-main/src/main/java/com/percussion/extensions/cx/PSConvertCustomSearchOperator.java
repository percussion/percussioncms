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
package com.percussion.extensions.cx;

import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSHtmlParamDocument;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Converts the custom search operator from what the applet will send to the
 * proper backend SQL operator.  Can also convert the operator and value(s) sent
 * to the appropriate sql where clause syntax.
 */
public class PSConvertCustomSearchOperator extends PSDefaultExtension 
   implements IPSRequestPreProcessor
{

   /**
    * Converts an operator value sent by a custom search to the appropriate
    * SQL operator. 
    * 
    * @param params The request parameters, not <code>null</code>.  
    *
    * The following params are expected:
    * <table border="1">
    *   <tr><th>Param #</th><th>Description</th><th>Required?</th><th>default
    *     value</th><tr>
    *   <tr>
    *     <td>1</td>
    *     <td>The html parameter name containing the operator to convert, one of 
    *       the <code>PSSearchField#OP_xxx</code> values is expected. If
    *       a value parameter name is not also supplied in param #2, then the
    *       operator value is converted and assigned to the html parameter with
    *       that name.  Only text operators are supported in this case.  If a
    *       value parameter name is also supplied, the behavior is different and
    *       this html parameter is not modified.  See param #2 for details.</td>
    *     <td>yes</td>
    *     <td>none</td>
    *   </tr>
    *   <tr>
    *     <td>2</td>
    *     <td>The html parameter name containing the value(s) to convert.  If 
    *       supplied, then the operator and value(s) are used to construct a 
    *       SQL fragment that can be used as a where clause.  The resulting
    *       value is stored in the parameter name specified by param #3. If the 
    *       specified parameter does not contain a value, then no action is 
    *       taken.</td>
    *     <td>no</td>
    *     <td>none</td>
    *   </tr>
    *   <tr>
    *     <td>3</td>
    *     <td>The parameter name to use to store the resulting sql fragment.
    *       Required only if the value parameter is supplied.</td>
    *     <td>If param #2 is supplied</td>
    *     <td>none</td>
    *   </tr>
    *   <tr>
    *     <td>4</td>
    *     <td>Backend column name to use in the SQL fragment.</td>
    *     <td>If param #2 is supplied</td>
    *     <td>none</td>
    *   </tr> 
    *   <tr>
    *     <td>5</td>
    *     <td>Datatype of backend column, must be TEXT, NUMBER, or DATE, used
    *       to format values in the sql query.</td>
    *     <td>no</td>
    *     <td>TEXT</td>
    *   </tr>
    *   <tr>
    *     <td>6</td>
    *     <td>Operator to preprend onto the SQL fragment (e.g. AND or OR).  If 
    *       not supplied, no operator value is prepended. Ignored if no value
    *       parameter is supplied, or if that parameter does not supply a value.
    *       </td>
    *     <td>no</td>
    *     <td>none</td>
    *   </tr>
    *   <tr>
    *     <td>7</td>
    *     <td>Format to use if the backend column type is DATE.  Supplied values
    *       must conform to those accepted by {@link SimpleDateFormat}.  
    *       </td>
    *     <td>no</td>
    *     <td>yyyy-MM-dd</td>
    *   </tr>
    *   <tr>
    *     <td>8</td>
    *     <td>Flag to get HTML param values directly from the doc sent by the 
    *       applet, or to look for HTML params in the request context.  'y' to 
    *       get them from the doc, 'n' to get them from the request context. If 
    *       'y', then and inputdoc conforming to the format expected by 
    *       {@link PSHtmlParamDocument#fromXml(Element)} must be found in the 
    *       request context.</td>
    *     <td>no</td>
    *     <td>y</td>
    *   </tr> 
    * </table> 
    * 
    * @param request The request context, not <code>null</code>.
    * 
    * @throws PSParameterMismatchException if the expected parameter is not
    * supplied.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request) 
      throws PSParameterMismatchException
   {
      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      if (params.length < 1 || params[0] == null || 
         params[0].toString().trim().length() == 0)
      {
         throw new PSParameterMismatchException(
            "Must supply operatorParamName parameter");
      }
      
      // handle getting params from doc vs request
      boolean useReqParams = "n".equalsIgnoreCase(
         getOptionalParamValue(params, 7, "y"));
      PSHtmlParamDocument paramDoc = null;   
      if (!useReqParams)
      {
         Document inputDoc = request.getInputDocument();
         if (inputDoc != null)
         {
            Element root = inputDoc.getDocumentElement();
            if (root != null && root.getNodeName().equals(
               PSHtmlParamDocument.ROOT))
            {
               paramDoc = new PSHtmlParamDocument();
               paramDoc.fromXml(root); 
            }
         }
         
         if (paramDoc == null)
         {
            // this means we didn't find or couldn't parse it
            throw new PSParameterMismatchException("No valid input doc found");
         }
      }
      
      String opParamName = params[0].toString();         
      String opVal;
      if (useReqParams)
         opVal = request.getParameter(opParamName);
      else
         opVal = (String)paramDoc.getParam(opParamName);
      
      String dataType = getOptionalParamValue(params, 4, 
         DATATYPE_TEXT).toUpperCase();
      if (!(dataType.equals(DATATYPE_DATE) || dataType.equals(DATATYPE_NUMBER) 
         || dataType.equals(DATATYPE_TEXT)))
      {
         throw new PSParameterMismatchException(
            "Invalid backendColumnDataType: " + dataType);
      }
      
      if (params.length == 1 || (params[1] == null || 
         params[1].toString().trim().length() == 0))
      {
         // operator only conversion
         if (!DATATYPE_TEXT.equals(dataType))
         {
            throw new PSParameterMismatchException(dataType + 
               " backendColumnDataType not supported for operator only" +
               " conversion.");            
         }
         
         if (PSSearchField.OP_EQUALS.equals(opVal))
            request.setParameter(opParamName, "=");
         
         request.printTraceMessage("converted operator: \"=\"");   
         return;
      }

      // use value(s) to construct SQL fragment
      if (params.length < 4)
      {
         throw new PSParameterMismatchException(
            "Invalid number of parameters");         
      }
      else
      {
         if (params[2] == null || params[2].toString().trim().length() == 0)
         {
            throw new PSParameterMismatchException(
               "Must supply sqlFragmentParamName parameter");
         }
                        
         if (params[3] == null || params[3].toString().trim().length() == 0)
         {
            throw new PSParameterMismatchException(
               "Must supply backendColumnName parameter");
         }
      } 
         
      // see if we have any values to convert
      Object[] values = null;
      if (useReqParams) 
         values = request.getParameterList(params[1].toString().trim());
      else
      {
         Object val = paramDoc.getParam(params[1].toString().trim());
         if (val != null)
         {
            if (val instanceof List)
            {
               List valList = (List)val;
               values = valList.toArray();
            }
            else
            {
               String strVal = val.toString().trim();
               if (strVal.length() > 0)
               {
                  values = new Object[1];
                  values[0] = strVal;
               }
            }
         }
      }
         
      if (values == null || values.length == 0)
      {
         // no value supplied, so we're done
         return;
      }      
      
      // get other params for the conversion   
      String sqlFragmentParamName = params[2].toString().trim();
      String beColName = params[3].toString().trim();
      
      // check for other optional params      
      String connector = getOptionalParamValue(params, 5, "");
      String dateFormat = getOptionalParamValue(params, 6, DEFAULT_DATE_FORMAT);
      
      // handle conversion
      String fragment = convert(request, opVal, values, beColName, dataType, 
         connector, dateFormat); 
      request.printTraceMessage("converted fragment: " + fragment);
      
      request.setParameter(sqlFragmentParamName, fragment);
   }
   
   /**
    * Convert the supplied value data to a where clause expression. 
    * 
    * @param req The request context, to use to log any warnings, assumed not 
    * <code>null</code>.
    * @param opVal The operator, assumed not <code>null</code> or empty, and to 
    * be one of the <code>PSSearchField.OP_XXX</code> values. 
    * @param values The value(s) to convert assumed not <code>null</code> or 
    * empty, and to contain at least one non-empty value.
    * @param beColName The name of back-end column, assumed not 
    * <code>null</code> or empty and to be qualified as necessary.
    * @param dataType One of the <code>DATATYPE_xxx</code> values, assumed not 
    * <code>null</code>.
    * @param connector Prepended onto the expression with a space following it,
    *  assumed not <code>null</code>, may be empty. 
    * @param dateFormat If <code>dataType</code> is {@link #DATATYPE_DATE}, then
    * this is assumed to be a valid simple date format string, not 
    * <code>null</code> or empty.  Ignored otherwise.
    * 
    * @return The clause, never <code>null</code> or empty, padded with spaces
    * on either end.
    */
   private String convert(IPSRequestContext req, String opVal, Object[] values, 
      String beColName, String dataType, String connector, String dateFormat)
   {
      StringBuffer buf = new StringBuffer();
      
      buf.append(" ");
      if (connector.length() > 0)
      {
         buf.append(connector);
         buf.append(" ");         
      }      
      
      if (opVal.equals(PSSearchField.OP_LIKE))
      {
         // only expecting one value
         buf.append(beColName);
         buf.append(" like ");
         buf.append(getValue(req, values[0].toString(), dataType, opVal, 
            dateFormat).toString());
      }
      else if (opVal.equals(PSSearchField.OP_IN))
      {
         buf.append(beColName);
         buf.append(" in ");
         buf.append(" (");
         boolean first = true;
         for (int i = 0; i < values.length; i++)
         {
            if (!first)
               buf.append(",");
            else
               first = false;
               
            buf.append(getValue(req, values[i].toString(), dataType, opVal, 
               dateFormat).toString());            
         }
         buf.append(")");         
      }
      else if (opVal.equals(PSSearchField.OP_EQUALS) || 
         opVal.equals(PSSearchField.OP_GREATERTHAN) || 
         opVal.equals(PSSearchField.OP_LESSTHAN))
      {
         // only expecting one value
         Object val = getValue(req, values[0].toString(), dataType, dataType, 
            dateFormat);
         if (opVal.equals(PSSearchField.OP_EQUALS) && 
            val instanceof String[])
         {
            // need to search range of start of date1 to start of date2
            String[] ops = {" >= ", " < "};
            String[] dates = (String[])val;            
            buf.append(formatMultiValues(beColName, dates, ops));
         }
         else
         {
            String convOp;
            if (opVal.equals(PSSearchField.OP_EQUALS))
               convOp = " = ";
            else if (opVal.equals(PSSearchField.OP_GREATERTHAN))
               convOp = " > ";
            else
               convOp = " < ";
            
            buf.append(beColName);
            buf.append(convOp);                     
            buf.append(val);
         }
      }
      else if (opVal.equals(PSSearchField.OP_BETWEEN))
      {
         for (int i = 0; i < values.length; i++)
         {
            values[i] = getValue(req, values[i].toString(), dataType, opVal, 
               dateFormat);
         }
         String[] ops = {" >= ", " <= "};
         buf.append(formatMultiValues(beColName, values, ops));
      }
      
      buf.append(" ");
      return buf.toString();
   }
   
   /**
    * Format the provided values and operators into a clause fragment.
    * 
    * @param beColName The name of the column, assumed not <code>null</code> or 
    * empty and to be qualified as necessary. 
    * @param vals Array of values, assumed not <code>null</code>.
    * @param ops Array of operators for each value, assumed to contian valid
    * sql operators and to contain the same number of entries as 
    * <code>vals</code>.
    * 
    * @return The formated query string.
    */
   private String formatMultiValues(String beColName, Object[] vals, 
      String[] ops)
   {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < vals.length && i < ops.length; i++)
      {
         if (i > 0)
            buf.append(" AND ");
         buf.append(beColName);
         buf.append(ops[i]);
         buf.append(vals[i].toString());
      }
      
      return buf.toString();
   }

   /**
    * Get the value for the supplied data, converted as necessary.
    * 
    * @param req The request context to use for logging warnings, assumed not 
    * <code>null</code> or empty.
    * 
    * @param val The value to convert, assumed not <code>null</code> or empty.
    * @param dataType Assumed to be one of the <code>DATATYPE_xxx</code> values.
    * @param operator Assumed to be one of the <code>PSSearchField.OP_XXX</code> 
    * values.
    * @param dateFormat If <code>dataType</code> is {@link #DATATYPE_DATE}, then
    * this is assumed to be a valid simple date format string, not 
    * <code>null</code> or empty.  Ignored otherwise.
    * 
    * @return The formated value.  If <code>dataType</code> is 
    * {@link #DATATYPE_DATE}, and the <code>operator</code> is 
    * {@link PSSearchField#OP_EQUALS}, a <code>String[2]</code> is returned,
    * containing the supplied date value and the following day as the two 
    * values. Otherwise a <code>String</code> is returned. All returned values 
    * are formatted for immediate use in a sql statement, never 
    * <code>null</code> or empty.
    */
   private Object getValue(IPSRequestContext req, String val, String dataType, 
      String operator, String dateFormat)
   {
      Object result = val;
      if (dataType.equals(DATATYPE_TEXT))
       {
          result = getTextVal(val);
       }
       else if (dataType.equals(DATATYPE_DATE))
       {
          SimpleDateFormat srcFormat = new SimpleDateFormat(
            DEFAULT_DATE_FORMAT);
          SimpleDateFormat tgtFormat = new SimpleDateFormat(dateFormat);
          
          String strDate = val;
          try
          {
             Date date = srcFormat.parse(strDate);
             if (operator.equals(PSSearchField.OP_EQUALS))
             {
               // need to create two values, one for the date given, one for the
               // following date
             
                Calendar cal = new GregorianCalendar();
                cal.setTime(date);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                Date date2 = cal.getTime();
                String[] dates = new String[2];
                dates[0] = getTextVal(tgtFormat.format(date));
                dates[1] = getTextVal(tgtFormat.format(date2));
                result = dates;
             }
             else
             {
                result = getTextVal(strDate);
             }
          }
          catch (ParseException e)
          {
             // log it and don't convert the format
               
             req.printTraceMessage("failed to convert date value: " + 
                e.getLocalizedMessage());
             result = getTextVal(strDate);
          }            
       }
       
       return result;         
   }
   
   /**
    * Surrounds the supplied string with single-quotes.
    * 
    * @param val The value to quote, assumed not <code>null</code> or empty.
    * 
    * @return The quoted string, never <code>null</code> or empty.
    */
   private String getTextVal(String val)
   {
      return "'" + val + "'";
   }

   /**
    * Get an optional parameter value as a string.  If the params array contains
    * the specified index, and that param value is not <code>null</code>, 
    * <code>toString()</code> is called and if the resulting string is not 
    * empty, it is returned.  Otherwise the default value is returned.
    * 
    * @param params The param array, assumed not <code>null</code>.
    * @param index The index into the array of the parameter to retrieve.  May
    * be greater than <code>params.length</code>.
    * @param defaultValue The default value to return if the parameter is not
    * supplied or does not have a non-empty string value.  May be 
    * <code>null</code> or empty.
    * 
    * @return A non-empty value for the parameter if supplied, or else the
    * default value which may be <code>null</code> or empty.
    */
   private String getOptionalParamValue(Object[] params, int index, 
      String defaultValue)
   {
      String val = defaultValue;
      
      if (params.length > index && params[index] != null)
      {
         String tempVal = params[index].toString().trim();
         if (tempVal.length() > 0)
            val = tempVal;
      }
      
      return val;
   }   
   
   /**
    * Constant for datatype text.
    */
   private static final String DATATYPE_TEXT = "TEXT";
   
   /**
    * Constant for datatype number.
    */
   private static final String DATATYPE_NUMBER = "NUMBER";
   
   /**
    * Constant for datatype date.
    */
   private static final String DATATYPE_DATE = "DATE";
   
   /**
    * Expected format of any date value supplied.
    */
   private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
}

/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.cms;

import com.percussion.data.IPSDataExtractor;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Used to perform a query and validate the results against expected values.
 * Executes a query by using an internal request handler and then gets the
 * result set and checks a list of supplied column name-value pairs.
 */
public class PSValidateModifyStep extends PSModifyStep
{
   /**
    * Constructs a step.  The request handler must be set on the step after
    * construction by a call to {@link
    * #setHandler(IPSInternalRequestHandler) setHandler}.
    *
    * @param requestName The request name that is used to retrieve the resource
    * handler.  Must refer to a resource handler that implements the
    * IPSInternalResultHandler interface.  May not be <code>null</code>.
    *
    * @param dbActionType The dbaction type that must be set in the request
    * params before making a request against this handler.  May not be
    * <code>null</code>.
    *
    * @param validations A Map of column names to column value validations to
    * perform.  May not be <code>null</code> or empty.  Each entry is validated
    * against the result returned by the execute query.  The Key is the column
    * name and the Value is an IPSReplacementValue or <code>null</code>.
    * The column will be retrieved from the result set and the value
    * will be retrieved as a String, trimmed, and compared to the
    * expected result which is extracted from the execution data, unless the
    * validation entries value is <code>null</code>,
    * in which case the result set's value should also be <code>null</code>.
    */
   public PSValidateModifyStep(String requestName, String dbActionTypeParam,
      String dbActionType, Map validations)
   {
      super(requestName, dbActionTypeParam, dbActionType);

      if (validations == null || validations.size() == 0)
         throw new IllegalArgumentException(
            "validations may not be null or empty");

      // build map, replacing value with extractors
      try
      {
         Iterator entries = validations.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Map.Entry)entries.next();
            Object key = entry.getKey();
            Object value = null;
            IPSReplacementValue repVal = (IPSReplacementValue)entry.getValue();
            if (repVal != null)
            {
               value = PSDataExtractorFactory.createReplacementValueExtractor(
                  repVal);
            }
            m_validations = new HashMap();
            m_validations.put(key, value);
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }


   /**
    * Executes this step.  See {@link PSModifyStep#execute(PSExecutionData)
    * super.execute()} for more info.  The request that is executed must
    * return a result, and those results are validated against the enties
    * supplied in the ctor.
    */
   public void execute(PSExecutionData data)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException, PSSystemValidationException
   {
      if (data == null)
         throw new IllegalArgumentException(
            "data must be supplied");

      if (getHandler() == null)
         throw new IllegalStateException(
            "cannot execute unless a handler has been set");

      PSRequest request = data.getRequest();
      Map<String, Object> tmpParams = null;
      PSExecutionData intExecData = null;
      ResultSet resultSet = null;

      try
      {

         /* need to save the params truncate the current params, and then set
          * the saved params back on the request once we're done.
          */
         tmpParams = request.getParameters();
         request.setParameters(request.getTruncatedParameters());

         // set the DBActionType - it may not match what's in the request
         // get the param map
         request.setParameter(m_dbActionParam, m_dbActionType);

         IPSInternalResultHandler handler =
            (IPSInternalResultHandler)getHandler();
         intExecData = handler.makeInternalRequest(request);

         // now get the results
         resultSet = handler.getResultSet(intExecData);

         validate(data, resultSet);
         resultSet.close();
         resultSet = null;

      }
      catch (SQLException e)
      {
         // convert to validation exception
         String errorText = "";
         for (int errNo = 1; e != null; errNo++)
         {
            errorText +=   "[" + errNo + "] " + e.getSQLState() +
                           ": " + e.getMessage() + " ";
            e = e.getNextException();
         }

         Object[] args = {getName(), errorText};
         throw new PSSystemValidationException(
            IPSServerErrors.CE_MODIFY_VALIDATION_EXCEPTION, args);
      }
      catch (PSSystemValidationException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         Object[] args = {getName(), e.toString()};
         throw new PSSystemValidationException(
            IPSServerErrors.CE_MODIFY_VALIDATION_EXCEPTION, args);
      }
      finally
      {
         try{
            if (resultSet != null)
               resultSet.close();
         } catch (Exception e){/* ignore - already an error */}

         if (intExecData != null)
            intExecData.release();

         if (tmpParams != null)
            request.setParameters(tmpParams);
      }
   }

   /**
    * Sets the handler on this step.  See {@link
    * PSModifyStep#setHandler(IPSInternalRequestHandler) super.setHandler()} for
    * more info. This class additionally validates that the handler is an
    * instance of {@link IPSInternalResultHandler}.
    */
   public void setHandler(IPSInternalRequestHandler handler)
   {
      super.setHandler(handler);

      if (!(handler instanceof IPSInternalResultHandler))
         throw new IllegalArgumentException(
            "handler must be an instance of IPSInternalResultHandler");

   }

   /**
    * Walks map of validations and checks each column against the result set.
    * If multiple rows are supplied, each row is validated.  If no rows are
    * supplied, the validation will fail.  Column values are retrieved as
    * Strings and trimmed before comparing to the supplied validation entries.
    * If a column value supplied is <code>null</code>, then the expected result
    * from the resultset will be <code>null</code> as well.
    *
    * @param data The execution data to use to extract IPSReplacement values.
    * May not be <code>null</code>.
    * @param rs The result set to validate. May not be <code>null</code>.
    *
    * @throws PSSystemValidationException if any column specified in the validations
    * is missing from the resultset, or whose value does not match the expected
    * result for any row returned.
    * @throws SQLException if there are any errors using the result set.
    * @throws PSDataExtractionException if a replacement value cannot be
    * extracted from the data.
    */
   private void validate(PSExecutionData data, ResultSet rs) throws
           PSSystemValidationException, SQLException, PSDataExtractionException
   {
      if (data == null || rs == null)
         throw new IllegalArgumentException("data or rs may not be null");

      String[][] validations = extractValues(data);

      boolean hasRows = false;
      while(rs.next())
      {
         hasRows = true;

         for (int i = 0; i < validations.length; i++)
         {
            String colName = validations[i][0];
            String colValue = validations[i][1];

            String resultVal = rs.getString(colName);
            if ((resultVal == null && colValue != null) ||
               (resultVal != null && !resultVal.trim().equals(colValue)))
            {
               if(colName.equals("EDITREVISION")){

                  if (resultVal.trim().equals("-1"))
                     throw new PSSystemValidationException(
                        IPSServerErrors.CE_MODIFY_VALIDATION_FAIL_NOT_CHECKOUT);
                  else
                     throw new PSSystemValidationException(
                        IPSServerErrors
                           .CE_MODIFY_VALIDATION_FAIL_OLD_EDITREVISION);

               }
               else
               {
                  Object[] args = {colName, colValue == null ? "null" : colValue,
                     resultVal == null ? "null" : resultVal};

                  throw new PSSystemValidationException(
                     IPSServerErrors.CE_MODIFY_VALIDATION_FAIL, args);
               }
            }
         }
      }
      if (!hasRows)
      {
         Object[] args = {getName(), validations[0][0]};
         throw new PSSystemValidationException(
            IPSServerErrors.CE_MODIFY_VALIDATION_FAIL, args);
      }

   }

   /**
    * Extracts runtime values to use to validate results of the query.
    *
    * @param data The execution data, assumed not <code>null</code>.
    *
    * @return A two dimensional array, each row is the column as a String and
    * the expected value either as a String or <code>null</code>.
    *
    * @throws PSDataExtractionException if a replacement value cannot be
    * extracted from the data.
    */
   private String[][] extractValues(PSExecutionData data)
      throws PSDataExtractionException
   {
      String[][] results = new String[m_validations.size()][2];
      Iterator entries = m_validations.entrySet().iterator();
      int i = 0;
      while(entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         results[i][0] = (String)entry.getKey();
         IPSDataExtractor extractor = (IPSDataExtractor)entry.getValue();
         results[i++][1] = extractor.extract(data).toString();
      }

      return results;
   }


   /**
    * Map of validations to perform.  Key is a String referencing a column name
    * in the result set, and value is the appropriate IPSDataExtractor to be
    * used at runtime.  Set in ctor, never <code>null</code> or modified after
    * that.
    */
   private Map m_validations = null;
}

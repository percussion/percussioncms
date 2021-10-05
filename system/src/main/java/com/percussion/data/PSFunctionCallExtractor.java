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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.data;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSFunctionParamValue;
import com.percussion.extension.PSDatabaseFunctionDef;
import com.percussion.extension.PSDatabaseFunctionDefParam;

import java.text.ChoiceFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The PSFunctionCallExtractor class is used to extract data for all the
 * function parameter values for the database function call (specified in its
 * ctor). The processed parameter values are then substituted in proper order
 * in the body of the database function.
 *
 * @see com.percussion.design.objectstore.PSFunctionCall
 * @see com.percussion.design.objectstore.PSFunctionParamValue
 * @see com.percussion.extension.PSDatabaseFunctionDef
 */
public class PSFunctionCallExtractor extends PSDataExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param funcCall The database function that contains the function
    * definition and the parameter values, may not be <code>null</code>.
    */
   public PSFunctionCallExtractor(PSFunctionCall funcCall)
   {
      super(getReplacementValues(funcCall));
      m_funcCall = funcCall;
   }

   /**
    * See {@link com.percussion.data.IPSDataExtractor#extract(PSExecutionData)}
    * for details.
    */
   public Object extract(PSExecutionData data)
      throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * See {@link com.percussion.data.IPSDataExtractor#extract(
    * PSExecutionData, Object)}for details.
    */
   public Object extract(PSExecutionData data, Object defValue)
         throws PSDataExtractionException
   {
      PSDatabaseFunctionDef funcDef = m_funcCall.getDatabaseFunctionDef();
      String body = funcDef.getBody();
      IPSDataExtractor dataExtractor = null;
      PSFunctionParamValue[] params = m_funcCall.getParamValues();
      String[] args = new String[params.length];

      for (int i = 0; i < params.length; i++)
      {
         String text = null;
         PSFunctionParamValue param = params[i];
         IPSReplacementValue value = param.getValue();
         boolean inQuotes = encloseInQuotes(funcDef, param, i);
         boolean staticBindParam = isStaticBindParam(funcDef, i);

         if (!staticBindParam)
         {
            Object obj = extract(value, data);
            if (obj == null)
            {
               text = null;
            }
            else if (obj instanceof Collection)
            {
               // check if this collection contains a non-null value
               text = null;
               Iterator itCol = ((Collection)obj).iterator();
               while (itCol.hasNext())
               {
                  if (itCol.next() != null)
                  {
                     text = "?";
                     break;
                  }
               }
            }
            else
            {
               text = "?";
            }
         }
         else if (param.isStaticValue() || (data == null))
         {
            text = value.getValueText();
            if (text.trim().length() < 1)
            {
               text = null;
            }
            else if (inQuotes)
            {
               text = "\'" + text + "\'";
            }
         }
         else
         {
            try
            {
               Object obj = extract(value, data);
               if (obj == null)
               {
                  text = null;
               }
               else if (obj instanceof Collection)
               {
                  Collection nonNullColl = new ArrayList();
                  Iterator it = ((Collection)obj).iterator();
                  while (it.hasNext())
                  {
                     Object o = it.next();
                     if (o != null)
                        nonNullColl.add(o);
                  }

                  Iterator nit = nonNullColl.iterator();
                  boolean hasNext = nit.hasNext();

                  if (!hasNext)
                     text = null;
                  else
                     text = "";

                  while (hasNext)
                  {
                     String temp = nit.next().toString();
                     if (inQuotes)
                        temp = "\'" + temp + "\'";
                     text += temp;
                     hasNext = nit.hasNext();
                     if (hasNext)
                        text += ", ";
                  }
               }
               else
               {
                  text = obj.toString();
                  if (text.trim().length() < 1)
                  {
                     text = null;
                  }
                  else if (inQuotes)
                  {
                     text = "\'" + text + "\'";
                  }
               }
            }
            catch (IllegalArgumentException ex)
            {
               throw new PSDataExtractionException(
                  IPSDataErrors.DATA_EXTRACTOR_CREATE_ERROR,
                  value.getValueType());
            }
         }
         args[i] = text;
      }

      String ret = null;
      try
      {
         ret = formatFunctionBody(body, args);
      }
      catch (ParseException pex)
      {
         Object[] errArgs = new Object[]
         {
            funcDef.getName(), pex.getLocalizedMessage()
         };
         throw new PSDataExtractionException(
            IPSObjectStoreErrors.DATABASE_FUNCTION_PARSE_ERROR,
            errArgs);
      }
      return ret;
   }

   /**
    * Extracts the value of the specified replacement value from the execution
    * data.
    *
    * @param value replacement value whose value is to be extracted from the
    * execution data, assumed not <code>null</code>
    *
    * @param data execution data, assumed not <code>null</code>
    *
    * @return the value of the specified replacement value, may be
    * <code>null</code>
    *
    * @throws PSDataExtractionException if an error condition causes the
    * extraction to fail
    */
   private Object extract(IPSReplacementValue value, PSExecutionData data)
      throws PSDataExtractionException
   {
      IPSDataExtractor dataExtractor =
         PSDataExtractorFactory.createReplacementValueExtractor(value);
      return dataExtractor.extract(data);
   }

   /**
    * Formats the database function body using the specified paramater values.
    *
    * @param body the database function body, assumed not <code>null</code> and
    * non-empty
    * @param paramValues contains the value of database function parameters,
    * assumed not <code>null</code>, may be empty
    *
    * @return the formatted database function body, may be <code>null</code>
    */
   public String formatFunctionBody(String body, String[] paramValues)
      throws ParseException
   {
      MessageFormat msgFormat = new MessageFormat(body);
      Format[] formats = msgFormat.getFormats();

      // fix any choice formats
      for (int i = 0; i < formats.length; i++)
      {
         if (formats[i] instanceof ChoiceFormat)
         {
            ChoiceFormat chFormat = (ChoiceFormat)formats[i];
            double[] indices =  chFormat.getLimits();
            msgFormat.setFormat(i, null);
            String value = null;
            boolean found = false;

            for (int j = 0; j < indices.length; j++)
            {
               int index = (int)indices[j];
               if (index < paramValues.length)
               {
                  if ( (!found) && (paramValues[index] != null) &&
                  (paramValues[index].trim().length() > 0))
                  {
                     value = paramValues[index];
                     found = true;
                  }
                  else
                  {
                     paramValues[index] = "";
                  }
               }
            }
            if (value == null)
            {
               // all values for the choice are null or empty
               return null;
            }
            else
            {
               paramValues[i] = value;
            }
         }
      }

      for (int k = 0; k < paramValues.length; k++)
      {
         if (paramValues[k] == null)
            return null;
      }

      return msgFormat.format(paramValues);
   }

   /**
    * Returns the replacement values contained in the database function
    * parameters of the specified function call.
    *
    * @param funcCall The database function that contains the function
    * definition and the parameter values, may not be <code>null</code>.
    *
    * @return the replacement values contained in the database function call
    * parameters, never <code>null</code>, may be empty if the database
    * function does not require any paramaters. All members of the returned
    * array are guaranteed to be non-<code>null</code>.
    */
   private static IPSReplacementValue[] getReplacementValues(
      PSFunctionCall funcCall)
   {
      if (funcCall == null)
         throw new IllegalArgumentException("funcCall may not be null");

      List values = new ArrayList();
      PSFunctionParamValue[] vals = funcCall.getParamValues();

      for (int i = 0; i < vals.length; i++)
         values.add(vals[i].getValue());

      IPSReplacementValue[] valueArray = new IPSReplacementValue[values.size()];
      return (IPSReplacementValue[])(values.toArray(valueArray));
   }

   /**
    * Determines if the specified parameter value (<code>value</code>) should
    * be enclosed in single quotes when being converted to a string
    * (for use in WHERE clause).
    * <p>
    * If the parameter value is a backend column, then <code>false</code> is
    * returned, else the parameter definition at the specified index is obtained
    * from the function definition. If the parameter is of type number then
    * <code>false</code> is returned, otherwise <code>true</code> is returned.
    *
    * @param funcDef, database function definition, assumed not
    * <code>null</code>
    * @param value the function parameter, assumed not <code>null</code>
    * @param index the index of the parameter, assumed valid index
    *
    * @return <code>true</code> if this parameter should be enclosed in
    * single quotes, <code>false</code> otherwise.
    */
   private boolean encloseInQuotes(
      PSDatabaseFunctionDef funcDef, PSFunctionParamValue value, int index)
   {
      boolean encloseInQuotes = true;
      if (value.isBackEndColumn())
      {
         encloseInQuotes = false;
      }
      else
      {
         PSDatabaseFunctionDefParam param = funcDef.getParamAtIndex(index);
         encloseInQuotes = param.isText();
      }
      return encloseInQuotes;
   }

   /**
    * Determines if the database function parameter defined at the specified
    * index is bound statically or dynamically.
    *
    * @param funcDef, database function definition, assumed not
    * <code>null</code>
    * @param index the index of the parameter, assumed valid index
    *
    * @return <code>true</code> if the parameter at the specified index is
    * bound statically, <code>false</code> otherwise.
    */
   private boolean isStaticBindParam(PSDatabaseFunctionDef funcDef, int index)
   {
      PSDatabaseFunctionDefParam param = funcDef.getParamAtIndex(index);
      return param.isStaticBind();
   }

   /**
    * Stores the database function for which the data has to be extracted,
    * initialized in the ctor, never <code>null</code> or modified after
    * initialization.
    */
   private PSFunctionCall m_funcCall;

}

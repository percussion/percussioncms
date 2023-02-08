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

package com.percussion.data;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSFunctionParamValue;
import com.percussion.extension.PSDatabaseFunctionDef;
import com.percussion.extension.PSDatabaseFunctionDefParam;

import java.util.Iterator;


/**
 * The PSFunctionBlock class defines a block of text which contains one or
 * more database functions.
 *
 * @see PSStatementBlock
 */
public class PSFunctionBlock extends PSStatementBlock
   implements IPSStatementBlock
{
   /**
    * Construct an empty function block.
    *
    * @param isStatic <code>true</code> if the block should always be used,
    * <code>false</code> if it should be ignored when any of the XML fields
    * contain <code>NULL</code> values
    */
   public PSFunctionBlock(boolean isStatic)
   {
      super(isStatic);
   }

   /**
    * See {@link IPSStatementBlock#hasStaticSql()} for details.
    */
   public boolean hasStaticSql()
   {
      boolean staticSql = true;
      for (int i = 0; (i < m_blocks.size()) && staticSql; i++)
      {
         Object cur = m_blocks.get(i);
         if (cur instanceof PSFunctionCall)
            staticSql = ((PSFunctionCall)cur).hasStaticParamsOnly();
      }
      return staticSql;
   }

   /**
    * See {@link IPSStatementBlock#addReplacementField(
    * IPSReplacementValue, Object[])} for details.
    */
   public void addReplacementField(IPSReplacementValue value, Object[] params)
   {
      if (value == null)
         throw new IllegalArgumentException(
            "replacement value may not be null");

      try
      {
         if (value instanceof PSFunctionCall)
         {
            PSFunctionCall funcCall = (PSFunctionCall)value;
            funcCall.initialize();
            m_blocks.add(value);
            addDynamicBindParams(funcCall);
         }
         else
         {
            super.addReplacementField(value, params);
         }
      }
      catch(PSDataExtractionException ex)
      {
         throw new IllegalArgumentException(ex.getLocalizedMessage());
      }
   }

   /**
    * Check if the specified database function has params bound dynamically.
    * Adds a <code>PSStatementColumn</code> object for each dynamically bound
    * param to the list of statement blocks.
    *
    * @param funcCall the database function call whose dynamically bound params
    * are to be added to the list of statement blocks, assumed not
    * <code>null</code>
    */
   private void addDynamicBindParams(PSFunctionCall funcCall)
   {
      PSDatabaseFunctionDef funcDef = funcCall.getDatabaseFunctionDef();
      Iterator itDef = funcDef.getParams();
      Iterator itVal = funcCall.getParameters().iterator();
      while (itDef.hasNext() && itVal.hasNext())
      {
         PSDatabaseFunctionDefParam paramDef =
            (PSDatabaseFunctionDefParam)itDef.next();
         PSFunctionParamValue paramVal =
            (PSFunctionParamValue)itVal.next();

         if (!paramDef.isStaticBind())
         {
            int jdbcType = paramDef.getJDBCType();
            PSStatementColumn stmtCol = new PSStatementColumn(
               paramVal.getValue(), jdbcType, null, null);

            stmtCol.setPlaceHolder("");
            m_blocks.add(stmtCol);
         }
      }
   }

   /**
    * See {@link IPSStatementBlock#hasStaticSql()} for details.
    */
   public void buildStatement(StringBuilder buf, PSExecutionData data)
      throws PSDataExtractionException
   {
      // if we're doing omit when null, check if we have any null values
      if (shouldThisBeOmitted(data))
         return;

      StringBuilder tmpBuf = new StringBuilder();
      for (int i = 0; i < m_blocks.size(); i++)
      {
         Object cur = m_blocks.get(i);
         if (cur instanceof PSStatementColumn)
            tmpBuf.append(((PSStatementColumn)cur).getPlaceHolder(data));
         else if (cur instanceof PSFunctionCall)
         {
            String res =
               getFunctionCallReplacementText((PSFunctionCall)cur, data);
            if ((res == null) && (!isStaticBlock()))
            {
               // found a block which is dynamic and null, so do not modify
               // the input buffer "buf". clear the temporary buffer.
               tmpBuf.setLength(0);
               break;
            }
            tmpBuf.append(res);
         }
         else
            tmpBuf.append((String)cur);
      }
      buf.append(tmpBuf);
   }

   /**
    * Returns the string which can be used to represent the specified
    * database function call in a WHERE clause.
    *
    * @param funcCall the database function call whose replacement text is
    * to be generated, assumed not <code>null</code>
    *
    * @param data the run-time context info for the request, may be
    * <code>null</code> only if <code>hasStaticSql()</code> returns
    * <code>true</code>
    *
    * @return the string form of the specified database function call which
    * can be used in a WHERE clause, may be <code>null</code>, never empty
    * if not <code>null</code>
    *
    * @throws PSDataExtractionException if the definition of the
    * database function is missing or any error occurs extracting the
    * function parameter values from <code>data</code>
    */
   private String getFunctionCallReplacementText(
      PSFunctionCall funcCall, PSExecutionData data)
         throws PSDataExtractionException
   {
      PSFunctionParamValue[] params = funcCall.getParamValues();
      PSFunctionCallExtractor funcCallEx =
         new PSFunctionCallExtractor(funcCall);

      Object res = funcCallEx.extract(data);
      return (res == null) ? null : res.toString();
   }
}


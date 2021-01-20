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
package com.percussion.data;

import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.error.PSEvaluationException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.PSRequest;
import com.percussion.util.PSCollection;
import com.percussion.utils.tools.PSPatternMatcher;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * The PSConditionalEvaluator class is used to perform evaluate
 * conditions, returning <code>true</code> if the conditions are
 * met or <code>false</code> otherwise.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSConditionalEvaluator
{
   /**
    * Constructs a conditional evaluator, parsing the conditionals
    * and building the appropriate internal representation which is ready
    * for run-time execution.
    *
    * @param   conditionals   the collection of PSConditional objects
    */
   public PSConditionalEvaluator(PSCollection conditionals)
   {
      super();
      try
      {
         tokenize(conditionals);
      }
      catch (PSEvaluationException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Checks the conditionals against the specified data. Tokens representing
    * variables are substituted with their run-time values before performing
    * the check.
    * <p>
    * This evaluator can use the request context hash tables, the input
    * XML document and the result set(s) for processing.
    * <P>
    * When using multiple conditionals (chaining conditionals) a boolean
    * operator must be specified on all but the last conditional. The boolean
    * operators currently supported are AND and OR. AND is the default
    * boolean operator. AND has a higher precedence than OR. All conditionals
    * joined by AND will be evaluated before the corresponding OR
    * conditionals. For instance, in the following example:
    * <P>
    * <TABLE>
    *     <TR>
    *        <TH>Name</TH>
    *        <TH>Operator</TH>
    *        <TH>Value</TH>
    *        <TH>Boolean</TH>
    *     </TR>
    *     <TR>
    *        <TD>products.status</TD>
    *        <TD>=</TD>
    *        <TD>'P'</TD>
    *        <TD>OR</TD>
    *     </TR>
    *     <TR>
    *        <TD>PSXUserContext/Logins/SecurityProvider</TD>
    *        <TD>=</TD>
    *        <TD>LDAP</TD>
    *        <TD>AND</TD>
    *     </TR>
    *     <TR>
    *        <TD>PSXUserContext/Logins/UserAttributes/ou</TD>
    *        <TD>=</TD>
    *        <TD>Engineering</TD>
    *        <TD></TD>
    *     </TR>
    * </TABLE>
    * <P>
    * Any product with a status of 'P' will be returned. In addition, any user
    * who logged in through LDAP and is part of the Engineering organizational
    * unit will get back all rows -- that is, with status set to any value.
    * If the AND conditions were not of higher precedence, the result set
    * would change. It would cause the first check to be if the status is 'P' or
    * the user logged in through LDAP. This filters correctly, but then we apply
    * the rule that they must also be in Engineering. This will cause
    * Engineering to get all records, as expected, but everyone outside of
    * Engineering will now get NO records, rather than records with a status
    * of 'P'.
    * <P>
    * One use of conditionals is in the PSRequestor object to check the input
    * data. If the input data meets the selection criteria, the request is
    * handled. Input data is often provided as an INPUT parameter defined on a
    * HTML FORM.
    *
    * @param   data The execution data the evaluator will be applied to.
    * The row data will be obtained by calling getCurrentResultRowData() on
    * this parameter.
    *
    *
    * @return   <code>true</code> if the conditional criteria is met,
    *          <code>false</code> otherwise
    */
   public boolean isMatch(PSExecutionData data)
      throws PSEvaluationException
   {
      if (m_tokens == null)
         return true;

      final String nullId = "";

      Stack myStack = new Stack();
      boolean result, popBoolOne, popBoolTwo;

      Object pushIn = null;
      Object value  = null;
      Object variable = null;

      // data.setCurrentResultSetMetaData(m_metaData);

      // set up trace
      Object handler = data.getLogHandler();
      PSDebugLogHandler dh = null;
      if(handler instanceof PSDebugLogHandler)      
         dh = (PSDebugLogHandler)handler;
      int traceFlag = PSTraceMessageFactory.CONDITIONAL_EVAL_FLAG;

      for (int i = 0; i < m_tokens.size(); i++)
      {
         boolean popFlag = false;
         PSConditionalToken token = (PSConditionalToken)m_tokens.get(i);

         switch (token.m_op)
         {
         case OPCODE_LOAD:
            if (data == null)
               pushIn = nullId;
            else {
               try {
                  pushIn = token.m_rtValue.extract(data);
               } catch (com.percussion.data.PSDataExtractionException e) {
                  throw new PSEvaluationException(e.getErrorCode(), e.getErrorArguments());
               }
            }
            break;
         default:
            popFlag = true;  // in this case, we pop
            break;
         }

         if (!popFlag)
         {
            if (pushIn == null)
               myStack.push(nullId);
            else
               myStack.push(pushIn);
         }
         else
         {
            // pop up one or two values based on unary or binary operator
            int opCode = token.m_op;

            if ((opCode == OPCODE_ISNULL) || (opCode == OPCODE_ISNOTNULL))
            {
               variable = myStack.pop();

               if (variable instanceof List)
               {
                  List listVariable = (List)variable;

                  result = (listVariable==null || isListEmpty(listVariable));
               }
               else if (variable instanceof String)
               {
                  result = (((String)variable).equals(nullId));
               }
               else
               {
                  try
                  {
                     variable = PSDataConverter.convert(variable,
                              PSDataConverter.DATATYPE_NULL);
                  }
                  catch (IllegalArgumentException illArg)
                  {
                     // This is ok
                  }

                  result = (variable == null);
               }

               // if they want NOT NULL, then invert the result
               if (opCode == OPCODE_ISNOTNULL)
               {
                  result = !result;
               }

               myStack.push(String.valueOf(result));

               // trace the result
               if (dh != null && dh.isTraceEnabled(traceFlag))
               {
                  Object[] args = {variable, getStringOperator(opCode), result};
                  dh.printTrace(traceFlag, args);
               }
            }
            else
            {
               if ((opCode != OPCODE_AND) && (opCode != OPCODE_OR))
               {
                  value    = myStack.pop();
                  variable = myStack.pop();

                  result = makeComparable2(variable, value, opCode);

                  myStack.push(String.valueOf(result));

                  // trace the result
                  if (dh != null && dh.isTraceEnabled(traceFlag))
                  {
                     Object[] args = {variable, getStringOperator(opCode), value,
                        result};
                     dh.printTrace(traceFlag, args);
                  }
               }
               else /* if (opCode == OPCODE_AND || opCode == OPCODE_OR) */
               {
                  // only two booleans should be in the stack
                  String popStringOne = (String)myStack.pop();
                  String popStringTwo = (String)myStack.pop();

                  popBoolOne = Boolean.parseBoolean(popStringOne);
                  popBoolTwo = Boolean.parseBoolean(popStringTwo);

                  if (opCode == OPCODE_AND)
                     result = (popBoolOne && popBoolTwo);
                  else
                     result = (popBoolOne || popBoolTwo);

                  myStack.push(String.valueOf(result));

                  // trace the result
                  if (dh != null && dh.isTraceEnabled(traceFlag))
                  {
                     Object[] args = {popStringOne, getStringOperator(opCode),
                        popStringTwo, result};
                     dh.printTrace(traceFlag, args);
                  }

               }
            }
         }   // end of out-most else
      }     // end of for loop

      String retBoolean = (String)myStack.pop();
      return Boolean.parseBoolean(retBoolean);
   }
   
   /**
    * Utility method to return <code>true</code> if the supplied 
    * list is <code>null</code> or list does not contain any 
    * elements or all elements of list are <code>null<code> 
    * or empty strings, otherwise <code>false</code>.
    *
    * @param ls List object. 
    * @return <code>true</code> if the list is <code>null</code> or
    * list does not contain any elements or all elements of list are 
    * <code>null<code> or empty strings, otherwise <code>false</code>.
    */
   private boolean isListEmpty(List ls)
   {
      if(ls == null || ls.isEmpty())
         return true;
         
      Iterator iter = ls.iterator();
      while(iter.hasNext())
      {
         Object obj = iter.next();
         if(obj != null && obj.toString().length()>0 )
            return false;
      }
      
      return true;
   }
   /**
    * Process a row of data returned from one or more back-ends. The
    * data can be manipulated, or the entire row can be omitted from
    * the result set.
     * <P>
    * E2 uses the request information to execute queries against the
    * back-end(s). It then merges all results into a unified result
    * set (if more than one query was executed) and allows this evaluator
    * to process the data on a row by row basis. The evaluator can be used
    * to remove the row from the result set, or to modify the data
    * associated with the row.
    * <P>
    * Any changes made to the data will be made available to any other
    * evaluators and for all additional processing done by the application.
    * <P>
    * <em>NOTE:</em> Modifying rows in the result set will not effect
    * the back-end data store. The modifications will only be applied to
    * the result set being used to generate the resulting XML document.
    *
    * @param   data   The execution data, which contains the request. The
    * row, if needed, will be obtained by calling getCurrentResultRowData()
    * on the execution data object.
    *
    * @return                     <code>true</code> to keep the row
    *                              (including any changes which may have
    *                              been made); </code>false</code> to remove
    *                              the row from the result set which will
    *                              be used to generate the resulting XML
    *                              document
    */
   public boolean processRow(PSExecutionData data)
      throws PSParameterMismatchException
   {
      return isMatch(data);
   }

   /**
    * Set the result set structure which will be given to this evaluator.
    * The best use of this method is to store the index of columns this
    * evaluator will act upon as member variables. When processRow is called,
    * the column index can be used to quickly access the desired data.
    *
    * @param      meta            the meta data describing the result set
    *                              which will be processed.
    */
   public void setResultSetMetaData(ResultSetMetaData meta)
      throws SQLException
   {
      m_metaData = meta;
   }

   /**
    * Get the parameter definitions for this evaluator.
    *
    * @return                     an array of PSExtensionParamDef objects
    *                              describing the required parameters
    */
   public PSExtensionParamDef[] getParamDefs()
   {
      return null;
   }

   /**
    * Set the data extractors which can be used to retrieve the parameter
    * values at run-time. For each instance of the evaluator, a different set
    * of parameter values may be used.
    * <P>
    * IPSDataExtractors provide a transparent mechanism for accessing
    * data. The PSExecutionData object passed in to the evaluator's
    * execute method is used with the extractor to load the runtime value.
    *
    * @param   extractors         an array of IPSDataExtractor objects
    *                              defining the parameter values for this
    *                              instance
    */
   public void setParamValues(IPSDataExtractor[] extractors)
      throws PSParameterMismatchException
   {

   }

   /**
    * Gets the list of all back end column names
    *
    * @param   request
    *
    * @return   String[]
    */
   public String[] getBackEndColumnList(PSRequest request)
   {
      if (m_backEndColNames == null)
         return new String[0];
      return (String[])m_backEndColNames.toArray(new String[m_backEndColNames.size()]);
   }


     /**
       * Tokenize the specified conditionals.
       * <P>
       * The logic we want to follow here is that all terms ANDed together
       * are evaluated first. There results can then be checked with all
       * the ORs. For instance, when the following conditions exist:
       * <P>
       * <TABLE BORDER="1">
       *      <TR>
       *         <TH>Name</TH>
       *         <TH>Operator</TH>
       *         <TH>Value</TH>
       *         <TH>Boolean</TH>
       *      </TR>
       *      <TR>
       *         <TD>t1.c1</TD>
       *         <TD>&gt;</TD>
       *         <TD>0</TD>
       *         <TD>AND</TD>
       *      </TR>
       *      <TR>
       *         <TD>t1.c1</TD>
       *         <TD>&lt;</TD>
       *         <TD>100</TD>
       *         <TD>OR</TD>
       *      </TR>
       *      <TR>
       *         <TD>t1.c1</TD>
       *         <TD>=</TD>
       *         <TD>0</TD>
       *         <TD>AND</TD>
       *      </TR>
       *      <TR>
       *         <TD>t1.c2</TD>
       *         <TD>=</TD>
       *         <TD>X</TD>
       *         <TD></TD>
       *      </TR>
       * </TABLE>
       * <P>
       * In the above example, it is important that the first two conditions
       * be evaluated, then the last two conditions and finally the OR between
       * the two computed values. To accomplish this task, we will act as a
       * boolean prefix calculator. The following set of instructions will
       * be stored as the execution steps:
       * <P>
       * <TABLE BORDER="1">
       *      <TR>
       *         <TH>OP Code</TH>
       *         <TH>Value</TH>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSBackEndColumn (for t1.c1)</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSLiteral (for 0)</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_GREATERTHAN</TD>
       *         <TD>-none-</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSBackEndColumn (for t1.c1)</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSLiteral (for 100)</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LESSTHAN</TD>
       *         <TD>-none-</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_AND</TD>
       *         <TD>-none-</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSBackEndColumn (for t1.c1)</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSLiteral (for 0)</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_EQUAL</TD>
       *         <TD>-none-</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSBackEndColumn (for t1.c2)</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSLiteral (for X)</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_EQUAL</TD>
       *         <TD>-none-</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_AND</TD>
       *         <TD>-none-</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_OR</TD>
       *         <TD>-none-</TD>
       *      </TR>
       * </TABLE>
       * <P>
       * Let's assume we have t1.c1 = 0 and t1.c2 = Y. The following execution
       * would occur:
       * <P>
       * <TABLE BORDER="1">
       *      <TR>
       *         <TH>OP Code</TH>
       *         <TH>Value</TH>
       *         <TH>Stack</TH>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSBackEndColumn (for t1.c1)</TD>
       *         <TD>0</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSLiteral (for 0)</TD>
       *         <TD>0<BR>0</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_GREATERTHAN</TD>
       *         <TD>-none-</TD>
       *         <TD>false</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSBackEndColumn (for t1.c1)</TD>
       *         <TD>false<BR>0</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSLiteral (for 100)</TD>
       *         <TD>false<BR>0</BR><BR>100</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LESSTHAN</TD>
       *         <TD>-none-</TD>
       *         <TD>false<BR>true</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_AND</TD>
       *         <TD>-none-</TD>
       *         <TD>false</TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSBackEndColumn (for t1.c1)</TD>
       *         <TD>false<BR>0</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSLiteral (for 0)</TD>
       *         <TD>false<BR>0</BR><BR>0</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_EQUAL</TD>
       *         <TD>-none-</TD>
       *         <TD>false<BR>true</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSBackEndColumn (for t1.c2)</TD>
       *         <TD>false<BR>true</BR><BR>Y</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_LOAD</TD>
       *         <TD>PSLiteral (for X)</TD>
       *         <TD>false<BR>true</BR><BR>Y</BR><BR>X</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_EQUAL</TD>
       *         <TD>-none-</TD>
       *         <TD>false<BR>true</BR><BR>false</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_AND</TD>
       *         <TD>-none-</TD>
       *         <TD>false<BR>false</BR></TD>
       *      </TR>
       *      <TR>
       *         <TD>OPCODE_OR</TD>
       *         <TD>-none-</TD>
       *         <TD>false</TD>
       *      </TR>
       * </TABLE>
       *
       * @param   conds         the conditional collection
       */
   private void tokenize(PSCollection conds)
      throws PSEvaluationException
   {
      if ((conds == null) || (conds.size() == 0))
      {   // no conditions means always match!
         m_tokens = null;
         return;
      }

      if (m_backEndColNames == null)
         m_backEndColNames = new ArrayList();

      // verify we have the correct collection type
      if (!com.percussion.design.objectstore.PSConditional.class.isAssignableFrom(
         conds.getMemberClassType()))
      {
         throw new IllegalArgumentException("coll bad content type, Conditional Handler: " +
            conds.getMemberClassName());
      }

      /* tokenize the conditionals for fast execution
       * (create appropriate PSConditionalToken objects)
       */
      m_tokens = new java.util.ArrayList();

      PSConditionalToken condToken;
      PSConditionalToken condTokenAND = new PSConditionalToken(OPCODE_AND);
      PSConditionalToken condTokenOR  = new PSConditionalToken(OPCODE_OR);

      PSConditional cur;  // with attributes: varible_name, operator, value, boolean
      String opBool = null;
      int preBoolType = 0;
      int orCount = 0;
      int intOp = 0;
      for (int i = 0; i < conds.size(); i++)
      {
         cur = (PSConditional)conds.get(i);

         IPSReplacementValue condVar = cur.getVariable();
         condToken = new PSConditionalToken(condVar);  //var_name attribute
         if (condVar instanceof PSBackEndColumn)
         {
            m_backEndColNames.add(((PSBackEndColumn)condVar).getColumn());
         }
         m_tokens.add(condToken);

         intOp = intOpBool(cur.getOperator());
         if (!cur.isUnary())
         {
            if (cur.getValue() == null)
            {
               String arg = "null";
               throw new PSEvaluationException(
                  IPSDataErrors.UNKNOWN_OPCODE_LOAD_TYPE, arg);
            }

            condToken = new PSConditionalToken(cur.getValue());  //value attribute

            m_tokens.add(condToken);
         }

         condToken = new PSConditionalToken(intOp);
         m_tokens.add(condToken);

         opBool = cur.getBoolean();

         if (i == 0)
         {
            preBoolType = intOpBool(opBool);
            continue;
         }

         if (preBoolType == OPCODE_AND)
         {
            m_tokens.add(condTokenAND);
            preBoolType = intOpBool(opBool);
         }
         else if (preBoolType == OPCODE_OR)
         {
            if (opBool.length() == 0)   // reach the last one
               m_tokens.add(condTokenOR);
            else
            { // opBool.equals(cur.OPBOOL_OR) || opBool.equals(cur.OPBOOL_AND)
               orCount += 1;
               preBoolType = intOpBool(opBool);
            }
         }
      }

      for (int i = 0; i < orCount; i++)
      {
         m_tokens.add(condTokenOR);
      }
   }

   /**
    * Get the integer number corresponding to the operator defined in PSConditional.
    *
    * @param   opType    an operator
    *
    * @return             an integer representing the operator
    */
   private int intOpBool(String opType)
   {
      if (opType.equalsIgnoreCase(PSConditional.OPTYPE_EQUALS))
         return OPCODE_EQUALS;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_NOTEQUALS))
         return OPCODE_NOTEQUALS;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_LESSTHAN))
         return OPCODE_LESSTHAN;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_LESSTHANOREQUALS))
         return OPCODE_LESSTHANOREQUALS;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_GREATERTHAN))
         return OPCODE_GREATERTHAN;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_GREATERTHANOREQUALS))
         return OPCODE_GREATERTHANOREQUALS;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_ISNULL))
         return OPCODE_ISNULL;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_ISNOTNULL))
         return OPCODE_ISNOTNULL;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_BETWEEN))
         return OPCODE_BETWEEN;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_NOTBETWEEN))
         return OPCODE_NOTBETWEEN;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_IN))
         return OPCODE_IN;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_NOTIN))
         return OPCODE_NOTIN;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_LIKE))
         return OPCODE_LIKE;
      else if (opType.equalsIgnoreCase(PSConditional.OPTYPE_NOTLIKE))
         return OPCODE_NOTLIKE;
      else if (opType.equalsIgnoreCase(PSConditional.OPBOOL_AND))
         return OPCODE_AND;
      else if (opType.equalsIgnoreCase(PSConditional.OPBOOL_OR))
         return OPCODE_OR;
      else
         throw new IllegalArgumentException("Illegal op type: " + opType);

   }

   /**
    * Compare two numbers according to the given operator.
    *
    * @param   op      operator represented by an integer
    * @param   left     the number in the left-hand side of the operator
    * @param   right   the number in the right-hand side of the operator
    *
    * @return   <code>true</code> if the conditional criteria is met,
    *          <code>false</code> otherwise
    */
   private static boolean compareNumber(int op, double left, double right)
      throws PSEvaluationException
   {
      switch(op)
      {
      case OPCODE_EQUALS:
         return (left == right);
      case OPCODE_NOTEQUALS:
         return (left != right);
      case OPCODE_LESSTHAN:
         return (left < right);
      case OPCODE_LESSTHANOREQUALS:
         return (left <= right);
      case OPCODE_GREATERTHAN:
         return (left > right);
      case OPCODE_GREATERTHANOREQUALS:
         return (left >= right);
      case OPCODE_IN:
      case OPCODE_LIKE:
         return (left == right);
      case OPCODE_NOTIN:
      case OPCODE_NOTLIKE:
         return (left != right);
      default:
         Object[] args = {
            String.valueOf(left), getStringOperator(op), String.valueOf(right) };
         throw new PSEvaluationException(
            IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }
   }

   private static boolean compare(int op, Comparable left, Comparable right)
      throws PSEvaluationException
   {
      int ret = left.compareTo(right);

      switch(op)
      {
      case OPCODE_EQUALS:
         return (ret == 0);
      case OPCODE_NOTEQUALS:
         return (ret != 0);
      case OPCODE_LESSTHAN:
         return (ret < 0);
      case OPCODE_LESSTHANOREQUALS:
         return (ret <= 0);
      case OPCODE_GREATERTHAN:
         return (ret > 0);
      case OPCODE_GREATERTHANOREQUALS:
         return (ret >= 0);
      case OPCODE_IN:
      case OPCODE_LIKE:
         return (ret == 0);
      case OPCODE_NOTIN:
      case OPCODE_NOTLIKE:
         return (ret != 0);
      default:
         Object[] args = {
            String.valueOf(left), getStringOperator(op), String.valueOf(right) };
         throw new PSEvaluationException(
            IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }
   }

   /**
    * Compare two null values according to the given operator.
    *
    * @param   op      operator represented by an integer
    *
    * @return   <code>true</code> if the conditional criteria is met,
    *          <code>false</code> otherwise
    */
   private static boolean compareNulls(int op)
   {
      // they must both be null to be here
      switch(op)
      {
         case OPCODE_EQUALS:
         case OPCODE_LESSTHANOREQUALS:
         case OPCODE_GREATERTHANOREQUALS:
         case OPCODE_LIKE:
         case OPCODE_IN:
         case OPCODE_BETWEEN:
            return true;
      }

      return false;
   }

   /**
    * Compare two strings according to the given operator.
    *
    * @param   op            operator represented by an integer
    *
    * @param   leftString   the left string to compare
    *
    * @param   rightString   the right string to compare
    *
    * @return   <code>true</code> if the conditional criteria is met,
    *          <code>false</code> otherwise
    */
   private static boolean compareStrings(
      int op, String leftString, String rightString)
      throws PSEvaluationException
   {
      // they must both be null to be here
      switch(op)
      {
         case OPCODE_EQUALS:
            return leftString.equals(rightString);

         case OPCODE_NOTEQUALS:
            return !leftString.equals(rightString);

         case OPCODE_LESSTHAN:
            return (leftString.compareTo(rightString) < 0);

         case OPCODE_LESSTHANOREQUALS:
            return (leftString.compareTo(rightString) <= 0);

         case OPCODE_GREATERTHAN:
            return (leftString.compareTo(rightString) > 0);

         case OPCODE_GREATERTHANOREQUALS:
            return (leftString.compareTo(rightString) >= 0);

         case OPCODE_IN:
         case OPCODE_NOTIN:
         case OPCODE_LIKE:
         case OPCODE_NOTLIKE:
            PSPatternMatcher patt = PSPatternMatcher.SQLPatternMatcher(rightString);
            boolean result = patt.doesMatchPattern(leftString);
            if (op == OPCODE_NOTLIKE)
            {
               result = !result;
            }
            return result;

         default:
            Object[] args = {leftString, getStringOperator(op), rightString};
            throw new PSEvaluationException(
               IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }  //end of switch
   }


   /**
    * Find out whether a number is within a certain range. The range can be
    * either lowBound and upBound or upBound and lowBound.
    * <P>
    * Example:
    * <UL>
    * <LI>It is true that 123 is between 100 and 150
    * <LI>It is true that 123 is between 150 and 100
    * </UL>
    *
    * @param   mid   the number whose inclusion in the range is to be tested
    * @param   lo    the lower bound of the range, inclusive
    * @param   hi    the upper bound of the range, inclusive
    *
    * @return   <code>true</code> if the conditional criteria is met,
    *          <code>false</code> otherwise
    */
   private static boolean isBetween(double mid, double lo, double hi)
   {
      if (hi < lo)
      {
         double swap = hi;
         hi = lo;
         lo = swap;
      }

      return ((lo <= mid) && (mid <= hi));
   }

   /**
    * Get an operator in String type based on its integer typed counterpart.
    *
    * @param    op     an operator represented by an integer
    * @return           an operator in String type
    */
   private static String getStringOperator(int op)
   {
      String stringOp = "UNKNOWN OPERATOR";
      switch(op)
      {
      case OPCODE_EQUALS:
         stringOp = "=";
         break;
      case OPCODE_NOTEQUALS:
         stringOp = "<>";
         break;
      case OPCODE_LESSTHAN:
         stringOp = "<";
         break;
      case OPCODE_LESSTHANOREQUALS:
         stringOp = "<=";
         break;
      case OPCODE_GREATERTHAN:
         stringOp = ">";
         break;
      case OPCODE_GREATERTHANOREQUALS:
         stringOp = ">=";
         break;
      case OPCODE_ISNULL:
         stringOp = "IS NULL";
         break;
      case OPCODE_ISNOTNULL:
         stringOp = "IS NOT NULL";
         break;
      case OPCODE_BETWEEN:
         stringOp = "BETWEEN";
         break;
      case OPCODE_NOTBETWEEN:
         stringOp = "NOT BETWEEN";
         break;
      case OPCODE_IN:
         stringOp = "IN";
         break;
      case OPCODE_NOTIN:
         stringOp = "NOT IN";
         break;
      case OPCODE_LIKE:
         stringOp = "LIKE";
         break;
      case OPCODE_NOTLIKE:
         stringOp = "NOT LIKE";
         break;
      case OPCODE_AND:
         stringOp = "AND";
         break;
      case OPCODE_OR:
         stringOp = "OR";
         break;
      }

      return stringOp;
   }

   class PSConditionalToken
   {
      /**
       * Construct a token which will load the specified replacement value
       * onto the execution stack. This sets the op code to OPCODE_LOAD.
       */
      PSConditionalToken(IPSReplacementValue value)
      {
         super();

         if (value != null){
            m_op = OPCODE_LOAD;
         }
         else{
            throw new IllegalArgumentException("null replacement value, PSConditionalToken");
         }

         m_rtValue = PSDataExtractorFactory.createReplacementValueExtractor(value);
      }

      /**
       * The op code to run. OPCODE_LOAD should not be used in this
       * constructor! Use validateOperatorCode(int) first.
       */
      PSConditionalToken(int op)
      {
         super();
         m_op         = op;
         m_rtValue   = null;
      }

      int m_op;
      IPSDataExtractor m_rtValue;
   } // end class PSConditionalToken


   /**
    * Compares a list of values on the left of the operand with a list of values
    * on the right. To compare single objects uses {@link #makeComparable2Obj()}.
    *
    * @param leftList list of objects, expected never <code>null</code>, may be
    * <code>empty</code>.
    * @param rightList list of objects, expected never <code>null</code>, may
    * <code>empty</code>.
    * @param opCode the operator to be used, expected to be valid.
    *
    * @return <code>true</code> if objects are the same, <code>false</code>
    * otherwise.
    * @exception   PSEvaluationException if one or both of the data types or
    * the operand or a combination of all of the above is not acceptable.
    */
   private static boolean makeComparable2Lists(List leftList, List rightList,
      int opCode) throws PSEvaluationException
   {
      int  leftListSize = leftList.size();
      Iterator itLeft = leftList.iterator();

      int  rightListSize = rightList.size();
      Iterator itRight = rightList.iterator();

      switch(opCode)
      {
         case OPCODE_EQUALS:
         {
            if (leftList.isEmpty() && rightList.isEmpty())
               return true;

            if (leftListSize != rightListSize)
               return false; //they can't be equal

            //compare one to one
            while (itLeft.hasNext() && itRight.hasNext())
               if (!makeComparable2Obj(itLeft.next(), itRight.next(), opCode))
                  return false; //at least one is not the same

            //none are different - all are the same
            return true;
         }
         case OPCODE_NOTEQUALS:
         {
            if (leftList.isEmpty() && rightList.isEmpty())
               return false; //empty is equal to empty

            if (leftListSize != rightListSize)
               return true;  //one has more items then the other - not equal

            //compare one to one
            while (itLeft.hasNext() && itRight.hasNext())
               if (!makeComparable2Obj(itLeft.next(), itRight.next(), opCode))
                  return true; //at least one is not the same

            //all are the same
            return false;
         }
         case OPCODE_LESSTHAN:
         case OPCODE_LESSTHANOREQUALS:
         case OPCODE_GREATERTHAN:
         case OPCODE_GREATERTHANOREQUALS:
         {
            if ((leftList.isEmpty() || rightList.isEmpty()) ||
                (leftListSize > 1 && rightListSize > 1))
            {
               //empty collections or both collections with more then one
               //item are considered illegal for any of these operands.
               Object[] args = { leftList.getClass().getName(), ""+opCode,
                rightList.getClass().getName()};

               throw new PSEvaluationException(
                  IPSDataErrors.WRONG_DATA_COMPARISON, args);
            }

            //compare one by one joining individual results by AND operand
            while (itLeft.hasNext())
            {
               Object leftObj = itLeft.next();

               itRight = rightList.iterator();

               while (itRight.hasNext())
               {
                  if (!makeComparable2Obj(leftObj, itRight.next(), opCode))
                     return false;
               }
            }

            return true;
         }
         case OPCODE_ISNULL:
         {
            return leftList.isEmpty() || itLeft.next()==null;
         }
         case OPCODE_ISNOTNULL:
         {
            return !leftList.isEmpty() && itLeft.next()!=null;
         }
         case OPCODE_BETWEEN:
         case OPCODE_NOTBETWEEN:
         {
            if (leftListSize == 1 && rightListSize==2)
            {
               return makeComparable2Obj(itLeft.next(), rightList, opCode);
            }
            else
            {
               Object[] args = { leftList.getClass().getName(), ""+opCode,
                rightList.getClass().getName()};

               throw new PSEvaluationException(
                  IPSDataErrors.WRONG_DATA_COMPARISON, args);
            }
         }
         case OPCODE_IN:
         {
            if (leftList.isEmpty() && rightList.isEmpty())
               return true; //empty is IN empty

            while (itLeft.hasNext())
            {
               if(!makeComparable2Obj(itLeft.next(), rightList, opCode))
                  return false; //one from the left is not IN the right
            }

            return true; //all from the left are IN the right
         }
         case OPCODE_NOTIN:
         {
            if (leftList.isEmpty() && rightList.isEmpty())
               return false; //empty is IN empty

            while (itLeft.hasNext())
            {
               if (!makeComparable2Obj(itLeft.next(), rightList, opCode))
                  return false; //at least one is IN
            }

            return true; //none from the left are in the right
         }
         case OPCODE_LIKE:
         {
            if (leftList.isEmpty() && rightList.isEmpty())
               return true; //both are empty - they are LIKE

            while (itLeft.hasNext())
            {
               Object leftObj = itLeft.next();

               itRight = rightList.iterator();

               while(itRight.hasNext())
                  if (makeComparable2Obj(leftObj, itRight.next(), opCode))
                     return true; //one is LIKE - left is LIKE right
            }

            return false; //none are like
         }
         case OPCODE_NOTLIKE:
         {
            if (leftList.isEmpty() && rightList.isEmpty())
               return false; //both are empty - empty is LIKE empty

            while (itLeft.hasNext())
            {
               Object leftObj = itLeft.next();

               itRight = rightList.iterator();

               while(itRight.hasNext())
                  if (!makeComparable2Obj(leftObj, itRight.next(), opCode))
                     return false; //one is LIKE - left is LIKE right
            }

            return true; //none are like
         }
         default:
            throw new PSEvaluationException(IPSDataErrors.WRONG_OPERATOR_USAGE);
         }
   }

   /**
    * Makes two objects comparable and process the comparison. Currently the
    * supported left/right data types are null, PSDateLiteral, Date,
    * PSNumericLiteral, Number, PSTextLiteral, String, PSLiteralSet or
    * a List of one of the above types.
    *
    * If single objects are supplied, converts them to a List with one item
    * and calls {@link #makeComparable2Lists(List, List, int)} method; which
    * depending on the operand either iterates through the lists evaluating
    * objects one by one or converts list to PSLiteralSet, then delegates
    * actual operand evaluation to the legacy logic that is factored into the
    * {@link #makeComparable2Obj(Object, Object, int)} method.
    *
    * note: even though this method can now handle List(s) the name is preserved
    * for backwards compatibility.
    *
    * For OPCODE_BETWEEN || OPCODE_NOTBETWEEN || OPCODE_IN || OPCODE_NOTIN
    * the right object is expected to be of type (List) or (PSLiteralSet) or
    * (PSLiteral or a String object with text delimited by commas).
    * For OPCODE_BETWEEN and OPCODE_NOTBETWEEN there is a restriction that
    * requires exactly 1 item on the left and 2 items on the right.
    *
    * <p>
    * A special case is when one object is Date while the other object is a
    * String or a set of PSTextLiterals, namely, all element of this
    * PSLiteralSet is of type PSTextLiteral. Under this situation, every text
    * literal must be in certain date format pattern in order to let the
    * comparison work. Otherwise, an exception will be thrown.
    * Our default date format pattern is "yyyy.MM.dd".
    *
    * Here are some working examples:
    *
    * (1) "1999.08.12";
    * (2) "1999.08.12 AD";
    * (3) "1999.08.12 AD at 14:04:24";
    * (4) "1999.08.12 at 01:01:01 PDT"
    *
    * @param   left    the left object, may be <code>null</code>.
    * @param   right  the right object, may be <code>null</code>.
    * @param   opCode the operator to be used.
    *
    * @return <code>true</code> if objects are the same, <code>false</code>
    * otherwise.
    * @exception   PSEvaluationException if one or both of the data types or
    * the operand or a combination of all of the above is not acceptable.
    */
   public static boolean makeComparable2(Object left, Object right, int opCode)
      throws PSEvaluationException
   {
      boolean isValidOperator = validateOperatorCode(opCode);
      if (isValidOperator == false){
         throw new PSEvaluationException(IPSDataErrors.WRONG_OPERATOR_USAGE);
      }

      if ((left != null) && (right == null)){
         Object[] args = { left.getClass().getName(), "NULL" };
         throw new PSEvaluationException(IPSDataErrors.RVALUE_INVALID_TYPE, args);
      }

      if ((left == null) && (right != null)){
         Object[] args = { "NULL", right.getClass().getName() };
         throw new PSEvaluationException(IPSDataErrors.LVALUE_INVALID_TYPE, args);
      }

      if ((left == null) && (right == null))
         return compareNulls(opCode);

      List leftList = null;
      List rightList = null;

      if ((left instanceof List) && (right instanceof List))
      {
         leftList = (List)left;
         rightList = (List)right;
      }
      else if (left instanceof List)
      {
         leftList = (List)left;
         rightList = new ArrayList();
         rightList.add(right);
      }
      else if (right instanceof List)
      {
         leftList = new ArrayList();
         leftList.add(left);
         rightList = (List)right;
      }
      else
      {
         //must be two singular objects
         return makeComparable2Obj(left, right, opCode);
      }

      return makeComparable2Lists(leftList, rightList, opCode);
   }

   /**
    * Make two objects comparable and process the comparison. Currently the
    * supported left/right data types are null, PSDateLiteral, Date,
    * PSNumericLiteral, Number, PSTextLiteral, String, and PSLiteralSet.
    * For OPCODE_BETWEEN || OPCODE_NOTBETWEEN || OPCODE_IN || OPCODE_NOTIN
    * the right object is expected to be of type List or PSLiteralSet.
    *
    * <p>
    * A special case is when one object is Date while the other object is a
    * String or a set of PSTextLiterals, namely, all element of this PSLiteralSet
    * is of type PSTextLiteral. Under this situation, every text literal must be
    * in certain date format pattern in order to let the comparison work.
    * Otherwise, an exception will be thrown.
    * Our default date format pattern is "yyyy.MM.dd".
    * Here are some working examples:
    *
    * (1) "1999.08.12";
    * (2) "1999.08.12 AD";
    * (3) "1999.08.12 AD at 14:04:24";
    * (4) "1999.08.12 at 01:01:01 PDT"
    *
    * @param   left      the left object, expected never <code>null</code>.
    * @param   right    the right object, expected never <code>null</code>.
    * @param   opCode   the operator to be used, expected to be valid.
    *
    * @return <code>true</code> if objects are the same, <code>false</code>
    * otherwise.
    * @exception   PSEvaluationException if data type or operator or
    * a combination of those is unacceptable.
    */
   private static boolean makeComparable2Obj(Object left, Object right,
      int opCode)   throws PSEvaluationException
   {
      boolean result = false;

      int leftType = PSDataConverter.getDataType(left);
      int rightType = PSDataConverter.getDataType(right);

      /* Bug Id: Rx-99-10-0122 recognize null value on left side,
         return false except in the case where the opcode is
         OPCODE_ISNULL as per SQL92 standard which defines that all
         cases, even = and <> should return false when the left side
         is NULL */
      if (opCode == OPCODE_ISNULL || opCode == OPCODE_ISNOTNULL)
      {
         if ((leftType == PSDataConverter.DATATYPE_NULLTEXT) ||
            (leftType == PSDataConverter.DATATYPE_NULL))
         {
            //null or empty string on the left - true for ISNULL false otherwise
            return (opCode == OPCODE_ISNULL);
         }
         else
         {
            //not null on the left - reverse of the above case.
            return (opCode != OPCODE_ISNULL);
         }
      }

      /* this method doesn't support comparing sets with sets, must use
         {@link @makeComparable2Lists(List, List, int)} instead.
      */
      if (((leftType & PSDataConverter.DATATYPE_SET_FLAG) != 0) &&
         ((rightType & PSDataConverter.DATATYPE_SET_FLAG) != 0))
      {
         Object[] args = { left.getClass().getName(),
            right.getClass().getName() };
         throw new PSEvaluationException(
            IPSDataErrors.TYPE_COMPARISON_UNSUPPORTED, args);
      }

      /* this method doesn't compare left side of type SET, must use
         {@link @makeComparable2Lists(List, List, int)} instead.
      */
      if ((leftType & PSDataConverter.DATATYPE_SET_FLAG) != 0)
      {
         Object[] args = { left.getClass().getName() };
         throw new PSEvaluationException(
            IPSDataErrors.LVALUE_INVALID_TYPE, args);
      }

      /*
         If opCode is of type OPCODE_IN || OPCODE_NOTIN || OPCODE_BETWEEN ||
         OPCODE_NOTBETWEEN and the object is not of type PSLiteralSet then
         make an attempt to convert into PSLiteralSet.
      */
      if( (opCode == OPCODE_IN) || (opCode == OPCODE_NOTIN) ||
          (opCode == OPCODE_BETWEEN) || (opCode == OPCODE_NOTBETWEEN) )
      {
         try
         {
            //attempt to convert into a PSLiteralSet
            right = PSDataConverter.convertToSet(right);
            rightType = PSDataConverter.getDataType(right);
         }
         catch (Exception e)
         {
            Object[] args = { PSDataConverter.getTypeString(rightType),
            getStringOperator(opCode) };
            throw new PSEvaluationException(
               IPSDataErrors.OPERATOR_INVALID_FOR_TYPE, args);
         }
      }

      /*
       * make sure that on the right of the BETWEEN operand we have a set
       * of exactly two items.
       */
      if ((opCode == OPCODE_BETWEEN) || (opCode == OPCODE_NOTBETWEEN))
      {
         if ( ((rightType & PSDataConverter.DATATYPE_SET_FLAG)==0) ||
              ((List)right).size() != 2)
         {
            Object[] args = { PSDataConverter.getTypeString(rightType),
            getStringOperator(opCode) };
            throw new PSEvaluationException(
               IPSDataErrors.OPERATOR_INVALID_FOR_TYPE, args);
         }
      }

      /* Get the best type for comparison */
      int bestType = PSDataConverter.getBestComparisonType(leftType, rightType);

      // this is important to handle conversion from string to date
      java.text.SimpleDateFormat dateFormat = null;

      if (left instanceof PSDateLiteral)
         dateFormat = ((PSDateLiteral)left).getDateFormat();
      else if (right instanceof PSDateLiteral)
         dateFormat = ((PSDateLiteral)right).getDateFormat();
      else if (right instanceof PSLiteralSet)
         dateFormat = getDateFormat((PSLiteralSet)right);

      /*
       if appropriate attempt to convert left and right to the same data type
      */

      try
      {
         // PSLiteralSet type will remain the same, no conversion is made
         left = PSDataConverter.convert(left, bestType, dateFormat);
      }
      catch (Exception e)
      {
         bestType = leftType;
      }

      try
      {
         // PSLiteralSet type will remain the same, no conversion is made
         right = PSDataConverter.convert(right, bestType, dateFormat);
      }
      catch (Exception e)
      {
         Object[] args = { PSDataConverter.getTypeString(rightType),
         PSDataConverter.getTypeString(bestType), right};

         throw new PSEvaluationException(
            IPSDataErrors.UNSUPPORTED_CONVERSION, args);
      }

      switch (bestType)
      {
         case PSDataConverter.DATATYPE_NUMERIC:
         case PSDataConverter.DATATYPE_DOUBLE:
         case PSDataConverter.DATATYPE_LONG:
         case PSDataConverter.DATATYPE_INT:

            if (right instanceof PSLiteralSet)
            {
               result = compareWithNumericSet(opCode, left, right, dateFormat);
            }
            else
            {
               //based on the type must be one of the Comparable classes
               Comparable leftComp = (Comparable) left;
               Comparable rightComp = (Comparable) right;

               result = compare(opCode, leftComp, rightComp);
            }
            break;

         case PSDataConverter.DATATYPE_DATE:

            if (right instanceof PSLiteralSet)
            {
               result = compareWithNumericSet(opCode, left, right, dateFormat);
            }
            else
            {
               Date leftDate = (Date)left;
               Date rightDate = (Date)right;

               try
               {
                  result = compareNumber(opCode, leftDate.getTime(),
                     rightDate.getTime());
               }
               catch (PSEvaluationException e)
               {
                  // that message is not as clear as this one
                  // (shows the numbers rather than times)
                  Object[] args = {   leftDate.toString(),
                     getStringOperator(opCode), rightDate.toString() };
                  throw new PSEvaluationException(
                     IPSDataErrors.WRONG_OPERATOR_USAGE, args);
               }
            }
            break;

         case PSDataConverter.DATATYPE_TEXT:

            if (right instanceof PSLiteralSet)
            {
               result = compareWithTextSet(opCode, left, right);
            }
            else
            {
               String leftString = (String)left;
               String rightString = (String)right;
               result = compareStrings(opCode, leftString, rightString);
            }
            break;

         case PSDataConverter.DATATYPE_NULL:

            result = compareNulls(opCode);
            break;

         // case PSDataConverter.DATATYPE_BINARY:
         // case PSDataConverter.DATATYPE_UNKNOWN:
         // case PSDataConverter.DATATYPE_NUMERICSET:
         // case PSDataConverter.DATATYPE_DATESET:
         // case PSDataConverter.DATATYPE_TEXTSET:
         // case PSDataConverter.DATATYPE_BINARYSET:
         default:
            Object[] args = { left.getClass().getName(),
               right.getClass().getName() };
            throw new PSEvaluationException(
               IPSDataErrors.TYPE_COMPARISON_UNSUPPORTED, args);
      }

      return result;
   }

   /**
    * Determine whether the operator code is valid or not.
    */
   private static boolean validateOperatorCode(int opCode)
   {
      switch(opCode)
      {
      case OPCODE_EQUALS:
      case OPCODE_NOTEQUALS:
      case OPCODE_LESSTHAN:
      case OPCODE_LESSTHANOREQUALS:
      case OPCODE_GREATERTHAN:
      case OPCODE_GREATERTHANOREQUALS:
      case OPCODE_ISNULL:
      case OPCODE_ISNOTNULL:
      case OPCODE_BETWEEN:
      case OPCODE_NOTBETWEEN:
      case OPCODE_IN:
      case OPCODE_NOTIN:
      case OPCODE_LIKE:
      case OPCODE_NOTLIKE:
      case OPCODE_AND:
      case OPCODE_OR:
         return true;
      }

      return false;
   }

   private static java.text.SimpleDateFormat getDateFormat(PSLiteralSet literalSet)
   {
      java.text.SimpleDateFormat dateFormat = null;

      if (literalSet == null)
         return dateFormat;

      if ((literalSet.get(0)) instanceof PSDateLiteral)
         dateFormat = ((PSDateLiteral)(literalSet.get(0))).getDateFormat();

      return dateFormat;
   }

   /**
    * Determine whether a number or date is in a set of number or date, respectively.
    *
    * @param   opCode   an integer stands for BETWEEN, NOTBETWEEN, IN, or NOT IN
    * @param   left      an object of BigDecimal, or Date, or String
    * @param   right      an object of type PSLiteralSet, with elements being
    *                     PSNumericLiteral, or PSDateLiteral, or PSTextLiteral
    * @param   dateFormat   a given date format to parse a string into a date
    *
    * @exception   PSEvaluationException   if a wrong operator or data type is used
    */
   private static boolean compareWithNumericSet(int opCode, Object left, Object right,
                                                java.text.SimpleDateFormat dateFormat)
      throws PSEvaluationException
   {
      String leftName = left.getClass().getName();
      String rightName=right.getClass().getName();

      if ((opCode != OPCODE_BETWEEN) && (opCode != OPCODE_NOTBETWEEN) &&
          (opCode != OPCODE_IN) && (opCode != OPCODE_NOTIN))
      {
         Object[] args = { leftName, getStringOperator(opCode), rightName };
         throw new PSEvaluationException(IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }

      // set elements could be PSNumericLiteral or PSDateLiteral
      PSLiteralSet numSet = (PSLiteralSet)right;

      int setSize = numSet.size();

      if (setSize < 1)
      {
         Object[] args = { leftName, getStringOperator(opCode), rightName };
         throw new PSEvaluationException(IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }

      boolean convertTextLiteralToDate = false;

      if (dateFormat != null)
      {
         PSLiteral lit = (PSLiteral) ((PSLiteralSet) right).get(0);
         if ((lit instanceof PSDateLiteral) && (left instanceof String))
         {
            try {
               left = dateFormat.parse((String) left);
            } catch (Exception ignore)
            {
               // If this fails, let it fall through to the data converter
               // to attempt the comparison.
            }
         } else if ((lit instanceof PSTextLiteral) &&
            ((left instanceof Date) || (left instanceof PSDateLiteral)))
         {
            convertTextLiteralToDate = true;
         }

      }

      if ( ((opCode == OPCODE_BETWEEN) && (setSize != 2)) ||
         ((opCode == OPCODE_NOTBETWEEN) && (setSize!= 2)) )
      {
         Object[] args = { leftName, getStringOperator(opCode), rightName };
         throw new PSEvaluationException(IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }

      if ((opCode == OPCODE_IN) || (opCode == OPCODE_NOTIN))
      {
         for (int i = 0; i < numSet.size(); i++)
         {
            try {
               PSLiteral lit = (PSLiteral) numSet.get(i);
               if (convertTextLiteralToDate) {
                  Object obj;
                  obj = lit;
                  String str = ((PSTextLiteral)lit).getText();
                  try {
                     Date d = dateFormat.parse(str);
                     obj = d;
                  } catch (Exception ignore) {
                     // If this fails, let it fall through to the data converter
                     // to attempt the comparison.
                  }
                  if (PSDataConverter.compare(left, obj) == 0)
                     return (opCode == OPCODE_IN);

               } else {
                  if (PSDataConverter.compare(left, numSet.get(i)) == 0)
                     return (opCode == OPCODE_IN);
               }
            } catch (IllegalArgumentException e) {
throw new PSEvaluationException(0, e.getLocalizedMessage());            }

         }
         return (opCode == OPCODE_NOTIN);
      }

      //  It's a between
      int return1, return2;
      Object obj1, obj2;
      obj1 = numSet.get(0);
      obj2 = numSet.get(1);

      if (convertTextLiteralToDate)
      {
          try {
             Date d = dateFormat.parse(((PSTextLiteral) obj1).getText());
             obj1 = d;
          } catch (Exception ignore) {
             // If this fails, let it fall through to the data converter
             // to attempt the comparison.
          }
          try {
             Date d = dateFormat.parse(((PSTextLiteral) obj2).getText());
             obj2 = d;
          } catch (Exception ignore) {
             // If this fails, let it fall through to the data converter
             // to attempt the comparison.
          }
      }

      try {
         return1 = PSDataConverter.compare(left, obj1);
         return2 = PSDataConverter.compare(left, obj2);
      } catch (IllegalArgumentException e) {
         throw new PSEvaluationException(0, e.getLocalizedMessage());
      }

      if ( ((return1 < 0) && (return2 > 0)) ||
           ((return1 > 0) && (return2 < 0)) ||
           (return1 == 0) || (return2 == 0) )
      {
         return (opCode == OPCODE_BETWEEN);
      } else
      {
         return (opCode == OPCODE_NOTBETWEEN);
      }
   }

   /**
    * Determine whether a string is in a set of string or not.
    *
    * @param   opCode   an integer stands for IN or NOT IN
    * @param   left      an object of type string
    * @param   right      an object of type PSLiteralSet
    *
    * @exception   PSEvaluationException   if a wrong operator is used
    */
   private static boolean compareWithTextSet(int opCode, Object left, Object right)
      throws PSEvaluationException
   {
      String leftName = left.getClass().getName();
      String rightName=right.getClass().getName();

      if ((opCode != OPCODE_IN) && (opCode != OPCODE_NOTIN)){
         Object[] args = { leftName, getStringOperator(opCode), rightName };
         throw new PSEvaluationException(IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }

      // elements in the set could be PSNumericLiteral, PSDateLiteral, or PSTextLiteral
      PSLiteralSet numSet = (PSLiteralSet)right;

      int setSize = numSet.size();

      if (setSize < 1){
         Object[] args = { leftName, getStringOperator(opCode), rightName };
         throw new PSEvaluationException(IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }

      boolean isNumericLiteralSet = false;
      boolean isDateLiteralSet = false;
      boolean isTextLiteralSet = false;

      if ((numSet.get(0)) instanceof PSTextLiteral)
         isTextLiteralSet = true;
      else if ((numSet.get(0)) instanceof PSNumericLiteral)
         isNumericLiteralSet = true;
      else if ((numSet.get(0)) instanceof PSDateLiteral)
         isDateLiteralSet = true;
      else
         throw new PSEvaluationException(IPSDataErrors.WRONG_DATA_COMPARISON);


      String leftString = (String)left;

      boolean result;
      switch(opCode)
      {
      case OPCODE_IN:
      case OPCODE_NOTIN:
         result = false;
         String rightString = "";
         for (int i = 0; i < setSize; i++){
            if (isTextLiteralSet == true)
               rightString = ((PSTextLiteral)(numSet.get(i))).getText();
            else if (isNumericLiteralSet == true)
               rightString = (((PSNumericLiteral)(numSet.get(i))).getNumber()).toString();
            else if (isDateLiteralSet == true)
               rightString = (((PSDateLiteral)(numSet.get(i))).getDate()).toString();

            if (leftString.equals(rightString)){
               result = true;
               break;
            }
         }
         if (opCode == OPCODE_NOTIN)
            result = !result;

         break;
      default:
         Object[] args = { leftName, getStringOperator(opCode), rightName };
         throw new PSEvaluationException(IPSDataErrors.WRONG_OPERATOR_USAGE, args);
      }  // end of switch

      return result;
   }

   // this is where we store our PSConditionalToken objects
   private List m_tokens = null;
   private List<String> m_backEndColNames = null;
   private ResultSetMetaData m_metaData = null;

   // load the specified variable and PUSH it on the execution stack
   public static final int OPCODE_EQUALS               = 1;
   public static final int OPCODE_NOTEQUALS            = 2;
   public static final int OPCODE_LESSTHAN             = 3;
   public static final int OPCODE_LESSTHANOREQUALS     = 4;
   public static final int OPCODE_GREATERTHAN          = 5;
   public static final int OPCODE_GREATERTHANOREQUALS  = 6;
   public static final int OPCODE_ISNULL               = 7;
   public static final int OPCODE_ISNOTNULL            = 8;
   public static final int OPCODE_BETWEEN              = 9;
   public static final int OPCODE_NOTBETWEEN           = 10;
   public static final int OPCODE_IN                   = 11;
   public static final int OPCODE_NOTIN                = 12;
   public static final int OPCODE_LIKE                 = 13;
   public static final int OPCODE_NOTLIKE              = 14;
   public static final int OPCODE_AND                  = 15;
   public static final int OPCODE_OR                   = 16;

   public static final int OPCODE_LOAD                 = 100;
}


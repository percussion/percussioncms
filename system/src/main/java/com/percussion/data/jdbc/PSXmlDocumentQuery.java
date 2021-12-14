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

package com.percussion.data.jdbc;

import com.percussion.data.IPSExecutionStep;
import com.percussion.data.PSConditionalEvaluator;
import com.percussion.data.PSDtdRelationalMapper;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSResultSet;
import com.percussion.data.PSResultSetMetaData;
import com.percussion.data.PSRowDataBuffer;
import com.percussion.data.PSSortedResultJoiner;
import com.percussion.data.jdbc.sqlparser.ASTBetweenPredicate;
import com.percussion.data.jdbc.sqlparser.ASTBooleanFactor;
import com.percussion.data.jdbc.sqlparser.ASTBooleanTerm;
import com.percussion.data.jdbc.sqlparser.ASTColumnReference;
import com.percussion.data.jdbc.sqlparser.ASTComparisonPredicate;
import com.percussion.data.jdbc.sqlparser.ASTDerivedColumn;
import com.percussion.data.jdbc.sqlparser.ASTDirectSQLDataStatement;
import com.percussion.data.jdbc.sqlparser.ASTDirectSelectStatementMultipleRows;
import com.percussion.data.jdbc.sqlparser.ASTEscapeClause;
import com.percussion.data.jdbc.sqlparser.ASTFileSpec;
import com.percussion.data.jdbc.sqlparser.ASTFromClause;
import com.percussion.data.jdbc.sqlparser.ASTGroupByClause;
import com.percussion.data.jdbc.sqlparser.ASTHavingClause;
import com.percussion.data.jdbc.sqlparser.ASTLikePredicate;
import com.percussion.data.jdbc.sqlparser.ASTLiteral;
import com.percussion.data.jdbc.sqlparser.ASTNullPredicate;
import com.percussion.data.jdbc.sqlparser.ASTOrderByClause;
import com.percussion.data.jdbc.sqlparser.ASTParameterSpecification;
import com.percussion.data.jdbc.sqlparser.ASTQuerySpecification;
import com.percussion.data.jdbc.sqlparser.ASTSelectList;
import com.percussion.data.jdbc.sqlparser.ASTSortKey;
import com.percussion.data.jdbc.sqlparser.ASTSortSpecification;
import com.percussion.data.jdbc.sqlparser.ASTStatementRoot;
import com.percussion.data.jdbc.sqlparser.ASTTableExpression;
import com.percussion.data.jdbc.sqlparser.ASTTableReference;
import com.percussion.data.jdbc.sqlparser.ASTWhereClause;
import com.percussion.data.jdbc.sqlparser.Node;
import com.percussion.data.jdbc.sqlparser.ParseException;
import com.percussion.data.jdbc.sqlparser.SQLParser;
import com.percussion.data.jdbc.sqlparser.SQLParserVisitor;
import com.percussion.data.jdbc.sqlparser.SimpleNode;
import com.percussion.data.jdbc.sqlparser.UncheckedSQLException;
import com.percussion.design.catalog.PSCatalogException;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSDtd;
import com.percussion.xml.PSDtdElementEntry;
import com.percussion.xml.PSDtdGenerator;
import com.percussion.xml.PSDtdParser;
import com.percussion.xml.PSDtdTree;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

/****************************************************************************
 * This class runs SQL queries, given a SQL statement parse tree. The main()
 * method allows interactive debugging of the class.
 *
 * DEBUGGING NOTE: To enable trace, follow these steps:
 *
 * 1) Replace all occurrences of the string "//--+trace(" with "trace(".
 *
 * 2) When you construct a PSXmlDocumentQuery object, before calling
 * run() on it, call enableTrace(System.out) on it.
 *
 * Now trace messages should be sent to the console when you run this.
 *
 * To disable trace, follow the steps in reverse, but be careful when
 * replacing all occurrences of "trace(" with "//--+trace". You must
 * only replace occurrences that are preceded by whitespace, otherwise
 * you will end up changing "enableTrace(" to "enable//--+trace". You
 * can do this in Developer Studio by replacing with regular expressions:
 * replace "\(\:b+\)trace(" with "\1//--+trace(" (check the Regular
 * Expression checkbox in the Replace dialog).
 *
 * All this crap is necessary because there is no Java preprocessor.
 *
 ***************************************************************************/
public class PSXmlDocumentQuery implements SQLParserVisitor
{
   public static void main(String[] args)
   {
      // first deal with the command-line arguments as a single query statement
      SQLParser parser = null;
      if (args.length > 0)
      {
         String stmtStr = "";
         for (int i = 0; i < args.length; i++)
         {
            stmtStr += " " + args[i];
         }
         System.out.println("Executing " + stmtStr);
         parser = new SQLParser(new StringReader(stmtStr));
      }
      else
      {
         parser = new SQLParser(System.in);
      }
      while (true)
      {
         try
         {
            System.out.print("Enter Expression: ");
            System.out.flush();

            // parse the SQL query and run it
            ASTStatementRoot statement = parser.StatementRoot();
            PSXmlDocumentQuery xq = new PSXmlDocumentQuery(statement);
            xq.enableTrace(System.out);
            xq.run();
         }
         catch (ParseException e)
         {
            System.out.println("***ERROR: " + e.toString() );
            parser.ReInit(System.in);
         }
         catch (Exception ex)
         {
            System.out.println("***ERROR: " + ex.getMessage());
         }
      }
   }

   public PSXmlDocumentQuery(SimpleNode root)
   {
      m_root = (ASTStatementRoot)root;
   }

   public ResultSet run() throws SQLException
   {
      try
      {
         return (ResultSet)m_root.jjtAccept(this, null);
      }
      catch (UncheckedSQLException e)
      {
         //--+trace(e);
         throw new SQLException(e.getMessage());
      }
   }

   public void enableTrace(PrintStream out)
   {
      m_traceOut = out;
      m_trace = true;
   }

   public void disableTrace()
   {
      m_trace = false;
      m_traceOut = null;
   }

   /**
    * We do no special processing here. Proceed immediately to children.
    *
    */
   public Object visit(SimpleNode node, Object data)
   {
      //--+trace("Visit SimpleNode(" + data + ")");
      ASTStatementRoot stRoot
         = (ASTStatementRoot)node.jjtGetChild(0);

      return stRoot.jjtAccept(this, data);
   }

   /**
    * We do no special processing here. Proceed immediately to children.
    *
    */
   public Object visit(ASTStatementRoot node, Object data)
   {
      //--+trace("Visit statement root: (" + data + ")");
      ASTDirectSQLDataStatement dirStmt
         = (ASTDirectSQLDataStatement)node.jjtGetChild(0);

      return dirStmt.jjtAccept(this, data);
   }

   /**
    * We do no special processing here. Proceed immediately to children.
    *
    */
   public Object visit(ASTDirectSQLDataStatement node, Object data)
   {
      //--+trace("Visit SQL statement: (" + data + ")");
      ASTDirectSelectStatementMultipleRows dSel
         = (ASTDirectSelectStatementMultipleRows)node.jjtGetChild(0);

      return dSel.jjtAccept(this, data);

      // TODO: handle ORDER BY clause
   }

   /**
    * We do no special processing here. Proceed immediately to children.
    *
    */
   public Object visit(ASTDirectSelectStatementMultipleRows node, Object data)
   {
      //--+trace("Visit direct select statement (" + data + ")");
      ASTQuerySpecification qSpec =
         (ASTQuerySpecification)node.jjtGetChild(0);

      return qSpec.jjtAccept(this, data);
   }

   /**
    * Marks the starting point for the query context, which will be passed to
    * other nodes and filled out as we visit them.
    *
    * Returns a result set which is the result of the query specification.
    */
   public Object visit(ASTQuerySpecification node, Object data)
   {
      //--+trace("Visit query specification (" + data + ")");

      QueryContext con = new QueryContext();

      // first visit the FROM clause to set up the table definitions,
      // even though the FROM clause comes later in the query than
      // the SELECT list
      for (int i = 0; i < node.jjtGetNumChildren(); i++)
      {
         Node n = node.jjtGetChild(i);
         if (n instanceof ASTTableExpression)
         {
            n.jjtAccept(this, con);
         }
      }

      // now do the SELECT list and everything but the FROM clause
      for (int i = 0; i < node.jjtGetNumChildren(); i++)
      {
         Node n = node.jjtGetChild(i);
         if (!(n instanceof ASTTableExpression))
         {
            n.jjtAccept(this, con);
         }
      }

      // commented out -- handled above
      // node.childrenAccept(this, con);

      // finally, do some joining and stuff
      //--+trace("Now trying joins...");

      PSResultSet joins = con.runJoins();

      try
      {
         joins.setBeforeFirst();

         if (m_trace)
         {
            while (joins.next())
            {
               String rowString = "=====";
               Object[] rowData = joins.getRowBuffer();
               for (int i = 0; i < rowData.length; i++)
               {
                  rowString += " | " + rowData[i];
               }
               //--+trace(rowString);
            }
            joins.setBeforeFirst();
         }
      }
      catch (SQLException e)
      {
         throw new UncheckedSQLException(e.getMessage());
      }

      return joins;
   }


   /**
    *
    *
    */
   public Object visit(ASTLiteral node, Object data)
   {
      //--+trace("Visit literal (" + data + ")");
      node.childrenAccept(this, data);
      return node;
   }

   /**
    *
    *
    */
   public Object visit(ASTColumnReference node, Object data)
   {
      //--+trace("Visit column reference (" + data + ")");
      node.childrenAccept(this, data);
      return node;
   }

   /**
    * From the SQL standard, Sec 5.4, Syntax Rule 12:
    *
    * <P/>
    * <OL start="12" type="1">
    * <LI> An <identifier> that is a <correlation name> is associated with
    *      a table within a particular scope. The scope of a <correlation
    *      name> is either a <select statement: single row>, <subquery>, or
    *      <query specification> (see Subclause 6.3, "<table reference>").
    *      Scopes may be nested. In different scopes, the same <correlation
    *      name> may be associated with different tables or with the same
    *      table.
    * </LI>
    * </OL>
    * <P/>
    * From the SQL standard, Sec 6.3:
    * <OL start="3" type="1">
    * <LI> A <table name> that is exposed by a <table reference> TR shall
    *      not be the same as any other <table name> that is exposed by a
    *      <table reference> with the same scope clause as TR.
    * </LI>
    * </OL>
    * <P/>
    * We create a query context at the ASTQuerySpecification level (above
    * us) and pass it in as the data argument.
    */
   public Object visit(ASTTableReference node, Object data)
   {
      //--+trace("Visit table reference (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTFileSpec node, Object data)
   {
      //--+trace("Visit file specification (" + data + ")");

      node.childrenAccept(this, data);

      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTParameterSpecification node, Object data)
   {
      //--+trace("Visit parameter specification (" + data + ")");
      node.childrenAccept(this, data);
      return node;
   }

   /**
    *
    *
    */
   public Object visit(ASTSelectList node, Object data)
   {
      //--+trace("Visit select list (" + data + ")");
      QueryContext con = (QueryContext)data;

      if (node.isAllColumns())
      {
         //--+trace("SELECT specified all colums (*)");
      }
      else
      {
         for (int i = 0; i < node.jjtGetNumChildren(); i++)
         {
            ASTDerivedColumn derivedCol = (ASTDerivedColumn)node.jjtGetChild(i);
            IPSReplacementValue val
               = (IPSReplacementValue)derivedCol.jjtAccept(this, data);
            con.addSelectColumn(val, derivedCol.getColumnAlias());
         }
      }
      return null;
   }

   /**
    * Could be a reference to a derived column in either a SELECT list
    * or a WHERE clause
    *
    * @return IPSReplacementValue
    */
   public Object visit(ASTDerivedColumn node, Object data)
   {
      //--+trace("Visit derived column (" + data + ")");
      QueryContext con = (QueryContext)data;

      IPSReplacementValue retVal = null;
      // get the value to which this derived column refers
      // this value can be a literal, a parameter reference, or a real
      // column reference
      Object colValue = node.jjtGetChild(0).jjtAccept(this, data);
      if (colValue instanceof ASTLiteral)
      {
         retVal = new PSTextLiteral(((ASTLiteral)colValue).getValue().toString());
      }
      else if (colValue instanceof ASTParameterSpecification)
      {
         if (true)
            throw new UncheckedSQLException(
               "Parameter specifications not supported: " +
               ((ASTParameterSpecification)colValue).getName());
      }
      else if (colValue instanceof ASTColumnReference)
      {
         // look up the fully qualified column name for this column
         ASTColumnReference ref = (ASTColumnReference)colValue;
         retVal = qualifyColumnName(ref, con);
      }

      return retVal;
   }

   /**
    *
    *
    */
   public Object visit(ASTTableExpression node, Object data)
   {
      //--+trace("Visit table expression (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTFromClause node, Object data)
   {
      //--+trace("Visit FROM clause: (" + data + ")");

      // set state
      m_state = STATE_FROM_CLAUSE;

      QueryContext con = (QueryContext)data;
      PSDtdTree dtdTree = null;

      try
      {
         // do children first, to set up the mappings
         for (int i = 0; i < node.jjtGetNumChildren(); i++)
         {
            ASTTableReference tableRef = (ASTTableReference)node.jjtGetChild(i);
            Node fileSpec = tableRef.jjtGetChild(0);
            if (!(fileSpec instanceof ASTFileSpec))
            {
               throw new UncheckedSQLException("table reference must be a file");
            }
            ASTFileSpec fs = (ASTFileSpec)fileSpec;

            File xmlFile = new File(fs.getValue());

            if (!xmlFile.canRead() || !xmlFile.isFile())
            {
               throw new UncheckedSQLException("The file " + fs.getValue() +
                  " does not exist.");
            }

            {
               String alias = tableRef.getAlias();
               String table = tableRef.getTable();

               if (table == null)
               {
                  // maybe it's a file specification instead, so treat it as
                  // the top level table
                  // TODO: handle queries across multiple documents with diff.
                  // DTDs
                  // table = con.getRelationalMapper().getTable(1).getName(); // 1-based
               }

               // only correlate the table with an alias if there is a real alias,
               // otherwise don't bother, it has already been correlated with itself
               // by the mapping
               if (table != null && alias != null && !alias.equals(table))
                  con.addTableCorrelation(alias, table);
            }

            // read in and parse the XML document, getting the DTD tree if
            // it is present in the document
            BufferedInputStream in = new BufferedInputStream(
               new FileInputStream(xmlFile));

            InputSource src = new InputSource(in);
            src.setEncoding(PSCharSets.rxStdEnc());
            con.addDocument(PSXmlDocumentBuilder.createXmlDocument(src, false));
            in.close();
         }

         // make sure either every or none of the documents has a DTD
         boolean dtdPresent = false;
         DocumentType sharedDocType = null;
         Document doc = null;

         for (int i = 0; i < con.getNumDocuments(); i++)
         {
            doc = con.getDocument(i);

            DocumentType docType = doc.getDoctype();
            if (docType == null)
            {
               if (dtdPresent)
                  throw new UncheckedSQLException("Documents do not share a common DTD.");
            }
            else
            {
               dtdPresent = true;
               if (sharedDocType != null)
               {
                  if (!docType.equals(sharedDocType))
                  {
                     throw new UncheckedSQLException("Documents do not share a common DTD: " +
                        docType.getName() + " != " + sharedDocType.getName());
                  }
               }
               sharedDocType = docType;
            }
         }

         PSDtdParser dtdParser = new PSDtdParser();
         PSDtd dtd = null;
         if (dtdPresent)
         {
            //doc contains the last doc in con
            if (doc != null)
               dtdParser.parseXmlForDtd(doc, false);
         }
         // no DTD present, we should guess at one
         else
         {
            Document[] exemplars = new Document[con.getNumDocuments()];
            for (int i = 0; i < con.getNumDocuments(); i++)
            {
               exemplars[i] = con.getDocument(i);
            }

            PSDtdGenerator dtdGen = new PSDtdGenerator();
            dtdGen.generateDtd(exemplars);

            // collect the DTD in a byte array
            ByteArrayOutputStream bout =
               new ByteArrayOutputStream(2048);

            dtdGen.writeDtd(bout);

            // dump the DTD to the trace stream if desired
            if (m_trace)
            {
               dtdGen.writeDtd(m_traceOut);
            }

            // now parse the DTD with the parser
            ByteArrayInputStream bias =
               new ByteArrayInputStream(bout.toByteArray());
            dtdParser.parseDtd(bias, null);
         }
         // create a DTD tree from the DTD in the parser
         dtd = dtdParser.getDtd();
         if (dtd != null)
            dtdTree = new PSDtdTree(dtd);
      }
      catch (PSCatalogException e)
      {
         //--+trace(e);
         throw new UncheckedSQLException("Error getting DTD: " +
            e.getMessage());
      }
      catch (IOException ioe)
      {
         //--+trace(ioe);
         throw new UncheckedSQLException(ioe.getMessage());
      }
      catch (SAXException saxe)
      {
         //--+trace(saxe);
         throw new UncheckedSQLException(saxe.getMessage());
      }

      PSDtdElementEntry dtdRootEntry = null;
      if (dtdTree != null)
         dtdRootEntry = dtdTree.getRoot();

      if (dtdRootEntry == null)
      {
         throw new UncheckedSQLException("Could not obtain DTD for document.");
      }

      // finally, generate tables for this DTD tree
      generateTables(dtdTree, con);

      // add table name mappings to the context
      PSDtdRelationalMapper mapper = con.getRelationalMapper();
      for (int i = 1; i <= mapper.getNumTables(); i++) // 1 based
      {
         PSDtdRelationalMapper.TableDef tab = mapper.getTable(i);
         con.addTableCorrelation(tab.getName(), tab.getName());
      }

      // make sure every table reference in the FROM clause is an
      // actual table

      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTWhereClause node, Object data)
   {
      //--+trace("Visit WHERE clause (" + data + ")");

      // set state
      m_state = STATE_WHERE_CLAUSE;

      node.childrenAccept(this, data);
      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTGroupByClause node, Object data)
   {
      //--+trace("Visit GROUP BY clause (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTHavingClause node, Object data)
   {
      //--+trace("Visit HAVING clause: (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }

   /**
    * BooleanTerm ( <OR> BooleanTerm )*
    *
    * @param node
    * @param data A query context
    *
    * THIS IS ORPHANED, NO ONE EVER VISITS THIS NODE
    */
   public Object visit(ASTBooleanTerm node, Object data)
   {
      //--+trace("Visit boolean term (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }

   /**
    * BooleanFactor ( <AND> BooleanFactor )*
    *
    */
   public Object visit(ASTBooleanFactor node, Object data)
   {
      //--+trace("Visit boolean factor (" + data + ")");

      QueryContext con = (QueryContext)data;

      // TODO: this child could be the first in a nested group of BooleanTerms
      // rather than a simple predicate that returns a PSConditional
      PSConditional cond = (PSConditional)node.jjtGetChild(0).jjtAccept(this, data);

      // set the boolean linking operator for the next conditional in the chain
      try
      {
         cond.setBoolean(node.getOp());
      }
      catch (IllegalArgumentException e)
      {
         //--+trace(e);
         throw new UncheckedSQLException(e.getLocalizedMessage());
      }

      if (node.isNegated())
         cond.negate();

      //--+trace("Boolean factor, conditional is " + cond.getOperator());

      if (STATE_WHERE_CLAUSE == m_state)
      {
         con.addWhereConditional(cond);
      }

      return null;
   }

   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/9
    *
    *
    *
    * @param   node the node
    * @param   data a query context
    *
    * @return   PSConditional
    */
   public Object visit(ASTComparisonPredicate node, Object data)
   {
      //--+trace("Visit comparison predicate (" + data + ")");

      // TODO: could be a NullSpecification instead of a DerivedColumn
      ASTDerivedColumn leftDerivedCol = (ASTDerivedColumn)node.jjtGetChild(0);
      IPSReplacementValue leftVal
         = (IPSReplacementValue)leftDerivedCol.jjtAccept(this, data);

      ASTDerivedColumn rightDerivedCol = (ASTDerivedColumn)node.jjtGetChild(1);
      IPSReplacementValue rightVal
         = (IPSReplacementValue)rightDerivedCol.jjtAccept(this, data);

      String op = null;
      switch (node.getOperator())
      {
      case ASTComparisonPredicate.EQ:
         op = PSConditional.OPTYPE_EQUALS;
         break;
      case ASTComparisonPredicate.NEQ:
         op = PSConditional.OPTYPE_NOTEQUALS;
         break;
      case ASTComparisonPredicate.LT:
         op = PSConditional.OPTYPE_LESSTHAN;
         break;
      case ASTComparisonPredicate.GT:
         op = PSConditional.OPTYPE_GREATERTHAN;
         break;
      case ASTComparisonPredicate.LTE:
         op = PSConditional.OPTYPE_LESSTHANOREQUALS;
         break;
      case ASTComparisonPredicate.GTE:
         op = PSConditional.OPTYPE_GREATERTHANOREQUALS;
         break;
      default:
         throw new UncheckedSQLException("unknown optype: " + node.getOperator());
      }

      PSConditional cond = null;
      try
      {
         cond = new PSConditional(leftVal, op, rightVal);
      }
      catch (IllegalArgumentException e)
      {
         //--+trace(e);
         throw new UncheckedSQLException(e.getLocalizedMessage());
      }

      // there are only two children (the two arguments to the op), and we
      // already visited them at the beginning of this method
      // node.childrenAccept(this, data);

      return cond;
   }

   /**
    *
    * @return PSConditional
    */
   public Object visit(ASTBetweenPredicate node, Object data)
   {
      //--+trace("Visit BETWEEN predicate (" + data + ")");

      // TODO: could be a NullSpecification instead of a DerivedColumn
      ASTDerivedColumn leftDerivedCol = (ASTDerivedColumn)node.jjtGetChild(0);
      IPSReplacementValue leftVal
         = (IPSReplacementValue)leftDerivedCol.jjtAccept(this, data);

      ASTDerivedColumn rightDerivedCol = (ASTDerivedColumn)node.jjtGetChild(1);
      IPSReplacementValue rightVal
         = (IPSReplacementValue)rightDerivedCol.jjtAccept(this, data);

      PSConditional cond = null;
      try
      {
         cond = new PSConditional(leftVal, PSConditional.OPTYPE_BETWEEN, rightVal);
         if (node.isNegated())
            cond.negate();
      }
      catch (IllegalArgumentException e)
      {
         //--+trace(e);
         throw new UncheckedSQLException(e.getLocalizedMessage());
      }

      // there are only two children (the two arguments to the op), and we
      // already visited them at the beginning of this method
      // node.childrenAccept(this, data);

      return cond;
   }

   /**
    *
    * @return PSConditional
    */
   public Object visit(ASTLikePredicate node, Object data)
   {
      //--+trace("Visit LIKE predicate (" + data + ")");

      // TODO: could be a NullSpecification instead of a DerivedColumn
      ASTDerivedColumn leftDerivedCol = (ASTDerivedColumn)node.jjtGetChild(0);
      IPSReplacementValue leftVal
         = (IPSReplacementValue)leftDerivedCol.jjtAccept(this, data);

      ASTDerivedColumn rightDerivedCol = (ASTDerivedColumn)node.jjtGetChild(1);
      IPSReplacementValue rightVal
         = (IPSReplacementValue)rightDerivedCol.jjtAccept(this, data);

      PSConditional cond = null;
      try
      {
         cond = new PSConditional(leftVal, PSConditional.OPTYPE_LIKE, rightVal);
         if (node.isNegated())
            cond.negate();
      }
      catch (IllegalArgumentException e)
      {
         //--+trace(e);
         throw new UncheckedSQLException(e.getLocalizedMessage());
      }

      // there are only two children (the two arguments to the op), and we
      // already visited them at the beginning of this method
      // node.childrenAccept(this, data);

      return cond;
   }

   /**
    *
    *
    */
   public Object visit(ASTEscapeClause node, Object data)
   {
      //--+trace("Visit ESCAPE clause (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTNullPredicate node, Object data)
   {
      //--+trace("Visit IS NULL predicate (" + data + ")");

      // TODO: could be a NullSpecification instead of a DerivedColumn
      ASTDerivedColumn leftDerivedCol = (ASTDerivedColumn)node.jjtGetChild(0);
      IPSReplacementValue leftVal
         = (IPSReplacementValue)leftDerivedCol.jjtAccept(this, data);

      PSConditional cond = null;
      try
      {
         cond = new PSConditional(leftVal, PSConditional.OPTYPE_ISNULL, null);
         if (node.isNegated())
            cond.negate();
      }
      catch (IllegalArgumentException e)
      {
         //--+trace(e);
         throw new UncheckedSQLException(e.getLocalizedMessage());
      }

      // there is only one child (the one argument to the op), and we
      // already visited it at the beginning of this method
      // node.childrenAccept(this, data);

      return cond;
   }

   public Object visit(ASTSortKey node, Object data)
   {
      //--+trace("Visit sort key (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTOrderByClause node, Object data)
   {
      //--+trace("Visit ORDER BY clause (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }

   /**
    *
    *
    */
   public Object visit(ASTSortSpecification node, Object data)
   {
      //--+trace("Visit sort specification (" + data + ")");
      node.childrenAccept(this, data);
      return null;
   }


   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/4
    *
    * Private utilility method to build a relational table structure from
    * a DTD.
    *
    * @param   dtdTree The DTD
    *
    */
   private void generateTables(PSDtdTree dtdTree, QueryContext con)
   {
      PSDtdRelationalMapper mapper = new PSDtdRelationalMapper(dtdTree, m_traceOut);
      con.setRelationalMapper(mapper);

      flattenDocument(con);
   }


   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/4
    *
    * Private utility method to flatten an XML document to a series of ResultSets,
    * one for each table defined in the relational mapping.
    *
    * @param   mapper
    *
    */
   private void flattenDocument(QueryContext con)
   {
      //--+trace("Flattening document...");
      PSDtdRelationalMapper mapper = con.getRelationalMapper();

      PSResultSet rs = new PSResultSet();
      if (mapper.getNumTables() < 1)
         throw new UncheckedSQLException("Error: no tables");

      PSDtdRelationalMapper.TableDef table = mapper.getTable(1); // 1 based

      try
      {
         table.initResultSet(rs);
      }
      catch (SQLException e)
      {
         //--+trace(e);
         throw new UncheckedSQLException(e.getMessage());
      }

      con.addResultSet(table.getName(), rs);

      for (int i = 0; i < con.getNumDocuments(); i++)
      {
         processElement(con, con.getDocument(i).getDocumentElement(), table, rs, i);
      }
   }

   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/4
    *
    * A recursive function that looks at the table definition and adds all
    * the relevant column data to the result set according to the columns
    * in the table def. Then, for every table that uses one of this table's
    * columns as a foreign key, process that table, setting that table's
    * foreign key column to the value of the corresponding column in this
    * table.
    *
    * @param   el
    * @param   table
    * @param   rs
    *
    */
   private void processElement(
      QueryContext con,
      Element el,
      PSDtdRelationalMapper.TableDef table,
      PSResultSet rs,
      int uniqueIDValue)
   {
      //--+trace("Processing element " + el.getNodeName() + "; table = " + table.getName());

      // the table name is the name of the highest-level element whose data
      // is directly contained in the columns of that table
      String tableName = table.getName();

      Element firstSibling = seekToFirstElement(el, tableName);

      // do not add a row, just return from the method without doing
      // children (because they would have no parent against which to join)
      if (null == firstSibling)
      {
         //--+trace("seekToFirstElement: " + el.getNodeName() + "," + tableName + " returned null.");
         return;
      }

      //--+trace("seekToFirstElement(" + tableName + ") positioned us on " + firstSibling.getNodeName());

      // loop over siblings of this element; each sibling is a single row
      org.w3c.dom.Node sib = firstSibling;

      //--+trace("" + sib);

      while (sib != null)
      {
         if (!(sib instanceof Element) || !sib.getNodeName().equals(tableName))
         {
            // skip siblings that don't have the right name or type
            //--+trace(sib.getNodeName() + " != " + tableName);
            sib = sib.getNextSibling();
            continue;
         }

         //--+trace("Processing row in table " + tableName);
         processRow((Element)sib, table, rs, ++uniqueIDValue);

         try
         {
            // now do any tables that refer to columns in this table as their
            // foreign key
            for (int i = 1; i <= table.getNumColumns(); i++) // 1 based
            {
               PSDtdRelationalMapper.ColumnDef col = table.getColumn(i);
               for (int j = 0; j < col.getNumPkeyReferences(); j++)
               {
                  PSDtdRelationalMapper.ColumnDef referringCol =
                     col.getPkeyReference(j);

                  PSDtdRelationalMapper.TableDef referringTable =
                     referringCol.getTable();

                  //--+trace("Chasing pkey ref " + referringTable.getName() + "." + referringCol.getName() + "(->" + col.getName() + ")");

                  PSResultSet referringTableRS =
                     con.getResultSet(referringTable.getName());

                  if (referringTableRS == null)
                  {
                     // create a new result set for this table
                     referringTableRS = new PSResultSet();
                     referringTable.initResultSet(referringTableRS);
                     con.addResultSet(referringTable.getName(), referringTableRS);
                  }

                  // run the query for the subtable with this result set
                  processElement(con, (Element)sib, referringTable, referringTableRS, 1);
               }

               // get values for foreign key columns in this table
               PSDtdRelationalMapper.ColumnDef fKeyCol = col.getForeignKey();

               if (fKeyCol == null)
                  continue;

               int fKeyColOrdinal = fKeyCol.getOrdinal();

               //--+trace("Getting value for foreign key column " + fKeyCol.getName());

               PSResultSet fTableRS = con.getResultSet(fKeyCol.getTable().getName());

               if (fTableRS == null)
                  throw new UncheckedSQLException("Could not get result set for parent table");

               int parentID = fTableRS.getInt(fKeyColOrdinal);

               //--+trace(col.getName() + " = " + parentID);

               rs.updateInt(i, parentID);
            }
         }
         catch (SQLException e)
         {
            //--+trace(e);
            throw new UncheckedSQLException(e.getMessage());
         }

         sib = sib.getNextSibling();
      } // end loop over siblings
   }

   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/4
    *
    * Process a single row for a table.
    *
    * @param   el   The element (whose name is generally equal to the table name) that
    * contains, directly or indirectly, all of the values for a row in the table.
    *
    * @param   table The table definition
    *
    * @param   rs   The result set to which a row
    *
    * @param   uniqueIDValue   The value for the unique ID column of the table, if
    * one exists. This parameter will be ignored if there is no unique ID column.
    */
   private void processRow(
      Element el,
      PSDtdRelationalMapper.TableDef table,
      PSResultSet rs,
      int uniqueIDValue)
   {
      //--+trace("Processing row: " + el.getNodeName());

      try
      {
         PSDtdRelationalMapper.ColumnDef unIDCol = table.getUniqueIDColumn();

         // add a null row and move to it so we can set values
         rs.setRow(rs.appendNullRow());

         if (unIDCol != null)
         {
            //--+trace("Unique ID column: " + unIDCol.getName() + " = " + uniqueIDValue);
            rs.updateInt(unIDCol.getOrdinal(), uniqueIDValue);
         }

         //--+trace("Getting value for other columns.");

         // get values for all the other columns
         for (int i = 1; i <= table.getNumColumns(); i++) // 1 based
         {
            // //--+trace("Getting value for column: " + i);

            if (unIDCol != null)
            {
               if (i == unIDCol.getOrdinal())
                  continue;
            }

            PSDtdRelationalMapper.ColumnDef col = table.getColumn(i);

            if (col.getForeignKey() != null)
            {
               continue; // foreign keys are filled in elsewhere
            }

            String colVal = getColumnValue(el, col.getName());
            //--+trace(col.getName() + " = " + colVal);
            if (colVal == null)
               rs.updateNull(i);
            else
               rs.updateString(i, colVal);
         }
      }
      catch (SQLException e)
      {
         //--+trace(e);
         throw new UncheckedSQLException(e.getMessage());
      }

      //--+trace("Finished row: " + el.getNodeName());
   }

   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/4
    *
    * Find the highest level element with the given name that is, or is under,
    * the given element.
    *
    * @param   el   The element
    * @param   name The name. If el's name equals name, returns el.
    *
    * @return   Element The highest level element named name, or null.
    */
   private Element seekToFirstElement(Element el, String name)
   {
      if (!el.getNodeName().equals(name))
      {
         NodeList children = el.getChildNodes();
         if (children == null)
            return null;
         if (children.getLength() == 0)
            return null;

         for (int i = 0; i < children.getLength(); i++)
         {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
            {
               if (n.getNodeName().equals(name))
                  return (Element)n;
            }
            else
               continue;
         }

         // if not found among immediate children, look in their children
         for (int i = 0; i < children.getLength(); i++)
         {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
            {
               el = seekToFirstElement((Element)n, name);
               if (el != null)
               {
                  return el;
               }
            }
         }

         // not found in immediate children, also not found in any of their
         // children and so on...so return null
      }
      else
         return el;

      return null;
   }


   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/7
    *
    * Gets the column value for the first element with the given name that
    * is, or is under, the given element.
    *
    * @param   el
    * @param   colName
    *
    * @return   String
    */
   private String getColumnValue(Element el, String colName)
   {
      // the column name uses forward slash to separate levels
      StringTokenizer tok = new StringTokenizer(colName, "/");
      Element valEl = el;
      while (tok.hasMoreTokens())
      {
         String elName = tok.nextToken();
         if (elName.startsWith("@")) // attribute?
         {
            Attr attr = valEl.getAttributeNode(elName.substring(1));
            if (attr == null)
               return null;
            else
               return attr.getValue();
         }

         valEl = seekToFirstElement(valEl, elName);
         if (valEl == null)
            return null;
      }

      if (valEl != null)
      {
         return getElementData(valEl);
      }
      return null;
   }

   /**
    * Get the value (text data) associated with the specified element.
    * If the specified element is null or has no text data,
    * returns the empty string.
    *
    * @param   node the element to retrieve data from
    *
    * @return                 the value of the element
    */
   public java.lang.String getElementData(org.w3c.dom.Element node)
   {
      String ret = "";
      org.w3c.dom.Node text;

      if (node != null)
      {
         for (text = node.getFirstChild();
            text != null;
            text = text.getNextSibling() )
         {
            /* the item's value is in one or more text nodes which are
             * its immediate children
             */
            if (text instanceof org.w3c.dom.Text)
               ret += text.getNodeValue();
            else
               break; // assumes normalized document
         }
      }

      return ret;
   }

   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/7
    *
    * Gets the fully qualified column name for a given column reference.
    *
    * @param   col   The column reference.
    *
    * @param   con   The query context which contains, among other things,
    * the table alias mappings.
    *
    * @return   PSBackEndColumn
    */
   PSBackEndColumn qualifyColumnName(
      ASTColumnReference col,
      QueryContext con   )
   {
      // *TODO* if column ref starts with a slash, then only look from root

      String colName = col.getColumn();
      IPSReplacementValue val = con.getSelectColumn(colName);

      // this column has already been qualified because it was in the
      // select list, so we can just return it directly from the
      // select list
      if (val != null && val instanceof PSBackEndColumn)
      {
         return (PSBackEndColumn)val;
      }

      String searchCol = colName;
      if (!searchCol.startsWith("/"))
         searchCol = "/" + searchCol;

      ArrayList occurrences = new ArrayList();

      PSDtdRelationalMapper.TableDef startTable = null;

      // if the column was qualified with a table or table alias, then
      // resolve the table reference
      String table = col.getTable();
      if (table != null && table.length() != 0)
      {
         table = con.getTableName(table); // resolve aliases
         startTable = con.getRelationalMapper().getTable(table);
         if (startTable == null)
         {
            throw new UncheckedSQLException("Column reference " + col.getTable()
               + "." + colName +
               " refers to an invalid table or table alias.");
         }
      }

      // if there is no table qualification, start from the top level table
      if (startTable == null)
         startTable = con.getRelationalMapper().getTable(1); // 1-based

      findColumnName(occurrences, "/", startTable, searchCol);

      // if the column reference was not found, then error
      if (occurrences.size() == 0)
         throw new UncheckedSQLException("Could not qualify column " + colName);

      // if the column reference was found in more than one place, then
      // warn about it
      String warnText = "";
      for (int i = 1; i < occurrences.size(); i++)
      {
         PSDtdRelationalMapper.ColumnDef matchingCol
            = (PSDtdRelationalMapper.ColumnDef)occurrences.get(i);

         warnText += "\n\tColumn ref " + colName + " can also be matched by " +
            matchingCol.getName() + " in table " + matchingCol.getTable().getName();
      }

      PSDtdRelationalMapper.ColumnDef qualifiedCol
         = (PSDtdRelationalMapper.ColumnDef)occurrences.get(0);

      if (warnText.length() > 0)
      {
         warnText = "Column ref " + colName + " is assumed to mean " +
            qualifiedCol.getName() + " in table " +
            qualifiedCol.getTable().getName() + ", but: " + warnText;

         addWarning(warnText);
      }

      // this is a little indirect -- we ask the column's table to give us
      // a version of the column that has been converted to a PSBackEndColumn,
      // which has its PSBackEndTable set to the table's internal PSBackEndTable
      // instance
      //--+trace("Qualified column ordinal is " + qualifiedCol.getOrdinal());
      try
      {
         return qualifiedCol.getTable().getBackEndColumn(qualifiedCol.getOrdinal());
      }
      catch (IllegalArgumentException e)
      {
         throw new UncheckedSQLException(e.getLocalizedMessage());
      }
   }

   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/8
    *
    * A recursive method to disambiguate and qualify a column name that
    * may contain only part of a full column name.
    *
    * @param   occurrences   The number of disambiguations. If there is more
    * than one element in this list after calling this method, it means
    * the column name is still ambiguous. If there are no elements in
    * this list after calling this method, then no disambiguation was
    * found.
    *
    * @param   prefix The string prefix for this table level, used for
    * recursion. The initial string prefix should be "/".
    *
    * @param   tabDef The starting table definition. This method will
    * look in this table and all of its child tables.
    *
    * @param   colName The column name fragment to disambiguate.
    *
    */
   void findColumnName(
      List occurrences,
      String prefix,
      PSDtdRelationalMapper.TableDef tabDef,
      String colName   )
   {
      // are we a column in the given table ?
      for (int i = 1; i <= tabDef.getNumColumns(); i++) // 1 based
      {
         PSDtdRelationalMapper.ColumnDef col = tabDef.getColumn(i);
         if ((prefix + col.getName()).endsWith(colName))
         {
            occurrences.add(col);
         }
      }

      // are we a column in any sub tables of the given table ?
      for (int i = 1; i <= tabDef.getNumColumns(); i++) // 1 based
      {
         PSDtdRelationalMapper.ColumnDef col = tabDef.getColumn(i);

         // for every foreign column that refers to this column as a
         // foreign key, recursively search for the column name fragment
         // in the tables of those columns
         for (int j = 0; j < col.getNumPkeyReferences(); j++)
         {
            PSDtdRelationalMapper.ColumnDef referringCol =
               col.getPkeyReference(j);

            PSDtdRelationalMapper.TableDef referringTable =
               referringCol.getTable();

            findColumnName(occurrences, prefix + tabDef.getName() + "/",
               referringTable, colName);
         }
      }
   }

   // TODO: add this to a SQL warning object
   void addWarning(String warnText)
   {
      //--+trace("WARNING: " + warnText);
      m_warnings.add(warnText);
   }


   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/15
    *
    * Gets the warnings, useful for adding to SQL objects.
    * @return   Collection
    */
   Collection getWarnings()
   {
      return Collections.unmodifiableCollection(m_warnings);
   }

   /** the root of the parse tree */
   private Node m_root;

   /** true if tracing is enabled; false otherwise */
   private boolean m_trace;

   /** if tracing is enabled, refers to a PrintStream object; otherwise is null */
   private PrintStream m_traceOut;

   /** the current state of the query processer */
   private int m_state = STATE_INVALID;

   /** a collection of warning strings **/
   private Collection m_warnings = new ArrayList();

   private static final int STATE_INVALID = 0;
   private static final int STATE_FROM_CLAUSE = 10;
   private static final int STATE_WHERE_CLAUSE = 20;

   /*
    *
    *                            START INNER CLASSES
    *
    */

   /**
    * Defines a query context containing table aliases and derived column names.
    * This query context is also known as a scope.
    *
    */
   private class QueryContext
   {
      public QueryContext()
      {
         m_tableCorrelations = new HashMap();
         m_selectColumns = new HashMap();
         m_selectColumnsOrdered = new ArrayList();
         m_resultSets = new HashMap();
         m_resultSetsInOrder = new ArrayList();
         m_postJoinSelectionCriteria = new HashMap();
         m_preJoinSelectionCriteria = new HashMap();
         m_docs = new ArrayList();
         m_whereColumnNames = new ArrayList();

         // we initialize m_selectColumnNames only when needed, because
         // we rely on it being null if it wasn't needed
         m_selectColumnNames = null;
      }

      /**
       * @author   chad loder
       *
       * @version 1.0 1999/6/7
       *
       * Adds a derived column along with an AS name (alias) for that column.
       *
       * @param   dCol   The derived column data source
       * @param   asName   The AS name for the column
       *
       */
      public void addSelectColumn(IPSReplacementValue dCol, String asName)
      {
         if (asName != null)
         {
            if (m_selectColumns.containsKey(asName))
            {
               throw new UncheckedSQLException("Derived column name already assigned: " + asName);
            }
            m_selectColumns.put(asName, dCol);
            m_selectColumnsOrdered.add(dCol);
         }
         else
         {
            // alias the column to its real name
            m_selectColumns.put(dCol.toString(), dCol);
         }

         if (dCol instanceof PSBackEndColumn)
         {
            if (m_selectColumnNames == null)
               m_selectColumnNames = new ArrayList();

            PSBackEndColumn beCol = (PSBackEndColumn)dCol;

            // add the column name to our list of selected column names
            m_selectColumnNames.add(beCol.getColumn());

            //--+trace("Adding SELECT column: " + beCol.getColumn());

            // keep track of the highest and lowest tables involved in
            // joining, so we join no more than we have to
            String tabName = beCol.getTable().getAlias();
            int tabOrd = m_dtdMapper.getTableOrdinal(tabName);
            if (tabOrd < 1)
               throw new UncheckedSQLException("Unknown table: " + tabName);

            if (tabOrd < m_highestJoinOrdinal || m_highestJoinOrdinal < 1)
            {
               //--+trace("HIGHEST JOIN TABLE: " + tabName);
               m_highestJoinOrdinal = tabOrd;
            }

            if (tabOrd > m_lowestJoinOrdinal)
            {
               //--+trace("LOWEST JOIN TABLE: " + tabName);
               m_lowestJoinOrdinal = tabOrd;
            }
         }
      }

      public void addTableCorrelation(String alias, String table)
      {
         //--+trace("Correlating " + table + " with alias " + alias);
         if (alias != null)
         {
            if (m_tableCorrelations.containsKey(alias))
            {
               throw new UncheckedSQLException("Table correlation name already assigned: " + alias);
            }
            if (m_tableCorrelations.containsValue(alias))
            {
               throw new UncheckedSQLException("Table correlation name is a real table name: " + alias);
            }

            // we do allow people to refer to the same table with different
            // correlations, so don't check for table already a value

            // correlate this table with the given alias
            m_tableCorrelations.put(alias, table);
         }
         else    // no alias, so correlate the table name with itself
         {
            if (m_tableCorrelations.containsKey(table))
            {
               throw new UncheckedSQLException("Table name used as a correlation: " + table);
            }
            m_tableCorrelations.put(table, table);
         }
      }

      public IPSReplacementValue getSelectColumn(String asName)
      {
         return (IPSReplacementValue)m_selectColumns.get(asName);
      }

      public String getTableName(String alias)
      {
         return (String)m_tableCorrelations.get(alias);
      }

      public Set getTableCorrelations()
      {
         return m_tableCorrelations.keySet();
      }

      public void addResultSet(String tableName, PSResultSet rs)
      {
         m_resultSets.put(tableName, rs);
         m_resultSetsInOrder.add(rs);
      }

      public PSResultSet getResultSet(String tableName)
      {
         return (PSResultSet)m_resultSets.get(tableName);
      }

      public int getNumResultSets()
      {
         return m_resultSetsInOrder.size();
      }

      public PSResultSet getResultSet(int i) // 0 based
      {
         return (PSResultSet)m_resultSetsInOrder.get(i);
      }

      public PSDtdRelationalMapper getRelationalMapper()
      {
         return m_dtdMapper;
      }

      public void setRelationalMapper(PSDtdRelationalMapper mapper)
      {
         m_dtdMapper = mapper;
      }

      public void addDocument(Document doc)
      {
         m_docs.add(doc);
      }

      public Document getDocument(int i)
      {
         return (Document)m_docs.get(i);
      }

      public int getNumDocuments()
      {
         return m_docs.size();
      }

      /**
       * @author   chad loder
       *
       * @version 1.0 1999/6/10
       *
       * Adds a WHERE conditional to the query context. The conditional
       * will be analyzed and put in the appropriate place according to
       * these rules:
       * <UL>
       * <LI>If all arguments to the conditional are either from the same
       * table or literals, then add that conditional to the pre-join
       * selection criteria for that table.
       * <LI>If arguments are from different tables, then add that conditional
       * to the post-join selection criteria for the lowest-level (child)
       * table, because higher values cannot be compared with lower values
       * until a join has made their relation meaningful.
       * </UL>
       *
       * @param   cond
       *
       */
      public void addWhereConditional(PSConditional cond)
      {
         //--+trace("Adding where conditional: " + cond.toString());
         IPSReplacementValue left = cond.getVariable();
         IPSReplacementValue right = cond.getValue();

         boolean preJoin = false;
         String tableName = null;

         if (left instanceof PSBackEndColumn)
         {
            PSBackEndColumn leftCol = (PSBackEndColumn)left;
            //--+trace("leftCol == " + leftCol.getColumn());
            tableName = leftCol.getTable().getAlias();
            preJoin = true;

            // add the column name to our list of WHERE column names
            m_whereColumnNames.add(leftCol.getColumn());
         }

         if (right instanceof PSBackEndColumn)
         {
            PSBackEndColumn rightCol = (PSBackEndColumn)right;

            // add the column name to our list of WHERE column names
            m_whereColumnNames.add(rightCol.getColumn());

            String rightTableName = rightCol.getTable().getAlias();
            if (tableName == null)
            {
               // the left side is not a column, so put this with
               // the right column's pre-join conditions
               tableName = rightTableName;
               preJoin = true;
            }
            else
            {
               // there is a precedence question between two tables,
               // we need to figure out the dependency relationship
               // between the tables and put the conditional with the
               // post-join conditionals of the dependent table
               PSDtdRelationalMapper.TableDef leftTab
                  = m_dtdMapper.getTable(tableName);
               PSDtdRelationalMapper.TableDef rightTab
                  = m_dtdMapper.getTable(rightTableName);

               if (leftTab.getName().equals(rightTab.getName()))
               {
                  // they are from the same table, so add this to the
                  // prejoin conditions for the table
                  tableName = leftTab.getName();
                  preJoin = true;
               }
               else
               {
                  if (leftTab.dependsOn(rightTab))
                  {
                     tableName = leftTab.getName();
                  }
                  else if (rightTab.dependsOn(leftTab))
                  {
                     tableName = rightTab.getName();
                  }
                  else
                  {
                     throw new UncheckedSQLException("Cannot relate table " +
                        leftTab.getName() + " with " + rightTab.getName());
                  }

                  preJoin = false;
               }
            }
         }
         else
         {
            // the right side is not a column, so put this conditional
            // with the left side's pre-join conditionals
            preJoin = true;
         }

         if (tableName == null)
         {
            // TODO: add this to final result set selection
            //--+trace("TODO: Adding conditional to final result set selection.");
         }
         else
         {
            // keep track of the highest and lowest tables involved in
            // joining, so we join no more than we have to
            int tabOrd = m_dtdMapper.getTableOrdinal(tableName);
            if (tabOrd < 1)
               throw new UncheckedSQLException("Unknown table: " + tableName);

            if (tabOrd < m_highestJoinOrdinal || m_highestJoinOrdinal < 1)
            {
               //--+trace("HIGHEST JOIN TABLE: " + tableName);
               m_highestJoinOrdinal = tabOrd;
            }

            if (tabOrd > m_lowestJoinOrdinal)
            {
               //--+trace("LOWEST JOIN TABLE: " + tableName);
               m_lowestJoinOrdinal = tabOrd;
            }

            if (preJoin)
            {
               //--+trace("Adding conditional to " + tableName + " (PREJOIN)");
               addPreJoinSelectionConditional(tableName, cond);
            }
            else
            {
               //--+trace("Adding conditional to " + tableName + " (POSTJOIN)");
               addPostJoinSelectionConditional(tableName, cond);
            }
         }
      }

      protected void addPostJoinSelectionConditional(String tableName, PSConditional cond)
      {
         PSCollection coll = (PSCollection)m_postJoinSelectionCriteria.get(tableName);
         if (null == coll)
         {
            coll = new PSCollection(cond.getClass());
            m_postJoinSelectionCriteria.put(tableName, coll);
         }
         coll.add(cond);
      }

      protected void addPreJoinSelectionConditional(String tableName, PSConditional cond)
      {
         PSCollection coll = (PSCollection)m_preJoinSelectionCriteria.get(tableName);
         if (null == coll)
         {
            coll = new PSCollection(cond.getClass());
            m_preJoinSelectionCriteria.put(tableName, coll);
         }
         coll.add(cond);
      }

      /**
       * @author   chad loder
       *
       * @version 1.0 1999/6/10
       *
       * @param   tableName The name of the table to which the selection
       * conditions should be applied.
       *
       * @return   PSCollection of PSConditional objects that can be applied
       * to this table before joining it with anything.
       */
      public PSCollection getPreJoinSelectionConditionals(String tableName)
      {
         return (PSCollection)m_preJoinSelectionCriteria.get(tableName);
      }

      /**
       * @author   chad loder
       *
       * @version 1.0 1999/6/10
       *
       * @param   tableName The name of the table to which the selection
       * conditions should be applied.
       *
       * @return   PSCollection of PSConditional objects that can be applied
       * to this table after it is joined to the appropriate parent table(s).
       */
      public PSCollection getPostJoinSelectionConditionals(String tableName)
      {
         return (PSCollection)m_postJoinSelectionCriteria.get(tableName);
      }

      public PSResultSet runJoins()
      {
         PSResultSet parentJoinResult = null;
         PSResultSet finalResultSet = null;

         try
         {
            PSExecutionData data = new PSExecutionData(null, null, null);
            Stack joinStack = data.getResultSetStack();
            PSBackEndColumn parentJoinCol = null;

            // start at the highest level table and join every table back up to the
            // big-assed result set. This requires that the tables be in order
            // from highest level to lowest, the order of sibling tables relative
            // to each other is not important. It also requires that the top level
            // table have no siblings (i.e., that there really be a highest table)
            // This will always apply because XML documents have only one root,
            // and we make this our first table.

            int highTableOrdinal = m_highestJoinOrdinal; // 1-based
            int lowTableOrdinal = m_lowestJoinOrdinal; // 1-based

            if (highTableOrdinal < 0)
            {
               // there is no high table index, which means that the query
               // selected all columns (using SELECT *)
               highTableOrdinal = 1; // 1-based
            }

            if (lowTableOrdinal < 0)
            {
               // there is no low table index, which means that the query
               // selected all columns (using SELECT *)
               lowTableOrdinal = m_dtdMapper.getNumTables(); // 1-based
            }

            // Before we go through the loop, we will be on the root table
            // and we will need to set the parent join result to the plain
            // old result set of the root table.
            {
               PSDtdRelationalMapper.TableDef highTab
                  = m_dtdMapper.getTable(highTableOrdinal);
               parentJoinResult = (PSResultSet)m_resultSets.get(highTab.getName());
               joinStack.push(parentJoinResult);

               // get and run the the prejoin selection conditions, if there are any
               PSCollection coll = getPreJoinSelectionConditionals(highTab.getName());
               if (coll != null)
               {
                  //--+trace("Running pre-join selection on " + highTab.getName());
                  ConditionalApplier condApp = new ConditionalApplier(coll);
                  condApp.execute(data);
                  // after execution, the modified result set is pushed onto the stack
               }
            }

            // start the loop at the next lowest table
            for (int i = highTableOrdinal + 1; i <= lowTableOrdinal; i++) // 1-based
            {
               PSDtdRelationalMapper.TableDef tab = m_dtdMapper.getTable(i);
               PSBackEndColumn childJoinCol = null;
               PSDtdRelationalMapper.TableDef parentTab = null;

               //--+trace("Joining up table " + tab.getName());

               // find the join column for this table up to the parent table
               // this loop assumes that there is only one foreign key
               // reference per table, which is the parentID column
               // that points to the primary ID column in the parent table
               for (int j = 1; j <= tab.getNumColumns(); j++) // 1-based
               {
                  PSDtdRelationalMapper.ColumnDef col = tab.getColumn(j);
                  PSDtdRelationalMapper.ColumnDef parentCol = col.getForeignKey();
                  if (parentCol != null)
                  {
                     parentTab = parentCol.getTable();
                     parentJoinCol
                        = parentTab.getBackEndColumn(parentCol.getOrdinal());
                     childJoinCol = tab.getBackEndColumn(j);
                     parentJoinCol.setTable(new PSBackEndTable(parentCol.getTable().getName()));
                     break; // we found it
                  }
               }

               if (null == childJoinCol)
               {
                  // for some reason, we didn't find a way to join this column up to
                  // the parent table
                  throw new UncheckedSQLException("Can't join table " + tab.getName());
               }

               // create the join definition
               PSBackEndJoin join = new PSBackEndJoin(parentJoinCol, childJoinCol);

               // LEFT: we need to go through the parent result set and build the list
               // of columns. This assumes that the joiner sets up the result set
               // meta data correctly after doing a join.
               PSResultSetMetaData parentMeta = (PSResultSetMetaData)parentJoinResult.getMetaData();
               String[] parentColumnNames = new String[parentMeta.getColumnCount()];
               for (int j = 1; j <= parentMeta.getColumnCount(); j++) // 1-based
               {
                  //--+trace("parentColumnNames[j - 1] = " + parentMeta.getColumnName(j));
                  parentColumnNames[j - 1] = parentMeta.getColumnName(j);
               }

               // RIGHT: get the column names from the table definition of the current
               // table
               String[] childColumnNames = new String[tab.getNumColumns()];
               for (int j = 1; j <= childColumnNames.length; j++) // 1-based
               {
                  childColumnNames[j-1] = tab.getColumn(j).getName();
               }

               // the parent (left) table is already pushed onto the stack, so
               // just push the child (right) table onto the stack here
               {
                  PSResultSet childRS = (PSResultSet)m_resultSets.get(tab.getName());
                  childRS.setBeforeFirst();
                  joinStack.push(childRS);

                  // run the prejoin selection criteria on this table
                  PSCollection coll = getPreJoinSelectionConditionals(tab.getName());
                  if (coll != null)
                  {
                     //--+trace("Running pre-join selection on " + tab.getName());
                     ConditionalApplier condApp = new ConditionalApplier(coll);
                     condApp.execute(data);
                     // after execution, the modified result set is pushed onto the stack
                  }
               }

               String[] parentOmitCols = null;
               String[] childOmitCols = null;

               // if we are on the last child table, then omit from this join all
               // columns that we didn't select and do not need for post selection
               // criteria
               // TODO: optimize...this seems excessive
               if (i == lowTableOrdinal)
               {
                  // if the user chose to select some columns but maybe not others
                  if (m_selectColumnNames != null)
                  {
                     String[] pColumnNames = new String[parentMeta.getColumnCount()];
                     for (int k = 1; k <= parentMeta.getColumnCount(); k++) // 1-based
                     {
                        pColumnNames[k - 1] = parentMeta.getColumnName(k);
                     }

                     // RIGHT: get the column names from the table definition of the current
                     // table
                     String[] cColumnNames = new String[tab.getNumColumns()];
                     for (int k = 1; k <= cColumnNames.length; k++) // 1-based
                     {
                        cColumnNames[k-1] = tab.getColumn(k).getName();
                     }

                     ArrayList tmpParentOmitCols = new ArrayList();
                     if (m_selectColumnNames != null)
                        tmpParentOmitCols.removeAll(m_selectColumnNames);
                     if (m_whereColumnNames != null)
                        tmpParentOmitCols.removeAll(m_whereColumnNames);
                     parentOmitCols = (String[])tmpParentOmitCols.toArray(new String[0]);

                     ArrayList tmpChildOmitCols = new ArrayList();
                     if (m_selectColumnNames != null)
                        tmpChildOmitCols.removeAll(m_selectColumnNames);
                     if (m_whereColumnNames != null)
                        tmpChildOmitCols.removeAll(m_whereColumnNames);
                     childOmitCols = (String[])tmpChildOmitCols.toArray(new String[0]);
                  }
               }

               PSSortedResultJoiner joiner
                  = new PSSortedResultJoiner(
                     null, // no app handler
                     join,
                     parentColumnNames,
                     parentOmitCols,
                     childColumnNames,
                     childOmitCols,
                     100);

               joiner.closeInputResultsAfterJoin(false);

               // make sure the result sets are set at the beginning...
               parentJoinResult.setBeforeFirst();

               // run the join, which will pop both result sets off the stack and
               // push the joined result set back on
               joiner.execute(data);

               // get and run the post-join selection conditions, if there are any
               PSCollection coll = getPostJoinSelectionConditionals(tab.getName());
               if (coll != null)
               {
                  //--+trace("Running post-join selection on " + tab.getName());
                  ConditionalApplier condApp = new ConditionalApplier(coll);
                  condApp.execute(data);
                  // after execution, the modified result set is pushed onto the stack
               }

               parentJoinResult = (PSResultSet)joinStack.peek();

               if (joinStack.size() != 1)
               {
                  throw new UncheckedSQLException
                     ("Extra stack stuff: " + joinStack.pop().getClass().getName());
               }

               if (m_trace)
               {
                  try
                  {
                     ResultSetMetaData meta = parentJoinResult.getMetaData();
                     for (int k = 1; k <= meta.getColumnCount(); k++) // 1-based
                     {
                        //--+trace("==>" + meta.getColumnName(k));
                     }
                  }
                  catch (SQLException e)
                  {
                     //--+trace(e);
                  }
               }
            }

            List selectCols = m_selectColumnNames;
            // if there are no select column names, then the user
            // specified select ALL columns (select *), so build the list
            // now
            if (selectCols == null)
            {
               // for all tables T, for all columns C of T, add C to the select list
               selectCols
                  = new ArrayList(m_dtdMapper.getNumTables() * 3); // TODO: tune size

               for (int i = 1; i <= m_dtdMapper.getNumTables(); i++) // 1-based
               {
                  PSDtdRelationalMapper.TableDef tab = m_dtdMapper.getTable(i);
                  for (int j = 1; j <= tab.getNumColumns(); j++) // 1-based
                  {
                     selectCols.add(tab.getColumn(j).getName());
                  }
               }
            }

            // finally, use and re-order only those columns we desire and
            // build a result set out of them
            List[] finalColumns = new List[selectCols.size()];

            HashMap colNameMap = new HashMap();
            for (int k = 0; k < selectCols.size(); k++)
            {
               String colName = (String)selectCols.get(k);
               //--+trace("SELECT column: " + colName);
               finalColumns[k] = parentJoinResult.getColumnData(colName);
               colNameMap.put(colName, new Integer(k+1));
            }
            finalResultSet = new PSResultSet(finalColumns, colNameMap, null);
         }
         catch (Throwable t)
         {
            //--+trace(t);
            throw new UncheckedSQLException(t.getMessage());
         }

         return finalResultSet;
      }

      public class ConditionalApplier implements IPSExecutionStep
      {
         public ConditionalApplier(PSCollection conds)
         {
            m_conds = conds;
         }

         /**
          * Execute this step in the execution plan.
          *
          * @param   data     execution data is a container for the input data
          *                   as well as a collection of result sets generated
          *                   by queries.
          *
          * @exception   SQLException
          *                                                                                                                                                                                                       if a SQL error occurs
          */
         public void execute(PSExecutionData data)
            throws java.sql.SQLException
         {
            // there had better be a result set and a collection of conditionals on the stack
            java.util.Stack stack = data.getResultSetStack();

            PSResultSet in = (PSResultSet)stack.pop();
            in.setBeforeFirst();

            PSResultSet out = new PSResultSet();
            out.setMetaData(new PSResultSetMetaData(in.getMetaData()));

            int rowsAdded = 0;
            try
            {
               PSConditionalEvaluator rowProc = new PSConditionalEvaluator(m_conds);

               rowProc.setResultSetMetaData(in.getMetaData());

               PSRowDataBuffer rowData
                  = new PSRowDataBuffer(in);

               while (rowData.readRow())
               {
                  Object row[] = rowData.getCurrentRow();

                  data.setCurrentResultRowData(row);

                  if (m_trace)
                  {
                     String rowString = "";
                     for (int i = 0; i < row.length; i++)
                     {
                        rowString += row[i] + ",";
                     }
                     //--+trace("Read row " + rowString);
                  }

                  if (rowProc.processRow(data))
                  {
                     //--+trace("Added row");
                     out.addRow(row);
                     rowsAdded++;
                  }
                  else
                  {
                     //--+trace("Dropped row");
                  }
               }

               in.setBeforeFirst();
               out.setBeforeFirst();
               stack.push(out);
            }
            catch (Exception e)
            {
               throw new UncheckedSQLException(e.getMessage());
            }
         }

         private PSCollection m_conds;
      }

      /** a map from SELECT column names to IPSReplacementValues */
      private Map m_selectColumns;

      /** a list, in order, of the SELECT columns as IPSReplacementValues */
      private List m_selectColumnsOrdered;

      /** a list, in order, of the SELECT column names */
      private List m_selectColumnNames;

      /** a list, in order, of the WHERE column names */
      private List m_whereColumnNames;

      /** a map from table alias to table name */
      private Map m_tableCorrelations;

      /** a map from table names to result sets */
      private Map m_resultSets;

      /** the result sets, in the order they were created (parent to child) */
      private List m_resultSetsInOrder;

      /** the mapping from XML document(s) to a relational structure */
      private PSDtdRelationalMapper m_dtdMapper;

      /** the documents against which the query will be run */
      private List m_docs;

      /** a Map from table name to PSCollection of PSConditionals */
      private Map m_preJoinSelectionCriteria;

      /** a Map from table name to PSCollection of PSConditionals */
      private Map m_postJoinSelectionCriteria;

      private int m_highestJoinOrdinal = -1;

      private int m_lowestJoinOrdinal = -1;

   } // end private class QueryContext
}

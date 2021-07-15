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
package com.percussion.data.jdbc.sqlparser;

public class SQLParser/*@bgen(jjtree)*/implements SQLParserTreeConstants, SQLParserConstants {/*@bgen(jjtree)*/
  protected static JJTSQLParserState jjtree = new JJTSQLParserState();public static void main(String args[]) throws ParseException
        {
                SQLParser parser = new SQLParser(System.in);
                while (true)
                {
                        try
                        {
                                System.out.print("Enter Expression: ");
                                System.out.flush();

                                // this is the JJTree version
                                ASTStatementRoot statement = parser.StatementRoot();
                                statement.dump("");

                                System.out.println("Parsed statement.");
                                System.out.flush();
                        }
                        catch (ParseException e)
                        {
                                System.out.println("ERROR: " + e.toString() );
                                parser.ReInit(System.in);
                        }
                }
    }

/*************************************************************************
 *   ENTRY POINT -- the root contains the valid statements
 ************************************************************************/
  static final public ASTStatementRoot StatementRoot() throws ParseException {
                                                   /*@bgen(jjtree) StatementRoot */
  ASTStatementRoot jjtn000 = new ASTStatementRoot(JJTSTATEMENTROOT);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      DirectSQLStatement();
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
          {if (true) return jjtn000;}
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
    throw new Error("Missing return statement in function");
  }

/*************************************************************************
 *   DIRECT SQL STATEMENT (Sec. 20.1, p.443)
 ************************************************************************/
  static final public void DirectSQLStatement() throws ParseException {
    DirectlyExecutableStatement();
    jj_consume_token(Semicolon);
  }

  static final public void DirectlyExecutableStatement() throws ParseException {
    DirectSQLDataStatement();
  }

  static final public void DirectSQLDataStatement() throws ParseException {
                                 /*@bgen(jjtree) DirectSQLDataStatement */
  ASTDirectSQLDataStatement jjtn000 = new ASTDirectSQLDataStatement(JJTDIRECTSQLDATASTATEMENT);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      DirectSelectStatementMultipleRows();
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   DIRECT SELECT STATEMENT: MULTIPLE ROWS (Sec. 20.2, p.447)
 ************************************************************************/
  static final public void DirectSelectStatementMultipleRows() throws ParseException {
                                                                               /*@bgen(jjtree) DirectSelectStatementMultipleRows */
  ASTDirectSelectStatementMultipleRows jjtn000 = new ASTDirectSelectStatementMultipleRows(JJTDIRECTSELECTSTATEMENTMULTIPLEROWS);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      QueryExpression();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ORDER:
        OrderByClause();
        break;
      default:
        jj_la1[0] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   QUERY EXPRESSION (Sec. 7.10, p.159)
 ************************************************************************/
  static final public void QueryExpression() throws ParseException {
    NonJoinQueryExpression();
  }

  static final public void NonJoinQueryExpression() throws ParseException {
    NonJoinQueryTerm();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SELECT:
      case LeftParen:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      QueryExpression();
      jj_consume_token(UNION);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ALL:
        jj_consume_token(ALL);
        break;
      default:
        jj_la1[2] = jj_gen;
        ;
      }
      QueryTerm();
    }
  }

  static final public void QueryTerm() throws ParseException {
    NonJoinQueryTerm();
  }

  static final public void NonJoinQueryTerm() throws ParseException {
    NonJoinQueryPrimary();
  }

  static final public void QueryPrimary() throws ParseException {
    NonJoinQueryPrimary();
  }

  static final public void NonJoinQueryPrimary() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SELECT:
      SimpleTable();
      break;
    case LeftParen:
      jj_consume_token(LeftParen);
      NonJoinQueryExpression();
      jj_consume_token(RightParen);
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  static final public void SimpleTable() throws ParseException {
    QuerySpecification();
  }

/*************************************************************************
 *   NUMERIC LITERALS
 *   Note that some of the numeric literals are tokens (where we could get
 *   away with it) and some of them are productions (where we couldn't make
 *   tokens out of them). The general philosophy is that most productions
 * don't get addet to the tree, but instead return on object, the class
 * of which is the most specific class I felt safe using to represent
 * that object (for example, a SignedNumericLiteral returns a
 * java.math.BigDecimal, but a Literal returns a java.lang.Comparable because
 * it could be either a Number or a String).
 ************************************************************************/
  static final public java.math.BigDecimal SignedNumericLiteral() throws ParseException {
                int sign = 1;   // the sign from the token
                java.math.BigDecimal num;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PlusSign:
    case MinusSign:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PlusSign:
        jj_consume_token(PlusSign);
        break;
      case MinusSign:
        jj_consume_token(MinusSign);
                                sign = -1;
        break;
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[5] = jj_gen;
      ;
    }
    num = UnsignedNumericLiteral();
                if (sign == -1)
                {
                        {if (true) return num.negate();}
                }
                else
                        {if (true) return num;}
    throw new Error("Missing return statement in function");
  }

  static final public java.math.BigDecimal UnsignedNumericLiteral() throws ParseException {
                Token t;
                java.math.BigDecimal val;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ApproximateNumericLiteral:
      t = jj_consume_token(ApproximateNumericLiteral);
                  val = new java.math.BigDecimal(t.image);
      break;
    case ExactNumericLiteral:
      t = jj_consume_token(ExactNumericLiteral);
                  val = new java.math.BigDecimal(t.image);
          {if (true) return val;}
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/*************************************************************************
 *   LITERALS (Sec. 5.3, p. 71)
 ************************************************************************/
  static final public void Literal() throws ParseException {
         /*@bgen(jjtree) Literal */
                ASTLiteral jjtn000 = new ASTLiteral(JJTLITERAL);
                boolean jjtc000 = true;
                jjtree.openNodeScope(jjtn000);Comparable val = null;
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ExactNumericLiteral:
      case ApproximateNumericLiteral:
      case PlusSign:
      case MinusSign:
        val = SignedNumericLiteral();
        break;
      case CharacterStringLiteral:
        val = GeneralLiteral();
        break;
      default:
        jj_la1[7] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
          jjtn000.setValue(val);
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public Comparable UnsignedLiteral() throws ParseException {
                Comparable val = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ExactNumericLiteral:
    case ApproximateNumericLiteral:
      val = UnsignedNumericLiteral();
      break;
    case CharacterStringLiteral:
      val = GeneralLiteral();
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
          {if (true) return val;}
    throw new Error("Missing return statement in function");
  }

  static final public String GeneralLiteral() throws ParseException {
                Token t;
    t = jj_consume_token(CharacterStringLiteral);
                String litVal = ""; // strip leading and trailing single quotes
                if (t.image.length() > 2)
                {
                        litVal = t.image.substring(1, t.image.length() - 1);
                }

                {if (true) return litVal;}
    throw new Error("Missing return statement in function");
  }

/*************************************************************************
 *   COLUMN NAMING
 ************************************************************************/

// a column name identifies a column
  static final public String ColumnName() throws ParseException {
        Token t;
        String name = "";
    // MODIFIED FROM STANDARD
            // The standard version would have:
            // 
            // <Identifier>
            // 
            // but we have added support for a subset of valid XML identifiers.
            // 
            // See http://www.w3.org/TR/1998/REC-xml-19980210#NT-Name
            // We allow forward slashes in XML element names to specify hierarchy
            // and @ symbols to specify attributes as opposed to elements
            //
            // This allows statements like:
            //      "SELECT Book/Author/FirstName FROM foo"
            // and
            //      "SELECT Book/Author/FirstName/@languageId FROM foo"
            // and
            //      "SELECT _ElementName FROM FOO"
            //
            t = jj_consume_token(Identifier);
                           name += t.image;
    label_2:
    while (true) {
      if (jj_2_1(2)) {
        ;
      } else {
        break label_2;
      }
      jj_consume_token(Solidus);
      t = jj_consume_token(Identifier);
                                                                    name += "/" + t.image;
    }
    if (jj_2_2(2)) {
      jj_consume_token(Solidus);
      jj_consume_token(AtSign);
      t = jj_consume_token(Identifier);
                                                                     name += "/@" + t.image;
    } else {
      ;
    }
          {if (true) return name;}
    throw new Error("Missing return statement in function");
  }

// a column reference is a reference to a column in a table
// (or a table alias). For example: MyTable.age would refer
// to the column named 'age' in the table named 'MyTable'
// Note that the qualifier is optional
  static final public void ColumnReference() throws ParseException {
         /*@bgen(jjtree) ColumnReference */
                ASTColumnReference jjtn000 = new ASTColumnReference(JJTCOLUMNREFERENCE);
                boolean jjtc000 = true;
                jjtree.openNodeScope(jjtn000);String table = null;
                String colName = null;
    try {
      if (jj_2_3(3)) {
        table = ColumnNameQualifier();
                                                       jjtn000.setTable(table);
        jj_consume_token(Period);
      } else {
        ;
      }
      colName = ColumnName();
                                 jjtree.closeNodeScope(jjtn000, true);
                                 jjtc000 = false;
                                 jjtn000.setColumn(colName);
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public String ColumnNameQualifier() throws ParseException {
        String tab = null;
    tab = TableName();
          {if (true) return tab;}
    throw new Error("Missing return statement in function");
  }

/*************************************************************************
 *   TABLE NAMING
 ************************************************************************/

// a table name identifies a table
  static final public String TableName() throws ParseException {
        Token t;
    t = jj_consume_token(Identifier);
          {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

// a correlation name identifies a local "alias" for a table
// for example "SELECT M.name from MyTable M"
  static final public String CorrelationName() throws ParseException {
        Token t;
    t = jj_consume_token(Identifier);
          {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

/*************************************************************************
 *   TABLE REFERENCES (Sec. 6.3, p. 94)
 ************************************************************************/
  static final public void TableReference() throws ParseException {
 /*@bgen(jjtree) TableReference */
        ASTTableReference jjtn000 = new ASTTableReference(JJTTABLEREFERENCE);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);String table;
        String alias;
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case Identifier:
        table = TableName();
                                        jjtn000.setTable(table);
        break;
      case FileSpecification:
        FileSpec();
        break;
      default:
        jj_la1[9] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case Identifier:
        alias = CorrelationName();
                                      jjtn000.setAlias(alias);
        break;
      default:
        jj_la1[10] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void FileSpec() throws ParseException {
         /*@bgen(jjtree) FileSpec */
                ASTFileSpec jjtn000 = new ASTFileSpec(JJTFILESPEC);
                boolean jjtc000 = true;
                jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(FileSpecification);
                  jjtree.closeNodeScope(jjtn000, true);
                  jjtc000 = false;
                        jjtn000.setValue(t.image);
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void ParameterSpecification() throws ParseException {
         /*@bgen(jjtree) ParameterSpecification */
                ASTParameterSpecification jjtn000 = new ASTParameterSpecification(JJTPARAMETERSPECIFICATION);
                boolean jjtc000 = true;
                jjtree.openNodeScope(jjtn000);Token t;
                String indicator;
    try {
      // we have to differentiate between a free-standing parameter name and
              // a parameter name followed by an indicator
              t = jj_consume_token(ParameterName);
                              jjtree.closeNodeScope(jjtn000, true);
                              jjtc000 = false;
                              jjtn000.setName(t.image);
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   QUERY SPECIFICATION (Sec. 7.9, p. 155)
 ************************************************************************/
  static final public int SetQuantifier() throws ParseException {
                int type;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case DISTINCT:
      jj_consume_token(DISTINCT);
                       type = ASTQuerySpecification.DISTINCT;
      break;
    case ALL:
      jj_consume_token(ALL);
                       type = ASTQuerySpecification.ALL;
          {if (true) return type;}
      break;
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

// specify a table derived from the result of a table expression
  static final public void QuerySpecification() throws ParseException {
         /*@bgen(jjtree) QuerySpecification */
                ASTQuerySpecification jjtn000 = new ASTQuerySpecification(JJTQUERYSPECIFICATION);
                boolean jjtc000 = true;
                jjtree.openNodeScope(jjtn000);int type;
    try {
      jj_consume_token(SELECT);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ALL:
      case DISTINCT:
        type = SetQuantifier();
                                           jjtn000.setType(type);
        break;
      default:
        jj_la1[12] = jj_gen;
        ;
      }
      SelectList();
      TableExpression();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case WHERE:
        WhereClause();
        break;
      default:
        jj_la1[13] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case GROUP:
        GroupByClause();
        break;
      default:
        jj_la1[14] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case HAVING:
        HavingClause();
        break;
      default:
        jj_la1[15] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void SelectList() throws ParseException {
                                 /*@bgen(jjtree) SelectList */
  ASTSelectList jjtn000 = new ASTSelectList(JJTSELECTLIST);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case Asterisk:
        jj_consume_token(Asterisk);
                     jjtree.closeNodeScope(jjtn000, true);
                     jjtc000 = false;
                     jjtn000.setAllColumns(true);
        break;
      case Identifier:
      case ParameterName:
      case ExactNumericLiteral:
      case ApproximateNumericLiteral:
      case CharacterStringLiteral:
      case PlusSign:
      case MinusSign:
        DerivedColumn();
        label_3:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case Comma:
            ;
            break;
          default:
            jj_la1[16] = jj_gen;
            break label_3;
          }
          jj_consume_token(Comma);
          DerivedColumn();
        }
        break;
      default:
        jj_la1[17] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

// A derived column is not necessarily a table column. It refers
// to a "virtual" result column. For example, if the statement
// said "SELECT ColumnA, MAX(ColumnB), ColumnC + 10, 200 * 3.14 FROM FOO",
// there would be a derived column entry for each entry in the select
// list (only one of which corresponds to an unadulterated column value).
  static final public void DerivedColumn() throws ParseException {
 /*@bgen(jjtree) DerivedColumn */
        ASTDerivedColumn jjtn000 = new ASTDerivedColumn(JJTDERIVEDCOLUMN);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);Object columnValue;
        String alias;
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ExactNumericLiteral:
      case ApproximateNumericLiteral:
      case CharacterStringLiteral:
      case PlusSign:
      case MinusSign:
        Literal();
        break;
      case Identifier:
        ColumnReference();
        break;
      case ParameterName:
        ParameterSpecification();
        break;
      default:
        jj_la1[18] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AS:
      case Identifier:
        alias = AsClause();
                  jjtn000.setColumnAlias(alias);
        break;
      default:
        jj_la1[19] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public String AsClause() throws ParseException {
        Token t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case AS:
      jj_consume_token(AS);
      break;
    default:
      jj_la1[20] = jj_gen;
      ;
    }
    t = jj_consume_token(Identifier);
          {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

/*************************************************************************
 *   TABLE EXPRESSION (Sec. 7.3, p. 142)
 ************************************************************************/
  static final public void TableExpression() throws ParseException {
                          /*@bgen(jjtree) TableExpression */
  ASTTableExpression jjtn000 = new ASTTableExpression(JJTTABLEEXPRESSION);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      FromClause();
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   FROM CLAUSE (Sec. 7.4, p. 143)
 ************************************************************************/
  static final public void FromClause() throws ParseException {
                     /*@bgen(jjtree) FromClause */
  ASTFromClause jjtn000 = new ASTFromClause(JJTFROMCLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(FROM);
      TableReference();
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case Comma:
          ;
          break;
        default:
          jj_la1[21] = jj_gen;
          break label_4;
        }
        jj_consume_token(Comma);
        TableReference();
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   WHERE CLAUSE (Sec. 7.6, p. 150)
 ************************************************************************/
  static final public void WhereClause() throws ParseException {
                      /*@bgen(jjtree) WhereClause */
  ASTWhereClause jjtn000 = new ASTWhereClause(JJTWHERECLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(WHERE);
      SearchCondition();
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   GROUP BY CLAUSE (Sec. 7.7, p. 151)
 ************************************************************************/
  static final public void GroupByClause() throws ParseException {
                        /*@bgen(jjtree) GroupByClause */
  ASTGroupByClause jjtn000 = new ASTGroupByClause(JJTGROUPBYCLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(GROUP);
      jj_consume_token(BY);
      GroupingColumnReferenceList();
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void GroupingColumnReferenceList() throws ParseException {
    GroupingColumnReference();
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case Comma:
        ;
        break;
      default:
        jj_la1[22] = jj_gen;
        break label_5;
      }
      jj_consume_token(Comma);
      GroupingColumnReference();
    }
  }

  static final public void GroupingColumnReference() throws ParseException {
    ColumnReference();
  }

/*************************************************************************
 *   HAVING CLAUSE (Sec. 7.8, p. 153)
 ************************************************************************/
  static final public void HavingClause() throws ParseException {
                       /*@bgen(jjtree) HavingClause */
  ASTHavingClause jjtn000 = new ASTHavingClause(JJTHAVINGCLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(HAVING);
      SearchCondition();
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   SEARCH CONDITION (Sec. 8.12, p. 188)
 ************************************************************************/

/*
 ***** commented out because PSConditional objects don't map easily to
       boolean operator trees
   void SearchCondition() #void : {}
   {
      BooleanTerm() ( <OR> BooleanTerm() )*
   }

 *****
*/

// this is the PSConditional-compatible version of SearchCondition
  static final public void SearchCondition() throws ParseException {
                Token t;
                ASTBooleanFactor lastFactor;
    lastFactor = BooleanFactor();
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
      case OR:
        ;
        break;
      default:
        jj_la1[23] = jj_gen;
        break label_6;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        t = jj_consume_token(AND);
        break;
      case OR:
        t = jj_consume_token(OR);
        break;
      default:
        jj_la1[24] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                         lastFactor.setOp(t.image);
      lastFactor = BooleanFactor();
    }
  }

// this is orphaned...it will make a class but no one refers to it as long as
// the original version of SearchCondition is commented out
  static final public void BooleanTerm() throws ParseException {
                      /*@bgen(jjtree) BooleanTerm */
  ASTBooleanTerm jjtn000 = new ASTBooleanTerm(JJTBOOLEANTERM);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      BooleanFactor();
      label_7:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case AND:
          ;
          break;
        default:
          jj_la1[25] = jj_gen;
          break label_7;
        }
        jj_consume_token(AND);
        BooleanFactor();
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public ASTBooleanFactor BooleanFactor() throws ParseException {
                                    /*@bgen(jjtree) BooleanFactor */
  ASTBooleanFactor jjtn000 = new ASTBooleanFactor(JJTBOOLEANFACTOR);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        jj_consume_token(NOT);
                  jjtn000.setNegated(true);
        break;
      default:
        jj_la1[26] = jj_gen;
        ;
      }
      BooleanTest();
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
          {if (true) return jjtn000;}
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
    throw new Error("Missing return statement in function");
  }

  static final public void BooleanTest() throws ParseException {
    BooleanPrimary();
  }

  static final public void BooleanPrimary() throws ParseException {
    if (jj_2_4(4)) {
      Predicate();
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LeftParen:
        jj_consume_token(LeftParen);
        SearchCondition();
        jj_consume_token(RightParen);
        break;
      default:
        jj_la1[27] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
  }

/*************************************************************************
 *   PREDICATE (Sec. 8.1, p. 167)
 ************************************************************************/
  static final public void Predicate() throws ParseException {
    if (jj_2_5(2147483647)) {
      LikePredicate();
    } else if (jj_2_6(2147483647)) {
      BetweenPredicate();
    } else if (jj_2_7(2147483647)) {
      NullPredicate();
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NULL:
      case Identifier:
      case ParameterName:
      case ExactNumericLiteral:
      case ApproximateNumericLiteral:
      case CharacterStringLiteral:
      case PlusSign:
      case MinusSign:
        ComparisonPredicate();
        break;
      default:
        jj_la1[28] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
  }

/*************************************************************************
 *   COMPARISON PREDICATE (Sec. 8.2, p. 169)
 ************************************************************************/
  static final public void ComparisonPredicate() throws ParseException {
         /*@bgen(jjtree) ComparisonPredicate */
                ASTComparisonPredicate jjtn000 = new ASTComparisonPredicate(JJTCOMPARISONPREDICATE);
                boolean jjtc000 = true;
                jjtree.openNodeScope(jjtn000);int compType;
    try {
      RowValueConstructor();
      compType = CompOp();
      RowValueConstructor();
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
          jjtn000.setOperator(compType);
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public int CompOp() throws ParseException {
                int compType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case EqualsOperator:
      jj_consume_token(EqualsOperator);
                           compType = ASTComparisonPredicate.EQ;
      break;
    case NotEqualsOperator:
      jj_consume_token(NotEqualsOperator);
                                compType = ASTComparisonPredicate.NEQ;
      break;
    case LessThanOperator:
      jj_consume_token(LessThanOperator);
                               compType = ASTComparisonPredicate.LT;
      break;
    case GreaterThanOperator:
      jj_consume_token(GreaterThanOperator);
                                  compType = ASTComparisonPredicate.GT;
      break;
    case LessThanOrEqualsOperator:
      jj_consume_token(LessThanOrEqualsOperator);
                                       compType = ASTComparisonPredicate.LTE;
      break;
    case GreaterThanOrEqualsOperator:
      jj_consume_token(GreaterThanOrEqualsOperator);
                                          compType = ASTComparisonPredicate.GTE;
      break;
    default:
      jj_la1[29] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
          {if (true) return compType;}
    throw new Error("Missing return statement in function");
  }

/*************************************************************************
 *   BETWEEN PREDICATE (Sec. 8.3, p. 172)
 ************************************************************************/
  static final public void BetweenPredicate() throws ParseException {
                           /*@bgen(jjtree) BetweenPredicate */
  ASTBetweenPredicate jjtn000 = new ASTBetweenPredicate(JJTBETWEENPREDICATE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      RowValueConstructor();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        jj_consume_token(NOT);
                                        jjtn000.setNegated(true);
        break;
      default:
        jj_la1[30] = jj_gen;
        ;
      }
      jj_consume_token(BETWEEN);
      RowValueConstructor();
      jj_consume_token(AND);
      RowValueConstructor();
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   LIKE PREDICATE (Sec. 8.5, p. 175)
 ************************************************************************/
  static final public void LikePredicate() throws ParseException {
                        /*@bgen(jjtree) LikePredicate */
  ASTLikePredicate jjtn000 = new ASTLikePredicate(JJTLIKEPREDICATE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      DerivedColumn();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        jj_consume_token(NOT);
                                  jjtn000.setNegated(true);
        break;
      default:
        jj_la1[31] = jj_gen;
        ;
      }
      jj_consume_token(LIKE);
      DerivedColumn();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ESCAPE:
        EscapeClause();
        break;
      default:
        jj_la1[32] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void EscapeClause() throws ParseException {
                       /*@bgen(jjtree) EscapeClause */
  ASTEscapeClause jjtn000 = new ASTEscapeClause(JJTESCAPECLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(ESCAPE);
      ColumnReference();
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   NULL PREDICATE (Sec. 8.6, p. 178)
 ************************************************************************/
  static final public void NullPredicate() throws ParseException {
                        /*@bgen(jjtree) NullPredicate */
  ASTNullPredicate jjtn000 = new ASTNullPredicate(JJTNULLPREDICATE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      ColumnReference();
      jj_consume_token(IS);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        jj_consume_token(NOT);
                                         jjtn000.setNegated(true);
        break;
      default:
        jj_la1[33] = jj_gen;
        ;
      }
      jj_consume_token(NULL);
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

/*************************************************************************
 *   ROW VALUE CONSTRUCTOR (Sec. 7.1, p. 139)
 ************************************************************************/
  static final public void RowValueConstructor() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case Identifier:
    case ParameterName:
    case ExactNumericLiteral:
    case ApproximateNumericLiteral:
    case CharacterStringLiteral:
    case PlusSign:
    case MinusSign:
      DerivedColumn();
      break;
    case NULL:
      NullSpecification();
      break;
    default:
      jj_la1[34] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  static final public void NullSpecification() throws ParseException {
    jj_consume_token(NULL);
  }

/*************************************************************************
 *   COLLATE CLAUSE (Sec. 10.5, p. 207)
 ************************************************************************/
  static final public String CollateClause() throws ParseException {
                String coll = null;
    jj_consume_token(COLLATE);
    coll = CollationName();
          {if (true) return coll;}
    throw new Error("Missing return statement in function");
  }

  static final public String CollationName() throws ParseException {
                Token t;
    t = jj_consume_token(Identifier);
          {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

/*************************************************************************
 *   ORDER BY CLAUSE (Sec. 13.1, p. 307)
 ************************************************************************/
  static final public void OrderByClause() throws ParseException {
                        /*@bgen(jjtree) OrderByClause */
  ASTOrderByClause jjtn000 = new ASTOrderByClause(JJTORDERBYCLAUSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(ORDER);
      jj_consume_token(BY);
      SortSpecificationList();
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void SortSpecificationList() throws ParseException {
    SortSpecification();
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case Comma:
        ;
        break;
      default:
        jj_la1[35] = jj_gen;
        break label_8;
      }
      jj_consume_token(Comma);
      SortSpecification();
    }
  }

  static final public void SortSpecification() throws ParseException {
         /*@bgen(jjtree) SortSpecification */
                ASTSortSpecification jjtn000 = new ASTSortSpecification(JJTSORTSPECIFICATION);
                boolean jjtc000 = true;
                jjtree.openNodeScope(jjtn000);int spec = ASC;
                String coll = null;
    try {
      SortKey();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COLLATE:
        coll = CollateClause();
                                              jjtn000.setCollationName(coll);
        break;
      default:
        jj_la1[36] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASC:
      case DESC:
        spec = OrderingSpecification();
                                                   jjtn000.setType(spec);
        break;
      default:
        jj_la1[37] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void SortKey() throws ParseException {
         /*@bgen(jjtree) SortKey */
                ASTSortKey jjtn000 = new ASTSortKey(JJTSORTKEY);
                boolean jjtc000 = true;
                jjtree.openNodeScope(jjtn000);Token t;
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case Identifier:
        ColumnReference();
        break;
      case ExactNumericLiteral:
        t = jj_consume_token(ExactNumericLiteral);
                                      jjtree.closeNodeScope(jjtn000, true);
                                      jjtc000 = false;
                                      jjtn000.setOrdinal(Integer.parseInt(t.image));
        break;
      default:
        jj_la1[38] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public int OrderingSpecification() throws ParseException {
                int spec = ASTSortSpecification.ASC;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ASC:
      jj_consume_token(ASC);
                spec = ASTSortSpecification.ASC;
      break;
    case DESC:
      jj_consume_token(DESC);
                                                              spec = ASTSortSpecification.DESC;
      break;
    default:
      jj_la1[39] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
          {if (true) return spec;}
    throw new Error("Missing return statement in function");
  }

  static final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_1();
    jj_save(0, xla);
    return retval;
  }

  static final private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_2();
    jj_save(1, xla);
    return retval;
  }

  static final private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_3();
    jj_save(2, xla);
    return retval;
  }

  static final private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_4();
    jj_save(3, xla);
    return retval;
  }

  static final private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_5();
    jj_save(4, xla);
    return retval;
  }

  static final private boolean jj_2_6(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_6();
    jj_save(5, xla);
    return retval;
  }

  static final private boolean jj_2_7(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_7();
    jj_save(6, xla);
    return retval;
  }

  static final private boolean jj_3R_56() {
    if (jj_scan_token(ExactNumericLiteral)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_12() {
    if (jj_3R_22()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_23()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(BETWEEN)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_22()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(AND)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_22()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3_3() {
    if (jj_3R_9()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(Period)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_55() {
    if (jj_scan_token(ApproximateNumericLiteral)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_52() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_55()) {
    jj_scanpos = xsp;
    if (jj_3R_56()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_24() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_3()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_34()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3_1() {
    if (jj_scan_token(Solidus)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(Identifier)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3_2() {
    if (jj_scan_token(Solidus)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(AtSign)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(Identifier)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_45() {
    if (jj_scan_token(GreaterThanOrEqualsOperator)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_44() {
    if (jj_scan_token(LessThanOrEqualsOperator)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_21() {
    if (jj_3R_31()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_43() {
    if (jj_scan_token(GreaterThanOperator)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_42() {
    if (jj_scan_token(LessThanOperator)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_54() {
    if (jj_scan_token(MinusSign)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_41() {
    if (jj_scan_token(NotEqualsOperator)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_53() {
    if (jj_scan_token(PlusSign)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_51() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_53()) {
    jj_scanpos = xsp;
    if (jj_3R_54()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_40() {
    if (jj_scan_token(EqualsOperator)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_35() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_40()) {
    jj_scanpos = xsp;
    if (jj_3R_41()) {
    jj_scanpos = xsp;
    if (jj_3R_42()) {
    jj_scanpos = xsp;
    if (jj_3R_43()) {
    jj_scanpos = xsp;
    if (jj_3R_44()) {
    jj_scanpos = xsp;
    if (jj_3R_45()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_34() {
    if (jj_scan_token(Identifier)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_1()) { jj_scanpos = xsp; break; }
      if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    }
    xsp = jj_scanpos;
    if (jj_3_2()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_49() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_51()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_52()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_26() {
    if (jj_3R_22()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_35()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_22()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3_7() {
    if (jj_3R_13()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_37() {
    if (jj_scan_token(ParameterName)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3_6() {
    if (jj_3R_12()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3_5() {
    if (jj_3R_11()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_18() {
    if (jj_3R_26()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_17() {
    if (jj_3R_13()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_16() {
    if (jj_3R_12()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_15() {
    if (jj_3R_11()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_10() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_15()) {
    jj_scanpos = xsp;
    if (jj_3R_16()) {
    jj_scanpos = xsp;
    if (jj_3R_17()) {
    jj_scanpos = xsp;
    if (jj_3R_18()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3_4() {
    if (jj_3R_10()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_25() {
    if (jj_scan_token(NOT)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_48() {
    if (jj_scan_token(AS)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_50() {
    if (jj_scan_token(CharacterStringLiteral)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_39() {
    if (jj_scan_token(NULL)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_38() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_48()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(Identifier)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_30() {
    if (jj_3R_38()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_33() {
    if (jj_3R_39()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_32() {
    if (jj_3R_19()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_29() {
    if (jj_3R_37()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_22() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_32()) {
    jj_scanpos = xsp;
    if (jj_3R_33()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_28() {
    if (jj_3R_24()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_27() {
    if (jj_3R_36()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_20() {
    if (jj_scan_token(NOT)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_47() {
    if (jj_3R_50()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_46() {
    if (jj_3R_49()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_23() {
    if (jj_scan_token(NOT)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_13() {
    if (jj_3R_24()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(IS)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_25()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(NULL)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_14() {
    if (jj_scan_token(Identifier)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_19() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_27()) {
    jj_scanpos = xsp;
    if (jj_3R_28()) {
    jj_scanpos = xsp;
    if (jj_3R_29()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    xsp = jj_scanpos;
    if (jj_3R_30()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_36() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_46()) {
    jj_scanpos = xsp;
    if (jj_3R_47()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_31() {
    if (jj_scan_token(ESCAPE)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_24()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_11() {
    if (jj_3R_19()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_20()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(LIKE)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_19()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    xsp = jj_scanpos;
    if (jj_3R_21()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static final private boolean jj_3R_9() {
    if (jj_3R_14()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static private boolean jj_initialized_once = false;
  static public SQLParserTokenManager token_source;
  static ASCII_CharStream jj_input_stream;
  static public Token token, jj_nt;
  static private int jj_ntk;
  static private Token jj_scanpos, jj_lastpos;
  static private int jj_la;
  static public boolean lookingAhead = false;
  static private boolean jj_semLA;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[40];
  static final private int[] jj_la1_0 = {0x0,0x0,0x4000,0x0,0x0,0x0,0x0,0x0,0x0,0x400,0x0,0x4000,0x4000,0x0,0x0,0x0,0x0,0x0,0x0,0x100000,0x100000,0x0,0x0,0x20000,0x20000,0x20000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x200000,0x0,0x200000,};
  static final private int[] jj_la1_1 = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x400,0x0,0x0,0x0,};
  static final private int[] jj_la1_2 = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x4000,0x4000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x200000,0x0,0x0,0x0,0x0,0x200,0x0,0x200,};
  static final private int[] jj_la1_3 = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x800,0x1000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,};
  static final private int[] jj_la1_4 = {0x80000000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x40000000,0x40000000,0x0,0x100000,0x0,0x200000,0x0,0x100000,0x100000,0x0,0x100000,0x200000,0x0,0x0,0x0,0x0,0x0,};
  static final private int[] jj_la1_5 = {0x0,0x8000000,0x0,0x8000000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,};
  static final private int[] jj_la1_6 = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,};
  static final private int[] jj_la1_7 = {0x0,0x0,0x0,0x0,0x0,0x0,0x18000000,0x18000000,0x18000000,0x400000,0x400000,0x0,0x0,0x100,0x0,0x0,0x0,0x19400000,0x19400000,0x400000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x19400000,0x0,0x0,0x0,0x0,0x0,0x19400000,0x0,0x0,0x0,0x8400000,0x0,};
  static final private int[] jj_la1_8 = {0x0,0x4000,0x0,0x4000,0xa0000,0xa0000,0x0,0xa0002,0x2,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x40000,0xb0002,0xa0002,0x0,0x0,0x40000,0x40000,0x0,0x0,0x0,0x0,0x4000,0xa0002,0x39c,0x0,0x0,0x0,0x0,0xa0002,0x40000,0x0,0x0,0x0,0x0,};
  static final private JJCalls[] jj_2_rtns = new JJCalls[7];
  static private boolean jj_rescan = false;
  static private int jj_gc = 0;

  public SQLParser(java.io.InputStream stream) {
    if (jj_initialized_once) {
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new SQLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 40; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 40; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public SQLParser(java.io.Reader stream) {
    if (jj_initialized_once) {
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new SQLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 40; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 40; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public SQLParser(SQLParserTokenManager tm) {
    if (jj_initialized_once) {
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 40; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(SQLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 40; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    return (jj_scanpos.kind != kind);
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector jj_expentries = new java.util.Vector();
  static private int[] jj_expentry;
  static private int jj_kind = -1;
  static private int[] jj_lasttokens = new int[100];
  static private int jj_endpos;

  static private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements();) {
        int[] oldentry = (int[])(e.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  static final public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[285];
    for (int i = 0; i < 285; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 40; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
          if ((jj_la1_2[i] & (1<<j)) != 0) {
            la1tokens[64+j] = true;
          }
          if ((jj_la1_3[i] & (1<<j)) != 0) {
            la1tokens[96+j] = true;
          }
          if ((jj_la1_4[i] & (1<<j)) != 0) {
            la1tokens[128+j] = true;
          }
          if ((jj_la1_5[i] & (1<<j)) != 0) {
            la1tokens[160+j] = true;
          }
          if ((jj_la1_6[i] & (1<<j)) != 0) {
            la1tokens[192+j] = true;
          }
          if ((jj_la1_7[i] & (1<<j)) != 0) {
            la1tokens[224+j] = true;
          }
          if ((jj_la1_8[i] & (1<<j)) != 0) {
            la1tokens[256+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 285; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

  static final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 7; i++) {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
            case 6: jj_3_7(); break;
          }
        }
        p = p.next;
      } while (p != null);
    }
    jj_rescan = false;
  }

  static final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}

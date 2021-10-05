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

public interface SQLParserTreeConstants
{
  public int JJTSTATEMENTROOT = 0;
  public int JJTVOID = 1;
  public int JJTDIRECTSQLDATASTATEMENT = 2;
  public int JJTDIRECTSELECTSTATEMENTMULTIPLEROWS = 3;
  public int JJTLITERAL = 4;
  public int JJTCOLUMNREFERENCE = 5;
  public int JJTTABLEREFERENCE = 6;
  public int JJTFILESPEC = 7;
  public int JJTPARAMETERSPECIFICATION = 8;
  public int JJTQUERYSPECIFICATION = 9;
  public int JJTSELECTLIST = 10;
  public int JJTDERIVEDCOLUMN = 11;
  public int JJTTABLEEXPRESSION = 12;
  public int JJTFROMCLAUSE = 13;
  public int JJTWHERECLAUSE = 14;
  public int JJTGROUPBYCLAUSE = 15;
  public int JJTHAVINGCLAUSE = 16;
  public int JJTBOOLEANTERM = 17;
  public int JJTBOOLEANFACTOR = 18;
  public int JJTCOMPARISONPREDICATE = 19;
  public int JJTBETWEENPREDICATE = 20;
  public int JJTLIKEPREDICATE = 21;
  public int JJTESCAPECLAUSE = 22;
  public int JJTNULLPREDICATE = 23;
  public int JJTORDERBYCLAUSE = 24;
  public int JJTSORTSPECIFICATION = 25;
  public int JJTSORTKEY = 26;


  public String[] jjtNodeName = {
    "StatementRoot",
    "void",
    "DirectSQLDataStatement",
    "DirectSelectStatementMultipleRows",
    "Literal",
    "ColumnReference",
    "TableReference",
    "FileSpec",
    "ParameterSpecification",
    "QuerySpecification",
    "SelectList",
    "DerivedColumn",
    "TableExpression",
    "FromClause",
    "WhereClause",
    "GroupByClause",
    "HavingClause",
    "BooleanTerm",
    "BooleanFactor",
    "ComparisonPredicate",
    "BetweenPredicate",
    "LikePredicate",
    "EscapeClause",
    "NullPredicate",
    "OrderByClause",
    "SortSpecification",
    "SortKey",
  };
}

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

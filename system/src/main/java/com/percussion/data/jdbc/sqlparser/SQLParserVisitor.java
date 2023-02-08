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

public interface SQLParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTStatementRoot node, Object data);
  public Object visit(ASTDirectSQLDataStatement node, Object data);
  public Object visit(ASTDirectSelectStatementMultipleRows node, Object data);
  public Object visit(ASTLiteral node, Object data);
  public Object visit(ASTColumnReference node, Object data);
  public Object visit(ASTTableReference node, Object data);
  public Object visit(ASTFileSpec node, Object data);
  public Object visit(ASTParameterSpecification node, Object data);
  public Object visit(ASTQuerySpecification node, Object data);
  public Object visit(ASTSelectList node, Object data);
  public Object visit(ASTDerivedColumn node, Object data);
  public Object visit(ASTTableExpression node, Object data);
  public Object visit(ASTFromClause node, Object data);
  public Object visit(ASTWhereClause node, Object data);
  public Object visit(ASTGroupByClause node, Object data);
  public Object visit(ASTHavingClause node, Object data);
  public Object visit(ASTBooleanTerm node, Object data);
  public Object visit(ASTBooleanFactor node, Object data);
  public Object visit(ASTComparisonPredicate node, Object data);
  public Object visit(ASTBetweenPredicate node, Object data);
  public Object visit(ASTLikePredicate node, Object data);
  public Object visit(ASTEscapeClause node, Object data);
  public Object visit(ASTNullPredicate node, Object data);
  public Object visit(ASTOrderByClause node, Object data);
  public Object visit(ASTSortSpecification node, Object data);
  public Object visit(ASTSortKey node, Object data);
}

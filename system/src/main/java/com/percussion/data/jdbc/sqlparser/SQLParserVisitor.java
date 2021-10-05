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

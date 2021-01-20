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

package com.percussion.data.jdbc.sqlparser;

public class ASTBooleanFactor extends SimpleNode {
  public ASTBooleanFactor(int id) {
    super(id);
  }

  public ASTBooleanFactor(SQLParser p, int id) {
    super(p, id);
  }

  public void setNegated(boolean neg)
  {
     m_negated = neg;
  }

  public boolean isNegated()
  {
     return m_negated;
  }

  /**
   * @author  chad loder
   * 
   * @version 1.0 1999/6/10
   * 
   * Set the boolean linking op for the next conditional in the chain, if
   * there is one
   * 
   * @param   op
   * 
   */
  public void setOp(String op)
  {
      m_op = op;
  }

  public String getOp()
  {
     return m_op;
  }

  private boolean m_negated = false;
  private String m_op = "AND";
  /** Accept the visitor. **/
  public Object jjtAccept(SQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}

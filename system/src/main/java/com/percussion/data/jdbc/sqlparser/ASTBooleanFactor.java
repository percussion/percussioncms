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

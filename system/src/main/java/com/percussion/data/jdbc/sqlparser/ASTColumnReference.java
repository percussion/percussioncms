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

public class ASTColumnReference extends SimpleNode {
  public ASTColumnReference(int id) {
    super(id);
  }

  public ASTColumnReference(SQLParser p, int id) {
    super(p, id);
  }

  public void setTable(String table)
  {
     m_table = table;
  }

  public String getTable()
  {
     return m_table;
  }

  public void setColumn(String column)
  {
     m_column = column;
  }

  public String getColumn()
  {
     return m_column;
  }

  private String m_column = null;
  private String m_table = null;

  /** Accept the visitor. **/
  public Object jjtAccept(SQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}

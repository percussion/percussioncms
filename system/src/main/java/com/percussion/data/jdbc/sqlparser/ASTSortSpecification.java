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

public class ASTSortSpecification extends SimpleNode {
  public ASTSortSpecification(int id) {
    super(id);
  }

  public ASTSortSpecification(SQLParser p, int id) {
    super(p, id);
  }

  public static final int ASC = 0;
  public static final int DESC = 1;

  public void setCollationName(String coll)
  {
     m_coll = coll;
  }

  public String getCollationName()
  {
     return m_coll;
  }

  public void setType(int type)
  {
     m_type = type;
  }

  public int getType()
  {
     return m_type;
  }

  private int m_type = ASC;
  private String m_coll;

  /** Accept the visitor. **/
  public Object jjtAccept(SQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}

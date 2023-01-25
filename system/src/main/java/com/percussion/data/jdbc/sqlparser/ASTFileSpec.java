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

public class ASTFileSpec extends SimpleNode {
   public ASTFileSpec(int id) {
      super(id);
   }
   
   public ASTFileSpec(SQLParser p, int id) {
      super(p, id);
   }
   
   
   /** Accept the visitor. **/
   public Object jjtAccept(SQLParserVisitor visitor, Object data) {
      return visitor.visit(this, data);
   }
   
   public void setValue(String filePath)
   {
      // remove double quotes from beginning and end
      filePath = filePath.substring(1, filePath.length() - 1);
      if (filePath.endsWith("*"))
      {
         m_isRecursive = true;
         // remove * from end
         filePath = filePath.substring(0, filePath.length() - 1);
      }
      m_filePath = filePath;
   }
   
   public String getValue() { return m_filePath; }
   
   public boolean isRecursive() { return m_isRecursive; }
   
   private String m_filePath;
   private boolean m_isRecursive = false;
}

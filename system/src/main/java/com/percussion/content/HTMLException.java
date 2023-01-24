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
package com.percussion.content;

import org.w3c.dom.DOMException;

public class HTMLException extends DOMException
{
   public HTMLException(short code, String msg)
   {
      super(code, msg);
   }

   public void setLineNumber(int num)
   {
      m_lineNum = num;
   }

   public int getLineNumber()
   {
      return m_lineNum;
   }

   public String getMessage()
   {
      String msg = super.getMessage();
      StringBuilder buf = new StringBuilder(msg.length() + 35);
      buf.append("HTML parse error on line ");
      buf.append(getLineNumber());
      buf.append(": ");
      buf.append(msg);
      return buf.toString();
   }

   private int m_lineNum = -1;
}

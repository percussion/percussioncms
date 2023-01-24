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
package com.percussion.services.contentmgr.impl.query.nodes;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a variable substitution in the query. A variable is a subclass
 * of value.
 * 
 * @author dougrand
 */
public class PSQueryNodeVariable extends PSQueryNodeValue
{   
   /**
    * Ctor
    * @param var
    */
   public PSQueryNodeVariable(String var)
   {
      super(var);
      if (StringUtils.isBlank(var))
      {
         throw new IllegalArgumentException("var may not be null or empty");
      }
   }
   
   public Op getOp()
   {
      return null;
   }

   /**
    * Get the variable name
    * @return the name, never <code>null</code> or empty
    */
   public String getVariable()
   {
      String varname = (String) m_value;
      if (varname.startsWith(":"))
      {
         varname = varname.substring(1);
      }
      return varname;
   }

   @Override
   public String toString()
   {
      return "var(" + getVariable() + ")";
   }
   
   
}

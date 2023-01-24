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
package com.percussion.deployer.jexl;

import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import org.apache.commons.jexl3.parser.ParserVisitor;
import org.apache.commons.jexl3.parser.SimpleNode;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class PSDeployJexlUtils
{
   /**
    * Create an ID visitor that takes the binding string and parses the 
    * expression/script to a tree. Then the visitor will collect all the nodes
    * that can have an integer or a String representation of an integer
    * @param val the expression/script that need to be parsed for ids. May not 
    *        be <code>null</code> or empty
    * @return list of ids from the binding that need to be mapped, never 
    *         <code>null</code>, may be empty
    * @throws PSDeployException
    */
   public static List<String> getIdsFromBinding(String val)
         throws PSDeployException
   {
      if (StringUtils.isBlank(val))
         throw new IllegalArgumentException("expression may not be null");
      
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp;
      try
      {
         psExp = PSJexlParserUtils.createNewExpression(val, true);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getCause(), "unable to create jexl expression from the binding");
      }
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      return ((PSGetIDsJexlVisitor)visitor).getIds();
   }
}

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
package com.percussion.deployer.jexl;

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
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

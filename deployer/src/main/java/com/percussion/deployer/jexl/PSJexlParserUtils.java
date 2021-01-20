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

import org.apache.commons.jexl3.parser.ASTJexlScript;
import org.apache.commons.jexl3.parser.ParseException;
import org.apache.commons.jexl3.parser.Parser;
import org.apache.commons.jexl3.parser.SimpleNode;
import org.apache.commons.jexl3.parser.TokenMgrError;

import java.io.StringReader;

/**
 * A util class for parsing jexl expressions or scripts
 * @author vamsinukala
 *
 */
public class PSJexlParserUtils
{
   /**
    * the jexl parser
    */
   protected static Parser ms_parser = new Parser(new StringReader(";"));

   /**
    * With the JEXL script, parse it 
    * @param scriptText 
    * @return the parsed expression as a simple node
    * @throws Exception any parser exception
    */
   public static PSJexlSimpleNode createScriptNode(String scriptText)
         throws Exception
   {
      SimpleNode script;
      try
      {
         script = ms_parser.parse(null, scriptText, null, false, false);
      }
      catch (TokenMgrError tme)
      {
         throw new ParseException(tme.getMessage());
      }

      if (script instanceof ASTJexlScript)
      {
         return new PSJexlSimpleNode(script, scriptText);
      }
      else
      {
         throw new IllegalStateException("Parsed script is not "
               + "a Jexl Script");
      }
   }

   /**
    * With the JEXL expression, parse it 
    * @param expression 
    * @return the parsed expression as a simple node
    */

   public static PSJexlSimpleNode createNewExpression(final String expression, boolean isBoolean) throws ParseException {
      String expr = expression.trim();
      if (!expr.endsWith(";") && !expr.endsWith("}"))
         expr += ";";

      // Parse the Expression
      SimpleNode tree;
      try
      {
         tree = ms_parser.parse(null, expression, null, false, isBoolean);
      }
      catch (TokenMgrError tme)
      {
         throw new ParseException(tme.getMessage());
      }

      SimpleNode node = (SimpleNode) tree.jjtGetChild(0);

      return new PSJexlSimpleNode(node, expression);

   }
}

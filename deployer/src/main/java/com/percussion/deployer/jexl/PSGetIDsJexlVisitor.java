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

import org.apache.commons.jexl3.parser.ASTNullLiteral;
import org.apache.commons.jexl3.parser.ASTNumberLiteral;
import org.apache.commons.jexl3.parser.ASTStringLiteral;
import org.apache.commons.jexl3.parser.ParserVisitor;
import org.apache.commons.jexl3.parser.SimpleNode;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor for any jexl expression or script. This will call the jexl's parser
 * to build a tree and then walk the tree nodes. It will grab all the string and
 * integer literals that are potential ids for MSM 
 * @author vamsinukala
 * 
 */

public class PSGetIDsJexlVisitor extends PSBaseJexlParserVisitor
{   
   /**
    * The list of ids if any that need to be mapped may be <code>empty</code>
    */
   private List<String> m_ids = new ArrayList<>();

   
   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTStringLiteral, Object)
    */
   @Override
   public Object visit(ASTStringLiteral arg0, Object arg1)
   {
      String value = null;
      try
      {
         value = arg0.getLiteral();
      }
      catch (Exception e)
      {
      }
      if (StringUtils.isNotBlank(value) && StringUtils.isNumeric(value))
         addID(value);
      return arg1;
   }
   
   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTIntegerLiteral, Object)
    */
   public Object visit(ASTNumberLiteral arg0, Object arg1)
   {
      Number value = null;
      try
      {
         value = arg0.getLiteral();
      }
      catch (Exception e)
      { 
      }
      addID(String.valueOf(value));
      return arg1;
   }
   
   
   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTNullLiteral, Object)
    */
   public Object visit(ASTNullLiteral arg0, Object arg1)
   {
      return arg1;
   }

   
   /**
    * Add the add to the list of ids
    * @param id the string representation of a number never <code>null</code>
    */
   public void addID(String id)
   {
      if ( StringUtils.isBlank(id) )
         throw new IllegalArgumentException("id may not be null");
      m_ids.add(id);
   }
   
   /**
    * Util method to print all the ids found in the expression
    */
   public void printIDs()
   {
      int i=0;
      for (String id : m_ids)
         System.out.println("   id["+ i++ +"] = " + id);
   }
   
   /**
    * get the ids that are in the current parsed expression or script
    * @return list of ids as strings
    */
   public List<String> getIds()
   {
      return m_ids;
   }

   /**
    * A util method to visit child nodes
    * @param child the current node never <code>null</code>
    * @param parent the parent node never <code>null</code>
    */
   protected Object doVisit(SimpleNode child, Object parent)
   {
      if ( child == null )
         throw new IllegalArgumentException("SimpleNode cannot be null");
      if ( parent == null )
         throw new IllegalArgumentException("parent node cannot be null");
      
     for (int i = 0; i < child.jjtGetNumChildren(); i++)
     {
        SimpleNode c = child.jjtGetChild(i);
        // visit self and then its children..
        c.jjtAccept(this, parent);
     }
  
      return parent;
   }
   
}

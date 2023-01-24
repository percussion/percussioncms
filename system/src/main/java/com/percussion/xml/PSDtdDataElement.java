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

package com.percussion.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;

/**
 * The PSDtdDataElement denotes a #PCDATA Element
 *   this must extend PSDtdNode is so that it will
 *   be a valid content object to place in a PSDtdElement
 *
 * @see         PSDtdElement
 *
 * @author      David Gennaco
 * @version    1.0
 * @since      1.0
 */
public class PSDtdDataElement extends PSDtdNode
{
   private static final Logger log = LogManager.getLogger(PSDtdDataElement.class);
   /**
    * Construct a PSDtdDataElement
    *
    */
   PSDtdDataElement()
   {
      ;
   }

   /**
    *  Return the name of this node
    *
    *     @return  PCDATA_STRING
    */
   public String getName()
   {
      return PCDATA_STRING;
   }

   /**
    *  print is used for debugging purposes, and checking DTDs manually
    */
   public void print(String tab)
   {
      log.info(tab + PCDATA_STRING);
   }

   /**
    * Add this data element to the catalog list.
    *
    * This function should be overridden for all extended classes.
    *
    *  @param    stack       the recursion detection stack
    *
    *  @param    catalogList the catalog list being built
    *
    *  @param    cur          the current name to expand on
    *
    *  @param    sep          the element separator string
    *
    *  @param    attribId    the string used to identify an attribute entry
    *
    */
   public void catalog(HashMap stack, List catalogList, String cur,
      String sep, String attribId)
   {
      //      if (catalogList.size() >= PSDtdTree.MAX_CATALOG_SIZE)
      //      {
      //         catalogList.add("TRUNCATED!");
      //         return;
      //      }

      //      catalogList.add(cur /* + "<" + PCDATA_STRING + ">" */);
      catalogList.add(cur);
      return;
   }

   public Object acceptVisitor(PSDtdTreeVisitor visitor, Object data)
   {
      return visitor.visit(this, data);
   }

   static final String PCDATA_STRING = "#PCDATA";
}

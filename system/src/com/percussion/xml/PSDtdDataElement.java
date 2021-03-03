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

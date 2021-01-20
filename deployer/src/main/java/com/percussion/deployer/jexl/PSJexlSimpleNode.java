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

import org.apache.commons.jexl3.parser.SimpleNode;
/**
 * 
 * A wrapper for SimpleNode.
 * @author vamsinukala
 *
 */

public class PSJexlSimpleNode
{
   /**
    * A SimpleNode that JEXL expects to parse and Rx does "visit"
    */
   private SimpleNode m_node = null;
   
   private String m_code = null; 
   
   public PSJexlSimpleNode(SimpleNode n, String c)
   {
      m_node = n; 
   }

   /**
    * Accessor for the simple node
    * @return the simple node
    */
   public SimpleNode getNode()
   {
      return m_node;
   }

   /**
    * Accessor for the expression/script
    * @return the expression that this node tree represents
    */
   public String getCode()
   {
      return m_code;
   }   
}

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

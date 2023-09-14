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
package com.percussion.utils.jsr170;


import org.apache.commons.collections4.MultiValuedMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.Map;

/**
 * Implementation of node iterator
 * 
 * @author dougrand
 */
public class PSNodeIterator  extends PSItemIterator<Node>
   implements NodeIterator
{
   /**
    * Ctor for node iterator
    * @param children child map, may not be <code>null</code>
    * @param filter filter, may be <code>null</code> if no filter is needed
    */
   public PSNodeIterator(MultiValuedMap children, String filter) {
      super(children.asMap(), filter);
   }

   public Node nextNode()
   {
      return next();
   }


}

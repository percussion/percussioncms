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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * Pretty print a JSR-170 node tree, mostly useful for debugging purposes
 * @author dougrand
 */
public class PSNodePrettyPrinter
{
   private static final int INDENT = 2;
   
   public static String toString(Node n) throws RepositoryException
   {
      return toString(n, 0);
   }
   
   public static String toString(Node n, int indentation) throws RepositoryException
   {
      StringBuilder b = new StringBuilder();
      for(int i = 0; i < indentation; i++)
      {
         b.append(" ");
      }
      b.append("<h2>Node");
      b.append(n.getName());
      b.append(" uuid: ");
      b.append(n.getUUID());
      b.append("</h2>");
      PropertyIterator piter = n.getProperties();
      while(piter.hasNext())
      {
         b.append("\n");
         b.append(toString(piter.nextProperty(), indentation + INDENT));
      }
      NodeIterator niter = n.getNodes();
      while(niter.hasNext())
      {
         b.append("\n");
         b.append(toString(niter.nextNode(), indentation + INDENT));
      }
      return b.toString();
   }
   
   public static String toString(Property p, int indentation) throws RepositoryException
   {
      StringBuilder b = new StringBuilder();
      for(int i = 0; i < indentation; i++)
      {
         b.append(" ");
      }
      b.append("<h3>Property ");
      b.append(p.getName());
      b.append("</h3>");
      b.append(p.getString());
      return b.toString();
   }
   
   
}

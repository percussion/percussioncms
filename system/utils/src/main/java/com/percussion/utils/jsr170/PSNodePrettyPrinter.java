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
      StringBuffer b = new StringBuffer();
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
      StringBuffer b = new StringBuffer();
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

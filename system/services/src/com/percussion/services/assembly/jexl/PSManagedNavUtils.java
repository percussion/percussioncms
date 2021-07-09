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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Utility methods to aid in managed navigation implementation.
 * 
 * @author dougrand
 */
public class PSManagedNavUtils extends PSJexlUtilBase
{
   /**
    * Search for a non-empty property looking from the given node up the parent
    * tree, used with the special nodes from managed nav as the regular content
    * nodes do not yet implement any real hierarchy.
    * 
    * @param node the content node being examined, if <code>null</code> then
    *           <code>null</code> is returned. The node is a starting place
    *           and will normally be a proxy node created by the managed nav
    *           system. If this node doesn't contain the given property, then
    *           the parent is examined if it is present (not <code>null</code>).
    * @param propertyname the name of the property being searched for, never
    *           <code>null</code> or empty. The property name is a case
    *           sensitive name, which optionally can start with the namespace
    *           identifier "rx:". If this namespace id is not present it will be
    *           prepended. Other namespaces may be used as appropriate. Such
    *           namespaces are documented elsewhere.
    * @return the found property or <code>null</code> if no matching property
    *         is found
    * @throws RepositoryException on error calling <code>getParent</code> on
    *            the node, never expected to be thrown
    * @throws AccessDeniedException on error calling <code>getParent</code> on
    *            the node, never expected to be thrown
    * @throws ItemNotFoundException on error calling <code>getParent</code> on
    *            the node, never expected to be thrown
    */
   @IPSJexlMethod(description = "Starting at the specified node, search up the node parents until a node has the given property with a value", params =
   {
         @IPSJexlParam(name = "node", description = "the starting node"),
         @IPSJexlParam(name = "propertyname", description = "the name of the property")})
   public Property findProperty(Node node, String propertyname)
         throws ItemNotFoundException, AccessDeniedException,
         RepositoryException
   {
      if (StringUtils.isBlank(propertyname))
      {
         throw new IllegalArgumentException(
               "propertyname may not be null or empty");
      }
      while (node != null)
      {
         try
         {
            Property p = node.getProperty(propertyname);
            if (p != null && p.getLength() > 0)
            {
               return p;
            }
         }
         catch (Exception e)
         {
            // Just skip to the next
         }
         node = node.getParent();
      }

      return null;
   }

   /**
    * Search for a defined child node looking from the given node up the parent
    * tree, used with the special nodes from managed nav as the regular content
    * nodes do not yet implement any real hierarchy.
    * 
    * @param node the content node being examined, if <code>null</code> then
    *           <code>null</code> is returned. The node is a starting place
    *           and will normally be a proxy node created by the managed nav
    *           system. If this node doesn't contain the given property, then
    *           the parent is examined if it is present (not <code>null</code>).
    * @param nodename the name of the node being searched for, never
    *           <code>null</code> or empty. The nodename is a case sensitive
    *           name.
    * @return the found child node or <code>null</code> if not found
    * @throws ItemNotFoundException on error calling <code>getParent</code> on
    *            the node, never expected to be thrown
    * @throws AccessDeniedException on error calling <code>getParent</code> on
    *            the node, never expected to be thrown
    * @throws RepositoryException on error calling <code>getParent</code> on
    *            the node, never expected to be thrown
    */
   @IPSJexlMethod(description = "Starting at the specified node, search up the node parents until a node has the given named node as a child", params =
   {
         @IPSJexlParam(name = "node", description = "the starting node"),
         @IPSJexlParam(name = "nodename", description = "the name of the child node")})
   public Node findNode(Node node, String nodename)
         throws ItemNotFoundException, AccessDeniedException,
         RepositoryException
   {
      if (StringUtils.isBlank(nodename))
      {
         throw new IllegalArgumentException("nodename may not be null or empty");
      }
      while (node != null)
      {
         try
         {
            Node c = node.getNode(nodename);
            if (c != null)
            {
               return c;
            }
         }
         catch (Exception e)
         {
            // Just skip to the next
         }
         node = node.getParent();
      }

      return null;
   }
}

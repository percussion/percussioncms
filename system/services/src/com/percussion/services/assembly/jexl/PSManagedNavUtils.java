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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import org.apache.commons.lang.StringUtils;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

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

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
 
package com.percussion.deployer.objectstore.idtypes;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

/**
 * Class to restore {@link PSApplicationIdContext} objects from their XML 
 * state.
 */
public class PSApplicationIDContextFactory
{
   /**
    * Creates an application id context object from its xml representatation.
    * 
    * @param sourceNode the XML element node to populate from, may not  be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>soureNode</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * represent a type supported by the class.
    */
   public static PSApplicationIdContext fromXml(Element sourceNode) 
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      String nodeName = sourceNode.getNodeName();
      PSApplicationIdContext ctx = null;
      if (nodeName.equals(PSAppCEItemIdContext.XML_NODE_NAME))
         ctx = new PSAppCEItemIdContext(sourceNode);
      else if (nodeName.equals(PSAppConditionalIdContext.XML_NODE_NAME))
         ctx = new PSAppConditionalIdContext(sourceNode);
      else if (nodeName.equals(PSAppDataMappingIdContext.XML_NODE_NAME))
         ctx = new PSAppDataMappingIdContext(sourceNode);
      else if (nodeName.equals(PSAppDisplayMapperIdContext.XML_NODE_NAME))
         ctx = new PSAppDisplayMapperIdContext(sourceNode);
      else if (nodeName.equals(PSAppEntryIdContext.XML_NODE_NAME))
         ctx = new PSAppEntryIdContext(sourceNode);
      else if (nodeName.equals(PSAppExtensionCallIdContext.XML_NODE_NAME))
         ctx = new PSAppExtensionCallIdContext(sourceNode);
      else if (nodeName.equals(PSAppExtensionParamIdContext.XML_NODE_NAME))
         ctx = new PSAppExtensionParamIdContext(sourceNode);
      else if (nodeName.equals(PSAppIndexedItemIdContext.XML_NODE_NAME))
         ctx = new PSAppIndexedItemIdContext(sourceNode);
      else if (nodeName.equals(PSAppNamedItemIdContext.XML_NODE_NAME))
         ctx = new PSAppNamedItemIdContext(sourceNode);
      else if (nodeName.equals(PSAppUISetIdContext.XML_NODE_NAME))
         ctx = new PSAppUISetIdContext(sourceNode);
      else if (nodeName.equals(PSAppUrlRequestIdContext.XML_NODE_NAME))
         ctx = new PSAppUrlRequestIdContext(sourceNode);
      else if (nodeName.equals(PSBindingParamIdContext.XML_NODE_NAME))
         ctx = new PSBindingParamIdContext(sourceNode);
      else if (nodeName.equals(PSBindingIdContext.XML_NODE_NAME))
         ctx = new PSBindingIdContext(sourceNode);
      else
      {
         Object[] args = {"PSXApplicationIDContext", nodeName};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      return ctx;   
   }
}

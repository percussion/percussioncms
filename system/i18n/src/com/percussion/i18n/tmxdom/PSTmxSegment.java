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
package com.percussion.i18n.tmxdom;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * This class wraps the functionality of segment DOM element as an easy to use
 * TMX node. The TMX counterpart provides methods to manipulate the segment, the
 * most important one being to merge two nodes appying the merge configuration.
 */
public class PSTmxSegment
   extends PSTmxLeafNode
   implements IPSTmxSegment
{
   /**
    * Constructor. Takes the parent TMX document object and the DOM element
    * representing the segment. The value of the segment is constructed from the
    * supplied DOM element.
    * @param tmxdoc parent TMX document, nust not be <code>null</code>.
    * @param seg DOM element for the segment to be contstructed, must not be
    * <code>null</code>.
    * @throws IllegalArgumentException if tmxDoc or seg is <code>null</code>
    */
   PSTmxSegment(IPSTmxDocument tmxdoc, Element seg)
   {
      if(tmxdoc == null)
         throw new IllegalArgumentException("tmxdoc must not be null");
      if(seg == null)
         throw new IllegalArgumentException("seg must not be null");

      m_PSTmxDocument = tmxdoc;
      m_DOMElement = seg;
      Node node = m_DOMElement.getFirstChild();
      if(node instanceof Text)
         m_Value = ((Text)node).getData();
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void merge(IPSTmxNode node)
      throws PSTmxDomException
   {
      if(node == null)
      {
         throw new IllegalArgumentException("node must not be null for merging");
      }
      else if(!(node instanceof IPSTmxSegment))
      {
         throw new PSTmxDomException("onlyOneTypeAllowedToMerge",
            "IPSTmxSegment");
      }
      IPSTmxSegment seg = (IPSTmxSegment)node;
      //No merge rules below the Segment node. Just replace the value.
      setValue(seg.getValue());
   }
}

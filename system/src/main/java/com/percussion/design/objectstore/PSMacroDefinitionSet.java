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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents a collection of <code>PSMacroDefinition</code> objects.
 */
public class PSMacroDefinitionSet extends PSCollectionComponent
{
   /**
    * Constucts an empty macro set.
    */
   public PSMacroDefinitionSet()
   {
      super(PSMacroDefinition.class);
   }
   
   /**
    * Construct a Java object from its XML representation.
    *
    * @param source the XML element node to construct this object from,
    *    not <code>null</code>, see {@link toXml(Document)} for the expected
    *    XML format.
    * @throws PSUnknownNodeTypeException if the XML element node is not of 
    *    the appropriate type
    */
   public PSMacroDefinitionSet(Element source) throws PSUnknownNodeTypeException
   {
      super(PSMacroDefinition.class);

      fromXml(source, null, null);
   }

   /** 
    * See {@link toXml(Document) for the expected XML format.
    * 
    * @see IPSComponent 
    */
   public void fromXml(Element source, IPSDocument parent, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(source.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element node = null;
      try 
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
         
         clear();
         node = tree.getNextElement(PSMacroDefinition.XML_NODE_NAME, 
            firstFlags);
         while (node != null)
         {
            PSMacroDefinition macro = new PSMacroDefinition(node, null, null);
            add(macro);
            
            node = tree.getNextElement(PSMacroDefinition.XML_NODE_NAME, 
               nextFlags);
         }
      } 
      finally 
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /** 
    * Creates the XML serialization for this class. The structure of the XML 
    * document conforms to this DTD:
    * <pre><code>
    * &lt;!ELEMENT PSXMacroDefinitionSet (PSXMacroDefinition*)&gt;
    * </code></pre>
    * 
    * @see IPSComponent 
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      for (int i=0; i<size(); i++)
      {
         PSMacroDefinition macro = (PSMacroDefinition) get(i);
         root.appendChild(macro.toXml(doc));
      }

      return root;
   }
   
   /**
    * Get the macro definition for the supplied name.
    * 
    * @param name the macro name for which to get the definition, not
    *    <code>null</code> or empty.
    * @return the macro definition if found, <code>null</code> otherwise.
    */
   public PSMacroDefinition getMacroDefinition(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      Iterator defs = iterator();
      while (defs.hasNext())
      {
         PSMacroDefinition def = (PSMacroDefinition) defs.next();
         if (def.getName().equals(name))
            return def;
      }
      
      return null;
   }

   /** The XML node name */
   public static final String XML_NODE_NAME = "PSXMacroDefinitionSet";
}

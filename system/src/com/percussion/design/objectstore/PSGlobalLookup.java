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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Represents a global lookup choice, with {@link PSEntry} children.
 */
public class PSGlobalLookup extends PSComponent
{
   /**
    * Initializes a newly created <code>PSGlobalLookup</code> object, from
    * an XML representation.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode the XML element node to construct this object from.
    *    Cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format
    */
   public PSGlobalLookup(Element sourceNode)
         throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode cannot be null");
      fromXml(sourceNode, null, null);
   }


   /**
    * Initializes a newly created <code>PSGlobalLookup</code> object, with
    * the specified key and name.
    *
    * @param globalKey the RXLOOKUP identifier.  Assumed unique across all
    *                  <code>PSGlobalLookup</code> objects.
    * @param name a descriptive term for this lookup set; cannot be
    *             <code>null</code>
    * @throws IllegalArgumentException if name is <code>null</code>
    */
   public PSGlobalLookup( int globalKey, String name )
   {
      if (null == name)
         throw new IllegalArgumentException("name cannot be null");

      m_globalKey = globalKey;
      m_name = name;
   }

   // TODO: implement
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("Must provide a valid Document");
      Element root = doc.createElement(XML_NODE_NAME);
      return root;
   }

   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode element with name specified by {@link #XML_NODE_NAME}
    * @param parentDoc ignored.
    * @param parentComponents ignored.
    * @throws PSUnknownNodeTypeException  if an expected XML element is missing,
    *    or <code>null</code>
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      validateElementName(sourceNode, XML_NODE_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      // get the required elements
      setGlobalKey(Integer.parseInt(getRequiredElement(tree, "key")));
      setName(getRequiredElement(tree, "name"));

      // get the child PSEntries
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      for (Element e = tree.getNextElement(PSEntry.XML_NODE_NAME, firstFlags);
           e != null;
           e = tree.getNextElement(PSEntry.XML_NODE_NAME, nextFlags))
      {
         m_entries.add(new PSEntry(e, parentDoc, parentComponents));
      }
   }


   /**
    * @return a string representation of the object, its name
    * @see #getName
    */
   public String toString()
   {
      return getName();
   }


   /**
    * Sets the name for this global lookup
    * @param name
    */
   public void setName( String name )
   {
      m_name = name;
   }


   public void setGlobalKey( int globalKey )
   {
      m_globalKey = globalKey;
   }


   public int getGlobalKey()
   {
      return m_globalKey;
   }


   public Iterator getEntries()
   {
      return m_entries.iterator();
   }


   public String getName()
   {
      return m_name;
   }


   public static final String XML_NODE_NAME = "PSXGlobalLookup";

   private int m_globalKey = -1;
   private String m_name;
   private PSCollection m_entries = new PSCollection(PSEntry.class);

}

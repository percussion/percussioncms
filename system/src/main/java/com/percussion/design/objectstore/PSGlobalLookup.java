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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;


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
                       List parentComponents)
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

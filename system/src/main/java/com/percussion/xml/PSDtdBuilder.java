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

package com.percussion.xml;

import com.percussion.util.PSCharSets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to build and write a DTD.  Does not support mixed content
 * models or attributes at this time.
 */
public class PSDtdBuilder
{
   /**
    * Constructor for this class.  Creates a DTD with the specified root
    * element.
    *
    * @param rootName The name of the root.  May not be <code>null</code>
    * or <code>zero</code>-length.
    */
   public PSDtdBuilder(String rootName)
   {
      if (rootName == null || rootName.length() == 0)
         throw new IllegalArgumentException("root may not be null or empty");

      m_root = rootName;
      m_elements.put(rootName, null);
   }

   /**
    * Creates an element with the specified name and appends
    * it to the parent's list of child elements with the specified occurence.
    * The same child element name may be added to multiple parents, using a
    * different occurence if desired.
    *
    * @param name The name of the element.  May not be <code>null</code>.
    * @param occurence The occurence type.  Once of the following:
    * <ol>
    * <li>{@link #OCCURS_ONCE}</li>
    * <li>{@link #OCCURS_OPTIONAL}</li>
    * <li>{@link #OCCURS_ATLEASTONCE}</li>
    * <li>{@link #OCCURS_ANY}</li>
    * </ol>
    * @param parent The child will be added to the parent with this name. May
    * not be <code>null</code> and must already exist in this dtd.
    *
    * @throws IllegalArgumentException if name or parent is
    * <code>null</code>, parent is not an existing element,
    * if name already exists, or if occurence is not a recognized occurence
    * value.
    */
   public void addElement(String name, int occurence, String parent)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");

      if (!m_elements.containsKey(parent))
         throw new IllegalArgumentException(
            "parent must be an existing element");

      String occString = null;
      switch (occurence)
      {
         case OCCURS_ONCE:
            occString = "";
            break;
         case OCCURS_ANY:
            occString = "*";
            break;
         case OCCURS_ATLEASTONCE:
            occString = "+";
            break;
         case OCCURS_OPTIONAL:
            occString = "?";
            break;
         default:
            throw new IllegalArgumentException("invalid occurence");
      }


      // add to dtd
      m_elements.put(name, null);

      // add it's occurence for this parent
      String occKey = parent + ":" + name;
      m_occurences.put(occKey, occString);

      // add to it's parent
      ArrayList childList = (ArrayList)m_elements.get(parent);
      if (childList == null)
         childList = new ArrayList();
      childList.add(name);
      m_elements.put(parent, childList);
   }

   /**
    * Writes a valid DTD file to the specified output stream.  Any empty
    * elements are added with #PCDATA as their child data. Encoding specified
    * in the dtd will be the {@link PSCharSets#rxStdEnc()}.
    *
    * @param out The output stream to write to.  This will be wrapped with
    * a buffered writer, so this stream should not already be buffered in
    * any way.  The caller of this method is responsible for closing the
    * output stream.
    *
    * @throws IllegalArgumentException if out is <code>null</code>.
    * @throws IOException if there is an error writing to the output stream.
    */
   public void write(OutputStream out) throws IOException
   {
      if (out == null)
         throw new IllegalArgumentException("out may not be null");

      BufferedWriter writer =
         new BufferedWriter(new OutputStreamWriter(out));

      // add the xml header
      String csResultString = new String("<?xml version='1.0' encoding='" +
         PSCharSets.rxStdEnc() + "'?>");
      writer.write(csResultString);
      writer.newLine();

      // add the root and recursively add all children
      List writtenElements = new ArrayList();
      writeElement(writer, m_root, writtenElements);
      writer.flush();
   }

   /**
    * Writes out the element.  Also recursivley writes out any children that
    * element contains if they have not yet been written. If no children, then
    * writes #PCDATA for element data.
    *
    * @param writer The writer to writer to. Assumed not <code>null</code>.
    * @param element The name of the element to write.  Assumed not
    * <code>null</code>.
    * @param writtenElements List of Element names whose definitions have
    * already been written to the DTD.  Allows multiple parents to reference the
    * same child element without the child's definition being written to the
    * DTD more than once.  Assumed not <code>null</code>.
    *
    * @throws IOException if there is an error writing to the output stream.
    */
   private void writeElement(BufferedWriter writer, String element,
      List writtenElements) throws IOException
   {
      // add it to the written list now so when recursing we don't add it twice
      writtenElements.add(element);

      // now add the actual element info
      writer.write("<!ELEMENT " + element + " (");
      ArrayList children = (ArrayList)m_elements.get(element);
      if (children == null || children.size() == 0)
      {
         writer.write("#PCDATA)>");
         writer.newLine();
      }
      else
      {
         String sep = "";
         Iterator i = children.iterator();
         while (i.hasNext())
         {
            String child = (String)i.next();
            String occurence = (String)m_occurences.get(element + ":" + child);
            if (occurence == null)
               occurence = "";
            writer.write(sep + child + occurence);
            sep = ", ";
         }
         writer.write(")>");
         writer.newLine();

         // now write the children's elements too if they have not been written
         i = children.iterator();
         while (i.hasNext())
         {
            String child = (String)i.next();
            if (!writtenElements.contains(child))
               writeElement(writer, child, writtenElements);
         }
      }
   }

   /**
    * Returns the root element name.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getRootName()
   {
      return m_root;
   }

   /** Constant for occurence: required */
   public static final int OCCURS_ONCE = 0;

   /** Constant for occurence: ? (0 or 1) */
   public static final int OCCURS_OPTIONAL = 1;

   /** Constant for occurence: + (1 or more) */
   public static final int OCCURS_ATLEASTONCE = 2;

   /** Constant for occurence: * (zero or more) */
   public static final int OCCURS_ANY = 3;

   /**
    * Map of elements and their children by name.  Key is element name,
    * value is an ArrayList of their children's names or null if no child
    * element has been added to this element.  Each child
    * element will be in the map and in their parents child list.  Never
    * <code>null</code>, may be empty.
    */
   private HashMap m_elements = new HashMap();

   /**
    * Map of elements and their occurence types converted to Strings.
    * Never <code>null</code>, may be empty.  Key is "parentName:childName", so
    * that each child's occurence is associated with a parent.
    */
   private HashMap m_occurences = new HashMap();

   /**
    * The name of the root element. Initialized in the constructor, never <code>
    * null</code> after that.
    */
   private String m_root = null;
}


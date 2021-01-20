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
package com.percussion.design.objectstore;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to extract parameters through macros.
 */
public class PSMacro extends PSNamedReplacementValue
{
   /**
    * Construct a Java object from it's XML representation.
    *
    * @param source the XML element node to construct this object from, not
    *    <code>null</code>, see {@link toXml(Document)} for the expected XML
    *    format.
    * @param parent the Java object which is the parent of this object, may be
    *    <code>null</code>.
    * @param parentComponents the parent objects of this object, may be 
    *    <code>null</code> or empty.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *    appropriate type.
    */
   public PSMacro(Element source, IPSDocument parent, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      super(source, parent, parentComponents);
      fromXml(source, parent, parentComponents);
   }

   /**
    * Constructs a macro replacement value.
    *
    * @param name the name of the macro, not <code>null</code> or empty.
    */
   public PSMacro(String name)
   {
      super(name);
   }

   /**
    * Get the type of replacement value that this object represents.
    * 
    * @return {@link #VALUE_TYPE}
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }
   
   /**
    * This method is called to populate this instance from a XML 
    * representation. See the {@link #toXml(Document)} method for a description 
    * of the XML object.
    *
    * @param source the XML element node to construct this object from,
    *    must not be <code>null</code>.
    * @param parent may be <code>null</code>.
    * @param parentComponents may be <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format.
    */
   public void fromXml(Element source, IPSDocument parent, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      super.fromXml(source, parent, parentComponents);
   }

   /**
    * Creates the XML serialization for this class. The structure of the XML 
    * document conforms to this DTD:
    * <pre><code>
    * &lt;!ELEMENT PSXMacro (name)&lt;
    * &lt;!ATTLIST PSXMacro
    *    id CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * @return a newly created Element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      Element root = super.toXml(doc);

      return root;
   }
   
   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSMacro))
         return false;
         
      PSMacro t = (PSMacro) o;
      if (!getName().equals(t.getName()))
         return false;
         
      return true;
   }
   
   /**
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return getName().hashCode();
   }

   // see base class for description
   protected String getNodeName()
   {
      return ms_NodeType;
   }

   // see base class for description
   protected int getErrorCode()
   {
      return IPSObjectStoreErrors.MACRO_NAME_EMPTY;
   }

   /**
    * The value type associated with this instances of this class.
    */
   public static final String VALUE_TYPE = "Macro";

   /**
    * The XML node name, package access on this so they may reference each 
    * other in <code>fromXml</code>.
    */
   static final String ms_NodeType = "PSXMacro";
}

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * This class is used to extract parameters through macros.
 */
public class PSMacro extends PSNamedReplacementValue
{
   /**
    * Construct a Java object from it's XML representation.
    *
    * @param source the XML element node to construct this object from, not
    *    <code>null</code>, see {@link #toXml(Document)} for the expected XML
    *    format.
    * @param parent the Java object which is the parent of this object, may be
    *    <code>null</code>.
    * @param parentComponents the parent objects of this object, may be 
    *    <code>null</code> or empty.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *    appropriate type.
    */
   public PSMacro(Element source, IPSDocument parent, 
      List parentComponents) throws PSUnknownNodeTypeException
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
      List parentComponents) throws PSUnknownNodeTypeException
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

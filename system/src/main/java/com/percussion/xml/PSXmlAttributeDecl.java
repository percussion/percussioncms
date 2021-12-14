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

import org.apache.xerces.impl.dtd.XMLAttributeDecl;
import org.apache.xerces.impl.dtd.XMLSimpleType;

import java.util.Enumeration;
import java.util.Vector;

/**
 * This class encapsulates an element's attribute declaration in the DTD.
 */
public class PSXmlAttributeDecl extends XMLAttributeDecl
{
   /**
    * Default constructor
    */
   public PSXmlAttributeDecl()
   {
      super();
   }

   /**
    * constructor
    * @param xmlAttrDecl the Attribute Declaration object to encapsulate,
    * may not be <code>null</code>
    * @throw IllegalArgumentException if xmlAttrDecl is <code>null</code>
    */
   public PSXmlAttributeDecl(XMLAttributeDecl xmlAttrDecl)
   {
      super();
      if (xmlAttrDecl == null)
         throw new IllegalArgumentException("xmlAttrDecl may not be null");
      setValues(xmlAttrDecl.name, xmlAttrDecl.simpleType, xmlAttrDecl.optional);
   }

   /**
    * constant for attribute of type CDATA
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short TYPE_CDATA = XMLSimpleType.TYPE_CDATA;

   /**
    * constant for attribute type ENTITY
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short TYPE_ENTITY = XMLSimpleType.TYPE_ENTITY;

   /**
    * constant for attribute type ENUMERATION
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short TYPE_ENUMERATION = XMLSimpleType.TYPE_ENUMERATION;

   /**
    * constant for attribute type ID
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short TYPE_ID = XMLSimpleType.TYPE_ID;

   /**
    * constant for attribute type IDREF
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short TYPE_IDREF = XMLSimpleType.TYPE_IDREF;

   /**
    * constant for attribute type NMTOKEN
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short TYPE_NMTOKEN = XMLSimpleType.TYPE_NMTOKEN;

   /**
    * constant for attribute type NOTATION
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short TYPE_NOTATION = XMLSimpleType.TYPE_NOTATION;

   /**
    * constant for attribute type NAMED
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short TYPE_NAMED = XMLSimpleType.TYPE_NAMED;

   /**
    * constant for attribute type IDREFS
    */
   public static final int TYPE_IDREFS = 8;

   /**
    * constant for attribute type ENTITIES
    */
   public static final int TYPE_ENTITIES = 9;

   /**
    * constant for attribute type NMTOKENS
    */
   public static final int TYPE_NMTOKENS = 10;

   /**
    * constant for attribute whose type is not defined.
    */
   public static final int TYPE_UNKNOWN = -1;

   /**
    * constant for attribute default type DEFAULT.
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short DEFAULT_TYPE_DEFAULT =
      XMLSimpleType.DEFAULT_TYPE_DEFAULT;

   /**
    * constant for attribute default type FIXED.
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short DEFAULT_TYPE_FIXED =
      XMLSimpleType.DEFAULT_TYPE_FIXED;

   /**
    * constant for attribute default type IMPLIED.
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short DEFAULT_TYPE_IMPLIED =
      XMLSimpleType.DEFAULT_TYPE_IMPLIED;

   /**
    * constant for attribute default type REQUIRED.
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final short DEFAULT_TYPE_REQUIRED =
      XMLSimpleType.DEFAULT_TYPE_REQUIRED;

   /**
    * constant for attribute default type is not defined.
    * @see org.apache.xerces.impl.dtd.XMLSimpleType
    */
   public static final int DEFAULT_TYPE_NOFIXED = -1;

   /**
    * Returns the name of this attribute in the DTD definition.
    * @return Name of this attribute in the DTD definition.
    * Never <code>null</code> or empty.
    */
   public String getName()
   {
      return name.rawname;
   }

   /**
    * Returns the declared type of this attribute in the DTD definition.
    * @return Declared type of this attribute in the DTD definition.
    */
   public int getDeclaredType()
   {
      return simpleType.type;
   }

   /**
    * Returns the default type of this attribute's value in the DTD definition.
    * @return the default type of this attribute in the DTD definition.
    */
   public int getDefaultType()
   {
      return simpleType.defaultType;
   }

   /**
    * Returns the number of tokens in the list of tokens that can be used as
    * values for this enumerated attribute type; the returned value has no
    * meaning for other attribute types. Enumerated attribute types are:
    * NOTATION and NAME_TOKEN_GROUP.
    * @return Number of tokens, or <code>null</code> if no tokens defined.
    */
   public int size()
   {
      String[] elems = simpleType.enumeration;
      if (elems == null)
         return 0;
      return elems.length;
   }

   /**
    * Returns a list of all tokens that can be used as values for this
    * enumerated attribute type; the returned value has no meaning for other
    * attribute types. Enumerated attribute types are: NOTATION and
    * NAME_TOKEN_GROUP.
    * @return the list of allowed tokens for the attribute value as defined
    * by the DTD definition, or <code>null</code> if none specified.
    * Never <code>null</code>, may be empty.
    */
   public Enumeration elements()
   {
      Vector el = new Vector();
      String[] elems = simpleType.enumeration;
      if (!((elems == null) || (elems.length == 0)))
      {
         int size = elems.length;
         for (int i = 0; i < size; i++)
            el.addElement(elems[i]);
      }
      return el.elements();
   }

   /**
    * Returns the default value of this attribute in the DTD definition.
    * This is useful if this attribute was not given an explicit value in
    * the document instance. The returned value is only meaningful for
    * attribute types defined as FIXED or NOFIXED.
    * @return the default value of the attribute, or <code>null</code> if
    * none specified.  May be <code>null</code> or empty.
    */
   public String getDefaultStringValue()
   {
      return simpleType.defaultValue;
   }

}

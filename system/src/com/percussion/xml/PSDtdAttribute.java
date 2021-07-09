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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * The PSDtdAttribute class will provide an interface to attribute information.
 */
public class PSDtdAttribute implements Serializable {
   /**
    * Constructor.
    * @param attr the <code>PSXmlAttributeDecl</code> object that is returned
    * by the <code>getAttributeDeclarations</code> method of the
    * <code>PSDtd</code> class.
    */
   public PSDtdAttribute(PSXmlAttributeDecl attr)
   {
      m_name = attr.getName();

      m_possibleValues = new ArrayList();

      switch (attr.getDeclaredType()) {
      case PSXmlAttributeDecl.TYPE_NOTATION:
         m_type = ENOTATION;
         processEnumeration(attr);
         break;
      case PSXmlAttributeDecl.TYPE_CDATA:
         m_type = CDATA;
         break;
      case PSXmlAttributeDecl.TYPE_ID:
         m_type = ID;
         break;
      case PSXmlAttributeDecl.TYPE_IDREFS:
         m_type = IDREFS;
         break;
      case PSXmlAttributeDecl.TYPE_ENTITIES:
         m_type = ENTITIES;
         break;
      case PSXmlAttributeDecl.TYPE_IDREF:
         m_type = IDREF;
         break;
      case PSXmlAttributeDecl.TYPE_ENUMERATION:
         m_type = ENUMERATION;
         processEnumeration(attr);
         break;
      case PSXmlAttributeDecl.TYPE_NMTOKEN:
         m_type = TOKEN;
         break;
      case PSXmlAttributeDecl.TYPE_NMTOKENS:
         m_type = TOKENS;
         break;
      case PSXmlAttributeDecl.TYPE_ENTITY:
         m_type = ENTITY;
         break;
      case PSXmlAttributeDecl.TYPE_UNKNOWN:
      default:
         m_type = UNKNOWN;
      }

      switch (attr.getDefaultType()) {
      case PSXmlAttributeDecl.DEFAULT_TYPE_FIXED:
         m_occurrence = FIXED;
         setDefault(attr);
         break;
      case PSXmlAttributeDecl.DEFAULT_TYPE_NOFIXED:
         m_occurrence = NOFIXED;
         setDefault(attr);
         break;
      case PSXmlAttributeDecl.DEFAULT_TYPE_REQUIRED:
         m_occurrence = REQUIRED;
         break;
      default:
         m_occurrence = IMPLIED;
         break;
      }
   }

   public PSDtdAttribute(String name)
   {
      m_name = name;
      m_occurrence = REQUIRED;
      m_type = UNKNOWN;
   }

   /**
    * Creates a string array of possible values for the attribute represented by
    * this object.
    * @param att the <code>PSXmlAttributeDecl</code> object that is returned
    * by the <code>getAttributeDeclarations</code> method of the
    * <code>PSDtd</code> class.
    */
   private void processEnumeration(PSXmlAttributeDecl att)
   {
      // Create string array of possible values
      if (att.size() == 0)
         return;
      m_possibleValues = new ArrayList(att.size());
      Enumeration e = att.elements();
      while (e.hasMoreElements()) {
         m_possibleValues.add((String) e.nextElement());
      }
   }

   /**
    * Sets the default value for the attribute represented by this object.
    * @param att the <code>PSXmlAttributeDecl</code> object that is returned
    * by the <code>getAttributeDeclarations</code> method of the
    * <code>PSDtd</code> class.
    */
   private void setDefault(PSXmlAttributeDecl att)
   {
      m_default = att.getDefaultStringValue();
   }

   /**
    * Return the default string value for this attribute or
    * null if it is not a default-containing type.
    */
   public String getDefaultStringValue()
   {
      return m_default;
   }

   public String getName()
   {
      return m_name;
   }

  public String getOcurrenceText()
 {
      String csRet=new String();
      switch(m_occurrence)
      {
         case PSDtdAttribute.REQUIRED:
             csRet="#REQUIRED";
         break;
         case PSDtdAttribute.IMPLIED:
              csRet="#IMPLIED";
         break;
         case  PSDtdAttribute.FIXED:
               csRet="#FIXED";
         break;
         case PSDtdAttribute.NOFIXED:
               csRet="#NOFIXED";
         break;
      }
      return(csRet);
 }

 public String getTypeText()
 {
      String csRet=new String();
      switch(m_type)
      {
        case  PSDtdAttribute.CDATA:
            csRet="CDATA";
         break;
          case  PSDtdAttribute.ID:
             csRet="ID";
         break;
         case  PSDtdAttribute.ENUMERATION:
              csRet="ENUMERATION";
         break;
          case  PSDtdAttribute.IDREF:
              csRet="IDREF";
         break;
          case  PSDtdAttribute.IDREFS:
               csRet="IDREFS";
         break;
          case  PSDtdAttribute.TOKEN:
               csRet="TOKEN";
         break;
         case  PSDtdAttribute.TOKENS:
                csRet="TOKENS";
         break;
          case  PSDtdAttribute.NOTATION:
                csRet="NOTATION";
         break;
          case PSDtdAttribute.ENOTATION:
                csRet="ENOTATION";
         break;
          case PSDtdAttribute.ENTITY:
               csRet="ENTITY";
         break;
            case PSDtdAttribute.ENTITIES:
               csRet="ENTITIES";
         break;
          case PSDtdAttribute.UNKNOWN:
               csRet="UNKNOWN";
         break;
       }
      return(csRet);
   }



   /**
    Return the occurence type for this attribute.

    Possible Values:
    <code>REQUIRED  </code> - attribute must be defined
    <code>IMPLIED   </code> - attibute may or may not be defined
    <code>FIXED    </code> - static attribute value
    <code>NOFIXED   </code> - attribute value has a default value

    For FIXED and NOFIXED, use getDefaultStringValue to determine
    either the FIXED value or default value
    */
   public int getOccurrence()
   {
      return m_occurrence;
   }

  /**
   * Allows this attribute occurrence to be changed.
   * Use the following static variables:
   * <code>PSDtdAttribute.REQUIRED</code>
    * <code>PSDtdAttribute.IMPLIED</code>
    * <code>PSDtdAttribute.FIXED</code>
    * <code>PSDtdAttribute.NOFIXED</code>
   */
  public void setOccurrence( int occur )
  {
    m_occurrence = occur;
  }

   /**
    * Return the type of this attribute.
    * <p>
    * Possible Values:
    * <code>CDATA      </code> - standard data
    * <code>ID         </code> - unique identifier in XML
    * <code>ENUMERATION </code> - enumerated pick list*
    * <code>IDREF      </code> - reference to an XML ID
    * <code>IDREFS     </code> - whitespace delimited list of IDREFs
    * <code>TOKEN      </code> - xml name token
    * <code>TOKENS     </code> - whitespace delimited list of XML name tokens
    * <code>NOTATION   </code> - notation (not supported by IBM parser!)
    * <code>ENOTATION  </code> - enumerated notation pick list
    * <code>ENTITY     </code> - entity
    * <code>ENTITIES   </code> - whitespace delimited list of entities
    * <code>UNKNOWN     </code> - unknown?
    * <p>
    * - The IBM TXDOM parser comtains enumerated XML name tokens,
    * and does not include a generic enumerated type
    *
    */
   public int getType()
   {
      return m_type;
   }

  void setType( int type )
  {
    m_type = type;
  }

   /**
    Return the array of possible values for this attribute or
    null if it is not an enumerated type.  The enumerated types
    are ENUMERATION and ENOTATION.
    */
   public List getPossibleValues()
   {
      return Collections.unmodifiableList(m_possibleValues);
   }

   public void setPossibleValues(List possibleValues)
   {
      m_possibleValues = possibleValues;
   }

   public void addPossibleValue(String val)
   {
      m_possibleValues.add(val);
   }

   /**
    Catalog method for this DTD item.
    */
   public void catalog(List catalogList, String cur,
      String sep, String attribId)
   {
      if (catalogList.size() >= PSDtdTree.MAX_CATALOG_SIZE)
      {
         catalogList.add("TRUNCATED!");
         return;
      }

      if (m_name == null)
         catalogList.add(cur + sep + attribId + "<NULL>");
      else
         catalogList.add(cur + sep + attribId + m_name);
   }

   int      m_type            = 0;
   String    m_default         = null;
   List      m_possibleValues   = null;
   String   m_name            = null;
   int      m_occurrence      = 0;

   static public final int REQUIRED   = 0;
   static public final int IMPLIED   = 1;
   static public final int FIXED      = 2;
   static public final int NOFIXED   = 3;

   static public final int CDATA         = 0;
   static public final int ID            = 1;
   static public final int ENUMERATION   = 2;
   static public final int IDREF         = 3;
   static public final int IDREFS      = 4;
   static public final int TOKEN         = 5;
   static public final int TOKENS      = 6;
   static public final int NOTATION      = 7;
   static public final int ENOTATION   = 8;
   static public final int ENTITY      = 9;
   static public final int ENTITIES      = 10;
   static public final int UNKNOWN      = 11;

}


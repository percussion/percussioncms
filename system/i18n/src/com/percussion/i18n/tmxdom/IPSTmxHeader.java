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
package com.percussion.i18n.tmxdom;

/**
 * This interface defines methods specific to TMX document's header. TMX DTD
 * defines a set of required and optional attributes for the header. This
 * interface currently makes use of only the required attributes. A header
 * element can contain &lt;note&gt; and &lt;prop&gt; child elements. We use
 * &lt;prop&gt; elements to specify the list supported languages by the TMX
 * resource bundle as follows:
 * &lt;header&gt;
 * &lt;prop type="supportedlanguage"&gt;en-us&lt;/prop&gt;
 * &lt;prop type="supportedlanguage"&gt;fr-ca&lt;/prop&gt;
 * ...
 * &lt;/header&gt;
 * @see IPSTmxDtdConstants
 */
public interface IPSTmxHeader
   extends IPSTmxNode
{
   /**
    * Method to get an array of supported or defined langauges in the TMX
    * document.
    * @return array of language strings of suported languages. May be
    * <code>null</code>.
    * @see IPSTmxDtdConstants
    */
   Object[] getSupportedLanguages();

   /**
    * Method to set the property of the header.
    * @param propertyType should be one of the values defined in this class.
    * @param value should not be <code>null</code>, if <code>null</code>,
    * <code>empty</code> is assumed.
    * @see #PROPNAMEMAP
    */
   void setProperty(int propertyType, String value);

   /**
    * Method to add a new language to the TMX document. Adding a language to
    * header  does not add the stubs for the translation variants automatically.
    * @param language must not be <code>null</code>.
    */
   void addLanguage(String language);

   /**
    * Property Type for creationtool
    */
   static final int PROP_CREATION_TOOL = 0;
   /**
    * Property Type for creationtool
    */
   static final int PROP_CREATION_TOOL_VERSION = 1;
   /**
    * Property Type for creationtool
    */
   static final int PROP_SEG_TYPE = 2;
   /**
    * Property Type for creationtool
    */
   static final int PROP_O_TMF = 3;
   /**
    * Property Type for creationtool
    */
   static final int PROP_ADMIN_LANG = 4;
   /**
    * Property Type for creationtool
    */
   static final int PROP_SRC_LANG = 5;
   /**
    * Property Type for creationtool
    */
   static final int PROP_DATA_TYPE = 6;

   /**
    * Property type string map for the TMX header. These are actually attributes
    * of the hedaer element of the TMX document. These are the required
    * attributes as per the TMX 1.4 DTD.
    */
   static final String[] PROPNAMEMAP =
   {
      IPSTmxDtdConstants.ATTR_CREATION_TOOL,
      IPSTmxDtdConstants.ATTR_CREATION_TOOL_VERSION,
      IPSTmxDtdConstants.ATTR_SEG_TYPE,
      IPSTmxDtdConstants.ATTR_O_TMF,
      IPSTmxDtdConstants.ATTR_ADMIN_LANG,
      IPSTmxDtdConstants.ATTR_SRC_LANG,
      IPSTmxDtdConstants.ATTR_DATA_TYPE,
   };
}

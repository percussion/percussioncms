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

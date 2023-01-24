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
 * This interface defines the string constants from the tmx14.dtd that are
 * normally required in the implementing classes. The DTD tmx14.dtd corresponds
 * to the Translation Memory Exchange Specification version 1.4. Details can
 * be found at:
 * <a href="http://www.lisa.org/tmx/">Localisation Industry Standards Association</a>
 *
 */

public interface IPSTmxDtdConstants
{
   /**
    * Name of the root element in the TMX document.
    */
   static public final String ELEM_TMX_ROOT = "tmx";
   /**
    * Name of the only header element in the TMX document. First child element
    * of the root element.
    */
   static public final String ELEM_HEADER = "header";

   /**
    * Name of the only body element in the TMX document. Second child element
    * of the root element.
    */
   static public final String ELEM_BODY = "body";

   /**
    * Name of the prop element in the TMX document. This optional element can
    * occur multiple times as children of {@link #ELEM_HEADER}, {@link #ELEM_TU}
    * or {@link #ELEM_TUV} elements. This provides ability to add arbitrary
    * parameters to these elements.
    */
   static public final String ELEM_PROP = "prop";

   /**
    * Name of the element representing translational unit in the document.
    * Element {@link #ELEM_BODY} will have zero or more children of these.
    */
   static public final String ELEM_TU = "tu";

   /**
    * Name of the element representing translational unit variant in the
    * document. Element {@link #ELEM_TU} will have one or more children of these.
    */
   static public final String ELEM_TUV = "tuv";

   /**
    * Name of the element representing translational unit variant segment in the
    * document. Each {@link #ELEM_TUV} element will have one child of this.
    */
   static public final String ELEM_SEG = "seg";

   /**
    * Name of the note element in the TMX document. This optional element can
    * occur multiple times as children of {@link #ELEM_HEADER}, {@link #ELEM_TU}
    * or {@link #ELEM_TUV} elements. This element can provide content
    * information for the translator.
    */
   static public final String ELEM_NOTE = "note";

   /**
    * Name of the type attribute of {@link #ELEM_PROP} element
    */
   static public final String ATTR_TYPE = "type";

   /**
    * Name of the type attribute of {@link #ELEM_TU} element. This is the lookup
    * key for the tanslation unit.
    */
   static public final String ATTR_TUID = "tuid";

   /**
    * Name of the language attribute optional for {@link #ELEM_NOTE} and
    * {@link #ELEM_PROP} elements and required for {@link #ELEM_TUV} element.
    */
   static public final String ATTR_XML_LANG = "xml:lang";

   /**
    * Name of the required attribute for {@link #ELEM_HEADER} element.
    */
   static public final String ATTR_CREATION_TOOL = "creationtool";

   /**
    * Name of the required attribute for {@link #ELEM_HEADER} element.
    */
   static public final String ATTR_CREATION_TOOL_VERSION = "creationtoolversion";

   /**
    * Name of the required attribute for {@link #ELEM_HEADER} element.
    */
   static public final String ATTR_SEG_TYPE = "segtype";

   /**
    * Name of the required attribute for {@link #ELEM_HEADER} element.
    */
   static public final String ATTR_O_TMF = "o-tmf";

   /**
    * Name of the required attribute for {@link #ELEM_HEADER} element.
    */
   static public final String ATTR_ADMIN_LANG = "adminlang";

   /**
    * Name of the required attribute for {@link #ELEM_HEADER} element.
    */
   static public final String ATTR_SRC_LANG = "srclang";

   /**
    * Name of the required attribute for {@link #ELEM_HEADER} element.
    */
   static public final String ATTR_DATA_TYPE = "datatype";

   /**
    * Default language string for the TMX resource bundle
    */
   static public final String DEFAULT_LANG = "en-us";

   /**
    * Default value of the required attribute {@link #ATTR_ADMIN_LANG} of
    * {@link #ELEM_HEADER} element.
    */
   static public final String DEFAULT_VALUE_ATTR_ADMIN_LANG = DEFAULT_LANG;

   /**
    * Default value of the required attribute {@link #ATTR_SRC_LANG} of
    * {@link #ELEM_HEADER} element.
    */
   static public final String DEFAULT_VALUE_ATTR_SRC_LANG = DEFAULT_LANG;

   /**
    * Default value of the required attribute {@link #ATTR_CREATION_TOOL} of
    * {@link #ELEM_HEADER} element.
    */
   static public final String DEFAULT_VALUE_ATTR_CREATION_TOOL =
      "Percussion Language Tool";

   /**
    * Default value of the required attribute {@link #ATTR_CREATION_TOOL_VERSION}
    * of {@link #ELEM_HEADER} element.
    */
   static public final String DEFAULT_VALUE_ATTR_CREATION_TOOL_VERSION = "0.0";

   /**
    * Default value of the required attribute {@link #ATTR_DATA_TYPE}
    * of {@link #ELEM_HEADER} element.
    */
   static public final String DEFAULT_VALUE_ATTR_DATA_TYPE = "plaintext";

   /**
    * Default value of the required attribute {@link #ATTR_SEG_TYPE}
    * of {@link #ELEM_HEADER} element.
    */
   static public final String DEFAULT_VALUE_ATTR_SEG_TYPE = "block";

   /**
    * Default value of the required attribute {@link #ATTR_O_TMF} of
    * {@link #ELEM_HEADER} element.
    */
   static public final String DEFAULT_VALUE_ATTR_O_TMF = "none";

   /**
    * Value of the attribute {@link #ATRR_TYPE} of {@link #ELEM_PROP} element
    * when it is a child element of {@link #ELEM_HEADER} element. This is the
    * way to set the languages supported in the product. This attribute is not
    * from the standard DTD and is specific to our purpose.
    */
   static public final String ATTR_VAL_SUPPORTEDLANGUAGE = "supportedlanguage";

   /**
    * Value of the attribute {@link #ATRR_TYPE} of {@link #ELEM_PROP} element
    * when it is a child element of {@link #ELEM_TU} element. This is the
    * way to set the section name for each translation unit. This attribute is
    * not from the standard DTD and is specific to our purpose.
    */
   static public final String ATTR_VAL_SECTIONNAME = "sectionname";
   
   /**
    * Value of the attribute {@link #ATRR_TYPE} of {@link #ELEM_PROP} element
    * when it is a child element of {@link #ELEM_TUV} element. This is the
    * way to specify a mnemonic character for each translation unit. This 
    * attribute is not from the standard DTD and is specific to our purpose.
    */
   static public final String ATTR_VAL_MNEMONIC = "mnemonic";
   
   /**
    * Value of the attribute {@link #ATRR_TYPE} of {@link #ELEM_PROP} element
    * when it is a child element of {@link #ELEM_TUV} element. This is the
    * way to specify a tooltip for each translation unit. This 
    * attribute is not from the standard DTD and is specific to our purpose.
    */
   static public final String ATTR_VAL_TOOLTIP = "tooltip";
}

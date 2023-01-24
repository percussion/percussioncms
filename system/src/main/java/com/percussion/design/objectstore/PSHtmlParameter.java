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

import org.w3c.dom.Element;

import java.util.List;

/**
 * The PSHtmlParameter class is used to define a replacement value is a
 * HTML parameter value.
 *
 * @see         IPSReplacementValue
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSHtmlParameter extends PSNamedReplacementValue
{
   /**
    * Gets the type of replacement value this object represents.
    * @return {@link #VALUE_TYPE}
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }


   /**
    * Constructs a HTML parameter replacement value.
    *
    * @param name the name of the HTML parameter
    */
   public PSHtmlParameter(String name)
   {
      super( name );
   }


   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode the XML element node to construct this object from
    * @param parentDoc the Java object which is the parent of this object
    * @param parentComponents   the parent objects of this object
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *   appropriate type
    */
   public PSHtmlParameter(Element sourceNode, IPSDocument parentDoc,
                          List parentComponents)
         throws PSUnknownNodeTypeException
   {
      super( sourceNode, parentDoc, parentComponents );
   }


   /**
    * Gets the text which can be displayed to represent this value.
    * @return "PSXParam/" + <code>getName()</code>
    */
   public String getValueDisplayText()
   {
      return getParamValueText();
   }
   
   /**
    * Gets the text the represents the value of the param. This method is
    * final so that <code>PSSingleHtmlParameter</code> will input the
    * same value text that contains a common base. This is needed when the
    * execution plan is being determined.
    * @return "PSXParam/" + <code>getName()</code>
    */
   public final String getParamValueText()
   {
      return "PSXParam/" + getName();
   }
   
   // see base class for description
   protected String getNodeName()
   {
      return ms_NodeType;
   }


   // see base class for description
   protected int getErrorCode()
   {
      return IPSObjectStoreErrors.HTML_PARAM_NAME_EMPTY;
   }


   /**
    * The value type associated with this instances of this class.
    */
   public static final String VALUE_TYPE = "HtmlParameter";

   /* package access on this so they may reference each other in fromXml */
   static final String ms_NodeType = "PSXHtmlParameter";
}

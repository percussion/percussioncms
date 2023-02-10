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

import java.util.ArrayList;
import java.util.List;

/**
 * The PSExtensionParamValue class is used to set the value associated with a
 * parameter in a call to an exit. The value may refer to a literal,
 * CGI variable, HTML parameter, XML field or back-end column.
 *
 * @see PSExtensionCall#getParamValues()
 */
public class PSExtensionParamValue
   extends PSAbstractParamValue
   implements IPSParameter
{
   /**
    * Constructs this object from its XML representation. See the
    * {@link #toXml(Document) toXml()} method for the DTD of the
    * <code>sourceNode</code> element.
    *
    * @param sourceNode the XML element node to construct this object from,
    * may not be <code>null</code>
    *
    * @param parentDoc the Java object which is the parent of this object, may
    * be <code>null</code>
    *
    * @param parentComponents   the parent objects of this object, may be
    * <code>null</code> or empty
    *
    * @exception PSUnknownNodeTypeException if <code>sourceNode</code> is
    * <code>null</code> or the XML element node is not of the appropriate type
    */
   public PSExtensionParamValue(Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      super(sourceNode, parentDoc, parentComponents);
   }


   /**
    * Construct a parameter value for use in a call to a UDF exit.
    *
    * @param value the value to use at run-time for the parameter, may not be
    * <code>null</code>
    *
    * @see PSAbstractParamValue#setValue()
    */
   public PSExtensionParamValue(IPSReplacementValue value)
   {
      super(value);
   }

   /**
    * Creates a clone of this object.
    *
    * @return cloned object, never <code>null</code>
    */
   public Object clone()
   {
      return (PSExtensionParamValue)super.clone();
   }

   /**
    * Compares this object with the specified object.
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <code>true</code> if the specified object is an instance of this
    * class and the contained replacement value is equal.
    */
   public boolean equals(Object obj)
   {
      boolean equals = super.equals(obj);
      if (equals && (!(obj instanceof PSExtensionParamValue)))
         equals = false;
      return equals;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   /**
    * Returns the tag name of the root element from which this object can be
    * constructed.
    *
    * @return the name of the root node of the XML document returned by a call
    * to {@link#toXml(Document) toXml()} method.
    *
    * @see toXml(Document)
    */
   public String getNodeName()
   {
      return ms_NodeType;
   }

   /**
    * This method is called to serialize this object to an XML element.
    * <p>
    * The DTD of the returned XML element is:
    * <pre><code>
    *
    * &lt;!ELEMENT PSXExtensionParamValue   (value)>
    * &lt;!ELEMENT value (PSXBackEndColumn | PSXLiteral |PSXCgiVariable |
    *                   PSXHtmlParameter | PSXCookie | PSXUserContext |
    *                   PSXXmlField)
    * >
    *
    * </code></pre>
    *
    * See the "sys_BasicObjects.dtd" file for the DTD of the elements contained
    * by the value element.
    *
    * @see {@link PSAbstractParamValue#toXml(Document) toXml()} method for the
    * description of the parameters and returned value.
    */
   public Element toXml(Document doc)
   {
      return super.toXml(doc);
   }

   /**
    * The tag name of the root element from which this object can be
    * constructed.
    * This has package access for backwards compatibility. Other classes should
    * use the {@link #getNodeName()} method instead of directly accessing this
    * variable.
    * @see toXml(Document)
    */
   static final String ms_NodeType = "PSXExtensionParamValue";
}


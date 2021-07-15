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

package com.percussion.design.objectstore;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSFunctionParamValue class is used to set the value associated with a
 * parameter in a call to a database function. The value may refer to a literal,
 * CGI variable, HTML parameter, XML field or back-end column.
 * (See the DTD defined in {@link #toXml(Document) toXml()} method for all
 * possible types of supported parameters)
 */
public class PSFunctionParamValue
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
   public PSFunctionParamValue(Element sourceNode,
      IPSDocument parentDoc, ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      super(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct this object from the specified replacement value.
    *
    * @param value the value to use at run-time for the parameter, may not be
    * <code>null</code>
    *
    * @see PSAbstractParamValue#setValue()
    */
   public PSFunctionParamValue(IPSReplacementValue value)
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
      return (PSFunctionParamValue)super.clone();
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
      if (equals && (!(obj instanceof PSFunctionParamValue)))
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
    * to {@link #toXml(Document) toXml()} method.
    *
    * @see toXml(Document)
    */
   public String getNodeName()
   {
      return NODE_NAME;
   }

   /**
    * Determines whether this parameter's replacement value is static or not.
    *
    * @return <code>true</code> if the replacement value depends
    * upon the runtime data for its value, <code>false</code> otherwise.
    */
   public boolean isStaticValue()
   {
      boolean staticValue = false;

      if (isBackEndColumn())
         staticValue = true;
      else if (getValue() instanceof PSLiteral)
         staticValue = true;

      return staticValue;
   }

   /**
    * This method is called to serialize this object to an XML element.
    * <p>
    * The DTD of the returned XML element is:
    * <pre><code>
    *
    * &lt;!ELEMENT PSXFunctionParamValue   (value)>
    * &lt;!ATTLIST PSXFunctionParamValue
    *   id %UniqueId; #REQUIRED
    * >
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
    * @see toXml(Document)
    * @see getNodename()
    */
   public static final String NODE_NAME = "PSXFunctionParamValue";
}


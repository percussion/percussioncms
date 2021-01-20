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
package com.percussion.deployer.objectstore.idtypes;

import com.percussion.deployer.objectstore.IPSDeployComponent;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;

/**
 * ID Context to represent a binding in a template
 * This binding is usually a <name, value> pair, but sometimes name may be null.
 * To circumvent problems with name as a key, an index is introduced for the 
 * binding. Thus bindings are referenced by both an index and a name.
 * @author vamsinukala
 *
 */
public class PSBindingIdContext extends PSApplicationIdContext
{
   /** 
    * Construct a JEXL binding.  
    * 
    * @param  index of the binding, never <code>null</code> or empty
    * @param  name may be <code>null</code> or empty
    * @param  value may not be <code>null</code> or empty
    * 
    * @throws IllegalArgumentException if <code>index</code> is 
    * <code>null</code> or empty.
    */
   public PSBindingIdContext(String index, String name, String value) {
      if (StringUtils.isBlank(index))
         throw new IllegalArgumentException("index may not be null");
      if (StringUtils.isBlank(value))
         throw new IllegalArgumentException("value may not be null");

      setIndex(Integer.parseInt(index));
      setName(name);
      setValue(value);
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSBindingIdContext(Element source) throws PSUnknownNodeTypeException {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the ordinal position of this context's extension in its parent's list.
    * 
    * @return The index, will be <code>-1</code> if the index has not been
    * specified.
    */
   public int getIndex()
   {
      return m_index;
   }

   /**
    * Set the ordinal position of this context's binding in its parent's list.
    */
   public void setIndex(int ix)
   {
      m_index = ix;
   }

   /**
    * the name of the binding
    * @return name it may be <code>null</code> or empty
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * sets the name of the binding
    */

   private void setName(String name)
   {
      this.m_name = name;
   }

   /**
    * the value of the binding, this is either an expression or a script
    * @return value it may be <code>null</code> or empty
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * set the value of this binding
    * @param value
    */
   private void setValue(String value)
   {
      this.m_value = value;
   }

   //see PSApplicationIdContext
   public String getDisplayText()
   {
      String text = null;

      if (StringUtils.isNotBlank(getName()))
      {
         text = MessageFormat.format(getBundle().getString(
               "bindingCtxIndexNameValue"), new Object[]
         {String.valueOf(getIndex()), getName(), getValue()});
      }
      else
      {
         text = MessageFormat.format(getBundle().getString(
               "bindingCtxIndexValue"), new Object[]
         {String.valueOf(getIndex()), getValue()});
      }
      text = addParentDisplayText(text);

      return text;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext that
    *    is this context's parent context.
    * -->
    * <pre><code>
    * &lt;!ELEMENT PSXBindingIdContext 
    * &lt;!ATTLIST  PSXBindingIdContext   
    *    index  CDATA #REQUIRED
    *    bname  CDATA #IMPLIED
    *    bValue CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_BNAME, getName());
      root.setAttribute(XML_ATTR_BVALUE, getValue());
      if (validateIndex(getIndex()))
         root.setAttribute(XML_ATTR_INDEX, String.valueOf(getIndex()));
      PSApplicationIdContext parent = getParentCtx();
      if (parent != null)
         root.appendChild(parent.toXml(doc));

      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode should not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      setName(sourceNode.getAttribute(XML_ATTR_BNAME));
      setValue(PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_BVALUE));
      setIndex(-1);
      String strIndex = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_INDEX);
      // only set index if specified
      if (strIndex != null && strIndex.trim().length() > 0)
      {
         try
         {
            setIndex(Integer.parseInt(strIndex));
         }
         catch (NumberFormatException ex)
         {
            // fall thru
         }

         if (!validateIndex(getIndex()))
         {
            Object[] args =
            {XML_NODE_NAME, XML_ATTR_INDEX, strIndex};
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element ctxEl = tree
            .getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (ctxEl != null)
         setParentCtx(PSApplicationIDContextFactory.fromXml(ctxEl));
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSBindingIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSBindingIdContext other = (PSBindingIdContext) obj;

      setName(other.getName());
      setValue(other.getValue());
      setIndex(other.getIndex());
      super.copyFrom(other);
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSBindingIdContext))
         isEqual = false;
      else
      {
         PSBindingIdContext other = (PSBindingIdContext) obj;
         if (getIndex() != other.getIndex())
            isEqual = false;
         else if (!getValue().equals(other.getValue()))
            isEqual = false;
         else if (StringUtils.isNotBlank(getName())
               && StringUtils.isNotBlank(other.getName())
               && !getName().equals(other.getName()))
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }

   // see IPSDeployComponent
   public int hashCode()
   {
      if (StringUtils.isNotBlank(getName()))
         return getName().hashCode() + getValue().hashCode() + getIndex()
               + super.hashCode();
      else
         return getValue().hashCode() + getIndex() + super.hashCode();
   }

   /**
    * Check the supplied index to see if it is valid (>=0)
    * 
    * @param index The index to check.
    * 
    * @return <code>true</code> if it is valid, <code>false</code> otherwise.
    */
   private boolean validateIndex(int index)
   {
      return index >= 0;
   }

   /**
    * the value of the binding, binding has a name, value pair. 
    */
   private String m_value;

   /**
    * the name of the binding, binding has a name, value pair. name may be 
    * <code>null</code>
    */
   private String m_name;

   /**
    * Index of this call in its parent's list.  Intialized during ctor, modfied 
    * only by calls to <code>copyFrom()</code>.  Will be <code>-1</code> if an
    * index has not been specified.
    */
   private int m_index = -1;

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXBindingIdContext";

   // private xml constant
   /**
    * the binding name if any, may be <code>null</code>
    */
   private static final String XML_ATTR_BNAME = "bName";

   /**
    * the binding value may be <code>null</code>
    */
   private static final String XML_ATTR_BVALUE = "bValue";

   private static final String XML_ATTR_INDEX = "index";

}

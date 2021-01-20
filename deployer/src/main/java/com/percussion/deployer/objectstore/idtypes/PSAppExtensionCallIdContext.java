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
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;

/**
 * ID Context to represent an extension call
 */
public class PSAppExtensionCallIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context from an extension call.  This version does not
    * set an index, assuming the call's parent context is not a list of 
    * extension calls.
    * 
    * @param The extension call, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>call</code> is 
    * <code>null</code>.
    */
   public PSAppExtensionCallIdContext(PSExtensionCall call)
   {
      if (call == null)
         throw new IllegalArgumentException("call may not be null");
         
      m_extensionRef = call.getExtensionRef().getFQN();
   }

   /**
    * Construct this context from an extension call, specifying its ordinal
    * position in its parent context.
    * 
    * @param The extension call, may not be <code>null</code>.
    * @param index The ordinal position of this call in its parent's list.  Must
    * be greater than or equal to 0.
    * 
    * @throws IllegalArgumentException if either param is invalid.
    */
   public PSAppExtensionCallIdContext(PSExtensionCall call, int index)
   {
      this(call);
               
      if (!validateIndex(index))
         throw new IllegalArgumentException("index is invalid");
         
      m_index = index;
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
   public PSAppExtensionCallIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Get the extension ref of the extension to which this context refers.
    * 
    * @return The ref, never <code>null</code> or empty.
    */
   public String getExtensionRef()
   {
      return m_extensionRef;
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
   
   //see PSApplicationIdContext
   public String getDisplayText()
   {
      String text;
      if (validateIndex(m_index))
      {
         text = MessageFormat.format(getBundle().getString(
            "appIdCtxOrdinalExtensionCall"), new Object[] {m_extensionRef, 
               String.valueOf(m_index)});
      }
      else
      {
         text = MessageFormat.format(getBundle().getString(
            "appIdCtxExtensionCall"), new Object[] {m_extensionRef});
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
    * &lt;!ELEMENT PSXAppExtensionCallIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppExtensionCallIdContext
    *    extRef CDATA #REQUIRED
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
      root.setAttribute(XML_ATTR_EXT_REF, m_extensionRef);
      if (validateIndex(m_index))
         root.setAttribute(XML_ATTR_INDEX, String.valueOf(m_index));
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
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      m_extensionRef = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_EXT_REF);
         
      m_index = -1;
      String strIndex = sourceNode.getAttribute(XML_ATTR_INDEX);
      // only set index if specified
      if (strIndex != null && strIndex.trim().length() > 0)
      {
         try 
         {
            m_index = Integer.parseInt(strIndex);
         }
         catch (NumberFormatException ex) 
         {
            // fall thru
         }
         
         if (!validateIndex(m_index))
         {
            Object[] args = {XML_NODE_NAME, XML_ATTR_INDEX, strIndex};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element ctxEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (ctxEl != null)
         setParentCtx(PSApplicationIDContextFactory.fromXml(ctxEl));
   }
   
   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSAppExtensionCallIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppExtensionCallIdContext other = (PSAppExtensionCallIdContext)obj;
      m_extensionRef = other.m_extensionRef;
      m_index = other.m_index;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppExtensionCallIdContext))
         isEqual = false;
      else 
      {
         PSAppExtensionCallIdContext other = (PSAppExtensionCallIdContext)obj;
         if (!m_extensionRef.equals(other.m_extensionRef))
            isEqual = false;
         else if (m_index != other.m_index)
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   // see IPSDeployComponent
   public int hashCode()
   {
      return m_extensionRef.hashCode() + m_index + super.hashCode();
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
    * Fully qualified name of the extension call this object represents.  Never
    * <code>null</code> or empty after construction, modified only by a call to
    * <code>copyFrom()</code>
    */
   private String m_extensionRef;
   
   /**
    * Index of this call in its parent's list.  Intialized during ctor, modfied 
    * only by calls to <code>copyFrom()</code>.  Will be <code>-1</code> if an
    * index has not been specified.
    */
   private int m_index = -1;
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppExtensionCallIdContext";

   // private xml constant
   private static final String XML_ATTR_EXT_REF = "extRef";
   private static final String XML_ATTR_INDEX = "index";
   
}

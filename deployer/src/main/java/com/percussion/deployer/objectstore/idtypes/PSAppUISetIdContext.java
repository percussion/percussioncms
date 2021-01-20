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
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;

/**
 * ID Context to represent a ui set
 */
public class PSAppUISetIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context from a ui set.  
    * 
    * @param uiSet The uiSet, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>uiSet</code> is 
    * <code>null</code>.
    */
   public PSAppUISetIdContext(PSUISet uiSet)
   {
      if (uiSet == null)
         throw new IllegalArgumentException("uiSet may not be null");
         
      String name = uiSet.getName();
      if (name != null && name.trim().length() > 0)
         m_name = name;
      else
         m_name = null;
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
   public PSAppUISetIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Get the name of this uiset if one has been set.
    * 
    * @return The name, may be <code>null</code>, never empty.
    */
   public String getName()
   {
      return m_name;
   }

   //see PSApplicationIdContext
   public String getDisplayText()
   {
      String text;
      if (m_name == null)
      {
         text = getBundle().getString("appIdUISet");
      }
      else
      {
         text = MessageFormat.format(getBundle().getString(
               "appIdUISetName"), new Object[] {m_name});
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
    * &lt;!ELEMENT PSXAppUISetIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppUISetIdContext
    *    name CDATA #IMPLIED
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
      if (m_name != null)
         root.setAttribute(XML_ATTR_NAME, m_name);
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

      m_name = sourceNode.getAttribute(XML_ATTR_NAME);
      if (m_name != null && m_name.trim().length() == 0)
         m_name = null;
         
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

      if (!(obj instanceof PSAppUISetIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppUISetIdContext other = (PSAppUISetIdContext)obj;
      m_name = other.m_name;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppUISetIdContext))
         isEqual = false;
      else 
      {
         PSAppUISetIdContext other = (PSAppUISetIdContext)obj;
         if (m_name == null ^ other.m_name == null)
            isEqual = false;
         else if (m_name != null && !m_name.equals(other.m_name))
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   public int hashCode()
   {
      return (m_name == null ? 0 : m_name.hashCode()) + super.hashCode();
   }

   /**
    * Name of the ui Set this object represents.  May be <code>null</code> or 
    * but not empty if the uiset does not have a name, modified only by a call 
    * to <code>copyFrom()</code>
    */
   private String m_name;
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppUISetIdContext";

   // private xml constant
   private static final String XML_ATTR_NAME = "name";
   
}

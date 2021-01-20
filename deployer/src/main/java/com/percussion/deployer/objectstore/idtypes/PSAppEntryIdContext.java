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
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;

/**
 * ID Context to represent a <code>PSEntry</code> object
 */
public class PSAppEntryIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context from the entry object
    * 
    * @param entry The entry, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>call</code> is 
    * <code>null</code>.
    */
   public PSAppEntryIdContext(PSEntry entry)
   {
      if (entry == null)
         throw new IllegalArgumentException("entry may not be null");
         
      m_entry = entry;
      m_origEntry = entry;
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
   public PSAppEntryIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Get the current entry represented by this context.
    * 
    * @return The entry, never <code>null</code>.
    */
   public PSEntry getEntry()
   {
      return m_entry;
   }

   /**
    * Get the entry this object was constructed with.
    * 
    * @return The entry, never <code>null</code>.
    */
   public PSEntry getOriginalEntry()
   {
      return m_origEntry;
   }
   
   
   //see PSApplicationIdContext
   public String getDisplayText()
   {
      
      String text = MessageFormat.format(getBundle().getString(
         "appIdEntry"), new Object[] {m_entry.getLabel().getText(), 
            m_entry.getValue(), String.valueOf(m_entry.getSequence())});
      text = addParentDisplayText(text);
      
      return text;
   }
   
   //see PSApplicationIdContext
   public void updateCtxValue(Object value)
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");
      
      if (!(value instanceof PSEntry))
         throw new IllegalArgumentException("value must be instanceof PSEntry");
            
       m_entry = (PSEntry)value;
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext that
    *    is this context's parent context.
    * -->
    * <pre><code>
    * &lt;!ELEMENT PSXAppEntryIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppEntryIdContext
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
      root.appendChild(m_entry.toXml(doc));
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

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element entryEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (entryEl == null)
      {
         Object[] args = {XML_NODE_NAME, "null", "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      m_entry = new PSEntry(entryEl, null, null);
      m_origEntry = m_entry;
      Element ctxEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if (ctxEl != null)
         setParentCtx(PSApplicationIDContextFactory.fromXml(ctxEl));
   }
   
   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSAppEntryIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppEntryIdContext other = (PSAppEntryIdContext)obj;
      m_entry = other.m_entry;
      m_origEntry = m_entry;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppEntryIdContext))
         isEqual = false;
      else 
      {
         PSAppEntryIdContext other = (PSAppEntryIdContext)obj;
         if (!m_entry.equals(other.m_entry))
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   // see IPSDeployComponent
   public int hashCode()
   {
      return m_entry.hashCode() + super.hashCode();
   }

   /**
    * Entry this context represents.  Never <code>null</code> after ctor, 
    * modified by a calls to <code>copyFrom()</code> and 
    * <code>updateCtxValue()</code>.
    */
   private PSEntry m_entry;
   
   /**
    * The entry this context represented at construction time, initially the 
    * same as {@link #m_entry}, but immutable after contruction. This value is 
    * not used as part of {@link #equals(Object)}, {@link #hashCode()}, nor is 
    * it serialized to and from this object's XML representation.
    */
   private PSEntry m_origEntry;
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppEntryIdContext";

}

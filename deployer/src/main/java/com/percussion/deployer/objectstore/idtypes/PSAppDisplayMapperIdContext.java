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
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;

/**
 * Context to represent a display mapper in a content editor.
 */
public class PSAppDisplayMapperIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context from its member data.
    * 
    * @param mapper The mapper within a content editor this context 
    * represents.  May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>fieldRef</code> is invalid.
    */
   public PSAppDisplayMapperIdContext(PSDisplayMapper mapper)
   {
      if (mapper == null)
         throw new IllegalArgumentException("mapper may not be null");
      
      m_fieldSetRef = mapper.getFieldSetRef();
      m_id = mapper.getId();         
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
   public PSAppDisplayMapperIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the field set ref of the mapper to which this context refers.
    * 
    * @return The ref, never <code>null</code> or empty.
    */
   public String getFieldSetRef()
   {
      return m_fieldSetRef;
   }
   
   /**
    * Get the id of the mapper to which this context refers.
    * 
    * @return the id.
    */
   public int getId()
   {
      return m_id;
   }
   
   //see PSApplicationIdContext
   public String getDisplayText()
   {
      String text = MessageFormat.format(getBundle().getString(
         "appIdDisplayMapper"), new Object[] {String.valueOf(m_id), 
            m_fieldSetRef});
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
    * &lt;!ELEMENT PSXAppDisplayMapperIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppDisplayMapperIdContext
    *    fieldSetRef CDATA #REQUIRED
    *    id CDATA #REQUIRED
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
      root.setAttribute(XML_ATTR_FIELD_SET_REF, m_fieldSetRef);
      root.setAttribute(XML_ATTR_ID, String.valueOf(m_id));
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

      m_fieldSetRef = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_FIELD_SET_REF);
      String strId = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_ID);
      try
      {
         m_id = Integer.parseInt(strId);
      }
      catch (NumberFormatException e)
      {
         Object[] args = {XML_NODE_NAME, XML_ATTR_ID, strId};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
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

      if (!(obj instanceof PSAppDisplayMapperIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppDisplayMapperIdContext other = (PSAppDisplayMapperIdContext)obj;
      m_fieldSetRef = other.m_fieldSetRef;
      m_id = other.m_id;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppDisplayMapperIdContext))
         isEqual = false;
      else 
      {
         PSAppDisplayMapperIdContext other = (PSAppDisplayMapperIdContext)obj;
         if (!m_fieldSetRef.equals(other.m_fieldSetRef))
            isEqual = false;
         else if (m_id != other.m_id)
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   // see IPSDeployComponent
   public int hashCode()
   {
      return m_fieldSetRef.hashCode() + m_id + super.hashCode();
   }

   /**
    * Name of the fieldSet ref of the mapper this context represents, never 
    * <code>null</code> or empty after ctor, may be modified by a call to 
    * <code>copyFrom()</code>.
    */
   private String m_fieldSetRef;
   
   /**
    * ID of the mapper this context represents, intialized during ctor, may be 
    * modified by a call to <code>copyFrom()</code>.
    */
   private int m_id;
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppDisplayMapperIdContext";

   // private xml constant
   private static final String XML_ATTR_FIELD_SET_REF = "fieldSetRef";
   private static final String XML_ATTR_ID = "id";
   
}

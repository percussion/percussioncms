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
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;


/**
 * ID Context to represent an item whose context is determined only by its
 * ordinal position in its parent context.
 */
public class PSAppDataMappingIdContext extends PSApplicationIdContext
{
   /**
    * Construct this from the source mapping
    * 
    * @param mapping The source mapping, may not be <code>null</code>.
    * @param type One of the <code>TYPE_XXX</code> constants to indicate which
    * part of the mapping the id represents.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSAppDataMappingIdContext(PSDataMapping mapping, int type)
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
         
      if (!validateType(type))
         throw new IllegalArgumentException("invalid type");
      
      m_beMapping = mapping.getBackEndMapping().toString();
      m_docMapping = mapping.getDocumentMapping().toString();
      
      m_type = type;
      setOrigMapping();
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
   public PSAppDataMappingIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Get the <code>String</code> representation of this mapping's document
    * mapping.
    * 
    * @return The text, never <code>null</code> or empty.
    */
   public String getDocMappingText()
   {
      return m_docMapping;
   }
   
   /**
    * Get the <code>String</code> representation of this mapping's backend
    * mapping.
    * 
    * @return The text, never <code>null</code> or empty.
    */
   public String getBackendMappingText()
   {
      return m_beMapping;
   }
   
   
   
   /**
    * Get the type indicating which portion of this context's mapping is
    * a literal id.
    * 
    * @return The type, one of the <code>TYPE_xxx</code> values.
    */
   public int getType()
   {
      return m_type;
   }
   
   /**
    * Convenience method that calls 
    * {@link #isSameMapping(PSDataMapping, boolean) 
    * isSameMapping(mapping, false)}
    */
   public boolean isSameMapping(PSDataMapping mapping)
   {
      return isSameMapping(mapping, false);
   }
   
   /**
    * Determines if the supplied mapping is the same mapping this context was 
    * constructed with.
    * 
    * @param mapping The mapping to check, may not be <code>null</code>.
    * @param compareOriginal <code>true</code> to compare the mappings with
    * which this object was originally constructed, <code>false</code> to use 
    * the current values one of which may have been updated by a call to
    * {@link #updateCtxValue(Object)} or 
    * {@link #ctxValueUpdated(PSApplicationIdContext)}
    *  
    * @return <code>true</code> if it is the same, <code>false</code> otherwise.
    * 
    * @throws IllegalArgumentException if <code>mapping</code> is 
    * <code>null</code>.
    */
   public boolean isSameMapping(PSDataMapping mapping, boolean compareOriginal)
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      
      String beMapping = m_beMapping;
      String docMapping = m_docMapping;
      if (m_type == TYPE_BACK_END && compareOriginal)
         beMapping = m_origMapping;
      else if (m_type == TYPE_XML && compareOriginal)
         docMapping = m_origMapping;
                  
      if (mapping.getBackEndMapping().toString().equals(beMapping) && 
         mapping.getDocumentMapping().toString().equals(docMapping))
      {
         return true;
      }
      return false;
   }
   
   //see PSApplicationIdContext
   public String getDisplayText()
   {
      String key = "appIdDataMapping";
      String sideKey = key + TYPE_ENUM[m_type];
      String side = getBundle().getString(sideKey);
      
      String text = MessageFormat.format(getBundle().getString(
         key), new Object[] {side, m_beMapping, m_docMapping});
      text = addParentDisplayText(text);
      
      return text;
   }
   
   //see PSApplicationIdContext
   public void updateCtxValue(Object value)
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");
      
      if (!(value instanceof IPSReplacementValue))
         throw new IllegalArgumentException(
            "value must be instanceof IPSReplacementValue");
            
      if (m_type == TYPE_BACK_END)
         m_beMapping = value.toString();
      else if (m_type == TYPE_XML)
         m_docMapping = value.toString();
      
      // now notify listeners
      notifyCtxChangeListeners(this);
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext that
    *    is this context's parent context.
    * -->
    * <pre><code>
    * &lt;!ELEMENT PSXAppDataMappingIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppDataMappingIdContext
    *    beMapping CDATA #REQUIRED
    *    docMapping CDATA #REQUIRED
    *    type CDATA #REQUIRED
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
      root.setAttribute(XML_ATTR_BE_MAPPING, m_beMapping);
      root.setAttribute(XML_ATTR_DOC_MAPPING, m_docMapping);
      root.setAttribute(XML_ATTR_TYPE, TYPE_ENUM[m_type]);
      
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

      m_beMapping = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_BE_MAPPING);
      m_docMapping = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_DOC_MAPPING);
      String strType = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_TYPE);
      
      m_type = -1;
      for (int i = 0; i < TYPE_ENUM.length && m_type == -1; i++) 
      {
         if (TYPE_ENUM[i].equals(strType))
            m_type = i;
      }
      if (!validateType(m_type))
      {
         Object[] args = {XML_NODE_NAME, XML_ATTR_TYPE, strType};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      setOrigMapping();
      
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

      if (!(obj instanceof PSAppDataMappingIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppDataMappingIdContext other = (PSAppDataMappingIdContext)obj;
      m_beMapping = other.m_beMapping;
      m_docMapping = other.m_docMapping;
      m_type = other.m_type;
      setOrigMapping();      
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppDataMappingIdContext))
         isEqual = false;
      else 
      {
         PSAppDataMappingIdContext other = (PSAppDataMappingIdContext)obj;
         if (!m_beMapping.equals(other.m_beMapping))
            isEqual = false;
         else if (!m_docMapping.equals(other.m_docMapping))
            isEqual = false;
         else if (m_type != other.m_type)
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   // see IPSDeployComponent
   public int hashCode()
   {
      return m_beMapping.hashCode() + m_docMapping.hashCode() + m_type + 
         super.hashCode();
   }
   
   // see base class
   protected boolean hasSameData(PSApplicationIdContext ctx)
   {
      boolean hasSame = false;
      if (ctx instanceof PSAppDataMappingIdContext)
      {
         PSAppDataMappingIdContext other = (PSAppDataMappingIdContext)ctx;
         if (m_beMapping.equals(other.m_beMapping) && 
            m_docMapping.equals(other.m_docMapping))
         {
            hasSame = true;
         }
      }
      
      return hasSame;
   }
   
   // see base class
   protected void checkAddListener(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      // add as listener if same data mapping
      if (ctx instanceof PSAppDataMappingIdContext)
      {
         PSAppDataMappingIdContext other = (PSAppDataMappingIdContext)ctx;
         if (m_beMapping.equals(other.m_beMapping) && 
            m_docMapping.equals(other.m_docMapping))
         {
            ctx.addCtxChangeListener(this);
            addCtxChangeListener(ctx);
         }         
      }
   }
   
   // see base class
   protected void checkRemoveListener(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      // remove as listener if same data mapping
      if (ctx instanceof PSAppDataMappingIdContext)
      {
         PSAppDataMappingIdContext other = (PSAppDataMappingIdContext)ctx;
         if (m_beMapping.equals(other.m_beMapping) && 
            m_docMapping.equals(other.m_docMapping))
         {
            ctx.removeCtxChangeListener(this);
            removeCtxChangeListener(ctx);
         }         
      }
   }
   
   // see base class
   protected void ctxValueUpdated(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      if (ctx instanceof PSAppDataMappingIdContext)
      {
         PSAppDataMappingIdContext other = (PSAppDataMappingIdContext)ctx;
         m_beMapping = other.m_beMapping;
         m_docMapping = other.m_docMapping;
      }
   }
   
   /**
    * Validates the supplied type is one of the <code>TYPE_XXX</code> values.
    * 
    * @param type The value to check.
    * 
    * @return <code>true</code> if the type is valid, <code>false</code>
    * otherwise.
    */
   private boolean validateType(int type)
   {
      return type >=0 && type < TYPE_ENUM.length;
   }
   
   /**
    * Sets the value of the {@link #m_origMapping} member based on the mapping
    * type.  Assumes all other member variables have been initialized.
    */
   private void setOrigMapping()
   {
      if (m_type == TYPE_BACK_END)
         m_origMapping = m_beMapping;
      else if (m_type == TYPE_XML)
         m_origMapping = m_docMapping;
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppDataMappingIdContext";
   
   /**
    * Text representation of the mapping's backend mapping, never 
    * <code>null</code> or empty, modified by calls to <code>copyFrom()</code> 
    * and <code>updateCtxValue()</code>.
    */
   private String m_beMapping;
   
   /**
    * Text representation of the mapping's document mapping, never 
    * <code>null</code> or empty, modified by calls to <code>copyFrom()</code> 
    * and <code>updateCtxValue()</code>.
    */
   private String m_docMapping;
   
   /**
    * The mapping this context represented at construction time, 
    * initially either the same as {@link #m_beMapping} or {@link #m_docMapping}
    * depending on the value of {@link #m_type}, immutable after contruction.
    * This value is not used as part of {@link #equals(Object)}, 
    * {@link #hashCode()}, nor is it serialized to and from this object's
    * XML representation.
    */
   private String m_origMapping;

   /**
    * Indicates which part of the mapping this context specifies, one of the 
    * <code>TYPE_xxx</code> values.
    */
   private int m_type;
   
   /**
    * Constant to indicate type is for the back-end side of the mapping
    */
   public static final int TYPE_BACK_END = 0;
   
   /**
    * Constant to indicate type is for the Xml side of the mapping
    */
   public static final int TYPE_XML = 1;
   
   /**
    * Constant to indicate type is for the conditional of the mapping
    */
   public static final int TYPE_COND = 2;
   
   /**
    * Enumeration of string constants representing each of the 
    * <code>TYPE_XXX</code> values, for Xml serialization.  
    */
   public static final String[] TYPE_ENUM = {"Backend", "Xml", "Conditional"};
   
   // private xml constants
   private static final String XML_ATTR_BE_MAPPING = "beMapping";
   private static final String XML_ATTR_DOC_MAPPING = "docMapping";
   private static final String XML_ATTR_TYPE = "type";
   
}

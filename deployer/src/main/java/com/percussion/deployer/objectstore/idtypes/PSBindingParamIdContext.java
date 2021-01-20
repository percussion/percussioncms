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
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;

/**
 * ID Context to represent a parameter in a JEXL binding that needs idmapping
 * This context always has a parent context: PSBindingIdContext. The same 
 * parameter can appear n-times in an expression. This context holds the 
 * occurence information.
 * 
 * @author vamsinukala
 */

public class PSBindingParamIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context from a binding context.
    * 
    * @param index The index into the param list of the JEXL binding this
    * parameter is from.  May not be less than 0.
    * @param occur the occurence of this parameter in the JEXL binding. May not
    * be less than 0
    * @param param The param, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSBindingParamIdContext(int index, int occur, PSTextLiteral param)
   {
      if (!validateIndex(index))
         throw new IllegalArgumentException("index is invalid");
         
      if (param == null)
         throw new IllegalArgumentException("param may not be null");
         
      m_index = index;
      m_occurence  = occur;
      m_param = param;
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
   public PSBindingParamIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the JEXL binding param to which this context refers.
    * 
    * @return The param, never <code>null</code>.
    */
   public PSTextLiteral getParam()
   {
      return m_param;
   }
   
   
   /**
    * Get index of this param within the JEXL binding context
    * 
    * @return The index, >=0.
    */
   public int getIndex()
   {
      return m_index;
   }

   /**
    * Get the name of this param.
    * 
    * @return The name, may be <code>null</code> if 
    * {@link #setParamName(String)} has not been called, never empty.
    */
   public String getParamName()
   {
      return m_paramName;
   }
   
   /**
    * the occurence of this parameter in a JEXL expression
    * @return the occurence
    */
   public int getOccurence()
   {
      return m_occurence;
   }

   /**
    * see above
    * @param occurence
    */
   public void setOccurence(int occurence)
   {
      this.m_occurence = occurence;
   }
   
   /**
    * Set the name of this param.
    * 
    * @param name The name to set, may not be <code>null</code> or empty.
    */
   public void setParamName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_paramName = name;
   }
   
   //see PSApplicationIdContext
   public String getDisplayText()
   {
      // handle different resource and arg list if name is specified
      Object[] args = new Object[2];

      args[0] = String.valueOf(m_index);
      args[1] = m_param.getText();
      String text = MessageFormat.format(getBundle().getString(
            "bindingCtxParamIndexValue"), args);
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
            
      if ( value instanceof PSTextLiteral)
         m_param = (PSTextLiteral)value;
   }

   // see base class
   public String getIdentifier()
   {
      return m_paramName;
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext that
    *    is this context's parent context.
    * -->
    * <pre><code>
    * &lt;!ELEMENT PSXBindingParamIdContext (PSXBindingParamValue?>
    * &lt;!ATTLIST PSXBindingParamIdContext
    *    index CDATA #REQUIRED
    *    occur CDATA #REQUIRED
    *    paramName CDATA #IMPLIED
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
      root.setAttribute(XML_ATTR_INDEX, String.valueOf(m_index));
      root.setAttribute(XML_ATTR_OCCUR, String.valueOf(m_occurence));
      if (m_paramName != null)
         root.setAttribute(XML_ATTR_NAME, m_paramName);
      root.appendChild(m_param.toXml(doc));
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

      String strIndex = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_INDEX);
      m_index = -1;
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
      
      String strOccur = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_OCCUR);
      m_occurence = -1;
      try
      {
         m_occurence = Integer.parseInt(strOccur);
      }
      catch (NumberFormatException ex)
      {
         // fall thru
      }
     
      if ( m_occurence < 0 )
      {
         Object[] args = {XML_NODE_NAME, XML_ATTR_OCCUR, strOccur};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      } 
      
      m_paramName = null;
      String paramName = sourceNode.getAttribute(XML_ATTR_NAME);
      if (paramName != null && paramName.trim().length() > 0)
      {
         m_paramName = paramName;
      }
            
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element paramEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (paramEl == null)
      {
         Object[] args = {XML_NODE_NAME, "null", "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      
      m_param = new PSTextLiteral(paramEl, null, null);
      
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

      if (!(obj instanceof PSBindingParamIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSBindingParamIdContext other = (PSBindingParamIdContext)obj;
      m_index = other.m_index;
      m_occurence = other.m_occurence;
      m_param = other.m_param;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSBindingParamIdContext))
         isEqual = false;
      else 
      {
         // do not include param name as it is dynamic and transient
         PSBindingParamIdContext other = (PSBindingParamIdContext)obj;
         if (m_index != other.m_index)
            isEqual = false;
         else if ( m_occurence != other.m_occurence )
            isEqual = false;
         else if (!m_param.equals(other.m_param))
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   // see IPSDeployComponent
   public int hashCode()
   {
      // do not include param name as it is dynamic and transient
      return m_index + m_param.hashCode() + m_occurence + super.hashCode();
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
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXBindingParamIdContext";
 
   /**
    * the occurence of this parameter in the expression such as
    * a hypothetical stupid expression
    * occurence is 0, 2, 4 for "301"
    * $rx.db.foo("301", "356", "301", "344", "301")
    */
   private int m_occurence;
   /**
    * Index of this param in the parent binding context.  Intialized
    * during ctor, modfied only by calls to <code>copyFrom()</code>.
    */
   private int m_index;
   
   /**
    * Name of this param in the binding's param list.  Initially
    * <code>null</code>, modified by calls to {@link #setParamName(String)}.
    */
   private transient String m_paramName = null;
   
   /**
    * The param this context refers to, never <code>null</code> after
    * construction, modified by a calls to <code>copyFrom()</code> 
    * and <code>updateCtxValue()</code>.
    */
   private PSTextLiteral m_param;
   
   // private xml constant
   private static final String XML_ATTR_INDEX = "index";
   private static final String XML_ATTR_OCCUR = "occur";
   private static final String XML_ATTR_NAME = "paramName";
   
   
}

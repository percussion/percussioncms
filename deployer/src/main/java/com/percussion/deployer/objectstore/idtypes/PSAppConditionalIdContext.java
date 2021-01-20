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
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Context to represent a conditional in an application.
 */
public class PSAppConditionalIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context from the conditional object it represents.
    *
    * @param cond The conditional, may not be <code>null</code>.
    * @param type One of the <code>TYPE_XXX</code> constants to indicate which
    * part of the conditional the id represents.
    */
   public PSAppConditionalIdContext(PSConditional cond, int type)
   {
      if (cond == null)
         throw new IllegalArgumentException("cond may not be null");

      if (!validateType(type))
         throw new IllegalArgumentException("invalid type");

      m_cond = cond;
      m_origCond = (PSConditional)cond.clone();
      m_type = type;
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSAppConditionalIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Convenience method that calls 
    * {@link #isSameConditional(PSConditional, boolean) 
    * isSameConditional(cond, false)}
    */
   public boolean isSameConditional(PSConditional cond)
   {
      return isSameConditional(cond, false);
   }

   /**
    * Determine if the supplied conditional is equivalent to the conditional
    * represented by this context
    *
    * @param cond The conditional, may not be <code>null</code>.
    * @param compareOriginal <code>true</code> to compare the conditional with
    * which this object was originally constructed, <code>false</code> to use 
    * the current conditional value which may have been updated by a call to
    * {@link #updateCtxValue(Object)} or 
    * {@link #ctxValueUpdated(PSApplicationIdContext)}
    * 
    * @return <code>true</code> if they are the same, <code>false</code>
    * otherwise.
    */
   public boolean isSameConditional(PSConditional cond, boolean compareOriginal)
   {
      if (cond == null)
         throw new IllegalArgumentException("cond may not be null");

      boolean isSame = true;

      PSConditional thisCond = compareOriginal ? m_origCond : m_cond;

      if (!cond.getBoolean().equals(thisCond.getBoolean()))
         isSame = false;
      else if (!cond.getOperator().equals(thisCond.getOperator()))
         isSame = false;
      else if (m_type == TYPE_VALUE)
      {
         if (!cond.getValue().equals(thisCond.getValue()))
           isSame = false;
         else if (!cond.getVariable().getValueDisplayText().equals(
         thisCond.getVariable().getValueDisplayText()))
         {
           isSame = false;
         }
      }
      else
      {
         if (!cond.getVariable().equals(thisCond.getVariable()))
           isSame = false;
         else if (!cond.getValue().getValueDisplayText().equals(
         thisCond.getValue().getValueDisplayText()))
         {
            isSame = false;
         }
      }

      return isSame;      
   }
   
   /**
    * Get the type indicating which portion of this context's conditional is
    * a literal id.
    *
    * @return The type, one of the <code>TYPE_xxx</code> values.
    */
   public int getType()
   {
      return m_type;
   }

   //see PSApplicationIdContext
   public String getDisplayText()
   {
      ResourceBundle bundle = getBundle();
      String side = null;
      if (m_type == TYPE_VARIABLE)
         side = bundle.getString("appIdCtxConditionalVariable");
      else
         side = bundle.getString("appIdCtxConditionalValue");

      Object[] args = {side, m_cond.getVariable().getValueDisplayText(),
         m_cond.getOperator(), m_cond.getValue(), m_cond.getBoolean()};
      String text = MessageFormat.format(getBundle().getString(
         "appIdCtxConditional"), args);
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

      IPSReplacementValue replVal = (IPSReplacementValue)value;
      try
      {
         if (m_type == TYPE_VARIABLE)
            m_cond.setVariable(replVal);
         else
            m_cond.setValue(replVal);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      // now notify listeners
      notifyCtxChangeListeners(this);
   }

   // see base class
   public String getIdentifier()
   {
      return m_type == TYPE_VALUE ? m_cond.getVariable().getValueText() : 
         m_cond.getValue().getValueText();
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext that
    *    is this context's parent context.
    * -->
    * <pre><code>
    * &lt;!ELEMENT PSXAppConditionalIdContext (PSXConditional,
    *    PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppConditionalIdContext
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
      root.setAttribute(XML_ATTR_TYPE, TYPE_ENUM[m_type]);
      root.appendChild(m_cond.toXml(doc));
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


      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element condEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (condEl == null)
      {
         Object[] args = {XML_NODE_NAME, "null", "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      String condNodeName = condEl.getNodeName();
      if (condNodeName.equals(PSConditional.ms_NodeType))
         m_cond = new PSConditional(condEl, null, null);
      else if (condNodeName.equals(PSWhereClause.ms_NodeType))
         m_cond = new PSWhereClause(condEl, null, null);
      else
      {
         Object[] args = {PSConditional.ms_NodeType, condEl.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      m_origCond = (PSConditional)m_cond.clone();

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

      if (!(obj instanceof PSAppConditionalIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppConditionalIdContext other = (PSAppConditionalIdContext)obj;
      m_type = other.m_type;
      m_cond = other.m_cond;
      m_origCond = (PSConditional)m_cond.clone();
      super.copyFrom(other);
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppConditionalIdContext))
         isEqual = false;
      else
      {
         PSAppConditionalIdContext other = (PSAppConditionalIdContext)obj;
         if (m_type != other.m_type)
            isEqual = false;
         else if (!isSameConditional(other.m_cond))
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }

   // see IPSDeployComponent
   public int hashCode()
   {
      return m_cond.hashCode() + m_type + super.hashCode();
   }

   // see base class
   protected boolean hasSameData(PSApplicationIdContext ctx)
   {
      boolean hasSame = false;
      if (ctx instanceof PSAppConditionalIdContext)
      {
         PSAppConditionalIdContext other = (PSAppConditionalIdContext)ctx;
         hasSame = isSameConditional(other.m_cond);          
      }
      
      return hasSame;
   }
   
   // see base class
   protected void checkAddListener(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      // add as listener if same cond
      if (ctx instanceof PSAppConditionalIdContext)
      {
         PSAppConditionalIdContext other = (PSAppConditionalIdContext)ctx;
         if (isSameConditional(other.m_cond))
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
      
      // remove as listener if same cond
      if (ctx instanceof PSAppConditionalIdContext)
      {
         PSAppConditionalIdContext other = (PSAppConditionalIdContext)ctx;
         if (isSameConditional(other.m_cond))
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
      
      if (ctx instanceof PSAppConditionalIdContext)
      {
         PSAppConditionalIdContext other = (PSAppConditionalIdContext)ctx;
         m_cond = (PSConditional)other.m_cond.clone();
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
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppConditionalContext";

   /**
    * The conditional object this context represents, never <code>null</code>
    * after construction, modified by a calls to <code>copyFrom()</code> and
    * <code>updateCtxValue()</code>
    */
   private PSConditional m_cond;

   /**
    * The conditional object this context represented at construction time, 
    * initially a clone of {@link #m_cond}, but immutable after contruction.
    * This value is not used as part of {@link #equals(Object)}, 
    * {@link #hashCode()}, nor is it serialized to and from this object's
    * XML representation.
    */   
   private PSConditional m_origCond;

   /**
    * Indicates which part of the condtional this object represents, one of the
    * <code>TYPE_XXX</code> values.  Initialized during construction, only
    * modified by a call to <code>copyFrom()</code>.
    */
   private int m_type;

   /**
    * Constant to indicate this context represents the value side of the
    * conditional.
    */
   public static final int TYPE_VALUE = 0;

   /**
    * Constant to indicate this context represents the variable side of the
    * conditional.
    */
   public static final int TYPE_VARIABLE = 1;

   /**
    * Enumeration of string constants representing each of the
    * <code>TYPE_XXX</code> values, for Xml serialization.
    */
   private static final String[] TYPE_ENUM = {"value", "variable"};

   // private xml constant
   private static final String XML_ATTR_TYPE = "type";

}

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

import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSExecutionContext;
import com.percussion.util.PSCollection;
import com.percussion.util.PSXMLDomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the PSXConditionalEffect element defined in
 * sys_RelationshipConfig.dtd.
 */
public class PSConditionalEffect extends PSComponent
{
   /**
    * Construct a Java object from its XML representation.
    * 
    * @param sourceNode the XML element node to construct this object from, not
    * <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object, may
    * be <code>null</code>.
    * @param parentComponents the parent objects of this object, may be
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    * appropriate type
    */
   public PSConditionalEffect(Element sourceNode, IPSDocument parentDoc,
         List parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs this object with supplied extension call.
    * 
    * @param effect the extension to use as effect, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if effect is <code>null</code>
    */
   public PSConditionalEffect(PSExtensionCall effect)
   {
      if (effect == null)
         throw new IllegalArgumentException("effect may not be null.");

      m_effect = effect;
   }

   /**
    * Sets the conditions to be satisfied to execute this object's extension.
    * 
    * @param conds list of conditions, may not be <code>null</code>, can be
    * empty.
    * 
    * @throws IllegalArgumentException if conds is <code>null</code>.
    */
   public void setConditions(Iterator conds)
   {
      if (conds == null)
         throw new IllegalArgumentException("conds may not be null.");

      m_conditions.clear();
      while (conds.hasNext())
         m_conditions.add(conds.next());
   }

   /**
    * Returns the effect.
    * 
    * @return the effect, never <code>null</code>.
    */
   public PSExtensionCall getEffect()
   {
      return m_effect;
   }

   /**
    * @return activation end point for this effect. One of the
    * ACTIVATION_ENDPOINT_XXXX values defined in {@link PSRelationshipConfig}.
    * Never <code>null</code> or empty.
    */
   public String getActivationEndPoint()
   {
      return m_activationEndPoint;
   }

   /**
    * Set activation end point for this effect.
    * 
    * @param activationEndPoint must be one of the ACTIVATION_ENDPOINT_XXXX
    * values defined in {@link PSRelationshipConfig}.
    */
   public void setActivationEndPoint(String activationEndPoint)
   {
      if (!PSRelationshipConfig.isActivationEndPointValid(activationEndPoint))
      {
         throw new IllegalArgumentException(
               "activationEndPoint must be one of ACTIVATION_ENDPOINT_XXXX values");
      }
      m_activationEndPoint = activationEndPoint;
   }

   /**
    * Get the current collection of conditions (a collection of {@link PSRule}
    * objects).
    * 
    * @return the collection of conditions {@link PSRule}, never
    * <code>null</code>, may be empty.
    */
   public Iterator getConditions()
   {
      return m_conditions.iterator();
   }

   /** @see IPSComponent */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
         List parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      Element node = null;
      try
      {
         // REQUIRED: attribute "activationEndpoint", default value is "owner"
         m_activationEndPoint = sourceNode
               .getAttribute(XML_ATTR_ACTIVATIONENDPOINT);
         if (!PSRelationshipConfig
               .isActivationEndPointValid(m_activationEndPoint))
            m_activationEndPoint = PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER;

         // REQUIRED: the PSXExtensionCall element
         node = PSXMLDomUtil.getFirstElementChild(sourceNode,
               PSExtensionCall.ms_NodeType);
         m_effect = new PSExtensionCall(node, parentDoc, parentComponents);

         // OPTIONAL: get the conditions
         m_conditions.clear();
         Element optionalNode = PSXMLDomUtil.getNextElementSibling(node);
         boolean hasConditions = false;
         if (optionalNode != null
               && optionalNode.getNodeName().equalsIgnoreCase(ELEM_CONDITIONS))
         {
            hasConditions = true;
            node = PSXMLDomUtil.getFirstElementChild(optionalNode);
            while (node != null
                  && node.getNodeName().equalsIgnoreCase(PSRule.XML_NODE_NAME))
            {
               m_conditions.add(new PSRule(node, parentDoc, parentComponents));
               node = PSXMLDomUtil.getNextElementSibling(node);
            }
         }

         // OPTIONAL: get the Execution Contexts
         m_exeContexts.clear();
         Element exeCtxNode = null;
         if (hasConditions)
            exeCtxNode = PSXMLDomUtil.getNextElementSibling(optionalNode);
         else
            exeCtxNode = optionalNode;
         if (exeCtxNode != null
               && exeCtxNode.getNodeName().equalsIgnoreCase(
                     ELEM_EXE_CONTEXT_SET))
         {
            node = PSXMLDomUtil.getFirstElementChild(exeCtxNode);
            while (node != null
                  && node.getNodeName().equalsIgnoreCase(ELEM_EXE_CONTEXT))
            {
               String name = node.getAttribute(ATTR_TYPE);
               Integer id = ms_ContextNameIdMapper.get(name.toLowerCase());
               if (id != null)
                  m_exeContexts.add(id);
               else
                  log.warn("Ignore invalid Execution Context, ' {} ,'.",name);

               node = PSXMLDomUtil.getNextElementSibling(node);
            }
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);

      // set the activationEndPoint attribute
      root.setAttribute(XML_ATTR_ACTIVATIONENDPOINT, m_activationEndPoint);

      // store the effect
      root.appendChild(m_effect.toXml(doc));

      // store the conditions
      Iterator conditions = getConditions();
      if (conditions.hasNext())
      {
         Element elem = doc.createElement(ELEM_CONDITIONS);
         while (conditions.hasNext())
            elem.appendChild(((IPSComponent) conditions.next()).toXml(doc));

         root.appendChild(elem);
      }
      if (m_exeContexts.size() > 0)
      {
         Element cxtSet = doc.createElement(ELEM_EXE_CONTEXT_SET);
         Element node;
         for (Integer ctx : m_exeContexts)
         {
            node = doc.createElement(ELEM_EXE_CONTEXT);
            node.setAttribute(ATTR_TYPE, ms_ContextIdNameMapper.get(ctx));
            cxtSet.appendChild(node);
         }
         root.appendChild(cxtSet);
      }

      return root;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      PSConditionalEffect clone = new PSConditionalEffect(
            (PSExtensionCall) m_effect.clone());

      clone.m_activationEndPoint = m_activationEndPoint;
      clone.m_conditions = new PSCollection(PSRule.class);
      Iterator iter = m_conditions.iterator();
      while (iter.hasNext())
         clone.m_conditions.add(((PSRule) iter.next()).clone());
      clone.m_exeContexts.addAll(m_exeContexts);

      return clone;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSConditionalEffect))
         return false;

      PSConditionalEffect other = (PSConditionalEffect) obj;

      return new EqualsBuilder().append(m_activationEndPoint,
            other.m_activationEndPoint).append(m_conditions,
            other.m_conditions).append(m_exeContexts, other.m_exeContexts)
            .append(m_effect, other.m_effect).isEquals();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return new HashCodeBuilder(32, 2).appendSuper(super.hashCode()).append(
            m_activationEndPoint).append(m_conditions).append(m_exeContexts)
            .append(m_effect).toHashCode();
   }

   /**
    * Set the Execution Contexts.
    * 
    * @param exeContexts a list of Execution Contexts. Each element must be
    * between {@link IPSExcecutionContext#VALIDATION_MIN} and
    * {@link IPSExcecutionContext#VALIDATION_MAX}. It may be EMPTY if unknown,
    * never <code>null</code>.
    */
   public void setExecutionContexts(Collection<Integer> exeContexts)
   {
      if (exeContexts == null)
         throw new IllegalArgumentException("exeContexts must not be null");

      for (Integer ctx : exeContexts)
      {
         if (!PSExecutionContext.isContextValid(ctx.intValue()))
            throw new IllegalArgumentException("The Execution Context, '"
                  + ctx + "' is not valid.");
      }
      m_exeContexts = exeContexts;
   }

   /**
    * Get a list of Execution Contexts that is relevent for this effect.
    * 
    * @return the Execution Context list. Each element is between
    * {@link IPSExcecutionContext#VALIDATION_MIN} and
    * {@link IPSExcecutionContext#VALIDATION_MAX}. It may be EMPTY if unknown,
    * never <code>null</code>.
    */
   public Collection<Integer> getExecutionContexts()
   {
      return m_exeContexts;
   }

   /**
    * Determines whether the supplied Execution Context is interested by this
    * effect.
    * 
    * @param testCtx the to be tested Execution Context.
    * 
    * @return <code>true</code> if the Execution Context is relevent to this
    * effect; otherwise return <code>false</code>. Always return
    * <code>true</code> if there is no Execution Contexts in this effect.
    */
   public boolean hasExecutionContext(int testCtx)
   {
      if (m_exeContexts.isEmpty())
         return true;

      for (Integer ctx : m_exeContexts)
      {
         if (ctx.intValue() == testCtx)
            return true;
      }
      return false;
   }

   /**
    * Returns the execution context value for the given string representation.
    * If not found removes the "-" from the supplied string and tries to find
    * the value.
    * 
    * @param executionContextName, name of the execution context.
    * @return Integer value of execution context or <code>null</code> if the
    * supplied string is does not represent a valid execution context.
    */
   public static Integer getExecutionContextValueForName(
         String executionContextName)
   {
      Integer ctxVal = null;
      if (StringUtils.isBlank(executionContextName))
         return ctxVal;
      ctxVal = ms_ContextNameIdMapper.get(executionContextName);
      if (ctxVal == null)
      {
         executionContextName = executionContextName.toLowerCase().replace(
               "-", "");
         ctxVal = ms_ContextNameIdMapper.get(executionContextName);
      }
      return ctxVal;
   }

   /**
    * Returns the execution context name for the given integer representation.
    * If addHyphen is true then adds "-" after Pre or Post.
    * 
    * @param executionContextValue, value of the execution context.
    * @return String name of execution context or <code>null</code> if the
    * supplied integer does not represent a valid execution context.
    */
   public static String getExecutionContextNameForValue(
         Integer executionContextValue, boolean addHyphen)
   {
      String ctxName = null;
      if (executionContextValue == null)
         return ctxName;
      ctxName = ms_ContextIdNameMapper.get(executionContextValue);
      if(addHyphen)
      {
         //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
         ctxName = ctxName.replace("Pre", "Pre-");
         ctxName = ctxName.replace("Post", "Post-");
      }
      return ctxName;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXConditionalEffect";

   /**
    * Attribute name of {@link XML_NODE_NAME} element indicating the activation
    * end point for the effect.
    */
   public static final String XML_ATTR_ACTIVATIONENDPOINT = "activationEndpoint";

   /**
    * Activation end point for this effect. One of the ACTIVATION_ENDPOINT_XXXX
    * values defined in {@link PSRelationshipConfig}. Default is
    * PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER. Never <code>null</code>
    * or empty.
    */
   private String m_activationEndPoint = PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER;

   /**
    * Holds the effect to be executed, initialized in ctor, never changed or
    * <code>null</code> after that.
    */
   private PSExtensionCall m_effect = null;

   /**
    * A collection of conditions ({@link PSRule} objects) that specify if the
    * effect need to be executed or not. Initialized in the ctor, and may be
    * modified through a call to <code>setConditions(Iterator)</code>. Never
    * <code>null</code>, might be empty.
    */
   private PSCollection m_conditions = new PSCollection(PSRule.class);

   /*
    * The following strings define all elements/attributes used to parse/create
    * the XML for this object. No Java documentation will be added to this.
    */
   private static final String ELEM_CONDITIONS = "Conditions";

   /**
    * The XML element name for the Execution Context Set, which contains a list
    * of Execution Context element, refer to sys_RelationshipConfig.dtd for
    * detail.
    */
   private static final String ELEM_EXE_CONTEXT_SET = "ExecutionContextSet";

   /**
    * The XML element name for a Execution Context
    */
   private static final String ELEM_EXE_CONTEXT = "ExecutionContext";

   /**
    * The attribute of the {@link #ELEM_EXE_CONTEXT} element
    */
   private static final String ATTR_TYPE = "type";

   /**
    * A list of Execution Contexts that is relevent for this effect. It may be
    * EMPTY if unknown, never <code>null</code>.
    */
   private Collection<Integer> m_exeContexts = new ArrayList<>();

   /**
    * Computed number
    */
   private static final long serialVersionUID = -8339993723317778994L;

   /**
    * The logger of this class.
    */

   private static final Logger log = LogManager.getLogger(PSConditionalEffect.class);

   /**
    * It maps context name (in lower case) to its related id that is defined in
    * IPSExecutionContext.RS_XXX. The names are the allowed attributes for
    * <code>ExecutionContext</code> element that are defined in
    * <code>sys_RelationshipConfig.dtd</code>.
    */
   private static HashMap<String, Integer> ms_ContextNameIdMapper = new HashMap<>();

   /**
    * It maps context id to its related name. The id is defined in
    * IPSExecutionContext.RS_XXX. The names are the allowed attributes for
    * <code>ExecutionContext</code> element that are defined in
    * <code>sys_RelationshipConfig.dtd</code>.
    */
   private static HashMap<Integer, String> ms_ContextIdNameMapper = new HashMap<>();

   /**
    * A list of allowed attribute names for Execution Context element that is
    * defined in sys_RelationshipConfig.dtd
    */
   public static final String RS_PRE_CONSTRUCTION = "PreConstruction";

   public static final String RS_PRE_DESTRUCTION = "PreDestruction";

   public static final String RS_PRE_WORKFLOW = "PreWorkflow";

   public static final String RS_POST_WORKFLOW = "PostWorkflow";

   public static final String RS_PRE_CLONE = "PreClone";

   public static final String RS_PRE_CHECKIN = "PreCheckin";

   public static final String RS_PRE_CHECKOUT = "PreCheckout";

   public static final String RS_PRE_UPDATE = "PreUpdate";

   static
   {
      // init ms_ContextNameIdMapper
      ms_ContextNameIdMapper.put(RS_PRE_CONSTRUCTION.toLowerCase(),
            IPSExecutionContext.RS_PRE_CONSTRUCTION);
      ms_ContextNameIdMapper.put(RS_PRE_DESTRUCTION.toLowerCase(),
            IPSExecutionContext.RS_PRE_DESTRUCTION);
      ms_ContextNameIdMapper.put(RS_PRE_WORKFLOW.toLowerCase(),
            IPSExecutionContext.RS_PRE_WORKFLOW);
      ms_ContextNameIdMapper.put(RS_POST_WORKFLOW.toLowerCase(),
            IPSExecutionContext.RS_POST_WORKFLOW);
      ms_ContextNameIdMapper.put(RS_PRE_CLONE.toLowerCase(),
            IPSExecutionContext.RS_PRE_CLONE);
      ms_ContextNameIdMapper.put(RS_PRE_CHECKIN.toLowerCase(),
            IPSExecutionContext.RS_PRE_CHECKIN);
      ms_ContextNameIdMapper.put(RS_PRE_CHECKOUT.toLowerCase(),
            IPSExecutionContext.RS_POST_CHECKOUT);
      ms_ContextNameIdMapper.put(RS_PRE_UPDATE.toLowerCase(),
            IPSExecutionContext.RS_PRE_UPDATE);

      // init ms_ContextIdNameMapper
      ms_ContextIdNameMapper.put(IPSExecutionContext.RS_PRE_CONSTRUCTION,
            RS_PRE_CONSTRUCTION);
      ms_ContextIdNameMapper.put(IPSExecutionContext.RS_PRE_DESTRUCTION,
            RS_PRE_DESTRUCTION);
      ms_ContextIdNameMapper.put(IPSExecutionContext.RS_PRE_WORKFLOW,
            RS_PRE_WORKFLOW);
      ms_ContextIdNameMapper.put(IPSExecutionContext.RS_POST_WORKFLOW,
            RS_POST_WORKFLOW);
      ms_ContextIdNameMapper.put(IPSExecutionContext.RS_PRE_CLONE,
            RS_PRE_CLONE);
      ms_ContextIdNameMapper.put(IPSExecutionContext.RS_PRE_CHECKIN,
            RS_PRE_CHECKIN);
      ms_ContextIdNameMapper.put(IPSExecutionContext.RS_POST_CHECKOUT,
            RS_PRE_CHECKOUT);
      ms_ContextIdNameMapper.put(IPSExecutionContext.RS_PRE_UPDATE,
            RS_PRE_UPDATE);

   }
}

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

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Implementation for the PSXConditionalExit DTD in BasicObjects.dtd.
 */
public class PSConditionalExit extends PSComponent
{
   /**
    * Create a new conditional exit for the provided rules.
    *
    * @param rules a valid extension call set, never <code>null</code>.
    */
   public PSConditionalExit(PSExtensionCallSet rules)
   {
      setRules(rules);
   }

   /**
    * Create a new conditional exit for the provided rules.
    *
    * @param rules a valid extension call set, never <code>null</code>.
    * @param condition the conditions defining when this exit is to be
    *    executed.
    */
   public PSConditionalExit(PSExtensionCallSet rules, PSApplyWhen condition)
   {
      setRules(rules);
      setCondition(condition);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSConditionalExit(Element sourceNode, IPSDocument parentDoc,
                            ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSConditionalExit()
   {
   }

   /**
    * Get the maximum number of errors allowed before the process is stopped.
    *
    * @return the number of errors allowed before the process is stopped.
    */
   public int getMaxErrorsToStop()
   {
      return m_maxErrorsToStop;
   }

   /**
    * Set the maximum number of errors allowed before the process is stopped.
    *
    * @param maxErrorsToStop the number of errors allowed before the process
    *    is stopped.
    */
   public void setMaxErrorsToStop(int maxErrorsToStop)
   {
      m_maxErrorsToStop = maxErrorsToStop;
   }

   /**
    * Get the the set of extension rules.
    *
    * @return the extension call set, never
    *    <code>null</code>, might be empty.
    */
   public PSExtensionCallSet getRules()
   {
      return m_rules;
   }

   /**
    * Set a new set of extension rules.
    *
    * @param rules the new extension call set for this conditional exit,
    *    never <code>null</code>, might be empty.
    */
   public void setRules(PSExtensionCallSet rules)
   {
      if (rules == null)
         throw new IllegalArgumentException(
            "the extension call set cannot be null");

      m_rules = rules;
   }

   /**
    * Get the condition when to apply this extits.
    *
    * @return the condition specifying when to execute the rules
    *    of this object.
    */
   public PSApplyWhen getCondition()
   {
      return m_condition;
   }

   /**
    * Set the new condition when to apply this exits, provide
    * <code>null</code> to remove the condition.
    *
    * @param condition the new condition for this exit, may be
    *    <code>null</code>.
    */
   public void setCondition(PSApplyWhen condition)
   {
      m_condition = condition;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSConditionalExit, not <code>null</code>.
    */
   public void copyFrom(PSConditionalExit c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setCondition(c.getCondition());
      setMaxErrorsToStop(c.getMaxErrorsToStop());
      setRules(c.getRules());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSConditionalExit)) return false;
      if (!super.equals(o)) return false;
      PSConditionalExit that = (PSConditionalExit) o;
      return m_maxErrorsToStop == that.m_maxErrorsToStop &&
              m_rules.equals(that.m_rules) &&
              m_condition.equals(that.m_condition);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_maxErrorsToStop, m_rules, m_condition);
   }

   /**
    * Makes a deep copy of this object.
    *
    * @return A deep copy.
    */
   public Object clone()
   {
      PSConditionalExit ce = (PSConditionalExit) super.clone();
      if ( null != m_condition )
         ce.m_condition = (PSApplyWhen) m_condition.clone();
      if ( null != m_rules )
         ce.m_rules = (PSExtensionCallSet) m_rules.clone();
      return ce;
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
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

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the maxErrorsToStop attribute
         data = tree.getElementData(MAX_ERRORS_TO_STOP_ATTR);
         if (data != null)
            m_maxErrorsToStop = Integer.parseInt(data);

         // REQUIRED: get the extension call set
         node = tree.getNextElement(PSExtensionCallSet.ms_NodeType, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSExtensionCallSet.ms_NodeType,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_rules = new PSExtensionCallSet(node, parentDoc, parentComponents);

         // OPTIONAL: get the applyWhen
         node = tree.getNextElement(PSApplyWhen.XML_NODE_NAME, nextFlags);
         if (node != null)
            m_condition = new PSApplyWhen(node, parentDoc, parentComponents);
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(MAX_ERRORS_TO_STOP_ATTR,
         Integer.toString(getMaxErrorsToStop()));

      // REQUIRED: the extension call set
      root.appendChild(m_rules.toXml(doc));

      // create the apply when condition
      if (m_condition != null)
         root.appendChild(m_condition.toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (m_rules == null)
         {
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CONDITIONAL_EXIT, null);
         }
         else
            m_rules.validate(context);

         if (m_condition != null)
            m_condition.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXConditionalExit";

   /**
    * This attributes specifies the number of errors until the process is
    * stopped and the errors returned to the user.
    */
   private int m_maxErrorsToStop = 10;

   /** A set of extensions, never <code>null</code> after construction. */
   private PSExtensionCallSet m_rules = null;

   /** A condition when to apply the extensions, might be <code>null</code>. */
   private PSApplyWhen m_condition = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String MAX_ERRORS_TO_STOP_ATTR = "maxErrorsToStop";
}


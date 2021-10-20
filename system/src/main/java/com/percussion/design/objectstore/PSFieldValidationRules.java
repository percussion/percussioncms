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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for the PSXFieldValidationRules DTD in BasicObjects.dtd.
 */
public class PSFieldValidationRules extends PSComponent
{
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
   public PSFieldValidationRules(Element sourceNode, IPSDocument parentDoc,
                                 List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Create an empty field validation rule object.
    */
   public PSFieldValidationRules()
   {
   }

   /**
    * Get the name of this rules.
    *
    * @return the rules name, might be <code>null</code>.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set a new name.
    *
    * @param name the new name for this rules, might be be <code>null</code>,
    *    not empty.
    */
   public void setName(String name)
   {
      if (name != null && name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      m_name = name;
   }

   /**
    * Get the number of errors after validation should stop.
    *
    * @return the number of errors after validation should stop.
    */
   public int getMaxErrorsToStop()
   {
      return m_maxErrorsToStop;
   }

   /**
    * Set a new number ob errors after validation should stop.
    *
    * @param maxErrorsToStop the new number of errors after validation should
    *    stop. Must be greater than 0.
    */
   public void setMaxErrorsToStop(int maxErrorsToStop)
   {
      if (maxErrorsToStop <= 0)
         throw new IllegalArgumentException(
            "mxaErrorsToStop must be greater than 0");

      m_maxErrorsToStop = maxErrorsToStop;
   }

   /**
    * Get the validation rules.
    *
    * @return a collection of PSRule objects, not <code>null</code>,
    *    might be empty.
    */
   public Iterator getRules()
   {
      return m_rules.iterator();
   }

   /**
    * Set the validation rules.
    *
    * @param rules the new rules to set, might be <code>null</code> or empty.
    */
   public void setRules(PSCollection rules)
   {
      if (rules != null &&
         !rules.getMemberClassName().equals(m_rules.getMemberClassName()))
         throw new IllegalArgumentException("PSRule collection expected");

      m_rules.clear();
      if (rules != null)
         m_rules.addAll(rules);
   }

   /**
    * Get the collection of rule references.
    *
    * @return a collection of String objects conatining the
    *    name of the referenced PSRule. Never <code>null</code>, might be
    *    empty.
    */
   public Iterator getRuleReferences()
   {
      return m_ruleReferences.iterator();
   }

   /**
    * Set the collection of rule references.
    *
    * @param ruleReferences a collection of String objects conatining the
    *    name of the referenced PSRule. Might be <code>null</code> or empty.
    */
   public void setRuleReferences(PSCollection ruleReferences)
   {
      if (ruleReferences != null &&
         !ruleReferences.getMemberClassName().equals("java.lang.String"))
         throw new IllegalArgumentException("String collection expected");

      m_ruleReferences.clear();
      if (ruleReferences != null)
         m_ruleReferences.addAll(ruleReferences);
   }

   /**
    * Get the error message.
    *
    * @return the error message, might be <code>null</code>.
    */
   public PSDisplayText getErrorMessage()
   {
      return m_errorMessage;
   }

   /**
    * Set the error message.
    *
    * @param errorMessage the new error message, might be <code>null</code>.
    */
   public void setErrorMessage(PSDisplayText errorMessage)
   {
      m_errorMessage = errorMessage;
   }

   /**
    * Get the apply when condition.
    *
    * @return the current apply when condition, might be
    *    <code>null</code>.
    */
   public PSApplyWhen getApplyWhen()
   {
      return m_applyWhen;
   }

   /**
    * Set a new apply when condition.
    *
    * @param applyWhen the new apply when condition, migth be
    *    <code>null</code>.
    */
   public void setApplyWhen(PSApplyWhen applyWhen)
   {
      m_applyWhen = applyWhen;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSFieldValidationRules, not <code>null</code>.
    */
   public void copyFrom(PSFieldValidationRules c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setApplyWhen(c.getApplyWhen());
      setErrorMessage(c.getErrorMessage());
      setMaxErrorsToStop(c.getMaxErrorsToStop());
      setName(c.getName());
      setRuleReferences(c.m_ruleReferences);
      setRules(c.m_rules);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSFieldValidationRules)) return false;
      if (!super.equals(o)) return false;
      PSFieldValidationRules that = (PSFieldValidationRules) o;
      return m_maxErrorsToStop == that.m_maxErrorsToStop &&
              Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_rules, that.m_rules) &&
              Objects.equals(m_ruleReferences, that.m_ruleReferences) &&
              Objects.equals(m_errorMessage, that.m_errorMessage) &&
              Objects.equals(m_applyWhen, that.m_applyWhen);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_maxErrorsToStop, m_rules, m_ruleReferences, m_errorMessage, m_applyWhen);
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
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

         // OPTIONAL: get the name attribute
         data = tree.getElementData(NAME_ATTR);
         if (data != null && data.trim().length() != 0)
            m_name = data;

         // OPTIONAL: get the maxErrorsToStop attribute
         data = tree.getElementData(MAX_ERRORS_TO_STOP_ATTR);
         if (data != null) // use the default if not specified
            m_maxErrorsToStop = Integer.parseInt(data);

         // OPTIONAL: get the rules
         node = tree.getNextElement(PSRule.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            m_rules.add(
               new PSRule(node, parentDoc, parentComponents));

            node = tree.getNextElement(PSRule.XML_NODE_NAME, nextFlags);
         }

         // OPTIONAL: get the rules references
         node = tree.getNextElement(RULE_REFERENCE_ELEM, nextFlags);
         while (node != null)
         {
            String ref = tree.getElementData(node);
            if (ref != null && ref.trim().length() > 0)
               m_ruleReferences.add(ref);

            node = tree.getNextElement(RULE_REFERENCE_ELEM, nextFlags);
         }

         // OPTIONAL: get the apply when condition
         node = tree.getNextElement(PSApplyWhen.XML_NODE_NAME, nextFlags);
         if (node != null)
         {
            m_applyWhen = new PSApplyWhen(
               node, parentDoc, parentComponents);
         }

         // OPTIONAL: get the error message
         node = tree.getNextElement(ERROR_MESSAGE_ELEM, nextFlags);
         if (node != null)
         {
            Node current = tree.getCurrent();

            node = tree.getNextElement(PSDisplayText.XML_NODE_NAME, firstFlags);
            m_errorMessage = new PSDisplayText(
               node, parentDoc, parentComponents);

            tree.setCurrent(current);
         }
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
      if (m_name != null)
         root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(
         MAX_ERRORS_TO_STOP_ATTR, Integer.toString(m_maxErrorsToStop));

      // OPTIONAL: create the rules
      Iterator it = getRules();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      // OPTIONAL: create the rule references
      it = getRuleReferences();
      while (it.hasNext())
         PSXmlDocumentBuilder.addElement(
            doc, root, RULE_REFERENCE_ELEM, (String) it.next());

      // OPTIONAL: create the apply when
      if (m_applyWhen != null)
         root.appendChild(m_applyWhen.toXml(doc));

      // OPTIONAL: create the error message
      if (m_errorMessage != null)
      {
         Element element = doc.createElement(ERROR_MESSAGE_ELEM);
         element.appendChild(m_errorMessage.toXml(doc));
         root.appendChild(element);
      }

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
         if (m_rules != null)
         {
            Iterator it = getRules();
            while (it.hasNext())
               ((IPSComponent) it.next()).validate(context);
         }

         if (m_errorMessage != null)
            m_errorMessage.validate(context);

         if (m_applyWhen != null)
            m_applyWhen.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXFieldValidationRules";

   /**
    * The name for this rules, is used to share this rules. Might be
    * <code>null</code>.
    */
   private String m_name = null;

   /**
    * This attribute specifies the number of errors until the process is
    * stopped and the errors returned to the user. Defaults to 
    * <code>Integer.MAX_VALUE</code> if not specified.
    */
   private int m_maxErrorsToStop = Integer.MAX_VALUE;

   /**
    * A collection of PSRule objects. Never <code>null</code> after
    * construction, might be empty.
    */
   private PSCollection m_rules = new PSCollection((new PSRule()).getClass());

   /**
    * A collection of String objects, containing the names of the referenced
    * rule. Never <code>null</code> after construction, might be empty.
    */
   private PSCollection m_ruleReferences =
      new PSCollection((new String()).getClass());

   /**
    * The error display text for this rule. Might be <code>null</code>.
    */
   private PSDisplayText m_errorMessage = null;

   /** The apply when conditions, might be <code>null</code>. */
   private PSApplyWhen m_applyWhen = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String RULE_REFERENCE_ELEM = "RuleReference";
   private static final String ERROR_MESSAGE_ELEM = "ErrorMessage";
   private static final String NAME_ATTR = "name";
   private static final String MAX_ERRORS_TO_STOP_ATTR = "maxErrorsToStop";
}


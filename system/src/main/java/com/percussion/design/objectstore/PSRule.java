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
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation for the PSXRule DTD in BasicObjects.dtd.
 */
public class PSRule extends PSComponent
{
   /**
    * Creates a new rule objects.
    *
    * @param extensionRules the extension rule set, not <code>null</code>
    *    only UDF's are allowed.
    */
   public PSRule(PSExtensionCallSet extensionRules)
   {
      setExtensionRules(extensionRules);
   }

   /**
    * Creates a new rule objects.
    *
    * @param conditionalRules a collection of PSConditional objects,
    *    not <code>null</code>, might be empty.
    */
   public PSRule(PSCollection conditionalRules)
   {
      setConditionalRules(conditionalRules);
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
   public PSRule(Element sourceNode, IPSDocument parentDoc,
                 List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor that can be removed once all four classes that call
    * new PSRule().getClass() are switched to PSRule.class
    */
   protected PSRule()
   {
   }


   // see interface for description
   @Override
   public Object clone()
   {
      PSRule copy = (PSRule) super.clone();
      if (m_errorLabel != null)
         copy.m_errorLabel = (PSDisplayText) m_errorLabel.clone();
      if (m_extensionRules != null)
         copy.m_extensionRules = (PSExtensionCallSet) m_extensionRules.clone();

      // clone the PSCollection
      copy.m_conditionalRules = new PSCollection( PSConditional.class );
      for (int i = 0; i < m_conditionalRules.size(); i++)
      {
         PSConditional cond = (PSConditional) m_conditionalRules.elementAt( i );
         copy.m_conditionalRules.add( i, cond.clone() );
      }
      return copy;
   }


   /**
    * Is this rule an extension set rule?
    *
    * @return <code>true</code> if this rule is an
    * extension set rule, else <code>false</code>.
    */
   public boolean isExtensionSetRule()
   {
      return (m_extensionRules != null);
   }

   /**
    * Get the boolean operator how to combine this with the next rule.
    *
    * @return the boolean operator.
    */
   public int getOperator()
   {
      if (m_extensionRules == null)
         ((PSConditional) m_conditionalRules.lastElement()).getBoolean();

      return m_operator;
   }

   /**
    * Set a new boolean operator.
    *
    * @param operator the new boolean operator.
    */
   public void setOperator(int operator)
   {
      if (operator != BOOLEAN_AND && operator != BOOLEAN_OR)
         throw new IllegalArgumentException("unsupported boolean operator");

      m_operator = operator;
   }

   /**
    * Get the conditional rules.
    *
    * @return the conditional rules, never <code>null</code> might
    *    be empty.
    */
   public Iterator getConditionalRules()
   {
      return m_conditionalRules.iterator();
   }

   /**
    * Get the conditional rules.
    *
    * @return the conditional rules, never <code>null</code> might
    *    be empty.
    */
   public PSCollection getConditionalRulesCollection()
   {
      return m_conditionalRules;
   }

   /**
    * Set new conditional rules.
    *
    * @param conditionalRules a collection of PSConditional objects, might be
    *    <code>null</code>.
    */
   private void setConditionalRules(PSCollection conditionalRules)
   {
      if (!conditionalRules.getMemberClassName().equals(
          m_conditionalRules.getMemberClassName()))
         throw new IllegalArgumentException("PSConditional collection expected");

      m_conditionalRules.clear();
      if (conditionalRules != null)
         m_conditionalRules.addAll(conditionalRules);
   }

   /**
    * Get the extension rules.
    *
    * @return the extension rules, might be
    *    <code>null</code>.
    */
   public PSExtensionCallSet getExtensionRules()
   {
      return m_extensionRules;
   }

   /**
    * Set new extension rules.
    *
    * @param extensionRules the new extension rules, not <code>null</code>.
    */
   private void setExtensionRules(PSExtensionCallSet extensionRules)
   {
      if (extensionRules == null)
         throw new IllegalArgumentException("extensionRules cannot be null");

      m_extensionRules = extensionRules;
   }

   /**
    * Get the error label.
    *
    * @return the error label, might be <code>null</code>.
    */
   public PSDisplayText getErrorLabel()
   {
      return m_errorLabel;
   }

   /**
    * Set a new error label, provide <code>null</code> to remove it.
    *
    * @param errorLabel the new error label, might be <code>null</code>.
    */
   public void setErrorLabel(PSDisplayText errorLabel)
   {
      m_errorLabel = errorLabel;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSRule, not <code>null</code>.
    */
   public void copyFrom(PSRule c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      };

      setConditionalRules(c.m_conditionalRules);
      m_extensionRules = c.getExtensionRules();
      setOperator(c.getOperator());
      m_errorLabel = c.m_errorLabel;
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSRule))
         return false;

      PSRule t = (PSRule) o;

      boolean equal = true;
      if (!compare(m_conditionalRules, t.m_conditionalRules))
         equal = false;
      else if (!compare(m_extensionRules, t.m_extensionRules))
         equal = false;
      else if (getOperator() != t.getOperator())
         equal = false;
      else if (!compare(m_errorLabel, t.m_errorLabel))
         equal = false;

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
            .append(m_conditionalRules)
            .append(m_extensionRules)
            .toHashCode();
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

         // OPTIONAL: get the operator attribute
         data = tree.getElementData(BOOLEAN_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<BOOLEAN_ENUM.length; i++)
            {
               if (BOOLEAN_ENUM[i].equalsIgnoreCase(data))
               {
                  m_operator = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  BOOLEAN_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }
         else
            m_operator = BOOLEAN_AND;

         // REQUIRED: an extension call set or a collection of conditional rules
         node = tree.getNextElement(
            PSExtensionCallSet.ms_NodeType, firstFlags);
         if (node != null)
            m_extensionRules = new PSExtensionCallSet(
               node, parentDoc, parentComponents);
         else
         {
            node = tree.getNextElement(
               PSConditional.ms_NodeType, firstFlags);
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSExtensionCallSet.ms_NodeType + " and " +
                     PSConditional.ms_NodeType,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            while (node != null)
            {
               m_conditionalRules.add(
                  new PSConditional(node, parentDoc, parentComponents));
               node = tree.getNextElement(
                  PSConditional.ms_NodeType, nextFlags);
            }
         }

         // OPTIONAL: get the error label
         node = tree.getNextElement(ERROR_LABEL_ELEM, nextFlags);
         if (node != null)
         {
            node = tree.getNextElement(PSDisplayText.XML_NODE_NAME, firstFlags);
            m_errorLabel = new PSDisplayText(
               node, parentDoc, parentComponents);
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
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(BOOLEAN_ATTR, BOOLEAN_ENUM[m_operator]);

      // create the rules
      if (m_extensionRules != null)
      {
         root.appendChild(m_extensionRules.toXml(doc));
      }
      else
      {
         Iterator it = getConditionalRules();
         while (it.hasNext())
            root.appendChild(((IPSComponent) it.next()).toXml(doc));
      }

      // create the error label
      if (m_errorLabel != null)
      {
         Element elem = doc.createElement(ERROR_LABEL_ELEM);
         elem.appendChild(m_errorLabel.toXml(doc));
         root.appendChild(elem);
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
         if (m_extensionRules != null)
            m_extensionRules.validate(context);

         Iterator it = getConditionalRules();
         while (it.hasNext())
            ((IPSComponent) it.next()).validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXRule";

   /** And boolean specifier. */
   public static final int BOOLEAN_AND = 0;

   /** Or boolean specifier. */
   public static final int BOOLEAN_OR = 1;

   /**
    * An array of XML attribute values for the boolean. They are
    * specified at the index of the specifier.
    */
   private static final String[] BOOLEAN_ENUM =
   {
      "and", "or"
   };

   /** The operator for how to combine this with the next rule. */
   private int m_operator = BOOLEAN_AND;

   /**
    * A collection of PSConditional objects, never <code>null</code> after
    * construction.
    */
   private PSCollection m_conditionalRules =
      new PSCollection( PSConditional.class );

   /** A set of extensions, might be <code>null</code>. */
   private PSExtensionCallSet m_extensionRules = null;

   /** The error label, might be <code>null</code> */
   private PSDisplayText m_errorLabel = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String ERROR_LABEL_ELEM = "ErrorLabel";
   private static final String BOOLEAN_ATTR = "boolean";


}


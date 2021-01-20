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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation for the PSXUISet DTD in BasicObjects.dtd.
 */
public class PSUISet extends PSComponent
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
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
   public PSUISet(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   public PSUISet()
   {
   }

   // see iterface for description
   public Object clone()
   {
      PSUISet copy = (PSUISet) super.clone();
      if (m_choices != null)
         copy.m_choices = (PSChoices) m_choices.clone();
      if (m_control != null)
         copy.m_control = (PSControlRef) m_control.clone();
      if (m_customActionGroup != null)
         copy.m_customActionGroup =
            (PSCustomActionGroup) m_customActionGroup.clone();
      if (m_errorLabel != null)
         copy.m_errorLabel = (PSDisplayText) m_errorLabel.clone();
      if (m_label != null)
         copy.m_label = (PSDisplayText) m_label.clone();
      if (m_labelSourceType != null)
         copy.m_labelSourceType = new String(m_labelSourceType);
      // clone the PSCollection
      copy.m_readOnlyRules = new PSCollection( PSRule.class );
      for (int i = 0; i < m_readOnlyRules.size(); i++)
      {
         PSRule rule = (PSRule) m_readOnlyRules.elementAt( i );
         copy.m_readOnlyRules.add( i, rule.clone() );
      }
      return copy;
   }

   /**
    * Gets shallow copy of this uiset merged with supplied source. The merged
    * uiset will have all non-<code>null</code> properties of this uiset and
    * overlays with properties of source for <code>null</code> properties of
    * this uiset. The properties copied from source are copied by reference.
    *
    * @param source the source uiset to merge with, may not be <code>null</code>
    *
    * @return the merged uiset, never <code>null</code>
    */
   public PSUISet merge(PSUISet source)
   {
      if (source == null)
         throw new IllegalArgumentException("the source cannot be null");

      PSUISet mergedSet = new PSUISet();
      mergedSet.copyFrom(this);

      if (getChoices() == null)
         mergedSet.setChoices(source.getChoices());
      if (getControl() == null)
         mergedSet.setControl(source.getControl());
      if (getDefaultSet() == null)
         mergedSet.setDefaultSet(source.getDefaultSet());
      if (getErrorLabel() == null)
         mergedSet.setErrorLabel(source.getErrorLabel());
      if (getLabel() == null)
         mergedSet.setLabel(source.getLabel());
      if (getAccessKey() == null)
         mergedSet.setAccessKey(source.getAccessKey());
      if (m_readOnlyRules.isEmpty())
         mergedSet.setReadOnlyRules(source.m_readOnlyRules);
      if (getCustomActionGroup() == null)
         mergedSet.setCustomActionGroup(source.getCustomActionGroup());
      if (getLabelSourceType() == null)
         mergedSet.setLabelSourceType(source.getLabelSourceType());
      if (m_name == null || m_name.trim().length() == 0)
         mergedSet.setName(source.getName());
      
      return mergedSet;
   }

   /**
    * Gets shallow copy of this field demerged with supplied source. The
    * demerged uiset will have all properties of this uiset that differ from
    * the properties of source.
    *
    * @param source the source uiset to demerge from, may not be
    * <code>null</code>
    *
    * @return the demerged field, never <code>null</code>
    */
   public PSUISet demerge(PSUISet source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      PSUISet diffSet = new PSUISet();

      if (m_labelSourceType != null && !compare(m_labelSourceType,
            source.m_labelSourceType))
         diffSet.m_labelSourceType = m_labelSourceType;
      if (m_choices != null && !compare(m_choices, source.m_choices))
         diffSet.m_choices = m_choices;
      if (m_control != null && !compare(m_control, source.m_control))
         diffSet.m_control = m_control;
      if (m_defaultSet != null && !compare(m_defaultSet, source.m_defaultSet))
         diffSet.m_defaultSet = m_defaultSet;
      if (m_errorLabel != null && !compare(m_errorLabel, source.m_errorLabel))
         diffSet.m_errorLabel = m_errorLabel;
      if (m_label != null && !compare(m_label, source.m_label))
         diffSet.m_label = m_label;
      if (!compare(m_accessKey, source.m_accessKey))
         diffSet.m_accessKey = m_accessKey;
      if (m_name != null && !compare(m_name, source.m_name))
         diffSet.m_name = m_name;
      if (!m_readOnlyRules.isEmpty() && !compare(m_readOnlyRules,
         source.m_readOnlyRules))
      {
         diffSet.m_readOnlyRules = m_readOnlyRules;
      }
      if (m_customActionGroup != null && !compare(m_customActionGroup,
         source.m_customActionGroup))
      {
         diffSet.m_customActionGroup = m_customActionGroup;
      }

      return diffSet;
   }

   /**
    * Get the UI set name.
    *
    * @return the UI set name, might be <code>null</code>.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set a new name.
    *
    * @param name the new name, might be <code>null</code>.
    */
   public void setName(String name)
   {
      m_name = name;
   }

   /**
    * Sets the label source type. See {#link getLabelSourceType()} for allowed
    * types.
    *
    * @param sourceType the source type, may be <code>null</code> but not empty.
    */
   public void setLabelSourceType(String sourceType)
   {
      PSContentEditorMapper.validateSourceType(sourceType);
      m_labelSourceType = sourceType;
   }

   /**
    * Gets the label source type, one of
    * <code>PSContentEditorMapper.SYSTEM</code>,
    * <code>PSContentEditorMapper.SHARED</code> or
    * <code>PSContentEditorMapper.LOCAL</code>, defaults to
    * <code>PSContentEditorMapper.SYSTEM</code>.
    *
    * @return the label source type. Can be <code>null</code> but not empty.
    */
   public String getLabelSourceType()
   {
      return m_labelSourceType;
   }

   /**
    * Get the default UI set.
    *
    * @return the default UI set, might be <code>null</code>.
    */
   public String getDefaultSet()
   {
      return m_defaultSet;
   }

   /**
    * Set a new default set.
    *
    * @param defaultSet the new default set, might be <code>null</code>.
    */
   public void setDefaultSet(String defaultSet)
   {
      m_defaultSet = defaultSet;
   }

   /**
    * Get the label text.
    *
    * @return the label text, might be <code>null</code>.
    */
   public PSDisplayText getLabel()
   {
      return m_label;
   }

   /**
    * Set a new label text.
    *
    * @param label the new label text, might be <code>null</code>.
    */
   public void setLabel(PSDisplayText label)
   {
      m_label = label;
   }

   /**
    * Get the Access Key text.
    *
    * @return the access key text, may be empty, but never <code>null</code>.
    */
   public String getAccessKey()
   {
      return m_accessKey;
   }

   /**
    * Set a new access key.
    *
    * @param accessKey the new accesskey, if <code>null</code> an empty string
    *    will be assigned. If the length of the string is more than one 
    *    character an <code>IllegalArgumentException</code> is thrown.
    */
   public void setAccessKey(String accessKey)
   {
      if (accessKey != null)
      {
         accessKey = accessKey.trim();
         if (accessKey.length() > 1)
            throw new IllegalArgumentException("accesskey cannot be > 1");
      }
      else
         accessKey = "";
      m_accessKey = accessKey;
   }

   /**
    * Get the error label text.
    *
    * @return the error label text, might be <code>null</code>.
    */
   public PSDisplayText getErrorLabel()
   {
      return m_errorLabel;
   }

   /**
    * Set a new error label text.
    *
    * @param errorLabel the new error label text, might be <code>null</code>.
    */
   public void setErrorLabel(PSDisplayText errorLabel)
   {
      m_errorLabel = errorLabel;
   }

   /**
    * Get the UI control.
    *
    * @return the UI control, might be <code>null</code>.
    */
   public PSControlRef getControl()
   {
      return m_control;
   }

   /**
    * Set a new UI control.
    *
    * @param control the UI control, might be <code>null</code>.
    */
   public void setControl(PSControlRef control)
   {
      m_control = control;
   }

   /**
    * Get the choices.
    *
    * @return the choices, might be <code>null</code>.
    */
   public PSChoices getChoices()
   {
      return m_choices;
   }

   /**
    * Set new choices.
    *
    * @param choices the new choices, might be <code>null</code>.
    */
   public void setChoices(PSChoices choices)
   {
      m_choices = choices;
   }

   /**
    * Get read only rules.
    *
    * @return a list of PSRule objects, never <code>null</code>,
    *    might be empty.
    */
   public Iterator getReadOnlyRules()
   {
      return m_readOnlyRules.iterator();
   }

   /**
    * Set new read only rules.
    *
    * @param readOnlyRules a collection of PSRule objects, might be
    *    <code>null</code> or empty.
    */
   public void setReadOnlyRules(PSCollection readOnlyRules)
   {
      if (readOnlyRules != null && !readOnlyRules.getMemberClassName().equals(
          m_readOnlyRules.getMemberClassName()))
         throw new IllegalArgumentException(
            "PSRule collection expected");

      m_readOnlyRules.clear();
      if (readOnlyRules != null)
         m_readOnlyRules.addAll(readOnlyRules);
   }

   /**
    * Get the custom action group.
    *
    * @return the current custom action group, might be
    *    <code>null</code>.
    */
   public PSCustomActionGroup getCustomActionGroup()
   {
      return m_customActionGroup;
   }

   /**
    * Set a new custom acton group.
    *
    * @param customActionGroup the new custom action group, set this to
    *    <code>null</code> if not used.
    */
   public void setCustomActionGroup(PSCustomActionGroup customActionGroup)
   {
      m_customActionGroup = customActionGroup;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSUISet, not <code>null</code>.
    */
   public void copyFrom(PSUISet c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      };

      setChoices(c.getChoices());
      setControl(c.getControl());
      setDefaultSet(c.getDefaultSet());
      setErrorLabel(c.getErrorLabel());
      setLabel(c.getLabel());
      setName(c.getName());
      setAccessKey(c.getAccessKey());
      setLabelSourceType(c.getLabelSourceType());
      m_readOnlyRules = c.m_readOnlyRules;
      m_customActionGroup = c.m_customActionGroup;
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
      if (!(o instanceof PSUISet))
         return false;

      PSUISet t = (PSUISet) o;

      boolean equal = true;
      if (!compare(m_labelSourceType, t.m_labelSourceType))
         equal = false;
      if (!compare(m_choices, t.m_choices))
         equal = false;
      else if (!compare(m_control, t.m_control))
         equal = false;
      else if (!compare(m_defaultSet, t.m_defaultSet))
         equal = false;
      else if (!compare(m_errorLabel, t.m_errorLabel))
         equal = false;
      else if (!compare(m_label, t.m_label))
         equal = false;
      else if (!compare(m_accessKey, t.m_accessKey))
         equal = false;
      else if (!compare(m_name, t.m_name))
         equal = false;
      else if (!compare(m_readOnlyRules, t.m_readOnlyRules))
         equal = false;
      else if (!compare(m_customActionGroup, t.m_customActionGroup))
         equal = false;

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_accessKey).append(m_defaultSet)
         .append(m_label).toHashCode();
   }

   // @see IPSComponent
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

         // OPTIONAL: name attribute
         data = tree.getElementData(NAME_ATTR);
         if (data != null && data.trim().length() != 0)
            m_name = data;

         // OPTIONAL: accessKey attribute
         data = tree.getElementData(ACCESSKEY_ATTR);
         setAccessKey(data);

         // OPTIONAL: defaultSet attribute
         data = tree.getElementData(DEFAULT_SET_ATTR);
         if (data != null && data.trim().length() != 0)
            m_defaultSet = data;

         // OPTIONAL: get all optional objects
         node = tree.getNextElement(firstFlags);
         while(node != null)
         {
            String elementName = node.getTagName();

            if (elementName.equals(LABEL_ELEM))
            {
               Node current = tree.getCurrent();

               node = tree.getNextElement(
                  PSDisplayText.XML_NODE_NAME, firstFlags);
               m_label = new PSDisplayText(node, parentDoc, parentComponents);

               tree.setCurrent(current);
            }
            else if (elementName.equals(ERROR_LABEL_ELEM))
            {
               Node current = tree.getCurrent();

               node = tree.getNextElement(
                  PSDisplayText.XML_NODE_NAME, firstFlags);
               m_errorLabel = new PSDisplayText(
                  node, parentDoc, parentComponents);

               tree.setCurrent(current);
            }
            else if (elementName.equals(PSControlRef.XML_NODE_NAME))
            {
               m_control = new PSControlRef(node, parentDoc, parentComponents);
            }
            else if (elementName.equals(PSChoices.XML_NODE_NAME))
            {
               m_choices = new PSChoices(node, parentDoc, parentComponents);
            }
            else if (elementName.equals(READ_ONLY_RULES_ELEM))
            {
               Node current = tree.getCurrent();

               node = tree.getNextElement(PSRule.XML_NODE_NAME, firstFlags);
               while (node != null)
               {
                  m_readOnlyRules.add(
                     new PSRule(node, parentDoc, parentComponents));

                  node = tree.getNextElement(PSRule.XML_NODE_NAME, nextFlags);
               }

               tree.setCurrent(current);
            }
            else if (elementName.equals(PSCustomActionGroup.XML_NODE_NAME))
            {
               m_customActionGroup =
                  new PSCustomActionGroup(node, parentDoc, parentComponents);
            }

            node = tree.getNextElement(nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      if (m_name != null)
         root.setAttribute(NAME_ATTR, m_name);
      if (m_defaultSet != null)
         root.setAttribute(DEFAULT_SET_ATTR, m_defaultSet);

      if (m_accessKey.length() > 0)
         root.setAttribute(ACCESSKEY_ATTR, m_accessKey);

      // create the label
      if (m_label != null)
      {
         Element elem = doc.createElement(LABEL_ELEM);
         elem.appendChild(m_label.toXml(doc));
         root.appendChild(elem);
      }

      // create the control
      if (m_control != null)
         root.appendChild(m_control.toXml(doc));

      // create the error label
      if (m_errorLabel != null)
      {
         Element elem = doc.createElement(ERROR_LABEL_ELEM);
         elem.appendChild(m_errorLabel.toXml(doc));
         root.appendChild(elem);
      }

      // create the choices
      if (m_choices != null)
         root.appendChild(m_choices.toXml(doc));

      // create the read only rules
      Iterator it = getReadOnlyRules();
      if (it.hasNext())
      {
         Element elem = doc.createElement(READ_ONLY_RULES_ELEM);
         while (it.hasNext())
            elem.appendChild(((IPSComponent) it.next()).toXml(doc));

         root.appendChild(elem);
      }

      // create the custom action group
      if (m_customActionGroup != null)
         root.appendChild(m_customActionGroup.toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (m_choices != null)
            m_choices.validate(context);

         if (m_control != null)
            m_control.validate(context);

         if (m_label != null)
            m_label.validate(context);

         if (m_errorLabel != null)
            m_errorLabel.validate(context);

         Iterator it = getReadOnlyRules();
         while (it.hasNext())
            ((IPSComponent) it.next()).validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXUISet";

   /** The name of this UI set, might be <code>null</code>. */
   private String m_name = null;

   /** The name of the defaul UI set, might be <code>null</code>. */
   private String m_defaultSet = null;

   /** The display label, might be <code>null</code>. */
   private PSDisplayText m_label = null;

   /** The error label, might be <code>null</code>. */
   private PSDisplayText m_errorLabel = null;

   /** 
    * The access key, may be <code>empty</code>, but never <code>null</code>. 
    */
   private String m_accessKey = "";

   /** A control reference, might be <code>null</code>. */
   private PSControlRef m_control = null;

   /** A choice list, might be <code>null</code>. */
   private PSChoices m_choices = null;

   /** A group of custom actions, might be <code>null</code> */
   private PSCustomActionGroup m_customActionGroup = null;

   /**
    * A collection of PSRule objects, never <code>null</code> after
    * construction, might be empty.
    */
   private PSCollection m_readOnlyRules = new PSCollection( PSRule.class );

  /**
   * Indicates where the definition of this label was located. If a
   * label is originally defined in the system def, then overridden in the
   * local def, this value will be <code>PSContentEditorMapper.LOCAL</code>.
   * Allowed values are <code>PSContentEditorMapper.SYSTEM</code>,
   * <code>PSContentEditorMapper.SHARED</code> and
   * <code>PSContentEditorMapper.LOCAL</code>. This attribute will not be
   * persisted, and is therefore excluded from to/from XML methods. It will
   * be included in all other operations like cloning, comparing, etc.
   */
   private String m_labelSourceType = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
   private static final String ACCESSKEY_ATTR = "accessKey";
   private static final String DEFAULT_SET_ATTR = "defaultSet";
   private static final String LABEL_ELEM = "Label";
   private static final String ERROR_LABEL_ELEM = "ErrorLabel";
   private static final String READ_ONLY_RULES_ELEM = "ReadOnlyRules";
}


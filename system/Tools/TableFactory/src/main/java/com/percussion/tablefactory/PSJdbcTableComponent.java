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
package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract base class for all table component classes such as column
 * defintions, primary keys and foreign keys.
 */
public abstract class PSJdbcTableComponent
{
   /**
    * Parameterless constructor for derived classes only
    */
   protected PSJdbcTableComponent()
   {
   }

   /**
    * Constructor for this class.
    *
    * @param name The name of this object.  May not be <code>null</code> or
    * empty unless this object does not require a name (see {@link
    * #isNameRequired()}).
    * @param action The action to perform with this component.  Must be one of
    * the ACTION_xxx types.
    *
    * @throws IllegalArgumentException if name or action is not valid.
    */
   public PSJdbcTableComponent(String name, int action)
   {
      if (!validateName(name))
         throw new IllegalArgumentException("name is invalid");

      if (!validateAction(action))
         throw new IllegalArgumentException("action is invalid");

      m_name = (name == null) ? "" : name;
      m_action = action;
   }


   /**
    * This method is called to set the base component state from the supplied
    * element.
    * See {@link #setComponentState(Element)} for more info.
    *
    * @param sourceNode The node from which to restore the state of this object.
    *    May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml format is not valid.
    */
   protected void getComponentState(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      m_name = getAttribute(tree, NAME_ATTR, isNameRequired());
      if (m_name == null)
         m_name = "";
      m_action = getEnumeratedAttributeIndex(tree, ACTION_ATTR, ACTION_ENUM);
   }

   /**
    * This method is called to set the base component state on the supplied
    * Element, conforming with the tabledef.dtd.   The structure of the Xml
    * assuming an element named "root" is: <b>
    *
    * <code><pre>
    * &lt;!--
    *     Attributes:
    *     name - the name of the object.  Optional.
    *     action - The action to take:
    *        c: create - object is created if it does not already exist
    *        r: replace - existing object is removed and then re-created.
    *        d: delete - existing object is removed.
    * --&gt;
    *
    * &lt;!ATTLIST root
    * name CDATA #IMPLIED
    * action (c | r | d) "c"
    * &gt;
    * </pre></code>
    * <b>See the tabledef.dtd for more info.
    *
    * @param root The Element to set the base component state on.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if root is <code>null</code>.
    */
   protected void setComponentState(Element root)
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");

      if (isNameRequired())
         root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(ACTION_ATTR, ACTION_ENUM[m_action]);
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    *    null</code>.
    *
    * @return The element containing this object's state, never <code>
    *    null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public abstract Element toXml(Document doc);

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the component
    *    element in the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public abstract void fromXml(Element sourceNode)
      throws PSJdbcTableFactoryException;

   /**
    * Serializes this object's state to a string. Creates an empty document,
    * then calls <code>toXm()</code> method and then serializes the returned
    * root element to a string.
    *
    * @return The string containing this object's state, never <code>
    * null</code> or empty
    */
   public String toString()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element   root = toXml(doc);
      return PSXmlDocumentBuilder.toString(root);
   }

   /**
    * Compares this component to another object and determines if they
    * represent the same component.
    *
    * @param obj the object to compare
    * @return returns <code>true</code> if the object is a PSJdbcTableComponent
    * with identical values for name (case-insensitive comparison) and action.
    * Otherwise returns <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      return (compare(obj, 0) >= IS_EXACT_MATCH);
   }

   /**
    * Compares this component to another object.
    *
    * @param obj the object to compare, may be <code>null</code>
    * @param flags one or more <code>COMPARE_XXX</code> values OR'ed
    * together
    *
    * @return one of the <code>IS_XXX</code> values indicating the type of
    * match/mismatch between this object and the specified object
    * <code>obj</code>
    */
   public int compare(Object obj, int flags)
   {
      int match = IS_GENERIC_MISMATCH;
      // dummy do...while loop to avoid many return statements
      do
      {
         if (!(obj instanceof PSJdbcTableComponent))
         {
            match = IS_CLASS_MISMATCH;
            break;
         }

         match = IS_EXACT_MATCH;

         PSJdbcTableComponent other = (PSJdbcTableComponent)obj;
         if (((flags & COMPARE_IGNORE_ACTION) != COMPARE_IGNORE_ACTION)
          && (this.m_action != other.m_action))
         {
            match = IS_ACTION_MISMATCH;
            break;
         }

         if (((flags & COMPARE_IGNORE_NAME) != COMPARE_IGNORE_NAME)
          && (this.m_name.equalsIgnoreCase(other.m_name)))
         {
            match = IS_CASE_INSENSITIVE_MATCH;
            if (this.m_name.equals(other.m_name))
            {
               match = IS_EXACT_MATCH;
            }
            break;
         }
      }
      while (false);
      return match;
   }

   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects.
    */
   public int hashCode()
   {
      int hash = 0;
      if ( null != m_name )
         hash += m_name.hashCode();
      hash += (new Integer(m_action)).hashCode();
      return hash;
   }

   /**
    * Ensures that this component has an action that is valid for an alter
    * table statement.
    *
    * @return <code>true</code> if the component can be altered, <code>false
    * </code> if not.
    */
   public boolean canAlter()
   {
      return (getAction() == ACTION_CREATE ||
         getAction() == ACTION_NONE) ||
         getAction() == ACTION_DELETE
         ;
   }


   /**
    * Gets the element data from an attribute.  It is an error for a
    * required attribute to be absent or empty.
    *
    * @param tree a valid PSXmlTreeWalker currently positioned at the element
    *    that should contain the specified attribute.
    * @param attrName the name of the attribute to retrieve data from;
    *    not <code>null</code> or empty.
    * @param required If <code>true</code>, then attribute must exist and
    *    contain a non-empty value.
    *
    * @return String containing the element data from the specified attribute
    *    node.  Never empty or <code>null</code> if required is
    *    <code>true</code>.
    * @throws PSJdbcTableFactoryException if the specified attribute is missing,
    *         or empty and required is <code>true</code>.
    * @throws IllegalArgumentException if either parameter is <code>null</code>
    *         or attrName is empty.
    * @throws IllegalStateExcpetion if tree is not postioned at an Element.
    */
   public static String getAttribute(PSXmlTreeWalker tree, String attrName,
      boolean required)
         throws PSJdbcTableFactoryException
   {
      if (null == tree)
         throw new IllegalArgumentException("tree cannot be null");

      if (null == attrName || attrName.trim().length() == 0)
         throw new IllegalArgumentException("attrName cannot be null or empty");

      if (tree.getCurrent() == null)
         throw new IllegalStateException(
            "tree must be positioned on an element");

      Attr data = (Attr)tree.getCurrent().getAttributes().getNamedItem(
         attrName);
      if (required && (null == data ||
         data.getValue().trim().length() == 0))
      {
         String parentName = tree.getCurrent().getNodeName();
         Object[] args = {  parentName, attrName, "null" };
         throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      return data == null ? null : data.getValue();
   }


   /**
    * Gets the element data from an attribute and validates that the data
    * is a legal value.  If the data is <code>null</code> or empty, it will be
    * set with a default value (assumed to be the value at index 0 of the legal
    * value array).
    *
    * @param tree a valid PSXmlTreeWalker currently positioned at the element
    *        that should contain the specified attribute.
    * @param attrName the name of the attribute to retrieve data from;
    *        not <code>null</code> or emtpy.
    * @param legalValues the array of permitted values (case sensitive), with a
    *        default value at index 0.
    * @return The index into the array of the value to use.
    * @throws PSJdbcTableFactoryException if the node has an illegal value
    * @throws IllegalArgumentException if any parameter is <code>null</code>,
    *         or if attrName or legalValues is empty.
    * @throws IllegalStateExcpetion if tree is not postioned at an Element.
    */
   public static int getEnumeratedAttributeIndex(PSXmlTreeWalker tree,
                                                  String attrName,
                                                  String[] legalValues)
         throws PSJdbcTableFactoryException
   {
      if (null == tree)
         throw new IllegalArgumentException("tree cannot be null");
      if (null == attrName)
         throw new IllegalArgumentException("attrName cannot be null");
      if (null == legalValues || legalValues.length == 0)
         throw new IllegalArgumentException("legalValues");
      if (tree.getCurrent() == null)
         throw new IllegalStateException(
            "tree must be positioned on an element");

      int index = 0;
      String data = tree.getElementData(attrName);
      if (null == data || data.trim().length() == 0)
         // no value means use the default
         index = 0;
      else
      {
         // make sure the value is legal
         boolean found = false;
         for (int i = 0; i < legalValues.length; i++)
         {
            if (legalValues[i] != null && legalValues[i].equals(data))
            {
               found = true;
               index = i;
               break;
            }
         }

         if (!found)
         {
            String parentName = tree.getCurrent().getNodeName();
            Object[] args = {parentName, attrName, data};
            throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }
      return index;
   }


   /**
    * Used by <code>getComponentState</code> and <code>setComponentState</code>
    * to determine if the name attribute is required.  Returns <code>true</code>
    * by default. Derived classes not requiring name should override this method
    * to return <code>false</code>.
    *
    * @return <code>true</code>.
    */
   protected boolean isNameRequired()
   {
      return true;
   }

   /**
    * Returns the name of this object.
    *
    * @return The name, may be <code>null</code> or emtpy only if {@link
    * #isNameRequired()} returns <code>false</code>.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Sets the name for this object.  See {@link #getName()} for more info.
    *
    * @param name The name.  If {@link #isNameRequired()} returns <code>true
    * </code>determines it may not be <code>null</code> or empty, otherwise
    * it may.
    *
    * @throws IllegalArgumentException if name is not valid.
    */
   public void setName(String name)
   {
      if (!validateName(name))
         throw new IllegalArgumentException("name is invalid");

      m_name = name;
   }

   /**
    * Returns the action to perform on the table schema with this object.
    *
    * @return The action, one of the ACTION_xxx contstant values.
    */
   public int getAction()
   {
      return m_action;
   }

   /**
    * Sets the action for this object.  See {@link #getAction()} for more info.
    *
    * @param action The action, one of the ACTION_xxx contstant values.
    *
    * @throws IllegalArgumentException if action is not valid.
    */
   public void setAction(int action)
   {
      if (!validateAction(action))
         throw new IllegalArgumentException("action is invalid");

      m_action = action;
   }

   /**
    * @return <code>false</code> if this table components has an action of
    * PSJdbcTableComponent#ACTION_NONE, <code>true</code> if not.
    */
   public boolean hasChanges()
   {
      return m_action != ACTION_NONE;
   }

   /**
    * If {@link #isNameRequired()} returns <code>true</code>, validates that
    * name is not <code>null</code> or empty.
    *
    * @return <code>true</code> if name is valid, <code>false</code> if not.
    */
   private boolean validateName(String name)
   {
      boolean isValid = true;
      if ((name == null || name.trim().length() == 0) && isNameRequired())
         isValid = false;

      return isValid;
   }

   /**
    * Validates that action is one of the valid ACTION_xxx types.
    *
    * @param action The action to validate.
    *
    * @return <code>true</code> if action is valid, <code>false</code> if not.
    */
   private boolean validateAction(int action)
   {
      boolean isValid = false;
      switch (action)
      {
         case ACTION_CREATE:
         case ACTION_REPLACE:
         case ACTION_DELETE:
         case ACTION_NONE:
            isValid = true;
            break;
      }

      return isValid;
   }

   /**
    * Constant for the create action.  This will cause this object to be
    * created if it does not already exist.
    */
   public static final int ACTION_CREATE = 0;

   /**
    * Constant for the replace action.  This will cause this object to be
    * created, first deleting it if it already exists.
    */
   public static final int ACTION_REPLACE = 1;

   /**
    * Constant for the delete action.  This will cause this object to be
    * deleted if it already exists.
    */
   public static final int ACTION_DELETE = 2;

   /**
    * Constant for no action.  No changes will be processed on this object.
    */
   public static final int ACTION_NONE = 3;

   /**
    * An array of XML attribute values for the action. They are
    * specified at the index matching the constant's internal value for that
    * action.
    */
   private static final String[] ACTION_ENUM = {"c", "r", "d", "n"};

   /**
    * The name of this object.  May be <code>null</code> or empty if this object
    * does not require a name.
    */
   private String m_name = null;

   /**
    * The action to take for this object when processing the table schema.
    * Initialized to the default value of {@link #ACTION_CREATE}.
    */
   private int m_action = ACTION_CREATE;

   // Constants for Xml Elements and Attibutes
   private static final String NAME_ATTR = "name";
   private static final String ACTION_ATTR = "action";

   /**
    * Code returned from <code>compare()</code> method if the action for this
    * object does not match the action of the object being compared to.
    */
   public static final int IS_ACTION_MISMATCH = -2;

   /**
    * Code returned from <code>compare()</code> method if the object being
    * compared to is not an instance of this class.
    */
   public static final int IS_CLASS_MISMATCH = -1;

   /**
    * Code returned from <code>compare()</code> method if this object does not
    * match the object being compared to.
    */
   public static final int IS_GENERIC_MISMATCH = 0;

   /**
    * Code returned from <code>compare()</code> method if this object exactly
    * matches the object being compared to.
    */
   public static final int IS_EXACT_MATCH = 1;

   /**
    * Code returned from <code>compare()</code> method if this object
    * matches the object being compared to if the name is compared in
    * case-insensitive manner
    */
   public static final int IS_CASE_INSENSITIVE_MATCH = 2;

   /**
    * Constant used as a flag in <code>compare()</code> method. This flag
    * implies that the name of this object should be ignored when comparing
    * with the specified object.
    */
   public static final int COMPARE_IGNORE_NAME = 1;

   /**
    * Constant used as a flag in <code>compare()</code> method. This flag
    * implies that the action of this object should be ignored when comparing
    * with the specified object.
    */
   public static final int COMPARE_IGNORE_ACTION = 2;
}



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
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * Implementation for the PSCustomActionGroup DTD in BasicObjects.dtd.
 */
public class PSCustomActionGroup extends PSComponent
{
   /**
    * Creates a new custom action group for the provided location and actions.
    *
    * @param location the group location, not <code>null</code>.
    * @param actions the group actions, not <code>null</code>.
    */
   public PSCustomActionGroup(PSLocation location, PSActionLinkList actions)
   {
      setLocation(location);
      setActionLinkList(actions);
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
   public PSCustomActionGroup(Element sourceNode, IPSDocument parentDoc,
                              ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }


   // see interface for description
   public Object clone()
   {
      PSCustomActionGroup copy = (PSCustomActionGroup) super.clone();
      if (null != m_actionLinkList)
         copy.m_actionLinkList = (PSActionLinkList) m_actionLinkList.clone();
      if (null != m_formAction)
         copy.m_formAction = (PSFormAction) m_formAction.clone();
      if (null != m_location)
         copy.m_location = (PSLocation) m_location.clone();
      // don't need to clone m_removeActions because it contains Strings
      return copy;
   }


   /**
    * Get the custom action group location.
    *
    * @return the custom action location, never <code>null</code>.
    */
   public PSLocation getLocation()
   {
      return m_location;
   }


   /**
    * Set a new custom action group location.
    *
    * @param location the new custom action group location, not
    *    <code>null</code>.
    */
   public void setLocation(PSLocation location)
   {
      if (location == null)
         throw new IllegalArgumentException("location cannot be null");

      m_location = location;
   }

   /**
    * Get the remove action link list.
    *
    * @return the list of action links to be removed, never
    *    <code>null</code>, might be empty.
    */
   public Iterator getRemoveActions()
   {
      return m_removeActions.iterator();
   }

   /**
    * Set a new remove action link list, provide <code>null</code> if
    * nothing is to be removed.
    *
    * @param removeActions the new remove action link list (a collection of
    *    String objects) , <code>null</code> if nothing is to be removed.
    */
   public void setRemoveActions(PSCollection removeActions)
   {
      if (removeActions != null && !removeActions.getMemberClassName().equals(
          m_removeActions.getMemberClassName()))
         throw new IllegalArgumentException(
            "String collection expected");

      m_removeActions.clear();
      if (removeActions != null)
         m_removeActions.addAll(removeActions);
   }


   /**
    * The form action is the target url of this action. If the end user
    * activates this action, the browser will redirect to this page.
    *
    * @return A valid action, or <code>null</code> if no action has been set.
    */
   public PSFormAction getFormAction()
   {
      return m_formAction;
   }


   /**
    * See {@link #getFormAction() getFormAction} for a description.
    *
    * @param formAction The new target url for this action. May be <code>null
    *    </code>.
    */
   public void setFormAction( PSFormAction formAction )
   {
      m_formAction = formAction;
   }


   /**
    * Get the action link list of this group. These actions should be used
    * in addition to or in place of a set of buttons located at the position
    * specified by the location object.
    *
    * @return the action link list of this group, never <code>null</code>,
    *    may be empty. It may be empty if this group is only removing buttons.
    */
   public Iterator getActionLinkList()
   {
      return null == m_actionLinkList ? PSIteratorUtils.emptyIterator() :
            m_actionLinkList.iterator();
   }

   /**
    * Set a new action link list.
    *
    * @param actionLinkList the new action link list
    */
   public void setActionLinkList(PSActionLinkList actionLinkList)
   {
      if (actionLinkList != null && actionLinkList.size() == 0 )
         throw new IllegalArgumentException("actionLinkList cannot be empty");

      m_actionLinkList = actionLinkList;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSField, not <code>null</code>.
    */
   public void copyFrom(PSCustomActionGroup c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_actionLinkList = c.m_actionLinkList;
      m_location = c.m_location;
      m_removeActions = c.m_removeActions;
      m_formAction = c.m_formAction;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSCustomActionGroup)) return false;
      if (!super.equals(o)) return false;
      PSCustomActionGroup that = (PSCustomActionGroup) o;
      return Objects.equals(m_location, that.m_location) &&
              Objects.equals(m_removeActions, that.m_removeActions) &&
              Objects.equals(m_actionLinkList, that.m_actionLinkList) &&
              Objects.equals(m_formAction, that.m_formAction);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_location, m_removeActions, m_actionLinkList, m_formAction);
   }

   // see IPSComponent
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
         // dtd = (PSXLocation,
         // (RemoveAction | (RemoveAction?, PSXFormAction, PSXActionLinkList)))
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the location
         node = tree.getNextElement(PSLocation.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSLocation.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         else
            m_location = new PSLocation(node, parentDoc, parentComponents);

         Node current = tree.getCurrent();
         // OPTIONAL or REQUIRED: get the remove actions
         boolean foundRemovals = false;
         node = tree.getNextElement(REMOVE_ACTION_ELEM, nextFlags);
         if (node != null)
         {
            node = tree.getNextElement(REMOVE_ACTION_REF_ELEM, firstFlags);
            while (node != null)
            {
               m_removeActions.add(tree.getElementData(node));
               node = tree.getNextElement(REMOVE_ACTION_REF_ELEM, nextFlags);
            }
            foundRemovals = true;
         }

         tree.setCurrent( current );
         // may be REQUIRED based on previous node: 
         //get the form action
         node = tree.getNextElement(PSFormAction.XML_NODE_NAME, nextFlags);
         if (node == null && !foundRemovals )
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSFormAction.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         else if (node != null)
         {
            m_formAction = new PSFormAction(node, parentDoc, parentComponents);
         }

         //get the action link list
         node = tree.getNextElement(PSActionLinkList.XML_NODE_NAME, nextFlags);
         if (node == null)
         {
            Object[] args =
            {XML_NODE_NAME, PSActionLinkList.XML_NODE_NAME, "null"};
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_actionLinkList = new PSActionLinkList(node, parentDoc,
               parentComponents);
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }


   // see IPSComponent
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);

      // REQUIRED: create the location
      root.appendChild(m_location.toXml(doc));

      // OPTIONAL: create the remove action list
      Iterator it = getRemoveActions();
      if (it.hasNext())
      {
         Element elem = doc.createElement(REMOVE_ACTION_ELEM);
         while (it.hasNext())
         {
            Element ref = doc.createElement(REMOVE_ACTION_REF_ELEM);
            ref.appendChild(doc.createTextNode((String) it.next()));
            elem.appendChild(ref);
         }
         root.appendChild(elem);
      }

      if ( null != m_formAction )
         root.appendChild( m_formAction.toXml(doc));

      // create the action links
      if ( null != m_actionLinkList )
         root.appendChild(m_actionLinkList.toXml(doc));

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
         if (m_location != null)
            ((IPSComponent) m_location).validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CUSTOM_ACTION_GROUP, null);

         if (m_actionLinkList != null)
            ((IPSComponent) m_actionLinkList).validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CUSTOM_ACTION_GROUP, null);

         if (m_formAction != null)
            ((IPSComponent) m_formAction).validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CUSTOM_ACTION_GROUP, null);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXCustomActionGroup";

   /**
    * The location of this custom action group, never <code>null</code>.
    */
   private PSLocation m_location = null;

   /**
    * A list of action link references (String objects) to be removed, never
    * <code>null</code>, might be empty.
    */
   private PSCollection m_removeActions = new PSCollection( String.class );

   /**
    * A list of new action links, may be <code>null</code>. Not empty if
    * valid.
    */
   private PSActionLinkList m_actionLinkList = null;

   /**
    * When associated actions are activated, if this property is present,
    * then this will be the target of the action, rather than the original
    * form action set by the handler. Should be present if action links are
    * present. If this group is only for removing actions, then this property
    * should be <code>null</code>.
    */
   private PSFormAction m_formAction;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String REMOVE_ACTION_ELEM = "RemoveActions";
   private static final String REMOVE_ACTION_REF_ELEM = "ActionLinkRef";
}


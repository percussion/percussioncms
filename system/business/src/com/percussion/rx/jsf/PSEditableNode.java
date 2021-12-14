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
package com.percussion.rx.jsf;

import com.percussion.rx.publisher.jsf.nodes.PSDesignNode;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a reference to an object to be edited. When the edit
 * action is called, the type of the contained GUID is used to dispatch to the
 * specific editor view that is used.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSEditableNode extends PSLockableNode
{
   /**
    * Ctor.
    * 
    * @param title never <code>null</code> or empty
    * @param guid the GUID of the design object, never <code>null</code>
    */
   public PSEditableNode(String title, IPSGuid guid) 
   {
      super(title, guid);
   }

   /**
    * The edit action uses the type of the GUID to dispatch to the appropriate
    * outcome that will edit the kind of object. The outcomes all named
    * using the lower cased name of the type, with the prefix of
    * {@link #getOutcome()}
    * and the suffix of "-editor". The actual mapping is, of course, in the
    * faces configuration file.
    *
    * @return the outcome. Never <code>null</code> or blank.
    */
   @Override
   public String perform()
   {
      // Setup the navigator to hold the current object being edited
      setNavigatorToCurrentNode();
      
      PSTypeEnum type = PSTypeEnum.valueOf(getGUID().getType());
      String outcome = getOutcomePrefix()
            + type.name().toLowerCase().replace('_', '-') + "-editor";
      
      return outcome;
   }

   /**
    * Setup the navigator to hold/point to the current node.
    */
   protected void setNavigatorToCurrentNode()
   {
      final PSNavigation navigator = getModel().getNavigator();
      navigator.setCurrentCategoryKey(getCategoryKey());
      navigator.setCurrentItemGuid(getGUID());
      navigator.setCurrentItemKey((String) getKey());      
   }
   
   /**
    * Returns the category key.
    * @return the container category key.
    */
   protected abstract String getCategoryKey();

   /**
    * Delete this object, and return the appropriate outcome. 
    * @return the outcome, should navigate back to the appropriate view or 
    * editor.
    */
   public abstract String delete() throws PSNotFoundException;

   /**
    * Copy this object, and return the appropriate outcome. 
    * @return the outcome, should navigate back to the appropriate view or 
    * editor.
    */
   public abstract String copy() throws PSNotFoundException;

   /**
    * Get the properties for the node. The properties are set by the node's
    * creator for use in list views of the nodes.
    * 
    * @return the properties, never <code>null</code>.
    */
   public Map<String, String> getProperties()
   {
      return m_properties;  
   }

   /**
    * @param properties the properties to set, never <code>null</code>.
    */
   public void setProperties(Map<String, String> properties)
   {
      if (properties == null)
         throw new IllegalArgumentException("properties may not be null.");
      
      m_properties = properties;
   }

   /**
    * This simply call {@link super#getGUID()}. However, the returned value
    * can never be <code>null</code>.
    * 
    * @return the GUID for this item, never <code>null</code>.
    */
   @Override
   public IPSGuid getGUID()
   {
      return super.getGUID();
   }

   /**
    * Combines the title of the node and the numeric id of the associated design
    * object into a string suitable for presentation to the implementer.
    * 
    * @param name the object name, never <code>null</code> or empty.
    * @param uuid the UUID (not the GUID) of the object.
    * 
    * @return the display name, never <code>null</code> or empty.
    */
   public String getNameWithId()
   {
      return getNameWithId(getTitle(), getGUID().getUUID());
   }
   
   /**
    * Combines the name and numeric id into a string suitable for presentation
    * to the implementer.
    *  
    * @param name the object name, never <code>null</code> or empty.
    * @param uuid the UUID (not the GUID) of the object.
    * 
    * @return the display name, never <code>null</code> or empty.
    */
   public static String getNameWithId(String name, long uuid)
   {
      return name + " (" + uuid + ")";
   }

   /**
    * Navigate back to the appropriate list view for this design object type.
    * It is the same as {@link #getCategoryKey()}, unless override by derided
    * classes.
    * 
    * @return the outcome, never <code>null</code> or empty.
    */
   public abstract String navigateToList();

   /**
    * Add the new node to the tree and navigate to it for editing.
    * 
    * @param newnode the new node, never <code>null</code>.
    * @return the outcome of the perform method.
    */
   protected String editNewNode(PSDesignNode newnode)
   {
      ((PSEditableNodeContainer) getParent()).addNode(newnode);
      return editNewNode((PSCategoryNodeBase) getParent(), newnode);
   }

   /**
    * Add the new node to a parent node and navigate to it for editing.
    * 
    * @param parent the parent node of the new node, never <code>null</code>.
    * @param newnode the new node, never <code>null</code>.
    * @return the outcome of the perform method.
    */
   public String editNewNode(PSCategoryNodeBase parent, PSDesignNode newnode)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      if (newnode == null)
         throw new IllegalArgumentException("newnode may not be null");

      parent.addNode(newnode);
      return newnode.perform();
   }

   /**
    * Existing (current) editing mode, which will also enable all tree nodes.
    */
   private void exitEditMode()
   {
      PSNavigation nav = getModel().getNavigator();
      nav.setCurrentItemGuid(null);
      nav.setCurrentItemKey(getCategoryKey());
   }

   /**
    * Cancel the current editor.
    * 
    * @return <code>cancel</code> as the outcome of the cancel action
    */
   public String cancel() throws PSNotFoundException {
      exitEditMode();
      return "cancel";
   }
   
   /**
    * This should be called when exiting edit mode and navigating to its parent
    * node. For example, this should be called by save or cancel methods of the
    * design node.
    * 
    * @return the outcome of the parent node.
    */
   protected String gotoParentNode()
   {
      exitEditMode();
      return navigateToList();
   }

   /**
    * The prefix for the outcome generated by {@link #perform()}.
    * @return the outcome prefix. Never <code>null</code> or blank.
    */
   protected abstract String getOutcomePrefix();

   
   /**
    * Returns a parent container.
    * @return the parent container. <code>null</code> if parent container can't
    * be found. 
    */
   public PSEditableNodeContainer getContainer()
   {
      return getParent() instanceof  PSEditableNodeContainer
            ? (PSEditableNodeContainer) getParent()
            : null;
   }

   /**
    * The properties are available primarily to add extra information for
    * use in list views of nodes. 
    */
   private Map<String,String> m_properties = new HashMap<>();
}

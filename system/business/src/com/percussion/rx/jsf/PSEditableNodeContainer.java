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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.error.PSRuntimeException;
import com.percussion.utils.string.PSStringUtils;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Random;

import org.apache.myfaces.trinidad.component.UIXTable;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.apache.myfaces.trinidad.event.RangeChangeListener;

/**
 * A category node for {@link PSEditableNode}.
 *
 * @author Andriy Palamarchuk
 * @author Doug Rand
 */
public abstract class PSEditableNodeContainer extends PSCategoryNodeBase 
{
   /**
    * This is used for receiving notification when the range of the (child)
    * table changes.
    */
   public class RangeChange implements RangeChangeListener
   {
      /**
       * Whenever the range of displayed rows changes, removes all previously 
       * selected node if there is any.
       */
      public void processRangeChange(RangeChangeEvent event)
      {
         try {
            if (getChildren() == null)
               return;


         for (PSNodeBase node : getChildren())
         {
            node.setSelectedRow(false);
         }
         } catch (PSNotFoundException e) {
            throw new PSRuntimeException(e);
         }
      }
   }
   
   /**
    * The listener of the child table for range change event. 
    */
   private RangeChange m_rangeChangelistener = new RangeChange();
   
   /**
    * Just calls the super constructor.
    */
   public PSEditableNodeContainer(String title, String outcome)
   {
      super(title, outcome);
   }

   /**
    * Just calls the super constructor.
    */
   public PSEditableNodeContainer(
         String title, String outcome, String label)
   {
      super(title, outcome, label);
   }

   /**
    * A dispatching edit action. It finds the selected node, and then
    * calls the perform method on the selected node. This method ignores
    * selected nodes that are not timed event notification nodes.
    * 
    * @return the outcome, determined by the selected node's method.
    * <code>null</code>, if no editable node is currently selected.
    */
   public String edit()
   {
      final PSNodeBase selected = findSelectedEditable();
      return selected == null ? PSNavigation.NONE_SELECT_WARNING : selected
            .perform();
   }

   /**
    * A dispatching copy action. It finds the selected node, and then
    * calls the copy method on the selected node. This method ignores
    * selected nodes that are not design nodes.
    * 
    * @return the outcome, determined by the selected node's method.
    */
   public String copy() throws PSNotFoundException {
      final PSEditableNode selected = findSelectedEditable();
      return selected == null ? PSNavigation.NONE_SELECT_WARNING : selected
            .copy(); 
   }

   /**
    * Sets the backing bean of the child table, called by JFS.
    * @param table the table instance, never <code>null</code>.
    */
   public void setTable(UIXTable table) 
   { 
      if (table == null)
         throw new IllegalArgumentException("table may not be null.");
      
      table.addRangeChangeListener(m_rangeChangelistener);
      m_table = table; 
   }
   
   /**
    * Gets the backing bean of the child table. This is used by JFS.
    * @return the table instance.
    */
   public UIXTable getTable() 
   { 
      return m_table; 
   }

   /**
    * The backing bean of the UI (child) table.
    */
   private UIXTable m_table;

   /**
    * The initial delete action, which dispatches to a confirmation page.
    * The confirmation page will hit {@link #removeSelected()} to actually
    * remove the underlying object if the user confirms.
    * 
    * @return the outcome. Never <code>null</code> or blank.
    */
   public String delete()
   {
      return findSelectedEditable() == null ? PSNavigation.NONE_SELECT_WARNING
            : "delete";
   }

   /**
    * A dispatching delete action. It finds the selected node, and then
    * calls the delete method on the selected node. This method ignores
    * selected nodes that are not design nodes. It is the responsibility
    * of the node to remove itself from the model - this is to allow the
    * node to not remove itself if the delete action would fail. It is also
    * the node's responsibility to determine the navigation after the action.
    * 
    * @return the outcome, determined by the selected node's method.
    */
   public String removeSelected() throws PSNotFoundException {
      final PSEditableNode selected = findSelectedEditable();
      return selected == null ? null : selected.delete();
   }

   /**
    * Finds an object for a given object name. This is used by 
    * {@link #getUniqueName(String, boolean)}. This must be overridden by 
    * derived classes; otherwise the {@link #getUniqueName(String, boolean)}
    * cannot be called by the derived classes.
    * 
    * @param name the name of the searched objects, not <code>null</code> or
    *    empty.
    *    
    * @return <code>true</code> if found an object with the specified name;
    *    return <code>false</code> otherwise.
    */
   abstract protected boolean findObjectByName(String name) throws PSNotFoundException;
   
   /**
    * Same as <code>findSelected(PSEditableNode.class)</code>.
    * @return the editable node. <code>null</code> if no node of that class
    * is found. 
    * @see PSEditableNode
    */
   private PSEditableNode findSelectedEditable()
   {
      return (PSEditableNode) findSelected(PSEditableNode.class);
   }

   /**
    * Get the type of the currently selected node. Used for the removal 
    * confirmation page to display information about the currently selected
    * object being removed.
    * 
    * @return the type, or the string "unknown".
    */
   public String getSelectedType()
   {
      final PSEditableNode selected = findSelectedEditable();
      return selected == null
            ? UNKNOWN_OUTCOME
            : PSTypeEnum.valueOf(selected.getGUID().getType()).getDisplayName();
   }
   
   /**
    * Get the name of the currently selected node. Used for the removal 
    * confirmation page to display information about the currently selected
    * object being removed.
    * @return the name, or the string "unknown". Never <code>null</code>.
    */
   public String getSelectedName()
   {
      final PSNodeBase node = findSelectedEditable();
      return node == null ? UNKNOWN_NAME : node.getLabel();
   }

   /**
    * A convenience method to creates a unique name for the nodes of this
    * category. 
    * 
    * @param baseName the base name for the unique name, must not be 
    *    <code>null</code> or empty.
    * @param isCopyFrom indicates if the name is used for "copy from" an 
    *    existing object. It is <code>true</code> if it is used when copy 
    *    from an existing object; otherwise it is used to create a new object.
    *    
    * @return the created name, never <code>null</code> or empty.
    */
   public String getUniqueName(String baseName, boolean isCopyFrom) throws PSNotFoundException {
      return getUniqueName(baseName, isCopyFrom, null);
   }

   /**
    * It is the same as {@link #getUniqueName(String, boolean)}, in addition,
    * caller may provide a list of existing names, which will be used in the
    * process, so that the returned name will not exist in the supplied list.
    *  
    * @param baseName the base name for the unique name, must not be 
    *    <code>null</code> or empty.
    * @param isCopyFrom indicates if the name is used for "copy from" an 
    *    existing object. It is <code>true</code> if it is used when copy 
    *    from an existing object; otherwise it is used to create a new object.
    * @param existingNames the name list, may be <code>null</code> if not 
    *    supplied. If it is not <code>null</code>, then the returned name will
    *    not exist in this list.
    * 
    * @return the unique name, never <code>null</code>.
    */
   public String getUniqueName(String baseName, boolean isCopyFrom, 
         Collection<String> existingNames) throws PSNotFoundException {
      PSStringUtils.notBlank(baseName, "baseName cannot be null or empty.");
      
      String name = getUniqueName(baseName, isCopyFrom, null, existingNames);
      
      // if cannot get the sequenced number, let's get one from random number
      if (name == null)
      {
         SecureRandom rd = new SecureRandom();
         while (name == null)
         {
            name = getUniqueName(
                  baseName, isCopyFrom, new SecureRandom(), existingNames);
         }
      }
      
      return name;      
   }
   
   /**
    * The same as {@link #getUniqueName(String, boolean)}, except it creates
    * the new name from a random object if supplied.
    * 
    * @param baseName the base name for the unique name, must not be 
    *    <code>null</code> or empty.
    * @param isCopyFrom indicates if the name is used for "copy from" an 
    *    existing object. It is <code>true</code> if it is used when copy 
    *    from an existing object; otherwise it is used to create a new object.
    * @param rd the random object to generate numbers for the new name.
    * @param existingNames a list of existing names, it may be <code>null</code>
    *    if not supplied. If it is not <code>null</code>, then the returned 
    *    name is not one of element in the list.
    * 
    * @return new name, never <code>null</code> or empty.
    */
   private String getUniqueName(String baseName, boolean isCopyFrom,
         SecureRandom rd, Collection<String> existingNames) throws PSNotFoundException {
      final int MAX_ATTEMPTS = 200;
      String name = null;
      int nextNum;

      for (int i = 0; i < MAX_ATTEMPTS; i++)
      {
         nextNum = rd != null ? rd.nextInt() : i;
         name = getNewName(baseName, nextNum, isCopyFrom);
         if (existingNames != null)
         {
            if (!existingNames.contains(name))
            return name;
         }
         else if (!findObjectByName(name))
         {
            return name;
         }
      }
      
      return name;
   }

   /**
    * Creates a name from a set of parameters.
    * 
    * @param baseName the base name of the new name, assumed not 
    *    <code>null</code> or empty.
    * @param number the number used to be part of the new name.
    * @param isCopyFrom <code>true</code> if the name is used as "copy from" an
    *    existing object; otherwise simply append the number to the base name.
    *    
    * @return the new name; never <code>null</code> or empty.
    */
   private String getNewName(String baseName, int number, boolean isCopyFrom)
   {
      if (isCopyFrom)
      {
         return number == 0
               ? "Copy_of_" + baseName
               : "Copy_(" + number + ")_of_" + baseName;  
      }
      else
      {
         return baseName + "_" + number;
      }
   }

   /**
    * The unknown outcome string.
    */
   private static final String UNKNOWN_OUTCOME = "unknown";

   /**
    * The string indicating an unknown name.
    */
   private static final String UNKNOWN_NAME = "unknown";   
}

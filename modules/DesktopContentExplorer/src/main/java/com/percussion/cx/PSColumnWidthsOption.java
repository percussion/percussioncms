/**[ PSColumnWidthsOption.java ]************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSXMLDomUtil;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides a way of constructing the Column Widths Option object 
 * from the XML document and to get XML document from the Column Width object. 
 */
public class PSColumnWidthsOption implements IPSClientObjects
{
   static Logger log = Logger.getLogger(PSColumnWidthsOption.class);
   /**
    * Empty constructor.
    */
   public PSColumnWidthsOption()
   {
   }

   /**
    * Default constructor, this is needed so that an instance of this class can
    * be created by reflection.
    *
    * @param sourceNode   the XML element node from which to populate.  Must not
    * be <code>null</code>.
    */
   public PSColumnWidthsOption(Element sourceNode)
   {
      try
      {
         fromXml(sourceNode);
      }
      catch (PSContentExplorerException e)
      {
         log.error(e);
      }
   }

   /** @see IPSClientObjects */
   public void fromXml(Element sourceNode) throws PSContentExplorerException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("options Element must not be null");
      try
      {
         // validate the root element:
         PSXMLDomUtil.checkNode(sourceNode, ELEM_COLUMN_WIDTHS_OPTION);
         Element el = null;

         el = PSXMLDomUtil.getFirstElementChild(sourceNode);
         while (el != null && el.getNodeName().equals(ELEM_ITEM))
         {
            String itemPath =
               PSXMLDomUtil.checkAttribute(el, ATTR_ITEM_PATH, true);

            String tmp = PSXMLDomUtil.getElementData(el);
            if (tmp != null && tmp.trim().length() > 0)
            {
               List tmpList = PSStringOperation.getSplittedList(tmp, ',');

               addItemColumnWidths(itemPath, tmpList);
            }
            el = PSXMLDomUtil.getNextElementSibling(el);
         }
      }
      catch (Exception e)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.MISC_PROCESSING_OPTIONS_ERROR,
            e.getLocalizedMessage());
      }
   }

   /** @see IPSClientObjects  */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(ELEM_COLUMN_WIDTHS_OPTION);
      doc.appendChild(root);

      // create temp element
      Element el = null;

      Iterator iter = m_map.keySet().iterator();
      while (iter.hasNext())
      {
         String itemPath = (String)iter.next();
         List widths = (List)m_map.get(itemPath);
         if (widths != null)
         {
            String strWidths = PSStringOperation.append(widths, ",");

            el = doc.createElement(ELEM_ITEM);
            el.setAttribute(ATTR_ITEM_PATH, itemPath);
            el.appendChild(doc.createTextNode(strWidths));

            root.appendChild(el);
         }
      }
      return root;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    * Overrides the method in {@link Object.equals(Object) Object} and adheres
    * to that contract.
    * @param obj the reference object with which to compare.
    * @return <code>true</code> if this object is the same as the
    * <code>obj</code> argument; <code>false</code> otherwise. If
    * <code>null</code> supplied or obj is not an instance of this class,
    * <code>false</code> is returned.
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSColumnWidthsOption comp = (PSColumnWidthsOption)obj;

      if (m_map.equals(comp.m_map) == false)
         return false;

      return true;
   }

   /**
    * Overridden to fulfill contract of this method as described in
    * {@link Object#hashCode() Object}.
    *
    * @return A hash code value for this object
    */
   public int hashCode()
   {
      int hash = 0;
      // super is abtract, don't call
      hash += m_map.hashCode();

      return hash;
   }

   /**
    * Get a column width list given the specified item path.
    *  
    * @param itemPath a string representing an existing item within the saved
    *    item map, must not be <code>null</code> or empty.
    * 
    * @return a list representing the saved column widths for the given
    *    item path, if the item path is not found returns null.
    */
   public List getItemColumnWidths(String itemPath)
   {
      if (itemPath == null || itemPath.trim().length() == 0)
         throw new IllegalArgumentException("itemPath must not be null or empty");

      return (List)m_map.get(itemPath);
   }

   /**
    * Adds the specified column width list to the specified item path in the map.
    * 
    * @param itemPath a string representing the key for the map to save the
    *    column width list for, must not be <code>null</code> or empty.
    * 
    * @param widths a list representing the width of each column for the
    *    specified item path, may be <code>null</code> or empty.
    */
   public void addItemColumnWidths(String itemPath, List widths)
   {
      if (itemPath == null || itemPath.trim().length() == 0)
         throw new IllegalArgumentException("itemPath must not be null or empty");

      m_map.put(itemPath, widths);
   }

   /**
    * Removes the specified column width list based on the item path from the map.
    * 
    * @param itemPath a string representing the key for the map to remove the
    *    column width list for, must not be <code>null</code> or empty.
    */
   public void removeItemColumnWidths(String itemPath)
   {
      if (itemPath == null || itemPath.trim().length() == 0)
         throw new IllegalArgumentException("itemPath must not be null or empty");

      m_map.remove(itemPath);
   }

   /**
    * If there are column widths set, returns true, otherwise false.
    * 
    * @return true if any column widths have been set, otherwise false
    */
   public boolean haveColumnWidths()
   {
      return !m_map.isEmpty();
   }

   /**
    * The map representing the items column widths, set to an empty map
    * here, may be changed. The key is the item path, the value is the column
    * width list for the specific item.
    */
   private Map m_map = new HashMap();

   /**
    * Name of the Root element of the XML document 
    * representing a item column width list option.
    */
   private static final String ELEM_COLUMN_WIDTHS_OPTION =
      "PSXColumnWidthsOption";

   /**
    * Name of the element representing the item for the column width list option.
    */
   private static final String ELEM_ITEM = "Item";

   /**
    * Name of the attributes defining the column width list and item path.
    */
   private static final String ATTR_ITEM_PATH = "itemPath";
}
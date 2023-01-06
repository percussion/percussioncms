/**[ PSDisplayFormatOption.java ]***********************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.util.PSXMLDomUtil;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides a way of constructing the Display Format Option object 
 * from the XML document and to get XML document from the Display Format object. 
 */
public class PSDisplayFormatOption implements IPSClientObjects
{
   static Logger log = Logger.getLogger(PSDisplayFormatOption.class);
   /**
    * Empty constructor.
    */
   public PSDisplayFormatOption()
   {
   }

   /**
    * Default constructor, this is needed so that an instance of this class can
    * be created by reflection.  {@link #fromXml(Element) fromXml(Element)}
    * should be called immediately after calling this constructor in order
    * to create a valid object.
    *
    * @param sourceNode   the XML element node from which to populate.  Must not
    * be <code>null</code>.
    * @throws PSContentExplorerException if the XML element node does not
    * represent a type supported by this class.
    */
   public PSDisplayFormatOption(Element sourceNode)
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
         PSXMLDomUtil.checkNode(sourceNode, ELEM_DISPLAY_FORMAT_OPTION);
         Element el = null;

         el = PSXMLDomUtil.getFirstElementChild(sourceNode);
         while (el != null && el.getNodeName().equals(ELEM_ITEM))
         {
            String itemPath =
               PSXMLDomUtil.checkAttribute(el, ATTR_ITEM_PATH, true);
            String displayFormatId =
               PSXMLDomUtil.checkAttribute(el, ATTR_DISPLAY_FORMAT_ID, true);

            addItemDisplayFormat(itemPath, displayFormatId);

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
      Element root = doc.createElement(ELEM_DISPLAY_FORMAT_OPTION);
      doc.appendChild(root);

      // create temp element
      Element el = null;

      Iterator iter = m_map.keySet().iterator();
      while (iter.hasNext())
      {
         String itemPath = (String)iter.next();
         String displayFormatId = (String)m_map.get(itemPath);

         el = doc.createElement(ELEM_ITEM);
         el.setAttribute(ATTR_ITEM_PATH, itemPath);
         el.setAttribute(ATTR_DISPLAY_FORMAT_ID, displayFormatId);
         root.appendChild(el);
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

      PSDisplayFormatOption comp = (PSDisplayFormatOption)obj;

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
    * Get a display format given the specified item path.
    *  
    * @param itemPath a string representing an existing item within the saved
    *    item map, must not be <code>null</code> or empty.
    * 
    * @return a string representing the saved display format for the given
    *    item path, if the item path is not found returns null.
    */
   public String getItemDisplayFormat(String itemPath)
   {
      if (itemPath == null || itemPath.trim().length() == 0)
         throw new IllegalArgumentException("itemPath must not be null or empty");

      return (String)m_map.get(itemPath);
   }

   /**
    * Adds the specified display format to the specified item path in the map.
    * 
    * @param itemPath a string representing the key for the map to save the
    *    display format for, must not be <code>null</code> or empty.
    * 
    * @param displayFormatId a string representing the display format for the
    *    specified item path, may be <code>null</code> or empty.
    */
   public void addItemDisplayFormat(String itemPath, String displayFormatId)
   {
      if (itemPath == null || itemPath.trim().length() == 0)
         throw new IllegalArgumentException("itemPath must not be null or empty");

      m_map.put(itemPath, displayFormatId);
   }

   /**
    * Removes the specified display format based on the item path from the map.
    * 
    * @param itemPath a string representing the key for the map to remove the
    *    display format for, must not be <code>null</code> or empty.
    */
   public void removeItemDisplayFormat(String itemPath)
   {
      if (itemPath == null || itemPath.trim().length() == 0)
         throw new IllegalArgumentException("itemPath must not be null or empty");

      m_map.remove(itemPath);
   }

   /**
    * If there are display formats set, returns true, otherwise false.
    * 
    * @return true if any display formats have been set, otherwise false
    */
   public boolean haveDisplayFormats()
   {
      return !m_map.isEmpty();
   }

   /**
    * The map representing the items display formats, set to an empty map
    * here, may be changed. The key is the item path, the value is the display
    * format for the specific item.
    */
   private Map m_map = new HashMap();

   /**
    * Name of the Root element of the XML document 
    * representing a item display format option.
    */
   private static final String ELEM_DISPLAY_FORMAT_OPTION =
      "PSXDisplayFormatOption";

   /**
    * Name of the element representing the item for the display format option.
    */
   private static final String ELEM_ITEM = "Item";

   /**
    * Name of the attributes defining the display format and item path.
    */
   private static final String ATTR_ITEM_PATH = "itemPath";
   private static final String ATTR_DISPLAY_FORMAT_ID = "displayFormatId";
}
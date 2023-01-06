/**[ PSExpanded.java ]**********************************************************
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
import org.w3c.dom.Text;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class provides a way of constructing the Expanded object from the
 * XML document and to get XML document from the Expanded object. 
 */
public class PSExpandedOption implements IPSClientObjects
{
   static Logger log = Logger.getLogger(PSExpandedOption.class);
   
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
   public PSExpandedOption(Element sourceNode)
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

   /**
    * Constructs this object from the supplied list of strings.
    *
    * @param paths a list of paths of the expanded items, 
    *    may not be <code>null</code>
    */
   public PSExpandedOption(List paths)
   {
      setPaths(paths);
   }

   /** @see IPSClientObjects */
   public void fromXml(Element sourceNode) throws PSContentExplorerException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("options Element must not be null");
      try
      {
         // validate the root element:
         PSXMLDomUtil.checkNode(sourceNode, ELEM_EXPANDED);
         Element el = null;

         el = PSXMLDomUtil.getFirstElementChild(sourceNode, ELEM_PATH);
         while (el != null && el.getNodeName().equals(ELEM_PATH))
         {
            addPath(PSXMLDomUtil.getElementData(el));
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
      Element root = doc.createElement(ELEM_EXPANDED);
      doc.appendChild(root);

      // create temp element
      Element el = null;

      // create temp text node
      Text textNode = null;

      Iterator iter = m_paths.iterator();
      while (iter.hasNext())
      {
         el = doc.createElement(ELEM_PATH);
         textNode = doc.createTextNode((String)iter.next());

         el.appendChild(textNode);
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

      PSExpandedOption comp = (PSExpandedOption)obj;

      if (m_paths.equals(comp.m_paths) == false)
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
      hash += m_paths.hashCode();

      return hash;
   }

   /**
    * Populates the list of paths to the expanded items.
    *
    * @param paths a list of strings respresenting the expanded items, must
    *    not be <code>null</code>, may be an empty list.
    */
   public void setPaths(List paths)
   {
      if (paths == null)
         throw new IllegalArgumentException("paths must not be null");

      Iterator iter = paths.iterator();
      while (iter.hasNext())
      {
         String path = (String)iter.next();
         addPath(path);
      }
   }

   /**
    * Returns the current list of paths.
    * 
    * @return
    */
   public Set getPaths()
   {
      return m_paths;
   }

   /**
    * Add a path to the list of paths.
    * 
    * @param path the location of the expanded item, must not be <code>null
    *    </code> or empty. 
    */
   private void addPath(String path)
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path must not be null or empty");
      
      m_paths.add(path);
   }

   /**
    * The list of paths representing the expanded items, set to an empty list
    * here, may be changed.
    */
   private Set m_paths = new TreeSet();

   /**
    * Name of the Root element of the XML document 
    * representing an expanded item.
    */
   private static final String ELEM_EXPANDED = "PSXExpandedOption";

   /**
    * Name of the element representing the path of the expanded item.
    */
   private static final String ELEM_PATH = "Path";
}
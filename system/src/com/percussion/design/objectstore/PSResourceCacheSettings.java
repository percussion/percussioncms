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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class to represent the cache settings for a resource.
 */
public class PSResourceCacheSettings extends PSComponent
{
   /**
    * Construct the cache settings with default values.  This will be caching
    * disabled, and no additional keys or dependencies.
    */
   public PSResourceCacheSettings()
   {
   }

   /**
    * Copy constructor, creates a shallow copy of the supplied
    * <code>settings</code>.
    *
    * @param settings The settings from which to make a shallow copy, may not
    * be <code>null</code>.
    */
   public PSResourceCacheSettings(PSResourceCacheSettings settings)
   {
      copyFrom(settings);
   }

   /**
    * Construct this object from its XML representation.
    *
    * @param source The XML element node to populate from, not <code>null
    * </code>.  See {@link #toXml(Document)} for the format expected.
    *
    * @throws PSUnknownNodeTypeException if <code>source</code> is not in
    * the expected format.
    */
   public PSResourceCacheSettings(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source, null, null);
   }

   /**
    * Determines if resource caching is enabled.
    *
    * @return <code>true</code> if it is enabled, <code>false</code> otherwise.
    */
   public boolean isCachingEnabled()
   {
      return m_isEnabled;
   }

   /**
    * Sets resource caching to be enabled or disabled.
    *
    * @param isEnabled <code>true</code> to enable resource caching,
    * <code>false</code> to disable it.
    */
   public void setIsCachingEnabled(boolean isEnabled)
   {
      m_isEnabled = isEnabled;
   }

   /**
    * Gets a list of resources to be dependent upon.  If the data used by those
    * resources is modified, this resource's data should be considered modified
    * as well.
    *
    * @return an Iterator over zero or more page names as <code>String</code>
    * objects in the format appName/resourceName.  Never <code>null</code>.
    * Each page name is never <code>null</code> or empty.
    */
   public Iterator getDependencies()
   {
      return m_dependencies.iterator();
   }

   /**
    * Gets a list of additional keys to use when caching this resource's data.
    *
    * @return An iterator over zero or more <code>PSNamedReplacementValue</code>
    * objects, never <code>null</code>.
    */
   public Iterator getAdditionalKeys()
   {
      return m_extraKeys.iterator();
   }

   /**
    * Sets the list of dependent resources.  See {@link #getDependencies()} for
    * more info.
    *
    * @param dependencies A list of page names as <code>String</code> objects
    * in the format appName/resourceName.  Never <code>null</code>.
    * Each page name may not be <code>null</code> or empty.
    */
   public void setDependencies(Iterator dependencies)
   {
      if (dependencies == null)
         throw new IllegalArgumentException("dependencies may not be null");

      m_dependencies.clear();

      while (dependencies.hasNext())
      {
         Object depObj = dependencies.next();
         if (!(depObj instanceof String))
            throw new IllegalArgumentException(
               "dependencies may only contain non-null Strings");

         String dep = (String)depObj;
         if (dep.trim().length() == 0)
            throw new IllegalArgumentException(
               "dependencies may not contain empty String");

         m_dependencies.add(dep);
      }
   }

   /**
    * Sets the list of additional keys.  See {@link #getAdditionalKeys()} for
    * more info.
    *
    * @param keys An iterator over zero or more
    * <code>PSNamedReplacementValue</code> objects, may not be
    * <code>null</code>.
    */
   public void setAdditionalKeys(Iterator keys)
   {
      if (keys == null)
         throw new IllegalArgumentException("keys may not be null");

      m_extraKeys.clear();

      while (keys.hasNext())
      {
         Object obj = keys.next();
         if (!(obj instanceof PSNamedReplacementValue))
         {
            throw new IllegalArgumentException(
               "keys may only contain non-null PSNamedReplacementValues");
         }

         m_extraKeys.add((IPSReplacementValue)obj);
      }
   }

   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. The format is:
    * <pre><code>
    *  &lt;!ELEMENT PSXResourceCacheSettings (Keys, Dependencies)>
    *  &lt;!ATTLIST PSXResourceCacheSettings
    *    id ID  #REQUIRED
    *    enabled (yes|no) "no"
    *  >
    *  &lt;!ELEMENT Keys (Key*)>
    *  &lt;!ELEMENT Key ((PSXCgiVariable | PSXCookie | PSXUserContext))>
    *  &lt;!ELEMENT Dependencies (Dependency*)>
    *  &lt;!ELEMENT Dependency (#PCDATA)>
    * </code></pre>
    *
    * @param doc the document used to create element, may not be <code>null
    * </code>
    *
    * @return The newly created XML element node, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      root.setAttribute(ID_ATTR, String.valueOf(getId()));
      root.setAttribute(ENABLED_ATTR, m_isEnabled ? TRUE_ATTR_VAL :
         FALSE_ATTR_VAL);

      Element keysEl = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         KEYS_XML_EL);
      Iterator keys = m_extraKeys.iterator();
      while (keys.hasNext())
      {
         Element keyEl = PSXmlDocumentBuilder.addEmptyElement(doc, keysEl,
            KEY_XML_EL);
         PSNamedReplacementValue val = (PSNamedReplacementValue)keys.next();
         keyEl.appendChild(val.toXml(doc));
      }

      Element depsEl = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         DEPS_XML_EL);
      Iterator deps = m_dependencies.iterator();
      while (deps.hasNext())
      {
         PSXmlDocumentBuilder.addElement(doc, depsEl, DEP_XML_EL,
            (String)deps.next());
      }

      return root;
   }

   /**
    * This method is called to populate this object from its XML representation.
    *
    * @param sourceNode   The XML element node to populate from, not <code>null
    * </code>.  See {@link #toXml(Document)} for the format expected.
    *
    * @param parentDoc The parent document that contains the element, may be
    * <code>null</code>.
    * @param parentComponents a collection of all the components created in
    * the process of creating this component.  May be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if <code>sourceNode</code> is not in
    * the expected format.
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException(
            "sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try
      {
         // REQUIRED: get the ID attribute
         String sTemp = sourceNode.getAttribute(ID_ATTR);
         try
         {
           m_id = Integer.parseInt(sTemp);
         }
         catch (Exception e)
         {
            Object[] args = {XML_NODE_NAME, ((sTemp == null) ? "null" : sTemp)};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         // get the enabled attribute
         m_isEnabled = TRUE_ATTR_VAL.equals(sourceNode.getAttribute(
            ENABLED_ATTR));

         // get child element data
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;

         int nextFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

         m_extraKeys.clear();
         Element keys = tree.getNextElement(KEYS_XML_EL, firstFlags);
         if (keys == null)
         {
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, KEYS_XML_EL);
         }

         Element key = tree.getNextElement(KEY_XML_EL, firstFlags);
         while (key != null)
         {
            Element child = tree.getNextElement(firstFlags);
            // PSNamedReplacementValue implements IPSReplacementValue, so this
            // will work.
            IPSReplacementValue replVal =
               PSReplacementValueFactory.getReplacementValueFromXml(parentDoc,
                  parentComponents, child, XML_NODE_NAME, KEY_XML_EL);

            // make sure its a named replacement value
            if (!(replVal instanceof PSNamedReplacementValue))
            {
               Object args[] = {XML_NODE_NAME, KEY_XML_EL, child.getNodeName()};
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }

            m_extraKeys.add(replVal);
            tree.setCurrent(key);
            key = tree.getNextElement(KEY_XML_EL, nextFlags);
         }
         tree.setCurrent(keys);

         m_dependencies.clear();
         Element deps = tree.getNextElement(DEPS_XML_EL, nextFlags);
         if (deps == null)
         {
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, DEPS_XML_EL);
         }

         Element dep = tree.getNextElement(DEP_XML_EL, firstFlags);
         while (dep != null)
         {
            String resource = PSXmlTreeWalker.getElementData(dep);
            if (resource.trim().length() == 0)
            {
               Object args[] = {XML_NODE_NAME, DEP_XML_EL, resource};
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }

            m_dependencies.add(resource);
            tree.setCurrent(dep);
            dep = tree.getNextElement(DEP_XML_EL, nextFlags);
         }

      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Performs a shallow copy of the data in the supplied
    * <code>PSServerCacheSettings</code> to this object.
    *
    * @param c A valid <code>PSServerCacheSettings</code> object. Cannot be
    * <code>null</code>.
    */
   public void copyFrom(PSResourceCacheSettings c)
   {
      try {
         super.copyFrom(c);
      }
      catch(IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_isEnabled = c.m_isEnabled;
      m_extraKeys.clear();
      m_extraKeys.addAll(c.m_extraKeys);
      m_dependencies.clear();
      m_dependencies.addAll(c.m_dependencies);
   }

   /**
    * Determines if the supplied object is equal to this one.
    *
    * @param obj An object, may be <code>null</code>.
    *
    * @return <code>true</code> if the supplied object is an instance of
    * <code>PSResourceCacheSettings</code> with the same member values.
    */
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSResourceCacheSettings))
         isEqual = false;
      else
      {
         PSResourceCacheSettings other = (PSResourceCacheSettings)obj;
         if (m_id != other.m_id)
            isEqual = false;
         if (m_isEnabled != other.m_isEnabled)
            isEqual = false;
         else if (!m_extraKeys.equals(other.m_extraKeys))
            isEqual = false;
         else if (!m_dependencies.equals(other.m_dependencies))
            isEqual = false;
      }

      return isEqual;
   }

   /**
    * Returns a hash code value for the object. See
    * {@link java.lang.Object#hashCode() Object.hashCode()} for more info.
    */
   public int hashCode()
   {
      return (m_isEnabled ? 1 : 0) + m_extraKeys.hashCode() +
         m_dependencies.hashCode();
   }

   /**
    * The xml node to represent this object.
    */
   public final static String XML_NODE_NAME = "PSXResourceCacheSettings";

   /**
    * List of <code>PSNamedReplacementValue</code> objects that are used as
    * additional keys when caching data for this resource.  Never
    * <code>null</code>, may be empty.  Modified by calls to
    * <code>fromXml()</code> and <code>setAdditionalKeys()</code>
    */
   private List m_extraKeys = new ArrayList();

   /**
    * List of <code>String</code> objects that identify resources whose data this
    * resource is dependent upon (see {@link #getDependencies()} for more info).
    * Never <code>null</code>, may be empty.  Modified by calls to
    * <code>fromXml()</code> and <code>setDependencies()</code>
    */
   private List m_dependencies = new ArrayList();


   /**
    * The flag to determine whether the caching is enabled/disabled, gets
    * initialized to <code>false</code> when the instance is created.
    */
   private boolean m_isEnabled = false;

   // XML Constants
   private static final String KEYS_XML_EL = "Keys";
   private static final String KEY_XML_EL = "Key";
   private static final String DEPS_XML_EL = "Dependencies";
   private static final String DEP_XML_EL = "Dependency";
   private static final String ENABLED_ATTR = "enabled";
   private static final String TRUE_ATTR_VAL = "yes";
   private static final String FALSE_ATTR_VAL = "no";
}

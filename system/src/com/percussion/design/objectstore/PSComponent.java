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

import com.percussion.design.objectstore.legacy.IPSComponentConverter;
import com.percussion.design.objectstore.legacy.IPSComponentUpdater;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.*;

/**
 * The PSComponent class implements some of the IPSComponent interface
 * as a convenience to objects extending this class.  When subclassing,
 * make sure to override copyFrom().
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public abstract class PSComponent implements IPSComponent, Serializable
{
  
   /**
    * 
    */
   private static final long serialVersionUID = 8994604589246361347L;

   /**
    * Gets the id assigned to this component by the UI (E2Designer).
    *
    * @return the id assigned to this component
    */
   public int getId()
   {
      return m_id;
   }

   /**
    * Sets the UI (E2Designer) id for this component.
    *
    * @param id the to assign the component
    */
   public void setId(int id)
   {
      m_id = id;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSComponent. Cannot be <code>null</code>.
    */
   public void copyFrom(PSComponent c)
   {
      if (null == c)
         throw new IllegalArgumentException("Invalid object for copy");
      setId(c.getId());
   }

   /**
    * This method is present to improve our backwards compatibility support.
    * The user of the objectstore should check if the connected server 
    * is an older version than the version of this client software. If it
    * is, the featureset from the server should be set using this method 
    * before any objectstore classes are used. To prevent garbage collection,
    * a reference to an instance of this class or the class object should be 
    * kept for the duration of use. 
    * <p>Derived classes will use this information to support writing app
    * data to servers in different formats. This will allow us to maintain
    * backwards compatibility for a longer period of time.
    * 
    * @param fs If <code>null</code>, returns immediately. The feature set 
    * obtained from the connected server. 
    * 
    * @throws IllegalStateException If called more than once.
    */
   public synchronized static void setFeatureSet(PSFeatureSet fs)
   {
      if (null != ms_features)
         throw new IllegalStateException("Can only set feature set once.");

      ms_features = new HashMap<String, Integer>();
      Iterator<?> features = fs.getFeatureSet();
      while (features.hasNext())
      {
         PSFeature feature = (PSFeature) features.next();
         Iterator<?> versions = feature.getVersionList();
         int maxVer = -1;
         while (versions.hasNext())
         {
            PSVersion ver = (PSVersion) versions.next();
            if (ver.getNumber() > maxVer)
               maxVer = ver.getNumber();
         }
         ms_features.put(feature.getName().toLowerCase(), maxVer);
      }
   }
   
   /**
    * This method checks if the requested version is present in the
    * feature set. If no feature set has been set, this method will always
    * return <code>true</code>. This provides a default behavior of writing
    * data in the newest format.
    * 
    * @param featureName The name of the feature as found in the featureset.xml
    * file. Never <code>null</code> or empty. Name is case-insensitive.
    * 
    * @param minVersion The minimum version of the feature to check for. 
    * 
    * @return <code>true</code> if the feature is found in the feature set with
    * a version greater than or equal the requested version or no feature set 
    * has been set, <code>false</code> otherwise.
    */
   protected static boolean isFeatureSupported(String featureName, 
         int minVersion)
   {
      if (null == ms_features)
         return true;
      Integer version = ms_features.get(featureName.toLowerCase());
      return null != version && version.intValue() >= minVersion;
   }

   /**
    * Add this to the list of parent objects in the array list.
    * <P>
    * After a call to this method, the caller should keep get the size
    * of the arraylist so that a call to resetParentList can
    * be made (with size - 1) to allow for proper reset.
    *
    * @param      parentComponents      the parent list
    *
    * @return      the new parent list (in case parentComponents was null)
    */
   protected ArrayList<PSComponent> updateParentList(ArrayList<PSComponent> parentComponents)
   {
      if (parentComponents == null)
         parentComponents = new ArrayList<PSComponent>();

      parentComponents.add(this);

      return parentComponents;
   }

   /**
    * Reset the list of parent objects in the array list to the specified
    * size.
    *
    * @param      parentComponents      the parent list
    *
    * @param      size                  the size to set the list to
    */
   protected void resetParentList(ArrayList<?> parentComponents, int size)
   {
      if (parentComponents == null)
         return;

      if (size == 0)
         parentComponents.clear();
      else
      {
         for (int i = parentComponents.size(); i > size;)
         {
            i--;
            parentComponents.remove(i);
         }
      }
   }


   /**
    * Safely compares two objects for equality when those objects might be
    * <code>null</code> or Arrays.
    *
    * @return <code>true</code> if both objects are <code>null</code>, or
    * if both objects are equal; <code>false</code> otherwise.
    */
   protected static boolean compare(Object a, Object b)
   {
      if (a == null || b == null)
      {
         if (a != null || b != null)
            return false;
      }
      else if (a.getClass().isArray() && b.getClass().isArray())
      {
         return Arrays.equals((Object[]) a, (Object[]) b);
      }
      else if (!a.equals(b))
         return false;

      return true;
   }

   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      // no op
   }
   

   /**
    * Set the component converters to allow on-the-fly conversion of legacy XML
    * during de-serialization.
    * 
    * @param converters The converters to set, may be <code>null</code> or empty
    * to clear the current list.
    */
   public static void setComponentConverters(
      List<IPSComponentConverter> converters)
   {
      if (converters == null)
         converters = new ArrayList<IPSComponentConverter>();

      ms_converters = converters;
   }

   /**
    * Set the component updaters to allow on-the-fly conversion of legacy object
    * during de-serialization.
    * 
    * @param updaters The updaters to set, may be <code>null</code> or empty
    * to clear the current list.
    */
   public static void setComponentUpdaters(
      List<IPSComponentUpdater> updaters)
   {
      if (updaters == null)
         updaters = new ArrayList<IPSComponentUpdater>();

      ms_updaters = updaters;
   }

   /**
    * Get a copy of the current list of converters.  
    * See {@link #setComponentConverters(List)}.
    * 
    * @return The list, never <code>null</code>, may be empty.  Changes to the
    * returned list are not reflected in this object.
    */
   public static List<IPSComponentConverter> getComponentConverters()
   {
      return new ArrayList<IPSComponentConverter>(ms_converters);
   }
   
   /**
    * Convenience method that returns the first converter for which 
    * {@link IPSComponentConverter#canConvertComponent(Class)} returns 
    * <code>true</code>.  See {@link #setComponentConverters(List)}.
    * 
    * @param clazz The class to test, may not be <code>null</code>.
    * 
    * @return The converter, or <code>null</code> if none matched.
    */
   public static IPSComponentConverter getComponentConverter(Class<?> clazz)
   {
      if (clazz == null)
         throw new IllegalArgumentException("clazz may not be null");
      
      IPSComponentConverter converter = null; 
      for (IPSComponentConverter test : ms_converters)
      {
         if (test.canConvertComponent(clazz))
         {
            converter = test;
            break;
         }
      }
      
      return converter;
   }

   /**
    * Convenience method that returns the first updater for which 
    * {@link IPSComponentUpdater#canUpdateComponent(Class)} returns 
    * <code>true</code>.  See {@link #setComponentUpdaters(List)}.
    * 
    * @param clazz The class to test, may not be <code>null</code>.
    * 
    * @return The updater, or <code>null</code> if none matched.
    */
   public static IPSComponentUpdater getComponentUpdater(Class<?> clazz)
   {
      if (clazz == null)
         throw new IllegalArgumentException("clazz may not be null");
      
      IPSComponentUpdater updater = null; 
      for (IPSComponentUpdater test : ms_updaters)
      {
         if (test.canUpdateComponent(clazz))
         {
            updater = test;
            break;
         }
      }
      
      return updater;
   }

   /**
    * Validates that <code>elem</code> is not null and has the expected name.
    *
    * @param elem Element to be validated
    * @param expectedName the name you are expecting elem to have; cannot be
    *        <code>null</code>
    * @throws PSUnknownNodeTypeException if <code>elem</code> is <code>null
    *         </code>, or has a name other than the expected name.
    */
   protected static void validateElementName(Element elem, String expectedName)
         throws PSUnknownNodeTypeException
   {
      if (null == expectedName)
         throw new IllegalArgumentException("expectedName cannot be null");
      if (null == elem)
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, expectedName);

      //TODO: support namespaces correctly (requires DOM level 2)
      //String localName = elem.getLocalName();
      //String namespace = elem.getNamespaceURI();
      String localName = elem.getNodeName();

      if (!expectedName.equals(localName))
      {
         Object[] args = { expectedName, localName };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
   }

   /**
    * Gets the element data from an <code>#IMPLIED</code> attribute.  If the
    * data is <code>null</code> it will return <code>null</code>.
    * <p />
    * This was added to accomodate the replacewith node that's used for
    * control deprecation.
    * <p />
    * @param tree a valid PSXmlTreeWalker currently positioned at the element
    *        that should contain the specified attribute.
    * @param attrName the name of the attribute to retrieve data from
    *        not <code>null</code> or empty.
    * @return a value for the specified attribute, may return <code>null</code>
    */
   protected static String getImpliedAttribute(PSXmlTreeWalker tree,
      String attrName)
   {
      if (null == tree)
         throw new IllegalArgumentException("tree cannot be null");
      if (null == attrName || attrName.length() == 0)
         throw new IllegalArgumentException("attrName cannot be null or empty.");

      String data = tree.getElementData(attrName);

      return data;
   }
   
   /**
    * Convenience method that calls {@link #getEnumeratedAttribute(
    * PSXmlTreeWalker, String, String[], boolean) getEnumeratedAttribute(tree, 
    * attrName, legalValues, false)}.
    */
   protected static String getEnumeratedAttribute(PSXmlTreeWalker tree,
      String attrName, String[] legalValues) throws PSUnknownNodeTypeException
   {
      return getEnumeratedAttribute(tree, attrName, legalValues, false);
   }

   /**
    * Gets the element data from an attribute and validates that the data is a
    * legal value. If the data is <code>null</code> or empty, it will be set
    * with a default value (assumed to be the value at index 0 of the legal
    * value array).
    * 
    * @param tree a valid PSXmlTreeWalker currently positioned at the element
    *    that should contain the specified attribute.
    * @param attrName the name of the attribute to retrieve data from; not
    *    <code>null</code>
    * @param legalValues the array of permitted values, with a default value at
    *    index 0.
    * @return a legal value for the specified attribute, never 
    *    <code>null</code> or empty.
    * @throws PSUnknownNodeTypeException if the node has an illegal value.
    */
   protected static String getEnumeratedAttribute(PSXmlTreeWalker tree,
      String attrName, String[] legalValues, boolean ignoreCase) 
      throws PSUnknownNodeTypeException
   {
      if (null == tree)
         throw new IllegalArgumentException("tree cannot be null");
      if (null == attrName)
         throw new IllegalArgumentException("attrName cannot be null");
      if (null == legalValues || legalValues.length == 0)
         throw new IllegalArgumentException("legalValues");

      String data = tree.getElementData(attrName, false);
      if (null == data || data.trim().length() == 0)
         // no value means use the default
         data = legalValues[0];
      else
      {
         // make sure the value is legal
         boolean found = false;
         for (int i=0; !found && i<legalValues.length; i++)
         {
            if (legalValues[i] != null)
               found = ignoreCase ? legalValues[i].equalsIgnoreCase(data) : 
                  legalValues[i].equals(data);
         }

         if (!found)
         {
            String parentName = tree.getCurrent().getNodeName();
            Object[] args =
            { parentName, attrName, data };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }
      
      return data;
   }

   /**
    * Gets the element data from a required attribute or child element. It is an
    * error for a required node to be absent or empty.
    * 
    * @param tree a valid PSXmlTreeWalker currently positioned at the element
    *           that is the parent of the required node, not <code>null</code>.
    * @param elemName the name of the node to retrieve data from, not
    *           <code>null</code>.
    * @param fromRoot <code>true</code> to start the search from the root
    *           element, <code>false</code> to start from the current element
    *           in the tree.
    * @return a <code>String</code> containing the element data from the
    *         specified node, never <code>null</code> or empty.
    * @throws PSUnknownNodeTypeException if the specified node is missing or
    *            empty.
    */
   protected static String getRequiredElement(PSXmlTreeWalker tree,
      String elemName, boolean fromRoot) throws PSUnknownNodeTypeException
   {
      if (tree == null)
         throw new IllegalArgumentException("tree cannot be null");

      if (elemName == null)
         throw new IllegalArgumentException("elemName cannot be null");

      String data = tree.getElementData(elemName, fromRoot);
      if (data == null || data.trim().length() == 0)
      {
         String parentName = tree.getCurrent().getNodeName();
         Object[] args = {  parentName, elemName, "null or empty" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      return data;
   }

   /**
    * Same as {@link #getRequiredElement(PSXmlTreeWalker, String, boolean)}
    * starting the search always from the root.
    */
   protected static String getRequiredElement(PSXmlTreeWalker tree,
      String elemName) throws PSUnknownNodeTypeException
   {
      return getRequiredElement(tree, elemName, true);
   }

   // see interface for description
   @Override
   public Object clone()
   {
      try
      {
         return super.clone();
      } catch (CloneNotSupportedException e) {} // cannot happen
      return null;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSComponent)) return false;
      PSComponent that = (PSComponent) o;
      return m_id == that.m_id;
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_id);
   }

   /**
    * Extracts the data for this class from the supplied element. If not
    * present, an exception is thrown. All base classes should call this 
    * method while performing their <code>fromXml</code>.
    * <p>See {@link #toXml(Element)} for the dtd.
    * 
    * @param source Never <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException If the data for this class is not 
    * found in the supplied node.
    */
   protected void fromXml(Element source)
      throws PSUnknownNodeTypeException
   {
      String data = source.getAttribute(ID_ATTR);
      try 
      {
         setId(Integer.parseInt(data));
      } 
      catch (NumberFormatException e) 
      {
         String[] args = 
         { 
            source.getNodeName(), 
            ((data == null) ? "null" : data) 
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }
   }

   /**
    * Adds the data for this class to the supplied element as attributes.
    * All base classes should call this method while performing their <code>
    * toXml</code>.
    * <p>Conforms to the following dtd:
    * <pre>
    * &lt;!ELEMENT sourceName ...&gt;
    * &lt;!ATTLIST sourceName
    *    id CDATA #REQUIRED
    *    &gt;
    * </pre>
    */
   protected Element toXml(Element source)
   {
      source.setAttribute(ID_ATTR, ""+getId());
      return source;
   }


   /**
    * The id assigned to this component. Defaults to {@link #UNKNOWN_ID} 
    */
   protected int m_id = UNKNOWN_ID;

   protected static final String ID_ATTR = "id";
   
   /**
    * The initial unknown id, which is the default value for m_id.
    */
   protected static final int UNKNOWN_ID = 0;
   
   /**
    * Each entry has a string key that is the lower-cased version of the 
    * feature and a value that is an Integer object containing the latest 
    * version. <code>null</code> until set with the <code>setFeatureSet</code> 
    * method, then never modified. It is assumed that versions increase with 
    * time.
    */
   private static Map<String, Integer> ms_features;
   
   /**
    * List of converters, never <code>null</code>, empty unless modified by 
    * {@link #setComponentConverters(List)}.
    */
   private static List<IPSComponentConverter> ms_converters = 
      new ArrayList<IPSComponentConverter>();

   /**
    * List of updaters, never <code>null</code>, empty unless modified by 
    * {@link #setComponentConverters(List)}.
    */
   private static List<IPSComponentUpdater> ms_updaters = 
      new ArrayList<IPSComponentUpdater>();
   
}


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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.impl.jaxb.Pair;
import com.percussion.rx.config.impl.jaxb.Property;
import com.percussion.rx.config.impl.jaxb.PropertySet;
import com.percussion.rx.config.impl.jaxb.SolutionConfig;
import com.percussion.rx.config.impl.jaxb.SolutionConfigurations;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A class used for normalizing the Percussion CM System configuration files 
 * and merging the normalized properties. The configuration files are in XML
 * and defined by the XMSschema file<code>localConfig.xsd</code>.
 * 
 */
public class PSConfigNormalizer
{
   /**
    * Normalizes the supplied configuration file.
    * 
    * @param in Input stream corresponding to the config file. Must not be
    * <code>null</code> assumes the caller closes the stream.
    * 
    * @return Map of name value pairs of normalized properties, may be empty
    * never <code>null</code>.
    * 
    * @throws JAXBException if error occurs.
    */
   public Map<String, Object> getNormalizedMap(InputStream in)
      throws JAXBException
   {
      return getNormalizedMap(in, true);
   }
   
   /**
    * This is the same as {@link #getNormalizedMap(InputStream)}, except it has
    * optional to include the fully qualified names for all the map values.
    *  
    * @param in Input stream corresponding to the config file. Must not be
    * <code>null</code> assumes the caller closes the stream.
    * @param resolveValueMap <code>true</code> if the returned map includes
    * the fully qualified names for all the map values. 
    * 
    * @return Map of name value pairs of normalized properties, may be empty
    * never <code>null</code>.
    * 
    * @throws JAXBException if error occurs.
    */
   public Map<String, Object> getNormalizedMap(InputStream in,
         boolean resolveValueMap) throws JAXBException
   {
      SolutionConfigurations sc = getSolutionConfigurations(in);
      Map<String, Object> result = solConfToNormMap(sc);
      if (resolveValueMap)
      {
         Map<String, Object> tgtMap = new HashMap<String, Object>();
         tgtMap.putAll(result);
         appendFQNames(tgtMap, null, result);
         result = tgtMap;
      }
      
      return result;
   }
   
   @SuppressWarnings("unchecked")
   private void appendFQNames(Map<String, Object> tgtMap, String prefix,
         Map<String, Object> srcMap)
   {
      //for (Map.Entry<String, Object> entry : srcMap.entrySet())
      for (String k : srcMap.keySet())
      {
         String key = prefix == null ? k : prefix + "." + k;
         Object value = srcMap.get(k);
         if (prefix != null)
            tgtMap.put(key, value);
         if (value instanceof Map)
         {
            appendFQNames(tgtMap, key, (Map<String, Object>)value);
         }
      }
   }
   
   /**
    * Unmarshall config file.
    * 
    * @param is Input stream corresponding to the config file. Must not be
    * <code>null</code> assumes the caller closes the stream.
    * @return unmarshalled SolutionConfigurations
    * @throws JAXBException 
    */
   private SolutionConfigurations getSolutionConfigurations(InputStream is)
      throws JAXBException
   {
      JAXBContext jc = JAXBContext
            .newInstance("com.percussion.rx.config.impl.jaxb");
      Unmarshaller unmarshaller = jc.createUnmarshaller();

      SolutionConfigurations collection = (SolutionConfigurations) unmarshaller
            .unmarshal(is);

      return collection;
   }

   /**
    * Processes the "Solution Configurations" within the configuration
    * file and returns a normalized map of Property Name to Property value.
    * 
    * @param sc The JAXB Collection of "Solution Configurations". This is 
    * represented by the <code><SolutionConfiguration></code> tag in the
    * XML configuration. Within that tag, there can be multiple 
    * <code><SolutionConfig></code> tags, each one representing a different
    * solution (configurable entity of the Percussion CM System. May be empty,
    * never <code>null</code>.
    * @return Map of name value pairs of normalized properties, may be empty
    * never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Map<String, Object> solConfToNormMap(SolutionConfigurations sc)
   {
      Map<String, Object> nameMap = new HashMap<String, Object>();
      List<SolutionConfig> solConfList = sc.getSolutionConfig();

      for (SolutionConfig solConf :  solConfList)
      {
         List<Object> propOrPropSetList = solConf.getPropertyOrPropertySet();
         String prefix = 
            sc.getPublisherPrefix().trim() + "." + solConf.getName().trim();

         nameMap = processPropertyOrPropertySetList( propOrPropSetList, 
               prefix, nameMap);
         
      }
      return nameMap;
   }


   /**
    * Process a list of Property or PropertySet elements from the Deployer
    * configuration file. Adds the elements found to the provided property map.
    * 
    * @param propOrPropSetList The list of Property or PropertySet elements to
    * be processed.
    * @param prefix The prefix of the property key under which these Property or
    * PropertySet elements should be stored. May not be <code>null</code>, but 
    * may be empty.
    * @param map The Map into which to store the Property data. May be
    * <code>null</code>.
    * @return The map with the property data. May be <code>null</code>.
    */
   private Map<String, Object> 
   processPropertyOrPropertySetList(
         List<Object> propOrPropSetList, String prefix, Map<String, Object> map)
   {
      if (propOrPropSetList == null)
         throw new IllegalArgumentException("propOrPropSetList may not be null");
      
      if (map == null)
         map = new HashMap<String, Object>();
      
      m_tagLevel++;
      for (Object obj : propOrPropSetList)
      {
         if (obj == null) 
         {
            continue;
         }
         
         if (obj instanceof Property)
         {
            map = processProperty( (Property) obj, prefix, map);
         }
         else if (obj instanceof PropertySet)
         {
            PropertySet propSet = (PropertySet) obj;
            boolean isValid;
            isValid = isValidPropertySet(propSet, propOrPropSetList, prefix);
            if (!isValid)
               continue;

            // Now parse the PropertySet
            map = processPropertySet(propSet, m_tagLevel == 1 ? prefix : null,
                  map); 
            
         }
      }  
      m_tagLevel--;
      return map;
   }

   /**
    * Validates the given propertySet. Make sure it has non-blank name if it
    * is at the 1st level.
    * 
    * @param propSet the propertySet in question, assumed not <code>null</code>.
    * @param propOrPropSetList the sibling (include itself) lists, assumed not
    * <code>null</code>.
    * @param prefix the prefix used to create map key, may be <code>null</code>
    * or empty.
    * 
    * @return <code>null</code> if valid; <code>false</code> otherwise.
    */
   private boolean isValidPropertySet(PropertySet propSet,
         List<Object> propOrPropSetList, String prefix)
   {
      // Check for sibling Properties to this PropertySet.
      // Business Rules (as opposed to parsing rules):
      // 1) PropertySets with Sibling Properties may not have name attribute
      // 2) All PropertySets at tag level 1 must have names.
      String psName = propSet.getName();
      Boolean hasSiblingProperties = 
         checkForSiblingProperties( propOrPropSetList);
       if (psName == null)
       {
          if (m_tagLevel <= 1)
          {
             ms_log.warn("PropertySet [" + prefix +
                   "] at first tag level must have a name. " + 
                   "This PropertySet has been ignored.");                   
             return false;
          }
             
          if (hasSiblingProperties)
          {
             // TODO: include better exception with property data
             // log: "PropertySet whose siblings are properties must have a name."
             ms_log.warn("PropertySet [" + prefix +
                   "] whose siblings are Properties must have a name." + 
                   "This PropertySet has been ignored.");                   
             return false;
          }
       }
       
       return true;
   }

   /**
    * Process a PropertySet element of the Deployer configuration file. Adds the 
    * elements found to the provided property map. A map will be created if
    * one is not provided. A PropertySet tag can contain:
    * <ul>
    *   <li>Property tags</li>
    *   <li>PropertySet tags</li>
    * </ul>
    * Method recurses when a Property contains one or more PropertySets until 
    * it reaches the leaf node. 
    * <p>
    * PropertySets containing only PropertySets are treated specially: 
    * <ol>
    *   <li>the names of the child PropertySets are ignored.</li>
    *   <li>a List of Maps is created instead of a Map of Maps</li>
    * </ol>
    * <p>
    * @param propertySet The PropertySet tag object being parsed. 
    * May not be <code>null</code>.
    * @param prefix The property key under which this property should be stored.
    * May be <code>null</code> or empty.
    * @param map The Map into which to store the Property data.
    * May be <code>null</code>. If <code>null</code>, a map will be created and
    * returned. This allows a local copy of a map to be created and used as 
    * user sees fit.
    * @return The map with the property data. Never <code>null</code>.
    */
   private Map<String, Object>
   processPropertySet(PropertySet propertySet, String prefix, Map<String, Object> map)
   {
      // Validate Argument: PropertySet
      if (propertySet == null)
      {
         throw new IllegalArgumentException("propertySet may not be null");
      }

      // Validate Argument: map
      // If a map is passed in, use it. 
      // If not, create one.
      Map<String, Object> returnMap = null;  
      if (map == null)
         returnMap = new HashMap<String, Object>();
      else
         returnMap = map;
      
      // Validate Argument: propertySet
      // Set up the name of the PropertySet and the full PropertySet key 
      String propKey = getPropKey(prefix, propertySet.getName());        
      
      // Get the children of this property set
      List<Object> propOrPropSet = propertySet.getPropertySetOrProperty();
      Object valueObj = getPropertySetValue(propOrPropSet);
      returnMap.put(propKey, valueObj);

      return returnMap;
   }

   /**
    * Gets a property key from a prefix and a property name.
    * 
    * @param prefix
    * @param propName
    * @return
    */
   private String getPropKey(String prefix, String propName)
   {
      propName = (propName != null ? propName.trim() : "");
      return StringUtils.isBlank(prefix) ? propName : prefix + "." + propName;
   }
   
   //@SuppressWarnings("unchecked")
   /**
    * Process a Property element of the Deployer configuration file. Adds the 
    * elements found to the provided property map. A map will be created if
    * one is not provided. Handles the element types:
    * <ul>
    *   <li>Simple Property</li>
    *   <li>Property Value List</li>
    *   <li>Property Value Pairs List</li>
    *   <li>PropertySet</li>
    * </ul>
    * Method recurses when a Property contains one or more PropertySets until 
    * it reaches the leaf node. 
    * <p>
    * Properties containing only PropertySets are treated specially: 
    * <ol>
    *   <li>a Map if all propertySet elements have the name attribute.</li>
    *   <li>a List of Maps if one of the propertySet does not have name attribute</li>
    * </ol>
    * <p>
    * @param prop The Property tag object being parsed. 
    * May not be <code>null</code>.
    * @param prefix The prefix of the property key under which this property 
    * should be stored. May be <code>null</code> or empty.
    * @param map The Map into which to store the Property data.
    * May be <code>null</code>. If <code>null</code>, a map will be created and
    * returned. This allows a local copy of a map to be created and used as 
    * user sees fit.
    * @return The map with the property data. Never <code>null</code>.
    */
   private Map<String, Object>
   processProperty(Property prop, String prefix, Map<String, Object> map)
   {

      // Check arguments
      if (prop == null)
      {
         throw new IllegalArgumentException("prop may not be null");
      }
      // If a map is passed in, use it. 
      // If not, create one to pass back.
      Map<String, Object> returnMap = null;  
      if (map == null)
      {
         returnMap = new HashMap<String, Object>();
      }
      else
      {
         returnMap = map;
      }

      
      String propName = prop.getName();
      propName = (propName != null ? propName.trim() : "");
      String propKey = getPropKey(prefix, propName);
      String value = null;
      String propValue = null;
      Property.Pvalues values = null;
      List<PropertySet> propertySetList = null;
      
      
      // Simple (propValue) Property Value
      value = prop.getValue();       // tag attribute "value"
      propValue = prop.getPvalue();  // element tag "pValue"
      if ( value != null && propValue == null)
      {
         returnMap.put(propKey, new String(value));
      }
      else if ( propValue != null)
      {
         returnMap.put(propKey, new String(propValue));
      }
      else if ((values = prop.getPvalues()) != null)
      {
         Object valuesObj = getValues(values);
         returnMap.put(propKey, valuesObj);
      }

      //  A PropertySet
      else if ( (propertySetList = prop.getPropertySet()) != null &&
                !propertySetList.isEmpty() )
      {
         List<Object> objectList = new ArrayList<Object>();
         for (PropertySet ps : propertySetList)
            objectList.add(ps);

         Object valueObj = getPropertySetValue(objectList);
         returnMap.put(propKey, valueObj);
      }
      else
      {
         returnMap.put(propKey, null);
      }

      return returnMap;
   }

   /**
    * Converts a list of propertySet(s) to a list or a map.
    * 
    * @param propSetList the value (or child components) of a propertySet,
    * assumed not <code>null</code> or empty.
    * 
    * @return a list or map of the value, never <code>null</code>.
    */
   private Object getPropertySetValue(List<Object> propSetList)
   {
      Boolean flags[] = checkForPropertySets( propSetList);
      Boolean allSiblingsArePropertySets = flags[0];
      Boolean allSiblingsHaveNames       = flags[1];
      // If all siblings of the PropertySet are PropertySets, 
      // but not all siblings have names,ignore the names of all the siblings
      // and build a special List of Maps of Properties or PropertySets.
      if (allSiblingsArePropertySets && !allSiblingsHaveNames)
      {
         return createPropSetNameList(propSetList);
      }
      else 
      {
         return  processPropertyOrPropertySetList( propSetList, null, null);
      }
   }
   
   /**
    * Converts the {@link Property.Pvalues} object. It may contain a list of
    * pvalue or a list of pairs.
    * 
    * @param values the values, assumed not <code>null</code>.
    * 
    * @return a list of {@link String} or {@link PSPair}. It may be 
    * <code>null</code> if the list is empty.
    */
   private Object getValues(Property.Pvalues values)
   {
      List<String> valuesList = null; 
      List<Pair>   pairList   = null; 

      // Simple properties
      if ( (valuesList = values.getPvalue()) != null &&
            !valuesList.isEmpty())
      {
         // deep copy
         List<String> newValues = new ArrayList<String>();
         for (String s : valuesList)
         {
            newValues.add(new String(s));
         }
         return newValues;
      }
      // Pairs
      // May be pairs of attribute values: "value1", "value2"
      // or tagged values "pvalue1" or "pvalue2".
      else if ( (pairList = values.getPair()) != null &&
            !pairList.isEmpty())
      {
         List<PSPair<String,String>> newPairs = getPairList(pairList);
         return newPairs;
      }
      
      return Collections.emptyList();
   }
   
   /**
    * Converts the list of pair (from Jaxb) to a list of {@link PSPair}
    * 
    * @param pairList the list of Jaxb pairs, assumed not <code>null</code>.
    * 
    * @return the converted list, never <code>null</code>, may be empty.
    */
   private List<PSPair<String,String>> getPairList(List<Pair> pairList)
   {
      List<PSPair<String,String>> newPairs = 
         new ArrayList<PSPair<String,String>>();
      for (Pair xpair : pairList)
      {
         PSPair<String,String> npair = 
            new PSPair<String,String>();
         String pvalue1 = xpair.getPvalue1();
         String pvalue2 = xpair.getPvalue2();
         String firstVal  = null;
         String secondVal = null;
         // First, try to use the "pvalues" tag values.
         // Next, try for the tag attribute "values".
         // Finally use an empty string.
         if (pvalue1 != null && pvalue2 != null)
         {
            firstVal  = pvalue1;
            secondVal = pvalue2;
         }
         else
         {
            firstVal  = xpair.getValue1();
            secondVal = xpair.getValue2();
         }
         if (firstVal == null && secondVal == null)
         {
            firstVal = "";
            secondVal = "";
         }
         npair.setFirst(new String(firstVal));
         npair.setSecond(new String(secondVal));
         newPairs.add(npair);
      }
      
      return newPairs;
   }
   
   /**
    * Tests if any members of the provided list are instances of "Property".
    *  
    * @param siblingList List of sibling objects to check. May not be
    * <code>null</code>, but may be empty. 
    * @return True - sibling list contains a property object.
    */
   private Boolean checkForSiblingProperties( List<Object> siblingList)
   {
      if (siblingList == null)
      {
         throw new IllegalArgumentException("siblingList may not be null");
      }

      // Walk through list, check siblings:
      //  If all or any siblings are Properties, note it.
      //  This PropertySet will require a Map.
      Boolean hasPropertySibling = false;
      for ( Object sibling : siblingList)
      {
         if (sibling instanceof Property  &&  hasPropertySibling == false)
         {
            hasPropertySibling = true;
         }
      } 
      return hasPropertySibling;
   }

   

   /**
    * Tests if all members of the provided list are instances of "PropertySet".
    *  
    * @param objectList Objects to test for "PropertySet"-ness. May not be
    * <code>null</code>, but may be empty. 
    * @return Array of Boolean flags: <br>
    * [0] - True if all objects in the <code>objectList</code> are PropertySets.<br>
    * [1] - True if all objects in list have names 
    */
   private Boolean[] checkForPropertySets(List<Object> objectList)
   {
      if (objectList == null)
      {
         throw new IllegalArgumentException("objectList may not be null");
      }
      
      Boolean [] returnFlags =  new Boolean[2];
      Boolean allObjectsArePropSets = true;
      Boolean allObjectsHaveNames = true;
      if (objectList.isEmpty())
      {
         allObjectsArePropSets = false;
      }
      
      // Walk through child list, check children:
      //  If all children are PropertySets, note it.
      //  -This PropertySet will require a List.
      for ( Object object : objectList)
      {
         if (!(object instanceof PropertySet))
         {
            allObjectsArePropSets = false;
         }
         else if ((object instanceof PropertySet) && 
               StringUtils.isBlank(((PropertySet)object).getName()))
         {
            allObjectsHaveNames = false;
         }
      } 

      returnFlags[0] = allObjectsArePropSets;
      returnFlags[1] = allObjectsHaveNames;
      
      return returnFlags;
   }

   /**
    * Creates a List of Maps of each of the (child) properties in each
    * of the PropertySets in the given list. This method is called when
    * all of the children of PropertySet are themselves PropertySets.
    * 
    * @param childPropSetList The list for PropertySets. Each of the 
    * child Properties or PropertySets in this list should be added to the 
    * resulting list.
    * 
    * @return A List of Maps of properties.
    */
   private List<Object>
   createPropSetNameList(List<Object>childPropSetList)
   {
      List<Object> listOfPropSetMaps = new ArrayList<Object>();

      // Traverse the list of PropertySets (which contains only PropertySets)
      for (Object childPropSet: childPropSetList)
      {
         // Be sure it is in fact a PropertySet
         if (!(childPropSet instanceof PropertySet))
         {
            return listOfPropSetMaps; // return what you have
         }

         // From this PropertySet, extract the Map of Properties/PropertySets
         // and add it to the list.
         List<Object> propSetOrPropList = 
            ((PropertySet) childPropSet).getPropertySetOrProperty();

         Map<String, Object> propSetOrPropMap = new HashMap<String, Object>();
         propSetOrPropMap = processPropertyOrPropertySetList( propSetOrPropList, 
               null, null);

         listOfPropSetMaps.add(propSetOrPropMap);
      }
      return listOfPropSetMaps;
   }
   
//------   
   
   /**
   * The "tag level" is the embedded depth at which the tag occurs.
   *   It is used to determine whether the "propertySet" tag is 
   *    occurring at the first level of the configuration file. 
   *    If a "propertySet" tag has embedded "propertySet" tags, the embedded
   *    ones' name attributes are optional and all properties of the embedded
   *    "propertySet" tags must be coalleced into a List of Maps (instead of 
   *    a Map of Maps. 
   */
   private int m_tagLevel = 0;
   
   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static Logger ms_log = Logger.getLogger(
         "com.percussion.rx.config.impl.PSConfigNormalizer");

}

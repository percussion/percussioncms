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


package com.percussion.cms.objectstore;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class is used to store a multi-part identifier for a component. Each
 * part of the key has a unique name (within an instance). In general, the
 * key parts are the column names of the primary key for a specific component
 * type.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSKey implements IPSCmsComponent, Serializable
{
   /**
    * Creates an assigned key. An assigned key has values for the key parts.
    * The constructed object will not need generate id,
    * {@link #needGenerateId()} return <code>false</code>.
    *
    * @param definition  The names of the parts that make up this key. Each
    *    entry in the supplied array must be a non-<code>null</code>,
    *    non-empty string.
    *
    * @param values  The values for each key part. The i'th value must
    *    correspond to the i'th entry in the definition. Each entry in the
    *    supplied array may be <code>null</code> or empty.
    *
    * @param persisted A flag to indicate whether this key represents a
    *    component that exists in the db. Realize that this is a snapshot,
    *    so the component represented by this key could be removed while
    *    this key is instantiated.
    */
   public PSKey(String [] definition, String [] values, boolean persisted)
   {
      this(definition, values, persisted, false);
   }

   /**
    * Just like the {@link #PSKey(String[],String[],boolean) other} ctor,
    * except there is an additional parameter, <code>needGenerateId</code>.
    *
    * @param needGenerateId see {@link #needGenerateId()} for detail.
    */
   public PSKey(String [] definition, String [] values, boolean persisted,
      boolean needGenerateId)
   {
      init(definition, values, persisted, needGenerateId, false);
   }

   /**
    * Just like the {@link #PSKey(String[],String[],boolean) other} ctor,
    * except the values are supplied as integers.
    * See the javadoc of that method for details.
    */
   public PSKey(String [] definition, int [] values, boolean persisted)
   {
      this(definition, values, persisted, false);
   }

   /**
    * Just like the {@link #PSKey(String[],int[],boolean) other} ctor,
    * except there is an additional parameter, <code>needGenerateId</code>.
    *
    * @param needGenerateId see {@link #needGenerateId()} for detail.
    */
   public PSKey(String [] definition, int [] values, boolean persisted,
      boolean needGenerateId)
   {
      if (values == null)
         throw new IllegalArgumentException("The values may not be null");

      String[] sValues = new String[values.length];
      for (int i=0; i < sValues.length; i++)
         sValues[i] = Integer.toString(values[i]);

      init(definition, sValues, persisted, needGenerateId, false);
   }

   /**
    * This ctor is used to create a <code>null</code>, or unassigned key.
    * The {@link #needGenerateId()} return <code>true</code> and
    * {@link #isPersisted()} return <code>false</code>.
    *
    * @param definition  The names of the parts that make up this key. Each
    *    entry in the supplied array must be a non-<code>null</code>,
    *    non-empty string.
    */
   public PSKey(String [] definition)
   {
      this(definition, true);
   }

   /**
    * Just like the {@link #PSKey(String[]) other} ctor,
    * except the {@link #needGenerateId()} is determined by the parameter,
    * <code>needGenerateId</code>.
    *
    * @param needGenerateId see {@link #needGenerateId()} for detail.
    */
   public PSKey(String [] definition, boolean needGenerateId)
   {
      init(definition, null, false, needGenerateId, true);
   }

   /**
    * This ctor can be used to create the definition and data from a
    * previously serialized key.
    * <p>Derived classes must implement a similar ctor and call this one.
    *
    * @param source The xml previously created with <code>toXml</code>.
    *    Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException See {@link #fromXml(Element)}.
    */
   public PSKey(Element source) throws PSUnknownNodeTypeException
   {
      fromXml(source, false);
   }


   /**
    * Default ctor, needed by derived classes, such as PSLocator(Element)
    */
   protected PSKey()
   {
   }

   /**
    * Initialize the instance from the given set of parameters. Do the same as
    * {@link #PSKey(String[],String[],boolean) other} ctor,
    * except the following.
    *
    * @param values <code>values</code> can be <code>null</code> when
    *    the <code>allowNullValues</code> is <code>false</code>.
    *
    * @param persisted see {@link #isPersisted()} for detail
    *
    * @param needGenId see {@link #needGenerateId()} for detail. It cannot
    *    be <code>true</code> if <code>persisted</code> is <code>true</code>.
    *
    * @param allowNullValues <code>true</code> then <code>values</code> can be
    *    <code>null</code> for creating empty object.
    */
   private void init(String [] definition, String [] values, boolean persisted,
      boolean needGenId, boolean allowNullValues)
   {
      if (definition == null)
         throw new IllegalArgumentException("The definition may not be null");
      if ((! allowNullValues) && values == null)
         throw new IllegalArgumentException("The values may not be null");
      if (values != null && definition.length != values.length)
         throw new IllegalArgumentException(
            "The length of definition and values must be the same");

      if (persisted == true && needGenId == true)
         throw new IllegalArgumentException(
            "needGenId has to be false if persisted is true");

      m_isPersisted = persisted;
      m_needGenerateId = needGenId;

      m_definition = new String[definition.length];

      for (int i=0; i < definition.length; i++)
      {
         if (definition[i] == null && definition[i].trim().length() == 0)
            throw new IllegalArgumentException(
               "The definition may not be null or empty");

         String value = (allowNullValues && values == null) ? "" : values[i];

         if (value == null)
            value = "";

         m_nameValueMap.put(definition[i].toLowerCase(), value);
         m_definition[i] = definition[i];
      }
   }

   /**
    * Each key is made up of 1 or more named parts. This method returns the
    * # of parts in this key.
    *
    * @return a positive integer.
    */
   public int getPartCount()
   {
      return m_nameValueMap.size();
   }

   /**
    * This method is used to determine if this key instance has a value or
    * is just a definition. If the {@link #isPersisted()} method returns
    * <code>true</code>, this method is guaranteed to return <code>true</code>.
    *
    * @return <code>true</code> if this key has values assigned for its parts,
    *    otherwise <code>false</code>.
    */
   public boolean isAssigned()
   {
      for (int i=0; i < m_definition.length; i++)
      {
         if (getPart(m_definition[i]).trim().length() == 0)
            return false;
      }

      return true;
   }

   /**
    * A persisted key is one that references an existing db component (at the
    * point in time that the key was created). If the state of the object
    * is known to change, the {@link #setPersisted(boolean) setPersisted}
    * method can be used to resynchronize the key with the db.
    *
    * @return <code>true</code> if this key references a component that exists
    *    in the db, <code>false</code> otherwise.
    */
   public boolean isPersisted()
   {
      return m_isPersisted;
   }

   /**
    * Determines whether the object need a generated id or not.
    *
    * @return <code>true</code> if it does need the id; <code>false</code>
    *    otherwise.
    */
   public boolean needGenerateId()
   {
      return m_needGenerateId;
   }

   /**
    * See {@link #isPersisted()} for a description. In general, this method
    * should only be used by the system.
    *
    * @param persisted The new persisted state.
    */
   public void setPersisted(boolean persisted)
   {
      m_isPersisted = persisted;

      if (m_isPersisted)
         m_needGenerateId = false;
   }


   /**
    * Assigns a new value to each key part.
    *
    * @param values  Must have a non-<code>null</code>, non-empty entry for
    *    each key part in the same order as the definition. Never
    *    <code>null</code>.
    */
   public void assign(String[] values)
   {
      String[] def = getDefinition();
      if (def.length != values.length)
      {
         throw new IllegalArgumentException("Expected " + def.length
               + " values, but got " + values.length + ".");
      }

      //validate the values
      for (int i=0; i < def.length; i++)
      {
         if (null == values[i] || values[i].trim().length() == 0)
         {
            throw new IllegalArgumentException(
                  "Null or empty value not allowed for key.");
         }
      }

      for (int i=0; i < values.length; i++)
      {
         setPart(def[i], values[i]);
      }
      m_needGenerateId = false;
   }


   /**
    * Removes all values. After this method returns, the <code>isAssigned
    * </code> and <code>isPersisted</code> methods will return <code>false
    * </code>.
    */
   public void clear()
   {
      m_needGenerateId = true;
      m_isPersisted = false;
      for (int i=0; i < m_definition.length; i++)
         m_nameValueMap.put(m_definition[i].toLowerCase(), "");
   }

   /**
    * Gets the value for a particular part of this key. If the key is
    * unassigned, the empty string is returned.
    *
    * @param name  The name of the key part for which you want the value.
    *    Never <code>null</code> or empty. If the supplied name is not a part
    *    of this key, an exception is thrown. The comparison is done case-
    *    insensitive.
    *
    * @return The value for the named part, or "" if no value has been
    *    assigned.
    */
   public String getPart(String name)
   {
      String value = (String) m_nameValueMap.get(name.toLowerCase());

      if (value == null)
         return "";
      else
         return value;
   }

   /**
    * Convenience method that calls {@link #getPart(String) 
    * getPart(getDefinition()[0])}.
    */
   public String getPart()
   {
      return getPart(getDefinition()[0]);
   }

   /**
    * Gets the names for all the parts making up this key.
    *
    * @return An array whose length is equal to the value returned by
    *    {@link #getPartCount()}. If the key is assigned, each entry is a
    *    name with 1 or more chars, otherwise, all entries are "".
    */
   public String [] getDefinition()
   {
      return m_definition;
   }

   /**
    * Compares only the definition of the supplied key with this key.
    *
    * @param key May be <code>null</code>.
    *
    * @return If all the parts of the supplied kay and this key have the
    *    same name (case insensitive compare) and are in the same order,
    *    <code>true</code> is returned. Otherwise, or if <code>null</code>,
    *    <code>false</code> is returned.
    */
   public boolean isSameType( PSKey key )
   {
      return isSameType(key.m_definition);
   }

   /**
    * Just like {@link #isSameType(PSKey)}, exception it takes array of
    * <code>String</code>.
    */
   private boolean isSameType(String[] definition)
   {
      if (m_definition.length != definition.length)
      {
         return false;
      }
      else
      {
         for (int i=0; i < m_definition.length; i++)
         {
            if (! m_definition[i].equals(definition[i]))
               return false;
         }
      }
      return true;
   }

   /**
    * Convenience method for {@link #getPart(String)}, converting the value
    * to an integer. See that method for full description.
    *
    * @return The requested key part converted to int.
    *
    * @throws NumberFormatException If the value returned by <code>
    *    getPart</code> cannot be parsed as an integer.
    */
   public int getPartAsInt(String name)
      throws NumberFormatException
   {
      String value = getPart(name);

      if ( value == null )
         throw new NumberFormatException(name + " does not exist");

      return Integer.parseInt(value);
   }

   /**
    * Convenience method that calls {@link #getPartAsInt(String) 
    * getPartAsInt(getDefinition()[0])}.
    */
   public int getPartAsInt()
   {
      return getPartAsInt(getDefinition()[0]);
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * &lt;!ELEMENT PSXKey (Property+)>
    * &lt;!ATTLIST PSXKey
    *    isPersisted        (yes|no) yes
    *    needGenerateId     (yes|no) no
    *    >
    * &lt;!ELEMENT Property (#PCDATA)>
    * </pre>
    * Where <code>Property</code> is the name of various definitions.
    * the <code>needGenerateId</code> attribute only needed for non-persisted
    * key object.
    *
    * @param doc Used to generate the element. Never <code>null</code>.
    *
    * @return the generated element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("doc must be supplied");

      Element root = doc.createElement(getNodeName());
      if ( ! m_isPersisted ) // if not default value
      {
         root.setAttribute(XML_ATTR_IS_PERSISTED, XML_FALSE);
         if ( m_needGenerateId )
            root.setAttribute(XML_ATTR_NEED_GEN_ID, XML_TRUE);
      }

      Iterator entries = Arrays.asList(m_definition).iterator();
      while (entries.hasNext())
      {
         String name = (String) entries.next();

         String val = (String) m_nameValueMap.get(name.toLowerCase());

         PSXmlDocumentBuilder.addElement(doc, root, name, val);
      }

      return root;
   }

   //see interface for description
   public void fromXml(Element src)
      throws PSUnknownNodeTypeException
   {
      fromXml(src, true);
   }

   /**
    * This method allows the fromXml to be used by the ctor and the 1 param
    * version. The 1 param version will throw an exception if the key parts
    * of the serialized version don't match this one. During ctor, we
    * don't have any parts to compare to, so we need to skip that step.
    *
    * @param src Assumed not <code>null</code>.
    *
    * @param validate If <code>true</code>, the key parts in the source are
    *    used to initialize this method. Otherwise, the key parts in the
    *    source must match the key parts of this method or an exception is
    *    thrown.
    *
    * @throws PSUnknownNodeTypeException If validate is <code>true</code> and
    *    the key parts don't match, or any elements or attributes are not
    *    correct.
    */
   private void fromXml(Element src, boolean validate)
      throws PSUnknownNodeTypeException
   {
      if (null == src)
         throw new IllegalArgumentException("src must be supplied");

      PSXMLDomUtil.checkNode(src, getNodeName());

      // get the attributes
      String sIsPersisted = PSXMLDomUtil.checkAttribute(src,
         XML_ATTR_IS_PERSISTED, false);
      String sNeedGenerateId = PSXMLDomUtil.checkAttribute(src,
         XML_ATTR_NEED_GEN_ID, false);

      // get the properties / definition & values
      List defs = new ArrayList();
      Map nameValueMap = new HashMap();
      Element propEl = PSXMLDomUtil.getFirstElementChild(src);
      while (propEl != null)
      {
         String name = propEl.getNodeName();
         String value = PSXMLDomUtil.getElementData(propEl);

         nameValueMap.put(name.toLowerCase(), value);
         defs.add(name);

         propEl = PSXMLDomUtil.getNextElementSibling(propEl);
      }
      String[] definition = new String[defs.size()];
      for (int i=0; i < defs.size(); i++)
         definition[i] = (String) defs.get(i);

      // validate the key part
      if (validate && !isSameType(definition))
      {
         Object[] args = {arrayToString(m_definition), arrayToString(definition)};

         throw new PSUnknownNodeTypeException(
            IPSCmsErrors.KEY_PARTS_NOT_MATCH, args);
      }

      // set all internal variables from the retrieved values

      m_definition = definition;

      m_nameValueMap.clear();
      m_nameValueMap.putAll(nameValueMap);

      if (sIsPersisted.trim().length() == 0)
         m_isPersisted = true;
      else
         m_isPersisted = sIsPersisted.equalsIgnoreCase(XML_TRUE);
      if (m_isPersisted)
         m_needGenerateId = false;
      else if (sNeedGenerateId.trim().length() != 0)
         m_needGenerateId = sNeedGenerateId.equalsIgnoreCase(XML_TRUE);
      else if (isAssigned())
         m_needGenerateId = false;
      else
         m_needGenerateId = true;
   }

   /**
    * Helper method to convert array of strings to a single string for error
    * messages.
    *
    * @param definition The string array, assume not <code>null</code>.
    *
    * @return The string representation of the parameter.
    *    Never <code>null</code>
    */
   private String arrayToString(String[] definition)
   {
      StringBuffer buffer = new StringBuffer();

      for (int i=0; i < definition.length; i++)
      {
         if (i > 0)
            buffer.append(",");
         buffer.append(definition[i]);
      }

      return buffer.toString();
   }

   /**
    * By default, takes the base class name and replaces the leading PS with
    * PSX. If the base name doesn't begin with PS, the base name is returned.
    *
    * @return The name to use for the Element returned by toXml and expected
    *    by fromXml. Never <code>null</code> or empty.
    */
   public String getNodeName()
   {
      String name = getClass().getName();
      name = name.substring(name.lastIndexOf('.')+1);
      if ( name.startsWith("PS"))
         name = "PSX" + name.substring(2);
      return name;
   }


   //see interface for description
   public Object clone()
   {
      PSKey copy = null;
      try
      {
         copy = (PSKey) super.clone();
         copy.m_definition = m_definition.clone();
         copy.m_nameValueMap = new HashMap();
         Iterator pairs = m_nameValueMap.keySet().iterator();
         while (pairs.hasNext())
         {
            String key = (String) pairs.next();
            copy.m_nameValueMap.put(key, m_nameValueMap.get(key));
         }
      }
      catch (Exception e)
      { /* not possible */
         System.out.println("PSKey.clone() caught exception: \n" + e.toString());
      }

      return copy;
   }

   /**
    * Since it is expected that derived classes will only add behavior (and not
    * data) that makes this class easier to use in specific situations, this 
    * method assumes that no data outside this class needs to be compared when
    * checking equality. 
    * <p>This allows instances of derived classes to be equivalent to instances 
    * of this class where a typical implementation would be asymmetrical.
    * <p>If a derived class has additional data that must be considered, it
    * should override this method and call it, then test the additional data
    * it has.
    */
   public boolean equals(Object obj)
   {
      if ( null == obj || !(getClass().isInstance(obj) 
            || obj.getClass().isInstance(this)))
      {
         return false;
      }
      else
      {
         PSKey other = (PSKey) obj;

         return m_nameValueMap.equals(other.m_nameValueMap) &&
            m_isPersisted == other.m_isPersisted &&
            m_needGenerateId == other.m_needGenerateId;
      }
   }


   //see interface for description
   public int hashCode()
   {
      return m_nameValueMap.hashCode() + ("" + (m_isPersisted ? 1 : 0) +
         (m_needGenerateId ? 1 : 0)).hashCode();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuffer rval = new StringBuffer(80);
      rval.append("<PSKey ");
      for(int i = 0; i < m_definition.length; i++)
      {
         String def = m_definition[i];
         String val = getPart(def);
         rval.append(def);
         rval.append("=");
         rval.append(val);
         rval.append(" ");
      }
      rval.append(">");
      return rval.toString();
   }

   /**
    * Override the value of an existing part
    *
    * @param partName The name of the part, may not be <code>null</code> or
    *    empty.
    */
   protected void setPart(String partName, String value)
   {
      if (m_nameValueMap.get(partName.toLowerCase()) == null)
      {
         throw new IllegalArgumentException(
            "partName, " + partName + ", does not exist");
      }

      value = (value == null) ? "" : value;
      m_nameValueMap.put(partName.toLowerCase(), value);
   }

   /**
    * The XML element name of the PSKey class
    */
   public final static String XML_NODE_NAME = "PSXKey";

   /**
    * Private XML tag or attribute names
    */
   public static final String XML_ATTR_NEED_GEN_ID = "needGenerateId";
   public static final String XML_ATTR_IS_PERSISTED = "isPersisted";
   public static final String XML_TRUE = "yes";
   public static final String XML_FALSE = "no";

   /**
    * See {@link #needGenerateId()} for its description.
    */
   private boolean m_needGenerateId = true;

   /**
    * See {@link #isPersisted()} for its description.
    */
   private boolean m_isPersisted = false;

   /**
    * Maps the definition to its corresponding value. The map key is the
    * definition in <code>String</code>, which is normalized to lower case.
    * The map value is the value of the definition in <code>String</code>.
    * Never <code>null</code>, but may be empty.
    */
   private Map m_nameValueMap = new HashMap();

   /**
    * The definitions in its original case sensitive form. Initialized by
    * the constructor, never <code>null</code> after that.
    */
   private String[] m_definition;

}

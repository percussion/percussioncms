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

import com.percussion.security.PSSecurityProvider;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for all group provider instances.  Provides
 * wrapper element Xml services, and serves as a factory class
 * as well.
 */

public abstract class PSGroupProviderInstance extends PSComponent
   implements IPSGroupProviderInstance
{

   /**
    * Parameterless constructor for this class.  Used for serialization.
    * Should always call <code>fromXml</code> following use of this constructor.
    */
   protected PSGroupProviderInstance()
   {
   }

   /**
    * Constructor for this class.  Initializes common base class member values.
    *
    * @param name The name of this group provider.  May not be <code>null</code>
    * or empty.
    * @param type The security provider type this instance uses.  Must be one of
    * the PSSecurityprovider.SP_TYPE_xxx types.
    * @param className The name of the derived class that should be instantiated
    * to handle serialization of the content of this element.  May not be
    * <code>null</code> or empty.
    */
   public PSGroupProviderInstance(String name, int type, String className)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      if (!validateType(type))
         throw new IllegalArgumentException("type is invalid");

      if (className == null || className.trim().length() == 0)
         throw new IllegalArgumentException(
            "className may not be null or empty");

      m_name = name;
      m_type = type;
      m_className = className;
   }

   // see IPSGroupProviderInstance
   public String getName()
   {
      return m_name;
   }


   // see IPSGroupProviderInstance
   public int getType()
   {
      return m_type;
   }


   /**
    * Serializes the base PSXGroupProviderInstance element and delegates the
    * serialization of the content of that element to the derived class by a
    * call to {@link #toXmlEx(Document) toXmlEx}.
    *
    * see {@link IPSGroupProviderInstance#toXml(Document)} for more information.
    */
   public final Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute("id", String.valueOf(m_id));
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(TYPE_ATTR,
         PSSecurityProvider.getSecurityProviderTypeString(m_type));
      root.setAttribute(CLASSNAME_ATTR, m_className);
      root.appendChild(toXmlEx(doc));

      return root;
   }

   /**
    * Restores this class's state from its Xml representation.  Restores base
    * class state and then calls {@link #fromXmlEx(Element) fromXmlEx} to
    * delegate the restoration of the derived class state.
    * See {@link IPSComponent#fromXml(Element, IPSDocument, ArrayList)
    * IPSComponent.fromXml} and {@link #fromXml(Element) fromXml}for more info.
    *
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported or does not contain valid attribute values.
    */
   public final void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      validateElementName(sourceNode, XML_NODE_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { XML_NODE_NAME, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      m_name = getRequiredElement(tree, NAME_ATTR);

      sTemp = getRequiredElement(tree, CLASSNAME_ATTR);
      if (!validateClass(sTemp))
      {
         Object[] args = {XML_NODE_NAME, CLASSNAME_ATTR, sTemp};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      m_className = sTemp;


      sTemp = getRequiredElement(tree, TYPE_ATTR);
      int type = PSSecurityProvider.getSecurityProviderTypeFromXmlFlag(sTemp);
      if (type == 0)
      {
         Object[] args = {XML_NODE_NAME, TYPE_ATTR, sTemp};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      m_type = type;

      fromXmlEx(sourceNode);
   }


   /**
    * Called by base class to allow derived classes to serialize their own
    * state.
    *
    * @param doc The document to use when creating the element containing the
    * state of this group provider.  May not be <code>null</code>.
    *
    * @return The element containing the derived class's state.  Never
    * <code>null</code>.
    */
   protected abstract Element toXmlEx(Document doc);

   /**
    * Called by base class to allow derived classes to restore their own state.
    *
    * @param source The PSXGroupProviderInstance element that contains the
    * derived class state.  May not be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported or does not contain valid attribute values.
    */
   protected abstract void fromXmlEx(Element source)
      throws PSUnknownNodeTypeException;




   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSGroupProviderInstance. May not be <code>null</code>.
    */
   public void copyFrom( PSGroupProviderInstance c )
   {
      // call super's copy
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      // copy all members
      m_name = c.m_name;
      m_type = c.m_type;
      m_className = c.m_className;

   }



   @Override
   public Object clone()
   {
      return super.clone();
   }

   /**
    * Creates an instance of an IPSGroupProviderInstance using the correct
    * derived class based on the supplied element.
    *
    * @param source The source PSXGroupProviderInstance element used to
    * instantiate the class.  May not be <code>null</code>.
    *
    * @return The group provider instance, never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported or does not contain valid attribute values.
    */
   public static IPSGroupProviderInstance newInstance(Element source)
      throws PSUnknownNodeTypeException
   {
      validateElementName(source, XML_NODE_NAME);
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      String className = getRequiredElement(tree, CLASSNAME_ATTR);
      IPSGroupProviderInstance newInstance = null;
      try
      {
         Class newClass = Class.forName(className);
         if (validateClass(newClass))
         {
            newInstance = (IPSGroupProviderInstance)newClass.newInstance();
         }
      }
      catch (IllegalAccessException e) {}
      catch (ClassNotFoundException e) {}
      catch (InstantiationException e) {}

      if (newInstance == null)
      {
         Object[] args = {XML_NODE_NAME, CLASSNAME_ATTR, className};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      newInstance.fromXml(source, null, null);
      return newInstance;
   }


   /**
    * Validates that the supplied type is one of the valid
    * <code>PSSecurityProvider.SP_TYPE_xxx</code> types.
    *
    * @param type The type to validate.
    *
    * @return <code>true</code> if the type is valid, <code>false</code> if not.
    */
   private boolean validateType(int type)
   {
      return PSSecurityProvider.isSupportedType(type);
   }

   /**
    * Validates that the supplied class is an instance of a
    * PSGroupProviderInstance.
    *
    * @param cls The Class to check, assumed not <code>null</code>.
    */
   private static boolean validateClass(Class cls)
   {
      return PSGroupProviderInstance.class.isAssignableFrom(cls);
   }

   /**
    * Validates that the supplied class name represents a class that is an
    * instance of a PSGroupProviderInstance.
    *
    * @param className The name of the Class to check, assumed not
    * <code>null</code> or empty.
    */
   private static boolean validateClass(String className)
   {
      Class cls = null;
      try
      {
         cls = Class.forName(className);
      }
      catch (ClassNotFoundException e)
      {
      }

      return cls == null ? false : validateClass(cls);
   }

   /**
    * @return the name of this group provider, never <code>null</code> or empty.
    */
   @Override
   public String toString()
   {
      return getName();
   }

   @Override
   public boolean equals(Object obj) {
      boolean isMatch = true;
      if (!(obj instanceof PSGroupProviderInstance))
         isMatch = false;
      else
      {
         PSGroupProviderInstance other = (PSGroupProviderInstance)obj;
         if (!super.equals(other))
            isMatch = false;
         else if (!this.m_className.equals(other.m_className))
            isMatch = false;
         else if (!this.m_name.equals(other.m_name))
            isMatch = false;
         else if (this.m_type != other.m_type)
            isMatch = false;
      }

      return isMatch;

   }

   @Override
   public int hashCode() {
      return super.hashCode() +
              m_name.hashCode() + m_className.hashCode() + m_type;
   }

   /**
    * Name of this group provider.  Never <code>null</code>, empty, or modified
    * after construction.
    */
   private String m_name = null;

   /**
    * The type of security provider that may use this group provider.  Intially
    * {@link PSSecurityProvider#SP_TYPE_ANY}, it is set to a valid type in the
    * ctor, never modified after that.
    */
   private int m_type = PSSecurityProvider.SP_TYPE_ANY;

   /**
    * The name of the derived class that should be instantiated to handle
    * serialization of the content of this element.  Never <code>null</code>,
    * empty, or modified after construction.
    */
   private String m_className = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
   private static final String TYPE_ATTR = "type";
   private static final String CLASSNAME_ATTR = "classname";

}

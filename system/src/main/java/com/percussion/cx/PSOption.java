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
package com.percussion.cx;

import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.lang.reflect.Constructor;

/**
 * This class represents the <code>Option</code> element in the sys_Options.dtd.
 */
public class PSOption implements IPSClientObjects
{
   /**
    * Creates an instance from the XML representation specified in the
    * dtd mentioned in the  class description.
    *
    * @param optionsElement - must not be <code>null</code>, must be defined
    * as specified in the dtd list in the class description.
    * @throws PSContentExplorerException - if optionsElement has an invalid
    * definition.
    */
   public PSOption(Element optionsElement) throws PSContentExplorerException
   {
      fromXml(optionsElement);
   }

   /**
    * Construct this option with supplied parameters.
    * 
    * @param context the context of the option, may not be <code>null</code> or
    * empty.
    * @param optionId the unique identifier of the option, may not be <code>null
    * </code> or empty.
    * @param optionValue the option value, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public PSOption(String context, String optionId, Object optionValue)
   {
      setContext(context);
      setOptionId(optionId);
      setOptionValue(optionValue);
   }

   /**
    * Given an element representation of this object this method creates the
    * object.  There is one important aspect to note about
    * this particular method.  Because this element (see the class description
    * to find dtd name) must have an "ANY" element, which means it may have text
    * or other children, there needs to be a way to store the data in a dynamic
    * fashion.  Therefore, if the <code>ELEM_OPTION</code> has a child node, the
    * <code>ATTR_OPTIONID</code> value must correspond to a PSxxxx.class in the
    * classpath,
    * or an exception will be thrown.  That class must properly implement
    * <code>IPSClientObjects</code>, and have an accessible constructor that
    * takes and <code>Element</code>.
    *
    * For example. if &lt;font&gt; is found there must be a
    * PSFont in the class path that properly implements
    * <code>IPSClientObjects</code>
    *
    * @see IPSClientObjects
    */
   public void fromXml(Element sourceNode) throws PSContentExplorerException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("optionsElement must not be null");

      try
      {
         // validate the root element
         PSXMLDomUtil.checkNode(sourceNode, ELEM_OPTION);

         // get context attribute (required)
         setContext(
            PSXMLDomUtil.checkAttribute(sourceNode, ATTR_CONTEXT, true));

         // get optionid attribute (required)
         setOptionId(
            PSXMLDomUtil.checkAttribute(sourceNode, ATTR_OPTIONID, true));

         // if has children and is one of ours try to instantiate class:
         if (PSXMLDomUtil.getFirstElementChild(sourceNode) != null
            && PSXMLDomUtil
               .getFirstElementChild(sourceNode)
               .getNodeName()
               .toLowerCase()
               .startsWith("psx"))
         {
            Element childEl = PSXMLDomUtil.getFirstElementChild(sourceNode);

            String className =
               PSXMLDomUtil.checkAttribute(sourceNode, ATTR_CLASSNAME, false);

            if (className == null || className.trim().length() == 0)
               className = getOptionId();

            IPSClientObjects clientObj = makeObject(childEl, className);

            setOptionValue(clientObj);
         }
         else
            setOptionValue(PSXMLDomUtil.getElementData(sourceNode));
      }
      catch (Exception e)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.MISC_PROCESSING_OPTIONS_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * This method locates the class specified by the argument, and returns
    * its instance.
    *
    * @param source assumed not <code>null</code>
    * @param className The name of the class that needs to be loaded to 
    * represent the option value, assumed not <code>null</code> or empty.
    * 
    * @return new instance, never <code>null</code>
    * 
    * @throws PSContentExplorerException if there is any problem accessing
    * the class or createing an instance of it from the node name provided.
    */
   private IPSClientObjects makeObject(Element source, String className)
      throws PSContentExplorerException
   {
      IPSClientObjects clientObj = null;

      try
      {
         Class theClass = Class.forName(className);
         Class[] c = { Element.class };
         Constructor ctor = theClass.getConstructor(c);

         if (ctor == null)
            throw new ClassNotFoundException("not a valid class");

         Element[] e = { source };
         Object obj = ctor.newInstance(e);
         if (!(obj instanceof IPSClientObjects))
            throw new ClassNotFoundException("not a valid class");

         clientObj = (IPSClientObjects)obj;
      }
      catch (Exception e)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.PSCLASS_INSTANTIATION_ERROR,
            new String[] { getOptionId(), getOptionId()});
      }
      return clientObj;
   }

   /**
    * This toXml() creates a XML representation of this object defined in the
    * dtd listed in the class description.  If the value of this option is of
    * type <code>IPSClientObjects</code> it calls <code>toXml()</code> on that
    * object and appends the element returned, otherwise this will call
    * <code>toString()</code> on the object stored as the value and that
    * value will be used as the #TEXT element returned by this method.
    *
    * @see IPSClientObjects
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // create root and its attributes
      Element root = doc.createElement(ELEM_OPTION);
      root.setAttribute(ATTR_CONTEXT, getContext());
      root.setAttribute(ATTR_OPTIONID, getOptionId());

      if (getOptionValue() instanceof IPSClientObjects)
      {
         IPSClientObjects clientObj = (IPSClientObjects)getOptionValue();
         root.setAttribute(ATTR_CLASSNAME, clientObj.getClass().getName());
         root.appendChild(clientObj.toXml(doc));
      }
      else
      {
         // the text value node to hold the ips field value:
         Text valueTextNode = doc.createTextNode(getOptionValue().toString());

         // add text to valueNode:
         root.appendChild(valueTextNode);
      }
      doc.appendChild(root);

      return root;
   }

   /**
    * Gets the value of this option.
    *
    * @return theOptionValue, any modifications to the
    * <code>Obect</code> will affect this class, never <code>null</code>.
    */
   public Object getOptionValue()
   {
      return m_optionValue;
   }

   /**
    * Sets the value of this option.
    * @todo the responsibility of this class has changed a bit..maybe, it seems
    * that this class is really for loading and persisting and that noone will be accessing
    * this except for handlers that know what they're looking for, if that's the case
    * this should just take a string.
    * @param theOptionValue must not <code>null</code>.
    */
   public void setOptionValue(Object theOptionValue)
   {
      if (theOptionValue == null)
         throw new IllegalArgumentException("theOptionValue must not be null");

      m_optionValue = theOptionValue;
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

      PSOption comp = (PSOption)obj;

      if (!PSOptionManagerConstants.compare(m_optionValue, comp.m_optionValue))
         return false;
      if (!m_optionId.equals(comp.m_optionId))
         return false;
      if (!m_context.equals(comp.m_context))
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

      hash += (m_optionValue != null) ? m_optionValue.hashCode() : 0;
      hash += m_optionId.hashCode();
      hash += m_context.hashCode();

      return hash;
   }

   /**
    * Gets the context value of this option.
    *
    * @return context value, never <code>null</code> or empty.
    */
   private String getContext()
   {
      return m_context;
   }

   /**
    * Sets the context value of this option.
    *
    * @param theContext must not <code>null</code> or empty.
    */
   private void setContext(String theContext)
   {
      if (theContext == null || theContext.trim().length() == 0)
         throw new IllegalArgumentException("theContext must not be null or empty");

      m_context = theContext;
   }

   /**
    * Gets the option id value of this option.
    *
    * @return optionId never <code>null</code> or empty.
    */
   public String getOptionId()
   {
      return m_optionId;
   }

   /**
    * Sets the option id value of this option.
    *
    * @param optionId must not <code>null</code> or empty.
    */
   private void setOptionId(String optionId)
   {
      if (optionId == null || optionId.trim().length() == 0)
         throw new IllegalArgumentException("optionId must not be null or empty");

      m_optionId = optionId;
   }

   /**
    * The value of this option, never <code>null</code> and is invariant.
    */
   private Object m_optionValue = null;

   /**
    * The context value of this option, never <code>null</code> or empty once
    * initialized by <code>setContext()</code>, is invariant.
    */
   private String m_context = "";

   /**
    * The id of this option, never <code>null</code> or empty once
    * initialized by <code>setOptionId()</code>, is invariant.
    */
   private String m_optionId = "";

   /**
    * Name of the element holding one unit of option
    */
   public static final String ELEM_OPTION = "PSXOption";

   /**
    * Attribute name of the element ELEM_OPTION representing the context of the
    * option.
    */
   public static final String ATTR_CONTEXT = "context";

   /**
    * Attribute name of the element ELEM_OPTION representing the name or id of
    * the option.
    */
   public static final String ATTR_OPTIONID = "optionid";

   /**
    * The attribute name that represents the class name of the element that
    * is the value (child element) of this option.
    */
   public static final String ATTR_CLASSNAME = "classname";

}

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

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to help build replacement value objects without prior
 * knowledge of their type.
 *
 * @see         IPSReplacementValue
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSReplacementValueFactory
{
   /**
    * No need to construct this.
    */
   private PSReplacementValueFactory()
   {
      super();
   }

   /**
    * Creates a new replacement value from an XML field definition.  Looks
    * up the node's name in a static hash of <code>IPSReplacementValue</code>
    * classes, and uses reflection to construct a new instance.
    *
    * @param parentDoc the doc containing this XML field.  Passed on to
    * IPSReplacementValue constructor.  May be <code>null</code>.
    * @param parentComponents the parents of this component.  Passed on to
    * IPSReplacementValue constructor.  May be <code>null</code>.
    * @param node the node to convert to a replacement value.  May not be
    * <code>null</code>.
    * @param xmlNodeName the name of the container XML node.  Used in error
    * messages.
    * @param xmlVarName the name of the XML node containing the replacement
    * value.  Used in error messages.
    *
    * @return the replacement value, never <code>null</code>
    *
    * @throws PSUnknownNodeTypeException if the node is <code>null</code>, or
    * is not a known replacement value, or if a reflection error occurs.
    */
   public static IPSReplacementValue getReplacementValueFromXml(
      IPSDocument parentDoc, ArrayList parentComponents,
      Element node, String xmlNodeName, String xmlVarName)
      throws PSUnknownNodeTypeException
   {
      if (node == null) {
         Object[] args = { xmlNodeName, xmlVarName, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      // now figure out which type it is
      IPSReplacementValue value;
      String nodeName = node.getTagName();

      try {
         Class replValueClass = (Class)ms_rvClasses.get(nodeName.toLowerCase());
         if (replValueClass == null)
         {
            Object[] args = { xmlNodeName, xmlVarName, nodeName };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         Class[] constrArgs = {
            org.w3c.dom.Element.class,
            com.percussion.design.objectstore.IPSDocument.class,
            java.util.ArrayList.class };

         Constructor constr =
            replValueClass.getDeclaredConstructor(constrArgs);
         value = (IPSReplacementValue)constr.newInstance(
            new Object[] { node, parentDoc, parentComponents });

         // we need to special case XML fields as they're not always real
         // XML fields
         if ((value != null) && (value instanceof PSXmlField)) {
            PSXmlField xmlField = (PSXmlField)value;
            value = getReplacementValueFromXmlFieldName(xmlField.getName());
         }
      }
      catch (InvocationTargetException e) {
         Throwable orig = e.getTargetException();
         Object[] args
            = { xmlNodeName, xmlVarName, nodeName + ": " + orig.toString()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      catch (PSUnknownNodeTypeException e)
      {
         // no need to wrap this in a new PSUnknownNodeTypeException
         throw e;
      }
      catch (Exception e)
      {
         Object[] args
            = { xmlNodeName, xmlVarName, nodeName + ": " + e.toString()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      return value;
   }

   /**
    * Create the appropriate replacement value from the specified XML
    * field name. We allow users to specify PSXParam/... as an XML field
    * name, though it's really a PSHtmlParameter object. This returns the
    * appropriate object type based on field name.
    *
    * @param xmlField the XML field name to build an object for
    *
    * @return the appropriate replacement value for the specified field name
    */
   public static IPSReplacementValue getReplacementValueFromXmlFieldName(
      String xmlField)
   {
      try
      {
         if ((xmlField == null) || (xmlField.length() == 0)) 
         {
            throw new IllegalArgumentException("XML field name empty");
         }

         // now figure out which type it is
         IPSReplacementValue value = null;
         String[] parsedField = parseFieldName(xmlField);         
         Constructor constr = getReplacementValueCtor(parsedField[0]);
         if (constr != null)
         {
            value = (IPSReplacementValue)constr.newInstance(
               new Object[] { parsedField[1] }
            );            
         }

         if (value == null)
            value = new PSXmlField(xmlField);

         return value;
      }
      catch (InvocationTargetException e)
      {
         Throwable orig = e.getTargetException();
         throw new IllegalArgumentException("XML param invalid: " + 
            xmlField + " " + orig.toString());
      }
      catch (IllegalAccessException e)
      {
         throw new IllegalArgumentException("XML param invalid: " + 
            xmlField + " " + e.toString());
      }
      catch (InstantiationException e)
      {
         throw new IllegalArgumentException("XML param invalid: " + 
            xmlField + " " + e.toString());
      }
   }

   /**
    * Creates a replacement value for the supplied string. Assumes the supplied
    * string to be in two parts separated by /. The first part is assumed as the
    * type like (PSX...) and second one is assumed to be value. Looks up the
    * type in a static hash of <code>IPSReplacementValue</code> classes, and
    * uses reflection to construct a new instance, if the class is an
    * instance of <code>PSNamedReplacementValue</code>. Otherwise 
    * 
    * @param repString must not be <code>null</code>.
    * @return the replacement value, never <code>null</code>
    */
   public static IPSReplacementValue getReplacementValueFromString(
         String repString)
   {
      if (StringUtils.isBlank(repString))
         throw new IllegalArgumentException("repString must not be blank");
      // now figure out which type it is
      IPSReplacementValue value = null;
      String[] parts = parseFieldName(repString);
      try
      {
         Class replValueClass = (Class) ms_rvClasses.get(parts[0]);
         String msg = "The supplied string ({0}) is not valid for creating "
               + "a replacement value.";
         Object[] args = { repString };
         if (replValueClass == null)
         {
            throw new IllegalArgumentException(MessageFormat.format(msg, args));
         }
         Class[] constrArgs = { String.class };

         Constructor constr = replValueClass.getConstructor(constrArgs);
         value = (IPSReplacementValue) constr
               .newInstance(new Object[] { parts[1] });
      }
      catch (Exception e)
      {
         String msg = "Failed to create a replacement value for the " +
               "supplied string ({0}).";
         Object[] args = { repString };
         throw new RuntimeException(MessageFormat.format(msg, args),e);
      }
      return value;
   }

   /**
    * Determine if the supplied <code>name</code> may be used to create a
    * replacement value using 
    * {@link #getReplacementValueFromXmlFieldName(String)}, or can be used to
    * specify a {@link PSXmlField}.
    * 
    * @param field The name to check, may not be <code>null</code> or empty.
    * Generally in the form PSX&lt;type&gt;/&lt;value&gt; where there is a class 
    * PS&lt;type&gt; that has a ctor in the form PSX&lt;type&gt;(String).
    *     
    * @return <code>true</code> if it is valid, <code>false</code> if not.
    */
   public static boolean isValidFieldName(String field)
   {
      if (field == null || field.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      String name = parseFieldName(field)[0];
      
      // if field does not specify a repl val class, assume it's a field, 
      // otherwise make sure we can construct the class.
      return (!ms_rvClasses.containsKey(name) || 
         getReplacementValueCtor(name) != null);
   }
   
   /**
    * Get the ctor of the class specified by the supplied field name.  If a 
    * replacement value class is specified, checks for a single arg ctor that
    * takes a <code>String</code> object. 
    * 
    * @param type The replacement value type to check, from the form specified
    * by {@link #isValidFieldName(String)}.
    * 
    * @return The matching constructor or <code>null</code> if not found.  The
    * value to supply to the ctor is appended to the supplied <code>value</code>
    * string buffer if supplied. 
    */
   private static Constructor getReplacementValueCtor(String type)
   {
      Constructor ctor = null;

         Class realClass = (Class)ms_rvClasses.get(type);
         if (realClass != null)
         {
            Class[] constrArgs = {  String.class };

            try
            {
               ctor = realClass.getDeclaredConstructor(constrArgs);
            }
            catch (NoSuchMethodException e)
            {
               // noop, fall thru, return null
            }           
         }

      
      return ctor;
   }
   
   /**
    * Parse the supplied field into its type name and value portions.
    * 
    * @param field The field to parse, see 
    * {@link #isValidFieldName(String)} for more info.
    * 
    * @return A <code>String[2]</code>, never <code>null</code>, where the first 
    * value is the type name and the second is the value, both may be 
    * <code>null</code> or empty.
    */
   private static String[] parseFieldName(String field)
   {
      String[] result = new String[2];
      int slashPos = field.indexOf('/');
      if (slashPos > 0)
      {
         result[0] = field.substring(0, slashPos).toLowerCase();
         result[1] = field.substring(slashPos+1);
      }
      else
         result[0] = field;
      
      return result;
   }

   /**
    * Maps from lowercased XML node name (String) to the proper replacement
    * value class (Class).
    */
   private static ConcurrentHashMap ms_rvClasses = new ConcurrentHashMap();

   static
   {
      // initialize the node name -> class mappings
      ms_rvClasses.put(PSBackEndColumn.ms_NodeType.toLowerCase(),
            PSBackEndColumn.class);
      ms_rvClasses.put(PSCgiVariable.ms_NodeType.toLowerCase(),
            PSCgiVariable.class);
      ms_rvClasses.put(PSHtmlParameter.ms_NodeType.toLowerCase(),
            PSHtmlParameter.class);
      ms_rvClasses.put(PSCookie.ms_NodeType.toLowerCase(),
            PSCookie.class);
      ms_rvClasses.put(PSExtensionCall.ms_NodeType.toLowerCase(),
            PSExtensionCall.class);
      ms_rvClasses.put(PSFunctionCall.XML_NODE_NAME.toLowerCase(),
            PSFunctionCall.class);
      ms_rvClasses.put(PSUserContext.ms_NodeType.toLowerCase(),
            PSUserContext.class);
      ms_rvClasses.put(PSXmlField.ms_NodeType.toLowerCase(),
            PSXmlField.class);
      ms_rvClasses.put(PSSingleHtmlParameter.ms_NodeType.toLowerCase(),
            PSSingleHtmlParameter.class);
      ms_rvClasses.put(PSDisplayFieldRef.XML_NODE_NAME.toLowerCase(),
            PSDisplayFieldRef.class);
      ms_rvClasses.put(PSUrlRequest.XML_NODE_NAME.toLowerCase(),
            PSUrlRequest.class);
      ms_rvClasses.put(PSRelationshipProperty.XML_NODE_NAME.toLowerCase(),
            PSRelationshipProperty.class);
      ms_rvClasses.put(PSOriginatingRelationshipProperty.XML_NODE_NAME.toLowerCase(),
            PSOriginatingRelationshipProperty.class);
      ms_rvClasses.put(PSContentItemStatus.XML_NODE_NAME.toLowerCase(),
            PSContentItemStatus.class);
      ms_rvClasses.put(PSContentItemData.XML_NODE_NAME.toLowerCase(),
            PSContentItemData.class);
      ms_rvClasses.put(PSMacro.ms_NodeType.toLowerCase(), PSMacro.class);

      // literals
      ms_rvClasses.put(PSDateLiteral.ms_NodeType.toLowerCase(),
            PSDateLiteral.class);
      ms_rvClasses.put(PSLiteralSet.ms_NodeType.toLowerCase(),
            PSLiteralSet.class);
      ms_rvClasses.put(PSNumericLiteral.ms_NodeType.toLowerCase(),
            PSNumericLiteral.class);
      ms_rvClasses.put(PSTextLiteral.ms_NodeType.toLowerCase(),
            PSTextLiteral.class);
      ms_rvClasses.put(PSDisplayTextLiteral.XML_NODE_NAME.toLowerCase(),
            PSDisplayTextLiteral.class);

      // old-style mappings (for backwards compatibility)
      ms_rvClasses.put("psxparam", PSHtmlParameter.class); 
      ms_rvClasses.put("psxcgivar", PSCgiVariable.class);  
      ms_rvClasses.put("psxsingleparam", PSSingleHtmlParameter.class); 
   }
}


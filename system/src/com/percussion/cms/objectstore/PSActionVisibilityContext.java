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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.Iterator;

import org.w3c.dom.Element;

public class PSActionVisibilityContext
   extends PSMultiValuedProperty
{
   /**
    * no-args constructor
    */
   
   public PSActionVisibilityContext()
   {
   }
   
   /**
    * A convenience method that calls {@link #PSActionVisibilityContext(String,
    * String[], String) PSActionVisibilityContext(name, null, null)}.
    */
   public PSActionVisibilityContext(String name)
   {
      this(name, null, null);
   }


   /**
    * A convenience method that calls {@link #PSActionVisibilityContext(String,
    * String[], String) PSActionVisibilityContext(name, new String[] {value},
    * null)}.
    */
   public PSActionVisibilityContext(String name, String value)
   {
      this(name, new String[] {value}, null);
   }


   /**
    * Create a new property for the visibility contexts.
    *
    * @param name The property name. Never <code>null</code> or empty.
    *    Immutable after being set.  Should be one of the
    *    <code>VIS_CONTEXT_XXX</code> constant values, but this is not enforced.
    *
    * @param values  0 or more values to assign to this property. May be
    *    <code>null</code>. All the values must be unique, case-insensitive,
    *    or an exception is thrown.
    *
    * @param desc An optional string that describes what this property is
    *    used for.
    */
   public PSActionVisibilityContext(String name, String[] values, String desc)
   {
      super(PSVisibilityContextEntry.class, name);
      if (null != values)
      {
         for (int i=0; i < values.length; i++)
         {
            add(values[i]);
         }
      }
      setDescription(desc);
   }


   /**
    * Create one from a previously serialized one.
    *
    * @param src Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the element name doesn't match,
    *    any required attributes are missing or don't have the correct value,
    *    or any required child nodes are missing or are not correct.
    */
   public PSActionVisibilityContext(Element src)
      throws PSUnknownNodeTypeException
   {
      super(src);
   }

   //see base class for description
   protected PSCmsProperty createProperty(String name, String value)
   {
      return new PSVisibilityContextEntry(name, value);
   }

   /**
    * Constant for the name of the visibility context that specifies which
    * assignment types are valid.
    */
   public static final String VIS_CONTEXT_ASSIGNMENT_TYPE = "1";

   /**
    * Constant for the name of the visibility context that specifies which
    * communities are valid.
    */
   public static final String VIS_CONTEXT_COMMUNITY = "2";

   /**
    * Constant for the name of the visibility context that specifies which
    * content types are valid.
    */
   public static final String VIS_CONTEXT_CONTENT_TYPE = "3";

   /**
    * Constant for the name of the visibility context that specifies which
    * object types are valid.
    */
   public static final String VIS_CONTEXT_OBJECT_TYPE = "4";

   /**
    * Constant for the name of the visibility context that specifies which
    * client contexts are valid.
    */
   public static final String VIS_CONTEXT_CLIENT_CONTEXT = "5";

   /**
    * Constant for the name of the visibility context that specifies which
    * checkout status are valid.
    */
   public static final String VIS_CONTEXT_CHECKOUT_STATUS = "6";

   /**
    * Constant for the name of the visibility context that specifies which
    * roles are valid.
    */
   public static final String VIS_CONTEXT_ROLES_TYPE = "7";

   /**
    * Constant for the name of the visibility context that specifies which
    * locales are valid.
    */
   public static final String VIS_CONTEXT_LOCALES_TYPE = "8";

   /**
    * Constant for the name of the visibility context that specifies which
    * workflows are valid.
    */
   public static final String VIS_CONTEXT_WORKFLOWS_TYPE = "9";

   /**
    * Constant for the name of the visibility context that specifies the context
    * for the publishable workflow state porperty.
    */
   public static final String VIS_CONTEXT_PUBLISHABLE_TYPE = "10";

   /**
    * Constant for the name of the visibility context that specifies the context
    * for the Folder Security.
    */
   public static final String VIS_CONTEXT_FOLDER_SECURITY = "11";

}
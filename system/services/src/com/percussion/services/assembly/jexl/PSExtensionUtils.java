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
package com.percussion.services.assembly.jexl;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;

/**
 * Methods to be called from Jexl that will assemble and call an existing UDF
 * extension.
 * 
 * @author dougrand
 */
public class PSExtensionUtils extends PSJexlUtilBase
{
   /**
    * Do the actual call to the extension. It would be nice if this were
    * callable from JEXL, but JEXL doesn't know about varargs
    * 
    * @param extensionContext the context, assumed never <code>null</code>
    * @param extensionName the name, assumed never <code>null</code>
    * @param args the arguments, may be empty
    * @return a return value
    */
   private Object docall(String extensionContext, String extensionName,
         Object... args)
   {
      PSExtensionWrapper wrapper = new PSExtensionWrapper(extensionContext,
            extensionName);
      try
      {
         return wrapper.call(args);
      }
      catch (PSConversionException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension")}, returns = "Object")
   public Object call(String extensionContext, String extensionName)
   {
      return docall(extensionContext, extensionName);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1)
   {
      return docall(extensionContext, extensionName, p1);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2)
   {
      return docall(extensionContext, extensionName, p1, p2);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3)
   {
      return docall(extensionContext, extensionName, p1, p2, p3);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6, p7);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @param p13 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter"),
         @IPSJexlParam(name = "p13", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12, Object p13)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12, p13);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @param p13 parameter
    * @param p14 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter"),
         @IPSJexlParam(name = "p13", description = "parameter"),
         @IPSJexlParam(name = "p14", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12, Object p13,
         Object p14)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12, p13, p14);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @param p13 parameter
    * @param p14 parameter
    * @param p15 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter"),
         @IPSJexlParam(name = "p13", description = "parameter"),
         @IPSJexlParam(name = "p14", description = "parameter"),
         @IPSJexlParam(name = "p15", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12, Object p13,
         Object p14, Object p15)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12, p13, p14, p15);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @param p13 parameter
    * @param p14 parameter
    * @param p15 parameter
    * @param p16 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter"),
         @IPSJexlParam(name = "p13", description = "parameter"),
         @IPSJexlParam(name = "p14", description = "parameter"),
         @IPSJexlParam(name = "p15", description = "parameter"),
         @IPSJexlParam(name = "p16", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12, Object p13,
         Object p14, Object p15, Object p16)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12, p13, p14, p15, p16);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @param p13 parameter
    * @param p14 parameter
    * @param p15 parameter
    * @param p16 parameter
    * @param p17 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter"),
         @IPSJexlParam(name = "p13", description = "parameter"),
         @IPSJexlParam(name = "p14", description = "parameter"),
         @IPSJexlParam(name = "p15", description = "parameter"),
         @IPSJexlParam(name = "p16", description = "parameter"),
         @IPSJexlParam(name = "p17", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12, Object p13,
         Object p14, Object p15, Object p16, Object p17)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @param p13 parameter
    * @param p14 parameter
    * @param p15 parameter
    * @param p16 parameter
    * @param p17 parameter
    * @param p18 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter"),
         @IPSJexlParam(name = "p13", description = "parameter"),
         @IPSJexlParam(name = "p14", description = "parameter"),
         @IPSJexlParam(name = "p15", description = "parameter"),
         @IPSJexlParam(name = "p16", description = "parameter"),
         @IPSJexlParam(name = "p17", description = "parameter"),
         @IPSJexlParam(name = "p18", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12, Object p13,
         Object p14, Object p15, Object p16, Object p17, Object p18)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @param p13 parameter
    * @param p14 parameter
    * @param p15 parameter
    * @param p16 parameter
    * @param p17 parameter
    * @param p18 parameter
    * @param p19 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter"),
         @IPSJexlParam(name = "p13", description = "parameter"),
         @IPSJexlParam(name = "p14", description = "parameter"),
         @IPSJexlParam(name = "p15", description = "parameter"),
         @IPSJexlParam(name = "p16", description = "parameter"),
         @IPSJexlParam(name = "p17", description = "parameter"),
         @IPSJexlParam(name = "p18", description = "parameter"),
         @IPSJexlParam(name = "p19", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12, Object p13,
         Object p14, Object p15, Object p16, Object p17, Object p18, Object p19)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19);
   }

   /**
    * Call an existing extension
    * 
    * @param extensionContext the context of the target extension, never
    *           <code>null</code> or empty
    * @param extensionName the name of the target extension, never
    *           <code>null</code> or empty
    * @param p1 parameter
    * @param p2 parameter
    * @param p3 parameter
    * @param p4 parameter
    * @param p5 parameter
    * @param p6 parameter
    * @param p7 parameter
    * @param p8 parameter
    * @param p9 parameter
    * @param p10 parameter
    * @param p11 parameter
    * @param p12 parameter
    * @param p13 parameter
    * @param p14 parameter
    * @param p15 parameter
    * @param p16 parameter
    * @param p17 parameter
    * @param p18 parameter
    * @param p19 parameter
    * @param p20 parameter
    * @return the result of the extension call
    */
   @IPSJexlMethod(description = "call an existing extension", params =
   {
         @IPSJexlParam(name = "extensionContext", description = "the context of the target extension"),
         @IPSJexlParam(name = "extensionName", description = "the name of the target extension"),
         @IPSJexlParam(name = "p1", description = "parameter"),
         @IPSJexlParam(name = "p2", description = "parameter"),
         @IPSJexlParam(name = "p3", description = "parameter"),
         @IPSJexlParam(name = "p4", description = "parameter"),
         @IPSJexlParam(name = "p5", description = "parameter"),
         @IPSJexlParam(name = "p6", description = "parameter"),
         @IPSJexlParam(name = "p7", description = "parameter"),
         @IPSJexlParam(name = "p8", description = "parameter"),
         @IPSJexlParam(name = "p9", description = "parameter"),
         @IPSJexlParam(name = "p10", description = "parameter"),
         @IPSJexlParam(name = "p11", description = "parameter"),
         @IPSJexlParam(name = "p12", description = "parameter"),
         @IPSJexlParam(name = "p13", description = "parameter"),
         @IPSJexlParam(name = "p14", description = "parameter"),
         @IPSJexlParam(name = "p15", description = "parameter"),
         @IPSJexlParam(name = "p16", description = "parameter"),
         @IPSJexlParam(name = "p17", description = "parameter"),
         @IPSJexlParam(name = "p18", description = "parameter"),
         @IPSJexlParam(name = "p19", description = "parameter"),
         @IPSJexlParam(name = "p20", description = "parameter")}, returns = "Object")
   public Object call(String extensionContext, String extensionName, Object p1,
         Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
         Object p8, Object p9, Object p10, Object p11, Object p12, Object p13,
         Object p14, Object p15, Object p16, Object p17, Object p18,
         Object p19, Object p20)
   {
      return docall(extensionContext, extensionName, p1, p2, p3, p4, p5, p6,
            p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20);
   }
}

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

package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import org.w3c.dom.Element;

/**
 * Interface implemented by a class that handles conversion of a type of
 * objectstore component where the old XML is no longer supported by the current
 * component class. The new component class ctor that takes an element should
 * check the XML and if it requires conversion, invoke the converter for which
 * {@link #canConvertComponent(Class)} returns <code>true</code>, and then
 * copy the state of the returned component to itself rather than calling it's
 * own <code>fromXml()</code> method.
 */
public interface IPSComponentConverter
{
   /**
    * Uses the supplied element containing the legacy XML to correctly 
    * instantiate the new component, handling any required configuration 
    * changes.
    *   
    * @param source The legacy XML element, may not be <code>null</code> and
    * must be of the type the converter is expecting based.  See
    * {@link #canConvertComponent(Class)}.
    * 
    * @return The converted object, can be casted to the class for which 
    * {@link #canConvertComponent(Class)} returns <code>true</code>.
    * 
    *  @throws PSUnknownNodeTypeException if there is a problem with the source
    *  XML.
    */
   public Object convertComponent(Element source) 
      throws PSUnknownNodeTypeException;

   /**
    * Determines if this converter supports converting elements to the specified
    * class.  Each converter should handle a single class, and it is expected
    * that it will know the legacy XML format of that class.
    *  
    * @param type The type for which the serialized XML requires conversion, 
    * may not be <code>null</code>.
    * 
    * @return <code>true</code> if this converter supports the supplied type,
    * <code>false</code> if not.
    */
   public boolean canConvertComponent(Class type);

   /**
    * Indicates if the component should be converted to a default configuration
    * (implementation specific for each converter).  Value is set by calling 
    * framework using {@link #setForcedConversion(boolean)}.
    * 
    * @return <code>true</code> if conversion to the default configuration is
    * required, <code>false</code> if a normal conversion should be performed.
    */
   public boolean isForcedConversion();

   /**
    * Set to indicate the converter will force a conversion to the default
    * configuration.  See {@link #isForcedConversion()} for details.
    * 
    * @param isRequired <code>true</code> to force the conversion, 
    * <code>false</code> otherwise.
    */
   public void setForcedConversion(boolean isRequired);

   /**
    * Set on the converter the current conversion context that may be used in
    * log messages.  Implementations should handle this in a thread safe manner
    * as instances of converters should be otherwise stateless and may be 
    * executed by multiple threads.
    *  
    * @param ctx The string used to identify the current context, such as the
    * name of the application currently loading.  May be <code>null</code> or
    * empty to clear the context.
    */
   public void setConversionContext(String ctx);
   
   /**
    * Get the name of the current context (such as the name of the application
    * currently loading).
    * 
    * @return The name, may be <code>null</code>, never empty.
    */
   public String getConversionContext();
}

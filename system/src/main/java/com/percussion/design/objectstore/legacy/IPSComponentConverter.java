/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

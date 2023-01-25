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

package com.percussion.utils.spring;

import com.percussion.utils.xml.PSInvalidXmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface to allow classes that are implemented as Spring beans to be loaded
 * from and saved to a Spring beans config file.  See the 
 * {@link PSSpringConfiguration} for more information. 
 */
public interface IPSBeanConfig
{
   /**
    * Constant for the root element name for a spring bean.
    */
   public static final String BEAN_NODE_NAME = "bean";

   /**
    * Serializes the bean to its XML format.  Result must conform to the
    * "spring-beans.dtd" DTD.
    * 
    * @param doc The document to use, never <code>null</code>.
    * 
    * @return The "bean" root element, never <code>null</code>. 
    */
   public Element toXml(Document doc);


   /**
    * Serializes the bean from its XML format.  See {@link #toXml(Document)}
    * for more info.
    * 
    * @param source The source "bean" XML root element, never <code>null</code>.
    * 
    * @throws PSInvalidXmlException If the xml format is invalid for the bean.
    */
   public void fromXml(Element source) throws PSInvalidXmlException;

   /**
    * Get the name of this bean.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getBeanName();

   /**
    * Get the name of the class used to instantiate this bean.
    * 
    * @return The class name, never <code>null</code> or empty.
    */
   public String getClassName();
}

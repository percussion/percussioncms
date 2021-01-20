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
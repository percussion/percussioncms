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
package com.percussion.rx.config.impl.spring;

import com.percussion.rx.config.IPSBeanProperties;

import java.io.File;

/**
 * This interface exposed some methods for internal use, such as unit test.
 *
 * @author YuBingChen
 */
public interface IPSBeanPropertiesInternal extends IPSBeanProperties
{
   /**
    * Gets the properties file. This is not exposed through the interface, but
    * can be called directly, e.g., from unit test.
    * 
    * @return the properties file, never <code>null</code>.
    */
   File getPropertiesFile();
   

}

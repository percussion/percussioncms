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
/**
 * 
 */
package com.percussion.extension;

/**
 * This interface must be implemented by extensions that are used
 * as field validators.
 * <p>
 * The field value is available in a request parameter of the same name and can
 * be obtained either by configuring a Single HTML Parameter replacement value
 * as an input parameter to the extension, or else directly from the request 
 * context. The <code>processUdf()</code> method must return an object of type
 * <code>Boolean</code> indicating <code>true</code> if the field passed
 * validation, <code>false</code> if not. 
 * <p>
 * At run-time, the extension handler that handles this extension will
 * construct an instance of this class. This occurs when Rhythmyx calls
 * the {@link IPSExtensionHandler#prepare <code>prepare</code>} method
 * of the {@link IPSExtensionHandler <code>IPSExtensionHandler</code>}
 * managing the extension.
 * <p>
 * <em>NOTE:</em> The IPSFieldValidator implementation must be safe for
 * multi-threaded use. One instance of the class will be defined for
 * each usage in an application. For example, if the UDF is defined
 * to act in two separate mappings within the data mapper, two
 * separate instances of the class will be created. However, the same
 * instance may be accessed simultaneously by several threads. Each
 * thread will have its own set of context data. As such, any execution
 * specific variables should be defined within the method (not the
 * class). Another alternative is to use variables of type
 * <code>java.lang.ThreadLocal</code> to define thread specific
 * copies of the variable.
 */
public interface IPSFieldValidator extends IPSUdfProcessor
{

}

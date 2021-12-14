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
package com.percussion.extension;

import com.percussion.data.PSConversionException;
import com.percussion.server.IPSRequestContext;

/**
 * This interface is implemented by extensions capable of handling
 * user defined functions (UDFs). UDFs are used to generate single
 * values which can be used in bindings, conditionals, etc.
 * The primary use of UDFs is for data transformation.
 * <p>
 * At run-time, the extension handler that handles this extension will
 * construct an instance of this class. This occurs when Rhythmyx calls
 * the {@link IPSExtensionHandler#prepare <code>prepare</code>} method
 * of the {@link IPSExtensionHandler <code>IPSExtensionHandler</code>}
 * managing the extension.
 * <p>
 * <em>NOTE:</em> The IPSUdfProcessor implementation must be safe for
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
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 * 
 */
public interface IPSUdfProcessor extends IPSExtension
{
   /**
    * Executes the UDF with the specified parameters and request context.
    *
    * @param params The parameter values supplied with the request in the
    * appropriate order, as specified by the runtime parameter definitions
    * returned by IPSExtensionDef associated with this UDF.
    *
    * @param request The current request context.
    *
    * @throws PSConversionException If an error occurred during data
    * conversion. This exception takes two parameters, a message code and
    * an argument.  You should always pass in zero (0) for the 
    * message code.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException;
}

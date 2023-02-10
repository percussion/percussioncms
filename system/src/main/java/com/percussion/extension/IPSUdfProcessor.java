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
   Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException;
}

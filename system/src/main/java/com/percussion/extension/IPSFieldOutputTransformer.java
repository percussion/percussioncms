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
/*
 * 
 */
package com.percussion.extension;

/**
 * This interface must be implemented by extensions that are used
 * as field output transformers.
 * <p>
 * The field value is available in a request parameter of the same name and can
 * be obtained either by configuring a Single HTML Parameter replacement value
 * as an input parameter to the extension, or else directly from the request 
 * context. The transformed value should be returned by the 
 * <code>processUdf()</code> method.
 * <p>
 * At run-time, the extension handler that handles this extension will
 * construct an instance of this class. This occurs when Rhythmyx calls
 * the {@link IPSExtensionHandler#prepare <code>prepare</code>} method
 * of the {@link IPSExtensionHandler <code>IPSExtensionHandler</code>}
 * managing the extension.
 * <p>
 * <em>NOTE:</em> The IPSFieldOutputTransformer implementation must be safe for
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
public interface IPSFieldOutputTransformer extends IPSUdfProcessor
{

}

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

package com.percussion.process;

import java.util.Map;

/**
 * Interface to be implemented by process parameter resolvers. The framework
 * uses classes implementing this interface in the following way:
 * <p>While parsing the xml:
 * <ol>
 *    <li>Instantiate the class defined as the resolver.</li>
 *    <li>Call {@link #setName(String) setName} with the name supplied in
 * the def.</li>
 *    <li>Call {@link #setValue(String) setValue} with the value supplied in
 * the def. If no value is present, "" is set.</li>
 * </ol>
 * During process instantiation, the {@link #getName()} and {@link 
 * #getValue(Map) getValue} methods are called and their results are passed to
 * the process or process container.
 */
public interface IPSVariableResolver
{
   /**
    * Returns the resolved value using the supplied context.
    *
    * @param value the string to resolve, may be <code>null</code> or empty
    * @param ctx a {@link Map map} that contains data for executing the
    * process, may not be <code>null</code>. Each entry has a <code>String
    * </code> key and a <code>String</code> value. The supplied parameters
    * are dependent upon the context in which the process is executed.
    *
    * @return the resolved string, may be empty, never <code>null</code>
    *
    * @throws PSResolveException if any error occurs resolving the specified
    * string
    */
   public String getValue(String value, Map ctx) throws PSResolveException;
}

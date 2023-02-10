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

/**
 * Marker interface for jexl function bindings. Classes marked with this
 * interface are usually registered with the extensions manager for use in the
 * evaluation of jexl expressions. The classes are instantiated and bound to a
 * set naming scheme. $rx.name is bound for each system context class and
 * $user.name is bound for each user context class. 
 * <p>
 * For example, $rx.codec is
 * bound to the implementation class for codec utilities. This bound name can
 * then be dereferenced to call methods, e.g. $rx.codec.base64Decoder(string)
 * will do a base64 decode of the passed string.
 * <p>
 * The velocity tools are typically also bound, but they do not implement this
 * marker interface.
 * 
 * @author dougrand
 * 
 */
public interface IPSJexlExpression extends IPSExtension
{
   // No methods defined
}

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
package com.percussion.utils.jsr170;

import javax.jcr.RepositoryException;

/**
 * Interface to be implemented by jcr objects that need to report a size to
 * determine cache status for the assembly service. This is generally used to
 * calculate an approximate size, calculating a true size is impossible in Java
 * without knowing details of each JVM. Implementers should basically report
 * major memory usage such as strings and arrays.
 * 
 * @author dougrand
 * 
 */
public interface IPSJcrCacheItem
{
   /**
    * Return an approximate site in bytes for the component. If implemented by
    * an object that contains other objects, the implementation must recurse,
    * and the child objects should implement this method as well for simplicity.
    * 
    * @return the size in bytes, might be <code>0</code>
    */
   long getSizeInBytes() throws RepositoryException;
}

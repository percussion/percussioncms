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
package com.percussion.test.util;

/**
 * Objects that want to be used in resource pools managed by {@link
 * PSResourcePool} must implement this interface.
 */
public interface IPSPooledResource
{
   /**
    * The pool manager will call this method when it is removing a resource
    * from the pool or is shutting down. The implementor should free all
    * resources and make itself ready for garbage collection.
    */
   public void close();
} 

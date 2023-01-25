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
package com.percussion.guitools;

/**
 * @author DougRand
 *
 * This interface allows a model to state that it can create new instances
 * of an appropriate type for itself.
 */
public interface IPSCreateModelItem
{
   /**
    * Create a new instance of the appropriate object for the given model.
    * If there is a problem, throws an exception.
    * 
    * @return an instance of the correct class for the model, see the
    * specific model code for details.
    * 
    * @throws InstantiationException if an instance cannot be created.
    */
   Object createInstance() throws InstantiationException;
}

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
package com.percussion.services.data;

/**
 * This interface defines one method that makes the colne a valid object that
 * can be persisted. Every object that supports clone (or copy construction)
 * must implement this.
 */
public interface IPSCloneTuner
{
   /**
    * Tune the self and return so that the returned object can be persisted.
    * Typically adjusts the id and and may additional changes to the object to
    * make it persistable. Please note that this does not clone itself and it
    * assumes it is already clone of a persisted object and makes changes to
    * self and returns.
    * 
    * @param newId new id for the object. An exact clone will have same id as
    * the its clone parent that must be changed or reset before persisting. This
    * id will be set on the self.
    * @return the tuned version of the self object that can be persisted. Never
    * <code>null</code>.
    */
   Object tuneClone(long newId);
}

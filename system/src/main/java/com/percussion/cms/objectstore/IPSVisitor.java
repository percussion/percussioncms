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
package com.percussion.cms.objectstore;

/**
 * Implemented by objects that are accepted by the IPSItemAccessor.
 */
public interface IPSVisitor
{
   /**
    * Returns an object on which to act.  This is the key part of the visitor
    * pattern implemented in the CMS layer.
    *
    * @return An object on which to act.  May be <code>null</code>.
    */
   public Object getObject();
}

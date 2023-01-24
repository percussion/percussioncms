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
package com.percussion.services.contentmgr.impl;

/**
 * Marker interface for content type lookup in the legacy content repository.
 * 
 * @author dougrand
 */
public interface IPSTypeKey
{
   /**
    * Get the content type that this key is for. This is used to remove
    * all the old configurations before processing new ones.
    * @return the content type
    */
   long getContentType();
}

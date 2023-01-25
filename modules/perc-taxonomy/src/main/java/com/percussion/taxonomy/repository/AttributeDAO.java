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

package com.percussion.taxonomy.repository;

import java.util.Collection;

import com.percussion.taxonomy.domain.*;

public interface AttributeDAO {

    public Collection getAllAttributes(int taxonomy_id, int language_id);

    public Collection getAttribute(int id);

    public void removeAttribute(Attribute attribute);

    public void saveAttribute(Attribute attribute);

    /**
     * Return all Attribute names and IDs
     */
    public Collection getAttributeNames(int taxonomy_id, int language_id);
}

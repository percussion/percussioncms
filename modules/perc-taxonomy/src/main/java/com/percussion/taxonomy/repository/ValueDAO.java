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
import java.util.Map;

import com.percussion.taxonomy.domain.*;

public interface ValueDAO {

   /////////////////////////////////////////////////////////////////////////////////////
   
    public Collection<Value> getAllValues();

    public Value getValue(int id);

    /////////////////////////////////////////////////////////////////////////////////////

    public void removeValue(Value value);

    public void saveValue(Value value);

    /////////////////////////////////////////////////////////////////////////////////////

    public Map<String, String> saveValuesFromParams(Map<String, String[]> params,
                                                    Collection<Attribute> attributes,
                                                    Node node, 
                                                    int langID, 
                                                    String user_name);

    /////////////////////////////////////////////////////////////////////////////////////

}

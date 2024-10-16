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

public interface Node_statusDAO {

    public Collection getAllNode_statuss();

    public Node_status getNode_status(int id);

    public void removeNode_status(Node_status node_status);

    public void saveNode_status(Node_status node_status);
}

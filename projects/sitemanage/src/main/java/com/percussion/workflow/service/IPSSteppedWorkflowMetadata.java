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
package com.percussion.workflow.service;

import java.util.List;

/**
 * @author JaySeletz
 *
 */
public interface IPSSteppedWorkflowMetadata
{

    /**
     * Finds all the states which are locked down by the system (were not created by
     * the user) 
     * 
     * @author federicoromanelli
     * 
     * @return a <code>List<String><code> object including all the states' names. 
     * Maybe empty or <code>null</code>
     */
    public List<String> getSystemStatesList();

}

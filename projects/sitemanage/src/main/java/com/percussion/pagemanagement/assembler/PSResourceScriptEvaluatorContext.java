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
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.data.PSResourceInstance;

/**
 * 
 * This object contains state that is passed to script evaluators
 * for resolving links, locations, mime-type, and (eventually) output.
 * <strong>Since the script can mutate this object, 
 * this object may also contain the output data of the script.</strong>
 * <p>
 * Scripts can access this object through the binding/variable
 * <code>$perc</code>. One way a link and location generation script
 * can pass its data on is by getting the {@link #getResourceInstance() resource instance} 
 * and then setting the 
 * {@link PSResourceInstance#setLinkAndLocations(java.util.List) links and locations}.
 * <p>
 * The scripts can be found in the resource definition files.
 * 
 * @author adamgent
 *
 */
public class PSResourceScriptEvaluatorContext {
    
    private PSResourceInstance resourceInstance;


    /**
     * The resource for this context.
     * @return never <code>null</code>.
     */
    public PSResourceInstance getResourceInstance()
    {
        return resourceInstance;
    }

    public void setResourceInstance(PSResourceInstance resourceInstance)
    {
        this.resourceInstance = resourceInstance;
    }
}


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
package com.percussion.itemmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper class to hold the list of {@link PSApprovableItems}.
 * 
 * @author leonardohildt
 * 
 */
@XmlRootElement(name = "ApprovableItems")
public class PSApprovableItems extends PSAbstractDataObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * List of approvable items associated to the gadget
     */
    private List<PSApprovableItem> approvableItems;

    private List<PSApprovableItem> processedItems;

    private Map<String, String> errors = new HashMap<>();

    public PSApprovableItems() {
        // empty for jax-rs
    }

    /**
     * @return the items
     */
    public List<PSApprovableItem> getApprovableItems()
    {
        return approvableItems;
    }

    public void setApprovableItems(List<PSApprovableItem> approvableItems)
    {
        this.approvableItems = approvableItems;
    }
    
    public Map<String, String> getErrors()
    {
        return errors;
    }

    public void setErrors(Map<String, String> errors)
    {
        this.errors = errors;
    }

    public List<PSApprovableItem> getProcessedItems()
    {
        return processedItems;
    }

    public void setProcessedItems(List<PSApprovableItem> processedItems)
    {
        this.processedItems = processedItems;
    }

}

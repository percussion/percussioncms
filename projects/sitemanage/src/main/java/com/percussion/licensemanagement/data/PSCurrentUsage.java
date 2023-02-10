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
package com.percussion.licensemanagement.data;

/**
 * @author LucasPiccoli
 *
 */
public class PSCurrentUsage
{
    Integer currentLivePages;
    Integer currentLiveSites;
    /**
     * @return the currentLivePages
     */
    public Integer getCurrentLivePages()
    {
        return currentLivePages;
    }
    /**
     * @param currentLivePages the currentLivePages to set
     */
    public void setCurrentLivePages(Integer currentLivePages)
    {
        this.currentLivePages = currentLivePages;
    }
    /**
     * @return the currentLiveSites
     */
    public Integer getCurrentLiveSites()
    {
        return currentLiveSites;
    }
    /**
     * @param currentLiveSites the currentLiveSites to set
     */
    public void setCurrentLiveSites(Integer currentLiveSites)
    {
        this.currentLiveSites = currentLiveSites;
    }
    
}

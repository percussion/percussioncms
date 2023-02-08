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
package com.percussion.delivery.metadata.data;


public class PSMetadataBlogResult
{
    private PSMetadataRestEntry previous;

    private PSMetadataRestEntry current;

    private PSMetadataRestEntry next;

    /**
     * @return the previous
     */
    public PSMetadataRestEntry getPrevious()
    {
        return previous;
    }

    /**
     * @param previous the previous to set
     */
    public void setPrevious(PSMetadataRestEntry previous)
    {
        this.previous = previous;
    }

    /**
     * @return the current
     */
    public PSMetadataRestEntry getCurrent()
    {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(PSMetadataRestEntry current)
    {
        this.current = current;
    }

    /**
     * @return the next
     */
    public PSMetadataRestEntry getNext()
    {
        return next;
    }

    /**
     * @param next the next to set
     */
    public void setNext(PSMetadataRestEntry next)
    {
        this.next = next;
    }

}

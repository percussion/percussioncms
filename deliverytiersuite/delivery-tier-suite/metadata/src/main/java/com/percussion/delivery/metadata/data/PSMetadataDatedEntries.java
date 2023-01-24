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

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the structure of the object returned by the Rest method.
 * The object is composed of a list of {@link PSMetadataDatedEvent}.
 * 
 * @author rafaelsalis
 * 
 */
public class PSMetadataDatedEntries
{
    private List<PSMetadataDatedEvent> events;
    
    public PSMetadataDatedEntries()
    {
        events = new ArrayList<>();
    }
    
    /**
     * Add an event to the entries list.
     * 
     * @param event a {@link PSMetadataDatedEvent} object.
     */
    public void add(PSMetadataDatedEvent event)
    {
        if (event.getTitle() != null)
            events.add(event);
    }

    /**
     * @return the events, may be empty but never <code>null</code>.
     */
    public List<PSMetadataDatedEvent> getEvents()
    {
        return events;
    }

    /**
     * @param events the events to set.
     */
    public void setEvents(List<PSMetadataDatedEvent> events)
    {
        this.events = events;
    }
    
}

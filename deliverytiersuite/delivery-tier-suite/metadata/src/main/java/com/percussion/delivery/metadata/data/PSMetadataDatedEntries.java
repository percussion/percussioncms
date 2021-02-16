/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

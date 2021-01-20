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
package com.percussion.delivery.listeners;

import java.util.Set;

/**
 * @author erikserating
 *
 */
public interface IPSServiceDataChangeListener
{
    /**
     * Called when  data is changed as a result of an update or
     * delete and is committed to the repository. An insert will not fire this method.
     * @param site the site whose data was changed. Never blank.
     * @param services affected by the data change. Never <code>null</code> or
     * empty.
     */
    public void dataChanged(Set<String> site, String[] services);
    
    /**
     * Called when a data change is requested but before the data is
     * actually committed to the repository.
     */
    public void dataChangeRequested(Set<String> sites, String[] services);
}

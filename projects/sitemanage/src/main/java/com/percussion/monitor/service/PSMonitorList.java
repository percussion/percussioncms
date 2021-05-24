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

package com.percussion.monitor.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class PSMonitorList
{
	@XmlElement
	public List<PSMonitor> monitor = new ArrayList<>();
	
	public void addEntriesToList(Map<String, PSMonitor> monitors)
	{
	    monitor.addAll(monitors.values());
		Collections.sort(monitor, new Comparator<PSMonitor>()
        {

            @Override
            public int compare(PSMonitor o1, PSMonitor o2)
            {
                String name1 = o1.getStats().getEntries().get("name");
                String name2 = o2.getStats().getEntries().get("name");
                if (name1 == null) {
					return -1;
				}
                
                return name1.compareToIgnoreCase(name2);
            }           
        });
	}
	public PSMonitorList()
	{
	    
	}
	
}
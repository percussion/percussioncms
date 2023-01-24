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

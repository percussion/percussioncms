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
package com.percussion.dashboardmanagement.service.impl;

import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.dashboardmanagement.service.IPSGadgetService;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.util.PSSiteManageBean;

import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean("gadgetService")
public class PSGadgetService implements IPSGadgetService {

    public PSGadget load(String id) {
    	return new PSGadget();
    }

    public PSGadget save(PSGadget gadget)  {
    	return new PSGadget();//"New Gadget", "http://ewq.xml", "New Description");
    }

    public List<PSGadget> findAll(){
    	return createGadgetList(allGadgetUrls);
    }

    public PSGadget find(String id){
    	return new PSGadget();
    }

    public void delete(String id) {
		throw new UnsupportedOperationException("validate is not yet supported");
    }
 	
    public PSValidationErrors validate(PSGadget object) {
        throw new UnsupportedOperationException("validate is not yet supported");
    }

    // stub support methods and data
    private ArrayList<PSGadget> createGadgetList(String[] urlList) {
    	ArrayList<PSGadget> list = new ArrayList<>(urlList.length);
    	for(int i=0; i<urlList.length; i++) {
    		String url = urlList[i];
    		PSGadget gadget = new PSGadget();
    		gadget.setUrl(url);
    		list.add(gadget);
    	}
    	return list;
    }
    
    String[] allGadgetUrls = {
    	"http://annunziato.org/gadgets/inbox.xml",
		"http://www.google.com/ig/modules/horoscope.xml",
		"http://www.labpixies.com/campaigns/todo/todo.xml",
		"http://www.labpixies.com/campaigns/weather/weather.xml",
		"http://www.labpixies.com/campaigns/calendar/calendar.xml",
		"http://www.labpixies.com/campaigns/wiki/wiki.xml",
		"http://localhost:9982/shindig/gadgets/hello_world.xml"
	};

    String[] alexGadgetUrls = {
    		"http://www.google.com/ig/modules/horoscope.xml",
    		"http://www.labpixies.com/campaigns/todo/todo.xml",
    		"http://www.labpixies.com/campaigns/weather/weather.xml",
   	};

    String[] bobGadgetUrls = {
    		"http://www.labpixies.com/campaigns/weather/weather.xml",
    		"http://www.labpixies.com/campaigns/calendar/calendar.xml",
    		"http://www.labpixies.com/campaigns/wiki/wiki.xml",
   	};
    ArrayList<PSGadget> alexGadgets = new ArrayList<>();
    ArrayList<PSGadget> bobGadgets = new ArrayList<>();
}

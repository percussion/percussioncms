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
package com.percussion.dashboardmanagement.service.impl;

import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.dashboardmanagement.service.IPSGadgetUserService;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.util.PSSiteManageBean;

import java.util.ArrayList;
import java.util.List;


@PSSiteManageBean("gadgetUserService")
public class PSGadgetUserService implements IPSGadgetUserService {


    public PSGadget load(String id)throws PSGadgetNotFoundException, PSGadgetServiceException {
    	return new PSGadget();
    }
    

    public List<PSGadget> findAll(String username)throws PSGadgetNotFoundException, PSGadgetServiceException {
    	return this.createGadgetList(this.alexGadgetUrls);
    }
    

    public List<PSGadget> findAll()throws PSGadgetNotFoundException, PSGadgetServiceException {
    	return this.createGadgetList(this.alexGadgetUrls);
    }
    
    public PSGadget find(String username)throws PSGadgetNotFoundException, PSGadgetServiceException {
    	return new PSGadget();
    }
    

    public PSGadget save(String username, PSGadget gadget)throws PSGadgetNotFoundException, PSGadgetServiceException {
    	return new PSGadget();
    }
    
    public PSGadget save(PSGadget gadget)throws PSGadgetNotFoundException, PSGadgetServiceException {
    	return new PSGadget();
    }
    
    public void delete(String username, String id)throws PSGadgetNotFoundException, PSGadgetServiceException {
    	//TODO
    }

    public void delete(String id)throws PSGadgetNotFoundException, PSGadgetServiceException {
    	//TODO
    }
    
    public PSValidationErrors validate(PSGadget object) {
        // TODO Auto-generated method stub
        //return null;
        throw new UnsupportedOperationException("validate is not yet supported");
    }
    
    
//    @POST
//    @Path("/updatefor/{username}")
//    public void updateGadgetForUser(String username, PSGadget gadget)throws PSGadgetNotFoundException, PSGadgetServiceException {
//        throw new UnsupportedOperationException("update is not yet supported");
//    }
    
    // stub support methods and data
    private ArrayList<PSGadget> createGadgetList(String[] urlList) {
    	ArrayList<PSGadget> list = new ArrayList<>(urlList.length);
    	for(int i=0; i<urlList.length; i++) {
    		String url = urlList[i];
    		PSGadget gadget = new PSGadget();
    		String name = url.substring(url.lastIndexOf('/')+1, url.lastIndexOf('.'));
            String firstLetter = name.substring(0,1);  // Get first letter
            String remainder   = name.substring(1);    // Get remainder of word.
            String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();
//     		gadget.setName(capitalized);
    		gadget.setUrl(url);
    		gadget.setCol(this.alexGadgetLayout[i][0]);
    		gadget.setRow(this.alexGadgetLayout[i][1]);
//    		gadget.setXPosition(this.alexGadgetLayout[i][0]);
//    		gadget.setYPosition(this.alexGadgetLayout[i][1]);
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
    int[][] alexGadgetLayout = {{0,0},{0,1},{1,0}};

    String[] bobGadgetUrls = {
    		"http://www.labpixies.com/campaigns/weather/weather.xml",
    		"http://www.labpixies.com/campaigns/calendar/calendar.xml",
    		"http://www.labpixies.com/campaigns/wiki/wiki.xml",
   	};
    int[][] bobGadgetLayout = {{0,0},{1,0},{1,1}};

    ArrayList<PSGadget> alexGadgets = new ArrayList<>();
    ArrayList<PSGadget> bobGadgets = new ArrayList<>();
}
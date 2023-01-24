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

package com.percussion.soln.p13n.tracking.data;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;


public class VisitorProfileDataSetup implements InitializingBean {
    
    IVisitorProfileDataService visitorProfileDataService;

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(VisitorProfileDataSetup.class);
    
    public void afterPropertiesSet() throws Exception {
        setupData();
    }
    
    public void setupData() throws Exception {

        log.info("Setting Up Data");
        
    	VisitorProfile vp1 = visitorProfileDataService.createProfile();
    	vp1.setLabel("NA - Travel Visitor");
    	vp1.setUserId("user1");
    	HashMap<String,Integer> vp1Seg = new HashMap<String,Integer>();
    	// NA
    	vp1Seg.put("1103", 1);
    	// Travel & Tourism
    	vp1Seg.put("1136", 1);
    	vp1.setSegmentWeights(vp1Seg);
        visitorProfileDataService.save(vp1);

    	VisitorProfile vp2 = visitorProfileDataService.createProfile();
    	vp2.setLabel("UK - Finance Visitor");
    	vp2.setUserId("User2");
    	HashMap<String,Integer> vp2Seg = new HashMap<String,Integer>();
    	// UK
    	vp2Seg.put("1118", 1);
    	//  Finance
    	vp2Seg.put("1141", 1);
    	vp2.setSegmentWeights(vp2Seg);
        visitorProfileDataService.save(vp2);

        VisitorProfile vp3 = visitorProfileDataService.createProfile();
        vp3.setLabel("Global - Higher Education");
        vp3.setUserId("User2");
        HashMap<String,Integer> vp3Seg = new HashMap<String,Integer>();
        // Higher Ed.
        vp3Seg.put("2189", 1);
        vp3.setSegmentWeights(vp3Seg);
        visitorProfileDataService.save(vp3);
    }

    public IVisitorProfileDataService getVisitorProfileDataService() {
    	return visitorProfileDataService;
    }
    public void setVisitorProfileDataService(
    		IVisitorProfileDataService visitorProfileDataService) {
    	this.visitorProfileDataService = visitorProfileDataService;
    }

}

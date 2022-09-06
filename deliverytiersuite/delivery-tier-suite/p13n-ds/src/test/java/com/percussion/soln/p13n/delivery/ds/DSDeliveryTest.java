/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.soln.p13n.delivery.ds;

import static integrationtest.spring.SpringSetup.*;
import static org.junit.Assert.*;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.Controller;

import com.percussion.soln.p13n.delivery.DeliveryResponse;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;

public class DSDeliveryTest  {
    
    private static Controller deliveryController;

    private String listItemIdParam = "listItemId";
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(DSDeliveryTest.class);
    
    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @BeforeClass
    public static void setupSpring() throws Exception {
        loadXmlBeanFiles("file:ds/webapp/WEB-INF/applicationContext.xml",
                "file:ds/webapp/WEB-INF/delivery-servlet.xml",
                "file:ds/webapp/WEB-INF/spring/ds/*.xml",
                "classpath:integrationtest/p13n/ds/test-beans.xml");
        deliveryController = getBean("deliveryController", Controller.class);
        //See DSDataSetup for the data we preload using spring.
//        dataSetup = getBean("dataSetup",DSDataSetup.class);
//        dataSetup.setupData();
    }
    
    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        
        request.setParameter(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_REQUEST_PARAM, "1");
        response = new MockHttpServletResponse();
    }
    
    @Test
    public void shouldRunLocalRule() throws Exception {
        request.setParameter(listItemIdParam, "10");
        deliveryController.handleRequest(request, response);
        DeliveryResponse dr = getDeliveryResponse(response);
        log.info(response.getContentAsString());
        assertEquals("Delivery response should be ok:", "OK", dr.getStatus());
    }
    
    @Test
    public void shouldRunSpringRule() throws Exception {
        request.setParameter(listItemIdParam, "20");
        deliveryController.handleRequest(request, response);
        DeliveryResponse dr = getDeliveryResponse(response);
        log.info(response.getContentAsString());
        assertEquals("Delivery response should be ok:", "OK", dr.getStatus());
    }
    
    
    @Test
    public void shouldFailWhenNoRuleItemFound() throws Exception {
        log.info("shouldFailWhenNoRuleItemFound");
        request.setParameter(listItemIdParam, "9999");
        deliveryController.handleRequest(request, response);
        log.info(response.getContentAsString());
        DeliveryResponse dr = getDeliveryResponse(response);
        assertEquals("Delivery response should be an error", "ERROR", dr.getStatus());
    }
    
    @Test
    public void shouldFailWhenNoVisitorProfileFoundForGivenId() throws Exception {
        log.info("shouldFailWhenNoVisitorProfileFoundForGivenId");
        request.setParameter(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_REQUEST_PARAM, "9999");
        request.setParameter(listItemIdParam, "20");
        deliveryController.handleRequest(request, response);
        DeliveryResponse dr = getDeliveryResponse(response);
        log.info(response.getContentAsString());
        //Long id = VisitorTrackingWebUtils.getVisitorProfileIdFromCookie(request);
        //log.info("New Visitor Profile id: " + id);
        //assertTrue("Visitor Profile should have a new id", id != 9999L);
        assertEquals("Delivery response should be an error", "ERROR", dr.getStatus());
        
    }
    
    @Test
    public void shouldCreateANewVisitorProfile() throws Exception {
        request.removeParameter(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_REQUEST_PARAM);
        request.setParameter(listItemIdParam, "20");
        deliveryController.handleRequest(request, response);
        DeliveryResponse dr = getDeliveryResponse(response);
        assertEquals("Delivery response should not be an error", "OK", dr.getStatus());
        log.info(response.getContentAsString());
    }
    
    public DeliveryResponse getDeliveryResponse(MockHttpServletResponse response) throws Exception {
        JSONObject obj = JSONObject.fromObject(response.getContentAsString());
        DeliveryResponse dr = (DeliveryResponse) JSONObject.toBean(obj, DeliveryResponse.class);
        return dr;
    }
    

}

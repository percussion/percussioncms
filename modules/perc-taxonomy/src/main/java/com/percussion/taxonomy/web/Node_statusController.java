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

package com.percussion.taxonomy.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.taxonomy.TaxonomySecurityHelper;
import com.percussion.taxonomy.domain.Node_status;
import com.percussion.taxonomy.service.Node_statusService;

import java.util.Map;
import java.util.HashMap;

@Controller
public class Node_statusController {

    protected final Logger logger = LogManager.getLogger(getClass());
    private Node_statusService node_statusService;

    public Node_statusController() {
        //TODO: Fix me
       /* setCommandClass(Node_status.class);
        setCommandName("node_status");
        */
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        //--------------------------- Templated - Modify or replace -----------------------------
    	TaxonomySecurityHelper.raise_error_if_cannot_admin();
    	Map<String, Object> myModel = new HashMap<String, Object>();
        myModel.put("model", new Object());
        return new ModelAndView("node_status", "model", myModel);
        //------------------------------------- End Template -----------------------------------------
    }

    public void setNode_statusService(Node_statusService node_statusService) {
        this.node_statusService = node_statusService;
    }
}

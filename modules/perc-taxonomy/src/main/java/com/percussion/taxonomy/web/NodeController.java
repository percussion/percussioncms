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
import org.springframework.validation.BindException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.percussion.taxonomy.TaxonomySecurityHelper;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.service.NodeService;

import java.util.Collection;


import java.util.Map;
import java.util.HashMap;

@Controller
public class NodeController {

    protected final Logger logger = LogManager.getLogger(getClass());
    private NodeService nodeService;

    public NodeController() {
        //TODO: Fix me
       /* setCommandClass(Node.class);
        setCommandName("node");
        */
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
 
    	TaxonomySecurityHelper.raise_error_if_cannot_admin();
    	Collection all = nodeService.getAllNodes(1,1);
        Map<String, Object> myModel = new HashMap<String, Object>();
        myModel.put("all", all);
        myModel.put("node", nodeService.getNode(1,1));
        return new ModelAndView("node", "model", myModel);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}

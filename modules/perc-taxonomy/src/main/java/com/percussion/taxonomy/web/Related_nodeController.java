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
import com.percussion.taxonomy.domain.Related_node;
import com.percussion.taxonomy.service.Related_nodeService;

import java.util.Collection;

import java.util.Map;
import java.util.HashMap;

@Controller
public class Related_nodeController{

    protected final Logger logger = LogManager.getLogger(getClass());
    private Related_nodeService related_nodeService;

    public Related_nodeController() {
        //TODO: Fix me
    /*    setCommandClass(Related_node.class);
        setCommandName("related_node");

     */
    }


    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        //--------------------------- Templated - Modify or replace -----------------------------
    	TaxonomySecurityHelper.raise_error_if_cannot_admin();
    	Collection all = related_nodeService.getAllRelated_nodes();
        Map<String, Object> myModel = new HashMap<String, Object>();
        myModel.put("all", all);
        return new ModelAndView("related_node", "model", myModel);
        //------------------------------------- End Template -----------------------------------------
    }

    public void setRelated_nodeService(Related_nodeService related_nodeService) {
        this.related_nodeService = related_nodeService;
    }
}

/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.taxonomy.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Collection;

import java.util.Map;
import java.util.HashMap;

import com.percussion.taxonomy.TaxonomySecurityHelper;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.service.ValueService;

@Controller
public class ValueController {

    protected final Logger logger = LogManager.getLogger(getClass());
    private ValueService valueService;

    public ValueController() {
        //TODO: Fix me
      /*  setCommandClass(Value.class);
        setCommandName("value");
       */
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        //--------------------------- Templated - Modify or replace -----------------------------
    	TaxonomySecurityHelper.raise_error_if_cannot_admin();
    	Collection all = valueService.getAllValues();
        Map<String, Object> myModel = new HashMap<String, Object>();
        myModel.put("all", all);
        return new ModelAndView("value", "model", myModel);
        //------------------------------------- End Template -----------------------------------------
    }

    public void setValueService(ValueService valueService) {
        this.valueService = valueService;
    }
}

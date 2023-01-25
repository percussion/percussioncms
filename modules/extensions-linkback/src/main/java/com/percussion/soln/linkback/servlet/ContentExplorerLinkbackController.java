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

package com.percussion.soln.linkback.servlet;

import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Linkback controller to redirect to Rhythmyx Action Panel. The redirect path
 * is internal (hard coded), so there is no need to specify
 * <code>redirectPath</code> in bean configuration. Recommend to set
 * <code>helpViewName</code>.
 */
public class ContentExplorerLinkbackController extends GenericLinkbackController {

    @SuppressWarnings("unused")
    private static final Logger log = LogManager.getLogger(ContentExplorerLinkbackController.class);

    private static final String REDIRECT_PATH = "/sys_cx/mainpage.html";

    public ContentExplorerLinkbackController() {
        super();
        setRedirectPath(REDIRECT_PATH);
        setRequiredParameterNames(Arrays.<String> asList(new String[] { IPSHtmlParameters.SYS_CONTENTID }));
    }

}

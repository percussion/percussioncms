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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

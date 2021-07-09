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
package com.percussion.share.web.service;

public class PSRestServicePathConstants
{
    public final static String ID_PATH_PARAM = "id";

    public final static String JOB_ID_PARAM = "jobId";

    private final static String BASE_PATH = "/";

    private final static String ID_PATH = BASE_PATH + "{" + ID_PATH_PARAM + "}";

    public final static String DELETE_PATH = ID_PATH;

    public final static String FIND_PATH = BASE_PATH + "summary/{" + ID_PATH_PARAM + "}";

    public final static String FIND_ALL_PATH = BASE_PATH;

    public final static String LOAD_PATH = ID_PATH;

    public final static String RENDER_PREVIEW_LINK = BASE_PATH + "preview" + ID_PATH;

    public final static String SAVE_PATH = BASE_PATH;

    public final static String IMPORT_SITE_FROM_URL_PATH = BASE_PATH + "importFromUrl";

    public final static String IMPORT_SITE_FROM_URL_PATH_ASYNC = BASE_PATH + "importFromUrlAsync";

    public final static String VALIDATE_PATH = BASE_PATH + "validate";

    public final static String GET_IMPORTED_SITE_PATH = BASE_PATH + "getImportedSite/{" + JOB_ID_PARAM + "}";
}

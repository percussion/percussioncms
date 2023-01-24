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

/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.util;

public interface Constants
{

    public static final String NAMESPACE = "namespace";

    public static final String API_AUDIT_MAP = "api_audit_map";

    public static final String API_AUDIT_MAP_CONF = "api_audit_map.conf";

    public static String DEFAULT_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS z";

    public static final String MIGRATE_ACTION = "migrate";

    public static final String RECALL_ACTION = "recall";

    public static final String INITIATOR_TYPE_URI = "initiator_type_uri";

    public static final String TARGET_TYPE_URI = "target_type_uri";

    public static final String OBSERVER_TYPE_URI = "observer_type_uri";

    public static String CSV_AUDIT_FILES_NAME = "audit_events.csv";

    public static String JSON_AUDIT_FILES_NAME = "audit_events.json";

    public static String CONFIT_ACTOR_ID = "101";

    public static String MANAGEMENT_ACTOR_ID = "102";

    public static String MANAGEMENT_ACTIVITY_ID = "103";

    public static String CSV_SEPERATOR = ",";

    public static String AUDIT_FORMAT_TYPE_CSV = "CSV";

    public static String AUDIT_FORMAT_TYPE_JSON = "Json";

    public static String INITIATOR = "initiator";

    public static String TARGET = "target";

}

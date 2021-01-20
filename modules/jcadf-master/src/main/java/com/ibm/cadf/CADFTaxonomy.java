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
package com.ibm.cadf;

import static java.util.Arrays.asList;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CADFTaxonomy
{

    public static String UNKNOWN = "unknown";

    // Commonly used (valid) Event.action values from Nova
    public static String ACTION_CREATE = "create";

    public static String ACTION_READ = "read";

    public static String ACTION_UPDATE = "update";

    public static String ACTION_DELETE = "delete";

    public static String ACTION_BACKUP = "backup";

    public static String ACTION_RESTORE = "restore";

    // OpenStack specific, Profile or change CADF spec. to add this action
    public static String ACTION_LIST = "read/list";

    List<String> ACTION_TAXONOMY = asList("backup", "capture", ACTION_CREATE, "configure", ACTION_READ,
                                          ACTION_LIST, ACTION_UPDATE, ACTION_DELETE, "monitor", "start", "stop",
                                          "deploy",
                                          "undeploy", "enable", "disable", "send", "receive", "authenticate",
                                          "authenticate/login",
                                          "revoke", "renew", "restore", "evaluate", "allow", "deny", "notify", UNKNOWN);

    public boolean isValidAction(String value)
    {
        return findElementStartsWith(ACTION_TAXONOMY, value);
    }

    // Valid Event.outcome values
    public enum OUTCOME
    {

        SUCCESS("success"),
        FAILURE("failure"),
        PENDING("pending"),
        UNKNOWN("unknown");

        public String value;

        private OUTCOME(String value)
        {
            this.value = value;
        }
    }

    public static String OUTCOME_SUCCESS = "success";

    public static String OUTCOME_FAILURE = "failure";

    public static String OUTCOME_PENDING = "pending";

    List<String> OUTCOME_TAXONOMY = asList(OUTCOME_SUCCESS, OUTCOME_FAILURE, OUTCOME_PENDING, UNKNOWN);

    public boolean isValidOutcome(String value)
    {
        return findElementStartsWith(OUTCOME_TAXONOMY, value);
    }

    public static String SERVICE_SECURITY = "service/security";

    public static String ACCOUNT_USER = "service/security/account/user";

    public static String CADF_AUDIT_FILTER = "service/security/audit/filter";

    List<String> RESOURCE_TAXONOMY = asList("storage", "storage/node",
                                            "storage/volume", "storage/memory", "storage/container",
                                            "storage/directory",
                                            "storage/database", "storage/queue", "compute", "compute/node",
                                            "compute/cpu", "compute/machine", "compute/process", "compute/thread",
                                            "network", "network/node", "network/node/host", "network/connection",
                                            "network/domain",
                                            "network/cluster", "service", "service/oss", "service/bss",
                                            "service/bss/metering",
                                            "service/composition", "service/compute", "service/database",
                                            SERVICE_SECURITY,
                                            "service/security/account", ACCOUNT_USER, CADF_AUDIT_FILTER,
                                            "service/storage",
                                            "service/storage/block", "service/storage/image", "service/storage/object",
                                            "service/network", "data", "data/message", "data/workload",
                                            "data/workload/app", "data/workload/service", "data/workload/task",
                                            "data/workload/job", "data/file", "data/file/catalog", "data/file/log",
                                            "data/template",
                                            "data/package", "data/image", "data/module", "data/config",
                                            "data/directory", "data/database",
                                            "data/security", "data/security/account", "data/security/credential",
                                            "data/security/group", "data/security/identity", "data/security/key",
                                            "data/security/license", "data/security/policy", "data/security/profile",
                                            "data/security/role", "data/security/service",
                                            "data/security/account/user",
                                            "data/security/account/user/privilege", "data/database/alias",
                                            "data/database/catalog", "data/database/constraints",
                                            "data/database/index", "data/database/instance", "data/database/key",
                                            "data/database/routine", "data/database/schema", "data/database/sequence",
                                            "data/database/table", "data/database/trigger", "data/database/view",
                                            UNKNOWN);

    public boolean isValidResource(String value)
    {
        return findElementStartsWith(RESOURCE_TAXONOMY, value);
    }

    public boolean findElementStartsWith(List<String> list, final String value)
    {
        boolean startsWithValue = Iterables.any(list, new Predicate<String>()
        {
            public boolean apply(String input)
            {
                return input.startsWith(value);
            }
        });

        return startsWithValue;
    }
}

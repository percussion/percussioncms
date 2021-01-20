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

package com.ibm.cadf.auditlogger;

import com.ibm.cadf.auditlogger.csv.CSVAuditLogger;
import com.ibm.cadf.auditlogger.json.JsonAuditLogger;
import com.ibm.cadf.util.Constants;

public class AuditLoggerFactory
{

    /**
     * The default file format is csv
     * 
     * @param auditorType
     * @return
     */
    public static AuditLogger getAuditLogger(String auditorType)
    {

        if (auditorType.equals(Constants.AUDIT_FORMAT_TYPE_CSV))
        {
            return CSVAuditLogger.getInstance();
        }
        else if (auditorType.equals(Constants.AUDIT_FORMAT_TYPE_JSON))
        {
            return JsonAuditLogger.getInstance();
        }
        return CSVAuditLogger.getInstance();
    }

}

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

package com.percussion.auditlog;

import com.ibm.cadf.middleware.AuditContext;

public class AbstractEvent extends AuditContext {

    private final static String SYSTEM_OBSERVER="service/bss/cms";
    private String outcome;
    public String getOutcome(){
        return outcome;
    }

    public void setOutcome(String outcome){
        this.outcome = outcome;
    }

    public AbstractEvent(){

        //Set some defaults
        this.setOutcome(PSActionOutcome.UNKNOWN.name());
        this.setObserverName(SYSTEM_OBSERVER);
        this.setTargetName(SYSTEM_OBSERVER);
    }


}

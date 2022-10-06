/*
 * Copyright 1999-2022 Percussion Software, Inc.
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

package com.percussion.services.assembly.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.utils.jsr170.IPSPropertyInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSInlineLinkProcessor2 implements IPSPropertyInterceptor {

    private static final String PERC_BROKENLINK = "perc-brokenlink";
    private static final String PERC_NOTPUBLICLINK = "perc-notpubliclink";
    private static final PSLocationUtils locationUtils = new PSLocationUtils();
    private static final Logger log = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);

    private PSInlineLinkProcessor2(){}

    private static final PSInlineLinkProcessor2 instance = new PSInlineLinkProcessor2();

    public static IPSPropertyInterceptor getInstance() {
        return instance;
    }

    @Override
    public Object translate(Object originalValue) {
        return originalValue;
    }
}

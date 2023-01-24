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
package com.percussion.soln.linkback.assembly;

import com.percussion.extension.*;
import com.percussion.soln.linkback.codec.LinkbackTokenCodec;
import com.percussion.soln.linkback.codec.impl.StringLinkBackTokenImpl;
import com.percussion.soln.linkback.utils.LinkbackUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * JEXL function to encode assembly parameters into a linkback token.
 */
public class LinkbackJexlTools implements IPSJexlExpression {

    private static final Logger log = LogManager.getLogger(LinkbackJexlTools.class);

    private LinkbackTokenCodec linkbackCodec = null;

    @IPSJexlMethod(description = "encode a map of parameters into a linkback token.", 
            params = { @IPSJexlParam(name = "map", description = "map of <String, Object>") })
    public String encode(Map<String, Object> map) {
        return linkbackCodec.encode(map);
    }
    
    public String getLinkbackParamName() {
        return LinkbackUtils.LINKBACK_PARAM_NAME;
    }

    public LinkbackTokenCodec getLinkbackCodec() {
        return linkbackCodec;
    }

    public void setLinkbackCodec(LinkbackTokenCodec linkbackCodec) {
        this.linkbackCodec = linkbackCodec;
    }

    public void init(IPSExtensionDef arg0, File arg1) throws PSExtensionException {
        if (linkbackCodec == null) {
            log.debug("create a default codec");
            linkbackCodec = new StringLinkBackTokenImpl();
        }
    }
}

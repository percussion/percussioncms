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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.soln.linkback.assembly;

import com.percussion.extension.*;
import com.percussion.soln.linkback.codec.LinkbackTokenCodec;
import com.percussion.soln.linkback.codec.impl.StringLinkBackTokenImpl;
import com.percussion.soln.linkback.utils.LinkbackUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Map;

/**
 * JEXL function to encode assembly parameters into a linkback token.
 */
public class LinkbackJexlTools implements IPSJexlExpression {

    private static final Log log = LogFactory.getLog(LinkbackJexlTools.class);

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

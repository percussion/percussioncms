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
package com.percussion.pagemanagement.dao.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.pagemanagement.data.IPSHtmlMetadata;

import java.util.Map;

/**
 * Utilities for {@link IPSHtmlMetadata}.
 * 
 * @author adamgent
 * 
 */
public class PSHtmlMetadataUtils
{

    /**
     * Copies meta data to map of fields.
     * 
     * @param meta never <code>null</code>.
     * @param f never <code>null</code>.
     */
    public static void toMap(IPSHtmlMetadata meta, Map<String, Object> f)
    {
        notNull(meta);
        notNull(f);
        f.put("code_insert_after_body_start", meta.getAfterBodyStartContent());
        f.put("code_insert_before_body_close", meta.getBeforeBodyCloseContent());
        f.put("additional_head_content", meta.getAdditionalHeadContent());
        f.put("protected_region", meta.getProtectedRegion());
        f.put("protected_region_text", meta.getProtectedRegionText());
    }

    /**
     * Copies meta data to map of fields.
     * 
     * @param meta never <code>null</code>.
     * @param f never <code>null</code>.
     */
    public static void fromMap(IPSHtmlMetadata meta, Map<String, Object> f)
    {
        notNull(meta);
        notNull(f);
        String addHeadContent = (String) f.get("additional_head_content");
        String afterBodyStart = (String) f.get("code_insert_after_body_start");
        String beforeBodyClose = (String) f.get("code_insert_before_body_close");
        String protectedRegion = (String) f.get("protected_region");
        String protectedRegionText = (String) f.get("protected_region_text");
        meta.setAdditionalHeadContent(addHeadContent);
        meta.setAfterBodyStartContent(afterBodyStart);
        meta.setBeforeBodyCloseContent(beforeBodyClose);
        meta.setProtectedRegion(protectedRegion);
        meta.setProtectedRegionText(protectedRegionText);
    }

    /**
     * @param from never <code>null</code>.
     * @param to never <code>null</code>.
     */
    public static void copy(IPSHtmlMetadata from, IPSHtmlMetadata to)
    {
        notNull(from);
        notNull(to);
        to.setAdditionalHeadContent(from.getAdditionalHeadContent());
        to.setAfterBodyStartContent(from.getAfterBodyStartContent());
        to.setBeforeBodyCloseContent(from.getBeforeBodyCloseContent());
        to.setProtectedRegion(from.getProtectedRegion());
        to.setProtectedRegionText(from.getProtectedRegionText());
        to.setDocType(from.getDocType());
    }

}

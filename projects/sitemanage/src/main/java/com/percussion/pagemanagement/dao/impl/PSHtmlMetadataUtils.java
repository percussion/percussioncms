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

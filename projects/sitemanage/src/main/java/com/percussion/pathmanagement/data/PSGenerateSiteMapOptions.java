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
package com.percussion.pathmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.List;
import java.util.Objects;

/**
 * This class contains the permissions of a folder.
 *
 * @author yubingchen
 */
public class PSGenerateSiteMapOptions extends PSAbstractDataObject
{
    private String generateSitemapExcludeImage;
    private String generateSitemap;

    public String getGenerateSitemap() {
        return generateSitemap;
    }

    public void setGenerateSitemap(String generateSitemap) {
        this.generateSitemap = generateSitemap;
    }


    public String getGenerateSitemapExcludeImage() {
        return generateSitemapExcludeImage;
    }

    public void setGenerateSitemapExcludeImage(String generateSitemapExcludeImage) {
        this.generateSitemapExcludeImage = generateSitemapExcludeImage;
    }


}

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

package com.percussion.cms.handlers;

import org.junit.Assert;
import org.junit.Test;

public class PSProxyURLTest {

    public PSProxyURLTest(){}

    @Test
    public void testURL() throws Exception{

        String url = "http://macbook-pro.local:9992/Rhythmyx/psx_cepercRichTextAsset/percRichTextAsset.html?sys_revision=1&sys_contentid=10051&sys_command=edit&sys_pageid=0&sys_view=sys_HiddenFields%3Asys_title";

        String result = PSCommandHandler.fixProxiedUrl(url, "macbook-pro.local",8080 );
        System.out.println(result);
        Assert.assertTrue("http://macbook-pro.local:8080/Rhythmyx/psx_cepercRichTextAsset/percRichTextAsset.html?sys_revision=1&sys_contentid=10051&sys_command=edit&sys_pageid=0&sys_view=sys_HiddenFields%3Asys_title".equals(result));

    }
}

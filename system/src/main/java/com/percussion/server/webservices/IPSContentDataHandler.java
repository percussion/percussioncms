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

package com.percussion.server.webservices;

import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import org.w3c.dom.Element;

import java.util.List;

public interface IPSContentDataHandler extends IPSPortActionHandler {
    /**
     * The resource path used to purge content items.
     */
    String PURGE_PATH = "sys_cxSupport/purgecontent.html";

    static void purgeItems(PSRequest request, List<String> itemIds){}

    void newCopy(PSRequest request) throws PSException;

    PSServerItem updateItem(
            PSRequest request,
            Element item,
            PSLocator loc,
            long typeId)
            throws PSException;

    void processInsertItem(PSRequest request, String folderContenttype, Element toXml) throws PSException;
}

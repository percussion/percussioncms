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

package com.percussion.rest.displayformat;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Lazy
public class DisplayFormatTestAdaptor implements IDisplayFormatAdaptor{
    @Override
    public List<DisplayFormat> createDisplayFormats(List<String> names, String session, String user) {
        return null;
    }

    @Override
    public void deleteDisplayFormats(List<IPSGuid> ids, boolean ignoreDependencies, String session, String user) {

    }

    @Override
    public List<DisplayFormat> findAllDisplayFormats() throws PSCmsException, PSErrorResultsException, PSUnknownNodeTypeException {
        return null;
    }

    @Override
    public DisplayFormat findDisplayFormat(IPSGuid id) throws PSCmsException, PSUnknownNodeTypeException {
        return null;
    }

    @Override
    public DisplayFormat findDisplayFormat(String name) throws PSCmsException, PSUnknownNodeTypeException {
        return null;
    }

    @Override
    public void saveDisplayFormats(List<DisplayFormat> displayFormats, boolean release, String session, String user) {

    }
}

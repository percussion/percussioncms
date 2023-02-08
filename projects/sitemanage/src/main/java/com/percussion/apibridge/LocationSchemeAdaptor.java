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

package com.percussion.apibridge;

import com.percussion.rest.locationscheme.ILocationSchemeAdaptor;
import com.percussion.rest.locationscheme.LocationScheme;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.PSSiteManageBean;

@PSSiteManageBean
public class LocationSchemeAdaptor implements ILocationSchemeAdaptor {

    private IPSSiteManager siteManager;

    public LocationSchemeAdaptor(){
       siteManager =  PSSiteManagerLocator.getSiteManager();
    }


    @Override
    public LocationScheme createOrUpdateLocationScheme(LocationScheme scheme) {
        return null;
    }

    @Override
    public void deletedLocationScheme(String guid) {

    }
}

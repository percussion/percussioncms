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

import com.percussion.rest.errors.UnexpectedException;
import com.percussion.rest.preferences.IPreferenceAdaptor;
import com.percussion.rest.preferences.UserPreference;
import com.percussion.rest.preferences.UserPreferenceList;
import com.percussion.server.PSPersistentProperty;
import com.percussion.server.PSPersistentPropertyMeta;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.PSSiteManageBean;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.NotFoundException;
import java.util.List;

@PSSiteManageBean
public class PreferencesAdaptor implements IPreferenceAdaptor {

    private static final Logger log = LogManager.getLogger(PreferencesAdaptor.class);

    @Autowired
    IPSCmsObjectMgr objectMgr;

    @Override
    public UserPreferenceList getAllUserPreferences() {
        PSUserSession session = getSession();

        List<PSPersistentProperty> userPrefs =  objectMgr.findPersistentPropertiesByName(session.getRealAuthenticatedUserEntry());

        return ApiUtils.convertUserProperties(userPrefs);
    }

    @Override
    public UserPreferenceList saveAllUserPreferences(UserPreferenceList prefs) {
        PSUserSession session = getSession();

        //Update user preferences
        try {

                   for(UserPreference pref : prefs){
                       objectMgr.savePersistentPropertyMeta(ApiUtils.convertUserPreferenceToMeta(pref));
                       objectMgr.savePersistentProperty(ApiUtils.convertUserPreference(pref));
                   }

                   return this.getAllUserPreferences();
        }catch(Exception e){
            log.error("An error happened when updating user preferences.",e);
            throw new UnexpectedException();
        }
    }

    @Override
    public UserPreference loadPreference(String preference) {
        PSUserSession session = getSession();

        for(PSPersistentProperty p : objectMgr.findPersistentPropertiesByName(session.getRealAuthenticatedUserEntry())){
            if(p.getName().toLowerCase().equals(preference.toLowerCase())){
                return ApiUtils.convertUserProperty(p);
            }
        }
        //If we get this far we didn't find the preference.
        throw new NotFoundException();
    }

    @Override
    public UserPreference savePreference(UserPreference pref) {
        PSUserSession session = getSession();
        PSPersistentProperty p = ApiUtils.convertUserPreference(pref);
        PSPersistentPropertyMeta pm = ApiUtils.convertUserPreferenceToMeta(pref);

        objectMgr.savePersistentPropertyMeta(pm);
        objectMgr.savePersistentProperty(p);

        return ApiUtils.convertPSPersistentProperty(p);
    }

    @Override
    public void deletePreference(UserPreference pref) {
        PSUserSession session = getSession();

        if(session.getUserProperties().contains(pref)){
            objectMgr.deletePersistentProperty(ApiUtils.convertUserPreference(pref));
        }else{
            throw new NotFoundException();
        }
    }

    private PSUserSession getSession(){
        PSRequest req = PSSecurityFilter.getCurrentRequest();

        return req.getUserSession();
    }


}

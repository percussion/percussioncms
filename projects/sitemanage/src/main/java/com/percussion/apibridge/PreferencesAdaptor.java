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

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

package com.percussion.soln.p13n.tracking.location.ip2location;

import java.io.IOException;
import java.net.UnknownHostException;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import com.percussion.soln.p13n.tracking.VisitorLocation;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorRequest;
import com.percussion.soln.p13n.tracking.location.IVisitorLocationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IP2LocationService implements IVisitorLocationService {

    IP2Location ip2Location;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(IP2LocationService.class);
    
    public IP2LocationService() {
        ip2Location = new IP2Location();
    }
    
    public IP2LocationService(String databasePath, String licensePath) {
        this();
        setDatabasePath(databasePath);
        setLicensePath(licensePath);
    }
    
    
    public VisitorLocation findLocation(VisitorRequest request, VisitorProfile profile) {
        validateSetup();
        VisitorLocation location = null;
        String errorMessage = "Error in location service: ";
        String address = request.getAddress();
        try {
            IPResult result = ip2Location.IPQuery(address);
            location = new VisitorLocation();
            location.setLatitude(result.getLatitude());
            location.setLongitude(result.getLongitude());
            location.setCity(result.getCity());
            location.setCountryLong(result.getCountryLong());
            location.setCountryShort(result.getCountryShort());
            location.setDomainName(result.getDomain());
            location.setISP(result.getISP());
            location.setRegion(result.getRegion());
            location.setZipCode(result.getZipCode());
            location.setNetSpeed(result.getNetSpeed());
        } catch (UnknownHostException e) {
            log.error(errorMessage, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error(errorMessage, e);
        }
        return location;
    }
    
    private void validateSetup() {
        if (getDatabasePath() == null) throw new IllegalStateException("IP2Location needs a database path.");
        if (getLicensePath() == null) log.trace("IP2Location license is not set. In evaluation mode." );
    }
    public String getDatabasePath() {
        return ip2Location.IPDatabasePath;
    }
    public String getLicensePath() {
        return ip2Location.IPLicensePath;
    }
    public void setDatabasePath(String path) {
        ip2Location.IPDatabasePath = path;
    }
    
    public void setLicensePath(String path) {
        ip2Location.IPLicensePath = path;
    }

}

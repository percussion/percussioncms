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

package com.percussion;

import javax.net.ssl.HttpsURLConnection;

public class SecureHeaderChecker {

    private static final String[] secureHeaders = {
            "X-Frame-Options",
            "Content-Security-Policy",
            "X-Content-Type-Options",
            "Strict-Transport-Security",
            "X-XSS-Protection",
            "Cache-Control",
             "Referrer-Policy"
    };


    /**
     * Checks the connection for the presence of secure headers and
     * @param conn
     * @return
     */
    public static SecureHeaderCheckResponse check(HttpsURLConnection conn) {

        SecureHeaderCheckResponse response = new SecureHeaderCheckResponse();

        for(String h : secureHeaders){
            String result = conn.getHeaderField(h);
            if(result == null){
                response.getChecks().put(h,false);
                response.setFailedCheck(true);
            }else{
                response.getChecks().put(h,true);
            }
        }
        return response;
    }


}

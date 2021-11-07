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

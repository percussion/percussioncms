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

/**
 * 
 */
package com.percussion.delivery.forms.impl;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Service for checking recaptcha on form. 
 * 
 * @author natechadwick
 *
 */
public class PSRecaptchaService {

    	private final static Logger log = LogManager.getLogger(PSRecaptchaService.class);

		public final static String RECAPTCHA_RESPONSE = "g-recaptcha-response";
		//Possible errors 
		public final static String RECAPTCHA_ERR_MISSING_SECRET = "missing-input-secret";//	The secret parameter is missing.
		public final static String RECAPTCHA_ERR_INVALID_SECRET="invalid-input-secret"; //	The secret parameter is invalid or malformed.
		public final static String RECAPTCHA_ERR_MISSING_INPUT= "missing-input-response";	//The response parameter is missing.
		public final static String RECAPTCHA_ERR_INVALID_INPUT = "invalid-input-response"; //	The response parameter is invalid or malformed.
		public final static String RECAPTCHA_ERR_BAD_REQUEST = "bad-request";	//The request is invalid or malformed.
		public final static String RECAPTCHA_ERR_TIMEOUT = "timeout-or-duplicate"; //The response is no longer valid: either is too old or has been used previously.
		
		private String url = "https://www.google.com/recaptcha/api/siteverify";
		private String secret;
		private String userAgent = "Mozilla/5.0";
		private boolean captchaOn = false;
		
		public PSRecaptchaService(boolean captchaOn, String captchaUrl, String secret, String userAgent){
			this.captchaOn = captchaOn;
			this.url = captchaUrl;
			this.secret = secret;
			this.userAgent = userAgent;
		}
		
		
		
		public String getUrl() {
			return url;
		}



		public void setUrl(String url) {
			this.url = url;
		}



		public String getSecret() {
			return secret;
		}



		public void setSecret(String secret) {
			this.secret = secret;
		}



		public String getUserAgent() {
			return userAgent;
		}



		public void setUserAgent(String userAgent) {
			this.userAgent = userAgent;
		}



		public boolean isCaptchaOn() {
			return captchaOn;
		}



		public void setCaptchaOn(boolean captchaOn) {
			this.captchaOn = captchaOn;
		}



		public boolean verify(String gRecaptchaResponse) throws IOException {
			if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
				return false;
			}
			
			try{
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			// add requqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", userAgent);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String postParams = "secret=" + secret + "&response="
					+ gRecaptchaResponse;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			log.debug("reCaptcha: \nSending 'POST' request to URL : {}", url);
			log.debug("reCaptcha: Post parameters : {}", postParams);
			log.debug("reCaptcha: Response Code : {}", responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			log.debug("{}",response);
			
			//parse JSON response and return 'success' value
			 JSONObject json = new JSONObject(response.toString());
			
			boolean ret = json.getBoolean("success");
			
			if(log.isDebugEnabled()){
				if(ret==true){
					log.debug("reCaptcha: Successful validation.  This is not a robot! Yay humans!");
				}else{
					log.debug("reCaptcha: Validation failed.  Bad robot!");
				}
			}
			
			return ret;
		
			}catch(Exception e){
				log.error("An error occurred validating reCaptcha.  Failing validation.");
				log.debug("reCaptcha: Validation failed with exception", e);
				return false;
			}
		}
}


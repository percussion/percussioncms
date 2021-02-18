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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.analytics.service.impl.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.PemReader.Section;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;
import com.percussion.utils.date.PSDateRange;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author erikserating
 *
 */
public class PSGoogleAnalyticsProviderHelper
{

    private static PSGoogleAnalyticsProviderHelper INSTANCE;

    public static PSGoogleAnalyticsProviderHelper getInstance(){
        synchronized(PSGoogleAnalyticsProviderHelper.class) {
            if (INSTANCE == null) {
                INSTANCE = new PSGoogleAnalyticsProviderHelper();
            }
        }
        return INSTANCE;
    }

    private PSGoogleAnalyticsProviderHelper(){ }

    /**
     * Helper method to retrieve an <code>AnalyticsService</code> object for communication
     * to the Google Analytics services. This will check credentials and throw an exception
     * if the creds are not valid.
     * NOTE: THIS IS OAuth2, after Google changed its access in its APIs, this code was written to adapt to their
     * new changes.
     * @param email the Service Email Account for access to the provider. Cannot be <code>null</code> or empty.
     * @param key the name of the keyfile the client downloads for access to the provider. Cannot be <code>null</code> or empty.
     * @return the analytics service object, never <code>null</code>.
     * @throws PSAnalyticsProviderException if an error occurs when getting the service.

     */
    public Analytics getAnalyticsService(String email, String key) throws PSAnalyticsProviderException
    {
        JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        com.google.api.services.analytics.Analytics service = null;
        // Construct a GoogleCredential object with the service account email
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            ObjectMapper mapper = new ObjectMapper();
            GoogleCreds creds = mapper.readValue(key, GoogleCreds.class);
            if (!StringUtils.equals(creds.getClient_email(), email)) {
                throw new PSAnalyticsProviderException("Email does not match key file", CAUSETYPE.INVALID_CREDS);
            }
            PrivateKey serviceAccountPrivateKey = privateKeyFromPkcs8(creds.getPrivate_key());
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId(email)
                    .setServiceAccountPrivateKeyId(creds.getPrivate_key_id())
                    .setServiceAccountPrivateKey(serviceAccountPrivateKey)
                    .setServiceAccountScopes(AnalyticsScopes.all())
                    .build();
            service = new com.google.api.services.analytics.Analytics.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                    APPLICATION_NAME).setHttpRequestInitializer(credential).build();

        } catch (PSAnalyticsProviderException e) {
            throw e;

        }
        catch (GeneralSecurityException e) {
            log.error("Google Auth error: {}",e.getMessage());
            throw new PSAnalyticsProviderException(e.getMessage(), CAUSETYPE.AUTHENTICATION_ERROR);

        }
        catch (IOException e)
        {
            log.error("Google Auth error:",e);
            throw new PSAnalyticsProviderException(e, CAUSETYPE.INVALID_CREDS);
        }
        return service;
    }

    /**
     *
     * @param email
     * @param key
     * @return
     */
    public  AnalyticsReporting initializeAnalyticsReporting(String email, String key) throws PSAnalyticsProviderException {
        // Construct a GoogleCredential object with the service account email
        JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        AnalyticsReporting analyticsReporting = null;
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            ObjectMapper mapper = new ObjectMapper();
            GoogleCreds creds = mapper.readValue(key, GoogleCreds.class);
            if (!StringUtils.equals(creds.getClient_email(), email)) {
                throw new PSAnalyticsProviderException("Email does not match key file", CAUSETYPE.INVALID_CREDS);
            }
            PrivateKey serviceAccountPrivateKey = privateKeyFromPkcs8(creds.getPrivate_key());
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId(email)
                    .setServiceAccountPrivateKeyId(creds.getPrivate_key_id())
                    .setServiceAccountPrivateKey(serviceAccountPrivateKey)
                    .setServiceAccountScopes(AnalyticsScopes.all())
                    .build();
            // Construct the Analytics Reporting service object.
            analyticsReporting = new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
        } catch (PSAnalyticsProviderException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            log.error("Google Auth error:",e);
            throw new PSAnalyticsProviderException(e.getMessage(), CAUSETYPE.AUTHENTICATION_ERROR);
        } catch (IOException e) {
                log.error("Google Auth error:",e);
                throw new PSAnalyticsProviderException(e, CAUSETYPE.INVALID_CREDS);
        }
            return analyticsReporting;
    }
   /**
    * Helper method to create a new Google analytics <code>DataQuery</code> object.
    * The start and end dates will be set to the passed in date range values.
    * @param range the date range, cannot be <code>null</code>.
    * @return the ReportRequest  object, never <code>null</code>.
    */
    public  ReportRequest createNewDataQuery(PSDateRange range)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        DateRange dateRange = new DateRange();
        dateRange.setStartDate(formatter.format(range.getStart()));
        dateRange.setEndDate(formatter.format(range.getEnd()));

        return new ReportRequest()
        .setDateRanges(Arrays.asList(dateRange));
    }
   /**
    * Helper method to parse a google date string into a <code>java.util.Date</code> object.
    * @param googleDate the date string, cannot be <code>null</code> or empty.
    * @return the date object, never <code>null</code>
    * @throws PSAnalyticsProviderException if a date parse error occurs.
    */
   public Date parseDate(String googleDate) throws PSAnalyticsProviderException
   {
      if(StringUtils.isBlank(googleDate))
         throw new IllegalArgumentException("googleDate cannot be null or empty.");
      try
      {
    	  Date ret = null;
    	  synchronized(PSGoogleAnalyticsProviderHelper.class){
    		  ret = DATE_FORMAT.parse(googleDate);
    		  }
         return ret;
      }
      catch (ParseException e)
      {
         throw new PSAnalyticsProviderException(
                  "Invalid date returned by provider.",
                  CAUSETYPE.INVALID_DATA);
      }
   }

   /*
    * Checks if the start date of the range is not before google analytics launch date.
    * if that is the case set the start date to analytics launch date
    */
    public PSDateRange createValidPSDateRange(PSDateRange range) throws PSAnalyticsProviderException
    {

        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

        try
        {
            Date analyticsLaunchDate = formatter.parse(ANALYTICS_LAUNCH_DATE);

            if (analyticsLaunchDate.compareTo(range.getStart()) > 0)
            {
                range = new PSDateRange(analyticsLaunchDate, range.getEnd(), range.getGranularity());
            }
        }
        catch (ParseException e)
        {
            throw new PSAnalyticsProviderException("Error occurred while parsing the analytics launch date.",
                    CAUSETYPE.INVALID_DATA);
        }
        return range;
    }
   /**
    * Date format to use to parse date from a google query. Never <code>null</code>.
    */
   private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

   public static final String  ANALYTICS_LAUNCH_DATE = "11/14/2005";

    public static final String APPLICATION_NAME = "Percussion CMS";

    public synchronized DateFormat getDateFormat(){
        return DATE_FORMAT;
    }
   /**
    * Mappings of Google exceptions to are own cause enums.
    */
   public static final Map<String, PSAnalyticsProviderException.CAUSETYPE> CAUSE_MAPPINGS =
      new HashMap<>();

   /**
    * Helper to convert from a PKCS#8 String to an RSA private key
    */
   static PrivateKey privateKeyFromPkcs8(String privateKeyPkcs8) throws IOException, PSAnalyticsProviderException {
     Reader reader = new StringReader(privateKeyPkcs8);
     Section section = PemReader.readFirstSectionAndClose(reader, "PRIVATE KEY");
     if (section == null) {
       throw new IOException("Invalid PKCS#8 data.");
     }
     byte[] bytes = section.getBase64DecodedBytes();
     PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
     Exception unexpectedException = null;
     try {
       KeyFactory keyFactory = SecurityUtils.getRsaKeyFactory();
       return keyFactory.generatePrivate(keySpec);

     } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
       unexpectedException = exception;
     }
       throw new PSAnalyticsProviderException(unexpectedException.getMessage(), CAUSETYPE.AUTHENTICATION_ERROR);
   }

   static
   {
      CAUSE_MAPPINGS.put("AccountDeletedException", CAUSETYPE.ACCOUNT_DELETED);
      CAUSE_MAPPINGS.put("AccountDisabledException", CAUSETYPE.ACCOUNT_DISABLED);
      CAUSE_MAPPINGS.put("CaptchaRequiredException", CAUSETYPE.INVALID_CREDS);
      CAUSE_MAPPINGS.put("InvalidCredentialsException", CAUSETYPE.INVALID_CREDS);
      CAUSE_MAPPINGS.put("NotVerifiedException", CAUSETYPE.NOT_VERIFIED);
      CAUSE_MAPPINGS.put("ServiceUnavailableException", CAUSETYPE.SERVICE_UNAVAILABLE);
      CAUSE_MAPPINGS.put("SessionExpiredException", CAUSETYPE.SESSION_EXPIRED);
      CAUSE_MAPPINGS.put("TermsNotAgreedException", CAUSETYPE.TERMS_NOT_AGREED);
   }

   private static Logger log = LogManager.getLogger(PSGoogleAnalyticsProviderHelper.class);

}

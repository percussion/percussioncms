package com.intsof.support.migrations.zohodesk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.zoho.desk.logger.ZDLogger;
import com.zoho.oauth.client.ZohoOAuthClient;
import com.zoho.oauth.client.ZohoOAuthTokens;
import com.zoho.oauth.client.ZohoPersistenceHandler;
import com.zoho.oauth.common.ZohoOAuthException;

public class ZohoOAuthFilePersistence implements ZohoPersistenceHandler{

    private static final String USER_IDENTIFIER = "useridentifier=";

    private static final String ACCESS_TOKEN = "accesstoken=";

    private static final String REFRESH_TOKEN = "refreshtoken=";

    private static final String EXPIRY_TIME = "expirytime=";

    public void saveOAuthData(ZohoOAuthTokens tokens) throws ZohoOAuthException {
        try {
            File file = new File( getPersistenceHandlerFilePath() );
            PrintWriter writer = null;
            if( !file.exists() ) {
                writer = new PrintWriter( new BufferedWriter( new FileWriter( file ) ) );
            } else {
                writer = new PrintWriter( new BufferedWriter( new FileWriter( file, true ) ) );
                writer.println();
            }
            if( isUserAuthenticated( tokens.getUserMailId() ) ) {
                List<UserProperty> userProperties = getAllUserProperties();
                for( UserProperty property : userProperties ) {
                    if( property.getUserIdentifier().equals( tokens.getUserMailId() ) ) {
                        property.setAccessToken( tokens.getAccessToken() );
                        property.setRefreshToken( tokens.getRefreshToken() );
                        property.setExpiryTime( String.valueOf( tokens.getExpiryTime() ) );
                        break;
                    }
                }
                writeAllProperties( userProperties );
                ZDLogger.logError("Tokens are updated in a file.");
                return;
            }
            writer.println( USER_IDENTIFIER + tokens.getUserMailId() );
            writer.println( ACCESS_TOKEN + tokens.getAccessToken() );
            writer.println( REFRESH_TOKEN + tokens.getRefreshToken() );
            writer.println( EXPIRY_TIME + tokens.getExpiryTime() );
            writer.close();
            ZDLogger.logError("Tokens inserted into a file.");
        } catch (Exception exp) {
            ZDLogger.logError("Exception while inserting tokens to file.");
            exp.printStackTrace();
            throw new ZohoOAuthException(exp);
        }
    }

    public ZohoOAuthTokens getOAuthTokens(String mailId) throws ZohoOAuthException {
        try {
            UserProperty property = getUserProperty(mailId);
            if( property == null ) {
                throw new ZohoOAuthException("Given User not found in a file.");
            }
            ZohoOAuthTokens tokens = new ZohoOAuthTokens();
            tokens.setUserMailId( property.getUserIdentifier() );
            tokens.setAccessToken( property.getAccessToken() );
            tokens.setRefreshToken( property.getRefreshToken() );
            tokens.setExpiryTime( Long.valueOf( property.getExpiryTime() ) );
            return tokens;
        } catch (Exception exp) {
            throw new ZohoOAuthException(exp);
        }
    }

    public void deleteOAuthTokens(String mailId) throws ZohoOAuthException {
        try {
            List<UserProperty> userProperties = new ArrayList<>();
            for( UserProperty property : getAllUserProperties() ) {
                if( !property.getUserIdentifier().equals( mailId ) ) {
                    userProperties.add( property );
                }
            }
            writeAllProperties(userProperties);
            ZDLogger.logError("tokens deleted from a file.");
        } catch (Exception exp) {
            ZDLogger.logError("Exception while deleting tokens from file.");
            exp.printStackTrace();
            throw new ZohoOAuthException(exp);
        }
    }

    public boolean isUserAuthenticated(String mailId) {
        return getUserProperty(mailId) != null;
    }

    private UserProperty getUserProperty(String mailId) {
        UserProperty userProperty = null;
        for( UserProperty property : getAllUserProperties() ) {
            if( property.getUserIdentifier().equals( mailId ) ) {
                return property;
            }
        }
        return userProperty;
    }

    private List<UserProperty> getAllUserProperties() {
        try {
            BufferedReader reader = new BufferedReader( new FileReader( new File( getPersistenceHandlerFilePath() ) ) );
            String line = null;
            List<UserProperty> properties = new ArrayList<>();
            while( ( line = reader.readLine() ) != null ) {
                if( line.contains("useridentifier")  ) {
                    UserProperty property = new UserProperty();
                    property.setUserIdentifier( line );
                    property.setAccessToken( reader.readLine() );
                    property.setRefreshToken( reader.readLine() );
                    property.setExpiryTime( reader.readLine() );
                    properties.add( property );
                }
            }
            reader.close();
            return properties;
        } catch(Exception ex) {
            throw new ZohoOAuthException(ex);
        }
    }

    private void writeAllProperties( List<UserProperty> properties ) throws IOException, ZohoOAuthException {
        PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( new File( getPersistenceHandlerFilePath() ) ) ) );
        for( int i = 0; i < properties.size(); i++ ) {
            UserProperty property = properties.get(i);
            writer.println( USER_IDENTIFIER + property.getUserIdentifier() );
            writer.println( ACCESS_TOKEN + property.getAccessToken() );
            writer.println( REFRESH_TOKEN + property.getRefreshToken() );
            writer.println( EXPIRY_TIME + property.getExpiryTime() );
            if( i != ( properties.size() - 1 ) ) {
                writer.println();
            }
        }
        writer.close();
    }

    public static String getPersistenceHandlerFilePath() throws ZohoOAuthException {
        String filePath = ZohoOAuthClient.getInstance().getConfigValue("oauth_tokens_file_path");
        if( filePath != null ) {
            return filePath;
        }
        throw new ZohoOAuthException("oauth_tokens_file_path - property is missing/invalid.");
    }

}

class UserProperty {

    String userIdentifier;

    String accessToken;

    String refreshToken;

    String expiryTime;

    String getUserIdentifier() {
        return userIdentifier;
    }

    void setUserIdentifier(String userIdentifier) {
        String[] split = userIdentifier.split("=");
        this.userIdentifier = split.length == 1 ? userIdentifier : split[1].trim();
    }

    String getAccessToken() {
        return accessToken;
    }

    void setAccessToken(String accessToken) {
        String[] split = accessToken.split("=");
        this.accessToken = split.length == 1 ? accessToken : split[1].trim();
    }

    String getRefreshToken() {
        return refreshToken;
    }

    void setRefreshToken(String refreshToken) {
        String[] split = refreshToken.split("=");
        this.refreshToken = split.length == 1 ? refreshToken : split[1].trim();
    }

    String getExpiryTime() {
        return expiryTime;
    }

    void setExpiryTime(String expiryTime) {
        String[] split = expiryTime.split("=");
        this.expiryTime = split.length == 1 ? expiryTime : split[1].trim();
    }

}
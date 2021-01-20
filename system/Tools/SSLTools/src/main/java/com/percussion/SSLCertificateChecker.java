package com.percussion;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This Java class can run on command line in main method
 * and take an httpsurl as an argument and a warningDays
 * argument where warningDays is the number of days before an SSL
 * certificate expires.   This class also accepts a urlFile argument that
 * can be a list of urls to check.  If there are certificates expiring
 * this class post to a configurable slack account and channel.
 *
 * Slack account configurations can be added to slack.properties in the resources directory.
 */
public class SSLCertificateChecker {

    private static final String SLACK_PROPERTY_FILE = "/slack.properties";
    private  URL slackUrl;
    private  String slackUrlStr;
    private  String slackChannel;
    private  String slackUserName;
    private  boolean messagePostedFlag = false;
    private StringBuffer  messageBuffer = null;
    static Logger log = Logger.getLogger(SSLCertificateChecker.class.getName());

    public static void main(String[] args)  {
        if (args.length <2){
            System.out.println("Usage: PSSSLCertificateChecker [url or file containing urls] [warningDays] ");
            return;
        }
        SSLCertificateChecker sslChecker = new SSLCertificateChecker();
        sslChecker.initSlackProperties();
        String urlStr = args[0];
        String warnDays = args[1];
        int warningDays = Integer.parseInt(warnDays);
        //b'coz maximum limit of slack post message is 4000 chars.

        URL url = null;
        try {
            URI uri = URI.create(urlStr);
            url = uri.toURL();
        }catch(Exception e){
            //Eating this Exception as may be list of urls sent in file rather than 1 url.
        }

        File urlsFile = null;
        if(url != null) {
            sslChecker.checkCertificate(urlStr,warningDays);
        }else{
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(urlStr));
                String line = reader.readLine();
                while (line != null) {
                    sslChecker.checkCertificate(line,warningDays);
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Invalid URL or File passed : " + urlStr);
            }

        }
        if(!sslChecker.messagePostedFlag){
            if(sslChecker.messageBuffer != null &&  sslChecker.messageBuffer.toString().trim().length() !=0) {
                sslChecker. postSlackMessage();
            }
        }
    }


    private void checkCertificate(String urlStr, int warningDays) {
        URL url = null;

        try {
            URI uri = URI.create(urlStr);
            url = uri.toURL();
        }catch(MalformedURLException e){
            System.out.println("Not a Valid URL : " + urlStr);
            return;
        }
        if(url == null){
            System.out.println("Not a valid URL : " + urlStr);
            return;
        }
        try {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.connect();
            Certificate[] certs = conn.getServerCertificates();
            for (Certificate c : certs) {
                if (c instanceof X509Certificate) {
                    X509Certificate xc = (X509Certificate) c;
                    Date expiresOn = xc.getNotAfter();
                    Date now = new Date();
                    long daysLeft = (expiresOn.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
                    if (daysLeft < warningDays) {
                        String msg = url + " : " + " Certificate Will expire on : " + expiresOn + " So, only " +
                                daysLeft + " days to go";
                        sendSlackMessage(msg);
                    }
                }
            }

        }catch (IOException io){
            System.out.println("Filed to Load Certificates for given URL : " + urlStr);
        }
    }

    /**
     * This API loads slack post properties required from slack.properties file
     * in the current directory resources folder
     */
    private void initSlackProperties()  {

        try {

           InputStream input = this.getClass().getResourceAsStream(SLACK_PROPERTY_FILE);
           // InputStream input = new FileInputStream(new File(SLACK_PROPERTY_FILE));
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            slackUrlStr = prop.getProperty("url");
            URI uri = URI.create(slackUrlStr);
            slackUrl = uri.toURL();
            slackChannel = prop.getProperty("channel");
            slackUserName = prop.getProperty("username");

        }catch (FileNotFoundException fnf){
            System.out.println( "Slack Properties file not found");
        }catch (IOException io){
            System.out.println("Failed to load Slack Properties File");
        }
    }

    /**
     * This API keeps collecting the messages in messageBuffer
     * and puts the message on console as well.
     */

    private void sendSlackMessage(String message) {

        System.out.println(message);
        if(messageBuffer == null) {
            messageBuffer = new StringBuffer(4000);
            messageBuffer.append(message);
        }else if (messageBuffer.length() < 2000) {
            messageBuffer.append(System.getProperty("line.separator"));
            messageBuffer.append(message);

        } else {
            messageBuffer.append(System.getProperty("line.separator"));
            messageBuffer.append(message);
            postSlackMessage();

        }
    }

    /**
     * This API actually posts an http Request to Slack Url
     * In case Slack properties are not set, then just system out will happen on console.
     */
    private void postSlackMessage() {

        if (slackUrl != null) {

            //Incoming Webhook e.g."https://hooks.slack.com/services/your-token-here"
            HttpPost post = new HttpPost(slackUrlStr);

            CloseableHttpClient httpClient = HttpClients.createDefault();

            JSONObject json = new JSONObject();
            try {
                json.put("channel", slackChannel);
                json.put("text", messageBuffer.toString());
                json.put("username", slackUserName);

                StringEntity params = new StringEntity(json.toString());
                post.addHeader("content-type", "application/json");
                post.setEntity(params);
                httpClient.execute(post);

            } catch (Exception ex) {
                ex.printStackTrace();
                // handle exception here
            } finally {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messagePostedFlag = true;
                messageBuffer = null;
            }
        }
    }
}


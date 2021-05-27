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

package com.percussion.tls;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TLSTester {

    private static final Logger log = LogManager.getLogger(TLSTester.class);

    private static String OS = null;
    public static String getOsName()
    {
        if(OS == null) { OS = System.getProperty("os.name"); }
        return OS;
    }
    public static boolean isWindows()
    {
        return getOsName().startsWith("Windows");
    }

    public static final String KEYSTORE_PASS = "changeit";


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException, KeyStoreException {




        String yahoocert="-----BEGIN CERTIFICATE-----\n" +
                "MIIJHzCCCAegAwIBAgIQCJCo+qXyE8vjILXtpTJnkjANBgkqhkiG9w0BAQsFADBw\n" +
                "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\n" +
                "d3cuZGlnaWNlcnQuY29tMS8wLQYDVQQDEyZEaWdpQ2VydCBTSEEyIEhpZ2ggQXNz\n" +
                "dXJhbmNlIFNlcnZlciBDQTAeFw0xOTA1MDEwMDAwMDBaFw0xOTEwMjgxMjAwMDBa\n" +
                "MGMxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRIwEAYDVQQHEwlT\n" +
                "dW5ueXZhbGUxETAPBgNVBAoTCE9hdGggSW5jMRgwFgYDVQQDDA8qLnd3dy55YWhv\n" +
                "by5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCkM1GHoSo/9oKj\n" +
                "PqENo9GMbP5yvtXZQoi8doHlLkHOGToMV90U+zKxfobAGYlJYV4kJjCxHXg/8FU0\n" +
                "AYvHVcs+VhicEGaSIUZ6p1T87YqqKC5x7QSUMk+ffmHA0Y75bMqOogmy4o6p+7fq\n" +
                "t4qaW1XHm0a3vXCZNFAz2nrJg3RI8bcCFbdP4mFccHuDH31s9gNJDFKD/qgaB29p\n" +
                "gR+uL0X/T8REDHVDtIfaDHE9WpPxeqdwltDQieGlg4Bm40jJDcHA7u0gpVnlTX77\n" +
                "0JajcqGguTXIpAqQJH14iHDE8oqSvFJF0Hy3xZTl4cn/LqvHBzvdYYMu6RCaKNvG\n" +
                "fqhKJSzvAgMBAAGjggXAMIIFvDAfBgNVHSMEGDAWgBRRaP+QrwIHdTzM2WVkYqIS\n" +
                "uFlyOzAdBgNVHQ4EFgQUrpm7KbnCAMUN1HOkiQNiNVmUAZswggLpBgNVHREEggLg\n" +
                "MIIC3IIPKi53d3cueWFob28uY29tghBhZGQubXkueWFob28uY29tgg4qLmFtcC55\n" +
                "aW1nLmNvbYIMYXUueWFob28uY29tggxiZS55YWhvby5jb22CDGJyLnlhaG9vLmNv\n" +
                "bYIPY2EubXkueWFob28uY29tghNjYS5yb2dlcnMueWFob28uY29tggxjYS55YWhv\n" +
                "by5jb22CEGRkbC5mcC55YWhvby5jb22CDGRlLnlhaG9vLmNvbYIUZW4tbWFrdG9v\n" +
                "Yi55YWhvby5jb22CEWVzcGFub2wueWFob28uY29tggxlcy55YWhvby5jb22CD2Zy\n" +
                "LWJlLnlhaG9vLmNvbYIWZnItY2Eucm9nZXJzLnlhaG9vLmNvbYISZnJvbnRpZXIu\n" +
                "eWFob28uY29tggxmci55YWhvby5jb22CDGdyLnlhaG9vLmNvbYIMaGsueWFob28u\n" +
                "Y29tgg5oc3JkLnlhaG9vLmNvbYIXaWRlYW5ldHNldHRlci55YWhvby5jb22CDGlk\n" +
                "LnlhaG9vLmNvbYIMaWUueWFob28uY29tggxpbi55YWhvby5jb22CDGl0LnlhaG9v\n" +
                "LmNvbYIRbWFrdG9vYi55YWhvby5jb22CEm1hbGF5c2lhLnlhaG9vLmNvbYIMbWJw\n" +
                "LnlpbWcuY29tggxteS55YWhvby5jb22CDG56LnlhaG9vLmNvbYIMcGgueWFob28u\n" +
                "Y29tggxxYy55YWhvby5jb22CDHJvLnlhaG9vLmNvbYIMc2UueWFob28uY29tggxz\n" +
                "Zy55YWhvby5jb22CDHR3LnlhaG9vLmNvbYIMdWsueWFob28uY29tggx1cy55YWhv\n" +
                "by5jb22CEXZlcml6b24ueWFob28uY29tggx2bi55YWhvby5jb22CDXd3dy55YWhv\n" +
                "by5jb22CCXlhaG9vLmNvbYIMemEueWFob28uY29tgg9oay5yZC55YWhvby5jb22C\n" +
                "D3R3LnJkLnlhaG9vLmNvbTAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYB\n" +
                "BQUHAwEGCCsGAQUFBwMCMHUGA1UdHwRuMGwwNKAyoDCGLmh0dHA6Ly9jcmwzLmRp\n" +
                "Z2ljZXJ0LmNvbS9zaGEyLWhhLXNlcnZlci1nNi5jcmwwNKAyoDCGLmh0dHA6Ly9j\n" +
                "cmw0LmRpZ2ljZXJ0LmNvbS9zaGEyLWhhLXNlcnZlci1nNi5jcmwwTAYDVR0gBEUw\n" +
                "QzA3BglghkgBhv1sAQEwKjAoBggrBgEFBQcCARYcaHR0cHM6Ly93d3cuZGlnaWNl\n" +
                "cnQuY29tL0NQUzAIBgZngQwBAgIwgYMGCCsGAQUFBwEBBHcwdTAkBggrBgEFBQcw\n" +
                "AYYYaHR0cDovL29jc3AuZGlnaWNlcnQuY29tME0GCCsGAQUFBzAChkFodHRwOi8v\n" +
                "Y2FjZXJ0cy5kaWdpY2VydC5jb20vRGlnaUNlcnRTSEEySGlnaEFzc3VyYW5jZVNl\n" +
                "cnZlckNBLmNydDAMBgNVHRMBAf8EAjAAMIIBAwYKKwYBBAHWeQIEAgSB9ASB8QDv\n" +
                "AHYA7ku9t3XOYLrhQmkfq+GeZqMPfl+wctiDAMR7iXqo/csAAAFqdMSsygAABAMA\n" +
                "RzBFAiEA+x2otregfacyFT3PRD33cgNQWIi4yrR0kBAtnCZsn2kCIDrHp/xP6zwD\n" +
                "dsGqEjSINAE9jmKrgo/elELKjftwT83lAHUAh3W/51l8+IxDmV+9827/Vo1HVjb/\n" +
                "SrVgwbTq/16ggw8AAAFqdMSt5gAABAMARjBEAiAB4YV22p0U22BJh5roMLNgx+Ms\n" +
                "h2VIEz0Jz56BSmtv6gIgP2dSVn2gw61bntjp9yGGR14Lyj5Q+LwTlVXmvNrlW1sw\n" +
                "DQYJKoZIhvcNAQELBQADggEBALAFKLcI0WP4KM5SSnQniOi0Y3lVaVCRsEX40aIp\n" +
                "2vA1oPnrN+Y1ZvheFnZXfT2wlfbvEW4RBIT2NBm7z+adVldZ+lQE56qgng+Tab/j\n" +
                "bccWlpHioITDQHkILEZEi4jpD6L3A55OfJtOtanYF4ZriagYW7XUmaHGsKEgAJ7N\n" +
                "OsqsXud1I8L/DYkokttQnbiPvl+3jNnwlq4vbHvYJMBHTr9vwUJHRpLyGkpD7cwn\n" +
                "FRqHMK/+/gxjRr+GgNgA5UwjptyEwzfiXlHpOgYhawSS/pJphxjpNpnwbfozwo4j\n" +
                "ThR/tNqj9qhqwdtKQKNYEhyQNipodImwdKGcDIOC77cgj/A=\n" +
                "-----END CERTIFICATE-----\n";


        final File store = new File("truststore.jks");
        KeyStore jksKeystore = getJKSKeystore(store, KEYSTORE_PASS, true);

        boolean isWindows = isWindows();
        WrappedTrustManager customTm = new WrappedTrustManager();
        customTm.addKeyStore("Local Keystore", jksKeystore);

        if (isWindows) {
            KeyStore windowsMyKeystore = KeyStore.getInstance("Windows-MY");
            KeyStore winRootKeystore = KeyStore.getInstance("Windows-ROOT");
            customTm.addKeyStore("Windows-MY",windowsMyKeystore);
            customTm.addKeyStore("Windows-ROOT",winRootKeystore);
        }


        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new TrustManager[] { customTm }, null);


        SSLSocketFactory sf = sslContext.getSocketFactory();
        String[] supportedCiphers = null;
        String[] protocols = null;
        try ( SSLSocket socket = (SSLSocket) sf.createSocket("www.google.com", 443)){

             supportedCiphers = socket.getSupportedCipherSuites();
             protocols = new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"};
        }
        Set<String> workingProtocols = new HashSet<>();
        Map<String,Set<String>> serverSupported = new HashMap<>();

        boolean connected = true;

        Set<String> testProt = new HashSet<>(Arrays.asList(protocols));
        Set<String> testCipher = null;
        String lastProt = null;
        testCipher = new HashSet<>(Arrays.asList(supportedCiphers));
        System.out.println("test"+testCipher);
        try{
        while (connected = true)
        {
            try {


                try ( SSLSocket socket = (SSLSocket) sf.createSocket("www.percussion.com", 443)) {
                    socket.setSoTimeout(500);
                    socket.setEnabledProtocols(testProt.toArray(new String[testProt.size()]));
                    socket.setEnabledCipherSuites(testCipher.toArray(new String[testCipher.size()]));
                    socket.startHandshake();
                    SSLSession session = socket.getSession();
                    socket.close();
                    String cipher = session.getCipherSuite();
                    String protocol = session.getProtocol();
                    socket.close();

                    lastProt = protocol;
                    System.out.println("Connected with "+protocol+" cipher "+cipher);
                    testCipher.remove(cipher);
                    workingProtocols.add(protocol+":"+cipher);
                }
            }catch (IOException e){

                if (testProt.size()>0 && testCipher.size()!=supportedCiphers.length) {
                    testProt.remove(lastProt);
                    testCipher = new HashSet<>(Arrays.asList(supportedCiphers));
                }
                else
                {

                    throw e;
                }

            }
        }
        }catch (IOException e2){
            System.out.println("No more connections");
        }

        

        //  SSLSocket csf = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        String https_url = "https://www.google.com/";
        URL url;
        try {

            url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

            //dumpl all cert info
            print_https_cert(con);

            //dump all the content
           //print_content(con);

        } catch (MalformedURLException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }


    private static void importCertificate(X509Certificate caCert, String pem, CertificateFactory cf, KeyStore myTrustStore, File store)  {

        try (FileInputStream myKeys = new FileInputStream(store)) {

// Do the same with your trust store this time
// Adapt how you load the keystore to your needs

            myTrustStore.load(myKeys, KEYSTORE_PASS.toCharArray());
        Principal DN = caCert.getSubjectDN();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(pem.getBytes()))
        {
            Certificate cert = cf.generateCertificate(bis);
            String alias = caCert.getSubjectDN().getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            System.out.println("adding alias "+alias);
            myTrustStore.setCertificateEntry( alias, cert);
        }
        try(FileOutputStream fo = new FileOutputStream( store )) {
            myTrustStore.store(fo, KEYSTORE_PASS.toCharArray());
        }
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        } catch (KeyStoreException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        } catch (CertificateException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        return;
    }

    private static void print_content(HttpsURLConnection con){
        if(con!=null){



                System.out.println("****** Content of the URL ********");
                try(BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(con.getInputStream()))){

                String input;

                while ((input = br.readLine()) != null){
                    System.out.println(input);
                }

            } catch (IOException e) {
                    log.error(e.getMessage());
                    log.debug(e.getMessage(), e);
            }

        }

    }

    private static void print_https_cert(HttpsURLConnection con){

        if(con!=null){

            try {

                System.out.println("Response Code : " + con.getResponseCode());
                System.out.println("Cipher Suite : " + con.getCipherSuite());
                System.out.println("\n");

                Certificate[] certs = con.getServerCertificates();
                for(Certificate cert : certs){
                    System.out.println("Cert Type : " + cert.getType());
                    System.out.println("Cert Hash Code : " + cert.hashCode());
                    System.out.println("Cert Public Key Algorithm : "
                            + cert.getPublicKey().getAlgorithm());
                    System.out.println("Cert Public Key Format : "
                            + cert.getPublicKey().getFormat());
                    System.out.println("\n");
                }

            } catch (SSLPeerUnverifiedException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
            } catch (IOException e){
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
            }

        }

    }


    private static void listCiphers() throws IOException {
        SSLServerSocketFactory ssf = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        String scheme = "https";

        //SecureProtocolSocketFactory ssf = new TLSV12ProtocolSocketFactory(baseFactory);

        SSLSocket csf = (SSLSocket) SSLSocketFactory.getDefault().createSocket();


        Set<String> enabledCiphers = new HashSet<>(Arrays.asList(csf.getEnabledCipherSuites()));
        Set<String> defaultCiphers =  new HashSet<>(Arrays.asList(ssf.getDefaultCipherSuites()));
        Set<String> availableCiphers = new HashSet<>(Arrays.asList(ssf.getSupportedCipherSuites()));


        System.out.println("Default\tCipher");
        for (Iterator i = availableCiphers.iterator(); i.hasNext(); ) {
            String cipher = (String) i.next();
            if (defaultCiphers.contains(cipher))
                System.out.print('*');
            else
                System.out.print(' ');
            if (enabledCiphers.contains(cipher))
                System.out.print('*');
            else
                System.out.print(' ');

            System.out.print('\t');
            System.out.println(cipher);
        }
    }

    static boolean isUnlimitedCryptoLength() {

        try {
            int length = Cipher.getMaxAllowedKeyLength("AES");
            // 128 is the limited cryto length, and Int.max_value is is unlimited.
            boolean unlimited = (length == Integer.MAX_VALUE);
            return unlimited;
        } catch (NoSuchAlgorithmException e) {
        }
        //catch (NoSuchProviderException e) {
        //}
        return false;
    }

    public static void getEnabledCiphers() {
        for (Provider provider : Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key : provider.stringPropertyNames())
                System.out.println("\t" + key + "\t" + provider.getProperty(key));
        }
    }

    protected static String convertToPem(X509Certificate cert) throws CertificateEncodingException {

        String cert_begin = "-----BEGIN CERTIFICATE-----\n";
        String end_cert = "-----END CERTIFICATE-----";

        byte[] derCert = cert.getEncoded();
        String pemCertPre = new String( Base64.encodeBase64(derCert,true));
        String pemCert = cert_begin + pemCertPre + end_cert;
        return pemCert;
    }
    private static KeyStore getJKSKeystore(File store, String password, boolean create) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        
        if (!store.exists()) {

            myTrustStore.load(null, null);
            try(FileOutputStream fo = new FileOutputStream( store )) {
                myTrustStore.store(fo, password.toCharArray());
            }
        }

        try (FileInputStream myKeys = new FileInputStream(store)) {
            myTrustStore.load(myKeys, KEYSTORE_PASS.toCharArray());
        }
        return myTrustStore;
    }

}

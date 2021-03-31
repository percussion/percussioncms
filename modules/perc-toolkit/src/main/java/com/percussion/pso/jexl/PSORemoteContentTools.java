package com.percussion.pso.jexl;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Provide tools to work with remote XML/HTML/JSON content
 *
 * @author justinraines
 */
@SuppressWarnings("unused")
public class PSORemoteContentTools extends PSJexlUtilBase implements IPSJexlExpression {


    private static final Log log = LogFactory.getLog(PSORemoteContentTools.class);

    public PSORemoteContentTools(){
        super();
    }

    /**
     * This gets remote JSON content and returns a JSONobject.
     * @param urlString
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns a status code for a url",
            params={
                    @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired")
            },
            returns="Returns an integer status code")
    public int getHTTPStatusCode(String urlString)
            throws IllegalArgumentException, IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);
        String responseBody;
        int statusCode = 400;
        try{
            statusCode = client.executeMethod(get);
        }finally{
            // Process the data from the input stream.
            get.releaseConnection();
        }
        return statusCode;
    }

    /**
     * This gets remote JSON content and returns a JSONobject.
     * @param urlString
     * @param username
     * @param password
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns a status code for a url",
            params={
                    @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
                    @IPSJexlParam(name="username", description="username"),
                    @IPSJexlParam(name="password", description="password")
            },
            returns="Returns a integer status code")
    public int getHTTPStatusCode(String urlString, String username, String password)
            throws IllegalArgumentException, IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);

        Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
        URI uriObject = new URI(urlString);

        int statusCode = 400;
        try{
            client.getState().setCredentials(
                    new AuthScope(uriObject.getHost(),uriObject.getPort(), AuthScope.ANY_REALM),
                    defaultcreds);

            statusCode = client.executeMethod(get);

        }finally{
            // Process the data from the input stream.
            get.releaseConnection();
        }

        return statusCode;
    }

    /**
     * This gets remote JSON content and returns a JSONobject.
     * @param urlString
     * @param headers
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns status code based on url",
            params={
                    @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
                    @IPSJexlParam(name="headers", description="map of headers to set")
            },
            returns="Returns a integer status code")
    public int getHTTPStatusCode(String urlString, Map<String,String> headers)
            throws IllegalArgumentException, IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);

        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            get.setRequestHeader(entry.getKey(), entry.getValue());
        }


        int statusCode = 400;
        try{

            statusCode = client.executeMethod(get);

        }finally{
            // Process the data from the input stream.
            get.releaseConnection();
        }

        return statusCode;
    }

    /**
     * This gets a status code for a url.
     * @param urlString
     * @param headers
     * @param username
     * @param password
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns status code based on a URL.",
            params={
                    @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
                    @IPSJexlParam(name="headers", description="map of headers to set"),
                    @IPSJexlParam(name="username", description="username"),
                    @IPSJexlParam(name="password", description="password")
            },
            returns="Returns a integer status code")
    public int getHTTPStatusCode(String urlString, Map<String,String> headers, String username, String password)
            throws IllegalArgumentException, IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);

        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            get.setRequestHeader(entry.getKey(), entry.getValue());
        }

        Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
        URI uriObject = new URI(urlString);

        int statusCode = 400;
        try{
            client.getState().setCredentials(
                    new AuthScope(uriObject.getHost(),uriObject.getPort(), AuthScope.ANY_REALM),
                    defaultcreds);

            statusCode = client.executeMethod(get);

        }finally{
            // Process the data from the input stream.
            get.releaseConnection();
        }

        return statusCode;
    }




    /**
     * This gets remote JSON content and returns a JSONobject.
     * @param urlString
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSONObject based on a URL.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired")
        },
        returns="Returns a net.sf.json.JSONObject")
    public JSONObject getRemoteJSONContent(String urlString)
            throws IllegalArgumentException, IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);
        String responseBody;
        try{
            int statusCode = client.executeMethod(get);
            if (statusCode != HttpStatus.SC_OK) {
                log.debug("Get failed" + get.getStatusLine() + ": " + urlString);
            }
            // execute method and handle any error responses.
            responseBody = get.getResponseBodyAsString();
        }finally{
            // Process the data from the input stream.
            get.releaseConnection();
        }

        JSONObject jsonObject = JSONObject.fromObject(responseBody);
        return jsonObject;
    }

    /**
     * This gets remote JSON content and returns a JSONobject.
     * @param urlString
     * @param username
     * @param password
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSONObject based on a URL.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="username", description="username"),
            @IPSJexlParam(name="password", description="password")
        },
        returns="Returns a net.sf.json.JSONObject")
    public JSONObject getRemoteJSONContent(String urlString, String username, String password)
            throws IllegalArgumentException, IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);

        Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
        URI uriObject = new URI(urlString);

        String responseBody;
        try{
            client.getState().setCredentials(
                new AuthScope(uriObject.getHost(),uriObject.getPort(), AuthScope.ANY_REALM),
                defaultcreds);

            int statusCode = client.executeMethod(get);
            if (statusCode != HttpStatus.SC_OK) {
                log.debug("Get failed" + get.getStatusLine() + ": " + urlString);
            }
            responseBody = get.getResponseBodyAsString();
        }finally{
            // Process the data from the input stream.
            get.releaseConnection();
        }

        JSONObject jsonObject = JSONObject.fromObject(responseBody);
        return jsonObject;
    }

    /**
     * This gets remote JSON content and returns a JSONobject.
     * @param urlString
     * @param headers
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSONObject based on a URL.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="headers", description="map of headers to set")
        },
        returns="Returns a net.sf.json.JSONObject")
    public JSONObject getRemoteJSONContent(String urlString, Map<String,String> headers)
            throws IllegalArgumentException, IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);

        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            get.setRequestHeader(entry.getKey(), entry.getValue());
        }

        String responseBody;
        try{

            int statusCode = client.executeMethod(get);
            if (statusCode != HttpStatus.SC_OK) {
                log.debug("Get failed" + get.getStatusLine() + ": " + urlString);
            }
            responseBody = get.getResponseBodyAsString();
        }finally{
            // Process the data from the input stream.
            get.releaseConnection();
        }

        JSONObject jsonObject = JSONObject.fromObject(responseBody);
        return jsonObject;
    }

    /**
     * This gets remote JSON content and returns a JSONobject.
     * @param urlString
     * @param headers
     * @param username
     * @param password
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSONObject based on a URL.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="headers", description="map of headers to set"),
            @IPSJexlParam(name="username", description="username"),
            @IPSJexlParam(name="password", description="password")
        },
        returns="Returns a net.sf.json.JSONObject")
    public JSONObject getRemoteJSONContent(String urlString, Map<String,String> headers, String username, String password)
            throws IllegalArgumentException, IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);

        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            get.setRequestHeader(entry.getKey(), entry.getValue());
        }

        Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
        URI uriObject = new URI(urlString);

        String responseBody;
        try{
            client.getState().setCredentials(
                    new AuthScope(uriObject.getHost(),uriObject.getPort(), AuthScope.ANY_REALM),
                    defaultcreds);

            int statusCode = client.executeMethod(get);
            if (statusCode != HttpStatus.SC_OK) {
                log.debug("Get failed" + get.getStatusLine() + ": " + urlString);
            }
            responseBody = get.getResponseBodyAsString();
        }finally{
            // Process the data from the input stream.
            get.releaseConnection();
        }

        JSONObject jsonObject = JSONObject.fromObject(responseBody);
        return jsonObject;
    }


    /**
     * This gets remote XML content and returns a JSOUP Document object.
     * @param urlString
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSOUP document with xml content, returns a JSoup Document element.",
            params={
                    @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired")
            },
            returns="Returns a JSOUP document")
    public Document getRemoteXMLContent(String urlString)
            throws IllegalArgumentException, IOException {
        Document doc = Jsoup.connect(urlString).get();
        return doc;
    }

    /**
     * This gets remote XML content with basic authentication and returns a JSOUP Document object.
     * @param urlString
     * @param username
     * @param password
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSOUP document with xml content, returns a JSoup Document element.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="username", description="username"),
            @IPSJexlParam(name="password", description="password")
        },
        returns="Returns a JSOUP document")
    public org.jsoup.nodes.Document getRemoteXMLContent(String urlString, String username, String password)
            throws IllegalArgumentException, IOException {
        String login = username + ":" + password;
        String base64login = new String(Base64.encodeBase64(login.getBytes()));
        Document doc = Jsoup.connect(urlString)
                .header("Authorization", "Basic " + base64login)
                .get();
        return doc;
    }


    /**
     * This gets remote XML content with map of headers and returns a JSOUP Document object.
     * @param urlString
     * @param headers
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSOUP document with xml content, returns a JSoup Document element.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="headers", description="Map of headers")
        },
        returns="Returns a JSOUP document")
    public Document getRemoteXMLContent(String urlString, Map<String, String> headers)
            throws IllegalArgumentException, IOException {
        Connection connection = Jsoup.connect(urlString);
        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            connection = connection.header(entry.getKey(),entry.getValue());
        }
        Document doc = connection.get();
        return doc;
    }


    /**
     * This gets remote XML content with map of headers, username, and password then returns a JSOUP Document object.
     * @param urlString
     * @param headers
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSOUP document with xml content, returns a JSoup Document element.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="headers", description="Map of headers"),
            @IPSJexlParam(name="username", description="username"),
            @IPSJexlParam(name="password", description="password")
        },
        returns="Returns a JSOUP document")
    public Document getRemoteXMLContent(String urlString, Map<String, String> headers,
                                                        String username, String password)
            throws IllegalArgumentException, IOException {
        String login = username + ":" + password;
        String base64login = new String(Base64.encodeBase64(login.getBytes()));
        Connection connection = Jsoup.connect(urlString).header("Authorization", "Basic " + base64login);
        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            connection = connection.header(entry.getKey(),entry.getValue());
        }
        Document doc = connection.get();
        return doc;
    }



    /* ALIAS METHODS FOR HTML CONTENT, THESE SIMPLY CALL THEIR XML COUNTERPARTS */
    /**
     * This is an aliased method for getRemoteXMLContent
     * @param urlString
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSOUP document with xml content, returns a JSoup Document element. Alias for getRemoteXMLContent",
            params={@IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired")},
            returns="Returns a JSOUP document")
    public Document getRemoteHTMLContent(String urlString)
            throws IllegalArgumentException, IOException {
        return getRemoteXMLContent(urlString);
    }


    /**
     * This is an aliased method for getRemoteXMLContent, with basic authorization
     * @param urlString
     * @param username
     * @param password
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSOUP document with xml content, returns a JSoup Document element.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="username", description="username"),
            @IPSJexlParam(name="password", description="password")
        },
        returns="Returns a JSOUP document")
    public Document getRemoteHTMLContent(String urlString, String username, String password)
            throws IllegalArgumentException, IOException {
        return getRemoteXMLContent(urlString, username, password);
    }

    /**
     * This is an aliased method for getRemoteXMLContent, with headers
     * @param urlString
     * @param headers
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSOUP document with xml content, returns a JSoup Document element.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="headers", description="Map of headers")
        },
        returns="Returns a JSOUP document")
    public Document getRemoteHTMLContent(String urlString, Map<String,String> headers)
            throws IllegalArgumentException, IOException {
        return getRemoteXMLContent(urlString, headers);
    }

    /**
     * This is an aliased method for getRemoteXMLContent, with headers & basic authorization
     * @param urlString
     * @param headers
     * @param username
     * @param password
     * @return org.jsoup.nodes.Document
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @IPSJexlMethod(description="Returns JSOUP document with xml content, returns a JSoup Document element.",
        params={
            @IPSJexlParam(name="urlString", description="url to pull content from, include query params if desired"),
            @IPSJexlParam(name="headers", description="Map of headers"),
            @IPSJexlParam(name="username", description="username"),
            @IPSJexlParam(name="password", description="password")
        },
        returns="Returns a JSOUP document")
    public Document getRemoteHTMLContent(String urlString, Map<String,String> headers, String username, String password)
            throws IllegalArgumentException, IOException {
        return getRemoteXMLContent(urlString, headers, username, password);
    }
}

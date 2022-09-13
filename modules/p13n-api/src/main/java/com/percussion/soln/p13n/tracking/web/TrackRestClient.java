package com.percussion.soln.p13n.tracking.web;

import static com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils.convertServletRequestToTrackingRequest;
import static com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils.jsonToResponse;
import static com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils.parameterizeTrackingRequest;
import static com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils.setVisitorProfileToCookie;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;

/**
 * An abstract class that provides REST client access to the tracking service.
 * See the REST API portion of the {@link com.percussion.soln.p13n.tracking Visitor Tracking Guide}.
 * @author adamgent
 * @see #clientRequest(HttpServletRequest, HttpServletResponse, VisitorTrackingActionRequest)
 * @see com.percussion.soln.p13n.tracking
 */
public class TrackRestClient {

    private final String trackingPath = "/soln-p13n/track/track";
    private final String defaultTrackingURI = "http://localhost:8080" + trackingPath;

    private String trackingURI = null;
    private int timeOut = 0;

    /**
     * Makes a tracking request with the supplied parameters.
     * This is a convenience method 
     * for {@link #clientRequest(HttpServletRequest, HttpServletResponse, VisitorTrackingActionRequest)}.
     * 
     * @param servletRequest
     *            Servlet request. Maybe null. If passed the tracking cookie will be retrieved.
     * @param servletResponse Servlet response. Maybe null. If passed cookie will be set.
     * @param visitorProfileId
     *            Visitor profile id if null will try servlet request for it.
     * @param action tracking action.
     * @param userName userName of profile. Maybe null.
     * @param label label is usually null.
     * @param segmentWeights segment weights.
     * @return the response.
     * @see #clientRequest(HttpServletRequest, HttpServletResponse, VisitorTrackingActionRequest)
     */
    protected VisitorTrackingResponse clientRequest(
            HttpServletRequest servletRequest, 
            HttpServletResponse servletResponse, 
            String visitorProfileId,
            String action, 
            String userName, 
            String label, 
            Map<String, Integer> segmentWeights) {
        
        VisitorTrackingActionRequest tr = new VisitorTrackingActionRequest();

        if (visitorProfileId != null) {
            tr.setVisitorProfileId(Long.parseLong(visitorProfileId));
        } 

        tr.setSegmentWeights(segmentWeights);
        tr.setActionName(action);
        tr.setUserId(userName);
        tr.setLabel(label);
        
        return clientRequest(servletRequest, servletResponse, tr);
    }
    
    /**
     * 
     * Makes tracking action request based on the trackingRequest parameter.
     * 
     * @param servletRequest
     *            Servlet request. Maybe null. If passed the tracking cookie will be retrieved.
     * @param servletResponse Servlet response. Maybe null. If passed cookie will be set.
     * @param trackingRequest {@link VisitorTrackingActionRequest#getActionName() actionName property} should never be <code>null</code>, empty, or blank.
     * @return never <code>null</code>.
     * @see VisitorTrackingActionRequest
     */
    public VisitorTrackingResponse clientRequest(
            HttpServletRequest servletRequest, 
            HttpServletResponse servletResponse, 
            VisitorTrackingActionRequest trackingRequest) {
        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.socket.timeout", getTimeOut());
        final VisitorTrackingActionRequest tr = trackingRequest;
        /*
         * Extract data from the servlet request.
         */
        if (servletRequest != null) {
            javax.servlet.http.Cookie cookies[] = servletRequest.getCookies();
            HttpState state = new HttpState();
            /*
             * Forward all the cookies to the p13n server.
             */
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    javax.servlet.http.Cookie c = cookies[i];
                    String domain = c.getDomain();
                    if (domain == null) {
                        domain = servletRequest.getServerName();
                    }
                    String cpath = c.getPath();
                    if (cpath == null) {
                        cpath = servletRequest.getContextPath();
                    }
                    log.debug("forwarding cookie domain=" + c.getDomain() + " name=" + c.getName() + " value="
                            + c.getValue() + " path=" + c.getPath());
                    Cookie cookie = new Cookie(domain, c.getName(), c.getValue(), cpath, c.getMaxAge(), c.getSecure());
                    state.addCookie(cookie);
                }
            }
            client.setState(state);
            convertServletRequestToTrackingRequest(servletRequest, tr);
        } 

        Map<String, String> nvp = parameterizeTrackingRequest(tr);
        NameValuePair[] paramArray = nameValuePairs(nvp.entrySet());

        HttpMethod method = createMethod(getTrackingURI(servletRequest));
        method.setQueryString(paramArray);
        String response =  executeMethod(client, method);
        VisitorTrackingResponse trackResponse = jsonToResponse(response);
        if (servletRequest != null && servletResponse != null && "OK".equals(trackResponse.getStatus())) {
            setVisitorProfileToCookie(servletRequest, servletResponse, trackResponse.getVisitorProfileId());
        }
        return trackResponse;
    }
    
    
    private NameValuePair[] nameValuePairs(Collection<Entry<String, String>> params) {
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        for(Entry<String,String> e : params) {
            if (isNotBlank(e.getValue())) {
                NameValuePair nvp = new NameValuePair(e.getKey(),e.getValue());
                list.add(nvp);
            }
        }
        return list.toArray(new NameValuePair[] { });
    }

    private HttpMethod createMethod(String requestURI) {
        return new GetMethod(requestURI);
    }

    private String executeMethod(HttpClient client, HttpMethod method) throws TrackRestClientException {
        String uri = "";
        try {
            uri = method.getURI().toString();
            int stat = client.executeMethod(method);
            log.debug("HTTP return code: " + stat);
            if (/* Not Success */!(200 <= stat && stat < 300)) {
                String error = "HTTP Error: " + stat + " Response: \n" + method.getResponseBodyAsString();
                log.error(error);
                throw new TrackRestClientException(error);
            }
            String response = method.getResponseBodyAsString();
            if (log.isDebugEnabled())
                log.debug("Response: " + response);
            return response;
        } catch (HttpException e) {
            throw new TrackRestClientException("HTTP Problem with " + uri, e);
        } catch (IOException e) {
            String error = "Problem connecting to " + uri;
            throw new TrackRestClientException(error, e);
        }

    }

    /**
     * 
     * Indicates an error happened while using trying to connect
     * and use a REST service.
     * 
     * @author adamgent
     *
     */
    public static class TrackRestClientException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public TrackRestClientException(String message) {
            super(message);
        }

        public TrackRestClientException(String message, Throwable cause) {
            super(message, cause);
        }

        public TrackRestClientException(Throwable cause) {
            super(cause);
        }

    }

    /**
     * Gets the tracking request from the servlet request.
     * @param request maybe <code>null</code>.
     * @return never <code>null</code>, empty, or blank.
     */
    public String getTrackingURI(HttpServletRequest request) {
        if (getTrackingURI() != null) {
            return getTrackingURI();
        }
        if (request == null) {
            return defaultTrackingURI;
        }
        return createUrlFromRequest(request);

    }

    /**
     * Creates the REST URL end-point from the servlet request.
     * @param request never <code>null</code>.
     * @return never <code>null</code>, empty, or blank.
     */
    protected String createUrlFromRequest(HttpServletRequest request) {

        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // hostname.com
        int serverPort = request.getServerPort(); // 80
        return createAbsURL(trackingPath, scheme, serverName, serverPort);

    }

    private String createAbsURL(String resource, String scheme, String serverName, int port) {
        URI siteURI = URI.create(scheme + "://" + serverName + ":" + port);
        URI origURI = URI.create(resource);

        if (origURI.isAbsolute()) {
            return origURI.toString();
        }
        return siteURI.resolve(resource).toString();

    }
    
    public int getTimeOut() {
        return timeOut;
    }

    
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * The REST end-point for the tracking service as a configurable property.
     * See {@link com.percussion.soln.p13n.tracking Visitor Tracking Developer Guide}
     * @return if <code>null</code> the servlet request's server will be used.
     */
    public String getTrackingURI() {
        return trackingURI;
    }

    /**
     * See getter.
     * @param trackingURI maybe <code>null</code>.
     * @see #getTrackingURI()
     */
    public void setTrackingURI(String trackingURI) {
        this.trackingURI = trackingURI;
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(TrackLoginRestClient.class);

}

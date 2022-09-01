/**
 * 
 */
package com.percussion.soln.p13n.tracking;

import java.io.Serializable;
import java.util.Map;

/**
 * Abstract Visitor Request that usually represents a <em>single visit</em> to a web resource such
 * as a web page.
 * @author adamgent
 *
 */
public class VisitorRequest implements Cloneable, Serializable {
    
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = -3295795586119132645L;
    private String address;
    private String hostname;
    private String referrerUrl;
    private String srcUrl;
    private String locale;
    private String userId;
    private long id;
    private Map<String, String> headers;

    public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	/**
     * IP Address.
     * @return IP as a string.
     */
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    /**
     * Gets the FQN hostname of the visitor.
     * @return FQN hostname.
     */
    public String getHostname() {
        return hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    /**
     * The last address the visitor visited before the address
     * that created the visitor request.
     * This usually comes from the the Referrer header.
     * @return last address
     */
    public String getReferrerUrl() {
        return referrerUrl;
    }
    public void setReferrerUrl(String referrerUrl) {
        this.referrerUrl = referrerUrl;
    }
    /**
     * The page or URL visited that this request originated from.
     * @return never <code>null</code>.
     */
    public String getSrcUrl() {
        return srcUrl;
    }
    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }
    /**
     * Unique Identifier for serialization purposes.
     * Not needed for programmatic use.
     * @return request id.
     */
    public long getId() {
        return id;
    }
    /**
     * No need to set.
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }
    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    /**
     * Third party user id for systems that manage user registration.
     * 
     * @return The third party user id. May be <code>null</code>.
     */
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userid) {
        this.userId = userid;
    }
    @Override
    public VisitorRequest clone() {
        try {
            return (VisitorRequest) super.clone();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to clone", e);
        }
    }
    
    
}
package com.percussion.soln.p13n.delivery.ds.web;


/**
 * See <a href="http://bob.pythonmac.org/archives/2005/12/05/remote-json-jsonp/">Remote JSON - JSONP</a>
 * @author adamgent
 *
 */
public class JSONP {
    private Object json;
    private String callback;

    public JSONP() {
    }
    
    public JSONP(String callback, Object json) {
        this();
        this.callback = callback;
        this.json = json;
    }


    @Override
    public String toString() {
        String jsonString = getJson() == null ? "undefined" : getJson().toString();
        if (getCallback() != null)
            return getCallback() + "(" + jsonString + ")";
        return jsonString;
    }
    
    public Object getJson() {
        return json;
    }

    public void setJson(Object json) {
        this.json = json;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
    
    

}

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
package com.percussion.webui.gadget.servlets;

import com.percussion.delivery.client.EasySSLProtocolSocketFactory;
import com.percussion.util.PSURLEncoder;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.ws.rs.core.MediaType;

/**
 * This class will takes a JSON object with gadget user preference meta data and will turn it into an html form
 * that is used by the gadget framework to present a form for users to modify gadget user preference settings. It
 * is based on the public Google settings service found at http://www.gmodules.com/ig/gadgetsettings.
 * 
 * 
 * @author erikserating
 *
 */
public class PSUserPrefFormContent {

	private static final Logger log = LogManager.getLogger(PSUserPrefFormContent.class);

	private boolean sslSocketFactoryRegistered;

	private void registerSslProtocol()
	{

		if (sslSocketFactoryRegistered)
			return;

		ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();
		Protocol.registerProtocol("https", new Protocol("https", socketFactory, 443));

		sslSocketFactoryRegistered = true;
	}


	/**
	 * Initialize and build form content based on passed in user prefs.
	 * @param prefs the JSON preferences object, can not be <code>null</code>.
	 * @param moduleId the moduleId, may be <code>null</code> or empty, in which
	 * @param upValues map of name/values that the user persisted for gadget preferences
	 * for this gadget. Cannot be <code>null</code>.
	 * case it will default to &quote;0&quote;.
	 * @param servername
	 * @param serverport
	 * @param pssessionid
	 */
   public PSUserPrefFormContent(List<JSONObject> prefs, String moduleId,
               Map<String, String> upValues,
               String servername, String serverscheme, int serverport, String pssessionid){
		if(prefs == null)
		   throw new IllegalArgumentException("prefs cannot be null.");
		if(upValues == null)
		   throw new IllegalArgumentException("upValues cannot be null.");
		if(moduleId != null || moduleId.length() > 0)	
	      m_moduleId = moduleId;

	   registerSslProtocol();

		m_userPrefValues = upValues;
		m_servername = servername;
		m_serverport = serverport;
		m_serverscheme =serverscheme;
		m_pssessionid = pssessionid;
	   buildFormContent(prefs);
	}	


   @Override
   public String toString()
	{
	   return m_content.toString();
	}
   
   /**
    * Returns the form content within a javascript variable and also 
    * adds the other needed js functions for user prefs to work.
    * @return js string, never <code>null</code> or empty.
    */
   public String toJavaScript()
   {
      StringBuilder buff = new StringBuilder();
      buff.append("var ");
      buff.append(replaceTokens(VAR_HTML));
      buff.append("='");
      buff.append(hexEncodeString(m_content.toString()));
      buff.append("';");
      buff.append(replaceTokens(FUNCTIONS));
      return buff.toString();
   }
   
   
	/**
	 * Builds the html form based on the passed in JSON preferences
	 * object.
	 * @param prefs the JSON preferences object, assumed not <code>null</code>.
	 */
	private void buildFormContent(List<JSONObject> prefs)
	{
		//Container div
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", replaceTokens(ID_MAIN_DIV));
		params.put("class", "perc-gadget-pref-div");
		params.put("style", "display:none");
		m_content.append(createStartTag("div", params, false));
		m_content.append(createStartTag("table", null, false));
		m_content.append(createStartTag("tr", null, false));
		m_content.append(createStartTag("td", null, false));
		params.clear();
		params.put("class", "perc-gadget-pref-inner-div");
		m_content.append(createStartTag("div", params, false));
		for(JSONObject pref : prefs)
		{
		    m_userPrefs.put((String)pref.get("fieldname"), pref);
		}
		for(JSONObject pref : prefs)
		{
		   addField(pref);      
		}
		Map<String, String> nfParams = new HashMap<String, String>();
		nfParams.put("id", replaceTokens(ID_NUMFIELDS));
		nfParams.put("type", "hidden");
		nfParams.put("value", String.valueOf(m_fieldCount + 1));
		m_content.append(createStartTag("input", nfParams, true));
		m_content.append(createEndTag("div"));
		m_content.append(createEndTag("td"));
		m_content.append(createEndTag("tr"));
		m_content.append(createEndTag("table"));
		m_content.append(createEndTag("div"));
		m_content.append("\\n");
		
	}
	
	/**
	 * Creates an HTML start tag string.
	 * @param name the name of the tag, assumed not <code>null</code>.
	 * @param attribs map of attributes that the tag should contain. May
	 * be <code>null</code>.
	 * @param isEmpty flag indicating that the HTML tag is empty.
	 * @return the HTML string, never <code>null</code> or empty.
	 */
	private String createStartTag(String name, Map<String, String> attribs, boolean isEmpty){
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		sb.append(name);
		if(attribs != null)
		{
		   for(String key : attribs.keySet())
		   {
			   sb.append(" ");
			   sb.append(key);
			   sb.append("=");
			   sb.append("\"");
			   sb.append(attribs.get(key));
			   sb.append("\"");
		   }
		}
		if(isEmpty)
			sb.append("/");
		sb.append(">");
		return sb.toString();
	}
	
	/**
	 * Hex encodes the following chars: ( &lt; &gt; &amp; ' &quot; = ? ).
	 * @param str the string to be encoded, assumed not <code>null</code>,
	 * can be empty.
	 * @return hex encoded string, never <code>null</code>.
	 */
	private String hexEncodeString(String str){
	   StringBuilder buff = new StringBuilder();
	   StringReader sr = new StringReader(str);
	   int c = -2;
	   try
	   {
	      while((c = sr.read()) != -1)
	      {
	         switch(c)
	         {
	            case 34:
	               buff.append("\\x22");
	               break;
	            case 38:
	               buff.append("\\x26");
	               break;
	            case 39:
	               buff.append("\\x27");
	               break;
	            case 60:
	               buff.append("\\x3c");
	               break;
	            case 61:
	               buff.append("\\x3d");
	               break;
	            case 62:
	               buff.append("\\x3e");
	               break;
	            case 63:
	               buff.append("\\x3f");
	               break;
	            default:
	               buff.append((char)c);
	         }
	      }
	   }
	   catch(IOException e)
	   {
	      // Should never happen
	      throw new RuntimeException(e);
	   }
	   finally
	   {
	      sr.close();
	   }
	   return buff.toString();
	}
	
	
	/**
	 * Creates an HTML end tag string.
	 * @param name the tag name, assumed not <code>null</code>.
	 * @return HTML end tag string, never <code>null</code> or empty.
	 */
	private String createEndTag(String name){
		return "</" + name + ">";
	}
	
	/**
	 * Adds a form field to the content buffer based on the passed in JSON preference
	 * object.
	 * @param pref the JSON preference object, assumed not <code>null</code>.
	 */
	private void addField(JSONObject pref){
	   m_fieldCount++;
	   String type = (String)pref.get("type");
	   String fn = (String)pref.get("fieldname");
	   String fieldname = replaceTokens(INPUTNAME) + fn;
	   String displayName = (String)pref.get("displayName");
	   if(displayName == null || displayName.length() == 0)
	      displayName = fn;
	   
	   if(type.equals("hidden"))
	   {
	      addHiddenField(fieldname, pref);
	      return;
	   }
	   if(type.equals("bool"))
	   {
	      addBooleanField(fieldname, displayName, pref);
	      return;
	   }
	   if(type.equals("separator"))
	   {
	      addSeparator(fieldname, displayName, pref);
	      return;
	   }
	   		
	   // Label
	   Map<String, String> params = new HashMap<String, String>();
	   params.put("for", fieldname);
	   m_content.append(createStartTag("label", params, false));
	   m_content.append(displayName);
	   m_content.append(":");
	   m_content.append(createEndTag("label"));
	   m_content.append(createStartTag("br", null, true));
	   
	   if(type.equals("string"))
	   {
	      addTextField(fieldname, pref);
	   }
	   else if(type.equals("list"))
	   {
	      addListField(fieldname, pref);
	   }
	   else if(type.equals("enum"))
	   {
	      addEnumField(fieldname, pref);
	   }	   
	   
	}
	
	/**
	 * Add a text form field to the content buffer. Sets default value if
	 * one exists.
	 * @param fieldname the fieldname for the input field, assumed not <code>null</code>,
	 * or empty.
	 * @param pref the JSON preference object, assumed not <code>null</code>.
	 */
	private void addTextField(String fieldname, JSONObject pref){
	   Map<String, String> params = new HashMap<String, String>();
	   String dVal = getFieldValue(pref);
	   params.put("type", "text");
	   params.put("id", replaceTokens(ID_INPUT));
	   params.put("name", fieldname);
	   if(dVal != null && dVal.length() > 0)
	      params.put("value", dVal);
	   m_content.append(createStartTag("input", params, true));
	   m_content.append(createStartTag("br", null, true));
		
	}
	
	/**
    * Add a select form field to the content buffer. Sets the enumeration values as options
    * to the select field and selects default value if it exists in the list.
    * @param fieldname the fieldname for the input field, assumed not <code>null</code>,
    * or empty.
    * @param pref the JSON preference object, assumed not <code>null</code>.
    */
	@SuppressWarnings("unchecked")
   private void addEnumField(String fieldname, JSONObject pref){
	   Map<String, String> params = new HashMap<String, String>();
      String dVal = getFieldValue(pref);
      JSONArray vals = (JSONArray)pref.get("orderedEnumValues");
      params.put("id", replaceTokens(ID_INPUT));
      params.put("name", fieldname);
      m_content.append(createStartTag("select", params, false));
      //Add options
      Iterator it = vals.iterator();
      while(it.hasNext()){
         JSONObject current = (JSONObject)it.next();
         String val = (String)current.get("value");
         String displayVal = (String)current.get("displayValue");
         if(val.startsWith("@url:"))
         {
            handleRemoteOptions(val, dVal);
         }
         else
         {
            addOption(val, displayVal, dVal);
         }
              
      }         	
      m_content.append(createEndTag("select"));
      m_content.append(createStartTag("br", null, true));
	}
	
	private void addOption(String val, String displayVal, String defaultVal)
	{
	   Map<String, String> optParams = new HashMap<String, String>();
      
      if(displayVal == null || displayVal.length() == 0)
         displayVal = val;
      
      optParams.put("value", val);
      if(defaultVal != null && defaultVal.equals(val))
         optParams.put("selected", "true");
      m_content.append(createStartTag("option", optParams, false));
      m_content.append(displayVal);
      m_content.append(createEndTag("option"));    
	}
	
	/**
	 * Handles remote enumeration options. The service must return a JSON array
	 * with JSONObjects that have a &quot;value&quot; and a &quot;display_value&quot; property,
	 * with the &quot;display_value&quot; option being optional.
	 * @param urlString
	 * @param defaultVal
	 */
	@SuppressWarnings("unchecked")
   private void handleRemoteOptions(String urlString, String defaultVal)
   {	
	   String rawurl = urlString.substring(5);
	   String url = rawurl;	  
	   
	   try
       {
         if (!rawurl.startsWith("http"))
         {
            String sep = rawurl.indexOf('?') == -1 ? "?" : "&";
            URL lUrl = new URL(m_serverscheme, m_servername,
               m_serverport, rawurl);
            url = lUrl.toString();
         }
       
         int index = url.indexOf('@');
         if (index != -1)
         {
             String replaceField = url.substring(index + 1);
             replaceField = replaceField.substring(0, replaceField.indexOf('@'));
             String fieldValue = getFieldValue(m_userPrefs.get(replaceField));
             url = url.replace('@' + replaceField + '@', PSURLEncoder.encodePath(fieldValue));
         }
                  
         String result = makeJSONGetRequest(url);
         JSONParser parser=new JSONParser();
         Object res = null;
         try
         {
            res = parser.parse(new StringReader(result));
            if(isRemoteEnumValJsonValid(res))
            {
               JSONObject jobj = (JSONObject)res;
               Object temp = ((JSONObject)jobj.get("EnumVals")).get("entries");
               if(temp == null)
                  return;
               JSONArray arr = null;
               if(temp instanceof JSONObject)
               {
                  arr = new JSONArray();
                  arr.add(temp);
               }
               else
               {
                  arr = (JSONArray)temp;
               }
               Iterator it = arr.iterator();
               while(it.hasNext())
               {
                  Object obj = it.next();
                  if(obj instanceof JSONObject)
                  {
                     JSONObject entry = (JSONObject)obj;
                     String val = (String)entry.get("value");
                     String displayVal = (String)entry.get("display_value");
                     if(val != null && val.length() > 0)
                        addOption(val, displayVal, defaultVal);
                  }
                  else
                  {
                     throw new IOException("Invalid json data format");
                  }
               }
            }
            else
            {
               throw new IOException("Invalid json data format");
            }
         }
         catch(Exception e)
         {
			 log.error(e.getMessage());
			 log.debug(e.getMessage(), e);
            throw new IOException("Problem retrieving or parsing data.");   
         }
      }
      catch (IOException e)
      {
		  log.error(e.getMessage());
		  log.debug(e.getMessage(), e);
      }
	}
	
	/**
    * Add a checkbox form field representing a boolean to the content buffer. Sets as checked if 
    * that default exists.
    * @param fieldname the fieldname for the input field, assumed not <code>null</code>,
    * or empty.
    * @param pref the JSON preference object, assumed not <code>null</code>.
    */
	private void addBooleanField(String fieldname, String displayName, JSONObject pref){
	   Map<String, String> params = new HashMap<String, String>();
      String dVal = getFieldValue(pref);
      params.put("type", "checkbox");
      params.put("id", replaceTokens(ID_INPUT));
      params.put("name", fieldname);
      params.put("value", "true");
      if(dVal != null && dVal.length() > 0 && dVal.equalsIgnoreCase("true"))
         params.put("checked", "true");
      m_content.append(createStartTag("input", params, true));
      Map<String, String> lParams = new HashMap<String, String>();
      lParams.put("for", fieldname);
      m_content.append(createStartTag("label", lParams, false));
      m_content.append(displayName);
      m_content.append(createEndTag("label"));
      m_content.append(createStartTag("br", null, true));
	}
	
	/**
    * Add a select form field to the content buffer. No option values are added as this is done
    * programatically by the gadget.
    * @param fieldname the fieldname for the input field, assumed not <code>null</code>,
    * or empty.
    * @param pref the JSON preference object, assumed not <code>null</code>.
    */
	private void addListField(String fieldname, JSONObject pref){
	   Map<String, String> params = new HashMap<String, String>();      
      params.put("id", replaceTokens(ID_INPUT));
      params.put("name", fieldname);
      m_content.append(createStartTag("select", params, false));      
      m_content.append(createEndTag("select"));
      m_content.append(createStartTag("br", null, true));	
	}
	
	/**
    * Add a hidden form field to the content buffer. Uses the default value as its value.
    * @param fieldname the fieldname for the input field, assumed not <code>null</code>,
    * or empty.
    * @param pref the JSON preference object, assumed not <code>null</code>.
    */
	private void addHiddenField(String fieldname, JSONObject pref){
	   Map<String, String> params = new HashMap<String, String>();
      String dVal = getFieldValue(pref);
      params.put("type", "hidden");
      params.put("id", replaceTokens(ID_INPUT));
      params.put("name", fieldname);
      if(dVal != null && dVal.length() > 0)
         params.put("value", dVal);
      m_content.append(createStartTag("input", params, true));   
	}
	
	private void addSeparator(String fieldname, String displayname, JSONObject pref)
	{
	   Map<String, String> params = new HashMap<String, String>();
	   String id = replaceTokens(ID_INPUT);
	   String dVal = (String)pref.get("default");
	   params.put("type", "hidden");
      params.put("id", id);
      params.put("name", fieldname);
      params.put("value", "");
      m_content.append(createStartTag("input", params, true)); 
	   if(displayname != null && displayname.length() > 0)
	   {
	      params.clear();
	      params.put("id", id + "_separator_label");
         params.put("class", "perc-gadget-form-separator");
         m_content.append(createStartTag("label", params, false));
         m_content.append(displayname);
         m_content.append(createEndTag("label"));
	   }
	   
	   if(dVal != null && !dVal.equals("@noline"))
	   {
	      params.clear();
	      params.put("id", id + "_separator_line");
         params.put("class", "perc-gadget-form-separator-line");
         m_content.append(createStartTag("hr", params, true));
	   }
	   else
	   {
	      m_content.append(createStartTag("br", null, true));
	   }
	}
	
	/**
	 * Helper method to return the user set value of a field if it exists and
	 * if not, then the default value, or <code>null</code> if neither exists.
	 * @param pref assumed not <code>null</code>.
	 * @return the user or default field value.
	 */
	private String getFieldValue(JSONObject pref){
	   String dVal = (String)pref.get("default");
	   String fName = (String)pref.get("fieldname");
	   String uVal = m_userPrefValues.get("up_" + fName);
	   return (uVal == null || uVal.length() == 0) ? dVal : uVal;	   
	}
	
	/**
	 * Replaces @@MODULEID@@ and @@FIELDIDX@@ tokens with the m_moduleId and
	 * m_fieldCount values respectively. 
	 * @param str the string to do the replacement on, assumed not <code>null</code>.
	 * @return the replaced string, never <code>null</code>, may be empty.
	 */
	private String replaceTokens(String str){
	   str = str.replaceAll("@@MODULEID@@", m_moduleId);
	   str = str.replaceAll("@@FIELDIDX@@", String.valueOf(m_fieldCount));
	   return str;
	}
	
	private boolean isRemoteEnumValJsonValid(Object obj)
	{
	   //TODO: Validate the json format.
	   return true;
	}
	
	/**
    * Sends a JSON string to a specific URL and returns the plain response of the server
    */
   private String makeJSONGetRequest(String url) throws IOException
   {

       HttpClient httpClient = new HttpClient();
             
       GetMethod get = new GetMethod(url);
       get.setRequestHeader("x-shindig-dos", "true");
       get.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
       get.setRequestHeader("Accept", MediaType.APPLICATION_JSON);

       try
       {
          httpClient.executeMethod(get);          
          return get.getResponseBodyAsString();
       }
       finally
       {
          get.releaseConnection();
       }
       
   }   
	/**
	 * The module id value. Never <code>null</code> or empty, generally
	 * set by the ctor.
	 */
	private String m_moduleId = "0";
	
	/**
	 * Map of user persisted preference values for this gadget. Initialized
	 * in the ctor, never <code>null<code> after that. May be empty.
	 */
	private Map<String, String> m_userPrefValues;
	
	/**
	 * The content buffer where the form is built up, never <code>null</code>, may
	 * be empty.
	 */
	private StringBuilder m_content = new StringBuilder();
	
	/**
	 * The current number of fields in the form. The is incremented by {@link #addField(JSONObject)}.
	 */
	private int m_fieldCount = -1;
	
	/**
	 * The name of the server, initialized in the ctor.
	 */
	private String m_servername;
	
	/**
    * The server port, initialized in the ctor.
    */
	private int m_serverport;

	private String m_serverscheme;
	/**
	 * The pssessionid coming from the server request, initialized in the ctor.
	 */
	private String m_pssessionid;
	
	private Map<String, JSONObject> m_userPrefs = new HashMap<String, JSONObject>();
	
	// CONSTANTS
	private static final String INPUTNAME = "m_@@MODULEID@@_up_";
	
	private static final String ID_MAIN_DIV = "m_edit_div@@MODULEID@@";
	private static final String ID_INPUT = "m_@@MODULEID@@_@@FIELDIDX@@";
	private static final String ID_NUMFIELDS = "m_@@MODULEID@@_numfields";
	
	private static final String VAR_HTML = "ig_html_@@MODULEID@@";
	
	private static final String FUNCTIONS = "/* PNC edit window -- leave in this line for testing */" + 
			"function _gel(n) {return document.getElementById ? document.getElementById(n) : null;}" + 
			"/* collect the userprefs into a ampersand-separate string, suitable for appending to a URL */" + 
			"/* note: this doesn\'t gather the locale */" + 
			"function gatherUserprefs_@@MODULEID@@() {var res = \"\";" + 
			"var num_fields = document.getElementById(\"m_@@MODULEID@@_numfields\").value;" + 
			"for (var i = 0; i < num_fields; i++) {var obj = document.getElementById(\"m_@@MODULEID@@_\"+i);" + 
			"/* stop at the end-- should be num_fields, but you never know */" + 
			"if (obj == null) { continue; }" + 
			"/* strip module IDs */" + 
			"var name = obj.name.replace(/^m_.*?_/, \"\");" + 
			"/* separate by ampersands */\r\n" + 
			"if (res != \"\") { res += \"&\"; }" + 
			"res += name + \"=\" + encodeURIComponent(obj.value);}" + 
			"return res;}" + 
			"if (ig_callback_@@MODULEID@@) ig_callback_@@MODULEID@@(ig_html_@@MODULEID@@);";
	
}

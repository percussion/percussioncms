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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webui.gadget.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.percussion.error.PSExceptionUtils;
import com.percussion.security.ToDoVulnerability;
import com.percussion.share.data.PSEnumVals;
import com.percussion.sitemanage.service.impl.PSSiteDataRestService;
import com.percussion.utils.PSSpringBeanProvider;
import com.percussion.workflow.service.impl.PSSteppedWorkflowRestService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


	private static final String CLASS = "class";
	private static final String FIELD_NAME="fieldname";
	private static final String HIDDEN="hidden";
	private static final String VALUE="value";
	private static final String INPUT="input";
	private static final String LABEL="label";
	private static final String SELECT="select";
	private static final String FOR = "for";
	private static final String BR = "br";

	private GadgetSettingsFormServlet servlet;
	private HttpServletRequest request;
	private HttpServletResponse response;


	private static final Logger log = LogManager.getLogger(PSUserPrefFormContent.class);

	protected static final String URL_PREFIX="@url:";
	protected static final String SITELIST_URL="/services/sitemanage/site/choices";
	protected static final String WORKFLOWS_URL="/Rhythmyx/services/workflowmanagement/workflows/";
	protected static final String WORKFLOW_STATE_URL="/Rhythmyx/services/workflowmanagement/workflows/@ssworkflow@/states/choices";

	/**
	 * Initialize and build form content based on passed in user prefs.
	 * @param prefs the JSON preferences object, can not be <code>null</code>.
	 * @param moduleId the moduleId, may be <code>null</code> or empty, in which
	 * @param upValues map of name/values that the user persisted for gadget preferences
	 * for this gadget. Cannot be <code>null</code>.
	 * case it will default to &quote;0&quote;.
	 */
	public PSUserPrefFormContent(List<JSONObject> prefs, String moduleId, Map<String, String> upValues, GadgetSettingsFormServlet gadgetSettingsFormServlet, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if(prefs == null)
			throw new IllegalArgumentException("prefs cannot be null.");
		if(upValues == null)
			throw new IllegalArgumentException("upValues cannot be null.");
		if(moduleId != null && moduleId.length() > 0)
			m_moduleId = moduleId;

		m_userPrefValues = upValues;
		servlet = gadgetSettingsFormServlet;
		request = req;
		response = resp;

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
	private void buildFormContent(List<JSONObject> prefs) throws IOException {
		//Container div
		Map<String, String> params = new HashMap<>();
		params.put("id", replaceTokens(ID_MAIN_DIV));
		params.put(CLASS, "perc-gadget-pref-div");
		params.put("style", "display:none");
		m_content.append(createStartTag("div", params, false));
		m_content.append(createStartTag("table", null, false));
		m_content.append(createStartTag("tr", null, false));
		m_content.append(createStartTag("td", null, false));
		params.clear();
		params.put(CLASS, "perc-gadget-pref-inner-div");
		m_content.append(createStartTag("div", params, false));
		for(JSONObject pref : prefs)
		{
		    m_userPrefs.put((String)pref.get(FIELD_NAME), pref);
		}
		for(JSONObject pref : prefs)
		{
		   addField(pref);      
		}
		Map<String, String> nfParams = new HashMap<>();
		nfParams.put("id", replaceTokens(ID_NUMFIELDS));
		nfParams.put("type", HIDDEN);
		nfParams.put(VALUE, String.valueOf(m_fieldCount + 1));
		m_content.append(createStartTag(INPUT, nfParams, true));
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

	   int c = -2;
	   try(StringReader sr = new StringReader(str))
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
	   	log.error(PSExceptionUtils.getMessageForLog(e));
		log.debug(e);
	      throw new RuntimeException(e);
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
	private void addField(JSONObject pref) throws IOException {
	   m_fieldCount++;
	   String type = (String)pref.get("type");
	   String fn = (String)pref.get(FIELD_NAME);
	   String fieldname = replaceTokens(INPUTNAME) + fn;
	   String displayName = (String)pref.get("displayName");
	   if(displayName == null || displayName.length() == 0)
	      displayName = fn;
	   
	   if(type.equals(HIDDEN))
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
	   Map<String, String> params = new HashMap<>();
	   params.put("for", fieldname);
	   m_content.append(createStartTag(LABEL, params, false));
	   m_content.append(displayName);
	   m_content.append(":");
	   m_content.append(createEndTag(LABEL));
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
	   Map<String, String> params = new HashMap<>();
	   String dVal = getFieldValue(pref);
	   params.put("type", "text");
	   params.put("id", replaceTokens(ID_INPUT));
	   params.put("name", fieldname);
	   if(dVal != null && dVal.length() > 0)
	      params.put(VALUE, dVal);
	   m_content.append(createStartTag(INPUT, params, true));
	   m_content.append(createStartTag("br", null, true));
		
	}
	
	/**
    * Add a select form field to the content buffer. Sets the enumeration values as options
    * to the select field and selects default value if it exists in the list.
    * @param fieldname the fieldname for the input field, assumed not <code>null</code>,
    * or empty.
    * @param pref the JSON preference object, assumed not <code>null</code>.
    */
   private void addEnumField(String fieldname, JSONObject pref) throws IOException {
	   Map<String, String> params = new HashMap<>();
      String dVal = getFieldValue(pref);
      JSONArray vals = (JSONArray)pref.get("orderedEnumValues");
      params.put("id", replaceTokens(ID_INPUT));
      params.put("name", fieldname);
      m_content.append(createStartTag(SELECT, params, false));
      //Add options
	   for (Object o : vals) {
		   JSONObject current = (JSONObject) o;
		   String val = (String) current.get(VALUE);
		   String displayVal = (String) current.get("displayValue");
		   if (val.startsWith("@url:")) {
			   handleRemoteOptions(val,dVal);
		   } else {
			   addOption(val, displayVal, dVal);
		   }

	   }
      m_content.append(createEndTag(SELECT));
      m_content.append(createStartTag(BR, null, true));
	}
	
	private void addOption(String val, String displayVal, String defaultVal)
	{
	   Map<String, String> optParams = new HashMap<>();
      
      if(displayVal == null || displayVal.length() == 0)
         displayVal = val;
      
      optParams.put("value", val);
      if(defaultVal != null && defaultVal.equals(val))
         optParams.put("selected", "true");
      m_content.append(createStartTag("option", optParams, false));
      m_content.append(displayVal);
      m_content.append(createEndTag("option"));    
	}


	protected String getEnumUrlValue(String enumValue){
		if(enumValue.startsWith("@url:"))
			return enumValue.replace("@url:","");
		else
			return enumValue;

	}

	/**
	 * Get a list of available sites.
	 *
	 * "@url:/services/sitemanage/site/choices"
	 * @return Returns a JSON string containing a list of sites
	 */
	protected String getSiteList(){
		PSSiteDataRestService siteSvc = (PSSiteDataRestService)PSSpringBeanProvider.getBean("siteDataRestService");
		String ret = "{}"; //default ot an empty object
		PSEnumVals siteList = siteSvc.getChoices();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

		try {
			return mapper.writeValueAsString(siteList);
		} catch (JsonProcessingException e) {
			log.error("Error converting Site List to JSON. Error: {}",
					PSExceptionUtils.getMessageForLog(e));
		}
		return ret;
	}

	/**
	 * Get a list of available workflows.
	 *
	 * "@url:/Rhythmyx/services/workflowmanagement/workflows/"
	 * @return A json string containing workflows.
	 */
	protected String getWorkflows(){
		PSSteppedWorkflowRestService svc = (PSSteppedWorkflowRestService) PSSpringBeanProvider.getBean("steppedWorkflowRestService");
		String ret = "{}";
		PSEnumVals wfList = svc.getWorflowList();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

		try {
			return mapper.writeValueAsString(wfList);
		} catch (JsonProcessingException e) {
			log.error("Error converting Workflow List to JSON. Error: {}",
					PSExceptionUtils.getMessageForLog(e));
		}
		return ret;
	}

	/**
	 * Get a list of workflow states for the supplied workflow name
	 * "@url:/Rhythmyx/services/workflowmanagement/workflows/@ssworkflow@/states/choices"
	 * @param workflowName Required. A valid workflow name, if null or empty returns {}
	 * @return A json string representing the list of workflows.
	 */
	protected String getWorkflowStates(String workflowName){
		PSSteppedWorkflowRestService svc = (PSSteppedWorkflowRestService) PSSpringBeanProvider.getBean("steppedWorkflowRestService");
		String ret = "{}";
		PSEnumVals wfList = svc.getStatesChoices(workflowName);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

		try {
			return mapper.writeValueAsString(wfList);
		} catch (JsonProcessingException e) {
			log.error("Error converting Workflow State List to JSON for workflow: {}. Error: {}",
					workflowName,
					PSExceptionUtils.getMessageForLog(e));
		}
		return ret;
	}

	/**
	 * Handles remote enumeration options. The service must return a JSON array
	 * with JSONObjects that have a &quot;value&quot; and a &quot;display_value&quot; property,
	 * with the &quot;display_value&quot; option being optional.
	 * @param defaultVal
	 */
	//TODO: If this is an @url param, we need to validate that the URL is safe.
	//NOTE: I am not sure why we need to call the RPC servlet here.  We should just be able to call the service
	//directly as these urls are always with the same web app.
	@ToDoVulnerability
   private void handleRemoteOptions(String enumValue,String defaultVal) throws IOException {

   	  if(enumValue != null && !StringUtils.isEmpty(enumValue)) {
		 	  String url = getEnumUrlValue(enumValue);
		  try {
		  String result;
		  switch(url){
			  case SITELIST_URL:
			  	result = getSiteList();
			  	break;
			  case WORKFLOWS_URL:
			  	result = getWorkflows();
			  	break;
			  case WORKFLOW_STATE_URL:
			  	result=getWorkflowStates("");
			  	break;
			  default:
			  	return;
		  }

		  if(result == null || StringUtils.isEmpty(result)){
		    	log.debug("No results returned for remote options.");
		    	result = "{}";
		  }

			  JSONParser parser = new JSONParser();
			  Object res = null;

				  res = parser.parse(new StringReader(result));
				  if (isRemoteEnumValJsonValid(res)) {
					  JSONObject jobj = (JSONObject) res;

					  if(jobj==null)
					  	return;

					  Object temp = ((JSONObject) jobj.get("EnumVals")).get("entries");
					  if (temp == null)
						  return;
					  JSONArray arr = null;
					  if (temp instanceof JSONObject) {
						  arr = new JSONArray();
						  arr.add(temp);
					  } else {
						  arr = (JSONArray) temp;
					  }
					  for (Object obj : arr) {
						  if (obj instanceof JSONObject) {
							  JSONObject entry = (JSONObject) obj;
							  String val = (String) entry.get("value");
							  String displayVal = (String) entry.get("display_value");
							  if (val != null && val.length() > 0)
								  addOption(val, displayVal, defaultVal);
						  } else {
							  throw new IOException("Invalid json data format");
						  }
					  }
				  } else {
					  throw new IOException("Invalid json data format");
				  }
			  } catch (ParseException e) {
			  log.error(PSExceptionUtils.getMessageForLog(e));
			  log.debug(e);
		  }
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
	   Map<String, String> params = new HashMap<>();
      String dVal = getFieldValue(pref);
      params.put("type", "checkbox");
      params.put("id", replaceTokens(ID_INPUT));
      params.put("name", fieldname);
      params.put("value", "true");
      if(dVal != null && dVal.equalsIgnoreCase("true"))
         params.put("checked", "true");
      m_content.append(createStartTag(INPUT, params, true));
      Map<String, String> lParams = new HashMap<>();
      lParams.put(FOR, fieldname);
      m_content.append(createStartTag(LABEL, lParams, false));
      m_content.append(displayName);
      m_content.append(createEndTag(LABEL));
      m_content.append(createStartTag(BR, null, true));
	}
	
	/**
    * Add a select form field to the content buffer. No option values are added as this is done
    * programatically by the gadget.
    * @param fieldname the fieldname for the input field, assumed not <code>null</code>,
    * or empty.
    * @param pref the JSON preference object, assumed not <code>null</code>.
    */
	private void addListField(String fieldname, JSONObject pref){
	   Map<String, String> params = new HashMap<>();
      params.put("id", replaceTokens(ID_INPUT));
      params.put("name", fieldname);
      m_content.append(createStartTag(SELECT, params, false));
      m_content.append(createEndTag(SELECT));
      m_content.append(createStartTag(BR, null, true));
	}
	
	/**
    * Add a hidden form field to the content buffer. Uses the default value as its value.
    * @param fieldname the fieldname for the input field, assumed not <code>null</code>,
    * or empty.
    * @param pref the JSON preference object, assumed not <code>null</code>.
    */
	private void addHiddenField(String fieldname, JSONObject pref){
	   Map<String, String> params = new HashMap<>();
      String dVal = getFieldValue(pref);
      params.put("type", HIDDEN);
      params.put("id", replaceTokens(ID_INPUT));
      params.put("name", fieldname);
      if(dVal != null && dVal.length() > 0)
         params.put("value", dVal);
      m_content.append(createStartTag("input", params, true));   
	}
	
	private void addSeparator(String fieldname, String displayname, JSONObject pref)
	{
	   Map<String, String> params = new HashMap<>();
	   String id = replaceTokens(ID_INPUT);
	   String dVal = (String)pref.get("default");
	   params.put("type", HIDDEN);
      params.put("id", id);
      params.put("name", fieldname);
      params.put("value", "");
      m_content.append(createStartTag("input", params, true)); 
	   if(displayname != null && displayname.length() > 0)
	   {
	      params.clear();
	      params.put("id", id + "_separator_label");
         params.put(CLASS, "perc-gadget-form-separator");
         m_content.append(createStartTag(LABEL, params, false));
         m_content.append(displayname);
         m_content.append(createEndTag(LABEL));
	   }
	   
	   if(dVal != null && !dVal.equals("@noline"))
	   {
	      params.clear();
	      params.put("id", id + "_separator_line");
         params.put(CLASS, "perc-gadget-form-separator-line");
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
	   String fName = (String)pref.get(FIELD_NAME);
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
	   str = str.replace("@@MODULEID@@", m_moduleId);
	   str = str.replace("@@FIELDIDX@@", String.valueOf(m_fieldCount));
	   return str;
	}
	
	private boolean isRemoteEnumValJsonValid(Object obj)
	{
	   return obj != null;
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

	
	private Map<String, JSONObject> m_userPrefs = new HashMap<>();
	
	// CONSTANTS
	private static final String INPUTNAME = "m_@@MODULEID@@_up_";
	
	private static final String ID_MAIN_DIV = "m_edit_div@@MODULEID@@";
	private static final String ID_INPUT = "m_@@MODULEID@@_@@FIELDIDX@@";
	private static final String ID_NUMFIELDS = "m_@@MODULEID@@_numfields";
	
	private static final String VAR_HTML = "ig_html_@@MODULEID@@";
	
	private static final String FUNCTIONS = "/* PNC edit window -- leave in this line for testing */" + 
			"function _gel(n) {return document.getElementById ? document.getElementById(n) : null;}" + 
			"/* collect the userprefs into a ampersand-separate string, suitable for appending to a URL */" + 
			"/* note: this doesn't gather the locale */" +
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

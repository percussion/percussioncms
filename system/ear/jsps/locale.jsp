<%@page language="java" contentType="application/json;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="com.percussion.i18n.PSLocaleManager" %>
<%@ page import="com.percussion.i18n.PSLocale"%>

<%
    PSLocaleManager locManager = PSLocaleManager.getInstance();
    Iterator<PSLocale> locales = locManager.getLocales();
    JSONObject activeLocalesJson = new JSONObject();
    JSONArray localeJsonArray = new JSONArray();
    while (locales.hasNext()){
        PSLocale locale = locales.next();
        JSONObject localeJson = new JSONObject();
        localeJson.put("localecode", locale.getName());
        localeJson.put("localedisplayname", locale.getDisplayName());
        localeJsonArray.put(localeJson);
    }
    activeLocalesJson.put("activelocales", localeJsonArray);
    out.write(activeLocalesJson.toString());
%>
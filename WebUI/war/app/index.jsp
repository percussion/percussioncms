<%@ page import="com.percussion.maintenance.service.impl.PSMaintenanceManager,com.percussion.pathmanagement.data.PSPathItem,com.percussion.pathmanagement.service.impl.PSPathService" %>
<%@ page import="com.percussion.role.service.impl.PSRoleService,com.percussion.sitemanage.data.PSSiteSummary" %>
<%@ page import="com.percussion.sitemanage.service.impl.PSSiteDataService" %>
<%@ page import="com.percussion.user.data.PSCurrentUser" %>
<%@ page import="com.percussion.user.service.impl.PSUserService" %>

<%@ page import="com.percussion.utils.PSSpringBeanProvider" %>
<%@ page import="com.percussion.utils.container.IPSConnector" %>
<%@ page import="com.percussion.utils.container.PSContainerUtilsFactory" %>

<%@ page import="com.percussion.widgetbuilder.service.PSWidgetBuilderService" %>
<%@ page import="org.apache.commons.lang.ArrayUtils"  %>

<%@ page import="org.json.JSONArray" %>
<%@ page import="javax.servlet.http.Cookie" %>

<%@ page import="java.io.BufferedReader" %>

<%@ page import ="java.io.IOException" %>

<%@ page import=" java.io.InputStream" %>

<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.percussion.server.PSServer" %>

<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
    setCurrentUserInfo(request, response);
    setSitesInfo(request, response);
    setWidgetBuilderActiveInfo(request, response);
    String linkback = request.getParameter("perc_linkback_id");

    String view = request.getParameter("view");
    String site = request.getParameter("site");
    String path = request.getParameter("path");
    String mode = request.getParameter("mode");
    String popuppage = request.getParameter("popuppage");
    String memento = request.getParameter("memento");
    String debug = request.getParameter("debug");
    // The default view to use if not specified
    String defaultView = getDefaultView(request, response);


    String proxyURL ="";
    if(PSServer.isRequestBehindProxy(request)) {
        proxyURL = PSServer.getProxyURL(request,true);
    }

    // Set attribute indicating  we have gone through the dispatcher.
    request.setAttribute("dispatched", "true");

    // List of views and their pages
    Map<String, String> views = new HashMap<String, String>();
    views.put("editAsset", "editAsset.jsp");
    views.put("dash", "dashboard.jsp");
    views.put("design", "admin.jsp");
    views.put("arch", "siteArchitecture.jsp");
    views.put("editor", "webmgt.jsp");
    views.put("publish", "publish.jsp");
    views.put("workflow", "adminWorkflow.jsp");
    views.put("editTemplate", "editTemplate.jsp");
    views.put("widgetbuilder", "widgetBuilder.jsp");
    views.put("home", "home.jsp");


    // List of views requiring admin role
    String[] adminViews = new String[]{
            "design",
            "arch",
            "publish",
            "workflow",
            "widgetbuilder"
    };

    // List of views requiring designer role
    String[] designerViews = new String[]{
            "design",
            "arch",
            "publish",
            "widgetbuilder"
    };

    boolean isAdminView = (view != null && ArrayUtils.contains(adminViews, view));
    boolean isDesignerView =  (view != null && ArrayUtils.contains(designerViews, view));

    if(isAdminView && !(Boolean)request.getAttribute(IS_ADMIN_KEY))
    {
        if (isDesignerView)
        {
            if (!(Boolean)request.getAttribute(IS_DESIGNER_KEY))
                view = null; //reset view sending user to default view
        }
        else
        {
            // no designer access to workflow view, send to default view
            view = null;
        }
    }



    String forwardTo = MAINT_ERROR_PAGE_URL;
    // Add the default view and redirect so it shows up in the url
    if (hasMaintenanceFailed(request, response))
    {
        response.sendRedirect(MAINT_ERROR_PAGE_URL);
    }
    else if (isMaintenanceInProgress(request, response))
    {
        response.sendRedirect(MAINT_PAGE_URL);
    }
    else if(view == null)
    {
        Enumeration paramNames = request.getParameterNames();
        StringBuilder buff = new StringBuilder();
        int count = 0;
        while(paramNames.hasMoreElements())
        {
            String key = (String)paramNames.nextElement();
            String value = request.getParameter(key);
            System.out.println(value);
            if(key.equals("view"))
                continue;
            buff.append(count == 0 ? "" : "&");
            buff.append(key);
            buff.append("=");
            buff.append(value);
            count++;

        }


        String sep = buff.length() == 0 ? "" : "&";
        String url = proxyURL+"/cm/app/?" + buff.toString() + sep + "view=" + defaultView;
        response.setHeader( "Pragma", "no-cache" );
        response.setHeader( "Cache-Control", "no-cache" );
        response.setDateHeader( "Expires", 0 );
        response.sendRedirect(url);
    }
    else if(view.equals("popup") && popuppage != null)
    {
        String url = proxyURL+"/cm/app/popups/" + popuppage;
        response.sendRedirect(url);
    }
    else if(view.equals("editor") && linkback != null)
    {
        String url = proxyURL+"/cm/app/?view=editor";
        Map params = getItemEditorInfo(request,response);
        for (Object key : params.keySet())
        {
            url += "&" + key.toString() + "=" + params.get(key);
        }
        response.sendRedirect(url);
    }
    else
    {
        String temp = views.get(view);
        if(temp != null)
            forwardTo = temp;

        pageContext.forward(forwardTo);
    }
%>
<%-- Define methods --%>
<%!

    protected boolean isMaintenanceInProgress(HttpServletRequest request, HttpServletResponse response) throws JspException
    {
        try
        {
            PSMaintenanceManager maintenanceManager = (PSMaintenanceManager) PSSpringBeanProvider.getBean("maintenanceManager");
            return maintenanceManager.isWorkInProgress();
        }
        catch(Exception e)
        {
            throw new JspException(e);
        }
    }
    protected String getDefaultView(HttpServletRequest request, HttpServletResponse response) throws JspException
    {
        try
        {
            PSRoleService roleService = (PSRoleService) PSSpringBeanProvider.getBean("roleService");
            String uhp = roleService.getUserHomepage();
            String view = "home";
            if(uhp.equals("Dashboard")){
                view = "dash";
            }
            else if(uhp.equals("Editor")){
                view = "editor";
            }
            return view;
        }
        catch(Exception e)
        {
            throw new JspException(e);
        }
    }

    protected boolean hasMaintenanceFailed(HttpServletRequest request, HttpServletResponse response) throws JspException
    {
        try
        {
            PSMaintenanceManager maintenanceManager = (PSMaintenanceManager) PSSpringBeanProvider.getBean("maintenanceManager");
            return maintenanceManager.hasFailures();
        }
        catch(Exception e)
        {
            throw new JspException(e);
        }
    }

    /**
     * Retrieves and sets user info in the request and in a session cookie.
     * @param request the servlet request, assumed not <code>null</code>.
     * @param response the servlet response, assumed not <code>null</code>.
     */
    protected void setCurrentUserInfo(HttpServletRequest request, HttpServletResponse response) throws JspException
    {
        String name = null;
        Boolean isAdmin = Boolean.FALSE;
        Boolean isDesigner = Boolean.FALSE;
        Boolean isAccessibilityUser = Boolean.FALSE;

        try
        {
            PSUserService userService = (PSUserService) PSSpringBeanProvider.getBean("userService");
            PSCurrentUser user = userService.getCurrentUser();

            name = user.getName();

            isAccessibilityUser = user.isAccessibilityUser();
            isAdmin = user.isAdminUser();
            isDesigner = user.isDesignerUser();


            List<String> roles  = user.getRoles();
            if(roles == null)
                  roles = new ArrayList<String>();

            request.setAttribute(CURRENT_USER_NAME_KEY, name);
            request.setAttribute(CURRENT_USER_ROLES_KEY, roles.toString());
            request.setAttribute(IS_ADMIN_KEY, isAdmin);
            request.setAttribute(IS_DESIGNER_KEY, isDesigner);
        } catch (Exception e) {
            throw new JspException(e);
        }

        setCookie(request, response, "perc_userName", name);
        setCookie(request, response, "perc_isAdmin", isAdmin.toString());
        setCookie(request, response, "perc_isDesigner", isDesigner.toString());
        setCookie(request, response, "perc_isAccessibilityUser", isAccessibilityUser.toString());
    }

    private void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue) {
        String secure = "";
        if (request.isSecure())
            secure = " Secure;";
        response.addHeader("Set-Cookie", cookieName + "=" + cookieValue + "; SameSite=Strict;" + secure);
    }


    /**
     * Retrieves and sets information about item editor.
     * @param request the servlet request, assumed not <code>null</code>.
     * @param response the servlet response, assumed not <code>null</code>.
     */
    protected Map getItemEditorInfo(HttpServletRequest request, HttpServletResponse response) throws JspException {
        Map urlParams = new HashMap();
        try
        {
            PSPathService pathService = (PSPathService) PSSpringBeanProvider.getBean("pathService");
            PSPathItem item = pathService.findById(request.getParameter("perc_linkback_id"));

            Object temp = item.getName();
            String pageName = temp != null?temp.toString():"";
            List fpaths = item.getFolderPaths();
            String path = fpaths != null && !fpaths.isEmpty()?fpaths.get(0) + "/" + pageName:"";
            path = path.replace("//Sites/","/Sites/");
            String siteName = path.replace("/Sites/","").split("/")[0];
            path = URLEncoder.encode(path,"UTF-8");
            temp = item.getId();
            String pageId = temp != null?temp.toString():"";
            urlParams.put("site", siteName);
            urlParams.put("mode", "readonly");
            urlParams.put("id", pageId);
            urlParams.put("name", pageName);
            urlParams.put("path", path);
            urlParams.put("pathType", "page");
        }
        catch(Exception e)
        {
            //TODO: I18N below
            urlParams.put("warningMessage", "The page you are attempting to reach, does not exist in the CMS.");
        }
        return urlParams;
    }

    /**
     * Retrieves and sets information about sites.
     * @param request the servlet request, assumed not <code>null</code>.
     * @param response the servlet response, assumed not <code>null</code>.
     */
    protected void setSitesInfo(HttpServletRequest request, HttpServletResponse response) throws JspException
    {
        try
        {
            PSSiteDataService siteService = (PSSiteDataService) PSSpringBeanProvider.getBean("siteDataService");
            List<PSSiteSummary> sites = siteService.findAll();
            JSONArray siteArray = new JSONArray(sites);
            boolean hasSites = siteArray.length() > 0;
            request.setAttribute(HAS_SITES_KEY, hasSites);
        }
        catch(Exception e)
        {
            throw new JspException(e);
        }
    }

    protected void setWidgetBuilderActiveInfo(HttpServletRequest request, HttpServletResponse response) throws JspException
    {
        try
        {
            PSWidgetBuilderService widgetBuilderService = (PSWidgetBuilderService) PSSpringBeanProvider.getBean("widgetBuilderService");
            request.setAttribute(IS_WIDGET_BUILDER_ACTIVE, new Boolean(widgetBuilderService.isWidgetBuilderEnabled()).toString());
        }
        catch(Exception e)
        {
            throw new JspException(e);
        }
    }

    /**
     * Make a get request to the server and get back the response code
     * @param theUrl url string, assumed not <code>null</code> or empty.
     * @param request the http request object, assumed not <code>null</code>.
     * @return The response code
     * @throws IOException upon any error.
     */
    protected int getResponseCode(String theUrl, HttpServletRequest request) throws IOException
    {
        StringBuilder rUrl = new StringBuilder(theUrl);
        rUrl.append(theUrl.indexOf("?") == -1 ? "?" : "&");
        rUrl.append(PSSESSIONID);
        rUrl.append("=");
        rUrl.append(getPSSessionId(request));

        IPSConnector connector = PSContainerUtilsFactory.getInstance().getConnectorInfo().getHttpConnector().get();
        URL url = new URL("http",
                connector.getCallbackHost(),
                connector.getPort(),
                rUrl.toString());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        try
        {
            conn.connect();
            return conn.getResponseCode();
        }
        finally
        {
            if(conn != null)
                conn.disconnect();
        }
    }

    /**
     * Make a get request to the server.
     * @param theUrl url string, assumed not <code>null</code> or empty.
     * @param request the http request object, assumed not <code>null</code>.
     * @throws IOException upon any error.
     */
    protected String makeRequest(String theUrl, HttpServletRequest request)
            throws IOException
    {

        StringBuilder rUrl = new StringBuilder(theUrl);
        rUrl.append(theUrl.indexOf("?") == -1 ? "?" : "&");
        rUrl.append(PSSESSIONID);
        rUrl.append("=");
        rUrl.append(getPSSessionId(request));
        IPSConnector connector = PSContainerUtilsFactory.getInstance().getConnectorInfo().getHttpConnector().get();

        URL url = new URL("http",
                connector.getCallbackHost(),
                connector.getPort(),
                rUrl.toString());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        try
        {
            conn.connect();
            InputStream in = (InputStream)conn.getContent();
            String content = convertStreamToString(in);
            return content;
        }
        finally
        {
            if(conn != null)
                conn.disconnect();
        }
    }

    /**
     * Retrieve the pssessionid value from the request header.
     * @param request the request assumed not <code>null</code>.
     * @return the pssessionid value or <code>null</code> if not found.
     */
    protected String getPSSessionId(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies)
        {
            if(cookie.getName().equals(PSSESSIONID))
                return cookie.getValue();
        }
        return null;
    }


    /**
     * Converts an input stream to a utf-8 string.
     * @param is the input stream, assumed not <code>null</code>.
     */
    private  String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        if (is != null)
        {
            StringBuilder sb = new StringBuilder();
            String line;

            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    //Constants
    private static final String PSSESSIONID = "pssessionid";
    private static final String CURRENT_USER_NAME_KEY = "currentUserName";
    private static final String CURRENT_USER_ROLES_KEY = "currentUserRoles";
    private static final String HAS_SITES_KEY = "hasSites";
    private static final String IS_ADMIN_KEY = "isAdmin";
    private static final String IS_DESIGNER_KEY = "isDesigner";
    private static final String ADMIN_ROLE = "Admin";
    private static final String IS_ACCESSIBILITY_USER = "isAccessibilityUser";
    private static final String IS_WIDGET_BUILDER_ACTIVE = "isWidgetBuilderActive";
    private static final String MAINT_PAGE_URL = "/maintenance.jsp";
    private static final String MAINT_ERROR_PAGE_URL = "/maintenance-errors.jsp";

    //Bad Developers ... NO Coffee for you....
    //Don't ever do this again... calling HTTP internally causes port exhaustion

    //private static final String CURRENT_USER_ROLES_URL = "/Rhythmyx/services/user/user/current";
    //private static final String SITES_URL = "/Rhythmyx/services/sitemanage/site";
    //private static final String PATH_ITEM_URL = "/Rhythmyx/services/pathmanagement/path/item/id/";
    //private static final String WIDGET_BUILDER_URL = "/Rhythmyx/services/widgetmanagement/widgetbuilder/active";

    private static final String MAINT_STATUS_SERVER_URL = "/Rhythmyx/services/maintenance/manager/status/server";
    private static final String MAINT_STATUS_PROC_URL = "/Rhythmyx/services/maintenance/manager/status/process";

%>

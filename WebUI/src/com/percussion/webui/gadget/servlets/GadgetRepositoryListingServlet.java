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

import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * This servlet is used to retrieve a listing of gadgets from the gadget repository.  For each gadget, the following
 * information returned will be returned: name, description, url, icon url.
 */
public class GadgetRepositoryListingServlet extends HttpServlet
{

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(
     *    javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings({"unused", "unchecked"})
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        String type = req.getParameter("type");
        if(type == null || type.trim().length() < 1)
            type="All";

        resp.setContentType("application/json");


        PrintWriter out = resp.getWriter();
        List gadgets = new JSONArray();
        File root = new File(gadgetsRoot.getPath());

        File[] gadgetFiles = root.listFiles();
        try
        {
            for (File gadgetFile : gadgetFiles)
            {
                if (!gadgetFile.isDirectory())
                {
                    // only concerned with directories
                    continue;
                }

                JSONObject gadget = null;
                File[] gadgetConfigFiles = gadgetFile.listFiles();
                try
                {
                    for (File gadgetConfigFile : gadgetConfigFiles)
                    {
                        if (gadgetConfigFile.isDirectory())
                        {
                            // only concerned with files
                            continue;
                        }

                        if (gadgetConfigFile.getName().endsWith(".xml"))
                        {
                            // try to load the gadget
                            gadget = loadGadget(gadgetConfigFile);
                            if (gadget != null)
                            {
                                // the gadget is loaded
                                break;
                            }
                        }
                    }
                }
                catch (NullPointerException e)
                {
                }
                if (gadget != null && (type.equalsIgnoreCase("All") || type.equalsIgnoreCase(gadget.get("type").toString())))
                {
                    gadgets.add(gadget);
                }
            }
        }
        catch(NullPointerException e)
        {
        }
        Collections.sort(gadgets, gComp);

        JSONObject gadgetListing = new JSONObject();
        gadgetListing.put("Gadget", gadgets);
        out.println(gadgetListing.toString());
    }

    /**
     * Loads a gadget from the specified configuration file.
     *
     * @param config the gadget configuration file, assumed not <code>null</code>.
     *
     * @return the gadget as a <code>JSONObject</code> object.  May be <code>null</code> if the gadget could not be
     * loaded.
     *
     * <p>
     * The format of the returned object is as follows:
     * <p>
     * {"name":"The gadget name",
     *  "description":"The gadget description.",
     *  "url":"/cm/gadgets/repository/MyGadget/MyGadget.xml",
     *  "iconUrl":"/cm/gadgets/repository/MyGadget/images/MyGadgetIcon.png"}
     */
    @SuppressWarnings("unchecked")
    private JSONObject loadGadget(File config)
    {
        JSONObject gadget = null;

        FileInputStream fin = null;
        try
        {
            fin = new FileInputStream(config);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(fin, false);
            NodeList modulePrefs = doc.getElementsByTagName("ModulePrefs");
            if (modulePrefs.getLength() > 0)
            {
                Element modulePref = (Element) modulePrefs.item(0);
                gadget = new JSONObject();
                gadget.put("name", modulePref.getAttribute("title"));
                gadget.put("type", getGadgetType(modulePref.getAttribute("title")));
                gadget.put("category", modulePref.getAttribute("category"));
                String adminOnly = modulePref.getAttribute("adminOnly");
                gadget.put("adminOnly", adminOnly != null &&
                        (adminOnly.equalsIgnoreCase("true") || adminOnly.equalsIgnoreCase("yes")));
                gadget.put("description", modulePref.getAttribute("description"));

                String path = config.getCanonicalPath().replaceAll("\\\\", "/");
                String absRootPath = gadgetsRoot.getCanonicalPath().replaceAll("\\\\", "/");
                String url = path.replace(absRootPath + "/","");
                gadget.put("url", GADGETS_BASE_URL + url);

                String configParentPath = config.getParentFile().getCanonicalPath().replaceAll("\\\\", "/");
                String iconBaseUrl = configParentPath.replace(absRootPath + "/","");
                gadget.put("iconUrl", GADGETS_BASE_URL + iconBaseUrl + '/' + modulePref.getAttribute("thumbnail"));
            }
        }
        catch (Exception e)
        {
            System.err.println("Failed to load gadget from file : " + config.getAbsolutePath());
            e.printStackTrace();
        }
        finally
        {
            if (fin != null)
            {
                try
                {
                    fin.close();
                }
                catch (IOException e)
                {
                }
            }
        }

        return gadget;
    }

    /**
     * Used for sorting json representations of gadgets.  Gadgets will be sorted alphabetically by name, case-sensitive.
     * It is assumed that each json respresentation will have a name field.
     */
    public class GadgetComparator implements Comparator<JSONObject>
    {
        public int compare(JSONObject obj1, JSONObject obj2)
        {
            return ((String) obj1.get("name")).compareTo((String) obj2.get("name"));
        }
    }

    /**
     * Helper method to get the gadget type for the supplied gadget name. If the
     * gadgetTypeMap is <code>null</code>, then initializes it by loading
     * GadgetRegistry.xml. If the supplied gadget is not a registered gadget
     * then returns the type as "Custom".
     *
     * @param gadgetName The name of the gadget for which the type needs to be
     *            found, assumed not blank.
     * @return The gadget type, never <code>null</code>, will be "Custom" if the
     *         gadget is not found in the registry.
     */
    private String getGadgetType(String gadgetName)
    {
        // Load the map if needed
        if (gadgetTypeMap == null)
        {
            gadgetTypeMap = loadGadgetTypeMap();
        }

        String gadgetType = gadgetTypeMap.get(gadgetName);
        if (gadgetType == null)
            gadgetType = "Custom";
        return gadgetType;
    }

    /**
     * Helper method that loads the GadgetRegistry.xml and creates a map of gadget name as key and
     * gadget type as value.
     * @return Map of gadget name and type, never <code>null</code> may be empty.
     */
    private Map<String, String> loadGadgetTypeMap()
    {
        Map<String, String> gadTypeMap = new HashMap<String, String>();
        InputStream in = null;
        in = this.getClass().getClassLoader()
                .getResourceAsStream("com/percussion/webui/gadget/servlets/GadgetRegistry.xml");
        if(in == null)
        {
            System.err.println("Gadget registry file is missing in gadgets jar.");
            return gadTypeMap;
        }
        try
        {
            Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            NodeList groupElems = doc.getElementsByTagName("group");
            for (int i = 0; i < groupElems.getLength(); i++)
            {
                Element groupElem = (Element) groupElems.item(i);
                String groupName = groupElem.getAttribute("name");
                NodeList gadgetElems = groupElem.getElementsByTagName("gadget");
                for (int j = 0; j < gadgetElems.getLength(); j++)
                {
                    Element gadgetElem = (Element) gadgetElems.item(j);
                    String gdgName = gadgetElem.getAttribute("name");
                    gadTypeMap.put(gdgName, groupName);
                }
            }
        }
        catch (IOException e)
        {
            // This should not happen as we are reading the file from JAR
            // incase if it happens logging it and returning empty Gadget
            // map.
            System.err.println("Failed to load gadget registry file :");
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            // This should not happen as we are reading the file from JAR
            // incase if it happens logging it and returning empty Gadget
            // map.
            System.err.println("Failed to parse gadget registry file :");
            e.printStackTrace();
        }
        catch (Exception e)
        {
            // This should not happen as we are reading the file from JAR
            // incase if it happens logging it and returning empty Gadget
            // map.
            System.err.println("Failed to parse gadget registry file :");
            e.printStackTrace();
        }
        return gadTypeMap;
    }

    /**
     * The base url for all gadgets.
     */
    private static final String GADGETS_BASE_URL = "/cm/gadgets/repository/";

    /**
     * Used for sorting gadgets.
     */
    private GadgetComparator gComp = new GadgetComparator();

    /**
     * The root directory of all gadgets (i.e., the gadget repository).  Never <code>null</code>.
     */
    private File gadgetsRoot = new File(PSServer.getRxDir() + "/cm/gadgets/repository");

    //Private data variable initialized in getGadgetType method.
    private Map<String,String> gadgetTypeMap = null;

}

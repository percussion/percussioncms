/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.cms;

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PSAuthenticateUserUtils {
    /**
     * Name of user default community properties.
     */
    static public final String SYS_DEFAULTCOMMUNITY = "sys_defaultCommunity";

    /**
     * Name of the internal request to get the community id with a c
     * community name. Requires parameter communityname=value, where value is
     * a valid community name.
     */
    static public final String IREQ_COMMUNITYLOOKUP =
            "sys_commSupport/communityidlookup";
    /**
     * Name of the parameter requires for community id lookup. This
     * paremeter is added when we lookup the community id.
     */
    static public final String COMMUNITYNAME = "communityname";
    /**
     * Name of the element "Community" in the result document of the internal
     * request for user communities.
     */
    static public final String ELEM_COMMUNITY = "Community";

    /**
     * Name of the attribute of the communityid of the element "Community" in
     * the result document of the internal request for user communities.
     */
    static public final String ATTR_COMMID = "commid";
    /**
     * Name of the internal request to get the user communities. This is a
     * standard Rhythmyx resource meant for internal request.
     */
    static public final String IREQ_USERCOMMUNITIES =
            "sys_commSupport/usercommunities";



    /**
     * This method retrieves the list user's role-communities, viz. list of all
     * communities via his role membership.
     * @param request <code>IPSRequestContext</code> object that is available in
     * the extension's process request method, assumed never <code>null</code>.
     * @return list of user communities (community ids) as Java List object never
     * <code>null</code> may be empty.
     *
     */
    private List getUserCommunities(IPSRequestContext request)
            throws Exception
    {
        ArrayList list = new ArrayList();
        // Make an internal request to get the user roles.
        IPSInternalRequest iReq =
                request.getInternalRequest(IREQ_USERCOMMUNITIES);
        Document doc = null;
        try
        {
            iReq.makeRequest();
            doc = iReq.getResultDoc();
        }
        finally
        {
            if(iReq != null)
                iReq.cleanUp();
        }
        NodeList nl = doc.getElementsByTagName(ELEM_COMMUNITY);
        if(nl == null || nl.getLength() < 1)
            return list;

        Element elem = null;
        for(int i=0; i<nl.getLength(); i++)
        {
            elem = (Element)nl.item(i);
            list.add(elem.getAttribute(ATTR_COMMID));
        }
        return list;
    }

    /**
     * This mehod retrieves the community id from
     * "sys_commSupport/communityidlookup" by their community name.
     * @param request <code>IPSRequestContext</code> object that is available in
     * the extension's process request method, assumed never <code>null</code>.
     * @param name Community name, can not be <code>null</null>
     * @return Community id.
     * @throws Exception
     */
    public static String getCommunityId(IPSRequestContext request,String name )
            throws Exception
    {
        //Backup parameters
        Map<String,Object> paramsBackup = request.getParameters();
        Document doc;
        try
        {
            request.setParameter(COMMUNITYNAME,name);
            IPSInternalRequest iReq =
                    request.getInternalRequest(IREQ_COMMUNITYLOOKUP);
            try
            {
                iReq.makeRequest();
                doc = iReq.getResultDoc();
            }
            finally
            {
                if(iReq != null)
                    iReq.cleanUp();
            }
        }
        finally
        {
            //restore parameters
            request.setParameters(paramsBackup);
        }
        NodeList nl = doc.getElementsByTagName(ELEM_COMMUNITY);
        Element elem = null;
        if(null != nl)
            elem = (Element)nl.item(0);
        return elem.getAttribute(ATTR_COMMID);
    }


    /**
     * This method retrieves the value of the given attribute for the user role.
     * If user happens to be in multiple roles the first non empty value is
     * considered
     * @param request <code>IPSRequestContext</code> object that is available in
     * the extension's process request method, assumed never <code>null</code>.
     * @param srcAttrName, Name of the role attribute to retrieve, cannot be
     * <code>null</code>, if <code>null</code> the result will be <code>null</code>.
     * @return value of the given attribute, may be <code>null</code>
     * @throws Exception, if it cannot retrieve tha role
     * attribute for any reason.
     */
    static public String getUserRoleAttribute(IPSRequestContext request,
                                              String srcAttrName )
            throws Exception
    {
        if(srcAttrName == null)
            return null;
        String attrValue = null;
        List roles = request.getSubjectRoles();
        Object role = null;
        List roleAttribs = null;
        PSAttribute attr = null;
        List attrList = null;
        String attrName = null;
        for(int i=0; roles != null && i<roles.size(); i++)
        {
            role = roles.get(i);
            if(role == null)
                continue;
            roleAttribs = request.getRoleAttributes(role.toString().trim());
            for(int j=0; roleAttribs != null && j<roleAttribs.size(); j++)
            {
                attr = (PSAttribute)roleAttribs.get(j);
                if(attr == null)
                    continue;
                attrName = attr.getName();
                if(attrName.equals(srcAttrName))
                {
                    attrList = attr.getValues();
                    if(attrList != null && attrList.size() > 0)
                    {
                        // we take only the first attribute
                        attrValue = attrList.get(0).toString();
                    }
                }
                if(attrValue != null && attrValue.length() > 0)
                    return attrValue;
            }
        }
        return attrValue;
    }

}

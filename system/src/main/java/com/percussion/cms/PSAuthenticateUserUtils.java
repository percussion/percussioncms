/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

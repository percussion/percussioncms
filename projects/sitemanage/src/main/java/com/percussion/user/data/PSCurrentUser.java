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

package com.percussion.user.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlRootElement;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@XmlRootElement(name = "CurrentUser")
@JsonRootName("CurrentUser")
public class PSCurrentUser extends PSUser
{
    private boolean accessibilityUser = false;
    private boolean adminUser = false;
    private boolean designerUser = false;
    private boolean navAdmin = false;
    private boolean userAdmin = false;
    
    public PSCurrentUser()
    {
        super();
    }
    
    public PSCurrentUser(PSUser user)
    {
        setName(user.getName());
        setPassword(user.getPassword());
        setEmail(user.getEmail());
        setProviderType(user.getProviderType());
        setRoles(user.getRoles());
    }
    
    public boolean isAccessibilityUser()
    {
        return accessibilityUser;
    }
    
    public void setAccessibilityUser(boolean isAccessibility)
    {
        accessibilityUser = isAccessibility;
    }
    
    public boolean isAdminUser()
    {
        return adminUser;
    }
    
    public void setAdminUser(boolean isAdmin)
    {
        adminUser = isAdmin;
    }

    /**
     * @param isDesigner
     */
    public void setDesignerUser(boolean isDesigner)
    {
        designerUser = isDesigner;
    }
    
    public boolean isDesignerUser()
    {
        return designerUser;
    }

    public boolean isNavAdmin() {
        return navAdmin;
    }

    public void setNavAdmin(boolean navAdmin) {
        this.navAdmin = navAdmin;
    }

    public boolean isUserAdmin() {
        return userAdmin;
    }

    public void setUserAdmin(boolean userAdmin) {
        this.userAdmin = userAdmin;
    }
}

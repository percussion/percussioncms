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

package com.percussion.security;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;
import java.util.Set;

public class PSNotificationEmailAddress implements Serializable {

    private String email;
    private String sourceSubject;
    private String sourceRoleOrGroup;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSourceSubject() {
        return sourceSubject;
    }

    public void setSourceSubject(String sourceSubject) {
        this.sourceSubject = sourceSubject;
    }

    public void setSourceSubject(Subject sourceSubject) {
        Set<Principal> principles = sourceSubject.getPrincipals();

        StringBuilder combined = new StringBuilder();
        if(principles != null && !principles.isEmpty()){
            for(Principal p : principles){
                if(combined.length()>0)
                    combined.append(",");
                combined.append(p.getName());
            }
        }
        this.sourceSubject = combined.toString();
    }

    public String getSourceRoleOrGroup() {
        return sourceRoleOrGroup;
    }

    public void setSourceRoleOrGroup(String sourceRoleOrGroup) {
        this.sourceRoleOrGroup = sourceRoleOrGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSNotificationEmailAddress)) return false;
        PSNotificationEmailAddress that = (PSNotificationEmailAddress) o;
        return getEmail().equals(that.getEmail()) && Objects.equals(getSourceSubject(), that.getSourceSubject()) && Objects.equals(getSourceRoleOrGroup(), that.getSourceRoleOrGroup());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getSourceSubject(), getSourceRoleOrGroup());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PSNotificationEmailAddress{");
        sb.append("email='").append(email).append('\'');
        sb.append(", sourceSubject='").append(sourceSubject).append('\'');
        sb.append(", sourceRoleOrGroup='").append(sourceRoleOrGroup).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public PSNotificationEmailAddress(String email, String sourceSubject, String sourceRoleOrGroup) {
        this.email = email;
        this.sourceSubject = sourceSubject;
        this.sourceRoleOrGroup = sourceRoleOrGroup;
    }

    public PSNotificationEmailAddress() {
    }
}

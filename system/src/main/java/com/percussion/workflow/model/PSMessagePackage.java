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

package com.percussion.workflow.model;

import com.percussion.security.PSNotificationEmailAddress;

import java.util.List;

/**
 * Light weight class used to contain the following
 * fields for sending to a message queue.  This was implemented
 * to send a  list of unique messages to the executer as one
 * thread as opposed to creating many threads for each e-mail for each
 * recipient.
 *
 * <ol>
 * <li> To email address </li>
 * <li> Email Subject </li>
 * <li> Email Body </li>
 * <li> From email address </li>
 * </ol>
 * @author chriswright
 *
 */
public class PSMessagePackage {
    private String userEmail;
    private String emailToStr;
    private String subj;
    private String emailBody;
    private List<PSNotificationEmailAddress> sourceEmailTo;
    private List<PSNotificationEmailAddress> sourceEmailCC;

    public List<PSNotificationEmailAddress> getSourceEmailTo() {
        return sourceEmailTo;
    }

    public void setSourceEmailTo(List<PSNotificationEmailAddress> sourceEmailTo) {
        this.sourceEmailTo = sourceEmailTo;
    }

    public List<PSNotificationEmailAddress> getSourceEmailCC() {
        return sourceEmailCC;
    }

    public void setSourceEmailCC(List<PSNotificationEmailAddress> sourceEmailCC) {
        this.sourceEmailCC = sourceEmailCC;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public String getEmailToStr() {
        return emailToStr;
    }

    public String getSubj() {
        return subj;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public void setEmailToStr(String emailToStr) {
        this.emailToStr = emailToStr;
    }

    public void setSubj(String subj) {
        this.subj = subj;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

}

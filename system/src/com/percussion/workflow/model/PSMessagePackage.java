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

package com.percussion.workflow.model;

/**
 * Light weight class used to contain the following
 * fields for sending to a message queue.  This was implemented
 * to send a  list of unique messages to the {@link #executor} as one
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

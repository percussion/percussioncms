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
package com.percussion.delivery.email.data;

public interface IPSEmailRequest
{
    public void setToList(String toList);

    public void setCCList(String ccList);

    public void setBCCList(String bccList);

    public void setBody(String bodycontent);

    public void setSubject(String subject);

    public String getToList();

    public String getCCList();

    public String getBCCList();

    public String getBody();

    public String getSubject();

}

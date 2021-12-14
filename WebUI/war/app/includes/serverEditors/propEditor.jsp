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

<% String editorName = request.getParameter("editorName");
if(editorName == null)
editorName = ""; %>
<!----------- Local Editor ------------><% if(editorName.equals("Local")){ %>
<div id = 'perc-local-editor'>
    <div>
        <label>
            <span class='perc-required-field'></span>Folder Location:
        </label>
        <div style="float: left; width:274px">
            <input style ='float:left;margin:0px 3px' type ='radio' name = 'local-folder-path' id ='perc-defaultServer' percName = 'defaultServerFlag' checked ='true'/><span style ='display:block'>Use Percussion web server setup (default)</span>
        </div>
        <div style ='width:315px; float:left'>
            <input style ='float:left;margin:0px 3px' type ='radio' name = 'local-folder-path' percName = 'ownServerFlag' id ='perc-ownServer'/><span style ='display:block'>Use my own web server (exact path)</span>
        </div>
        <div style ='clear:both'>
        </div>
        <input style ='display:block;margin-top:5px;margin-right:0px; background:#cccccc; border:1px solid #cccccc;padding:2px;width:676px;' readonly="readonly" name = 'defaultServer' id = "perc-local-default-location" percName = 'defaultServer' type = 'text' /><input style ='display:block;margin-top:5px; margin-right:0px;width:676px;' id = "perc-local-own-location" name = 'ownServer' percName = 'ownServer' type = 'text' />
    </div>
    <div>
        <label>
            <span class='perc-required-field'></span>Format:
        </label>
        <div style="float: left; width:67px">
            <input style ='float:left;margin:0px 3px' type ='radio' name = 'local-format' id ='perc-format-html' percName = 'HTML' checked ='true'/><span style ='display:block'>HTML</span>
        </div>
        <div style ='width:50px; float:left'>
            <input style ='float:left;margin:0px 3px' type ='radio' name = 'local-format' percName = 'XML' id ='perc-format-xml'/><span style ='display:block'>XML</span>
        </div>
    </div>
    <div style ='clear:both'>
    </div>
</div>
<!----------- Amazon S3 Editor ------------><%}else if(editorName.equals("AMAZONS3")){ %>
<div id = 'perc-amazon-s3-editor'>
    <div>
        <label for="perc-amazon-s3-bucket-location">
            <span class='perc-required-field'></span></>Bucket Name:
        </label>
        <input style ='display:block;margin-bottom:5px;margin-right:0px;width:676px;' name = 'bucketlocation' id='perc-amazon-s3-bucket-location' type='text' percName='bucketlocation'/>
    </div>
    <div style ='clear:both'>
    </div>
    <div style="float: left;">
        <label for="perc-access-key">
            <span class='perc-required-field'></span>Access Key:
        </label>
        <input id='perc-access-key' type='text' name = 'accesskey' percName='accesskey'/>
    </div>
    <div style="float: left;">
        <label for="perc-security-key">
            <span class='perc-required-field'></span>Security Key:
        </label>
        <input id='perc-security-key' name = 'securitykey' class = 'perc-right-col' type='text' percName='securitykey'/>
    </div>
    <div style ='clear:both'>
        <label for="perc-ec2-Region">
            <span class='perc-required-field'></span>Security Key:
        </label>
        <input id='perc-ec2-Region' name = 'region' type='text' percName='region'/>
    </div>
</div>
<!----------- FTP Editor ------------><%}else if(editorName.equals("FTP")){ %>
<div id="perc-define-ftp-server-info" type="sys_error">
    <div>
        <label for="perc-ftp-folder-location">
            <span class='perc-required-field'></span></>Folder Location:
        </label>
        <input style ='display:block;margin-bottom:5px;margin-right:0px;width:676px;' name = 'folder' id='perc-ftp-folder-location' type='text' percName='folder'/>
        <div>
            <input type ='radio' percName = 'defaultServerFlag' name = 'ftp-folder-path' id ='perc-defaultServer' checked ='true' style ='float:left;margin-left:0;margin-top:0;'/><span style ='display:block'>Use Percussion web server setup (default)</span>
        </div>
        <div style ='margin:5px 0px 17px'>
            <input type ='radio' percName = 'ownServerFlag' name = 'ftp-folder-path' id ='perc-ownServer' style ='float:left;margin-left:0;margin-top:0;'/><span style ='display:block'>Use my own web server (exact path)</span>
        </div>
    </div>
    <div style ='clear:both'>
    </div>
    <div style="float: left;">
        <label for="perc-define-ftp-address">
            <span class='perc-required-field'></span>FTP Server IP Address:
        </label>
        <input id='perc-define-ftp-address' type='text' name = 'serverip' percName='serverip'/>
    </div>
    <div style="float: left;">
        <label for="perc-define-ftp-user-input">
            <span class='perc-required-field'></span>User ID:
        </label>
        <input id='perc-define-ftp-user-input' name = 'userid' class = 'perc-right-col' type='text' percName='userid'/>
    </div>
    <div style ='clear:both'>
    </div>
    <div style="float: left;">
        <label for="perc-server-port">
            <span class='perc-required-field'></span>Port:
        </label>
        <input id='perc-server-port' type='text' name = 'port' percName='port'/>
    </div>
    <div style="float: left;">
        <input id="perc-ftp-password-rb" percName="passwordFlag" name = 'passwordkey' style ='float:left; margin-left:-21px' type="radio" checked="true" />
        <label for="perc-ftp-password-rb">
            Password:
        </label>
        <input id = 'perc-ftp-password' class = 'perc-right-col' name = 'password' percName = 'password' type="password" />
    </div>
    <div style ='clear:both'>
    </div>
    <div style="float:left">
        <input id='perc-define-secure-ftp-input' class='datadisplay' percName = 'secure' type='checkbox' />
        <label for='perc-define-secure-ftp-input' style ='display:inline'>
            Secure FTP
        </label>
    </div>
    <div style="float:right">
        <input id="perc-ftp-private-file-key-rb" style ='float:left; margin-left:-21px' percName = 'privateKeyFlag' name="passwordkey" type="radio" />
        <label for="perc-ftp-private-file-key-rb">
            
            Private key file:
        </label>
        <select id="perc-ftp-private-file-key" class = 'perc-right-col' percName = 'privateKey'>
        </select>
    </div>
    <div style ='clear:both'>
    </div>
    <div style="float:left">
        <label>
            <span class='perc-required-field'></span>Format:
        </label>
        <div style="float: left; width:67px">
            <input style ='float:left;margin: 0 3px 0 0;' type ='radio' name = 'ftp-format' id ='perc-format-html' percName = 'HTML' checked ='true'/><span style ='display:block'>HTML</span>
        </div>
        <div style ='width:50px; float:left'>
            <input style ='float:left;margin: 0 3px 0 0;' type ='radio' name = 'ftp-format' percName = 'XML' id ='perc-format-xml'/><span style ='display:block'>XML</span>
        </div>
    </div>
    <div style ='clear:both'>
    </div>
</div>
<!----------- MS SQL Editor || Oracle Editor || MySQL Editor ------------><%}else if(editorName.equals("MSSQL") || editorName.equals("MySQL") || editorName.equals("Oracle")){ %>
<div id="perc-server-details" type="sys_error">
    <div style="float: left;">
        <label for="perc-server-name-input">
            <span class='perc-required-field'></span></>Server Name:
        </label>
        <input id="perc-server-name-input" name = 'server' percName ='server' type='text'/>
    </div>
    <div style="float: left;">
        <label for="perc-server-port">
            <span class='perc-required-field'></span>Port:
        </label>
        <% if(editorName.equals("MSSQL")){ %><input id="perc-server-port" class = 'perc-right-col' value = '1433' type='text' name = 'port' percName='port'/><%} else %>
        <% if(editorName.equals("MySQL")){ %><input id="perc-server-port" class = 'perc-right-col' value = '3306' type='text' name = 'port' percName='port'/><%} else %>
        <% if(editorName.equals("Oracle")){ %><input id="perc-server-port" class = 'perc-right-col' value = '1521' type='text' name = 'port' percName='port'/><%} %>
    </div>
    <div style ='clear:both'>
    </div>
    <div style="float: left;">
        <label for="perc-server-userid">
            <span class='perc-required-field'></span>User ID:
        </label>
        <input id='perc-server-userid' type='text' name = 'userid' percName='userid'/>
    </div>
    <div style="float: left;">
        <label>
            <span class='perc-required-field'></span>
            <% if(editorName.equals("MSSQL") || editorName.equals("MySQL")){ %>Database Name: <%} else if(editorName.equals("Oracle")){ %>SID: <%} %>
        </label>
        <% if(editorName.equals("MSSQL") || editorName.equals("MySQL")){ %><input id='perc-server-database' class = 'perc-right-col' type='text' name = 'database' percName='database'/><%} else if(editorName.equals("Oracle")){ %><input id='perc-server-sid' type='text' class = 'perc-right-col' name = 'sid' percName='sid'/><%} %>
    </div>
    <div style ='clear:both'>
    </div>
    <div style="float: left;">
        <label for="perc-server-password">
            <span class='perc-required-field'></span>
            Password:
        </label>
        <input type="password" id="perc-server-password" name = 'password' percName='password'/>
    </div>
    <div style="float:left">
        <label>
            <% if(editorName.equals("MSSQL")){ %><span class='perc-required-field'></span>Owner: <%} else if(editorName.equals("Oracle")){ %><span class='perc-required-field'></span>Schema: <%} %>
        </label>
        <% if(editorName.equals("MSSQL")){ %><input id='perc-server-owner' class = 'perc-right-col' value = 'dbo' type='text' name = 'owner' percName='owner'/><%} else if(editorName.equals("Oracle")){ %><input id='perc-server-schema' class = 'perc-right-col' type='text' name = 'schema' percName='schema'/><%} %>
    </div>
    <div style ='clear:both'>
    </div>
</div>
<%} %>

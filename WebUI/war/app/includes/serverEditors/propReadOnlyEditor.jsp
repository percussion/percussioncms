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
<div id='perc-publish-header'>
    <div id = 'perc-server-summary'>
        Summary
    </div>
    <div id='perc-publish-button'>
    </div>
</div>
<div id = 'perc-editor-prop'>
    <ul id="perc-editor-prop-container">
	    <!----------- Local Editor ------------><% if(editorName.equals("Local")){ %>
	    <li>
	        <span class = 'perc-prop-name'>Type: </span>
	        <span percName ='type' class = 'perc-pro-value'>File</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Driver: </span>
	        <span percName = 'driver' class = 'perc-pro-value'>Local</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Folder Location: </span>
	        <span percName = 'folder' class = 'perc-pro-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Format: </span>
	        <span percName = 'format' class = 'perc-pro-value'></span>
	    </li>
        <li>
            <span class = 'perc-prop-name'>Ignore Un-modified Assets: </span>
            <span percName = 'ignoreUnModifiedAssets' class = 'perc-pro-value'>All assets will be published.</span>
            <span>&nbsp; <i class="icon-question-sign icon-large fas fa-question"></i>"
                title="Option to only publish assets that have been modified since the last publish."></i>
            </span>
        </li>
        <li style='display:none'>
            <span class = 'perc-prop-name'>Publish Related Items: </span>
            <span percName = 'publishRelatedItems' class = 'perc-pro-value'>Related items will be published during incremental publish.</span>
            <span>&nbsp; <i class="icon-question-sign icon-large fas fa-question"
                title="Option to publish related items to the incremental items list that are in non approved states. The content is approved first and then published."></i>
            </span>
        </li>
	    <!----------- FTP Editor ------------><%}else if(editorName.equals("FTP")){ %>
	    <li>
	        <span class = 'perc-prop-name'>Type: </span>
	        <span percName ='type' class = 'perc-pro-value'>File</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Driver: </span>
	        <span percName = 'driver' class = 'perc-pro-value'>FTP</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Folder Location: </span>
	        <span percName = 'folder' class = 'perc-pro-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>FTP Server IP Address: </span>
	        <span percName = 'serverip' class = 'perc-pro-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Port: </span>
	        <span percName = 'port' class = 'perc-pro-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Format: </span>
	        <span percName = 'format' class = 'perc-pro-value'></span>
	    </li>
        <li>
            <span class = 'perc-prop-name'>Ignore Un-modified Assets: </span>
            <span percName = 'ignoreUnModifiedAssets' class = 'perc-pro-value'>All assets will be published.</span>
            <span>&nbsp; <i class="icon-question-sign icon-large fas fa-question fa-lg"
                title="Option to only publish assets that have been modified since the last publish."></i>
            </span>
        </li>
        <li style='display:none'>
            <span class = 'perc-prop-name'>Publish Related Items: </span>
            <span percName = 'publishRelatedItems' class = 'perc-pro-value'>Related items will be published during incremental publish.</span>
            <span>&nbsp; <i class="icon-question-sign icon-large fas fa-question fa-lg"
                title="Option to publish related items to the incremental items list that are in non approved states. The content is approved first and then published."></i>
            </span>
        </li>
	    <li>
	        <span class = 'perc-prop-name'>Secure FTP: </span>
	        <span percName = 'secure' class = 'perc-pro-value'></span>
	    </li>
        <!----------- Amazon S3 Editor ------------><%}else if(editorName.equals("AMAZONS3")){ %>
        <li>
            <span class = 'perc-prop-name'>Type: </span>
            <span percName ='type' class = 'perc-pro-value'>File</span>
        </li>
        <li>
            <span class = 'perc-prop-name'>Driver: </span>
            <span percName = 'driver' class = 'perc-pro-value'>Amazon S3</span>
        </li>
        <li>
            <span class = 'perc-prop-name'>Bucket Name: </span>
            <span percName = 'bucketlocation' class = 'perc-pro-value'></span>
        </li>
	    <!----------- MS SQL Editor ------------><%}else if(editorName.equals("MSSQL")){ %>
	    <li>
	        <span class = 'perc-prop-name'>Type: </span>
	        <span percName = 'type' class = 'perc-prop-value'>Database</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Driver: </span>
	        <span percName = 'driver' class = 'perc-prop-value'>MS SQL</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Server Name: </span>
	        <span percName = 'server' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Port: </span>
	        <span percName ='port' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Database Name: </span>
	        <span percName ='database' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Owner: </span>
	        <span percName ='owner' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>User ID: </span>
	        <span percName ='userid' class = 'perc-prop-value'></span>
	    </li>
	    <!----------- Oracle Editor ------------><%}else if(editorName.equals("Oracle")){ %>
	    <li>
	        <span class = 'perc-prop-name'>Type: </span>
	        <span percName ='type' class = 'perc-prop-value'>Database</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Driver: </span>
	        <span percName ='driver' class = 'perc-prop-value'>Oracle</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Server Name: </span>
	        <span percName ='server' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Port: </span>
	        <span percName ='port' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>SID: </span>
	        <span percName ='sid' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Schema: </span>
	        <span percName ='schema' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>User ID: </span>
	        <span percName ='userid' class = 'perc-prop-value'></span>
	    </li>
	    <!----------- MySQL Editor ------------><%}else if(editorName.equals("MySQL")){ %>
	    <li>
	        <span class = 'perc-prop-name'>Type: </span>
	        <span percName ='type' class = 'perc-prop-value'>Database</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Driver: </span>
	        <span percName ='driver' class = 'perc-prop-value'>MySQL</span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Server Name: </span>
	        <span percName ='server' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Port: </span>
	        <span percName ='port' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>Database Name: </span>
	        <span percName ='database' class = 'perc-prop-value'></span>
	    </li>
	    <li>
	        <span class = 'perc-prop-name'>User ID: </span>
	        <span percName ='userid' class = 'perc-prop-value'></span>
	    </li>
	    <%} %>
    </ul>
    <div id = "perc-default-server">
        Publish Now Default
    </div>
</div>

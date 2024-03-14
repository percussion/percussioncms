<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>


<%

    String locale = PSRoleUtilities.getUserCurrentLocale();
    String lang = "en";
    if (locale == null) {
        locale = "en-us";
    } else {
        if (locale.contains("-"))
            lang = locale.split("-")[0];
        else
            lang = locale;
    }
    String debug = request.getParameter("debug");
    String status = request.getParameter("status");
    String msgClass = null;
    if (status != null && status.equals("PERC_SUCCESS"))
        msgClass = "perc-success";
    else if (status != null && status.equals("PERC_ERROR"))
        msgClass = "perc-error";
    String message = request.getParameter("message");


%>
<html>
<head>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%= locale %>"></script>
    <script src="/JavaScriptServlet"></script>
    <i18n:settings lang="<%=locale %>" prefixes="perc.ui.revisionDialog." debug="<%= debug %>"/>
    <script src="/cm/jslib/profiles/3x/jquery/jquery-3.6.0.js"></script>
    <script type="text/javascript" src="/cm/services/PercCompareService.js"></script>
    <script type="text/javascript" src="/cm/services/PercRevisionService.js"></script>
    <script type="text/javascript" src="/cm/plugins/perc_path_constants.js"></script>
    <script type="text/javascript" src="/cm/services/PercServiceUtils.js"></script>
    <script type="text/javascript" src="/cm/services/PercPathService.js"></script>
    <script type="text/javascript" src="/cm/widgets/htmldiff.js"></script>
    <script type="text/javascript" src="/cm/plugins/perc_utils.js"></script>
    <style>
        .tab {
            display: none;
        }
        .Page{
            position:absolute;
            width:100%;
            height:100%;
        }

        .perc-title {
            height: 60px;
            background-color: #133c55;
            margin: 6px;
            color: #FFFFFF;
            font-weight: 800;
            font-size: 18pt;
            font-family: 'Open Sans', sans-serif;
            text-align: center;
        }
        .perc-header{
            color: #FFFFFF;
            font-weight: 800;
            font-size: 14pt;
            font-family: 'Open Sans', sans-serif;
            text-align: center;
            padding: 6px;
        }

        .perc-compare-header {
            height: 40px;
            background-color: #133c55;
            color: #FFFFFF;
            margin: 10px;
            font-weight: 800;
            font-size: 14pt;
            font-family: 'Open Sans', sans-serif;
            text-align: center;
            padding: 6px;
        }

        .perc-checkbox-label {
            background-color: white;
            padding: 5px 10px;
            text-align: left;
            display: inline-block;
            font-size: 14px;
            margin: 10px 10px;
            cursor: pointer;
        }

        .tab-label {
            cursor: pointer;
            color: #FFFFFF;
        }

        .tab-selected {
            cursor: pointer;
            color: #FFFFFF;
            text-decoration: underline;
        }

        ins {
            text-decoration: none;
            background-color: #d4fcbc;
        }

        .ins-reg {
            text-decoration: none;
            background-color: #d4fcbc;
        }

        .ins-contrast {
            color: #FFFFFF;
            text-decoration: none;
            background-color: #000000;
        }

        del {
            text-decoration: line-through;
            background-color: #fbb6c2;
            color: #555;
        }

        .del-reg {
            text-decoration: line-through;
            background-color: #fbb6c2;
            color: #555;
        }

        .del-contrast {
            text-decoration: line-through;
            background-color: #000000;
            color: #FFFFFF;
        }

    </style>
</head>
<body>

<div class="container">
    <div class="perc-title" style="z-index: 4990;">
        <i18n:message key="perc.ui.revisionDialog.compareRevision@compareRevisions"/>
    </div>
    <div width="100%">
        <section>

            <div id="leftSide" width="10%" style="float:left;" >
                <section class="left">
                    <div class="perc-header">
                        <i18n:message key="perc.ui.revisionDialog.selectRevToCompare@selectRevToCompare"/>
                    </div>
                    <div style="padding:20px">
                        <table style="padding:20px" height="100%">
                            <tr>
                                <td class="headercell" >
                                    <table border="0"  cellpadding="0" cellspacing="0">
                                        <tr style="margin-bottom:20px;padding:5px">
                                            <td align="center" valign="top">
                                                <form name="itemdetails1">
                                                    <table align="center" border="0" cellpadding="0" cellspacing="1"
                                                           class="outerboxcell">
                                                        <tr class="outerboxcell">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                Id
                                                            </td>
                                                            <td id="rev1ContentId" align="left" class="outerboxcellfont"
                                                                colspan="2" height="20">Item:501
                                                            </td>
                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.itemTitle@ItemTitle"/>
                                                            </td>
                                                            <td id="rev1Title" align="left" style="width:80px;word-wrap: break-word;" class="datacell1font">revData.revId
                                                            </td>
                                                        </tr>
                                                        <tr class="datacell1">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.label@Revision"/>
                                                            </td>
                                                            <td id="rev1No" align="left" class="datacell1font">revData.revId
                                                                <input name="sys_revision" type="hidden" value="2"></td>
                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.label@Last Modified"/>
                                                            </td>
                                                            <td id="rev1Date" align="left" class="datacell1font">revData.revId

                                                            </td>
                                                        </tr>
                                                        <tr class="datacell1">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.label@Last Modifier"/>
                                                            </td>
                                                            <td id="rev1ModifiedBy" align="left" class="datacell1font">
                                                                revData.revId

                                                            </td>
                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.label@Status"/>
                                                            </td>
                                                            <td id="rev1State" align="left" class="datacell1font">revData.revId

                                                            </td>
                                                        </tr>
                                                        <tr class="datacell1 ">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.selectRev@selectRev"/>
                                                            </td>
                                                            <td align="left" class="datacell1font" height="20">
                                                                <select align="left" id="revisionList1"
                                                                        onchange="compareSelectedRevisions();">
                                                                </select>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </form>
                                            </td>
                                        </tr>
                                        <tr class="headercell">
                                            <td align="center" class="headercellfont" colspan="2" height="10">
                                            </td>
                                        </tr>
                                        <tr class="outerboxcell">
                                            <td align="center" class="outerboxcellfont" colspan="2" height="2">
                                                <hr>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td align="center">
                                                <form name="itemdetails2">
                                                    <table align="center" border="0" cellpadding="0" cellspacing="1"
                                                           class="outerboxcell">
                                                        <tr class="outerboxcell">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                Id
                                                            </td>
                                                            <td id="rev2ContentId" align="left" class="outerboxcellfont"
                                                                colspan="2" height="20" width="150">Item:501
                                                            </td>
                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.itemTitle@ItemTitle"/>
                                                            </td>
                                                            <td id="rev2Title" align="left" style="width:80px; word-wrap: break-word;" class="datacell1font" width="150">
                                                            </td>
                                                        </tr>
                                                        <tr class="datacell1">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.label@Revision"/>
                                                            </td>
                                                            <td id="rev2No" align="left" class="datacell1font">1
                                                                <input name="sys_revision" type="hidden" value="1"></td>
                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.label@Last Modified"/>
                                                            </td>
                                                            <td id="rev2Date" align="left" class="datacell1font">
                                                            </td>
                                                        </tr>
                                                        <tr class="datacell1">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.label@Last Modified"/>
                                                            </td>
                                                            <td id="rev2ModifiedBy" align="left" class="datacell1font">admin1

                                                            </td>
                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.label@Status"/>
                                                            </td>
                                                            <td id="rev2State" align="left" class="datacell1font">Quick Edit

                                                            </td>
                                                        </tr>
                                                        <tr class="datacell1">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <i18n:message key="perc.ui.revisionDialog.selectRev@selectRev"/>
                                                            </td>
                                                            <td align="left" class="datacell1font" height="20">
                                                                <select align="left" id="revisionList2"
                                                                        onchange="compareSelectedRevisions();"/>
                                                            </td>
                                                            </td>
                                                        </tr>
                                                        <tr class="outerboxcell">
                                                            <td align="center" class="outerboxcellfont" colspan="2" height="2">
                                                                <hr>
                                                            </td>
                                                        </tr>
                                                        <tr class="datacell1" id="templateList">

                                                            <td align="left" class="datacellfontheader" height="20" width="150"><i18n:message key="perc.ui.revisionDialog.selectTemplate@Select Template"/></td>
                                                            <td align="left" class="datacell1font"  height="20">
                                                                <select id="variantlist" onchange="compareSelectedRevisions();">
                                                                </select>
                                                            </td>

                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" class="datacellfontheader" height="20" width="150">
                                                                <label class="perc-checkbox-label">
                                                                    <input id="contrastCheckbox" type="checkbox" name="checkbox"
                                                                           onchange="setContrast()" value="text"><i18n:message key="perc.ui.revisionDialog.highContrast@High Contrast"/> :
                                                                </label>
                                                            </td>
                                                            <td align="left" class="datacell1font">
                                                            </td>
                                                        </tr>
                                                        <tr class="outerboxcell">
                                                            <td align="center" class="outerboxcellfont" colspan="2" height="2">
                                                                <hr>
                                                            </td>
                                                        </tr>
                                                        <tr class="outerboxcell" style="margin-bottom:20px;padding:5px">
                                                            <td align="left"
                                                                style="margin-bottom:20px;padding:5px;font-weight: bold;"
                                                                class="outerboxcellfont" colspan="2" height="2">
                                                                <i18n:message key="perc.ui.revisionDialog.legends@Legends"/> :
                                                            </td>
                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td  align="left" class="outerboxcellfont"
                                                                 style="margin-bottom:20px;padding:5px" colspan="2" height="20">
                                                                <span tabindex="0" role="definition" id="AddedText" aria-label="Legend for Added Text"  class="ins-reg">&nbsp;&nbsp;a&nbsp;&nbsp;</span>
                                                                <span tabindex="0" role="term" aria-details="AddedText" ><i18n:message key="perc.ui.revisionDialog.addedText@Added Text"/></span>
                                                            </td>

                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" style="margin-bottom:20px;padding:5px"
                                                                class="outerboxcellfont" colspan="2" height="20">
                                                                <span tabindex="0" role="definition" id="AddedText-HighContrast" aria-label="Legend for Added Text in high contrast" class="ins-contrast">&nbsp;&nbsp;a&nbsp;&nbsp;</span>
                                                                <span  role="term" aria-details="AddedText-HighContrast"  tabindex="0" ><i18n:message key="perc.ui.revisionDialog.addedTextHighContrast@Added Text High Contrast"/></span>
                                                            </td>

                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td  align="left" style="margin-bottom:20px;padding:5px"
                                                                 class="outerboxcellfont" colspan="2" height="20">
                                                                <span tabindex="0" role="definition" id = "RemovedText" aria-label="Legend for Removed Text" class="del-reg">&nbsp;&nbsp;a&nbsp;&nbsp;</span>
                                                                <span tabindex="0" role="term" aria-details="RemovedText" ><i18n:message key="perc.ui.revisionDialog.removedText@Removed Text"/></span>
                                                            </td>

                                                        </tr>
                                                        <tr class="datacell2">
                                                            <td align="left" style="margin-bottom:20px;padding:5px"
                                                                class="outerboxcellfont" colspan="2" height="20">
                                                                <span tabindex="0" role="definition" id="RemovedText-HighContrast" aria-label="Legend for Removed Text in high contrast" class="del-contrast">&nbsp;&nbsp;a&nbsp;&nbsp;</span>
                                                                <span tabindex="0" role="term" aria-details="RemovedText-HighContrast"><i18n:message key="perc.ui.revisionDialog.removedTextHighContrast@Removed Text High Contrast"/></span>
                                                            </td>

                                                        </tr>
                                                    </table>
                                                </form>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </div>
                </section>
            </div>

            <div id="rightSide"  width="90%">
                <div class="perc-compare-header">
                    <a id="tabLink1" class="perc-compare-header" style="padding:5px" onclick="switchTab(0)"><i18n:message key="perc.ui.revisionDialog.revision@Revision"/> 1</a>
                    <a id="tabLink2" class="perc-compare-header" style="padding:5px" onclick="switchTab(1)"><i18n:message key="perc.ui.revisionDialog.compare@Compare"/></a>
                    <a id="tabLink3" class="perc-compare-header" style="padding:5px" onclick="switchTab(2)"><i18n:message key="perc.ui.revisionDialog.revision@Revision"/> 2</a>
                </div>

                <div class="tab-container" >

                    <div class="tab"  id="tab1">
                        <iframe class="page" id="page1">

                        </iframe>
                    </div>
                    <div class="tab" id="tab2">
                        <iframe class="page"  id="comparedPage"  >

                        </iframe></div>
                    <div class="tab"  id="tab3">
                        <iframe  class="page"  id="page2">

                        </iframe></div>
                </div>
            </div>
        </section>
    </div>
</div>
</body>
<script>
    document.addEventListener('DOMContentLoaded', function () {
            console.log("setRevisionPages Called");
            var percCompareService;
            if(parent.opener != null && typeof(parent.opener) !== "undefined" && typeof(parent.opener._percCompareService) !== "undefined"){
                percCompareService = parent.opener._percCompareService;
                refreshFullPage(percCompareService.params);

            }else{
                percCompareService = $.PercCompareService();
                percCompareService.params.compareWindow = window;                   ;
                var params = getParams();
                var revId1 = params['sys_revision1'];
                var itemId = params['sys_contentid1'];
                var siteId = params['sys_siteid'];
                var folderId = params['sys_folderid'];
                percCompareService.params.itemId = itemId;
                percCompareService.params.revision1 = revId1;
                percCompareService.params.siteId = siteId;
                percCompareService.params.folderId = folderId;
                percCompareService.params.openNewWindow = false;
                if(typeof(siteId) != 'undefined') {
                    percCompareService.params.assemblerRenderer= true;
                }
                percCompareService.openComparisonWindow(params);
            }
        }
    );

    function setContrast() {
        var checkbox = document.getElementById("contrastCheckbox");
        var addClass;
        var delClass;
        if (checkbox.checked == true) {
            addClass = 'ins-contrast';
            delClass = 'del-contrast';
        } else {
            addClass = 'ins-reg';
            delClass = 'del-reg';
        }
        var addElem = $('.page').contents().find('ins'), i;
        for (i = 0; i < addElem.length; i++) {
            addElem[i].className = addClass;
        }
        var delElem = $('.page').contents().find('del'), i;
        for (i = 0; i < delElem.length; i++) {
            delElem[i].className = delClass;
        }
    }

    function getParams (url = window.location) {

        // Create a params object
        let params = {};

        new URL(url).searchParams.forEach(function (val, key) {
            if (params[key] !== undefined) {
                if (!Array.isArray(params[key])) {
                    params[key] = [params[key]];
                }
                params[key].push(val);
            } else {
                params[key] = val;
            }
        });

        return params;

    }

    function refreshRightSide(data) {
        document.getElementById('page1').srcdoc = data.page1;
        document.getElementById('comparedPage').srcdoc = data.comparedPage;
        document.getElementById('page2').srcdoc = data.page2;
    }

    function refreshFullPage(data) {
        var rev1Data = data.allRevisions.get(Number(data.revision1));
        var rev2Data = data.allRevisions.get(Number(data.revision2));
        document.getElementById('page1').srcdoc = data.page1;
        document.getElementById('comparedPage').srcdoc = data.comparedPage;
        document.getElementById('page2').srcdoc = data.page2;

        document.getElementById("rev1ContentId").innerHTML = data.itemId;
        document.getElementById("rev1Title").innerHTML = data.title;
        document.getElementById("rev1No").innerHTML = rev1Data.revId;
        document.getElementById("rev1Date").innerHTML = rev1Data.lastModified;
        document.getElementById("rev1ModifiedBy").innerHTML = rev1Data.modifier;
        document.getElementById("rev1State").innerHTML = rev1Data.status;

        document.getElementById("rev2ContentId").innerHTML = data.itemId;
        document.getElementById("rev2Title").innerHTML = data.title;
        document.getElementById("rev2No").innerHTML = rev2Data.revId;
        document.getElementById("rev2Date").innerHTML = rev2Data.lastModified;
        document.getElementById("rev2ModifiedBy").innerHTML = rev2Data.modifier;
        document.getElementById("rev2State").innerHTML = rev2Data.status;

        document.getElementById("tabLink1").text = "Revision : " + rev1Data.revId;
        document.getElementById("tabLink3").text = "Revision : " + rev2Data.revId;


        if(data.refreshFullPage){
            populateRevision(data);
            let templateRow = document.getElementById("templateList");
            if(data.assemblerRenderer){
                populateTemplates(data);
                templateRow.removeAttribute("hidden");
            }else{
                templateRow.setAttribute("hidden", "hidden");
            }
        }
    }

    function populateTemplates(data){

        const list1 = document.getElementById("variantlist");

        for(var i = 0, size = data.templates.length; i < size ; i++){
            var item = data.templates[i];
            var option1 = document.createElement("option");
            option1.id = item.templateId;
            option1.value = item.templateId;
            option1.text = item.templateName;
            list1.appendChild(option1);
        }
        if(data.selectedTemplate){
            list1.value = data.selectedTemplate;
        }else{
            list1.value = data.templates[0].templateId;
        }
    }


    function populateRevision(data){
        if(data.revisionsPopulated){
            return;
        }

        const list1 = document.getElementById("revisionList1");
        const list2 = document.getElementById("revisionList2");


        for (const [key, value] of data.allRevisions) {

            var option1 = document.createElement("option");
            option1.id = key;
            option1.value = key;
            option1.text = key + "- " + value.lastModified;
            list1.appendChild(option1);
            var option2 = document.createElement("option");
            option2.id = key;
            option2.value = key;
            option2.text = key + "- " + value.lastModified;
            list2.appendChild(option2);

        }

        list1.value = data.revision1;
        list2.value = data.revision2;
        data.revisionsPopulated = true;


    }


    function compareSelectedRevisions() {

        const list1 = document.getElementById("revisionList1");
        const list2 = document.getElementById("revisionList2");
        var rev1Id = list1.options[list1.selectedIndex].id;
        var rev2Id = list2.options[list2.selectedIndex].id;

        const tempList = document.getElementById("variantlist");

        if( typeof(tempList.options[tempList.selectedIndex]) != 'undefined'){
            var tempId = tempList.options[tempList.selectedIndex].id;
        }


        document.getElementById("tabLink1").text ="<i18n:message key= 'perc.ui.revisionDialog.label@Revision'/> : "+ rev1Id;
        document.getElementById("tabLink3").text = "<i18n:message key= 'perc.ui.revisionDialog.label@Revision' /> : "+ rev2Id;

        var rev1 = parseInt(rev1Id);
        var rev2 = parseInt(rev2Id);



        var percCompareService = $.PercCompareService();

        if(parent.opener != null && parent.opener._percCompareService){
            percCompareService =  parent.opener._percCompareService;
        }else{
            percCompareService.params.comapreWindow = window;
        }

        if(typeof(tempId) != 'undefined' && typeof(tempId) != null){
            percCompareService.params.assemblerRenderer = true;
            var temp1 = parseInt(tempId);
            percCompareService.params.selectedTemplate = temp1;
        }
        percCompareService.params.revision1 = rev1;
        percCompareService.params.revision2 = rev2;
        percCompareService.params.openWindow = false;
        percCompareService.params.refreshFullPage = false;
        percCompareService.loadComparePages();
        refreshRightSide(percCompareService.params);
    }

    function openSelectRevision(url) {
        window.open(url);
    }

    function openSelectDependent(url) {
        window.open(url);
    }

    class TabManager {
        constructor() {
            this.tabs = document.querySelectorAll('.tab');
            this.initTabs();
        }

        initTabs() {
            this.showTab(0);
        }

        showTab(tabIndex) {
            this.tabs.forEach((tab, index) => {
                var tabLinkName = "tabLink" + (index + 1);
                if (index === tabIndex) {
                    document.getElementById(tabLinkName).className = 'tab-selected';
                    tab.style.display = 'block';
                } else {
                    tab.style.display = 'none';
                    document.getElementById(tabLinkName).className = 'tab-label';
                }
            });
        }
    }

    const tabManager = new TabManager();

    function switchTab(tabIndex) {
        tabManager.showTab(tabIndex);
    }

</script>
</html>


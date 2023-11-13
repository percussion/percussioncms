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

(function($){
    $.percCompareServiceInstance = null;
    $.PercCompareService = function () {

        if($.percCompareServiceInstance == null){
            $.percCompareServiceInstance = PercCompareService();
        }

        return $.percCompareServiceInstance;
    };

    function PercCompareService() {
        var page1;
        var page2;
        var comparedPage;
        var revision1;
        var revision2;
        var allRevisions;
        var itemId;
        var title;
        var siteId;
        var folderId;
        var itemHref;
        var mobilePreview;
        var compareWindow;
        var assemblerRenderer = false;
        var templates;
        var selectedTemplate;

        return {
            openComparisonWindow:openComparisonWindow,
            compareRevisions:compareRevisions,
            setPageData:setPageData,
            getAllReveisionsAndOpenComparisonWindow:getAllReveisionsAndOpenComparisonWindow,
            setCompareWindow:setCompareWindow,
            setAssemblyRenderer:setAssemblyRenderer

        };

        function setAssemblyRenderer(assmRend){
            $.percCompareServiceInstance.assemblerRenderer = assmRend;
        }

        function setCompareWindow(compareWind){
            compareWindow = compareWind;
        }

        function setPageData(page1,page2,comparedPage){
            $.percCompareServiceInstance.page1=page1;
            $.percCompareServiceInstance.page2=page2;
            $.percCompareServiceInstance.comparedPage = comparedPage;
        }
        //sys_revision1=3&sys_contentid1=499&sys_siteid=301

        function getAllReveisionsAndOpenComparisonWindow(itemId,selectedRev,siteId,folderId){
            assemblerRenderer = true;
            $.percCompareServiceInstance.siteId = siteId;
            $.percCompareServiceInstance.folderId = folderId;
            getAllTemplates(itemId,selectedRev,siteId,folderId);
        }

        function getAllTemplates(itemId,selectedRev,siteId,folderId){
            var url = "../../../rest/templates/summaries-by-filter";
            var payload = {TemplateFilter: {
                    contentId: itemId,
                }};
            $.PercServiceUtils.makeJsonRequest(url,  $.PercServiceUtils.TYPE_POST,false, function callback(status,result){
                if(status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    //$.perc_utils.alert_dialog({title: 'Error', content: defaultMsg});
                    callback(false);
                    return;
                }else{
                    var templateList = result.data.TemplateSummaryList;
                    $.PercRevisionService.getRevisionDetails(itemId,function callback(status,result){
                        if(status === $.PercServiceUtils.STATUS_ERROR)
                        {
                            var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                            // $.perc_utils.alert_dialog({title: 'Error', content: defaultMsg});
                            callback(false);
                            return;
                        }else{
                            var revData = convertRevisions(result.data.RevisionsSummary.revisions);
                            openComparisonWindow(itemId,siteId,folderId,null,selectedRev,1,revData,templateList,true);
                        }
                    });
                }
            },payload);
        }

        function isComparable(response) {
            const notAllowedCT = ["application/pdf", "image/gif", "application/octet-stream",
                "image/jpeg","image/png","image/svg+xml","audio/mpeg","video/mp4","application/zip",
                "application/x-gzip","application/x-tar"];
            var contentType = response.headers.get("content-type");

            if(notAllowedCT.includes(contentType)){
                return false;
            }else{
                return true;
            }
        }

        function isIteratable(value){

            if(value != null && typeof value[Symbol.iterator] === 'function'){
                return true;
            }
            return false;
        }

        function convertRevisions(revs){
            var allRevisions = new Map([]);
            if(isIteratable(revs)){
                for (let item of revs) {
                    allRevisions.set(Number(item.revId), { revId: item.revId, lastModified: item.lastModifiedDate, modifier: item.lastModifier,status : item.status});
                }
            }else if (typeof(revs) != 'undefined'){
                allRevisions.set(Number(revs.revId), { revId: revs.revId, lastModified: revs.lastModifiedDate, modifier: revs.lastModifier,status : revs.status});

            }
            return allRevisions;
        }

        function openComparisonWindow(itemId,siteId,folderId,itemName,selectedRev,latestRev,allRevisions,templatesList,populateList,refreshLeftHand){
            $.percCompareServiceInstance.itemId = itemId;
            $.percCompareServiceInstance.title = itemName;
            $.percCompareServiceInstance.folderId = folderId;
            $.percCompareServiceInstance.siteId = siteId;
            $.percCompareServiceInstance.revision1 = Number(selectedRev);
            $.percCompareServiceInstance.revision2= Number(latestRev);
            $.percCompareServiceInstance.allRevisions=allRevisions;
            $.percCompareServiceInstance.templates = templatesList;
            if(assemblerRenderer){
                $.percCompareServiceInstance.selectedTemplate = templatesList[0].templateId;
            }

            // Retrieve the path for the given page id to build the friendly URL and open hte preview
            $.PercPathService.getPathItemById(itemId, function(status, result, errorCode) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    var href = result.PathItem.folderPaths + "/" + result.PathItem.name;
                    var mobilePreview = result.PathItem.mobilePreviewEnabled;
                    if(typeof mobilePreview === "undefined" || mobilePreview === null){
                        mobilePreview = false;
                    }
                    $.percCompareServiceInstance.title = result.PathItem.name;
                    href = href.substring(1);
                    $.percCompareServiceInstance.itemHref = href;
                    $.percCompareServiceInstance.mobilePreview = mobilePreview;
                    if(assemblerRenderer){
                        compareRevisions(selectedRev,latestRev,false, templatesList[0].templateId,populateList,true);
                    }else{
                        compareRevisions(selectedRev,latestRev,true, null, null ,true,true);
                    }
                }
                else {
                    // We failed retrieving the friendly URL. Show the error dialog
                    $.unblockUI();

                    var msg = "";
                    if (errorCode == "cannot.find.item")
                    {
                        msg = I18N.message( 'perc.ui.common.error@Preview Content Deleted' );
                    }
                    else
                    {
                        msg = result;
                    }

                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: msg});
                }
            });
        }

        function openCompareWindow(page1,page2,output){
            compareWindow = window.open('/cm/app/compare.jsp');
            compareWindow.refreshUI($.percCompareServiceInstance,true);
        }

        function refreshCompareWindow(page1,page2,output,populateLists,refreshLeftHand){
            if(refreshLeftHand){
                compareWindow.refreshUI($.percCompareServiceInstance,populateLists);
            }else{
                compareWindow.refreshRightSide($.percCompareServiceInstance,populateLists);
            }
        }

        function compareRevisions(revId1,revId2,openWindow,selectedTemplate,populateLists,refreshLeftHand){
            $.percCompareServiceInstance.revision1 = Number(revId1);
            $.percCompareServiceInstance.revision2 = Number(revId2);
            var href1 = $.percCompareServiceInstance.itemHref;
            var href2=$.percCompareServiceInstance.itemHref;
            var mobilePreview = $.percCompareServiceInstance.mobilePreview;

            if(revId1)
            {
                if(assemblerRenderer){
                    href1 = "/assembler/render?sys_revision=" + revId1 + "&sys_context=0&sys_siteid="+
                        $.percCompareServiceInstance.siteId+"&sys_contentid="+
                        $.percCompareServiceInstance.itemId+
                        "&sys_itemfilter=preview&sys_template=" + selectedTemplate +
                        "&sys_folderid="+ $.percCompareServiceInstance.folderId;
                }else{
                    href1 += "?sys_revision=" + revId1 + "&percmobilepreview="+mobilePreview;
                }


            }

            if(revId2)
            {
                if(assemblerRenderer){
                    href2 = "/assembler/render?sys_revision=" + revId2 +
                        "&sys_context=0&sys_folderid=513&sys_siteid="+
                        $.percCompareServiceInstance.siteId+"&sys_contentid="+ $.percCompareServiceInstance.itemId+
                        "&sys_itemfilter=preview&sys_template=" + selectedTemplate+
                        "&sys_folderid="+ $.percCompareServiceInstance.folderId;
                }else{
                    href2 += "?sys_revision=" + revId2 + "&percmobilepreview="+mobilePreview;
                }
            }

            fetch(href1)
                .then(function (response) {
                    switch (response.status) {
                        case 200:{
                            if(!isComparable(response)){
                                var contentType = response.headers.get("content-type");
                                var message = "Page with ContentType:" + contentType + " is not comparable. Please select different template.";
                                $.PercCompareService().setPageData(message,message,message);
                                if(openWindow)
                                    openCompareWindow(message,message,message);
                                else
                                    refreshCompareWindow(message,message,message,populateLists,refreshLeftHand);
                            }else{
                                return response.text();
                            }
                        }
                        case 404:
                            throw response;
                    }
                })
                .then(function (template) {
                    page1 = template;
                    fetch(href2)
                        .then(function (response) {
                            switch (response.status) {
                                // status "OK"
                                case 200:
                                    return response.text();
                                // status "Not Found"
                                case 404:
                                    throw response;
                            }
                        })
                        .then(function (template) {
                            var page2 = template;
                            // Diff HTML strings
                            var output = htmldiff(page1, page2);
                            $.PercCompareService().setPageData(page1,page2,output);
                            if(openWindow)
                                openCompareWindow(page1,page2,output);
                            else
                                refreshCompareWindow(page1,page2,output,populateLists,refreshLeftHand);
                        })
                        .catch(function (response) {
                            console.log(response.statusText);
                            throw response;
                        });
                })
                .catch(function (response) {
                    console.log(response.statusText);
                    $.percCompareServiceInstance.comparedPage =  "Page Failed to Load. Error: " + response;
                });
        }
    }
})(jQuery);

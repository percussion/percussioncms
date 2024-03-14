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
    var params = null;
    $.PercCompareService = function () {
        if($.percCompareServiceInstance == null){
            $.percCompareServiceInstance = PercCompareService();
        }
        return $.percCompareServiceInstance;
    };

    class CompareParams {
        constructor() {
            this.page1 = null;
            this.page2 = null;
            this.comparedPage = null;
            this.revision1 = null;
            this.revision2 = null;
            this.allRevisions = null;
            this.itemId = null;
            this.title = null;
            this.siteId = null;
            this.folderId = null;
            this.itemHref = null;
            this.mobilePreview = null;
            this.compareWindow = null;
            this.assemblerRenderer = false;
            this.templates = null;
            this.selectedTemplate = null;
            this.openNewWindow = true;
            this.refreshFullPage = true;
            this.revisionsPopulated = false;
        }
    }

    function PercCompareService() {

        var params = new CompareParams();

        return {
            openComparisonWindow:openComparisonWindow,
            params:params,
            loadComparePages:loadComparePages
        };


        function getAllTemplates(passedParams){
            var url = "../../../rest/templates/summaries-by-filter";
            var payload = {TemplateFilter: {
                    contentId: passedParams.itemId,
                }};
            $.PercServiceUtils.makeJsonRequest(url,  $.PercServiceUtils.TYPE_POST,false, function callback(status,result){
                if(status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false);
                    return;
                }else{
                    var templateList = result.data.TemplateSummaryList;
                    passedParams.templates = templateList;
                    passedParams.selectedTemplate = templateList[0].templateId;
                    getRevisionDetails(passedParams);

                }
            },payload);
        }

        function getRevisionDetails(passedParams){
            $.PercRevisionService.getRevisionDetails(passedParams.itemId,function callback(status,result){
                if(status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false);
                    return;
                }else{
                    convertRevisions(result.data.RevisionsSummary.revisions,passedParams);
                    openComparisonWindow();
                }
            });
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

        function convertRevisions(revs,passedParams){
            var allRevisions = new Map([]);
            var lastRevision;
            if(isIteratable(revs)){
                for (let item of revs) {
                    allRevisions.set(Number(item.revId), { revId: item.revId, lastModified: item.lastModifiedDate, modifier: item.lastModifier,status : item.status});
                    lastRevision = item.revId;
                }
            }else if (typeof(revs) != 'undefined'){
                allRevisions.set(Number(revs.revId), { revId: revs.revId, lastModified: revs.lastModifiedDate, modifier: revs.lastModifier,status : revs.status});
                lastRevision = revs.revId;
            }
            passedParams.allRevisions = allRevisions;
            passedParams.revision2 = lastRevision;
        }

        function openComparisonWindow(){
            if(params.assemblerRenderer && params.refreshFullPage && params.templates == null){
                getAllTemplates(params);
            }else if(params.assemblerRenderer && !params.refreshFullPage){
                $.percCompareServiceInstance.loadComparePages();
            }else if(!params.assemblerRenderer && params.refreshFullPage && params.allRevisions == null){
                getRevisionDetails(params);
            }else{
                // Retrieve the path for the given page id to build the friendly URL and open hte preview
                $.PercPathService.getPathItemById(params.itemId, function(status, result, errorCode) {
                    if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                        var href = result.PathItem.folderPaths + "/" + result.PathItem.name;
                        var mobilePreview = result.PathItem.mobilePreviewEnabled;
                        if(typeof mobilePreview === "undefined" || mobilePreview === null){
                            mobilePreview = false;
                        }
                        params.title = result.PathItem.name;
                        href = href.substring(1);
                        $.percCompareServiceInstance.params.itemHref = href;
                        $.percCompareServiceInstance.params.mobilePreview = mobilePreview;
                        $.percCompareServiceInstance.loadComparePages();

                    }
                    else {
                        // We failed retrieving the friendly URL. Show the error dialog
                        $.unblockUI();
                        var msg = "";
                        if (errorCode == "cannot.find.item")
                        {
                            msg = I18N.message( 'perc.ui.revisionDialog.failedPageLoad@Failed Page Load' );
                            console.log("Failed to Load Page. Item Id:" + itemId);
                        }
                        else
                        {
                            msg = result;
                        }
                    }
                });
            }
        }

        function createWindow(){
            var url = "/cm/app/compare.jsp?sys_revision1=" + $.percCompareServiceInstance.params.revision1 + "&sys_contentid1=" + $.percCompareServiceInstance.params.itemId;
            if($.percCompareServiceInstance.params.assemblerRenderer){
                url = url +"&sys_siteid=" + $.percCompareServiceInstance.params.siteId + "&sys_folderid=" + $.percCompareServiceInstance.params.folderId;
            }
            compareWindow = window.open(url);
            params.compareWindow = compareWindow;
            compareWindow.onload = function () {
                compareWindow.refreshFullPage($.percCompareServiceInstance.params);
            };
        }

        function refreshCompareWindow(){
            if(params.refreshFullPage){
                if(params.openNewWindow){
                    createWindow();
                }else{
                    params.compareWindow.refreshFullPage(params);
                }
            }else{
                params.compareWindow.refreshRightSide(params);
            }
        }

        function loadComparePages(){
            var href1 = params.itemHref;
            var href2=params.itemHref;
            var mobilePreview = params.mobilePreview;
            if(params.revision1)
            {
                if(params.assemblerRenderer){
                    href1 = "/assembler/render?sys_revision=" + params.revision1 + "&sys_context=0&sys_siteid="+
                        params.siteId+"&sys_contentid="+
                        params.itemId+
                        "&sys_itemfilter=preview&sys_template=" + params.selectedTemplate +
                        "&sys_folderid="+ params.folderId;
                }else{
                    href1 += "?sys_revision=" + params.revision1 + "&percmobilepreview="+params.mobilePreview;
                }
            }

            if(params.revision2)
            {
                if(params.assemblerRenderer){
                    href2 = "/assembler/render?sys_revision=" + params.revision2 +
                        "&sys_context=0&sys_folderid=513&sys_siteid="+
                        params.siteId+"&sys_contentid="+ params.itemId+
                        "&sys_itemfilter=preview&sys_template=" + params.selectedTemplate+
                        "&sys_folderid="+ params.folderId;
                }else{
                    href2 += "?sys_revision=" + params.revision2 + "&percmobilepreview="+ params.mobilePreview;
                }
            }

            fetch(href1)
                .then(function (response) {
                    switch (response.status) {
                        case 200:{
                            if(!isComparable(response)){
                                var contentType = response.headers.get("content-type");
                                // var message = "Content is not comparable. ContentType : " + contentType ;
                                var message =  I18N.message( 'perc.ui.revisionDialog.notComparable@Not compareable' ) + " ContentType: " + contentType;
                                params.page1 = message;
                                params.page2 = message;
                                params.comparedPage = message;
                                refreshCompareWindow(params);
                                return false;
                            }else{
                                return response.text();
                            }
                            break;
                        }
                        case 404:
                            throw response;
                    }
                })
                .then(function (template) {
                    //means page is not comparable, thus no need to load page2
                    if(template == false){
                        return;
                    }
                    params.page1 = template;
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
                            params.page2 = template;
                            // Diff HTML strings
                            var output = htmldiff(params.page1, params.page2);
                            params.comparedPage = output;
                            refreshCompareWindow();
                        })
                        .catch(function (response) {
                            console.log(response.statusText);
                            throw response;
                        });
                })
                .catch(function (response) {
                    console.log(response.statusText);
                    $.percCompareServiceInstance.comparedPage =  I18N.message("perc.ui.revisionDialog.failedCompare@Failed Compare.")+ " Error: " + response;
                });
        }
    }
})(jQuery);

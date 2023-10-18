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

        if($.percCompareServiceInstance == null)
            $.percCompareServiceInstance = PercCompareService();
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
        var itemHref;
        var mobilePreview;
        var compareWindow;

        return {
            openComparisonWindow:openComparisonWindow,
            setPageData:setPageData,
            compareRevisions:compareRevisions
        };

        function setPageData(page1,page2,comparedPage){
            $.percCompareServiceInstance.page1=page1;
            $.percCompareServiceInstance.page2=page2;
            $.percCompareServiceInstance.comparedPage = comparedPage;
        }

        function openComparisonWindow(itemId,title,selectedRev,latestRev,allRevisions){
            $.percCompareServiceInstance.itemId = itemId;
            $.percCompareServiceInstance.title = title;
            $.percCompareServiceInstance.revision1 = selectedRev;
            $.percCompareServiceInstance.revision2= latestRev;
            $.percCompareServiceInstance.allRevisions = allRevisions;

            // Retrieve the path for the given page id to build the friendly URL and open hte preview
            $.PercPathService.getPathItemById(itemId, function(status, result, errorCode) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    var href = result.PathItem.folderPaths + "/" + result.PathItem.name;
                    var mobilePreview = result.PathItem.mobilePreviewEnabled;
                    if(typeof mobilePreview === "undefined" || mobilePreview === null){
                        mobilePreview = false;
                    }
                    href = href.substring(1);
                    $.percCompareServiceInstance.itemHref = href;
                    $.percCompareServiceInstance.mobilePreview = mobilePreview;
                    compareRevisions(selectedRev,latestRev,true );
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
        }

        function refreshCompareWindow(page1,page2,output){
            compareWindow.refreshUI(false);
        }

        function compareRevisions(revId1,revId2,openWindow){
            $.percCompareServiceInstance.revision1 = revId1;
            $.percCompareServiceInstance.revision2 = revId2;
            var href1 = $.percCompareServiceInstance.itemHref;
            var href2=$.percCompareServiceInstance.itemHref;
            var mobilePreview = $.percCompareServiceInstance.mobilePreview;

            if(revId1)
            {
                href1 += "?sys_revision=" + revId1 + "&percmobilepreview="+mobilePreview;
            }
            else{
                href1 += "?percmobilepreview="+mobilePreview;
            }

            if(revId2)
            {
                href2 += "?sys_revision=" + revId2 + "&percmobilepreview="+mobilePreview;
            }
            else{
                href2 += "?percmobilepreview="+mobilePreview;
            }
            fetch(href1)
                .then(function (response) {
                    switch (response.status) {
                        case 200:
                            return response.text();
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
                            let output = htmldiff(page1, page2);
                            $.PercCompareService().setPageData(page1,page2,output);
                            if(openWindow)
                                openCompareWindow(page1,page2,output);
                            else
                                refreshCompareWindow(false);
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

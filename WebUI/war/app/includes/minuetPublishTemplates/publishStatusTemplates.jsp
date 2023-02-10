

<script id="templatePercPublishStatus" type="text/x-handlebars-template">
    <div id="percPublishStatusContainer" class="container">
        <div class="row">
            <div class="col-lg-12">
                <div class="table-responsive-md">
                    <table class="table table-striped" id="percPublishStatusList">
                        <thead>
                        <tr>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="siteName"><i18n:message key="perc.ui.publish.title@SiteName"/>&nbsp;&nbsp;</th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="status"><i18n:message key="perc.ui.publish.title@Status"/>&nbsp;&nbsp;</th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-active-sort" data-perc-sort-order="desc" data-perc-sort-prop="startTime"><i18n:message key="perc.ui.publish.title@Time"/>&nbsp;&nbsp;</th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-order="desc" data-perc-sort-prop="elapsedTime"><i18n:message key="perc.ui.publish.title@Duration"/>&nbsp;&nbsp;</th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="calculatedProgress"><i18n:message key="perc.ui.publish.title@Progress"/>&nbsp;&nbsp;</th>
                            <th scope="col"><i18n:message key="perc.ui.publish.title@Action"/></th>
                        </tr>
                        </thead>
                        <tbody id="percPublishStatusListTarget"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="templatePercPublishStatusList" type="text/x-handlebars-template">
    {{#each SitePublishJob}}
    <tr>
        <td class="align-middle">{{siteName}}</td>
        <td class="align-middle">{{status}}</td>
        <td class="align-middle">{{startDate}} {{startTime}}</td>
        <td class="align-middle">{{#duration elapsedTime}}{{/duration}}</td>
        <td class="align-middle">
            <div class="progress perc-progress">
                <div class="progress-bar perc-progress-bar" role="progressbar" style="width: {{#publishProgress completedItems totalItems}}{{/publishProgress}}%;" aria-valuenow="{{#publishProgress completedItems totalItems}}{{/publishProgress}}" aria-valuemin="0" aria-valuemax="100">{{#publishProgress completedItems totalItems}}{{/publishProgress}}%</div>
            </div>
        </td>
        <td class="align-middle">
            <button data-perc-job-id="{{jobId}}" role="button" class="btn btn-sm perc-btn-primary perc-stop-job-button" {{#if isStopping}}disabled{{/if}}><i18n:message key="perc.ui.publish.title@Stop"></i18n:message></button>
        </td>
    </tr>
    {{/each}}
</script>

// assumed dependencies:
//  - require.js
//  - jquery

requirejs
(
    [
        'modules/adaptors/adaptor.ajax',
        'widgets/ping/ping'
    ],
    function (dataAdaptor)
    {
        $(document).ready(function ()
        {
            var options = {
                dataAdaptor: dataAdaptor,
                pingUri: $('#config').find('[name="pingUri"]').val(),
                isDebug: ($('#config').find('[name="isDebug"]').val() === 'true')
            };
            
            var $app = $('#application');
            $app.ping(options);
        });
    }
);

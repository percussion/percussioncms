$(document).ready(function() {
    $("#page_calendar").multiselect({
        height:175,
        minWidth:225,
        checkAllText: 'Select all',
        uncheckAllText: 'Deselect all'
    });
    var startDate = $("#perc-content-edit-page_start_date").val().replace(':00.0', '');
    $('#display_calendar_start_date').val(startDate);
    var endDate = $("#perc-content-edit-page_end_date").val().replace(':00.0', '');
    $('#display_calendar_end_date').val(endDate);


    $('#display_calendar_start_date, #display_calendar_end_date').datepicker({
        showTime:true,
        timeFormat: 'hh:mm:ss.s',
        stepHours: 1,
        time24h: true,
        stepMinutes: 10,
        changeMonth:true,
        changeyear:true,
        constrainInput:true,
        showOn: 'button',
        buttonImage: '../rx_resources/controls/percQueryControl/images/calendar.gif',
        buttonImageOnly: true,
        dateFormat: 'yy-mm-dd',
        buttonText: ''
    }).bind('paste', function(evt){evt.preventDefault();})
        .bind('keypress keydown', function(evt){
            if(evt.keyCode === 46 || evt.keyCode === 8 )
            {
                var field = evt.target;
                field.value = "";
                evt.preventDefault();
                return;
            }
            if(evt.charCode === 0 || typeof(evt.charCode) == 'undefined')
                return;
            evt.preventDefault();
        });

    $.topFrameJQuery.PercContentPreSubmitHandlers.addHandler(updateCalendar);
});

function updateCalendar()
{
    $("#perc-content-edit-page_calendar").find('.perc_field_error').remove();
    var startDate = $('#display_calendar_start_date').val();
    $("#perc-content-edit-page_start_date").val(startDate);
    var endDate = $('#display_calendar_end_date').val();
    $("#perc-content-edit-page_end_date").val(endDate);
    if(startDate === '' && endDate !== '')
    {
        $("#perc-content-edit-page_calendar").append('<label style="display: block;" for="page_calendar" class="perc_field_error">Start date must be less than End date.</label>');
        return false;
    }
    var p_start_date_temp = startDate.replace(/-|:| /g, ',').split(',');
    p_start_date = new Date(p_start_date_temp[0], p_start_date_temp[1]-1, p_start_date_temp[2], p_start_date_temp[3], p_start_date_temp[4]);
    var p_end_date_temp = endDate.replace(/-|:| /g, ',').split(',');
    p_end_date = new Date(p_end_date_temp[0], p_end_date_temp[1]-1, p_end_date_temp[2], p_end_date_temp[3], p_end_date_temp[4]);

    if(p_start_date >= p_end_date)
    {
        $("#perc-content-edit-page_calendar").append('<label style="display: block;" for="page_calendar" class="perc_field_error">Start date must be less than End date.</label>');
        return false;
    }
    if($("#page_calendar").val() && startDate === '')
    {
        $("#perc-content-edit-page_calendar").append('<label style="display: block;" for="page_calendar" class="perc_field_error">Start date must not be empty, if at least one calendar is selected.</label>');
        return false;
    }
    return true;
}
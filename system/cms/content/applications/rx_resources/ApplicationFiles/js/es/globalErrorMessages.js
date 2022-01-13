/* Global variable holding JavaScript message map
 * Note to translator: The messages inside the quotes only are eligible for localization
 */
var PsxMessageMap_Locale ={
	Delete_Confirm:	"Delete actions cannot be undone. Are you sure you want to continue?",	
	Field_Required:		" is a required field",
	save_search_prompt:	"Please enter a name for this search.\nNote: search will be overwritten if name already exists.",
	search_name: "Search Name",
	special_char_alert: "Special characters like ampersand (&) and quotation marks (' and \") are not allowed in search names.",
	workflow_comment_required:	"Workflow Comment is required to transition this content item.",
	contenttypes_not_available:	"There are no content types available for this slot.",
	contenttypes_not_available_through_communities:	"There are no content types available for this slot, through your login community.",
	homepage_url_generation_error: "Could not generate virtual site home page URL. Contact your administrator.",
	select_item_before_deleting: "Please select an item before deleting.",
	select_one_item_before_inserting: "Please select at least one item to insert.",
	form_change_warning: "Changes have been made to this form.\nDo you want to save before closing?",
	override_checkout_warning_part1: "This item is checked out by <",
	override_checkout_warning_part2: ">.\nIf you override the check-out, modifications to this item may be lost.\n\nClick 'OK' to check-in this item. Click 'Cancel' to abort check-in.",
	no_entries: "No Entries",
	noSearchForTextMsg: "Search button is deactivated when 'Search For' field is empty.",
	workflow_comment_cannot_exceed_255_chars: "Workflow comment cannot exceed 255 chracters.",
	translation_may_take_time: "Generating Translation Content Items may take a few minutes."
};


/* 
 * Function to convert javascript message to user locale
 * Note to translator: Do not touch this function under any circumstances
 */
function LocalizedMessage(msg)
{
	var localemsg = "";
	//Check whether msg exists or not.
	if(msg)
	{
		//Look for the message for keyword msg in user locale
		localemsg = PsxMessageMap_Locale[msg];
		if(!localemsg) //not found, look in default map
			localemsg = PsxMessageMap[msg];
		if(!localemsg) //not found in default map too, return msg itself.
			localemsg = msg;
	}
	else
	{
		//If msg it self does not exist, then return error message.
		localemsg = "Message is missing";
	}
	return localemsg;
}


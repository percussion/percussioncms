/* Global variable holding JavaScript message map
 * Note to translator: The messages inside the quotes only are eligible for localization
 */
var PsxMessageMap_Locale ={
	Delete_Confirm:	"Las acciones de eliminación no se pueden deshacer. Estás seguro de que quieres continuar?",
	Field_Required:		" es un campo obligatorio",
	save_search_prompt:	"Por favor ingrese un nombre para esta búsqueda.\nNota: la búsqueda se sobrescribirá si el nombre ya existe.",
	search_name: "Nombre de búsqueda",
	special_char_alert: "Los caracteres especiales como el signo comercial (&) y las comillas (' y \") no están permitidos en los nombres de búsqueda.",
	workflow_comment_required:	"Se requiere un comentario de flujo de trabajo para realizar la transición de este elemento de contenido.",
	contenttypes_not_available:	"No hay tipos de contenido disponibles para este espacio.",
	contenttypes_not_available_through_communities:	"No hay tipos de contenido disponibles para este espacio a través de su comunidad de inicio de sesión.",
	homepage_url_generation_error: "No se pudo generar la URL de la página de inicio del sitio virtual. Póngase en contacto con su administrador.",
	select_item_before_deleting: "Seleccione un elemento antes de eliminarlo.",
	select_one_item_before_inserting: "Seleccione al menos un elemento para insertar.",
	form_change_warning: "Se han realizado cambios en este formulario.\n¿Quieres guardar antes de cerrar?",
	override_checkout_warning_part1: "Este artículo está prestado por <",
	override_checkout_warning_part2: ">.\nSi anula el check-out, es posible que se pierdan las modificaciones realizadas a este artículo. Haga clic en \"Aceptar\" para registrar este artículo.\nHaga clic en 'Cancelar' para cancelar el registro.",
	no_entries: "No entrar",
	noSearchForTextMsg: "El botón Buscar se desactiva cuando el campo 'Buscar' está vacío.",
	workflow_comment_cannot_exceed_255_chars: "El comentario del flujo de trabajo no puede exceder los 255 caracteres.",
	translation_may_take_time: "La generación de elementos de contenido de traducción puede tardar unos minutos."
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


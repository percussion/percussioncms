/**
 * Used to spell check the text and replace it with corrected text in DHTML Editor. 	
 * Pops up messages when error happens or no misspelled words in the text or when 
 * misspelled words are corrected automatically by previous settings of 'ChangeAll' 
 * in Spell Check Dialog.
 **/


function spellCheckText(){ 
   
   if(document.spellcheck.checkSpelling(Composition.document.body.innerHTML,
      Composition.document.body.innerText))
   {
      var result = document.spellcheck.checkResult();
      if(result == 1)
         alert("Error occured while spell checking.Please inform Administrator");
      else if(result == 2)
      {
         refreshText(document.spellcheck.getChangedText());
         alert( "Misspelled words are automatically changed as per previous 'ChangeAll' Settings");		
      }
      else if(result == 0)
         alert("No misspelled words");
   }
}

function refreshText(html)
{
   Composition.document.body.innerHTML = html;
}

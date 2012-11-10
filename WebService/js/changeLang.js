$('input').change(function() {
  if ( $('input[name="grpLang"]:checked').val() == "1" ) {
    $('body').removeClass('dir_rtl');
    $('body').addClass('dir_ltr');

  } else {
    $('body').removeClass('dir_ltr');
    $('body').addClass('dir_rtl');


  }

});

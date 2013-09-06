$(window).on('mercury:ready', function() {
  
  function getUrlParam(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20'))||null
  }
  
  Mercury.saveUrl = '/moscato/rest/mercury' + window.location.pathname;
  
  Mercury.on('saved', function(event) {
    $.bootstrapGrowl("Document saved.", {
      type: 'success',
      delay: 2000
    });
  });

  //Mercury.preloadedViews['/mercury/selects/formatblock.html'] += "Hello";
  
});

/*
$(document).ready(function() {
  $('[data-edit]').click(function(evt) {
    var button = $(evt.target);
    button.html(button.html() === 'Edit' ? 'Cancel' : 'Edit');
    Mercury.trigger('toggle:interface');
  });
});
*/
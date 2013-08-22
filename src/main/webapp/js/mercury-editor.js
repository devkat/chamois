$(window).on('mercury:ready', function() {
  
  Mercury.saveUrl = '/api' + window.location.pathname;
  
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
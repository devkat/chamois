define([
  'jquery'
], function(
  $
) {
  
  $(window).on('mercury:ready', function() {
    Mercury.saveUrl = '/api' + window.location.pathname;
  });
  
  $(document).ready(function() {
    $('[data-edit]').click(function(evt) {
      var button = $(evt.target);
      button.html(button.html() === 'Edit' ? 'Cancel' : 'Edit');
      Mercury.trigger('toggle:interface');
    });
  });
  
});
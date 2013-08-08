define([
  'jquery',
  'bootstrap'
], function(
  $
) {
  
  $(document).ready(function() {
    $('span.has-error').closest('.form-group').addClass('has-error');
  });
  
});
define([
  'jquery',
  'underscore',
  'jsrender'
], function(
  $,
  _
) {
  
  var
    nodeTemplate = $('#tree-node-template'),
    openIcon = 'icon-minus-sign-alt',
    closedIcon = 'icon-plus-sign-alt',
    fileIcon = 'icon-sign-blank';
  
  return function(parent, settings) {
    
    var
      apiUrl = settings.apiUrl,
      hrefUrl = settings.hrefUrl,
      currentPath = window.location.pathname.substring(hrefUrl.length);
    
    function loadChildren(container, path) {
      
      function renderNode(node) {
        var
          nodePath = path + '/' + node.slug,
          elem = $(nodeTemplate.render(_.extend({
            id: _.uniqueId(),
            href: hrefUrl + nodePath,
            current: currentPath === nodePath,
            closedIcon: closedIcon,
            fileIcon: fileIcon
          }, node)).trim()),
          triggerElem = $('> .collapse-trigger', elem),
          childrenElem = $('> ul', elem);
        
        triggerElem.on('click', function(evt) {
          childrenElem.collapse('toggle');
          evt.stopPropagation();
        });

        childrenElem.on('show.bs.collapse', function(evt) {
          evt.stopPropagation();
          triggerElem.removeClass(closedIcon).addClass(openIcon);
          if ($('> *', childrenElem).length === 0) {
            loadChildren(childrenElem, nodePath);
          }
        });
        childrenElem.on('hide.bs.collapse', function(evt) {
          evt.stopPropagation();
          triggerElem.removeClass(openIcon).addClass(closedIcon);
        });
        if (currentPath.match(new RegExp('^' + nodePath))) {
          childrenElem.collapse({toggle: true});
        }
        return elem;
      }

      var loader = $('<span class="icon icon-rotate icon-refresh"></span>');
      container.append(loader);
      $.ajax({
        url: apiUrl + path,
        dataType: 'json',
        success: function(data) {
          loader.remove();
          container.append(
            path === '' && data.length === 0
              ? "No resources."
              : _.map(data, renderNode)
          );
        },
        error: function(error) {
          debugger;
        }
      });
    }
    
    if (!parent.hasClass('tree')) {
      parent.addClass('tree');
      loadChildren(parent, '');
    }
  };

});
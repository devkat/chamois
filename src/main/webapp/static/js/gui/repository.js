require([
  'require',
  'jquery',
  'jqform',
  'bootstrap',
  'underscore',
  'dgrid/OnDemandGrid',
  'dgrid/tree',
  "dojo/date/locale",
  'dojo/date/stamp',
  "dojo/store/Observable",
  'core/documentStore'
], function(
  require,
  $,
  jqform,
  bootstrap,
  _,
  OnDemandGrid,
  Tree,
  dateLocale,
  dateStamp,
  Observable,
  documentStore
) {
  var docStore = documentStore();
  
  function renderDate(object, data, td, options) {
    if (data) {
      var date = dateStamp.fromISOString(data); // + " UTC", { datePattern: "yyyy-MM-dd", timePattern: "HH:mm:ss vz" });
      td.innerHTML = date;//dateLocale.format(date);
    }
  };
  
  var repoId = parseInt(window.location.pathname.match(/(\d+)$/)[1]);

  $(function() {
    
    var grid = new OnDemandGrid({
      columns: {
        col1: Tree({
          label: 'Name',
          field: 'name',
          renderCell: function(object, data, td, options) {
            $(td).append($('<a>').attr("href", "/document/" + object.idField).html(data));
            
            /*
            var iconClass = "";
            if (object.nodeType === 1) {
              on(link, 'click', function(evt) {
                grid.expand(grid.row(evt));
              });
              var suffix = "Closed";
              iconClass = "bedocsIcon bedocsIconFolder" + suffix;
            }
            else {
              iconClass = "bedocsIcon bedocsIconDocument";
            }
            dojo.create("img", {
              className: iconClass,
              src: "/classpath/dojo/resources/blank.gif"
            }, link);
            
            on(link, "mouseover", function(evt) {
              evt.preventDefault();
              selectedId = object.id;
              array.forEach([newDocumentItem, newFolderItem], function(item) {
                dojo.style(item.domNode, 'display', object.nodeType === 1 ? '' : 'none');
              });
              popup.open({
                popup: tooltipDialog,
                around: link,
                orient: [ "after-centered" ]
              });
            });
            */
          }
        }),
        col2: {
          field: "created",
          label: "Created",
          renderCell: renderDate
        }
      },
      //store: Observable(Cache(documentStore, Memory()))
      store: Observable(docStore)
    }, "documents");
    
    grid.startup();
    
    _.each(['document', 'image'], function(type) {
      var dialog = $('#dialog-new-' + type);
      $('input[name="repositoryId"]', dialog).attr('value', repoId);
      $('form', dialog).ajaxForm(function() { 
        dialog.modal('hide'); 
      });
      
      /*
      $('.chm-cancel', dialog).click(function() {
        dialog.modal('hide');
      });
      
      $('.chm-submit', dialog).click(function(evt) {
        var data = _.reduce($('form', dialog).serializeArray(), function(item, pair) {
          item[pair.name] = pair.value;
          return item;
        }, {});
        data.repositoryId = repoId;
        docStore.put(data);
        dialog.modal('hide');
      });
      */
    });
    
  });
  
});

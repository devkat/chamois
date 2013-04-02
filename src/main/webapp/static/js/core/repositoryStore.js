/*
 * http://dojotoolkit.org/documentation/tutorials/1.7/store_driven_tree/
 */

define([
  "dojo/_base/xhr",
  "dojo/store/Observable",
  "dojo/store/JsonRest"
], function(
  xhr,
  Observable,
  JsonRest
) {

  var store = new JsonRest({
    target: "/api/repository/",
    idProperty: 'idField'
  });
  
  return function() {
    return store;
  };
});
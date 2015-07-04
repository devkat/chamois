var restStore = new Amygdala({
  'config': {
    'apiUrl': 'http://localhost:8080/cms/api/v1',
    'localStorage': false
  },
  'schema': {
    'users': {
      'url': '/users'
    }
  }
});
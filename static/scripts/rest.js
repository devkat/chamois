var restStore = new Amygdala({
  config: {
    apiUrl: 'http://localhost:8080/cms/api/v1',
    localStorage: false,
    idAttribute: 'id'
  },
  schema: {
    users: {
      url: '/users/',
      idAttribute: 'username'
    }
  }
});
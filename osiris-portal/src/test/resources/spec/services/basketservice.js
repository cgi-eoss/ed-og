'use strict';

describe('Service: basketservice', function () {

  // load the service's module
  beforeEach(module('osirisApp'));

  // instantiate service
  var basketservice;
  beforeEach(inject(function (_basketservice_) {
    basketservice = _basketservice_;
  }));

  it('should do something', function () {
    expect(!!basketservice).toBe(true);
  });

});

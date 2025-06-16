import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { of } from 'rxjs';

import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let interceptor: authInterceptor;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [authInterceptor]
    });
    interceptor = TestBed.inject(authInterceptor);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should intercept HTTP requests', (done) => {
    const req = new HttpRequest('GET', '/test');
    const next: HttpHandler = {
      handle: () => of({} as HttpEvent<any>)
    };

    interceptor.intercept(req, next).subscribe(event => {
      expect(event).toBeDefined();
      done();
    });
  });
});

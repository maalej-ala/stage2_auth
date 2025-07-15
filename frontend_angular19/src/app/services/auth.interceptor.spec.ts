import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { AuthInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

describe('AuthInterceptor', () => {
  let interceptor: AuthInterceptor;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const authSpy = jasmine.createSpyObj('AuthService', ['getToken', 'logout']);
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthInterceptor,
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerMock }
      ]
    });

    interceptor = TestBed.inject(AuthInterceptor);
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Setup getToken to return a fake token by default
    authServiceSpy.getToken.and.returnValue('fake-token');
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should add Authorization header if token exists', (done) => {
    const req = new HttpRequest('GET', '/test');
    const next: HttpHandler = {
      handle: (request: HttpRequest<any>) => {
        expect(request.headers.has('Authorization')).toBeTrue();
        expect(request.headers.get('Authorization')).toBe('Bearer fake-token');
        return of({} as HttpEvent<unknown>);
      }
    };

    interceptor.intercept(req, next).subscribe(() => done());
  });

  it('should call logout on 401 error', (done) => {
    const req = new HttpRequest('GET', '/test');
    const errorResponse = { status: 401, statusText: 'Unauthorized' };

    const next: HttpHandler = {
      handle: () => throwError(() => errorResponse)
    };

    interceptor.intercept(req, next).subscribe({
      error: (error) => {
        expect(error).toBe(errorResponse);
        expect(authServiceSpy.logout).toHaveBeenCalled();
        done();
      }
    });
  });
});

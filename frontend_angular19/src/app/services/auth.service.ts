// auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { catchError, tap, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';

// Interfaces remain the same
export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  user: User;
  expiresIn?: number;
}

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role?: string;
}

export interface UserCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: 'USER' | 'ADMIN' | 'DOCTOR';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly REFRESH_KEY = 'refresh_token';

  private currentUserSubject = new BehaviorSubject<User | null>(this.getCurrentUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.checkTokenValidity();
  }

  /**
   * Check token validity and refresh if needed before making a request
   */
  private ensureValidToken(): Observable<boolean> {
    if (this.hasValidToken()) {
      return of(true);
    }

    const refresh = this.getRefreshToken();
    if (!refresh) {
      this.logout();
      return throwError(() => new Error('No refresh token available'));
    }

    return this.refreshToken().pipe(
      switchMap((response: AuthResponse) => {
        console.log('Token refreshed successfully:', response); // DEBUG
        return of(true);
      }),
      catchError((error) => {
        console.error('Token refresh failed:', error); // DEBUG
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Login user
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials)
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(this.handleError)
      );
  }

  /**
   * Register new user
   */
  signup(userData: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData)
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(this.handleError)
      );
  }

  /**
   * Create user (for testing)
   */
  createUser(userData: UserCreateRequest): Observable<any> {
    return this.ensureValidToken().pipe(
      switchMap(() => {
        const token = this.getToken();
        const headers = token
          ? new HttpHeaders({ Authorization: `Bearer ${token}` })
          : new HttpHeaders();

        return this.http.post(`${this.API_URL}/create`, userData, { headers }).pipe(
          catchError(this.handleError)
        );
      })
    );
  }

  /**
   * Logout user
   */
  logout(): void {
    this.clearAuthData();
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  /**
   * Refresh authentication token
   */
  refreshToken(): Observable<AuthResponse> {
    const refresh = this.getRefreshToken();
    if (!refresh) return throwError(() => new Error('No refresh token found'));

    console.log('Sending refresh token:', refresh); // DEBUG
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { token: refresh })
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(error => {
          console.error('Refresh error:', error); // DEBUG
          this.logout();
          return throwError(() => error);
        })
      );
  }

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (userJson) {
      try {
        return JSON.parse(userJson);
      } catch {
        this.clearAuthData();
        return null;
      }
    }
    return null;
  }

  /**
   * Get authentication token
   */
  getToken(): string | null {
    console.log('Retrieved token from localStorage:', localStorage.getItem(this.TOKEN_KEY)); // DEBUG
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.hasValidToken();
  }

  /**
   * Check if user has a specific role
   */
  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.role === role;
  }

  private handleAuthSuccess(response: AuthResponse): void {
    this.saveToken(response.token);
    this.saveUser(response.user);

    if (response.refreshToken) {
      this.saveRefreshToken(response.refreshToken);
    }

    this.currentUserSubject.next(response.user);
    this.isAuthenticatedSubject.next(true);

    // if (response.expiresIn) {
    //   this.scheduleTokenRefresh(response.expiresIn);
    // }
  }

  private saveToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private saveUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private saveRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_KEY, token);
  }

  private getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_KEY);
  }

  private clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
  }

  private hasValidToken(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp > currentTime;
    } catch {
      return false;
    }
  }

  private checkTokenValidity(): void {
    if (!this.hasValidToken()) {
      this.clearAuthData();
      this.currentUserSubject.next(null);
      this.isAuthenticatedSubject.next(false);
    }
  }

  // private scheduleTokenRefresh(expiresIn: number): void {
  //   // Refresh token 30 seconds before expiration
  //   const refreshTime = (expiresIn - 30) * 1000;
  //   setTimeout(() => {
  //     if (this.isAuthenticated()) {
  //       this.refreshToken().subscribe({
  //         error: () => this.logout()
  //       });
  //     }
  //   }, refreshTime);
  // }

  // updateToken(token: string): void {
  //   this.saveToken(token);
  //   const decoded = this.decodeToken(token);
  //   if (decoded?.exp) {
  //     const expiresIn = decoded.exp - Math.floor(Date.now() / 1000);
  //     this.scheduleTokenRefresh(expiresIn);
  //   }
  // }

  // private decodeToken(token: string): any {
  //   try {
  //     return JSON.parse(atob(token.split('.')[1]));
  //   } catch {
  //     return null;
  //   }
  // }

  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else {
      switch (error.status) {
        case 400:
          errorMessage = error.error?.message || 'Bad request';
          break;
        case 401:
          errorMessage = 'Invalid credentials';
          break;
        case 403:
          errorMessage = 'Access forbidden';
          break;
        case 404:
          errorMessage = 'Service not found';
          break;
        case 409:
          errorMessage = error.error?.message || 'Email already exists';
          break;
        case 422:
          errorMessage = error.error?.message || 'Validation error';
          break;
        case 500:
          errorMessage = 'Server error. Please try again later';
          break;
        default:
          errorMessage = error.error?.message || `Error ${error.status}: ${error.statusText}`;
      }
    }

    console.error('Auth Service Error:', error);
    return throwError(() => new Error(errorMessage));
  };
}
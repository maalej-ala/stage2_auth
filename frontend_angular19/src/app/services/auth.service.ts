import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';

// Interfaces for type safety
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

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth'; // Replace with your API URL
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly REFRESH_KEY = 'refresh_token';


  // BehaviorSubject to track authentication state
  private currentUserSubject = new BehaviorSubject<User | null>(this.getCurrentUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

constructor(
    private http: HttpClient,
    private router: Router
) {
    // Check token validity on service initialization
    this.checkTokenValidity();
    // Periodically check token validity (e.g., every 30 seconds)
    setInterval(() => this.checkTokenValidity(), 30 * 1000);
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
   * Logout user
   */
  logout(): void {
    // Optional: Call logout endpoint to invalidate token on server
    // this.http.post(`${this.API_URL}/logout`, {}).subscribe();
    
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

  return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { token: refresh })
    .pipe(
      tap(response => this.handleAuthSuccess(response)),
      catchError(error => {
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



  // Private helper methods

private handleAuthSuccess(response: AuthResponse): void {
  this.saveToken(response.token);
  this.saveUser(response.user);

  if (response.refreshToken) {
    this.saveRefreshToken(response.refreshToken);
  }

  this.currentUserSubject.next(response.user);
  this.isAuthenticatedSubject.next(true);

  if (response.expiresIn) {
    this.scheduleTokenRefresh(response.expiresIn);
  }
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
      // Basic JWT token validation (check if it's not expired)
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

  private scheduleTokenRefresh(expiresIn: number): void {
    // Refresh token 5 minutes before expiration
    const refreshTime = (expiresIn - 18) * 1000;
    setTimeout(() => {
      if (this.isAuthenticated()) {
        this.refreshToken().subscribe({
          error: () => this.logout()
        });
      }
    }, refreshTime);
  }

  updateToken(token: string): void {
  this.saveToken(token);
  const decoded = this.decodeToken(token);
  if (decoded?.exp) {
    const expiresIn = decoded.exp - Math.floor(Date.now() / 1000);
    this.scheduleTokenRefresh(expiresIn);
  }
}
 private decodeToken(token: string): any {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch {
    return null;
  }
}



  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      switch (error.status) {
        case 400:
          errorMessage = error.error?.message || 'Bad request';
          break;
        case 401:
          errorMessage = 'Invalid credentials';
          this.logout(); // Force logout on 401
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
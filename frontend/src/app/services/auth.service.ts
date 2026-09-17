import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, UserInfo } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<UserInfo | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    if (this.isBrowser && this.getAccessToken()) {
      this.loadCurrentUser();
    }
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/auth/login`, request).pipe(
      tap(response => {
        if (this.isBrowser) {
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
        }
        this.loadCurrentUser();
      })
    );
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.API}/users/register`, request);
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
    this.currentUserSubject.next(null);
  }

  refreshToken(): Observable<{ accessToken: string }> {
    const refreshToken = this.isBrowser ? localStorage.getItem('refreshToken') : null;
    return this.http.post<{ accessToken: string }>(`${this.API}/auth/refresh`, { refreshToken }).pipe(
      tap(response => {
        if (this.isBrowser) {
          localStorage.setItem('accessToken', response.accessToken);
        }
      })
    );
  }

  getAccessToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem('accessToken');
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  // ===== Базовая проверка =====

  hasRole(role: 'VIEWER' | 'TECHNIC' | 'ENGINEER' | 'ADMIN'): boolean {
    const user = this.currentUserSubject.value;
    return user?.roles?.includes(role) ?? false;
  }

  hasAnyRole(...roles: Array<'VIEWER' | 'TECHNIC' | 'ENGINEER' | 'ADMIN'>): boolean {
    const user = this.currentUserSubject.value;
    if (!user?.roles) return false;
    return roles.some(r => user.roles!.includes(r));
  }

  // ===== Удобные алиасы (используются в шаблонах) =====

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  /** Может управлять оборудованием, портами, IP-адресами */
  canEditEquipment(): boolean {
    return this.hasAnyRole('ENGINEER', 'ADMIN');
  }

  /** Может создавать/редактировать записи обслуживания */
  canCreateMaintenance(): boolean {
    return this.hasAnyRole('TECHNIC', 'ENGINEER', 'ADMIN');
  }

  /** Может удалять записи обслуживания */
  canDeleteMaintenance(): boolean {
    return this.hasAnyRole('ENGINEER', 'ADMIN');
  }

  /** Может управлять справочниками (типы, сотрудники) и пользователями */
  canManageAdminEntities(): boolean {
    return this.hasRole('ADMIN');
  }

  /** Доступ к сканированию сети */
  canUseDiscovery(): boolean {
    return this.hasAnyRole('ENGINEER', 'ADMIN');
  }

  getCurrentUser(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  private loadCurrentUser(): void {
    this.http.get<UserInfo>(`${this.API}/auth/me`).subscribe({
      next: user => this.currentUserSubject.next(user),
      error: () => this.logout()
    });
  }
}

import { HttpInterceptorFn } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  // Не добавляем токен к запросам на логин и регистрацию
  if (req.url.includes('/auth/login') ||
      req.url.includes('/auth/refresh') ||
      req.url.includes('/users/register')) {
    return next(req);
  }

  const accessToken = localStorage.getItem('accessToken');

  if (accessToken) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${accessToken}`)
    });
    return next(authReq);
  }

  return next(req);
};

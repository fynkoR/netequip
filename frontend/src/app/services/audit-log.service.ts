import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLog } from '../models/audit-log.model';
import { Page } from '../models/page.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private readonly API = `${environment.apiUrl}/audit`;

  constructor(private http: HttpClient) {}

  getPage(page: number, size: number, search: string = '', sort: string = 'timestamp,desc'): Observable<Page<AuditLog>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search && search.trim().length > 0) {
      params = params.set('search', search.trim());
    }
    return this.http.get<Page<AuditLog>>(this.API, { params });
  }
}
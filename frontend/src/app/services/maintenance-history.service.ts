import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { MaintenanceHistory } from '../models/maintenance-history.model';
import { Page } from '../models/page.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MaintenanceHistoryService {
  private readonly API = `${environment.apiUrl}/histoires`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<MaintenanceHistory[]> {
    const params = new HttpParams().set('size', 1000);
    return this.http.get<Page<MaintenanceHistory>>(this.API, { params })
      .pipe(map(p => p.content));
  }

  getPage(page: number, size: number, search: string = '', sort: string = 'id,asc'): Observable<Page<MaintenanceHistory>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search && search.trim().length > 0) {
      params = params.set('search', search.trim());
    }
    return this.http.get<Page<MaintenanceHistory>>(this.API, { params });
  }

  getById(id: number): Observable<MaintenanceHistory> {
    return this.http.get<MaintenanceHistory>(`${this.API}/${id}`);
  }

  create(history: MaintenanceHistory): Observable<MaintenanceHistory> {
    return this.http.post<MaintenanceHistory>(this.API, history);
  }

  update(id: number, history: MaintenanceHistory): Observable<MaintenanceHistory> {
    return this.http.put<MaintenanceHistory>(`${this.API}/${id}`, history);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
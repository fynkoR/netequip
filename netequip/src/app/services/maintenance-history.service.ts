import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MaintenanceHistory } from '../models/maintenance-history.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MaintenanceHistoryService {
  private readonly API = environment.apiUrl + '/histoires';

  constructor(private http: HttpClient) {}

  getAll(): Observable<MaintenanceHistory[]> {
    return this.http.get<MaintenanceHistory[]>(this.API);
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

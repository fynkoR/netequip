import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Equipment } from '../models/equipment.model';
import { Page } from '../models/page.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EquipmentService {
  private readonly API = environment.apiUrl + '/equipments';

  constructor(private http: HttpClient) {}

  // используется в 6 местах (дропдауны, дашборд, топология) — сохраняем сигнатуру
  getAll(): Observable<Equipment[]> {
    const params = new HttpParams().set('size', 1000);
    return this.http.get<Page<Equipment>>(this.API, { params })
      .pipe(map(p => p.content));
  }

  // для таблицы оборудования с пагинацией
  getPage(page: number, size: number, search: string = '', sort: string = 'id,asc'): Observable<Page<Equipment>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search && search.trim().length > 0) {
      params = params.set('search', search.trim());
    }
    return this.http.get<Page<Equipment>>(this.API, { params });
  }

  getById(id: number): Observable<Equipment> {
    return this.http.get<Equipment>(`${this.API}/${id}`);
  }

  create(equipment: Equipment): Observable<Equipment> {
    return this.http.post<Equipment>(this.API, equipment);
  }

  update(id: number, equipment: Equipment): Observable<Equipment> {
    return this.http.put<Equipment>(`${this.API}/${id}`, equipment);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
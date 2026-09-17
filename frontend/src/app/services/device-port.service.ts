import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { DevicePort } from '../models/device-port.model';
import { Page } from '../models/page.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DevicePortService {
  private readonly API = environment.apiUrl + '/ports';

  constructor(private http: HttpClient) {}

  getAll(): Observable<DevicePort[]> {
    const params = new HttpParams().set('size', 1000);
    return this.http.get<Page<DevicePort>>(this.API, { params })
      .pipe(map(p => p.content));
  }

  getPage(page: number, size: number, search: string = '', sort: string = 'id,asc'): Observable<Page<DevicePort>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search && search.trim().length > 0) {
      params = params.set('search', search.trim());
    }
    return this.http.get<Page<DevicePort>>(this.API, { params });
  }

  getById(id: number): Observable<DevicePort> {
    return this.http.get<DevicePort>(`${this.API}/${id}`);
  }

  create(port: DevicePort): Observable<DevicePort> {
    return this.http.post<DevicePort>(this.API, port);
  }

  update(id: number, port: DevicePort): Observable<DevicePort> {
    return this.http.put<DevicePort>(`${this.API}/${id}`, port);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }

  /**
   * Порты конкретного оборудования.
   * Дергаем общий endpoint с фильтрацией на фронте через поиск по имени —
   * это компромисс, пока на бэке нет специального endpoint /ports/by-equipment/{id}.
   * Берём большую страницу, чтобы захватить все порты устройства.
   */
  getByEquipmentId(equipmentId: number): Observable<DevicePort[]> {
    const params = new HttpParams().set('size', 1000);
    return this.http.get<Page<DevicePort>>(this.API, { params })
      .pipe(map(p => p.content.filter(port => port.equipmentId === equipmentId)));
  }
}
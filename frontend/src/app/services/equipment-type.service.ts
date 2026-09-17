import { Injectable } from '@angular/core';
import { HttpClient, HttpParams  } from '@angular/common/http';
import { Observable , map } from 'rxjs';
import { EquipmentType } from '../models/equipment-type.model';
import { environment } from '../../environments/environment';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class EquipmentTypeService {
  private readonly API = `${environment.apiUrl}/types`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<EquipmentType[]> {
    const params = new HttpParams().set('size', 1000);
    return this.http.get<Page<EquipmentType>>(this.API, { params })
      .pipe(map(page => page.content));
  }

  getPage(page: number, size: number, search: string = '', sort: string = 'id,asc'): Observable<Page<EquipmentType>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    if (search && search.trim().length > 0) {
      params = params.set('search', search.trim());
    }

    return this.http.get<Page<EquipmentType>>(this.API, { params });
  }

  getById(id: number): Observable<EquipmentType> {
    return this.http.get<EquipmentType>(`${this.API}/${id}`);
  }

  create(type: EquipmentType): Observable<EquipmentType> {
    return this.http.post<EquipmentType>(this.API, type);
  }

  update(id: number, type: EquipmentType): Observable<EquipmentType> {
    return this.http.put<EquipmentType>(`${this.API}/${id}`, type);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}

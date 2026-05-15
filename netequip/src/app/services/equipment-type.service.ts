import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EquipmentType } from '../models/equipment-type.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EquipmentTypeService {
  private readonly API = environment.apiUrl + '/types';

  constructor(private http: HttpClient) {}

  getAll(): Observable<EquipmentType[]> {
    return this.http.get<EquipmentType[]>(this.API);
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

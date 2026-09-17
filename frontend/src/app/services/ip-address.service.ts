import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { IpAddress } from '../models/ip-address.model';
import { Page } from '../models/page.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class IpAddressService {
  private readonly API = environment.apiUrl + '/ip-addresses';

  constructor(private http: HttpClient) {}

  getAll(): Observable<IpAddress[]> {
    const params = new HttpParams().set('size', 1000);
    return this.http.get<Page<IpAddress>>(this.API, { params })
      .pipe(map(p => p.content));
  }

  getPage(page: number, size: number, search: string = '', sort: string = 'id,asc'): Observable<Page<IpAddress>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search && search.trim().length > 0) {
      params = params.set('search', search.trim());
    }
    return this.http.get<Page<IpAddress>>(this.API, { params });
  }

  getById(id: number): Observable<IpAddress> {
    return this.http.get<IpAddress>(`${this.API}/${id}`);
  }

  create(ip: IpAddress): Observable<IpAddress> {
    return this.http.post<IpAddress>(this.API, ip);
  }

  update(id: number, ip: IpAddress): Observable<IpAddress> {
    return this.http.put<IpAddress>(`${this.API}/${id}`, ip);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Employee } from '../models/employee.model';
import { Page } from '../models/page.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly API = `${environment.apiUrl}/employees`;

  constructor(private http: HttpClient) {}

  // для дропдаунов — берёт всё через большую страницу
  getAll(): Observable<Employee[]> {
    const params = new HttpParams().set('size', 1000);
    return this.http.get<Page<Employee>>(this.API, { params })
      .pipe(map(p => p.content));
  }

  // для таблицы с пагинацией
  getPage(page: number, size: number, search: string = '', sort: string = 'id,asc'): Observable<Page<Employee>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search && search.trim().length > 0) {
      params = params.set('search', search.trim());
    }
    return this.http.get<Page<Employee>>(this.API, { params });
  }

  getById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.API}/${id}`);
  }

  create(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.API, employee);
  }

  update(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.API}/${id}`, employee);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
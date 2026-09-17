import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ImportRequest,
  ImportResult,
  ScanRequest,
  ScanResult
} from '../models/discovery.model';

@Injectable({ providedIn: 'root' })
export class DiscoveryService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/discovery`;

  scan(request: ScanRequest): Observable<ScanResult> {
    return this.http.post<ScanResult>(`${this.baseUrl}/scan`, request);
  }

  import(request: ImportRequest): Observable<ImportResult> {
    return this.http.post<ImportResult>(`${this.baseUrl}/import`, request);
  }
}
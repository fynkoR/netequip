import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DiscoveryService } from '../../services/discovery.service';
import { EquipmentTypeService } from '../../services/equipment-type.service';
import {
  DiscoveredDevice,
  ImportRequest,
  ImportResult,
  ScanRequest,
  ScanResult
} from '../../models/discovery.model';
import { EquipmentType } from '../../models/equipment-type.model';

interface DiscoveredDeviceUI extends DiscoveredDevice {
  selected: boolean;
  selectedTypeId: number | null;
}

@Component({
  selector: 'app-discovery',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './discovery.component.html',
  styleUrl: './discovery.component.css'
})
export class DiscoveryComponent implements OnInit {
  private discoveryService = inject(DiscoveryService);
  private equipmentTypeService = inject(EquipmentTypeService);
  private cdr = inject(ChangeDetectorRef);

  // Параметры формы сканирования
  request: ScanRequest = {
    cidr: '127.0.0.0/29',
    port: 1161,
    community: '{ip}/public'
  };

  // Состояние
  scanning = false;
  importing = false;
  errorMessage: string | null = null;

  // Результаты сканирования
  scanResult: ScanResult | null = null;
  devices: DiscoveredDeviceUI[] = [];

  // Все типы оборудования (для выпадающих списков)
  equipmentTypes: EquipmentType [] = [];

  // Результат импорта
  importResult: ImportResult | null = null;

  ngOnInit(): void {
    this.loadEquipmentTypes();
  }

  private loadEquipmentTypes(): void {
  this.equipmentTypeService.getAll().subscribe({
    next: (types) => {
      this.equipmentTypes = types;
      this.cdr.detectChanges();  // ← новое
    },
    error: (err) => console.error('Failed to load equipment types', err)
  });
}

  onScan(): void {
    this.errorMessage = null;
    this.scanResult = null;
    this.devices = [];
    this.importResult = null;
    this.scanning = true;
    this.cdr.detectChanges();

    this.discoveryService.scan(this.request).subscribe({
      next: (result) => {
        this.scanResult = result;
        this.devices = result.discoveredDevices.map(d => ({
          ...d,
          selected: d.matchConfidence === 'EXACT', // галочка по умолчанию для точных совпадений
          selectedTypeId: d.suggestedTypeId
        }));
        this.scanning = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Не удалось выполнить сканирование';
        this.scanning = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleAll(checked: boolean): void {
    this.devices.forEach(d => d.selected = checked);
  }

  get selectedCount(): number {
    return this.devices.filter(d => d.selected).length;
  }

  get canImport(): boolean {
    // Можно импортировать только если выбран хотя бы один,
    // и у всех выбранных проставлен тип
    return this.selectedCount > 0
        && this.devices.filter(d => d.selected).every(d => d.selectedTypeId !== null);
  }

  onImport(): void {
    if (!this.canImport) return;

    const request: ImportRequest = {
      devices: this.devices
        .filter(d => d.selected && d.selectedTypeId !== null)
        .map(d => ({
          snmpData: d.snmpData,
          typeId: d.selectedTypeId as number
        }))
    };

    this.importing = true;
    this.cdr.detectChanges();

    this.discoveryService.import(request).subscribe({
      next: (result) => {
        this.importResult = result;
        this.importing = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Ошибка импорта';
        this.importing = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Удобный геттер для шаблона
  confidenceIcon(c: string): string {
    if (c === 'EXACT') return '✓';
    if (c === 'HEURISTIC') return '?';
    return '!';
  }

  confidenceClass(c: string): string {
    if (c === 'EXACT') return 'badge-success';
    if (c === 'HEURISTIC') return 'badge-warning';
    return 'badge-danger';
  }
}
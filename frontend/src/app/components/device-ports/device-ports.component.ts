import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { DevicePortService } from '../../services/device-port.service';
import { EquipmentService } from '../../services/equipment.service';
import { DevicePort } from '../../models/device-port.model';
import { Equipment } from '../../models/equipment.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-device-ports',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './device-ports.component.html',
  styleUrl: './device-ports.component.css'
})
export class DevicePortsComponent implements OnInit, OnDestroy {
  items: DevicePort[] = [];
  equipments: Equipment[] = [];
  showForm = false;
  editing = false;
  editId: number | null = null;
  isAdmin = false;
  hoveredPortId: number | null = null;
  hoveredPairId: number | null = null;

  // пагинация
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [10, 20, 50];

  // поиск
  searchControl = new FormControl('');
  searchText = '';
  private destroy$ = new Subject<void>();

  form: DevicePort = { equipmentId: 0, portNumber: 1, status: 'NOT_CONNECTED' };
  statuses = ['NOT_CONNECTED', 'CONNECTED', 'DISABLE'];

  showConnectModal = false;
  connectSourcePort: DevicePort | null = null;
  connectTargetEquipmentId: number | null = null;
  connectTargetPortId: number | null = null;
  targetPorts: DevicePort[] = [];

  constructor(
    private service: DevicePortService,
    private equipmentService: EquipmentService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
    this.equipmentService.getAll().subscribe(data => this.equipments = data);
    this.isAdmin = this.authService.isAdmin();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(value => {
        this.searchText = value || '';
        this.currentPage = 0;
        this.load();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    this.service.getPage(this.currentPage, this.pageSize, this.searchText).subscribe(page => {
      this.items = page.content;
      this.totalElements = page.totalElements;
      this.totalPages = page.totalPages;
      this.cdr.detectChanges();
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.load();
  }
  nextPage(): void { this.goToPage(this.currentPage + 1); }
  prevPage(): void { this.goToPage(this.currentPage - 1); }

  changePageSize(newSize: number): void {
    this.pageSize = +newSize;
    this.currentPage = 0;
    this.load();
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }

  // === CRUD === (без изменений)
  openCreate(): void {
    this.form = { equipmentId: 0, portNumber: 1, status: 'NOT_CONNECTED' };
    this.editing = false;
    this.editId = null;
    this.showForm = true;
  }

  openEdit(item: DevicePort): void {
    this.form = { ...item };
    this.editing = true;
    this.editId = item.id!;
    this.showForm = true;
  }

  cancel(): void { this.showForm = false; }

  save(): void {
    if (this.editing && this.editId) {
      this.service.update(this.editId, this.form).subscribe(() => { this.showForm = false; this.load(); });
    } else {
      this.service.create(this.form).subscribe(() => { this.showForm = false; this.load(); });
    }
  }

  delete(id: number): void {
    if (confirm('Удалить порт?')) {
      this.service.delete(id).subscribe(() => {
        if (this.items.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.load();
      });
    }
  }

  // === СОЕДИНЕНИЕ ===

  openConnect(port: DevicePort): void {
    this.connectSourcePort = port;
    this.connectTargetEquipmentId = null;
    this.connectTargetPortId = null;
    this.targetPorts = [];
    this.showConnectModal = true;
  }

  // ВАЖНО: теперь догружаем порты целевого оборудования отдельным запросом,
  // потому что в this.items только текущая страница
  onTargetEquipmentChange(): void {
    this.connectTargetPortId = null;
    if (this.connectTargetEquipmentId) {
      this.service.getByEquipmentId(Number(this.connectTargetEquipmentId)).subscribe(ports => {
        this.targetPorts = ports.filter(p =>
          !p.connectedToPortId && p.id !== this.connectSourcePort?.id
        );
        this.cdr.detectChanges();
      });
    } else {
      this.targetPorts = [];
    }
  }

  cancelConnect(): void {
    this.showConnectModal = false;
    this.connectSourcePort = null;
  }

  confirmConnect(): void {
    if (!this.connectSourcePort || !this.connectTargetPortId || !this.connectTargetEquipmentId) return;

    const targetPort = this.targetPorts.find(p => p.id === Number(this.connectTargetPortId));
    if (!targetPort) return;

    const sourceUpdate: DevicePort = {
      ...this.connectSourcePort,
      connectedToEquipmentId: Number(this.connectTargetEquipmentId),
      connectedToPortId: targetPort.id,
      status: 'CONNECTED'
    };
    const targetUpdate: DevicePort = {
      ...targetPort,
      connectedToEquipmentId: this.connectSourcePort.equipmentId,
      connectedToPortId: this.connectSourcePort.id,
      status: 'CONNECTED'
    };

    this.service.update(this.connectSourcePort.id!, sourceUpdate).subscribe(() => {
      this.service.update(targetPort.id!, targetUpdate).subscribe(() => {
        this.showConnectModal = false;
        this.connectSourcePort = null;
        this.load();
      });
    });
  }

  disconnect(port: DevicePort): void {
    if (!confirm('Отсоединить порт?')) return;
    const linkedPortId = port.connectedToPortId;

    const sourceUpdate: DevicePort = {
      ...port,
      connectedToEquipmentId: undefined,
      connectedToPortId: undefined,
      status: 'NOT_CONNECTED'
    };

    this.service.update(port.id!, sourceUpdate).subscribe(() => {
      if (linkedPortId) {
        this.service.getById(linkedPortId).subscribe(targetPort => {
          const targetUpdate: DevicePort = {
            ...targetPort,
            connectedToEquipmentId: undefined,
            connectedToPortId: undefined,
            status: 'NOT_CONNECTED'
          };
          this.service.update(targetPort.id!, targetUpdate).subscribe(() => this.load());
        });
      } else {
        this.load();
      }
    });
  }

  getAvailableEquipments(): Equipment[] {
    if (!this.connectSourcePort) return this.equipments;
    return this.equipments.filter(e => e.id !== this.connectSourcePort!.equipmentId);
  }

  onRowHover(port: DevicePort): void {
    this.hoveredPortId = port.id || null;
    this.hoveredPairId = port.connectedToPortId || null;
  }

  onRowLeave(): void {
    this.hoveredPortId = null;
    this.hoveredPairId = null;
  }

  isHighlighted(port: DevicePort): boolean {
    return port.id === this.hoveredPairId || port.id === this.hoveredPortId;
  }
}
import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { MaintenanceHistoryService } from '../../services/maintenance-history.service';
import { EquipmentService } from '../../services/equipment.service';
import { MaintenanceHistory } from '../../models/maintenance-history.model';
import { Equipment } from '../../models/equipment.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-maintenance-history',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './maintenance-history.component.html',
  styleUrl: './maintenance-history.component.css'
})
export class MaintenanceHistoryComponent implements OnInit, OnDestroy {
  items: MaintenanceHistory[] = [];
  equipments: Equipment[] = [];
  showForm = false;
  editing = false;
  editId: number | null = null;
  isAdmin = false;

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

  form: MaintenanceHistory = { equipmentId: 0, date: '', type: 'Routine' };
  types = ['Routine', 'Repair', 'Upgrade', 'Emergency', 'Preventive'];

  constructor(
    private service: MaintenanceHistoryService,
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

  openCreate(): void {
    this.form = { equipmentId: 0, date: new Date().toISOString().slice(0, 16), type: 'Routine' };
    this.editing = false;
    this.editId = null;
    this.showForm = true;
  }

  openEdit(item: MaintenanceHistory): void {
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
    if (confirm('Удалить запись?')) {
      this.service.delete(id).subscribe(() => {
        if (this.items.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.load();
      });
    }
  }
}
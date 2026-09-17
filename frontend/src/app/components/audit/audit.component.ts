import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { AuditLogService } from '../../services/audit-log.service';
import { AuditLog } from '../../models/audit-log.model';

@Component({
  selector: 'app-audit',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './audit.component.html',
  styleUrl: './audit.component.css'
})
export class AuditComponent implements OnInit, OnDestroy {
  items: AuditLog[] = [];

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

  constructor(
    private service: AuditLogService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();

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

  // === вспомогательные методы для шаблона ===

  /** CSS-класс для бэйджа действия */
  actionClass(action: string): string {
    switch (action) {
      case 'CREATE': return 'badge-create';
      case 'UPDATE': return 'badge-update';
      case 'DELETE': return 'badge-delete';
      default: return 'badge-default';
    }
  }

  /** Человеческое имя сущности */
  entityLabel(entityType: string): string {
    switch (entityType) {
      case 'equipment':           return 'Оборудование';
      case 'equipment_type':      return 'Тип оборудования';
      case 'employee':            return 'Сотрудник';
      case 'ip_address':          return 'IP-адрес';
      case 'device_port':         return 'Порт';
      case 'maintenance_history': return 'Обслуживание';
      default:                    return entityType;
    }
  }
}
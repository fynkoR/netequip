import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { EquipmentService } from '../../services/equipment.service';
import { EquipmentTypeService } from '../../services/equipment-type.service';
import { EmployeeService } from '../../services/employee.service';
import { Equipment } from '../../models/equipment.model';
import { EquipmentType } from '../../models/equipment-type.model';
import { Employee } from '../../models/employee.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-equipment',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './equipment.component.html',
  styleUrl: './equipment.component.css'
})
export class EquipmentComponent implements OnInit, OnDestroy {
  equipments: Equipment[] = [];
  types: EquipmentType[] = [];
  employees: Employee[] = [];
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

  form: Equipment = { name: '', typeId: 0, status: 'ACTIVE' };
  statuses = ['ACTIVE', 'INACTIVE', 'REPAIR', 'DECOMMISSIONED'];

  constructor(
    private equipmentService: EquipmentService,
    private typeService: EquipmentTypeService,
    private employeeService: EmployeeService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
    this.isAdmin = this.authService.isAdmin();
    this.typeService.getAll().subscribe(data => this.types = data);
    this.employeeService.getAll().subscribe(data => this.employees = data);

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
    this.equipmentService.getPage(this.currentPage, this.pageSize, this.searchText).subscribe(page => {
      this.equipments = page.content;
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
    this.form = { name: '', typeId: 0, status: 'ACTIVE' };
    this.editing = false;
    this.editId = null;
    this.showForm = true;
  }

  openEdit(item: Equipment): void {
    this.form = { ...item };
    this.editing = true;
    this.editId = item.id!;
    this.showForm = true;
  }

  cancel(): void { this.showForm = false; }

  save(): void {
    if (this.editing && this.editId) {
      this.equipmentService.update(this.editId, this.form).subscribe(() => { this.showForm = false; this.load(); });
    } else {
      this.equipmentService.create(this.form).subscribe(() => { this.showForm = false; this.load(); });
    }
  }

  delete(id: number): void {
    if (confirm('Удалить оборудование?')) {
      this.equipmentService.delete(id).subscribe(() => {
        if (this.equipments.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.load();
      });
    }
  }
}
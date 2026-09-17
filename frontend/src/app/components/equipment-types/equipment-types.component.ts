import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { EquipmentTypeService } from '../../services/equipment-type.service';
import { EquipmentType } from '../../models/equipment-type.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-equipment-types',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './equipment-types.component.html',
  styleUrl: './equipment-types.component.css'
})
export class EquipmentTypesComponent implements OnInit, OnDestroy {
  items: EquipmentType[] = [];
  showForm = false;
  editing = false;
  editId: number | null = null;
  isAdmin = false;

  // --- пагинация ---
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [10, 20, 50];

  // --- поиск ---
  searchControl = new FormControl('');
  searchText = '';

  // для отписки от потока при уничтожении компонента
  private destroy$ = new Subject<void>();

  form: EquipmentType = {
    typeName: '', manufacturer: '', model: '',
    defaultPortCount: undefined, connectionType: '', osiLevel: '', description: ''
  };

  constructor(
    private service: EquipmentTypeService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
    this.isAdmin = this.authService.isAdmin();

    // подписываемся на изменения поля поиска с debounce
    this.searchControl.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(value => {
        this.searchText = value || '';
        this.currentPage = 0;   // при новом поиске возвращаемся на первую страницу
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

  clearSearch(): void {
    this.searchControl.setValue('');   // это вызовет valueChanges → load()
  }

  // --- пагинация (без изменений) ---
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

  // --- CRUD (без изменений) ---
  openCreate(): void {
    this.form = {
      typeName: '', manufacturer: '', model: '', snmpObjectId: '',
      defaultPortCount: undefined, connectionType: '', osiLevel: '', description: ''
    };
    this.editing = false;
    this.editId = null;
    this.showForm = true;
  }

  openEdit(item: EquipmentType): void {
    this.form = { ...item };
    this.editing = true;
    this.editId = item.id!;
    this.showForm = true;
  }

  cancel(): void { this.showForm = false; }

  save(): void {
    if (this.editing && this.editId) {
      this.service.update(this.editId, this.form).subscribe({
        next: () => { this.showForm = false; this.load(); },
        error: err => console.error('Ошибка обновления:', err)
      });
    } else {
      this.service.create(this.form).subscribe({
        next: () => { this.showForm = false; this.load(); },
        error: err => console.error('Ошибка создания:', err)
      });
    }
  }

  delete(id: number): void {
    if (confirm('Удалить тип оборудования?')) {
      this.service.delete(id).subscribe(() => {
        if (this.items.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.load();
      });
    }
  }
}
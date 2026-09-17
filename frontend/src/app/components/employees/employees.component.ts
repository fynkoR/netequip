import { Component, OnInit, ChangeDetectorRef , OnDestroy} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeeService } from '../../services/employee.service';
import { Employee } from '../../models/employee.model';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { User, UserRole } from '../../models/user.model';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

@Component({
  selector: 'app-employees',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './employees.component.html',
  styleUrl: './employees.component.css'
})
export class EmployeesComponent implements OnInit, OnDestroy {
  employees: Employee[] = [];
  showForm = false;
  editing = false;
  editId: number | null = null;
  isAdmin = false;

  form: Employee = { fullName: '', position: 'ENGINEER', email: '' };
  positions = ['ADMIN', 'ENGINEER', 'TECHNIC', 'VIEWER'];

  // === Управление учётной записью ===
  showAccountModal = false;
  accountEmployee: Employee | null = null;
  linkedUser: User | null = null;          // привязанный к этому сотруднику юзер
  allUsers: User[] = [];                    // все юзеры, для выбора непривязанного
  selectedUserId: number | null = null;     // что выбрал админ в селекте "Привязать"
  selectedRole: UserRole = 'VIEWER';

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

  readonly roles: UserRole[] = ['VIEWER', 'TECHNIC', 'ENGINEER', 'ADMIN'];

  constructor(
    private employeeService: EmployeeService,
    public authService: AuthService,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
    this.isAdmin = this.authService.isAdmin();

    this.searchControl.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(value => {
        this.searchText = value || '';
        this.currentPage = 0;
        this.loadEmployees();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadEmployees(): void {
    this.employeeService.getPage(this.currentPage, this.pageSize, this.searchText).subscribe({
      next: page => {
        this.employees = page.content;
        this.totalElements = page.totalElements;
        this.totalPages = page.totalPages;
        this.cdr.detectChanges();
      },
      error: err => console.error('Error loading employees:', err)
    });
  }

  openCreate(): void {
    this.form = { fullName: '', position: 'ENGINEER', email: '' };
    this.editing = false;
    this.editId = null;
    this.showForm = true;
  }

  openEdit(emp: Employee): void {
    this.form = { ...emp };
    this.editing = true;
    this.editId = emp.id!;
    this.showForm = true;
  }

  cancel(): void {
    this.showForm = false;
  }

  save(): void {
    if (this.editing && this.editId) {
      this.employeeService.update(this.editId, this.form).subscribe(() => {
        this.showForm = false;
        this.loadEmployees();
      });
    } else {
      this.employeeService.create(this.form).subscribe(() => {
        this.showForm = false;
        this.loadEmployees();
      });
    }
  }

  delete(id: number): void {
    if (confirm('Удалить сотрудника?')) {
      this.employeeService.delete(id).subscribe(() => {
        if (this.employees.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.loadEmployees();
      });
    }
  }

  openAccountModal(emp: Employee): void {
  this.accountEmployee = emp;
  this.showAccountModal = true;
  this.refreshAccountModal();
  }

  closeAccountModal(): void {
    this.showAccountModal = false;
    this.accountEmployee = null;
    this.linkedUser = null;
    this.selectedUserId = null;
  }

  private refreshAccountModal(): void {
    if (!this.accountEmployee) return;
    this.userService.getAll().subscribe(users => {
      this.allUsers = users;
      this.linkedUser = users.find(u => u.employeeId === this.accountEmployee!.id) || null;
      this.selectedRole = this.linkedUser?.role ?? 'VIEWER';
      this.selectedUserId = null;
      this.cdr.detectChanges();
    });
  }

  get unlinkedUsers(): User[] {
    return this.allUsers.filter(u => !u.employeeId);
  }

  linkUser(): void {
    if (!this.selectedUserId || !this.accountEmployee) return;
    this.userService.update(this.selectedUserId, {
      employeeId: this.accountEmployee.id
    }).subscribe(() => this.refreshAccountModal());
  }

  unlinkUser(): void {
    const u = this.linkedUser;
    if (!u?.id) return;
    if (!confirm('Отвязать учётную запись от сотрудника?')) return;
    this.userService.update(u.id, { employeeId: null })
      .subscribe(() => this.refreshAccountModal());
  }

  changeRole(): void {
    const u = this.linkedUser;
    if (!u?.id) return;
    this.userService.update(u.id, {
      employeeId: u.employeeId,
      role: this.selectedRole
    }).subscribe(() => this.refreshAccountModal());
  }

  deleteUser(): void {
    const u = this.linkedUser;
    if (!u?.id) return;     // ← здесь TS уже знает, что u есть и u.id есть
    if (!confirm(`Удалить учётную запись "${u.username}"? Сотрудник останется, привязка пропадёт.`)) return;
    this.userService.delete(u.id).subscribe({
      next: () => this.refreshAccountModal(),
      error: err => alert(err?.error?.message || err?.error || 'Ошибка удаления')
    });
  }
  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadEmployees();
  }
  nextPage(): void { this.goToPage(this.currentPage + 1); }
  prevPage(): void { this.goToPage(this.currentPage - 1); }

  changePageSize(newSize: number): void {
    this.pageSize = +newSize;
    this.currentPage = 0;
    this.loadEmployees();
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }
}

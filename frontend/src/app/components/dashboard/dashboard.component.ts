import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DevicePortService } from '../../services/device-port.service';
import { EmployeeService } from '../../services/employee.service';
import { EquipmentService } from '../../services/equipment.service';
import { EquipmentTypeService } from '../../services/equipment-type.service';
import { IpAddressService } from '../../services/ip-address.service';
import { MaintenanceHistoryService } from '../../services/maintenance-history.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  portsCount = 0;
  employeeCount = 0;
  equipmentCount = 0;
  typesCount = 0;
  ipCount = 0;
  historiesCount = 0;
  username = '';

  constructor(
    private devicePortService: DevicePortService,
    private employeeService: EmployeeService,
    private equipmentService: EquipmentService,
    private typesService: EquipmentTypeService,
    private ipService: IpAddressService,
    private historiesService: MaintenanceHistoryService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.username = user?.fullName || user?.username || '';
    this.cdr.detectChanges();
    this.devicePortService.getAll().subscribe(data => {
      this.portsCount = data.length
      this.cdr.detectChanges();
    });
    this.employeeService.getAll().subscribe(data => {
      this.employeeCount = data.length
      this.cdr.detectChanges();
    });
    this.equipmentService.getAll().subscribe(data => {
      this.equipmentCount = data.length
      this.cdr.detectChanges();
    });
    this.typesService.getAll().subscribe(data => {
      this.typesCount = data.length
      this.cdr.detectChanges();
    });
    this.ipService.getAll().subscribe(data => {
      this.ipCount = data.length
      this.cdr.detectChanges();
    });
    this.historiesService.getAll().subscribe(data => {
      this.historiesCount = data.length
      this.cdr.detectChanges();
    });
  }
}

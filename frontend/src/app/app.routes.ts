import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { LayoutComponent } from './components/layout/layout.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { EmployeesComponent } from './components/employees/employees.component';
import { EquipmentComponent } from './components/equipment/equipment.component';
import { EquipmentTypesComponent } from './components/equipment-types/equipment-types.component';
import { IpAddressesComponent } from './components/ip-addresses/ip-addresses.component';
import { DevicePortsComponent } from './components/device-ports/device-ports.component';
import { MaintenanceHistoryComponent } from './components/maintenance-history/maintenance-history.component';
import { DiscoveryComponent } from './components/discovery/discovery.component';
import { AuditComponent } from './components/audit/audit.component';


export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'employees', component: EmployeesComponent },
      { path: 'equipment', component: EquipmentComponent },
      { path: 'equipment-types', component: EquipmentTypesComponent },
      { path: 'ip-addresses', component: IpAddressesComponent },
      { path: 'device-ports', component: DevicePortsComponent },
      { path: 'audit', component: AuditComponent },
      { path: 'maintenance', component: MaintenanceHistoryComponent },
      { path: 'discovery', component: DiscoveryComponent },
      {
        path: 'topology',
        loadComponent: () =>
          import('./components/topology/topology.component').then(m => m.TopologyComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'login' }
];

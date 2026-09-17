export interface MaintenanceHistory {
  id?: number;
  equipmentId: number;
  equipmentName?: string;
  date: string;
  type: string;
  description?: string;
  performedById?: number;
  performedByName?: string;
  cost?: number;
  nextMaintenanceDate?: string;
}

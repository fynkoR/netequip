export interface AuditLog {
  id: number;
  timestamp: string;
  username: string;
  employeeId?: number;
  employeeFullName?: string;
  action: string;
  entityType: string;
  entityId?: number;
  entityName?: string;
  description?: string;
}
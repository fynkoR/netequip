export interface Equipment {
  id?: number;
  typeId: number;
  typeName?: string;
  employeeId?: number;
  employeeFullName?: string;
  name: string;
  serialNumber?: string;
  macAddress?: string;
  ipAddress?: string;
  address?: string;
  status?: string;
  dateAdded?: string;
  dateUpdated?: string;
}

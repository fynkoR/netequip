export interface IpAddress {
  id?: number;
  equipmentId: number;
  equipmentName?: string;
  ipAddress: string;
  subnetMask?: string;
  gateway?: string;
  networkType?: string;
  isPrimary?: boolean;
  assignedDate?: string;
}

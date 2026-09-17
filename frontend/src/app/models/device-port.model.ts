export interface DevicePort {
  id?: number;
  equipmentId: number;
  equipmentName?: string;
  portNumber: number;
  portType?: string;
  status: string;
  speed?: string;
  connectedToEquipmentId?: number;
  connectedToEquipmentName?: string;
  connectedToPortId?: number;
  connectedToPortNumber?: number;
  description?: string;
}

export interface EquipmentType {
  id?: number;
  typeName: string;
  manufacturer?: string;
  model?: string;
  snmpObjectId?: string;
  defaultPortCount?: number;
  connectionType?: string;
  osiLevel?: string;
  description?: string;
}

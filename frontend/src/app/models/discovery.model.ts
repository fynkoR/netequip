// Структуры, зеркалирующие DTO бэкенда модуля discovery.

export interface SnmpSystemInfo {
  ipAddress: string;
  description: string;
  objectId: string;
  name: string;
  location: string;
  uptime: number;
}

export interface SnmpPortInfo {
  ifIndex: number;
  description: string;
  ifType: number;
  speed: number;
  macAddress: string;
  adminStatus: number;
  operStatus: number;
}

export interface SnmpIpInfo {
  ipAddress: string;
  ifIndex: number;
  subnetMask: string;
}

export interface SnmpDeviceInfo {
  system: SnmpSystemInfo;
  ports: SnmpPortInfo[];
  ipAddresses: SnmpIpInfo[];
}

// Уровень достоверности автосопоставления типа
export type MatchConfidence = 'EXACT' | 'HEURISTIC' | 'NONE';

export interface DiscoveredDevice {
  snmpData: SnmpDeviceInfo;
  suggestedTypeId: number | null;
  suggestedTypeName: string | null;
  matchConfidence: MatchConfidence;
}

// Запрос на сканирование
export interface ScanRequest {
  cidr: string;
  port: number;
  community: string;
}

// Ответ от /scan
export interface ScanResult {
  cidr: string;
  totalAddressesScanned: number;
  durationMs: number;
  discoveredDevices: DiscoveredDevice[];
}

// DTO для отправки на /import
export interface ImportDeviceItem {
  snmpData: SnmpDeviceInfo;
  typeId: number;
}

export interface ImportRequest {
  devices: ImportDeviceItem[];
}

// Ответ от /import
export type ImportStatus = 'CREATED' | 'SKIPPED' | 'FAILED';

export interface ImportDeviceReport {
  ipAddress: string;
  deviceName: string;
  status: ImportStatus;
  equipmentId: number | null;
  portsCreated: number;
  ipsCreated: number;
  message: string;
}

export interface ImportResult {
  requestedCount: number;
  createdCount: number;
  skippedCount: number;
  failedCount: number;
  reports: ImportDeviceReport[];
}
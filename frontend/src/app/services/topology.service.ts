import { Injectable } from '@angular/core';
import { forkJoin, Observable, map } from 'rxjs';
import { EquipmentService } from './equipment.service';
import { DevicePortService } from './device-port.service';
import { Equipment } from '../models/equipment.model';
import { DevicePort } from '../models/device-port.model';
import { TopologyData, TopologyNode, TopologyEdge } from '../models/topology.model';

@Injectable({ providedIn: 'root' })
export class TopologyService {

  /** Цвета узлов по статусу устройства */
  private readonly STATUS_COLORS: Record<string, { bg: string; border: string }> = {
    ACTIVE:      { bg: '#22c55e', border: '#15803d' }, // зелёный
    INACTIVE:    { bg: '#94a3b8', border: '#475569' }, // серый
    MAINTENANCE: { bg: '#f59e0b', border: '#b45309' }, // оранжевый
    RETIRED:     { bg: '#ef4444', border: '#991b1b' }, // красный
  };
  private readonly DEFAULT_COLOR = { bg: '#64748b', border: '#334155' };

  /** Цвета рёбер по статусу порта */
  private readonly EDGE_COLOR_CONNECTED  = '#3b82f6'; // синий
  private readonly EDGE_COLOR_DISABLED   = '#94a3b8'; // серый
  private readonly EDGE_COLOR_ANOMALY    = '#ef4444'; // красный — рассогласование данных

  constructor(
    private equipmentService: EquipmentService,
    private devicePortService: DevicePortService
  ) {}

  /**
   * Собирает граф топологии: тянет устройства и порты параллельно,
   * затем строит узлы/рёбра.
   */
  getTopology(): Observable<TopologyData> {
    return forkJoin({
      equipments: this.equipmentService.getAll(),
      ports: this.devicePortService.getAll()
    }).pipe(
      map(({ equipments, ports }) => this.buildGraph(equipments, ports))
    );
  }

  // ===== Внутренняя логика =====

  private buildGraph(equipments: Equipment[], ports: DevicePort[]): TopologyData {
    const nodes = equipments.map(e => this.toNode(e));
    const edges = this.buildEdges(ports);
    return { nodes, edges };
  }

  private toNode(eq: Equipment): TopologyNode {
    const status = (eq.status ?? '').toUpperCase();
    const palette = this.STATUS_COLORS[status] ?? this.DEFAULT_COLOR;

    // Двухстрочная подпись: имя + IP (если есть)
    const label = eq.ipAddress
      ? `${eq.name}\n${eq.ipAddress}`
      : eq.name;

    // HTML-тултип (vis-network умеет HTML, если передать DOM-элемент;
    // строку он отрисует как plain text — этого нам пока достаточно)
    const title = [
      `Имя: ${eq.name}`,
      eq.typeName     ? `Тип: ${eq.typeName}`               : null,
      eq.ipAddress    ? `IP: ${eq.ipAddress}`               : null,
      eq.macAddress   ? `MAC: ${eq.macAddress}`             : null,
      eq.serialNumber ? `S/N: ${eq.serialNumber}`           : null,
      `Статус: ${status || '—'}`
    ].filter(Boolean).join('\n');

    return {
      id: eq.id!,
      label,
      title,
      shape: 'box',
      group: status,
      color: {
        background: palette.bg,
        border: palette.border,
        highlight: { background: palette.bg, border: '#1e293b' }
      },
      meta: {
        typeName: eq.typeName,
        ipAddress: eq.ipAddress,
        macAddress: eq.macAddress,
        serialNumber: eq.serialNumber,
        status
      }
    };
  }

  private buildEdges(ports: DevicePort[]): TopologyEdge[] {
    const edgesMap = new Map<string, TopologyEdge>();

    for (const port of ports) {
      // Соединение есть, только если заполнены ОБА поля
      if (!port.connectedToPortId || !port.connectedToEquipmentId) continue;
      if (!port.id) continue;

      // Нормализованный ключ — пара id портов, отсортированная
      const key = [port.id, port.connectedToPortId].sort((a, b) => a - b).join('-');
      if (edgesMap.has(key)) continue; // парное ребро уже добавлено

      const status = (port.status ?? '').toUpperCase();
      const isDisabled = status === 'DISABLE';
      // Аномалия: порт говорит "не подключён", но connectedTo заполнен
      const isAnomaly = status === 'NOT_CONNECTED';

      let color: string;
      if (isAnomaly)       color = this.EDGE_COLOR_ANOMALY;
      else if (isDisabled) color = this.EDGE_COLOR_DISABLED;
      else                 color = this.EDGE_COLOR_CONNECTED;

      const label = `#${port.portNumber} ↔ #${port.connectedToPortNumber}`;
      const title = [
        `${port.equipmentName} (порт #${port.portNumber})`,
        `↕`,
        `${port.connectedToEquipmentName} (порт #${port.connectedToPortNumber})`,
        port.portType ? `Тип: ${port.portType}` : null,
        port.speed    ? `Скорость: ${port.speed}` : null,
        `Статус: ${status}`
      ].filter(Boolean).join('\n');

      edgesMap.set(key, {
        id: key,
        from: port.equipmentId,
        to: port.connectedToEquipmentId,
        label,
        title,
        color,
        dashes: isDisabled,
        width: isAnomaly ? 3 : 2
      });
    }

    return Array.from(edgesMap.values());
  }
}
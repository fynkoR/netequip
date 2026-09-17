/**
 * Узел графа топологии — соответствует одному устройству (Equipment).
 * Поля соответствуют формату vis-network: https://visjs.github.io/vis-network/docs/network/nodes.html
 */
export interface TopologyNode {
  /** ID устройства из БД (Equipment.id) */
  id: number;

  /** Подпись на узле (имя устройства, можно с переносом строки \n для второй строки) */
  label: string;

  /** HTML-тултип при наведении (тип, IP, MAC, статус) */
  title?: string;

  /** Форма узла: 'box' | 'ellipse' | 'database' | 'dot' | 'diamond' | 'triangle' | 'star' */
  shape?: string;

  /** Цвет узла. Можно строкой или объектом с border/background/highlight */
  color?: string | {
    background: string;
    border: string;
    highlight?: { background: string; border: string };
  };

  /** Группа для фильтрации/легенды — кладём сюда статус устройства */
  group?: string;

  /** Дополнительные поля для боковой панели (не нужны vis-network, но удобны нам) */
  meta?: {
    typeName?: string;
    ipAddress?: string;
    macAddress?: string;
    serialNumber?: string;
    status?: string;
  };
}

/**
 * Ребро графа — соединение порт-в-порт между двумя устройствами.
 */
export interface TopologyEdge {
  /** Уникальный id ребра — нормализованный ключ пары портов, например "12-37" */
  id: string;

  /** id устройства-источника (Equipment.id) */
  from: number;

  /** id устройства-приёмника (Equipment.id) */
  to: number;

  /** Подпись на ребре, например "#1 ↔ #24" */
  label?: string;

  /** Тултип с деталями (скорость, тип портов, описание) */
  title?: string;

  /** Цвет линии */
  color?: string | { color: string; highlight?: string; hover?: string };

  /** Пунктир — для отключённых соединений (DISABLE) */
  dashes?: boolean;

  /** Толщина линии */
  width?: number;
}

/**
 * Итоговая структура, которую отдаёт TopologyService и принимает компонент.
 */
export interface TopologyData {
  nodes: TopologyNode[];
  edges: TopologyEdge[];
}
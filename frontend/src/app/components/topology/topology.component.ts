import {
  Component,
  OnInit,
  AfterViewInit,
  OnDestroy,
  ElementRef,
  ViewChild,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TopologyService } from '../../services/topology.service';
import { TopologyData, TopologyNode, TopologyEdge } from '../../models/topology.model';

import type { Network, Options } from 'vis-network';
import type { DataSet } from 'vis-data';

interface StatusLegendItem {
  key: string;
  label: string;
  color: string;
  enabled: boolean;
}

@Component({
  selector: 'app-topology',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './topology.component.html',
  styleUrl: './topology.component.css'
})
export class TopologyComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('networkContainer', { static: false })
  containerRef!: ElementRef<HTMLDivElement>;

  graphData: TopologyData | null = null;
  loading = false;
  errorMessage: string | null = null;

  /** Выбранный узел — показываем в боковой панели */
  selectedNode: TopologyNode | null = null;
  /** Соседи выбранного узла (для панели деталей) */
  selectedNeighbors: TopologyNode[] = [];

  /** Состояние физики */
  physicsEnabled = true;

  /** Легенда + чекбоксы фильтрации */
  legend: StatusLegendItem[] = [
    { key: 'ACTIVE',      label: 'Активно',       color: '#22c55e', enabled: true },
    { key: 'INACTIVE',    label: 'Неактивно',     color: '#94a3b8', enabled: true },
    { key: 'MAINTENANCE', label: 'Обслуживание',  color: '#f59e0b', enabled: true },
    { key: 'RETIRED',     label: 'Списано',       color: '#ef4444', enabled: true }
  ];

  private network?: Network;
  private nodesDataSet?: DataSet<any>;
  private edgesDataSet?: DataSet<any>;
  private viewReady = false;

  constructor(
    private topologyService: TopologyService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadTopology();
  }

  private resizeListener = () => {
    this.network?.redraw();
    this.network?.fit({ animation: false });
  };

  ngAfterViewInit(): void {
    this.viewReady = true;
    if (this.graphData) this.renderNetwork();
    window.addEventListener('resize', this.resizeListener);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.resizeListener);
    this.network?.destroy();
    this.network = undefined;
  }

  loadTopology(): void {
    this.loading = true;
    this.errorMessage = null;
    this.selectedNode = null;

    this.topologyService.getTopology().subscribe({
      next: (data) => {
        this.graphData = data;
        this.loading = false;
        this.cdr.detectChanges();
        if (this.viewReady) this.renderNetwork();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Не удалось загрузить топологию: ' + (err?.message ?? 'неизвестная ошибка');
        this.cdr.detectChanges();
        console.error('[Topology] Ошибка загрузки:', err);
      }
    });
  }

  // ===== Рендер =====

  private async renderNetwork(): Promise<void> {
    if (!this.graphData) return;
    if (!this.containerRef?.nativeElement) return;

    const { Network } = await import('vis-network/standalone');
    const { DataSet } = await import('vis-data');

    this.network?.destroy();

    this.nodesDataSet = new DataSet(this.graphData.nodes as any);
    this.edgesDataSet = new DataSet(this.graphData.edges as any);

    const options: Options = {
      autoResize: false,                 // ← добавить
      width: '100%',                     // ← добавить
      height: '100%',                    // ← добавить
      nodes: {
        shape: 'box',
        margin: { top: 12, right: 14, bottom: 12, left: 14 } as any,
        font: { size: 14, color: '#ffffff', face: 'Inter, system-ui, sans-serif' },
        borderWidth: 2,
        shadow: { enabled: true, size: 8, x: 0, y: 2, color: 'rgba(0,0,0,0.15)' }
      },
      edges: {
        arrows: { to: { enabled: false } },
        smooth: { enabled: true, type: 'continuous', roundness: 0.4 },
        font: {
          size: 11, align: 'middle', color: '#475569',
          background: 'rgba(255,255,255,0.85)', strokeWidth: 0
        },
        width: 2,
        selectionWidth: 3
      },
      physics: {
        enabled: this.physicsEnabled,
        solver: 'forceAtlas2Based',
        forceAtlas2Based: {
          gravitationalConstant: -50,
          springLength: 130,
          springConstant: 0.08,
          avoidOverlap: 0.6
        },
        stabilization: { enabled: true, iterations: 250, fit: true }
      },
      interaction: {
        hover: true,
        tooltipDelay: 150,
        navigationButtons: true,
        keyboard: { enabled: true },
        zoomView: true,
        dragView: true
      },
      layout: { improvedLayout: true }
    };

    this.network = new Network(
      this.containerRef.nativeElement,
      { nodes: this.nodesDataSet, edges: this.edgesDataSet },
      options
    );

    this.bindEvents();

    this.network.once('stabilizationIterationsDone', () => {
      this.network?.fit({ animation: { duration: 400, easingFunction: 'easeInOutQuad' } });
    });
  }

  private bindEvents(): void {
    if (!this.network) return;

    // Клик: либо узел выбран — показать панель, либо клик в пустоту — снять выделение
    this.network.on('click', (params: any) => {
      if (params.nodes && params.nodes.length > 0) {
        this.onSelectNode(params.nodes[0] as number);
      } else {
        this.clearSelection();
      }
    });

    // Двойной клик — приблизить и центрировать выбранный узел
    this.network.on('doubleClick', (params: any) => {
      if (params.nodes && params.nodes.length > 0) {
        this.network?.focus(params.nodes[0], {
          scale: 1.4,
          animation: { duration: 500, easingFunction: 'easeInOutQuad' }
        });
      }
    });
  }

  // ===== Выбор узла и подсветка соседей =====

  private onSelectNode(nodeId: number): void {
    if (!this.graphData) return;

    const node = this.graphData.nodes.find(n => n.id === nodeId) ?? null;
    this.selectedNode = node;

    // Соседи: устройства, к которым ведут рёбра из/в данный узел
    const neighborIds = new Set<number>();
    for (const e of this.graphData.edges) {
      if (e.from === nodeId) neighborIds.add(e.to);
      if (e.to === nodeId)   neighborIds.add(e.from);
    }
    this.selectedNeighbors = this.graphData.nodes
      .filter(n => neighborIds.has(n.id));

    this.cdr.detectChanges();
  }

  private clearSelection(): void {
    this.selectedNode = null;
    this.selectedNeighbors = [];
    this.cdr.detectChanges();
  }

  /** Из шаблона — закрыть боковую панель крестиком */
  closeDetails(): void {
    this.clearSelection();
    this.network?.unselectAll();
  }

  /** Из боковой панели — переключиться на соседа */
  focusNeighbor(nodeId: number): void {
    this.network?.selectNodes([nodeId]);
    this.network?.focus(nodeId, {
      scale: 1.3,
      animation: { duration: 400, easingFunction: 'easeInOutQuad' }
    });
    this.onSelectNode(nodeId);
  }

  // ===== Управление =====

  togglePhysics(): void {
    this.physicsEnabled = !this.physicsEnabled;
    this.network?.setOptions({ physics: { enabled: this.physicsEnabled } });
  }

  fitToScreen(): void {
    this.network?.fit({ animation: { duration: 500, easingFunction: 'easeInOutQuad' } });
  }

  // ===== Фильтр по статусу =====

  toggleStatusFilter(item: StatusLegendItem): void {
    item.enabled = !item.enabled;
    this.applyFilter();
  }


  private applyFilter(): void {
    if (!this.graphData || !this.nodesDataSet || !this.edgesDataSet) return;

    const enabledStatuses = new Set(
      this.legend.filter(l => l.enabled).map(l => l.key)
    );

    // Какие узлы видимы
    const visibleNodeIds = new Set(
      this.graphData.nodes
        .filter(n => enabledStatuses.has(n.group ?? ''))
        .map(n => n.id)
    );

    

    // Рёбра показываем только между видимыми узлами
    const filteredEdges = this.graphData.edges
      .filter(e => visibleNodeIds.has(e.from) && visibleNodeIds.has(e.to));

    // Пересобираем DataSet'ы
    this.nodesDataSet.clear();
    this.nodesDataSet.add(this.graphData.nodes.filter(n => visibleNodeIds.has(n.id)) as any);
    this.edgesDataSet.clear();
    this.edgesDataSet.add(filteredEdges as any);
  }
}
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

import { ChartPoint } from '../../models/seller-dashboard';

@Component({
  selector: 'app-analytics-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analytics-chart.html',
  styleUrl: './analytics-chart.scss',
})
export class AnalyticsChart {
  @Input({ required: true }) title = '';
  @Input({ required: true }) subtitle = '';
  @Input({ required: true }) data: ChartPoint[] = [];
  @Input() mode: 'line' | 'bar' = 'line';

  get maxValue(): number {
    return Math.max(...this.data.map((point) => point.value), 1);
  }

  get polylinePoints(): string {
    if (!this.data.length) {
      return '';
    }

    return this.data
      .map((point, index) => {
        const x = this.data.length === 1 ? 20 : (index / (this.data.length - 1)) * 280 + 20;
        const y = 150 - (point.value / this.maxValue) * 110;
        return `${x},${y}`;
      })
      .join(' ');
  }

  get areaPoints(): string {
    return `20,150 ${this.polylinePoints} 300,150`;
  }
}
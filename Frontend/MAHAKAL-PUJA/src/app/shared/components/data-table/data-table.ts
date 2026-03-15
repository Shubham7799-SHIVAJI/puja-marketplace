import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, Input } from '@angular/core';

import { BadgeCell, ImageTextCell, SellerTableColumn, SellerTableRow } from '../../models/seller-dashboard';

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe],
  templateUrl: './data-table.html',
  styleUrl: './data-table.scss',
})
export class DataTable {
  @Input({ required: true }) title = '';
  @Input({ required: true }) subtitle = '';
  @Input({ required: true }) columns: SellerTableColumn[] = [];
  @Input({ required: true }) rows: SellerTableRow[] = [];

  isImageTextCell(value: unknown): value is ImageTextCell {
    return !!value && typeof value === 'object' && 'image' in value && 'title' in value;
  }

  isBadgeCell(value: unknown): value is BadgeCell {
    return !!value && typeof value === 'object' && 'label' in value;
  }

  getCell(row: SellerTableRow, key: string): unknown {
    return row[key];
  }

  getCurrencyValue(row: SellerTableRow, key: string): number {
    const value = row[key];
    return typeof value === 'number' ? value : Number(value) || 0;
  }

  getDateValue(row: SellerTableRow, key: string): string | number | Date | null {
    const value = row[key];
    return typeof value === 'string' || typeof value === 'number' || value instanceof Date ? value : null;
  }

  getBadgeCell(row: SellerTableRow, key: string): BadgeCell | null {
    const value = row[key];
    return this.isBadgeCell(value) ? value : null;
  }

  getImageTextCell(row: SellerTableRow, key: string): ImageTextCell | null {
    const value = row[key];
    return this.isImageTextCell(value) ? value : null;
  }

  getListCell(row: SellerTableRow, key: string): string[] {
    const value = row[key];
    return Array.isArray(value) ? value.map((item) => String(item)) : [];
  }
}
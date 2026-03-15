import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

import { NotificationItem } from '../../models/seller-dashboard';

@Component({
  selector: 'app-notifications-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications-panel.html',
  styleUrl: './notifications-panel.scss',
})
export class NotificationsPanel {
  @Input({ required: true }) notifications: NotificationItem[] = [];
}
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-seller-form-field',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './seller-form-field.html',
  styleUrl: './seller-form-field.scss',
})
export class SellerFormField {
  @Input({ required: true }) label = '';
  @Input() hint = '';
}
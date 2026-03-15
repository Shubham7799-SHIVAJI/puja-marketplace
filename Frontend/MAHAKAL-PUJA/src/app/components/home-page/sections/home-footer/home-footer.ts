import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-home-footer',
  imports: [RouterLink],
  templateUrl: './home-footer.html',
  styleUrl: './home-footer.scss',
})
export class HomeFooter {
  readonly currentYear = new Date().getFullYear();
}

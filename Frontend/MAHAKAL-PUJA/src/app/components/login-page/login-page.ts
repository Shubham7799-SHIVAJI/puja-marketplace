import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../shared/services/auth';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-login-page',
  imports: [
    // Angular form support
    ReactiveFormsModule,
    RouterLink,CommonModule
  ],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage implements OnInit {
  // '!' tells TypeScript that we'll initialize this in ngOnInit
  loginForm!: FormGroup;
  submitted = false;

  // country code logic removed; only one contact field now

  constructor(private fb: FormBuilder, private authService: AuthService) {}
  

  ngOnInit() {
    this.loginForm = this.fb.group({
      // require non‑empty and not just whitespace
      name: ['', [Validators.required, Validators.pattern(/\S+/)]],
      contact: ['', [Validators.required, this.emailOrPhoneValidator.bind(this)]]
    });
  }

  // custom validator to ensure contact is either valid email or phone number
  emailOrPhoneValidator(control: AbstractControl): ValidationErrors | null {
    const value: string = control.value;
    if (!value) {
      return null;
    }
    const emailRegex = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
    const phoneRegex = /^[0-9]{10}$/; // exactly 10 digits
    if (emailRegex.test(value) || phoneRegex.test(value)) {
      return null;
    }
    return { invalidContact: true };
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit() {
    this.submitted = true;
    if (this.loginForm.invalid) {
      // mark every control as touched so the template can show errors
      this.loginForm.markAllAsTouched();
      return;
    }
    const formValue = this.loginForm.value;
    const payload = {
      name: formValue.name,
      contact: formValue.contact
    };
    console.log('Form submitted', payload);
    // determine whether entered contact looks like an email
    const emailRegex = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
    const label = emailRegex.test(payload.contact) ? 'Email id' : 'Phone number';
    alert(`Name: ${payload.name}\n${label}: ${payload.contact}`);

    // call the login API
    this.authService.login(payload).subscribe({
    next: (res) => {
      console.log("Response from backend:", res);
    },
    error: (err) => {
      console.error("Backend error:", err);
    }
  });
  }

}

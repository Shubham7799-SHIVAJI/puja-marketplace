import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShopRegistration } from './shop-registration';

describe('ShopRegistration', () => {
  let component: ShopRegistration;
  let fixture: ComponentFixture<ShopRegistration>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShopRegistration]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShopRegistration);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

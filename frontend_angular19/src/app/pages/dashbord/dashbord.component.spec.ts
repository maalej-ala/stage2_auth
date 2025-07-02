import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashbordComponent } from './dashbord.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('DashbordComponent', () => {
  let component: DashbordComponent;
  let fixture: ComponentFixture<DashbordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashbordComponent,
        HttpClientTestingModule,  // Permet de mocker HttpClient dans les tests
        RouterTestingModule  ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashbordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { SignupComponent } from './pages/signup/signup.component';
import { DashbordComponent } from './pages/dashbord/dashbord.component';
import { AdminComponent } from './pages/admin/admin.component';
import { ProductManagementComponent } from './pages/product-management/product-management.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: SignupComponent },
   { path: 'dashboard', component: DashbordComponent },
    { path: 'admin', component: AdminComponent },
    { path: 'products', component: ProductManagementComponent },
    { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' } // Wildcard route for 404 pages
];
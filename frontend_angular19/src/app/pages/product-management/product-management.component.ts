import { Component, inject, OnInit } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface Product {
  id?: number;
  name: string;
  price: number;
}

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-management.component.html',
  styleUrls: ['./product-management.component.css']
})
export class ProductManagementComponent implements OnInit {
  private baseUrl = 'http://localhost:8081/api/products'; // Use '/api/products' if using proxy
  products: Product[] = [];
  product: Product = { name: '', price: 0 };
  isEditing = false;
  currentEditId: number | null = null;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  private router = inject(Router);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadProducts();
  }
goToDash(){
  this.router.navigate(['/dashboard']);
}
  loadProducts(): void {
    this.isLoading = true;
    this.http.get<Product[]>(this.baseUrl).subscribe({
      next: (products) => {
        this.products = products || [];
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        let message = 'Failed to load products';
        if (error.status === 0 && error.statusText === 'Unknown Error') {
          message = 'Cannot connect to the server. This may be due to a CORS issue or the backend not running at ' + this.baseUrl;
        } else {
          message += ': ' + (error.message || error.statusText);
        }
        this.showError(message);
        this.isLoading = false;
      }
    });
  }

  handleSubmit(form: NgForm): void {
    if (this.isLoading || !form.valid) return;

    this.isLoading = true;
    const request = this.isEditing && this.currentEditId
      ? this.http.put(`${this.baseUrl}/${this.currentEditId}`, this.product)
      : this.http.post(this.baseUrl, this.product);

    request.subscribe({
      next: () => {
        this.showSuccess(this.isEditing ? 'Product updated successfully!' : 'Product created successfully!');
        this.resetForm();
        this.loadProducts();
      },
      error: (error: HttpErrorResponse) => {
        let message = 'Operation failed';
        if (error.status === 0 && error.statusText === 'Unknown Error') {
          message = 'Cannot connect to the server. This may be due to a CORS issue or the backend not running.';
        } else {
          message += ': ' + (error.message || error.statusText);
        }
        this.showError(message);
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  editProduct(product: Product): void {
    this.product = { ...product };
    this.isEditing = true;
    this.currentEditId = product.id || null;
    this.hideMessages();
    document.querySelector('.form-section')?.scrollIntoView({ behavior: 'smooth' });
  }

  deleteProduct(id: number): void {
    if (!window.confirm('Are you sure you want to delete this product?')) return;

    this.isLoading = true;
    this.http.delete(`${this.baseUrl}/${id}`).subscribe({
      next: () => {
        this.showSuccess('Product deleted successfully!');
        this.loadProducts();
      },
      error: (error: HttpErrorResponse) => {
        let message = 'Failed to delete product';
        if (error.status === 0 && error.statusText === 'Unknown Error') {
          message = 'Cannot connect to the server. This may be due to a CORS issue or the backend not running.';
        } else {
          message += ': ' + (error.message || error.statusText);
        }
        this.showError(message);
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  resetForm(): void {
    this.product = { name: '', price: 0 };
    this.isEditing = false;
    this.currentEditId = null;
    this.hideMessages();
  }

  cancelEdit(): void {
    this.resetForm();
  }

  showSuccess(message: string): void {
    this.hideMessages();
    this.successMessage = message;
    setTimeout(() => this.hideMessages(), 3000);
  }

  showError(message: string): void {
    this.hideMessages();
    this.errorMessage = message;
    setTimeout(() => this.hideMessages(), 5000);
  }

  hideMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
  import { Component } from '@angular/core';
  import { CommonModule } from '@angular/common';
  import { FormsModule } from '@angular/forms'; // ✅ needed for ngModel
  import { NgIf, NgFor } from '@angular/common';
  import { User } from '../../services/auth.service';

  @Component({
    selector: 'app-admin',
    standalone: true,
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css'],
    imports: [CommonModule, FormsModule, NgIf, NgFor], // ✅ required in standalone
  })
  export class AdminComponent {
    users: User[] = [
      { id: '1', firstName: 'Alice', lastName: 'Smith', email: 'alice@example.com', role: 'USER' },
      { id: '2', firstName: 'Bob', lastName: 'Brown', email: 'bob@example.com', role: 'ADMIN' },
    ];

    editingUser: User | null = null;

    startEditing(user: User) {
      this.editingUser = { ...user };
    }

    cancelEditing() {
      this.editingUser = null;
    }

    saveUserChanges() {
      if (!this.editingUser) return;

      const index = this.users.findIndex(u => u.id === this.editingUser!.id);
      if (index !== -1) {
        this.users[index] = { ...this.editingUser };
      }

      this.editingUser = null;
    }
  }

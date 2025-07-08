// admin.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgIf, NgFor } from '@angular/common';
import { AuthService, User,  } from '../../services/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
  imports: [CommonModule, FormsModule, NgIf, NgFor],
})
export class AdminComponent implements OnInit {
  users: User[] = [];
  editingUser: User | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.authService.getAllUsers().subscribe({
      next: (users) => {
        // Ensure role is set to a default if undefined
        this.users = users.map(user => ({
          ...user,
          role: user.role || 'USER' // Default to 'USER' if role is undefined
        }));
      },
      error: (err) => {
        console.error('Failed to load users:', err);
        alert('Error loading users: ' + err.message);
      }
    });
  }

  startEditing(user: User): void {
    this.editingUser = { ...user };
  }

  saveUserChanges(): void {
    if (!this.editingUser) return;

    // Prepare the user data for update (assuming you have an update endpoint)
    const updatedUser: User = {
      id: this.editingUser.id,
      firstName: this.editingUser.firstName,
      lastName: this.editingUser.lastName,
      email: this.editingUser.email,
      role: this.editingUser.role as 'USER' | 'ADMIN' | 'DOCTOR' // Ensure role is one of the allowed values
    };

    // Note: You'll need an updateUser method in AuthService
    this.authService.updateUser(updatedUser).subscribe({
      next: () => {
        // Update local users array
        const index = this.users.findIndex(u => u.id === this.editingUser!.id);
        if (index !== -1) {
          this.users[index] = { ...this.editingUser! };
        }
        this.editingUser = null;
        alert('User updated successfully');
      },
      error: (err) => {
        console.error('Failed to update user:', err);
        alert('Error updating user: ' + err.message);
      }
    });
  }

  cancelEditing(): void {
    this.editingUser = null;
  }

  deleteUser(userId: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      // Note: You'll need a deleteUser method in AuthService
      this.authService.deleteUser(userId).subscribe({
        next: () => {
          this.users = this.users.filter(user => user.id !== userId);
          if (this.editingUser?.id === userId) {
            this.editingUser = null;
          }
          alert('User deleted successfully');
        },
        error: (err) => {
          console.error('Failed to delete user:', err);
          alert('Error deleting user: ' + err.message);
        }
      });
    }
  }
}
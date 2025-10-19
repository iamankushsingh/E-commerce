import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../services/user.service';
import { User, UserFilters } from '../../../model/user.model';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  loading = true;
  
  filters: UserFilters = {};
  showDeleteModal = false;
  userToDelete?: User;

  // Debounced search subject
  private searchSubject = new Subject<string>();

  constructor(private userService: UserService) {
    // Set up debounced search
    this.searchSubject.pipe(
      debounceTime(500), // Wait 500ms after user stops typing
      distinctUntilChanged() // Only emit if the value actually changed
    ).subscribe((searchTerm) => {
      this.filters.search = searchTerm;
      this.loadUsers();
    });
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userService.getUsers(this.filters).subscribe({
      next: (users) => {
        this.users = users;
        this.filteredUsers = users;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.loadUsers();
  }

  // Handle search input changes with debouncing
  onSearchChange(searchTerm: string): void {
    this.searchSubject.next(searchTerm);
  }

  // Handle non-search filter changes (immediate)
  applyNonSearchFilters(): void {
    this.loadUsers();
  }

  clearFilters(): void {
    this.filters = {};
    this.loadUsers();
  }

  blockUser(user: User): void {
    this.userService.blockUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        console.error('Error blocking user:', error);
      }
    });
  }

  unblockUser(user: User): void {
    this.userService.unblockUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        console.error('Error unblocking user:', error);
      }
    });
  }

  openDeleteModal(user: User): void {
    this.userToDelete = user;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.userToDelete = undefined;
  }

  confirmDelete(): void {
    if (this.userToDelete) {
      this.userService.deleteUser(this.userToDelete.id).subscribe({
        next: () => {
          this.loadUsers();
          this.closeDeleteModal();
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          this.closeDeleteModal();
        }
      });
    }
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      active: '#22c55e',
      ACTIVE: '#22c55e',
      blocked: '#ef4444',
      BLOCKED: '#ef4444',
      inactive: '#6b7280',
      INACTIVE: '#6b7280'
    };
    return colors[status] || '#6b7280';
  }

  getRoleColor(role: string): string {
    return (role === 'admin' || role === 'ADMIN') ? '#8b5cf6' : '#3b82f6';
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'CUSTOMER' | 'admin' | 'customer'; // Support both formats
  status: 'ACTIVE' | 'BLOCKED' | 'INACTIVE' | 'active' | 'blocked' | 'inactive'; // Support both formats
  createdAt: Date;
  lastLogin?: Date;
  avatar?: string;
  phoneNumber?: string;
  address?: string;
  updatedAt?: Date;
}

export interface UserFilters {
  role?: string;
  status?: string;
  search?: string;
}

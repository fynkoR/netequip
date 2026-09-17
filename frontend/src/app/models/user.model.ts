export type UserRole = 'VIEWER' | 'TECHNIC' | 'ENGINEER' | 'ADMIN';

export interface User {
  id?: number;
  username: string;
  password?: string;
  employeeId?: number | null;
  employeeFullName?: string;
  role: UserRole;
}
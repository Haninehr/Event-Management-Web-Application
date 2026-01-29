export interface Notification {
  id: number;
  title: string;
  message: string;
  type: 'REGISTRATION' | 'UPDATE' | 'CANCEL' | 'REMINDER';
  isread: boolean;
  createdAt: string;
}
export interface User {
  id: number;
  username: string;
  role: 'ORGANIZER' | 'PARTICIPANT';
}
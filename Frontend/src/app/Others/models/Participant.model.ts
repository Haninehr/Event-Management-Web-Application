export interface Participant {
  userId: number;
  username: string;
  email:String;
  status: 'ENATTEND' | 'REFUSED' | 'CANCELED' | 'REGISTERED';
  registeredAt : string;
  //problem : created at is localdatetime in the backend !!!
}




export interface Event {
  id: number;
  title: string;
  description: string;
  eventDate: string;
  eventTime: string;
  location: string;
  type: string;
  organizerId: number;
  Organizerofevent?:boolean
  views: number;
  createdAt: string;
  medias: MediaItem[];
  participantCount?: number;
  maxcapacity:number;
  status:String;
  isRegistered?:boolean;
}


export interface MediaItem {
  url: string;
  title: string;
  // Optional: add this if you later include mediaType in MediaDto
  // mediaType?: 'IMAGE' | 'VIDEO' | 'DOCUMENT';
}
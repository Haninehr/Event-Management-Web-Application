import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators ,FormsModule} from '@angular/forms';
import { EventService } from '../../Services/event.service';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../Services/auth.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { Event , MediaItem } from '../../Others/models/event.model';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { lastValueFrom } from 'rxjs';
import { MatRipple, MatOption } from "@angular/material/core";
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-update-event',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatDatepickerModule, MatSelectModule, MatOption  , FormsModule],
  templateUrl: './UpdateEvent.component.html',
  styleUrl: './UpdateEvent.component.scss'
})
export class UpdateEventComponent implements OnInit {
  eventForm: any;
  eventId!: number;
  //selectedFiles: File[] = [];
  selectedFiles: { file: File; title: string }[] = [];

     // Add a list to track removed items
removedMedia: MediaItem[] = [];

  currentMediaUrls: string[] = [];
  currentMedias: MediaItem[]= [];
  currentParticipantCount: number = 0;
  originalMaxCapacity: number = 0;
  isSubmitting = false;
  isDragOver = false;

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,


  ) {
    this.eventForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      eventDate: ['', Validators.required],
      eventTime: ['', Validators.required],
      location: [''],
      type: [''],
      maxcapacity: ['']
    });
  }

  ngOnInit(): void {
    this.eventId = +this.route.snapshot.paramMap.get('id')!;
    this.loadEvent();
  }

  async loadEvent() {
    try {
      const event: Event = await lastValueFrom(this.eventService.getEvent(this.eventId));
      //this.currentMediaUrls = event.mediaUrls || [];
      // Extract only the URLs if you still need an array of strings somewhere
      this.currentMedias = event.medias;
      this.currentMediaUrls = event.medias?.map(m => m.url) || [];
      this.currentParticipantCount = event.participantCount || 0;
      this.originalMaxCapacity = event.maxcapacity;

      this.eventForm.patchValue({
        title: event.title,
        description: event.description,
        eventDate: event.eventDate ? new Date(event.eventDate) : null,
        eventTime: event.eventTime,
        location: event.location,
        type: event.type,
        maxcapacity: event.maxcapacity
      });

      this.setupMaxCapacityValidator();
    } catch (err) {
      this.snackBar.open('Événement introuvable', 'OK', { duration: 5000 });
      this.router.navigate(['/events']);
    }
  }

  onFilesSelected(event: any) {
    this.addFiles(Array.from(event.target.files));
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
    if (event.dataTransfer?.files) this.addFiles(Array.from(event.dataTransfer.files));
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  private addFiles(newFiles: File[]) {
    for (const file of newFiles) {
      if ((this.currentMediaUrls.length + this.selectedFiles.length) >= 5) {
        this.snackBar.open('Maximum 5 médias autorisés', 'OK', { duration: 3000, panelClass: ['warning-snackbar'] });
        break;
      }
      if (file.size > 10 * 1024 * 1024) {
        this.snackBar.open(`${file.name} > 10 Mo`, 'OK', { duration: 4000, panelClass: ['warning-snackbar'] });
        continue;
      }
      if (!this.selectedFiles.some(f => f.file.name === file.name && f.file.size === file.size)) {
        this.selectedFiles.push({
          file,
          title: '' 
        });
      }
    }
    this.selectedFiles = [...this.selectedFiles];
  }

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
    this.selectedFiles = [...this.selectedFiles];
  }

  /*removeExistingMedia(index: number) {
    console.log("currentmeadis before: ",this.currentMedias.length);
    this.currentMedias.splice(index, 1);
    this.currentMedias = [...this.currentMedias];
    console.log("currentmeadis after: ",this.currentMedias.length);
  }*/

 

removeExistingMedia(index: number) {
  const removed = this.currentMedias.splice(index, 1)[0];
  this.currentMedias = [...this.currentMedias];
  this.currentMediaUrls = this.currentMedias.map(m => m.url);

  //console.log("this url : ",removed.url);
  if ( removed.url) {
    this.removedMedia.push(removed);
  }
}

  // === Helpers affichage ===
  isImage(url: string): boolean {
    return /\.(jpe?g|png|gif|webp|svg)$/i.test(url);
  }

  isVideo(url: string): boolean {
    return /\.(mp4|webm|ogg|mov)$/i.test(url);
  }

  getFileName(url: string): string {
    return url.split('/').pop()?.split('?')[0] || 'fichier';
  }

  getFileIconFromUrl(url: string): string {
    const name = this.getFileName(url).toLowerCase();
    if (this.isImage(url)) return 'image';
    if (this.isVideo(url)) return 'videocam';
    if (name.endsWith('.pdf')) return 'picture_as_pdf';
    if (name.endsWith('.doc') || name.endsWith('.docx')) return 'description';
    return 'insert_drive_file';
  }

  getFileIcon(file: File): string {
    const type = file.type;
    if (type.startsWith('image/')) return 'image';
    if (type.startsWith('video/')) return 'videocam';
    if (type.includes('pdf')) return 'picture_as_pdf';
    return 'insert_drive_file';
  }

  getFileIconClass(file: File): string {
    const type = file.type;
    if (type.startsWith('image/')) return 'image';
    if (type.startsWith('video/')) return 'video';
    if (type.includes('pdf')) return 'pdf';
    return 'file';
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' o';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' Ko';
    return (bytes / (1024 * 1024)).toFixed(1) + ' Mo';
  }

  goBack() {
    this.router.navigate(['/events', this.eventId]);
  }

  private generateRandomFilename(originalFile: File): string {
  const now = new Date();

  // Date part: YYYYMMDD
  const datePart = now.getFullYear().toString() +
    (now.getMonth() + 1).toString().padStart(2, '0') +
    now.getDate().toString().padStart(2, '0');

  // Time part: HHmm (24-hour format)
  const timePart = now.getHours().toString().padStart(2, '0') +
    now.getMinutes().toString().padStart(2, '0');

  // Random part: 6 hex characters (3 bytes)
  const array = new Uint8Array(3);
  crypto.getRandomValues(array);
  const randomPart = Array.from(array)
    .map(b => b.toString(16).padStart(2, '0'))
    .join('');

  // Original extension
  const extension = originalFile.name.includes('.')
    ? '.' + originalFile.name.split('.').pop()
    : '';

  // Final filename: date-time-random.ext
  return `${datePart}-${timePart}-${randomPart}${extension}`;
}

  async onSubmit() {
    if (this.eventForm.invalid) return;

    this.isSubmitting = true;

    try {
      const updatedEvent = {
        title: this.eventForm.value.title,
        description: this.eventForm.value.description || '',
        eventDate: this.eventForm.value.eventDate.toISOString().split('T')[0],
        eventTime: this.eventForm.value.eventTime,
        location: this.eventForm.value.location || '',
        type: this.eventForm.value.type || '',
        maxcapacity: this.eventForm.value.maxcapacity || null,
        mediaUrls: this.currentMediaUrls
      };

      console.log("Submitting event update:", updatedEvent);


      await lastValueFrom(this.eventService.updateEvent(this.eventId, updatedEvent));


      // Delete removed media
    for (const media of this.removedMedia) {

      const fileName = media.url.split('/').pop()?.split('?')[0] || media.url; //send only filename (not full url)
      try {
    await lastValueFrom(this.eventService.deleteMedia(this.eventId, fileName));
  } catch (deleteError) {
    console.warn('Failed to delete media (already gone or error):', fileName, deleteError);
    // Continue anyway — don't break the whole save
  }
    }


      if (this.selectedFiles.length > 0) {
      for (const item of this.selectedFiles) {
        if (!item.title?.trim()) {
            this.snackBar.open('Veuillez donner un titre à chaque média', 'OK', {
              duration: 5000,
              panelClass: ['warning-snackbar']
            });
            this.isSubmitting = false;
            return;
          }


        const mediaType = item.file.type.startsWith('image/') ? 'IMAGE' :
          item.file.type.startsWith('video/') ? 'VIDEO' : 'DOCUMENT';


           // Create a new File object with a random hashed filename
          const renamedFile = new File(
            [item.file],
            this.generateRandomFilename(item.file),
            { type: item.file.type }
          );

        const uploadedUrl = await lastValueFrom(
          this.eventService.uploadMedia(this.eventId, renamedFile, mediaType ,item.title.trim())
        );

        if (typeof uploadedUrl === 'string') {
          this.currentMediaUrls.push(uploadedUrl);
        }
      }

    }

      this.snackBar.open('Événement mis à jour !', '', {
        duration: 4000,
        panelClass: ['success-snackbar']
      });

      this.router.navigate(['/events', this.eventId]);

    } catch (error: any) {
      this.snackBar.open(error.error?.message || 'Erreur de mise à jour', 'OK', {
        duration: 6000,
        panelClass: ['error-snackbar']
      });
    } finally {
      this.isSubmitting = false;
      this.removedMedia = [];
      this.selectedFiles = [];
    }
  }


  private setupMaxCapacityValidator() {
    const maxCapacityControl = this.eventForm.get('maxcapacity');

    maxCapacityControl?.setValidators([
      Validators.required,
      Validators.min(1),

      (control: { value: any; }) => {
        const value = control.value;
        if (value != null && value < this.currentParticipantCount) {
          return { minParticipants: true };
        }
        return null;
      }
    ]);

    maxCapacityControl?.updateValueAndValidity();
  }
}
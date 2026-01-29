import { Component } from '@angular/core';
import { FormBuilder, FormsModule, Validators } from '@angular/forms';
import { EventService } from '../../Services/event.service';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../Services/auth.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { Event } from '../../Others/models/event.model';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatRipple, MatOption } from "@angular/material/core";
import { lastValueFrom } from 'rxjs';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-create-event',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatDatepickerModule, MatOption, MatSelectModule , FormsModule],
  templateUrl: './CreateEvent.component.html',
  styleUrl: './CreateEvent.component.scss'
})
export class CreateEventComponent {
  eventForm: any;


  selectedFileName: any;




  isSubmitting = false;
  //selectedFiles: File[] = [];
  selectedFiles: { file: File; title: string }[] = [];

  constructor(private fb: FormBuilder, private eventService: EventService, private router: Router, private authService: AuthService, public snackBar: MatSnackBar) {
    this.eventForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      eventDate: ['', Validators.required],
      location: ['', Validators.required],
      type: ['CONCERT', Validators.required],
      maxcapacity: ['', Validators.required],
      eventTime: ['', Validators.required]
    });
  }


  onFilesSelected(event: any) {
    const files: FileList = event.target.files;
    this.addFiles(Array.from(files));
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
    if (event.dataTransfer?.files) {
      this.addFiles(Array.from(event.dataTransfer.files));
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  isDragOver = false;

  private addFiles(newFiles: File[]) {
    for (const file of newFiles) {
      if (this.selectedFiles.length >= 5) {
        this.snackBar.open('Maximum 5 fichiers autorisés', 'OK', { duration: 3000, panelClass: ['error-snackbar'] });
        break;
      }
      if (file.size > 10 * 1024 * 1024) { // 10 Mo
        this.snackBar.open(`${file.name} dépasse 10 Mo`, 'OK', { duration: 4000, panelClass: ['error-snackbar'] });
        continue;
      }
      if (!this.selectedFiles.find(f => f.file.name === file.name && f.file.size === file.size)) {
        //this.selectedFiles.push(file);
        this.selectedFiles.push({
          file,
          title: '' 
        });
      }
    }
    this.selectedFiles = [...this.selectedFiles]; // trigger change detection
  }

  // Optional: suggest a title based on file name
  /*private suggestTitle(file: File): string {
    const name = file.name.toLowerCase();
    if (name.includes('affiche') || name.includes('poster')) return 'Affiche';
    if (name.includes('programme') || name.includes('program')) return 'Programme';
    if (name.includes('plan') || name.includes('salle') || name.includes('seating')) return 'Plan de salle';
    if (name.includes('flyer')) return 'Flyer';
    if (name.includes('photo') || name.includes('image')) return 'Photo promotionnelle';
    return ''; // empty = user must fill
  }*/

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
    this.selectedFiles = [...this.selectedFiles];
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

  // Helpers
  getFileIcon(file: File): string {
    const type = file.type;
    if (type.startsWith('image/')) return 'image';
    if (type.startsWith('video/')) return 'video_camera';
    if (type.includes('pdf')) return 'picture_as_pdf';
    if (type.includes('word') || type.includes('doc')) return 'description';
    if (type.includes('sheet') || type.includes('excel')) return 'table_chart';
    return 'insert_drive_file';
  }

  getFileIconClass(file: File): string {
    const type = file.type;
    if (type.startsWith('image/')) return 'image';
    if (type.startsWith('video/')) return 'video';
    if (type.includes('pdf')) return 'pdf';
    if (type.includes('word') || type.includes('doc')) return 'doc';
    return 'file';
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' o';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' Ko';
    return (bytes / (1024 * 1024)).toFixed(1) + ' Mo';
  }




  async onSubmit() {
    if (this.eventForm.invalid) { //|| this.selectedFiles.length === 0
      this.snackBar.open('Champs obligatoires manquants ou aucun fichier', 'OK', { duration: 5000, panelClass: ['warning-snackbar'] });
      return;
    }

    this.isSubmitting = true;

    try {
      // Étape 1 : créer l'événement sans fichiers (ton endpoint existant)
      const eventData = {
        title: this.eventForm.value.title,
        description: this.eventForm.value.description || '',
        eventDate: (this.eventForm.value.eventDate as Date).toISOString().split('T')[0],
        eventTime: this.eventForm.value.eventTime,
        location: this.eventForm.value.location || '',
        type: this.eventForm.value.type || '',
        maxcapacity: this.eventForm.value.maxcapacity || null
      };

      const createdEvent: Event = await lastValueFrom(
        this.eventService.createEvent(eventData)
      );


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


          // Create a new File object with a random hashed filename
          const renamedFile = new File(
            [item.file],
            this.generateRandomFilename(item.file),
            { type: item.file.type }
          );
          const mediaType = item.file.type.startsWith('image/') ? 'IMAGE' :
            item.file.type.startsWith('video/') ? 'VIDEO' : 'DOCUMENT';

          await lastValueFrom(
            this.eventService.uploadMedia(createdEvent.id, renamedFile, mediaType, item.title.trim())
          );
        }
      }


      this.snackBar.open('Événement créé avec succès !', 'Super', {
        duration: 4000,
        panelClass: ['success-snackbar']
      });

      // after creation , go back to dashboard not events !!
      //no , go to event to see
      this.router.navigate(['/events', createdEvent.id]);

    } catch (error: any) {
      console.error(error);
      this.snackBar.open(
        error.error?.message || 'Erreur lors de la création',
        'OK',
        { duration: 6000, panelClass: ['error-snackbar'] }
      );
    } finally {
      this.isSubmitting = false;
    }
  }



  goBack() { //if ignore creation , go back to dashboard
    this.router.navigate(['/dashboard']);
  }

}
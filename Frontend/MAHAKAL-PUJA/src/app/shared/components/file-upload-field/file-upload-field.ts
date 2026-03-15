import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-file-upload-field',
  imports: [CommonModule],
  templateUrl: './file-upload-field.html',
  styleUrl: './file-upload-field.scss',
})
export class FileUploadField {
  @Input({ required: true }) label = '';
  @Input() helperText = '';
  @Input() accept = '.pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document';
  @Input() error = '';
  @Input() previewUrl: string | null = null;
  @Input() fileName = '';
  @Input() progress: number | null = null;
  @Input() isUploading = false;
  @Input() uploaded = false;

  @Output() fileSelected = new EventEmitter<File>();
  @Output() clearFile = new EventEmitter<void>();

  isDragging = false;

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    const file = event.dataTransfer?.files?.item(0);

    if (file) {
      this.fileSelected.emit(file);
    }
  }

  onFileInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);

    if (file) {
      this.fileSelected.emit(file);
    }

    input.value = '';
  }

  clear(): void {
    this.clearFile.emit();
  }

  get hasImagePreview(): boolean {
    return !!this.previewUrl;
  }
}
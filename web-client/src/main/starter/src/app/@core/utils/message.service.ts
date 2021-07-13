import {
  NbComponentStatus,
  NbGlobalPhysicalPosition,
  NbGlobalPosition,
  NbToastrConfig,
  NbToastrService
} from "@nebular/theme";
import {Injectable} from "@angular/core";

export class Messages{
  static generic_error = "An error has occurred...";
  static generic_success = "Operation successful"
}

@Injectable({ providedIn: 'root' })
export class MessageService {
  constructor(private toastrService: NbToastrService) {}

  config: NbToastrConfig;

  public errorToast( title: string, body: string){
    this.showToast('warning', title, body);
  }

  public logToast( title: string, body: string){
    this.showToast('primary', title, body, 5000);
  }

  private showToast(type: NbComponentStatus, title: string, body: string, duration: number = 2000) {
    const config = {
      status: type,
      destroyByClick: true,
      duration: duration,
      hasIcon: true,
      position: NbGlobalPhysicalPosition.TOP_RIGHT,
      preventDuplicates: false,
    };
    const titleContent = title ? `${title}` : ''.toUpperCase();

    this.toastrService.show(
      body,
      `${titleContent}`,
      config);
  }
}

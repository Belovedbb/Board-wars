import {Component, Input, OnInit} from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import {
  ControlContainer,
  FormBuilder,
  FormGroup,
  FormGroupDirective
} from '@angular/forms';
import {BehaviorSubject} from "rxjs";
import {RoleEntity} from "../../marker-model";
import {Messages, MessageService} from "../../../../@core/utils/message.service";
import {ApiHttpService} from "../../../../@core/utils";
import {Config} from "../../../../config/config";
import {map} from "rxjs/operators";
import {Router} from "@angular/router";

@Component({
  selector: 'marker-loader',
  templateUrl: 'marker-loader.component.html',
  styleUrls: ['marker-loader.component.scss'],
  viewProviders:[{provide:ControlContainer,useExisting: FormGroupDirective }],
  providers: [ApiHttpService, MessageService]
})

export class MarkerLoaderComponent implements OnInit {

  globalForm: FormGroup;
  storageForm: FormGroup;
  roleForm: FormGroup;
  tokenForm: FormGroup;
  completedForm: FormGroup;
  deactivateComponentView = new BehaviorSubject(false);
  //completed button listener
  isGlobalCompleted = new BehaviorSubject(false);
  isRoleCompleted = new BehaviorSubject(false);
  isStorageCompleted = new BehaviorSubject(false);
  isTokenCompleted = new BehaviorSubject(false);
  private fb: FormBuilder;
  //utils
  entities: BehaviorSubject<RoleEntity[]> = new BehaviorSubject<RoleEntity[]>([]);

  @Input() title: string;

  constructor(private http: ApiHttpService, private messageService: MessageService,
              private router: Router,
              protected ref: NbDialogRef<MarkerLoaderComponent>) {
    this.initialize();
  }

  ngOnInit() {
    this.fb = new FormBuilder();
    this.globalForm = new FormGroup({});
    this.storageForm = new FormGroup({});
    this.roleForm = new FormGroup({});
    this.tokenForm = new FormGroup({});
  }

  onGlobalSubmit() {
    this.globalForm.markAsDirty();
  }

  initialize() {
    this.http.get(Config.API_ROUTE.LOGOUT).subscribe();
  }

  finalize() {
    this.http.get(Config.API_ROUTE.AUTH_MARKER_SAVE)
      .pipe(
        map((response: AuthenticationResponse) => {
          if(!response.status){
            this.messageService.errorToast('Error', Messages.generic_error);
          }else{
            this.ref.close();
            this.messageService.logToast('Success!', response.value);
            this.router.navigate(['pages/dashboard']);

          }
        })
      )
      .subscribe()
  }
}

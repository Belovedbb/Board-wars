import {Injectable} from '@angular/core';
import {
  ExternalConfigurationHandlerInterface,
  ExternalConfiguration,
  ResourceHelper,
  Resource, HalOptions, SubTypeBuilder, ResourceArray, ExternalService, HalParam
} from '@lagoshny/ngx-hal-client';
import {HttpClient, HttpHeaders, HttpParams, HttpUrlEncodingCodec} from '@angular/common/http';
import {Config} from "../../config/config";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";

@Injectable()
export class ExternalConfigurationService implements ExternalConfigurationHandlerInterface {

  constructor(private http: HttpClient) {
    let headers = new HttpHeaders();
    headers = headers.set('Accept', '*/*');
    ResourceHelper.headers = headers;
  }

  getProxyUri(): string {
    return Config.API_ROUTE.BASE;
  }

  getHttp(): HttpClient {
    return this.http;
  }

  getRootUri(): string {
    return null;
  }

  getExternalConfiguration(): ExternalConfiguration {
    return null;
  }

  setExternalConfiguration(externalConfiguration: ExternalConfiguration) {
  }

  deserialize(): any {
  }

  serialize(): any {
  }
}

@Injectable({ providedIn: 'root' })
export class HateoasUtil<T extends Resource>  {

  constructor(private externalService: ExternalService) {
  }

  public getAllFollowUp<T extends Resource>(type: { new(): T }, resource: string, followUps?: string[],
                                            options?: HalOptions, subType?: SubTypeBuilder): Observable<T[]> {
    const uri = HateoasUtil.getResourceUrl(resource, followUps);
    const httpParams = ResourceHelper.optionParams(new HttpParams({encoder: new HttpUrlEncodingCodec()}), options);
    const result: ResourceArray<T> = new ResourceArray<T>('_embedded');
    this.setUrls(result);
    result.sortInfo = options ? options.sort : undefined;
    const observable = ResourceHelper.getHttp().get(uri, {headers: ResourceHelper.headers, params: httpParams});
    return observable.pipe(
      map(response => ResourceHelper.instantiateResourceCollection(type, response, result, subType)),
      map(result => result.result),
      catchError(error => throwError(error)));
  }

  public getFollowUp<T extends Resource>(type: { new(): T }, resource: string, id: any, params?: HalParam[]): Observable<T> {
    const uri = HateoasUtil.getResourceUrl(resource).concat('/', id);
    const result: T = new type();
    const httpParams = ResourceHelper.params(new HttpParams(), params);

    this.setUrlsResource(result);
    const observable = ResourceHelper.getHttp().get(uri, {headers: ResourceHelper.headers, params: httpParams});
    return observable.pipe(map(data => ResourceHelper.instantiateResource(result, data)),
      catchError(error => throwError(error)));
  }

  private static getResourceUrl(resource?: string, followUps?: string[]): string {
    let url: string = ResourceHelper.getURL();
    if (!url.endsWith('/')) {
      url = url.concat('/');
    }

    if (resource) {
      url =  url.concat(resource).concat('/');
    }
    if(followUps && followUps.length > 0){
      followUps.forEach(value =>  {
        url = url.concat(value).concat('/');
      } )
    }
    url = url.replace('{?projection}', '');

    return url;
  }


  private setUrls<T extends Resource>(result: ResourceArray<T>) {
    result.proxyUrl = this.externalService.getProxyUri();
    result.rootUrl = this.externalService.getRootUri();
  }

  private setUrlsResource<T extends Resource>(result: T) {
    result.proxyUrl = this.externalService.getProxyUri();
    result.rootUrl = this.externalService.getRootUri();
  }

  public static trimEmbedded(payload): any {
    let embeddedKey = "_embedded";
    if (payload[embeddedKey]) {
      const embeddedClassName = Object.keys(payload[embeddedKey])[0];
      const embedded: any = payload[embeddedKey];
      return embedded[embeddedClassName];
    }
    return payload;
  }
}

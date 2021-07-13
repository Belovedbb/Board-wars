
 class APIRoute{
  public API_URL: string;
  public AUTH_STATE: string;
  public AUTH_STATE_TYPE: string;
  public AUTH_ENTRY_LINK: string;
  public AUTH_MARKER_GLOBAL: string;
  public AUTH_MARKER_STORAGE: string;
  public AUTH_MARKER_ROLE: string;
  public AUTH_MARKER_TOKEN: string;
  public AUTH_MARKER_SAVE: string;
  public GLOBAL_MARKER: string;
  public LOGOUT: string;
  public BASE: string;
  public LOGGED_IN_USER: string;
  public HISTORY_EVENT_KANBAN: string;
   public HISTORY_EVENT_ALL: string;
  public HISTORY_EVENT_MANAGEMENT: string;
  public HISTORY_PERSISTENT_KANBAN: string;
  public HISTORY_PERSISTENT_MANAGEMENT: string;
  public HISTORY_PERSISTENT_ALL: string;
  public KANBAN_ACTIVITY_FREQUENCY: string;
  public KANBAN_ACTIVITY_FREQUENCY_POINTS: string;

  public append_host_url(data: string) : string{
    return this.API_URL + data;
  }
}

 class APIAuthKey{
   public AUTH_BEARER: string;
   public MARKER_BEARER: string;
 }

class APIAuthLevel{
  public GLOBAL: string;
  public STORAGE: string;
  public ROLE: string;
  public TOKEN: string;
  public PRIMARY_SUCCESS: string;
  public PRIMARY_FAILURE: string;
}

class APIAuthType{
  public GITHUB: string;
  public LOCAL: string;
  public SUCCESS: string;
  public FAILURE: string;
}

class Network{
  public TIMEOUT: number;
}

export class Config {
   static SELF_URL: string;
   static API_ROUTE: APIRoute = new APIRoute();
   static API_AUTH_TYPE: APIAuthType = new APIAuthType();
   static NETWORK: Network = new Network();
   static API_AUTH_LEVEL: APIAuthLevel = new APIAuthLevel();
   static API_AUTH_KEY: APIAuthKey = new APIAuthKey();

  static load(config: any) {
    this.SELF_URL = config.self_host;

    //api route instantiate
    this.API_ROUTE.API_URL = config.api_host;
    this.API_ROUTE.AUTH_STATE = this.API_ROUTE.append_host_url(config.auth_state);
    this.API_ROUTE.AUTH_STATE_TYPE = this.API_ROUTE.append_host_url(config.auth_state_type);
    this.API_ROUTE.AUTH_ENTRY_LINK = this.API_ROUTE.append_host_url(config.auth_entry_link);
    this.API_ROUTE.AUTH_MARKER_GLOBAL = this.API_ROUTE.append_host_url(config.auth_marker_global);
    this.API_ROUTE.AUTH_MARKER_STORAGE = this.API_ROUTE.append_host_url(config.auth_marker_storage);
    this.API_ROUTE.AUTH_MARKER_ROLE = this.API_ROUTE.append_host_url(config.auth_marker_role);
    this.API_ROUTE.AUTH_MARKER_TOKEN = this.API_ROUTE.append_host_url(config.auth_marker_token);
    this.API_ROUTE.AUTH_MARKER_SAVE = this.API_ROUTE.append_host_url(config.auth_marker_save);
    this.API_ROUTE.LOGOUT = this.API_ROUTE.append_host_url(config.logout);
    this.API_ROUTE.GLOBAL_MARKER = this.API_ROUTE.append_host_url(config.global_marker);
    this.API_ROUTE.BASE = this.API_ROUTE.append_host_url(config.api_version);
    this.API_ROUTE.LOGGED_IN_USER = this.API_ROUTE.append_host_url(config.api_logged_in_user);
    this.API_ROUTE.HISTORY_EVENT_ALL= this.API_ROUTE.BASE + config.history_event_all;
    this.API_ROUTE.HISTORY_EVENT_KANBAN = this.API_ROUTE.BASE + config.history_event_kanban;
    this.API_ROUTE.HISTORY_EVENT_MANAGEMENT = this.API_ROUTE.BASE + config.history_event_management;
    this.API_ROUTE.HISTORY_PERSISTENT_KANBAN = this.API_ROUTE.BASE + config.history_persistent_kanban;
    this.API_ROUTE.HISTORY_PERSISTENT_MANAGEMENT = this.API_ROUTE.BASE + config.history_persistent_management;
    this.API_ROUTE.HISTORY_PERSISTENT_ALL = this.API_ROUTE.BASE + config.history_persistent_all;
    this.API_ROUTE.KANBAN_ACTIVITY_FREQUENCY = this.API_ROUTE.BASE + config.kanban_activity_frequency;
    this.API_ROUTE.KANBAN_ACTIVITY_FREQUENCY_POINTS = this.API_ROUTE.BASE + config.kanban_activity_frequency_points;

    //auth type
    this.API_AUTH_TYPE.GITHUB = config.auth_type_github;
    this.API_AUTH_TYPE.LOCAL = config.auth_type_local;
    this.API_AUTH_TYPE.SUCCESS = config.api_auth_success;
    this.API_AUTH_TYPE.FAILURE = config.api_auth_failure;

    //network
    this.NETWORK.TIMEOUT = config.network_timeout;

    //auth level
    this.API_AUTH_LEVEL.GLOBAL = config.auth_level_global;
    this.API_AUTH_LEVEL.STORAGE = config.auth_level_storage;
    this.API_AUTH_LEVEL.ROLE = config.auth_level_role;
    this.API_AUTH_LEVEL.TOKEN = config.auth_level_token;
    this.API_AUTH_LEVEL.PRIMARY_FAILURE = config.auth_level_primary_failure;
    this.API_AUTH_LEVEL.PRIMARY_SUCCESS = config.auth_level_primary_success;

    //auth key
    this.API_AUTH_KEY.AUTH_BEARER = config.api_auth_bearer_id_key;
    this.API_AUTH_KEY.MARKER_BEARER = config.api_marker_token_hash_key;
  }

  public static resolveParameterLink(data: string, ...args: string[]){
    if(args) {
      for (let i = 0; i < args.length; i++) {
        let key = '{' + i + '}';
        data = data.replace(key, args[i]);
      }
    }
    return data;
  }

}

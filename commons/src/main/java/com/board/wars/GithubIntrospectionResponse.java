package com.board.wars;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GithubIntrospectionResponse {
    public static String scope_key = "scope", client_id_key = "client_id";
    private String login;
    private List<String> roles;
    private String rawData;
    private Map<String, List<String>> rawHeaders;
    private List<String> audiences;
    private Instant expirationTime;
    private Instant issueTime;
    private Instant notBeforeTime;
    private List<String> scopes;
    private String clientId;
    private String issuer;


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public GithubIntrospectionResponse setRawHeaders(List<String> headers, String key){
        if(rawHeaders == null){
            rawHeaders = new HashMap<>();
        }
        if(!CollectionUtils.isEmpty(headers)){
            rawHeaders.put(key, headers);
        }
        return this;
    }

    public GithubIntrospectionResponse parse(){
        JsonParser springParser = JsonParserFactory.getJsonParser();
        Map<String, Object> result = springParser.parseMap(rawData);
        new ParserHelper(result, rawHeaders);
        login = ParserHelper.getLogin();
        scopes = ParserHelper.getScopes();
        clientId = ParserHelper.getClientId();
        return this;
    }

    public boolean isActive(){
        return !CollectionUtils.isEmpty(scopes) && StringUtils.hasText(login);
    }

    public List<String> getAudience(){
        return audiences;
    }

    public Instant getExpirationTime(){
        return expirationTime;
    }

    public Instant getIssueTime(){
        return issueTime;
    }

    public Instant getNotBeforeTime(){
        return notBeforeTime;
    }

    public List<String> getScopes(){
        return scopes;
    }

    public String getClientID(){
        return clientId;
    }

    public String getIssuer(){
        return issuer;
    }

    @SuppressWarnings("unchecked")
    private static class ParserHelper{
        private static String EMPTY_STRING= "";
        static Map<String, Object> map = null;
        static Map<String, List<String>> mapHeaders;

        ParserHelper(final Map<String, Object> parsedMap, final Map<String, List<String>> parsedHeaderMap){
            map = parsedMap;
            mapHeaders = parsedHeaderMap;
        }

        static String getLogin(){
            if(StringUtils.hasText((String)map.getOrDefault("login", EMPTY_STRING))){
                return (String) map.get("login");
            }
            return EMPTY_STRING;
        }

        static List<String> getScopes(){
            if(!CollectionUtils.isEmpty((List<String>)mapHeaders.getOrDefault(scope_key, Collections.EMPTY_LIST))){
                return mapHeaders.get(scope_key);
            }
            return Collections.EMPTY_LIST;
        }

        static String getClientId(){
            if(!CollectionUtils.isEmpty((List<String>)mapHeaders.getOrDefault(client_id_key, Collections.EMPTY_LIST))){
                List<String> clientIds = mapHeaders.get(client_id_key);
                return StringUtils.hasText(clientIds.get(0)) ? clientIds.get(0) : EMPTY_STRING;
            }
            return EMPTY_STRING;
        }

    }

}

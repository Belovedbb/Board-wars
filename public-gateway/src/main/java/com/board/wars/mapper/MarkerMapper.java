package com.board.wars.mapper;

import com.board.wars.domain.LocalRegister;
import com.board.wars.domain.marker.OrganizationDetail;
import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.domain.marker.global.StorageMarker;
import com.board.wars.domain.marker.parts.GlobalMarkerPart;
import com.board.wars.domain.marker.parts.RoleMarkerPart;
import com.board.wars.domain.marker.parts.StorageMarkerPart;
import com.board.wars.domain.marker.parts.TokenMarkerPart;
import com.board.wars.payload.marker.MarkerResponse;
import com.board.wars.payload.marker.RoleMarkerPayload;
import com.board.wars.payload.marker.StorageMarkerPayload;
import com.board.wars.payload.marker.TokenMarkerPayload;
import com.board.wars.utils.MarkerContainerUtil;
import com.board.wars.utils.RouteUtil;
import org.apache.commons.lang.StringUtils;
import org.mapstruct.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Map;

@Mapper
public interface MarkerMapper {

    @Mapping(target = "attributes", source = "principal.attributes")
    @Mapping(target = "fullName", source = "name")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "fullyLocal", constant = "true")
    @Mapping(target = "terms", constant = "true")
    @Mapping(target = "organization", qualifiedByName = "organizationSubMapper", source = "credentials")
    LocalRegister transformAuthenticationTokenToLocalRegister(OAuth2AuthenticationToken token, @Context OAuth2AuthorizedClient authClient);

    @AfterMapping
    default void mapAttributes(@MappingTarget LocalRegister localRegister) {
        Map<String, Object> attributes = localRegister.getAttributes();
        if(localRegister.getOrganization() != null) {
            localRegister.setOrganizationName(localRegister.getOrganization().getLogin());
            String password = localRegister.getOrganizationName();
            localRegister.setPassword(password);
            localRegister.setConfirmPassword(password);
            localRegister.setEmail((String) attributes.get("email"));
            localRegister.setPictureUrl("github:" + attributes.get("avatar_url"));
        }else {
            String password = localRegister.getFullName();
            localRegister.setPassword(password);
            localRegister.setConfirmPassword(password);
            localRegister.setEmail( StringUtils.isNotEmpty((String)attributes.get("email")) ?  (String)attributes.get("email") : localRegister.getFullName().toLowerCase() + "@gmail.com");
            localRegister.setPictureUrl("github:" + attributes.get("avatar_url"));
        }

    }

    @Named("organizationSubMapper")
    default OrganizationDetail mapOrganizationDetail(Object credentials, @Context OAuth2AuthorizedClient authClient) {
        return WebClient.create().get()
                .uri(RouteUtil.External.ENDPOINT_GITHUB_ORGANIZATIONS)
                .acceptCharset(Charset.defaultCharset())
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, RouteUtil.USER_AGENT)
                .header(HttpHeaders.AUTHORIZATION, "bearer "+authClient.getAccessToken().getTokenValue())
                .retrieve()
                .bodyToFlux(OrganizationDetail.class)
                .next()
                .defaultIfEmpty(this.defaultDetail(authClient.getPrincipalName()))
                .block();
    }

    default OrganizationDetail defaultDetail(String name) {
        OrganizationDetail detail = new OrganizationDetail();
        detail.setLogin(name);
        detail.setAvatarUrl("");
        detail.setReposUrl("");
        detail.setUrl("https://github.com/"+detail.getLogin());
        return detail;
    }

    @Mapping(target = "registrarName", source = "fullName")
    @Mapping(target = "registrarEmail", source = "email")
    GlobalMarkerPart transformLocalRegisterToGlobalPart(LocalRegister localRegister);

    //storage mapping
   @Mapping(target = "type", source = "storageType", qualifiedByName = "storageTypeMapper")
    StorageMarkerPart transformStorageFromPayloadToDomain(StorageMarkerPayload payload);

    @ValueMappings({
            @ValueMapping(source = "FILESYSTEM", target = "FILE_SYSTEM"),
            @ValueMapping(source = "GOOGLE_CLOUD", target = "DRIVE"),
            @ValueMapping(source = "AMAZON_AWS", target = "AWS")
    })
    @Named("storageTypeMapper")
     StorageMarkerPart.Type mapTypeEnum(StorageMarkerPayload.StorageType payloadType);

    //role mapping
    RoleMarkerPart transformRoleFromPayloadToDomain(RoleMarkerPayload payload);

    //token mapping
    @Mapping(target = "enableToken", source = "allowTokenPass")
    @Mapping(target = "tokenLifeTime", source = "expiryPeriod", qualifiedByName = "tokenLifeTimeMarker")
    TokenMarkerPart transformTokenFromPayloadToDomain(TokenMarkerPayload payload);

    @Named("tokenLifeTimeMarker")
    default Duration mapTokenExpiryDuration(Long expirySeconds){
        return Duration.ofSeconds(expirySeconds);
    }

    @Mapping(target = "email", source = "registrarEmail")
    @Mapping(target = "fullName", source = "registrarName")
    @Mapping(target = "applicationName", constant = MarkerContainerUtil.PRINCIPAL_APPLICATION_NAME)
    @Mapping(target = "id", ignore = true)
    GlobalMarker updateGlobalMarkerFromGlobalPart(@MappingTarget GlobalMarker marker, GlobalMarkerPart globalMarkerPart);

    @Mapping(target = "storageMarker.type", source = "type", qualifiedByName = "storageTypeEnum")
    @Mapping(target = "storageMarker.baseLocation", source = "baseLocation")
    @Mapping(target = "storageMarker.allowedTypes", source = "allowedTypes")
    @Mapping(target = "id", ignore = true)
    GlobalMarker updateGlobalMarkerFromStoragePart(@MappingTarget GlobalMarker marker, StorageMarkerPart storageMarkerPart);

    @ValueMappings({
            @ValueMapping(source = "FILE_SYSTEM", target = "FILESYSTEM"),
            @ValueMapping(source = "DRIVE", target = "GOOGLE_CLOUD"),
            @ValueMapping(source = "AWS", target = "AMAZON_AWS")
    })
    @Named("storageTypeEnum")
    StorageMarker.Type mapStorageTypeEnum(StorageMarkerPart.Type  type);

    @Mapping(target = "roleMarker.activate", source = "activate")
    @Mapping(target = "roleMarker.roleEntities", source = "roleEntities")
    @Mapping(target = "id", ignore = true)
    GlobalMarker updateGlobalMarkerFromRolePart(@MappingTarget GlobalMarker marker, RoleMarkerPart roleMarkerPart);

    @Mapping(target = "tokenMarker.enableToken", source = "enableToken")
    @Mapping(target = "tokenMarker.baseEmail", source = "baseEmail")
    @Mapping(target = "tokenMarker.attachedRole", source = "attachedRole")
    @Mapping(target = "tokenMarker.enableGithubAuth", source = "enableGithubAuth")
    @Mapping(target = "tokenMarker.tokenLifeTime", source = "tokenLifeTime")
    @Mapping(target = "id", ignore = true)
    GlobalMarker updateGlobalMarkerFromTokenMarker(@MappingTarget GlobalMarker marker, TokenMarkerPart tokenMarkerPart);

    @Mapping(target = "storageMarkerPayload", source = "storageMarker")
    @Mapping(target = "tokenMarkerPayload", source = "tokenMarker")
    @Mapping(target = "roleMarkerPayload", source = "roleMarker")
    MarkerResponse transformGlobalPartToMarkerResponse(GlobalMarker globalMarker);

    //TODO use correct token duration value
    @Mapping(target = "tokenMarker.tokenLifeTime", constant = "1L")
    com.board.wars.marker.global.GlobalMarker convertToCommonsMarker(GlobalMarker globalMarker);
}

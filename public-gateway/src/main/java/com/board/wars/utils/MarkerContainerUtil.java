package com.board.wars.utils;

import com.board.wars.domain.marker.parts.*;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.store.marker.parts.GlobalMarkerPartRepository;
import com.board.wars.store.marker.parts.RoleMarkerPartRepository;
import com.board.wars.store.marker.parts.StorageMarkerPartRepository;
import com.board.wars.store.marker.parts.TokenMarkerPartRepository;
import com.board.wars.utils.identity.IdentityGeneratorContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Component
public class MarkerContainerUtil {
    private GlobalMarkerPart globalMarkerPart;
    private StorageMarkerPart storageMarkerPart;
    private RoleMarkerPart roleMarkerPart;
    private TokenMarkerPart tokenMarkerPart;

    private String markerStat = "";
    public static final String PRINCIPAL_AUTH_MARKER_SERVER = "github";
    public static final String PRINCIPAL_APPLICATION_NAME = "board-wars";

    //repositories
    private GlobalMarkerPartRepository globalMarkerPartRepository;
    private RoleMarkerPartRepository roleMarkerPartRepository;
    private StorageMarkerPartRepository storageMarkerPartRepository;
    private TokenMarkerPartRepository tokenMarkerPartRepository;

    MarkerContainerUtil(GlobalMarkerPartRepository globalMarkerPartRepository, RoleMarkerPartRepository roleMarkerPartRepository,
                        StorageMarkerPartRepository storageMarkerPartRepository, TokenMarkerPartRepository tokenMarkerPartRepository){
        this.globalMarkerPartRepository = globalMarkerPartRepository;
        this.roleMarkerPartRepository = roleMarkerPartRepository;
        this.storageMarkerPartRepository = storageMarkerPartRepository;
        this.tokenMarkerPartRepository = tokenMarkerPartRepository;
    }

    public MarkerContainerUtil injectHash(){
        partAssertions();
        String token = IdentityGeneratorContext.generate();
        globalMarkerPart.setPartId(token);
        storageMarkerPart.setPartId(token);
        roleMarkerPart.setPartId(token);
        tokenMarkerPart.setPartId(token);
        return  this;
    }

    public Mono<? extends MarkerPart> buildPart(){
        partAssertions();
        return globalMarkerPartRepository.save(globalMarkerPart)
                .switchIfEmpty(Mono.just(globalMarkerPart))
                .flatMap(global -> storageMarkerPartRepository.save(storageMarkerPart))
                .switchIfEmpty(Mono.just(storageMarkerPart))
                .flatMap(storage -> roleMarkerPartRepository.save(roleMarkerPart))
                .switchIfEmpty(Mono.just(roleMarkerPart))
                .flatMap(role -> tokenMarkerPartRepository.save(tokenMarkerPart))
                .switchIfEmpty(Mono.just(tokenMarkerPart));
    }

    public MarkerContainerUtil createNewPartInstances(){
        globalMarkerPart = new GlobalMarkerPart();
        storageMarkerPart = new StorageMarkerPart();
        roleMarkerPart = new RoleMarkerPart();
        tokenMarkerPart = new TokenMarkerPart();
        return this;
    }

    private void partAssertions(){
        Assert.notNull(globalMarkerPart, "subject global must not be null");
        Assert.notNull(storageMarkerPart, "subject storage must not be null");
        Assert.notNull(roleMarkerPart, "subject role must not be null");
        Assert.notNull(tokenMarkerPart, "subject token must not be null");
    }

    public Mono<AuthenticatedResponse.Level> nextLevelMarker(String partId){
        return Mono.just(partId).flatMap(tokenMarkerPartRepository::findByPartId)
                .flatMap(tokenPart -> nextLevelWhenPartIdExist(tokenPart.getPartId()))
                .switchIfEmpty(nextLevelWhenPartIdDoesNotExist());
    }

    private  <T extends MarkerPart> void setMarkerStat(T stat){
        if(stat != null) {
            markerStat = stat.getMarkerType();
        }else{
            markerStat = "";
        }
    }

    //from top to bottom so as to be naturally overridden
    private Mono<AuthenticatedResponse.Level> nextLevelWhenPartIdExist(String partId){
        return tokenMarkerPartRepository.findByPartIdAndCompleteFalse(partId).doOnNext(this::setMarkerStat)
                .defaultIfEmpty(new TokenMarkerPart())
                .flatMap(marker -> roleMarkerPartRepository.findByPartIdAndCompleteFalse(partId).doOnNext(this::setMarkerStat))
                .defaultIfEmpty(new RoleMarkerPart())
                .flatMap(marker -> storageMarkerPartRepository.findByPartIdAndCompleteFalse(partId).doOnNext(this::setMarkerStat))
                .defaultIfEmpty(new StorageMarkerPart())
                .flatMap(marker -> globalMarkerPartRepository.findByPartIdAndCompleteFalse(partId).doOnNext(this::setMarkerStat))
                .defaultIfEmpty(new GlobalMarkerPart())
                .map(e -> getLevelResponse());
    }

    private Mono<AuthenticatedResponse.Level> nextLevelWhenPartIdDoesNotExist(){
        return Mono.just(AuthenticatedResponse.Level.ORGANIZATION_FAILURE);
    }

    private AuthenticatedResponse.Level getLevelResponse(){
        AuthenticatedResponse.Level level = AuthenticatedResponse.Level.ORGANIZATION_SUCCESS;
        if(StringUtils.hasText(markerStat)){
            switch (markerStat) {
                case "global":
                    level =  AuthenticatedResponse.Level.ORGANIZATION_GLOBAL;
                    break;
                case "storage":
                    level =  AuthenticatedResponse.Level.ORGANIZATION_STORAGE;
                    break;
                case "role":
                    level = AuthenticatedResponse.Level.ORGANIZATION_ROLE;
                    break;
                case "token":
                    level = AuthenticatedResponse.Level.ORGANIZATION_TOKEN;
                    break;
            }
        }
        setMarkerStat(null);
        return level;
    }

}

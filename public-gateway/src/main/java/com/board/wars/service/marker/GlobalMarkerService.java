package com.board.wars.service.marker;

import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.domain.HistoryEntity;
import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.init.MarkerStarter;
import com.board.wars.mapper.MarkerMapper;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.store.HistoryRepository;
import com.board.wars.store.marker.GlobalMarkerRepository;
import com.board.wars.store.marker.parts.GlobalMarkerPartRepository;
import com.board.wars.store.marker.parts.RoleMarkerPartRepository;
import com.board.wars.store.marker.parts.StorageMarkerPartRepository;
import com.board.wars.store.marker.parts.TokenMarkerPartRepository;
import com.board.wars.utils.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GlobalMarkerService {

    private final MarkerMapper markerMapper;
    private final TokenMarkerPartRepository tokenMarkerPartRepository;
    private final GlobalMarkerPartRepository globalMarkerPartRepository;
    private final RoleMarkerPartRepository roleMarkerPartRepository;
    private final StorageMarkerPartRepository storageMarkerPartRepository;
    private final GlobalMarkerRepository globalMarkerRepository;
    private final MarkerStarter markerStarter;

    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    KafkaTemplate<String, Object> kanbanTemplate;

    public GlobalMarkerService(MarkerMapper markerMapper, TokenMarkerPartRepository tokenMarkerPartRepository, GlobalMarkerPartRepository globalMarkerPartRepository,
                               RoleMarkerPartRepository roleMarkerPartRepository, StorageMarkerPartRepository storageMarkerPartRepository, GlobalMarkerRepository globalMarkerRepository,
                               MarkerStarter markerStarter) {
        this.markerMapper = markerMapper;
        this.tokenMarkerPartRepository = tokenMarkerPartRepository;
        this.globalMarkerPartRepository = globalMarkerPartRepository;
        this.roleMarkerPartRepository = roleMarkerPartRepository;
        this.storageMarkerPartRepository = storageMarkerPartRepository;
        this.globalMarkerRepository = globalMarkerRepository;
        this.markerStarter = markerStarter;
    }

    public Flux<AuthenticatedResponse> serve(String partId){
        return this.loadUpGlobalMarker(partId)
                .filter(marker -> StringUtils.hasText(marker.getOrganizationName()))
                .flatMap(globalMarkerRepository::save)
                .flatMap(marker -> markerStarter.loadGlobalMarker(Mono.just(marker)))
                .map(marker -> new AuthenticatedResponse(true,
                        String.format("Hey %s, %s as %s", marker.getEmail(), AuthenticatedResponse.SUCCESSFUL_AUTHENTICATED_ORGANIZATION_MESSAGE, marker.getOrganizationName()),
                        AuthenticatedResponse.Level.PRIMARY, LocalDateTime.now()))
                .defaultIfEmpty(new AuthenticatedResponse(false, AuthenticatedResponse.AUTHENTICATED_ORGANIZATION_MESSAGE, AuthenticatedResponse.Level.INTERNAL, LocalDateTime.now()))
                .flux();
    }

    Mono<GlobalMarker> loadUpGlobalMarker(String partId){
        return Mono.just(new GlobalMarker())
                .flatMap(marker ->  globalMarkerPartRepository.findByPartIdAndCompleteTrue(partId).map(savedMarker -> markerMapper.updateGlobalMarkerFromGlobalPart(marker, savedMarker)))
                .flatMap(response -> storageMarkerPartRepository.findByPartIdAndCompleteTrue(partId).map(savedMarker -> markerMapper.updateGlobalMarkerFromStoragePart(response, savedMarker)))
                .flatMap(response -> roleMarkerPartRepository.findByPartIdAndCompleteTrue(partId).map(savedMarker -> markerMapper.updateGlobalMarkerFromRolePart(response, savedMarker)))
                .flatMap(response -> tokenMarkerPartRepository.findByPartIdAndCompleteTrue(partId).map(savedMarker -> markerMapper.updateGlobalMarkerFromTokenMarker(response, savedMarker)))
                .defaultIfEmpty(new GlobalMarker());
    }

}

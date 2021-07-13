package com.board.wars.init;

import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.store.marker.GlobalMarkerRepository;
import com.board.wars.utils.MarkerContainerUtil;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class MarkerStarter {

    private final GlobalMarkerRepository globalMarkerRepo;
    private Mono<GlobalMarker> globalMarker;

    public MarkerStarter(GlobalMarkerRepository globalMarkerRepo){
        this.globalMarkerRepo = globalMarkerRepo;
    }

    @PostConstruct
    public void init(){
         globalMarker = globalMarkerRepo.findByApplicationName(MarkerContainerUtil.PRINCIPAL_APPLICATION_NAME);
    }

    public Mono<GlobalMarker> getGlobalMarker() {
        return globalMarker;
    }

    public Mono<GlobalMarker> loadGlobalMarker(Mono<GlobalMarker> globalMarker) {
        this.globalMarker = globalMarker;
        return this.globalMarker;
    }

}

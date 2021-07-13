package com.board.wars.utils;

import com.board.wars.domain.ContextUser;
import com.board.wars.domain.InlineContainer;
import com.board.wars.store.InlineContainerRepository;
import com.board.wars.utils.identity.IdentityGeneratorContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class InlineContainerUtil {

    private  InlineContainerRepository inlineContainerRepository;

    //linkers
    private InlineContainer inlineContainer;
    private ContextUser user;

    InlineContainerUtil(InlineContainerRepository inlineContainerRepository) {
        this.inlineContainerRepository = inlineContainerRepository;
    }

    public InlineContainerUtil linkContainer(Duration period){
        InlineContainer inlineContainer = new InlineContainer();
        inlineContainer.setContainerId(IdentityGeneratorContext.generate());
        inlineContainer.setContextUser(user);
        inlineContainer.setValidUntil(LocalDateTime.now().plus(period));
        this.inlineContainer = inlineContainer;
        return this;
    }

    public InlineContainerUtil linkContextUser(SecurityContextImpl context){
        ContextUser user = new ContextUser();
        user.setContext(context);
        this.user = user;
        return this;
    }

    //returns inline container id
    public Mono<String> build(){
        return inlineContainerRepository.save(inlineContainer).map(InlineContainer::getContainerId);
    }

    public Mono<Void> removeInlineContainer(String containerId){
        return inlineContainerRepository.deleteInlineContainersByContainerId(containerId);
    }

    public Mono<InlineContainer> getInlineContainer(String containerId){
        return inlineContainerRepository.getInlineContainerByContainerId(containerId);
    }

    @Scheduled(cron = "0 0/30 * * * ?")
    public void containerCleanUp() {
        inlineContainerRepository.deleteAllByValidUntilBefore(LocalDateTime.now()).subscribeOn(Schedulers.parallel()).log().subscribe();
    }

}

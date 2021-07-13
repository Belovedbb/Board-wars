package com.board.wars.service.core;

import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.domain.HistoryEntity;
import com.board.wars.domain.Team;
import com.board.wars.domain.User;
import com.board.wars.mapper.TeamMapper;
import com.board.wars.mapper.UserMapper;
import com.board.wars.marker.global.GlobalMarker;
import com.board.wars.payload.request.TeamRequestPayload;
import com.board.wars.payload.request.UserRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TeamResponsePayload;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.repository.TeamRepository;
import com.board.wars.service.TeamService;
import com.board.wars.service.UserService;
import com.board.wars.util.Utilities;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.board.wars.util.Utilities.MANAGEMENT;

@Service
public class TeamServiceCore implements TeamService {

    final private TeamRepository teamRepository;
    final private UserService userService;
    final private TeamMapper mapper;
    final private UserMapper userMapper;

    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    KafkaTemplate<String, Object> kanbanTemplate;


    public TeamServiceCore(TeamRepository teamRepository, UserService userService, TeamMapper mapper, UserMapper userMapper) {
        this.teamRepository = teamRepository;
        this.userService = userService;
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @TimeLimiter(name = MANAGEMENT)
    public Mono<GenericResponse<TeamResponsePayload>> getTeam(ServerWebExchange webExchange, Authentication authentication, String code) {
        return teamRepository.findByCode(code)
                .map(team -> mapper.mapTeamDomainToResponsePayload(team, userMapper))
                .flatMap(response -> injectStatelessField(response, authentication, webExchange, code))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @TimeLimiter(name = MANAGEMENT)
    public Flux<GenericResponse<TeamResponsePayload>> getTeams(ServerWebExchange webExchange, Authentication authentication, Boolean active, Integer page, Integer pageSize) {
        Pageable pageable = Utilities.Pager.resolvePagerFromSize(page, pageSize, Sort.by(Sort.Order.asc("code")));
        return  (active == null ? teamRepository.findAll(pageable) :
                teamRepository.findByActiveStatus(active, pageable))
                .map(team -> mapper.mapTeamDomainToResponsePayload(team, userMapper))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @TimeLimiter(name = MANAGEMENT)
    public Flux<GenericResponse<TeamResponsePayload>> getUserTeams(String username, ServerWebExchange webExchange, Authentication authentication, Boolean active, Integer page, Integer pageSize) {
        Pageable pageable = Utilities.Pager.resolvePagerFromSize(page, pageSize, Sort.by(Sort.Order.asc("code")));
        return  (active == null ? teamRepository.getMemberRelatedTeams(username, pageable) : teamRepository.getMemberRelatedTeamsByActive(username, active, pageable))
                .map(team -> mapper.mapTeamDomainToResponsePayload(team, userMapper))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @Retry(name = MANAGEMENT)
    public Mono<GenericResponse<TeamResponsePayload>> updateTeam(String code, TeamRequestPayload teamPayload, ServerWebExchange exchange, Authentication authentication) {
        return Mono.just((GlobalMarker)Objects.requireNonNull(exchange.getAttribute("marker")))
                .filterWhen(globalDetails -> getUpdateOrConstraint(code, authentication.getName()))
                .flatMap(marker -> getTeamForUpdate( teamPayload, code))
                .flatMap(teamRepository::save)
                .doOnSuccess(team -> this.createAndSendLog(team, authentication.getName(), Utilities.PersistentLog.UPDATE, null))
                .map(team -> mapper.mapTeamDomainToResponsePayload(team, userMapper))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(true, HttpStatus.BAD_REQUEST));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @Retry(name = MANAGEMENT)
    public Mono<GenericResponse<TeamResponsePayload>> createTeam(TeamRequestPayload teamPayload, ServerWebExchange exchange, Authentication authentication) {
        return Mono.just((GlobalMarker)Objects.requireNonNull(exchange.getAttribute("marker")))
                .filterWhen(globalDetails -> getCreateCriteria(teamPayload, authentication.getName()))
                .switchIfEmpty(Mono.error(new Utilities.ManagementErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "One or more parameters are malformed", "parameters").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(marker -> getTeamForCreate( teamPayload))
                .doOnSuccess(team -> this.createAndSendLog(team, authentication.getName(), Utilities.PersistentLog.CREATE, null))
                .flatMap(teamRepository::save)
                .map(team -> mapper.mapTeamDomainToResponsePayload(team, userMapper))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(true, HttpStatus.BAD_REQUEST));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @Retry(name = MANAGEMENT)
    public Mono<GenericResponse<Void>> deleteTeam(String code, Authentication authentication, ServerHttpRequest request) {
        return Mono.just(code)
                .filterWhen(codeStream -> getUpdateOrConstraint(codeStream, authentication.getName()))
                .flatMap(teamRepository::findByCode)
                .doOnSuccess(team -> this.createAndSendLog(team, authentication.getName(), Utilities.PersistentLog.DELETE, null))
                .flatMap(teamRepository::delete)
                .then(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(false, null));
    }

    //considering if update should be extended to admin
    private Mono<Boolean> getUpdateOrConstraint(String code, String currentUser){
        return teamRepository.findByCode(code)
                .map(team -> team.getLeader() != null  && currentUser.equals(team.getLeader().getUsername()))
                .defaultIfEmpty(false);
    }

    private Mono<Team> getTeamForUpdate( TeamRequestPayload teamPayload, String code){
        return teamRepository.findByCode(code)
                .map(team -> mapper.mapTeamRequestPayloadToTeam(team, teamPayload))
                .flatMap(team-> updateUserMembers(team, userService.getAllUsersInList(flatMapUserMemberToUsername(team.getMembers()))))
                ;
    }

    private Mono<Team> updateUserMembers(Team team, Flux<User> members){
        return members.collectList().map(userList -> Utilities.setAndGet(team, teamObject -> teamObject.setMembers(userList.toArray(User[]::new))));
    }

    private Mono<Boolean> getCreateCriteria(TeamRequestPayload teamPayload, String currentUser){
        return Mono.just(teamPayload)
                .filter(payload -> payload.getLeader() != null && StringUtils.hasText(payload.getLeader().getUsername())
                        && payload.getLeader().getUsername().equals(currentUser))
                .map(payload -> Utilities.setAndGet(payload, this::populateMembers))
                .filterWhen(payload -> userService.isUserNamesExist(flatMapMemberToUsername(payload.getMembers())))
                .filter(payload -> StringUtils.hasText(payload.getName()))
                .map(payload -> true)
                .defaultIfEmpty(false);
    }

    private void populateMembers(TeamRequestPayload payload){
        if(payload.getMembers() == null ) payload.setMembers(new UserRequestPayload[]{});
        if( !flatMapMemberToUsername(payload.getMembers()).contains(payload.getLeader().getUsername())){
            UserRequestPayload[] userRequestPayloads = new UserRequestPayload[payload.getMembers().length + 1];
            System.arraycopy(payload.getMembers(), 0, userRequestPayloads, 0, payload.getMembers().length);
            UserRequestPayload requestPayload = new UserRequestPayload();
            requestPayload.setUsername(payload.getLeader().getUsername());
            userRequestPayloads[payload.getMembers().length] = requestPayload;
            payload.setMembers(userRequestPayloads);
        }
    }

    private List<String> flatMapMemberToUsername(UserRequestPayload[] users){
        return Arrays.stream(users).map(UserRequestPayload::getUsername).collect(Collectors.toList());
    }

    private List<String> flatMapUserMemberToUsername(User[] users){
        return Arrays.stream(users).map(User::getUsername).collect(Collectors.toList());
    }

    private Mono<Team> getTeamForCreate(TeamRequestPayload teamPayload){
        return Mono.just(teamPayload)
                .map(mapper::mapRequestPayloadToTeamDomain)
                .flatMap(this::injectUnsavedProperties);
    }

    private Mono<Team> injectUnsavedProperties(Team team){
        return teamRepository.findAll().count().defaultIfEmpty(0L)
                .map(size -> Utilities.setAndGet(team, teamObject -> teamObject.setCode(String.format("%05d", size + 1))))
                .flatMap(this::loadUpTeamMemberUsers)
                .flatMap(this::loadUpTeamLeaderUsers);
    }

    private Mono<Team> loadUpTeamMemberUsers(Team team){
        return userService.getAllUsersInList(flatMapUserMemberToUsername(team.getMembers()))
                .buffer().map(users -> Utilities.setAndGet(team, teamDomain -> teamDomain.setMembers(users.toArray(User[]::new))))
                .single();
    }

    private Mono<Team> loadUpTeamLeaderUsers(Team team){
        return userService.getAllUsersInList(flatMapUserMemberToUsername(new User[]{team.getLeader()}))
                .buffer().map(users -> Utilities.setAndGet(team, teamDomain -> teamDomain.setLeader(users.toArray(User[]::new)[0])))
                .single();
    }

    private Mono<TeamResponsePayload> injectStatelessField(TeamResponsePayload response, Authentication authentication, ServerWebExchange exchange, String username){
        return Mono.just(response);
    }


    private History logTeamCreate(Team team, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Team ").append(team.getName()).append(" was successfully created.");
        messageBuilder.append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New Team", LocalDateTime.now(), "Team", username, team);
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTeamUpdate(Team team, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Team ").append(team.getName()).append(" was successfully updated.");
        messageBuilder.append(" Updated By ").append(username);
        History history = new History(messageBuilder.toString(), "Update Team", LocalDateTime.now(), "Team", username, team);
        history.setCategory(HistoryCategory.WARNING);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTeamDelete(Team team, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Team ").append(team.getName()).append(" was successfully deleted.");
        messageBuilder.append(" Deleted By ").append(username);
        History history = new History(messageBuilder.toString(), "Delete Team", LocalDateTime.now(), "Team", username, team);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTeamOperation(Team team, String username, String operation) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Team   was successfully ").append(operation);
        messageBuilder.append(" Operation performed By ").append(username);//moved from operaton 1 to pos 2
        History history = new History(messageBuilder.toString(), "Team " + operation, LocalDateTime.now(), "Team", username, team);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private void createAndSendLog(Team team, String username, Utilities.PersistentLog logType, String operation) {
        History history = getHistoryLog(team, username, logType, operation);
        if(history != null) {
            streamHistory(history);
            persistHistory(history);
        }
    }

    private void streamHistory(History history) {
        this.kanbanTemplate.send("management",  history.getId(), history);
    }

    private void persistHistory(History history) {
        historyRepository.save(new HistoryEntity("domain", history)).subscribe();
    }

    private History getHistoryLog(Team team, String username, Utilities.PersistentLog logType, String operation) {
        switch (logType) {
            case UPDATE: {
                return logTeamUpdate(team, username);
            }
            case CREATE: {
                return logTeamCreate(team, username);
            }
            case DELETE: {
                return  logTeamDelete(team, username);
            }
            case LOG: {
                return  logTeamOperation(team, username, operation);
            }
        }
        return null;
    }

}

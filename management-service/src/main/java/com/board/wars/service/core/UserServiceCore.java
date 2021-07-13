package com.board.wars.service.core;

import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.config.properties.ApplicationProperties;
import com.board.wars.config.properties.RouteProperties;
import com.board.wars.domain.HistoryEntity;
import com.board.wars.domain.Role;
import com.board.wars.domain.User;
import com.board.wars.mapper.UserMapper;
import com.board.wars.marker.global.GlobalMarker;
import com.board.wars.marker.global.RoleMarker;
import com.board.wars.payload.request.UserRequestPayload;
import com.board.wars.payload.response.AuthUserResponse;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.Operation;
import com.board.wars.payload.response.UserResponsePayload;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.repository.UserRepository;
import com.board.wars.service.UserService;
import com.board.wars.util.Utilities;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.board.wars.util.Utilities.MANAGEMENT;

@Service
public class UserServiceCore implements UserService {

    private final UserRepository userRepository;

    private final UserMapper mapper;

    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    KafkaTemplate<String, Object> kanbanTemplate;

    private final WebClient webClient;
    private final RouteProperties routeProperties;
    private final ApplicationProperties appProperties;

    public UserServiceCore(UserRepository userRepository, UserMapper mapper, WebClient webClient, RouteProperties routeProperties, ApplicationProperties appProperties) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.webClient = webClient;
        this.routeProperties = routeProperties;
        this.appProperties = appProperties;
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @TimeLimiter(name = MANAGEMENT)
    public Mono<GenericResponse<UserResponsePayload>> getUser(ServerWebExchange webExchange, Authentication authentication, String username) {
        return userRepository.findByUsername(username)
                .map(mapper::mapUserDomainToResponsePayload)
                .flatMap(response -> injectStatelessField(response, authentication, webExchange, username))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @TimeLimiter(name = MANAGEMENT)
    public Mono<GenericResponse<UserResponsePayload>> getCurrentUser(ServerWebExchange webExchange, Authentication authentication) {
        return getUser(webExchange, authentication, authentication.getName());
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @TimeLimiter(name = MANAGEMENT)
    public Flux<GenericResponse<UserResponsePayload>> getUsers(ServerWebExchange webExchange, Authentication authentication, Boolean active, Integer page, Integer pageSize) {
        Pageable pageable = Utilities.Pager.resolvePagerFromSize(page, pageSize, Sort.by(Sort.Order.asc("username")));
        return  (active == null ? userRepository.findAll(pageable) :
                userRepository.findByActiveStatus(active, pageable))
                .map(mapper::mapUserDomainToResponsePayload)
                .flatMap(response -> injectStatelessField(response, authentication, webExchange, response.getUsername()))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @RateLimiter(name = MANAGEMENT)
    @Retry(name = MANAGEMENT)
    public Mono<GenericResponse<UserResponsePayload>> updateUser(String username, UserRequestPayload userPayload,
                                                                 ServerWebExchange exchange, Authentication authentication) {
         GlobalMarker marker = exchange.getAttribute("marker");
        return getUserOperation(authentication, username)
                .flatMap(operation -> getUserForUpdate(operation, marker, userPayload, username, authentication.getName()))
                .flatMap(userRepository::save)
                .doOnSuccess(user -> this.createAndSendLog(user, authentication.getName(), Utilities.PersistentLog.UPDATE, null))
                .map(mapper::mapUserDomainToResponsePayload)
                .flatMap(response -> injectStatelessField(response, authentication, exchange, response.getUsername()))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(true, HttpStatus.BAD_REQUEST));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @RateLimiter(name = MANAGEMENT)
    @Retry(name = MANAGEMENT)
    public Mono<byte[]> updateUserProfilePicture(Mono<FilePart> file, ServerWebExchange exchange, Authentication authentication, String username) {
        GlobalMarker marker = exchange.getAttribute("marker");
        Assert.notNull(marker, "marker object cant be null");
        return Mono.just(username).filter(name -> authentication.getName().equals(name))
                .switchIfEmpty(Mono.error(new Exception("Can only update logged in user picture")))
                .flatMap(name -> file)
                .filter(filePart -> filePart != null && StringUtils.hasText(Objects.requireNonNull(filePart.headers().getContentType()).getType()))
                .flatMap(filePart -> pictureUserUpdate(marker, filePart, authentication.getName()))
                .doOnSuccess(updated -> this.createAndSendLog(null, authentication.getName(), Utilities.PersistentLog.LOG, "user profile picture updated"))
                .defaultIfEmpty(false)
                .flatMap(result -> getProfilePicture(marker, authentication.getName()));
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @Retry(name = MANAGEMENT)
    public Mono<GenericResponse<Void>> deleteUser(String username, Authentication authentication,  ServerHttpRequest request) {
        AuthUserResponse authUserResponse = new AuthUserResponse();
        authUserResponse.setExpired(true);
        return getUserOperation(authentication, username)
                .filter(operation -> operation == Operation.NATIVE)
                .flatMap(operation -> userRepository.findByUsername(username))
                .flatMap(user -> updateAuthServerUser(user, request, authUserResponse))
                .doOnSuccess(user -> this.createAndSendLog(user, authentication.getName(), Utilities.PersistentLog.DELETE, null))
                .flatMap(userRepository::delete)
                .then(Utilities.ManagementErrorBuilder.emptyResponseWithThrow(false, null));
    }

    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @Retry(name = MANAGEMENT)
    public Mono<User> updateAuthServerUser(User user, ServerHttpRequest request, AuthUserResponse authUserResponse) {
        return webClient.patch().uri(Utilities.resolveRoute(routeProperties.getIntermediate().getAuthServerUpdateUser(), user.getUsername()))
                .headers(httpHeaders -> httpHeaders.add(appProperties.getMarkerTokenHashKey(), request.getHeaders().toSingleValueMap().get(appProperties.getMarkerTokenHashKey())))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(authUserResponse), AuthUserResponse.class)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, response -> Mono.just(new IllegalStateException("unable to clean up: "+ response.statusCode().name())))
                .toBodilessEntity()
                .doOnSuccess(entity -> this.createAndSendLog(user, "System", Utilities.PersistentLog.UPDATE, null))
                //TODO LOG ERROR
                //.onErrorResume(error -> Mono.empty())
                .map(entity -> user);
    }

    @Override
    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @TimeLimiter(name = MANAGEMENT)
    public Mono<byte[]> getProfilePicture(GlobalMarker globalMarker, String name) {
        String pictureLoc = getProfilePictureLinks(name, globalMarker.getStorageMarker().getAllowedTypes(), true, true)[0] ;
        return Mono.just(globalMarker)
                .flatMap(marker -> Mono.just(marker.getStorageMarker().getBaseLocation()) )
                .map(location -> !StringUtils.hasText(location)  || location.equals("/") ? pictureLoc  :  location + pictureLoc)
                .map(this::loadUpProfilePicture)
                .filter(bytes -> bytes.length > 0)
                .switchIfEmpty(Mono.error(new Utilities.ManagementErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "picture does not exist", "picture").build(HttpStatus.NOT_FOUND, null)));
    }

    //TODO use static variable in utils for file size
    private String[] getProfilePictureLinks(String name, String[] exts, boolean defaultLink, boolean withExt){
        String initial = getProfileLink(name);
        String pictureExt = File.separator + "profile." + getPreferredExtension(exts);
        String initialLink = initial + File.separator + Utilities.PICTURE_LENGTH +"_" + Utilities.PICTURE_LENGTH  + (withExt ? pictureExt : "");
        return defaultLink ? new String[]{initialLink}:
                new String[]{initialLink,
                        initial + File.separator + (Utilities.PICTURE_LENGTH * 2) +"_" + (Utilities.PICTURE_LENGTH * 2)  +  (withExt ? pictureExt : ""),
                        initial + File.separator + "original" + (withExt ? pictureExt : "")};
    }

    private String getProfileLink(String name){
        return System.getProperty("user.home") + File.separator + name.replaceAll("@", "") + Math.abs(name.hashCode());
    }

    private String getPreferredExtension(String[] allowedExts){
        for (String ext : allowedExts) {
            switch (ext) {
                case "*":
                case "png":
                    return "png";
                case "jpg":
                case "jpeg":
                    return "jpg";
            }
        }
        throw new UnsupportedOperationException("extension not supported for picture handling");
    }

    private Mono<UserResponsePayload> injectStatelessField(UserResponsePayload response, Authentication authentication, ServerWebExchange exchange, String username){
        return getUserOperation(authentication, username)
                .flatMap(operation -> Utilities.getApiProfileLink(exchange, username).map(link -> getInjectedResponse(exchange, response, link, operation)));
    }

    private UserResponsePayload getInjectedResponse(ServerWebExchange exchange, UserResponsePayload response, String link, Operation operation){
        GlobalMarker marker = exchange.getAttribute("marker");
        response.setOperation(operation);
        assert marker != null;
        getProfilePicture(marker, response.getUsername()).map(res -> {
            response.setProfilePictureLink(link);
            return res;
        }).onErrorResume(error -> {
            response.setProfilePictureLink(null);
            return Mono.empty();
        }).subscribe();
        return response;
    }

    public Mono<Operation> getUserOperation(Authentication authentication, String subject){
        return Mono.just(authentication)
                .filter(auth -> auth.getName().equals(subject))
                .map(auth -> Operation.NATIVE)
                .switchIfEmpty(getDefaultOperation(authentication.getName()))
                ;
    }

    @Override
    public Mono<Boolean> isUserNamesExist(List<String> userNames) {
        return userRepository.countByUsernameInAndExpired(userNames, false)
                .map(count -> count == userNames.size())
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<User> getAllUsersInList(List<String> userNames) {
        return userRepository.findUsersByUsernameInAndExpired(userNames, false);
    }


    private Mono<Operation> getDefaultOperation(String username) {
        return hasAdminRole(username)
                .filter(result -> result)
                .map(res -> Operation.ROLE)
                .defaultIfEmpty(Operation.VIEW);
    }

    private Mono<Boolean> hasAdminRole(String username){
        return userRepository.findByUsername(username).map(user -> extractHasRole(user, Role.ROLE_ADMIN));
    }

    private boolean extractHasRole(User user, String inputRole){
        Role role = user.getRole();
        if(role != null && !CollectionUtils.isEmpty(role.getRoles())){
            for(RoleMarker.RoleEntity roleEntity: role.getRoles()){
                if(roleEntity.getName().equals(inputRole)){
                    return true;
                }
            }
        }
        return false;
    }

    private String[] createResourceDirectories(String name) throws Exception {
            String[] links = getProfilePictureLinks(name, new String[]{"*"}, false, false);
            for (String link: links){
                Files.createDirectories(Paths.get(link));
            }
            return links;
    }

    private Mono<User> getUserForUpdate(Operation operation, GlobalMarker marker, UserRequestPayload payload, String user, String selfUser){
        switch (operation){
            case ROLE:
                if (StringUtils.hasText(payload.getRole()))
                    return userRepository.findByUsername(user).map(userEntity -> updateUserRole(userEntity, marker, payload, selfUser));
            case VIEW:
                return userRepository.findByUsername(selfUser);
            case NATIVE:{
                Mono<User> savedUser = userRepository.findByUsername(selfUser);
                //TODO user loop for role check instead of single value assumption
                return savedUser.filter(userEntity -> StringUtils.hasText(payload.getRole())
                                    && userEntity.getRole().getRoles().get(0).getName().equalsIgnoreCase(Role.ROLE_ADMIN))
                        .map(userEntity -> updateUserRole(userEntity, marker, payload, selfUser))
                        .switchIfEmpty(savedUser)
                        .map(self -> mapper.mapUserRequestPayloadToUser(self, payload))
                        ;
            }
        }
        throw new UnsupportedOperationException("Operation is not supported");
    }

    public User updateUserRole(User savedUser, GlobalMarker marker, UserRequestPayload payload, String self){
        List<RoleMarker.RoleEntity> roleEntities = new Role().getAllRoles(marker)
                .stream()
                .filter(roleEntity -> roleEntity.getName().equalsIgnoreCase(payload.getRole()))
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(roleEntities) ){
            Role role = new Role();
            role.setRoles(roleEntities);
            role.setLastUpdatedBy(self);
            role.setModifiedAt(LocalDateTime.now());
            savedUser.setRole(role);
        }
        return savedUser;
    }

    private Mono<Boolean> pictureUserUpdate(GlobalMarker marker, FilePart part, String user) {
        try {
            createResourceDirectories(user);
            String originalExt = Objects.requireNonNull(part.headers().getContentType()).getSubtype();
            String[] links  = getProfilePictureLinks(user, new String[]{originalExt}, false, true);
                    //original picture must be last index
            Path original = Path.of(links[links.length - 1]);
            return part.transferTo(original).then(loadUpPicture(user, original, part, marker));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }
//TODO
    private Mono<Boolean> loadUpPicture(String user, Path original, FilePart part, GlobalMarker marker) {
        String extension = getPreferredExtension(marker.getStorageMarker().getAllowedTypes());
        String[] links = getProfilePictureLinks(user, new String[]{extension}, false, true);
        return Mono.just(extension).flatMap(preferredExt -> {
            BufferedImage bufferedImage = null, result = null;
            try {
                if (!preferredExt.equalsIgnoreCase(Objects.requireNonNull(part.headers().getContentType()).getSubtype())) {
                    bufferedImage = ImageIO.read(original.toFile());
                    String originalPathValue = original.toString();
                    String replacedPath = Utilities.replaceLast(originalPathValue, FilenameUtils.getExtension(originalPathValue), preferredExt);
                    result = new BufferedImage(
                            bufferedImage.getWidth(),
                            bufferedImage.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                    result.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                    ImageIO.write(result, preferredExt, Path.of(replacedPath).toFile());
                }
                for (int i = 0; i < links.length - 1; i++) {
                    String link = links[i];
                    resizeImage(String.valueOf(original.toAbsolutePath()), link, Utilities.PICTURE_LENGTH * (i + 1));
                }
            }catch (Exception ex){
                return Mono.error(ex);
            }finally {
                if (bufferedImage != null)
                    bufferedImage.flush();
                if (result != null)
                    result.flush();
            }
            return Mono.just(true);
        });
    }

    private void resizeImage(String source, String dest, int size) throws Exception {
        Thumbnails.of(source)
                .size(size, size)
                .toFile(dest);
    }

    private byte[] loadUpProfilePicture(String location){
        File file = new File(location);
        try {
            if(file.exists()) {
                return Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "".getBytes();
    }


    private History logUserCreate(User user, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("User ").append(user.getUsername()).append(" was successfully created.");
        messageBuilder.append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New User", LocalDateTime.now(), "User", username, user);
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logUserUpdate(User user, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("User ").append(user.getUsername()).append(" was successfully updated.");
        messageBuilder.append(" Updated By ").append(username);
        History history = new History(messageBuilder.toString(), "Update User", LocalDateTime.now(), "User", username, user);
        history.setCategory(HistoryCategory.WARNING);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logUserDelete(User user, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("User ").append(user.getUsername()).append(" was successfully deleted.");
        messageBuilder.append(" Deleted By ").append(username);
        History history = new History(messageBuilder.toString(), "Delete User", LocalDateTime.now(), "User", username, user);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logUserOperation(User user, String username, String operation) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("User was successfully ").append(operation);
        messageBuilder.append(" Operation performed By ").append(username);//moved from operaton 1 to pos 2
        History history = new History(messageBuilder.toString(), "User " + operation, LocalDateTime.now(), "User", username, user);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private void createAndSendLog(User user, String username, Utilities.PersistentLog logType, String operation) {
        History history = getHistoryLog(user, username, logType, operation);
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

    private History getHistoryLog(User user, String username, Utilities.PersistentLog logType, String operation) {
        switch (logType) {
            case UPDATE: {
                return logUserUpdate(user, username);
            }
            case CREATE: {
                return logUserCreate(user, username);
            }
            case DELETE: {
                return  logUserDelete(user, username);
            }
            case LOG: {
                return  logUserOperation(user, username, operation);
            }
        }
        return null;
    }
}

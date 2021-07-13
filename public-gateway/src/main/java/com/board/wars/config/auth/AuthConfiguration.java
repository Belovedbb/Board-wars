package com.board.wars.config.auth;

import com.board.wars.config.auth.properties.ApplicationProperties;
import com.board.wars.config.auth.properties.AuthServerProperties;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.HeaderWriterServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.Collections;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class AuthConfiguration {

    private final ApplicationProperties appProperties;

    private final AuthServerProperties authServerProperties;

    private final CookieServerSecurityContextRepository contextRepository;

    private final LogoutHandler logoutHandler;

    final ServerRequestCache requestCache;

    private ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler;

    public static final  String[] WHITELIST_RESOURCE_PATHS = {
            "/favicon.ico",
            "/favicon.png",
            "/assets/**",
            "/*.woff",
            "/ionicons.e9f4c425fc377740601b.ttf",
            "/*.tff",
            "/*.woff2",
            "/*.eot",
            "/*.png",
            "/*.gif",
            "/*.svg",
            "/*.jpg",
            "/*.html",
            "/*.json",
            "/*.css",
            "/*.js"
    };

    public static final  String[] WHITELIST_URL_PATHS = {
            "/auth/**",
            "/oauth2/**",
            "/signin/oauth2/code/**",
            "/"
    };

    public AuthConfiguration(AuthServerProperties authServerProperties, CookieServerSecurityContextRepository contextRepository,
                             LogoutHandler logoutHandler, ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler, ServerRequestCache requestCache, ApplicationProperties appProperties) {
        this.authServerProperties = authServerProperties;
        this.contextRepository = contextRepository;
        this.logoutHandler = logoutHandler;
        this.serverAuthenticationSuccessHandler = serverAuthenticationSuccessHandler;
        this.requestCache = requestCache;
        this.appProperties = appProperties;
    }

    @Bean
    @LoadBalanced
    WebClient.Builder webClientBuilder(){
        return WebClient.builder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        String allowedOrigin = StringUtils.hasText(appProperties.getInterfaceHost()) ? appProperties.getInterfaceHost() : "*";
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedOrigins(Collections.singletonList(allowedOrigin));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Profile("!dev")
    @Bean
    RequestRejectedHandler requestRejectedHandler() {
        return new HttpStatusRequestRejectedHandler();
    }

    @Bean
    WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager, WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create().followRedirect(false).resolver(DefaultAddressResolverGroup.INSTANCE);
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        return builder.filter(oauth2Client).clientConnector(connector).build();
    }

    //@Bean
    ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                          ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        ReactiveOAuth2AuthorizedClientProvider  authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .build();
        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange().pathMatchers(WHITELIST_URL_PATHS).permitAll()
                .and().securityMatcher(new NegatedServerWebExchangeMatcher(ServerWebExchangeMatchers.pathMatchers(WHITELIST_RESOURCE_PATHS)))
                .authorizeExchange().anyExchange().authenticated()
                .and()
                .oauth2Login(loginSpec -> loginSpec.authenticationSuccessHandler(serverAuthenticationSuccessHandler))
                .oauth2Client(withDefaults())
                .cors()
                .and()
                .requestCache().requestCache(requestCache)
                .and()
                .csrf().disable()/*.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())*/
                //.and()
                .securityContextRepository(contextRepository)
                .exceptionHandling()
                .authenticationEntryPoint(new RedirectServerAuthenticationEntryPoint("/auth/error"))
                .accessDeniedHandler(new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
                .and().logout(this::logoutHandler);
        return http.build();
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository(){
        ClientRegistration[] registrations = {githubClientRegistration(), localClientRegistration()};
        return new InMemoryReactiveClientRegistrationRepository(registrations);
    }


    private ClientRegistration githubClientRegistration(){
        AuthServerProperties.GithubProperties github = authServerProperties.getGithubProperties();
        return ClientRegistration.withRegistrationId(github.getRef())
                .clientId(github.getId())
                .clientSecret(github.getSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                .scope(github.getScopes().toArray(String[]::new))
                .redirectUri(github.getRedirect())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri(github.getAuthorize())
                .tokenUri(github.getToken())
                .clientName(github.getName())
                .userInfoAuthenticationMethod(AuthenticationMethod.HEADER)
                .userInfoUri(github.getUserInfo())
                .userNameAttributeName(github.getUserInfoName())
                .build();
    }

    private ClientRegistration localClientRegistration(){
        AuthServerProperties.LocalProperties local = authServerProperties.getLocalProperties();
        return ClientRegistrations.fromIssuerLocation(local.getIssuer())
                .redirectUri(local.getRedirect())
                .clientName(local.getName())
                .clientId(local.getId())
                .clientSecret(local.getSecret())
                .registrationId(local.getRef())
                .scope(local.getScopes().toArray(String[]::new))
                .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();
    }

    private void logoutHandler(ServerHttpSecurity.LogoutSpec spec){
        SecurityContextServerLogoutHandler securityContext = new SecurityContextServerLogoutHandler();
        securityContext.setSecurityContextRepository(contextRepository);
        ClearSiteDataAggregateHttpHeadersWriter.Directive[] directives = {ClearSiteDataAggregateHttpHeadersWriter.Directive.COOKIES, ClearSiteDataAggregateHttpHeadersWriter.Directive.CACHE};
        ClearSiteDataAggregateHttpHeadersWriter writer = new ClearSiteDataAggregateHttpHeadersWriter(directives);
        ServerLogoutHandler clearSiteData = new HeaderWriterServerLogoutHandler(writer);
        DelegatingServerLogoutHandler handler = new DelegatingServerLogoutHandler(securityContext, clearSiteData);
        spec.logoutHandler(handler)
                .logoutSuccessHandler(logoutHandler)
                .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/logout"));
    }




}

package com.sath.sistema_sath.Config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Vinculamos el provider directamente administrado por Spring Beans
                .authenticationProvider(authenticationProvider())
                // Filtro JWT intercepta la petición antes del filtro por defecto de Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Despachos de error y async de Tomcat (SockJS timeout, etc.)
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()

                        // ── PÚBLICOS ───────────────────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // Archivos estáticos del SPA (Vue)
                        .requestMatchers("/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/assets/**", "/images/**", "/imagesForReports/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/s/**", "/ajax/**").permitAll()
                        // Todas las rutas de navegación Vue (el HTML de la SPA es público; la auth la maneja Vue Router)
                        .requestMatchers("/", "/login", "/perfil",
                                "/solicitud", "/solicitud/**",
                                "/atenciones", "/tecnico-asignado", "/administrativo",
                                "/usuario", "/servicio", "/activo", "/historial-activo",
                                "/reportes", "/configuracion", "/tipo-servicio", "/tipo-activo",
                                "/falla", "/dashboard-tecnico", "/notificaciones", "/logs", "/logs-vue",
                                "/error/**").permitAll()

                        // ── PERFIL (todos los autenticados) ───────────────────────────
                        .requestMatchers("/api/perfil/**").authenticated()

                        // ── LOGS (solo Developer) ─────────────────────────────────────
                        .requestMatchers("/api/logs/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")

                        // ── ADMINISTRADOR + DEVELOPER ─────────────────────────────────
                        .requestMatchers("/api/usuario/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/rol/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/permiso/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/usrRol/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/usr-rol/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/rolPermiso/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/usr-permiso/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/cargo/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/unidad/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers(HttpMethod.GET, "/api/servicio/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/servicio/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/activo/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/tipoActivo/**", "/api/tipo-activo/**").hasAnyRole("ADMINISTRADOR","TECNICO" ,  "DEVELOPER")
                        .requestMatchers("/api/persona/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")
                        .requestMatchers("/api/administrativo/**").hasAnyRole("ADMINISTRADOR", "DEVELOPER")

                        // ── TÉCNICO + DEVELOPER ───────────────────────────────────────
                        .requestMatchers("/api/notificaciones/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/solicitud/manual").permitAll()
                        .requestMatchers("/api/solicitud/**").authenticated()
                        .requestMatchers("/api/atencionSolicitud/**", "/api/atencion-solicitud/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/detalleSolicitud/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/fallaReportada/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/falla/**", "/falla/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/tecnicoAsignado/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/informes-equipo-especifico/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/informes-equipo-general/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/reportes/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/evidencia/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")
                        .requestMatchers("/api/prioridad/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "DEVELOPER")

                        // ── RESTO DE ROLES (solo GET) ────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMINISTRADOR", "TECNICO", "SUPERVISOR", "USUARIO", "SOLICITANTE", "APOYO", "DEVELOPER")

                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
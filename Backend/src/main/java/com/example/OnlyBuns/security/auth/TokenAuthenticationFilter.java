package com.example.OnlyBuns.security.auth;

import java.io.IOException;
import com.example.OnlyBuns.util.TokenUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

	private final TokenUtils tokenUtils;
	private final UserDetailsService userDetailsService;

	public TokenAuthenticationFilter(TokenUtils tokenHelper, UserDetailsService userDetailsService) {
		this.tokenUtils = tokenHelper;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
									jakarta.servlet.http.HttpServletResponse response,
									jakarta.servlet.FilterChain filterChain)
			throws jakarta.servlet.ServletException, IOException {

		System.out.println("--- TOKEN FILTER [START]: Obrada zahteva za " + request.getRequestURI() + " ---");

		String authToken = tokenUtils.getToken(request);

		if (authToken != null) {
			System.out.println("TOKEN FILTER: Token pronađen u zahtevu.");
			String username = null;
			try {
				// 1. Čitanje korisničkog imena iz tokena
				username = tokenUtils.getUsernameFromToken(authToken);
				System.out.println("TOKEN FILTER: Korisničko ime iz tokena: '" + username + "'");

			} catch (ExpiredJwtException e) {
				System.err.println("TOKEN FILTER GREŠKA: Token je istekao. Poruka: " + e.getMessage());
			}
			 catch (Exception e) {
				// Hvata sve ostale greške pri parsiranju
				System.err.println("TOKEN FILTER GREŠKA: Nije moguće pročitati korisničko ime iz tokena. Poruka: " + e.getClass().getName() + " - " + e.getMessage());
			}

			// 2. Ako imamo korisničko ime i ako već ne postoji autentifikacija u kontekstu
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				System.out.println("TOKEN FILTER: Kontekst je prazan, vršim autentifikaciju.");
				UserDetails userDetails = null;
				try {
					// 3. Preuzimanje korisnika na osnovu username-a
					userDetails = this.userDetailsService.loadUserByUsername(username);

				} catch (UsernameNotFoundException e) {
					System.err.println("TOKEN FILTER GREŠKA: Korisnik '" + username + "' nije pronađen u bazi podataka.");
				}

				if (userDetails != null) {
					System.out.println("TOKEN FILTER: Detalji korisnika pronađeni. Validacija tokena...");

					// 4. Provera da li je prosleđeni token validan
					if (tokenUtils.validateToken(authToken, userDetails)) {
						System.out.println("TOKEN FILTER: USPEH! Token je validan.");

						// 5. Kreiraj autentifikaciju
						TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
						authentication.setToken(authToken);
						SecurityContextHolder.getContext().setAuthentication(authentication);
						System.out.println("TOKEN FILTER: SecurityContext popunjen za korisnika '" + username + "'.");
					} else {
						// OVDE JE NAJVEROVATNIJE PROBLEM
						System.err.println("TOKEN FILTER NEUSPEH: Metoda 'tokenUtils.validateToken(authToken, userDetails)' je vratila FALSE.");
					}
				}
			}
		} else {
			System.out.println("TOKEN FILTER: Nije pronađen token u Authorization zaglavlju.");
		}

		System.out.println("--- TOKEN FILTER [END]: Prosleđujem zahtev dalje... ---");
		// Prosledi request dalje u sledeći filter
		filterChain.doFilter(request, response);
	}
}
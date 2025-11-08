package com.school.lending.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");

		if (realmAccess == null || realmAccess.isEmpty()) {
			return Collections.emptyList();
		}

		@SuppressWarnings("unchecked")
		Collection<String> roles = (Collection<String>) realmAccess.get("roles");

		return roles.stream().map(role -> "ROLE_" + role.toUpperCase()) // Prefix to match hasRole()
				.map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
	}
}

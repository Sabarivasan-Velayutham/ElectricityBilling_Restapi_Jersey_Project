package com.electricity.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

@SecureUser
@Provider
@Priority(Priorities.AUTHENTICATION)
public class UserTokenFilter implements ContainerRequestFilter {

	private static final String SECRET_KEY = "user_secret_key";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		if (!isAnnotationPresent(requestContext)) {
			return; // Skip filtering if annotation is not present
		}

		// Get the authorization header from the request
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		// Check if the HTTP Authorization header is present and formatted correctly
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new NotAuthorizedException("Authorization header must be provided");
		}

		// Extract the JWT token from the authorization header
		String token = authorizationHeader.substring("Bearer".length()).trim();

		try {

			Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
			String username = claims.getSubject();
			int userId = claims.get("userId", Integer.class);

			// Set the principal in the request context
			requestContext.setSecurityContext(new SecurityContext() {

				@Override
				public Principal getUserPrincipal() {
					return new Principal() {

						@Override
						public String getName() {
							return username;
						}
					};
				}

				@Override
				public boolean isUserInRole(String role) {
					return true;
				}

				@Override
				public boolean isSecure() {
					return false;
				}

				@Override
				public String getAuthenticationScheme() {
					return null;
				}
			});

			// Check if the token's user ID matches the requested user ID
			int requestedUserId = getUserIdFromEndpoint(requestContext.getUriInfo().getPath());
			if (userId != requestedUserId) {
				throw new NotAuthorizedException("Token does not match the requested user ID");
			}

		} catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException
				| IllegalArgumentException e) {
			// Token is invalid or expired
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		}
	}

	private int getUserIdFromEndpoint(String path) {
		// Extract the user ID from the endpoint path
		String[] segments = path.split("/");
		return Integer.parseInt(segments[segments.length - 1]);
	}

	private boolean isAnnotationPresent(ContainerRequestContext requestContext) {
		// Obtain the URI info from the request context
		UriInfo uriInfo = requestContext.getUriInfo();

		// Check if the resource method has the annotation
		List<Object> matchedResources = uriInfo.getMatchedResources();
		// Iterate over the matched resources
		for (Object resource : matchedResources) {
			// Obtain the resource class from the resource object
			Class<?> resourceClass = resource.getClass();

			// Check if the resource class has the ValidateUserId annotation
			if (resourceClass.isAnnotationPresent(SecureUser.class)) {
				System.out.println("SecureUser Class Annotation is present ...");
				return true;
			}
			System.out.println("SecureUser Class Annotation not present ...");

			// Get the list of methods for the resource class
			Method[] methods = resourceClass.getDeclaredMethods();

			// Iterate over the methods
			for (Method method : methods) {
				// Check if the method has the ValidateUserId annotation
				if (method.isAnnotationPresent(SecureUser.class)) {
					System.out.println("SecureUser Method Annotation is present ...");
					return true;
				}
			}
		}

		// Return false if neither the method nor the class has the annotation
		System.out.println("SecureUser Method Annotation not present ...");
		return false;
	}

}

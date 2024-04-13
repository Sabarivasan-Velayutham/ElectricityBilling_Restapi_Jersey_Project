package com.electricity.admin;

import com.electricity.dao.AdminDAO;
import com.electricity.entity.Admin;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.sql.SQLException;
import java.util.Date;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
public class LoginResource {

	private AdminDAO adminDAO = new AdminDAO();

	private static final String SECRET_KEY = "admin_secret_key";
	private static final long EXPIRATION_TIME_MS = 300000; // 5 minutes

	@POST
	@Path("/admin/authentication")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@FormParam("username") String username, @FormParam("password") String password) {
		try {
			if (!isValidCredentials(username, password)) {
				String jsonResponse = "{\"error\":\"Invalid username or password format\"}";
				return Response.status(Response.Status.BAD_REQUEST).entity(jsonResponse).build();
			}

			Admin admin = adminDAO.getByUsernameAndPassword(username, password);
			if (admin != null) {

				String token = issueToken(username);

				// Create a JSON object containing the message and token
				JsonObject responseJson = Json.createObjectBuilder().add("message", "Login successful")
						.add("token", token).build();

				// Return the JSON object as the response
				return Response.ok(responseJson).build();
			} else {
				String jsonResponse = "{\"error\":\"Invalid username or password\"}";
				return Response.status(Response.Status.UNAUTHORIZED).entity(jsonResponse).build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			String jsonResponse = "{\"error\":\"Internal Server Error\"}";
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonResponse).build();
		}
	}

	private String issueToken(String username) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

		return Jwts.builder().setSubject(username).setIssuedAt(now).setExpiration(expiryDate)
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
	}

	private boolean isValidCredentials(String username, String password) {
		if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
			return false;
		}
		return true;
	}

}

package com.electricity.user;

import com.electricity.dao.UserDAO;
import com.electricity.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.sql.SQLException;
import java.util.Date;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
public class LoginResource {

	private UserDAO userDAO = new UserDAO();

	private static final String SECRET_KEY = "user_secret_key";
	private static final long EXPIRATION_TIME_MS = 300000; // 5 minutes

	@POST
	@Path("/authentication")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@FormParam("username") String username, @FormParam("password") String password) {
		try {
			
			if (!isValidCredentials(username, password)) {
                String jsonResponse = "{\"error\":\"Invalid username or password format\"}";
                return Response.status(Response.Status.BAD_REQUEST).entity(jsonResponse).build();
            }
			
			User user = userDAO.getByUsernameAndPassword(username, password);
			if (user != null) {
				String token = issueToken(username, user.getId());
				String jsonResponse = "{\n"
					    + "    \"message\": \"Login successful\",\n"
					    + "    \"instructions\": \"Proceed to search billhistory click the link below and use the provided method\",\n"
					    + "    \"userId\": " + user.getId() + ",\n"
					    + "    \"link_v1\": \"http://localhost:8090/electricity/webapi/v1/user/bills/"+ user.getId() + "\",\n"
					    + "    \"link_v2\": \"http://localhost:8090/electricity/webapi/v2/user/bills/" + user.getId() + "\",\n"
					    + "    \"method\": \"GET\",\n"
					    + "    \"token\": \"" + token + "\"\n"
					    + "}";

				return Response.ok(jsonResponse).build();
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

	private String issueToken(String username, int userId) {
	    Date now = new Date();
	    Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

	    return Jwts.builder()
	            .claim("userId", userId) // Add userId as a claim
	            .setSubject(username)
	            .setIssuedAt(now)
	            .setExpiration(expiryDate)
	            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
	            .compact();
	}

	
	private boolean isValidCredentials(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        return true;
    }

}

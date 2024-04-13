package com.electricity.user;

import com.electricity.dao.UserDAO;
import com.electricity.entity.User;
import com.electricity.filter.SecureUser;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/v2/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResourcesV2 {
	private UserDAO userDao;

	public UserResourcesV2() {
		this.userDao = new UserDAO();
	}

	@POST
	@Path("/registration")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUser(String json) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			User user = mapper.readValue(json, User.class);

			// Validate input fields
	        String validationError = isValidUser(user);
	        if (validationError != null) {
	            return Response.status(Response.Status.BAD_REQUEST).entity(validationError).build();
	        }

			int userId = userDao.registerUser2(user);
			if (userId != -1) {
				String jsonResponse = "{\"message\":\"Registration successful\", "
						+ "\"instructions\":\"Proceed to login by clicking the link below and using the provided method\", "
						+ "\"userId\":" + userId + ", "
						+ "\"link\":\"http://localhost:8090/electricity/webapi/users/authentication\", "
						+ "\"method\":\"POST\", " + "\"input\":\"username and password\"}";
				return Response.status(Response.Status.CREATED).entity(jsonResponse).build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to register user").build();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format").build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
		}
	}

	
	@SecureUser
	@GET
	@Path("/bills/{id}")
	public Response searchBillStatus(@PathParam("id") String userIdParam) {
		try {
			int userId;
			try {
				userId = Integer.parseInt(userIdParam);
				if (userId <= 0) {
					return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user ID: " + userIdParam)
							.build();
				}
			} catch (NumberFormatException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user ID: " + userIdParam).build();
			}

			User user = userDao.getUserById2(userId);
			if (user == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("User with ID " + userId + " not found")
						.build();
			}

			String billStatus = user.getBillStatus();
			String responseMessage;
			if (billStatus.equals("paid")) {
				responseMessage = "Hello " + user.getUsername() + ", your bill is already paid.";
			} else if (billStatus.equals("nobills")) {
				responseMessage = "Hello " + user.getUsername() + ", your bill has not been updated by the admin yet.";
			} else if (billStatus.equals("notpaid")) {
				responseMessage = "Hello " + user.getUsername() + ", your bill is not paid as of now"
						+ "\n If you want to pay, use the link below" + "\n Method : POST "
						+ "\n Input : { payment : CREDIT_CARD or DEBIT_CARD or UPI }\n";
				responseMessage += "/electricity/webapi/v2/users/bills/payment/" + user.getId();
			} else {
				responseMessage = "Invalid bill status";
			}

			String jsonResponse = "{" + "\"id\":" + user.getId() + "," + "\"name\":\"" + user.getUsername() + "\","
					+ "\"message\":\"" + responseMessage + "\"" + "}";

			return Response.status(Response.Status.OK).entity(jsonResponse).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
		}
	}
	

	
	
	@SecureUser
	@POST
	@Path("/bills/payment/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response payBill(@PathParam("id") String userIdParam, PaymentRequest paymentRequest) {
		try {
			int userId;
			try {
				userId = Integer.parseInt(userIdParam);
				if (userId <= 0) {
					return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user ID: " + userIdParam)
							.build();
				}
			} catch (NumberFormatException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user ID: " + userIdParam).build();
			}

			String payment = paymentRequest.getPayment();
			User user = userDao.getUserById2(userId);
			if (user == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("User with ID " + userId + " not found")
						.build();
			}

			String billStatus = user.getBillStatus();
			if (billStatus.equals("paid")) {
				return Response.status(Response.Status.BAD_REQUEST).entity("The bill is already paid").build();
			} else if (billStatus.equals("nobills")) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Bill not yet updated by the admin").build();
			} else if (billStatus.equals("notpaid")) {
				boolean paymentSuccess = processPayment(userId, payment);
				if (paymentSuccess) {
					userDao.updateUserBillStatus(userId, "paid");
					
					String responseMessage = "Payment successful. Bill status updated to 'paid'. Payment method: " + payment;
	                return Response.status(Response.Status.OK)
	                        .entity(responseMessage)
	                        .build();

				} else {
					return Response.status(Response.Status.BAD_REQUEST).entity("Payment failed. Please try again")
							.build();
				}
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Invalid bill status").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
		}
	}
	
	private boolean processPayment(int userId, String payment) {
	    return payment.equals("CREDIT_CARD") || payment.equals("DEBIT_CARD") || payment.equals("UPI");
	}


	public static class PaymentRequest {
		private String payment;

		public String getPayment() {
			return payment;
		}

		public void setPayment(String payment) {
			this.payment = payment;
		}
	}


	private String isValidUser(User user) {
	    if (user == null) {
	        return "User object is null";
	    }

	    if (user.getUsername() == null || user.getUsername().isEmpty()) {
	        return "Username is required";
	    }

	    if (user.getPassword() == null || user.getPassword().isEmpty()) {
	        return "Password is required";
	    }

	    // Validate email
	    if (user.getEmail() == null || !user.getEmail().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
	        return "Invalid email format";
	    }

	    // Validate phone number
	    if (user.getPhoneNo() == null || !user.getPhoneNo().matches("\\d{10}")) {
	        return "Phone number must be 10 digits";
	    }

	    // Validate city and state
	    if (user.getCity() == null || user.getCity().isEmpty() || user.getState() == null || user.getState().isEmpty()) {
	        return "City and state are required";
	    }

	    // Validate pincode
	    if (user.getPincode() == null || !user.getPincode().matches("\\d{6}")) {
	        return "Pincode must be 6 digits";
	    }

	    return null; // No validation errors
	}

}

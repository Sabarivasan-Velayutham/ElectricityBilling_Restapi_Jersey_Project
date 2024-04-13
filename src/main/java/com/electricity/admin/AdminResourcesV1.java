package com.electricity.admin;

import com.electricity.dao.UserDAO;
import com.electricity.entity.User;
import com.electricity.filter.SecureAdmin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

@SecureAdmin
@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResourcesV1 {
    private UserDAO userDao;

    public AdminResourcesV1() {
        this.userDao = new UserDAO();
    }
    
    
    @GET
    public Response getAllUsers() {
        try {
            List<User> users = userDao.getAllUsers1();
            return Response.ok(users).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            User user = mapper.readValue(json, User.class);
            
            if (!isValidUser(user)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user input").build();
            }

            boolean created = userDao.createUser1(user);
       
            if (created) {
                int userId = user.getId();
                String responseMessage = "User created successfully. User ID: " + userId;
                return Response.status(Response.Status.CREATED).entity(responseMessage).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to create user").build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format/Invalid input").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
    }

    
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("id") String idParam, String json) {
        try {
            int id = Integer.parseInt(idParam);
            if (id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ID entered in URL").build();
            }
            ObjectMapper mapper = new ObjectMapper();
            User existingUser = userDao.getUserById1(id);
            if (existingUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
            User updatedUser = mapper.readValue(json, User.class);
           
            if (!isValidUser(updatedUser)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user data").build();
            }
            if (!isValidBillStatus(updatedUser.getBillStatus())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid bill status").build();
            }
            if (updatedUser.getBillAmount() < 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Bill amount cannot be negative").build();
            }
           
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setAddress(updatedUser.getAddress());
            existingUser.setBillAmount(updatedUser.getBillAmount());
            existingUser.setBillStatus(updatedUser.getBillStatus());
            
            userDao.updateUser1(existingUser);
            return Response.status(Response.Status.OK).entity("User updated successfully").build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ID entered in URL").build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format").build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
    

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String userId) {
        try {
            int id = Integer.parseInt(userId);
            boolean deleted = userDao.deleteUser(String.valueOf(id));
            if (deleted) {
                return Response.status(Response.Status.OK).entity("User deleted successfully").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ID entered in URL").build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }

    
    @GET
    @Path("/{id}")
    public Response userById(@PathParam("id") String idParam) {
        try {
            int id = Integer.parseInt(idParam);
            if (id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ID entered in URL").build();
            }
            User user = userDao.getUserById1(id);
            if (user != null) {
                return Response.status(Response.Status.OK).entity(user).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ID entered in URL").build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
    

    @GET
    @Path("searching/{name}")
    public Response getUserByName(@PathParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Name cannot be empty").build();
        }
        if (!isValidName(name)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Name cannot contain numbers or special characters").build();
        }
        
        try {
            List<User> users = userDao.getUserByName1(name);
            if (!users.isEmpty()) {
                return Response.status(Response.Status.OK).entity(users).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("No users found with the specified name").build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
    private boolean isValidName(String name) {
        return Pattern.matches("[a-zA-Z\\s]+", name);
    }
    

    @GET
    @Path("/sorting")
    public Response sortAllUsers(@QueryParam("by") String sortBy,
                                 @QueryParam("order") String sortOrder) {
        try {
            if (!isValidSortField(sortBy)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid sortBy parameter").build();
            }
            if (!isValidSortOrder(sortOrder)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid sortOrder parameter").build();
            }
            
            List<User> users = userDao.getAllUsers1(sortBy, sortOrder);
            if (!users.isEmpty()) {
                return Response.status(Response.Status.OK).entity(users).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("No users found").build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
   
    private boolean isValidSortField(String sortBy) {
        String[] allowedSortFields = {"id", "username"};
        for (String field : allowedSortFields) {
            if (field.equals(sortBy)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isValidSortOrder(String sortOrder) {
        String[] allowedSortOrders = {"asc", "desc"};
        for (String order : allowedSortOrders) {
            if (order.equals(sortOrder)) {
                return true;
            }
        }
        return false;
    }
    
    
    @PATCH
    @Path("/updateuserbill/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    
    public Response patchUser(@PathParam("id") String idParam, String json) {
        try {
            int id = Integer.parseInt(idParam);
            if (id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ID entered in URL").build();
            }
            ObjectMapper mapper = new ObjectMapper();
            User existingUser = userDao.getUserById1(id);
            if (existingUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            // Deserialize JSON payload into a JsonNode
            JsonNode patchData = mapper.readTree(json);

            // Update user fields if provided in the JSON
            if (patchData.has("billAmount")) {
                Double billAmount = patchData.get("billAmount").asDouble();
                if (billAmount == null || billAmount <= 0) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Bill amount must be greater than zero").build();
                }
                existingUser.setBillAmount(billAmount);
            }
            if (patchData.has("billStatus")) {
                String billStatus = patchData.get("billStatus").asText();
                if (!isValidBillStatus(billStatus)) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Invalid bill status").build();
                }
                existingUser.setBillStatus(billStatus);
            }
            userDao.updateUser1(existingUser);
            return Response.status(Response.Status.OK).entity("Bill updated successfully").build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ID entered in URL").build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format").build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
    
    
    private boolean isValidUser(User user) {
        
        if (user == null || user.getUsername() == null || !user.getUsername().matches("[a-zA-Z]+( [a-zA-Z]+)*")) {
            return false;
        }
        
        if (user.getAddress() == null || !user.getAddress().matches(".*[a-zA-Z]+.*")) {
            return false;
        }
        
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return false;
        }
       
        return true;
    }
    
   
    private boolean isValidBillStatus(String billStatus) {
    	
        return "paid".equals(billStatus) || "notpaid".equals(billStatus);
    }
}

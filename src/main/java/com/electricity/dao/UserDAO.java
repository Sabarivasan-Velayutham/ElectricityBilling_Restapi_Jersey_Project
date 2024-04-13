package com.electricity.dao;

import com.electricity.db.DBConnection;
import com.electricity.entity.User;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {
	private static final String SELECT_BY_USERNAME_PASSWORD = "SELECT id, username, password, address, billamount, billstatus FROM userinfo WHERE username = ? ";
	
	private static final String SELECT_BY_ID_V1 = "SELECT id, username, password, address, billamount, billstatus FROM userinfo WHERE id = ?";
	private static final String SELECT_BY_ID_V2 = "SELECT * FROM userinfo WHERE id = ?";

	
	private static final String DELETE_USER = "DELETE FROM userinfo WHERE id = ?";
	
	private static final String SELECT_BY_NAME_V1 = "SELECT id, username, password, address, billamount, billstatus FROM userinfo WHERE username LIKE ?";
	private static final String SELECT_BY_NAME_V2 = "SELECT * FROM userinfo WHERE username LIKE ?";

	
	private static final String SELECT_ALL_USERS_V1 = "SELECT id, username, password, address, billamount, billstatus FROM userinfo";
	private static final String SELECT_ALL_USERS_V2 = "SELECT * FROM userinfo";
	
	private static final String UPDATE_BILL_STATUS = "UPDATE userinfo SET billstatus = ? WHERE id = ?";
	
	private static final String UPDATE_USER_V1 = "UPDATE userinfo SET "
			+ "username=?, password=?, address=?, billamount=?, billstatus=? WHERE id=?";
	private static final String UPDATE_USER_V2 = "UPDATE userinfo SET "
			+ "username=?, password=?, address=?, billamount=?, billstatus=?, email=?, phoneNo=?, city=?, state=?, pincode=? WHERE id=?";

	private static final String INSERT_USER_V1 = "INSERT INTO userinfo (username, password, address, billamount, billstatus) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_USER_V2 = "INSERT INTO userinfo "
			+ "(username, password, address, billamount, billstatus, email, phoneno, city, state, pincode) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	public User getByUsernameAndPassword(String username, String password) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(SELECT_BY_USERNAME_PASSWORD)) {
			statement.setString(1, username);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					String hashedPassword = rs.getString("password");
					// Check if the provided password matches the hashed password from the database
					// using BCrypt
					if (BCrypt.checkpw(password, hashedPassword)) {
						User user = new User();
						user.setId(rs.getInt("id"));
						user.setUsername(rs.getString("username"));
						user.setPassword(rs.getString("password"));
						user.setAddress(rs.getString("address"));
						user.setBillAmount(rs.getDouble("billamount"));
						user.setBillStatus(rs.getString("billstatus"));
						return user;
					}
				}
			}
		}
		return null;
	}

	public boolean createUser1(User user) {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(INSERT_USER_V1, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, user.getUsername());

			// Hash the password using BCrypt
			String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			statement.setString(2, hashedPassword);

			statement.setString(3, user.getAddress());
			statement.setDouble(4, user.getBillAmount());
			statement.setString(5, "notpaid");

			int rowsInserted = statement.executeUpdate();

			if (rowsInserted > 0) {
				// Retrieve the generated user ID
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int userId = generatedKeys.getInt(1);
						user.setId(userId); // Set the generated user ID
					}
				}
			}
			return rowsInserted > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean createUser2(User user) {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(INSERT_USER_V2, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, user.getUsername());

			// Hash the password using BCrypt
			String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			statement.setString(2, hashedPassword);

			statement.setString(3, user.getAddress());
			statement.setDouble(4, user.getBillAmount());
			statement.setString(5, "notpaid");
			statement.setString(6, user.getEmail());
			statement.setString(7, user.getPhoneNo());
			statement.setString(8, user.getCity());
			statement.setString(9, user.getState());
			statement.setString(10, user.getPincode());

			int rowsInserted = statement.executeUpdate();

			if (rowsInserted > 0) {
				// Retrieve the generated user ID
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int userId = generatedKeys.getInt(1);
						user.setId(userId); // Set the generated user ID
					}
				}
			}
			return rowsInserted > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	

	public int registerUser1(User user) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(INSERT_USER_V1, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, user.getUsername());

			// Hash the password using BCrypt
			String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			statement.setString(2, hashedPassword);
//            statement.setString(2, user.getPassword());
			statement.setString(3, user.getAddress());
			statement.setDouble(4, 0);
			statement.setString(5, "nobills");

			int rowsInserted = statement.executeUpdate();

			if (rowsInserted > 0) {
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}
		}
		return -1; // Return -1 if registration fails
	}

	public int registerUser2(User user) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(INSERT_USER_V2, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, user.getUsername());

			// Hash the password using BCrypt
			String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			statement.setString(2, hashedPassword);
//            statement.setString(2, user.getPassword());
			statement.setString(3, user.getAddress());
			statement.setDouble(4, 0);
			statement.setString(5, "nobills");
			statement.setString(6, user.getEmail());
			statement.setString(7, user.getPhoneNo());
			statement.setString(8, user.getCity());
			statement.setString(9, user.getState());
			statement.setString(10, user.getPincode());

			int rowsInserted = statement.executeUpdate();

			if (rowsInserted > 0) {
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}
		}
		return -1; // Return -1 if registration fails
	}

	public User getUserById1(int id) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(SELECT_BY_ID_V1)) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					User user = new User(resultSet.getInt("id"), resultSet.getString("username"),
							resultSet.getString("password"), resultSet.getString("address"),
							resultSet.getDouble("billamount"), resultSet.getString("billstatus"));
					return user;
				} else {
					return null;
				}
			}
		}
	}
	
	public User getUserById2(int id) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(SELECT_BY_ID_V2)) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					User user = new User(
							resultSet.getInt("id"), 
							resultSet.getString("username"),
							resultSet.getString("password"), 
							resultSet.getString("address"),
							resultSet.getDouble("billamount"), 
							resultSet.getString("billstatus"),
							resultSet.getString("email"),
							resultSet.getString("phoneNo"),
							resultSet.getString("city"), 
							resultSet.getString("state"),
							resultSet.getString("pincode")
							);;
					return user;
				} else {
					return null;
				}
			}
		}
	}

	public void updateUser1(User user) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(UPDATE_USER_V1)) {
			statement.setString(1, user.getUsername());
			// Hash the password using BCrypt
			String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			statement.setString(2, hashedPassword);
//			            statement.setString(2, user.getPassword());
			statement.setString(3, user.getAddress());
			statement.setDouble(4, user.getBillAmount());
			statement.setString(5, user.getBillStatus());
			statement.setInt(6, user.getId());
			statement.executeUpdate();
		}
	}
	
	public void updateUser2(User user) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(UPDATE_USER_V2)) {
			statement.setString(1, user.getUsername());
			// Hash the password using BCrypt
			String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			statement.setString(2, hashedPassword);
//			            statement.setString(2, user.getPassword());
			statement.setString(3, user.getAddress());
			statement.setDouble(4, user.getBillAmount());
			statement.setString(5, user.getBillStatus());
			statement.setString(6, user.getEmail());
	        statement.setString(7, user.getPhoneNo());
	        statement.setString(8, user.getCity());
	        statement.setString(9, user.getState());
	        statement.setString(10, user.getPincode());
	        statement.setInt(11, user.getId());
			statement.executeUpdate();
		}
	}
	

	public List<User> getAllUsers1() throws SQLException {
		List<User> users = new ArrayList<>();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(SELECT_ALL_USERS_V1);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				User user = new User(resultSet.getInt("id"), resultSet.getString("username"),
						resultSet.getString("password"), resultSet.getString("address"),
						resultSet.getDouble("billamount"), resultSet.getString("billstatus"));
				users.add(user);
			}
		}
		return users;
	}

	public List<User> getAllUsers2() throws SQLException {
		List<User> users = new ArrayList<>();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(SELECT_ALL_USERS_V2);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				User user = new User(
						resultSet.getInt("id"), 
						resultSet.getString("username"),
						resultSet.getString("password"), 
						resultSet.getString("address"),
						resultSet.getDouble("billamount"), 
						resultSet.getString("billstatus"),
						resultSet.getString("email"),
						resultSet.getString("phoneNo"),
						resultSet.getString("city"), 
						resultSet.getString("state"),
						resultSet.getString("pincode")
						);
				users.add(user);
			}
		}
		return users;
	}

	public boolean deleteUser(String userId) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(DELETE_USER)) {
			statement.setInt(1, Integer.parseInt(userId));
			int rowsDeleted = statement.executeUpdate();
			return rowsDeleted > 0;
		}
	}

	public List<User> getUserByName1(String name) throws SQLException {
		List<User> users = new ArrayList<>();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(SELECT_BY_NAME_V1)) {
			statement.setString(1, "%" + name + "%");
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					User user = new User();
					user.setId(resultSet.getInt("id"));
					user.setUsername(resultSet.getString("username"));
					user.setPassword(resultSet.getString("password"));
					user.setAddress(resultSet.getString("address"));
					user.setBillAmount(resultSet.getDouble("billamount"));
					user.setBillStatus(resultSet.getString("billstatus"));
					users.add(user);
				}
			}
		}
		return users;
	}
	
	public List<User> getUserByName2(String name) throws SQLException {
		List<User> users = new ArrayList<>();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(SELECT_BY_NAME_V2)) {
			statement.setString(1, "%" + name + "%");
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					User user = new User();
					user.setId(resultSet.getInt("id"));
					user.setUsername(resultSet.getString("username"));
					user.setPassword(resultSet.getString("password"));
					user.setAddress(resultSet.getString("address"));
					user.setBillAmount(resultSet.getDouble("billamount"));
					user.setBillStatus(resultSet.getString("billstatus"));
					user.setEmail(resultSet.getString("email"));
					user.setPhoneNo(resultSet.getString("phoneNo"));
					user.setCity(resultSet.getString("city"));
					user.setState(resultSet.getString("state"));
					user.setPincode(resultSet.getString("pincode"));
					users.add(user);
				}
			}
		}
		return users;
	}
	

	public List<User> getAllUsers1(String sortBy, String sortOrder) throws SQLException {
		List<User> users = new ArrayList<>();
		String query = SELECT_ALL_USERS_V1;
		if (sortBy != null && !sortBy.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
			query += " ORDER BY " + sortBy + " " + sortOrder;
		}
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String username = resultSet.getString("username");
				String password = resultSet.getString("password");
				String address = resultSet.getString("address");
				double billAmount = resultSet.getDouble("billamount");
				String billStatus = resultSet.getString("billstatus");
				User user = new User(username, password, address, billAmount, billStatus);
				user.setId(id);
				users.add(user);
			}
		}
		return users;
	}
	
	public List<User> getAllUsers2(String sortBy, String sortOrder) throws SQLException {
		List<User> users = new ArrayList<>();
		String query = SELECT_ALL_USERS_V2;
		if (sortBy != null && !sortBy.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
			query += " ORDER BY " + sortBy + " " + sortOrder;
		}
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement statement = conn.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String username = resultSet.getString("username");
				String password = resultSet.getString("password");
				String address = resultSet.getString("address");
				double billAmount = resultSet.getDouble("billamount");
				String billStatus = resultSet.getString("billstatus");
				String email = resultSet.getString("email");
				String phoneNo = resultSet.getString("phoneNo");
				String city = resultSet.getString("city");
				String state = resultSet.getString("state");
				String pincode = resultSet.getString("pincode");
				User user = new User(username, password, address, billAmount, billStatus, email, phoneNo, city, state, pincode);
				user.setId(id);
				users.add(user);
			}
		}
		return users;
	}
	

	public void updateUserBillStatus(int userId, String billStatus) throws SQLException {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(UPDATE_BILL_STATUS)) {
			stmt.setString(1, billStatus);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		}
	}
}

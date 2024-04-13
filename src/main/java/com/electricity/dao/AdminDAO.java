package com.electricity.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.electricity.db.DBConnection;
import com.electricity.entity.Admin;

public class AdminDAO {
    public Admin getByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT username, password FROM admininfo WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setUsername(rs.getString("username"));
                    admin.setPassword(rs.getString("password"));
                    return admin;
                }
            }
        }
        return null;
    }
}
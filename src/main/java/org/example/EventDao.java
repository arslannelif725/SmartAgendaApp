package org.example;

import main.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventDao {

    // Yeni etkinlik ekleme metodu
    public boolean addEvent(String username, String eventDate, String title, String description, String importance) {
        String query = "INSERT INTO events (username, event_date, title, description, importance) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, eventDate);
            pstmt.setString(3, title);
            pstmt.setString(4, description);
            pstmt.setString(5, importance);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Etkinlik ekleme hatası: " + e.getMessage());
            return false;
        }
    }
}
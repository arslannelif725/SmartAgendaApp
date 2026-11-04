package org.example;

import main.DatabaseConnection;

import javax.swing.*;
import java.sql.Connection;

public class Main {
    public static  void main(String[] args) {
        //veritabanı bağlantısını test et
        Connection connection = DatabaseConnection.getConnection();
        if (connection != null) {
            System.out.println("Test başarılı: veritabanına bağlanıldı");
        } else {
            System.out.println("Bağlantı kurulamadı!");
        }
        // giriş ekranını aç
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
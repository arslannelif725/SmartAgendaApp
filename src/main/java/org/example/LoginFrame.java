package org.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

public class LoginFrame extends JFrame {
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JCheckBox rememberMeCheckBox;

    private UserDao userDao;
    private Preferences prefs;

    public LoginFrame() {
        userDao = new UserDao();
        prefs = Preferences.userNodeForPackage(LoginFrame.class);

        setTitle("Smart Agenda - Giriş Yap");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Kullanıcı Adı:");
        userLabel.setBounds(50, 40, 100, 25);
        panel.add(userLabel);

        userTextField = new JTextField();
        userTextField.setBounds(150, 40, 160, 25);
        panel.add(userTextField);

        JLabel passwordLabel = new JLabel("Şifre:");
        passwordLabel.setBounds(50, 90, 100, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 90, 160, 25);
        panel.add(passwordField);

        // Beni Hatırla Seçeneği
        rememberMeCheckBox = new JCheckBox("Beni Hatırla");
        rememberMeCheckBox.setBounds(150, 130, 160, 25);
        panel.add(rememberMeCheckBox);

        loginButton = new JButton("Giriş Yap");
        loginButton.setBounds(50, 180, 110, 30);
        panel.add(loginButton);

        registerButton = new JButton("Kayıt Ol");
        registerButton.setBounds(180, 180, 130, 30);
        panel.add(registerButton);

        add(panel);

        // Daha önceden kaydedilen kullanıcı adı varsa doldur
        String rememberedUser = prefs.get("saved_username", "");
        if (!rememberedUser.isEmpty()) {
            userTextField.setText(rememberedUser);
            rememberMeCheckBox.setSelected(true);
        }

        // Giriş Yap butonuna tıklama olayı
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userTextField.getText();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Lütfen tüm alanları doldurun!");
                    return;
                }

                boolean isSuccess = userDao.loginUser(username, password);
                if (isSuccess) {
                    if (rememberMeCheckBox.isSelected()) {
                        prefs.put("saved_username", username);
                    } else {
                        prefs.remove("saved_username");
                    }

                    JOptionPane.showMessageDialog(null, "Giriş Başarılı! Hoş geldin " + username);

                    // Ana ajanda ekranını aç ve giriş ekranını kapat
                    new AgendaFrame(username).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Kullanıcı adı veya şifre hatalı!");
                }
            }
        });

        // Kayıt Ol butonuna tıklama olayı
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userTextField.getText();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Lütfen kayıt için kullanıcı adı ve şifre girin!");
                    return;
                }

                boolean isRegistered = userDao.registerUser(username, password);
                if (isRegistered) {
                    JOptionPane.showMessageDialog(null, "Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
                } else {
                    JOptionPane.showMessageDialog(null, "Kayıt olunamadı. Bu kullanıcı adı zaten alınmış olabilir.");
                }
            }
        });
    }
}
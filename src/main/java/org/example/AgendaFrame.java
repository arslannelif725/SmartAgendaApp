package org.example;

import com.toedter.calendar.JDateChooser;
import main.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AgendaFrame extends JFrame {
    private JTextField titleField;
    private JDateChooser dateChooser;
    private JTextArea descTextArea;
    private JComboBox<String> importanceComboBox;
    private JButton addButton;
    private JButton deleteButton;
    private JTextField searchField; // Arama Kutusu
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter; // Filtreleme yöneticisi

    private EventDao eventDao;
    private String loggedInUsername;

    public AgendaFrame(String username) {
        this.loggedInUsername = username;
        eventDao = new EventDao();

        setTitle("Smart Agenda - Gelişmiş Web Paneli (" + username + ")");
        setSize(800, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // --- SOL TARAF: FORM ALANI ---
        JLabel titleLabel = new JLabel("Etkinlik Başlığı:");
        titleLabel.setBounds(20, 20, 120, 25);
        add(titleLabel);

        titleField = new JTextField();
        titleField.setBounds(140, 20, 180, 25);
        add(titleField);

        JLabel dateLabel = new JLabel("Tarih Seç:");
        dateLabel.setBounds(20, 65, 120, 25);
        add(dateLabel);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setBounds(140, 65, 180, 25);
        add(dateChooser);

        JLabel importanceLabel = new JLabel("Önem Derecesi:");
        importanceLabel.setBounds(20, 110, 120, 25);
        add(importanceLabel);

        String[] priorities = {"Yüksek", "Orta", "Düşük"};
        importanceComboBox = new JComboBox<>(priorities);
        importanceComboBox.setBounds(140, 110, 180, 25);
        add(importanceComboBox);

        JLabel descLabel = new JLabel("Açıklama:");
        descLabel.setBounds(20, 150, 120, 25);
        add(descLabel);

        descTextArea = new JTextArea();
        JScrollPane scrollPaneDesc = new JScrollPane(descTextArea);
        scrollPaneDesc.setBounds(140, 150, 180, 75);
        add(scrollPaneDesc);

        addButton = new JButton("Etkinlik Ekle");
        addButton.setBounds(140, 245, 180, 35);
        add(addButton);

        // --- SAĞ TARAF: ARAMA ÇUBUĞU VE TABLO ---
        JLabel searchLabel = new JLabel("🔍 Ara:");
        searchLabel.setBounds(340, 20, 50, 25);
        add(searchLabel);

        searchField = new JTextField();
        searchField.setBounds(390, 20, 375, 25);
        add(searchField);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Tarih", "Başlık", "Önem", "Açıklama"}, 0);
        eventTable = new JTable(tableModel);

        // Tablo filtreleme (Arama için)
        rowSorter = new TableRowSorter<>(tableModel);
        eventTable.setRowSorter(rowSorter);

        JScrollPane scrollPaneTable = new JScrollPane(eventTable);
        scrollPaneTable.setBounds(340, 55, 425, 230);
        add(scrollPaneTable);

        // --- ÖNEM DERECESİNE GÖRE SATIR RENKLENDİRME ---
        eventTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String importance = (String) table.getModel().getValueAt(modelRow, 3);
                    if ("Yüksek".equals(importance)) {
                        c.setBackground(new Color(255, 225, 225)); // Açık Kırmızı
                    } else if ("Orta".equals(importance)) {
                        c.setBackground(new Color(255, 245, 210)); // Açık Sarı
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });

        // --- ALT KISIM: SİLME BUTONU ---
        deleteButton = new JButton("Seçili Etkinliği Sil");
        deleteButton.setBounds(340, 295, 425, 35);
        deleteButton.setBackground(new Color(220, 80, 80));
        deleteButton.setForeground(Color.WHITE);
        add(deleteButton);

        // Verileri yükle ve uyarıları kontrol et
        loadUserEvents();
        checkUpcomingEvents();

        // Arama Çubuğu Dinleyicisi (Yazdıkça anlık filtrele)
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // Etkinlik Ekle Butonu Olayı
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                Date selectedDate = dateChooser.getDate();
                String description = descTextArea.getText();
                String importance = (String) importanceComboBox.getSelectedItem();

                if (title.isEmpty() || selectedDate == null) {
                    JOptionPane.showMessageDialog(null, "Lütfen başlık doldurun ve takvimden bir tarih seçin!");
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = sdf.format(selectedDate);

                boolean isAdded = eventDao.addEvent(loggedInUsername, dateStr, title, description, importance);
                if (isAdded) {
                    JOptionPane.showMessageDialog(null, "Etkinlik başarıyla eklendi!");
                    titleField.setText("");
                    descTextArea.setText("");
                    dateChooser.setDate(null);
                    loadUserEvents();
                } else {
                    JOptionPane.showMessageDialog(null, "Etkinlik eklenirken bir hata oluştu.");
                }
            }
        });

        // Etkinlik Sil Butonu Olayı
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = eventTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Lütfen silmek için tablodan bir etkinlik seçin!");
                    return;
                }

                // Tablo filtrelenmiş olsa bile gerçek satır indeksini yakala
                int modelRow = eventTable.convertRowIndexToModel(selectedRow);
                int eventId = (int) tableModel.getValueAt(modelRow, 0);

                boolean isDeleted = deleteEventFromDb(eventId);
                if (isDeleted) {
                    JOptionPane.showMessageDialog(null, "Etkinlik silindi.");
                    loadUserEvents();
                } else {
                    JOptionPane.showMessageDialog(null, "Silme sırasında hata oluştu.");
                }
            }
        });
    }

    // Tabloda anlık arama yapma metodu
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    // Kullanıcı etkinliklerini yükleme
    private void loadUserEvents() {
        tableModel.setRowCount(0);
        String query = "SELECT id, event_date, title, importance, description FROM events WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInUsername);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("event_date");
                String title = rs.getString("title");
                String importance = rs.getString("importance");
                String description = rs.getString("description");

                tableModel.addRow(new Object[]{id, date, title, importance, description});
            }

        } catch (SQLException e) {
            System.out.println("Tablo yükleme hatası: " + e.getMessage());
        }
    }

    // Veritabanından etkinlik silme
    private boolean deleteEventFromDb(int eventId) {
        String query = "DELETE FROM events WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, eventId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Silme hatası: " + e.getMessage());
            return false;
        }
    }

    // Son 1 gün kalan etkinlikler için uyarı sistemi
    private void checkUpcomingEvents() {
        String query = "SELECT title, event_date FROM events WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInUsername);
            ResultSet rs = pstmt.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            StringBuilder alertMessage = new StringBuilder();

            while (rs.next()) {
                String title = rs.getString("title");
                String dateStr = rs.getString("event_date");

                try {
                    Date eventDate = sdf.parse(dateStr);
                    long diffInMillies = eventDate.getTime() - today.getTime();
                    long diffDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    if (diffDays >= 0 && diffDays <= 1) {
                        alertMessage.append("• ").append(title).append(" (Tarih: ").append(dateStr).append(")\n");
                    }
                } catch (Exception ignored) {}
            }

            if (alertMessage.length() > 0) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ DİKKAT! Aşağıdaki etkinliklerinize 1 gün veya daha az kaldı:\n\n" + alertMessage.toString(),
                        "Akıllı Hatırlatıcı",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            System.out.println("Uyarı kontrolü hatası: " + e.getMessage());
        }
    }
}
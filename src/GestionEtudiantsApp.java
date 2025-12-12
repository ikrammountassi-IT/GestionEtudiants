import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class GestionEtudiantsApp extends JFrame {

    private JTextField nomField, prenomField, note1Field, note2Field;
    private JButton ajouterButton, modifierButton, supprimerButton;
    private JTable table;
    private DefaultTableModel model;

    public GestionEtudiantsApp() {
        setTitle("Gestion des Étudiants");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        chargerIcone();
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        header.setBackground(new Color(0, 100, 0));
        header.setPreferredSize(new Dimension(2000, 80));

        JLabel logoLabel = new JLabel();

        try {
            java.net.URL url = getClass().getResource("/logo.jpg");
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(150, 80, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        header.add(logoLabel);

        JLabel title = new JLabel("Gestion des Étudiants:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        header.add(title);

        add(header, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(3, 4, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.setBackground(new Color(245, 245, 245));

        form.add(new JLabel("Nom :"));
        nomField = new JTextField();
        styleField(nomField);
        form.add(nomField);

        form.add(new JLabel("Prénom :"));
        prenomField = new JTextField();
        styleField(prenomField);
        form.add(prenomField);

        form.add(new JLabel("Note 1 :"));
        note1Field = new JTextField();
        styleField(note1Field);
        form.add(note1Field);

        form.add(new JLabel("Note 2 :"));
        note2Field = new JTextField();
        styleField(note2Field);
        form.add(note2Field);

        ajouterButton = new JButton("Ajouter");
        styleButton(ajouterButton, new Color(34, 139, 34));
        form.add(ajouterButton);

        modifierButton = new JButton("Modifier");
        styleButton(modifierButton, new Color(30, 144, 255));
        form.add(modifierButton);

        supprimerButton = new JButton("Supprimer");
        styleButton(supprimerButton, new Color(220, 20, 60));
        form.add(supprimerButton);

        add(form, BorderLayout.SOUTH);

        model = new DefaultTableModel(new String[] { "ID", "Nom", "Prénom", "Note 1", "Note 2", "Moyenne" }, 0);
        table = new JTable(model);

        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(230, 230, 230));

        DefaultTableCellRenderer render = new DefaultTableCellRenderer() {
    public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
        Component comp = super.getTableCellRendererComponent(t, v, sel, f, r, c);
        if (!sel) {
           Color c1 = new Color(204, 255, 204);  
                  Color c2 = new Color(230, 255, 230);  



            comp.setBackground(r % 2 == 0 ? c1 : c2);
        }
        return comp;
    }
};

        table.setDefaultRenderer(Object.class, render);

        add(new JScrollPane(table), BorderLayout.CENTER);

        chargerEtudiants();

        ajouterButton.addActionListener(e -> ajouterEtudiant());
        modifierButton.addActionListener(e -> modifierEtudiant());
        supprimerButton.addActionListener(e -> supprimerEtudiant());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) {
                    nomField.setText(model.getValueAt(r, 1).toString());
                    prenomField.setText(model.getValueAt(r, 2).toString());
                    note1Field.setText(model.getValueAt(r, 3).toString());
                    note2Field.setText(model.getValueAt(r, 4).toString());
                }
            }
        });
    }

    private void chargerEtudiants() {
        try (Connection conn = Database.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM etudiants")) {

            model.setRowCount(0);

            while (rs.next()) {
                double m = (rs.getDouble("note1") + rs.getDouble("note2")) / 2;

                model.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getDouble("note1"),
                        rs.getDouble("note2"),
                        m
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajouterEtudiant() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String n1 = note1Field.getText().trim();
        String n2 = note2Field.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || n1.isEmpty() || n2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs !");
            return;
        }

        try {
            double note1 = Double.parseDouble(n1);
            double note2 = Double.parseDouble(n2);

            Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO etudiants(nom, prenom, note1, note2) VALUES (?, ?, ?, ?)");

            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setDouble(3, note1);
            ps.setDouble(4, note2);
            ps.executeUpdate();

            chargerEtudiants();
            viderChamps();

            JOptionPane.showMessageDialog(this, "Etudiant ajouté !");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : notes invalides !");
        }
    }

    private void modifierEtudiant() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez une ligne !");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(r, 0).toString());

        try {
            Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE etudiants SET nom=?, prenom=?, note1=?, note2=? WHERE id=?");

            ps.setString(1, nomField.getText());
            ps.setString(2, prenomField.getText());
            ps.setDouble(3, Double.parseDouble(note1Field.getText()));
            ps.setDouble(4, Double.parseDouble(note2Field.getText()));
            ps.setInt(5, id);

            ps.executeUpdate();

            chargerEtudiants();
            viderChamps();

            JOptionPane.showMessageDialog(this, "Modification reussie !");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la modification !");
        }
    }

    private void supprimerEtudiant() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez une ligne !");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(r, 0).toString());

        try {
            Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM etudiants WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();

            chargerEtudiants();
            viderChamps();

            JOptionPane.showMessageDialog(this, "Etudiant supprimé !");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur de la suppression !");
        }
    }

    private void chargerIcone() {
        try {
            java.net.URL url = getClass().getResource("/logo.png");
            if (url != null) {
                setIconImage(new ImageIcon(url).getImage());
                return;
            }

            File f = new File("src/logo.png");
            if (f.exists()) {
                setIconImage(new ImageIcon(f.getAbsolutePath()).getImage());
                return;
            }

            System.out.println(" logo.png introuvable !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void styleField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void styleButton(JButton btn, Color c) {
        btn.setBackground(c);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    private void viderChamps() {
        nomField.setText("");
        prenomField.setText("");
        note1Field.setText("");
        note2Field.setText("");
    }

    public static void main(String[] args) {
        new GestionEtudiantsApp().setVisible(true);
    }
}

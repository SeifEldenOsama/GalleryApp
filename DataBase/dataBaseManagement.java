package DataBase;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;

public class dataBaseManagement {

    public static void insertImage(String imagePath, String imageName) {
        String sql = "INSERT INTO gallery_images (name, image_data) VALUES (?, ?)";

        try (
            Connection con = dataBaseConnection.connect();
            PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            FileInputStream fis = new FileInputStream(new File(imagePath));
        ) {
            pstmt.setString(1, imageName);
            pstmt.setBinaryStream(2, fis, fis.available());
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getLastInsertedId() {
        String sql = "SELECT LAST_INSERT_ID()";

        try (
            Connection con = dataBaseConnection.connect();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void deleteImageById(int id) {
        String sql = "DELETE FROM gallery_images WHERE id = ?";

        try (
            Connection conn = dataBaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getAllImages() {
        String sql = "SELECT id, name, image_data FROM gallery_images";

        try {
            Connection conn = dataBaseConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
} 

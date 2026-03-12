package pac.hy_da.ivokylinks;

import java.sql.*;
import java.util.UUID;

public class Database {
    private Connection connection;

    public Database(String path) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement st = connection.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS linked_players (uuid TEXT PRIMARY KEY, tg_id LONG, is_blocked INTEGER DEFAULT 0, rewarded INTEGER DEFAULT 0, last_ip TEXT)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void setBlocked(UUID uuid, boolean block) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE linked_players SET is_blocked = ? WHERE uuid = ?")) {
            ps.setInt(1, block ? 1 : 0);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean isBlocked(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT is_blocked FROM linked_players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt("is_blocked") == 1;
        } catch (SQLException e) { return false; }
    }

    // Остальные методы (linkPlayer, updateIp, getIp, getTelegramId, hasReceivedReward, setRewarded, getPlayerNameByTg, unlink) оставь без изменений, как были ранее.
    public void linkPlayer(UUID uuid, long tgId) { try (PreparedStatement ps = connection.prepareStatement("INSERT OR IGNORE INTO linked_players (uuid, tg_id) VALUES (?, ?)")) { ps.setString(1, uuid.toString()); ps.setLong(2, tgId); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); } }
    public void updateIp(UUID uuid, String ip) { try (PreparedStatement ps = connection.prepareStatement("UPDATE linked_players SET last_ip = ? WHERE uuid = ?")) { ps.setString(1, ip); ps.setString(2, uuid.toString()); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); } }
    public String getIp(UUID uuid) { try (PreparedStatement ps = connection.prepareStatement("SELECT last_ip FROM linked_players WHERE uuid = ?")) { ps.setString(1, uuid.toString()); ResultSet rs = ps.executeQuery(); return rs.next() ? rs.getString("last_ip") : null; } catch (SQLException e) { return null; } }
    public long getTelegramId(UUID uuid) { try (PreparedStatement ps = connection.prepareStatement("SELECT tg_id FROM linked_players WHERE uuid = ?")) { ps.setString(1, uuid.toString()); ResultSet rs = ps.executeQuery(); return rs.next() ? rs.getLong("tg_id") : -1; } catch (SQLException e) { return -1; } }
    public boolean hasReceivedReward(long tgId) { try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(rewarded) FROM linked_players WHERE tg_id = ?")) { ps.setLong(1, tgId); ResultSet rs = ps.executeQuery(); return rs.next() && rs.getInt(1) > 0; } catch (SQLException e) { return false; } }
    public void setRewarded(UUID uuid) { try (PreparedStatement ps = connection.prepareStatement("UPDATE linked_players SET rewarded = 1 WHERE uuid = ?")) { ps.setString(1, uuid.toString()); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); } }
    public String getPlayerNameByTg(long tgId) { try (PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM linked_players WHERE tg_id = ?")) { ps.setLong(1, tgId); ResultSet rs = ps.executeQuery(); return rs.next() ? rs.getString("uuid") : null; } catch (SQLException e) { return null; } }
    public void unlink(long tgId) { try (PreparedStatement ps = connection.prepareStatement("DELETE FROM linked_players WHERE tg_id = ?")) { ps.setLong(1, tgId); ps.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); } }
    public void close() { try { if (connection != null) connection.close(); } catch (SQLException e) { e.printStackTrace(); } }
}
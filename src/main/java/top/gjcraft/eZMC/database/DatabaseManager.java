package top.gjcraft.eZMC.database;

import java.sql.*;
import java.util.logging.Logger;

public class DatabaseManager {
    private final String dbPath;
    private final Logger logger;
    private Connection connection;

    public DatabaseManager(String dataFolder, Logger logger) {
        this.dbPath = "jdbc:sqlite:" + dataFolder + "/ezmc.db";
        this.logger = logger;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.SQLITE_JDBC");
            connection = DriverManager.getConnection(dbPath);
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            logger.severe("初始化数据库失败: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 创建玩家状态表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_states (
                    uuid TEXT PRIMARY KEY,
                    max_health DOUBLE,
                    current_health DOUBLE,
                    is_downed BOOLEAN,
                    downed_time BIGINT
                )
            """);

            // 创建世界状态表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS world_states (
                    world_name TEXT PRIMARY KEY,
                    is_eternal_night BOOLEAN,
                    is_doom_night BOOLEAN,
                    doom_night_start_time BIGINT
                )
            """);

            // 创建事件状态表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS event_states (
                    event_name TEXT PRIMARY KEY,
                    last_trigger_time BIGINT,
                    is_active BOOLEAN
                )
            """);
        }
    }

    public void savePlayerState(String uuid, double maxHealth, double currentHealth, boolean isDowned, long downedTime) {
        String sql = "INSERT OR REPLACE INTO player_states (uuid, max_health, current_health, is_downed, downed_time) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setDouble(2, maxHealth);
            pstmt.setDouble(3, currentHealth);
            pstmt.setBoolean(4, isDowned);
            pstmt.setLong(5, downedTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("保存玩家状态失败: " + e.getMessage());
        }
    }

    public void saveWorldState(String worldName, boolean isEternalNight, boolean isDoomNight, long doomNightStartTime) {
        String sql = "INSERT OR REPLACE INTO world_states (world_name, is_eternal_night, is_doom_night, doom_night_start_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, worldName);
            pstmt.setBoolean(2, isEternalNight);
            pstmt.setBoolean(3, isDoomNight);
            pstmt.setLong(4, doomNightStartTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("保存世界状态失败: " + e.getMessage());
        }
    }

    public void saveEventState(String eventName, long lastTriggerTime, boolean isActive) {
        String sql = "INSERT OR REPLACE INTO event_states (event_name, last_trigger_time, is_active) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eventName);
            pstmt.setLong(2, lastTriggerTime);
            pstmt.setBoolean(3, isActive);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("保存事件状态失败: " + e.getMessage());
        }
    }

    public ResultSet getPlayerState(String uuid) {
        String sql = "SELECT * FROM player_states WHERE uuid = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, uuid);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            logger.severe("获取玩家状态失败: " + e.getMessage());
            return null;
        }
    }

    public ResultSet getWorldState(String worldName) {
        String sql = "SELECT * FROM world_states WHERE world_name = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, worldName);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            logger.severe("获取世界状态失败: " + e.getMessage());
            return null;
        }
    }

    public ResultSet getEventState(String eventName) {
        String sql = "SELECT * FROM event_states WHERE event_name = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, eventName);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            logger.severe("获取事件状态失败: " + e.getMessage());
            return null;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.severe("关闭数据库连接失败: " + e.getMessage());
        }
    }
}
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

public class Main {
    private static String sftp_host;
    private static int sftp_port;
    private static String sftp_user;
    private static String sftp_password;
    private static String sftp_remote_dir;
    private static String local_dir;
    private static String sql_user;
    private static String sql_password;
    private static String sql_database;

    private static void getSettings(String settingsFile) {

        if (!new File(settingsFile).exists()) {
            System.out.println("Settings file not found.");
            System.exit(0);
        }

        List<String> settings = null;
        try {
            settings = Files.readAllLines(Paths.get(settingsFile), Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("File reading error.");
            e.printStackTrace();
        }

        if (settings != null) {
            settings.set(0, settings.get(0).substring(settings.get(0).indexOf("=") + 1));
            settings.set(1, settings.get(1).substring(settings.get(1).indexOf("=") + 1));
            settings.set(2, settings.get(2).substring(settings.get(2).indexOf("=") + 1));
            settings.set(3, settings.get(3).substring(settings.get(3).indexOf("=") + 1));
            settings.set(4, settings.get(4).substring(settings.get(4).indexOf("=") + 1));
            settings.set(5, settings.get(5).substring(settings.get(5).indexOf("=") + 1));
            settings.set(6, settings.get(6).substring(settings.get(6).indexOf("=") + 1));
            settings.set(7, settings.get(7).substring(settings.get(7).indexOf("=") + 1));
            settings.set(8, settings.get(8).substring(settings.get(8).indexOf("=") + 1));

            sftp_host = settings.get(0);
            sftp_port = Integer.valueOf(settings.get(1));
            sftp_user = settings.get(2);
            sftp_password = settings.get(3);
            sftp_remote_dir = settings.get(4);
            local_dir = settings.get(5);
            sql_user = settings.get(6);
            sql_password = settings.get(7);
            sql_database = settings.get(8);

            settings.clear();
        } else {
            System.out.println("Settings file is empty.");
        }
    }

    private static Session session = null;
    private static Channel channel = null;
    private static ChannelSftp sftpChannel = null;

    private static void sftp() throws ClassNotFoundException, SQLException {
        try {
            session = new JSch().getSession(sftp_user, sftp_host, sftp_port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(sftp_password);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();

            sftpChannel = (ChannelSftp) channel;

            String url = "jdbc:mysql://localhost:3306/"+sql_database;
            Class.forName("com.mysql.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(url, sql_user, sql_password)) {
                Statement sta = conn.createStatement();

                Vector<LsEntry> listFile = sftpChannel.ls(sftp_remote_dir);
                if (listFile != null) {
                    for (LsEntry lsEntry : listFile) {
                        String fileName = lsEntry.getFilename();
                        sftpChannel.get(sftp_remote_dir + fileName, local_dir + fileName);
                        sta.execute("INSERT INTO logs (fileName) VALUES ('" + fileName + "')");
                    }
                }
                conn.close();
            }
        } catch (SftpException | JSchException e) {
            e.printStackTrace();
        } finally {
            session.disconnect();
            channel.disconnect();
            sftpChannel.disconnect();
        }
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        if (args.length == 0) {
            System.out.println("Settings file not specified.");
            System.exit(0);
        }

        getSettings(args[0]);
        sftp();

    }
}

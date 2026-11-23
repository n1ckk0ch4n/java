package out;

import org.postgresql.ds.PGSimpleDataSource;
import controller.CmdController;
import exception.TechnicalException;
import repository.ArchiveDatabaseRepository;
import repository.ArchiveRepository;
import repository.FileDatabaseRepository;
import repository.FileRepository;
import service.ArchiveService;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        DataSource dataSource = connectDB();
        initDB(dataSource);

        ArchiveRepository archiveRepo = new ArchiveDatabaseRepository(dataSource);
        FileRepository fileRepo = new FileDatabaseRepository(dataSource);

        ArchiveService service = new ArchiveService(archiveRepo, fileRepo);
        Scanner scanner = new Scanner(System.in);
        CmdController controller = new CmdController(scanner, service);
        controller.run();
    }

    public static DataSource connectDB() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setPortNumbers(new int[]{5432});
        dataSource.setDatabaseName("archive");
        dataSource.setUser("archive");
        dataSource.setPassword("archive");
        return dataSource;
    }

    public static void initDB(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            var sql = """
                    CREATE TABLE IF NOT EXISTS archive (
                        id UUID PRIMARY KEY,
                        name TEXT NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    );

                    CREATE TABLE IF NOT EXISTS file (
                        id UUID PRIMARY KEY,
                        archive_id UUID NOT NULL REFERENCES archive(id) ON DELETE CASCADE,
                        name TEXT NOT NULL,
                        content TEXT,
                        created_at TIMESTAMP NOT NULL
                    );
                    """;
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new TechnicalException("Ошибка инициализации БД", e);
        }
    }
}
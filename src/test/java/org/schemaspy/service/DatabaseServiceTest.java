package org.schemaspy.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

public class DatabaseServiceTest {

    private static final ProgressListener progressListener = mock(ProgressListener.class);

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    private Instant currentTime = Instant.now();
    private Clock clock = mock(Clock.class);

    @Before
    public void setup() {
        when(clock.instant()).thenAnswer(invocation -> currentTime);
    }

    @Test
    @Logger(DatabaseService.class)
    public void databaseServicePrintsInformationWhenConnectionTablesWillTakeMoreThan30MinutesAndExportedKeysIsEnabled() throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Config config = new Config("-something","thisThing");
        TableService tableService = mock(TableService.class);
        doAnswer(invocation -> {
            currentTime = currentTime.plus(31, ChronoUnit.MINUTES);
            return null;
        }).when(tableService).connectForeignKeys(any(),any(),anyMap());
        ViewService viewService = mock(ViewService.class);
        SqlService sqlService = mock(SqlService.class);
        DatabaseService databaseService = new DatabaseService(clock, tableService, viewService, sqlService);
        List<Table> tablesList = new ArrayList<>();
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        Database database = mock(Database.class);
        when(database.getTables()).thenReturn(tablesList);
        Method connectTables = DatabaseService.class.getDeclaredMethod("connectTables", Database.class, ProgressListener.class);
        connectTables.setAccessible(true);

        connectTables.invoke(databaseService, database, progressListener);

        assertThat(loggingRule.getLog()).contains("Estimated time remaining");
    }

    @Test
    @Logger(DatabaseService.class)
    public void databaseServiceDoesNotPrintInformationWhenConnectionTablesWillTakeMoreThan30MinutesAndExportedKeysIsDisabled() throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Config config = new Config("-noexportedkeys");
        TableService tableService = mock(TableService.class);
        doAnswer(invocation -> {
            currentTime = currentTime.plus(31, ChronoUnit.MINUTES);
            return null;
        }).when(tableService).connectForeignKeys(any(),any(),anyMap());
        ViewService viewService = mock(ViewService.class);
        SqlService sqlService = mock(SqlService.class);
        DatabaseService databaseService = new DatabaseService(clock, tableService, viewService, sqlService);
        List<Table> tablesList = new ArrayList<>();
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        Database database = mock(Database.class);
        when(database.getTables()).thenReturn(tablesList);
        Method connectTables = DatabaseService.class.getDeclaredMethod("connectTables", Database.class, ProgressListener.class);
        connectTables.setAccessible(true);

        connectTables.invoke(databaseService, database, progressListener);

        assertThat(loggingRule.getLog()).doesNotContain("Estimated time remaining");
    }

    @Test
    @Logger(DatabaseService.class)
    public void databaseServiceDoesNotPrintInformationWhenConnectionTablesWillTakeLessThan30Minutes() throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Config config = new Config("-something","thisThing");
        TableService tableService = mock(TableService.class);
        doAnswer(invocation -> {
            currentTime = currentTime.plus(1, ChronoUnit.MINUTES);
            return null;
        }).when(tableService).connectForeignKeys(any(),any(),anyMap());
        ViewService viewService = mock(ViewService.class);
        SqlService sqlService = mock(SqlService.class);
        DatabaseService databaseService = new DatabaseService(clock, tableService, viewService, sqlService);
        List<Table> tablesList = new ArrayList<>();
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        Database database = mock(Database.class);
        when(database.getTables()).thenReturn(tablesList);
        Method connectTables = DatabaseService.class.getDeclaredMethod("connectTables", Database.class, ProgressListener.class);
        connectTables.setAccessible(true);

        connectTables.invoke(databaseService, database, progressListener);

        assertThat(loggingRule.getLog()).doesNotContain("Estimated time remaining");
    }

}
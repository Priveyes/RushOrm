package co.uk.rushorm.core;

import java.lang.reflect.*;
import java.util.*;

import co.uk.rushorm.core.exceptions.*;
import co.uk.rushorm.core.implementation.Insert.*;
import co.uk.rushorm.core.implementation.*;

/**
 * Created by Stuart on 10/12/14.
 */
public class RushCore {

    /**
     * RushInitializeConfig replaces this making it far easier to customise any class
     * without writing boiler plate code for the other classes
     *
     * @deprecated use {@link #initialize(RushInitializeConfig rushInitializeConfig)} instead.
     */
    @Deprecated
    public static void initialize(RushClassFinder rushClassFinder, RushStatementRunner statementRunner, RushQueueProvider queProvider, RushConfig rushConfig, RushStringSanitizer rushStringSanitizer, Logger logger, List<RushColumn> columns, RushObjectSerializer rushObjectSerializer, RushObjectDeserializer rushObjectDeserializer) {

//        if(rushConfig.usingMySql()) {
//            columns.add(new RushColumnBooleanNumerical());
//        }else {
            columns.add(new RushColumnBoolean());
//        }
        
        columns.add(new RushColumnDate());
        columns.add(new RushColumnDouble());
        columns.add(new RushColumnInt());
        columns.add(new RushColumnLong());
        columns.add(new RushColumnShort());
        columns.add(new RushColumnFloat());
        columns.add(new RushColumnString());

        RushColumns rushColumns = new RushColumnsImplementation(columns);

        RushUpgradeManager rushUpgradeManager = new ReflectionUpgradeManager(logger, rushConfig);
        RushSqlInsertGenerator rushSqlInsertGenerator = rushConfig.userBulkInsert() ? new SqlBulkInsertGenerator(rushConfig) : new SqlSingleInsertGenerator(rushConfig);

        RushSaveStatementGenerator saveStatementGenerator = new ReflectionSaveStatementGenerator(rushSqlInsertGenerator, rushConfig);
        RushConflictSaveStatementGenerator conflictSaveStatementGenerator = new ConflictSaveStatementGenerator(rushSqlInsertGenerator, rushConfig);
        RushDeleteStatementGenerator deleteStatementGenerator = new ReflectionDeleteStatementGenerator(rushConfig);
        RushJoinStatementGenerator rushJoinStatementGenerator = new ReflectionJoinStatementGenerator();
        RushTableStatementGenerator rushTableStatementGenerator = new ReflectionTableStatementGenerator(rushConfig);
        RushClassLoader rushClassLoader = new ReflectionClassLoader(rushConfig);

        initialize(rushUpgradeManager, saveStatementGenerator, conflictSaveStatementGenerator, deleteStatementGenerator, rushJoinStatementGenerator, rushClassFinder, rushTableStatementGenerator, statementRunner, queProvider, rushConfig, rushClassLoader, rushStringSanitizer, logger, rushObjectSerializer, rushObjectDeserializer, rushColumns, null);
    }

    public static void initialize(RushInitializeConfig rushInitializeConfig) {
        initialize(rushInitializeConfig.getRushUpgradeManager(),
                rushInitializeConfig.getSaveStatementGenerator(),
                rushInitializeConfig.getRushConflictSaveStatementGenerator(),
                rushInitializeConfig.getRushDeleteStatementGenerator(),
                rushInitializeConfig.getRushJoinStatementGenerator(),
                rushInitializeConfig.getRushClassFinder(),
                rushInitializeConfig.getRushTableStatementGenerator(),
                rushInitializeConfig.getRushStatementRunner(),
                rushInitializeConfig.getRushQueueProvider(),
                rushInitializeConfig.getRushConfig(),
                rushInitializeConfig.getRushClassLoader(),
                rushInitializeConfig.getRushStringSanitizer(),
                rushInitializeConfig.getRushLogger(),
                rushInitializeConfig.getRushObjectSerializer(),
                rushInitializeConfig.getRushObjectDeserializer(),
                rushInitializeConfig.getRushColumns(),
                rushInitializeConfig.getInitializeListener());
    }

    public static void initialize(final RushUpgradeManager rushUpgradeManager,
                                  RushSaveStatementGenerator saveStatementGenerator,
                                  RushConflictSaveStatementGenerator rushConflictSaveStatementGenerator,
                                  RushDeleteStatementGenerator deleteStatementGenerator,
                                  RushJoinStatementGenerator rushJoinStatementGenerator,
                                  RushClassFinder rushClassFinder,
                                  RushTableStatementGenerator rushTableStatementGenerator,
                                  final RushStatementRunner statementRunner,
                                  final RushQueueProvider queProvider,
                                  final RushConfig rushConfig,
                                  RushClassLoader rushClassLoader,
                                  RushStringSanitizer rushStringSanitizer,
                                  Logger logger,
                                  RushObjectSerializer rushObjectSerializer,
                                  RushObjectDeserializer rushObjectDeserializer,
                                  RushColumns rushColumns,
                                  final InitializeListener initializeListener) {

        if(rushCore != null) {
            logger.logError("RushCore has already been initialized, make sure initialize is only called once.");
        }

        rushCore = new RushCore(saveStatementGenerator, rushConflictSaveStatementGenerator, deleteStatementGenerator, rushJoinStatementGenerator, statementRunner, queProvider, rushConfig, rushTableStatementGenerator, rushClassLoader, rushStringSanitizer, logger, rushObjectSerializer, rushObjectDeserializer, rushColumns);
        rushCore.loadAnnotationCache(rushClassFinder);

        final boolean isFirstRun = statementRunner.isFirstRun();
        final RushQueue rushQueue = queProvider.blockForNextQueue();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFirstRun) {
                    rushCore.createTables(new ArrayList<>(rushCore.annotationCache.keySet()), rushQueue);
                } else if(rushConfig.inDebug() || statementRunner.requiresUpgrade(rushConfig.dbVersion(), rushQueue)){
                    rushCore.upgrade(new ArrayList<>(rushCore.annotationCache.keySet()), rushUpgradeManager, rushQueue);
                } else {
                    queProvider.queComplete(rushQueue);
                }
                statementRunner.initializeComplete(rushConfig.dbVersion());
                if(initializeListener != null) {
                    initializeListener.initialized(isFirstRun);
                }
            }
        }).start();
    }

    public static RushCore getInstance() {
        if (rushCore == null) {
            throw new RushCoreNotInitializedException();
        }
        return rushCore;
    }

    public void clearDatabase(){
        for (Map.Entry<Class<? extends Rush>, AnnotationCache> entry : annotationCache.entrySet()) {
            deleteAll(entry.getKey());
        }
    }

    public RushMetaData getMetaData(Rush rush) {
        return idTable.get(rush);
    }
    
    public String getId(Rush rush) {
        RushMetaData rushMetaData = getMetaData(rush);
        if (rushMetaData == null) {
            return null;
        }
        return rushMetaData.getId();
    }

    public void save(Rush rush) {
        List<Rush> objects = new ArrayList<>();
        objects.add(rush);
        save(objects);
    }

    public void save(List<? extends Rush> objects) {
        RushQueue que = rushQueueProvider.blockForNextQueue();
        save(objects, que);
    }

    public void save(final Rush rush, final RushCallback callback) {
        List<Rush> objects = new ArrayList<>();
        objects.add(rush);
        save(objects, callback);
    }

    public void save(final List<? extends Rush> objects, final RushCallback callback) {
        rushQueueProvider.waitForNextQue(new RushQueueProvider.RushQueCallback() {
            @Override
            public void callback(RushQueue rushQueue) {
                save(objects, rushQueue);
                if (callback != null) {
                    callback.complete();
                }
            }
        });
    }

    public void join(List<RushJoin> objects) {
        RushQueue que = rushQueueProvider.blockForNextQueue();
        join(objects, que);
    }

    public void join(final List<RushJoin> objects, final RushCallback callback) {
        rushQueueProvider.waitForNextQue(new RushQueueProvider.RushQueCallback() {
            @Override
            public void callback(RushQueue rushQueue) {
                join(objects, rushQueue);
                if (callback != null) {
                    callback.complete();
                }
            }
        });
    }

    public void deleteJoin(List<RushJoin> objects) {
        RushQueue que = rushQueueProvider.blockForNextQueue();
        deleteJoin(objects, que);
    }

    public void deleteJoin(final List<RushJoin> objects, final RushCallback callback) {
        rushQueueProvider.waitForNextQue(new RushQueueProvider.RushQueCallback() {
            @Override
            public void callback(RushQueue rushQueue) {
                deleteJoin(objects, rushQueue);
                if (callback != null) {
                    callback.complete();
                }
            }
        });
    }

    public void clearChildren(Class<? extends Rush> parent, String field, Class<? extends Rush> child, String id) {
        final RushQueue que = rushQueueProvider.blockForNextQueue();
        rushJoinStatementGenerator.deleteAll(parent, field, child, id, new RushJoinStatementGenerator.Callback() {
            @Override
            public void runSql(String sql) {
                logger.logSql(sql);
                statementRunner.runRaw(sql, que);
            }
        }, annotationCache);
        rushQueueProvider.queComplete(que);
    }

    public long count(String sql) {
        final RushQueue que = rushQueueProvider.blockForNextQueue();
        logger.logSql(sql);
        RushStatementRunner.ValuesCallback valuesCallback = statementRunner.runGet(sql, que);
        List<String> results = valuesCallback.next();
        long count = Long.parseLong(results.get(0));
        valuesCallback.close();
        rushQueueProvider.queComplete(que);
        return count;
    }

    public <T extends Rush> List<T> load(Class<T> clazz, String sql) {
        RushQueue que = rushQueueProvider.blockForNextQueue();
        return load(clazz, sql, que);
    }

    public <T extends Rush> void load(final Class<T> clazz, final String sql, final RushSearchCallback<T> callback) {
        rushQueueProvider.waitForNextQue(new RushQueueProvider.RushQueCallback() {
            @Override
            public void callback(RushQueue rushQueue) {
                callback.complete(load(clazz, sql, rushQueue));
            }
        });
    }

    public void delete(Rush rush) {
        List<Rush> objects = new ArrayList<>();
        objects.add(rush);
        delete(objects);
    }

    public void delete(List<? extends Rush> objects) {
        RushQueue que = rushQueueProvider.blockForNextQueue();
        delete(objects, que);
    }

    public void delete(final Rush rush, final RushCallback callback) {
        List<Rush> objects = new ArrayList<>();
        objects.add(rush);
        delete(objects, callback);
    }

    public void delete(final List<? extends Rush> objects, final RushCallback callback) {
        rushQueueProvider.waitForNextQue(new RushQueueProvider.RushQueCallback() {
            @Override
            public void callback(RushQueue rushQueue) {
                delete(objects, rushQueue);
                if (callback != null) {
                    callback.complete();
                }
            }
        });
    }

    public void deleteAll(Class<? extends Rush> clazz) {
        final RushQueue que = rushQueueProvider.blockForNextQueue();
        deleteAll(clazz, que);
    }

    public void deleteAll(final Class<? extends Rush> clazz, final RushCallback callback) {

        rushQueueProvider.waitForNextQue(new RushQueueProvider.RushQueCallback() {
            @Override
            public void callback(RushQueue rushQueue) {
                deleteAll(clazz, rushQueue);
                if (callback != null) {
                    callback.complete();
                }
            }
        });
    }

    public List<RushConflict> saveOnlyWithoutConflict(Rush rush) {
        List<Rush> objects = new ArrayList<>();
        objects.add(rush);
        return saveOnlyWithoutConflict(objects);
    }

    public List<RushConflict> saveOnlyWithoutConflict(List<? extends Rush> objects) {
        RushQueue que = rushQueueProvider.blockForNextQueue();
        return saveOnlyWithoutConflict(objects, que);
    }

    public void saveOnlyWithoutConflict(final Rush rush, final RushConflictCallback callback) {
        List<Rush> objects = new ArrayList<>();
        objects.add(rush);
        saveOnlyWithoutConflict(objects, callback);
    }

    public void saveOnlyWithoutConflict(final List<? extends Rush> objects, final RushConflictCallback callback) {
        rushQueueProvider.waitForNextQue(new RushQueueProvider.RushQueCallback() {
            @Override
            public void callback(RushQueue rushQueue) {
                List<RushConflict> conflicts = saveOnlyWithoutConflict(objects, rushQueue);
                if (callback != null) {
                    callback.complete(conflicts);
                }
            }
        });
    }

    public String serialize(List<? extends Rush> rush) {
        return serialize(rush, RushSqlUtils.RUSH_ID);
    }

    public String serialize(List<? extends Rush> rush, String idName) {
        return serialize(rush, idName, RushSqlUtils.RUSH_VERSION);
    }

    public String serialize(List<? extends Rush> rush, String idName, String versionName) {
        return rushObjectSerializer.serialize(rush, idName, versionName, rushColumns, annotationCache, new RushObjectSerializer.Callback() {
            @Override
            public RushMetaData getMetaData(Rush rush) {
                return idTable.get(rush);
            }
        });
    }

    public List<Rush> deserialize(String string) {
        return deserialize(string, RushSqlUtils.RUSH_ID);
    }

    public List<Rush> deserialize(String string, String idName) {
        return deserialize(string, idName, RushSqlUtils.RUSH_VERSION);
    }

    public List<Rush> deserialize(String string, String idName, String versionName) {
        return deserialize(string, idName, versionName, Rush.class);
    }

    public <T extends Rush> List<T> deserialize(String string,  Class<T> clazz) {
        return deserialize(string, RushSqlUtils.RUSH_ID, clazz);
    }

    public <T extends Rush> List<T> deserialize(String string, String idName, Class<T> clazz) {
        return deserialize(string, idName, RushSqlUtils.RUSH_VERSION, clazz);
    }

    public <T extends Rush> List<T> deserialize(String string, String idName, String versionName, Class<T> clazz) {
        return rushObjectDeserializer.deserialize(string, idName, versionName, rushColumns, annotationCache, clazz, new RushObjectDeserializer.Callback() {
            @Override
            public void addRush(Rush rush, RushMetaData rushMetaData) {
                idTable.put(rush, rushMetaData);
            }
        });
    }

    public void registerObjectWithId(Rush rush, String id) {
        RushMetaData rushMetaData = new RushMetaData(id, 0);
        registerObjectWithMetaData(rush, rushMetaData);
    }

    public void registerObjectWithMetaData(Rush rush, RushMetaData rushMetaData) {
        idTable.put(rush, rushMetaData);
    }

    public Map<Class<? extends Rush>, AnnotationCache> getAnnotationCache() {
        return annotationCache;
    }

    /* protected */
    protected String sanitize(String string) {
        return rushStringSanitizer.sanitize(string);
    }

    /* private */
    private static RushCore rushCore;
    private final Map<Rush, RushMetaData> idTable = new WeakHashMap<>();

    private final RushSaveStatementGenerator saveStatementGenerator;
    private final RushConflictSaveStatementGenerator rushConflictSaveStatementGenerator;
    private final RushDeleteStatementGenerator deleteStatementGenerator;
    private final RushJoinStatementGenerator rushJoinStatementGenerator;
    private final RushStatementRunner statementRunner;
    private final RushQueueProvider rushQueueProvider;
    private final RushConfig rushConfig;
    private final RushTableStatementGenerator rushTableStatementGenerator;
    private final RushClassLoader rushClassLoader;
    private final Logger logger;
    private final RushStringSanitizer rushStringSanitizer;
    private final RushObjectSerializer rushObjectSerializer;
    private final RushObjectDeserializer rushObjectDeserializer;
    private final RushColumns rushColumns;
    private final Map<Class<? extends Rush>, AnnotationCache> annotationCache = new HashMap<>();


    private RushCore(RushSaveStatementGenerator saveStatementGenerator,
                     RushConflictSaveStatementGenerator rushConflictSaveStatementGenerator,
                     RushDeleteStatementGenerator deleteStatementGenerator,
                     RushJoinStatementGenerator rushJoinStatementGenerator, RushStatementRunner statementRunner,
                     RushQueueProvider queProvider,
                     RushConfig rushConfig,
                     RushTableStatementGenerator rushTableStatementGenerator,
                     RushClassLoader rushClassLoader,
                     RushStringSanitizer rushStringSanitizer,
                     Logger logger,
                     RushObjectSerializer rushObjectSerializer,
                     RushObjectDeserializer rushObjectDeserializer,
                     RushColumns rushColumns) {

        this.saveStatementGenerator = saveStatementGenerator;
        this.rushConflictSaveStatementGenerator = rushConflictSaveStatementGenerator;
        this.deleteStatementGenerator = deleteStatementGenerator;
        this.rushJoinStatementGenerator = rushJoinStatementGenerator;
        this.statementRunner = statementRunner;
        this.rushQueueProvider = queProvider;
        this.rushConfig = rushConfig;
        this.rushTableStatementGenerator = rushTableStatementGenerator;
        this.rushClassLoader = rushClassLoader;
        this.rushStringSanitizer = rushStringSanitizer;
        this.logger = logger;
        this.rushObjectSerializer = rushObjectSerializer;
        this.rushObjectDeserializer = rushObjectDeserializer;
        this.rushColumns = rushColumns;
    }
    
    private void loadAnnotationCache(RushClassFinder rushClassFinder) {
        for(Class<? extends Rush> clazz : rushClassFinder.findClasses(rushConfig)) {
            List<Field> fields = new ArrayList<>();
            ReflectionUtils.getAllFields(fields, clazz, rushConfig.orderColumnsAlphabetically());
            annotationCache.put(clazz, new RushAnnotationCache(clazz, fields, rushConfig));
        }       
    }

    private void createTables(List<Class<? extends Rush>> classes, final RushQueue rushQueue) {
        rushTableStatementGenerator.generateStatements(classes, rushColumns, new RushTableStatementGenerator.StatementCallback() {
            @Override
            public void statementCreated(String statement) {
                logger.logSql(statement);
                statementRunner.runRaw(statement, rushQueue);
            }
        }, annotationCache);
        rushQueueProvider.queComplete(rushQueue);
    }

    private void upgrade(List<Class<? extends Rush>> classes, RushUpgradeManager rushUpgradeManager, final RushQueue rushQueue) {
        rushUpgradeManager.upgrade(classes, new RushUpgradeManager.UpgradeCallback() {
            @Override
            public RushStatementRunner.ValuesCallback runStatement(String sql) {
                logger.logSql(sql);
                return statementRunner.runGet(sql, rushQueue);
            }

            @Override
            public void runRaw(String sql) {
                logger.logSql(sql);
                statementRunner.runRaw(sql, rushQueue);
            }

            @Override
            public void createClasses(List<Class<? extends Rush>> missingClasses) {
                createTables(missingClasses, rushQueue);
            }
        }, annotationCache);
        rushQueueProvider.queComplete(rushQueue);
    }

    private static final int SAVE_GROUP_SIZE = 1000;
    private void save(List<? extends Rush> objects, final RushQueue rushQueue) {
        for (int i = 0; i < Math.ceil(objects.size() / ((float)SAVE_GROUP_SIZE)); i ++) {

            int start = i * SAVE_GROUP_SIZE;
            int end = Math.min(objects.size(), start + SAVE_GROUP_SIZE);
            List<? extends Rush> group = objects.subList(start, end);

            statementRunner.startTransition(rushQueue);
            saveStatementGenerator.generateSaveOrUpdate(group, annotationCache, rushStringSanitizer, rushColumns, new RushSaveStatementGeneratorCallback() {
                @Override
                public void addRush(Rush rush, RushMetaData rushMetaData) {
                    registerObjectWithMetaData(rush, rushMetaData);
                }

                @Override
                public void createdOrUpdateStatement(String sql) {
                    logger.logSql(sql);
                    statementRunner.runRaw(sql, rushQueue);
                }

                @Override
                public void deleteStatement(String sql) {
                    logger.logSql(sql);
                    statementRunner.runRaw(sql, rushQueue);
                }

                @Override
                public RushMetaData getMetaData(Rush rush) {
                    return idTable.get(rush);
                }
            });
            statementRunner.endTransition(rushQueue);
        }
        rushQueueProvider.queComplete(rushQueue);
    }

    private List<RushConflict> saveOnlyWithoutConflict(List<? extends Rush> objects, final RushQueue rushQueue) {
        final List<RushConflict> conflicts = new ArrayList<>();
        statementRunner.startTransition(rushQueue);
        rushConflictSaveStatementGenerator.conflictsFromGenerateSaveOrUpdate(objects, annotationCache, rushStringSanitizer, rushColumns, new RushConflictSaveStatementGenerator.Callback() {
            @Override
            public void conflictFound(RushConflict conflict) {
                conflicts.add(conflict);
            }

            @Override
            public <T extends Rush> T load(Class T, String sql) {
                List<T> objects = RushCore.this.load(T, sql, rushQueue);
                return objects.size() > 0 ? objects.get(0) : null;
            }

            @Override
            public void addRush(Rush rush, RushMetaData rushMetaData) {
                registerObjectWithMetaData(rush, rushMetaData);
            }

            @Override
            public void createdOrUpdateStatement(String sql) {
                logger.logSql(sql);
                statementRunner.runRaw(sql, rushQueue);
            }

            @Override
            public void deleteStatement(String sql) {
                logger.logSql(sql);
                statementRunner.runRaw(sql, rushQueue);
            }

            @Override
            public RushMetaData getMetaData(Rush rush) {
                return idTable.get(rush);
            }
        });
        statementRunner.endTransition(rushQueue);
        rushQueueProvider.queComplete(rushQueue);
        return conflicts;
    }

    private void delete(List<? extends Rush> objects, final RushQueue rushQueue) {
        statementRunner.startTransition(rushQueue);
        deleteStatementGenerator.generateDelete(objects, annotationCache, new RushDeleteStatementGenerator.Callback() {

            @Override
            public void removeRush(Rush rush) {
                RushCore.this.removeRush(rush);
            }

            @Override
            public void deleteStatement(String sql) {
                logger.logSql(sql);
                statementRunner.runRaw(sql, rushQueue);
            }

            @Override
            public RushMetaData getMetaData(Rush rush) {
                return idTable.get(rush);
            }
        });
        statementRunner.endTransition(rushQueue);
        rushQueueProvider.queComplete(rushQueue);
    }

    private void deleteAll(Class<? extends Rush> clazz, final RushQueue rushQueue) {
        deleteStatementGenerator.generateDeleteAll(clazz, annotationCache, new RushDeleteStatementGenerator.Callback() {
            @Override
            public void removeRush(Rush rush) { }
            @Override
            public void deleteStatement(String sql) {
                logger.logSql(sql);
                statementRunner.runRaw(sql, rushQueue);
            }

            @Override
            public RushMetaData getMetaData(Rush rush) {
                return null;
            }
        });
        rushQueueProvider.queComplete(rushQueue);
    }

    private <T extends Rush> List<T> load(Class<T> clazz, String sql, final RushQueue rushQueue) {
        logger.logSql(sql);
        RushStatementRunner.ValuesCallback values = statementRunner.runGet(sql, rushQueue);
        List<T> objects = rushClassLoader.loadClasses(clazz, rushColumns, annotationCache, values, new RushClassLoader.LoadCallback() {
            @Override
            public RushStatementRunner.ValuesCallback runStatement(String string) {
                logger.logSql(string);
                return statementRunner.runGet(string, rushQueue);
            }

            @Override
            public void didLoadObject(Rush rush, RushMetaData rushMetaData) {
                registerObjectWithMetaData(rush, rushMetaData);
            }
        });
        values.close();
        rushQueueProvider.queComplete(rushQueue);
        if(objects == null) {
            throw new RushTableMissingEmptyConstructorException(clazz);
        }
        return objects;
    }

    private void removeRush(Rush rush) {
        idTable.remove(rush);
    }

    private void join(List<RushJoin> objects, final RushQueue rushQueue) {
        statementRunner.startTransition(rushQueue);
        rushJoinStatementGenerator.createJoins(objects, new RushJoinStatementGenerator.Callback() {
            @Override
            public void runSql(String sql) {
                logger.logSql(sql);
                statementRunner.runRaw(sql, rushQueue);
            }
        }, annotationCache);
        statementRunner.endTransition(rushQueue);
        rushQueueProvider.queComplete(rushQueue);
    }

    private void deleteJoin(List<RushJoin> objects, final RushQueue rushQueue) {
        statementRunner.startTransition(rushQueue);
        rushJoinStatementGenerator.deleteJoins(objects, new RushJoinStatementGenerator.Callback() {
            @Override
            public void runSql(String sql) {
                logger.logSql(sql);
                statementRunner.runRaw(sql, rushQueue);
            }
        }, annotationCache);
        statementRunner.endTransition(rushQueue);
        rushQueueProvider.queComplete(rushQueue);
    }
}

/*
 * Copyright 2017 Redlink GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
var smarti = db.getCollection('smarti'),
    versionFrom = 3, versionTo = 4;

while (true) {
    var dbVersion = smarti.findOne({_id: 'db-version'});
    if (dbVersion === null) {
        print('No db-version found, are you running the migration scripts in the right order?');
        break;
    } else if (dbVersion.isTainted) {
        print('Refuse to upgrade tainted database!');
        break;
    } else if (dbVersion.isUpgrading) {
        print('Upgrade in progress, sleeping for 250ms');
        sleep(250);
        continue;
    } else if (dbVersion.version !== versionFrom) {
        if (dbVersion.version > versionFrom) {
            print('Database already migrated, Skipping');
        } else {
            print('Can only migrate database.version ' + versionFrom + ', found ' + dbVersion.version);
        }
        break;
    }
    var lock = smarti.update({_id: 'db-version', version: versionFrom, isUpgrading: false, isTainted: false}, {
        $set: { isUpgrading: true }
    }, { upsert: true });
    if (lock.nModified !== 1) {
        print('Could not acquire lock, trying again.');
        continue;
    }
    // Lock acquired
    var start = new ISODate();

    // Backup the database.
    dbVersion = smarti.findOne({_id: 'db-version'});
    if (!dbVersion.backup) {
        var now = new Date(),
            backupDB = db.getName() + '_' + now.getTime();
        db.copyDatabase(db.getName(), backupDB);
        print('Created Backup ' + backupDB + ' before starting migration');
        db.getSiblingDB(backupDB)
            .getCollection('smarti')
            .update({_id: 'db-version'}, { $set: { isUpgrading: false }});
        smarti.update({_id: 'db-version'}, {
            $set: {
                backup: {
                    name: backupDB,
                    version: dbVersion.version,
                    date: now
                }
            }
        });
    } else {
        print('Backup already exists: ' + dbVersion.backup.name + ' (' + dbVersion.backup.date + ')');
    }


    try {
        print('Starting Migration ' + versionFrom + ' --> ' + versionTo);
        var result = runDatabaseMigration();
        print('Completed Migration ' + versionFrom + ' --> ' + versionTo);
        smarti.update({_id: 'db-version'}, {
            $set: {
                version: versionTo
            },
            $addToSet: {
                history: { version: versionTo, start: start, complete: new ISODate(), result: result, success: true }
            }
        });
    } catch (err) {
        print('Error during Migration: ' + err);
        smarti.update({_id: 'db-version'}, {
            $set: { isTainted: true },
            $addToSet: {
                history: { version: versionTo, start: start, complete: new ISODate(), result: err, success: false }
            }
        });
        dbVersion = smarti.findOne({_id: 'db-version'});
        if (dbVersion.backup && dbVersion.backup.name) {
            print('Restore database backup: ' + dbVersion.backup.name);
            db.dropDatabase();
            db.copyDatabase(dbVersion.backup.name, db.getName());
        }
        break;
    } finally {
        // release lock
        smarti.update({_id: 'db-version'}, {
            $set: {
                isUpgrading: false
            }
        });
    }
    break;
}

function runDatabaseMigration() {
    var clientConfig = db.getCollection('configuration');

    clientConfig
        .find({'config.queryBuilder': { $elemMatch: { type: 'solrsearch' }}})
        .forEach(function (configuration) {
            configuration.config.queryBuilder.forEach(function (builder) {
                if (builder.type !== 'solrsearch') return;

                var convertedDefaults = {};
                for (var k in builder.defaults) {
                    if (builder.defaults.hasOwnProperty(k)){
                        try {
                            convertedDefaults[k] = decodeURIComponent(builder.defaults[k]);
                        } catch (e) {
                            print('Could not uri-decode "' + k + "' of config '" + builder.name + "' for client '" + configuration.client + "'." +
                                " Keeping unmodified parameter '" + builder.defaults[k] + "'");
                            convertedDefaults[k] = builder.defaults[k];
                        }
                    }
                }
                builder.defaults = convertedDefaults;
            });

            clientConfig.save(configuration);
        });
}
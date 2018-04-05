CREATE PROCEDURE Cw_UpdateSchema1_2
  BEGIN
    IF (SELECT MAX(version) = 1
        FROM mg_schema_version
        WHERE plugin = 'ColonyWars')
    THEN

      ALTER TABLE mccw_stats
        ADD COLUMN uuid BINARY(16) NOT NULL;
      UPDATE mccw_stats
      SET uuid = UNHEX(REPLACE(player_uuid, '-', ''));
      ALTER TABLE mccw_stats
        DROP INDEX unique_index;
      ALTER TABLE mccw_stats
        DROP COLUMN player_uuid;
      ALTER TABLE mccw_stats
        ADD UNIQUE INDEX unique_index (uuid, category);
      ALTER TABLE mccw_stats
        MODIFY COLUMN category VARCHAR(50) NOT NULL;
      ALTER TABLE mccw_stats
        MODIFY COLUMN version DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

      INSERT IGNORE INTO mg_name_cache (uuid, name, timestamp)
        SELECT
          m1.uuid,
          m1.name,
          m1.version
      FROM mccw_stats AS m1
        LEFT JOIN mccw_stats AS m2
          ON m1.uuid = m2.uuid AND m1.version > m2.version
      WHERE m2.uuid IS NULL;

      ALTER TABLE mccw_stats
        DROP COLUMN name;

      INSERT INTO mg_schema_version (version, plugin) VALUES (2, 'ColonyWars');

    END IF;
  END;

CALL Cw_UpdateSchema1_2();
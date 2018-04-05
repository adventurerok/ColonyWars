CREATE TABLE `mccw_stats` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category` varchar(50) NOT NULL,
  `game_wins` int(11) DEFAULT NULL,
  `game_losses` int(11) DEFAULT NULL,
  `kills` int(11) DEFAULT NULL,
  `deaths` int(11) DEFAULT NULL,
  `total_money` int(11) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `games` int(11) DEFAULT NULL,
  `version` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `uuid` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index` (`uuid`,`category`)
);

INSERT INTO mg_schema_version (version, plugin) VALUES (2, 'ColonyWars');
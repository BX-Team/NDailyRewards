CREATE TABLE IF NOT EXISTS `data` (
    `uuid` VARCHAR(36) NOT NULL PRIMARY KEY,
    `next_time` BIGINT NOT NULL,
    `next_day` INT NOT NULL
);

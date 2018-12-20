DROP TABLE IF EXISTS demo;

CREATE TABLE demo (
    id int(11) NOT NULL AUTO_INCREMENT,
    info text NOT NULL,
    is_marked tinyint(11) DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=6;

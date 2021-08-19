Video Production Inventory Management
=====================================

Keeping track of who uses what equipment out in the field and ensuring that it
comes back in good shape is important. Accountability is key when it comes to
these things.

VIMS can track the current inventory of equipment, as well as who last checked
out/in equipment and what comments they left.

VIMS utilizes a DYMO Label printer to print Inventory, Macro, and User Identification
Labels on [Dymo LV-30332 Labels](http://amzn.com/B00004Z60O). A browser that
supports the Dymo Framework (So far only Safari has been tested, Chrome does
not support printing).

## Setup
VIMS is written primarily in Java and is run using the TomCat framework to run
the website. A MySQL DB is used to store user information, as well as store the
inventory and record check out/in transactions.

VIMS uses Vault to access credentials for various tools and services
(DataBase, APIs, Email, etc.). Details on how to setup Vault for this WebApp
can be found towards the bottom of the ReadMe.

Unit Testing is a feature in VIMS that allows most Classes and Methods of the
WebApp's Backend to be tested.

### DB Config
To setup they MySQL database for VIMS, run the following commands in your
MySQL Database to setup the tables, you will need to first create the Database
and user to access the data, using your root credentials in the web-app is not
recommended.

You will also need to create the first user in the database, make sure that
supervisor is set to 1 for that user to allow them full access.

#### Table Breakdown
Tables            | Function/Description
----------------- | --------------------------------------------------------
users             | Stores the users for the system, including administrators.
inventory         | Stores all items that should be available for checkout, as well as the last comment associated with the item, and its current status (in/out). Short Name is used to print a short description at the top of the Inventory Sticker, instead of the default text.
categories        | Stores Item Categories which are used by the Front End to identify items that have been added to the transaction and mark the checklist item as completed.
transactions      | Stores int inbound and outbound transactions as well as the respective timestamps, conditions, owner and supervisor.
quotes            | To add a bit of excitement and variety, a quote is displayed on the start page, the front-end pulls the quote of the day daily, or when first loaded. (A good source for quotes is [Brainy Quote](http://www.brainyquote.com/)).
macros            | Stores the Item Macros. Item Macros allow several items to be represented by a single id/barcode. This information, as well as the Macro's Name, which is used only to identify the Macro on the Admin Page, are saved in this table.


#### DDL
```
CREATE TABLE users (
  `id`         INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  `username`   TEXT,
  `first_name` TEXT,
  `last_name`  TEXT,
  `supervisor` TINYINT(1),
  `password`   TEXT
);
CREATE TABLE categories (
  `id`   INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  `name` TEXT,
  `icon` BLOB
);
CREATE TABLE inventory (
  `id`          INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  `pub_id`      INT(8),
  `category`    INT(3),
  `name`        TEXT,
  `short_name`  TEXT,
  `comments`    TEXT,
  `checked_out` TINYINT(1) DEFAULT 0,
  `asset_id`    TEXT,
  FOREIGN KEY (`category`) REFERENCES categories (`id`)
);
CREATE TABLE transactions (
  `id`         VARCHAR(32)                             NOT NULL,
  `item_id`    INT,
  `owner`      INT,
  `time`       TIMESTAMP DEFAULT CURRENT_TIMESTAMP     NOT NULL,
  `supervisor` INT,
  `condition`  TEXT,
  `direction`  TINYINT(1), # 0 for Out, 1 for IN
  FOREIGN KEY (`item_id`) REFERENCES inventory (`id`),
  FOREIGN KEY (`owner`) REFERENCES users (`id`),
  FOREIGN KEY (`supervisor`) REFERENCES users (`id`)
);
CREATE TABLE quotes (
  `id`     INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  `text`   TEXT,
  `author` TEXT
);
CREATE TABLE macros (
  `id`      INT PRIMARY KEY NOT NULL,
  `name`    TEXT,
  `itemIDs` TEXT
);
```

### Vault Setup
You will need to create two secrets in the vault, one that will have the information for your production system, the other with your testing configuration. Information on how to setup Vault and AppRoles can be found at: https://sdsu-its.gitbooks.io/vault/content/

The name of the app that you want to use needs to be set as the `VIMS_APP` environment variable. You will also need to set the `VAULT_ADDR`, `VAULT_ROLE` and `VAULT_SECRET` environment variables to their corresponding values.

- `db-password` = Database Password
- `db-url` = jdbc:mysql://db_host:3306/db_name
 + *replace db_host, db_name and possibly the port with your MySQL server info*
- `db-user` = Database Username
- `project_token` = Unique Project Identifier for Session Tokens, If changed, all tokens will become invalid.
- `token_cypher` = Token Encryption Cypher. If changed, all tokens will become invalid.
- `token_ttl` = Token Longevity (How long will a user stay logged in) in Milliseconds
    + Recommended Value: 86400000 -> 24 Hours

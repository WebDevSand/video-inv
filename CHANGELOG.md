# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [2.1.0] - 2017-03-10 - _Los Angeles_
### Added
 - Added Support for Tracking an Item's Asset ID
 - This item ID is also now included in Transaction Receipts
 - Added Application/Touch Icon

### Changed
 - To support the Asset ID, a column was added to the `inventory` table.
 To adjust your Database to match the new required Schema, run the SQL statement
 below. This can be done prior to the update.
 ```
 ALTER TABLE inventory
     ADD asset_id TEXT;
 ```

### Fixed
 - [[VIMS-1](http://morden.sdsu.edu:9000/issue/VIMS-1)] Category was not
 assigned on item creation.
 - [[VIMS-2](http://morden.sdsu.edu:9000/issue/VIMS-2)] Added App Icon.
 - [[VIMS-8](http://morden.sdsu.edu:9000/issue/VIMS-8)] Added Asset ID Support.

## [2.0.1] - 2017-01-21 - _Coahoma_
### Added
 - Macro Support
 - Category Support
 - Improved Item Handling

### Changed
 - Improved UI and rebuilt Transaction Model
 - Redesign of Admin Panel
 - More Secure Admin login

### Fixed
 - Stability Issues


## [1.0.0] - 2016-04-19 - _Coles_
### Added
 - Initial Release

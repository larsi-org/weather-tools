-- Table creation for the larsi-weather2 schema (prospective successor to larsi-weather).
-- Generated 2026-07-13:
--   - `log`: single shared multi-tenant table for all stations, generated via
--     org.larsi.dev.CreateDB.table() (uncomment `table();` in its main(), `mvn compile`,
--     run `org.larsi.dev.CreateDB`, then re-comment) — the `station` ENUM is derived from
--     util/Icao.kt's registered station list.
--   - `location`: table definition copied verbatim (SHOW CREATE TABLE) from the live
--     larsi-weather schema, since GeoNames2/NDFD2/Ish2 all query it the same way GeoNames/NDFD
--     do. `Location` is a spatial POINT column (lng lat), same convention used by both
--     larsi-weather.location and larsi-sensors.location — enables SQL distance calculations via
--     e.g. ST_Distance_Sphere(a.Location, b.Location) (returns meters), no manual Haversine SQL
--     needed. Row data generated via org.larsi.dev.CreateDB.location() the same way as `table()`
--     above; `Prefix` values are intentionally lowercase, matching production's convention (code
--     always uppercases whatever it reads back before matching it against the `log.station` ENUM,
--     so the stored case doesn't affect querying).
-- Run against the larsi-weather2 database (empty, created 2026-07-13 on the production server).
--
-- WARNING (as of 2026-07-14): larsi-weather2.log now holds live production data — GeoNames2 has
-- been running against it since Jenkins was switched over from GeoNames. Since 2026-07-14, `log`
-- and `location`'s DROP TABLE IF EXISTS statements below are destructive if this file is ever
-- re-run in full; extract only the section you actually need to (re-)run (e.g. the `sensor` table
-- added below was applied standalone, not by rerunning this whole file).

DROP TABLE IF EXISTS `log`;
CREATE TABLE IF NOT EXISTS `log` (
  `epoch` int NOT NULL,
  `station` ENUM ('KABI','KABQ','KABR','KACK','KACY','KADM','KAFF','KAGS','KAHN','KAKO','KAKQ','KALB','KALO','KAMA','KANE','KANJ','KAPA','KAPN','KASD','KASE','KAST','KATL','KAUG','KAUS','KAVL','KAWM','KAXN','KBDR','KBED','KBFI','KBGM','KBGR','KBHM','KBIS','KBJC','KBKF','KBLV','KBNA','KBOI','KBOS','KBRD','KBRO','KBTR','KBTV','KBUF','KBWG','KBWI','KBZN','KCAE','KCAG','KCAR','KCDC','KCDS','KCHA','KCHS','KCID','KCKB','KCLE','KCLL','KCLT','KCMH','KCOD','KCOE','KCOS','KCOU','KCRG','KCRP','KCTB','KCVG','KCVS','KCYS','KDAB','KDAL','KDAY','KDBQ','KDCA','KDDC','KDEC','KDEN','KDET','KDFW','KDLH','KDLS','KDPG','KDRA','KDRO','KDRT','KDSM','KDTW','KDVN','KDVT','KEAU','KEDW','KEET','KEKN','KEKO','KELM','KELP','KELY','KEND','KENV','KERI','KEUG','KEVV','KEWR','KEYW','KFAR','KFAT','KFAY','KFDR','KFFC','KFLG','KFNT','KFSD','KFSM','KFTW','KFTY','KFWA','KFYV','KGCK','KGCN','KGEG','KGFK','KGGG','KGGW','KGJT','KGLD','KGLR','KGMU','KGNV','KGRB','KGRK','KGRR','KGSO','KGSP','KGTF','KGWO','KGXY','KGYY','KHFD','KHIB','KHLN','KHMN','KHOU','KHQM','KHSE','KHSV','KHTS','KHUL','KHVR','KHYS','KIAD','KIAG','KIAH','KICT','KILG','KILM','KILN','KIND','KINL','KINT','KINW','KISP','KITH','KIWA','KJAN','KJAX','KJFK','KJKL','KLAA','KLAF','KLAM','KLAN','KLAS','KLAX','KLBB','KLBF','KLBL','KLCH','KLEX','KLGA','KLIC','KLIT','KLNK','KLOT','KLOU','KLRD','KLRF','KLUK','KLVS','KMAF','KMCI','KMCN','KMCO','KMDW','KMEM','KMFR','KMGM','KMGW','KMHK','KMIA','KMKC','KMKE','KMKG','KMLB','KMLS','KMOB','KMPV','KMRB','KMRY','KMSN','KMSO','KMSP','KMSY','KMWL','KNEW','KNID','KNKT','KNKX','KNQA','KOAK','KOFF','KOGD','KOKC','KOMA','KONT','KOPF','KORD','KORF','KOUN','KPAH','KPBI','KPDT','KPDX','KPGA','KPHL','KPHX','KPIA','KPIH','KPIR','KPIT','KPKD','KPNA','KPNE','KPOE','KPOU','KPRC','KPSP','KPTK','KPUB','KPVD','KPWM','KRAP','KRDD','KRDU','KRFD','KRIC','KRIW','KRNO','KROA','KROC','KROW','KSAC','KSAF','KSAN','KSAT','KSAV','KSBN','KSBY','KSEA','KSFO','KSGF','KSHR','KSHV','KSJT','KSLC','KSLE','KSMP','KSPS','KSTC','KSTJ','KSTL','KSTP','KSUS','KSUX','KSYR','KTLH','KTOP','KTPA','KTPH','KTRI','KTUL','KTUP','KTUS','KTYR','KTYS','KUIL','KUNV','KVBG','KVRB','KVTN','KWAL','KWMC','KYKM') NOT NULL,
  `sensor_id` tinyint unsigned NOT NULL,
  `value` float NOT NULL,
  KEY `epoch` (`epoch`),
  KEY `station` (`station`),
  KEY `sensor_id` (`sensor_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
  `Prefix` varchar(16) NOT NULL,
  `Description` varchar(64) NOT NULL,
  `Location` point NOT NULL,
  `Elevation` float NOT NULL,
  `TimeZone` float NOT NULL DEFAULT 0,
  `Priority` tinyint(4) NOT NULL DEFAULT 0,
  `Comment` varchar(255) NOT NULL,
  PRIMARY KEY (`Prefix`),
  SPATIAL KEY `Location` (`Location`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT INTO `location` VALUES('kabq', 'Albuquerque, NM, US', GeomFromText('POINT(-106.616 +35.042)'), +1618.5, 0, 80, 'Albuquerque, Albuquerque International Airport, NM, US');
INSERT INTO `location` VALUES('kanj', 'Sault Ste. Marie, MI, US', GeomFromText('POINT(-084.357 +46.479)'), +0220.1, 0, 80, 'Sault Ste. Marie, MI, US');
INSERT INTO `location` VALUES('katl', 'Atlanta, GA, US', GeomFromText('POINT(-084.442 +33.630)'), +0307.9, 0, 80, 'Atlanta, Hartsfield - Jackson Atlanta International Airport, GA, US');
INSERT INTO `location` VALUES('kbis', 'Bismarck, ND, US', GeomFromText('POINT(-100.757 +46.783)'), +0503.2, 0, 80, 'Bismarck, Bismarck Municipal Airport, ND, US');
INSERT INTO `location` VALUES('kboi', 'Boise, ID, US', GeomFromText('POINT(-116.241 +43.567)'), +0857.7, 0, 80, 'Boise, Boise Air Terminal, ID, US');
INSERT INTO `location` VALUES('kbos', 'Boston, MA, US', GeomFromText('POINT(-071.010 +42.361)'), +0003.7, 0, 80, 'Boston, Logan International Airport, MA, US');
INSERT INTO `location` VALUES('kbro', 'Brownsville, TX, US', GeomFromText('POINT(-097.423 +25.914)'), +0007.3, 0, 80, 'Brownsville, Brownsville / South Padre Island International Airport, TX, US');
INSERT INTO `location` VALUES('kbuf', 'Buffalo, NY, US', GeomFromText('POINT(-078.736 +42.941)'), +0218.2, 0, 80, 'Buffalo, Greater Buffalo International Airport, NY, US');
INSERT INTO `location` VALUES('kcar', 'Caribou, ME, US', GeomFromText('POINT(-068.017 +46.871)'), +0190.2, 0, 80, 'Caribou, Caribou Municipal Airport, ME, US');
INSERT INTO `location` VALUES('kcvg', 'Covington / Cincinnati, KY, US', GeomFromText('POINT(-084.672 +39.044)'), +0269.1, 0, 80, 'Covington / Cincinnati, Cincinnati / Northern Kentucky International Airport, KY, US');
INSERT INTO `location` VALUES('kddc', 'Dodge City, KS, US', GeomFromText('POINT(-099.968 +37.769)'), +0787.0, 0, 80, 'Dodge City, Dodge City Regional Airport, KS, US');
INSERT INTO `location` VALUES('kden', 'Denver, CO, US', GeomFromText('POINT(-104.658 +39.833)'), +1650.2, 0, 80, 'Denver, Denver International Airport, CO, US');
INSERT INTO `location` VALUES('kdfw', 'Dallas / Fort Worth, TX, US', GeomFromText('POINT(-097.019 +32.898)'), +0170.7, 0, 80, 'Dallas / Fort Worth, Dallas / Fort Worth International Airport, TX, US');
INSERT INTO `location` VALUES('kgtf', 'Great Falls, MT, US', GeomFromText('POINT(-111.382 +47.473)'), +1116.8, 0, 80, 'Great Falls, Great Falls International Airport, MT, US');
INSERT INTO `location` VALUES('kiad', 'Washington DC, VA, US', GeomFromText('POINT(-077.447 +38.935)'), +0088.4, 0, 80, 'Washington DC, Washington-Dulles International Airport, VA, US');
INSERT INTO `location` VALUES('kilm', 'Wilmington, NC, US', GeomFromText('POINT(-077.900 +34.268)'), +0010.1, 0, 80, 'Wilmington, Wilmington International Airport, NC, US');
INSERT INTO `location` VALUES('kjax', 'Jacksonville, FL, US', GeomFromText('POINT(-081.694 +30.495)'), +0007.9, 0, 80, 'Jacksonville, Jacksonville International Airport, FL, US');
INSERT INTO `location` VALUES('klax', 'Los Angeles, CA, US', GeomFromText('POINT(-118.389 +33.938)'), +0029.6, 0, 80, 'Los Angeles, Los Angeles International Airport, CA, US');
INSERT INTO `location` VALUES('klbb', 'Lubbock, TX, US', GeomFromText('POINT(-101.823 +33.666)'), +0991.8, 0, 80, 'Lubbock, Lubbock International Airport, TX, US');
INSERT INTO `location` VALUES('klit', 'Little Rock, AR, US', GeomFromText('POINT(-092.239 +34.727)'), +0078.6, 0, 80, 'Little Rock, Adams Field, AR, US');
INSERT INTO `location` VALUES('kmci', 'Kansas City, MO, US', GeomFromText('POINT(-094.731 +39.297)'), +0306.3, 0, 80, 'Kansas City, Kansas City International Airport, MO, US');
INSERT INTO `location` VALUES('kmfr', 'Medford, OR, US', GeomFromText('POINT(-122.877 +42.375)'), +0400.2, 0, 80, 'Medford, Rogue Valley International Airport, OR, US');
INSERT INTO `location` VALUES('kmia', 'Miami, FL, US', GeomFromText('POINT(-080.317 +25.788)'), +0008.8, 0, 80, 'Miami, Miami International Airport, FL, US');
INSERT INTO `location` VALUES('kmsp', 'Minneapolis, MN, US', GeomFromText('POINT(-093.229 +44.883)'), +0265.8, 0, 80, 'Minneapolis, Minneapolis-St. Paul International Airport, MN, US');
INSERT INTO `location` VALUES('kmsy', 'New Orleans, LA, US', GeomFromText('POINT(-090.278 +29.997)'), +0001.2, 0, 80, 'New Orleans, New Orleans International Airport, LA, US');
INSERT INTO `location` VALUES('kord', 'Chicago, IL, US', GeomFromText('POINT(-087.932 +41.960)'), +0201.8, 0, 80, 'Chicago, Chicago-O''Hare International Airport, IL, US');
INSERT INTO `location` VALUES('kphx', 'Phoenix, AZ, US', GeomFromText('POINT(-112.004 +33.428)'), +0337.4, 0, 80, 'Phoenix, Phoenix Sky Harbor International Airport, AZ, US');
INSERT INTO `location` VALUES('ksat', 'San Antonio, TX, US', GeomFromText('POINT(-098.484 +29.544)'), +0240.5, 0, 80, 'San Antonio, San Antonio International Airport, TX, US');
INSERT INTO `location` VALUES('ksea', 'Seattle, WA, US', GeomFromText('POINT(-122.314 +47.444)'), +0112.8, 0, 80, 'Seattle, Seattle-Tacoma International Airport, WA, US');
INSERT INTO `location` VALUES('ksfo', 'San Francisco, CA, US', GeomFromText('POINT(-122.365 +37.620)'), +0002.4, 0, 80, 'San Francisco, San Francisco International Airport, CA, US');
INSERT INTO `location` VALUES('kslc', 'Salt Lake City, UT, US', GeomFromText('POINT(-111.969 +40.778)'), +1287.8, 0, 80, 'Salt Lake City, Salt Lake City International Airport, UT, US');
INSERT INTO `location` VALUES('kstl', 'St. Louis, MO, US', GeomFromText('POINT(-090.374 +38.753)'), +0161.9, 0, 80, 'St. Louis, Lambert-St. Louis International Airport, MO, US');
INSERT INTO `location` VALUES('ktph', 'Tonopah, NV, US', GeomFromText('POINT(-117.090 +38.051)'), +1644.4, 0, 80, 'Tonopah, Tonopah Airport, NV, US');
INSERT INTO `location` VALUES('kbfi', 'Seattle, WA, US', GeomFromText('POINT(-122.301 +47.530)'), +0005.5, 0, 75, 'Seattle, Seattle Boeing Field, WA, US');
INSERT INTO `location` VALUES('kbjc', 'Broomfield / Jeffco, CO, US', GeomFromText('POINT(-105.117 +39.900)'), +1705.4, 0, 75, 'Broomfield / Jeffco, CO, US');
INSERT INTO `location` VALUES('kblv', 'Belleville, IL, US', GeomFromText('POINT(-089.850 +38.550)'), +0139.9, 0, 75, 'Belleville, Scott AFB/MidAmerica Airport, IL, US');
INSERT INTO `location` VALUES('kchs', 'Charleston, SC, US', GeomFromText('POINT(-080.041 +32.899)'), +0012.2, 0, 75, 'Charleston, Charleston Air Force Base, SC, US');
INSERT INTO `location` VALUES('kcrg', 'Jacksonville, FL, US', GeomFromText('POINT(-081.515 +30.336)'), +0012.5, 0, 75, 'Jacksonville, Craig Municipal Airport, FL, US');
INSERT INTO `location` VALUES('kdal', 'Dallas, TX, US', GeomFromText('POINT(-096.856 +32.852)'), +0134.1, 0, 75, 'Dallas, Dallas Love Field, TX, US');
INSERT INTO `location` VALUES('kdca', 'Washington DC, VA, US', GeomFromText('POINT(-077.035 +38.847)'), +0003.1, 0, 75, 'Washington DC, Reagan National Airport, VA, US');
INSERT INTO `location` VALUES('kdtw', 'Detroit, MI, US', GeomFromText('POINT(-083.331 +42.231)'), +0192.3, 0, 75, 'Detroit, Detroit Metropolitan Wayne County Airport, MI, US');
INSERT INTO `location` VALUES('kdvt', 'Phoenix, AZ, US', GeomFromText('POINT(-112.082 +33.688)'), +0443.5, 0, 75, 'Phoenix, Phoenix-Deer Valley Municipal Airport, AZ, US');
INSERT INTO `location` VALUES('keug', 'Eugene, OR, US', GeomFromText('POINT(-123.221 +44.128)'), +0107.6, 0, 75, 'Eugene, Mahlon Sweet Field, OR, US');
INSERT INTO `location` VALUES('kfar', 'Fargo, ND, US', GeomFromText('POINT(-096.811 +46.925)'), +0274.3, 0, 75, 'Fargo, Hector International Airport, ND, US');
INSERT INTO `location` VALUES('kffc', 'Atlanta, GA, US', GeomFromText('POINT(-084.567 +33.355)'), +0243.2, 0, 75, 'Atlanta, Peachtree City-Falcon Field, GA, US');
INSERT INTO `location` VALUES('kgeg', 'Spokane, WA, US', GeomFromText('POINT(-117.528 +47.622)'), +0717.2, 0, 75, 'Spokane, Spokane International Airport, WA, US');
INSERT INTO `location` VALUES('khse', 'Hatteras, NC, US', GeomFromText('POINT(-075.622 +35.233)'), +0003.4, 0, 75, 'Hatteras, Mitchell Field, NC, US');
INSERT INTO `location` VALUES('kinl', 'International Falls, MN, US', GeomFromText('POINT(-093.398 +48.561)'), +0360.6, 0, 75, 'International Falls, Falls International Airport, MN, US');
INSERT INTO `location` VALUES('kjfk', 'New York, NY, US', GeomFromText('POINT(-073.764 +40.639)'), +0003.4, 0, 75, 'New York, Kennedy International Airport, NY, US');
INSERT INTO `location` VALUES('klga', 'New York, NY, US', GeomFromText('POINT(-073.880 +40.779)'), +0003.4, 0, 75, 'New York, La Guardia Airport, NY, US');
INSERT INTO `location` VALUES('klrf', 'Little Rock Air Force Base, AR, US', GeomFromText('POINT(-092.150 +34.917)'), +0094.8, 0, 75, 'Little Rock Air Force Base, AR, US');
INSERT INTO `location` VALUES('kluk', 'Cincinnati, OH, US', GeomFromText('POINT(-084.419 +39.103)'), +0149.4, 0, 75, 'Cincinnati, Cincinnati Municipal Airport Lunken Field, OH, US');
INSERT INTO `location` VALUES('kmdw', 'Chicago, IL, US', GeomFromText('POINT(-087.752 +41.786)'), +0186.5, 0, 75, 'Chicago, Chicago Midway Airport, IL, US');
INSERT INTO `location` VALUES('kmkc', 'Kansas City, MO, US', GeomFromText('POINT(-094.597 +39.121)'), +0226.2, 0, 75, 'Kansas City, Kansas City Downtown Airport, MO, US');
INSERT INTO `location` VALUES('knew', 'New Orleans, LA, US', GeomFromText('POINT(-090.029 +30.049)'), +0002.7, 0, 75, 'New Orleans, New Orleans Lakefront Airport, LA, US');
INSERT INTO `location` VALUES('koak', 'Oakland, CA, US', GeomFromText('POINT(-122.221 +37.721)'), +0001.8, 0, 75, 'Oakland, Metro Oakland International Airport, CA, US');
INSERT INTO `location` VALUES('kogd', 'Ogden, UT, US', GeomFromText('POINT(-112.011 +41.196)'), +1362.5, 0, 75, 'Ogden, Ogden-Hinckley Airport, UT, US');
INSERT INTO `location` VALUES('kont', 'Ontario, CA, US', GeomFromText('POINT(-117.600 +34.056)'), +0289.3, 0, 75, 'Ontario, Ontario International Airport, CA, US');
INSERT INTO `location` VALUES('kopf', 'Miami, FL, US', GeomFromText('POINT(-080.280 +25.907)'), +0003.1, 0, 75, 'Miami, Opa Locka Airport, FL, US');
INSERT INTO `location` VALUES('ksaf', 'Santa Fe, NM, US', GeomFromText('POINT(-106.089 +35.617)'), +1933.7, 0, 75, 'Santa Fe, Santa Fe County Municipal Airport, NM, US');
INSERT INTO `location` VALUES('kshr', 'Sheridan, WY, US', GeomFromText('POINT(-106.969 +44.769)'), +1202.4, 0, 75, 'Sheridan, Sheridan County Airport, WY, US');
INSERT INTO `location` VALUES('kstp', 'St. Paul, MN, US', GeomFromText('POINT(-093.056 +44.932)'), +0213.4, 0, 75, 'St. Paul, St. Paul Downtown Holman Field, MN, US');
INSERT INTO `location` VALUES('kvtn', 'Valentine, NE, US', GeomFromText('POINT(-100.550 +42.878)'), +0789.4, 0, 75, 'Valentine, Miller Field, NE, US');
INSERT INTO `location` VALUES('kack', 'Nantucket, MA, US', GeomFromText('POINT(-070.061 +41.253)'), +0014.6, 0, 70, 'Nantucket, Nantucket Memorial Airport, MA, US');
INSERT INTO `location` VALUES('kasd', 'Slidell, LA, US', GeomFromText('POINT(-089.822 +30.343)'), +0008.2, 0, 70, 'Slidell, Slidell Airport, LA, US');
INSERT INTO `location` VALUES('kbed', 'Bedford, MA, US', GeomFromText('POINT(-071.289 +42.470)'), +0040.5, 0, 70, 'Bedford, Hanscom Field, MA, US');
INSERT INTO `location` VALUES('kbkf', 'Aurora, CO, US', GeomFromText('POINT(-104.750 +39.717)'), +1726.1, 0, 70, 'Aurora, Buckley Air Force Base Airport, CO, US');
INSERT INTO `location` VALUES('kbna', 'Nashville, TN, US', GeomFromText('POINT(-086.689 +36.119)'), +0182.9, 0, 70, 'Nashville, Nashville International Airport, TN, US');
INSERT INTO `location` VALUES('kcrp', 'Corpus Christi, TX, US', GeomFromText('POINT(-097.512 +27.774)'), +0013.4, 0, 70, 'Corpus Christi, Corpus Christi International Airport, TX, US');
INSERT INTO `location` VALUES('kdlh', 'Duluth, MN, US', GeomFromText('POINT(-092.183 +46.837)'), +0436.8, 0, 70, 'Duluth, Duluth International Airport, MN, US');
INSERT INTO `location` VALUES('kdrt', 'Del Rio, TX, US', GeomFromText('POINT(-100.927 +29.378)'), +0304.5, 0, 70, 'Del Rio, Del Rio International Airport, TX, US');
INSERT INTO `location` VALUES('kdsm', 'Des Moines, IA, US', GeomFromText('POINT(-093.653 +41.534)'), +0291.7, 0, 70, 'Des Moines, Des Moines International Airport, IA, US');
INSERT INTO `location` VALUES('kelp', 'El Paso, TX, US', GeomFromText('POINT(-106.376 +31.811)'), +1194.2, 0, 70, 'El Paso, El Paso International Airport, TX, US');
INSERT INTO `location` VALUES('kewr', 'Newark, NJ, US', GeomFromText('POINT(-074.169 +40.683)'), +0002.1, 0, 70, 'Newark, Newark International Airport, NJ, US');
INSERT INTO `location` VALUES('keyw', 'Key West, FL, US', GeomFromText('POINT(-081.755 +24.557)'), +0000.3, 0, 70, 'Key West, Key West International Airport, FL, US');
INSERT INTO `location` VALUES('kfat', 'Fresno, CA, US', GeomFromText('POINT(-119.719 +36.780)'), +0101.5, 0, 70, 'Fresno, Fresno Air Terminal, CA, US');
INSERT INTO `location` VALUES('kflg', 'Flagstaff, AZ, US', GeomFromText('POINT(-111.666 +35.144)'), +2134.5, 0, 70, 'Flagstaff, Flagstaff Pulliam Airport, AZ, US');
INSERT INTO `location` VALUES('kfsd', 'Sioux Falls, SD, US', GeomFromText('POINT(-096.754 +43.578)'), +0435.3, 0, 70, 'Sioux Falls, Foss Field, SD, US');
INSERT INTO `location` VALUES('kftw', 'Fort Worth, TX, US', GeomFromText('POINT(-097.361 +32.819)'), +0209.4, 0, 70, 'Fort Worth, Meacham International Airport, TX, US');
INSERT INTO `location` VALUES('kgfk', 'Grand Forks, ND, US', GeomFromText('POINT(-097.184 +47.943)'), +0256.6, 0, 70, 'Grand Forks, Grand Forks International Airport, ND, US');
INSERT INTO `location` VALUES('kggw', 'Glasgow, MT, US', GeomFromText('POINT(-106.621 +48.214)'), +0696.5, 0, 70, 'Glasgow, Glasgow International Airport, MT, US');
INSERT INTO `location` VALUES('kgjt', 'Grand Junction, CO, US', GeomFromText('POINT(-108.540 +39.134)'), +1480.7, 0, 70, 'Grand Junction, Walker Field, CO, US');
INSERT INTO `location` VALUES('khul', 'Houlton, ME, US', GeomFromText('POINT(-067.793 +46.119)'), +0145.1, 0, 70, 'Houlton, Houlton International Airport, ME, US');
INSERT INTO `location` VALUES('kiah', 'Houston, TX, US', GeomFromText('POINT(-095.360 +29.980)'), +0029.0, 0, 70, 'Houston, Houston Intercontinental Airport, TX, US');
INSERT INTO `location` VALUES('kind', 'Indianapolis, IN, US', GeomFromText('POINT(-086.282 +39.725)'), +0241.1, 0, 70, 'Indianapolis, Indianapolis International Airport, IN, US');
INSERT INTO `location` VALUES('klas', 'Las Vegas, NV, US', GeomFromText('POINT(-115.163 +36.072)'), +0664.5, 0, 70, 'Las Vegas, McCarran International Airport, NV, US');
INSERT INTO `location` VALUES('kmco', 'Orlando, FL, US', GeomFromText('POINT(-081.325 +28.434)'), +0027.4, 0, 70, 'Orlando, Orlando International Airport, FL, US');
INSERT INTO `location` VALUES('kmem', 'Memphis, TN, US', GeomFromText('POINT(-089.987 +35.056)'), +0077.4, 0, 70, 'Memphis, Memphis International Airport, TN, US');
INSERT INTO `location` VALUES('kmry', 'Monterey, CA, US', GeomFromText('POINT(-121.845 +36.588)'), +0050.3, 0, 70, 'Monterey, Monterey Peninsula Airport, CA, US');
INSERT INTO `location` VALUES('kokc', 'Oklahoma City, OK, US', GeomFromText('POINT(-097.601 +35.389)'), +0391.7, 0, 70, 'Oklahoma City, Will Rogers World Airport, OK, US');
INSERT INTO `location` VALUES('koma', 'Omaha, NE, US', GeomFromText('POINT(-095.899 +41.310)'), +0299.3, 0, 70, 'Omaha, Eppley Airfield, NE, US');
INSERT INTO `location` VALUES('kpdt', 'Pendleton, OR, US', GeomFromText('POINT(-118.855 +45.698)'), +0452.9, 0, 70, 'Pendleton, Eastern Oregon Regional At Pendleton Airport, OR, US');
INSERT INTO `location` VALUES('kpdx', 'Portland, OR, US', GeomFromText('POINT(-122.609 +45.596)'), +0005.8, 0, 70, 'Portland, Portland International Airport, OR, US');
INSERT INTO `location` VALUES('kpit', 'Pittsburgh, PA, US', GeomFromText('POINT(-080.214 +40.485)'), +0366.7, 0, 70, 'Pittsburgh, Pittsburgh International Airport, PA, US');
INSERT INTO `location` VALUES('kptk', 'Pontiac, MI, US', GeomFromText('POINT(-083.418 +42.665)'), +0297.5, 0, 70, 'Pontiac, Oakland County International Airport, MI, US');
INSERT INTO `location` VALUES('krap', 'Rapid City, SD, US', GeomFromText('POINT(-103.054 +44.046)'), +0963.2, 0, 70, 'Rapid City, Rapid City Regional Airport, SD, US');
INSERT INTO `location` VALUES('krdu', 'Raleigh / Durham, NC, US', GeomFromText('POINT(-078.782 +35.892)'), +0126.8, 0, 70, 'Raleigh / Durham, Raleigh-Durham International Airport, NC, US');
INSERT INTO `location` VALUES('kriw', 'Riverton, WY, US', GeomFromText('POINT(-108.459 +43.064)'), +1659.6, 0, 70, 'Riverton, Riverton Regional Airport, WY, US');
INSERT INTO `location` VALUES('krno', 'Reno, NV, US', GeomFromText('POINT(-119.771 +39.484)'), +1344.2, 0, 70, 'Reno, Reno Tahoe International Airport, NV, US');
INSERT INTO `location` VALUES('ksac', 'Sacramento, CA, US', GeomFromText('POINT(-121.495 +38.507)'), +0004.6, 0, 70, 'Sacramento, Sacramento Executive Airport, CA, US');
INSERT INTO `location` VALUES('ksan', 'San Diego, CA, US', GeomFromText('POINT(-117.183 +32.734)'), +0004.6, 0, 70, 'San Diego, San Diego International-Lindbergh Field, CA, US');
INSERT INTO `location` VALUES('ksle', 'Salem, OR, US', GeomFromText('POINT(-123.001 +44.905)'), +0062.5, 0, 70, 'Salem, McNary Field, OR, US');
INSERT INTO `location` VALUES('ktus', 'Tucson, AZ, US', GeomFromText('POINT(-110.955 +32.131)'), +0776.9, 0, 70, 'Tucson, Tucson International Airport, AZ, US');
INSERT INTO `location` VALUES('kuil', 'Quillayute, WA, US', GeomFromText('POINT(-124.555 +47.938)'), +0056.4, 0, 70, 'Quillayute, Quillayute State Airport, WA, US');
INSERT INTO `location` VALUES('kabr', 'Aberdeen, SD, US', GeomFromText('POINT(-098.413 +45.443)'), +0395.3, 0, 65, 'Aberdeen, Aberdeen Regional Airport, SD, US');
INSERT INTO `location` VALUES('kalb', 'Albany, NY, US', GeomFromText('POINT(-073.799 +42.747)'), +0085.3, 0, 65, 'Albany, Albany International Airport, NY, US');
INSERT INTO `location` VALUES('kama', 'Amarillo, TX, US', GeomFromText('POINT(-101.704 +35.230)'), +1098.5, 0, 65, 'Amarillo, Amarillo International Airport, TX, US');
INSERT INTO `location` VALUES('kane', 'Minneapolis / Blaine, MN, US', GeomFromText('POINT(-093.217 +45.150)'), +0278.0, 0, 65, 'Minneapolis / Blaine, MN, US');
INSERT INTO `location` VALUES('kaus', 'Austin, TX, US', GeomFromText('POINT(-097.680 +30.183)'), +0146.3, 0, 65, 'Austin, Austin-Bergstrom International Airport, TX, US');
INSERT INTO `location` VALUES('kaxn', 'Alexandria, MN, US', GeomFromText('POINT(-095.394 +45.868)'), +0431.6, 0, 65, 'Alexandria, Chandler Field, MN, US');
INSERT INTO `location` VALUES('kbdr', 'Bridgeport, CT, US', GeomFromText('POINT(-073.127 +41.164)'), +0001.5, 0, 65, 'Bridgeport, Sikorsky Memorial Airport, CT, US');
INSERT INTO `location` VALUES('kbgm', 'Binghamton, NY, US', GeomFromText('POINT(-075.980 +42.207)'), +0486.2, 0, 65, 'Binghamton, Binghamton Regional Airport, NY, US');
INSERT INTO `location` VALUES('kbgr', 'Bangor, ME, US', GeomFromText('POINT(-068.819 +44.798)'), +0045.1, 0, 65, 'Bangor, Bangor International Airport, ME, US');
INSERT INTO `location` VALUES('kbtv', 'Burlington, VT, US', GeomFromText('POINT(-073.150 +44.468)'), +0100.6, 0, 65, 'Burlington, Burlington International Airport, VT, US');
INSERT INTO `location` VALUES('kbwi', 'Baltimore, MD, US', GeomFromText('POINT(-076.684 +39.173)'), +0047.6, 0, 65, 'Baltimore, Baltimore-Washington International Airport, MD, US');
INSERT INTO `location` VALUES('kcae', 'Columbia, SC, US', GeomFromText('POINT(-081.118 +33.942)'), +0068.6, 0, 65, 'Columbia, Columbia Metropolitan Airport, SC, US');
INSERT INTO `location` VALUES('kcds', 'Childress, TX, US', GeomFromText('POINT(-100.283 +34.427)'), +0594.7, 0, 65, 'Childress, Childress Municipal Airport, TX, US');
INSERT INTO `location` VALUES('kcle', 'Cleveland, OH, US', GeomFromText('POINT(-081.852 +41.406)'), +0238.1, 0, 65, 'Cleveland, Cleveland-Hopkins International Airport, OH, US');
INSERT INTO `location` VALUES('kcod', 'Cody, WY, US', GeomFromText('POINT(-109.017 +44.517)'), +1552.0, 0, 65, 'Cody, WY, US');
INSERT INTO `location` VALUES('kcos', 'Colorado Springs, CO, US', GeomFromText('POINT(-104.688 +38.810)'), +1884.0, 0, 65, 'Colorado Springs, City Of Colorado Springs Municipal Airport, CO, US');
INSERT INTO `location` VALUES('kcys', 'Cheyenne, WY, US', GeomFromText('POINT(-104.807 +41.158)'), +1863.2, 0, 65, 'Cheyenne, Cheyenne Airport, WY, US');
INSERT INTO `location` VALUES('kdab', 'Daytona Beach, FL, US', GeomFromText('POINT(-081.048 +29.183)'), +0009.5, 0, 65, 'Daytona Beach, Daytona Beach Regional Airport, FL, US');
INSERT INTO `location` VALUES('kdet', 'Detroit, MI, US', GeomFromText('POINT(-083.010 +42.409)'), +0190.8, 0, 65, 'Detroit, Detroit City Airport, MI, US');
INSERT INTO `location` VALUES('kdls', 'The Dalles, WA, US', GeomFromText('POINT(-121.166 +45.619)'), +0071.6, 0, 65, 'The Dalles, Columbia Gorge Regional / The Dalles Municipal Airport, WA, US');
INSERT INTO `location` VALUES('kdpg', 'Dugway Proving Grounds, UT, US', GeomFromText('POINT(-112.933 +40.183)'), +1325.6, 0, 65, 'Dugway Proving Grounds, UT, US');
INSERT INTO `location` VALUES('kdvn', 'Davenport, IA, US', GeomFromText('POINT(-090.591 +41.614)'), +0228.6, 0, 65, 'Davenport, Davenport Municipal Airport, IA, US');
INSERT INTO `location` VALUES('kedw', 'Edwards Air Force Base, CA, US', GeomFromText('POINT(-117.867 +34.900)'), +0704.4, 0, 65, 'Edwards Air Force Base, CA, US');
INSERT INTO `location` VALUES('keet', 'Alabaster, AL, US', GeomFromText('POINT(-086.782 +33.178)'), +0172.2, 0, 65, 'Alabaster, Shelby County Airport, AL, US');
INSERT INTO `location` VALUES('keko', 'Elko, NV, US', GeomFromText('POINT(-115.789 +40.829)'), +1533.1, 0, 65, 'Elko, Elko Regional Airport, NV, US');
INSERT INTO `location` VALUES('kely', 'Ely, NV, US', GeomFromText('POINT(-114.847 +39.295)'), +1908.7, 0, 65, 'Ely, Ely Airport, NV, US');
INSERT INTO `location` VALUES('kfsm', 'Fort Smith, AR, US', GeomFromText('POINT(-094.363 +35.333)'), +0136.9, 0, 65, 'Fort Smith, Fort Smith Regional Airport, AR, US');
INSERT INTO `location` VALUES('kfty', 'Atlanta, GA, US', GeomFromText('POINT(-084.521 +33.779)'), +0256.0, 0, 65, 'Atlanta, Fulton County Airport-Brown Field, GA, US');
INSERT INTO `location` VALUES('kfwa', 'Fort Wayne, IN, US', GeomFromText('POINT(-085.206 +40.971)'), +0241.1, 0, 65, 'Fort Wayne, Fort Wayne International Airport, IN, US');
INSERT INTO `location` VALUES('kgld', 'Goodland, KS, US', GeomFromText('POINT(-101.693 +39.367)'), +1114.4, 0, 65, 'Goodland, Renner Field, KS, US');
INSERT INTO `location` VALUES('kglr', 'Gaylord, MI, US', GeomFromText('POINT(-084.701 +45.013)'), +0406.9, 0, 65, 'Gaylord, Otsego County Airport, MI, US');
INSERT INTO `location` VALUES('kgrb', 'Green Bay, WI, US', GeomFromText('POINT(-088.137 +44.479)'), +0209.4, 0, 65, 'Green Bay, Austin Straubel International Airport, WI, US');
INSERT INTO `location` VALUES('kgrr', 'Grand Rapids, MI, US', GeomFromText('POINT(-085.524 +42.883)'), +0244.8, 0, 65, 'Grand Rapids, Gerald R. Ford International Airport, MI, US');
INSERT INTO `location` VALUES('kgso', 'Greensboro, NC, US', GeomFromText('POINT(-079.943 +36.097)'), +0271.3, 0, 65, 'Greensboro, Piedmont Triad International Airport, NC, US');
INSERT INTO `location` VALUES('kgsp', 'Greer, SC, US', GeomFromText('POINT(-082.213 +34.906)'), +0291.1, 0, 65, 'Greer, Greenville-Spartanburg Airport, SC, US');
INSERT INTO `location` VALUES('khib', 'Hibbing, MN, US', GeomFromText('POINT(-092.839 +47.386)'), +0412.1, 0, 65, 'Hibbing, Chisholm-Hibbing Airport, MN, US');
INSERT INTO `location` VALUES('khou', 'Houston, TX, US', GeomFromText('POINT(-095.282 +29.638)'), +0013.4, 0, 65, 'Houston, Houston Hobby Airport, TX, US');
INSERT INTO `location` VALUES('khts', 'Huntington, WV, US', GeomFromText('POINT(-082.555 +38.365)'), +0251.2, 0, 65, 'Huntington, Tri-State Airport, WV, US');
INSERT INTO `location` VALUES('khvr', 'Havre, MT, US', GeomFromText('POINT(-109.763 +48.543)'), +0787.9, 0, 65, 'Havre, Havre City-County Airport, MT, US');
INSERT INTO `location` VALUES('kict', 'Wichita, KS, US', GeomFromText('POINT(-097.430 +37.648)'), +0402.6, 0, 65, 'Wichita, Wichita Mid-Continent Airport, KS, US');
INSERT INTO `location` VALUES('kiln', 'Wilmington, OH, US', GeomFromText('POINT(-083.777 +39.431)'), +0325.2, 0, 65, 'Wilmington, Airborne Airpark Airport, OH, US');
INSERT INTO `location` VALUES('kiwa', 'Mesa, AZ, US', GeomFromText('POINT(-111.667 +33.300)'), +0421.2, 0, 65, 'Mesa, Williams Gateway Airport, AZ, US');
INSERT INTO `location` VALUES('kjan', 'Jackson, MS, US', GeomFromText('POINT(-090.078 +32.321)'), +0100.6, 0, 65, 'Jackson, Jackson International Airport, MS, US');
INSERT INTO `location` VALUES('klbf', 'North Platte, NE, US', GeomFromText('POINT(-100.669 +41.121)'), +0846.7, 0, 65, 'North Platte, North Platte Regional Airport, NE, US');
INSERT INTO `location` VALUES('klch', 'Lake Charles, LA, US', GeomFromText('POINT(-093.228 +30.125)'), +0002.7, 0, 65, 'Lake Charles, Lake Charles Regional Airport, LA, US');
INSERT INTO `location` VALUES('klnk', 'Lincoln, NE, US', GeomFromText('POINT(-096.748 +40.851)'), +0362.7, 0, 65, 'Lincoln, Lincoln Municipal Airport, NE, US');
INSERT INTO `location` VALUES('klou', 'Louisville, KY, US', GeomFromText('POINT(-085.664 +38.228)'), +0164.6, 0, 65, 'Louisville, Bowman Field Airport, KY, US');
INSERT INTO `location` VALUES('klrd', 'Laredo, TX, US', GeomFromText('POINT(-099.467 +27.533)'), +0150.6, 0, 65, 'Laredo, Laredo International Airport, TX, US');
INSERT INTO `location` VALUES('klvs', 'Las Vegas, NM, US', GeomFromText('POINT(-105.142 +35.654)'), +2095.2, 0, 65, 'Las Vegas, Las Vegas Municipal Airport, NM, US');
INSERT INTO `location` VALUES('kmcn', 'Macon, GA, US', GeomFromText('POINT(-083.653 +32.685)'), +0104.6, 0, 65, 'Macon, Middle Georgia Regional Airport, GA, US');
INSERT INTO `location` VALUES('kmgw', 'Morgantown, WV, US', GeomFromText('POINT(-079.916 +39.643)'), +0378.0, 0, 65, 'Morgantown, Morgantown Municipal-Hart Field, WV, US');
INSERT INTO `location` VALUES('kmke', 'Milwaukee, WI, US', GeomFromText('POINT(-087.904 +42.955)'), +0204.2, 0, 65, 'Milwaukee, General Mitchell International Airport, WI, US');
INSERT INTO `location` VALUES('kmob', 'Mobile, AL, US', GeomFromText('POINT(-088.246 +30.688)'), +0065.5, 0, 65, 'Mobile, Mobile Regional Airport, AL, US');
INSERT INTO `location` VALUES('kmso', 'Missoula, MT, US', GeomFromText('POINT(-114.093 +46.921)'), +0972.9, 0, 65, 'Missoula, Missoula International Airport, MT, US');
INSERT INTO `location` VALUES('kmwl', 'Mineral Wells, TX, US', GeomFromText('POINT(-098.060 +32.782)'), +0283.5, 0, 65, 'Mineral Wells, Mineral Wells Airport, TX, US');
INSERT INTO `location` VALUES('knkt', 'Cherry Point, NC, US', GeomFromText('POINT(-076.883 +34.900)'), +0008.8, 0, 65, 'Cherry Point, Marine Corps Air Station, NC, US');
INSERT INTO `location` VALUES('korf', 'Norfolk, VA, US', GeomFromText('POINT(-076.192 +36.903)'), +0009.1, 0, 65, 'Norfolk, Norfolk International Airport, VA, US');
INSERT INTO `location` VALUES('koun', 'Norman / Max Westheimer, OK, US', GeomFromText('POINT(-097.467 +35.250)'), +0360.3, 0, 65, 'Norman / Max Westheimer, OK, US');
INSERT INTO `location` VALUES('kpah', 'Paducah, KY, US', GeomFromText('POINT(-088.774 +37.056)'), +0125.9, 0, 65, 'Paducah, Barkley Regional Airport, KY, US');
INSERT INTO `location` VALUES('kpbi', 'West Palm Beach, FL, US', GeomFromText('POINT(-080.099 +26.685)'), +0005.8, 0, 65, 'West Palm Beach, Palm Beach International Airport, FL, US');
INSERT INTO `location` VALUES('kpga', 'Page, AZ, US', GeomFromText('POINT(-111.448 +36.926)'), +1313.7, 0, 65, 'Page, Page Municipal Airport, AZ, US');
INSERT INTO `location` VALUES('kphl', 'Philadelphia, PA, US', GeomFromText('POINT(-075.227 +39.873)'), +0003.1, 0, 65, 'Philadelphia, Philadelphia International Airport, PA, US');
INSERT INTO `location` VALUES('kpih', 'Pocatello, ID, US', GeomFromText('POINT(-112.571 +42.920)'), +1357.0, 0, 65, 'Pocatello, Pocatello Regional Airport, ID, US');
INSERT INTO `location` VALUES('kpir', 'Pierre, SD, US', GeomFromText('POINT(-100.286 +44.381)'), +0531.0, 0, 65, 'Pierre, Pierre Regional Airport, SD, US');
INSERT INTO `location` VALUES('kprc', 'Prescott, AZ, US', GeomFromText('POINT(-112.421 +34.652)'), +1536.8, 0, 65, 'Prescott, Love Field, AZ, US');
INSERT INTO `location` VALUES('kpsp', 'Palm Springs, CA, US', GeomFromText('POINT(-116.504 +33.822)'), +0124.7, 0, 65, 'Palm Springs, Palm Springs Regional Airport, CA, US');
INSERT INTO `location` VALUES('kpub', 'Pueblo, CO, US', GeomFromText('POINT(-104.506 +38.289)'), +1441.4, 0, 65, 'Pueblo, Pueblo Memorial Airport, CO, US');
INSERT INTO `location` VALUES('kpvd', 'Providence, RI, US', GeomFromText('POINT(-071.433 +41.723)'), +0016.8, 0, 65, 'Providence, Theodore Francis Green State Airport, RI, US');
INSERT INTO `location` VALUES('kpwm', 'Portland, ME, US', GeomFromText('POINT(-070.304 +43.642)'), +0013.7, 0, 65, 'Portland, Portland International Jetport, ME, US');
INSERT INTO `location` VALUES('krdd', 'Redding, CA, US', GeomFromText('POINT(-122.299 +40.518)'), +0151.5, 0, 65, 'Redding, Redding Municipal Airport, CA, US');
INSERT INTO `location` VALUES('kroa', 'Roanoke, VA, US', GeomFromText('POINT(-079.974 +37.317)'), +0358.1, 0, 65, 'Roanoke, Roanoke Regional Airport, VA, US');
INSERT INTO `location` VALUES('kshv', 'Shreveport, LA, US', GeomFromText('POINT(-093.824 +32.447)'), +0077.4, 0, 65, 'Shreveport, Shreveport Regional Airport, LA, US');
INSERT INTO `location` VALUES('ksmp', 'Stampede Pass, WA, US', GeomFromText('POINT(-121.337 +47.277)'), +1206.7, 0, 65, 'Stampede Pass, WA, US');
INSERT INTO `location` VALUES('kstc', 'St Cloud, MN, US', GeomFromText('POINT(-094.051 +45.543)'), +0310.3, 0, 65, 'St Cloud, St Cloud Regional Airport, MN, US');
INSERT INTO `location` VALUES('ksus', 'St. Louis, MO, US', GeomFromText('POINT(-090.656 +38.657)'), +0140.8, 0, 65, 'St. Louis, Spirit Of St. Louis Airport, MO, US');
INSERT INTO `location` VALUES('ksux', 'Sioux City, IA, US', GeomFromText('POINT(-096.379 +42.391)'), +0333.8, 0, 65, 'Sioux City, Sioux Gateway Airport, IA, US');
INSERT INTO `location` VALUES('ksyr', 'Syracuse, NY, US', GeomFromText('POINT(-076.104 +43.111)'), +0125.9, 0, 65, 'Syracuse, Syracuse Hancock International Airport, NY, US');
INSERT INTO `location` VALUES('ktlh', 'Tallahassee, FL, US', GeomFromText('POINT(-084.353 +30.393)'), +0016.8, 0, 65, 'Tallahassee, Tallahassee Regional Airport, FL, US');
INSERT INTO `location` VALUES('ktop', 'Topeka, KS, US', GeomFromText('POINT(-095.626 +39.073)'), +0267.0, 0, 65, 'Topeka, Philip Billard Municipal Airport, KS, US');
INSERT INTO `location` VALUES('ktpa', 'Tampa, FL, US', GeomFromText('POINT(-082.540 +27.962)'), +0005.8, 0, 65, 'Tampa, Tampa International Airport, FL, US');
INSERT INTO `location` VALUES('ktul', 'Tulsa, OK, US', GeomFromText('POINT(-095.887 +36.199)'), +0198.1, 0, 65, 'Tulsa, Tulsa International Airport, OK, US');
INSERT INTO `location` VALUES('kunv', 'State College, PA, US', GeomFromText('POINT(-077.850 +40.850)'), +0377.7, 0, 65, 'State College, University Park Airport, PA, US');
INSERT INTO `location` VALUES('kvbg', 'Lompoc, CA, US', GeomFromText('POINT(-120.567 +34.717)'), +0112.5, 0, 65, 'Lompoc, Vandenberg Air Force Base, CA, US');
INSERT INTO `location` VALUES('kwmc', 'Winnemucca, NV, US', GeomFromText('POINT(-117.808 +40.902)'), +1309.4, 0, 65, 'Winnemucca, Winnemucca Municipal Airport, NV, US');
INSERT INTO `location` VALUES('kykm', 'Yakima, WA, US', GeomFromText('POINT(-120.543 +46.568)'), +0324.3, 0, 65, 'Yakima, Yakima Air Terminal, WA, US');
INSERT INTO `location` VALUES('kabi', 'Abilene, TX, US', GeomFromText('POINT(-099.682 +32.411)'), +0545.6, 0, 60, 'Abilene, Abilene Regional Airport, TX, US');
INSERT INTO `location` VALUES('kacy', 'Atlantic City, NJ, US', GeomFromText('POINT(-074.567 +39.452)'), +0018.3, 0, 60, 'Atlantic City, Atlantic City International Airport, NJ, US');
INSERT INTO `location` VALUES('kadm', 'Ardmore, OK, US', GeomFromText('POINT(-097.017 +34.300)'), +0221.0, 0, 60, 'Ardmore, Ardmore Municipal Airport, OK, US');
INSERT INTO `location` VALUES('kaff', 'Air Force Academy, CO, US', GeomFromText('POINT(-104.817 +38.967)'), +2003.2, 0, 60, 'Air Force Academy, CO, US');
INSERT INTO `location` VALUES('kags', 'Augusta, GA, US', GeomFromText('POINT(-081.963 +33.364)'), +0040.2, 0, 60, 'Augusta, Bush Field, GA, US');
INSERT INTO `location` VALUES('kahn', 'Athens, GA, US', GeomFromText('POINT(-083.328 +33.948)'), +0239.3, 0, 60, 'Athens, Athens Airport, GA, US');
INSERT INTO `location` VALUES('kako', 'Akron, CO, US', GeomFromText('POINT(-103.217 +40.167)'), +1421.3, 0, 60, 'Akron, Akron-Washington County Airport, CO, US');
INSERT INTO `location` VALUES('kalo', 'Waterloo, IA, US', GeomFromText('POINT(-092.401 +42.554)'), +0264.6, 0, 60, 'Waterloo, Waterloo Municipal Airport, IA, US');
INSERT INTO `location` VALUES('kapa', 'Denver, CO, US', GeomFromText('POINT(-104.849 +39.570)'), +1793.1, 0, 60, 'Denver, Centennial Airport, CO, US');
INSERT INTO `location` VALUES('kapn', 'Alpena, MI, US', GeomFromText('POINT(-083.564 +45.072)'), +0208.5, 0, 60, 'Alpena, Alpena County Regional Airport, MI, US');
INSERT INTO `location` VALUES('kase', 'Aspen, CO, US', GeomFromText('POINT(-106.871 +39.230)'), +2353.1, 0, 60, 'Aspen, Aspen-Pitkin County Airport, CO, US');
INSERT INTO `location` VALUES('kast', 'Astoria, OR, US', GeomFromText('POINT(-123.883 +46.157)'), +0002.7, 0, 60, 'Astoria, Astoria Regional Airport, OR, US');
INSERT INTO `location` VALUES('kaug', 'Augusta, ME, US', GeomFromText('POINT(-069.797 +44.316)'), +0107.0, 0, 60, 'Augusta, Augusta State Airport, ME, US');
INSERT INTO `location` VALUES('kavl', 'Asheville, NC, US', GeomFromText('POINT(-082.538 +35.432)'), +0645.3, 0, 60, 'Asheville, Asheville Regional Airport, NC, US');
INSERT INTO `location` VALUES('kbhm', 'Birmingham, AL, US', GeomFromText('POINT(-086.745 +33.566)'), +0187.5, 0, 60, 'Birmingham, Birmingham International Airport, AL, US');
INSERT INTO `location` VALUES('kbrd', 'Brainerd, MN, US', GeomFromText('POINT(-094.131 +46.405)'), +0372.2, 0, 60, 'Brainerd, Brainerd-Crow Wing County Regional Airport, MN, US');
INSERT INTO `location` VALUES('kbtr', 'Baton Rouge, LA, US', GeomFromText('POINT(-091.147 +30.537)'), +0019.5, 0, 60, 'Baton Rouge, Baton Rouge Metropolitan, Ryan Field, LA, US');
INSERT INTO `location` VALUES('kbwg', 'Bowling Green, KY, US', GeomFromText('POINT(-086.424 +36.965)'), +0160.9, 0, 60, 'Bowling Green, Bowling Green-Warren County Regional Airport, KY, US');
INSERT INTO `location` VALUES('kbzn', 'Bozeman, MT, US', GeomFromText('POINT(-111.161 +45.788)'), +1349.4, 0, 60, 'Bozeman, Gallatin Field, MT, US');
INSERT INTO `location` VALUES('kcag', 'Craig, CO, US', GeomFromText('POINT(-107.524 +40.493)'), +1886.7, 0, 60, 'Craig, Craig-Moffat Airport, CO, US');
INSERT INTO `location` VALUES('kcdc', 'Cedar City, UT, US', GeomFromText('POINT(-113.094 +37.709)'), +1702.6, 0, 60, 'Cedar City, Cedar City Municipal Airport, UT, US');
INSERT INTO `location` VALUES('kcha', 'Chattanooga, TN, US', GeomFromText('POINT(-085.200 +35.034)'), +0204.2, 0, 60, 'Chattanooga, Lovell Field, TN, US');
INSERT INTO `location` VALUES('kcid', 'Cedar Rapids, IA, US', GeomFromText('POINT(-091.717 +41.883)'), +0264.6, 0, 60, 'Cedar Rapids, The Eastern Iowa Airport, IA, US');
INSERT INTO `location` VALUES('kckb', 'Clarksburg, WV, US', GeomFromText('POINT(-080.229 +39.296)'), +0366.7, 0, 60, 'Clarksburg, Clarksburg Benedum Airport, WV, US');
INSERT INTO `location` VALUES('kcll', 'College Station, TX, US', GeomFromText('POINT(-096.365 +30.589)'), +0093.0, 0, 60, 'College Station, Easterwood Field, TX, US');
INSERT INTO `location` VALUES('kclt', 'Charlotte, NC, US', GeomFromText('POINT(-080.955 +35.224)'), +0221.9, 0, 60, 'Charlotte, Charlotte / Douglas International Airport, NC, US');
INSERT INTO `location` VALUES('kcmh', 'Columbus, OH, US', GeomFromText('POINT(-082.877 +39.991)'), +0248.7, 0, 60, 'Columbus, Port Columbus International Airport, OH, US');
INSERT INTO `location` VALUES('kcoe', 'Coeur d''Alene, ID, US', GeomFromText('POINT(-116.817 +47.767)'), +0703.2, 0, 60, 'Coeur d''Alene, Coeur d''Alene Air Terminal, ID, US');
INSERT INTO `location` VALUES('kcou', 'Columbia, MO, US', GeomFromText('POINT(-092.218 +38.817)'), +0272.2, 0, 60, 'Columbia, Columbia Regional Airport, MO, US');
INSERT INTO `location` VALUES('kctb', 'Cut Bank, MT, US', GeomFromText('POINT(-112.375 +48.603)'), +1169.8, 0, 60, 'Cut Bank, Cut Bank Municipal Airport, MT, US');
INSERT INTO `location` VALUES('kcvs', 'Cannon Air Force Base / Clovis, NM, US', GeomFromText('POINT(-103.317 +34.383)'), +1309.1, 0, 60, 'Cannon Air Force Base / Clovis, NM, US');
INSERT INTO `location` VALUES('kday', 'Dayton, OH, US', GeomFromText('POINT(-084.219 +39.906)'), +0305.7, 0, 60, 'Dayton, Cox Dayton International Airport, OH, US');
INSERT INTO `location` VALUES('kdbq', 'Dubuque, IA, US', GeomFromText('POINT(-090.704 +42.398)'), +0321.9, 0, 60, 'Dubuque, Dubuque Regional Airport, IA, US');
INSERT INTO `location` VALUES('kdec', 'Decatur, IL, US', GeomFromText('POINT(-088.866 +39.834)'), +0205.7, 0, 60, 'Decatur, Decatur Airport, IL, US');
INSERT INTO `location` VALUES('kdro', 'Durango, CO, US', GeomFromText('POINT(-107.760 +37.143)'), +2033.0, 0, 60, 'Durango, Durango-La Plata County Airport, CO, US');
INSERT INTO `location` VALUES('keau', 'Eau Claire, WI, US', GeomFromText('POINT(-091.488 +44.867)'), +0269.8, 0, 60, 'Eau Claire, Chippewa Valley Regional Airport, WI, US');
INSERT INTO `location` VALUES('kekn', 'Elkins, WV, US', GeomFromText('POINT(-079.855 +38.890)'), +0597.7, 0, 60, 'Elkins, Elkins-Randolph County-Jennings Randolph Field, WV, US');
INSERT INTO `location` VALUES('kend', 'Vance Air Force Base / Enid, OK, US', GeomFromText('POINT(-097.917 +36.333)'), +0398.1, 0, 60, 'Vance Air Force Base / Enid, OK, US');
INSERT INTO `location` VALUES('kenv', 'Wendover / Air Force Auxillary Field, UT, US', GeomFromText('POINT(-114.036 +40.721)'), +1291.4, 0, 60, 'Wendover / Air Force Auxillary Field, UT, US');
INSERT INTO `location` VALUES('keri', 'Erie, PA, US', GeomFromText('POINT(-080.182 +42.080)'), +0222.2, 0, 60, 'Erie, Erie International Airport, PA, US');
INSERT INTO `location` VALUES('kevv', 'Evansville, IN, US', GeomFromText('POINT(-087.521 +38.044)'), +0121.9, 0, 60, 'Evansville, Evansville Regional Airport, IN, US');
INSERT INTO `location` VALUES('kfay', 'Fayetteville, NC, US', GeomFromText('POINT(-078.880 +34.991)'), +0056.7, 0, 60, 'Fayetteville, Fayetteville Regional Airport, NC, US');
INSERT INTO `location` VALUES('kfnt', 'Flint, MI, US', GeomFromText('POINT(-083.749 +42.967)'), +0234.7, 0, 60, 'Flint, Bishop International Airport, MI, US');
INSERT INTO `location` VALUES('kfyv', 'Fayetteville, AR, US', GeomFromText('POINT(-094.169 +36.010)'), +0381.3, 0, 60, 'Fayetteville, Drake Field, AR, US');
INSERT INTO `location` VALUES('kgck', 'Garden City, KS, US', GeomFromText('POINT(-100.725 +37.927)'), +0878.4, 0, 60, 'Garden City, Garden City Regional Airport, KS, US');
INSERT INTO `location` VALUES('kgcn', 'Grand Canyon, AZ, US', GeomFromText('POINT(-112.155 +35.946)'), +2013.5, 0, 60, 'Grand Canyon, Grand Canyon National Park Airport, AZ, US');
INSERT INTO `location` VALUES('kggg', 'Longview, TX, US', GeomFromText('POINT(-094.712 +32.385)'), +0111.3, 0, 60, 'Longview, Gregg County Airport, TX, US');
INSERT INTO `location` VALUES('kgnv', 'Gainesville, FL, US', GeomFromText('POINT(-082.276 +29.692)'), +0037.5, 0, 60, 'Gainesville, Gainesville Regional Airport, FL, US');
INSERT INTO `location` VALUES('kgwo', 'Greenwood, MS, US', GeomFromText('POINT(-090.087 +33.496)'), +0040.5, 0, 60, 'Greenwood, Greenwood-LeFlore Airport, MS, US');
INSERT INTO `location` VALUES('kgxy', 'Greeley, CO, US', GeomFromText('POINT(-104.632 +40.436)'), +1431.7, 0, 60, 'Greeley, Greeley-Weld County Airport, CO, US');
INSERT INTO `location` VALUES('kgyy', 'Gary Regional, IN, US', GeomFromText('POINT(-087.417 +41.617)'), +0180.1, 0, 60, 'Gary Regional, IN, US');
INSERT INTO `location` VALUES('khfd', 'Hartford, CT, US', GeomFromText('POINT(-072.651 +41.736)'), +0005.8, 0, 60, 'Hartford, Hartford-Brainard Airport, CT, US');
INSERT INTO `location` VALUES('khln', 'Helena, MT, US', GeomFromText('POINT(-111.964 +46.606)'), +1166.8, 0, 60, 'Helena, Helena Regional Airport, MT, US');
INSERT INTO `location` VALUES('khmn', 'Holloman Air Force Base, NM, US', GeomFromText('POINT(-106.100 +32.850)'), +1267.4, 0, 60, 'Holloman Air Force Base, NM, US');
INSERT INTO `location` VALUES('khqm', 'Hoquiam, WA, US', GeomFromText('POINT(-123.930 +46.973)'), +0003.7, 0, 60, 'Hoquiam, Bowerman Airport, WA, US');
INSERT INTO `location` VALUES('khsv', 'Huntsville, AL, US', GeomFromText('POINT(-086.786 +34.644)'), +0190.2, 0, 60, 'Huntsville, Huntsville International / Jones Field, AL, US');
INSERT INTO `location` VALUES('khys', 'Hays, KS, US', GeomFromText('POINT(-099.267 +38.850)'), +0609.0, 0, 60, 'Hays, Hays Regional Airport, KS, US');
INSERT INTO `location` VALUES('kiag', 'Niagara Falls, NY, US', GeomFromText('POINT(-078.938 +43.108)'), +0178.3, 0, 60, 'Niagara Falls, Niagara Falls International Airport, NY, US');
INSERT INTO `location` VALUES('kilg', 'Wilmington, DE, US', GeomFromText('POINT(-075.606 +39.674)'), +0024.1, 0, 60, 'Wilmington, New Castle County Airport, DE, US');
INSERT INTO `location` VALUES('kint', 'Winston Salem, NC, US', GeomFromText('POINT(-080.222 +36.134)'), +0295.7, 0, 60, 'Winston Salem, Smith Reynolds Airport, NC, US');
INSERT INTO `location` VALUES('kinw', 'Winslow, AZ, US', GeomFromText('POINT(-110.721 +35.028)'), +1489.3, 0, 60, 'Winslow, Winslow Municipal Airport, AZ, US');
INSERT INTO `location` VALUES('kisp', 'Islip, NY, US', GeomFromText('POINT(-073.102 +40.794)'), +0025.6, 0, 60, 'Islip, Long Island Mac Arthur Airport, NY, US');
INSERT INTO `location` VALUES('klaa', 'Lamar, CO, US', GeomFromText('POINT(-102.688 +38.070)'), +1129.0, 0, 60, 'Lamar, Lamar Municipal Airport, CO, US');
INSERT INTO `location` VALUES('klaf', 'Lafayette, IN, US', GeomFromText('POINT(-086.937 +40.412)'), +0182.6, 0, 60, 'Lafayette, Purdue University Airport, IN, US');
INSERT INTO `location` VALUES('klam', 'Los Alamos, NM, US', GeomFromText('POINT(-106.283 +35.883)'), +2185.7, 0, 60, 'Los Alamos, Los Alamos Airport, NM, US');
INSERT INTO `location` VALUES('klan', 'Lansing, MI, US', GeomFromText('POINT(-084.600 +42.776)'), +0261.8, 0, 60, 'Lansing, Capital City Airport, MI, US');
INSERT INTO `location` VALUES('klbl', 'Liberal, KS, US', GeomFromText('POINT(-100.967 +37.050)'), +0875.7, 0, 60, 'Liberal, Liberal Municipal Airport, KS, US');
INSERT INTO `location` VALUES('klex', 'Lexington, KY, US', GeomFromText('POINT(-084.606 +38.041)'), +0298.7, 0, 60, 'Lexington, Blue Grass Airport, KY, US');
INSERT INTO `location` VALUES('klic', 'Limon, CO, US', GeomFromText('POINT(-103.666 +39.275)'), +1638.0, 0, 60, 'Limon, Limon Municipal Airport, CO, US');
INSERT INTO `location` VALUES('kmgm', 'Montgomery, AL, US', GeomFromText('POINT(-086.408 +32.300)'), +0061.6, 0, 60, 'Montgomery, Dannelly Field, AL, US');
INSERT INTO `location` VALUES('kmhk', 'Manhattan, KS, US', GeomFromText('POINT(-096.679 +39.135)'), +0321.9, 0, 60, 'Manhattan, Manhattan Municipal Airport, KS, US');
INSERT INTO `location` VALUES('kmkg', 'Muskegon, MI, US', GeomFromText('POINT(-086.237 +43.171)'), +0190.5, 0, 60, 'Muskegon, Muskegon County Airport, MI, US');
INSERT INTO `location` VALUES('kmlb', 'Melbourne, FL, US', GeomFromText('POINT(-080.644 +28.101)'), +0008.2, 0, 60, 'Melbourne, Melbourne International Airport, FL, US');
INSERT INTO `location` VALUES('kmls', 'Miles City, MT, US', GeomFromText('POINT(-105.883 +46.427)'), +0799.8, 0, 60, 'Miles City, Frank Wiley Field Airport, MT, US');
INSERT INTO `location` VALUES('kmpv', 'Barre / Montpelier, VT, US', GeomFromText('POINT(-072.562 +44.204)'), +0343.2, 0, 60, 'Barre / Montpelier, Knapp State Airport, VT, US');
INSERT INTO `location` VALUES('kmrb', 'Martinsburg, WV, US', GeomFromText('POINT(-077.945 +39.404)'), +0162.8, 0, 60, 'Martinsburg, Eastern West Virginia Regional/Shepherd Airport, WV, US');
INSERT INTO `location` VALUES('kmsn', 'Madison, WI, US', GeomFromText('POINT(-089.345 +43.141)'), +0264.0, 0, 60, 'Madison, Dane County Regional-Truax Field, WI, US');
INSERT INTO `location` VALUES('knid', 'China Lake, CA, US', GeomFromText('POINT(-117.693 +35.688)'), +0679.7, 0, 60, 'China Lake, Naval Air Facility, CA, US');
INSERT INTO `location` VALUES('koff', 'Omaha / Offutt Air Force Base, NE, US', GeomFromText('POINT(-095.917 +41.117)'), +0319.1, 0, 60, 'Omaha / Offutt Air Force Base, NE, US');
INSERT INTO `location` VALUES('kpia', 'Peoria, IL, US', GeomFromText('POINT(-089.684 +40.668)'), +0198.1, 0, 60, 'Peoria, Greater Peoria Regional Airport, IL, US');
INSERT INTO `location` VALUES('kpkd', 'Park Rapids, MN, US', GeomFromText('POINT(-095.068 +46.901)'), +0437.1, 0, 60, 'Park Rapids, Park Rapids Municipal Airport, MN, US');
INSERT INTO `location` VALUES('kpne', 'Philadelphia, PA, US', GeomFromText('POINT(-075.013 +40.079)'), +0032.0, 0, 60, 'Philadelphia, Northeast Philadelphia Airport, PA, US');
INSERT INTO `location` VALUES('kpoe', 'Fort Polk, LA, US', GeomFromText('POINT(-093.183 +31.050)'), +0100.6, 0, 60, 'Fort Polk, Polk AAF Ft Polk, LA, US');
INSERT INTO `location` VALUES('kpou', 'Poughkeepsie, NY, US', GeomFromText('POINT(-073.882 +41.626)'), +0050.6, 0, 60, 'Poughkeepsie, Dutchess County Airport, NY, US');
INSERT INTO `location` VALUES('krfd', 'Rockford, IL, US', GeomFromText('POINT(-089.093 +42.193)'), +0222.5, 0, 60, 'Rockford, Greater Rockford Airport, IL, US');
INSERT INTO `location` VALUES('kric', 'Richmond, VA, US', GeomFromText('POINT(-077.323 +37.512)'), +0050.0, 0, 60, 'Richmond, Richmond International Airport, VA, US');
INSERT INTO `location` VALUES('krow', 'Roswell, NM, US', GeomFromText('POINT(-104.508 +33.308)'), +1112.2, 0, 60, 'Roswell, Roswell Industrial Air Center Airport, NM, US');
INSERT INTO `location` VALUES('ksav', 'Savannah, GA, US', GeomFromText('POINT(-081.202 +32.131)'), +0014.0, 0, 60, 'Savannah, Savannah International Airport, GA, US');
INSERT INTO `location` VALUES('ksbn', 'South Bend, IN, US', GeomFromText('POINT(-086.316 +41.707)'), +0235.6, 0, 60, 'South Bend, South Bend Regional Airport, IN, US');
INSERT INTO `location` VALUES('ksby', 'Salisbury, MD, US', GeomFromText('POINT(-075.513 +38.341)'), +0014.3, 0, 60, 'Salisbury, Salisbury-Ocean City Wicomico County Regional Airport, MD, US');
INSERT INTO `location` VALUES('ksps', 'Wichita Falls, TX, US', GeomFromText('POINT(-098.493 +33.979)'), +0310.0, 0, 60, 'Wichita Falls, Sheppard Air Force Base, TX, US');
INSERT INTO `location` VALUES('kstj', 'St. Joseph, MO, US', GeomFromText('POINT(-094.923 +39.774)'), +0249.3, 0, 60, 'St. Joseph, Rosecrans Memorial Airport, MO, US');
INSERT INTO `location` VALUES('ktri', 'Bristol / Johnson / Kingsport, TN, US', GeomFromText('POINT(-082.399 +36.480)'), +0456.3, 0, 60, 'Bristol / Johnson / Kingsport, Tri-City Regional Airport, TN, US');
INSERT INTO `location` VALUES('ktup', 'Tupelo, MS, US', GeomFromText('POINT(-088.771 +34.262)'), +0110.0, 0, 60, 'Tupelo, Tupelo Regional Airport, MS, US');
INSERT INTO `location` VALUES('ktyr', 'Tyler, TX, US', GeomFromText('POINT(-095.403 +32.354)'), +0165.8, 0, 60, 'Tyler, Tyler Pounds Field, TX, US');
INSERT INTO `location` VALUES('ktys', 'Knoxville, TN, US', GeomFromText('POINT(-083.986 +35.818)'), +0293.2, 0, 60, 'Knoxville, McGhee Tyson Airport, TN, US');
INSERT INTO `location` VALUES('kvrb', 'Vero Beach, FL, US', GeomFromText('POINT(-080.420 +27.651)'), +0008.5, 0, 60, 'Vero Beach, Vero Beach Municipal Airport, FL, US');
INSERT INTO `location` VALUES('kakq', 'Wakefield, VA, US', GeomFromText('POINT(-077.001 +36.983)'), +0033.8, 0, 55, 'Wakefield, Wakefield Municipal Airport, VA, US');
INSERT INTO `location` VALUES('kawm', 'West Memphis, AR, US', GeomFromText('POINT(-090.234 +35.135)'), +0065.2, 0, 55, 'West Memphis, West Memphis Municipal Airport, AR, US');
INSERT INTO `location` VALUES('kdra', 'Mercury, NV, US', GeomFromText('POINT(-116.028 +36.621)'), +0984.5, 0, 55, 'Mercury, Desert Rock Airport, NV, US');
INSERT INTO `location` VALUES('kfdr', 'Frederick, OK, US', GeomFromText('POINT(-098.983 +34.344)'), +0382.5, 0, 55, 'Frederick, Frederick Municipal Airport, OK, US');
INSERT INTO `location` VALUES('kgmu', 'Greenville, SC, US', GeomFromText('POINT(-082.346 +34.846)'), +0319.4, 0, 55, 'Greenville, Greenville Downtown Airport, SC, US');
INSERT INTO `location` VALUES('kgrk', 'Fort Hood, TX, US', GeomFromText('POINT(-097.833 +31.067)'), +0309.4, 0, 55, 'Fort Hood, Robert Gray AAF Ft Hood, TX, US');
INSERT INTO `location` VALUES('kjkl', 'Jackson, KY, US', GeomFromText('POINT(-083.314 +37.591)'), +0416.1, 0, 55, 'Jackson, Carroll Airport, KY, US');
INSERT INTO `location` VALUES('klot', 'Chicago/Romeoville, IL, US', GeomFromText('POINT(-088.085 +41.604)'), +0201.2, 0, 55, 'Chicago/Romeoville, Lewis University Airport, IL, US');
INSERT INTO `location` VALUES('kmaf', 'Midland, TX, US', GeomFromText('POINT(-102.209 +31.948)'), +0872.3, 0, 55, 'Midland, Midland International Airport, TX, US');
INSERT INTO `location` VALUES('knkx', 'San Diego, CA, US', GeomFromText('POINT(-117.133 +32.867)'), +0145.4, 0, 55, 'San Diego, Miramar MCAS/Mitscher Field Airport, CA, US');
INSERT INTO `location` VALUES('knqa', 'Millington, TN, US', GeomFromText('POINT(-089.867 +35.350)'), +0097.5, 0, 55, 'Millington, Millington Municipal Airport, TN, US');
INSERT INTO `location` VALUES('kpna', 'Pinedale, WY, US', GeomFromText('POINT(-109.807 +42.796)'), +2159.8, 0, 55, 'Pinedale, Ralph Wenz Field Airport, WY, US');
INSERT INTO `location` VALUES('ksgf', 'Springfield, MO, US', GeomFromText('POINT(-093.390 +37.240)'), +0384.7, 0, 55, 'Springfield, Springfield Regional Airport, MO, US');
INSERT INTO `location` VALUES('ksjt', 'San Angelo, TX, US', GeomFromText('POINT(-100.495 +31.352)'), +0584.0, 0, 55, 'San Angelo, Mathis Field, TX, US');
INSERT INTO `location` VALUES('kwal', 'Wallops Island, VA, US', GeomFromText('POINT(-075.466 +37.937)'), +0014.0, 0, 55, 'Wallops Island, Wallops Flight Facility Airport, VA, US');
INSERT INTO `location` VALUES('kelm', 'Elmira, NY, US', GeomFromText('POINT(-076.892 +42.159)'), +0291.1, 0, 50, 'Elmira, Elmira / Corning Regional Airport, NY, US');
INSERT INTO `location` VALUES('kith', 'Ithaca, NY, US', GeomFromText('POINT(-076.467 +42.483)'), +0335.0, 0, 50, 'Ithaca, Ithaca Tompkins Regional Airport, NY, US');
INSERT INTO `location` VALUES('kroc', 'Rochester, NY, US', GeomFromText('POINT(-077.677 +43.117)'), +0164.3, 0, 50, 'Rochester, Greater Rochester International Airport, NY, US');

-- `sensor`: per-sensor-ID display metadata (Description/Property/Unit), read by
-- ~/Public/html/weather/json/sensors.php's JOIN to render report.php's "Sensors" table.
-- Schema and row content copied from the live larsi-weather.sensor table (SHOW CREATE TABLE /
-- SELECT *), with `ID` remapped from the old measured/measured+1 scheme to the new
-- measured(0-6)/measured+16 scheme. Count/DateTimeMin/DateTimeMax/ValueMin/ValueMax are all 0 in
-- the source table too — sensors.php computes those live via its own JOIN aggregate, it never
-- reads the stored columns, so they're carried over as inert placeholders for schema parity only.
-- Excludes the source table's "Weather Condition" rows (old IDs 13/14): GeoNames2 has no sensor
-- slot for that field at all (removed entirely, not just unpopulated — see GeoNames2.kt/README.md
-- sensor ID table), so there is no corresponding channel to describe in larsi-weather2.
DROP TABLE IF EXISTS `sensor`;
CREATE TABLE `sensor` (
  `Prefix` varchar(16) NOT NULL,
  `ID` smallint(6) NOT NULL,
  `Description` varchar(64) NOT NULL,
  `Sensor` varchar(32) NOT NULL,
  `Property` varchar(32) NOT NULL,
  `Unit` varchar(16) NOT NULL,
  `Count` int(11) NOT NULL DEFAULT 0,
  `DateTimeMin` int(11) NOT NULL DEFAULT 0,
  `DateTimeMax` int(11) NOT NULL DEFAULT 0,
  `ValueMin` float NOT NULL DEFAULT 0,
  `ValueMax` float NOT NULL DEFAULT 0,
  `ZeusMinutes` int(11) NOT NULL DEFAULT 0,
  `ZeusSuccessful` tinyint(1) NOT NULL DEFAULT 0,
  `Comment` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT INTO `sensor` VALUES('ICAO', 0, 'Temperature (measured)', 'ICAO', 'Temperature', 'C', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 16, 'Temperature (predicted)', 'ICAO', 'Temperature', 'C', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 1, 'Dew Point Temperature (measured)', 'ICAO', 'Dew Point Temperature', 'C', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 17, 'Dew Point Temperature (predicted)', 'ICAO', 'Dew Point Temperature', 'C', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 2, 'Relative Humidity (measured)', 'ICAO', 'Relative Humidity', '%', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 18, 'Relative Humidity (predicted)', 'ICAO', 'Relative Humidity', '%', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 3, 'Pressure (measured)', 'ICAO', 'Pressure', 'hPa', 0, 0, 0, 0, 0, 0, 0, 'At sea level');
INSERT INTO `sensor` VALUES('ICAO', 19, 'Pressure (predicted)', 'ICAO', 'Pressure', 'hPa', 0, 0, 0, 0, 0, 0, 0, 'At sea level');
INSERT INTO `sensor` VALUES('ICAO', 4, 'Wind Direction (measured)', 'ICAO', 'Wind Direction', 'deg', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 20, 'Wind Direction (predicted)', 'ICAO', 'Wind Direction', 'deg', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 5, 'Wind Speed (measured)', 'ICAO', 'Wind Speed', 'm/s', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 21, 'Wind Speed (predicted)', 'ICAO', 'Wind Speed', 'm/s', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 6, 'Clouds (measured)', 'ICAO', 'Clouds', '10th', 0, 0, 0, 0, 0, 0, 0, '');
INSERT INTO `sensor` VALUES('ICAO', 22, 'Clouds (predicted)', 'ICAO', 'Clouds', '10th', 0, 0, 0, 0, 0, 0, 0, '');

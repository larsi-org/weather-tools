package org.larsi.dev

import org.larsi.util.Icao

object CreateDB
{
	val countryNames = mapOf(
		"  " to "?",
		"AA" to "Aruba",
		"AC" to "Antigua and Barbuda",
		"AF" to "Afghanistan",
		"AG" to "Algeria",
		"AI" to "Ascension Island",
		"AJ" to "Azerbaijan",
		"AL" to "Albania",
		"AM" to "Armenia",
		"AN" to "andorra",
		"AO" to "Angola",
		"AQ" to "American Samoa",
		"AR" to "Argentina",
		"AS" to "Australia",
		"AT" to "Ashmore and Cartier Islands",
		"AU" to "Austria",
		"AV" to "Anguilla",
		"AX" to "Antigua, St. Kitts, Nevis, Barbuda",
		"AY" to "Antarctica",
		"AZ" to "Azores",
		"BA" to "Bahrain",
		"BB" to "Barbados",
		"BC" to "Botswana",
		"BD" to "Bermuda",
		"BE" to "Belgium",
		"BF" to "The Bahamas",
		"BG" to "Bangladesh",
		"BH" to "Belize",
		"BT" to "Bhutan",
		"BK" to "Bosnia and Herzegovina",
		"BL" to "Bolivia",
		"BM" to "Burma",
		"BN" to "Benin",
		"BO" to "Belarus",
		"BP" to "Solomon Islands",
		"BQ" to "Navassa Island",
		"BR" to "Brazil",
		"BS" to "Bassas Da India",
		"BT" to "Bhutan",
		"BU" to "Bulgaria",
		"BV" to "Bouvet Island",
		"BX" to "Brunei",
		"BY" to "Burundi",
		"BZ" to "Belgium and Luxembourg",
		"CA" to "Canada",
		"CB" to "Cambodia",
		"CC" to "Ceuta and Melilla",
		"CD" to "Chad",
		"CE" to "Sri Lanka",
		"CF" to "Congo",
		"CG" to "Zaire",
		"CH" to "China",
		"CI" to "Chile",
		"CJ" to "Cayman Islands",
		"CK" to "Cocos (Keeling) Islands",
		"CL" to "Caroline Islands",
		"CM" to "Cameroon",
		"CN" to "Comoros",
		"CO" to "Colombia",
		"CP" to "Canary Islands",
		"CQ" to "Northern Mariana Islands",
		"CR" to "Coral Sea Islands",
		"CS" to "Costa Rica",
		"CT" to "Central African Republic",
		"CU" to "Cuba",
		"CV" to "Cape Verde",
		"CW" to "Cook Islands",
		"CY" to "Cyprus",
		"CZ" to "Canton Island",
		"DA" to "Denmark",
		"DJ" to "Djibouti",
		"DO" to "Dominica",
		"DQ" to "Jarvis Island",
		"DR" to "Dominican Republic",
		"DY" to "Democratic Yemen",
		"EC" to "Ecuador",
		"EG" to "Egypt",
		"EI" to "Ireland",
		"EK" to "Equatorial Guinea",
		"EN" to "Estonia",
		"ER" to "Eritrea",
		"ES" to "El Salvador",
		"ET" to "Ethiopia",
		"EU" to "Europa Island",
		"EZ" to "Czech Republic",
		"FG" to "French Guiana",
		"FI" to "Finland",
		"FJ" to "Fiji",
		"FK" to "Falkland Islands (Islas Malvinas)",
		"FM" to "Federated States Of Micronesia",
		"FO" to "Faroe Islands",
		"FP" to "French Polynesia",
		"FQ" to "Baker Island",
		"FR" to "France",
		"FS" to "French Southern and Antarctic Lands",
		"GA" to "The Gambia",
		"GB" to "Gabon",
		"GG" to "Georgia",
		"GH" to "Ghana",
		"GI" to "Gibraltar",
		"GJ" to "Grenada",
		"GK" to "Guernsey",
		"GL" to "Greenland",
		"GM" to "Germany",
		"GO" to "Glorioso Islands",
		"GP" to "Guadeloupe",
		"GQ" to "Guam",
		"GR" to "Greece",
		"GT" to "Guatemala",
		"GV" to "Guinea",
		"GY" to "Guyana",
		"GZ" to "Gaza Strip",
		"HA" to "Haiti",
		"HK" to "Hong Kong",
		"HM" to "Heard Island and McDonald Islands",
		"HO" to "Honduras",
		"HQ" to "Howland Island",
		"HR" to "Croatia",
		"HU" to "Hungary",
		"IC" to "Iceland",
		"ID" to "Indonesia",
		"IM" to "Isle Of Man",
		"IN" to "India",
		"IO" to "British Indian Ocean Territory",
		"IP" to "Clipperton Island",
		"IR" to "Iran",
		"IS" to "Israel",
		"IT" to "Italy",
		"IV" to "Cote d'Ivoire",
		"IW" to "Israel-Jordan Dmz",
		"IZ" to "Iraq",
		"JA" to "Japan",
		"JE" to "Jersey",
		"JM" to "Jamaica",
		"JN" to "Jan Mayen",
		"JO" to "Jordan",
		"JQ" to "Johnston Atoll",
		"JU" to "Juan De Nova Island",
		"KE" to "Kenya",
		"KG" to "Kyrgyzstan",
		"KN" to "Korea, North",
		"KQ" to "Kingman Reef",
		"KR" to "Kiribati",
		"KS" to "Korea, South",
		"KT" to "Christmas Island",
		"KU" to "Kuwait",
		"KV" to "Kosovo",
		"KZ" to "Kazakhstan",
		"LA" to "Laos",
		"LC" to "St. Lucia and St. Vincent",
		"LE" to "Lebanon",
		"LG" to "Latvia",
		"LH" to "Lithuania",
		"LI" to "Liberia",
		"LN" to "Southern Line Islands",
		"LO" to "Slovakia",
		"LQ" to "Palmyra Atoll",
		"LS" to "Liechtenstein",
		"LT" to "Lesotho",
		"LU" to "Luxembourg",
		"LY" to "Libya",
		"MA" to "Madagascar",
		"MB" to "Martinique",
		"MC" to "Macau",
		"MD" to "Moldova",
		"ME" to "Madeira",
		"MF" to "Mayotte",
		"MG" to "Mongolia",
		"MH" to "Montserrat",
		"MI" to "Malawi",
		"MJ" to "Montenegro",
		"MK" to "Macedonia",
		"ML" to "Mali",
		"MM" to "Burma (Myanmar)",
		"MN" to "Monaco",
		"MO" to "Morocco",
		"MP" to "Mauritius",
		"MQ" to "Midway Islands",
		"MR" to "Mauritania",
		"MT" to "Malta",
		"MU" to "Oman",
		"MV" to "Maldives",
		"MW" to "Montenegro",
		"MX" to "Mexico",
		"MY" to "Malaysia",
		"MZ" to "Mozambique",
		"NC" to "New Caledonia",
		"NE" to "Niue",
		"NF" to "Norfolk Island",
		"NG" to "Niger",
		"NH" to "Vanuatu",
		"NI" to "Nigeria",
		"NL" to "Netherlands",
		"NO" to "Norway",
		"NP" to "Nepal",
		"NR" to "Nauru",
		"NS" to "Suriname",
		"NT" to "Netherlands Antilles",
		"NU" to "Nicaragua",
		"NZ" to "New Zealand",
		"OD" to "South Sudan",
		"OW" to "Ocean Weather Stations",
		"PA" to "Paraguay",
		"PC" to "Pitcairn Islands",
		"PE" to "Peru",
		"PF" to "Paracel Islands",
		"PG" to "Spratly Islands",
		"PI" to "Phoenix Islands",
		"PK" to "Pakistan",
		"PL" to "Poland",
		"PM" to "Panama",
		"PN" to "North Pacific Islands",
		"PO" to "Portugal",
		"PP" to "Papua New Guinea",
		"PS" to "Palau (Trust Territory Of The Pacific Islands)",
		"PU" to "Guinea-Bissau",
		"PZ" to "South Pacific Islands",
		"QA" to "Qatar",
		"RE" to "Reunion and Associated Islands",
		"RI" to "Serbia",
		"RM" to "Marshall Islands",
		"RO" to "Romania",
		"RP" to "Philippines",
		"RQ" to "Puerto Rico",
		"RS" to "Russia",
		"RW" to "Rwanda",
		"SA" to "Saudi Arabia",
		"SB" to "St. Pierre and Miquelon",
		"SC" to "St. Kitts and Nevis",
		"SE" to "Seychelles",
		"SF" to "South Africa",
		"SG" to "Senegal",
		"SH" to "St. Helena",
		"SI" to "Slovenia",
		"SK" to "Sarawak and Saba",
		"SL" to "Sierra Leone",
		"SM" to "San Marino",
		"SN" to "Singapore",
		"SO" to "Somalia",
		"SP" to "Spain",
		"SR" to "Serbia",
		"SS" to "St. Maarten",
		"ST" to "St. Lucia",
		"SU" to "Sudan",
		"SV" to "Svalbard",
		"SW" to "Sweden",
		"SX" to "South Georgia and The South Sandwich Islands",
		"SY" to "Syria",
		"SZ" to "Switzerland",
		"TC" to "United Arab Emirates",
		"TD" to "Trinidad and Tobago",
		"TE" to "Tromelin Island",
		"TH" to "Thailand",
		"TI" to "Tajikistan",
		"TK" to "Turks and Caicos Islands",
		"TL" to "Tokelau",
		"TN" to "Tonga",
		"TO" to "Togo",
		"TP" to "Sao Tome and Principe",
		"TS" to "Tunisia",
		"TU" to "Turkey",
		"TV" to "Tuvalu",
		"TW" to "Taiwan",
		"TX" to "Turkmenistan",
		"TZ" to "Tanzania",
		"UA" to "Former USSR (Asia)",
		"UE" to "Former USSR (Europe)",
		"UG" to "Uganda",
		"UK" to "United Kingdom",
		"UP" to "Ukraine",
		"US" to "United States",
		"UV" to "Burkina Faso",
		"UY" to "Uruguay",
		"UZ" to "Uzbekistan",
		"VC" to "St. Vincent and The Grenadines",
		"VE" to "Venezuela",
		"VI" to "Virgin Islands (British)",
		"VM" to "Vietnam",
		"VQ" to "Virgin Islands (U.S.)",
		"VT" to "Vatican City",
		"WA" to "Namibia",
		"WE" to "West Bank",
		"WF" to "Wallis and Futuna",
		"WI" to "Western Sahara",
		"WQ" to "Wake Island",
		"WS" to "Western Samoa",
		"WZ" to "Swaziland",
		"YM" to "Yemen",
		"YU" to "Yugoslavia (and Former Territory)",
		"YY" to "St. Marteen, St. Eustatius, and Saba",
		"ZA" to "Zambia",
		"ZI" to "Zimbabwe",
		"ZM" to "Samoa",
		"ZZ" to "St. Martin and St. Bartholomew"
	)

	val stateNames = mapOf(
		"  " to "?",

		// 50 States
		"AK" to "Alaska",
		"AL" to "Alabama",
		"AR" to "Arkansas",
		"AZ" to "Arizona",
		"CA" to "California",
		"CO" to "Colorado",
		"CT" to "Connecticut",
		"DE" to "Delaware",
		"FL" to "Florida",
		"GA" to "Georgia",
		"HI" to "Hawaii",
		"IA" to "Iowa",
		"ID" to "Idaho",
		"IL" to "Illinois",
		"IN" to "Indiana",
		"KS" to "Kansas",
		"KY" to "Kentucky",
		"LA" to "Louisiana",
		"MA" to "Massachusetts",
		"MD" to "Maryland",
		"ME" to "Maine",
		"MI" to "Michigan",
		"MN" to "Minnesota",
		"MO" to "Missouri",
		"MS" to "Mississippi",
		"MT" to "Montana",
		"NC" to "North Carolina",
		"ND" to "North Dakota",
		"NE" to "Nebraska",
		"NH" to "New Hampshire",
		"NJ" to "New Jersey",
		"NM" to "New Mexico",
		"NV" to "Nevada",
		"NY" to "New York",
		"OH" to "Ohio",
		"OK" to "Oklahoma",
		"OR" to "Oregon",
		"PA" to "Pennsylvania",
		"RI" to "Rhode Island",
		"SC" to "South Carolina",
		"SD" to "South Dakota",
		"TN" to "Tennessee",
		"TX" to "Texas",
		"UT" to "Utah",
		"VA" to "Virginia",
		"VT" to "Vermont",
		"WA" to "Washington",
		"WI" to "Wisconsin",
		"WV" to "West Virginia",
		"WY" to "Wyoming",

		// Commonwealth / Territory
		"AS" to "American Samoa",
		"DC" to "District of Columbia",
		"FM" to "Federated States of Micronesia",
		"GU" to "Guam",
		"MH" to "Marshall Islands",
		"MP" to "Northern Mariana Islands",
		"PW" to "Palau",
		"PR" to "Puerto Rico",
		"VI" to "Virgin Islands",

		// Provinces of Canada
		// "AB" to "Alberta",
		// "BC" to "British Columbia",
		// "MB" to "Manitoba",
		// "NB" to "New Brunswick",
		// "NL" to "Newfoundland and Labrador",
		// "NT" to "Northwest Territories",
		// "NS" to "Nova Scotia",
		// "NU" to "Nunavut",
		// "ON" to "Ontario",
		// "PE" to "Prince Edward Island",
		// "QC" to "Qu�bec",
		// "SK" to "Saskatchewan",
		// "YT" to "Yukon",

		// States of Mexico
		// "AG" to "Aguascalientes",
		// "BC" to "Baja California Norte",
		// "BS" to "Baja California Sur",
		// "CH" to "Chihuahua",
		// "CL" to "Colima",
		// "CM" to "Campeche",
		// "CO" to "Coahuila",
		// "CS" to "Chiapas",
		// "DF" to "Distrito Federal",
		// "DG" to "Durango",
		// "GR" to "Guerrero",
		// "GT" to "Guanajuato",
		// "HG" to "Hidalgo",
		// "JA" to "Jalisco",
		// "MI" to "Michoacan",
		// "MO" to "Morelos",
		// "NA" to "Nayarit",
		// "NL" to "Nuevo Leon",
		// "OA" to "Oaxaca",
		// "PU" to "Puebla",
		// "QR" to "Quintana Roo",
		// "QT" to "Queretaro",
		// "SI" to "Sinaloa",
		// "SL" to "San Luis Potosi",
		// "SO" to "Sonora",
		// "TB" to "Tabasco",
		// "TL" to "Tlaxcala",
		// "TM" to "Tamaulipas",
		// "VE" to "Veracruz",
		// "YU" to "Yucatan",
		// "ZA" to "Zacatecas",
	)

	@JvmStatic
	fun location() {
		for (entry in Icao.entries) {
			val prefix = entry.name.lowercase()
			val country = entry.country.uppercase()
			val state = entry.state.uppercase()
			val lat = entry.latitude
			val lng = entry.longitude
			val elv = entry.elevation
			val snm = entry.stationName.replace("'", "''")
			val priority = 80 - 5 * entry.priority.toInt()

			var pst = ", $country"
			if (state != "  ")
				pst = ", $state$pst"

			var shrt = snm
			val p = shrt.indexOf(',')
			if (p > 0)
				shrt = shrt.substring(0, p)

			println(
					"INSERT INTO `location` VALUES('$prefix', '$shrt$pst', GeomFromText('POINT($lng $lat)'), $elv, 0, $priority, '$snm$pst');")
		}
	}

	@JvmStatic
	fun json() {
		for (entry in Icao.entries) {
			val icao = entry.name.uppercase()
			val state = entry.state.uppercase()
			val lat = entry.latitude.toFloat()
			val lng = entry.longitude.toFloat()
			var snm = entry.stationName

			val p = snm.indexOf(',')
			if (p > 0)
				snm = snm.substring(0, p)
			if (state != "  ")
				snm += ", $state"

			println("\t\t\t{ 'lat': $lat, 'lng': $lng, 'label': \"<b>${icao.uppercase()}</b> $snm\" },")
		}
	}

	@JvmStatic
	fun tables() {
		for (entry in Icao.entries) {
			val icao = entry.name.uppercase()
			val name = "${icao.lowercase()}_log"

			println("DROP TABLE IF EXISTS `$name`;")
			println("CREATE TABLE IF NOT EXISTS `$name` (")
			println("  `DateTime` int NOT NULL,")
			println("  `SensorID` smallint NOT NULL,")
			println("  `Value` float NOT NULL,")
			println("  KEY `DateTime` (`DateTime`),")
			println("  KEY `SensorID` (`SensorID`)")
			println(") ENGINE=MyISAM DEFAULT CHARSET=utf8;")
			println()
		}
	}

	@JvmStatic
	fun table() {
		val names = Icao.entries.map { it.name }.sorted()

		var st = "'${names[0]}'"
		for (i in 1 until names.size)
			st += ",'${names[i]}'"

		println("DROP TABLE IF EXISTS `log`;")
		println("CREATE TABLE IF NOT EXISTS `log` (")
		println("  `epoch` int NOT NULL,")
		println("  `station` ENUM ($st) NOT NULL,")
		println("  `sensor_id` tinyint NOT NULL,")
		println("  `value` float NOT NULL,")
		println("  KEY `epoch` (`epoch`),")
		println("  KEY `station` (`station`),")
		println("  KEY `sensor_id` (`sensor_id`)")
		println(") ENGINE=MyISAM DEFAULT CHARSET=utf8;")
		println()
	}

	@JvmStatic
	fun merge() {
		for (entry in Icao.entries) {
			val icao = entry.name.uppercase()
			val name = "${icao.lowercase()}_log"

			println("INSERT INTO merge_log SELECT * FROM $name;")
			// System.out.println("UPDATE " + name + " SET `Value`=ROUND(`Value` * 1852 /
			// 3600, 1) WHERE `SensorID`=22;");
			// System.out.println("DELETE FROM " + name + " WHERE `SensorID`=13 &&
			// `Value`='n/a';");
		}
	}

	@JvmStatic
	fun html() {
		var lastState = "??"

		for (entry in Icao.entries) {
			val icao = entry.name.uppercase()
			val state = entry.state.uppercase()
			val snm = entry.stationName
			val prefix = icao.lowercase()

			// String preComment = country + ", ";
			// if (!state.equals(" ")) preComment += state + ", ";

			if (lastState != state) {
				println("<h2>$state - ${stateNames[state]}</h2>")
				println()

				lastState = state
			}

			println("<div class=\"media\">")
			println("  <a class=\"pull-left\" href=\"report.php?prefix=$prefix\"><img class=\"media-object img-rounded\" src=\"images/icons/$prefix.png\" width=\"96\" height=\"48\"></a>")
			println("  <div class=\"media-body\"><a href=\"report.php?prefix=$prefix\">$icao</a><br>$snm</div>")
			println("</div>")
			println()

		}
	}

	@JvmStatic
	fun sitemap() {
		for (entry in Icao.entries) {
			val prefix = entry.name.lowercase()
			println("https://larsi.org/weather/report.php?prefix=$prefix")
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		// location();
		// json();

		// tables();
		// table();

		// merge();

		// html();

		// sitemap();
	}
}
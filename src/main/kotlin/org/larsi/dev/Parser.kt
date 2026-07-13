package org.larsi.dev

import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

import org.w3c.dom.Element
import org.xml.sax.SAXException

object Parser
{
	val stations: Array<Array<String>> = arrayOf(
				arrayOf("KABQ", "Albuquerque, Albuquerque International Airport", "0"),
				arrayOf("KANJ", "Sault Ste. Marie", "0"),
				arrayOf("KATL", "Atlanta, Hartsfield - Jackson Atlanta International Airport", "0"),
				arrayOf("KBIS", "Bismarck, Bismarck Municipal Airport", "0"),
				arrayOf("KBOI", "Boise, Boise Air Terminal", "0"),
				arrayOf("KBOS", "Boston, Logan International Airport", "0"),
				arrayOf("KBRO", "Brownsville, Brownsville / South Padre Island International Airport", "0"),
				arrayOf("KBUF", "Buffalo, Greater Buffalo International Airport", "0"),
				arrayOf("KCAR", "Caribou, Caribou Municipal Airport", "0"),
				arrayOf("KCVG", "Covington / Cincinnati, Cincinnati / Northern Kentucky International Airport", "0"),
				arrayOf("KDDC", "Dodge City, Dodge City Regional Airport", "0"),
				arrayOf("KDEN", "Denver, Denver International Airport", "0"),
				arrayOf("KDFW", "Dallas / Fort Worth, Dallas / Fort Worth International Airport", "0"),
				arrayOf("KGTF", "Great Falls, Great Falls International Airport", "0"),
				arrayOf("KIAD", "Washington DC, Washington-Dulles International Airport", "0"),
				arrayOf("KILM", "Wilmington, Wilmington International Airport", "0"),
				arrayOf("KJAX", "Jacksonville, Jacksonville International Airport", "0"),
				arrayOf("KLAX", "Los Angeles, Los Angeles International Airport", "0"),
				arrayOf("KLBB", "Lubbock, Lubbock International Airport", "0"),
				arrayOf("KLIT", "Little Rock, Adams Field", "0"),
				arrayOf("KMCI", "Kansas City, Kansas City International Airport", "0"),
				arrayOf("KMFR", "Medford, Rogue Valley International Airport", "0"),
				arrayOf("KMIA", "Miami, Miami International Airport", "0"),
				arrayOf("KMSP", "Minneapolis, Minneapolis-St. Paul International Airport", "0"),
				arrayOf("KMSY", "New Orleans, New Orleans International Airport", "0"),
				arrayOf("KORD", "Chicago, Chicago-O'Hare International Airport", "0"),
				arrayOf("KPHX", "Phoenix, Phoenix Sky Harbor International Airport", "0"),
				arrayOf("KSAT", "San Antonio, San Antonio International Airport", "0"),
				arrayOf("KSEA", "Seattle, Seattle-Tacoma International Airport", "0"),
				arrayOf("KSFO", "San Francisco, San Francisco International Airport", "0"),
				arrayOf("KSLC", "Salt Lake City, Salt Lake City International Airport", "0"),
				arrayOf("KSTL", "St. Louis, Lambert-St. Louis International Airport", "0"),
				arrayOf("KTPH", "Tonopah, Tonopah Airport", "0"),
				arrayOf("KBFI", "Seattle, Seattle Boeing Field", "1"),
				arrayOf("KBJC", "Broomfield / Jeffco", "1"),
				arrayOf("KBLV", "Belleville, Scott AFB/MidAmerica Airport", "1"),
				arrayOf("KCHS", "Charleston, Charleston Air Force Base", "1"),
				arrayOf("KCRG", "Jacksonville, Craig Municipal Airport", "1"),
				arrayOf("KDAL", "Dallas, Dallas Love Field", "1"),
				arrayOf("KDCA", "Washington DC, Reagan National Airport", "1"),
				arrayOf("KDTW", "Detroit, Detroit Metropolitan Wayne County Airport", "1"),
				arrayOf("KDVT", "Phoenix, Phoenix-Deer Valley Municipal Airport", "1"),
				arrayOf("KEUG", "Eugene, Mahlon Sweet Field", "1"),
				arrayOf("KFAR", "Fargo, Hector International Airport", "1"),
				arrayOf("KFFC", "Atlanta, Peachtree City-Falcon Field", "1"),
				arrayOf("KGEG", "Spokane, Spokane International Airport", "1"),
				arrayOf("KHSE", "Hatteras, Mitchell Field", "1"),
				arrayOf("KINL", "International Falls, Falls International Airport", "1"),
				arrayOf("KJFK", "New York, Kennedy International Airport", "1"),
				arrayOf("KLGA", "New York, La Guardia Airport", "1"),
				arrayOf("KLRF", "Little Rock Air Force Base", "1"),
				arrayOf("KLUK", "Cincinnati, Cincinnati Municipal Airport Lunken Field", "1"),
				arrayOf("KMDW", "Chicago, Chicago Midway Airport", "1"),
				arrayOf("KMKC", "Kansas City, Kansas City Downtown Airport", "1"),
				arrayOf("KNEW", "New Orleans, New Orleans Lakefront Airport", "1"),
				arrayOf("KOAK", "Oakland, Metro Oakland International Airport", "1"),
				arrayOf("KOGD", "Ogden, Ogden-Hinckley Airport", "1"),
				arrayOf("KONT", "Ontario, Ontario International Airport", "1"),
				arrayOf("KOPF", "Miami, Opa Locka Airport", "1"),
				arrayOf("KSAF", "Santa Fe, Santa Fe County Municipal Airport", "1"),
				arrayOf("KSHR", "Sheridan, Sheridan County Airport", "1"),
				arrayOf("KSTP", "St. Paul, St. Paul Downtown Holman Field", "1"),
				arrayOf("KVTN", "Valentine, Miller Field", "1"),
				arrayOf("KACK", "Nantucket, Nantucket Memorial Airport", "2"),
				arrayOf("KASD", "Slidell, Slidell Airport", "2"),
				arrayOf("KBED", "Bedford, Hanscom Field", "2"),
				arrayOf("KBKF", "Aurora, Buckley Air Force Base Airport", "2"),
				arrayOf("KBNA", "Nashville, Nashville International Airport", "2"),
				arrayOf("KCRP", "Corpus Christi, Corpus Christi International Airport", "2"),
				arrayOf("KDLH", "Duluth, Duluth International Airport", "2"),
				arrayOf("KDRT", "Del Rio, Del Rio International Airport", "2"),
				arrayOf("KDSM", "Des Moines, Des Moines International Airport", "2"),
				arrayOf("KELP", "El Paso, El Paso International Airport", "2"),
				arrayOf("KEWR", "Newark, Newark International Airport", "2"),
				arrayOf("KEYW", "Key West, Key West International Airport", "2"),
				arrayOf("KFAT", "Fresno, Fresno Air Terminal", "2"),
				arrayOf("KFLG", "Flagstaff, Flagstaff Pulliam Airport", "2"),
				arrayOf("KFSD", "Sioux Falls, Foss Field", "2"),
				arrayOf("KFTW", "Fort Worth, Meacham International Airport", "2"),
				arrayOf("KGFK", "Grand Forks, Grand Forks International Airport", "2"),
				arrayOf("KGGW", "Glasgow, Glasgow International Airport", "2"),
				arrayOf("KGJT", "Grand Junction, Walker Field", "2"),
				arrayOf("KHUL", "Houlton, Houlton International Airport", "2"),
				arrayOf("KIAH", "Houston, Houston Intercontinental Airport", "2"),
				arrayOf("KIND", "Indianapolis, Indianapolis International Airport", "2"),
				arrayOf("KLAS", "Las Vegas, McCarran International Airport", "2"),
				arrayOf("KMCO", "Orlando, Orlando International Airport", "2"),
				arrayOf("KMEM", "Memphis, Memphis International Airport", "2"),
				arrayOf("KMRY", "Monterey, Monterey Peninsula Airport", "2"),
				arrayOf("KOKC", "Oklahoma City, Will Rogers World Airport", "2"),
				arrayOf("KOMA", "Omaha, Eppley Airfield", "2"),
				arrayOf("KPDT", "Pendleton, Eastern Oregon Regional At Pendleton Airport", "2"),
				arrayOf("KPDX", "Portland, Portland International Airport", "2"),
				arrayOf("KPIT", "Pittsburgh, Pittsburgh International Airport", "2"),
				arrayOf("KPTK", "Pontiac, Oakland County International Airport", "2"),
				arrayOf("KRAP", "Rapid City, Rapid City Regional Airport", "2"),
				arrayOf("KRDU", "Raleigh / Durham, Raleigh-Durham International Airport", "2"),
				arrayOf("KRIW", "Riverton, Riverton Regional Airport", "2"),
				arrayOf("KRNO", "Reno, Reno Tahoe International Airport", "2"),
				arrayOf("KSAC", "Sacramento, Sacramento Executive Airport", "2"),
				arrayOf("KSAN", "San Diego, San Diego International-Lindbergh Field", "2"),
				arrayOf("KSLE", "Salem, McNary Field", "2"),
				arrayOf("KTUS", "Tucson, Tucson International Airport", "2"),
				arrayOf("KUIL", "Quillayute, Quillayute State Airport", "2"),
				arrayOf("KABR", "Aberdeen, Aberdeen Regional Airport", "3"),
				arrayOf("KALB", "Albany, Albany International Airport", "3"),
				arrayOf("KAMA", "Amarillo, Amarillo International Airport", "3"),
				arrayOf("KANE", "Minneapolis / Blaine", "3"),
				arrayOf("KAUS", "Austin, Austin-Bergstrom International Airport", "3"),
				arrayOf("KAXN", "Alexandria, Chandler Field", "3"),
				arrayOf("KBDR", "Bridgeport, Sikorsky Memorial Airport", "3"),
				arrayOf("KBGM", "Binghamton, Binghamton Regional Airport", "3"),
				arrayOf("KBGR", "Bangor, Bangor International Airport", "3"),
				arrayOf("KBTV", "Burlington, Burlington International Airport", "3"),
				arrayOf("KBWI", "Baltimore, Baltimore-Washington International Airport", "3"),
				arrayOf("KCAE", "Columbia, Columbia Metropolitan Airport", "3"),
				arrayOf("KCDS", "Childress, Childress Municipal Airport", "3"),
				arrayOf("KCLE", "Cleveland, Cleveland-Hopkins International Airport", "3"),
				arrayOf("KCOD", "Cody", "3"),
				arrayOf("KCOS", "Colorado Springs, City Of Colorado Springs Municipal Airport", "3"),
				arrayOf("KCYS", "Cheyenne, Cheyenne Airport", "3"),
				arrayOf("KDAB", "Daytona Beach, Daytona Beach Regional Airport", "3"),
				arrayOf("KDET", "Detroit, Detroit City Airport", "3"),
				arrayOf("KDLS", "The Dalles, Columbia Gorge Regional / The Dalles Municipal Airport", "3"),
				arrayOf("KDPG", "Dugway Proving Grounds", "3"),
				arrayOf("KDVN", "Davenport, Davenport Municipal Airport", "3"),
				arrayOf("KEDW", "Edwards Air Force Base", "3"),
				arrayOf("KEET", "Alabaster, Shelby County Airport", "3"),
				arrayOf("KEKO", "Elko, Elko Regional Airport", "3"),
				arrayOf("KELY", "Ely, Ely Airport", "3"),
				arrayOf("KFSM", "Fort Smith, Fort Smith Regional Airport", "3"),
				arrayOf("KFTY", "Atlanta, Fulton County Airport-Brown Field", "3"),
				arrayOf("KFWA", "Fort Wayne, Fort Wayne International Airport", "3"),
				arrayOf("KGLD", "Goodland, Renner Field", "3"),
				arrayOf("KGLR", "Gaylord, Otsego County Airport", "3"),
				arrayOf("KGRB", "Green Bay, Austin Straubel International Airport", "3"),
				arrayOf("KGRR", "Grand Rapids, Gerald R. Ford International Airport", "3"),
				arrayOf("KGSO", "Greensboro, Piedmont Triad International Airport", "3"),
				arrayOf("KGSP", "Greer, Greenville-Spartanburg Airport", "3"),
				arrayOf("KHIB", "Hibbing, Chisholm-Hibbing Airport", "3"),
				arrayOf("KHOU", "Houston, Houston Hobby Airport", "3"),
				arrayOf("KHTS", "Huntington, Tri-State Airport", "3"),
				arrayOf("KHVR", "Havre, Havre City-County Airport", "3"),
				arrayOf("KICT", "Wichita, Wichita Mid-Continent Airport", "3"),
				arrayOf("KILN", "Wilmington, Airborne Airpark Airport", "3"),
				arrayOf("KIWA", "Mesa, Williams Gateway Airport", "3"),
				arrayOf("KJAN", "Jackson, Jackson International Airport", "3"),
				arrayOf("KLBF", "North Platte, North Platte Regional Airport", "3"),
				arrayOf("KLCH", "Lake Charles, Lake Charles Regional Airport", "3"),
				arrayOf("KLNK", "Lincoln, Lincoln Municipal Airport", "3"),
				arrayOf("KLOU", "Louisville, Bowman Field Airport", "3"),
				arrayOf("KLRD", "Laredo, Laredo International Airport", "3"),
				arrayOf("KLVS", "Las Vegas, Las Vegas Municipal Airport", "3"),
				arrayOf("KMCN", "Macon, Middle Georgia Regional Airport", "3"),
				arrayOf("KMGW", "Morgantown, Morgantown Municipal-Hart Field", "3"),
				arrayOf("KMKE", "Milwaukee, General Mitchell International Airport", "3"),
				arrayOf("KMOB", "Mobile, Mobile Regional Airport", "3"),
				arrayOf("KMSO", "Missoula, Missoula International Airport", "3"),
				arrayOf("KMWL", "Mineral Wells, Mineral Wells Airport", "3"),
				arrayOf("KNKT", "Cherry Point, Marine Corps Air Station", "3"),
				arrayOf("KORF", "Norfolk, Norfolk International Airport", "3"),
				arrayOf("KOUN", "Norman / Max Westheimer", "3"),
				arrayOf("KPAH", "Paducah, Barkley Regional Airport", "3"),
				arrayOf("KPBI", "West Palm Beach, Palm Beach International Airport", "3"),
				arrayOf("KPGA", "Page, Page Municipal Airport", "3"),
				arrayOf("KPHL", "Philadelphia, Philadelphia International Airport", "3"),
				arrayOf("KPIH", "Pocatello, Pocatello Regional Airport", "3"),
				arrayOf("KPIR", "Pierre, Pierre Regional Airport", "3"),
				arrayOf("KPRC", "Prescott, Love Field", "3"),
				arrayOf("KPSP", "Palm Springs, Palm Springs Regional Airport", "3"),
				arrayOf("KPUB", "Pueblo, Pueblo Memorial Airport", "3"),
				arrayOf("KPVD", "Providence, Theodore Francis Green State Airport", "3"),
				arrayOf("KPWM", "Portland, Portland International Jetport", "3"),
				arrayOf("KRDD", "Redding, Redding Municipal Airport", "3"),
				arrayOf("KROA", "Roanoke, Roanoke Regional Airport", "3"),
				arrayOf("KSHV", "Shreveport, Shreveport Regional Airport", "3"),
				arrayOf("KSMP", "Stampede Pass", "3"),
				arrayOf("KSTC", "St Cloud, St Cloud Regional Airport", "3"),
				arrayOf("KSUS", "St. Louis, Spirit Of St. Louis Airport", "3"),
				arrayOf("KSUX", "Sioux City, Sioux Gateway Airport", "3"),
				arrayOf("KSYR", "Syracuse, Syracuse Hancock International Airport", "3"),
				arrayOf("KTLH", "Tallahassee, Tallahassee Regional Airport", "3"),
				arrayOf("KTOP", "Topeka, Philip Billard Municipal Airport", "3"),
				arrayOf("KTPA", "Tampa, Tampa International Airport", "3"),
				arrayOf("KTUL", "Tulsa, Tulsa International Airport", "3"),
				arrayOf("KUNV", "State College, University Park Airport", "3"),
				arrayOf("KVBG", "Lompoc, Vandenberg Air Force Base", "3"),
				arrayOf("KWMC", "Winnemucca, Winnemucca Municipal Airport", "3"),
				arrayOf("KYKM", "Yakima, Yakima Air Terminal", "3"),
				arrayOf("KABI", "Abilene, Abilene Regional Airport", "4"),
				arrayOf("KACY", "Atlantic City, Atlantic City International Airport", "4"),
				arrayOf("KADM", "Ardmore, Ardmore Municipal Airport", "4"),
				arrayOf("KAFF", "Air Force Academy", "4"),
				arrayOf("KAGS", "Augusta, Bush Field", "4"),
				arrayOf("KAHN", "Athens, Athens Airport", "4"),
				arrayOf("KAKO", "Akron, Akron-Washington County Airport", "4"),
				arrayOf("KALO", "Waterloo, Waterloo Municipal Airport", "4"),
				arrayOf("KAPA", "Denver, Centennial Airport", "4"),
				arrayOf("KAPN", "Alpena, Alpena County Regional Airport", "4"),
				arrayOf("KASE", "Aspen, Aspen-Pitkin County Airport", "4"),
				arrayOf("KAST", "Astoria, Astoria Regional Airport", "4"),
				arrayOf("KAUG", "Augusta, Augusta State Airport", "4"),
				arrayOf("KAVL", "Asheville, Asheville Regional Airport", "4"),
				arrayOf("KBHM", "Birmingham, Birmingham International Airport", "4"),
				arrayOf("KBRD", "Brainerd, Brainerd-Crow Wing County Regional Airport", "4"),
				arrayOf("KBTR", "Baton Rouge, Baton Rouge Metropolitan, Ryan Field", "4"),
				arrayOf("KBWG", "Bowling Green, Bowling Green-Warren County Regional Airport", "4"),
				arrayOf("KBZN", "Bozeman, Gallatin Field", "4"),
				arrayOf("KCAG", "Craig, Craig-Moffat Airport", "4"),
				arrayOf("KCDC", "Cedar City, Cedar City Municipal Airport", "4"),
				arrayOf("KCHA", "Chattanooga, Lovell Field", "4"),
				arrayOf("KCID", "Cedar Rapids, The Eastern Iowa Airport", "4"),
				arrayOf("KCKB", "Clarksburg, Clarksburg Benedum Airport", "4"),
				arrayOf("KCLL", "College Station, Easterwood Field", "4"),
				arrayOf("KCLT", "Charlotte, Charlotte / Douglas International Airport", "4"),
				arrayOf("KCMH", "Columbus, Port Columbus International Airport", "4"),
				arrayOf("KCOE", "Coeur d'Alene, Coeur d'Alene Air Terminal", "4"),
				arrayOf("KCOU", "Columbia, Columbia Regional Airport", "4"),
				arrayOf("KCTB", "Cut Bank, Cut Bank Municipal Airport", "4"),
				arrayOf("KCVS", "Cannon Air Force Base / Clovis", "4"),
				arrayOf("KDAY", "Dayton, Cox Dayton International Airport", "4"),
				arrayOf("KDBQ", "Dubuque, Dubuque Regional Airport", "4"),
				arrayOf("KDEC", "Decatur, Decatur Airport", "4"),
				arrayOf("KDRO", "Durango, Durango-La Plata County Airport", "4"),
				arrayOf("KEAU", "Eau Claire, Chippewa Valley Regional Airport", "4"),
				arrayOf("KEKN", "Elkins, Elkins-Randolph County-Jennings Randolph Field", "4"),
				arrayOf("KEND", "Vance Air Force Base / Enid", "4"),
				arrayOf("KENV", "Wendover / Air Force Auxillary Field", "4"),
				arrayOf("KERI", "Erie, Erie International Airport", "4"),
				arrayOf("KEVV", "Evansville, Evansville Regional Airport", "4"),
				arrayOf("KFAY", "Fayetteville, Fayetteville Regional Airport", "4"),
				arrayOf("KFNT", "Flint, Bishop International Airport", "4"),
				arrayOf("KFYV", "Fayetteville, Drake Field", "4"),
				arrayOf("KGCK", "Garden City, Garden City Regional Airport", "4"),
				arrayOf("KGCN", "Grand Canyon, Grand Canyon National Park Airport", "4"),
				arrayOf("KGGG", "Longview, Gregg County Airport", "4"),
				arrayOf("KGNV", "Gainesville, Gainesville Regional Airport", "4"),
				arrayOf("KGWO", "Greenwood, Greenwood-LeFlore Airport", "4"),
				arrayOf("KGXY", "Greeley, Greeley-Weld County Airport", "4"),
				arrayOf("KGYY", "Gary Regional", "4"),
				arrayOf("KHFD", "Hartford, Hartford-Brainard Airport", "4"),
				arrayOf("KHLN", "Helena, Helena Regional Airport", "4"),
				arrayOf("KHMN", "Holloman Air Force Base", "4"),
				arrayOf("KHQM", "Hoquiam, Bowerman Airport", "4"),
				arrayOf("KHSV", "Huntsville, Huntsville International / Jones Field", "4"),
				arrayOf("KHYS", "Hays, Hays Regional Airport", "4"),
				arrayOf("KIAG", "Niagara Falls, Niagara Falls International Airport", "4"),
				arrayOf("KILG", "Wilmington, New Castle County Airport", "4"),
				arrayOf("KINT", "Winston Salem, Smith Reynolds Airport", "4"),
				arrayOf("KINW", "Winslow, Winslow Municipal Airport", "4"),
				arrayOf("KISP", "Islip, Long Island Mac Arthur Airport", "4"),
				arrayOf("KLAA", "Lamar, Lamar Municipal Airport", "4"),
				arrayOf("KLAF", "Lafayette, Purdue University Airport", "4"),
				arrayOf("KLAM", "Los Alamos, Los Alamos Airport", "4"),
				arrayOf("KLAN", "Lansing, Capital City Airport", "4"),
				arrayOf("KLBL", "Liberal, Liberal Municipal Airport", "4"),
				arrayOf("KLEX", "Lexington, Blue Grass Airport", "4"),
				arrayOf("KLIC", "Limon, Limon Municipal Airport", "4"),
				arrayOf("KMGM", "Montgomery, Dannelly Field", "4"),
				arrayOf("KMHK", "Manhattan, Manhattan Municipal Airport", "4"),
				arrayOf("KMKG", "Muskegon, Muskegon County Airport", "4"),
				arrayOf("KMLB", "Melbourne, Melbourne International Airport", "4"),
				arrayOf("KMLS", "Miles City, Frank Wiley Field Airport", "4"),
				arrayOf("KMPV", "Barre / Montpelier, Knapp State Airport", "4"),
				arrayOf("KMRB", "Martinsburg, Eastern West Virginia Regional/Shepherd Airport", "4"),
				arrayOf("KMSN", "Madison, Dane County Regional-Truax Field", "4"),
				arrayOf("KNID", "China Lake, Naval Air Facility", "4"),
				arrayOf("KOFF", "Omaha / Offutt Air Force Base", "4"),
				arrayOf("KPIA", "Peoria, Greater Peoria Regional Airport", "4"),
				arrayOf("KPKD", "Park Rapids, Park Rapids Municipal Airport", "4"),
				arrayOf("KPNE", "Philadelphia, Northeast Philadelphia Airport", "4"),
				arrayOf("KPOE", "Fort Polk, Polk AAF Ft Polk", "4"),
				arrayOf("KPOU", "Poughkeepsie, Dutchess County Airport", "4"),
				arrayOf("KRFD", "Rockford, Greater Rockford Airport", "4"),
				arrayOf("KRIC", "Richmond, Richmond International Airport", "4"),
				arrayOf("KROW", "Roswell, Roswell Industrial Air Center Airport", "4"),
				arrayOf("KSAV", "Savannah, Savannah International Airport", "4"),
				arrayOf("KSBN", "South Bend, South Bend Regional Airport", "4"),
				arrayOf("KSBY", "Salisbury, Salisbury-Ocean City Wicomico County Regional Airport", "4"),
				arrayOf("KSPS", "Wichita Falls, Sheppard Air Force Base", "4"),
				arrayOf("KSTJ", "St. Joseph, Rosecrans Memorial Airport", "4"),
				arrayOf("KTRI", "Bristol / Johnson / Kingsport, Tri-City Regional Airport", "4"),
				arrayOf("KTUP", "Tupelo, Tupelo Regional Airport", "4"),
				arrayOf("KTYR", "Tyler, Tyler Pounds Field", "4"),
				arrayOf("KTYS", "Knoxville, McGhee Tyson Airport", "4"),
				arrayOf("KVRB", "Vero Beach, Vero Beach Municipal Airport", "4"),
				arrayOf("KAKQ", "Wakefield, Wakefield Municipal Airport", "5"),
				arrayOf("KAWM", "West Memphis, West Memphis Municipal Airport", "5"),
				arrayOf("KDRA", "Mercury, Desert Rock Airport", "5"),
				arrayOf("KFDR", "Frederick, Frederick Municipal Airport", "5"),
				arrayOf("KGMU", "Greenville, Greenville Downtown Airport", "5"),
				arrayOf("KGRK", "Fort Hood, Robert Gray AAF Ft Hood", "5"),
				arrayOf("KJKL", "Jackson, Carroll Airport", "5"),
				arrayOf("KLOT", "Chicago/Romeoville, Lewis University Airport", "5"),
				arrayOf("KMAF", "Midland, Midland International Airport", "5"),
				arrayOf("KNKX", "San Diego, Miramar MCAS/Mitscher Field Airport", "5"),
				arrayOf("KNQA", "Millington, Millington Municipal Airport", "5"),
				arrayOf("KPNA", "Pinedale, Ralph Wenz Field Airport", "5"),
				arrayOf("KSGF", "Springfield, Springfield Regional Airport", "5"),
				arrayOf("KSJT", "San Angelo, Mathis Field", "5"),
				arrayOf("KWAL", "Wallops Island, Wallops Flight Facility Airport", "5"),
				arrayOf("KELM", "Elmira, Elmira / Corning Regional Airport", "6"),
				arrayOf("KITH", "Ithaca, Ithaca Tompkins Regional Airport", "6"),
				arrayOf("KROC", "Rochester, Greater Rochester International Airport", "6")
		)

	val ishData = mutableMapOf<String, String>()

	/**
	 * @param e   element to be searched
	 * @param tag enclosing tags
	 * @return the value of the text within a certain tag
	 */
	@JvmStatic
	fun getTextValue(e: Element, tag: String): String? {
		val nl = e.getElementsByTagName(tag)
		if (nl == null || nl.length < 1)
			return null
		val n = nl.item(0).firstChild ?: return null
		return n.nodeValue
	}

	@JvmStatic
	fun inStations(call: String): Array<String>? {
		for (entry in stations) {
			if (entry[0] == call)
				return entry
		}
		return null
	}

	@JvmStatic
	fun parseIshHistory() {
		val duplicates = mutableMapOf<String, Int>()
		for (entry in stations)
			duplicates[entry[0]] = 0

		// final URL stations =new
		// URL("ftp://ftp.ncdc.noaa.gov/pub/data/noaa/isd-history.txt");
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(stations.openStream()));
		File("doc/isd-history.txt").useLines(Charset.defaultCharset()) { lines ->
		for (l in lines) {
			if (l.length < 99)
				continue

			val usaf = l.substring(0, 6)
			val wban = l.substring(7, 12)
			val name = l.substring(13, 42).trim()
			val ctry = l.substring(43, 45).trim().uppercase()
			val state = l.substring(48, 50).trim().uppercase()
			val call = l.substring(51, 55).trim().uppercase()
			val lat = l.substring(57, 64).trim()
			val lon = l.substring(65, 73).trim()
			val elev = l.substring(74, 81).trim()
			val start = l.substring(82, 90).trim()
			val end = l.substring(91, 99).trim()

			if (start.equals("NO DATA", ignoreCase = true) || end.equals("NO DATA", ignoreCase = true))
				continue

			if (name.isEmpty())
				continue
			if (call.isEmpty())
				continue

			if (lat == "-99.999")
				continue
			if (lon == "-999.999")
				continue
			if (lat == "-9999.9")
				continue

			if (usaf == "999999")
				continue
			if (wban == "99999")
				continue

			if (!call.startsWith("K"))
				continue
			if (call.matches(Regex(".*[0-9].*")))
				continue
			if (ctry != "US")
				continue

			val endYear = end.substring(0, 4).toInt()
			if (endYear < 2020)
				continue

			val startYear = start.substring(0, 4).toInt()
			if (startYear >= 2015)
				continue

			val stationsEntry = inStations(call) ?: continue

			duplicates[call] = if (duplicates.containsKey(call)) 1 + duplicates[call]!! else 1

			ishData[call] =
					"${stationsEntry[2]}\"$call\", \"$usaf-$wban\", \"$ctry\", \"$state\", \"$lat\", \"$lon\", \"$elev\", \"${stationsEntry[1]}\", \"${stationsEntry[2]}\""

		}
		}

		println("Duplicates: ")
		for (entry in duplicates.entries) {
			val key = entry.key
			val value = entry.value
			if (value != 1)
				println("$key => $value")
		}
		println()
	}

	@JvmStatic
	fun parseStations() {
		val stationsUrl = URL("http://weather.rap.ucar.edu/surface/stations.txt")
		InputStreamReader(stationsUrl.openStream()).useLines { lines ->
		for (l in lines) {
			if (l.length < 83)
				continue

			// String state = line.substring( 0, 2).toUpperCase();
			val icao = l.substring(20, 24).uppercase()
			val priority = l.substring(79, 80)
			// String country = line.substring(81, 83).toUpperCase();

			if (!(priority == "0" || priority == "1" || priority == "2" || priority == "3" ||
					priority == "4" || priority == "5" || icao == "KELM" || icao == "KITH" ||
					icao == "KROC"))
				continue

			if (icao[0] != 'K')
				continue

			val url = "http://api.geonames.org/weatherIcao?username=larsi&ICAO=$icao"
			try {
				val dbf = DocumentBuilderFactory.newInstance()
				val db = dbf.newDocumentBuilder()
				val doc = db.parse(URL(url).openStream())
				doc.documentElement.normalize()

				val root = doc.documentElement
				val nl = root.getElementsByTagName("observation")

				if (nl != null && nl.length > 0) {
					val obs = nl.item(0) as Element

					val snm = getTextValue(obs, "stationName")
					println("$priority { \"$icao\", \"$snm\", \"$priority\" },")
				}
			} catch (e: java.io.IOException) {
				e.printStackTrace()
			} catch (e: ParserConfigurationException) {
				e.printStackTrace()
			} catch (e: SAXException) {
				e.printStackTrace()
			}
		}
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		try {
			// parseStations();

			parseIshHistory()

			for (entry in ishData.entries)
				println("\t\t{ ${entry.value} },")

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

}
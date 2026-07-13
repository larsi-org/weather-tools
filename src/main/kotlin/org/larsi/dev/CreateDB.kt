package org.larsi.dev

object CreateDB
{
	enum class Icao(val prefix: String, val usafWban: String, val country: String, val state: String, val latitude: String, val longitude: String, val elevation: String, val stationName: String, val priority: String)
	{
		KABQ("KABQ", "723650-23050", "US", "NM", "+35.042", "-106.616", "+1618.5", "Albuquerque, Albuquerque International Airport", "0"),
		KANJ("KANJ", "727340-14847", "US", "MI", "+46.479", "-084.357", "+0220.1", "Sault Ste. Marie", "0"),
		KATL("KATL", "722190-13874", "US", "GA", "+33.630", "-084.442", "+0307.9", "Atlanta, Hartsfield - Jackson Atlanta International Airport", "0"),
		KBIS("KBIS", "727640-24011", "US", "ND", "+46.783", "-100.757", "+0503.2", "Bismarck, Bismarck Municipal Airport", "0"),
		KBOI("KBOI", "726810-24131", "US", "ID", "+43.567", "-116.241", "+0857.7", "Boise, Boise Air Terminal", "0"),
		KBOS("KBOS", "725090-14739", "US", "MA", "+42.361", "-071.010", "+0003.7", "Boston, Logan International Airport", "0"),
		KBRO("KBRO", "722500-12919", "US", "TX", "+25.914", "-097.423", "+0007.3", "Brownsville, Brownsville / South Padre Island International Airport", "0"),
		KBUF("KBUF", "725280-14733", "US", "NY", "+42.941", "-078.736", "+0218.2", "Buffalo, Greater Buffalo International Airport", "0"),
		KCAR("KCAR", "727120-14607", "US", "ME", "+46.871", "-068.017", "+0190.2", "Caribou, Caribou Municipal Airport", "0"),
		KCVG("KCVG", "724210-93814", "US", "KY", "+39.044", "-084.672", "+0269.1", "Covington / Cincinnati, Cincinnati / Northern Kentucky International Airport", "0"),
		KDDC("KDDC", "724510-13985", "US", "KS", "+37.769", "-099.968", "+0787.0", "Dodge City, Dodge City Regional Airport", "0"),
		KDEN("KDEN", "725650-03017", "US", "CO", "+39.833", "-104.658", "+1650.2", "Denver, Denver International Airport", "0"),
		KDFW("KDFW", "722590-03927", "US", "TX", "+32.898", "-097.019", "+0170.7", "Dallas / Fort Worth, Dallas / Fort Worth International Airport", "0"),
		KGTF("KGTF", "727750-24143", "US", "MT", "+47.473", "-111.382", "+1116.8", "Great Falls, Great Falls International Airport", "0"),
		KIAD("KIAD", "724030-93738", "US", "VA", "+38.935", "-077.447", "+0088.4", "Washington DC, Washington-Dulles International Airport", "0"),
		KILM("KILM", "723020-13748", "US", "NC", "+34.268", "-077.900", "+0010.1", "Wilmington, Wilmington International Airport", "0"),
		KJAX("KJAX", "722060-13889", "US", "FL", "+30.495", "-081.694", "+0007.9", "Jacksonville, Jacksonville International Airport", "0"),
		KLAX("KLAX", "722950-23174", "US", "CA", "+33.938", "-118.389", "+0029.6", "Los Angeles, Los Angeles International Airport", "0"),
		KLBB("KLBB", "722670-23042", "US", "TX", "+33.666", "-101.823", "+0991.8", "Lubbock, Lubbock International Airport", "0"),
		KLIT("KLIT", "723403-13963", "US", "AR", "+34.727", "-092.239", "+0078.6", "Little Rock, Adams Field", "0"),
		KMCI("KMCI", "724460-03947", "US", "MO", "+39.297", "-094.731", "+0306.3", "Kansas City, Kansas City International Airport", "0"),
		KMFR("KMFR", "725970-24225", "US", "OR", "+42.375", "-122.877", "+0400.2", "Medford, Rogue Valley International Airport", "0"),
		KMIA("KMIA", "722020-12839", "US", "FL", "+25.788", "-080.317", "+0008.8", "Miami, Miami International Airport", "0"),
		KMSP("KMSP", "726580-14922", "US", "MN", "+44.883", "-093.229", "+0265.8", "Minneapolis, Minneapolis-St. Paul International Airport", "0"),
		KMSY("KMSY", "722310-12916", "US", "LA", "+29.997", "-090.278", "+0001.2", "New Orleans, New Orleans International Airport", "0"),
		KORD("KORD", "725300-94846", "US", "IL", "+41.960", "-087.932", "+0201.8", "Chicago, Chicago-O'Hare International Airport", "0"),
		KPHX("KPHX", "722780-23183", "US", "AZ", "+33.428", "-112.004", "+0337.4", "Phoenix, Phoenix Sky Harbor International Airport", "0"),
		KSAT("KSAT", "722530-12921", "US", "TX", "+29.544", "-098.484", "+0240.5", "San Antonio, San Antonio International Airport", "0"),
		KSEA("KSEA", "727930-24233", "US", "WA", "+47.444", "-122.314", "+0112.8", "Seattle, Seattle-Tacoma International Airport", "0"),
		KSFO("KSFO", "724940-23234", "US", "CA", "+37.620", "-122.365", "+0002.4", "San Francisco, San Francisco International Airport", "0"),
		KSLC("KSLC", "725720-24127", "US", "UT", "+40.778", "-111.969", "+1287.8", "Salt Lake City, Salt Lake City International Airport", "0"),
		KSTL("KSTL", "724340-13994", "US", "MO", "+38.753", "-090.374", "+0161.9", "St. Louis, Lambert-St. Louis International Airport", "0"),
		KTPH("KTPH", "724855-23153", "US", "NV", "+38.051", "-117.090", "+1644.4", "Tonopah, Tonopah Airport", "0"),
		KBFI("KBFI", "727935-24234", "US", "WA", "+47.530", "-122.301", "+0005.5", "Seattle, Seattle Boeing Field", "1"),
		KBJC("KBJC", "724699-03065", "US", "CO", "+39.900", "-105.117", "+1705.4", "Broomfield / Jeffco", "1"),
		KBLV("KBLV", "724338-13802", "US", "IL", "+38.550", "-089.850", "+0139.9", "Belleville, Scott AFB/MidAmerica Airport", "1"),
		KCHS("KCHS", "722080-13880", "US", "SC", "+32.899", "-080.041", "+0012.2", "Charleston, Charleston Air Force Base", "1"),
		KCRG("KCRG", "747820-53860", "US", "FL", "+30.336", "-081.515", "+0012.5", "Jacksonville, Craig Municipal Airport", "1"),
		KDAL("KDAL", "722580-13960", "US", "TX", "+32.852", "-096.856", "+0134.1", "Dallas, Dallas Love Field", "1"),
		KDCA("KDCA", "724050-13743", "US", "VA", "+38.847", "-077.035", "+0003.1", "Washington DC, Reagan National Airport", "1"),
		KDTW("KDTW", "725370-94847", "US", "MI", "+42.231", "-083.331", "+0192.3", "Detroit, Detroit Metropolitan Wayne County Airport", "1"),
		KDVT("KDVT", "722784-03184", "US", "AZ", "+33.688", "-112.082", "+0443.5", "Phoenix, Phoenix-Deer Valley Municipal Airport", "1"),
		KEUG("KEUG", "726930-24221", "US", "OR", "+44.128", "-123.221", "+0107.6", "Eugene, Mahlon Sweet Field", "1"),
		KFAR("KFAR", "727530-14914", "US", "ND", "+46.925", "-096.811", "+0274.3", "Fargo, Hector International Airport", "1"),
		KFFC("KFFC", "722197-53819", "US", "GA", "+33.355", "-084.567", "+0243.2", "Atlanta, Peachtree City-Falcon Field", "1"),
		KGEG("KGEG", "727850-24157", "US", "WA", "+47.622", "-117.528", "+0717.2", "Spokane, Spokane International Airport", "1"),
		KHSE("KHSE", "723139-93729", "US", "NC", "+35.233", "-075.622", "+0003.4", "Hatteras, Mitchell Field", "1"),
		KINL("KINL", "727470-14918", "US", "MN", "+48.561", "-093.398", "+0360.6", "International Falls, Falls International Airport", "1"),
		KJFK("KJFK", "744860-94789", "US", "NY", "+40.639", "-073.764", "+0003.4", "New York, Kennedy International Airport", "1"),
		KLGA("KLGA", "725030-14732", "US", "NY", "+40.779", "-073.880", "+0003.4", "New York, La Guardia Airport", "1"),
		KLRF("KLRF", "723405-03930", "US", "AR", "+34.917", "-092.150", "+0094.8", "Little Rock Air Force Base", "1"),
		KLUK("KLUK", "724297-93812", "US", "OH", "+39.103", "-084.419", "+0149.4", "Cincinnati, Cincinnati Municipal Airport Lunken Field", "1"),
		KMDW("KMDW", "725340-14819", "US", "IL", "+41.786", "-087.752", "+0186.5", "Chicago, Chicago Midway Airport", "1"),
		KMKC("KMKC", "724463-13988", "US", "MO", "+39.121", "-094.597", "+0226.2", "Kansas City, Kansas City Downtown Airport", "1"),
		KNEW("KNEW", "722315-53917", "US", "LA", "+30.049", "-090.029", "+0002.7", "New Orleans, New Orleans Lakefront Airport", "1"),
		KOAK("KOAK", "724930-23230", "US", "CA", "+37.721", "-122.221", "+0001.8", "Oakland, Metro Oakland International Airport", "1"),
		KOGD("KOGD", "725750-24126", "US", "UT", "+41.196", "-112.011", "+1362.5", "Ogden, Ogden-Hinckley Airport", "1"),
		KONT("KONT", "747040-03102", "US", "CA", "+34.056", "-117.600", "+0289.3", "Ontario, Ontario International Airport", "1"),
		KOPF("KOPF", "722024-12882", "US", "FL", "+25.907", "-080.280", "+0003.1", "Miami, Opa Locka Airport", "1"),
		KSAF("KSAF", "723656-23049", "US", "NM", "+35.617", "-106.089", "+1933.7", "Santa Fe, Santa Fe County Municipal Airport", "1"),
		KSHR("KSHR", "726660-24029", "US", "WY", "+44.769", "-106.969", "+1202.4", "Sheridan, Sheridan County Airport", "1"),
		KSTP("KSTP", "726584-14927", "US", "MN", "+44.932", "-093.056", "+0213.4", "St. Paul, St. Paul Downtown Holman Field", "1"),
		KVTN("KVTN", "725670-24032", "US", "NE", "+42.878", "-100.550", "+0789.4", "Valentine, Miller Field", "1"),
		KACK("KACK", "725060-14756", "US", "MA", "+41.253", "-070.061", "+0014.6", "Nantucket, Nantucket Memorial Airport", "2"),
		KASD("KASD", "722330-53865", "US", "LA", "+30.343", "-089.822", "+0008.2", "Slidell, Slidell Airport", "2"),
		KBED("KBED", "725059-14702", "US", "MA", "+42.470", "-071.289", "+0040.5", "Bedford, Hanscom Field", "2"),
		KBKF("KBKF", "724695-23036", "US", "CO", "+39.717", "-104.750", "+1726.1", "Aurora, Buckley Air Force Base Airport", "2"),
		KBNA("KBNA", "723270-13897", "US", "TN", "+36.119", "-086.689", "+0182.9", "Nashville, Nashville International Airport", "2"),
		KCRP("KCRP", "722510-12924", "US", "TX", "+27.774", "-097.512", "+0013.4", "Corpus Christi, Corpus Christi International Airport", "2"),
		KDLH("KDLH", "727450-14913", "US", "MN", "+46.837", "-092.183", "+0436.8", "Duluth, Duluth International Airport", "2"),
		KDRT("KDRT", "722610-22010", "US", "TX", "+29.378", "-100.927", "+0304.5", "Del Rio, Del Rio International Airport", "2"),
		KDSM("KDSM", "725460-14933", "US", "IA", "+41.534", "-093.653", "+0291.7", "Des Moines, Des Moines International Airport", "2"),
		KELP("KELP", "722700-23044", "US", "TX", "+31.811", "-106.376", "+1194.2", "El Paso, El Paso International Airport", "2"),
		KEWR("KEWR", "725020-14734", "US", "NJ", "+40.683", "-074.169", "+0002.1", "Newark, Newark International Airport", "2"),
		KEYW("KEYW", "722010-12836", "US", "FL", "+24.557", "-081.755", "+0000.3", "Key West, Key West International Airport", "2"),
		KFAT("KFAT", "723890-93193", "US", "CA", "+36.780", "-119.719", "+0101.5", "Fresno, Fresno Air Terminal", "2"),
		KFLG("KFLG", "723750-03103", "US", "AZ", "+35.144", "-111.666", "+2134.5", "Flagstaff, Flagstaff Pulliam Airport", "2"),
		KFSD("KFSD", "726510-14944", "US", "SD", "+43.578", "-096.754", "+0435.3", "Sioux Falls, Foss Field", "2"),
		KFTW("KFTW", "747390-13961", "US", "TX", "+32.819", "-097.361", "+0209.4", "Fort Worth, Meacham International Airport", "2"),
		KGFK("KGFK", "727570-14916", "US", "ND", "+47.943", "-097.184", "+0256.6", "Grand Forks, Grand Forks International Airport", "2"),
		KGGW("KGGW", "727680-94008", "US", "MT", "+48.214", "-106.621", "+0696.5", "Glasgow, Glasgow International Airport", "2"),
		KGJT("KGJT", "724760-23066", "US", "CO", "+39.134", "-108.540", "+1480.7", "Grand Junction, Walker Field", "2"),
		KHUL("KHUL", "727033-14609", "US", "ME", "+46.119", "-067.793", "+0145.1", "Houlton, Houlton International Airport", "2"),
		KIAH("KIAH", "722430-12960", "US", "TX", "+29.980", "-095.360", "+0029.0", "Houston, Houston Intercontinental Airport", "2"),
		KIND("KIND", "724380-93819", "US", "IN", "+39.725", "-086.282", "+0241.1", "Indianapolis, Indianapolis International Airport", "2"),
		KLAS("KLAS", "723860-23169", "US", "NV", "+36.072", "-115.163", "+0664.5", "Las Vegas, McCarran International Airport", "2"),
		KMCO("KMCO", "722050-12815", "US", "FL", "+28.434", "-081.325", "+0027.4", "Orlando, Orlando International Airport", "2"),
		KMEM("KMEM", "723340-13893", "US", "TN", "+35.056", "-089.987", "+0077.4", "Memphis, Memphis International Airport", "2"),
		KMRY("KMRY", "724915-23259", "US", "CA", "+36.588", "-121.845", "+0050.3", "Monterey, Monterey Peninsula Airport", "2"),
		KOKC("KOKC", "723530-13967", "US", "OK", "+35.389", "-097.601", "+0391.7", "Oklahoma City, Will Rogers World Airport", "2"),
		KOMA("KOMA", "725500-14942", "US", "NE", "+41.310", "-095.899", "+0299.3", "Omaha, Eppley Airfield", "2"),
		KPDT("KPDT", "726880-24155", "US", "OR", "+45.698", "-118.855", "+0452.9", "Pendleton, Eastern Oregon Regional At Pendleton Airport", "2"),
		KPDX("KPDX", "726980-24229", "US", "OR", "+45.596", "-122.609", "+0005.8", "Portland, Portland International Airport", "2"),
		KPIT("KPIT", "725200-94823", "US", "PA", "+40.485", "-080.214", "+0366.7", "Pittsburgh, Pittsburgh International Airport", "2"),
		KPTK("KPTK", "726375-94817", "US", "MI", "+42.665", "-083.418", "+0297.5", "Pontiac, Oakland County International Airport", "2"),
		KRAP("KRAP", "726620-24090", "US", "SD", "+44.046", "-103.054", "+0963.2", "Rapid City, Rapid City Regional Airport", "2"),
		KRDU("KRDU", "723060-13722", "US", "NC", "+35.892", "-078.782", "+0126.8", "Raleigh / Durham, Raleigh-Durham International Airport", "2"),
		KRIW("KRIW", "726720-24061", "US", "WY", "+43.064", "-108.459", "+1659.6", "Riverton, Riverton Regional Airport", "2"),
		KRNO("KRNO", "724880-23185", "US", "NV", "+39.484", "-119.771", "+1344.2", "Reno, Reno Tahoe International Airport", "2"),
		KSAC("KSAC", "724830-23232", "US", "CA", "+38.507", "-121.495", "+0004.6", "Sacramento, Sacramento Executive Airport", "2"),
		KSAN("KSAN", "722900-23188", "US", "CA", "+32.734", "-117.183", "+0004.6", "San Diego, San Diego International-Lindbergh Field", "2"),
		KSLE("KSLE", "726940-24232", "US", "OR", "+44.905", "-123.001", "+0062.5", "Salem, McNary Field", "2"),
		KTUS("KTUS", "722740-23160", "US", "AZ", "+32.131", "-110.955", "+0776.9", "Tucson, Tucson International Airport", "2"),
		KUIL("KUIL", "727970-94240", "US", "WA", "+47.938", "-124.555", "+0056.4", "Quillayute, Quillayute State Airport", "2"),
		KABR("KABR", "726590-14929", "US", "SD", "+45.443", "-098.413", "+0395.3", "Aberdeen, Aberdeen Regional Airport", "3"),
		KALB("KALB", "725180-14735", "US", "NY", "+42.747", "-073.799", "+0085.3", "Albany, Albany International Airport", "3"),
		KAMA("KAMA", "723630-23047", "US", "TX", "+35.230", "-101.704", "+1098.5", "Amarillo, Amarillo International Airport", "3"),
		KANE("KANE", "726577-94974", "US", "MN", "+45.150", "-093.217", "+0278.0", "Minneapolis / Blaine", "3"),
		KAUS("KAUS", "722540-13904", "US", "TX", "+30.183", "-097.680", "+0146.3", "Austin, Austin-Bergstrom International Airport", "3"),
		KAXN("KAXN", "726557-14910", "US", "MN", "+45.868", "-095.394", "+0431.6", "Alexandria, Chandler Field", "3"),
		KBDR("KBDR", "725040-94702", "US", "CT", "+41.164", "-073.127", "+0001.5", "Bridgeport, Sikorsky Memorial Airport", "3"),
		KBGM("KBGM", "725150-04725", "US", "NY", "+42.207", "-075.980", "+0486.2", "Binghamton, Binghamton Regional Airport", "3"),
		KBGR("KBGR", "726070-14606", "US", "ME", "+44.798", "-068.819", "+0045.1", "Bangor, Bangor International Airport", "3"),
		KBTV("KBTV", "726170-14742", "US", "VT", "+44.468", "-073.150", "+0100.6", "Burlington, Burlington International Airport", "3"),
		KBWI("KBWI", "724060-93721", "US", "MD", "+39.173", "-076.684", "+0047.6", "Baltimore, Baltimore-Washington International Airport", "3"),
		KCAE("KCAE", "723100-13883", "US", "SC", "+33.942", "-081.118", "+0068.6", "Columbia, Columbia Metropolitan Airport", "3"),
		KCDS("KCDS", "723660-23007", "US", "TX", "+34.427", "-100.283", "+0594.7", "Childress, Childress Municipal Airport", "3"),
		KCLE("KCLE", "725240-14820", "US", "OH", "+41.406", "-081.852", "+0238.1", "Cleveland, Cleveland-Hopkins International Airport", "3"),
		KCOD("KCOD", "726700-24045", "US", "WY", "+44.517", "-109.017", "+1552.0", "Cody", "3"),
		KCOS("KCOS", "724660-93037", "US", "CO", "+38.810", "-104.688", "+1884.0", "Colorado Springs, City Of Colorado Springs Municipal Airport", "3"),
		KCYS("KCYS", "725640-24018", "US", "WY", "+41.158", "-104.807", "+1863.2", "Cheyenne, Cheyenne Airport", "3"),
		KDAB("KDAB", "747870-12834", "US", "FL", "+29.183", "-081.048", "+0009.5", "Daytona Beach, Daytona Beach Regional Airport", "3"),
		KDET("KDET", "725375-14822", "US", "MI", "+42.409", "-083.010", "+0190.8", "Detroit, Detroit City Airport", "3"),
		KDLS("KDLS", "726988-24219", "US", "WA", "+45.619", "-121.166", "+0071.6", "The Dalles, Columbia Gorge Regional / The Dalles Municipal Airport", "3"),
		KDPG("KDPG", "740030-24103", "US", "UT", "+40.183", "-112.933", "+1325.6", "Dugway Proving Grounds", "3"),
		KDVN("KDVN", "744550-94982", "US", "IA", "+41.614", "-090.591", "+0228.6", "Davenport, Davenport Municipal Airport", "3"),
		KEDW("KEDW", "723810-23114", "US", "CA", "+34.900", "-117.867", "+0704.4", "Edwards Air Force Base", "3"),
		KEET("KEET", "722300-53864", "US", "AL", "+33.178", "-086.782", "+0172.2", "Alabaster, Shelby County Airport", "3"),
		KEKO("KEKO", "725825-24121", "US", "NV", "+40.829", "-115.789", "+1533.1", "Elko, Elko Regional Airport", "3"),
		KELY("KELY", "724860-23154", "US", "NV", "+39.295", "-114.847", "+1908.7", "Ely, Ely Airport", "3"),
		KFSM("KFSM", "723440-13964", "US", "AR", "+35.333", "-094.363", "+0136.9", "Fort Smith, Fort Smith Regional Airport", "3"),
		KFTY("KFTY", "722195-03888", "US", "GA", "+33.779", "-084.521", "+0256.0", "Atlanta, Fulton County Airport-Brown Field", "3"),
		KFWA("KFWA", "725330-14827", "US", "IN", "+40.971", "-085.206", "+0241.1", "Fort Wayne, Fort Wayne International Airport", "3"),
		KGLD("KGLD", "724650-23065", "US", "KS", "+39.367", "-101.693", "+1114.4", "Goodland, Renner Field", "3"),
		KGLR("KGLR", "725407-04854", "US", "MI", "+45.013", "-084.701", "+0406.9", "Gaylord, Otsego County Airport", "3"),
		KGRB("KGRB", "726450-14898", "US", "WI", "+44.479", "-088.137", "+0209.4", "Green Bay, Austin Straubel International Airport", "3"),
		KGRR("KGRR", "726350-94860", "US", "MI", "+42.883", "-085.524", "+0244.8", "Grand Rapids, Gerald R. Ford International Airport", "3"),
		KGSO("KGSO", "723170-13723", "US", "NC", "+36.097", "-079.943", "+0271.3", "Greensboro, Piedmont Triad International Airport", "3"),
		KGSP("KGSP", "723120-03870", "US", "SC", "+34.906", "-082.213", "+0291.1", "Greer, Greenville-Spartanburg Airport", "3"),
		KHIB("KHIB", "727455-94931", "US", "MN", "+47.386", "-092.839", "+0412.1", "Hibbing, Chisholm-Hibbing Airport", "3"),
		KHOU("KHOU", "722440-12918", "US", "TX", "+29.638", "-095.282", "+0013.4", "Houston, Houston Hobby Airport", "3"),
		KHTS("KHTS", "724250-03860", "US", "WV", "+38.365", "-082.555", "+0251.2", "Huntington, Tri-State Airport", "3"),
		KHVR("KHVR", "727770-94012", "US", "MT", "+48.543", "-109.763", "+0787.9", "Havre, Havre City-County Airport", "3"),
		KICT("KICT", "724500-03928", "US", "KS", "+37.648", "-097.430", "+0402.6", "Wichita, Wichita Mid-Continent Airport", "3"),
		KILN("KILN", "724296-13841", "US", "OH", "+39.431", "-083.777", "+0325.2", "Wilmington, Airborne Airpark Airport", "3"),
		KIWA("KIWA", "722786-23104", "US", "AZ", "+33.300", "-111.667", "+0421.2", "Mesa, Williams Gateway Airport", "3"),
		KJAN("KJAN", "722350-03940", "US", "MS", "+32.321", "-090.078", "+0100.6", "Jackson, Jackson International Airport", "3"),
		KLBF("KLBF", "725620-24023", "US", "NE", "+41.121", "-100.669", "+0846.7", "North Platte, North Platte Regional Airport", "3"),
		KLCH("KLCH", "722400-03937", "US", "LA", "+30.125", "-093.228", "+0002.7", "Lake Charles, Lake Charles Regional Airport", "3"),
		KLNK("KLNK", "725510-14939", "US", "NE", "+40.851", "-096.748", "+0362.7", "Lincoln, Lincoln Municipal Airport", "3"),
		KLOU("KLOU", "724235-13810", "US", "KY", "+38.228", "-085.664", "+0164.6", "Louisville, Bowman Field Airport", "3"),
		KLRD("KLRD", "722520-12907", "US", "TX", "+27.533", "-099.467", "+0150.6", "Laredo, Laredo International Airport", "3"),
		KLVS("KLVS", "723677-23054", "US", "NM", "+35.654", "-105.142", "+2095.2", "Las Vegas, Las Vegas Municipal Airport", "3"),
		KMCN("KMCN", "722170-03813", "US", "GA", "+32.685", "-083.653", "+0104.6", "Macon, Middle Georgia Regional Airport", "3"),
		KMGW("KMGW", "724176-13736", "US", "WV", "+39.643", "-079.916", "+0378.0", "Morgantown, Morgantown Municipal-Hart Field", "3"),
		KMKE("KMKE", "726400-14839", "US", "WI", "+42.955", "-087.904", "+0204.2", "Milwaukee, General Mitchell International Airport", "3"),
		KMOB("KMOB", "722230-13894", "US", "AL", "+30.688", "-088.246", "+0065.5", "Mobile, Mobile Regional Airport", "3"),
		KMSO("KMSO", "727730-24153", "US", "MT", "+46.921", "-114.093", "+0972.9", "Missoula, Missoula International Airport", "3"),
		KMWL("KMWL", "722597-93985", "US", "TX", "+32.782", "-098.060", "+0283.5", "Mineral Wells, Mineral Wells Airport", "3"),
		KNKT("KNKT", "723090-13754", "US", "NC", "+34.900", "-076.883", "+0008.8", "Cherry Point, Marine Corps Air Station", "3"),
		KORF("KORF", "723080-13737", "US", "VA", "+36.903", "-076.192", "+0009.1", "Norfolk, Norfolk International Airport", "3"),
		KOUN("KOUN", "723570-03948", "US", "OK", "+35.250", "-097.467", "+0360.3", "Norman / Max Westheimer", "3"),
		KPAH("KPAH", "724350-03816", "US", "KY", "+37.056", "-088.774", "+0125.9", "Paducah, Barkley Regional Airport", "3"),
		KPBI("KPBI", "722030-12844", "US", "FL", "+26.685", "-080.099", "+0005.8", "West Palm Beach, Palm Beach International Airport", "3"),
		KPGA("KPGA", "723710-03162", "US", "AZ", "+36.926", "-111.448", "+1313.7", "Page, Page Municipal Airport", "3"),
		KPHL("KPHL", "724080-13739", "US", "PA", "+39.873", "-075.227", "+0003.1", "Philadelphia, Philadelphia International Airport", "3"),
		KPIH("KPIH", "725780-24156", "US", "ID", "+42.920", "-112.571", "+1357.0", "Pocatello, Pocatello Regional Airport", "3"),
		KPIR("KPIR", "726560-24025", "US", "SD", "+44.381", "-100.286", "+0531.0", "Pierre, Pierre Regional Airport", "3"),
		KPRC("KPRC", "723723-23184", "US", "AZ", "+34.652", "-112.421", "+1536.8", "Prescott, Love Field", "3"),
		KPSP("KPSP", "722868-93138", "US", "CA", "+33.822", "-116.504", "+0124.7", "Palm Springs, Palm Springs Regional Airport", "3"),
		KPUB("KPUB", "724640-93058", "US", "CO", "+38.289", "-104.506", "+1441.4", "Pueblo, Pueblo Memorial Airport", "3"),
		KPVD("KPVD", "725070-14765", "US", "RI", "+41.723", "-071.433", "+0016.8", "Providence, Theodore Francis Green State Airport", "3"),
		KPWM("KPWM", "726060-14764", "US", "ME", "+43.642", "-070.304", "+0013.7", "Portland, Portland International Jetport", "3"),
		KRDD("KRDD", "725920-24257", "US", "CA", "+40.518", "-122.299", "+0151.5", "Redding, Redding Municipal Airport", "3"),
		KROA("KROA", "724110-13741", "US", "VA", "+37.317", "-079.974", "+0358.1", "Roanoke, Roanoke Regional Airport", "3"),
		KSHV("KSHV", "722480-13957", "US", "LA", "+32.447", "-093.824", "+0077.4", "Shreveport, Shreveport Regional Airport", "3"),
		KSMP("KSMP", "727815-24237", "US", "WA", "+47.277", "-121.337", "+1206.7", "Stampede Pass", "3"),
		KSTC("KSTC", "726550-14926", "US", "MN", "+45.543", "-094.051", "+0310.3", "St Cloud, St Cloud Regional Airport", "3"),
		KSUS("KSUS", "724345-03966", "US", "MO", "+38.657", "-090.656", "+0140.8", "St. Louis, Spirit Of St. Louis Airport", "3"),
		KSUX("KSUX", "725570-14943", "US", "IA", "+42.391", "-096.379", "+0333.8", "Sioux City, Sioux Gateway Airport", "3"),
		KSYR("KSYR", "725190-14771", "US", "NY", "+43.111", "-076.104", "+0125.9", "Syracuse, Syracuse Hancock International Airport", "3"),
		KTLH("KTLH", "722140-93805", "US", "FL", "+30.393", "-084.353", "+0016.8", "Tallahassee, Tallahassee Regional Airport", "3"),
		KTOP("KTOP", "724560-13996", "US", "KS", "+39.073", "-095.626", "+0267.0", "Topeka, Philip Billard Municipal Airport", "3"),
		KTPA("KTPA", "722110-12842", "US", "FL", "+27.962", "-082.540", "+0005.8", "Tampa, Tampa International Airport", "3"),
		KTUL("KTUL", "723560-13968", "US", "OK", "+36.199", "-095.887", "+0198.1", "Tulsa, Tulsa International Airport", "3"),
		KUNV("KUNV", "725128-54739", "US", "PA", "+40.850", "-077.850", "+0377.7", "State College, University Park Airport", "3"),
		KVBG("KVBG", "723930-93214", "US", "CA", "+34.717", "-120.567", "+0112.5", "Lompoc, Vandenberg Air Force Base", "3"),
		KWMC("KWMC", "725830-24128", "US", "NV", "+40.902", "-117.808", "+1309.4", "Winnemucca, Winnemucca Municipal Airport", "3"),
		KYKM("KYKM", "727810-24243", "US", "WA", "+46.568", "-120.543", "+0324.3", "Yakima, Yakima Air Terminal", "3"),
		KABI("KABI", "722660-13962", "US", "TX", "+32.411", "-099.682", "+0545.6", "Abilene, Abilene Regional Airport", "4"),
		KACY("KACY", "724070-93730", "US", "NJ", "+39.452", "-074.567", "+0018.3", "Atlantic City, Atlantic City International Airport", "4"),
		KADM("KADM", "723555-93940", "US", "OK", "+34.300", "-097.017", "+0221.0", "Ardmore, Ardmore Municipal Airport", "4"),
		KAFF("KAFF", "745310-93065", "US", "CO", "+38.967", "-104.817", "+2003.2", "Air Force Academy", "4"),
		KAGS("KAGS", "722180-03820", "US", "GA", "+33.364", "-081.963", "+0040.2", "Augusta, Bush Field", "4"),
		KAHN("KAHN", "723110-13873", "US", "GA", "+33.948", "-083.328", "+0239.3", "Athens, Athens Airport", "4"),
		KAKO("KAKO", "724698-24015", "US", "CO", "+40.167", "-103.217", "+1421.3", "Akron, Akron-Washington County Airport", "4"),
		KALO("KALO", "725480-94910", "US", "IA", "+42.554", "-092.401", "+0264.6", "Waterloo, Waterloo Municipal Airport", "4"),
		KAPA("KAPA", "724666-93067", "US", "CO", "+39.570", "-104.849", "+1793.1", "Denver, Centennial Airport", "4"),
		KAPN("KAPN", "726390-94849", "US", "MI", "+45.072", "-083.564", "+0208.5", "Alpena, Alpena County Regional Airport", "4"),
		KASE("KASE", "724676-93073", "US", "CO", "+39.230", "-106.871", "+2353.1", "Aspen, Aspen-Pitkin County Airport", "4"),
		KAST("KAST", "727910-94224", "US", "OR", "+46.157", "-123.883", "+0002.7", "Astoria, Astoria Regional Airport", "4"),
		KAUG("KAUG", "726185-14605", "US", "ME", "+44.316", "-069.797", "+0107.0", "Augusta, Augusta State Airport", "4"),
		KAVL("KAVL", "723150-03812", "US", "NC", "+35.432", "-082.538", "+0645.3", "Asheville, Asheville Regional Airport", "4"),
		KBHM("KBHM", "722280-13876", "US", "AL", "+33.566", "-086.745", "+0187.5", "Birmingham, Birmingham International Airport", "4"),
		KBRD("KBRD", "726555-94938", "US", "MN", "+46.405", "-094.131", "+0372.2", "Brainerd, Brainerd-Crow Wing County Regional Airport", "4"),
		KBTR("KBTR", "722320-13970", "US", "LA", "+30.537", "-091.147", "+0019.5", "Baton Rouge, Baton Rouge Metropolitan, Ryan Field", "4"),
		KBWG("KBWG", "746716-93808", "US", "KY", "+36.965", "-086.424", "+0160.9", "Bowling Green, Bowling Green-Warren County Regional Airport", "4"),
		KBZN("KBZN", "726797-24132", "US", "MT", "+45.788", "-111.161", "+1349.4", "Bozeman, Gallatin Field", "4"),
		KCAG("KCAG", "725700-24046", "US", "CO", "+40.493", "-107.524", "+1886.7", "Craig, Craig-Moffat Airport", "4"),
		KCDC("KCDC", "724755-93129", "US", "UT", "+37.709", "-113.094", "+1702.6", "Cedar City, Cedar City Municipal Airport", "4"),
		KCHA("KCHA", "723240-13882", "US", "TN", "+35.034", "-085.200", "+0204.2", "Chattanooga, Lovell Field", "4"),
		KCID("KCID", "725450-14990", "US", "IA", "+41.883", "-091.717", "+0264.6", "Cedar Rapids, The Eastern Iowa Airport", "4"),
		KCKB("KCKB", "724175-03802", "US", "WV", "+39.296", "-080.229", "+0366.7", "Clarksburg, Clarksburg Benedum Airport", "4"),
		KCLL("KCLL", "747460-03904", "US", "TX", "+30.589", "-096.365", "+0093.0", "College Station, Easterwood Field", "4"),
		KCLT("KCLT", "723140-13881", "US", "NC", "+35.224", "-080.955", "+0221.9", "Charlotte, Charlotte / Douglas International Airport", "4"),
		KCMH("KCMH", "724280-14821", "US", "OH", "+39.991", "-082.877", "+0248.7", "Columbus, Port Columbus International Airport", "4"),
		KCOE("KCOE", "727834-24136", "US", "ID", "+47.767", "-116.817", "+0703.2", "Coeur d'Alene, Coeur d'Alene Air Terminal", "4"),
		KCOU("KCOU", "724450-03945", "US", "MO", "+38.817", "-092.218", "+0272.2", "Columbia, Columbia Regional Airport", "4"),
		KCTB("KCTB", "727690-24137", "US", "MT", "+48.603", "-112.375", "+1169.8", "Cut Bank, Cut Bank Municipal Airport", "4"),
		KCVS("KCVS", "722686-23008", "US", "NM", "+34.383", "-103.317", "+1309.1", "Cannon Air Force Base / Clovis", "4"),
		KDAY("KDAY", "724290-93815", "US", "OH", "+39.906", "-084.219", "+0305.7", "Dayton, Cox Dayton International Airport", "4"),
		KDBQ("KDBQ", "725470-94908", "US", "IA", "+42.398", "-090.704", "+0321.9", "Dubuque, Dubuque Regional Airport", "4"),
		KDEC("KDEC", "725316-03887", "US", "IL", "+39.834", "-088.866", "+0205.7", "Decatur, Decatur Airport", "4"),
		KDRO("KDRO", "724625-93005", "US", "CO", "+37.143", "-107.760", "+2033.0", "Durango, Durango-La Plata County Airport", "4"),
		KEAU("KEAU", "726435-14991", "US", "WI", "+44.867", "-091.488", "+0269.8", "Eau Claire, Chippewa Valley Regional Airport", "4"),
		KEKN("KEKN", "724170-13729", "US", "WV", "+38.890", "-079.855", "+0597.7", "Elkins, Elkins-Randolph County-Jennings Randolph Field", "4"),
		KEND("KEND", "723535-13909", "US", "OK", "+36.333", "-097.917", "+0398.1", "Vance Air Force Base / Enid", "4"),
		KENV("KENV", "725810-24193", "US", "UT", "+40.721", "-114.036", "+1291.4", "Wendover / Air Force Auxillary Field", "4"),
		KERI("KERI", "725260-14860", "US", "PA", "+42.080", "-080.182", "+0222.2", "Erie, Erie International Airport", "4"),
		KEVV("KEVV", "724320-93817", "US", "IN", "+38.044", "-087.521", "+0121.9", "Evansville, Evansville Regional Airport", "4"),
		KFAY("KFAY", "723035-93740", "US", "NC", "+34.991", "-078.880", "+0056.7", "Fayetteville, Fayetteville Regional Airport", "4"),
		KFNT("KFNT", "726370-14826", "US", "MI", "+42.967", "-083.749", "+0234.7", "Flint, Bishop International Airport", "4"),
		KFYV("KFYV", "723445-93993", "US", "AR", "+36.010", "-094.169", "+0381.3", "Fayetteville, Drake Field", "4"),
		KGCK("KGCK", "724515-23064", "US", "KS", "+37.927", "-100.725", "+0878.4", "Garden City, Garden City Regional Airport", "4"),
		KGCN("KGCN", "723783-03195", "US", "AZ", "+35.946", "-112.155", "+2013.5", "Grand Canyon, Grand Canyon National Park Airport", "4"),
		KGGG("KGGG", "722470-03901", "US", "TX", "+32.385", "-094.712", "+0111.3", "Longview, Gregg County Airport", "4"),
		KGNV("KGNV", "747560-12816", "US", "FL", "+29.692", "-082.276", "+0037.5", "Gainesville, Gainesville Regional Airport", "4"),
		KGWO("KGWO", "747580-13978", "US", "MS", "+33.496", "-090.087", "+0040.5", "Greenwood, Greenwood-LeFlore Airport", "4"),
		KGXY("KGXY", "724768-24051", "US", "CO", "+40.436", "-104.632", "+1431.7", "Greeley, Greeley-Weld County Airport", "4"),
		KGYY("KGYY", "725337-04807", "US", "IN", "+41.617", "-087.417", "+0180.1", "Gary Regional", "4"),
		KHFD("KHFD", "725087-14752", "US", "CT", "+41.736", "-072.651", "+0005.8", "Hartford, Hartford-Brainard Airport", "4"),
		KHLN("KHLN", "727720-24144", "US", "MT", "+46.606", "-111.964", "+1166.8", "Helena, Helena Regional Airport", "4"),
		KHMN("KHMN", "747320-23002", "US", "NM", "+32.850", "-106.100", "+1267.4", "Holloman Air Force Base", "4"),
		KHQM("KHQM", "727923-94225", "US", "WA", "+46.973", "-123.930", "+0003.7", "Hoquiam, Bowerman Airport", "4"),
		KHSV("KHSV", "723230-03856", "US", "AL", "+34.644", "-086.786", "+0190.2", "Huntsville, Huntsville International / Jones Field", "4"),
		KHYS("KHYS", "724518-03968", "US", "KS", "+38.850", "-099.267", "+0609.0", "Hays, Hays Regional Airport", "4"),
		KIAG("KIAG", "725287-04724", "US", "NY", "+43.108", "-078.938", "+0178.3", "Niagara Falls, Niagara Falls International Airport", "4"),
		KILG("KILG", "724180-13781", "US", "DE", "+39.674", "-075.606", "+0024.1", "Wilmington, New Castle County Airport", "4"),
		KINT("KINT", "723193-93807", "US", "NC", "+36.134", "-080.222", "+0295.7", "Winston Salem, Smith Reynolds Airport", "4"),
		KINW("KINW", "723740-23194", "US", "AZ", "+35.028", "-110.721", "+1489.3", "Winslow, Winslow Municipal Airport", "4"),
		KISP("KISP", "725050-04781", "US", "NY", "+40.794", "-073.102", "+0025.6", "Islip, Long Island Mac Arthur Airport", "4"),
		KLAA("KLAA", "724636-03013", "US", "CO", "+38.070", "-102.688", "+1129.0", "Lamar, Lamar Municipal Airport", "4"),
		KLAF("KLAF", "724386-14835", "US", "IN", "+40.412", "-086.937", "+0182.6", "Lafayette, Purdue University Airport", "4"),
		KLAM("KLAM", "723654-93091", "US", "NM", "+35.883", "-106.283", "+2185.7", "Los Alamos, Los Alamos Airport", "4"),
		KLAN("KLAN", "725390-14836", "US", "MI", "+42.776", "-084.600", "+0261.8", "Lansing, Capital City Airport", "4"),
		KLBL("KLBL", "724516-23020", "US", "KS", "+37.050", "-100.967", "+0875.7", "Liberal, Liberal Municipal Airport", "4"),
		KLEX("KLEX", "724220-93820", "US", "KY", "+38.041", "-084.606", "+0298.7", "Lexington, Blue Grass Airport", "4"),
		KLIC("KLIC", "724665-93010", "US", "CO", "+39.275", "-103.666", "+1638.0", "Limon, Limon Municipal Airport", "4"),
		KMGM("KMGM", "722260-13895", "US", "AL", "+32.300", "-086.408", "+0061.6", "Montgomery, Dannelly Field", "4"),
		KMHK("KMHK", "724555-03936", "US", "KS", "+39.135", "-096.679", "+0321.9", "Manhattan, Manhattan Municipal Airport", "4"),
		KMKG("KMKG", "726360-14840", "US", "MI", "+43.171", "-086.237", "+0190.5", "Muskegon, Muskegon County Airport", "4"),
		KMLB("KMLB", "722040-12838", "US", "FL", "+28.101", "-080.644", "+0008.2", "Melbourne, Melbourne International Airport", "4"),
		KMLS("KMLS", "742300-24037", "US", "MT", "+46.427", "-105.883", "+0799.8", "Miles City, Frank Wiley Field Airport", "4"),
		KMPV("KMPV", "726145-94705", "US", "VT", "+44.204", "-072.562", "+0343.2", "Barre / Montpelier, Knapp State Airport", "4"),
		KMRB("KMRB", "724177-13734", "US", "WV", "+39.404", "-077.945", "+0162.8", "Martinsburg, Eastern West Virginia Regional/Shepherd Airport", "4"),
		KMSN("KMSN", "726410-14837", "US", "WI", "+43.141", "-089.345", "+0264.0", "Madison, Dane County Regional-Truax Field", "4"),
		KNID("KNID", "746120-93104", "US", "CA", "+35.688", "-117.693", "+0679.7", "China Lake, Naval Air Facility", "4"),
		KOFF("KOFF", "725540-14949", "US", "NE", "+41.117", "-095.917", "+0319.1", "Omaha / Offutt Air Force Base", "4"),
		KPIA("KPIA", "725320-14842", "US", "IL", "+40.668", "-089.684", "+0198.1", "Peoria, Greater Peoria Regional Airport", "4"),
		KPKD("KPKD", "727453-94967", "US", "MN", "+46.901", "-095.068", "+0437.1", "Park Rapids, Park Rapids Municipal Airport", "4"),
		KPNE("KPNE", "724085-94732", "US", "PA", "+40.079", "-075.013", "+0032.0", "Philadelphia, Northeast Philadelphia Airport", "4"),
		KPOE("KPOE", "722390-03931", "US", "LA", "+31.050", "-093.183", "+0100.6", "Fort Polk, Polk AAF Ft Polk", "4"),
		KPOU("KPOU", "725036-14757", "US", "NY", "+41.626", "-073.882", "+0050.6", "Poughkeepsie, Dutchess County Airport", "4"),
		KRFD("KRFD", "725430-94822", "US", "IL", "+42.193", "-089.093", "+0222.5", "Rockford, Greater Rockford Airport", "4"),
		KRIC("KRIC", "724010-13740", "US", "VA", "+37.512", "-077.323", "+0050.0", "Richmond, Richmond International Airport", "4"),
		KROW("KROW", "722680-23009", "US", "NM", "+33.308", "-104.508", "+1112.2", "Roswell, Roswell Industrial Air Center Airport", "4"),
		KSAV("KSAV", "722070-03822", "US", "GA", "+32.131", "-081.202", "+0014.0", "Savannah, Savannah International Airport", "4"),
		KSBN("KSBN", "725350-14848", "US", "IN", "+41.707", "-086.316", "+0235.6", "South Bend, South Bend Regional Airport", "4"),
		KSBY("KSBY", "723980-93720", "US", "MD", "+38.341", "-075.513", "+0014.3", "Salisbury, Salisbury-Ocean City Wicomico County Regional Airport", "4"),
		KSPS("KSPS", "723510-13966", "US", "TX", "+33.979", "-098.493", "+0310.0", "Wichita Falls, Sheppard Air Force Base", "4"),
		KSTJ("KSTJ", "724490-13993", "US", "MO", "+39.774", "-094.923", "+0249.3", "St. Joseph, Rosecrans Memorial Airport", "4"),
		KTRI("KTRI", "723350-13877", "US", "TN", "+36.480", "-082.399", "+0456.3", "Bristol / Johnson / Kingsport, Tri-City Regional Airport", "4"),
		KTUP("KTUP", "723320-93862", "US", "MS", "+34.262", "-088.771", "+0110.0", "Tupelo, Tupelo Regional Airport", "4"),
		KTYR("KTYR", "722448-13972", "US", "TX", "+32.354", "-095.403", "+0165.8", "Tyler, Tyler Pounds Field", "4"),
		KTYS("KTYS", "723260-13891", "US", "TN", "+35.818", "-083.986", "+0293.2", "Knoxville, McGhee Tyson Airport", "4"),
		KVRB("KVRB", "747930-12843", "US", "FL", "+27.651", "-080.420", "+0008.5", "Vero Beach, Vero Beach Municipal Airport", "4"),
		KAKQ("KAKQ", "724019-93773", "US", "VA", "+36.983", "-077.001", "+0033.8", "Wakefield, Wakefield Municipal Airport", "5"),
		KAWM("KAWM", "722054-53959", "US", "AR", "+35.135", "-090.234", "+0065.2", "West Memphis, West Memphis Municipal Airport", "5"),
		KDRA("KDRA", "723870-03160", "US", "NV", "+36.621", "-116.028", "+0984.5", "Mercury, Desert Rock Airport", "5"),
		KFDR("KFDR", "723528-03981", "US", "OK", "+34.344", "-098.983", "+0382.5", "Frederick, Frederick Municipal Airport", "5"),
		KGMU("KGMU", "723119-13886", "US", "SC", "+34.846", "-082.346", "+0319.4", "Greenville, Greenville Downtown Airport", "5"),
		KGRK("KGRK", "722576-03902", "US", "TX", "+31.067", "-097.833", "+0309.4", "Fort Hood, Robert Gray AAF Ft Hood", "5"),
		KJKL("KJKL", "724190-03889", "US", "KY", "+37.591", "-083.314", "+0416.1", "Jackson, Carroll Airport", "5"),
		KLOT("KLOT", "725348-04831", "US", "IL", "+41.604", "-088.085", "+0201.2", "Chicago/Romeoville, Lewis University Airport", "5"),
		KMAF("KMAF", "722650-23023", "US", "TX", "+31.948", "-102.209", "+0872.3", "Midland, Midland International Airport", "5"),
		KNKX("KNKX", "722931-93107", "US", "CA", "+32.867", "-117.133", "+0145.4", "San Diego, Miramar MCAS/Mitscher Field Airport", "5"),
		KNQA("KNQA", "723284-93839", "US", "TN", "+35.350", "-089.867", "+0097.5", "Millington, Millington Municipal Airport", "5"),
		KPNA("KPNA", "720345-94086", "US", "WY", "+42.796", "-109.807", "+2159.8", "Pinedale, Ralph Wenz Field Airport", "5"),
		KSGF("KSGF", "724400-13995", "US", "MO", "+37.240", "-093.390", "+0384.7", "Springfield, Springfield Regional Airport", "5"),
		KSJT("KSJT", "722630-23034", "US", "TX", "+31.352", "-100.495", "+0584.0", "San Angelo, Mathis Field", "5"),
		KWAL("KWAL", "724020-93739", "US", "VA", "+37.937", "-075.466", "+0014.0", "Wallops Island, Wallops Flight Facility Airport", "5"),
		KELM("KELM", "725156-14748", "US", "NY", "+42.159", "-076.892", "+0291.1", "Elmira, Elmira / Corning Regional Airport", "6"),
		KITH("KITH", "725155-94761", "US", "NY", "+42.483", "-076.467", "+0335.0", "Ithaca, Ithaca Tompkins Regional Airport", "6"),
		KROC("KROC", "725290-14768", "US", "NY", "+43.117", "-077.677", "+0164.3", "Rochester, Greater Rochester International Airport", "6");
	}

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
		"AB" to "Alberta",
		"BC" to "British Columbia",
		"MB" to "Manitoba",
		"NB" to "New Brunswick",
		"NL" to "Newfoundland and Labrador",
		"NT" to "Northwest Territories",
		"NS" to "Nova Scotia",
		"NU" to "Nunavut",
		"ON" to "Ontario",
		"PE" to "Prince Edward Island",
		"QC" to "Qu�bec",
		"SK" to "Saskatchewan",
		"YT" to "Yukon",

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
			val prefix = entry.prefix.lowercase()
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
			val icao = entry.prefix.uppercase()
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
			val icao = entry.prefix.uppercase()
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
		val names = Icao.entries.map { it.prefix }.sorted()

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
			val icao = entry.prefix.uppercase()
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
			val icao = entry.prefix.uppercase()
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
			val prefix = entry.prefix.lowercase()
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
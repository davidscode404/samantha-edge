package com.cactus.example.pages

import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPage(onBack: () -> Unit) {
    val context = LocalContext.current

    // Date/time state
    var currentDateTime by remember { mutableStateOf(getMapDateTime(context)) }

    // Auto-refresh date/time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = getMapDateTime(context)
            delay(1000L)
        }
    }

    // Start: Near King's Cross  |  End: Near Tower Bridge
    val startPoint = GeoPoint(51.530196851073775, -0.12006173031079728)
    val endPoint = GeoPoint(51.5082258920356, -0.07524119963609556)

    // Route following actual London roads:
    // Pancras Rd → Euston Rd → Pentonville Rd → City Rd →
    // Moorgate → London Wall → Bishopsgate → Gracechurch St →
    // Fenchurch St → Tower Hill
    val routePoints = listOf(
        GeoPoint(51.530358, -0.120176),
        GeoPoint(51.530242, -0.120598),
        GeoPoint(51.530232, -0.120636),
        GeoPoint(51.530199, -0.120728),
        GeoPoint(51.530221, -0.120755),
        GeoPoint(51.530255, -0.120802),
        GeoPoint(51.530312, -0.120891),
        GeoPoint(51.530338, -0.120938),
        GeoPoint(51.530357, -0.120973),
        GeoPoint(51.530383, -0.121027),
        GeoPoint(51.530416, -0.121099),
        GeoPoint(51.530452, -0.121197),
        GeoPoint(51.530476, -0.121285),
        GeoPoint(51.530504, -0.121428),
        GeoPoint(51.530541, -0.121623),
        GeoPoint(51.530568, -0.121763),
        GeoPoint(51.53058, -0.121823),
        GeoPoint(51.530597, -0.121892),
        GeoPoint(51.530619, -0.121956),
        GeoPoint(51.530633, -0.121998),
        GeoPoint(51.530657, -0.122071),
        GeoPoint(51.530692, -0.122168),
        GeoPoint(51.530752, -0.122319),
        GeoPoint(51.530798, -0.122195),
        GeoPoint(51.530811, -0.122077),
        GeoPoint(51.530816, -0.121993),
        GeoPoint(51.530827, -0.12187),
        GeoPoint(51.530878, -0.121239),
        GeoPoint(51.530884, -0.12117),
        GeoPoint(51.530889, -0.121108),
        GeoPoint(51.530895, -0.120993),
        GeoPoint(51.530914, -0.120742),
        GeoPoint(51.530948, -0.120221),
        GeoPoint(51.530957, -0.12008),
        GeoPoint(51.530963, -0.120001),
        GeoPoint(51.530932, -0.119843),
        GeoPoint(51.530925, -0.119783),
        GeoPoint(51.530914, -0.119678),
        GeoPoint(51.530893, -0.11955),
        GeoPoint(51.530869, -0.119439),
        GeoPoint(51.530844, -0.119302),
        GeoPoint(51.530809, -0.119147),
        GeoPoint(51.530704, -0.11868),
        GeoPoint(51.530651, -0.118419),
        GeoPoint(51.530608, -0.118218),
        GeoPoint(51.530482, -0.1178),
        GeoPoint(51.5303, -0.117126),
        GeoPoint(51.53027, -0.117017),
        GeoPoint(51.5302, -0.116774),
        GeoPoint(51.530124, -0.116553),
        GeoPoint(51.530006, -0.116257),
        GeoPoint(51.529984, -0.11621),
        GeoPoint(51.529937, -0.116107),
        GeoPoint(51.529932, -0.116098),
        GeoPoint(51.529918, -0.116068),
        GeoPoint(51.52989, -0.11601),
        GeoPoint(51.529848, -0.115963),
        GeoPoint(51.52979, -0.115907),
        GeoPoint(51.52972, -0.11583),
        GeoPoint(51.529672, -0.115819),
        GeoPoint(51.529618, -0.115812),
        GeoPoint(51.529478, -0.115797),
        GeoPoint(51.529366, -0.115776),
        GeoPoint(51.529327, -0.115774),
        GeoPoint(51.529186, -0.115764),
        GeoPoint(51.529015, -0.115742),
        GeoPoint(51.528895, -0.115698),
        GeoPoint(51.528854, -0.115695),
        GeoPoint(51.528826, -0.115695),
        GeoPoint(51.52876, -0.115717),
        GeoPoint(51.528621, -0.115701),
        GeoPoint(51.528546, -0.115693),
        GeoPoint(51.528494, -0.115683),
        GeoPoint(51.528443, -0.115677),
        GeoPoint(51.528282, -0.115647),
        GeoPoint(51.528199, -0.115625),
        GeoPoint(51.528076, -0.115594),
        GeoPoint(51.527925, -0.115536),
        GeoPoint(51.527824, -0.115477),
        GeoPoint(51.527661, -0.115346),
        GeoPoint(51.527552, -0.115218),
        GeoPoint(51.52749, -0.11514),
        GeoPoint(51.527395, -0.114998),
        GeoPoint(51.527305, -0.114863),
        GeoPoint(51.527086, -0.114505),
        GeoPoint(51.5269, -0.114237),
        GeoPoint(51.526761, -0.114012),
        GeoPoint(51.526673, -0.113832),
        GeoPoint(51.526643, -0.113771),
        GeoPoint(51.526586, -0.113641),
        GeoPoint(51.526555, -0.11356),
        GeoPoint(51.526528, -0.113468),
        GeoPoint(51.52651, -0.1134),
        GeoPoint(51.526507, -0.113387),
        GeoPoint(51.526496, -0.113345),
        GeoPoint(51.526488, -0.11331),
        GeoPoint(51.526469, -0.113232),
        GeoPoint(51.526454, -0.113176),
        GeoPoint(51.526431, -0.113094),
        GeoPoint(51.526397, -0.112978),
        GeoPoint(51.526375, -0.112912),
        GeoPoint(51.526334, -0.112804),
        GeoPoint(51.526311, -0.112758),
        GeoPoint(51.526286, -0.112707),
        GeoPoint(51.526262, -0.112661),
        GeoPoint(51.526217, -0.11259),
        GeoPoint(51.526137, -0.112453),
        GeoPoint(51.526096, -0.112383),
        GeoPoint(51.526078, -0.112351),
        GeoPoint(51.526039, -0.11228),
        GeoPoint(51.526018, -0.112241),
        GeoPoint(51.525931, -0.112091),
        GeoPoint(51.525849, -0.111963),
        GeoPoint(51.525808, -0.111889),
        GeoPoint(51.525715, -0.11174),
        GeoPoint(51.52545, -0.111266),
        GeoPoint(51.525424, -0.111224),
        GeoPoint(51.525159, -0.110798),
        GeoPoint(51.525155, -0.110792),
        GeoPoint(51.525048, -0.110627),
        GeoPoint(51.524938, -0.110475),
        GeoPoint(51.52486, -0.110375),
        GeoPoint(51.524546, -0.109969),
        GeoPoint(51.524477, -0.109883),
        GeoPoint(51.524426, -0.109818),
        GeoPoint(51.524307, -0.109665),
        GeoPoint(51.52418, -0.109499),
        GeoPoint(51.524105, -0.109395),
        GeoPoint(51.523989, -0.109226),
        GeoPoint(51.523804, -0.108943),
        GeoPoint(51.52376, -0.108874),
        GeoPoint(51.523695, -0.108779),
        GeoPoint(51.523393, -0.108341),
        GeoPoint(51.523307, -0.108233),
        GeoPoint(51.523255, -0.108166),
        GeoPoint(51.523227, -0.108131),
        GeoPoint(51.523109, -0.107988),
        GeoPoint(51.523051, -0.107914),
        GeoPoint(51.523034, -0.107897),
        GeoPoint(51.523015, -0.107878),
        GeoPoint(51.522986, -0.107847),
        GeoPoint(51.522726, -0.107553),
        GeoPoint(51.522665, -0.107486),
        GeoPoint(51.52249, -0.107301),
        GeoPoint(51.522414, -0.107227),
        GeoPoint(51.522339, -0.107154),
        GeoPoint(51.522324, -0.10714),
        GeoPoint(51.522294, -0.107111),
        GeoPoint(51.522281, -0.1071),
        GeoPoint(51.522267, -0.107085),
        GeoPoint(51.522221, -0.107048),
        GeoPoint(51.522174, -0.107011),
        GeoPoint(51.522116, -0.106965),
        GeoPoint(51.522101, -0.106949),
        GeoPoint(51.522051, -0.106915),
        GeoPoint(51.522028, -0.106894),
        GeoPoint(51.521994, -0.106864),
        GeoPoint(51.521896, -0.106786),
        GeoPoint(51.521794, -0.106704),
        GeoPoint(51.521612, -0.106576),
        GeoPoint(51.521145, -0.106266),
        GeoPoint(51.520946, -0.106159),
        GeoPoint(51.52066, -0.10602),
        GeoPoint(51.520511, -0.105957),
        GeoPoint(51.520394, -0.105907),
        GeoPoint(51.520211, -0.105844),
        GeoPoint(51.520184, -0.105833),
        GeoPoint(51.519927, -0.10574),
        GeoPoint(51.51989, -0.105726),
        GeoPoint(51.51986, -0.105716),
        GeoPoint(51.519803, -0.105697),
        GeoPoint(51.519763, -0.105686),
        GeoPoint(51.519743, -0.10568),
        GeoPoint(51.519692, -0.105662),
        GeoPoint(51.51967, -0.105655),
        GeoPoint(51.519528, -0.105599),
        GeoPoint(51.519411, -0.105566),
        GeoPoint(51.519291, -0.105533),
        GeoPoint(51.519228, -0.105511),
        GeoPoint(51.519174, -0.105493),
        GeoPoint(51.51914, -0.105482),
        GeoPoint(51.519069, -0.10546),
        GeoPoint(51.518749, -0.10536),
        GeoPoint(51.518717, -0.105351),
        GeoPoint(51.518684, -0.105347),
        GeoPoint(51.51864, -0.105339),
        GeoPoint(51.518542, -0.105324),
        GeoPoint(51.518413, -0.105292),
        GeoPoint(51.51836, -0.105282),
        GeoPoint(51.518345, -0.10528),
        GeoPoint(51.518286, -0.105273),
        GeoPoint(51.518267, -0.105277),
        GeoPoint(51.518201, -0.10529),
        GeoPoint(51.518031, -0.105241),
        GeoPoint(51.517963, -0.105223),
        GeoPoint(51.51794, -0.105216),
        GeoPoint(51.517879, -0.105148),
        GeoPoint(51.517839, -0.105138),
        GeoPoint(51.517803, -0.105127),
        GeoPoint(51.51776, -0.105108),
        GeoPoint(51.517721, -0.105098),
        GeoPoint(51.517681, -0.105089),
        GeoPoint(51.517654, -0.105087),
        GeoPoint(51.517606, -0.105135),
        GeoPoint(51.517564, -0.105125),
        GeoPoint(51.517345, -0.105067),
        GeoPoint(51.517072, -0.105001),
        GeoPoint(51.516613, -0.10491),
        GeoPoint(51.516399, -0.104861),
        GeoPoint(51.516213, -0.104821),
        GeoPoint(51.515983, -0.104762),
        GeoPoint(51.515886, -0.10474),
        GeoPoint(51.515826, -0.10472),
        GeoPoint(51.515687, -0.104685),
        GeoPoint(51.515615, -0.104666),
        GeoPoint(51.515528, -0.104628),
        GeoPoint(51.515272, -0.104562),
        GeoPoint(51.515222, -0.104554),
        GeoPoint(51.515161, -0.104539),
        GeoPoint(51.515035, -0.104506),
        GeoPoint(51.51465, -0.104424),
        GeoPoint(51.514297, -0.104405),
        GeoPoint(51.514274, -0.104401),
        GeoPoint(51.514169, -0.104387),
        GeoPoint(51.514164, -0.104247),
        GeoPoint(51.51416, -0.104142),
        GeoPoint(51.514157, -0.104077),
        GeoPoint(51.514157, -0.10403),
        GeoPoint(51.514156, -0.104004),
        GeoPoint(51.51415, -0.103686),
        GeoPoint(51.514149, -0.103643),
        GeoPoint(51.514148, -0.103598),
        GeoPoint(51.514147, -0.103554),
        GeoPoint(51.514147, -0.103547),
        GeoPoint(51.514145, -0.103513),
        GeoPoint(51.514137, -0.103393),
        GeoPoint(51.514132, -0.103334),
        GeoPoint(51.51415, -0.103238),
        GeoPoint(51.514134, -0.103117),
        GeoPoint(51.514105, -0.103048),
        GeoPoint(51.514075, -0.103009),
        GeoPoint(51.51407, -0.102971),
        GeoPoint(51.514039, -0.102804),
        GeoPoint(51.514015, -0.102662),
        GeoPoint(51.513997, -0.102553),
        GeoPoint(51.513972, -0.102405),
        GeoPoint(51.513953, -0.102304),
        GeoPoint(51.513932, -0.102165),
        GeoPoint(51.513902, -0.101947),
        GeoPoint(51.513893, -0.101879),
        GeoPoint(51.513839, -0.101525),
        GeoPoint(51.513828, -0.101438),
        GeoPoint(51.513804, -0.101172),
        GeoPoint(51.513796, -0.101054),
        GeoPoint(51.513794, -0.10101),
        GeoPoint(51.513786, -0.10071),
        GeoPoint(51.513783, -0.100652),
        GeoPoint(51.513773, -0.100523),
        GeoPoint(51.513767, -0.100489),
        GeoPoint(51.513756, -0.100447),
        GeoPoint(51.513727, -0.100367),
        GeoPoint(51.513724, -0.100361),
        GeoPoint(51.513656, -0.100228),
        GeoPoint(51.513571, -0.1001),
        GeoPoint(51.513513, -0.099997),
        GeoPoint(51.513475, -0.099918),
        GeoPoint(51.513457, -0.099872),
        GeoPoint(51.51342, -0.099775),
        GeoPoint(51.513393, -0.099686),
        GeoPoint(51.513357, -0.099552),
        GeoPoint(51.513333, -0.099425),
        GeoPoint(51.513314, -0.099297),
        GeoPoint(51.5133, -0.099178),
        GeoPoint(51.513292, -0.099087),
        GeoPoint(51.513288, -0.099007),
        GeoPoint(51.513285, -0.098926),
        GeoPoint(51.513284, -0.098808),
        GeoPoint(51.513282, -0.098526),
        GeoPoint(51.513284, -0.098437),
        GeoPoint(51.513281, -0.09835),
        GeoPoint(51.513268, -0.098233),
        GeoPoint(51.513228, -0.098039),
        GeoPoint(51.513211, -0.097965),
        GeoPoint(51.513166, -0.097772),
        GeoPoint(51.51314, -0.097662),
        GeoPoint(51.513108, -0.097515),
        GeoPoint(51.513106, -0.097503),
        GeoPoint(51.513089, -0.097429),
        GeoPoint(51.513038, -0.097191),
        GeoPoint(51.513017, -0.097097),
        GeoPoint(51.513003, -0.097032),
        GeoPoint(51.512955, -0.096788),
        GeoPoint(51.51295, -0.096764),
        GeoPoint(51.512943, -0.096727),
        GeoPoint(51.512931, -0.096662),
        GeoPoint(51.512906, -0.096521),
        GeoPoint(51.512894, -0.096457),
        GeoPoint(51.512864, -0.09628),
        GeoPoint(51.512853, -0.096215),
        GeoPoint(51.512841, -0.096145),
        GeoPoint(51.512808, -0.095923),
        GeoPoint(51.512802, -0.095885),
        GeoPoint(51.512796, -0.095855),
        GeoPoint(51.512773, -0.095678),
        GeoPoint(51.512804, -0.095593),
        GeoPoint(51.512772, -0.095438),
        GeoPoint(51.512753, -0.095344),
        GeoPoint(51.512706, -0.095122),
        GeoPoint(51.512704, -0.095114),
        GeoPoint(51.512693, -0.095061),
        GeoPoint(51.512654, -0.095033),
        GeoPoint(51.51261, -0.09486),
        GeoPoint(51.512599, -0.094807),
        GeoPoint(51.512521, -0.094484),
        GeoPoint(51.512534, -0.09435),
        GeoPoint(51.512512, -0.094199),
        GeoPoint(51.512497, -0.094093),
        GeoPoint(51.512487, -0.094023),
        GeoPoint(51.512475, -0.093949),
        GeoPoint(51.512454, -0.093831),
        GeoPoint(51.512435, -0.093732),
        GeoPoint(51.51238, -0.093573),
        GeoPoint(51.512334, -0.093378),
        GeoPoint(51.512313, -0.0933),
        GeoPoint(51.512265, -0.09323),
        GeoPoint(51.512196, -0.092948),
        GeoPoint(51.512164, -0.092815),
        GeoPoint(51.512141, -0.092696),
        GeoPoint(51.512118, -0.0926),
        GeoPoint(51.511974, -0.091954),
        GeoPoint(51.511917, -0.091703),
        GeoPoint(51.511878, -0.091529),
        GeoPoint(51.511786, -0.091117),
        GeoPoint(51.511773, -0.091057),
        GeoPoint(51.51176, -0.090999),
        GeoPoint(51.51174, -0.09091),
        GeoPoint(51.511726, -0.090847),
        GeoPoint(51.5117, -0.090727),
        GeoPoint(51.511666, -0.090568),
        GeoPoint(51.511655, -0.090517),
        GeoPoint(51.511642, -0.090458),
        GeoPoint(51.511606, -0.090275),
        GeoPoint(51.511531, -0.08988),
        GeoPoint(51.511486, -0.089643),
        GeoPoint(51.51148, -0.08961),
        GeoPoint(51.511459, -0.089504),
        GeoPoint(51.51144, -0.089412),
        GeoPoint(51.511429, -0.089352),
        GeoPoint(51.511414, -0.089271),
        GeoPoint(51.511354, -0.088961),
        GeoPoint(51.5113, -0.088626),
        GeoPoint(51.511292, -0.088579),
        GeoPoint(51.511254, -0.088373),
        GeoPoint(51.511241, -0.088305),
        GeoPoint(51.511178, -0.087969),
        GeoPoint(51.511129, -0.087718),
        GeoPoint(51.511086, -0.087494),
        GeoPoint(51.511037, -0.087246),
        GeoPoint(51.511039, -0.087231),
        GeoPoint(51.511044, -0.087156),
        GeoPoint(51.511031, -0.087087),
        GeoPoint(51.511009, -0.086977),
        GeoPoint(51.51097, -0.08683),
        GeoPoint(51.510935, -0.08672),
        GeoPoint(51.510927, -0.08669),
        GeoPoint(51.510914, -0.086631),
        GeoPoint(51.510899, -0.086548),
        GeoPoint(51.510893, -0.086466),
        GeoPoint(51.510889, -0.086398),
        GeoPoint(51.510894, -0.086313),
        GeoPoint(51.510901, -0.086232),
        GeoPoint(51.510891, -0.086121),
        GeoPoint(51.510878, -0.086031),
        GeoPoint(51.510845, -0.08587),
        GeoPoint(51.510799, -0.085669),
        GeoPoint(51.510754, -0.085421),
        GeoPoint(51.510723, -0.085312),
        GeoPoint(51.510725, -0.085175),
        GeoPoint(51.510733, -0.084908),
        GeoPoint(51.510735, -0.08485),
        GeoPoint(51.510732, -0.084651),
        GeoPoint(51.510715, -0.084462),
        GeoPoint(51.510702, -0.084364),
        GeoPoint(51.510694, -0.084312),
        GeoPoint(51.510675, -0.084165),
        GeoPoint(51.510648, -0.083916),
        GeoPoint(51.510608, -0.083352),
        GeoPoint(51.510594, -0.083213),
        GeoPoint(51.510578, -0.083082),
        GeoPoint(51.510549, -0.082965),
        GeoPoint(51.510487, -0.08279),
        GeoPoint(51.510416, -0.08259),
        GeoPoint(51.510376, -0.082483),
        GeoPoint(51.510174, -0.081945),
        GeoPoint(51.510114, -0.08178),
        GeoPoint(51.510047, -0.081601),
        GeoPoint(51.509985, -0.081427),
        GeoPoint(51.509941, -0.081309),
        GeoPoint(51.509827, -0.080995),
        GeoPoint(51.509815, -0.080876),
        GeoPoint(51.509726, -0.080636),
        GeoPoint(51.509686, -0.080526),
        GeoPoint(51.509664, -0.080465),
        GeoPoint(51.509645, -0.080413),
        GeoPoint(51.509619, -0.080341),
        GeoPoint(51.509576, -0.080304),
        GeoPoint(51.509557, -0.08025),
        GeoPoint(51.509509, -0.080121),
        GeoPoint(51.509514, -0.080103),
        GeoPoint(51.509548, -0.079997),
        GeoPoint(51.509585, -0.07984),
        GeoPoint(51.509607, -0.079699),
        GeoPoint(51.509617, -0.07955),
        GeoPoint(51.509609, -0.079326),
        GeoPoint(51.509594, -0.079123),
        GeoPoint(51.509578, -0.078886),
        GeoPoint(51.509552, -0.078605),
        GeoPoint(51.509524, -0.078406),
        GeoPoint(51.509492, -0.078242),
        GeoPoint(51.509476, -0.07816),
        GeoPoint(51.509459, -0.078077),
        GeoPoint(51.509439, -0.077947),
        GeoPoint(51.509435, -0.077862),
        GeoPoint(51.509435, -0.07775),
        GeoPoint(51.509456, -0.077314),
        GeoPoint(51.50949, -0.07683),
        GeoPoint(51.509498, -0.076719),
        GeoPoint(51.509501, -0.076678),
        GeoPoint(51.509509, -0.076598),
        GeoPoint(51.509546, -0.076253),
        GeoPoint(51.509582, -0.076009),
        GeoPoint(51.509645, -0.075717),
        GeoPoint(51.509696, -0.075509),
        GeoPoint(51.509707, -0.075471),
        GeoPoint(51.509786, -0.075199),
        GeoPoint(51.509818, -0.07509),
        GeoPoint(51.509866, -0.075043),
        GeoPoint(51.509878, -0.074953),
        GeoPoint(51.509901, -0.074879),
        GeoPoint(51.509929, -0.07476),
        GeoPoint(51.509934, -0.074669),
        GeoPoint(51.509926, -0.074536),
        GeoPoint(51.509902, -0.07437),
        GeoPoint(51.509884, -0.074293),
        GeoPoint(51.509857, -0.074227),
        GeoPoint(51.509812, -0.074155),
        GeoPoint(51.509785, -0.074108),
        GeoPoint(51.509742, -0.074055),
        GeoPoint(51.509615, -0.073943),
        GeoPoint(51.509591, -0.073921),
        GeoPoint(51.509522, -0.073839),
        GeoPoint(51.509491, -0.073805),
        GeoPoint(51.509442, -0.073751),
        GeoPoint(51.509486, -0.073717),
        GeoPoint(51.509524, -0.073691),
        GeoPoint(51.509575, -0.073648),
        GeoPoint(51.509612, -0.073614),
        GeoPoint(51.509675, -0.073551),
        GeoPoint(51.509748, -0.073478),
        GeoPoint(51.509835, -0.073391),
        GeoPoint(51.509919, -0.073307),
        GeoPoint(51.50995, -0.073269),
        GeoPoint(51.509965, -0.073236),
        GeoPoint(51.510001, -0.073179),
        GeoPoint(51.510022, -0.073138),
        GeoPoint(51.510042, -0.07309),
        GeoPoint(51.510065, -0.073021),
        GeoPoint(51.510092, -0.072935),
        GeoPoint(51.510032, -0.072974),
        GeoPoint(51.509978, -0.073017),
        GeoPoint(51.509717, -0.073331),
        GeoPoint(51.509684, -0.073366),
        GeoPoint(51.509591, -0.073439),
        GeoPoint(51.509504, -0.073517),
        GeoPoint(51.509474, -0.073543),
        GeoPoint(51.509444, -0.073572),
        GeoPoint(51.509404, -0.073603),
        GeoPoint(51.509385, -0.073613),
        GeoPoint(51.509344, -0.073636),
        GeoPoint(51.509193, -0.073699),
        GeoPoint(51.509048, -0.073744),
        GeoPoint(51.508983, -0.073768),
        GeoPoint(51.508926, -0.073798),
        GeoPoint(51.508806, -0.073881),
        GeoPoint(51.508744, -0.07391),
        GeoPoint(51.508673, -0.073979),
        GeoPoint(51.508662, -0.073981),
        GeoPoint(51.508423, -0.074034),
        GeoPoint(51.508204, -0.074083),
        GeoPoint(51.508132, -0.074098)
    )

    // Configure osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Map")
                        Text(
                            text = currentDateTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)

                        // Center the map to show the full route
                        val centerLat = (startPoint.latitude + endPoint.latitude) / 2
                        val centerLon = (startPoint.longitude + endPoint.longitude) / 2
                        controller.setZoom(13.5)
                        controller.setCenter(GeoPoint(centerLat, centerLon))

                        // Draw the route
                        val route = Polyline().apply {
                            setPoints(routePoints)
                            outlinePaint.color = Color.parseColor("#4285F4")
                            outlinePaint.strokeWidth = 14f
                        }
                        overlays.add(route)

                        // Start marker
                        val startMarker = Marker(this).apply {
                            position = startPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Start"
                        }
                        overlays.add(startMarker)

                        // End marker
                        val endMarker = Marker(this).apply {
                            position = endPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Destination"
                        }
                        overlays.add(endMarker)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { }
    }
}

// Date/time helper function
private fun getMapDateTime(context: android.content.Context): String {
    val date = Date()
    val calendar = java.util.Calendar.getInstance()
    calendar.time = date

    val dayOfWeek = DateFormat.format("EEEE", date)
    val month = DateFormat.format("MMMM", date)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val year = calendar.get(java.util.Calendar.YEAR)
    val time = DateFormat.format("HH:mm:ss", date)

    val daySuffix = when (day) {
        1, 21, 31 -> "st"
        2, 22 -> "nd"
        3, 23 -> "rd"
        else -> "th"
    }

    return "$dayOfWeek, $month $day$daySuffix, $year, $time."
}
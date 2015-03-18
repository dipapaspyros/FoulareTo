<?php
/**
 * Created by PhpStorm.
 * User: dimitris
 * Date: 16/3/2015
 * Time: 7:11 μμ
 */

include "data.php";
include "data_info.php";

//calculate distance between two points
function haversineGreatCircleDistance($latitudeFrom, $longitudeFrom, $latitudeTo, $longitudeTo, $earthRadius = 6371000) {
    // convert from degrees to radians
    $latFrom = deg2rad($latitudeFrom);
    $lonFrom = deg2rad($longitudeFrom);
    $latTo = deg2rad($latitudeTo);
    $lonTo = deg2rad($longitudeTo);

    $latDelta = $latTo - $latFrom;
    $lonDelta = $lonTo - $lonFrom;

    $angle = 2 * asin(sqrt(pow(sin($latDelta / 2), 2) +
            cos($latFrom) * cos($latTo) * pow(sin($lonDelta / 2), 2)));
    return $angle * $earthRadius;
}

//function to sort element by prices
function storeAscSort($a, $b) {
    if ($a['price'] == $b['price']) return 0;
    return ($a['price'] > $b['price']) ? 1 : -1;
}

//get lat/lng of request
$lat = $_GET["lat"];
$lng = $_GET["lng"];

//get points in a radius of 2km or less
$MAX_RAD = 2000;
$points = array();
for ($i=0; $i<count($data); $i++) {
    if (isset($data_info[$i])) {
        if (haversineGreatCircleDistance($lat, $lng, $data_info[$i][0], $data_info[$i][1]) <= $MAX_RAD) {
            //check for duplicates
            $add = true;
            for ($j=0; $j<=count($points); $j++) {
                //same address & price
                if (($data[$points[$j]][6] == $data[$i][6]) && ($data[$points[$j]][11] == $data[$i][11])) {
                    $add = false;
                    break;
                }
            }

            if ($add) {
                array_push($points, $i);
            }
        }
    }
}

//get prices & info of gas stores
$stores = array();
foreach ($points as $point) {
    if (isset($data[$point][11])) { //must have a price
        $store = array(
            "lat" => $data_info[$point][0],
            "lng" => $data_info[$point][1],
            "address" => $data[$point][6],
            "name" => $data[$point][5],
            "brand" => $data[$point][9],
            "type" => $data[$point][10],
            "price" => $data[$point][11],
        );
        array_push($stores, $store);
    }
}

//order by price
usort($stores, 'storeAscSort');

//only get 5 first suggestions
$stores = array_slice($stores, 0, 4);

//form response
$response = array(
    "lat" => $lat,
    "lng" => $lng,
    "radius" => $MAX_RAD,
    "stores" => $stores);

//send response as json
header('Content-Type: application/json; charset=utf-8');
echo json_encode($response);

// 마커를 담을 배열입니다
var markers = [];
var currentMarker;
var total = 0;
var id, curLatitude, curLongitude;
var map, mapContainer, options, preLatitude, preLongitude;
// 장소 검색 객체를 생성합니다
var ps = new kakao.maps.services.Places();

// 검색 결과 목록이나 마커를 클릭했을 때 장소명을 표출할 인포윈도우를 생성합니다
var infowindow = new kakao.maps.InfoWindow({zIndex:1});

// 키워드 검색을 요청하는 함수입니다
function searchPlaces() {

    var keyword = document.getElementById('keyword').value;

    if (!keyword.replace(/^\s+|\s+$/g, '')) {
        alert('키워드를 입력해주세요!');
        return false;
    }

    // 장소검색 객체를 통해 키워드로 장소검색을 요청합니다
    ps.keywordSearch( keyword, placesSearchCB);

    document.getElementById("menu_wrap").style.display = "block";
}

// 장소검색이 완료됐을 때 호출되는 콜백함수 입니다
function placesSearchCB(data, status, pagination) {
    if (status === kakao.maps.services.Status.OK) {

        // 정상적으로 검색이 완료됐으면
        // 검색 목록과 마커를 표출합니다
        displayPlaces(data);
        console.log(data);

        // 페이지 번호를 표출합니다
        displayPagination(pagination);
    } else if (status === kakao.maps.services.Status.ZERO_RESULT) {

        alert('검색 결과가 존재하지 않습니다.');
        return;

    } else if (status === kakao.maps.services.Status.ERROR) {

        alert('검색 결과 중 오류가 발생했습니다.');
        return;

    }
}

// 검색 결과 목록과 마커를 표출하는 함수입니다
function displayPlaces(places) {

    var listEl = document.getElementById('placesList'),
        menuEl = document.getElementById('menu_wrap'),
        fragment = document.createDocumentFragment(),
        bounds = new kakao.maps.LatLngBounds(),
        listStr = '';

    // 검색 결과 목록에 추가된 항목들을 제거합니다
    removeAllChildNods(listEl);

    // 지도에 표시되고 있는 마커를 제거합니다
    removeMarker();

    for ( var i=0; i<places.length; i++ ) {

        // 마커를 생성하고 지도에 표시합니다
        var placePosition = new kakao.maps.LatLng(places[i].y, places[i].x),
            marker = addMarker(placePosition, i),
            itemEl = getListItem(i, places[i]); // 검색 결과 항목 Element를 생성합니다

        // 마커와 검색결과 항목에 mouseover 했을때
        // 해당 장소에 인포윈도우에 장소명을 표시합니다
        // mouseout 했을 때는 인포윈도우를 닫습니다
        (function(marker, title, place, index) {
            kakao.maps.event.addListener(marker, 'mouseover', function() {
                displayInfowindow(marker, title);
            });

            kakao.maps.event.addListener(marker, 'mouseout', function() {
                infowindow.close();
            });

            itemEl.querySelector('#placeInfo').addEventListener("click", function () {
                infowindow.close();
                panTo(place.y, place.x)
                displayInfowindow(marker, title);
            });

            itemEl.querySelector('#ridingButton').addEventListener("click", function () {
                infowindow.close();
                // 검색 결과 목록에 추가된 항목들을 제거합니다
                removeAllChildNods(listEl);
                // 지도에 표시되고 있는 마커를 제거합니다
                remainOneMarker(index);
                hideMenu();
                panTo(preLatitude, preLongitude)
                document.getElementById("start_riding").style.display = "block";

                id = navigator.geolocation.watchPosition((position)=>{
                    success(position);
                }, (err)=>{
                    error(err)
                }, { enableHighAccuracy: false, maximumAge: 5000, timeout: 5000 });
            });

        })(marker, places[i].place_name, places[i], i);

        fragment.appendChild(itemEl);
    }

    // 검색결과 항목들을 검색결과 목록 Element에 추가합니다
    listEl.appendChild(fragment);
    menuEl.scrollTop = 0;

}

// 검색결과 항목을 Element로 반환하는 함수입니다
function getListItem(index, places) {

    var el = document.createElement('li'),
        itemStr = '<span class="markerbg marker_' + (index+1) + '"></span>' +
            '<div class="info" id="placeInfo">' +
            '   <h5>' + places.place_name + '</h5>';

    if (places.road_address_name) {
        itemStr += '    <span>' + places.road_address_name + '</span>' +
            '   <span class="jibun gray">' +  places.address_name  + '</span>';
    } else {
        itemStr += '    <span>' +  places.address_name  + '</span>';
    }

    itemStr += '</div>' + '<div class="text-right py-1"><button id="ridingButton" class="btn border border-secondary rounded-pill text-primary px-1">라이딩 시작</button></div>';

    el.innerHTML = itemStr;
    el.className = 'item';

    return el;
}

// 마커를 생성하고 지도 위에 마커를 표시하는 함수입니다
function addMarker(position, idx, title) {
    var imageSrc = '../img/flag.png', // 마커 이미지 url, 스프라이트 이미지를 씁니다
        imageSize = new kakao.maps.Size(36, 37),  // 마커 이미지의 크기
        imageOption = {offset: new kakao.maps.Point(13, 37)}; // 마커이미지의 옵션입니다. 마커의 좌표와 일치시킬 이미지 안에서의 좌표를 설정합니다.
    markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption),
        marker = new kakao.maps.Marker({
            position: position, // 마커의 위치
            image: markerImage
        });

    marker.setMap(map); // 지도 위에 마커를 표출합니다
    markers.push(marker);  // 배열에 생성된 마커를 추가합니다

    return marker;
}

// 지도 위에 표시되고 있는 마커를 모두 제거합니다
function removeMarker() {
    for ( var i = 0; i < markers.length; i++ ) {
        markers[i].setMap(null);
    }
    markers = [];
}
// 지도 위에 표시되고 있는 마커를 모두 제거합니다
function remainOneMarker(index) {
    for ( var i = 0; i < markers.length; i++ ) {
        if(index != i){
            markers[i].setMap(null);
        }
    }
    markers = [markers[index]];
}
// 검색결과 목록 하단에 페이지번호를 표시는 함수입니다
function displayPagination(pagination) {
    document.getElementById('pagination').style.display = "block";
    var paginationEl = document.getElementById('pagination'),
        fragment = document.createDocumentFragment(),
        i;

    // 기존에 추가된 페이지번호를 삭제합니다
    while (paginationEl.hasChildNodes()) {
        paginationEl.removeChild (paginationEl.lastChild);
    }

    for (i=1; i<=pagination.last; i++) {
        var el = document.createElement('a');
        el.href = "#";
        el.innerHTML = i;

        if (i===pagination.current) {
            el.className = 'on';
        } else {
            el.onclick = (function(i) {
                return function() {
                    pagination.gotoPage(i);
                }
            })(i);
        }

        fragment.appendChild(el);
    }
    paginationEl.appendChild(fragment);
}

// 검색결과 목록 또는 마커를 클릭했을 때 호출되는 함수입니다
// 인포윈도우에 장소명을 표시합니다
function displayInfowindow(marker, title) {
    var content = '<div style="padding:5px;z-index:1;">' + title + '</div>';

    infowindow.setContent(content);
    infowindow.open(map, marker);
}

// 검색결과 목록의 자식 Element를 제거하는 함수입니다
function removeAllChildNods(el) {
    while (el.hasChildNodes()) {
        el.removeChild (el.lastChild);
    }
}
// 지도 중심 이동시키는 함수
function panTo(lat, long) {
    // 이동할 위도 경도 위치를 생성합니다
    var moveLatLon = new kakao.maps.LatLng(lat, long);

    // 지도 중심을 부드럽게 이동시킵니다
    // 만약 이동할 거리가 지도 화면보다 크면 부드러운 효과 없이 이동합니다
    map.panTo(moveLatLon);
}
function stopRiding() {
    axios.post('/route', {}, {
        params: {
            totalRiding: total * 1000
        }
    })
        .then(function (response) {
            removeMarker();
            document.getElementById("menu_wrap").style.display = "block";
            document.getElementById("start_riding").style.display = "none";
            navigator.geolocation.clearWatch(id);
            total = 0;
        })
        .catch(function (error) {
            // 오류 발생 시의 처리
            alert('실패했습니다. 다시 시도해주세요');
        });
}
function hideMenu(){
    document.getElementById("menu_wrap").style.display = "none"; // 요소를 화면에서 숨김
    document.getElementById('keyword').value = "";
    document.getElementById('pagination').style.display = "none";
}

//거리계산
function getDistance(lat1,lng1,lat2,lng2) {
    function deg2rad(deg) {
        return deg * (Math.PI/180)
    }
    var R = 6371; // Radius of the earth in km
    var dLat = deg2rad(lat2-lat1);  // deg2rad below
    var dLon = deg2rad(lng2-lng1);
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var d = R * c; // Distance in km
    return Math.round(d * 1000)/1000.0;
}

function success(pos) {
    var crd = pos.coords;

    curLatitude = crd.latitude;
    curLongitude = crd.longitude;

    currentMarker.setMap(null);

    var imageSrc = '../img/bicycle.png',
        imageSize = new kakao.maps.Size(36, 37),
        imageOption = {offset: new kakao.maps.Point(13, 37)};

    var markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption),
        markerPosition  = new kakao.maps.LatLng(curLatitude, curLongitude);
    currentMarker = new kakao.maps.Marker({
        position: markerPosition,
        image: markerImage // 마커이미지 설정
    });

    total = Math.round ((getDistance(curLatitude, curLongitude, preLatitude, preLongitude) + total) * 1000) / 1000;

    document.getElementById("distanceDisplay").innerText = total + "km"; // 거리를 갱신하여 표시

    preLatitude = curLatitude;
    preLongitude = curLongitude;

    // 마커가 지도 위에 표시되도록 설정합니다
    currentMarker.setMap(map);
}

function error(err) {
    console.warn("ERROR(" + err.code + "): " + err.message);
}

function getCurrentLocation(){
    navigator.geolocation.getCurrentPosition(geoSuccess, geoError);

    function geoSuccess(position) {
        preLatitude = position.coords.latitude;
        preLongitude = position.coords.longitude;

        mapContainer = document.getElementById('map'), // 지도를 표시할 div
            mapOption = {
                center: new kakao.maps.LatLng(preLatitude, preLongitude), // 지도의 중심좌표
                level: 3 // 지도의 확대 레벨
            };

        map = new kakao.maps.Map(mapContainer, mapOption);

        const imageSrc = '../img/bicycle.png',
            imageSize = new kakao.maps.Size(36, 37),
            imageOption = {offset: new kakao.maps.Point(13, 37)};

        const markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption),
            markerPosition  = new kakao.maps.LatLng(preLatitude, preLongitude);

        currentMarker = new kakao.maps.Marker({
            position: markerPosition,
            image: markerImage // 마커이미지 설정
        });

        currentMarker.setMap(map);
    }

    function geoError() {
        alert('위치 접근을 허용해 주세요.');
    }
}
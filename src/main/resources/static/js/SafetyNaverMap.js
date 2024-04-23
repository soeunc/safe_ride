let position = new naver.maps.LatLng(37.3595704, 127.105399)
let mapOptions = {
    center: position,
    zoom: 15,
    zoomControl: true,
    zoomControlOptions: {
        position: naver.maps.Position.TOP_RIGHT
    }
};

let map = new naver.maps.Map('map', mapOptions);
let markers = [];
let infoWindows = [];

const queryInput = document.getElementById('search-input');
document.getElementById('find-route-to-address-btn').addEventListener('click', () => {
    const query = queryInput.value;
    if (query.length === 0) {
        alert('검색어를 입력하세요');
        return;
    }
    fetch('/safety-direction/check', {
        method: 'post',
        headers: {
            'content-type': 'application/json'
        },
        body: JSON.stringify({
            query
        })
    }).then(async response => {
        if (response.ok) {
            const data = await response.json();
            const {lat, lng, accidentCoordinates, schoolZoneInfo} = data;
            map.setCenter({lat, lng});

            // 기존 마커 삭제
            markers.forEach(marker => marker.setMap(null));
            markers = [];
            infoWindows.forEach(infoWindow => infoWindow.close());
            infoWindows = [];

            // 사고 위치에 마커 생성
            accidentCoordinates.forEach(coordinate => {
                const marker = new naver.maps.Marker({
                    position: new naver.maps.LatLng(coordinate.accidentLat, coordinate.accidentLng),
                    map: map
                });
                // 마커에 대한 정보
                const contentString = [
                    '<div class="iw_inner" style="position: absolute; border-style: solid; border-width: 1.2rem 1rem 0 1rem; border-color: #ffffff transparent; ">',
                    `<p><strong>${coordinate.spotNm}</strong><br>`,
                    `발생 건수: ${coordinate.occrrncCnt}건<br>`,
                    `사망자 수: ${coordinate.dthDnvCnt}건</p>`,
                    '</div>'
                ].join('');

                // 마커에 대한 정보 창 생성
                const infoWindow = new naver.maps.InfoWindow({
                    content: contentString
                });

                // 마커에 마우스를 가져갔을 때 이벤트 리스너
                naver.maps.Event.addListener(marker, 'mouseover', () => {
                    infoWindow.open(map, marker);
                });

                // 마커에서 마우스를 뗐을 때 이벤트 리스너
                naver.maps.Event.addListener(marker, 'mouseout', () => {
                    infoWindow.close();
                });
                console.log(`마커 위치: 위도 ${marker.position.lat()}, 경도 ${marker.position.lng()}`);
                markers.push(marker);
                infoWindows.push(infoWindow);

            });
            var markerContent2 = document.createElement('div');
            markerContent2.innerHTML = '<img src="/img/schZone.png" width="40" height="40">';
            // 스쿨존에 마커 생성
            schoolZoneInfo.forEach(schoolZoneInfoList => {
                const schoolMarker = new naver.maps.Marker({
                    position: new naver.maps.LatLng(schoolZoneInfoList.schLat, schoolZoneInfoList.schLng),
                    map: map,
                    icon: {
                        content: markerContent2,
                        size: new naver.maps.Size(30, 30),
                        scaledSize: new naver.maps.Size(30, 30),
                        origin: new naver.maps.Point(0, 0),
                        anchor: new naver.maps.Point(15, 15)
                    }
                });
                // 마커에 대한 정보
                const contentString = [
                    '<div class="iw_inner" style="position: absolute; border-style: solid; border-width: 1.2rem 1rem 0 1rem; border-color: #ffffff transparent; ">',
                    `<p><strong>${schoolZoneInfoList.spotNm}</strong><br>`,
                    `발생 건수: ${schoolZoneInfoList.occrrncCnt}건<br>`,
                    `사망자 수: ${schoolZoneInfoList.dthDnvCnt}건</p>`,
                    '</div>'
                ].join('');

                // 마커에 대한 정보 창 생성
                const infoWindow = new naver.maps.InfoWindow({
                    content: contentString
                });

                // 마커에 마우스를 가져갔을 때 이벤트 리스너
                naver.maps.Event.addListener(schoolMarker, 'mouseover', () => {
                    infoWindow.open(map, schoolMarker);
                });

                // 마커에서 마우스를 뗐을 때 이벤트 리스너
                naver.maps.Event.addListener(schoolMarker, 'mouseout', () => {
                    infoWindow.close();
                });

                console.log(`스쿨존 위치: 위도 ${schoolMarker.position.lat()}, 경도 ${schoolMarker.position.lng()}`);
                markers.push(schoolMarker);
                infoWindows.push(infoWindow);
            });
        } else {
            alert('주소를 찾을 수 없습니다.');
        }
    });
});
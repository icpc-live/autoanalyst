const urlSearchParams = new URLSearchParams(window.location.search);
const params = Object.fromEntries(urlSearchParams.entries());
const streamIds = params['streams'].split(',');


var N = streamIds.length;

$("#headerRow").html('<td colspan="'+ N +'"><video width="100%" id="videoElement0" playsinline autoplay muted ></video></td>');
if (N > 1) {
    var bottomRow = $("#bottomRow");
    for (var i=1; i<N; i++) {
        var cell = $('<td><video width="100%" id="videoElement'+i+'" playsinline autoplay muted ></video></td>');
        bottomRow.append(cell);
    }
}

    
if (mpegts.getFeatureList().mseLivePlayback) {
    for (let index = 0; index < streamIds.length; index++) {
        const id = streamIds[index];
        var baseUrl = params['url'] ?? "";
        const streamURL = baseUrl + id;
        let videoElement = document.getElementById('videoElement' + index);
        
        videoElement.onclick = function(event) {
              event.preventDefault();
              videoElement.requestFullscreen();
/*            if (streamIds[0] == id) {
                // [id] is already a main screen, do nothing
                return;
            }
            var rotatedStreams = streamIds;
            // we put [id] on the first place
            while (rotatedStreams[0] != id) {
                rotatedStreams.push(rotatedStreams.shift());
            }     
            const newLink = location.protocol + '//' + location.host + location.pathname + "?url=" + params['url'] + "&streams=" + rotatedStreams.toString();
            window.location.href = newLink;*/
        };
        
        var player = mpegts.createPlayer({
	    type: 'm2ts',  // could also be mpegts, m2ts, flv
	    isLive: true,
	    url: streamURL
    	});
	player.attachMediaElement(videoElement);
	player.load();
	player.play();
    }
}


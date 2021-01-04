(function () {
    var requests = [];
    var blockedHosts = ["update.microsoft.com", "windowsupdate.com", "thwawte.com"];
    var videoUrls = [
        ".facebook.com|pagelet",
        "player.vimeo.com/",
        "instagram.com/p/"
    ];

    var fileExts = [
        "3GP", "7Z", "AVI", "BZ2", "DEB", "DOC", "DOCX", "EXE", "GZ", "ISO",
        "MSI", "PDF", "PPT", "PPTX", "RAR", "RPM", "XLS", "XLSX", "SIT", "SITX",
        "TAR", "JAR", "ZIP", "XZ"];
    var vidExts = [
        "MP4", "M3U8", "F4M", "WEBM", "OGG", "MP3", "AAC", "FLV", "MKV", "DIVX",
        "MOV", "MPG", "MPEG", "OPUS"];

    var isXDMUp = true;
    var monitoring = true;
    var debug = true;
    var xdmHost = "http://127.0.0.1:9614";
    var disabled = false;
    var lastIcon;
    var lastPopup;
    var videoList = [];
    var mimeList = ["video/", "audio/", "mpegurl", "f4m", "m3u8"];
    var port;

    initSelf();
    console.log("loaded");

    function processRequest(request, response) {
        if (shouldInterceptFile(request, response)) {
            var file = getAttachedFile(response);
            if (!file) {
                file = getFileFromUrl(response.url);
            }

            sendToXDM(request, response, file, false);
            return {
                cancel: true
            };//return { redirectUrl: "http://127.0.0.1:9614/204" };
        } else {
            checkForVideo(request, response);
        }
    }

    function sendToXDM(request, response, file, video) {
        console.log("sending to xdm: " + response.url);
        var data = {
            url: response.url,
            file: file,
            tabId: request.tabId,
            userAgent: navigator.userAgent,
            cookies: {},
            requestHeaders: {},
            responseHeaders: {}
        }

        var requestHeaders = data.requestHeaders;
        request.requestHeaders.forEach(function (x) {
            requestHeaders[x.name] = x.value;
        })

        var responseHeaders = data.responseHeaders;
        response.responseHeaders.forEach(function (x) {
            responseHeaders[x.name] = x.value;
        })

        chrome.cookies.getAll({"url": response.url}, function (cookies) {
            var urlCookies = data.cookies;
            cookies.forEach(function (x) {
                urlCookies[x.name] = x.value;
            })

            console.log("data", data);

            port.postMessage({message: (video ? "/video" : "/download") + "\r\n" + JSON.stringify(data)});
        });
    }

    function sendRecUrl(urls, data) {
        if (data.length === urls.length) {
            console.log(data);

            port.postMessage({"message": "/links" + "\r\n" + JSON.stringify(data)});
        } else {
            var url = urls[data.length];
            var datum = {
                url: url,
                userAgent: navigator.userAgent,
                cookies: {},
                requestHeaders: {},
                responseHeaders: {}
            }

            chrome.cookies.getAll({"url": url}, function (cookies) {
                var urlCookies = datum.cookies;
                cookies.forEach(function (x) {
                    urlCookies[x.name] = x.value;
                })

                data.push(datum)
                sendRecUrl(urls, data);
            });
        }
    }

    function sendUrlsToXDM(urls) {
        sendRecUrl(urls, []);
    }

    function sendUrlToXDM(url) {
        var data = {
            url: url,
            userAgent: navigator.userAgent
        };

        chrome.cookies.getAll({"url": url}, function (cookies) {
            var urlCookies = data.cookies;
            cookies.forEach(function (x) {
                urlCookies[x.name] = x.value;
            })

            port.postMessage({"message": "/download" + "\r\n" + data});
        });
    }

    function sendImageToXDM(info) {
        var url;
        if (info.mediaType) {
            if ("image" === info.mediaType) {
                if (info.srcUrl) {
                    url = info.srcUrl;
                }
            }
        }

        if (!url) {
            url = info.linkUrl;
        }

        if (!url) {
            url = info.pageUrl;
        }

        if (url) {
            sendUrlToXDM(url);
        }

    }

    function sendLinkToXDM(info) {
        var url = info.linkUrl;
        if (!url) {
            if (info.mediaType) {
                if ("video" === info.mediaType || "audio" === info.mediaType) {
                    if (info.srcUrl) {
                        url = info.srcUrl;
                    }
                }
            }
        }
        if (!url) {
            url = info.pageUrl;
        }
        if (!url) {
            return;
        }
        sendUrlToXDM(url);
    }

    function runContentScript() {
        console.log("running content script");
        chrome.tabs.executeScript({
            file: 'contentscript.js'
        });
    }

    function checkForVideo(request, response) {

        var mimeHeader = getHeader(response, "content-type");
        var mime = mimeHeader ? mimeHeader.value.toLowerCase() : "";
        var video = false;
        var url = response.url;
        console.log(url)

        if (mime.startsWith("audio/") || mime.startsWith("video/") ||
            mime.indexOf("mpegurl") > 0 || mime.indexOf("f4m") > 0 || isVideoMime(mime)) {
            console.log("Checking video mime: " + mime + " " + JSON.stringify(mimeList));
            video = true;
        }

        var i;
        if (!video) {
            if (videoUrls) {
                for (i = 0; i < videoUrls.length; i++) {
                    var arr = videoUrls[i].split("|");
                    var matched = true;
                    for (var j = 0; j < arr.length; j++) {
                        //console.log(arr[j]);
                        if (url.indexOf(arr[j]) < 0) {
                            matched = false;
                            break;
                        }
                    }
                    if (matched) {
                        video = true;
                        console.log(url)
                        break;
                    }
                }
            }
        }


        if (!video) {
            if (vidExts) {
                var file = getFileFromUrl(url);
                var ext = getFileExtension(file);
                if (ext) {
                    ext = ext.toUpperCase();
                }
                for (i = 0; i < vidExts.length; i++) {
                    if (vidExts[i] === ext) {
                        video = true;
                        break;
                    }
                }
            }
        }

        if (video) {
            if (request.tabId !== -1) {
                chrome.tabs.get
                (
                    request.tabId,
                    function (tab) {
                        sendToXDM(request, response, tab.title, true);
                    }
                );
            } else {
                sendToXDM(request, response, null, true);
            }
        }
    }

    function getAttachedFile(response) {
        var header = getHeader(response, 'content-disposition');
        if (header) {
            return getFileFromContentDisposition(header.value);
        }
    }

    function shouldInterceptFile(request, response) {
        var url = response.url;
        var isAttachment = false;
        if (isBlocked(url)) {
            return false;
        }

        if (isHtml(response)) {
            return false;
        }

        var file = getAttachedFile(response);
        if (!file) {
            file = getFileFromUrl(url);
        } else {
            isAttachment = true;
        }

        var ext = getFileExtension(file);
        if (ext) {
            var i;
            if (!isAttachment) {
                for (i = 0; i < vidExts.length; i++) {
                    if (vidExts[i] === ext.toUpperCase()) {
                        return false;
                    }
                }
            }
            for (i = 0; i < fileExts.length; i++) {
                if (fileExts[i] === ext.toUpperCase()) {
                    return true;
                }
            }
        }
    }

    function getFileFromContentDisposition(str) {
        var arr = str.split(";");
        for (var i = 0; i < arr.length; i++) {
            var ln = arr[i].trim();
            if (ln.indexOf("filename=") > 0) {
                console.log("matching line: " + ln);
                var arr2 = ln.split("=");
                console.log("name: " + arr2[1]);
                return arr2[1].replace(/"/g, '').trim();
            }
        }
    }

    function removeRequest(requestId) {
        for (var i = 0; i < requests.length; i++) {
            if (requests[i].requestId === requestId) {
                return requests.splice(i, 1)[0];
            }
        }
    }

    function updateBrowserAction() {
        if (!isXDMUp) {
            setBrowserActionPopUp("fatal.html");
            setBrowserActionIcon("icon_blocked.png");
            return;
        }

        if (monitoring) {
            if (disabled) {
                setBrowserActionIcon("icon_disabled.png");
            } else {
                setBrowserActionIcon("icon.png");
            }
            setBrowserActionPopUp("status.html");
        } else {
            setBrowserActionIcon("icon_disabled.png");
            setBrowserActionPopUp("disabled.html");
        }

        if (videoList && videoList.length > 0) {
            chrome.browserAction.setBadgeText({text: videoList.length + ""});
        } else {
            chrome.browserAction.setBadgeText({text: ""});
        }
    }

    function setBrowserActionIcon(icon) {
        if (lastIcon === icon) {
            return;
        }
        chrome.browserAction.setIcon({path: icon});
        lastIcon = icon;
    }

    function setBrowserActionPopUp(pop) {
        if (lastPopup === pop) {
            return;
        }
        chrome.browserAction.setPopup({popup: pop});
        lastPopup = pop;
    }


    function initSelf() {
        initPort()

        //This will add the request to request array for later use,
        //the object is removed from array when request completes or fails
        chrome.webRequest.onSendHeaders.addListener(function (info) {
                requests.push(info);
            },
            {urls: ["http://*/*", "https://*/*"]},
            ["requestHeaders"] // on chrome "extraHeaders" also needs to be added?
        );

        chrome.webRequest.onCompleted.addListener(function (info) {
                removeRequest(info.requestId);
            },
            {urls: ["http://*/*", "https://*/*"]}
        );

        chrome.webRequest.onErrorOccurred.addListener(function (info) {
                removeRequest(info.requestId);
            },
            {urls: ["http://*/*", "https://*/*"]}
        );

        //This will monitor and intercept files download if
        //criteria matches and XDM is running
        //Use request array to get request headers
        chrome.webRequest.onHeadersReceived.addListener(onHeadersReceived,
            {urls: ["http://*/*", "https://*/*"]},
            ["blocking", "responseHeaders"]
        );

        //check XDM if is running and enable monitoring
        //setInterval(function () { syncXDM(); }, 5000);

        chrome.runtime.onMessage.addListener(
            onRuntimeMessage
        );

        chrome.commands.onCommand.addListener(function () {
            if (isXDMUp && monitoring) {
                console.log("called")
                disabled = !disabled;
            }
        });

        chrome.contextMenus.create({
            title: "Download with XDM",
            contexts: ["link", "video", "audio"],
            onclick: sendLinkToXDM,
        });

        chrome.contextMenus.create({
            title: "Download Image with XDM",
            contexts: ["image"],
            onclick: sendImageToXDM,
        });

        chrome.contextMenus.create({
            title: "Download all links",
            contexts: ["all"],
            onclick: runContentScript,
        });
    }

    function initPort() {
        /*
        On startup, connect to the "native" app.
        */
        port = browser.runtime.connectNative("xdmff.native_host");

        /*
        Listen for messages from the app.
        */
        port.onMessage.addListener(onMessageFromXDM);

        /*
        On start up send the app a message.
        */
        console.log("Sending to native...")
        port.postMessage({"message": "hello from extension"});
    }

    function onMessageFromXDM(data) {
        monitoring = data.enabled;
        blockedHosts = data.blockedHosts;
        videoUrls = data.videoUrls;
        fileExts = data.fileExts;
        vidExts = data.vidExts;
        isXDMUp = true;
        videoList = data.vidList;
        if (data.mimeList) {
            mimeList = data.mimeList;
        }
        updateBrowserAction();

        console.log("Received: ", data);
    }

    function onRuntimeMessage(request, sender, sendResponse) {
        if (request.type === "links") {
            var arr = request.links;
            for (var i = 0; i < arr.length; i++) {
                console.log("link " + arr[i]);
            }

            sendUrlsToXDM(arr);
            sendResponse({done: "done"});
        } else if (request.type === "stat") {
            var resp = {isDisabled: disabled};
            resp.list = videoList;
            sendResponse(resp);
        } else if (request.type === "cmd") {
            disabled = request.disable;
            console.log("disabled " + disabled);
        } else if (request.type === "vid") {
            port.postMessage({"message": "/item\r\n" + request.itemId});
        } else if (request.type === "clear") {
            port.postMessage({"message": "/clear"});
        }
    }

    function onHeadersReceived(response) {
        var status = response.statusCode;
        var request = removeRequest(response.requestId);
        var url = response.url || "";
        if (!request ||
            !isXDMUp ||
            !monitoring ||
            disabled ||
            !status || status < 200 || status > 300 ||
            url.startsWith(xdmHost)) {
            return;
        }

        //console.log("processing request " + response.url);
        return processRequest(request, response);
    }

    function isHtml(response) {
        var header = getHeader(response, 'content-type');
        if (header) {
            return header.value.indexOf("text/html") > 0;
        }
    }

    function isVideoMime(mimeText) {
        if (!mimeList) {
            return false;
        }

        var mime = mimeText.toLowerCase();
        for (var i = 0; i < mimeList.length; i++) {
            if (mime.indexOf(mimeList[i]) > 0) {
                return true;
            }
        }

        return false;
    }

    function isBlocked(url) {
        for (var i = 0; i < blockedHosts.length; i++) {
            var hostName = parseUrl(url).hostname;
            if (blockedHosts[i] === hostName) {
                return true;
            }
        }
        return false;
    }

    function getFileFromUrl(str) {
        return parseUrl(str).pathname;
    }

    function parseUrl(str) {
        var match = str.match(/^(https?:)\/\/(([^:\/?#]*)(?::([0-9]+))?)([\/]?[^?#]*)(\?[^#]*|)(#.*|)$/);
        return match && {
            href: str,
            protocol: match[1],
            host: match[2],
            hostname: match[3],
            port: match[4],
            pathname: match[5],
            search: match[6],
            hash: match[7]
        }
    }

    function getFileExtension(file) {
        var index = file.lastIndexOf(".");
        if (index > 0) {
            return file.substr(index + 1);
        }
    }

    function getHeader(response, name) {
        name = name.toLowerCase();
        for (var i = 0; i < response.responseHeaders.length; i++) {
            var header = response.responseHeaders[i];
            if (header.name.toLowerCase() === name) {
                return header;
            }
        }
    }
})();

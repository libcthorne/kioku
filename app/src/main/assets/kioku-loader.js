kiokuJSI.log("kioku-loader");

if (document.readyState == 'complete' && document.getElementsByTagName("body")[0].innerHTML.length > 0) {
    kiokuJSI.loaded();
} else {
    kiokuJSI.retryLoad();
}
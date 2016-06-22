if (typeof(kiokuSave) != "undefined") {
    _kiokuExitSelectMode();

    kiokuSave();

    _KJQ("._kioku_selected_hidden, ._kioku_selected").each(function() {
        kiokuDeselect(this);
    });
} else {
    kiokuJSI.saveFinished();
}
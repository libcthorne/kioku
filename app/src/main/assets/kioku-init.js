kiokuInSelectMode = false; // Currently in select mode?
zIndex = 2; // z-index of select mode overlay box

function kiokuSelect(element) {
    kiokuJSI.selectedElement();

    _KJQ(element)
        .css("background-color", "")
        .css("box-shadow", "inset 0px 0px 0px 2px #108DB6")
        .addClass("_kioku_selected")
        .removeClass("_kioku_selected_hidden")
}

function kiokuDeselect(element) {
    kiokuJSI.deselectedElement();

    _KJQ(element)
        .css("background-color", "rgba(255, 255, 255, 0.8)")
        .css("box-shadow", "inset 0px 0px 1px 2px rgba(0, 0, 0, 0.1)")
        .removeClass("_kioku_selected")
        .removeClass("_kioku_selected_hidden")
}

function createSelectBoxes(selectorFor, informationType, selectedCallback, deselectedCallback, customCSS) {
    // Select box
    selectBoxStyle = "position: absolute;";
    selectBoxStyle += "z-index: " + zIndex + ";";
    //selectBoxStyle += "right: 0px;";
    selectBoxStyle += "box-shadow: inset 0px 0px 1px 2px rgba(0, 0, 0, 0.1);";
    selectBoxStyle += "background-color: rgba(255, 255, 255, 0.8);";
    selectBoxStyle += "font-size: 10px;";
    selectBoxStyle += "text-align: center;";
    selectBoxStyle += "vertical-align: middle;";

    // Add select box as overlay for each piece of information found
    _KJQ(selectorFor).each(function(i, el) {
        $el = _KJQ(el);

        // Don't recreate existing (check if overlay box already exists), only resize
        $existing = $el.prev("._kioku_select_" + informationType);
        if ($existing.length > 0) {
            $existing.each(function(j, el) {
                _KJQ(this).width($el.width());
                _KJQ(this).height($el.height());
                // Reapply custom CSS in case it defines width and height
                if (customCSS != undefined)
                    _KJQ(this).attr("style",  _KJQ(this).attr("style") + "; " + customCSS);
            });

            return true; // continue
        }

        $selectBox = _KJQ("<span class='_kioku_select_" + informationType + " _kioku_selectable' style='" + selectBoxStyle + "'></span>");
        $selectBox.width($el.width());
        $selectBox.height($el.height());
        $selectBox.insertBefore($el);
        // Apply custom CSS
        if (customCSS != undefined)
            $selectBox.attr("style",  $selectBox.attr("style") + "; " + customCSS);

        $selectBox.on("click", function(event) {
            if (_KJQ(this).hasClass("_kioku_selected")) {
                 if (deselectedCallback && deselectedCallback(this) == false)
                     return false;

                kiokuDeselect(this, informationType);
            } else {
                if (selectedCallback && selectedCallback(this) == false)
                    return false;

                kiokuSelect(this, informationType);
            }

            return false;
        });
    })
}

function kiokuGetSelected(informationType) {
    return _KJQ("._kioku_select_" + informationType + "._kioku_selected");
}

function _kiokuExitSelectMode() {
    kiokuInSelectMode = false;

    // Hide selectable boxes
    _KJQ("._kioku_selectable").css("visibility", "hidden");

    if (typeof(kiokuExitSelectMode) != "undefined")
        kiokuExitSelectMode();
}

function _kiokuEnterSelectMode() {
    kiokuInSelectMode = true;

    // Show selectable boxes
    _KJQ("._kioku_selectable").css("visibility", "visible");

    if (typeof(kiokuEnterSelectMode) != "undefined")
        kiokuEnterSelectMode();
}

function _kiokuToggleSelectMode() {
    if (kiokuInSelectMode) {
        _kiokuExitSelectMode();
    } else {
        _kiokuEnterSelectMode();
    }
}
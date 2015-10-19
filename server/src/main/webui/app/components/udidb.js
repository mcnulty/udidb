import React from "react"
import ContextSelector from "./contextSelector.js"
import SourceViewer from "./sourceViewer.js"
import CommandLine from "./commandLine.js"

export default React.createClass({
    render: function() {
        return (
            <div className="b-udidb">
                <div className="b-udidb-contextSelector">
                    <ContextSelector/>
                </div>
                <div className="b-udidb-sourcePane">
                    <SourceViewer/>
                    <CommandLine/>
                </div>
            </div>
        )
    }
});

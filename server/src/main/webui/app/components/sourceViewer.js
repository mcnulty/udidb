
import React from "react"
import hljs from "highlight.js"

let topLevelStyle = {
    fontFamily: 'monospace',
    fontSize: '11',
    whiteSpace: 'pre',
    border: '1px solid rgb(204, 204, 204)',
    borderRadius: '4px',
    padding: '5px 5px 5px 5px'
};
let testSource = ""

export default React.createClass({

    render: function() {
        let source = this.props.sourceMap.lines.join("\n");
        let highlightedSourceObj = hljs.highlightAuto(source, [ "C" ]);
        return (
            <div className="hljs" dangerouslySetInnerHTML={ { __html: highlightedSourceObj["value"] } } style={topLevelStyle}/>
        )
    }
});

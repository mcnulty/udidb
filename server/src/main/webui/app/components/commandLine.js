import React from "react"
import { Panel } from "react-bootstrap"

var topLevelStyle = {
    fontFamily: 'monospace',
    fontSize: '11',
    whiteSpace: 'pre',
    border: '1px solid rgb(204, 204, 204)',
    borderRadius: '4px',
    padding: '5px 5px 5px 5px'
};

var testText = ""

export default React.createClass({
    render: function() {
        return (
            <div className="hljs" style={topLevelStyle}>
                {testText}
            </div>
        )
    }
});

testText =
"(udidb) eval 1 + 2\n" +
"3\n" +
"(udidb) print main\n" +
"main = 0xe7f890\n" +
"(udidb)"

/** 
 * The styling is currently hard-coded. It would be nice to let the user
 * select from some hljs themes eventually.
 */

import React from "react"
import hljs from "highlight.js"

let topLevelStyle = {
    fontFamily: 'monospace',
    fontSize: '11',
    whiteSpace: 'pre',
    border: '1px solid rgb(204, 204, 204)',
    borderRadius: '4px',
    padding: '5px 0px 5px 0px',
};

let placeholderStyle = Object.assign({
    textAlign: "center"
}, topLevelStyle);

let noSourceContent;

export default React.createClass({

    render: function() {
        if (this.props.currentContext.id === "-1") {
            return this._renderPlaceholderContent(this.props.defaultSourceContent);
        }else{
            return this._renderContent();
        }
    },

    _renderPlaceholderContent: function(content) {
        let rows = content.map(function(line, index, array) {
            return <tr key={"defaultSourceContent-" + index}><td>{line}</td></tr>;
        });

        return (
            <div className="hljs" style={placeholderStyle}>
                <table style={ { width: "100%" } }>
                    <tbody>{rows}</tbody>
                </table>
            </div>
        );
    },

    _renderContent: function() {
        let currentContext = this.props.currentContext;
        let sourceContext = currentContext.threads[currentContext.activeThreadIndex].source;

        if (!sourceContext || !currentContext.sourceMap || !(sourceContext.file in currentContext.sourceMap) ) {
            return this._renderPlaceholderContent(noSourceContent);
        }

        let source = currentContext.sourceMap[sourceContext.file].lines.join("\n");

        let lineStart = currentContext.sourceMap[sourceContext.file].startLineNo;
        let activeLineNo = sourceContext.line;

        let highlightedSourceObj = hljs.highlightAuto(source, [ "C" ]);

        let numberedSourceRows = highlightedSourceObj.value.split("\n").map(function(line, index, array) {
            let currentLineNo = index + lineStart;

            let markedUpLine;
            let markedUpLineNo;
            if (activeLineNo == currentLineNo) {
                markedUpLine = '<td style="background-color: rgb(10,81,99); width: 100%;">' +
                    line +
                        '</td>';
                
                markedUpLineNo = ' ' + currentLineNo + ' >';
            }else{
                markedUpLine = '<td style="width: 100%;">' + line + '</td>';
                markedUpLineNo = ' ' + currentLineNo + '  ';
            }

            let lineNoCell = '<td style="' + 
                        'background-color: rgb(7,54,66);' + 
                        'border:1px solid rgb(7,54,66);' +
                        'color: rgb(131, 148, 150);' +
                   '">'
                   + markedUpLineNo + '</td>';
                   
            return '<tr>' + lineNoCell + markedUpLine + "</tr>";
        }).join("");

        return (
            <div className="hljs" style={topLevelStyle}>
                <table style={ { width: "100%" } }>
                    <tbody dangerouslySetInnerHTML={ { __html: numberedSourceRows } }/>
                </table>
            </div>
        );
    }
});

noSourceContent = [
        " ",
        "udidb - UDI debugger",
        " ",
        " ",
        " ",
        " ",
        "No source information available",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
];

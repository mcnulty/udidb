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
    padding: '5px 5px 5px 0px',
};

export default React.createClass({

    render: function() {
        let currentContext = this.props.currentContext;
        let sourceContext = currentContext.threads[currentContext.activeThreadIndex].source;
        let source = currentContext.sourceMap[sourceContext.file].lines.join("\n");

        let lineStart = currentContext.sourceMap[sourceContext.file].startLineNo;
        let activeLineNo = sourceContext.line;

        let highlightedSourceObj = hljs.highlightAuto(source, [ "C" ]);

        let numberedSource = highlightedSourceObj.value.split("\n").map(function(line, index, array) {
            let currentLineNo = index + lineStart;

            let markedUpLine;
            let markedUpLineNo;
            if (activeLineNo == currentLineNo) {
                markedUpLine = '<span style="background-color: rgb(10,81,99);">' +
                    line +
                        '</span>';
                
                markedUpLineNo = ' ' + currentLineNo + ' >';
            }else{
                markedUpLine = line;
                markedUpLineNo = ' ' + currentLineNo + '  ';
            }

            return '<span style="' + 
                        'background-color: rgb(7,54,66);' + 
                        'border:1px solid rgb(7,54,66);' +
                        'margin-right: 5px;' +
                        'color: rgb(131, 148, 150);' +
                   '">'
                + markedUpLineNo + '</span>' + markedUpLine;
        }).join("\n");
        return (
            <div className="hljs" dangerouslySetInnerHTML={ { __html: numberedSource } } style={topLevelStyle}/>
        )
    }
});

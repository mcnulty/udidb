import React from "react"

var statusLineStyle = {
    backgroundColor : "rgb(147, 161, 161)",
    color : "rgb(7, 54, 66)",
    borderRadius: "4px",
    paddingLeft: '5px',
    fontWeight: 'bold',
    fontFamily: 'monospace',
    fontSize: '12'
}

export default React.createClass({

    render: function() {

        var lineContent;
        if (this.props.currentContext.id === "-1") {
            lineContent = "<no process selected>";
        }else{
            var threadComponent;
            if (this.props.currentContext.activeThreadIndex >= 0) {
                var currentThread = this.props.currentContext.threads[this.props.currentContext.activeThreadIndex];

                threadComponent = ", Thread " + currentThread.id +
                    " - " + currentThread.source.file + ":" + currentThread.source.line;
            }else{
                threadComponent = "";
            }

            lineContent = "Process " + this.props.currentContext.processId + threadComponent;
        }

        return (
            <div style={statusLineStyle}>
                {lineContent}
            </div>
        )
    }
});

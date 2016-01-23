import React from "react"

var statusLineStyle = {
    backgroundColor : "rgb(147, 161, 161)",
    color : "rgb(7, 54, 66)",
    borderRadius: "4px",
    paddingLeft: '5px',
    fontWeight: 'bold',
    fontFamily: 'monospace',
}

export default React.createClass({

    render: function() {

        let lineContent;
        if (this.props.currentContext === null) {
            lineContent = "<no process selected>";
        }else{
            let threadComponent;
            if (this.props.currentContext.activeThreadIndex >= 0) {
                let currentThread = this.props.currentContext.threads[this.props.currentContext.activeThreadIndex];
                let sourceInfo = ( currentThread.source ? ( currentThread.source.file + ":" +
                                                          currentThread.source.line ) : "No source info" );

                threadComponent = ", Thread " + currentThread.id + " - " + sourceInfo;
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

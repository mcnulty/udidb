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
        return (
            <div style={statusLineStyle}>
                Process 5665, Thread 1 - src/main.c:1234
            </div>
        )
    }
});

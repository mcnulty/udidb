import React from "react"

var statusLineStyle = {
    backgroundColor : "rgb(47, 63, 69)",
    color : "rgb(216, 226, 230)",
    borderRadius: "4px",
    paddingLeft: '5px'
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

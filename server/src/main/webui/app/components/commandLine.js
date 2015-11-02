import React from "react"

var topLevelStyle = {
    fontFamily: 'monospace',
    fontSize: '11',
    whiteSpace: 'pre',
    border: '1px solid rgb(204, 204, 204)',
    borderRadius: '4px',
    padding: '5px 5px 5px 5px'
};

var inputStyle = {
    backgroundColor: 'rgb(0, 43, 54)',
    border: '0px',
    outline: '0px'
};

const PROMPT = "(udidb)";

export default React.createClass({

    getInitialState: function() {
        return {
            results: [
                {
                    index: 20,
                    value: "3"
                },
                {
                    index: 21,
                    value: "main = 0xe7f890"
                }
            ]
        };
    },

    handleNewOperation: function(e) {
        e.preventDefault();
        console.log(this.refs.operation.value.trim());
    },

    render: function() {
        var history = this.props.history;

        var output = [];

        var startIndex = history.operations.length - history.numDisplayedOps;
        if (startIndex < 0) {
            startIndex = 0;
        }

        for (var i = startIndex; i < history.operations.length; i++)
        {
            var operation = history.operations[i];
            var result = this.state.results.find(function(e, searchIndex, a) {
                return e.index === (i + history.baseIndex);
            });

            var operationValue = operation.operands.reduce(function(p, c, i, a) {
                if (c.type === "list") {
                    return p + " " + c.value.join(" ");
                }else{
                    return p + " " + c.value;
                }
            }, operation.name);

            output.push(PROMPT + " " + operationValue);
            output.push(result.value);
        }

        return (
            <div className="hljs" style={topLevelStyle}>
                <div>
                    {output.join("\n")}
                </div>
                <form onSubmit={this.handleNewOperation}>
                    <label>{PROMPT + " "}
                        <input style={inputStyle} type="text" ref="operation"/>
                    </label>
                </form>
            </div>
        )
    }
});

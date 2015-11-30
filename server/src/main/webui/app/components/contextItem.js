import React from "react"
import { Button, ButtonGroup, NavItem, Collapse } from "react-bootstrap"

import { UdidbRequest, PUT_METHOD } from "./requests.js"

export default React.createClass({

    _handleThreadSelect: function(index, e) {
        e.preventDefault();
        this.props.process(new UdidbRequest(PUT_METHOD, "currentContext.activeThreadIndex", index));
    },

    render: function() {
        let { context, process, ...other } = this.props;
        let threadItems = context.threads.map(function(currentValue, index, array) {
            let buttonStyle = (index === context.activeThreadIndex ? "info" : "default");
            let sourceInfo = ( currentValue.source ? ( currentValue.source.file + ":" +
                              currentValue.source.line ) : "No source info" );
            return (
                <Button onClick={this._handleThreadSelect.bind(this, index)} 
                    key={currentValue.id}
                    bsStyle={buttonStyle}>
                    {"Thread " + currentValue.id + " - " + sourceInfo}
                </Button>
            );
        }, this);

        return (
            <NavItem {...other}>
                <span>
                    { "Process " + this.props.context.processId }
                </span>
                <Collapse in={this.props.active}>
                    <div>
                        <ButtonGroup vertical block>
                            {threadItems}
                        </ButtonGroup>
                    </div>
                </Collapse>
            </NavItem>
        );
    }
});

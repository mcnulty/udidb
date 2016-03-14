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

        let title;
        let threadsContent;
        if (process !== null) {
            let threadItems = context.threads.map(function(currentValue, index, array) {
                let buttonStyle = (index === context.activeThreadIndex ? "info" : "default");
                return (
                    <Button onClick={this._handleThreadSelect.bind(this, index)} 
                        key={currentValue.id}
                        bsStyle={buttonStyle}>
                        {"Thread " + currentValue.id}
                    </Button>
                );
            }, this);
            title = "Process " + this.props.context.processId;
            threadsContent = (
                <Collapse in={this.props.active}>
                    <div>
                        <ButtonGroup vertical block>
                            {threadItems}
                        </ButtonGroup>
                    </div>
                </Collapse>);
        }else{
            title = "Global Context";
            threadsContent = "";
        }

        return (
            <NavItem {...other}>
                <span>
                    { title }
                </span>
                <div>
                    { threadsContent }
                </div>
            </NavItem>
        );
    }
});

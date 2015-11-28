import React from "react"
import { Nav, NavItem } from "react-bootstrap"

import ContextItem from "./contextItem.js"
import { UdidbRequest, POST_METHOD, PUT_METHOD } from "./requests.js"

export default React.createClass({

    _handleContextSelect: function(currentContextIndex, index, e) {
        e.preventDefault();
        if (currentContextIndex != index) {
            this.props.process(new UdidbRequest(PUT_METHOD, "currentContextIndex", index));
        }
    },

    render: function() {
        let selectedContextIndex;
        let items;
        if (this.props.currentContextIndex >= 0) {
            selectedContextIndex = this.props.currentContextIndex;
            items = this.props.contexts.map(function (currentValue, index, array) {
                return <ContextItem onClick={this._handleContextSelect.bind(this, 
                                                                            this.props.currentContextIndex, 
                                                                            index)}
                                    key={currentValue.id} 
                                    eventKey={index} 
                                    context={currentValue}
                                    process={this.props.process}/>
            }, this);
        }else{
            selectedContextIndex = 0;
            items = <NavItem key="defaultContextItem" eventKey={0}>
                <span>
                    {"No processes attached."}
                </span>
            </NavItem>

        }

        return (
            <Nav bsStyle="pills" stacked activeKey={selectedContextIndex}>
                {items}
            </Nav>
        )
    }
});

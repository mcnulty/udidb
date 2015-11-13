import React from "react"
import { Nav, NavItem } from "react-bootstrap"

import ContextItem from "./contextItem.js"
import { UdidbRequest, POST_METHOD, PUT_METHOD } from "./requests.js"

export default React.createClass({

    _handleContextSelect: function(index, e) {
        e.preventDefault();
        this.props.process(new UdidbRequest(PUT_METHOD, "currentContextIndex", index));
    },

    render: function() {
        let items = this.props.contexts.map(function (currentValue, index, array) {
            return <ContextItem onClick={this._handleContextSelect.bind(this, index)}
                                key={currentValue.id} 
                                eventKey={index} 
                                context={currentValue}
                                process={this.props.process}/>
        }, this);

        return (
            <Nav bsStyle="pills" stacked activeKey={this.props.currentContextIndex}>
                {items}
            </Nav>
        )
    }
});

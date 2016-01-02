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
        let selectedContextIndex = this.props.currentContextIndex;
        let items = this.props.contexts.map(function (currentValue, index, array) {
            return <ContextItem onClick={this._handleContextSelect.bind(this, 
                                                                        this.props.currentContextIndex, 
                                                                        index)}
                                key={currentValue.id} 
                                eventKey={index} 
                                context={currentValue}
                                process={this.props.process}/>
        }, this);
        items.push(
            <ContextItem onClick={this._handleContextSelect.bind(this,
                                                                 this.props.currentContextIndex,
                                                                 -1)}
                         key={-1}
                         eventKey={-1}
                         context={this.props.globalContext}
                         process={null}/>
        );

        return (
            <Nav bsStyle="pills" stacked activeKey={selectedContextIndex}>
                {items}
            </Nav>
        )
    }
});

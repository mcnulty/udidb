import React from "react"
import { Nav, NavItem } from "react-bootstrap"

import ContextItem from "./contextItem.js"

export default React.createClass({
    render: function() {
        return (
            <Nav bsStyle="pills" stacked activeKey={1}>
                <ContextItem eventKey={1} title="Process 5665"/>
                <ContextItem eventKey={2} title="Process 5666"/>
            </Nav>
        )
    }
});

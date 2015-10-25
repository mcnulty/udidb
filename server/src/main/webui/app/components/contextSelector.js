import React from "react"
import { Nav, NavItem } from "react-bootstrap"

import ContextItem from "./contextItem.js"

export default React.createClass({
    render: function() {
        return (
            <Nav bsStyle="pills" stacked activeKey={1}>
                <ContextItem eventKey={1} content="Context 1"/>
                <ContextItem eventKey={2} content="Context 2"/>
            </Nav>
        )
    }
});

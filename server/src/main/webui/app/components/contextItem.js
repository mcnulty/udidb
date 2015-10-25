import React from "react"
import { Nav, NavItem, Collapse } from "react-bootstrap"

var threadNavStyle = {
    backgroundColor: 'rgb(220, 220, 220)',
    color: 'black'
};

export default React.createClass({
    getInitialState: function() {
        return {}
    },

    render: function() {
        var { title, ...other } = this.props;
        return (
            <NavItem {...other}>
                <span>
                    { this.props.title }
                </span>
                <Collapse in={this.props.active}>
                    <div>
                        <Nav bsStyle="link" activeKey={1} style={threadNavStyle}>
                            <NavItem eventKey={1}>
                                Thread 1 - src/main.c:1234
                            </NavItem>
                            <NavItem eventKey={2}>
                                Thread 2 - src/events.c:120
                            </NavItem>
                        </Nav>
                    </div>
                </Collapse>
            </NavItem>
        );
    }
});

import React from "react"
import { NavItem } from "react-bootstrap"

export default React.createClass({
    getInitialState: function() {
        return {}
    },

    render: function() {
        var { content, ...other } = this.props;
        return (
            <NavItem {...other}>
                <span>
                    { this.props.content }
                </span>
            </NavItem>
        );
    }
});

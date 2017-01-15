import * as React from "react";
import {
    Nav,
    NavItem,
    NavItemProps
} from "react-bootstrap";

import * as ContextItem from "./contextItem";
import {
    UdidbRequest,
    Context,
    POST_METHOD,
    PUT_METHOD
} from "./types";

export interface Props {
    readonly currentContextIndex: number;
    readonly contexts: ReadonlyArray<Context>;
    readonly globalContext: Context;
    readonly process: (request: UdidbRequest) => void;
}

export class Component extends React.Component<Props, {}> {

    private handleContextSelect(currentContextIndex: number, index: number, e: React.FormEvent<HTMLFormElement>): void {
        e.preventDefault();
        if (currentContextIndex !== index) {
            this.props.process(new UdidbRequest(PUT_METHOD, "currentContextIndex", "" + index));
        }
    }

    public render(): JSX.Element {
        let selectedContextIndex = this.props.currentContextIndex;
        let items = this.props.contexts.map(function(currentValue, index, array) {
            let navProps: NavItemProps = {};
            navProps.key = index;
            navProps.eventKey = index;
            navProps.onClick = this.handleContextSelect.bind(this,
                this.props.currentContextIndex,
                index);

            let active = index === this.props.currentContextIndex;

            return (
                <ContextItem.Component navProps={navProps}
                                       context={currentValue}
                                       active={active}
                                       process={this.props.process} />
            );
        }, this);

        let defaultNavProps: NavItemProps = {};
        defaultNavProps.key = -1;
        defaultNavProps.eventKey = -1;
        defaultNavProps.onClick = this.handleContextSelect.bind(this,
            this.props.currentContextIndex,
            -1);

        let defaultActive = this.props.currentContextIndex == -1;

        let defaultItem = (
            <ContextItem.Component navProps={defaultNavProps}
                                   context={this.props.globalContext}
                                   active={defaultActive}
                                   process={this.props.process} />
        );
        items.push(defaultItem);

        return (
            <Nav bsStyle="pills" stacked activeKey={selectedContextIndex}>
                {items}
            </Nav>
        );
    }
}

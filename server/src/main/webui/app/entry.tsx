require("babel-polyfill");
require("bootstrap/dist/css/bootstrap.css");
require("highlight.js/styles/solarized_dark.css");
require("./entry.css");

import * as React from "react";
import * as ReactDOM from "react-dom";

import * as UdidbController from "./components/udidbController";

ReactDOM.render(
    <UdidbController.Component baseApiUri="http://localhost:8888" />,
    document.getElementById("udidb-app-container")
);

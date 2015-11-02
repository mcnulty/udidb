require("babel-polyfill");
require("bootstrap/dist/css/bootstrap.css");
require("highlight.js/styles/solarized_dark.css");
require("./entry.css");

import React from "react";
import ReactDOM from "react-dom";

import UdidbController from "./components/udidbController.js";

ReactDOM.render(
    <UdidbController/>,
    document.getElementById("udidb-app-container")
);

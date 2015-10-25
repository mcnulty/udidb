require("bootstrap/dist/css/bootstrap.css")
require("highlight.js/styles/solarized_dark.css");
require("./entry.css")

import React from "react";
import ReactDOM from "react-dom";

import Udidb from "./components/udidb.js";

ReactDOM.render(
    <Udidb/>,
    document.getElementById("udidb-app-container")
);

require("./entry.css")
require("./normalize.css")

import React from "react";
import ReactDOM from "react-dom";
import Udidb from "./components/udidb.js";

ReactDOM.render(
    <Udidb/>,
    document.getElementById("udidb-app-container")
);

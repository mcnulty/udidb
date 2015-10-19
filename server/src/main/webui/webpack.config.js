module.exports = {
    entry: {
        javascript: "./entry.js",
        html: "./index.html"
    },
    context: __dirname + "/app",

    output: {
        filename: "entry.bundle.js",
        path: __dirname + "/dist"
    },

    module: {
        loaders: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                loaders: ["react-hot", "babel-loader"]
            },
            {
                test: /\.html$/,
                loader: "file?name=[name].[ext]"
            },
            {
                test: /normalize\.css/,
                loader: "file?name=[name].[hash:6].[ext]"
            },
            {
                test: /\.css$/,
                exclude: /normalize\.css/,
                loader: "style-loader!css-loader"
            }
        ]
    }
};

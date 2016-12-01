const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
    entry: [
        "react-hot-loader/patch",
        "webpack-dev-server/client?http://localhost:8080",
        "webpack/hot/only-dev-server",
        "./app/entry.tsx",
    ],
    output: {
        path: path.join(__dirname, 'dist'),
        filename: "bundle.js",
        publicPath: "/static/",
    },

    // Enable sourcemaps for debugging webpack's output.
    devtool: "source-map",

    resolve: {
        // Add '.ts' and '.tsx' as resolvable extensions.
        extensions: ["", ".webpack.js", ".web.js", ".ts", ".tsx", ".js"]
    },

    plugins: [
        new webpack.DefinePlugin({
            'process.env': {
                'NODE_ENV': JSON.stringify('production')
            }
        }),
        new webpack.HotModuleReplacementPlugin(),
        new ExtractTextPlugin("bundle.css", {
            allChunks: true
        })
    ],

    module: {
        preLoaders: [
            {
                test: /.tsx?$/,
                loaders: [
                    'tslint-loader'
                ]
            }
        ],
        loaders: [
            {
                test: /\.tsx?$/,
                loaders: [
                    "react-hot-loader/webpack",
                    "ts-loader"
                ],
                exclude: [
                    path.resolve(__dirname, 'node_modules'),
                    path.resolve(__dirname, 'app/third_party'),
                ],
                include: path.resolve(__dirname, "app"),
            },
            {
                test: /\.css$/,
                loader: ExtractTextPlugin.extract("style-loader", "css-loader")
            },
            {
                test: /\.(woff|woff2)(\?v=\d+\.\d+\.\d+)?$/,
                loader: 'url?limit=10000&mimetype=application/font-woff'
            },
            {
                test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
                loader: 'url?limit=10000&mimetype=application/octet-stream'
            },
            {
                test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
                loader: 'file'
            },
            {
                test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
                loader: 'url?limit=10000&mimetype=image/svg+xml'
            },
            {
                test: /\.json$/,
                loader: "json"
            }
        ],

        preLoaders: [
            // All output '.js' files will have any sourcemaps re-processed by 'source-map-loader'.
            { test: /\.js$/, loader: "source-map-loader" }
        ],

        noParse: [
            /autoit\.js$/
        ]
    },

    tslint: {
        typeCheck: true,
        configFile: false,
        emitErrors: true,
        failOnHint: true
    }
};
